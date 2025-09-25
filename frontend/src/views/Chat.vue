<template>
  <div class="three-column-chat-container">
    <!-- 左侧角色列表 -->
    <div class="side-column left-column">
      <div
        v-for="chat in leftColumnChats"
        :key="chat.id"
        class="side-chat-card"
        @click="switchToActiveChat(chat.id)"
      >
        <div class="side-card-header">
          <div class="ai-avatar" :style="{ backgroundColor: getRoleColor(chat.roleId) }">
            <span>{{ getRoleName(chat.roleId)[0] || 'A' }}</span>
          </div>
          <button 
            class="delete-chat-btn"
            @click.stop="deleteChat(chat.id)"
            :title="`删除与${getRoleName(chat.roleId)}的对话`"
          >
            ×
          </button>
        </div>
        <div class="side-card-content">
          <div class="chat-title">{{ getRoleName(chat.roleId) }}</div>
          <div v-if="getSafeRoleDescription(chat.roleId)" class="chat-description">{{ getSafeRoleDescription(chat.roleId) }}</div>
          <div class="chat-time">{{ formatRelativeTime(chat.updateTime) }}</div>
        </div>
      </div>
    </div>

    <!-- 中间活跃对话区域 -->
    <div class="center-column">
      <div 
        v-if="activeChat"
        class="active-chat-card"
        :key="activeChat.id"
      >
        <div class="active-card-header">
          <div class="ai-avatar large" :style="{ backgroundColor: getRoleColor(activeChat.roleId) }">
            <span>{{ getRoleName(activeChat.roleId)[0] || 'A' }}</span>

          </div>
          <div class="chat-info">
            <div class="chat-title">{{ getRoleName(activeChat.roleId) }}</div>
            <div v-if="getSafeRoleDescription(activeChat.roleId)" class="chat-description">{{ getSafeRoleDescription(activeChat.roleId) }}</div>
          </div>
          <button 
            class="delete-chat-btn"
            @click.stop="deleteChat(activeChat.id)"
          >
            ×
          </button>
        </div>
        
        <!-- 消息显示区域 -->
        <div class="messages-container" ref="messagesContainer">
          <template v-for="(message, index) in activeMessages" :key="message.id">
            <!-- 时间分隔符 -->
            <div 
              v-if="index === 0 || shouldShowTimeLabel(activeMessages[index - 1]?.sentAt, message.sentAt)"
              class="message-time-divider"
            >
              {{ formatMessageTime(message.sentAt) }}
            </div>
            
            <div 
              class="message-item"
              :class="{ 'user': message.senderType === 'user', 'ai': message.senderType === 'ai' }"
            >
              <div class="message-avatar">
                <span v-if="message.senderType === 'user'">你</span>
                <span v-else>{{ getRoleName(activeChat.roleId)[0] }}</span>
              </div>
              
              <!-- 文本消息 -->
              <div v-if="message.messageType === 'text' || !message.messageType" class="message-content text-message">
                {{ message.content }}
              </div>
            
            <!-- 语音消息 -->
            <div v-else-if="message.messageType === 'voice'" class="message-content voice-message">
              <div class="voice-message-container">
                <!-- 语音播放控制 -->
                <div class="voice-controls">
                  <button 
                    class="play-btn"
                    @click="toggleVoicePlay(message.id, message.audioUrl || '')"
                    :class="{ playing: currentPlayingId === message.id }"
                  >
                    <svg v-if="currentPlayingId !== message.id" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                      <polygon points="5,3 19,12 5,21"></polygon>
                    </svg>
                    <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                      <rect x="6" y="4" width="4" height="16"></rect>
                      <rect x="14" y="4" width="4" height="16"></rect>
                    </svg>
                  </button>
                  
                  <!-- 语音波形/进度显示 -->
                  <div class="voice-progress">
                    <div 
                      class="voice-progress-bar" 
                      :style="{ width: getVoiceProgress(message.id) + '%' }"
                    ></div>
                  </div>
                  <span class="voice-duration">
                    {{ getCurrentTimeDisplay(message.id) }}/{{ getTotalTimeDisplay(message.id, message.audioDuration) }}
                  </span>
                  
                  <!-- 转文字按钮 -->
                  <button 
                    v-if="message.transcribedText || (message.senderType === 'ai' && message.content)" 
                    class="transcript-btn"
                    @click="toggleTranscript(message.id)"
                    :title="showTranscripts.get(message.id) ? '隐藏文字' : '显示文字'"
                  >
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                      <polyline points="14,2 14,8 20,8"></polyline>
                      <line x1="16" y1="13" x2="8" y2="13"></line>
                      <line x1="16" y1="17" x2="8" y2="17"></line>
                      <polyline points="10,9 9,9 8,9"></polyline>
                    </svg>
                  </button>
                </div>
                
                <!-- 转文字内容（可折叠） -->
                <div v-if="message.transcribedText && showTranscripts.get(message.id)" class="voice-transcript">
                  <div class="transcript-content">{{ message.transcribedText }}</div>
                </div>
                
                <!-- AI语音消息的文字内容 -->
                <div v-if="message.senderType === 'ai' && message.content && showTranscripts.get(message.id)" class="voice-transcript ai-content">
                  <div class="transcript-content">{{ message.content }}</div>
                </div>
              </div>
            </div>
            </div>
          </template>
          
          <!-- AI思考状态 -->
          <div v-if="isAiReplying" class="thinking-indicator">
            <div class="thinking-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
            <span class="thinking-text">{{ getRoleName(activeChat.roleId) }}正在思考...</span>
          </div>
        </div>

        <!-- 输入框 -->
        <div class="input-container">
          <input 
            v-model="newMessage"
            placeholder="输入消息..."
            @keydown.enter.prevent="sendMessage"
            class="message-input"
          />
          <!-- 语音输入按钮（预留接口） -->
          <button 
            class="voice-btn" 
            @click="isRecording ? stopVoiceInput() : startVoiceInput()"
            :class="{ active: isRecording }"
            :title="isRecording ? '停止录音' : '语音输入'"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 1a4 4 0 0 0-4 4v6a4 4 0 0 0 8 0V5a4 4 0 0 0-4-4z"></path>
              <path d="M19 10v1a7 7 0 0 1-14 0v-1"></path>
              <line x1="12" y1="19" x2="12" y2="23"></line>
            </svg>
          </button>
          <button class="send-btn" @click="sendMessage" :disabled="!newMessage.trim() || isAiReplying">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13"></line>
              <polygon points="22,2 15,22 11,13 2,9 22,2"></polygon>
            </svg>
          </button>
        </div>
      </div>
      
      <!-- 无活跃对话时的提示 -->
      <div v-else class="no-active-chat">
        <div class="welcome-text">
          <h2>选择一个角色开始对话</h2>
          <p>点击左右两边的角色卡片开始聊天</p>
        </div>
      </div>
    </div>

    <!-- 右侧角色列表 -->
    <div class="side-column right-column">
      <div
        v-for="chat in rightColumnChats"
        :key="chat.id"
        class="side-chat-card"
        @click="switchToActiveChat(chat.id)"
      >
        <div class="side-card-header">
          <div class="ai-avatar" :style="{ backgroundColor: getRoleColor(chat.roleId) }">
            <span>{{ getRoleName(chat.roleId)[0] || 'A' }}</span>
          </div>
          <button 
            class="delete-chat-btn"
            @click.stop="deleteChat(chat.id)"
            :title="`删除与${getRoleName(chat.roleId)}的对话`"
          >
            ×
          </button>
        </div>
        <div class="side-card-content">
          <div class="chat-title">{{ getRoleName(chat.roleId) }}</div>
          <div v-if="getSafeRoleDescription(chat.roleId)" class="chat-description">{{ getSafeRoleDescription(chat.roleId) }}</div>
          <div class="chat-time">{{ formatRelativeTime(chat.updateTime) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { chatAPI, messageAPI, roleAPI } from '@/api'
import { SSEConnection } from '@/api/message'
import type { Chat, Message, Role } from '@/types'
import { formatRelativeTime, formatMessageTime, shouldShowTimeLabel } from '@/utils/dateUtils'

const route = useRoute()
const router = useRouter()

// 响应式数据
const activeChatId = ref<number>(Number(route.params.id))
const chatList = ref<Chat[]>([])
const allMessages = ref<Map<number, Message[]>>(new Map())
const newMessage = ref('')
const isAiReplying = ref(false)

// 消息列表引用，用于滚动控制
const messagesContainer = ref<HTMLElement | null>(null)


// 语音输入状态
const isRecording = ref(false)
const mediaRecorder = ref<MediaRecorder | null>(null)
const audioChunks = ref<Blob[]>([])

// 语音播放状态
const currentPlayingId = ref<number | null>(null)
const showTranscripts = ref<Map<number, boolean>>(new Map())
const audioElements = ref<Map<number, HTMLAudioElement>>(new Map())
const playbackProgress = ref<Map<number, { current: number; duration: number }>>(new Map())
const progressTimer = ref<number | null>(null)

// 自动滚动到消息底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 语音输入开始
const startVoiceInput = async () => {
  try {
    console.log('开始语音录制...')
    
    // 获取麦克风权限
    const stream = await navigator.mediaDevices.getUserMedia({ 
      audio: {
        sampleRate: 16000, // 16kHz采样率，语音识别常用
        channelCount: 1,   // 单声道
        echoCancellation: true,
        noiseSuppression: true
      } 
    })
    
    // 创建录音器 - 优先使用MP3兼容格式
    let options: MediaRecorderOptions = {}
    
    if (MediaRecorder.isTypeSupported('audio/mp4')) {
      options.mimeType = 'audio/mp4' // MP4容器，AAC编码，七牛云兼容
      console.log('使用音频格式: audio/mp4 (AAC编码)')
    } else if (MediaRecorder.isTypeSupported('audio/webm;codecs=opus')) {
      options.mimeType = 'audio/webm;codecs=opus'
      console.log('使用音频格式: audio/webm (Opus编码)')
    } else if (MediaRecorder.isTypeSupported('audio/webm')) {
      options.mimeType = 'audio/webm'
      console.log('使用音频格式: audio/webm (默认编码)')
    } else {
      console.log('使用音频格式: 浏览器默认格式')
    }
    
    mediaRecorder.value = new MediaRecorder(stream, options)
    audioChunks.value = []
    
    // 录音数据收集
    mediaRecorder.value.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunks.value.push(event.data)
      }
    }
    
    // 录音结束处理 - 简化版本
    mediaRecorder.value.onstop = async () => {
      console.log('录音结束，处理音频数据...')
      
      // 获取录音器实际使用的MIME类型
      const mimeType = mediaRecorder.value?.mimeType || 'audio/webm'
      
      // 创建音频Blob，使用录音器的实际格式
      const audioBlob = new Blob(audioChunks.value, { type: mimeType })
      console.log('录制音频格式:', mimeType, '大小:', audioBlob.size, 'bytes')
      
      // 直接发送到后端处理（后端会处理格式转换）
      await processVoiceInput(audioBlob)
      
      // 清理资源
      stream.getTracks().forEach(track => track.stop())
    }
    
    // 开始录音
    mediaRecorder.value.start()
    isRecording.value = true
    
  } catch (error) {
    console.error('语音录制启动失败:', error)
    ElMessage.error('语音录制启动失败，请检查麦克风权限')
  }
}

