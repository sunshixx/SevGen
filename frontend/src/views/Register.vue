<template>
  <div class="register-container">
    <div class="register-card">
      <div class="logo-section">
        <h1 class="app-title">🎭 AI角色聊天</h1>
        <p class="app-subtitle">创建账户，开始AI角色扮演之旅</p>
      </div>
      
      <el-form 
        ref="registerFormRef" 
        :model="registerForm" 
        :rules="registerRules"
        size="large"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="邮箱地址"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="verificationCode">
          <div class="verification-input">
            <el-input
              v-model="registerForm.verificationCode"
              placeholder="邮箱验证码"
              prefix-icon="Key"
              clearable
            />
            <el-button
              type="primary"
              :loading="isSendingCode"
              :disabled="!canSendCode || countdown > 0"
              @click="sendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input

            v-model="registerForm.confirmPassword"

            type="password"
            placeholder="确认密码"
            prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large"
            style="width: 100%"
            :loading="authStore.isLoading"
            @click="handleRegister"
          >
            {{ authStore.isLoading ? '注册中...' : '注册账户' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-link">
        <span>已有账户？</span>
        <router-link to="/login" class="link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance } from 'element-plus'
import { useAuthStore } from '@/stores'
import type { RegisterRequest } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

// 表单引用
const registerFormRef = ref<FormInstance>()

// 注册表单数据

const registerForm = reactive<RegisterRequest & { confirmPassword: string }>({
  username: '',
  email: '',
  password: '',
  verificationCode: '',
  confirmPassword: ''
})

// 发送验证码相关状态
const isSendingCode = ref(false)
const countdown = ref(0)
let countdownTimer: number | null = null

// 计算是否可以发送验证码
const canSendCode = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return registerForm.email && emailRegex.test(registerForm.email)
})

// 自定义验证函数

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {

  if (value === '') {
    callback(new Error('请确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/, message: '用户名只能包含字母、数字、下划线和中文', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在 6 到 100 个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 发送验证码
const sendCode = async () => {
  if (!canSendCode.value) {
    ElMessage.warning('请输入有效的邮箱地址')
    return
  }

  try {
    isSendingCode.value = true
    const result = await authStore.sendVerificationCode({ email: registerForm.email })
    
    if (result.success) {
      ElMessage.success('验证码已发送，请查收邮件')
      
      // 开始倒计时
      countdown.value = 60
      countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0 && countdownTimer) {
          clearInterval(countdownTimer)
          countdownTimer = null
        }
      }, 1000)
    } else {
      ElMessage.error(result.message || '发送验证码失败')
    }
  } catch (error) {
    ElMessage.error('发送验证码失败')
  } finally {
    isSendingCode.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    // 验证表单
    await registerFormRef.value.validate()
    

    // 准备注册数据（去除confirmPassword字段）
    const { confirmPassword, ...registerData } = registerForm
    
    // 执行注册
    const result = await authStore.register(registerData as RegisterRequest)

    
    if (result.success) {
      ElMessage.success('注册成功！请登录')
      // 跳转到登录页
      router.push('/login')
    } else {
      ElMessage.error(result.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
  }
}

// 清理定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 450px;
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

.verification-input {
  display: flex;
  gap: 12px;
}

.verification-input .el-input {
  flex: 1;
}

.verification-input .el-button {
  white-space: nowrap;
  min-width: 120px;
}

.login-link {
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
  .register-card {
    padding: 32px 24px;
  }
  
  .app-title {
    font-size: 28px;
  }
  
  .verification-input {
    flex-direction: column;
    gap: 8px;
  }
  
  .verification-input .el-button {
    min-width: auto;
  }
}
</style>