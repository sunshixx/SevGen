import request from '@/utils/request'
import type { ApiResponse, Role } from '@/types'

// 角色相关API
export const roleAPI = {
  // 获取所有公开角色
  getAllPublicRoles: (): Promise<ApiResponse<Role[]>> => {
    return request.get('/roles')
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
  }
}