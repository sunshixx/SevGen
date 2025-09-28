<template>
  <div class="chatroom-detail-container">
    <!-- 头部 -->
    <div class="header">
      <div class="header-content">
        <div class="header-left">
          <el-button 
            type="text" 
            @click="$router.push('/chatrooms')"
            class="back-btn"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回聊天室列表
          </el-button>
          <div class="chatroom-info">
            <h1 class="title">{{ chatroomInfo?.name || '聊天室' }}</h1>
            <p class="description">{{ chatroomInfo?.description || '暂无描述' }}</p>
          </div>
        </div>
        <div class="header-right">
          <el-button 
            type="primary" 
            @click="showRoleSelector = true"
            :icon="Plus"
            :disabled="selectedRoles.length >= 5"
          >
            添加角色 ({{ selectedRoles.length }}/5)
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <!-- 角色选择区域 -->
      <div class="roles-section">
        <div class="section-header">
          <h3>参与角色</h3>
          <span class="role-count">{{ selectedRoles.length }} 个角色</span>
        </div>
        
        <div v-if="selectedRoles.length === 0" class="empty-roles">

          <div class="empty-state-container">
            <div class="empty-icon-wrapper">
              <div class="empty-icon-bg">
                <svg class="empty-icon-svg" viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M100 20C140 20 170 50 170 90C170 130 140 160 100 160C60 160 30 130 30 90C30 50 60 20 100 20Z" 
                        fill="url(#maskGradient)" stroke="url(#strokeGradient)" stroke-width="2"/>
                  <ellipse cx="75" cy="80" rx="12" ry="18" fill="rgba(0,0,0,0.3)"/>
                  <ellipse cx="125" cy="80" rx="12" ry="18" fill="rgba(0,0,0,0.3)"/>
                  <path d="M85 110 Q100 125 115 110" stroke="rgba(0,0,0,0.3)" stroke-width="3" fill="none" stroke-linecap="round"/>
                  <path d="M60 70 Q100 50 140 70" stroke="url(#decorGradient)" stroke-width="2" fill="none" opacity="0.6"/>
                  <path d="M60 130 Q100 150 140 130" stroke="url(#decorGradient)" stroke-width="2" fill="none" opacity="0.6"/>
                  <defs>
                    <linearGradient id="maskGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" style="stop-color:rgba(255,255,255,0.9);stop-opacity:1" />
                      <stop offset="50%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
                      <stop offset="100%" style="stop-color:rgba(255,255,255,0.5);stop-opacity:1" />
                    </linearGradient>
                    <linearGradient id="strokeGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" style="stop-color:rgba(0,255,255,0.8);stop-opacity:1" />
                      <stop offset="100%" style="stop-color:rgba(255,0,255,0.8);stop-opacity:1" />
                    </linearGradient>
                    <linearGradient id="decorGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                      <stop offset="0%" style="stop-color:rgba(0,255,255,0.6);stop-opacity:1" />
                      <stop offset="50%" style="stop-color:rgba(255,255,255,0.8);stop-opacity:1" />
                      <stop offset="100%" style="stop-color:rgba(255,0,255,0.6);stop-opacity:1" />
                    </linearGradient>
                  </defs>
                </svg>
                
                <div class="particle-container">
                  <div class="particle" v-for="i in 12" :key="i" :style="getParticleStyle(i)"></div>
                </div>
              </div>
              
              <div class="pulse-ring"></div>
              <div class="pulse-ring pulse-ring-delayed"></div>
            </div>
            
            <div class="empty-content">
              <h3 class="empty-title">开始你的角色扮演之旅</h3>
              <p class="empty-description">选择角色，让AI为你带来沉浸式的对话体验</p>
              
              <div class="empty-features">
                <div class="feature-item">
                  <div class="feature-icon">✨</div>
                  <span>智能对话</span>
                </div>
                <div class="feature-item">
                  <div class="feature-icon">🎭</div>
                  <span>角色扮演</span>
                </div>
                <div class="feature-item">
                  <div class="feature-icon">🚀</div>
                  <span>创意无限</span>
                </div>
              </div>
              
              <el-button 
                type="primary" 
                size="large" 
                @click="showRoleSelector = true"
                class="start-chat-btn"
              >
                <span class="btn-text">选择角色开始聊天</span>
                <div class="btn-glow"></div>
              </el-button>
            </div>
          </div>
        </div>

        <!-- 已选择的角色列表 -->
        <div v-else class="selected-roles">
          <div v-for="role in selectedRoles" :key="role.id" class="role-card">
            <div class="card-glow"></div>
            <div class="card-decoration">
              <div class="decoration-line top"></div>
              <div class="decoration-line bottom"></div>
              <div class="decoration-corner top-left"></div>
              <div class="decoration-corner top-right"></div>
              <div class="decoration-corner bottom-left"></div>
              <div class="decoration-corner bottom-right"></div>
            </div>
            
            <div class="role-avatar-container">
              <el-avatar 
                :size="60" 
                :src="getRoleAvatar(role)"
                :style="{ backgroundColor: getRoleAvatar(role) ? 'transparent' : '#409EFF' }"
                @error="handleAvatarError"
              >
                <span>{{ role.name.charAt(0) }}</span>
              </el-avatar>
              <div class="status-indicator online"></div>
            </div>
            
            <div class="role-info">
              <div class="role-name">{{ role.name }}</div>
              <div class="role-description">{{ role.description }}</div>
              <div class="role-tags">
                <span class="role-tag active">活跃</span>
                <span class="role-tag">{{ role.category || 'AI助手' }}</span>
              </div>
            </div>
            
            <div class="role-actions">
              <el-button 
                class="remove-btn" 
                @click="removeRole(role.id)"
                :icon="Close"
                circle
                size="small"
              />
              <el-button 
                class="more-btn" 
                :icon="MoreFilled"
                circle
                size="small"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 聊天区域 -->

      <div class="chat-section">
        <div class="messages-container" ref="messagesContainer">
          <div v-if="messages.length === 0" class="empty-messages">
            <div class="empty-messages-icon">
              <svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="50" cy="50" r="35" fill="url(#chatGradient)" opacity="0.8"/>
                <path d="M35 40 Q50 30 65 40" stroke="rgba(255,255,255,0.6)" stroke-width="2" fill="none" stroke-linecap="round"/>
                <path d="M35 50 Q50 60 65 50" stroke="rgba(255,255,255,0.6)" stroke-width="2" fill="none" stroke-linecap="round"/>
                <circle cx="42" cy="45" r="2" fill="rgba(255,255,255,0.8)"/>
                <circle cx="58" cy="45" r="2" fill="rgba(255,255,255,0.8)"/>
                <defs>
                  <linearGradient id="chatGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(76,175,80,0.8);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(69,160,73,0.8);stop-opacity:1" />
                  </linearGradient>
                </defs>
              </svg>
            </div>
            <h3>开始对话</h3>
            <p>选择角色后，在这里开始你的AI对话之旅</p>
          </div>

          <div v-for="message in messages" :key="message.id" class="message-wrapper" :class="{ 'user-message': message.type === 'user', 'ai-message': message.type === 'ai' }">
            <div class="message-bubble">
              <div class="message-avatar">
                <el-avatar 
                  :size="32"
                  :src="message.type === 'user' ? '' : getRoleAvatar(getRoleById(message.roleId))"
                  :style="{ backgroundColor: message.type === 'user' ? '#409EFF' : (getRoleAvatar(getRoleById(message.roleId)) ? 'transparent' : '#67C23A') }"
                  @error="handleAvatarError"
                >
                  <span>{{ message.senderName?.charAt(0) || 'U' }}</span>
                </el-avatar>
                <div class="avatar-status" :class="{ 'online': message.type === 'ai' }"></div>
              </div>
              
              <div class="message-content">
                <div class="message-header">
                  <span class="sender-name">{{ message.senderName || '用户' }}</span>
                  <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                </div>
                <div class="message-text">
                  <div 
                    class="text-content" 
                    v-if="message.type === 'ai'"
                    v-html="renderMarkdown(message.content)"
                  ></div>
                  <div 
                    class="text-content" 
                    v-else
                  >{{ message.content }}</div>
                  <div class="message-actions">
                    <el-button 
                      size="small" 
                      text 
                      :icon="Position"
                      @click="copyMessage(message.content)"
                    />
                    <el-button 
                      size="small" 
                      text 
                      :icon="Delete"
                      @click="deleteMessage(message.id)"
                    />
                  </div>
                  <div class="message-tail"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- AI思考中指示器 -->
          <div v-if="isThinking" class="thinking-indicator">
            <div class="thinking-avatar">
              <el-avatar 
                :size="32"
                style="background-color: #67C23A;"
              >
                <span>🤖</span>
              </el-avatar>
              <div class="thinking-animation">
                <div class="thinking-dot"></div>
                <div class="thinking-dot"></div>
                <div class="thinking-dot"></div>
              </div>
            </div>
            <div class="thinking-text">AI正在思考中...</div>
          </div>
        </div>

        <div class="input-section">
          <div class="input-container">
            <div class="input-wrapper">
              <el-input
                v-model="newMessage"
                type="textarea"
                :rows="1"
                :autosize="{ minRows: 1, maxRows: 4 }"
                placeholder="输入消息... (Enter发送，Shift+Enter换行)"
                class="message-input"
                @keydown.enter.exact.prevent="handleSendMessage"
                @keydown.enter.shift.exact="handleNewLine"
              />
              <div class="input-actions">
                <el-button 
                  type="primary" 
                  @click="handleSendMessage"
                  :disabled="!newMessage.trim() || selectedRoles.length === 0"
                  class="send-btn"
                  :loading="isThinking"
                >
                  <el-icon v-if="!isThinking"><Position /></el-icon>
                  <span v-if="isThinking">发送中</span>
                  <span v-else>发送</span>
                </el-button>
              </div>
            </div>
            
            <!-- 快捷操作 -->
            <div class="quick-actions" v-if="selectedRoles.length > 0">
              <div class="action-item" @click="clearMessages">
                <el-icon><Delete /></el-icon>
                <span>清空对话</span>
              </div>
              <div class="action-item" @click="exportChat">
                <el-icon><Download /></el-icon>
                <span>导出对话</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>



    <el-dialog
      v-model="showRoleSelector"
      title="选择角色"
      width="800px"

      :before-close="handleClose"
    >
      <div class="role-selector-content">
        <div v-for="role in availableRoles" :key="role.id" class="available-role-item">
          <el-avatar 
            :size="40" 
            :src="getRoleAvatar(role)"
            :style="{ backgroundColor: getRoleAvatar(role) ? 'transparent' : '#409EFF' }"
            @error="handleAvatarError"
          >
            <span>{{ role.name.charAt(0) }}</span>
          </el-avatar>
          <div class="role-info">
            <div class="role-name">{{ role.name }}</div>
            <div class="role-description">{{ role.description }}</div>
          </div>
          <el-button 
            type="primary" 
            @click="handleRoleSelected([role])"
            :disabled="selectedRoles.some(r => r.id === role.id)"
          >
            {{ selectedRoles.some(r => r.id === role.id) ? '已选择' : '选择' }}
          </el-button>
        </div>
      </div>


    </el-dialog>
  </div>
