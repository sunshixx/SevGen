import request from '@/utils/request'
import type {
  ApiResponse,
  LoginRequest,
  RegisterRequest,
  LoginResponse,
  SendVerificationCodeRequest,
  User
} from '@/types'

// 用户认证相关API
export const authAPI = {
  // 用户登录
  login: (data: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
    return request.post('/auth/login', data)
  },

  // 用户注册
  register: (data: RegisterRequest): Promise<ApiResponse<User>> => {
    return request.post('/auth/register', data)
  },

  // 发送验证码
  sendVerificationCode: (data: SendVerificationCodeRequest): Promise<ApiResponse> => {
    return request.post('/auth/send-verification-code', data)
  },

  // 获取当前用户信息
  getCurrentUser: (): Promise<ApiResponse<User>> => {
    return request.get('/auth/me')
  },

  // 用户登出
  logout: (): Promise<ApiResponse> => {
    return request.post('/auth/logout')
  },

  // 健康检查
  health: (): Promise<ApiResponse> => {
    return request.get('/auth/health')
  }
}