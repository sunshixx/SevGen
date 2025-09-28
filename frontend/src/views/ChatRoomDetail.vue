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
          <el-empty
            description="还没有选择角色"
            :image-size="120"
          >
            <template #image>
              <div class="empty-icon">🎭</div>
            </template>
            <el-button 
              type="primary" 
              @click="showRoleSelector = true"
              :icon="Plus"
            >
              选择角色开始聊天
            </el-button>
          </el-empty>
        </div>

        <div v-else class="selected-roles">
          <div 
            v-for="role in selectedRoles" 
            :key="role.id"
            class="role-card"
          >
            <div class="role-avatar" :style="{ backgroundColor: getRoleColor(role.id) }">
              {{ role.name[0] }}
            </div>
            <div class="role-info">
              <div class="role-name">{{ role.name }}</div>
              <div class="role-description">{{ role.description }}</div>
            </div>
            <el-button 
              type="text" 
              @click="removeRole(role.id)"
              class="remove-btn"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 聊天区域 -->
      <div v-if="selectedRoles.length > 0" class="chat-section">
        <!-- 消息显示区域 -->
        <div class="messages-container" ref="messagesContainer">
          <template v-for="(message, index) in messages" :key="message.id">
            <!-- 时间分隔符 -->
            <div 
              v-if="index === 0 || shouldShowTimeLabel(messages[index - 1]?.sentAt, message.sentAt)"
              class="message-time-divider"
            >
              {{ formatMessageTime(message.sentAt) }}
            </div>
            
            <div 
              class="message-item"
              :class="{ 
                'user': message.senderType === 'user', 
                'ai': message.senderType === 'ai',
                'system': message.senderType === 'system'
              }"
            >
              <div class="message-avatar">
                <span v-if="message.senderType === 'user'">你</span>
                <span v-else-if="message.senderType === 'system'">系统</span>
                <span v-else>{{ getRoleName(message.roleId)?.[0] || '🤖' }}</span>
              </div>
              
              <div class="message-content">
                <div v-if="message.senderType === 'ai' && message.roleId" class="role-label">
                  {{ getRoleName(message.roleId) }}
                </div>
                <div class="message-text" v-html="renderMarkdown(message.content)"></div>
              </div>
            </div>
          </template>

          <!-- 加载状态 -->
          <div v-if="isResponding" class="message-item ai loading">
            <div class="message-avatar">
              <span>🤖</span>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-section">
          <div class="input-container">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入消息，多个角色将协作回复..."
              @keydown.enter.prevent="handleSendMessage"
              :disabled="isResponding"
              resize="none"
            />
            <div class="input-actions">
              <el-button 
                type="primary" 
                @click="handleSendMessage"
                :disabled="!inputMessage.trim() || isResponding"
                :loading="isResponding"
              >
                发送
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 角色选择对话框 -->
    <el-dialog
      v-model="showRoleSelector"
      title="选择角色"
      width="800px"
      :before-close="handleRoleSelectorClose"
    >
      <div class="role-selector">
        <!-- 搜索 -->
        <div class="search-section">
          <el-input
            v-model="roleSearchQuery"
            placeholder="搜索角色..."
            :prefix-icon="Search"
            clearable
          />
        </div>

        <!-- 角色列表 -->
        <div class="available-roles">
          <div 
            v-for="role in filteredAvailableRoles" 
            :key="role.id"
            class="available-role-card"
            :class="{ selected: isRoleSelected(role.id) }"
            @click="toggleRole(role)"
          >
            <div class="role-avatar" :style="{ backgroundColor: getRoleColor(role.id) }">
              {{ role.name[0] }}
            </div>
            <div class="role-info">
              <div class="role-name">{{ role.name }}</div>
              <div class="role-description">{{ role.description }}</div>
              <div class="role-category">{{ role.category }}</div>
            </div>
            <div class="selection-indicator">
              <el-icon v-if="isRoleSelected(role.id)"><Check /></el-icon>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleRoleSelectorClose">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmRoleSelection"
            :disabled="selectedRoles.length === 0"
          >
            确定 ({{ selectedRoles.length }}/5)
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, 
  Plus, 
  Close, 
  Search, 
  Check 
} from '@element-plus/icons-vue'
import { marked } from 'marked'
import type { Role } from '@/types'
import { roleAPI, chatroomAPI } from '@/api'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()