</template>


<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, Close, MoreFilled, Position, Delete, Download } from '@element-plus/icons-vue'
import { chatroomAPI } from '@/api/chatroom'
import { roleAPI } from '@/api/role'
import { messageAPI } from '@/api/message'
import { useAuthStore } from '@/stores/auth'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const chatroomInfo = ref(null)
const selectedRoles = ref([])
const availableRoles = ref([])
const messages = ref([])
const newMessage = ref('')
const showRoleSelector = ref(false)
const messagesContainer = ref(null)

// 生命周期
onMounted(async () => {
  loadChatroomInfo()
  loadAvailableRoles()
  // 先加载聊天室角色，再加载消息，确保角色信息可用
  await loadChatroomRoles()
  loadChatroomMessages()
})

// 方法
const loadChatroomInfo = async () => {
  try {
    const response = await chatroomAPI.getChatroomInfo(route.params.id)
    if (response.success) {
      chatroomInfo.value = response.data
    }
  } catch (error) {
    console.error('加载聊天室信息失败:', error)
    ElMessage.error('加载聊天室信息失败')
  }
}

const loadAvailableRoles = async () => {
  try {
    const response = await roleAPI.getAllPublicRoles()
    if (response.success) {
      availableRoles.value = response.data
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error('加载角色列表失败')
  }
}

const loadChatroomRoles = async () => {
  try {
    const response = await chatroomAPI.getChatroomRoles(route.params.id)
    if (response.success && response.data && Array.isArray(response.data)) {
      // 从聊天室角色记录中提取角色信息，过滤掉无效的roleId
      const roleIds = response.data
        .filter(record => record && record.roleId != null)
        .map(record => record.roleId)
      
      if (roleIds.length > 0) {
        const rolePromises = roleIds.map(roleId => roleAPI.getRoleById(roleId))
        const roleResponses = await Promise.all(rolePromises)
        
        selectedRoles.value = roleResponses
          .filter(res => res && res.success && res.data)
          .map(res => res.data)
      } else {
        selectedRoles.value = []
      }
    } else {
      selectedRoles.value = []
    }

  } catch (error) {
    console.error('加载聊天室角色失败:', error)
    selectedRoles.value = []
  }
}


const loadChatroomMessages = async () => {
  try {
    const response = await chatroomAPI.getChatroomMessages(route.params.id)
    if (response.success) {
      const rawMessages = response.data.data || []
      // 处理历史消息，确保消息类型正确
      messages.value = await Promise.all(rawMessages.map(async message => {
        // 判断消息类型：如果有roleId且不为空，则为AI消息；否则为用户消息
        const messageType = message.roleId ? 'ai' : 'user'
        
        // 获取AI角色信息
        let senderName = message.senderName
        if (messageType === 'ai' && message.roleId) {
          // 先尝试从selectedRoles中获取
          let role = getRoleById(message.roleId)
          
          // 如果selectedRoles中没有，直接从API获取
          if (!role) {
            try {
              const roleResponse = await roleAPI.getRoleById(message.roleId)
              if (roleResponse.success && roleResponse.data) {
                role = roleResponse.data
              }
            } catch (error) {
              console.error('获取角色信息失败:', error)
            }
          }
          
          senderName = role ? role.name : (message.senderName || 'AI')
        } else if (messageType === 'user') {
          senderName = message.senderName || authStore.userInfo?.username || '我'
        }
        
        return {
          ...message,
          type: messageType,
          // 确保时间字段正确映射
          timestamp: message.timestamp || message.createTime || new Date(),
          // 确保发送者名称正确显示
          senderName: senderName
        }
      }))
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

// 获取角色头像
const getRoleAvatar = (role) => {
  if (!role || !role.avatar) {
    return '' // 如果没有头像，返回空字符串，让el-avatar显示文字
  }
  return role.avatar
}

// 头像加载错误处理
const handleAvatarError = (event) => {
  const target = event.target
  if (target) {
    console.log('头像加载失败:', target.src)
    target.src = '' // 清空src，让el-avatar显示文字
  }
}

// 根据角色ID获取角色信息
const getRoleById = (roleId) => {
  return selectedRoles.value.find(role => role.id === roleId) || null
}

const getParticleStyle = (index) => {
  const angle = (index * 30) % 360
  const delay = (index * 0.5) % 3
  return {
    '--angle': `${angle}deg`,
    '--delay': `${delay}s`
  }
}

const handleClose = () => {
  showRoleSelector.value = false
}

const handleRoleSelected = async (roles) => {
  try {
    // 添加角色到聊天室
    for (const role of roles) {
      const existingRole = selectedRoles.value.find(r => r.id === role.id)
      if (!existingRole) {
        const userId = 1 // 这里应该从用户状态获取
        await chatroomAPI.addRoleToChatroom(route.params.id, role.id, userId)
        selectedRoles.value.push(role)
      }
    }
    showRoleSelector.value = false
    ElMessage.success('角色添加成功！')
  } catch (error) {
    console.error('添加角色失败:', error)
    ElMessage.error('添加角色失败')
  }
}

const removeRole = async (roleId) => {
  try {
    // 找到要删除的角色记录
    const response = await chatroomAPI.getChatroomRoles(route.params.id)
    if (response.success) {
      const roleRecord = response.data.find(record => record.roleId === roleId)
      if (roleRecord) {
        await chatroomAPI.removeRoleFromChatroom(roleRecord.id)
        selectedRoles.value = selectedRoles.value.filter(role => role.id !== roleId)
        ElMessage.success('角色已移除')
      }
    }
  } catch (error) {
    console.error('移除角色失败:', error)
    ElMessage.error('移除角色失败')
  }
}

const isThinking = ref(false)

const handleSendMessage = async () => {
  if (!newMessage.value.trim() || selectedRoles.value.length === 0) return
  
  const messageContent = newMessage.value.trim()
  newMessage.value = ''
  isThinking.value = true
  
  // 立即添加用户消息到消息列表
  const userMessage = {
    id: Date.now() + Math.random(),
    type: 'user',
    senderName: authStore.userInfo?.username || '我',
    content: messageContent,
    timestamp: new Date()
  }
  messages.value.push(userMessage)
  
  // 滚动到底部
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
  
  try {
    // 使用聊天室专用的SSE流式连接发送消息
    const roleIds = selectedRoles.value.map(role => role.id)
    
    // 创建聊天室协作SSE连接
    const eventSource = chatroomAPI.createCollaborativeStreamConnection(
      route.params.id, 
      messageContent,
      roleIds // 传递角色ID数组给后端
    )
    
    // 使用Map来跟踪每个角色的当前消息
    const currentAiMessages = new Map()
    
    eventSource.onmessage = (event) => {
      try {
        console.log('收到SSE原始数据:', event.data)
        
        // 处理不同格式的SSE数据
        let data
        let rawData = event.data
        
        // 处理带有"data:"前缀的情况
        if (rawData.startsWith('data:')) {
          rawData = rawData.substring(5).trim() // 移除"data:"前缀
        }
        
        // 检查特殊控制消息
        if (rawData === '[DONE]') {
          isThinking.value = false
          eventSource.close()
          return
        }
        
        // 尝试解析为JSON
        try {
          data = JSON.parse(rawData)
        } catch (error) {
          console.error('解析SSE数据失败:', error, '原始数据:', event.data)
          return
        }
        
        // 处理结构化数据
        if (data) {
          switch (data.type) {
            case 'USER_MESSAGE':
              // 用户消息已经在发送时添加，这里不需要重复添加
              console.log('用户消息确认:', data.content)
              break
              
            case 'START':
              // 协作开始
              console.log('协作开始:', data.message)
              break
              
            case 'ROLE_START':
              // 角色开始回复 - 为每个角色创建独立的消息对象
              const newAiMessage = {
                id: Date.now() + Math.random(),
                type: 'ai',
                senderName: data.roleName,
                content: '',
                timestamp: new Date()
              }
              messages.value.push(newAiMessage)
              // 使用roleId作为key来跟踪每个角色的消息
              currentAiMessages.set(data.roleId, newAiMessage)
              break
              
            case 'ROLE_MESSAGE':
              // 流式接收AI消息 - 根据roleId找到对应的消息对象
              const targetMessage = currentAiMessages.get(data.roleId)
              if (targetMessage) {
                targetMessage.content += data.message
              }
              break
              
            case 'ROLE_COMPLETE':
              // 角色回复完成 - 移除该角色的跟踪
              currentAiMessages.delete(data.roleId)
              break
              
            case 'ROLE_ERROR':
              // 角色回复出错
              console.error('角色回复错误:', data.message)
              const errorMessage = currentAiMessages.get(data.roleId)
              if (errorMessage) {
                errorMessage.content += '\n[回复出错: ' + data.message + ']'
              }
              currentAiMessages.delete(data.roleId)
              break
              
            case 'COMPLETE':
              // 所有回复完成
              isThinking.value = false
              currentAiMessages.clear()
              eventSource.close()
              break
              
            case 'ERROR':
              console.error('SSE错误:', data.message)
              ElMessage.error('发送消息失败: ' + data.message)
              isThinking.value = false
              currentAiMessages.clear()
              eventSource.close()
              break
          }
          
          // 滚动到底部
          nextTick(() => {
            if (messagesContainer.value) {
              messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
            }
          })
        }
        
      } catch (error) {
        console.error('解析SSE数据失败:', error, '原始数据:', event.data)
      }
    }
    
    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error)
      ElMessage.error('连接失败，请重试')
      isThinking.value = false
      eventSource.close()
    }
    
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
    isThinking.value = false
  }
}

const handleNewLine = () => {
  newMessage.value += '\n'
}

const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('消息已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败')
  }
}

