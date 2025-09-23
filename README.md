# AI角色扮演聊天应用后端

这是一个基于Spring Boot的AI角色扮演聊天应用后端。用户可以搜索并选择自己感兴趣的角色（如哈利波特、苏格拉底等），并与这些角色进行语音或文本聊天。

## 技术栈

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- MySQL
- Spring AI (OpenAI集成)
- LangChain4J

## 项目结构

```
src/main/java/com/aichat/roleplay/
├── RolePlayApplication.java       # 主应用程序入口
├── config/                        # 配置类
│   ├── SecurityConfig.java        # Spring Security配置
│   ├── OpenAIConfig.java          # OpenAI客户端配置
│   └── DataLoader.java            # 数据加载器
├── controller/                    # REST控制器
│   ├── AuthController.java        # 用户认证控制器
│   ├── RoleController.java        # 角色控制器
│   ├── ChatController.java        # 聊天控制器
│   └── MessageController.java     # 消息控制器
├── model/                         # 实体类
│   ├── User.java                  # 用户实体
│   ├── Role.java                  # 角色实体
│   ├── Chat.java                  # 聊天会话实体
│   └── Message.java               # 消息实体
├── repository/                    # 数据访问层
│   ├── UserRepository.java        # 用户Repository
│   ├── RoleRepository.java        # 角色Repository
│   ├── ChatRepository.java        # 聊天Repository
│   └── MessageRepository.java     # 消息Repository
├── service/                       # 服务层
│   ├── UserService.java           # 用户服务
│   ├── RoleService.java           # 角色服务
│   ├── ChatService.java           # 聊天服务
│   └── MessageService.java        # 消息服务
└── exception/                     # 异常处理
    └── GlobalExceptionHandler.java # 全局异常处理器
```

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

这是一个初始版本，后续可以添加以下功能：

- 语音转文本和文本转语音功能
- 更多角色定制选项
- 用户创建自定义角色
- 聊天历史记录管理
- 用户个人资料设置

## 注意事项

- 本项目需要有效的OpenAI API密钥才能正常工作
- 默认情况下，应用程序运行在8080端口
