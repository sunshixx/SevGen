import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores'
import Home from '@/views/Home.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import Chat from '@/views/Chat.vue'
import ChatRoom from '@/views/ChatRoom.vue'
import ChatRoomList from '@/views/ChatRoomList.vue'
import Profile from '@/views/Profile.vue'
import NotFound from '@/views/NotFound.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home
    },
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/chat/:id',
      name: 'Chat',
      component: Chat,
      props: true
    },
    {
      path: '/chatrooms',
      name: 'ChatRoomList',
      component: ChatRoomList,
      meta: { requiresAuth: true }
    },
    {
      path: '/chatroom/:id',
      name: 'ChatRoom',
      component: ChatRoom,
      props: true,
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: Profile,
      meta: { requiresAuth: true }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: NotFound
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