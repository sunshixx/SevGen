import request from '@/utils/request'
import type { ApiResponse, Chat, CreateChatRequest } from '@/types'

// 聊天相关API
export const chatAPI = {
  // 创建聊天会话
  createChat: (data: CreateChatRequest): Promise<ApiResponse<Chat>> => {
    return request.post('/chats', data)
  },

  // 分页获取用户的聊天会话
  getUserChats: (lastUpdatedAt?: string, pageSize: number = 20): Promise<ApiResponse<Chat[]>> => {
    const params: any = { pageSize }
    if (lastUpdatedAt) {
      params.lastUpdatedAt = lastUpdatedAt
    }
    return request.get('/chats/list', { params })
  },

  // 删除聊天会话
  deleteChat: (id: number): Promise<ApiResponse> => {
    return request.delete(`/chats/${id}`)
  }
}