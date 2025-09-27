<template>
  <div class="chatroom-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="back-button">
          <el-button @click="goBack" icon="ArrowLeft" circle />
        </div>
        <div class="chatroom-info">
          <h2 v-if="currentChatRoom">{{ currentChatRoom.name }}</h2>
          <span v-if="currentChatRoom" class="chatroom-description">
            {{ currentChatRoom.description }}
          </span>
        </div>
        <div class="chatroom-actions">
          <el-button @click="showRoleManagement = true" type="primary" icon="User">
            管理角色
          </el-button>
        </div>
      </div>
    </header>

    <!-- 聊天区域 -->
    <div class="chat-area">
      <!-- 消息列表 -->
        <div class="messages-container" ref="messagesContainer" @scroll="handleScroll">
         <!-- 加载更多按钮 -->
         <div v-if="hasMoreMessages" class="load-more-container">
          <el-button 
            @click="loadMoreMessages" 
            :loading="isLoadingMore"
            type="text"
            size="small"
          >
            {{ isLoadingMore ? '加载中...' : '加载更多消息' }}
          </el-button>
        </div>
        
        <div v-if="messages.length === 0" class="empty-messages">
          <el-empty description="还没有消息，开始聊天吧！">
            <template #image>
              <div class="empty-icon">💬</div>
            </template>
          </el-empty>
        </div>
        
        <div
          v-for="message in messages"
          :key="message.id"
          class="message-item"
          :class="{ 'user-message': message.senderType === 'user' }"
        >
          <div class="message-avatar">
            <el-avatar 
              :size="40" 
              :src="getMessageAvatar(message)"
            >
              {{ getMessageInitial(message) }}
            </el-avatar>
          </div>
          
          <div class="message-content">
            <div class="message-header">
              <span class="sender-name">{{ getMessageSenderName(message) }}</span>
              <span class="message-time">{{ formatTime(message.sentAt || '') }}</span>
            </div>
            
            <div class="message-body">
              <div v-if="message.messageType === 'text'" class="text-message">
                {{ message.content }}
              </div>
              
              <div v-else-if="message.messageType === 'voice'" class="audio-message">
                <el-button 
                  @click="playAudio(message.audioUrl!)" 
                  icon="VideoPlay"
                  type="primary"
                  size="small"
                >
                  播放语音
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-container">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keydown.ctrl.enter="sendMessage"
            resize="none"
          />
          
          <div class="input-actions">
            <el-button @click="startVoiceInput" icon="Microphone" circle />
            <el-button @click="sendMessage" type="primary" icon="Position">
              发送 (Ctrl+Enter)
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 角色管理对话框 -->
    <el-dialog
      v-model="showRoleManagement"
      title="管理聊天室角色"
      width="600px"
    >
      <div class="role-management">
        <!-- 当前角色列表 -->
        <div class="current-roles">
          <h4>当前角色</h4>
          <div class="roles-list">
            <div
              v-for="role in chatRoomRoles"
              :key="role.id"
              class="role-item"
            >
              <el-avatar :size="40" :src="role.avatar">
                {{ role.name[0] }}
              </el-avatar>
              <div class="role-info">
                <span class="role-name">{{ role.name }}</span>
                <span class="role-description">{{ role.description }}</span>
              </div>
              <el-button
                @click="removeRoleFromRoom(role.id)"
                type="danger"
                size="small"
                icon="Delete"
                circle
              />
            </div>
          </div>
        </div>

        <!-- 添加角色 -->
        <div class="add-roles">
          <h4>添加角色</h4>
          <el-select
            v-model="selectedRoleToAdd"
            placeholder="选择要添加的角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in availableRoles"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            >
              <div class="role-option">
                <el-avatar :size="30" :src="role.avatar">
                  {{ role.name[0] }}
                </el-avatar>
                <span>{{ role.name }}</span>
              </div>
            </el-option>
          </el-select>
          
          <el-button
            @click="addRoleToRoom"
            type="primary"
            :disabled="!selectedRoleToAdd"
            style="margin-top: 10px; width: 100%"
          >
            添加角色
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 语音输入对话框 -->
    <el-dialog
      v-model="showVoiceInput"
      title="语音输入"
      width="400px"
    >
      <div class="voice-input">
        <div class="recording-status">
          <div v-if="isRecording" class="recording-indicator">
            <div class="pulse-dot"></div>
            <span>正在录音...</span>
          </div>
          <div v-else class="ready-indicator">
            <el-icon size="48"><Microphone /></el-icon>
            <span>点击开始录音</span>
          </div>
        </div>
        
        <div class="voice-actions">
          <el-button
            v-if="!isRecording"
            @click="startRecording"
            type="primary"
            icon="Microphone"
          >
            开始录音
          </el-button>
          <el-button
            v-else
            @click="stopRecording"
            type="danger"
            icon="VideoPause"
          >
            停止录音
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, 
  User, 
  VideoPlay, 
  Microphone, 
  Position, 
  Delete,
  VideoPause
} from '@element-plus/icons-vue'
import { chatAPI, messageAPI } from '@/api'
import { voiceAPI } from '@/api/voice'
import { useAuthStore, useRoleStore } from '@/stores'
import type { ChatRoom, Message, Role } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const roleStore = useRoleStore()

