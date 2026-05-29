import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useThemeStore } from './stores/theme'
import './styles/main.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)

// 저장된 테마 초기 적용
useThemeStore().init()

app.mount('#app')
