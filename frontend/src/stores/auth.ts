import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import { authAPI } from '@/api'
import type { User, LoginRequest, RegisterRequest, SendVerificationCodeRequest } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string>(localStorage.getItem('token') || '')
  const isLoading = ref(false)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const userInfo = computed(() => user.value)

  // 登录
  const login = async (loginData: LoginRequest) => {
    try {
      isLoading.value = true
      const response = await authAPI.login(loginData)
      
      if (response.data) {
        token.value = response.data.token
        user.value = response.data.user  // 修改为 user
        
        // 保存token到localStorage
        localStorage.setItem('token', response.data.token)
        
        return { success: true, data: response.data }
      }
      
      return { success: false, message: '登录失败' }
    } catch (error: any) {
      return { success: false, message: error.message }
    } finally {
      isLoading.value = false
    }
  }

  // 注册
  const register = async (registerData: RegisterRequest) => {
    try {
      isLoading.value = true
      const response = await authAPI.register(registerData)
      
      return { success: true, data: response.data }
    } catch (error: any) {
      return { success: false, message: error.message }
    } finally {
      isLoading.value = false
    }
  }

  // 发送验证码
  const sendVerificationCode = async (emailData: SendVerificationCodeRequest) => {
    try {
      isLoading.value = true
      await authAPI.sendVerificationCode(emailData)
      
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.message }
    } finally {
      isLoading.value = false
    }
  }

  // 获取当前用户信息
  const fetchCurrentUser = async () => {
    try {
      console.log('fetchCurrentUser - 开始获取用户信息')
      console.log('fetchCurrentUser - token存在:', !!token.value)
      console.log('fetchCurrentUser - token值:', token.value ? `${token.value.substring(0, 20)}...` : 'null')
      
      if (!token.value) {
        console.log('fetchCurrentUser - 无token，跳过')
        return
      }
      
      const response = await authAPI.getCurrentUser()
      console.log('fetchCurrentUser - 响应成功:', response)

      if (response.data) {
        user.value = response.data
        console.log('fetchCurrentUser - 用户信息设置成功:', response.data.username)
      }
    } catch (error: any) {
      console.error('fetchCurrentUser - 获取用户信息失败:', error)
      console.error('fetchCurrentUser - 错误类型:', error.name)
      console.error('fetchCurrentUser - 错误代码:', error.code)
      console.error('fetchCurrentUser - 错误消息:', error.message)
      
      // 如果是401错误，说明token无效，清除并跳转
      if (error.message?.includes('未登录') || error.message?.includes('401')) {
        console.log('fetchCurrentUser - Token无效，清除认证信息并跳转登录页')
        user.value = null
        token.value = ''
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
    }
  }

  // 获取当前用户信息（不自动登出版本，用于路由守卫）
  const fetchCurrentUserSilent = async () => {
    try {
      console.log('fetchCurrentUserSilent - 开始静默获取用户信息')
      console.log('fetchCurrentUserSilent - token存在:', !!token.value)
      
      if (!token.value) {
        console.log('fetchCurrentUserSilent - 无token，返回false')
        return false
      }
      
      const response = await authAPI.getCurrentUser()
      console.log('fetchCurrentUserSilent - 响应成功:', response)

      if (response.data) {
        user.value = response.data
        console.log('fetchCurrentUserSilent - 用户信息设置成功，返回true')
        return true
      }
      
      console.log('fetchCurrentUserSilent - 响应无数据，返回false')
      return false
    } catch (error: any) {
      console.error('fetchCurrentUserSilent - 静默获取用户信息失败:', error)
      console.error('fetchCurrentUserSilent - 错误类型:', error.name)
      console.error('fetchCurrentUserSilent - 错误代码:', error.code)
      console.error('fetchCurrentUserSilent - 错误消息:', error.message)
      
      // 区分网络错误和认证错误
      if (error.code === 'ECONNABORTED' || error.message?.includes('Request aborted') || error.code === 'NETWORK_ERROR') {
        console.log('fetchCurrentUserSilent - 网络错误，保留token，不清除认证信息')
        // 网络错误，不清除token，让用户重试
        return false
      }
      
      // 只有在明确的认证错误时才清除token
      if (error.message?.includes('未登录') || error.message?.includes('401')) {
        console.log('fetchCurrentUserSilent - 认证错误，清除token')
        user.value = null
        token.value = ''
        localStorage.removeItem('token')
      } else {
        console.log('fetchCurrentUserSilent - 其他错误，保留token')
      }
      
      return false
    }
  }

  // 登出
  const logout = async () => {
    try {
      await authAPI.logout()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      // 无论请求是否成功，都清除本地数据
      user.value = null
      token.value = ''
      localStorage.removeItem('token')
    }
  }

  // 初始化用户状态
  const initAuth = async () => {
    if (token.value) {
      await fetchCurrentUser()
    }
  }

  return {
    // state
    user: readonly(user),
    token: readonly(token),
    isLoading: readonly(isLoading),
    
    // getters
    isAuthenticated,
    userInfo,
    
    // actions
    login,
    register,
    sendVerificationCode,
    fetchCurrentUser,
    fetchCurrentUserSilent,
    logout,
    initAuth
  }
})