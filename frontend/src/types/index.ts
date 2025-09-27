// API响应通用结构
export interface ApiResponse<T = any> {
  success: boolean
  message: string
  data?: T
  code?: number
}

// 分页响应结构
export interface PagedResponse<T> {
  data: T[]
  nextCursor?: number | string | null
  hasMore: boolean
}

// 用户相关类型
export interface User {
  id: number
  username: string
  email: string
  avatar?: string
  active: boolean
  createTime?: string
  updateTime?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
  verificationCode: string
}

export interface LoginResponse {
  token: string
  user: User
}

export interface SendVerificationCodeRequest {
  email: string
}

// 角色相关类型
export interface Role {
  id: number
  name: string
  description: string
  characterPrompt: string
  avatar?: string
  category: string
  isPublic: boolean
  createTime?: string
  updateTime?: string
}

// 聊天相关类型
export interface Chat {
  id: number
  userId: number
  roleId: number
  title: string
  isActive: boolean
  createTime?: string
  updateTime?: string
}

export interface CreateChatRequest {
  roleId: number
  title?: string
}

// 聊天室相关类型
export interface ChatRoom {
  id: number
  userId: number
  name: string
  description?: string
  isActive: boolean
  createTime?: string
  updateTime?: string
  roles?: Role[]
}

export interface CreateChatRoomRequest {
  name: string
  description?: string
  roleIds: number[]
}

// 消息相关类型
export interface Message {
  id: number
  chatId: number
  roleId?: number
  senderType: 'user' | 'ai'
  content: string
  messageType?: 'text' | 'voice'
  audioUrl?: string
  audioDuration?: number
  transcribedText?: string
  isRead: boolean
  sentAt?: string
}

export interface SendMessageRequest {
  chatId: number
  content: string
}

export interface SendMessageResponse {
  userMessage: Message
  aiMessage: Message
}

// 路由参数类型
export interface ChatParams {
  id: string
}