<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { diaryApi, type DiaryInput } from '../api/diary'
import ThemeToggle from '../components/ThemeToggle.vue'
import PinDialog from '../components/PinDialog.vue'

const route = useRoute()
const router = useRouter()

const id = computed(() =>
  route.params.id ? Number(route.params.id) : null,
)
const isEdit = computed(() => id.value !== null)

const form = ref<DiaryInput>({
  title: '',
  content: '',
  diaryDate: today(),
  diaryTime: now(),
})

const loading = ref(false)
const saving = ref(false)
const needPin = ref(false)
const pinError = ref<string | null>(null)

function today() {
  const d = new Date()
  return d.toISOString().slice(0, 10)
}
function now() {
  const d = new Date()
  return d.toTimeString().slice(0, 5)
}

async function load() {
  if (!isEdit.value || id.value === null) return
  loading.value = true
  try {
    const d = await diaryApi.get(id.value)
    form.value = {
      title: d.title,
      content: d.content ?? '',
      diaryDate: d.diaryDate,
      diaryTime: d.diaryTime?.slice(0, 5),
    }
    // 잠긴 일기는 content 가 마스킹되어 옴 → PIN 으로 열람
    if (d.locked) {
      needPin.value = true
    }
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function onPinSubmit(pin: string) {
  if (id.value === null) return
  try {
    const full = await diaryApi.unlock(id.value, pin, false)
    form.value.content = full.content ?? ''
    needPin.value = false
  } catch (e: any) {
    pinError.value = e?.response?.data?.message ?? 'PIN이 올바르지 않습니다.'
  }
}

async function save() {
  if (!form.value.title.trim()) {
    alert('제목을 입력하세요.')
    return
  }
  saving.value = true
  try {
    const payload: DiaryInput = {
      ...form.value,
      diaryTime: form.value.diaryTime.length === 5
        ? form.value.diaryTime + ':00'
        : form.value.diaryTime,
    }
    if (isEdit.value && id.value !== null) {
      await diaryApi.update(id.value, payload)
    } else {
      await diaryApi.create(payload)
    }
    router.push({ name: 'list' })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="page">
    <header class="header">
      <button class="btn btn-ghost" @click="router.push({ name: 'list' })">
        ← 목록
      </button>
      <ThemeToggle />
    </header>

    <main class="container">
      <h2 class="page-title">{{ isEdit ? '일기 수정' : '새 일기' }}</h2>

      <div v-if="loading" class="muted">불러오는 중…</div>

      <div v-else class="form card">
        <div class="row">
          <div class="field">
            <label class="label">날짜</label>
            <input v-model="form.diaryDate" type="date" class="input" />
          </div>
          <div class="field">
            <label class="label">시간</label>
            <input v-model="form.diaryTime" type="time" class="input" />
          </div>
        </div>

        <div class="field">
          <label class="label">제목</label>
          <input
            v-model="form.title"
            class="input"
            placeholder="제목을 입력하세요"
          />
        </div>

        <div class="field">
          <label class="label">내용</label>
          <textarea
            v-model="form.content"
            class="textarea"
            placeholder="오늘 하루를 기록하세요…"
          />
        </div>

        <div class="actions">
          <button class="btn" @click="router.push({ name: 'list' })">취소</button>
          <button class="btn btn-primary" :disabled="saving" @click="save">
            {{ saving ? '저장 중…' : '저장' }}
          </button>
        </div>
      </div>
    </main>

    <PinDialog
      v-if="needPin"
      title="PIN 입력"
      subtitle="잠긴 일기를 수정하려면 PIN을 입력하세요."
      :error="pinError"
      @submit="onPinSubmit"
      @cancel="router.push({ name: 'list' })"
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
}
.container {
  max-width: 720px;
  margin: 0 auto;
  padding: 28px 24px 60px;
}
.page-title {
  margin: 0 0 20px;
  font-size: 20px;
}
.form {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.row {
  display: flex;
  gap: 14px;
}
.row .field {
  flex: 1;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.label {
  font-size: 13px;
  color: var(--text-muted);
  font-weight: 500;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 6px;
}
</style>
