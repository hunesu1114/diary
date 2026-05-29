import client from './client'
import { mockDiaryApi } from './mock'

export interface Diary {
  id: number
  title: string
  content: string | null
  diaryDate: string // YYYY-MM-DD
  diaryTime: string // HH:mm:ss
  locked: boolean
  createdAt: string
  updatedAt: string
}

export interface DiaryInput {
  title: string
  content: string
  diaryDate: string
  diaryTime: string
}

export interface Me {
  id: number
  nickname: string
  email: string | null
  hasPin: boolean
}

const realDiaryApi = {
  me: () => client.get<Me>('/me').then((r) => r.data),
  setPin: (pin: string) => client.post('/me/pin', { pin }),

  list: () => client.get<Diary[]>('/diaries').then((r) => r.data),
  get: (id: number) => client.get<Diary>(`/diaries/${id}`).then((r) => r.data),
  create: (input: DiaryInput) =>
    client.post<Diary>('/diaries', input).then((r) => r.data),
  update: (id: number, input: DiaryInput) =>
    client.put<Diary>(`/diaries/${id}`, input).then((r) => r.data),
  remove: (id: number) => client.delete(`/diaries/${id}`),

  lock: (id: number) => client.post(`/diaries/${id}/lock`),
  unlock: (id: number, pin: string, removeLock = false) =>
    client
      .post<Diary>(`/diaries/${id}/unlock`, { pin, removeLock })
      .then((r) => r.data),
}

export const isMock = import.meta.env.VITE_MOCK === 'true'

// mock 모드면 인메모리 구현, 아니면 실제 백엔드 호출
export const diaryApi = isMock ? mockDiaryApi : realDiaryApi
