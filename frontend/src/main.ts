import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores'

const app = createApp(App)
const pinia = createPinia()

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 初始化认证状态后挂载应用
const initApp = async () => {
  const authStore = useAuthStore()
  
  console.log('=== 应用启动初始化 ===')
  console.log('localStorage token:', !!localStorage.getItem('token'))
  console.log('store token:', !!authStore.token)
  
  // 如果localStorage中有token，尝试恢复用户信息
  if (authStore.token) {
    console.log('发现token，尝试恢复用户信息')
    try {
      await authStore.initAuth()
      console.log('用户信息恢复完成，用户:', !!authStore.user)
    } catch (error: any) {
      console.error('用户信息恢复失败:', error)
      // 网络错误不影响应用启动
      if (error.code !== 'ECONNABORTED' && !error.message?.includes('Request aborted')) {
        console.log('非网络错误，可能需要重新登录')
      } else {
        console.log('网络错误，保留token，稍后重试')
      }
    }
  } else {
    console.log('无token，跳过用户信息恢复')
  }
  
  console.log('=== 应用初始化完成，挂载应用 ===')
  app.mount('#app')
}

initApp()