// 语音输入结束
const stopVoiceInput = () => {
  if (mediaRecorder.value && isRecording.value) {
    console.log('停止语音录制...')
    mediaRecorder.value.stop()
    isRecording.value = false
  }
}

// 处理语音输入
const processVoiceInput = async (audioBlob: Blob) => {
  if (!activeChatId.value) {
    ElMessage.error('请先选择一个对话')
    return
  }
  
  const activeChat = chatList.value.find(chat => chat.id === activeChatId.value)
  if (!activeChat) {
    ElMessage.error('找不到当前对话')
    return
  }
  
  try {
    console.log('发送语音到后端处理...')
    
    // 创建FormData
    const formData = new FormData()
    const fileName = audioBlob.type === 'audio/wav' ? 'voice-input.wav' : 
                     audioBlob.type.startsWith('audio/mp4') ? 'voice-input.mp4' :
                     'voice-input.webm'
    formData.append('audio', audioBlob, fileName)
    formData.append('chatId', activeChatId.value.toString())
    formData.append('roleId', activeChat.roleId.toString())
    
    console.log('发送音频格式:', audioBlob.type, '文件大小:', audioBlob.size, 'bytes')
    
    // 显示处理状态
    isAiReplying.value = true
    
    // 使用相对路径通过Vite代理，避免CORS问题
    const url = '/api/voice/chat'
    
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    const headers: Record<string, string> = {}
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    
    console.log('发送语音请求到:', url)
    
    // 发送到后端语音对话接口 - 通过Vite代理避免跨域问题
    const response = await fetch(url, {
      method: 'POST',
      headers,
      body: formData
    })
    
    console.log('语音API响应状态:', response.status)
    
    if (response.ok) {
      // 获取AI语音回复
      const audioBuffer = await response.arrayBuffer()
      const responseAudioBlob = new Blob([audioBuffer], { type: 'audio/mpeg' })
      
      console.log('收到AI语音回复，大小:', responseAudioBlob.size, 'bytes')
      
      // 播放AI语音回复
      const audioUrl = URL.createObjectURL(responseAudioBlob)
      const audio = new Audio(audioUrl)
      
      audio.onended = () => {
        URL.revokeObjectURL(audioUrl) // 清理内存
        console.log('AI语音回复播放完成')
      }
      
      audio.onerror = (error) => {
        console.error('音频播放失败:', error)
        ElMessage.error('音频播放失败')
        URL.revokeObjectURL(audioUrl)
      }
      
      audio.onended = () => {
        URL.revokeObjectURL(audioUrl) // 清理内存
      }
      
      await audio.play()
      console.log('AI语音回复播放完成')
      
      // 刷新消息列表以显示新的对话内容
      await loadMessages(activeChatId.value)
      
    } else {
      console.error('语音处理失败:', response.status)
      ElMessage.error('语音处理失败，请重试')
    }
    
  } catch (error: any) {
    console.error('语音处理异常:', error)
    ElMessage.error('语音处理失败: ' + (error?.message || '未知错误'))
  } finally {
    isAiReplying.value = false
  }
}

