import React, { useState, useEffect } from 'react';
import { TextField, Button, Container, Typography, Paper, Alert, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../context/UserContext';
import Layout from '../components/Layout';

const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { login, isAuthenticated } = useUser();

  // 如果用户已登录，重定向到首页
  React.useEffect(() => {
    if (isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    if (!username || !password) {
      setError('请填写所有必填字段');
      setIsLoading(false);
      return;
    }

    try {
      await login(username, password);
      navigate('/');
    } catch (err) {
      setError('登录失败，请检查用户名和密码是否正确');
      console.error('Login error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRegisterRedirect = () => {
    navigate('/register');
  };

  return (
    <Layout title="用户登录">
      <Container maxWidth="sm">
        <Paper elevation={3} style={{ padding: 24, marginTop: 40 }}>
          <Typography variant="h5" component="h2" gutterBottom style={{ textAlign: 'center' }}>
            欢迎登录AI角色扮演聊天
          </Typography>
          
          {error && (
            <Alert severity="error" style={{ marginBottom: 20 }}>
              {error}
            </Alert>
          )}
          
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
              label="密码"
              name="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            
            <Button
              type="submit"
              fullWidth
              variant="contained"
              style={{ marginTop: 20, backgroundColor: '#3f51b5', padding: 10 }}
              disabled={isLoading}
            >
              {isLoading ? '登录中...' : '登录'}
            </Button>
          </form>
          
          <Box textAlign="center" style={{ marginTop: 20 }}>
            <Typography variant="body2" color="textSecondary" display="inline">
              还没有账号？
            </Typography>
            <Button 
              variant="text" 
              onClick={handleRegisterRedirect}
              style={{ color: '#3f51b5', textTransform: 'none' }}
            >
              立即注册
            </Button>
          </Box>
        </Paper>
      </Container>
    </Layout>
  );
};

export default LoginPage;