// 聊天室数据
const currentChatRoom = ref<ChatRoom | null>(null)
const messages = ref<Message[]>([])
const chatRoomRoles = ref<Role[]>([])

// 懒加载相关
const hasMoreMessages = ref(true)
const isLoadingMore = ref(false)
const oldestMessageId = ref<number | undefined>(undefined)

// 输入相关
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement>()

// 角色管理
const showRoleManagement = ref(false)
const selectedRoleToAdd = ref<number | null>(null)

// 语音输入
const showVoiceInput = ref(false)
const isRecording = ref(false)
const mediaRecorder = ref<MediaRecorder | null>(null)
const audioChunks = ref<Blob[]>([])

// 计算属性
const availableRoles = computed(() => {
  const currentRoleIds = chatRoomRoles.value.map(r => r.id)
  return roleStore.roles.filter(role => !currentRoleIds.includes(role.id))
})

// 页面初始化
onMounted(async () => {
  const chatRoomId = Number(route.params.id)
  if (chatRoomId) {
    await loadChatRoom(chatRoomId)
    await loadMessages(chatRoomId, 5) // 只加载最近5条消息
    await loadChatRoomRoles(chatRoomId)
  }
  
  // 确保角色数据已加载 - 只在需要时加载
  // 聊天室页面主要需要聊天室内的角色，不需要加载所有角色
  // if (roleStore.roles.length === 0) {
  //   await roleStore.fetchRoles()
  // }
})

// 加载聊天室信息
const loadChatRoom = async (chatRoomId: number) => {
  try {
    const response = await chatAPI.getChatRoomById(chatRoomId)
    currentChatRoom.value = response.data || null
  } catch (error) {
    console.error('加载聊天室信息失败:', error)
    ElMessage.error('加载聊天室信息失败')
  }
}

// 加载消息
const loadMessages = async (chatRoomId: number, pageSize: number = 10) => {
  try {
    const response = await messageAPI.getChatRoomMessages(chatRoomId, undefined, pageSize)
    const messageData = Array.isArray(response.data) ? response.data : response.data?.data || []
    messages.value = messageData
    
    // 设置最旧消息ID和是否还有更多消息
    if (messageData.length > 0) {
      oldestMessageId.value = messageData[0].id
      hasMoreMessages.value = messageData.length === pageSize
    } else {
      hasMoreMessages.value = false
    }
    
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  }
}

// 加载更多消息
const loadMoreMessages = async () => {
  if (!currentChatRoom.value || isLoadingMore.value || !hasMoreMessages.value) return
  
  isLoadingMore.value = true
  try {
    const response = await messageAPI.getChatRoomMessages(
      currentChatRoom.value.id, 
      oldestMessageId.value, 
      10
    )
    const newMessages = Array.isArray(response.data) ? response.data : response.data?.data || []
    
    if (newMessages.length > 0) {
      // 将新消息添加到现有消息的前面
      messages.value = [...newMessages, ...messages.value]
      oldestMessageId.value = newMessages[0].id
      hasMoreMessages.value = newMessages.length === 10
    } else {
      hasMoreMessages.value = false
    }
  } catch (error) {
    console.error('加载更多消息失败:', error)
    ElMessage.error('加载更多消息失败')
  } finally {
    isLoadingMore.value = false
  }
}

