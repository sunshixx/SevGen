import api from './apiConfig';

class MessageService {
  // 发送消息并获取AI回复
  async sendMessage(chatId, content) {
    try {
      const response = await api.post('/messages', { chatId, content });
      return response;
    } catch (error) {
      console.error('Failed to send message:', error);
      throw error;
    }
  }

  // 获取聊天会话的所有消息
  async getMessagesByChatId(chatId) {
    try {
      const response = await api.get(`/messages/chat/${chatId}`);
      return response;
    } catch (error) {
      console.error('Failed to fetch messages:', error);
      throw error;
    }
  }

  // 获取未读消息
  async getUnreadMessages(chatId) {
    try {
      const response = await api.get(`/messages/chat/${chatId}/unread`);
      return response;
    } catch (error) {
      console.error('Failed to fetch unread messages:', error);
      throw error;
    }
  }

  // 将消息标记为已读
  async markMessagesAsRead(chatId) {
    try {
      const response = await api.put(`/messages/chat/${chatId}/read`);
      return response;
    } catch (error) {
      console.error('Failed to mark messages as read:', error);
      throw error;
    }
  }
}

export default new MessageService();