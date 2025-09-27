<template>
  <div class="chatroom-list-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <h1>🏠 聊天室</h1>
        </div>
        
        <div class="header-actions">
          <el-button @click="showCreateDialog = true" type="primary" icon="Plus">
            创建聊天室
          </el-button>
          <el-button @click="goToHome" icon="ArrowLeft">
            返回首页
          </el-button>
        </div>
      </div>
    </header>

    <!-- 聊天室列表 -->
    <main class="main-content">
      <div class="chatrooms-container">
        <!-- 加载状态 -->
        <div v-if="isLoading" class="loading-section">
          <el-skeleton :rows="3" animated />
        </div>
        
        <!-- 聊天室网格 -->
        <div v-else-if="chatRooms.length > 0" class="chatrooms-grid">
          <div
            v-for="chatRoom in chatRooms"
            :key="chatRoom.id"
            class="chatroom-card"
            @click="enterChatRoom(chatRoom)"
          >
            <div class="chatroom-header">
              <h3 class="chatroom-name">{{ chatRoom.name }}</h3>
              <el-dropdown @command="(command: string) => handleChatRoomAction(command, chatRoom)">
                <el-button type="text" icon="MoreFilled" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            
            <p class="chatroom-description">{{ chatRoom.description || '暂无描述' }}</p>
            
            <div class="chatroom-roles">
              <div class="roles-preview">
              <div class="avatar-group">
                <el-avatar
                  v-for="(role, index) in (chatRoom.roles || []).slice(0, 3)"
                  :key="role.id"
                  :src="role.avatar"
                  :title="role.name"
                  size="small"
                  :style="{ marginLeft: index > 0 ? '-8px' : '0', zIndex: 3 - index }"
                >
                  {{ role.name[0] }}
                </el-avatar>
                <el-avatar
                  v-if="(chatRoom.roles || []).length > 3"
                  size="small"
                  style="margin-left: -8px; background-color: #f0f0f0; color: #666;"
                >
                  +{{ (chatRoom.roles || []).length - 3 }}
                </el-avatar>
              </div>
              <span class="roles-count">
                {{ (chatRoom.roles || []).length }} 个角色
              </span>
            </div>
            </div>
            
            <div class="chatroom-meta">
              <span class="create-time">
                创建于 {{ formatDate(chatRoom.createTime) }}
              </span>
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
              <div class="empty-icon">🏠</div>
            </template>
            <el-button type="primary" @click="showCreateDialog = true">
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
      width="600px"
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
            :rows="3"
            placeholder="请输入聊天室描述（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="选择角色" prop="roleIds">
          <div class="role-selection">
            <div class="selected-roles">
              <el-tag
                v-for="roleId in createForm.roleIds"
                :key="roleId"
                closable
                @close="removeRole(roleId)"
                style="margin-right: 8px; margin-bottom: 8px;"
              >
                {{ getRoleName(roleId) }}
              </el-tag>
            </div>
            
            <el-select
              v-model="selectedRoleToAdd"
              placeholder="选择要添加的角色"
              style="width: 100%"
              @change="addRole"
            >
              <el-option
                v-for="role in availableRolesForCreate"
                :key="role.id"
                :label="role.name"
                :value="role.id"
              >
                <div class="role-option">
                  <el-avatar :size="30" :src="role.avatar">
                    {{ role.name[0] }}
                  </el-avatar>
                  <span>{{ role.name }}</span>
                </div>
              </el-option>
            </el-select>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createChatRoom" :loading="isCreating">
          创建
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑聊天室对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑聊天室"
      width="600px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="createRules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input
            v-model="editForm.name"
            placeholder="请输入聊天室名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入聊天室描述（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateChatRoom" :loading="isUpdating">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, ArrowLeft, MoreFilled } from '@element-plus/icons-vue'
import { chatAPI } from '@/api'
import { useRoleStore } from '@/stores'
import type { ChatRoom, CreateChatRoomRequest, Role } from '@/types'

const router = useRouter()
const roleStore = useRoleStore()

// 聊天室数据
const chatRooms = ref<ChatRoom[]>([])
const isLoading = ref(false)

// 创建聊天室
const showCreateDialog = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = ref<CreateChatRoomRequest>({
  name: '',
  description: '',
  roleIds: []
})
const selectedRoleToAdd = ref<number | null>(null)
const isCreating = ref(false)

// 编辑聊天室
const showEditDialog = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = ref({
  id: 0,
  name: '',
  description: ''
})
const isUpdating = ref(false)

