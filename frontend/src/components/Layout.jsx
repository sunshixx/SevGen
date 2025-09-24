import React from 'react';
import { AppBar, Toolbar, Typography, Button, IconButton, Drawer, List, ListItem, ListItemText, Divider, useMediaQuery } from '@mui/material';
import { Menu, Logout, Home, Search, Message, Settings } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../context/UserContext';

const Layout = ({ children, title = 'AI角色扮演聊天' }) => {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useUser();
  const isMobile = useMediaQuery('(max-width:600px)');
  const [drawerOpen, setDrawerOpen] = React.useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const toggleDrawer = () => {
    setDrawerOpen(!drawerOpen);
  };

  const menuItems = [
    {
      text: '首页',
      icon: <Home size={18} />,
      path: '/',
      authRequired: false
    },
    {
      text: '搜索角色',
      icon: <Search size={18} />,
      path: '/roles',
      authRequired: true
    },
    {
      text: '我的聊天',
      icon: <Message size={18} />,
      path: '/chats',
      authRequired: true
    },
    {
      text: '设置',
      icon: <Settings size={18} />,
      path: '/settings',
      authRequired: true
    }
  ];

  const drawerContent = (
    <div style={{ width: 250, padding: 16 }}>
      <Typography variant="h6" component="div" style={{ marginBottom: 20 }}>
        AI角色扮演
      </Typography>
      <Divider />
      <List>
        {menuItems
          .filter(item => !item.authRequired || isAuthenticated)
          .map((item, index) => (
            <ListItem 
              button 
              key={index}
              onClick={() => {
                navigate(item.path);
                setDrawerOpen(false);
              }}
            >
              <span style={{ marginRight: 10 }}>{item.icon}</span>
              <ListItemText primary={item.text} />
            </ListItem>
          ))
        }
        {isAuthenticated && (
          <>
            <Divider style={{ margin: '10px 0' }} />
            <ListItem button onClick={handleLogout}>
              <Logout size={18} style={{ marginRight: 10 }} />
              <ListItemText primary="退出登录" />
            </ListItem>
          </>
        )}
      </List>
    </div>
  );

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      {/* 顶部导航栏 */}
      <AppBar position="static" sx={{ backgroundColor: '#3f51b5' }}>
        <Toolbar>
          {isMobile && (
            <IconButton edge="start" color="inherit" onClick={toggleDrawer} style={{ marginRight: 20 }}>
              <Menu />
            </IconButton>
          )}
          <Typography variant="h6" component="div" style={{ flexGrow: 1 }}>
            {title}
          </Typography>
          {isAuthenticated ? (
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" color="inherit" style={{ marginRight: 10 }}>
                欢迎, {user?.username}
              </Typography>
              <IconButton color="inherit" onClick={handleLogout}>
                <Logout />
              </IconButton>
            </div>
          ) : (
            <>
              <Button color="inherit" onClick={() => navigate('/login')}>登录</Button>
              <Button color="inherit" onClick={() => navigate('/register')}>注册</Button>
            </>
          )}
        </Toolbar>
      </AppBar>

      {/* 侧边栏 */}
      {isMobile ? (
        <Drawer
          anchor="left"
          open={drawerOpen}
          onClose={toggleDrawer}
        >
          {drawerContent}
        </Drawer>
      ) : (
        <div style={{ display: 'flex', flex: 1 }}>
          <div style={{ width: 250, borderRight: '1px solid #eee', minHeight: 'calc(100vh - 64px)' }}>
            {drawerContent}
          </div>
          <main style={{ flex: 1, padding: 20 }}>
            {children}
          </main>
        </div>
      )}

      {isMobile && (
        <main style={{ flex: 1, padding: 20 }}>
          {children}
        </main>
      )}

      {/* 页脚 */}
      <footer style={{ backgroundColor: '#f5f5f5', padding: 10, textAlign: 'center' }}>
        <Typography variant="body2" color="textSecondary">
          © 2024 AI角色扮演聊天应用 - 让对话更有趣
        </Typography>
      </footer>
    </div>
  );
};

export default Layout;