// 聊天室信息
interface ChatRoomInfo {
  id: number
  chatRoomId: string | number
  name?: string
  description?: string
}

// 消息类型
interface ChatMessage {
  id: number
  senderType: 'user' | 'ai' | 'system'
  roleId?: number
  content: string
  sentAt: string
}

// 响应式数据
const chatroomInfo = ref<ChatRoomInfo | null>(null)
const selectedRoles = ref<Role[]>([])
const availableRoles = ref<Role[]>([])
const messages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const isResponding = ref(false)
const showRoleSelector = ref(false)
const roleSearchQuery = ref('')
const messagesContainer = ref<HTMLElement>()

// 计算属性
const filteredAvailableRoles = computed(() => {
  if (!roleSearchQuery.value) return availableRoles.value
  
  const query = roleSearchQuery.value.toLowerCase()
  return availableRoles.value.filter(role => 
    role.name.toLowerCase().includes(query) ||
    role.description.toLowerCase().includes(query) ||
    role.category.toLowerCase().includes(query)
  )
})

// 页面初始化
onMounted(async () => {
  const chatRoomId = route.params.id as string
  await loadChatroomInfo(chatRoomId)
  await loadAvailableRoles()
  await loadChatroomMessages(chatRoomId) // 添加加载历史消息
})

