import api from './apiConfig';

class AuthService {
  // 用户登录
  async login(username, password) {
    try {
      const response = await api.post('/api/auth/login', { username, password });
      if (response.token) {
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response.user));
      }
      return response;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  }

  // 发送验证码
  async sendVerificationCode(email) {
    try {
      const response = await api.post('/api/auth/send-verification-code', { email });
      return response;
    } catch (error) {
      console.error('Failed to send verification code:', error);
      throw error;
    }
  }

  // 用户注册（带验证码）
  async register(username, email, password, verificationCode) {
    try {
      const response = await api.post('/api/auth/register', { 
        username, 
        email, 
        password, 
        verificationCode 
      });
      return response;
    } catch (error) {
      console.error('Registration failed:', error);
      throw error;
    }
  }

  // 获取当前登录用户信息
  async getCurrentUser() {
    try {
      const response = await api.get('/api/auth/me');
      return response;
    } catch (error) {
      console.error('Failed to get current user:', error);
      throw error;
    }
  }

  // 检查用户是否已登录
  isLoggedIn() {
    return !!localStorage.getItem('token');
  }

  // 登出
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  // 获取存储在localStorage中的用户信息
  getUserFromStorage() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }
}

export default new AuthService();