const deleteMessage = (messageId) => {
  const index = messages.value.findIndex(msg => msg.id === messageId)
  if (index > -1) {
    messages.value.splice(index, 1)
    ElMessage.success('消息已删除')
  }
}

const clearMessages = () => {
  messages.value = []
  ElMessage.success('对话已清空')
}

const exportChat = () => {
  const chatContent = messages.value.map(msg => 
    `${msg.senderName} (${formatTime(msg.timestamp)}): ${msg.content}`
  ).join('\n')
  
  const blob = new Blob([chatContent], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `聊天记录_${new Date().toLocaleDateString()}.txt`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('对话已导出')
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Markdown渲染函数
const renderMarkdown = (content) => {
  if (!content) return ''
  
  // 配置marked选项
  marked.setOptions({
    breaks: true, // 支持换行
    gfm: true,    // 支持GitHub风格的Markdown
  })
  
  // 渲染markdown并清理HTML
  const rawHtml = marked(content)
  return DOMPurify.sanitize(rawHtml)

}
</script>

<style scoped>
.chatroom-detail-container {
  height: 100vh;

  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;

}

.header-left {
  display: flex;
  align-items: center;

  gap: 20px;
}

.back-btn {
  color: rgba(255, 255, 255, 0.9) !important;
  font-size: 14px !important;
}

.chatroom-info .title {
  color: #fff;
  font-size: 24px;
  font-weight: 600;

  margin: 0 0 4px 0;
}

.chatroom-info .description {

  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;


  margin: 0;
}

.main-content {
  flex: 1;
  display: flex;

  gap: 20px;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;

  overflow: hidden;
}

.roles-section {

  width: 350px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 20px;

  overflow-y: auto;
}

.section-header {
  display: flex;

  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  color: #fff;
  font-size: 18px;
  font-weight: 600;

  margin: 0;
}

.role-count {

  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

/* 空状态样式 */
.empty-state-container {

  text-align: center;
  padding: 40px 20px;
}


.empty-icon-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 30px;
}

.empty-icon-bg {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto;
}

.empty-icon-svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 10px 30px rgba(0, 0, 0, 0.3));
  animation: floatIcon 6s ease-in-out infinite;
}

