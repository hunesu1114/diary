<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { diaryApi, type Diary } from '../api/diary'
import { useAuthStore } from '../stores/auth'
import ThemeToggle from '../components/ThemeToggle.vue'
import PinDialog from '../components/PinDialog.vue'

const router = useRouter()
const auth = useAuthStore()

const diaries = ref<Diary[]>([])
const revealed = ref<Record<number, string>>({}) // id -> content (열람된 잠금 일기)
const loading = ref(true)

// PIN 다이얼로그 상태
type PinMode =
  | { kind: 'set' }
  | { kind: 'view'; id: number }
  | { kind: 'remove'; id: number }
const pinMode = ref<PinMode | null>(null)
const pinError = ref<string | null>(null)

async function load() {
  loading.value = true
  try {
    diaries.value = await diaryApi.list()
    if (!auth.me) await auth.fetchMe()
  } finally {
    loading.value = false
  }
}

onMounted(load)

function newDiary() {
  router.push({ name: 'new' })
}

function openDiary(d: Diary) {
  if (d.locked && !revealed.value[d.id]) return
  router.push({ name: 'edit', params: { id: d.id } })
}

async function remove(d: Diary) {
  if (!confirm(`'${d.title}' 일기를 삭제할까요?`)) return
  await diaryApi.remove(d.id)
  await load()
}

// 잠금 버튼: PIN 없으면 설정 유도, 있으면 바로 잠금
async function lock(d: Diary) {
  if (!auth.me?.hasPin) {
    pinError.value = null
    pinMode.value = { kind: 'set' }
    return
  }
  await diaryApi.lock(d.id)
  delete revealed.value[d.id]
  await load()
}

function askView(d: Diary) {
  pinError.value = null
  pinMode.value = { kind: 'view', id: d.id }
}

function askRemoveLock(d: Diary) {
  pinError.value = null
  pinMode.value = { kind: 'remove', id: d.id }
}

async function onPinSubmit(pin: string) {
  const mode = pinMode.value
  if (!mode) return
  try {
    if (mode.kind === 'set') {
      await diaryApi.setPin(pin)
      await auth.fetchMe()
    } else if (mode.kind === 'view') {
      const full = await diaryApi.unlock(mode.id, pin, false)
      revealed.value[mode.id] = full.content ?? ''
    } else {
      await diaryApi.unlock(mode.id, pin, true)
      await load()
    }
    pinMode.value = null
  } catch (e: any) {
    pinError.value = e?.response?.data?.message ?? 'PIN이 올바르지 않습니다.'
  }
}

function logout() {
  auth.logout()
  router.push({ name: 'login' })
}

function preview(text: string | null) {
  if (!text) return ''
  return text.length > 120 ? text.slice(0, 120) + '…' : text
}
</script>

<template>
  <div class="page">
    <header class="header">
      <div class="brand">단지</div>
      <div class="header-right">
        <span v-if="auth.me" class="muted hello">{{ auth.me.nickname }}님</span>
        <ThemeToggle />
        <button class="btn btn-ghost" @click="logout">로그아웃</button>
      </div>
    </header>

    <main class="container">
      <div class="toolbar">
        <h2 class="page-title">나의 일기</h2>
        <button class="btn btn-primary" @click="newDiary">＋ 새 일기</button>
      </div>

      <p v-if="loading" class="muted">불러오는 중…</p>

      <p v-else-if="diaries.length === 0" class="muted empty">
        아직 작성한 일기가 없어요. 첫 일기를 남겨보세요.
      </p>

      <div v-else class="grid">
        <article
          v-for="d in diaries"
          :key="d.id"
          class="card item"
          :class="{ clickable: !(d.locked && !revealed[d.id]) }"
          @click="openDiary(d)"
        >
          <div class="item-head">
            <h3 class="item-title">
              <span v-if="d.locked" class="lock">🔒</span>{{ d.title }}
            </h3>
            <span class="muted date">{{ d.diaryDate }} {{ d.diaryTime?.slice(0, 5) }}</span>
          </div>

          <p v-if="d.locked && !revealed[d.id]" class="muted locked-note">
            잠긴 일기입니다.
          </p>
          <p v-else class="content">
            {{ preview(revealed[d.id] ?? d.content) }}
          </p>

          <div class="item-actions" @click.stop>
            <template v-if="d.locked && !revealed[d.id]">
              <button class="btn" @click="askView(d)">열기</button>
              <button class="btn btn-ghost" @click="askRemoveLock(d)">잠금 해제</button>
            </template>
            <template v-else>
              <button class="btn btn-ghost" @click="lock(d)" v-if="!d.locked">
                잠금
              </button>
              <button class="btn btn-ghost" @click="askRemoveLock(d)" v-else>
                잠금 해제
              </button>
              <button class="btn btn-danger" @click="remove(d)">삭제</button>
            </template>
          </div>
        </article>
      </div>
    </main>

    <PinDialog
      v-if="pinMode"
      :title="pinMode.kind === 'set' ? '잠금 PIN 설정' : 'PIN 입력'"
      :subtitle="
        pinMode.kind === 'set'
          ? '일기를 잠그려면 먼저 계정 공통 PIN을 설정하세요.'
          : pinMode.kind === 'remove'
            ? '잠금을 해제하려면 PIN을 입력하세요.'
            : '잠긴 일기를 열려면 PIN을 입력하세요.'
      "
      :confirm-label="pinMode.kind === 'set' ? '설정' : '확인'"
      :error="pinError"
      @submit="onPinSubmit"
      @cancel="pinMode = null"
    />
  </div>
</template>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 24px;
  border-bottom: 1px solid var(--border);
  background: var(--surface);
  position: sticky;
  top: 0;
  z-index: 10;
}
.brand {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.5px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.hello {
  font-size: 14px;
}
.container {
  max-width: 860px;
  margin: 0 auto;
  padding: 28px 24px 60px;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-title {
  margin: 0;
  font-size: 20px;
}
.empty {
  padding: 60px 0;
  text-align: center;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}
.item {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.item.clickable {
  cursor: pointer;
  transition: transform 0.08s ease, box-shadow 0.15s ease;
}
.item.clickable:hover {
  transform: translateY(-2px);
}
.item-head {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.item-title {
  margin: 0;
  font-size: 17px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.lock {
  font-size: 14px;
}
.date {
  font-size: 12px;
}
.content {
  margin: 0;
  line-height: 1.55;
  font-size: 14px;
  min-height: 40px;
  white-space: pre-wrap;
}
.locked-note {
  margin: 0;
  min-height: 40px;
  font-size: 14px;
}
.item-actions {
  display: flex;
  gap: 8px;
  margin-top: auto;
  flex-wrap: wrap;
}
.item-actions .btn {
  padding: 6px 12px;
  font-size: 13px;
}
</style>