// 加载聊天室历史消息
const loadChatroomMessages = async (chatRoomId: string) => {
  try {
    console.log('开始加载聊天室历史消息，chatRoomId:', chatRoomId)
    const response = await chatroomAPI.getChatroomMessages(chatRoomId)
    console.log('获取历史消息响应:', response)
    
    let messageData: any[] = []
    
    if (response && response.success && response.data) {
      // 标准格式：{success: true, data: PagedResponse<Message>}
      if (response.data.data && Array.isArray(response.data.data)) {
        messageData = response.data.data
      } else if (Array.isArray(response.data)) {
        messageData = response.data
      }
    } else if (response && Array.isArray(response)) {
      // 直接返回消息数组
      messageData = response
    }
    
    console.log('历史消息数据:', messageData)
    
    // 转换消息格式
    const chatMessages: ChatMessage[] = messageData.map((msg: any) => ({
      id: msg.id,
      senderType: msg.senderType,
      roleId: msg.roleId,
      content: msg.content,
      sentAt: msg.sentAt || msg.createTime
    }))
    
    // 按时间排序（最早的在前面）
    chatMessages.sort((a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime())
    
    messages.value = chatMessages
    console.log('加载的历史消息数量:', messages.value.length)
    
    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('加载历史消息失败:', error)
    ElMessage.error('加载历史消息失败')
  }
}

// 加载聊天室信息
const loadChatroomInfo = async (chatRoomId: string) => {
  try {
    const response = await chatroomAPI.getChatroomInfo(chatRoomId)
    // 检查响应格式并处理
    if (response && response.success && response.data) {
      // 标准格式：{success: true, data: ChatRoom}
      chatroomInfo.value = {
        id: response.data.id,
        chatRoomId: response.data.chatRoomId,
        name: response.data.name,
        description: response.data.description
      }
    } else if (response && (response as any).id) {
      // 直接返回ChatRoom对象的格式
      const chatRoom = response as any
      chatroomInfo.value = {
        id: chatRoom.id,
        chatRoomId: chatRoom.chatRoomId,
        name: chatRoom.name || chatRoom.title || '未命名聊天室',
        description: chatRoom.description
      }
    } else {
      ElMessage.error('加载聊天室信息失败')
    }
    
    // 加载聊天室已有的角色
    await loadSelectedRoles(chatRoomId)
  } catch (error) {
    console.error('加载聊天室信息失败:', error)
    ElMessage.error('加载聊天室信息失败')
  }
}

// 加载聊天室已选择的角色
const loadSelectedRoles = async (chatRoomId: string) => {
  try {
    console.log('开始加载聊天室角色，chatRoomId:', chatRoomId)
    const response = await chatroomAPI.getChatroomRoles(chatRoomId)
    console.log('getChatroomRoles完整响应:', response)
    
    // 响应拦截器可能直接返回数据数组，而不是包装的{data: ...}格式
    let roleData: any[] = []
    
    if (Array.isArray(response)) {
      // 响应拦截器直接返回了数据数组
      roleData = response
      console.log('响应是数组格式，直接使用:', roleData)
    } else if (response && response.data && Array.isArray(response.data)) {
      // 标准的ApiResponse格式
      roleData = response.data
      console.log('响应是标准ApiResponse格式:', roleData)
    } else {
      console.log('响应格式不正确:', response)
      selectedRoles.value = []
      return
    }
    
    console.log('聊天室角色数据:', roleData)
    console.log('数据长度:', roleData.length)
    
    // 打印每个数据项的详细信息
    roleData.forEach((item: any, index: number) => {
      console.log(`数据项 ${index}:`, JSON.stringify(item, null, 2))
      console.log(`数据项 ${index} roleId:`, item.roleId, typeof item.roleId)
      console.log(`数据项 ${index} 所有属性:`, Object.keys(item))
    })
    
    // 从角色ID列表获取完整的角色信息，过滤掉roleId为null或undefined的记录
    const roleIds = roleData
      .map((item: any) => item.roleId)
      .filter((roleId: any) => roleId !== null && roleId !== undefined && roleId !== 0)
    console.log('提取的角色ID列表:', roleIds)
    
    if (roleIds.length === 0) {
      console.log('聊天室中没有有效角色，selectedRoles将为空')
      selectedRoles.value = []
      return
    }
    
    const rolePromises = roleIds.map((roleId: number) => roleAPI.getRoleById(roleId))
    const roleResponses = await Promise.all(rolePromises)
    console.log('角色详情响应:', roleResponses)
    
    // 处理角色详情响应，同样需要适配响应格式
    selectedRoles.value = roleResponses
      .filter(res => {
        // 检查是否有有效的角色数据
        if (Array.isArray(res)) return false // 不应该是数组
        return res && (res.data || (res as any).id) // 有data字段或者直接是角色对象
      })
      .map(res => {
        // 提取角色数据
        if ((res as any).data) {
          return (res as any).data // 标准ApiResponse格式
        } else {
          return res as any // 直接是角色对象
        }
      })
    
    console.log('最终加载的角色列表:', selectedRoles.value)
  } catch (error) {
    console.error('加载聊天室角色失败:', error)
    selectedRoles.value = []
  }
}

// 加载可用角色
const loadAvailableRoles = async () => {
  try {
    // 调用API获取角色列表
    const response = await roleAPI.getAllPublicRoles()
    if (response.success && response.data) {
      availableRoles.value = response.data
    } else {
      ElMessage.error('加载角色列表失败')
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error('加载角色列表失败')
  }
}

// 角色选择相关
const isRoleSelected = (roleId: number) => {
  return selectedRoles.value.some(role => role.id === roleId)
}

const toggleRole = (role: Role) => {
  if (isRoleSelected(role.id)) {
    selectedRoles.value = selectedRoles.value.filter(r => r.id !== role.id)
  } else {
    if (selectedRoles.value.length < 5) {
      selectedRoles.value.push(role)
    } else {
      ElMessage.warning('最多只能选择5个角色')
    }
  }
}

const removeRole = async (roleId: number) => {
  if (!chatroomInfo.value) {
    ElMessage.error('聊天室信息未加载')
    return
  }

  try {
    // 首先获取聊天室中该角色的记录ID
    const roleRecord = await chatroomAPI.getChatroomRoleRecord(
      chatroomInfo.value.chatRoomId, 
      roleId
    )
    
    console.log('获取到的角色记录:', roleRecord)
    
    if (roleRecord.data && roleRecord.data.id) {
      // 调用后端API删除角色关联
      await chatroomAPI.removeRoleFromChatroom(roleRecord.data.id)
      
      // 从前端列表中移除
      selectedRoles.value = selectedRoles.value.filter(role => role.id !== roleId)
      
      ElMessage.success('角色移除成功')
    } else {
      console.error('角色记录数据异常:', roleRecord)
      ElMessage.error('未找到角色记录或记录ID缺失')
    }
  } catch (error) {
    console.error('移除角色失败:', error)
    ElMessage.error('移除角色失败，请重试')
  }
}

const confirmRoleSelection = async () => {
  if (!chatroomInfo.value) {
    ElMessage.error('聊天室信息未加载')
    return
  }

  try {
    // 从用户状态管理中获取当前用户ID
    const authStore = useAuthStore()
    if (!authStore.userInfo?.id) {
      ElMessage.error('用户未登录')
      return
    }
    
    const userId = authStore.userInfo.id
    
    // 获取当前聊天室已有的角色
    const existingRolesResponse = await chatroomAPI.getChatroomRoles(chatroomInfo.value.chatRoomId)
    const existingRoleIds = new Set(
      existingRolesResponse.data
        ?.map((item: any) => item.roleId)
        .filter((roleId: any) => roleId !== null && roleId !== undefined && roleId !== 0) || []
    )
    
    // 过滤出需要添加的新角色（不在聊天室中的角色）
    const newRoles = selectedRoles.value.filter(role => 
      !existingRoleIds.has(role.id)
    )
    
    console.log('准备添加的新角色:', newRoles)
    console.log('已存在的角色ID:', Array.from(existingRoleIds))
    
    // 为每个新角色调用添加API
    for (const role of newRoles) {
      console.log(`正在添加角色 ${role.name} (ID: ${role.id}) 到聊天室`)
      await chatroomAPI.addRoleToChatroom(
        chatroomInfo.value.chatRoomId, 
        role.id, 
        userId
      )
    }
    
    if (newRoles.length > 0) {
      ElMessage.success(`成功添加 ${newRoles.length} 个角色到聊天室`)
      // 重新加载聊天室角色
      await loadSelectedRoles(chatroomInfo.value.chatRoomId)
    } else {
      ElMessage.info('所选角色已在聊天室中')
    }
    
    showRoleSelector.value = false
    roleSearchQuery.value = ''
  } catch (error) {
    console.error('添加角色失败:', error)
    ElMessage.error('添加角色失败，请重试')
  }
}

const handleRoleSelectorClose = () => {
  showRoleSelector.value = false
  roleSearchQuery.value = ''
}

// 发送消息
const handleSendMessage = async () => {
  if (!inputMessage.value.trim() || isResponding.value) return
  if (selectedRoles.value.length === 0) {
    ElMessage.warning('请先选择角色')
    return
  }

  const userMessage: ChatMessage = {
    id: Date.now(),
    senderType: 'user',
    content: inputMessage.value.trim(),
    sentAt: new Date().toISOString()
  }

  messages.value.push(userMessage)
  const messageContent = inputMessage.value.trim()
  inputMessage.value = ''
  isResponding.value = true

  await nextTick()
  scrollToBottom()

  try {
    // 使用SSE连接进行协作聊天
    const chatRoomId = chatroomInfo.value?.chatRoomId || route.params.id as string
    const roleIds = selectedRoles.value.map(r => r.id).join(',')
    
    const eventSource = new EventSource(
      `/api/sse/collaborate?chatRoomId=${encodeURIComponent(chatRoomId)}&userMessage=${encodeURIComponent(messageContent)}&roleIds=${roleIds}`
    )

    let currentRoleMessage: { [roleId: string]: string } = {}
    let roleMessageIds: { [roleId: string]: number } = {} // 存储每个角色的消息ID

    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        
        switch (data.type) {
          case 'START':
            console.log('协作对话开始')
            break
            
          case 'ROLE_START':
            console.log(`角色 ${data.roleName} 开始响应`)
            currentRoleMessage[data.roleId] = ''
            
            // 为每个角色创建唯一的消息ID并立即创建消息占位符
            const messageId = Date.now() + Number(data.roleId)
            roleMessageIds[data.roleId] = messageId
            
            const aiMessage: ChatMessage = {
              id: messageId,
              senderType: 'ai',
              roleId: Number(data.roleId),
              content: '',
              sentAt: new Date().toISOString()
            }
            messages.value.push(aiMessage)
            scrollToBottom()
            break
            
          case 'ROLE_MESSAGE':
            // 累积角色消息内容
            if (data.roleId && roleMessageIds[data.roleId]) {
              currentRoleMessage[data.roleId] = (currentRoleMessage[data.roleId] || '') + data.message
              
              // 查找并更新现有消息
              const existingMessageIndex = messages.value.findIndex(
                msg => msg.id === roleMessageIds[data.roleId]
              )
              
              if (existingMessageIndex >= 0) {
                // 更新现有消息内容
                messages.value[existingMessageIndex].content = currentRoleMessage[data.roleId]
                scrollToBottom()
              }
            }
            break
            
          case 'ROLE_COMPLETE':
            console.log(`角色 ${data.roleName} 响应完成`)
            break
            
          case 'COMPLETE':
            console.log('协作对话完成')
            isResponding.value = false
            eventSource.close()
            // 清理临时数据
            currentRoleMessage = {}
            roleMessageIds = {}
            break
            
          case 'ERROR':
            console.error('协作对话错误:', data.message)
            ElMessage.error(`协作对话失败: ${data.message}`)
            isResponding.value = false
            eventSource.close()
            // 清理临时数据
            currentRoleMessage = {}
            roleMessageIds = {}
            break
        }
      } catch (error) {
        console.error('解析SSE消息失败:', error)
      }
    }

    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error)
      ElMessage.error('连接服务器失败')
      isResponding.value = false
      eventSource.close()
      // 清理临时数据
      currentRoleMessage = {}
      roleMessageIds = {}
    }

  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
    isResponding.value = false
  }
}