@keyframes floatIcon {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-10px) rotate(5deg); }
}

.particle-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.8) 0%, transparent 70%);
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform-origin: 0 0;
  animation: particleFloat 4s ease-in-out infinite;
  animation-delay: var(--delay);
}

@keyframes particleFloat {
  0%, 100% {
    transform: translate(-50%, -50%) rotate(var(--angle)) translateY(-60px) scale(0);
    opacity: 0;
  }
  50% {
    transform: translate(-50%, -50%) rotate(var(--angle)) translateY(-80px) scale(1);
    opacity: 1;
  }
}

.pulse-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 140px;
  height: 140px;
  border: 2px solid rgba(0, 255, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: pulseRing 3s ease-in-out infinite;
}

.pulse-ring-delayed {
  animation-delay: 1.5s;
  border-color: rgba(255, 0, 255, 0.3);
}

@keyframes pulseRing {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.3;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0.1;
  }
}

.empty-content {
  color: #fff;
}

.empty-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 12px;
  background: linear-gradient(45deg, #fff, #f0f0f0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.empty-description {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 30px;
  line-height: 1.5;
}

.empty-features {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 40px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.feature-icon {
  font-size: 24px;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  backdrop-filter: blur(10px);
}

.feature-item span {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.start-chat-btn {
  position: relative;
  padding: 16px 32px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: 50px !important;
  background: linear-gradient(45deg, #4CAF50, #45a049) !important;
  border: none !important;
  box-shadow: 0 8px 25px rgba(76, 175, 80, 0.4) !important;
  overflow: hidden !important;
  transition: all 0.3s ease !important;
}

.start-chat-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 12px 35px rgba(76, 175, 80, 0.6) !important;
}

.btn-text {
  position: relative;
  z-index: 2;
}

.btn-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

/* 角色卡片样式 */
.selected-roles {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.role-card {
  position: relative;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.12) 0%,
    rgba(255, 255, 255, 0.08) 50%,
    rgba(255, 255, 255, 0.05) 100%
  );
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  overflow: hidden;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.role-card:hover {
  transform: translateY(-8px) scale(1.02);
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.18) 0%,
    rgba(255, 255, 255, 0.12) 50%,
    rgba(255, 255, 255, 0.08) 100%
  );
  border-color: rgba(255, 255, 255, 0.4);
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.25),
    0 0 40px rgba(0, 255, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.role-avatar-container {
  position: relative;
  flex-shrink: 0;
}

.role-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  position: relative;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  box-shadow: 
    0 8px 25px rgba(76, 175, 80, 0.4),
    inset 0 2px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  overflow: hidden;
}

