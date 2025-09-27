import { defineStore } from 'pinia'
import { ref } from 'vue'
import { roleAPI } from '@/api'
import type { Role } from '@/types'

export const useRoleStore = defineStore('role', () => {
  const roles = ref<Role[]>([])
  const currentRole = ref<Role | null>(null)
  const isLoading = ref(false)
  const categories = ref<string[]>([])
  const isDataLoaded = ref(false) // 添加数据是否已加载的标志

  // 获取所有公开角色
  const fetchRoles = async () => {
    // 如果数据已经加载过且不为空，直接返回
    if (isDataLoaded.value && roles.value.length > 0) {
      console.log('角色数据已缓存，跳过重复加载')
      return
    }

    try {
      isLoading.value = true
      console.log('=== 开始获取角色数据 ===')

      const response = await roleAPI.getAllPublicRoles()

      console.log('=== API响应数据 ===')
      console.log('完整response:', response)
      console.log('response.data:', response.data)

      if (response && response.data && Array.isArray(response.data)) {
        console.log('=== 设置角色数据 ===')
        console.log('角色数据长度:', response.data.length)
        console.log('角色数据内容:', response.data)

        roles.value = response.data

        // 提取分类
        const categorySet = new Set<string>()
        response.data.forEach(role => {
          if (role.category) {
            categorySet.add(role.category)
          }
        })
        categories.value = Array.from(categorySet)
        console.log('分类数据:', categories.value)
        isDataLoaded.value = true // 标记数据已加载
      } else {
        console.log('=== 数据格式错误 ===')
        console.log('响应数据格式不正确:', response)
        roles.value = []
      }
    } catch (error) {
      console.log('=== API请求失败 ===')
      console.error('获取角色列表失败:', error)
      roles.value = []
    } finally {
      isLoading.value = false
      console.log('=== 最终状态 ===')
      console.log('最终roles数量:', roles.value.length)
    }
  }

  // 搜索角色
  const searchRoles = async (query: string) => {
    try {
      isLoading.value = true
      const response = await roleAPI.searchRoles(query)

      if (response.data) {
        roles.value = response.data
      }
    } catch (error) {
      console.error('搜索角色失败:', error)
    } finally {
      isLoading.value = false
    }
  }

  // 按分类获取角色
  const fetchRolesByCategory = async (category: string) => {
    try {
      isLoading.value = true
      const response = await roleAPI.getRolesByCategory(category)

      if (response.data) {
        roles.value = response.data
      }
    } catch (error) {
      console.error('获取分类角色失败:', error)
    } finally {
      isLoading.value = false
    }
  }

  // 获取角色详情
  const fetchRoleById = async (id: number) => {
    try {
      const response = await roleAPI.getRoleById(id)

      if (response.data) {
        currentRole.value = response.data
        return response.data
      }
    } catch (error) {
      console.error('获取角色详情失败:', error)
      return null
    }
  }

  // 清空当前角色
  const clearCurrentRole = () => {
    currentRole.value = null
  }

  // 强制刷新角色数据
  const refreshRoles = async () => {
    isDataLoaded.value = false
    roleAPI.clearCache() // 清理API缓存
    await fetchRoles()
  }

  // 清理所有缓存数据
  const clearCache = () => {
    roles.value = []
    currentRole.value = null
    categories.value = []
    isDataLoaded.value = false
    roleAPI.clearCache()
  }

  return {
    // state
    roles,
    currentRole,
    isLoading,
    categories,
    isDataLoaded,
    
    // actions
    fetchRoles,
    searchRoles,
    fetchRolesByCategory,
    fetchRoleById,
    clearCurrentRole,
    refreshRoles,
    clearCache
  }
})