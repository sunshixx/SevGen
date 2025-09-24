# 用户认证API使用指南

## 概述

本项目实现了完整的用户认证功能，包括：
- 基于邮箱验证码的用户注册
- 用户登录/登出
- JWT Token认证
- 获取当前用户信息

严格遵循SOLID原则，代码结构清晰，易于维护和扩展。

## API接口

### 1. 用户登录

**接口地址：** `POST /api/auth/login`

**请求体：**
```json
{
  "username": "testuser",
  "password": "123456"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com",
      "avatar": null,
      "active": true
    }
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 用户登出

**接口地址：** `POST /api/auth/logout`

**请求头：** `Authorization: Bearer <token>` （可选）

**响应示例：**
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null,
  "timestamp": "2024-01-01T12:00:00"
}
```

### 3. 获取当前用户信息

**接口地址：** `GET /api/auth/me`

**请求头：** `Authorization: Bearer <token>`

**响应示例：**
```json
{
  "code": 200,
  "message": "获取用户信息成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "avatar": null,
    "active": true
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### 4. 发送邮箱验证码

**接口地址：** `POST /api/auth/send-verification-code`

**请求体：**
```json
{
  "email": "user@example.com"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "验证码发送成功，请查收邮件",
  "data": null,
  "timestamp": "2024-01-01T12:00:00"
}
```

### 5. 用户注册

**接口地址：** `POST /api/auth/register`

**请求体：**
```json
{
  "username": "testuser",
  "password": "123456",
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "用户注册成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "user@example.com",
    "active": true,
    "createTime": "2024-01-01T12:00:00"
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### 6. 健康检查

**接口地址：** `GET /api/auth/health`

**响应示例：**
```json
{
  "code": 200,
  "message": "Auth service is running",
  "data": null,
  "timestamp": "2024-01-01T12:00:00"
}
```

## 使用流程

### 用户注册流程：
1. **发送验证码**：用户输入邮箱地址，系统发送6位数字验证码到邮箱
2. **验证码有效期**：验证码有效期为5分钟
3. **用户注册**：用户填写注册信息（用户名、密码、邮箱、验证码），系统验证后完成注册

### 用户登录流程：
1. **用户登录**：用户输入用户名和密码进行登录
2. **获取Token**：登录成功后获得JWT Token
3. **访问受保护资源**：在请求头中携带Token访问需要认证的接口
4. **用户登出**：调用登出接口清理用户状态

## 验证规则

- **用户名**：3-20个字符，不能重复
- **密码**：6-20个字符
- **邮箱**：有效的邮箱格式，不能重复
- **验证码**：6位数字，5分钟内有效

## 技术特点

### SOLID原则体现

1. **单一职责原则 (SRP)**：
   - `AuthController`: 只负责HTTP请求处理
   - `UserServiceImpl`: 只负责用户业务逻辑
   - `EmailServiceImpl`: 只负责邮件发送

2. **开放封闭原则 (OCP)**：
   - 通过接口抽象，易于扩展新功能

3. **里氏替换原则 (LSP)**：
   - 接口实现可以互相替换

4. **接口隔离原则 (ISP)**：
   - `IUserService`、`IEmailService` 接口专注特定功能

5. **依赖倒置原则 (DIP)**：
   - 控制器依赖服务接口，而非具体实现

### 简化设计

- 使用内存缓存存储验证码（生产环境建议使用Redis）
- 邮件服务使用模拟实现（可轻松替换为真实邮件服务）
- 密码暂未加密（生产环境需要使用BCrypt等加密）

## 扩展建议

1. **密码加密**：使用BCrypt加密存储密码
2. **缓存优化**：使用Redis存储验证码
3. **真实邮件服务**：集成阿里云邮件或其他邮件服务
4. **限流防刷**：添加发送频率限制
5. **日志审计**：完善操作日志记录

## 测试命令

```bash
# 1. 用户登录
curl -X POST http://localhost:16999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

# 2. 获取当前用户信息（需要先登录获取token）
curl -X GET http://localhost:16999/api/auth/me \
  -H "Authorization: Bearer <your_token>"

# 3. 用户登出
curl -X POST http://localhost:16999/api/auth/logout \
  -H "Authorization: Bearer <your_token>"

# 4. 发送验证码
curl -X POST http://localhost:16999/api/auth/send-verification-code \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'

# 5. 用户注册（使用控制台输出的验证码）
curl -X POST http://localhost:16999/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456","email":"test@example.com","verificationCode":"123456"}'
```

注意：验证码会在控制台日志中输出，用于测试。