.breathing-light {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 50%;
  background: linear-gradient(45deg, 
    rgba(0, 255, 255, 0.6) 0%,
    rgba(255, 0, 255, 0.6) 50%,
    rgba(0, 255, 255, 0.6) 100%
  );
  animation: breathingLight 3s ease-in-out infinite;
  z-index: -1;
}

@keyframes breathingLight {
  0%, 100% { 
    opacity: 0.3; 
    transform: scale(1); 
  }
  50% { 
    opacity: 0.8; 
    transform: scale(1.1); 
  }
}

.status-indicator {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #fff;
  z-index: 2;
}

.status-indicator.online {
  background: #4CAF50;
  box-shadow: 0 0 10px rgba(76, 175, 80, 0.8);
  animation: statusPulse 2s ease-in-out infinite;
}

@keyframes statusPulse {
  0%, 100% { box-shadow: 0 0 10px rgba(76, 175, 80, 0.8); }
  50% { box-shadow: 0 0 20px rgba(76, 175, 80, 1); }
}

.energy-ring {
  position: absolute;
  top: -8px;
  left: -8px;
  right: -8px;
  bottom: -8px;
  border: 2px solid transparent;
  border-top: 2px solid rgba(0, 255, 255, 0.6);
  border-right: 2px solid rgba(255, 0, 255, 0.4);
  border-radius: 50%;
  animation: energyRotate 4s linear infinite;
  z-index: -1;
}

