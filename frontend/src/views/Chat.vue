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
          <div class="chat-description">{{ getRoleDescription(chat.roleId) }}</div>
          <div class="message-preview">{{ getLastMessage(chat.id) || '开始对话...' }}</div>
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
            <div class="chat-description">{{ getRoleDescription(activeChat.roleId) }}</div>
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
          <div 
            v-for="message in activeMessages" 
            :key="message.id"
            class="message-item"
            :class="{ 'user': message.senderType === 'user', 'ai': message.senderType === 'ai' }"
          >
            <div class="message-avatar">
              <span v-if="message.senderType === 'user'">你</span>
              <span v-else>{{ getRoleName(activeChat.roleId)[0] }}</span>
            </div>
            <div class="message-content">{{ message.content }}</div>
          </div>
          
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
          <div class="chat-description">{{ getRoleDescription(chat.roleId) }}</div>
          <div class="message-preview">{{ getLastMessage(chat.id) || '开始对话...' }}</div>
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
import type { Chat, Message, Role, SendMessageRequest } from '@/types'

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
  
  try {
    console.log('发送语音到后端处理...')
    
    // 创建FormData
    const formData = new FormData()
    const fileName = audioBlob.type === 'audio/wav' ? 'voice-input.wav' : 'voice-input.webm'
    formData.append('audio', audioBlob, fileName)
    
    console.log('发送音频格式:', audioBlob.type, '文件大小:', audioBlob.size, 'bytes')
    
    // 显示处理状态
    isAiReplying.value = true
    
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    const headers: Record<string, string> = {}
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    
    // 发送到后端语音对话接口
    const response = await fetch('/api/voice/chat', {
      method: 'POST',
      headers,
      body: formData
    })
    
    if (response.ok) {
      // 获取AI语音回复
      const audioBuffer = await response.arrayBuffer()
      const audioBlob = new Blob([audioBuffer], { type: 'audio/mpeg' })
      
      // 播放AI语音回复
      const audioUrl = URL.createObjectURL(audioBlob)
      const audio = new Audio(audioUrl)
      
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
  return chatList.value.filter(chat => {
    if (seen.has(chat.roleId)) {
      return false
    }
    seen.add(chat.roleId)
    return true
  })
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
  const roleName = role?.name || '未知角色'
  
  // 添加调试信息
  if (!role) {
    console.log(`获取角色名称失败: roleId=${roleId}, 缓存中不存在该角色`)
    console.log('当前缓存状态:', Array.from(rolesCache.value.entries()))
  }
  
  return roleName
}

// 获取角色描述
const getRoleDescription = (roleId: number): string => {
  const role = rolesCache.value.get(roleId)
  return role?.description || '暂无描述'
}

// 获取最后一条消息
const getLastMessage = (chatId: number): string => {
  const messages = allMessages.value.get(chatId) || []
  const lastMessage = messages[messages.length - 1]
  return lastMessage?.content.slice(0, 30) + (lastMessage?.content.length > 30 ? '...' : '') || ''
}

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

// 发送消息到指定聊天 - 使用同步API，简单可靠
const sendMessageToChat = async (chatId: number, content: string) => {
  try {

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
    
    // 构建请求
    const sendRequest: SendMessageRequest = {
      chatId: chatId,
      content: content
    }
    
    console.log('发送同步消息请求:', sendRequest)
    
    // 使用同步API
    const response = await messageAPI.sendMessage(sendRequest)
    console.log('同步消息发送响应:', response)
    
    if (response.success && response.data) {
      // 获取当前消息列表
      const messages = allMessages.value.get(chatId) || []
      
      // 移除临时用户消息
      const filteredMessages = messages.filter(msg => msg.id !== tempUserMessage.id)
      
      // 添加服务器返回的真实消息
      const updatedMessages = [...filteredMessages]
      
      if (response.data.userMessage) {
        updatedMessages.push(response.data.userMessage)
      }
      
      if (response.data.aiMessage) {
        updatedMessages.push(response.data.aiMessage)
      }
      
      // 更新消息列表
      allMessages.value.set(chatId, updatedMessages)
      
      // 滚动到最新消息
      scrollToBottom()
      
      ElMessage.success('消息发送成功')
    } else {
      // 如果发送失败，移除临时消息
      const messages = allMessages.value.get(chatId) || []
      const filteredMessages = messages.filter(msg => msg.id !== tempUserMessage.id)
      allMessages.value.set(chatId, filteredMessages)
      
      console.error('消息发送失败:', response.message)
      ElMessage.error(response.message || '发送消息失败')
    }
    
  } catch (error: any) {
    // 发送失败时也要移除临时消息
    const messages = allMessages.value.get(chatId) || []
    const filteredMessages = messages.filter(msg => msg.id !== Date.now()) // 简单过滤临时消息
    allMessages.value.set(chatId, filteredMessages)
    
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败，请检查网络连接')
  } finally {
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
)


// 加载聊天列表
const loadChatList = async () => {
  try {
    console.log('开始加载聊天列表...')
    // 先尝试获取用户已有的聊天
    const response = await chatAPI.getUserChats()
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
    console.log(`找到 ${roles.length} 个公开角色:`, roles)
    
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
})


// 加载消息列表
const loadMessages = async (chatId: number) => {
  if (!chatId) return
  
  try {
    // 从API获取聊天消息
    const response = await messageAPI.getChatMessages(chatId)
    if (response.success && response.data) {
      allMessages.value.set(chatId, response.data)
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
})

// 监听activeChatId变化，加载对应消息
watch(activeChatId, (newChatId) => {
  if (newChatId) {
    loadMessages(newChatId)
  }
})
</script>

<style scoped>
.three-column-chat-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  overflow: hidden;
}

/* 侧边栏通用样式 */
.side-column {
  width: 300px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
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
  border-radius: 16px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 120px;
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
  margin-bottom: 12px;
}

.side-card-content {
  flex: 1;

}

.side-card-content .chat-title {
  font-size: 14px;
  font-weight: 600;
  color: white;
  margin-bottom: 6px;
}

.side-card-content .chat-description {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.side-card-content .message-preview {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
}

/* 消息容器 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 20px;
  padding: 10px 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
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
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.message-content {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 12px 16px;
  border-radius: 16px;
  color: white;
  font-size: 14px;
  line-height: 1.5;
  max-width: 70%;
  word-wrap: break-word;
}

.message-item.user .message-content {
  background: rgba(255, 255, 255, 0.25);
}

/* 输入容器 */
.input-container {
  display: flex;
  gap: 12px;
  align-items: center;
}

.message-input {
  flex: 1;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 12px 16px;
  color: white;
  font-size: 14px;
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
  width: 44px;
  height: 44px;
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

/* 响应式设计 */
@media (max-width: 1200px) {
  .side-column {
    width: 250px;
  }
}

@media (max-width: 768px) {
  .three-column-chat-container {
    flex-direction: column;
  }
  
  .side-column {
    width: 100%;
    height: 200px;
    flex-direction: row;
    overflow-x: auto;
    overflow-y: hidden;
    padding: 16px;
  }
  
  .side-chat-card {
    min-width: 200px;
    margin-right: 16px;
  }
  
  .center-column {
    padding: 20px;
  }
  
  .active-chat-card {
    height: 60vh;
  }
}
</style>