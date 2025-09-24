import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/Home.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register', 
      component: () => import('@/views/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/chat/:id',
      name: 'Chat',
      component: () => import('@/views/Chat.vue'),
      meta: { requiresAuth: true },
      props: true
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/Profile.vue'),
      meta: { requiresAuth: true }
    },
    {
      // 404页面
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue')
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()
  
  console.log('路由守卫 - 当前路由:', to.path)
  console.log('路由守卫 - Token存在:', !!authStore.token)
  console.log('路由守卫 - 用户信息存在:', !!authStore.user)
  console.log('路由守卫 - 认证状态:', authStore.isAuthenticated)
  
  // 如果有token但没有用户信息，先尝试获取用户信息
  if (authStore.token && !authStore.user) {
    console.log('路由守卫 - 尝试获取用户信息')
    try {
      const success = await authStore.fetchCurrentUserSilent()
      console.log('路由守卫 - 获取用户信息结果:', success)
    } catch (error) {
      console.error('路由守卫：获取用户信息失败', error)
    }
  }
  
  console.log('路由守卫 - 最终认证状态:', authStore.isAuthenticated)
  
  // 检查路由是否需要认证
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    console.log('路由守卫 - 未认证，跳转到登录页')
    // 未登录，跳转到登录页
    next('/login')
  } else if (!to.meta.requiresAuth && authStore.isAuthenticated) {
    // 已登录访问登录/注册页，跳转到首页
    if (to.name === 'Login' || to.name === 'Register') {
      console.log('路由守卫 - 已认证用户访问登录页，跳转到首页')
      next('/')
    } else {
      next()
    }
  } else {
    console.log('路由守卫 - 正常通过')
    next()
  }
})

export default router