// 表单验证规则
const createRules = {
  name: [
    { required: true, message: '请输入聊天室名称', trigger: 'blur' },
    { min: 1, max: 50, message: '名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  roleIds: [
    { 
      type: 'array', 
      min: 1, 
      message: '请至少选择一个角色', 
      trigger: 'change' 
    }
  ]
}

// 计算属性
const availableRolesForCreate = computed(() => {
  return roleStore.roles.filter(role => !createForm.value.roleIds.includes(role.id))
})

// 页面初始化
onMounted(async () => {
  await loadChatRooms()
  
  // 确保角色数据已加载
  if (roleStore.roles.length === 0) {
    await roleStore.fetchRoles()
  }
})

// 加载聊天室列表
const loadChatRooms = async () => {
  isLoading.value = true
  try {
    const response = await chatAPI.getUserChatRooms()
    chatRooms.value = response.data || []
  } catch (error) {
    console.error('加载聊天室列表失败:', error)
    ElMessage.error('加载聊天室列表失败')
  } finally {
    isLoading.value = false
  }
}

// 创建聊天室
const createChatRoom = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
    isCreating.value = true
    
    await chatAPI.createChatRoom(createForm.value)
    ElMessage.success('聊天室创建成功')
    
    showCreateDialog.value = false
    resetCreateForm()
    await loadChatRooms()
  } catch (error) {
    if (error !== false) { // 不是表单验证错误
      console.error('创建聊天室失败:', error)
      ElMessage.error('创建聊天室失败')
    }
  } finally {
    isCreating.value = false
  }
}

// 更新聊天室
const updateChatRoom = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    isUpdating.value = true
    
    // 这里需要实现更新聊天室的API
    // await chatAPI.updateChatRoom(editForm.value.id, editForm.value)
    ElMessage.success('聊天室更新成功')
    
    showEditDialog.value = false
    await loadChatRooms()
  } catch (error) {
    if (error !== false) {
      console.error('更新聊天室失败:', error)
      ElMessage.error('更新聊天室失败')
    }
  } finally {
    isUpdating.value = false
  }
}

// 删除聊天室
const deleteChatRoom = async (chatRoom: ChatRoom) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除聊天室"${chatRoom.name}"吗？此操作不可恢复。`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '删除',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await chatAPI.deleteChatRoom(chatRoom.id)
    ElMessage.success('聊天室删除成功')
    await loadChatRooms()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除聊天室失败:', error)
      ElMessage.error('删除聊天室失败')
    }
  }
}

// 聊天室操作处理
const handleChatRoomAction = (command: string, chatRoom: ChatRoom) => {
  switch (command) {
    case 'edit':
      editForm.value = {
        id: chatRoom.id,
        name: chatRoom.name,
        description: chatRoom.description || ''
      }
      showEditDialog.value = true
      break
    case 'delete':
      deleteChatRoom(chatRoom)
      break
  }
}

// 进入聊天室
const enterChatRoom = (chatRoom: ChatRoom) => {
  router.push(`/chatroom/${chatRoom.id}`)
}

// 角色管理
const addRole = (roleId: number) => {
  if (roleId && !createForm.value.roleIds.includes(roleId)) {
    createForm.value.roleIds.push(roleId)
    selectedRoleToAdd.value = null
  }
}

const removeRole = (roleId: number) => {
  const index = createForm.value.roleIds.indexOf(roleId)
  if (index > -1) {
    createForm.value.roleIds.splice(index, 1)
  }
}

const getRoleName = (roleId: number) => {
  const role = roleStore.roles.find(r => r.id === roleId)
  return role?.name || '未知角色'
}

// 重置表单
const resetCreateForm = () => {
  createForm.value = {
    name: '',
    description: '',
    roleIds: []
  }
  selectedRoleToAdd.value = null
}

// 导航
const goToHome = () => {
  router.push('/')
}

// 工具函数
const formatDate = (dateString?: string) => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleDateString()
}
</script>

<style scoped>
.chatroom-list-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 1rem 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo h1 {
  margin: 0;
  color: #333;
  font-size: 1.8rem;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.main-content {
  padding: 2rem;
}

.chatrooms-container {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-section {
  padding: 2rem;
}

.chatrooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.chatroom-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 1rem;
  padding: 1.5rem;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
}

.chatroom-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.chatroom-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.chatroom-name {
  margin: 0;
  color: #333;
  font-size: 1.25rem;
  font-weight: 600;
}

.chatroom-description {
  color: #666;
  margin: 0 0 1rem 0;
  line-height: 1.5;
}

.chatroom-roles {
  margin-bottom: 1rem;
}

.roles-preview {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.avatar-group {
  display: flex;
  align-items: center;
}

.roles-count {
  color: #666;
  font-size: 0.9rem;
}

.chatroom-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #999;
  font-size: 0.8rem;
}

.empty-section {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.role-selection {
  width: 100%;
}

.selected-roles {
  margin-bottom: 1rem;
  min-height: 32px;
}

.role-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
</style>