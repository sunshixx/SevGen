# AI角色扮演聊天系统 - 前端

这是一个基于 Vue3 + TypeScript + Element Plus 开发的现代化前端应用，用于与后端的AI角色扮演聊天系统进行交互。

## ✨ 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **TypeScript** - 类型安全的JavaScript超集  
- **Vite** - 快速的前端构建工具
- **Element Plus** - Vue 3 UI组件库
- **Vue Router** - 官方路由管理器
- **Pinia** - Vue 3 状态管理库
- **Axios** - HTTP客户端
- **Day.js** - 轻量级日期处理库

## 🚀 快速开始

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:5173 查看应用

### 3. 构建生产版本

```bash
npm run build
```

### 4. 预览生产版本

```bash
npm run preview
```

## 📁 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API 接口封装
│   │   ├── auth.ts        # 认证相关接口
│   │   ├── role.ts        # 角色相关接口
│   │   ├── chat.ts        # 聊天相关接口
│   │   ├── message.ts     # 消息相关接口
│   │   └── index.ts       # API 统一导出
│   ├── components/        # 可复用组件
│   ├── router/            # 路由配置
│   │   └── index.ts       # 路由定义
│   ├── stores/            # 状态管理
│   │   ├── auth.ts        # 用户认证状态
│   │   ├── role.ts        # 角色管理状态
│   │   ├── chat.ts        # 聊天管理状态
│   │   └── index.ts       # store 统一导出
│   ├── types/             # TypeScript 类型定义
│   │   └── index.ts       # 全局类型定义
│   ├── utils/             # 工具函数
│   │   └── request.ts     # HTTP 请求封装
│   ├── views/             # 页面组件
│   │   ├── Login.vue      # 登录页面
│   │   ├── Register.vue   # 注册页面
│   │   ├── Home.vue       # 首页（角色选择）
│   │   ├── Chat.vue       # 聊天页面
│   │   ├── Profile.vue    # 个人信息页面
│   │   └── NotFound.vue   # 404页面
│   ├── App.vue            # 根组件
│   └── main.ts            # 应用入口
├── index.html             # HTML 模板
├── package.json           # 项目配置
├── tsconfig.json          # TypeScript 配置
├── vite.config.ts         # Vite 配置
└── README.md              # 项目说明
```

## 🌟 主要功能

### 🔐 用户认证
- 用户注册（邮箱验证）
- 用户登录
- JWT Token 管理
- 自动登录状态保持

### 🎭 角色管理
- 角色列表展示
- 角色分类筛选
- 角色搜索功能
- 角色详情查看

### 💬 聊天功能
- 创建聊天会话
- 实时消息发送接收
- SSE 实时推送
- 聊天历史管理
- 消息已读状态

### 📱 响应式设计
- 移动端适配
- 触摸友好的交互
- 现代化的UI设计

## 🔧 配置说明

### API 代理配置

在 `vite.config.ts` 中配置了代理，将前端的 `/api` 请求代理到后端服务：

```typescript
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

### 环境要求

- Node.js >= 16
- npm >= 7

## 🎯 核心页面说明

### 1. 登录页面 (`/login`)
- 用户名/密码登录
- 表单验证
- 自动跳转到首页

### 2. 注册页面 (`/register`) 
- 邮箱验证码注册
- 密码强度验证
- 60秒倒计时限制

### 3. 首页 (`/`)
- 角色网格展示
- 分类筛选
- 搜索功能
- 用户菜单

### 4. 聊天页面 (`/chat/:id`)
- 左侧聊天列表
- 右侧消息区域
- 实时消息推送
- 消息输入框

### 5. 个人信息页面 (`/profile`)
- 用户信息展示
- 头像管理
- 账户设置

## 🔄 状态管理

使用 Pinia 进行状态管理，主要包含：

- **AuthStore**: 用户认证状态管理
- **RoleStore**: 角色数据管理  
- **ChatStore**: 聊天会话和消息管理

## 📡 API 集成

所有API调用都经过统一的请求拦截器处理：

- 自动添加 JWT Token
- 统一错误处理
- 响应数据格式化
- 401 自动跳转登录

## 🎨 UI/UX 特性

- Material Design 风格
- 流畅的过渡动画
- 加载状态提示
- 错误友好提示
- 暗色主题支持

## 🚦 开发建议

1. 确保后端服务正常运行在 `http://localhost:8080`
2. 开发时使用 Chrome DevTools 调试
3. 使用 Vue DevTools 检查组件状态
4. 遵循 TypeScript 类型约束

## 📞 技术支持

如有问题，请参考：
- Vue 3 官方文档: https://vuejs.org/
- Element Plus 官方文档: https://element-plus.org/
- TypeScript 官方文档: https://www.typescriptlang.org/