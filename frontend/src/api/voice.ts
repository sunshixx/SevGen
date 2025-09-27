import request from '@/utils/request'
import type { ApiResponse } from '@/types'

// 语音相关API
export const voiceAPI = {
  // 单角色语音对话
  voiceChat: async (audioFile: File, chatId: number, roleId: number): Promise<ArrayBuffer> => {
    const formData = new FormData()
    formData.append('audio', audioFile)
    formData.append('chatId', chatId.toString())
    formData.append('roleId', roleId.toString())

    const response = await fetch('/api/voice/chat', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: formData
    })

    if (!response.ok) {
      throw new Error(`语音对话失败: ${response.status}`)
    }

    return await response.arrayBuffer()
  },

  // 多角色语音对话
  multiRoleVoiceChat: async (audioFile: File, chatId: number): Promise<Record<string, string>> => {
    const formData = new FormData()
    formData.append('audio', audioFile)
    formData.append('chatId', chatId.toString())

    const response = await request.post('/voice/multi-role-chat', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    return response.data
  },

  // ========== 聊天室语音功能 ==========

  // 聊天室单角色语音对话
  chatroomVoiceChat: async (audioFile: File, chatId: number, roleId: number): Promise<ArrayBuffer> => {
    const formData = new FormData()
    formData.append('audio', audioFile)
    formData.append('chatId', chatId.toString())
    formData.append('roleId', roleId.toString())

    const response = await fetch('/api/voice/chatroom/chat', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: formData
    })

    if (!response.ok) {
      throw new Error(`聊天室语音对话失败: ${response.status}`)
    }

    return await response.arrayBuffer()
  },

  // 聊天室多角色语音对话
  chatroomMultiRoleVoiceChat: async (audioFile: File, chatId: number): Promise<Record<string, string>> => {
    const formData = new FormData()
    formData.append('audio', audioFile)
    formData.append('chatId', chatId.toString())

    const response = await request.post('/voice/chatroom/multi-role-chat', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    return response.data
  },

  // 播放Base64编码的音频
  playBase64Audio: (base64Audio: string, mimeType: string = 'audio/mpeg'): Promise<void> => {
    return new Promise((resolve, reject) => {
      try {
        // 将Base64转换为Blob
        const byteCharacters = atob(base64Audio)
        const byteNumbers = new Array(byteCharacters.length)
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i)
        }
        const byteArray = new Uint8Array(byteNumbers)
        const audioBlob = new Blob([byteArray], { type: mimeType })

        // 创建音频URL并播放
        const audioUrl = URL.createObjectURL(audioBlob)
        const audio = new Audio(audioUrl)

        audio.onended = () => {
          URL.revokeObjectURL(audioUrl)
          resolve()
        }

        audio.onerror = (error) => {
          URL.revokeObjectURL(audioUrl)
          reject(error)
        }

        audio.play()
      } catch (error) {
        reject(error)
      }
    })
  }
}