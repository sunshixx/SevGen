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
      if (!token.value) return
      
      const response = await authAPI.getCurrentUser()
      if (response.data) {
        user.value = response.data
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果获取失败，清除本地token
      logout()
    }
  }

  // 获取当前用户信息（不自动登出版本，用于路由守卫）
  const fetchCurrentUserSilent = async () => {
    try {
      if (!token.value) return false
      
      const response = await authAPI.getCurrentUser()
      if (response.data) {
        user.value = response.data
        return true
      }
      return false
    } catch (error) {
      console.error('获取用户信息失败:', error)
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