import request from '@/utils/request'
import type { 
  ApiResponse, 
  Message, 
  SendMessageRequest, 
  SendMessageResponse 
} from '@/types'

// 消息相关API
export const messageAPI = {
  // 发送消息并获取AI回复
  sendMessage: (data: SendMessageRequest): Promise<ApiResponse<SendMessageResponse>> => {
    return request.post('/messages', data)
  },

  // 异步发送消息
  sendMessageAsync: (data: SendMessageRequest): Promise<ApiResponse<Message>> => {
    return request.post('/messages/async', data)
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