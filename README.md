# AI角色扮演聊天应用后端

这是一个基于Spring Boot的AI角色扮演聊天应用初始后端框架。用户可以搜索并选择自己感兴趣的角色（如哈利波特、苏格拉底等），并与这些角色进行语音或文本聊天。

## 技术栈

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- MySQL
- Spring AI (OpenAI集成)

## 配置要求

1. **数据库配置**：
    - 在`application.properties`中配置MySQL连接信息
    - 创建名为`roleplay`的数据库

2. **OpenAI API配置**：
    - 设置环境变量`OPENAI_API_KEY`为你的OpenAI API密钥
    - 或直接在`application.properties`中配置

## API端点

### 认证

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前登录用户信息

### 角色

- `GET /api/roles` - 获取所有公开角色
- `GET /api/roles/search?query=xxx` - 搜索角色
- `GET /api/roles/category/{category}` - 按分类获取角色
- `GET /api/roles/{id}` - 获取单个角色详情

### 聊天

- `POST /api/chats` - 创建新的聊天会话
- `GET /api/chats` - 获取用户的所有聊天会话
- `GET /api/chats/active` - 获取用户的活跃聊天会话
- `GET /api/chats/{id}` - 获取单个聊天会话详情

### 消息

- `POST /api/messages` - 发送消息并获取AI回复
- `GET /api/messages/chat/{chatId}` - 获取聊天会话的所有消息
- `GET /api/messages/chat/{chatId}/unread` - 获取未读消息
- `PUT /api/messages/chat/{chatId}/read` - 将消息标记为已读

## 预设角色

应用启动时会自动加载以下预设角色：

- 哈利波特（文学人物）
- 苏格拉底（历史人物）
- 爱因斯坦（科学家）

## 如何运行

1. 确保已安装JDK 17和Maven
2. 配置MySQL数据库
3. 设置OpenAI API密钥
4. 运行以下命令：
   ```
   mvn spring-boot:run
   ```

## 项目状态

这是一个初始版本，后续计划添加以下功能：

- 语音转文本和文本转语音功能
- 更多角色定制选项
- 用户创建自定义角色
- 聊天历史记录管理
- 用户个人资料设置
