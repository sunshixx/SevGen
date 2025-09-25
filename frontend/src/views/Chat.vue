<template>
  <div class="chat-container">
    <!-- 左侧对话列表 -->
    <aside class="chat-sidebar">
      <div class="sidebar-header">
        <h3>我的对话</h3>
        <el-button 
          type="primary" 
          size="small" 
          @click="goHome"
        >
          新建对话
        </el-button>
      </div>
      
      <div class="chat-list">
        <div
          v-for="chat in chatList"
          :key="chat.id"
          class="chat-item"
          :class="{ active: currentChatId === chat.id }"
          @click="switchChat(chat.id)"
        >
          <div class="chat-title">{{ chat.title || '新对话' }}</div>
          <div class="chat-time">{{ formatTime(chat.updateTime) }}</div>
        </div>
      </div>
    </aside>
    
    <!-- 右侧聊天区域 -->
    <main class="chat-main">
      <!-- 聊天头部 -->
      <header class="chat-header">
        <div class="role-info" v-if="currentRole">
          <el-avatar :size="40" :src="currentRole.avatar">
            {{ currentRole.name[0] }}
          </el-avatar>
          <div class="role-details">
            <h3>{{ currentRole.name }}</h3>
            <p>{{ currentRole.description }}</p>
          </div>
        </div>
        
        <div class="chat-actions">
          <el-button size="small" @click="clearMessages">
            清空对话
          </el-button>
          <el-button size="small" @click="goHome">
            返回首页
          </el-button>
        </div>
      </header>
      
      <!-- 消息区域 -->
      <div class="messages-container" ref="messagesContainer">
        <div class="messages-list">
          <!-- 欢迎消息 -->
          <div v-if="messages.length === 0 && currentRole" class="welcome-message">
            <div class="message ai-message">
              <div class="message-avatar">
                <el-avatar :size="36" :src="currentRole.avatar">
                  {{ currentRole.name[0] }}
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-bubble">
                  你好！我是{{ currentRole.name }}，{{ currentRole.description }}。有什么可以帮助你的吗？
                </div>
                <div class="message-time">{{ formatTime(new Date()) }}</div>
              </div>
            </div>
          </div>
          
          <!-- 聊天消息 -->
          <div 
            v-for="message in messages" 
            :key="message.id"
            class="message"
            :class="message.senderType === 'user' ? 'user-message' : 'ai-message'"
          >
            <div class="message-avatar" v-if="message.senderType === 'ai'">
              <el-avatar :size="36" :src="currentRole?.avatar">
                {{ currentRole?.name[0] }}
              </el-avatar>
            </div>
            
            <div class="message-content">
              <div class="message-bubble">
                {{ message.content }}
              </div>
              <div class="message-time">{{ formatTime(message.sentAt) }}</div>
            </div>
            
            <div class="message-avatar" v-if="message.senderType === 'user'">
              <el-avatar :size="36" :src="authStore.userInfo?.avatar">
                {{ authStore.userInfo?.username[0]?.toUpperCase() }}
              </el-avatar>
            </div>
          </div>
          
          <!-- AI回复中状态 -->
          <div v-if="isAiReplying" class="message ai-message">
            <div class="message-avatar">
              <el-avatar :size="36" :src="currentRole?.avatar">
                {{ currentRole?.name[0] }}
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="message-bubble typing">
                <div class="typing-dots">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
                正在思考中...
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
            resize="none"
            @keydown.enter.exact="handleSend"
            @keydown.enter.shift.exact.prevent="addNewLine"
          />
          <div class="input-actions">
            <el-button 
              type="primary" 
              :loading="isSending"
              :disabled="!inputMessage.trim()"
              @click="handleSend"
            >
              发送
            </el-button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores'
import { chatAPI, messageAPI, roleAPI, SSEConnection } from '@/api'
import type { Chat, Message, Role } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const currentChatId = ref<number>(Number(route.params.id))
const currentChat = ref<Chat | null>(null)
const currentRole = ref<Role | null>(null)
const chatList = ref<Chat[]>([])
const messages = ref<Message[]>([])
const inputMessage = ref('')
const isSending = ref(false)
const isAiReplying = ref(false)
const messagesContainer = ref<HTMLElement>()

// SSE连接
let sseConnection: SSEConnection | null = null

// 页面初始化
onMounted(async () => {
  await initializePage()
})

// 清理资源
onUnmounted(() => {
  if (sseConnection) {
    sseConnection.disconnect()
  }
})

// 监听路由变化
watch(() => route.params.id, async (newId) => {
  if (newId && Number(newId) !== currentChatId.value) {
    currentChatId.value = Number(newId)
    await initializePage()
  }
})

// 初始化页面数据
const initializePage = async () => {
  try {
    // 加载用户的聊天列表
    await loadChatList()
    
    // 加载当前聊天详情
    if (currentChatId.value) {
      await loadChatDetails()
      await loadMessages()
      setupSSE()
    }
  } catch (error) {
    ElMessage.error('加载聊天数据失败')
    router.push('/')
  }
}

// 加载聊天列表
const loadChatList = async () => {
  try {
    const response = await chatAPI.getUserChats()
    if (response.data) {
      chatList.value = response.data
    }
  } catch (error) {
    console.error('加载聊天列表失败:', error)
  }
}

// 加载聊天详情
const loadChatDetails = async () => {
  try {
    const response = await chatAPI.getChatById(currentChatId.value)
    if (response.data) {
      currentChat.value = response.data
      
      // 加载角色信息
      const roleResponse = await roleAPI.getRoleById(response.data.roleId)
      if (roleResponse.data) {
        currentRole.value = roleResponse.data
      }
    }
  } catch (error) {
    console.error('加载聊天详情失败:', error)
  }
}

