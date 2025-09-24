import { defineStore } from 'pinia'
import { ref } from 'vue'
import { chatAPI, messageAPI } from '@/api'
import type { Chat, Message, CreateChatRequest, SendMessageRequest } from '@/types'

export const useChatStore = defineStore('chat', () => {
  const chats = ref<Chat[]>([])
  const currentChat = ref<Chat | null>(null)
  const messages = ref<Message[]>([])
  const isLoading = ref(false)
  const isSending = ref(false)

  // 获取用户的聊天列表
  const fetchUserChats = async () => {
    try {
      isLoading.value = true
      const response = await chatAPI.getUserChats()
      
      if (response.data) {
        chats.value = response.data
      }
    } catch (error) {
      console.error('获取聊天列表失败:', error)
    } finally {
      isLoading.value = false
    }
  }

  // 获取活跃聊天列表
  const fetchActiveChats = async () => {
    try {
      const response = await chatAPI.getUserActiveChats()
      
      if (response.data) {
        chats.value = response.data
      }
    } catch (error) {
      console.error('获取活跃聊天失败:', error)
    }
  }

  // 创建新聊天
  const createChat = async (data: CreateChatRequest) => {
    try {
      const response = await chatAPI.createChat(data)
      
      if (response.data) {
        chats.value.unshift(response.data)
        return response.data
      }
    } catch (error) {
      console.error('创建聊天失败:', error)
      return null
    }
  }

  // 获取聊天详情
  const getChatById = async (id: number) => {
    try {
      const response = await chatAPI.getChatById(id)
      
      if (response.data) {
        currentChat.value = response.data
        return response.data
      }
    } catch (error) {
      console.error('获取聊天详情失败:', error)
      return null
    }
  }

  // 更新聊天
  const updateChat = async (id: number, data: Partial<Chat>) => {
    try {
      const response = await chatAPI.updateChat(id, data)
      
      if (response.data) {
        const index = chats.value.findIndex(chat => chat.id === id)
        if (index !== -1) {
          chats.value[index] = response.data
        }
        
        if (currentChat.value?.id === id) {
          currentChat.value = response.data
        }
        
        return response.data
      }
    } catch (error) {
      console.error('更新聊天失败:', error)
      return null
    }
  }

  // 删除聊天
  const deleteChat = async (id: number) => {
    try {
      await chatAPI.deleteChat(id)
      
      chats.value = chats.value.filter(chat => chat.id !== id)
      
      if (currentChat.value?.id === id) {
        currentChat.value = null
        messages.value = []
      }
      
      return true
    } catch (error) {
      console.error('删除聊天失败:', error)
      return false
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
  const sendMessage = async (data: SendMessageRequest) => {
    try {
      isSending.value = true
      const response = await messageAPI.sendMessage(data)
      
      if (response.data) {
        // 添加用户消息
        messages.value.push(response.data.userMessage)
        
        // 如果有AI回复，也添加进去
        if (response.data.aiMessage) {
          messages.value.push(response.data.aiMessage)
        }
        
        return response.data
      }
    } catch (error) {
      console.error('发送消息失败:', error)
      return null
    } finally {
      isSending.value = false
    }
  }

  // 添加新消息（用于SSE推送）
  const addMessage = (message: Message) => {
    messages.value.push(message)
  }

  // 标记消息已读
  const markMessagesAsRead = async (chatId: number) => {
    try {
      await messageAPI.markMessagesAsRead(chatId)
      
      // 更新本地消息状态
      messages.value.forEach(message => {
        if (message.chatId === chatId) {
          message.isRead = true
        }
      })
    } catch (error) {
      console.error('标记消息已读失败:', error)
    }
  }

  // 清空当前聊天数据
  const clearCurrentChat = () => {
    currentChat.value = null
    messages.value = []
  }

  // 清空所有数据
  const clearAll = () => {
    chats.value = []
    currentChat.value = null
    messages.value = []
  }

  return {
    // state
    chats,
    currentChat,
    messages,
    isLoading,
    isSending,
    
    // actions
    fetchUserChats,
    fetchActiveChats,
    createChat,
    getChatById,
    updateChat,
    deleteChat,
    fetchChatMessages,
    sendMessage,
    addMessage,
    markMessagesAsRead,
    clearCurrentChat,
    clearAll
  }
})