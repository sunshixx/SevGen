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
  
  console.log('=== 路由守卫开始 ===')
  console.log('目标路由:', to.path)
  console.log('localStorage token:', !!localStorage.getItem('token'))
  console.log('store token:', !!authStore.token)
  console.log('store user:', !!authStore.user)
  
  // 如果路由不需要认证，直接通过
  if (!to.meta.requiresAuth) {
    console.log('路由无需认证，直接通过')
    // 但如果已登录访问登录/注册页，跳转到首页
    if ((to.name === 'Login' || to.name === 'Register') && authStore.isAuthenticated) {
      console.log('已登录用户访问登录页，跳转首页')
      next('/')
      return
    }
    next()
    return
  }
  
  // 路由需要认证
  console.log('路由需要认证')
  
  // 如果没有token，直接跳转登录
  if (!authStore.token) {
    console.log('无token，跳转登录页')
    next('/login')
    return
  }
  
  // 如果有token但没有用户信息，尝试获取
  if (authStore.token && !authStore.user) {
    console.log('有token无用户信息，尝试获取用户信息')
    try {
      const success = await authStore.fetchCurrentUserSilent()
      console.log('获取用户信息结果:', success)
      
      if (success) {
        console.log('用户信息获取成功，继续路由')
        next()
      } else {
        // 检查token是否还存在，如果存在说明是网络错误
        if (authStore.token) {
          console.log('网络错误，token保留，允许访问（可能离线状态）')
          next()
        } else {
          console.log('token已清除（认证错误），跳转登录')
          next('/login')
        }
      }
    } catch (error) {
      console.error('获取用户信息异常:', error)
      // 同样的逻辑，如果token还在就允许访问
      if (authStore.token) {
        console.log('异常但token保留，允许访问')
        next()
      } else {
        next('/login')
      }
    }
    return
  }
  
  // token和用户信息都存在，正常通过
  console.log('认证完整，正常通过')
  next()
})

export default router