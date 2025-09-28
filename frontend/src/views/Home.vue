<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <img src="/robot-logo.svg" alt="Agent广场" class="logo-image">
          <h1>Agent广场</h1>
        </div>
        
        <div class="search-section">
          <el-input
            v-model="searchQuery"
            placeholder="搜索角色..."
            prefix-icon="Search"
            clearable
            size="large"
            style="max-width: 400px"
            @input="handleSearch"
            @clear="handleSearchClear"
          />
          <el-button 
            type="primary" 
            size="large"
            @click="$router.push('/chatrooms')"
            class="chatroom-btn"
          >
            <el-icon><ChatLineRound /></el-icon>
            聊天室
          </el-button>
        </div>
        
        <div class="user-section">
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar 
                :src="authStore.userInfo?.avatar" 
                :size="40"
              >
                {{ authStore.userInfo?.username?.[0]?.toUpperCase() }}
              </el-avatar>
              <span class="username">{{ authStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人信息
                </el-dropdown-item>
                <el-dropdown-item command="chats">
                  <el-icon><ChatDotRound /></el-icon> 我的对话
                </el-dropdown-item>
                <el-dropdown-item command="chatrooms">
                  <el-icon><ChatLineRound /></el-icon> 聊天室
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    
    <!-- 分类筛选 -->
    <div class="category-section">
      <div class="category-content">
        <el-button-group>
          <el-button 
            :type="selectedCategory === '' ? 'primary' : ''"
            @click="handleCategorySelect('')"
          >
            全部
          </el-button>
          <el-button
            v-for="category in roleStore.categories"
            :key="category"
            :type="selectedCategory === category ? 'primary' : ''"
            @click="handleCategorySelect(category)"
          >
            {{ category }}
          </el-button>
        </el-button-group>
      </div>
    </div>
    
    <!-- 角色展示区域 -->
    <main class="main-content">
      <div class="roles-container">
        <!-- 加载状态 -->
        <div v-if="roleStore.isLoading" class="loading-section">
          <el-skeleton :rows="3" animated />
        </div>
        
        <!-- 角色网格 -->
        <div v-else class="roles-grid">
          <!-- 调试信息 -->
          <div v-if="roleStore.roles && roleStore.roles.length === 0" style="grid-column: 1/-1; padding: 20px; text-align: center; color: #666;">
            调试信息: roleStore.roles = {{ roleStore.roles }}<br>
            roleStore.isLoading = {{ roleStore.isLoading }}<br>
            roles数组长度: {{ roleStore.roles?.length || 0 }}
          </div>
          
          <div
            v-for="role in roleStore.roles"
            :key="role.id"
            class="role-card"
            @click="startChat(role)"
          >
            <div class="role-avatar">
              <el-avatar :size="80" :src="role.avatar">
                {{ role.name[0] }}
              </el-avatar>
            </div>
            
            <div class="role-info">
              <h3 class="role-name">{{ role.name }}</h3>
              <p class="role-description">{{ role.description }}</p>
              <div class="role-category">
                <el-tag size="small" type="info">{{ role.category }}</el-tag>
              </div>
            </div>
            
            <div class="role-actions">
              <el-button type="primary" size="small">
                开始聊天
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="!roleStore.isLoading && roleStore.roles.length === 0" class="empty-section">
          <el-empty
            description="没有找到符合条件的角色"
            :image-size="200"
          >
            <template #image>
              <div class="empty-icon">🎭</div>
            </template>
            <el-button type="primary" @click="handleSearchClear">
              查看全部角色
            </el-button>
          </el-empty>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowDown, 
  User, 
  ChatDotRound, 
  ChatLineRound,
  SwitchButton
} from '@element-plus/icons-vue'
import { useAuthStore, useRoleStore } from '@/stores'
import { chatAPI } from '@/api'
import type { Role } from '@/types'

console.log('Home.vue script setup 执行了')

const router = useRouter()
const authStore = useAuthStore()
const roleStore = useRoleStore()

console.log('Home.vue stores 初始化完成')
console.log('authStore:', authStore)
console.log('roleStore:', roleStore)

// 搜索和分类状态
const searchQuery = ref('')
const selectedCategory = ref('')

// 页面初始化
onMounted(async () => {
  console.log('Home组件挂载，开始获取角色数据')
  console.log('roleStore:', roleStore)
  console.log('roleStore.fetchRoles:', roleStore.fetchRoles)
  
  try {
    await roleStore.fetchRoles()
    console.log('角色数据获取完成，角色数量:', roleStore.roles?.length || 0)
    console.log('角色数据:', roleStore.roles)
    console.log('分类数据:', roleStore.categories)
  } catch (error) {
    console.error('获取角色数据时出错:', error)
  }
})

// 搜索处理
let searchTimeout: number | null = null
const handleSearch = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  
  searchTimeout = setTimeout(async () => {
    if (searchQuery.value.trim()) {
      await roleStore.searchRoles(searchQuery.value.trim())
    } else {
      await roleStore.fetchRoles()
    }
    selectedCategory.value = '' // 清除分类选择
  }, 500)
}

