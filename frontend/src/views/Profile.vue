<template>
  <div class="profile-container">
    <div class="profile-card">
      <header class="profile-header">
        <h2>个人信息</h2>
        <el-button @click="goBack">返回</el-button>
      </header>
      
      <div class="profile-content">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-avatar :size="100" :src="authStore.userInfo?.avatar">
              {{ authStore.userInfo?.username[0]?.toUpperCase() }}
            </el-avatar>
            <div class="avatar-overlay" @click="showAvatarDialog = true">
              <el-icon><Camera /></el-icon>
            </div>
          </div>
          <div class="avatar-actions">
            <el-button size="small" @click="showAvatarDialog = true">更换头像</el-button>
          </div>
        </div>
        
        <div class="info-section">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">
              {{ authStore.userInfo?.username }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ authStore.userInfo?.email }}
            </el-descriptions-item>
            <el-descriptions-item label="注册时间">
              {{ formatDate(authStore.userInfo?.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="账户状态">
              <el-tag :type="authStore.userInfo?.active ? 'success' : 'danger'">
                {{ authStore.userInfo?.active ? '正常' : '已停用' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="actions-section">
          <el-button type="danger" @click="handleLogout">
            退出登录
          </el-button>
        </div>
      </div>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog
      v-model="showAvatarDialog"
      title="更换头像"
      width="500px"
      :before-close="handleAvatarDialogClose"
    >
      <div class="avatar-upload-container">
        <!-- 当前头像预览 -->
        <div class="current-avatar">
          <h4>当前头像</h4>
          <el-avatar :size="80" :src="authStore.userInfo?.avatar">
            {{ authStore.userInfo?.username[0]?.toUpperCase() }}
          </el-avatar>
        </div>

        <!-- 上传区域 -->
        <div class="upload-area">
          <el-upload
            ref="uploadRef"
            class="avatar-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :on-error="handleAvatarError"
            :before-upload="beforeAvatarUpload"
            :on-progress="handleUploadProgress"
            accept="image/*"
            drag
          >
            <div v-if="!previewUrl" class="upload-placeholder">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <div class="upload-text">
                <p>点击或拖拽图片到此处上传</p>
                <p class="upload-tip">支持 JPG、PNG、GIF 格式，文件大小不超过 2MB</p>
              </div>
            </div>
            <div v-else class="preview-container">
              <img :src="previewUrl" class="preview-image" alt="预览图片" />
              <div class="preview-overlay">
                <el-icon><Edit /></el-icon>
                <span>点击重新选择</span>
              </div>
            </div>
          </el-upload>
        </div>

        <!-- 上传进度 -->
        <div v-if="uploading" class="upload-progress">
          <el-progress :percentage="uploadProgress" :show-text="true" />
          <p>正在上传头像...</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleAvatarDialogClose">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmAvatarChange"
            :loading="uploading"
            :disabled="!previewUrl"
          >
            确认更换
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera, Plus, Edit } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores'
import { authAPI } from '@/api/auth'
import dayjs from 'dayjs'

const router = useRouter()
const authStore = useAuthStore()

// 头像上传相关状态
const showAvatarDialog = ref(false)
const previewUrl = ref('')
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadRef = ref()
const selectedFile = ref<File | null>(null)

// 上传配置
const uploadAction = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL}/api/auth/avatar`
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    'Authorization': token ? `Bearer ${token}` : ''
  }
})

const goBack = () => {
  router.go(-1)
}

const formatDate = (date: string | undefined) => {
  if (!date) return '未知'
  return dayjs(date).format('YYYY年MM月DD日')
}

// 头像上传前的验证
const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }

  // 保存选中的文件
  selectedFile.value = file
  
  // 创建预览URL
  const reader = new FileReader()
  reader.onload = (e) => {
    previewUrl.value = e.target?.result as string
  }
  reader.readAsDataURL(file)
  
  return false // 阻止自动上传
}

// 上传进度处理
const handleUploadProgress = (event: any) => {
  uploadProgress.value = Math.round((event.loaded / event.total) * 100)
}

// 上传成功处理
const handleAvatarSuccess = (response: any) => {
  uploading.value = false
  if (response.success) {
    ElMessage.success('头像上传成功!')
    // 更新用户信息中的头像
    if (authStore.userInfo) {
      authStore.userInfo.avatar = response.data.avatarUrl
    }
    showAvatarDialog.value = false
    resetUploadState()
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

// 上传失败处理
const handleAvatarError = (error: any) => {
  uploading.value = false
  console.error('头像上传失败:', error)
  ElMessage.error('头像上传失败，请重试')
}

// 确认更换头像
const confirmAvatarChange = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择头像文件')
    return
  }

  try {
    uploading.value = true
    uploadProgress.value = 0

    // 使用authAPI上传头像
    const response = await authAPI.updateAvatar(selectedFile.value)
    
    if (response.success && response.data) {
      // 更新用户头像
      authStore.user.avatar = response.data
      
      ElMessage.success('头像更换成功')
      handleAvatarDialogClose()
      
      // 刷新用户信息
      await authStore.fetchCurrentUser()
    } else {
      ElMessage.error(response.message || '头像更换失败')
    }
  } catch (error: any) {
    console.error('头像更换失败:', error)
    ElMessage.error(error.message || '头像更换失败，请稍后重试')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

// 关闭对话框
const handleAvatarDialogClose = () => {
  if (uploading.value) {
    ElMessage.warning('正在上传中，请稍候...')
    return
  }
  showAvatarDialog.value = false
  resetUploadState()
}

// 重置上传状态
const resetUploadState = () => {
  previewUrl.value = ''
  selectedFile.value = null
  uploading.value = false
  uploadProgress.value = 0
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    // 用户取消操作
  }
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 40px 20px;
}

.profile-card {
  max-width: 600px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.profile-header {
  background: #409eff;
  color: white;
  padding: 24px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.profile-header h2 {
  margin: 0;
  font-size: 24px;
}

.profile-content {
  padding: 40px 32px;
}

.avatar-section {
  text-align: center;
  margin-bottom: 40px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: white;
  font-size: 24px;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-actions {
  margin-top: 16px;
}

/* 头像上传对话框样式 */
.avatar-upload-container {
  padding: 20px 0;
}

.current-avatar {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.current-avatar h4 {
  margin: 0 0 15px 0;
  color: #606266;
  font-weight: 500;
}

.upload-area {
  margin-bottom: 20px;
}

.avatar-uploader {
  width: 100%;
}

.avatar-uploader :deep(.el-upload) {
  width: 100%;
}

.avatar-uploader :deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.avatar-uploader :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
  background: #f0f9ff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #8c939d;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.upload-text p {
  margin: 4px 0;
}

.upload-tip {
  font-size: 12px;
  color: #a8abb2;
}

.preview-container {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-image {
  max-width: 100%;
  max-height: 180px;
  border-radius: 8px;
  object-fit: cover;
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: white;
  cursor: pointer;
}

.preview-container:hover .preview-overlay {
  opacity: 1;
}

.preview-overlay span {
  margin-top: 8px;
  font-size: 14px;
}

.upload-progress {
  text-align: center;
  margin-top: 20px;
}

.upload-progress p {
  margin-top: 10px;
  color: #606266;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.info-section {
  margin-bottom: 40px;
}

.actions-section {
  text-align: center;
}
</style>