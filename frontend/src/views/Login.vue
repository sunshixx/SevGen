<template>
  <div class="login-container">
    <div class="login-card">
      <div class="logo-section">
        <h1 class="app-title">🎭 AI角色聊天</h1>
        <p class="app-subtitle">与AI角色开启精彩对话</p>
      </div>
      
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名或邮箱"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large"
            style="width: 100%"
            :loading="authStore.isLoading"
            @click="handleLogin"
          >
            {{ authStore.isLoading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-link">
        <span>还没有账户？</span>
        <router-link to="/register" class="link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance } from 'element-plus'
import { useAuthStore } from '@/stores'
import type { LoginRequest } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

// 表单引用
const loginFormRef = ref<FormInstance>()

// 登录表单数据
const loginForm = reactive<LoginRequest>({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在 6 到 100 个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    // 验证表单
    await loginFormRef.value.validate()
    
    console.log('开始登录，表单数据:', loginForm)
    
    // 执行登录
    const result = await authStore.login(loginForm)
    
    console.log('登录结果:', result)
    
    if (result.success) {
      ElMessage.success('登录成功！')
      
      // 等待一小段时间确保状态更新完成，然后跳转
      await new Promise(resolve => setTimeout(resolve, 100))
      
      console.log('登录成功，认证状态:', authStore.isAuthenticated)
      console.log('用户信息:', authStore.userInfo)
      console.log('Token:', localStorage.getItem('token'))
      
      // 跳转到首页
      await router.push('/')
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录过程中出现错误')
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.logo-section {
  text-align: center;
  margin-bottom: 32px;
}

.app-title {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 8px 0;
}

.app-subtitle {
  font-size: 16px;
  color: #909399;
  margin: 0 0 32px 0;
}

.el-form-item {
  margin-bottom: 24px;
}

.register-link {
  text-align: center;
  margin-top: 24px;
  color: #909399;
}

.link {
  color: #409eff;
  text-decoration: none;
  margin-left: 8px;
}

.link:hover {
  color: #66b1ff;
}

@media (max-width: 480px) {
  .login-card {
    padding: 32px 24px;
  }
  
  .app-title {
    font-size: 28px;
  }
}
</style>