// 清除搜索
const handleSearchClear = async () => {
  searchQuery.value = ''
  selectedCategory.value = ''
  await roleStore.fetchRoles()
}

// 分类选择
const handleCategorySelect = async (category: string) => {
  selectedCategory.value = category
  searchQuery.value = '' // 清除搜索
  
  if (category) {
    await roleStore.fetchRolesByCategory(category)
  } else {
    await roleStore.fetchRoles()
  }
}

// 开始聊天
const startChat = async (role: Role) => {
  try {
    const response = await chatAPI.createChat({ roleId: role.id })
    
    if (response.data) {
      router.push(`/chat/${response.data.id}`)
    }
  } catch (error) {
    ElMessage.error('创建聊天失败')
  }
}

// 用户菜单处理
const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'chats':
      // TODO: 跳转到对话列表页面
      ElMessage.info('对话列表功能开发中...')
      break
    case 'chatrooms':
      router.push('/chatrooms')
      break
    case 'logout':
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
      break
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  position: relative;
  z-index: 2;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-image {
  width: 32px;
  height: 32px;
}

.logo h1 {
  font-size: 20px;
  color: #000;
  margin: 0;
  font-weight: 600;
}

@keyframes holographicText {
  0%, 100% { filter: hue-rotate(0deg); }
  50% { filter: hue-rotate(180deg); }
}

/* 搜索框样式 */
.search-section {
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  z-index: 2;
}

/* Element Plus 搜索框样式 */
:deep(.el-input) {
  position: relative;
}

:deep(.el-input__wrapper) {
  background: #fff !important;
  border: 1px solid #dcdfe6 !important;
  border-radius: 4px !important;
  box-shadow: none !important;
  transition: border-color 0.2s ease !important;
}

:deep(.el-input__wrapper:hover) {
  border-color: #c0c4cc !important;
}

:deep(.el-input__wrapper:focus-within) {
  border-color: #409eff !important;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2) !important;
}

:deep(.el-input__inner) {
  color: #606266 !important;
  font-weight: normal;
}

:deep(.el-input__inner::placeholder) {
  color: #c0c4cc !important;
}

:deep(.el-input__prefix) {
  color: #c0c4cc !important;
}

/* 聊天室按钮样式 */
.chatroom-btn {
  background: #409eff !important;
  border: 1px solid #409eff !important;
  border-radius: 4px !important;
  color: #fff !important;
  font-weight: normal;
  padding: 12px 24px !important;
  transition: all 0.3s ease;
}

.chatroom-btn:hover {
  background: #337ecc !important;
  border-color: #337ecc !important;
}

/* 用户下拉菜单样式 */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 4px;
  background: #fff;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: #f5f7fa;
  border-color: #c0c4cc;
}

.username {
  font-weight: 500;
  color: #303133;
}

/* Element Plus 下拉菜单样式 */
:deep(.el-dropdown-menu) {
  background: #fff !important;
  border: 1px solid #e4e7ed !important;
  border-radius: 4px !important;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1) !important;
  padding: 6px 0 !important;
}

:deep(.el-dropdown-menu__item) {
  color: #606266 !important;
  padding: 0 20px !important;
  line-height: 36px !important;
  font-size: 14px !important;
}

:deep(.el-dropdown-menu__item:hover) {
  background: #f5f7fa !important;
  color: #409eff !important;
}

.category-section {
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  padding: 16px 24px;
}

.category-content {
  max-width: 1200px;
  margin: 0 auto;
}

/* 分类按钮样式 */
:deep(.el-button-group .el-button) {
  background: #fff !important;
  border: 1px solid #dcdfe6 !important;
  color: #606266 !important;
  transition: all 0.3s ease;
}

:deep(.el-button-group .el-button:hover) {
  background: #ecf5ff !important;
  border-color: #b3d8ff !important;
  color: #409eff !important;
}

:deep(.el-button-group .el-button--primary) {
  background: #409eff !important;
  border-color: #409eff !important;
  color: #fff !important;
}

.main-content {
  padding: 32px 24px;
  position: relative;
  z-index: 2;
}

.roles-container {
  max-width: 1200px;
  margin: 0 auto;
}

.roles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-top: 24px;
}

.empty-section {
  text-align: center;
  padding: 80px 20px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 16px 64px rgba(0, 0, 0, 0.2);
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 32px;
  opacity: 0.8;
  animation: floatIcon 3s ease-in-out infinite;
  filter: drop-shadow(0 0 20px rgba(255, 255, 255, 0.3));
}

