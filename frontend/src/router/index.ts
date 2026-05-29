import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/oauth/callback',
      name: 'oauth-callback',
      component: () => import('../views/OAuthCallbackView.vue'),
    },
    {
      path: '/',
      name: 'list',
      component: () => import('../views/DiaryListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/new',
      name: 'new',
      component: () => import('../views/DiaryEditorView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/diary/:id',
      name: 'edit',
      component: () => import('../views/DiaryEditorView.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated()) {
    return { name: 'login' }
  }
  if (to.name === 'login' && auth.isAuthenticated()) {
    return { name: 'list' }
  }
})

export default router
