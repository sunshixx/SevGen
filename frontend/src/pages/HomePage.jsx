import React, { useState, useEffect } from 'react';
import { Container, Typography, Grid, Button, Box, Card, CardContent } from '@mui/material';
import { Search, Message, TrendingUp } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../context/UserContext';
import Layout from '../components/Layout';
import RoleCard from '../components/RoleCard';
import Loading from '../components/Loading';
import { mockRoles, mockChatSessions } from '../mockData';

const HomePage = () => {
  const [popularRoles, setPopularRoles] = useState([]);
  const [recentChats, setRecentChats] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { isAuthenticated } = useUser();

  // 获取热门角色（使用模拟数据）
  useEffect(() => {
    try {
      // 从mockData中获取热门角色数据
      const popularRolesData = mockRoles.slice(0, 4).map(role => ({
        id: role.id,
        name: role.name,
        category: role.category,
        description: role.description,
        avatar: role.avatar
      }));
      setPopularRoles(popularRolesData);
    } catch (err) {
      console.error('Failed to load roles:', err);
      setError('加载角色信息失败');
    }
  }, []);

  // 获取最近聊天记录（使用模拟数据）
  useEffect(() => {
    try {
      if (isAuthenticated) {
        // 格式化聊天记录数据
        const chats = mockChatSessions.map(chat => {
          const role = mockRoles.find(r => r.id === chat.roleId);
          return {
            id: chat.id,
            roleId: chat.roleId,
            roleName: role ? role.name : '未知角色',
            lastMessage: chat.id === 1 ? '你好，有什么我可以帮忙的吗？' : '相对论是理解宇宙的关键...',
            lastTime: chat.id === 1 ? '今天 14:30' : '昨天 19:15'
          };
        });
        setRecentChats(chats.slice(0, 5)); // 只显示最近5条
      }
    } catch (err) {
      console.error('Failed to load chats:', err);
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated]);

  const handleSearchRoles = () => {
    navigate('/roles');
  };

  const handleStartNewChat = () => {
    if (isAuthenticated) {
      navigate('/roles');
    } else {
      navigate('/login');
    }
  };

  const handleOpenChat = (chat) => {
    navigate(`/chat/${chat.roleId}`, { state: { chatId: chat.id } });
  };

  if (isLoading) {
    return (
      <Layout title="首页">
        <Container maxWidth="lg">
          <Loading />
        </Container>
      </Layout>
    );
  }

  return (
    <Layout title="首页">
      <Container maxWidth="lg">
        {/* 欢迎信息 */}
        <Box 
          sx={{ 
            my: 4, 
            p: 3, 
            backgroundColor: '#f5f5f5', 
            borderRadius: 2, 
            textAlign: 'center'
          }}
        >
          <Typography variant="h4" component="h1" gutterBottom>
            欢迎来到AI角色扮演聊天
          </Typography>
          <Typography variant="body1" paragraph>
            与历史名人、文学角色、科学家等进行逼真的对话体验
          </Typography>
          <Button 
            variant="contained" 
            size="large" 
            onClick={handleStartNewChat}
            style={{ backgroundColor: '#3f51b5', marginRight: 10 }}
          >
            开始聊天
          </Button>
          <Button 
            variant="outlined" 
            size="large" 
            onClick={handleSearchRoles}
            style={{ borderColor: '#3f51b5', color: '#3f51b5' }}
            startIcon={<Search />}
          >
            浏览角色
          </Button>
        </Box>

        {/* 热门角色部分 */}
        <section style={{ marginBottom: 40 }}>
          <Box display="flex" alignItems="center" justifyContent="space-between" marginBottom={3}>
            <Typography variant="h5" component="h2" style={{ display: 'flex', alignItems: 'center' }}>
              <TrendingUp style={{ marginRight: 10, color: '#3f51b5' }} />
              热门角色
            </Typography>
            <Button 
              variant="text" 
              onClick={handleSearchRoles}
              style={{ color: '#3f51b5' }}
            >
              查看全部
            </Button>
          </Box>

          <Grid container spacing={3}>
            {popularRoles.map((role) => (
              <Grid item xs={12} sm={6} md={3} key={role.id}>
                <RoleCard role={role} />
              </Grid>
            ))}
          </Grid>
        </section>

        {/* 最近聊天部分（仅对已登录用户显示） */}
        {isAuthenticated && (
          <section>
            <Box display="flex" alignItems="center" justifyContent="space-between" marginBottom={3}>
              <Typography variant="h5" component="h2" style={{ display: 'flex', alignItems: 'center' }}>
                <Message style={{ marginRight: 10, color: '#3f51b5' }} />
                最近聊天
              </Typography>
              <Button 
                variant="text" 
                onClick={() => navigate('/chats')}
                style={{ color: '#3f51b5' }}
              >
                查看全部
              </Button>
            </Box>

            {recentChats.length > 0 ? (
              <Grid container spacing={3}>
                {recentChats.map((chat) => (
                  <Grid item xs={12} key={chat.id}>
                    <Card 
                      elevation={1} 
                      style={{ cursor: 'pointer' }}
                      onClick={() => handleOpenChat(chat)}
                    >
                      <CardContent>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Typography variant="h6" component="div">
                            {chat.roleName}
                          </Typography>
                          <Typography variant="body2" color="textSecondary">
                            {chat.lastTime}
                          </Typography>
                        </div>
                        <Typography variant="body2" color="textSecondary" style={{ marginTop: 5 }}>
                          {chat.lastMessage}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            ) : (
              <Typography variant="body1" color="textSecondary" textAlign="center" style={{ padding: 20 }}>
                暂无聊天记录，快去开始新的对话吧！
              </Typography>
            )}
          </section>
        )}
      </Container>
    </Layout>
  );
};

export default HomePage;