@keyframes energyRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.particle-orbit {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 80px;
  height: 80px;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.orbit-particle {
  position: absolute;
  width: 3px;
  height: 3px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.9) 0%, transparent 70%);
  border-radius: 50%;
  top: 0;
  left: 50%;
  transform-origin: 0 40px;
  animation: orbitRotate 6s linear infinite;
  animation-delay: var(--delay);
}

@keyframes orbitRotate {
  0% { transform: rotate(0deg) translateY(-40px); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { transform: rotate(360deg) translateY(-40px); opacity: 0; }
}

.avatar-halo {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100px;
  height: 100px;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, 
    rgba(0, 255, 255, 0.1) 0%,
    rgba(255, 0, 255, 0.1) 50%,
    transparent 70%
  );
  border-radius: 50%;
  opacity: 0;
  transition: all 0.4s ease;
  z-index: -2;
}

.role-card:hover .avatar-halo {
  opacity: 0.8;
  transform: translate(-50%, -50%) scale(1.3);

}

.role-info {
  flex: 1;
  min-width: 0;
}

.role-name {

  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.5px;
}

.role-description {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.5;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  font-weight: 500;
}

.role-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.role-tag {
  font-size: 14px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.role-tag.active {
  background: rgba(75, 85, 99, 0.8);
  color: #10b981;
  border-color: rgba(75, 85, 99, 0.9);
  box-shadow: 0 0 20px rgba(75, 85, 99, 0.5);
  font-weight: 700;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.role-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  opacity: 0;
  transform: translateX(10px);
  transition: all 0.3s ease;
}

.role-card:hover .role-actions {
  opacity: 1;
  transform: translateX(0);
}

.remove-btn,
.more-btn {
  width: 32px !important;
  height: 32px !important;
  border-radius: 50% !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: rgba(255, 255, 255, 0.8) !important;
  transition: all 0.3s ease !important;
  backdrop-filter: blur(10px) !important;
}

.remove-btn:hover {
  background: rgba(244, 67, 54, 0.2) !important;
  border-color: rgba(244, 67, 54, 0.4) !important;
  color: #f44336 !important;
  transform: scale(1.1) !important;
  box-shadow: 0 0 15px rgba(244, 67, 54, 0.4) !important;
}

.more-btn:hover {
  background: rgba(33, 150, 243, 0.2) !important;
  border-color: rgba(33, 150, 243, 0.4) !important;
  color: #2196F3 !important;
  transform: scale(1.1) !important;
  box-shadow: 0 0 15px rgba(33, 150, 243, 0.4) !important;
}

.card-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.role-card:hover .card-decoration {
  opacity: 1;
}

.decoration-line {
  position: absolute;
  background: linear-gradient(90deg, 
    transparent 0%,
    rgba(0, 255, 255, 0.6) 50%,
    transparent 100%
  );
  height: 1px;
  animation: decorationScan 3s ease-in-out infinite;
}

.decoration-line.top {
  top: 0;
  left: 0;
  right: 0;
}

.decoration-line.bottom {
  bottom: 0;
  left: 0;
  right: 0;
  animation-delay: 1.5s;
}

@keyframes decorationScan {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

.decoration-corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(0, 255, 255, 0.4);
}

.decoration-corner.top-left {
  top: 0;
  left: 0;
  border-right: none;
  border-bottom: none;
  border-radius: 20px 0 0 0;
}

.decoration-corner.top-right {
  top: 0;
  right: 0;
  border-left: none;
  border-bottom: none;
  border-radius: 0 20px 0 0;
}

.decoration-corner.bottom-left {
  bottom: 0;
  left: 0;
  border-right: none;
  border-top: none;
  border-radius: 0 0 0 20px;
}

.decoration-corner.bottom-right {
  bottom: 0;
  right: 0;
  border-left: none;
  border-top: none;
  border-radius: 0 0 20px 0;
}

.card-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, 
    rgba(0, 255, 255, 0.1) 0%,
    rgba(255, 0, 255, 0.1) 25%,
    rgba(255, 255, 0, 0.1) 50%,
    rgba(255, 0, 255, 0.1) 75%,
    rgba(0, 255, 255, 0.1) 100%
  );
  background-size: 400% 400%;
  border-radius: 22px;
  opacity: 0;
  transition: all 0.4s ease;
  z-index: -1;
  animation: cardGlowShift 6s ease-in-out infinite;
}