// 工具函数
const getRoleColor = (roleId: number) => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
  return colors[roleId % colors.length]
}

const getRoleName = (roleId?: number) => {
  if (!roleId) return '🤖'
  
  // 先从已选择的角色中查找
  let role = selectedRoles.value.find(r => r.id === roleId)
  
  // 如果没找到，再从所有可用角色中查找
  if (!role) {
    role = availableRoles.value.find(r => r.id === roleId)
  }
  
  return role?.name || '🤖'
}

const renderMarkdown = (content: string) => {
  return marked(content)
}

const shouldShowTimeLabel = (prevTime?: string, currentTime?: string) => {
  if (!prevTime || !currentTime) return true
  
  const prev = new Date(prevTime)
  const current = new Date(currentTime)
  const diffMinutes = (current.getTime() - prev.getTime()) / (1000 * 60)
  
  return diffMinutes > 5
}

const formatMessageTime = (timeStr?: string) => {
  if (!timeStr) return ''
  
  const date = new Date(timeStr)
  const now = new Date()
  
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } else {
    return date.toLocaleDateString('zh-CN', { 
      month: 'short', 
      day: 'numeric',
      hour: '2-digit', 
      minute: '2-digit' 
    })
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.chatroom-detail-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.header {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
  flex-shrink: 0;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  color: #606266;
  font-size: 14px;
}

.back-btn:hover {
  color: #409eff;
}

.chatroom-info .title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px 0;
}

