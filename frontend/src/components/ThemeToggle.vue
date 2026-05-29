<script setup lang="ts">
import { useThemeStore } from '../stores/theme'
const theme = useThemeStore()
</script>

<template>
  <button
    class="btn btn-ghost toggle"
    :title="theme.theme === 'dark' ? '라이트 모드' : '다크 모드'"
    @click="theme.toggle()"
  >
    <span v-if="theme.theme === 'dark'">☀️</span>
    <span v-else>🌙</span>
  </button>
</template>

<style scoped>
.toggle {
  font-size: 18px;
  line-height: 1;
  padding: 8px 10px;
}
</style>
