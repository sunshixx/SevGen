import request from '@/utils/request'
import type { 
  ApiResponse, 
  Message, 
  SendMessageRequest, 
  SendMessageResponse 
} from '@/types'

// 消息相关API
export const messageAPI = {
  // 发送消息并获取AI回复 - 使用更长超时时间
  sendMessage: (data: SendMessageRequest): Promise<ApiResponse<SendMessageResponse>> => {
    return request.post('/messages', data, { timeout: 90000 }) // 90秒超时
  },

  // 异步发送消息
  sendMessageAsync: (data: SendMessageRequest): Promise<ApiResponse<Message>> => {
    return request.post('/messages/async', data, { timeout: 10000 }) // 异步接口10秒超时
  },

  // 获取聊天消息列表
  getChatMessages: (chatId: number): Promise<ApiResponse<Message[]>> => {
    return request.get(`/messages/chat/${chatId}`)
  },

  // 获取未读消息
  getUnreadMessages: (chatId: number): Promise<ApiResponse<Message[]>> => {
    return request.get(`/messages/chat/${chatId}/unread`)
  },

  // 标记消息为已读
  markMessagesAsRead: (chatId: number): Promise<ApiResponse> => {
    return request.put(`/messages/chat/${chatId}/read`)
  },

  // 创建SSE流式连接 - 基于你的SSE实现
  createStreamConnection: (chatId: number, roleId: number, userMessage: string): EventSource => {
    const token = localStorage.getItem('token')
    const baseURL = import.meta.env.DEV ? 'http://localhost:16999' : ''
    
    // 添加类型检查和转换，确保roleId是有效的数字
    let validRoleId: string
    if (typeof roleId === 'number') {
      validRoleId = roleId.toString()
    } else if (typeof roleId === 'string') {
      // 尝试将字符串转换为数字
      const numRoleId = parseInt(roleId, 10)
      if (!isNaN(numRoleId)) {
        validRoleId = numRoleId.toString()
      } else {
        console.error('无效的roleId:', roleId)
        validRoleId = '0' // 默认值，避免请求失败
      }
    } else {
      console.error('roleId类型错误:', typeof roleId)
      validRoleId = '0' // 默认值
    }
    
    // 构建查询参数
    const params = new URLSearchParams({
      chatId: chatId.toString(),
      roleId: validRoleId,
      userMessage: userMessage
    })
    
    if (token) {
      params.append('token', token)
    }
    
    const url = `${baseURL}/api/sse/stream?${params.toString()}`
    console.log('创建SSE流式连接:', url)
    
    return new EventSource(url)
  }
}

// SSE连接工具
export class SSEConnection {
  private eventSource: EventSource | null = null
  private chatId: number
  private onMessage?: (message: Message) => void
  private onError?: (error: Event) => void

  constructor(chatId: number) {
    this.chatId = chatId
  }

  // 建立SSE连接
  connect(onMessage?: (message: Message) => void, onError?: (error: Event) => void) {
    this.onMessage = onMessage
    this.onError = onError

    const token = localStorage.getItem('token')
    
    // 在开发环境中，EventSource不能直接使用Vite代理，所以需要完整URL
    // 在生产环境中，使用相对路径
    const baseURL = import.meta.env.DEV ? 'http://localhost:16999' : ''
    const url = `${baseURL}/api/sse/subscribe/${this.chatId}`
    
    console.log(`建立SSE连接，URL: ${url}?token=${token?.substring(0, 10)}...`)
    
    this.eventSource = new EventSource(`${url}?token=${token}`)
    
    this.eventSource.onmessage = (event) => {
      console.log('收到SSE消息:', event.data)
      try {
        const message = JSON.parse(event.data) as Message
        this.onMessage?.(message)
      } catch (error) {
        console.error('解析SSE消息失败:', error, event.data)
      }
    }

    this.eventSource.onerror = (event) => {
      console.error('SSE连接错误:', event)
      console.log('EventSource状态:', this.eventSource?.readyState)
      this.onError?.(event)
    }

    this.eventSource.onopen = () => {
      console.log(`SSE连接已建立 - Chat ${this.chatId}`)
    }
  }

  // 关闭连接
  disconnect() {
    if (this.eventSource) {
      this.eventSource.close()
      this.eventSource = null
      console.log(`SSE连接已关闭 - Chat ${this.chatId}`)
    }
  }

  // 检查连接状态
  get isConnected(): boolean {
    return this.eventSource?.readyState === EventSource.OPEN
  }
}