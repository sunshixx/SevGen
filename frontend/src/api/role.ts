import request from '@/utils/request'
import type { ApiResponse, Role } from '@/types'

// API级别的缓存
let publicRolesCache: ApiResponse<Role[]> | null = null
let cacheTimestamp = 0
const CACHE_DURATION = 5 * 60 * 1000 // 5分钟缓存

// 角色相关API
export const roleAPI = {
  // 获取所有公开角色（带缓存）
  getAllPublicRoles: (): Promise<ApiResponse<Role[]>> => {
    const now = Date.now()
    
    // 如果缓存存在且未过期，直接返回缓存数据
    if (publicRolesCache && (now - cacheTimestamp < CACHE_DURATION)) {
      console.log('使用角色API缓存数据')
      return Promise.resolve(publicRolesCache)
    }
    
    // 否则请求新数据并缓存
    console.log('请求新的角色数据')
    return request.get('/roles').then(response => {
      publicRolesCache = response
      cacheTimestamp = now
      return response
    })
  },

  // 搜索角色
  searchRoles: (query: string): Promise<ApiResponse<Role[]>> => {
    return request.get('/roles/search', { params: { query } })
  },

  // 按分类获取角色
  getRolesByCategory: (category: string): Promise<ApiResponse<Role[]>> => {
    return request.get(`/roles/category/${category}`)
  },

  // 根据ID获取角色详情
  getRoleById: (id: number): Promise<ApiResponse<Role>> => {
    return request.get(`/roles/${id}`)
  },

  // 根据名称获取角色
  getRoleByName: (name: string): Promise<ApiResponse<Role>> => {
    return request.get(`/roles/name/${name}`)
  },

  // 创建角色
  createRole: (data: Partial<Role>): Promise<ApiResponse<Role>> => {
    return request.post('/roles', data)
  },

  // 更新角色
  updateRole: (id: number, data: Partial<Role>): Promise<ApiResponse<Role>> => {
    return request.put(`/roles/${id}`, data)
  },

  // 删除角色
  deleteRole: (id: number): Promise<ApiResponse> => {
    return request.delete(`/roles/${id}`)
  },

  // 清理角色缓存
  clearCache: () => {
    publicRolesCache = null
    cacheTimestamp = 0
    console.log('角色API缓存已清理')
  }
}