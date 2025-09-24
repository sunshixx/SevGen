import api from './apiConfig';

class RoleService {
  // 获取所有公开角色
  async getAllPublicRoles() {
    try {
      const response = await api.get('/roles');
      return response;
    } catch (error) {
      console.error('Failed to fetch roles:', error);
      throw error;
    }
  }

  // 搜索角色
  async searchRoles(query) {
    try {
      const response = await api.get(`/roles/search?query=${encodeURIComponent(query)}`);
      return response;
    } catch (error) {
      console.error('Failed to search roles:', error);
      throw error;
    }
  }

  // 按分类获取角色
  async getRolesByCategory(category) {
    try {
      const response = await api.get(`/roles/category/${category}`);
      return response;
    } catch (error) {
      console.error('Failed to fetch roles by category:', error);
      throw error;
    }
  }

  // 获取角色详情
  async getRoleById(roleId) {
    try {
      const response = await api.get(`/roles/${roleId}`);
      return response;
    } catch (error) {
      console.error('Failed to fetch role details:', error);
      throw error;
    }
  }
}

export default new RoleService();