// 处理滚动事件
const handleScroll = (event: Event) => {
  const container = event.target as HTMLElement
  // 当滚动到顶部附近时自动加载更多
  if (container.scrollTop < 100 && hasMoreMessages.value && !isLoadingMore.value) {
    loadMoreMessages()
  }
}

// 加载聊天室角色
const loadChatRoomRoles = async (chatRoomId: number) => {
  try {
    const response = await chatAPI.getChatRoomRoles(chatRoomId)
    chatRoomRoles.value = response.data || []
  } catch (error) {
    console.error('加载聊天室角色失败:', error)
    ElMessage.error('加载聊天室角色失败')
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentChatRoom.value) return
  
  const messageContent = inputMessage.value.trim()
  inputMessage.value = ''
  
  try {
    // 立即显示用户消息
    const userMessage: Message = {
      id: Date.now(),
      chatId: currentChatRoom.value.id,
      content: messageContent,
      senderType: 'user' as const,
      sentAt: new Date().toISOString(),
      isRead: false
    }
    messages.value.push(userMessage)
    
    // 使用SSE协作接口发送消息
    const eventSource = messageAPI.createChatroomCollaborationConnection(
      currentChatRoom.value.id, 
      messageContent
    )
    
    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        console.log('收到SSE消息:', data)
        
        if (data.type === 'ROLE_RESPONSE') {
          // 添加角色回复消息
          const aiMessage: Message = {
            id: Date.now() + Math.random(),
            chatId: currentChatRoom.value!.id,
            roleId: data.roleId,
            content: data.content,
            senderType: 'ai' as const,
            sentAt: new Date().toISOString(),
            isRead: false
          }
          messages.value.push(aiMessage)
        } else if (data.type === 'COLLABORATION_END') {
          // 协作完成，关闭连接
          eventSource.close()
          // 重新加载消息以确保数据同步 - 只加载最新的几条
          if (currentChatRoom.value) {
            loadMessages(currentChatRoom.value.id, 5)
          }
        } else if (data.type === 'ERROR' || data.type === 'COLLABORATION_ERROR') {
          ElMessage.error(data.message || '发送消息失败')
          eventSource.close()
        }
      } catch (error) {
        console.error('解析SSE消息失败:', error)
      }
    }
    
    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error)
      ElMessage.error('消息发送失败')
      eventSource.close()
    }
    
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  }
}

// 添加角色到聊天室
const addRoleToRoom = async () => {
  if (!selectedRoleToAdd.value || !currentChatRoom.value) return
  
  try {
    await chatAPI.addRoleToRoom(currentChatRoom.value.id, selectedRoleToAdd.value)
    await loadChatRoomRoles(currentChatRoom.value.id)
    selectedRoleToAdd.value = null
    ElMessage.success('角色添加成功')
  } catch (error) {
    console.error('添加角色失败:', error)
    ElMessage.error('添加角色失败')
  }
}

// 从聊天室移除角色
const removeRoleFromRoom = async (roleId: number) => {
  if (!currentChatRoom.value) return
  
  try {
    await ElMessageBox.confirm('确定要移除这个角色吗？', '确认操作', {
      type: 'warning'
    })
    
    await chatAPI.removeRoleFromRoom(currentChatRoom.value.id, roleId)
    await loadChatRoomRoles(currentChatRoom.value.id)
    ElMessage.success('角色移除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除角色失败:', error)
      ElMessage.error('移除角色失败')
    }
  }
}

// 语音输入相关
const startVoiceInput = () => {
  showVoiceInput.value = true
}

const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder.value = new MediaRecorder(stream)
    audioChunks.value = []
    
    mediaRecorder.value.ondataavailable = (event) => {
      audioChunks.value.push(event.data)
    }
    
    mediaRecorder.value.onstop = async () => {
      const audioBlob = new Blob(audioChunks.value, { type: 'audio/wav' })
      await processVoiceInput(audioBlob)
    }
    
    mediaRecorder.value.start()
    isRecording.value = true
  } catch (error) {
    console.error('开始录音失败:', error)
    ElMessage.error('无法访问麦克风')
  }
}

