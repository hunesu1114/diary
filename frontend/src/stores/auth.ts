import { defineStore } from 'pinia'
import { ref } from 'vue'
import { diaryApi, type Me } from '../api/diary'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const me = ref<Me | null>(null)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('token', t)
  }

  function logout() {
    token.value = null
    me.value = null
    localStorage.removeItem('token')
  }

  async function fetchMe() {
    me.value = await diaryApi.me()
    return me.value
  }

  const isAuthenticated = () => !!token.value

  return { token, me, setToken, logout, fetchMe, isAuthenticated }
})