.chatroom-info .description {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.main-content {
  flex: 1;
  display: flex;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  gap: 24px;
  padding: 24px;
  overflow: hidden;
}

.roles-section {
  width: 300px;
  background: white;
  border-radius: 8px;
  padding: 20px;
  flex-shrink: 0;
  overflow-y: auto;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.role-count {
  font-size: 12px;
  color: #909399;
}

.empty-roles {
  text-align: center;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.selected-roles {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.role-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fafafa;
}

.role-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.role-info {
  flex: 1;
  min-width: 0;
}

.role-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.role-description {
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
}

.remove-btn {
  color: #909399;
  padding: 4px;
}

.remove-btn:hover {
  color: #f56c6c;
}

.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-time-divider {
  text-align: center;
  font-size: 12px;
  color: #909399;
  margin: 8px 0;
}

.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.message-item.user .message-avatar {
  background: #67c23a;
}

.message-item.system .message-avatar {
  background: #909399;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  background: #f0f2f5;
  position: relative;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.message-item.user .message-content {
  background: #409eff;
  color: white;
}

.role-label {
  font-size: 12px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 4px;
}

.message-item.user .role-label {
  color: rgba(255, 255, 255, 0.8);
}

.message-text {
  line-height: 1.5;
  word-break: break-word;
}

.loading .message-content {
  background: #f0f2f5;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #909399;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.input-section {
  border-top: 1px solid #e4e7ed;
  padding: 20px;
  flex-shrink: 0;
}

.input-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
}

.role-selector {
  max-height: 500px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-section {
  flex-shrink: 0;
}

.available-roles {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.available-role-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.available-role-card:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.available-role-card.selected {
  border-color: #409eff;
  background: #e6f7ff;
}

.available-role-card .role-info {
  flex: 1;
}

.available-role-card .role-category {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.selection-indicator {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>