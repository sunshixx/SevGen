import request from '@/utils/request'
import type { ApiResponse, Chat, CreateChatRequest } from '@/types'

// 聊天相关API
export const chatAPI = {
  // 创建聊天会话
  createChat: (data: CreateChatRequest): Promise<ApiResponse<Chat>> => {
    return request.post('/chats', data)
  },

  // 获取用户的所有聊天会话
  getUserChats: (): Promise<ApiResponse<Chat[]>> => {
    return request.get('/chats')
  },

  // 获取用户的活跃聊天会话
  getUserActiveChats: (): Promise<ApiResponse<Chat[]>> => {
    return request.get('/chats/active')
  },

  // 根据ID获取聊天会话详情
  getChatById: (id: number): Promise<ApiResponse<Chat>> => {
    return request.get(`/chats/${id}`)
  },

  // 更新聊天会话
  updateChat: (id: number, data: Partial<Chat>): Promise<ApiResponse<Chat>> => {
    return request.put(`/chats/${id}`, data)
  },

  // 停用聊天会话
  deactivateChat: (id: number): Promise<ApiResponse> => {
    return request.put(`/chats/${id}/deactivate`)
  },

  // 删除聊天会话
  deleteChat: (id: number): Promise<ApiResponse> => {
    return request.delete(`/chats/${id}`)
  }
}