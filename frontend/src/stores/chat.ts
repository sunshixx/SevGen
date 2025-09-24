import { defineStore } from 'pinia'
import { ref } from 'vue'
import { chatAPI, messageAPI } from '@/api'
import { SSEConnection } from '@/api/message'
import type { Chat, Message, CreateChatRequest, SendMessageRequest } from '@/types'

export const useChatStore = defineStore('chat', () => {
  const chats = ref<Chat[]>([])
  const currentChat = ref<Chat | null>(null)
  const messages = ref<Message[]>([])
  const isLoading = ref(false)
  const isSendingMessage = ref(false)
  
  // SSE连接管理
  const sseConnection = ref<SSEConnection | null>(null)

  // 创建聊天会话
  const createChat = async (chatData: CreateChatRequest) => {
    try {
      isLoading.value = true
      const response = await chatAPI.createChat(chatData)
      
      if (response.data) {
        chats.value.unshift(response.data)
        return response.data
      }
    } catch (error) {
      console.error('创建聊天失败:', error)
      return null
    } finally {
      isLoading.value = false
    }
  }

  // 获取用户聊天列表
  const fetchUserChats = async () => {
    try {
      isLoading.value = true
      const response = await chatAPI.getUserActiveChats()
      
      if (response.data) {
        chats.value = response.data
      }
    } catch (error) {
      console.error('获取聊天列表失败:', error)
    } finally {
      isLoading.value = false
    }
  }

  // 设置当前聊天
  const setCurrentChat = async (chatId: number) => {
    const chat = chats.value.find(c => c.id === chatId)
    if (chat) {
      currentChat.value = chat
      await fetchChatMessages(chatId)
      setupSSEConnection(chatId)
    }
  }

  // 获取聊天消息
  const fetchChatMessages = async (chatId: number) => {
    try {
      const response = await messageAPI.getChatMessages(chatId)
      
      if (response.data) {
        messages.value = response.data
      }
    } catch (error) {
      console.error('获取聊天消息失败:', error)
    }
  }

  // 发送消息
  const sendMessage = async (messageData: SendMessageRequest) => {
    try {
      isSendingMessage.value = true
      const response = await messageAPI.sendMessage(messageData)
      
      if (response.data) {
        // 添加用户消息和AI回复到消息列表
        messages.value.push(response.data.userMessage)
        messages.value.push(response.data.aiMessage)
        
        return response.data
      }
    } catch (error) {
      console.error('发送消息失败:', error)
      return null
    } finally {
      isSendingMessage.value = false
    }
  }

  // 异步发送消息（配合SSE）
  const sendMessageAsync = async (messageData: SendMessageRequest) => {
    try {
      isSendingMessage.value = true
      const response = await messageAPI.sendMessageAsync(messageData)
      
      if (response.data) {
        // 只添加用户消息，AI回复通过SSE推送
        messages.value.push(response.data)
        return response.data
      }
    } catch (error) {
      console.error('异步发送消息失败:', error)
      return null
    } finally {
      isSendingMessage.value = false
    }
  }

  // 建立SSE连接
  const setupSSEConnection = (chatId: number) => {
    // 关闭现有连接
    if (sseConnection.value) {
      sseConnection.value.disconnect()
    }

    // 创建新连接
    sseConnection.value = new SSEConnection(chatId)
    sseConnection.value.connect(
      // 收到消息回调
      (message: Message) => {
        messages.value.push(message)
      },
      // 错误回调
      (error: Event) => {
        console.error('SSE连接错误:', error)
      }
    )
  }

  // 关闭SSE连接
  const closeSSEConnection = () => {
    if (sseConnection.value) {
      sseConnection.value.disconnect()
      sseConnection.value = null
    }
  }

  // 删除聊天
  const deleteChat = async (chatId: number) => {
    try {
      await chatAPI.deleteChat(chatId)
      
      // 从列表中移除
      chats.value = chats.value.filter(c => c.id !== chatId)
      
      // 如果删除的是当前聊天，清空状态
      if (currentChat.value?.id === chatId) {
        currentChat.value = null
        messages.value = []
        closeSSEConnection()
      }
      
      return true
    } catch (error) {
      console.error('删除聊天失败:', error)
      return false
    }
  }

  // 标记消息已读
  const markMessagesAsRead = async (chatId: number) => {
    try {
      await messageAPI.markMessagesAsRead(chatId)
    } catch (error) {
      console.error('标记消息已读失败:', error)
    }
  }

  // 清空当前聊天状态
  const clearCurrentChat = () => {
    currentChat.value = null
    messages.value = []
    closeSSEConnection()
  }

  return {
    // state
    chats,
    currentChat,
    messages,
    isLoading,
    isSendingMessage,
    
    // actions
    createChat,
    fetchUserChats,
    setCurrentChat,
    fetchChatMessages,
    sendMessage,
    sendMessageAsync,
    deleteChat,
    markMessagesAsRead,
    clearCurrentChat,
    setupSSEConnection,
    closeSSEConnection
  }
})