const stopRecording = () => {
  if (mediaRecorder.value && isRecording.value) {
    mediaRecorder.value.stop()
    isRecording.value = false
    showVoiceInput.value = false
  }
}

const processVoiceInput = async (audioBlob: Blob) => {
  if (!currentChatRoom.value) return
  
  try {
    const audioFile = new File([audioBlob], 'voice.wav', { type: 'audio/wav' })
    const response = await voiceAPI.chatroomMultiRoleVoiceChat(audioFile, currentChatRoom.value.id)
    
    // 播放返回的音频
    for (const [roleName, base64Audio] of Object.entries(response)) {
      await voiceAPI.playBase64Audio(base64Audio)
    }
    
    // 重新加载消息 - 只加载最新的几条
    await loadMessages(currentChatRoom.value.id, 5)
  } catch (error) {
    console.error('语音处理失败:', error)
    ElMessage.error('语音处理失败')
  }
}

// 工具函数
const goBack = () => {
  router.push('/chatrooms')
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const getMessageAvatar = (message: Message) => {
  if (message.senderType === 'user') {
    return authStore.userInfo?.avatar
  }
  // 根据角色ID查找角色头像
  const role = chatRoomRoles.value.find(r => r.id === message.roleId)
  return role?.avatar
}

const getMessageInitial = (message: Message) => {
  if (message.senderType === 'user') {
    return authStore.userInfo?.username?.[0]?.toUpperCase() || 'U'
  }
  const role = chatRoomRoles.value.find(r => r.id === message.roleId)
  return role?.name?.[0] || 'A'
}

const getMessageSenderName = (message: Message) => {
  if (message.senderType === 'user') {
    return authStore.userInfo?.username || '用户'
  }
  const role = chatRoomRoles.value.find(r => r.id === message.roleId)
  return role?.name || 'AI助手'
}

const formatTime = (timestamp: string) => {
  return new Date(timestamp).toLocaleTimeString()
}

const playAudio = async (audioUrl: string) => {
  try {
    const audio = new Audio(audioUrl)
    await audio.play()
  } catch (error) {
    console.error('播放音频失败:', error)
    ElMessage.error('播放音频失败')
  }
}
</script>

<style scoped>
.chatroom-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 1rem 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.chatroom-info {
  flex: 1;
}

.chatroom-info h2 {
  margin: 0;
  color: #333;
  font-size: 1.5rem;
}

.chatroom-description {
  color: #666;
  font-size: 0.9rem;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 0 2rem;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.empty-messages {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.message-item {
  display: flex;
  gap: 0.75rem;
  max-width: 70%;
}

.message-item.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-content {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 1rem;
  padding: 0.75rem 1rem;
  backdrop-filter: blur(10px);
}

.user-message .message-content {
  background: rgba(103, 126, 234, 0.9);
  color: white;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.8rem;
}

.sender-name {
  font-weight: 600;
}

.message-time {
  opacity: 0.7;
}

.input-area {
  padding: 1rem 0;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1rem 1rem 0 0;
  backdrop-filter: blur(10px);
}

.input-container {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
}

.input-container :deep(.el-textarea) {
  flex: 1;
}

.input-actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.role-management {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.roles-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.role-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid #eee;
  border-radius: 0.5rem;
}

.role-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.role-name {
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.role-description {
  color: #666;
  font-size: 0.9rem;
}

.role-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.voice-input {
  text-align: center;
  padding: 2rem;
}

.recording-status {
  margin-bottom: 2rem;
}

.recording-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.pulse-dot {
  width: 20px;
  height: 20px;
  background: #f56565;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(245, 101, 101, 0.7);
  }
  
  70% {
    transform: scale(1);
    box-shadow: 0 0 0 10px rgba(245, 101, 101, 0);
  }
  
  100% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(245, 101, 101, 0);
  }
}

.ready-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  color: #666;
}

/* 加载更多按钮样式 */
.load-more-container {
  text-align: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 10px;
}

.load-more-container .el-button {
  color: #666;
  font-size: 12px;
}

.load-more-container .el-button:hover {
  color: #409eff;
}
</style>