// 语音消息播放控制
const toggleVoicePlay = async (messageId: number, audioUrl: string) => {
  try {
    const isPlaying = currentPlayingId.value === messageId
    
    // 停止当前播放的音频和定时器
    if (currentPlayingId.value !== null) {
      const currentAudio = audioElements.value.get(currentPlayingId.value)
      if (currentAudio) {
        currentAudio.pause()
        currentAudio.currentTime = 0
      }
      currentPlayingId.value = null
      stopProgressTimer()
    }
    
    // 如果点击的是当前播放的消息，则停止播放
    if (isPlaying) {
      return
    }
    
    // 开始播放新的音频
    let audio = audioElements.value.get(messageId)
    if (!audio) {
      // 如果音频还未预加载，创建新的音频元素
      audio = new Audio(audioUrl)
      audioElements.value.set(messageId, audio)
      
      audio.onloadedmetadata = () => {
        // 初始化进度数据
        playbackProgress.value.set(messageId, {
          current: 0,
          duration: audio?.duration || 0
        })
      }
    }
    
    // 设置播放事件（每次播放都需要重新设置）
    audio.onended = () => {
      currentPlayingId.value = null
      stopProgressTimer()
      // 重置进度
      const currentProgress = playbackProgress.value.get(messageId)
      if (currentProgress) {
        playbackProgress.value.set(messageId, {
          current: 0,
          duration: currentProgress.duration
        })
      }
    }
    
    audio.onerror = (error) => {
      console.error('音频播放失败:', error)
      ElMessage.error('音频播放失败')
      currentPlayingId.value = null
      stopProgressTimer()
    }
    
    currentPlayingId.value = messageId
    await audio.play()
    startProgressTimer(messageId)
  } catch (error) {
    console.error('语音播放错误:', error)
    ElMessage.error('语音播放失败')
    currentPlayingId.value = null
    stopProgressTimer()
  }
}

// 开始进度定时器
const startProgressTimer = (messageId: number) => {
  stopProgressTimer() // 确保之前的定时器已停止
  
  progressTimer.value = setInterval(() => {
    const audio = audioElements.value.get(messageId)
    if (audio && !audio.paused) {
      playbackProgress.value.set(messageId, {
        current: audio.currentTime,
        duration: audio.duration || 0
      })
    }
  }, 100) // 每100ms更新一次进度，确保流畅
}

// 停止进度定时器
const stopProgressTimer = () => {
  if (progressTimer.value) {
    clearInterval(progressTimer.value)
    progressTimer.value = null
  }
}

// 获取语音播放进度百分比
const getVoiceProgress = (messageId: number): number => {
  const progress = playbackProgress.value.get(messageId)
  if (!progress || progress.duration === 0) return 0
  return (progress.current / progress.duration) * 100
}

// 获取总时长显示
const getTotalTimeDisplay = (messageId: number, audioDuration?: number): string => {
  const progress = playbackProgress.value.get(messageId)
  // 优先使用音频文件的实际时长，如果没有则使用数据库保存的时长
  if (progress && progress.duration > 0) {
    return formatTime(progress.duration)
  }
  // 如果还没有播放过，使用数据库中保存的时长
  if (audioDuration && audioDuration > 0) {
    return formatTime(audioDuration)
  }
  return '0:00'
}

// 获取当前播放时间显示
const getCurrentTimeDisplay = (messageId: number): string => {
  const progress = playbackProgress.value.get(messageId)
  if (!progress) return '0:00'
  return formatTime(progress.current)
}

