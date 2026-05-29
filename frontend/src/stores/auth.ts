import { defineStore } from 'pinia'
import { ref } from 'vue'
import { diaryApi, isMock, type Me } from '../api/diary'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const me = ref<Me | null>(null)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('token', t)
  }

  // mock 모드: Kakao/백엔드 없이 가짜 세션으로 로그인
  function mockLogin() {
    setToken('mock-token')
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

  return { token, me, isMock, setToken, mockLogin, logout, fetchMe, isAuthenticated }
})
