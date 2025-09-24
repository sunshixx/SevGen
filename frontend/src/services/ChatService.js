import api from './apiConfig';

class ChatService {
  // 创建新的聊天会话
  async createChat(roleId, title = '') {
    try {
      const response = await api.post('/chats', { roleId, title });
      return response;
    } catch (error) {
      console.error('Failed to create chat:', error);
      throw error;
    }
  }

  // 获取用户的所有聊天会话
  async getAllChats() {
    try {
      const response = await api.get('/chats');
      return response;
    } catch (error) {
      console.error('Failed to fetch chats:', error);
      throw error;
    }
  }

  // 获取用户的活跃聊天会话
  async getActiveChats() {
    try {
      const response = await api.get('/chats/active');
      return response;
    } catch (error) {
      console.error('Failed to fetch active chats:', error);
      throw error;
    }
  }

  // 获取单个聊天会话详情
  async getChatById(chatId) {
    try {
      const response = await api.get(`/chats/${chatId}`);
      return response;
    } catch (error) {
      console.error('Failed to fetch chat details:', error);
      throw error;
    }
  }

  // 更新聊天会话
  async updateChat(chatId, data) {
    try {
      const response = await api.put(`/chats/${chatId}`, data);
      return response;
    } catch (error) {
      console.error('Failed to update chat:', error);
      throw error;
    }
  }

  // 停用聊天会话
  async deactivateChat(chatId) {
    try {
      const response = await api.put(`/chats/${chatId}/deactivate`);
      return response;
    } catch (error) {
      console.error('Failed to deactivate chat:', error);
      throw error;
    }
  }

  // 删除聊天会话
  async deleteChat(chatId) {
    try {
      const response = await api.delete(`/chats/${chatId}`);
      return response;
    } catch (error) {
      console.error('Failed to delete chat:', error);
      throw error;
    }
  }
}

export default new ChatService();