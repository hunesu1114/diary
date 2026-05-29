<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

onMounted(async () => {
  const token = route.query.token as string | undefined
  if (token) {
    auth.setToken(token)
    try {
      await auth.fetchMe()
    } catch {
      /* ignore */
    }
    router.replace({ name: 'list' })
  } else {
    router.replace({ name: 'login' })
  }
})
</script>

<template>
  <div class="wrap">
    <p class="muted">로그인 처리 중…</p>
  </div>
</template>

<style scoped>
.wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
}
</style>