// 加载消息
const loadMessages = async () => {
  try {
    const response = await messageAPI.getChatMessages(currentChatId.value)
    if (response.data) {
      messages.value = response.data
      await nextTick()
      scrollToBottom()
      
      // 标记消息为已读
      await messageAPI.markMessagesAsRead(currentChatId.value)
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

// 建立SSE连接
const setupSSE = () => {
  if (sseConnection) {
    sseConnection.disconnect()
  }
  
  sseConnection = new SSEConnection(currentChatId.value)
  sseConnection.connect(
    // 接收到新消息
    (message: Message) => {
      messages.value.push(message)
      isAiReplying.value = false
      nextTick(() => scrollToBottom())
    },
    // 连接错误
    (error) => {
      console.error('SSE连接错误:', error)
      isAiReplying.value = false
    }
  )
}

// 发送消息
const handleSend = async () => {
  if (!inputMessage.value.trim() || isSending.value) return
  
  const messageContent = inputMessage.value.trim()
  inputMessage.value = ''
  
  try {
    isSending.value = true
    isAiReplying.value = true
    
    // 创建用户消息对象
    const userMessage = {
      id: Date.now(), // 临时ID
      chatId: currentChatId.value,
      senderType: 'user',
      content: messageContent,
      sentAt: new Date().toISOString()
    } as Message
    
    // 添加用户消息到列表
    messages.value.push(userMessage)
    await nextTick()
    scrollToBottom()
    
    // 使用SSE流式发送消息，roleId先传1
    const eventSource = messageAPI.sendMessageToStream(currentChatId.value, 1, messageContent)
    
    let aiResponse = ''
    let aiMessageId = Date.now() + 1
    
    // 创建AI消息对象（初始为空）
    const aiMessage = {
      id: aiMessageId,
      chatId: currentChatId.value,
      senderType: 'ai',
      content: '',
      sentAt: new Date().toISOString()
    } as Message
    
    // 添加AI消息到列表（初始为空）
    messages.value.push(aiMessage)
    
    // 处理SSE事件
    eventSource.onmessage = (event) => {
      console.log('Received SSE message:', event.data)
      
      // 检查是否是完成标记
      if (event.data === '[DONE]') {
        eventSource.close()
        isAiReplying.value = false
        isSending.value = false
      } else if (event.data === '[ERROR]') {
        eventSource.close()
        ElMessage.error('AI回复生成失败')
        isAiReplying.value = false
        isSending.value = false
      } else {
        // 处理流式数据
        const chunk = event.data.replace('data: ', '')
        if (chunk.trim()) {
          aiResponse += chunk
          
          // 更新AI消息内容
          const messageIndex = messages.value.findIndex(msg => msg.id === aiMessageId)
          if (messageIndex !== -1) {
            messages.value[messageIndex].content = aiResponse
          }
          
          nextTick(() => scrollToBottom())
        }
      }
    }
    
    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error)
      eventSource.close()
      ElMessage.error('SSE连接失败')
      isAiReplying.value = false
      isSending.value = false
    }
    
  } catch (error) {
    ElMessage.error('发送消息失败')
    isAiReplying.value = false
    isSending.value = false
  }
}

// 换行处理
const addNewLine = () => {
  inputMessage.value += '\n'
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 切换聊天
const switchChat = (chatId: number) => {
  router.push(`/chat/${chatId}`)
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 清空对话
const clearMessages = async () => {
  try {
    await ElMessageBox.confirm('确定要清空当前对话吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 实现清空对话API
    messages.value = []
    ElMessage.success('对话已清空')
  } catch (error) {
    // 用户取消操作
  }
}

// 时间格式化
const formatTime = (time: string | Date | undefined) => {
  if (!time) return ''
  return dayjs(time).format('HH:mm')
}
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
  background: #f5f7fa;
}

.chat-sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar-header h3 {
  margin: 0;
  color: #303133;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
}

.chat-item {
  padding: 16px 20px;
  cursor: pointer;
  border-bottom: 1px solid #f5f7fa;
  transition: background-color 0.3s;
}

.chat-item:hover {
  background: #f5f7fa;
}

.chat-item.active {
  background: #ecf5ff;
  border-right: 3px solid #409eff;
}

.chat-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-time {
  font-size: 12px;
  color: #909399;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  background: white;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.role-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-details h3 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 16px;
}

.role-details p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.messages-list {
  max-width: 800px;
  margin: 0 auto;
}

.message {
  display: flex;
  margin-bottom: 20px;
  gap: 12px;
}

.user-message {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 70%;
}

.user-message .message-content {
  text-align: right;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  word-wrap: break-word;
  line-height: 1.5;
}

.ai-message .message-bubble {
  background: white;
  border: 1px solid #e4e7ed;
}

.user-message .message-bubble {
  background: #409eff;
  color: white;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.user-message .message-time {
  text-align: right;
}

.typing {
  display: flex;
  align-items: center;
  gap: 8px;
}

.typing-dots {
  display: flex;
  gap: 2px;
}

.typing-dots span {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #409eff;
  animation: typing 1.4s infinite;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

.input-area {
  background: white;
  padding: 20px 24px;
  border-top: 1px solid #e4e7ed;
}

.input-container {
  max-width: 800px;
  margin: 0 auto;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  gap: 12px;
}

@media (max-width: 768px) {
  .chat-sidebar {
    display: none;
  }
  
  .messages-container {
    padding: 16px;
  }
  
  .input-area {
    padding: 16px;
  }
  
  .message-content {
    max-width: 85%;
  }
}
</style>