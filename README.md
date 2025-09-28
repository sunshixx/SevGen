
<div align="center">

# 🎭 SevGen - AI角色扮演聊天系统

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=for-the-badge&logo=spring-boot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue.js-3.3.8-4FC08D?style=for-the-badge&logo=vue.js" alt="Vue.js">
  <img src="https://img.shields.io/badge/TypeScript-5.2.2-blue?style=for-the-badge&logo=typescript" alt="TypeScript">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-Cache-red?style=for-the-badge&logo=redis" alt="Redis">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/Version-1.0.0-blue.svg?style=flat-square" alt="Version">
  <img src="https://img.shields.io/badge/Status-Active-success.svg?style=flat-square" alt="Status">
</p>

<p align="center">
  <strong>🚀 一个基于AI的智能角色扮演聊天系统，让您与历史人物、文学角色进行深度对话</strong>
</p>

<p align="center">
  <a href="#-特性">特性</a> •
  <a href="#-技术栈">技术栈</a> •
  <a href="#-快速开始">快速开始</a> •
  <a href="#-api文档">API文档</a> •
  <a href="#-项目结构">项目结构</a> •
  <a href="#-贡献">贡献</a>
</p>

</div>

---

## ✨ 特性

<table>
<tr>
<td width="50%">

### 🎯 核心功能
- 🤖 **智能AI对话** - 基于LangChain4j的高质量AI对话
- 🎭 **角色扮演** - 与哈利波特、苏格拉底等经典角色对话
- 💬 **实时聊天** - 流畅的聊天体验
- 🔍 **智能搜索** - 快速找到感兴趣的角色
- 📱 **响应式设计** - 完美适配各种设备

</td>
<td width="50%">

### 🛡️ 技术特性
- 🔐 **JWT认证** - 安全的用户认证系统
- 📊 **数据持久化** - MySQL + Redis双重存储
- 🎨 **现代UI** - Element Plus + Vue 3组合
- 🚀 **高性能** - 优化的后端架构
- 📝 **类型安全** - 全面的TypeScript支持

</td>
</tr>
</table>

## 🛠️ 技术栈

