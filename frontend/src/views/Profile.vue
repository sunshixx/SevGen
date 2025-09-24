<template>
  <div class="profile-container">
    <div class="profile-card">
      <header class="profile-header">
        <h2>个人信息</h2>
        <el-button @click="goBack">返回</el-button>
      </header>
      
      <div class="profile-content">
        <div class="avatar-section">
          <el-avatar :size="100" :src="authStore.userInfo?.avatar">
            {{ authStore.userInfo?.username[0]?.toUpperCase() }}
          </el-avatar>
          <div class="avatar-actions">
            <el-button size="small">更换头像</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores'
import dayjs from 'dayjs'

const router = useRouter()
const authStore = useAuthStore()

const goBack = () => {
  router.go(-1)
}

const formatDate = (date: string | undefined) => {
  if (!date) return '未知'
  return dayjs(date).format('YYYY年MM月DD日')
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

.avatar-actions {
  margin-top: 16px;
}

.info-section {
  margin-bottom: 40px;
}

.actions-section {
  text-align: center;
}
</style>