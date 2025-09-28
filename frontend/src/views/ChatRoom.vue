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

  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* 动态粒子背景 */
.chatroom-container::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 219, 255, 0.3) 0%, transparent 50%);
  animation: particleFloat 20s ease-in-out infinite;
  z-index: -1;
}

@keyframes particleFloat {
  0%, 100% { 
    transform: translateY(0px) rotate(0deg);
    opacity: 1;
  }
  50% { 
    transform: translateY(-20px) rotate(180deg);
    opacity: 0.8;
  }
}

/* 光影动画 */
.chatroom-container::after {
  content: '';
  position: fixed;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: conic-gradient(from 0deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  animation: lightRotate 30s linear infinite;
  z-index: -1;
}

@keyframes lightRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.header {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

}

.header-content {
  max-width: 1200px;
  margin: 0 auto;

  height: 72px;

  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;

  gap: 20px;
}

.back-btn {
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.title {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #fff 0%, rgba(255, 255, 255, 0.8) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.main-content {
  padding: 40px 32px;
  position: relative;
  z-index: 1;

}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-section {

  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-radius: 24px;
  padding: 40px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.1);

  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 32px;
}

.chatroom-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-radius: 24px;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  transform-style: preserve-3d;
}

/* 卡片悬浮和倾斜效果 */
.chatroom-card:hover {
  transform: translateY(-12px) rotateX(5deg) rotateY(5deg);
  box-shadow: 
    0 25px 60px rgba(0, 0, 0, 0.2),
    0 0 0 1px rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
}

/* 卡片内部光效 */
.chatroom-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s ease;
}

.chatroom-card:hover::before {
  left: 100%;
}

/* 卡片边框光效 */
.chatroom-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 24px;
  padding: 2px;
  background: linear-gradient(45deg, 
    rgba(255, 255, 255, 0.3), 
    transparent, 
    rgba(255, 255, 255, 0.3));
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.chatroom-card:hover::after {
  opacity: 1;

}

.card-header {
  display: flex;

  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
  position: relative;
  z-index: 2;
}

.chatroom-name {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 0;
  line-height: 1.3;
  letter-spacing: -0.3px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.chatroom-description {
  color: rgba(255, 255, 255, 0.8);
  font-size: 16px;
  line-height: 1.6;
  margin: 0 0 24px 0;

  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;

  position: relative;
  z-index: 2;

}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;

  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  position: relative;
  z-index: 2;

}

.participants {
  display: flex;
  align-items: center;

  gap: 8px;
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 12px;
  border-radius: 12px;
  color: #fff;
  font-weight: 600;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.empty-section {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-radius: 32px;
  padding: 80px 40px;
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 16px 64px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 32px;
  opacity: 0.8;
  animation: floatIcon 3s ease-in-out infinite;
}

@keyframes floatIcon {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }

}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0.1) 100%);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  font-weight: 600;
  padding: 14px 24px;
  font-size: 15px;
  color: #fff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

:deep(.el-button--primary:hover) {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.25);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0.2) 100%);
}

:deep(.el-tag) {
  border-radius: 12px;
  font-weight: 600;
  font-size: 12px;
  padding: 6px 12px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

:deep(.el-tag--success) {
  background: rgba(52, 199, 89, 0.3);
  color: #fff;
  border: 1px solid rgba(52, 199, 89, 0.5);
  box-shadow: 0 4px 15px rgba(52, 199, 89, 0.2);
}

:deep(.el-tag--info) {
  background: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

:deep(.el-dialog) {
  border-radius: 24px;
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
}

:deep(.el-dialog__header) {
  padding: 32px 32px 0;
}

:deep(.el-dialog__title) {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

:deep(.el-input__wrapper) {
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

:deep(.el-input__inner) {
  color: #fff;
}

:deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.6);
}

:deep(.el-textarea__inner) {
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

:deep(.el-textarea__inner::placeholder) {
  color: rgba(255, 255, 255, 0.6);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 0 20px;
  }
  
  .header-content {
    height: 64px;
  }
  
  .title {
    font-size: 24px;
  }
  
  .main-content {
    padding: 24px 20px;
  }
  
  .chatroom-list {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .chatroom-card {
    padding: 24px;
  }
  
  .chatroom-card:hover {
    transform: translateY(-8px) rotateX(2deg) rotateY(2deg);
  }
}

@media (max-width: 480px) {
  .header-left {
    gap: 12px;
  }
  
  .title {
    font-size: 20px;
  }
  
  .chatroom-card {
    padding: 20px;
  }
  
  .chatroom-name {
    font-size: 18px;
  }
  
  .chatroom-card:hover {
    transform: translateY(-6px);
  }
}

</style>