.role-card:hover .card-glow {
  opacity: 1;
  transform: scale(1.1);
}

@keyframes cardGlowShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

/* 聊天区域样式 */

.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;

  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(15px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

  overflow: hidden;
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;

  scroll-behavior: smooth;
}

.empty-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: rgba(255, 255, 255, 0.6);
  text-align: center;
}

.empty-messages-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 20px;
  opacity: 0.6;
}

.empty-messages h3 {
  color: #fff;
  font-size: 20px;
  margin-bottom: 8px;
}

.empty-messages p {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.message-wrapper {
  display: flex;
  margin-bottom: 20px;
  animation: messageSlideIn 0.3s ease-out;
}

.message-wrapper.user-message {
  justify-content: flex-end;
}

.message-wrapper.ai-message {
  justify-content: flex-start;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-bubble {
  display: flex;
  gap: 12px;
  max-width: 70%;
  align-items: flex-start;
}

.user-message .message-bubble {

  flex-direction: row-reverse;
}

.message-avatar {

  position: relative;
  flex-shrink: 0;
}

.avatar-bg {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  background: linear-gradient(135deg, #4CAF50, #45a049);
  box-shadow: 0 4px 15px rgba(76, 175, 80, 0.3);
  transition: all 0.3s ease;
}

.user-message .avatar-bg {
  background: linear-gradient(135deg, #2196F3, #1976D2);
  box-shadow: 0 4px 15px rgba(33, 150, 243, 0.3);
}

.avatar-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #fff;
  background: #ccc;
}

.avatar-status.online {
  background: #4CAF50;
  box-shadow: 0 0 8px rgba(76, 175, 80, 0.6);
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  gap: 12px;
}

.user-message .message-header {
  flex-direction: row-reverse;
}

.sender-name {
  font-weight: 600;
  color: #fff;
  font-size: 14px;
  white-space: nowrap;
}

.message-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  white-space: nowrap;
}

.message-text {
  position: relative;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(15px);
  border-radius: 18px;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.user-message .message-text {
  background: rgba(33, 150, 243, 0.2);
  border-color: rgba(33, 150, 243, 0.3);
}

.message-text:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
}

.user-message .message-text:hover {
  background: rgba(33, 150, 243, 0.3);
  border-color: rgba(33, 150, 243, 0.4);
}

.text-content {
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.5;
  word-wrap: break-word;
  white-space: pre-wrap;
}

/* Markdown样式 */
.text-content h1, .text-content h2, .text-content h3, 
.text-content h4, .text-content h5, .text-content h6 {
  color: #fff;
  margin: 16px 0 8px 0;
  font-weight: 600;
}

.text-content h1 { font-size: 1.5em; }
.text-content h2 { font-size: 1.3em; }
.text-content h3 { font-size: 1.1em; }

.text-content p {
  margin: 8px 0;
}

.text-content code {
  background: rgba(0, 0, 0, 0.3);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
  color: #ffd700;
}

.text-content pre {
  background: rgba(0, 0, 0, 0.4);
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
  border-left: 4px solid #409EFF;
}

.text-content pre code {
  background: none;
  padding: 0;
  color: #e6e6e6;
}

.text-content blockquote {
  border-left: 4px solid #409EFF;
  padding-left: 12px;
  margin: 12px 0;
  color: rgba(255, 255, 255, 0.8);
  font-style: italic;
}

.text-content ul, .text-content ol {
  margin: 8px 0;
  padding-left: 20px;
}

.text-content li {
  margin: 4px 0;
}

.text-content strong {
  color: #fff;
  font-weight: 600;
}

.text-content em {
  color: rgba(255, 255, 255, 0.9);
  font-style: italic;
}

.text-content a {
  color: #409EFF;
  text-decoration: none;
}

.text-content a:hover {
  text-decoration: underline;
}

.text-content table {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
}

.text-content th, .text-content td {
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 8px 12px;
  text-align: left;
}

.text-content th {
  background: rgba(0, 0, 0, 0.3);
  font-weight: 600;
  color: #fff;
}

.message-actions {
  display: flex;
  gap: 4px;
  margin-top: 8px;
  opacity: 0;
  transform: translateY(5px);
  transition: all 0.3s ease;
}

.message-text:hover .message-actions {
  opacity: 1;
  transform: translateY(0);
}

.message-actions .el-button {
  padding: 4px !important;
  min-height: 24px !important;
  color: rgba(255, 255, 255, 0.6) !important;
  transition: all 0.3s ease !important;
}

.message-actions .el-button:hover {
  color: #fff !important;
  background: rgba(255, 255, 255, 0.1) !important;
  transform: scale(1.1) !important;
}

.message-tail {
  position: absolute;
  width: 0;
  height: 0;
  border-style: solid;
}

.ai-message .message-tail {
  left: -8px;
  top: 12px;
  border-width: 8px 8px 8px 0;
  border-color: transparent rgba(255, 255, 255, 0.1) transparent transparent;
}

.user-message .message-tail {
  right: -8px;
  top: 12px;
  border-width: 8px 0 8px 8px;
  border-color: transparent transparent transparent rgba(33, 150, 243, 0.2);
}

.thinking-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  animation: messageSlideIn 0.3s ease-out;
}

.thinking-avatar {
  position: relative;
  width: 40px;
  height: 40px;
}

.thinking-avatar .avatar-bg {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF9800, #F57C00);
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
  animation: thinkingPulse 2s ease-in-out infinite;
}

@keyframes thinkingPulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 6px 20px rgba(255, 152, 0, 0.5);
  }
}

