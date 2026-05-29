<script setup lang="ts">
import { useRouter } from 'vue-router'
import ThemeToggle from '../components/ThemeToggle.vue'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

// 브라우저가 직접 도달하는 백엔드 origin (compose/dev 모두 localhost:8081 노출)
const backendOrigin =
  import.meta.env.VITE_BACKEND_ORIGIN ?? 'http://localhost:8081'

function loginWithKakao() {
  // mock 모드: Kakao 없이 즉시 로그인해 화면 미리보기
  if (auth.isMock) {
    auth.mockLogin()
    router.push({ name: 'list' })
    return
  }
  window.location.href = `${backendOrigin}/oauth2/authorization/kakao`
}
</script>

<template>
  <div class="page">
    <div class="topbar">
      <ThemeToggle />
    </div>

    <div class="hero">
      <div class="card login-card">
        <h1 class="brand">단지</h1>
        <p class="muted subtitle">오늘 하루를 단정하게 기록하세요.</p>

        <button class="btn kakao" @click="loginWithKakao">
          <span class="kakao-icon">💬</span>
          {{ auth.isMock ? '미리보기로 들어가기' : '카카오로 시작하기' }}
        </button>

        <p v-if="auth.isMock" class="muted small">
          미리보기 모드 · 로그인 없이 둘러봅니다 (잠금 PIN: 0000)
        </p>
        <p v-else class="muted small">로그인하면 24시간 동안 세션이 유지됩니다.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.topbar {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
}
.hero {
  flex: 1;
  display: grid;
  place-items: center;
  padding: 24px;
}
.login-card {
  width: 100%;
  max-width: 380px;
  padding: 40px 32px;
  text-align: center;
}
.brand {
  font-size: 40px;
  margin: 0 0 8px;
  letter-spacing: -1px;
}
.subtitle {
  margin: 0 0 28px;
}
.kakao {
  width: 100%;
  background: #fee500;
  color: #191600;
  border-color: #fee500;
  font-weight: 600;
  padding: 13px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.kakao:hover {
  filter: brightness(0.97);
  background: #fee500;
}
.small {
  font-size: 12px;
  margin: 18px 0 0;
}
</style>
