<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <h1>🎭 AI角色聊天</h1>
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.logo h1 {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-weight: 500;
  color: #303133;
}

.category-section {
  background: white;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
}

.category-content {
  max-width: 1200px;
  margin: 0 auto;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.roles-container {
  min-height: 60vh;
}

.loading-section {
  padding: 40px;
}

.roles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.role-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.role-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border-color: #409eff;
}

.role-avatar {
  text-align: center;
  margin-bottom: 16px;
}

.role-info {
  text-align: center;
  margin-bottom: 20px;
}

.role-name {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 8px 0;
}

.role-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.role-category {
  margin-bottom: 8px;
}

.role-actions {
  text-align: center;
}

.empty-section {
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 16px;
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
}
</style>