.thinking-animation {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  gap: 3px;
}

.thinking-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
  animation: thinkingBounce 1.4s ease-in-out infinite both;
}

.thinking-dot:nth-child(1) { animation-delay: -0.32s; }
.thinking-dot:nth-child(2) { animation-delay: -0.16s; }
.thinking-dot:nth-child(3) { animation-delay: 0s; }

@keyframes thinkingBounce {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.thinking-text {
  color: rgba(255, 255, 255, 0.7);
  font-style: italic;
  font-size: 14px;
}

.input-section {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.02);

}

.input-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}


.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 25px;
  padding: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(15px);
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: rgba(76, 175, 80, 0.5);
  box-shadow: 0 0 20px rgba(76, 175, 80, 0.2);
  background: rgba(255, 255, 255, 0.12);
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-textarea__inner) {
  background: transparent !important;
  border: none !important;
  color: rgba(255, 255, 255, 0.9) !important;
  font-size: 14px !important;
  line-height: 1.5 !important;
  padding: 8px 12px !important;
  resize: none !important;
  box-shadow: none !important;
}

.message-input :deep(.el-textarea__inner):focus {
  box-shadow: none !important;
}

.message-input :deep(.el-textarea__inner)::placeholder {
  color: rgba(255, 255, 255, 0.5) !important;
}

.input-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.emoji-btn {
  width: 36px !important;
  height: 36px !important;
  border-radius: 50% !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: rgba(255, 255, 255, 0.8) !important;
  font-size: 16px !important;
  transition: all 0.3s ease !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 0 !important;
}

.emoji-btn:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  transform: scale(1.1) !important;
}

.send-btn {
  height: 36px !important;
  padding: 0 16px !important;
  border-radius: 18px !important;
  background: linear-gradient(45deg, #4CAF50, #45a049) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 600 !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 15px rgba(76, 175, 80, 0.3) !important;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 20px rgba(76, 175, 80, 0.4) !important;
  background: linear-gradient(45deg, #45a049, #4CAF50) !important;
}

.send-btn:disabled {
  opacity: 0.5 !important;
  cursor: not-allowed !important;
  transform: none !important;
}

.quick-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding-top: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.action-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
  transform: translateY(-1px);
  border-color: rgba(255, 255, 255, 0.2);
}

.action-item .el-icon {
  font-size: 14px;
}

/* 角色选择器样式 */
.role-selector-content {
  max-height: 400px;
  overflow-y: auto;
}

.available-role-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.available-role-item:hover {
  background: #f5f5f5;
  border-color: #4CAF50;
}

.available-role-item .role-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.available-role-item .role-description {
  color: #666;
  font-size: 14px;


}
</style>