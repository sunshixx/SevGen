import request from '@/utils/request'
import type { ApiResponse, PagedResponse } from '@/types'

// 聊天室相关类型定义
export interface ChatRoom {
  id: number
  chatRoomId: string | number
  name?: string
  title?: string
  description?: string
  isActive: boolean
  participantCount?: number
  createTime?: string
  updateTime?: string
  userId?: number
  roleId?: number
  joinOrder?: number
  deleted?: number
}

export interface CreateChatroomRequest {
  title: string
  description?: string
}

export interface CollaborativeMessageRequest {
  chatRoomId: string
  message: string
  roleIds: number[]
}

export interface CollaborativeMessageResponse {
  userMessage: {
    id: number
    content: string
    sentAt: string
  }
  aiMessages: Array<{
    id: number
    roleId: number
    content: string
    sentAt: string
  }>
}

// 聊天室API接口
export const chatroomAPI = {
  // 获取聊天室列表（包含参与人数和活跃状态）
  getChatroomList(): Promise<ApiResponse<ChatRoom[]>> {
    return request.get('/chatrooms/list')
  },

  // 获取聊天室列表（原有方法，保持兼容性）
  getChatrooms(): Promise<ApiResponse<ChatRoom[]>> {
    return request.get('/chatrooms')
  },

  // 获取聊天室信息
  getChatroomInfo(chatRoomId: string | number): Promise<ApiResponse<ChatRoom>> {
    return request.get(`/chatrooms/${chatRoomId}`)
  },

  // 创建聊天室
  createChatroom(data: CreateChatroomRequest): Promise<ApiResponse<ChatRoom>> {
    return request.post('/chatrooms', data)
  },

  // 删除聊天室
  deleteChatroom(chatRoomId: string | number): Promise<ApiResponse<void>> {
    return request.delete(`/chatrooms/room/${chatRoomId}`)
  },

  // 发送协作消息 (使用SSE)
  sendCollaborativeMessage(data: CollaborativeMessageRequest): Promise<ApiResponse<CollaborativeMessageResponse>> {
    return request.post('/sse/collaborate', {
      chatRoomId: data.chatRoomId,
      message: data.message,
      roleIds: data.roleIds.join(',')
    })
  },

  // 获取聊天室消息历史
  getChatroomMessages(chatRoomId: string | number, cursor?: string): Promise<ApiResponse<PagedResponse<any>>> {
    const params = cursor ? { cursor } : {}
    return request.get(`/chatrooms/${chatRoomId}/messages`, { params })
  },

  // 加入聊天室
  joinChatroom(chatRoomId: string | number): Promise<ApiResponse<void>> {
    return request.post(`/chatrooms/${chatRoomId}/join`)
  },

  // 离开聊天室
  leaveChatroom(chatRoomId: string | number): Promise<ApiResponse<void>> {
    return request.post(`/chatrooms/${chatRoomId}/leave`)
  },

  // 获取聊天室参与者
  getChatroomParticipants(chatRoomId: string | number): Promise<ApiResponse<any[]>> {
    return request.get(`/chatrooms/${chatRoomId}/participants`)
  },

  // 添加角色到聊天室
  addRoleToChatroom(chatRoomId: string | number, roleId: number, userId: number): Promise<ApiResponse<ChatRoom>> {
    return request.post(`/chatrooms/${chatRoomId}/roles/${roleId}?userId=${userId}`)
  },

  // 获取聊天室的角色列表
  getChatroomRoles(chatRoomId: string | number): Promise<ApiResponse<ChatRoom[]>> {
    return request.get(`/chatrooms/${chatRoomId}/roles`)
  },

  // 获取聊天室中特定角色的记录
  getChatroomRoleRecord(chatRoomId: string | number, roleId: number): Promise<ApiResponse<ChatRoom>> {
    return request.get(`/chatrooms/${chatRoomId}/role/${roleId}`)
  },

  // 从聊天室删除角色
  removeRoleFromChatroom(recordId: number): Promise<ApiResponse<void>> {
    return request.delete(`/chatrooms/${recordId}`)

  },

  // 创建聊天室协作流式连接
  createCollaborativeStreamConnection(chatRoomId: string | number, userMessage: string, roleIds?: number[]): EventSource {
    const token = localStorage.getItem('token')
    
    // 在开发环境中，EventSource不能直接使用Vite代理，所以需要完整URL
    const baseURL = import.meta.env.DEV ? 'http://localhost:16999' : ''
    
    // 构建查询参数
    const params = new URLSearchParams({
      chatRoomId: chatRoomId.toString(),
      userMessage: userMessage
    })
    
    // 添加角色ID参数
    if (roleIds && roleIds.length > 0) {
      params.append('roleIds', roleIds.join(','))
    }
    
    if (token) {
      params.append('token', token)
    }
    
    const url = `${baseURL}/api/sse/collaborate?${params.toString()}`
    console.log('创建聊天室协作SSE流式连接:', url)
    
    return new EventSource(url)

  }
}

export default chatroomAPI