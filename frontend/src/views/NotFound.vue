<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-icon">🤖</div>
      <h1>404</h1>
      <p>抱歉，您访问的页面不存在</p>
      <div class="actions">
        <el-button type="primary" @click="goHome">
          返回首页
        </el-button>
        <el-button @click="goBack">
          返回上页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.not-found-content {
  text-align: center;
  padding: 40px;
}

.error-icon {
  font-size: 120px;
  margin-bottom: 20px;
}

h1 {
  font-size: 72px;
  color: #909399;
  margin: 0 0 16px 0;
}

p {
  font-size: 18px;
  color: #606266;
  margin: 0 0 32px 0;
}

.actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}
</style>