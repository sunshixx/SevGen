import React from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import RolesPage from './pages/RolesPage';
import ChatPage from './pages/ChatPage';
import Layout from './components/Layout';

// 最简单的404组件
const NotFound = () => {
  return React.createElement(
    Layout,
    { title: '页面未找到' },
    React.createElement(
      'div',
      { style: { display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '50vh', padding: '2rem' } },
      React.createElement('h1', { style: { fontSize: '4rem', margin: '0 0 1rem 0', color: '#3f51b5' } }, '404'),
      React.createElement('h2', { style: { fontSize: '1.5rem', margin: '0 0 1rem 0', color: '#666' } }, '页面未找到'),
      React.createElement('p', { style: { fontSize: '1rem', color: '#999', margin: '0 0 2rem 0' } }, '抱歉，您访问的页面不存在或已被移除'),
      React.createElement(
        'button',
        { style: { padding: '0.5rem 1.5rem', backgroundColor: '#3f51b5', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '1rem' }, onClick: () => window.location.href = '/' },
        '返回首页'
      )
    )
  );
};

// 创建路由，使用更基本的对象结构
const router = createBrowserRouter([
  { path: '/', element: React.createElement(HomePage) },
  { path: '/login', element: React.createElement(LoginPage) },
  { path: '/register', element: React.createElement(RegisterPage) },
  { path: '/roles', element: React.createElement(RolesPage) },
  { path: '/chat/:chatId', element: React.createElement(ChatPage) },
  { path: '/chat', element: React.createElement(ChatPage) },
  { path: '*', element: React.createElement(NotFound) }
]);

export default router;