// 格式化时间显示
const formatTime = (seconds: number): string => {
  if (!seconds || seconds <= 0) return '0:00'
  const minutes = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${minutes}:${secs.toString().padStart(2, '0')}`
}

// 切换转录文本显示
const toggleTranscript = (messageId: number) => {
  const currentState = showTranscripts.value.get(messageId) || false
  const newState = !currentState
  showTranscripts.value.set(messageId, newState)
  
  // 如果是展开转文字内容，延迟一点时间等DOM更新后滚动到底部
  if (newState) {
    nextTick(() => {
      setTimeout(() => {
        scrollToBottom()
      }, 100) // 给一点时间让转文字内容完全展开
    })
  }
}

// 预加载语音消息元数据
const preloadVoiceMessage = (messageId: number, audioUrl: string, dbDuration: number) => {
  // 先初始化进度数据，使用数据库中的时长
  playbackProgress.value.set(messageId, {
    current: 0,
    duration: dbDuration
  })
  
  // 异步加载音频元数据获取真实时长
  const audio = new Audio(audioUrl)
  audio.preload = 'metadata' // 只预加载元数据，不加载整个音频文件
  
  audio.onloadedmetadata = () => {
    // 更新为真实的音频时长
    playbackProgress.value.set(messageId, {
      current: 0,
      duration: audio.duration
    })
    console.log(`语音消息 ${messageId} 元数据加载完成，时长: ${audio.duration}s`)
  }
  
  audio.onerror = (error) => {
    console.warn(`语音消息 ${messageId} 元数据加载失败:`, error)
    // 保持使用数据库中的时长
  }
  
  // 缓存音频元素以备后续播放使用
  audioElements.value.set(messageId, audio)
}

// 角色数据缓存
const rolesCache = ref<Map<number, Role>>(new Map())

// SSE连接引用 - 暂时保留但不使用
const sseConnection = ref<SSEConnection | null>(null)

// 角色颜色配置
const roleColors = [
  '#7c3aed', '#c084fc', '#a855f7', '#9333ea',
  '#8b5cf6', '#7c3aed', '#6366f1', '#4f46e5'
]

// 获取角色颜色
const getRoleColor = (roleId: number): string => {
  return roleColors[roleId % roleColors.length]
}

// 计算属性
const activeMessages = computed(() => {
  return allMessages.value.get(activeChatId.value) || []
})

// 当前活跃聊天
const activeChat = computed(() => {
  return chatList.value.find(chat => chat.id === activeChatId.value)
})

// 去重处理：确保每个角色只有一个聊天，按角色ID去重
const uniqueChats = computed(() => {
  const seen = new Set<number>()
  const result = chatList.value.filter(chat => {
    if (seen.has(chat.roleId)) {
      return false
    }
    seen.add(chat.roleId)
    return true
  })
  return result
})

// 左列聊天（排除当前活跃的聊天）
const leftColumnChats = computed(() => {
  const nonActiveChats = uniqueChats.value.filter(chat => chat.id !== activeChatId.value)
  const mid = Math.ceil(nonActiveChats.length / 2)
  return nonActiveChats.slice(0, mid)
})

// 右列聊天（排除当前活跃的聊天）
const rightColumnChats = computed(() => {
  const nonActiveChats = uniqueChats.value.filter(chat => chat.id !== activeChatId.value)
  const mid = Math.ceil(nonActiveChats.length / 2)
  return nonActiveChats.slice(mid)
})

// 获取角色名称
const getRoleName = (roleId: number): string => {
  const role = rolesCache.value.get(roleId)
  
  if (!role) {
    return '角色' // 如果角色不在缓存中，返回默认名称
  }
  
  // 确保角色名称不是 undefined、null 或空字符串
  const name = role.name
  if (!name || 
      name === 'undefined' || 
      name === undefined || 
      name === null ||
      name.toString().toLowerCase() === 'undefined' ||
      name.trim() === '') {
    return '角色' // 返回默认名称
  }
  
  return name.trim()
}

// 安全获取角色描述，绝对不显示undefined
const getSafeRoleDescription = (roleId: number): string => {
  try {
    const description = getRoleDescription(roleId)
    
    // 严格检查所有可能的无效值
    if (!description || 
        description === 'undefined' || 
        description === undefined ||
        description === null ||
        description === 'null' ||
        description.toString() === 'undefined' ||
        description.toString() === 'null' ||
        description.toString().toLowerCase().includes('undefined') ||
        description.toString().toLowerCase().includes('null') ||
        description.trim() === '') {
      return ''
    }
    
    const result = description.trim()
    
    // 最终检查结果
    if (result.toLowerCase().includes('undefined') || 
        result.toLowerCase().includes('null') ||
        result === 'undefined' ||
        result === 'null') {
      return ''
    }
    
    return result
  } catch (error) {
    console.warn('获取角色描述时出错:', error)
    return ''
  }
}

// 获取角色描述
const getRoleDescription = (roleId: number): string => {
  const role = rolesCache.value.get(roleId)
  
  if (!role) {
    return '' // 如果角色不在缓存中，返回空字符串
  }
  
  // 确保描述不是 undefined、null 或空字符串，或者字符串 "undefined"
  const description = role.description
  
  if (!description || 
      description === 'undefined' || 
      description === undefined || 
      description === null ||
      description.toString().toLowerCase() === 'undefined' ||
      description.trim() === '') {
    return '' // 返回空字符串
  }
  
  return description.trim()
}

// 获取最后一条消息
// 切换到活动聊天
const switchToActiveChat = (chatId: number) => {
  activeChatId.value = chatId
  router.push(`/chat/${chatId}`)
}

// 发送消息
const sendMessage = async () => {
  if (!newMessage.value.trim() || isAiReplying.value) return
  
  const messageContent = newMessage.value.trim()
  newMessage.value = ''
  
  await sendMessageToChat(activeChatId.value, messageContent)
}

// 发送消息到指定聊天 - 使用SSE流式接口
const sendMessageToChat = async (chatId: number, content: string) => {
  try {
    const chat = chatList.value.find(c => c.id === chatId)
    if (!chat) {
      ElMessage.error('聊天会话不存在')
      return
    }

    isAiReplying.value = true
    
    // 立即显示用户消息（临时消息，避免用户看不到自己发的内容）
    const tempUserMessage: Message = {
      id: Date.now(), // 临时ID
      chatId: chatId,
      content: content,
      senderType: 'user',
      sentAt: new Date().toISOString(),
      isRead: false
    }
    
    // 立即添加到消息列表中显示
    const currentMessages = allMessages.value.get(chatId) || []
    allMessages.value.set(chatId, [...currentMessages, tempUserMessage])
    
    // 自动滚动到底部，让用户看到刚发送的消息
    scrollToBottom()
    
    console.log('开始SSE流式对话:', {
      chatId,
      roleId: chat.roleId,
      content: content.substring(0, 50) + '...'
    })
    
    // 创建SSE连接
    const eventSource = messageAPI.createStreamConnection(chatId, chat.roleId, content)
    
    // 用于累积AI回复的变量
    let aiResponse = ''
    let tempAiMessage: Message | null = null
    
    // 监听SSE事件
    eventSource.onmessage = (event) => {
      try {
        const data = event.data.trim()
        console.log('收到SSE数据:', data)
        
        // 解析数据：你的后端发送的格式是 "data: token"
        if (data.startsWith('data: ')) {
          const token = data.substring(6) // 移除 "data: " 前缀
          
          if (token === '[DONE]') {
            // AI回复完成
            console.log('AI回复完成，累积内容长度:', aiResponse.length)
            
            // 移除临时消息，重新加载完整列表
            
            // 重新加载完整的消息列表（包含数据库中保存的真实消息）
            loadMessages(chatId)
            
            eventSource.close()
            isAiReplying.value = false
            ElMessage.success('消息发送成功')
            
          } else if (token === '[ERROR]') {
            // AI回复出错
            console.error('AI回复出错')
            ElMessage.error('AI回复异常，请重试')
            eventSource.close()
            isAiReplying.value = false
            
          } else {
            // 累积AI回复token
            aiResponse += token
            
            // 创建或更新临时AI消息用于实时显示
            if (!tempAiMessage) {
              tempAiMessage = {
                id: Date.now() + 1, // 确保与用户消息ID不同
                chatId: chatId,
                content: aiResponse,
                senderType: 'ai',
                sentAt: new Date().toISOString(),
                isRead: false
              }
              
              // 添加临时AI消息到列表
              const currentMessages = allMessages.value.get(chatId) || []
              allMessages.value.set(chatId, [...currentMessages, tempAiMessage])
              
            } else {
              // 更新现有临时AI消息的内容
              if (tempAiMessage) {
                tempAiMessage.content = aiResponse
                
                // 触发响应式更新
                const currentMessages = allMessages.value.get(chatId) || []
                const updatedMessages = currentMessages.map(msg => 
                  msg.id === tempAiMessage!.id ? { ...tempAiMessage! } : msg
                )
                allMessages.value.set(chatId, updatedMessages)
              }
            }
            
            // 滚动到最新消息
            scrollToBottom()
          }
        }
        
      } catch (error) {
        console.error('处理SSE消息失败:', error, event.data)
      }
    }
    
    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error)
      ElMessage.error('连接异常，请重试')
      eventSource.close()
      
      // 移除临时消息
      const messages = allMessages.value.get(chatId) || []
      const filteredMessages = messages.filter(msg => 
        msg.id !== tempUserMessage.id && 
        (tempAiMessage ? msg.id !== tempAiMessage.id : true)
      )
      allMessages.value.set(chatId, filteredMessages)
      
      isAiReplying.value = false
    }
    
    eventSource.onopen = () => {
      console.log('SSE连接已建立')
    }
    
  } catch (error: any) {
    // 发送失败时移除临时消息
    const messages = allMessages.value.get(chatId) || []
    const filteredMessages = messages.filter(msg => msg.id !== Date.now())
    allMessages.value.set(chatId, filteredMessages)
    
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败，请检查网络连接')
    isAiReplying.value = false
  }
}

// 删除聊天对话
const deleteChat = async (chatId: number) => {
  try {
    // 获取要删除的聊天信息
    const chatToDelete = chatList.value.find(chat => chat.id === chatId)
    const roleName = chatToDelete ? getRoleName(chatToDelete.roleId) : '角色'
    
    // 确认删除
    await ElMessageBox.confirm(
      `确定要删除与${roleName}的对话吗？此操作无法撤销。`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    // 调用后端API删除聊天
    await chatAPI.deleteChat(chatId)
    
    // 从列表中移除
    chatList.value = chatList.value.filter(chat => chat.id !== chatId)
    
    // 清除该聊天的消息
    allMessages.value.delete(chatId)
    
    // 如果删除的是当前活跃聊天，切换到第一个聊天
    if (activeChatId.value === chatId) {
      if (chatList.value.length > 0) {
        activeChatId.value = chatList.value[0].id
        router.push(`/chat/${chatList.value[0].id}`)
      } else {
        // 如果没有聊天了，可以导航到聊天列表页面或创建新聊天页面
        router.push('/chat')
      }
    }
    
    ElMessage.success('对话删除成功')
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除聊天失败:', error)
      ElMessage.error('删除对话失败')
    }
  }
}


// 加载聊天列表
const loadChatList = async () => {
  try {
    console.log('开始加载聊天列表...')
    // 先尝试获取用户已有的聊天（使用分页接口，pageSize设大一些获取所有聊天）
    const response = await chatAPI.getUserChats(undefined, 100)  // 获取前100个聊天
    console.log('聊天列表API响应:', response)
    
    if (response.success && response.data && response.data.length > 0) {
      console.log(`找到 ${response.data.length} 个现有聊天记录`)
      
      // 为现有聊天加载角色信息 - 等待所有角色加载完成
      const roleLoadPromises = response.data.map(async (chat) => {
        if (!rolesCache.value.has(chat.roleId)) {
          const role = await loadRole(chat.roleId)
          console.log(`角色加载结果: roleId=${chat.roleId}, role=`, role)
          return role
        }
        return rolesCache.value.get(chat.roleId)
      })
      
      // 等待所有角色信息加载完成
      await Promise.all(roleLoadPromises)
      console.log('所有角色信息加载完成，当前缓存:', Array.from(rolesCache.value.entries()))
      
      // 设置聊天列表（只有在角色信息都准备好后才显示）
      chatList.value = response.data
      console.log(`成功加载 ${chatList.value.length} 个聊天记录`)
      
      // 检查是否需要为其他角色创建聊天
      await ensureAllRoleChats()
      
    } else {
      console.log('没有现有聊天记录，创建基于角色的聊天...')
      await createChatsFromRoles()
    }
  } catch (error) {
    console.error('加载聊天列表失败:', error)
    // 如果API调用失败，尝试基于角色创建聊天
    await createChatsFromRoles()
  }
}

// 基于角色创建聊天对话
const createChatsFromRoles = async () => {
  try {
    console.log('开始获取公开角色列表...')
    // 获取公开角色列表
    const rolesResponse = await roleAPI.getAllPublicRoles()
    console.log('角色列表API响应:', rolesResponse)
    
    if (!rolesResponse.success || !rolesResponse.data) {
      console.log('获取角色列表失败:', rolesResponse.message)
      ElMessage.warning('暂无可用角色')
      return
    }
    
    const roles = rolesResponse.data // 移除限制，显示所有角色
    console.log(`从API获取到 ${roles.length} 个公开角色`)
    
    // 先缓存所有角色信息
    roles.forEach(role => {
      rolesCache.value.set(role.id, role)
      console.log(`缓存角色: ID=${role.id}, Name=${role.name}`)
    })

    // 为每个角色创建一个聊天对话
    const createdChats: Chat[] = []

    for (const role of roles) {
      try {
        console.log(`正在为角色 ${role.name} (ID: ${role.id}) 创建聊天...`)
        const createChatResponse = await chatAPI.createChat({
          roleId: role.id,
          title: `与${role.name}的对话`
        })
        
        if (createChatResponse.success && createChatResponse.data) {
          createdChats.push(createChatResponse.data)
          console.log(`成功创建聊天:`, createChatResponse.data)
        } else {
          console.error(`创建聊天失败:`, createChatResponse.message)
        }
      } catch (error) {
        console.error(`创建与${role.name}的聊天失败:`, error)
      }
    }
    
    chatList.value = createdChats
    console.log(`成功创建 ${createdChats.length} 个聊天对话`)
    console.log('最终角色缓存内容:', Array.from(rolesCache.value.entries()))
    
  } catch (error) {
    console.error('基于角色创建聊天失败:', error)
    ElMessage.error('加载角色数据失败')
  }
}

// 确保所有角色都有对应的聊天记录
const ensureAllRoleChats = async () => {
  try {
    console.log('=== 开始检查所有角色的聊天记录 ===')
    
    // 获取所有公开角色
    const rolesResponse = await roleAPI.getAllPublicRoles()
    if (!rolesResponse.success || !rolesResponse.data) {
      console.log('获取角色列表失败:', rolesResponse.message)
      return
    }
    
    const allRoles = rolesResponse.data
    console.log(`数据库中共有 ${allRoles.length} 个公开角色`)
    
    // 缓存所有角色信息
    allRoles.forEach(role => {
      rolesCache.value.set(role.id, role)
    })
    
    // 检查哪些角色没有聊天记录
    const existingRoleIds = new Set(chatList.value.map(chat => chat.roleId))
    const missingRoles = allRoles.filter(role => !existingRoleIds.has(role.id))
    
    console.log(`已有聊天的角色ID: [${Array.from(existingRoleIds).join(', ')}]`)
    console.log(`缺失聊天的角色: ${missingRoles.length} 个`, missingRoles.map(r => `${r.name}(ID:${r.id})`))
    
    // 为缺失的角色创建聊天记录
    for (const role of missingRoles) {
      try {
        console.log(`为角色 ${role.name} (ID: ${role.id}) 创建聊天...`)
        const createChatResponse = await chatAPI.createChat({
          roleId: role.id,
          title: `与${role.name}的对话`
        })
        
        if (createChatResponse.success && createChatResponse.data) {
          chatList.value.push(createChatResponse.data)
          console.log(`成功为角色 ${role.name} 创建聊天`)
        } else {
          console.error(`为角色 ${role.name} 创建聊天失败:`, createChatResponse.message)
        }
      } catch (error) {
        console.error(`为角色 ${role.name} 创建聊天时出错:`, error)
      }
    }
    
    console.log(`=== 聊天检查完成，最终聊天数量: ${chatList.value.length} ===`)
    
  } catch (error) {
    console.error('确保角色聊天记录时出错:', error)
  }
}

// 加载角色信息
const loadRole = async (roleId: number) => {
  try {
    if (rolesCache.value.has(roleId)) {
      return rolesCache.value.get(roleId)
    }
    
    // 从API获取角色信息
    const response = await roleAPI.getRoleById(roleId)
    
    if (response.success && response.data) {
      rolesCache.value.set(roleId, response.data)
      console.log(`角色加载成功: ID=${roleId}, Name=${response.data.name}`)
      return response.data
    } else {
      console.error(`获取角色信息失败: ID=${roleId}, Message=${response.message}`)
      return null
    }
  } catch (error) {
    console.error(`加载角色信息失败: ID=${roleId}`, error)
    return null
  }
}


// 加载消息列表
const loadMessages = async (chatId: number) => {
  if (!chatId) return
  
  try {
    // 从API获取聊天消息（使用分页接口，pageSize设大一些获取所有消息）
    const response = await messageAPI.getChatMessages(chatId, undefined, 100)  // 获取前100条消息
    if (response.success && response.data) {
      const messages = response.data.data || []  // 从分页响应中取出消息数组
      allMessages.value.set(chatId, messages)
      
      // 预加载语音消息的音频元数据
      messages.forEach((message: Message) => {
        if (message.messageType === 'voice' && message.audioUrl) {
          preloadVoiceMessage(message.id, message.audioUrl, message.audioDuration || 0)
        }
      })
      
      // 加载完消息后滚动到底部
      scrollToBottom()
    } else {
      console.log(`聊天 ${chatId} 暂无消息历史`)
      allMessages.value.set(chatId, [])
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)
    // 设置空消息列表，避免重复请求
    allMessages.value.set(chatId, [])
  }
}

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId) => {
    const chatId = Number(newId)
    if (chatId && chatId !== activeChatId.value) {
      activeChatId.value = chatId
      loadMessages(chatId)
    }
  }
)

// 组件挂载时初始化
onMounted(async () => {
  console.log('Chat组件挂载，开始加载数据...')
  await loadChatList()
  
  console.log('聊天列表加载完成，当前聊天数量:', chatList.value.length)
  console.log('角色缓存状态:', Array.from(rolesCache.value.entries()))
  
  if (activeChatId.value) {
    console.log('加载活跃聊天消息:', activeChatId.value)
    await loadMessages(activeChatId.value)
  }
})

// 组件卸载时清理资源
onUnmounted(() => {
  if (sseConnection.value) {
    sseConnection.value.disconnect()
    console.log('SSE连接已清理')
  }
  
  // 清理语音播放定时器
  stopProgressTimer()
  
  // 清理所有音频元素
  audioElements.value.forEach(audio => {
    audio.pause()
    audio.src = ''
  })
  audioElements.value.clear()
  playbackProgress.value.clear()
  
  console.log('语音播放资源已清理')
})

// 监听activeChatId变化，加载对应消息
watch(activeChatId, (newChatId) => {
  if (newChatId) {
    loadMessages(newChatId)
  }
})
</script>

<style scoped>
/* 全局隐藏任何包含 undefined 的文本 */
*:not(script):not(style) {
  font-size: inherit;
}

/* 隐藏任何可能显示 undefined 的文本内容 */
.chat-description:empty,
.chat-description:contains("undefined"),
*[data-undefined],
*:contains("undefined") {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  height: 0 !important;
  margin: 0 !important;
  padding: 0 !important;
}

.three-column-chat-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  overflow: hidden;
  font-size: clamp(12px, 1vw, 16px); /* 动态字体大小 */
}

/* 侧边栏通用样式 */
.side-column {
  width: clamp(250px, 20vw, 320px); /* 响应式宽度 */
  padding: clamp(15px, 1.5vw, 25px);
  display: flex;
  flex-direction: column;
  gap: clamp(12px, 1.2vw, 18px);
  overflow-y: auto;
}

.left-column {
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.right-column {
  border-left: 1px solid rgba(255, 255, 255, 0.1);
}

/* 侧边聊天卡片 */
.side-chat-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: clamp(12px, 1.2vw, 18px);
  padding: clamp(12px, 1.2vw, 18px);
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: clamp(100px, 8vw, 130px);
}

.side-chat-card:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.side-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: clamp(8px, 0.8vw, 14px);
}

.side-card-content {
  flex: 1;
}

.side-card-content .chat-title {
  font-size: clamp(13px, 1.1vw, 15px);
  font-weight: 600;
  color: white;
  margin-bottom: clamp(4px, 0.4vw, 8px);
}

.side-card-content .chat-description {
  font-size: clamp(11px, 0.9vw, 13px);
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: clamp(6px, 0.6vw, 10px);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 0;
}

.side-card-content .chat-description:empty {
  display: none; /* 完全隐藏空的描述元素 */
}

.side-card-content .chat-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 4px;
  text-align: right;
  font-weight: 400;
}

/* 中间活跃区域 */
.center-column {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;

}

.active-chat-card {
  width: 100%;
  max-width: 600px;
  height: 80vh;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(30px);
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 24px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.3);
  animation: slideIn 0.5s ease-out;
}


@keyframes slideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.active-card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-info {
  flex: 1;
}

.chat-info .chat-title {
  font-size: 20px;
  font-weight: 700;
  color: white;
  margin-bottom: 4px;
}

.chat-info .chat-description {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  min-height: 0; /* 允许空内容时不占用高度 */
}

.chat-info .chat-description:empty {
  display: none; /* 完全隐藏空的描述元素 */
}

/* 消息容器 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  margin-bottom: clamp(16px, 1.6vw, 24px);
  padding: clamp(8px, 0.8vw, 12px) 0;
  display: flex;
  flex-direction: column;
  gap: clamp(12px, 1.2vw, 20px);
}

/* 消息时间分隔符 */
.message-time-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: clamp(12px, 1.2vw, 20px) 0 clamp(6px, 0.6vw, 10px);
  position: relative;
}

.message-time-divider::before {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  margin-right: clamp(8px, 0.8vw, 14px);
}

.message-time-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  margin-left: clamp(8px, 0.8vw, 14px);
}

.message-time-divider {
  font-size: clamp(10px, 0.9vw, 13px);
  color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.1);
  padding: clamp(3px, 0.3vw, 5px) clamp(8px, 0.8vw, 14px);
  border-radius: clamp(10px, 1vw, 16px);
  align-self: center;
  backdrop-filter: blur(5px);
}

.message-item {
  display: flex;
  gap: clamp(8px, 0.8vw, 14px);
  align-items: flex-start;
}

.message-item.ai {
  align-self: flex-start;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: clamp(32px, 2.8vw, 40px);
  height: clamp(32px, 2.8vw, 40px);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: clamp(10px, 0.9vw, 13px);
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.message-content {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: clamp(8px, 0.8vw, 14px) clamp(12px, 1.2vw, 18px);
  border-radius: clamp(12px, 1.2vw, 20px);
  color: white;
  font-size: clamp(12px, 1.1vw, 15px);
  line-height: 1.5;
  max-width: min(70%, 600px);
  word-wrap: break-word;
}

.message-item.user .message-content {
  background: rgba(255, 255, 255, 0.25);
}

/* 输入容器 */
.input-container {
  display: flex;
  gap: clamp(8px, 0.8vw, 14px);
  align-items: center;
}

.message-input {
  flex: 1;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: clamp(12px, 1.2vw, 18px);
  padding: clamp(8px, 0.8vw, 14px) clamp(12px, 1.2vw, 18px);
  color: white;
  font-size: clamp(12px, 1.1vw, 15px);
  outline: none;
  transition: all 0.3s ease;
}

.message-input::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.message-input:focus {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
}


.send-btn, .voice-btn {
  width: clamp(40px, 3.2vw, 48px);
  height: clamp(40px, 3.2vw, 48px);
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.voice-btn {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);

}

.voice-btn.active {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}


.send-btn:hover:not(:disabled), .voice-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 无活跃聊天状态 */
.no-active-chat {
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
}

.welcome-text h2 {
  font-size: 24px;
  margin-bottom: 12px;
  color: white;
}

.welcome-text p {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
}

/* AI头像 */
.ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
}

.ai-avatar.large {
  width: 48px;
  height: 48px;
  font-size: 18px;
}

/* 删除按钮 */
.delete-chat-btn {
  width: 24px;
  height: 24px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.delete-chat-btn:hover {
  background: rgba(255, 0, 0, 0.2);
  color: #ff6b6b;
  transform: scale(1.1);
}

/* 思考指示器 */
.thinking-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.thinking-dots {
  display: flex;
  gap: 4px;
}

.thinking-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  animation: bounce 1.4s infinite ease-in-out both;
}

.thinking-dots span:nth-child(1) { animation-delay: -0.32s; }
.thinking-dots span:nth-child(2) { animation-delay: -0.16s; }
.thinking-dots span:nth-child(3) { animation-delay: 0s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}

/* 语音消息样式 */
.voice-message {
  max-width: 100%;
}

.voice-message-container {
  min-width: clamp(160px, 20vw, 200px);
  max-width: clamp(240px, 30vw, 320px);
  padding: clamp(3px, 0.3vw, 5px);
}

.voice-controls {
  display: flex;
  align-items: center;
  gap: clamp(6px, 0.6vw, 10px);
  margin-bottom: clamp(4px, 0.4vw, 8px);
}

.play-btn {
  width: clamp(28px, 2.4vw, 36px);
  height: clamp(28px, 2.4vw, 36px);
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.play-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  transform: scale(1.05);
}

.play-btn.playing {
  background: rgba(124, 58, 237, 0.9);
  color: #fff;
}

.voice-progress {
  flex: 1;
  height: 3px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 2px;
  overflow: hidden;
  margin: 0 4px;
}

.voice-progress-bar {
  height: 100%;
  background: rgba(124, 58, 237, 0.8);
  border-radius: 2px;
  transition: width 0.1s ease;
  width: 0%;
}

.voice-duration {
  font-size: clamp(9px, 0.9vw, 12px);
  color: rgba(255, 255, 255, 0.6);
  min-width: clamp(40px, 4vw, 50px);
  text-align: right;
  flex-shrink: 0;
  font-family: 'Times New Roman', serif;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.transcript-btn {
  width: clamp(20px, 2vw, 26px);
  height: clamp(20px, 2vw, 26px);
  border-radius: clamp(3px, 0.3vw, 5px);
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.transcript-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.8);
  transform: scale(1.05);
}

.voice-transcript {
  margin-top: clamp(6px, 0.6vw, 10px);
  padding: clamp(6px, 0.6vw, 10px) clamp(8px, 0.8vw, 12px);
  background: rgba(255, 255, 255, 0.08);
  border-radius: clamp(6px, 0.6vw, 10px);
  border-left: clamp(2px, 0.2vw, 4px) solid rgba(124, 58, 237, 0.6);
  font-size: clamp(11px, 1vw, 14px);
  line-height: 1.5;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.transcript-content {
  color: rgba(255, 255, 255, 0.9);
  word-wrap: break-word;
  word-break: break-word;
  white-space: pre-wrap;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.voice-transcript.ai-content {
  border-left-color: rgba(59, 130, 246, 0.7);
  background: rgba(59, 130, 246, 0.05);
}

/* 用户语音转录样式 */
.voice-transcript:not(.ai-content) {
  border-left-color: rgba(124, 58, 237, 0.7);
  background: rgba(124, 58, 237, 0.05);
}

/* 转文字内容前的标签 */
.voice-transcript:not(.ai-content)::before {
  content: "转录文字";
  font-size: clamp(9px, 0.9vw, 12px);
  color: rgba(124, 58, 237, 0.8);
  font-weight: 500;
  display: block;
  margin-bottom: clamp(2px, 0.2vw, 5px);
}

.voice-transcript.ai-content::before {
  content: "AI回复文字";
  font-size: clamp(9px, 0.9vw, 12px);
  color: rgba(59, 130, 246, 0.8);
  font-weight: 500;
  display: block;
  margin-bottom: clamp(2px, 0.2vw, 5px);
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .side-column {
    width: clamp(220px, 18vw, 280px);
  }
}

@media (max-width: 1024px) {
  .side-column {
    width: clamp(200px, 20vw, 250px);
  }
  
  .message-content {
    max-width: min(80%, 500px);
  }
}

@media (max-width: 768px) {
  .three-column-chat-container {
    flex-direction: column;
  }
  
  .side-column {
    width: 100%;
    height: clamp(180px, 25vh, 220px);
    flex-direction: row;
    overflow-x: auto;
    overflow-y: hidden;
    padding: clamp(12px, 1.5vw, 18px);
  }
  
  .side-chat-card {
    min-width: clamp(180px, 40vw, 220px);
    margin-right: clamp(12px, 1.5vw, 18px);
  }
  
  .center-column {
    padding: clamp(16px, 2.5vw, 24px);
  }
  
  .active-chat-card {
    height: clamp(50vh, 60vh, 70vh);
  }
  
  .message-content {
    max-width: min(90%, 400px);
    font-size: clamp(13px, 3.5vw, 16px);
  }
}
</style>