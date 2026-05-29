// 로그인/백엔드 없이 화면을 미리보기 위한 인메모리 mock 구현.
// `npm run dev:mock` (VITE_MOCK=true) 일 때만 diary.ts 에서 사용된다.
import type { Diary, DiaryInput, Me } from './diary'

const delay = (ms = 250) => new Promise((r) => setTimeout(r, ms))

const me: Me = {
  id: 1,
  nickname: '미리보기',
  email: 'preview@example.com',
  hasPin: false,
}
let storedPin = ''

let seq = 4
// 내부 저장본(실제 content 포함)
let store: Diary[] = [
  {
    id: 1,
    title: '봄 산책',
    content:
      '점심을 먹고 천변을 따라 걸었다. 벚꽃이 거의 다 피어서 바람이 불 때마다 꽃잎이 흩날렸다. 오랜만에 햇볕이 좋아 기분이 가벼웠다.',
    diaryDate: '2026-04-12',
    diaryTime: '14:30:00',
    locked: false,
    createdAt: '2026-04-12T14:31:00Z',
    updatedAt: '2026-04-12T14:31:00Z',
  },
  {
    id: 2,
    title: '비밀 메모',
    content: '이 일기는 잠겨 있습니다. PIN을 입력해야 내용을 볼 수 있어요.',
    diaryDate: '2026-04-10',
    diaryTime: '22:05:00',
    locked: true,
    createdAt: '2026-04-10T22:05:00Z',
    updatedAt: '2026-04-10T22:05:00Z',
  },
  {
    id: 3,
    title: '주말 계획',
    content:
      '토요일 오전엔 청소, 오후엔 도서관. 일요일은 미뤄둔 영화를 몰아서 볼 생각이다.',
    diaryDate: '2026-04-08',
    diaryTime: '09:15:00',
    locked: false,
    createdAt: '2026-04-08T09:15:00Z',
    updatedAt: '2026-04-08T09:15:00Z',
  },
]

// mock 모드에서는 PIN 기본값을 '0000' 으로 미리 설정해 잠금 동작을 바로 체험할 수 있게 한다.
storedPin = '0000'
me.hasPin = true

function clone(d: Diary): Diary {
  return { ...d }
}

// 잠긴 일기는 백엔드처럼 content 를 마스킹해서 반환
function mask(d: Diary): Diary {
  return d.locked ? { ...d, content: null } : clone(d)
}

function sorted(): Diary[] {
  return [...store].sort((a, b) => {
    const byDate = b.diaryDate.localeCompare(a.diaryDate)
    return byDate !== 0 ? byDate : b.diaryTime.localeCompare(a.diaryTime)
  })
}

function find(id: number): Diary {
  const d = store.find((x) => x.id === id)
  if (!d) throw { response: { status: 404, data: { message: '일기를 찾을 수 없습니다.' } } }
  return d
}

function pinError() {
  return { response: { status: 401, data: { message: 'PIN이 올바르지 않습니다.' } } }
}

export const mockDiaryApi = {
  async me(): Promise<Me> {
    await delay()
    return { ...me }
  },

  async setPin(pin: string) {
    await delay()
    storedPin = pin
    me.hasPin = true
  },

  async list(): Promise<Diary[]> {
    await delay()
    return sorted().map(mask)
  },

  async get(id: number): Promise<Diary> {
    await delay()
    return mask(find(id))
  },

  async create(input: DiaryInput): Promise<Diary> {
    await delay()
    const now = '2026-05-29T00:00:00Z'
    const created: Diary = {
      id: seq++,
      ...input,
      content: input.content,
      locked: false,
      createdAt: now,
      updatedAt: now,
    }
    store.push(created)
    return clone(created)
  },

  async update(id: number, input: DiaryInput): Promise<Diary> {
    await delay()
    const d = find(id)
    d.title = input.title
    d.content = input.content
    d.diaryDate = input.diaryDate
    d.diaryTime = input.diaryTime
    d.updatedAt = '2026-05-29T00:00:00Z'
    return clone(d)
  },

  async remove(id: number) {
    await delay()
    store = store.filter((x) => x.id !== id)
  },

  async lock(id: number) {
    await delay()
    if (!me.hasPin) throw { response: { status: 409, data: { message: '먼저 잠금 PIN을 설정하세요.' } } }
    find(id).locked = true
  },

  async unlock(id: number, pin: string, removeLock = false): Promise<Diary> {
    await delay()
    if (pin !== storedPin) throw pinError()
    const d = find(id)
    if (removeLock) d.locked = false
    return clone(d) // 전체 content 포함
  },
}