@keyframes floatIcon {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

/* Element Plus Empty 组件样式 */
:deep(.el-empty__description) {
  color: rgba(255, 255, 255, 0.8) !important;
  font-size: 16px;
  margin-bottom: 24px;
}

:deep(.el-empty .el-button--primary) {
  background: linear-gradient(135deg, 
    rgba(0, 255, 255, 0.3) 0%, 
    rgba(255, 0, 255, 0.3) 100%) !important;
  border: 2px solid rgba(0, 255, 255, 0.5) !important;
  border-radius: 20px !important;
  color: #fff !important;
  font-weight: 600;
  padding: 12px 24px !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 
    0 0 20px rgba(0, 255, 255, 0.3),
    0 8px 25px rgba(0, 0, 0, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-empty .el-button--primary:hover) {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 
    0 0 30px rgba(0, 255, 255, 0.5),
    0 12px 35px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 0, 255, 0.8) !important;
}

.search-section {
  display: flex;
  gap: 16px;
  align-items: center;
}

.chatroom-btn {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    height: auto;
    padding: 16px 0;
    gap: 16px;
  }
  
  .search-section {
    width: 100%;
    display: flex;
    gap: 12px;
    align-items: center;
  }
  
  .chatroom-btn {
    flex-shrink: 0;
  }
  
  .search-section .el-input {
    max-width: none;
  }
  
  .main-content {
    padding: 16px;
  }
  
  .roles-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .category-content {
    overflow-x: auto;
  }
  
  .el-button-group {
    white-space: nowrap;
  }
  /* 角色卡片样式 - 参考聊天界面设计 */
  .role-card {
    background: linear-gradient(135deg, 
      rgba(255, 255, 255, 0.12) 0%,
      rgba(255, 255, 255, 0.08) 50%,
      rgba(255, 255, 255, 0.05) 100%
    );
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    padding: 24px;
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    position: relative;
    overflow: hidden;
    box-shadow: 
      0 8px 32px rgba(0, 0, 0, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.1);
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 16px;
    min-height: 280px;
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
      0 0 40px rgba(102, 126, 234, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
  }

  /* 角色卡片发光效果 */
  .role-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.6s ease;
  }

  .role-card:hover::before {
    left: 100%;
  }

  /* 角色头像容器 */
  .role-avatar {
    position: relative;
    margin-bottom: 8px;
  }

  /* Element Plus 头像样式覆盖 */
  .role-card :deep(.el-avatar) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
    border: 3px solid rgba(255, 255, 255, 0.3);
    box-shadow: 
      0 8px 25px rgba(102, 126, 234, 0.4),
      inset 0 2px 0 rgba(255, 255, 255, 0.2);
    font-size: 32px !important;
    font-weight: 700;
    color: #fff !important;
    transition: all 0.3s ease;
  }

  .role-card:hover :deep(.el-avatar) {
    transform: scale(1.1);
    box-shadow: 
      0 12px 35px rgba(102, 126, 234, 0.6),
      inset 0 2px 0 rgba(255, 255, 255, 0.3);
  }

  /* 角色信息区域 */
  .role-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    width: 100%;
  }

  .role-name {
    font-size: 20px;
    font-weight: 700;
    color: #fff;
    margin: 0;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
    letter-spacing: 0.5px;
    line-height: 1.2;
  }

  .role-description {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.85);
    line-height: 1.5;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
    font-weight: 400;
    max-height: 63px;
  }

  .role-category {
    margin-top: 4px;
  }

  /* Element Plus 标签样式覆盖 */
  .role-card :deep(.el-tag) {
    background: rgba(102, 126, 234, 0.2) !important;
    color: rgba(255, 255, 255, 0.9) !important;
    border: 1px solid rgba(102, 126, 234, 0.4) !important;
    border-radius: 12px !important;
    font-weight: 600 !important;
    font-size: 12px !important;
    padding: 4px 12px !important;
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
  }

  /* 角色操作按钮 */
  .role-actions {
    width: 100%;
    margin-top: auto;
  }

  .role-actions :deep(.el-button) {
    width: 100% !important;
    background: linear-gradient(135deg, 
      rgba(102, 126, 234, 0.3) 0%, 
      rgba(118, 75, 162, 0.3) 100%) !important;
    border: 2px solid rgba(102, 126, 234, 0.5) !important;
    border-radius: 16px !important;
    color: #fff !important;
    font-weight: 600 !important;
    padding: 12px 24px !important;
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    box-shadow: 
      0 4px 15px rgba(102, 126, 234, 0.3),
      0 2px 8px rgba(0, 0, 0, 0.2);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
    position: relative;
    overflow: hidden;
  }

  .role-actions :deep(.el-button::before) {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, 
      transparent, 
      rgba(255, 255, 255, 0.2), 
      transparent
    );
    transition: left 0.5s;
  }

  .role-actions :deep(.el-button:hover) {
    transform: translateY(-2px) scale(1.02) !important;
    box-shadow: 
      0 8px 25px rgba(102, 126, 234, 0.5),
      0 4px 15px rgba(0, 0, 0, 0.3) !important;
    border-color: rgba(118, 75, 162, 0.8) !important;
    background: linear-gradient(135deg, 
      rgba(102, 126, 234, 0.4) 0%, 
      rgba(118, 75, 162, 0.4) 100%) !important;
  }

  .role-actions :deep(.el-button:hover::before) {
    left: 100%;
  }
}
</style>