### 后端技术
| 技术 | 版本 | 描述 |
|------|------|------|
| ![Java](https://img.shields.io/badge/-Java-007396?style=flat&logo=java&logoColor=white) | 17 | 核心编程语言 |
| ![Spring Boot](https://img.shields.io/badge/-Spring%20Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white) | 3.2.0 | 应用框架 |
| ![LangChain4j](https://img.shields.io/badge/-LangChain4j-FF6B6B?style=flat) | 0.34.0 | AI集成框架 |
| ![MySQL](https://img.shields.io/badge/-MySQL-4479A1?style=flat&logo=mysql&logoColor=white) | 8.0 | 主数据库 |
| ![Redis](https://img.shields.io/badge/-Redis-DC382D?style=flat&logo=redis&logoColor=white) | Latest | 缓存数据库 |
| ![MyBatis Plus](https://img.shields.io/badge/-MyBatis%20Plus-000000?style=flat) | 3.5.9 | ORM框架 |

### 前端技术
| 技术 | 版本 | 描述 |
|------|------|------|
| ![Vue.js](https://img.shields.io/badge/-Vue.js-4FC08D?style=flat&logo=vue.js&logoColor=white) | 3.3.8 | 前端框架 |
| ![TypeScript](https://img.shields.io/badge/-TypeScript-3178C6?style=flat&logo=typescript&logoColor=white) | 5.2.2 | 类型安全 |
| ![Element Plus](https://img.shields.io/badge/-Element%20Plus-409EFF?style=flat) | 2.4.4 | UI组件库 |
| ![Vite](https://img.shields.io/badge/-Vite-646CFF?style=flat&logo=vite&logoColor=white) | 5.0.0 | 构建工具 |
| ![Pinia](https://img.shields.io/badge/-Pinia-FFD859?style=flat) | 2.1.7 | 状态管理 |
| ![Axios](https://img.shields.io/badge/-Axios-5A29E4?style=flat) | 1.6.2 | HTTP客户端 |

## 🚀 快速开始

### 📋 环境要求

- ☕ **Java 17+**
- 🗄️ **MySQL 8.0+**
- 🔴 **Redis 6.0+**
- 📦 **Node.js 16+**
- 🔧 **Maven 3.6+**

### 🔧 安装步骤

#### 1️⃣ 克隆项目
```bash
git clone https://github.com/sunshixx/SevGen.git
cd SevGen
```

#### 2️⃣ 后端配置
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE roleplay CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 配置环境变量
export OPENAI_API_KEY=your_openai_api_key
```

#### 3️⃣ 启动后端服务
```bash
mvn clean install
mvn spring-boot:run
```

#### 4️⃣ 启动前端服务
```bash
cd frontend
npm install
npm run dev
```

#### 5️⃣ 访问应用
- 🌐 **前端地址**: http://localhost:5173
- 🔧 **后端API**: http://localhost:16999
- 📊 **健康检查**: http://localhost:16999/actuator/health

## 📚 API文档

### 🔐 认证接口
| 方法 | 端点 | 描述 |
|------|------|------|
| `POST` | `/api/auth/login` | 用户登录 |
| `POST` | `/api/auth/register` | 用户注册 |
| `GET` | `/api/auth/me` | 获取当前用户信息 |

### 🎭 角色接口
| 方法 | 端点 | 描述 |
|------|------|------|
| `GET` | `/api/roles` | 获取所有角色 |
| `GET` | `/api/roles/search?query={keyword}` | 搜索角色 |
| `GET` | `/api/roles/category/{category}` | 按分类获取角色 |
| `GET` | `/api/roles/{id}` | 获取角色详情 |

### 💬 聊天接口
| 方法 | 端点 | 描述 |
|------|------|------|
| `POST` | `/api/chats` | 创建聊天会话 |
| `GET` | `/api/chats` | 获取用户聊天列表 |
| `GET` | `/api/chats/active` | 获取活跃会话 |
| `GET` | `/api/chats/{id}` | 获取会话详情 |

### 📝 消息接口
| 方法 | 端点 | 描述 |
|------|------|------|
| `POST` | `/api/messages` | 发送消息 |
| `GET` | `/api/messages/chat/{chatId}` | 获取聊天消息 |
| `GET` | `/api/messages/chat/{chatId}/unread` | 获取未读消息 |
| `PUT` | `/api/messages/chat/{chatId}/read` | 标记已读 |

## 📁 项目结构

```
SevGen/
├── 📁 src/main/java/com/aichat/roleplay/    # 后端源码
│   ├── 📁 config/                           # 配置类
│   ├── 📁 controller/                       # 控制器
│   ├── 📁 service/                          # 业务逻辑
│   ├── 📁 entity/                           # 实体类
│   ├── 📁 dto/                              # 数据传输对象
│   └── 📁 util/                             # 工具类
├── 📁 src/main/resources/                   # 资源文件
│   ├── 📄 application.properties            # 配置文件
│   └── 📁 rag-documents/                    # RAG文档
├── 📁 frontend/                             # 前端项目
│   ├── 📁 src/
│   │   ├── 📁 views/                        # 页面组件
│   │   ├── 📁 components/                   # 通用组件
│   │   ├── 📁 api/                          # API接口
│   │   ├── 📁 stores/                       # 状态管理
│   │   └── 📁 utils/                        # 工具函数
│   └── 📄 package.json                      # 前端依赖
├── 📄 pom.xml                               # Maven配置
└── 📄 README.md                             # 项目文档
```

## 🎭 预设角色

系统内置了以下经典角色，启动后自动加载：

<div align="center">

| 角色 | 类型 | 特色 |
|------|------|------|
| 🧙‍♂️ **哈利波特** | 文学人物 | 魔法世界的冒险故事 |
| 🏛️ **苏格拉底** | 历史人物 | 哲学思辨与智慧对话 |
| 🧠 **爱因斯坦** | 科学家 | 科学探索与理论讨论 |
。。。

</div>

## 🔮 未来规划

- [ ] 🎤 **语音对话** - 语音转文本和文本转语音
- [ ] 🎨 **角色定制** - 用户自定义角色创建
- [ ] 📊 **数据分析** - 聊天数据统计和分析
- [ ] 🌍 **多语言支持** - 国际化功能
- [ ] 📱 **移动端APP** - React Native应用
- [ ] 🤝 **社交功能** - 用户间角色分享

## 🤝 贡献

我们欢迎所有形式的贡献！请查看 [贡献指南](CONTRIBUTING.md) 了解详情。

### 贡献者

<a href="https://github.com/sunshixx/SevGen/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=sunshixx/SevGen" />
</a>

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源协议。

## 📞 联系我们

- 📧 **邮箱**: sunshixx@gmail.com
- 🐛 **问题反馈**: [GitHub Issues](https://github.com/sunshixx/SevGen/issues)
- 💬 **讨论**: [GitHub Discussions](https://github.com/sunshixx/SevGen/discussions)

---

<div align="center">

**⭐ 如果这个项目对您有帮助，请给我们一个星标！**

Made with ❤️ by [Your Name](https://github.com/sunshixx)

</div>
