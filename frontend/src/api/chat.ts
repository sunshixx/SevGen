import request from '@/utils/request'
import type { ApiResponse, Chat, CreateChatRequest, ChatRoom, CreateChatRoomRequest, Role } from '@/types'

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
  },

  // ========== 聊天室相关API ==========
  
  // 创建聊天室
  createChatRoom: (data: CreateChatRoomRequest): Promise<ApiResponse<number>> => {
    const params = new URLSearchParams()
    // 后端只接受单个roleId，取第一个角色ID
    if (data.roleIds && data.roleIds.length > 0) {
      params.append('roleId', data.roleIds[0].toString())
    } else {
      // 如果没有选择角色，抛出错误
      throw new Error('请至少选择一个角色')
    }
    if (data.name) {
      params.append('title', data.name)
    }
    return request.post('/chats/chatroom', null, { params })
  },

  // 获取用户的聊天室列表
  getUserChatRooms: (): Promise<ApiResponse<ChatRoom[]>> => {
    return request.get('/chats/chatrooms')
  },

  // 根据ID获取聊天室详情
  getChatRoomById: (chatRoomId: number): Promise<ApiResponse<ChatRoom>> => {
    return request.get(`/chats/chatroom/${chatRoomId}`)
  },

  // 删除聊天室
  deleteChatRoom: (chatRoomId: number): Promise<ApiResponse> => {
    return request.delete(`/chats/chatroom/${chatRoomId}`)
  },

  // 向聊天室添加角色
  addRoleToRoom: (chatRoomId: number, roleId: number): Promise<ApiResponse> => {
    const params = new URLSearchParams()
    params.append('roleId', roleId.toString())
    return request.post(`/chats/chatroom/${chatRoomId}/roles`, null, { params })
  },

  // 从聊天室移除角色
  removeRoleFromRoom: (chatRoomId: number, roleId: number): Promise<ApiResponse> => {
    return request.delete(`/chats/chatroom/${chatRoomId}/roles/${roleId}`)
  },

  // 获取聊天室的角色列表
  getChatRoomRoles: (chatRoomId: number): Promise<ApiResponse<Role[]>> => {
    return request.get(`/chats/chatroom/${chatRoomId}/roles`)
  }
}