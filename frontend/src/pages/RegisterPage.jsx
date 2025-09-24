import React, { useState } from 'react';
import { TextField, Button, Container, Typography, Paper, Alert, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../context/UserContext';
import Layout from '../components/Layout';

const RegisterPage = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { register, isAuthenticated } = useUser();

  // 如果用户已登录，重定向到首页
  React.useEffect(() => {
    if (isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');
    setIsLoading(true);

    // 表单验证
    if (!username || !email || !password || !confirmPassword) {
      setError('请填写所有必填字段');
      setIsLoading(false);
      return;
    }

    if (password !== confirmPassword) {
      setError('两次输入的密码不一致');
      setIsLoading(false);
      return;
    }

    if (password.length < 6) {
      setError('密码长度至少为6个字符');
      setIsLoading(false);
      return;
    }

    // 简单的邮箱格式验证
    const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
    if (!emailRegex.test(email)) {
      setError('请输入有效的邮箱地址');
      setIsLoading(false);
      return;
    }

    try {
      await register(username, email, password);
      setSuccessMessage('注册成功！即将跳转到登录页面...');
      // 注册成功后重定向到登录页
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      setError('注册失败，请稍后重试');
      console.error('Registration error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLoginRedirect = () => {
    navigate('/login');
  };

  return (
    <Layout title="用户注册">
      <Container maxWidth="sm">
        <Paper elevation={3} style={{ padding: 24, marginTop: 40 }}>
          <Typography variant="h5" component="h2" gutterBottom style={{ textAlign: 'center' }}>
            创建新账号
          </Typography>
          
          {error && (
            <Alert severity="error" style={{ marginBottom: 20 }}>
              {error}
            </Alert>
          )}
          
          {successMessage && (
            <Alert severity="success" style={{ marginBottom: 20 }}>
              {successMessage}
            </Alert>
          )}
          
          {!successMessage && (
            <form onSubmit={handleSubmit}>
              <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                label="用户名"
                name="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoFocus
              />
              
              <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                label="邮箱"
                name="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
              
              <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                label="密码"
                name="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                helperText="密码长度至少为6个字符"
              />
              
              <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                label="确认密码"
                name="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
              
              <Button
                type="submit"
                fullWidth
                variant="contained"
                style={{ marginTop: 20, backgroundColor: '#3f51b5', padding: 10 }}
                disabled={isLoading}
              >
                {isLoading ? '注册中...' : '注册'}
              </Button>
            </form>
          )}
          
          <Box textAlign="center" style={{ marginTop: 20 }}>
            <Typography variant="body2" color="textSecondary" display="inline">
              已有账号？
            </Typography>
            <Button 
              variant="text" 
              onClick={handleLoginRedirect}
              style={{ color: '#3f51b5', textTransform: 'none' }}
            >
              立即登录
            </Button>
          </Box>
        </Paper>
      </Container>
    </Layout>
  );
};

export default RegisterPage;