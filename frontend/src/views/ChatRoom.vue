<template>
  <div class="chatroom-container">
    <!-- 头部 -->
    <div class="header">
      <div class="header-content">
        <div class="header-left">
          <el-button 
            type="text" 
            @click="$router.push('/')"
            class="back-btn"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回首页
          </el-button>
          <h1 class="title">聊天室</h1>
        </div>
        <div class="header-right">
          <el-button 
            type="primary" 
            @click="showCreateDialog = true"
            :icon="Plus"
          >
            创建聊天室
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <main class="main-content">
      <div class="content-wrapper">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-section">
          <el-skeleton :rows="3" animated />
        </div>

        <!-- 聊天室列表 -->
        <div v-else-if="chatrooms.length > 0" class="chatroom-list">
          <div 
            v-for="chatroom in chatrooms" 
            :key="chatroom.id"
            class="chatroom-card"
            @click="enterChatroom(chatroom)"
          >
            <div class="card-header">
              <h3 class="chatroom-name">{{ chatroom.name || chatroom.title || '未命名聊天室' }}</h3>
              <el-tag 
                :type="chatroom.isActive ? 'success' : 'info'"
                size="small"
              >
                {{ chatroom.isActive ? '活跃' : '空闲' }}
              </el-tag>
            </div>
            <p class="chatroom-description">{{ chatroom.description || '暂无描述' }}</p>
            <div class="card-footer">
              <div class="participants">
                <el-icon><User /></el-icon>
                <span>{{ chatroom.participantCount || 0 }} 人参与</span>
              </div>
              <div class="create-time">
                创建于 {{ formatTime(chatroom.createTime) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-section">
          <el-empty
            description="还没有聊天室"
            :image-size="200"
          >
            <template #image>
              <div class="empty-icon">💬</div>
            </template>
            <el-button 
              type="primary" 
              @click="showCreateDialog = true"
              :icon="Plus"
            >
              创建第一个聊天室
            </el-button>
          </el-empty>
        </div>
      </div>
    </main>

    <!-- 创建聊天室对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建聊天室"
      width="500px"
      :before-close="handleCreateDialogClose"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input
            v-model="createForm.name"
            placeholder="请输入聊天室名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            placeholder="请输入聊天室描述（可选）"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCreateDialogClose">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleCreateChatroom"
            :loading="creating"
          >
            创建
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { 
  ArrowLeft, 
  Plus, 
  User 
} from '@element-plus/icons-vue'
import { chatroomAPI } from '@/api'
import type { ChatRoom, CreateChatroomRequest } from '@/types'

const router = useRouter()

// 响应式数据
const loading = ref(true)
const chatrooms = ref<ChatRoom[]>([])
const showCreateDialog = ref(false)
const creating = ref(false)

// 创建表单
const createFormRef = ref<FormInstance>()
const createForm = ref({
  name: '',
  description: ''
})

const createRules: FormRules = {
  name: [
    { required: true, message: '请输入聊天室名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 页面初始化
onMounted(async () => {
  await fetchChatrooms()
})

// 获取聊天室列表
const fetchChatrooms = async () => {
  try {
    loading.value = true
    const response = await chatroomAPI.getChatroomList()
    // 检查响应格式并处理
    if (response && response.success && response.data) {
      // 标准格式：{success: true, data: ChatRoom[]}
      chatrooms.value = response.data
    } else if (response && Array.isArray(response)) {
      // 直接返回ChatRoom[]数组的格式
      chatrooms.value = response as any
    } else {
      ElMessage.error('获取聊天室列表失败')
    }
  } catch (error) {
    console.error('获取聊天室列表失败:', error)
    ElMessage.error('获取聊天室列表失败')
  } finally {
    loading.value = false
  }
}



// 进入聊天室
const enterChatroom = (chatroom: ChatRoom) => {
  router.push(`/chatroom/${chatroom.chatRoomId}`)
}

// 创建聊天室
const handleCreateChatroom = async () => {
  if (!createFormRef.value) return
  
  try {
    const valid = await createFormRef.value.validate()
    if (!valid) return
    
    creating.value = true
    
    const requestData: CreateChatroomRequest = {
      title: createForm.value.name,
      description: createForm.value.description
    }
    
    const response = await chatroomAPI.createChatroom(requestData)
    // 检查响应格式并处理
    if (response && response.success && response.data) {
      // 标准格式：{success: true, data: ChatRoom}
      chatrooms.value.unshift(response.data)
      ElMessage.success('聊天室创建成功')
      handleCreateDialogClose()
    } else if (response && (response as any).id) {
      // 直接返回ChatRoom对象的格式
      chatrooms.value.unshift(response as any)
      ElMessage.success('聊天室创建成功')
      handleCreateDialogClose()
    } else {
      ElMessage.error('创建聊天室失败')
    }
  } catch (error) {
    console.error('创建聊天室失败:', error)
    ElMessage.error('创建聊天室失败')
  } finally {
    creating.value = false
  }
}

// 关闭创建对话框
const handleCreateDialogClose = () => {
  showCreateDialog.value = false
  createForm.value = {
    name: '',
    description: ''
  }
  createFormRef.value?.clearValidate()
}

// 格式化时间
const formatTime = (timeStr?: string) => {
  if (!timeStr) return '未知'
  
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString()
  }
}
</script>

<style scoped>
.chatroom-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
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

.title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.main-content {
  padding: 24px;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
}

.chatroom-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.chatroom-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.3s ease;
}

.chatroom-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.chatroom-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.chatroom-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.participants {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-section {
  background: white;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>