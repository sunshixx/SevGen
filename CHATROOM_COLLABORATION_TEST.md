# 聊天室多角色协作功能 - Postman 测试用例

## 接口信息

**接口地址**: `GET /api/sse/collaborate`  
**接口类型**: Server-Sent Events (SSE)  
**功能描述**: 在聊天室内实现多个AI角色协作回答用户问题

## 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| chatId | Long | 是 | 聊天室ID |
| userMessage | String | 是 | 用户消息内容 |
| context | String | 否 | 上下文信息 |

## Postman 测试步骤

### 1. 准备测试数据

首先确保数据库中有以下测试数据：

#### 创建测试用户
```sql
INSERT INTO users (id, username, email, password, create_time, update_time) 
VALUES (1, 'testuser', 'test@example.com', 'password123', NOW(), NOW());
```

#### 创建测试角色
```sql
INSERT INTO roles (id, name, description, prompt, is_public, create_time, update_time) 
VALUES 
(1, '技术专家', '专业的技术顾问', '你是一位资深的技术专家，擅长分析技术问题和提供解决方案。', 1, NOW(), NOW()),
(2, '产品经理', '产品策略专家', '你是一位经验丰富的产品经理，擅长产品规划和用户需求分析。', 1, NOW(), NOW()),
(3, '设计师', 'UI/UX设计专家', '你是一位创意设计师，擅长用户体验设计和界面优化。', 1, NOW(), NOW());
```

#### 创建测试聊天室
```sql
INSERT INTO chats (id, user_id, role_id, title, is_active, create_time, update_time) 
VALUES (1, 1, 1, '多角色协作测试聊天室', 1, NOW(), NOW());
```

### 2. Postman 配置

#### 请求设置
- **方法**: GET
- **URL**: `http://localhost:8080/api/sse/collaborate`
- **Headers**: 
  ```
  Accept: text/event-stream
  Cache-Control: no-cache
  ```

#### 请求参数 (Query Parameters)
```
chatId: 1
userMessage: 请帮我分析一下如何设计一个用户友好的移动应用登录界面
context: 这是一个面向年轻用户的社交应用
```

### 3. 测试用例

#### 测试用例 1: 基本多角色协作
**请求参数**:
```
chatId: 1
userMessage: 请分析一下人工智能在教育领域的应用前景
context: 针对K12教育市场
```

**期望响应格式**:
```
data: [ROLE_SELECTION] {"roles":[{"roleName":"技术专家","executionOrder":1,"customPrompt":"从技术实现角度分析","reason":"需要技术专业知识"},{"roleName":"产品经理","executionOrder":2,"customPrompt":"从市场和用户需求角度分析","reason":"需要市场分析"}],"isParallel":false}

data: [ROLE_START] 技术专家

data: [ROLE_RESPONSE] 从技术角度来看，人工智能在K12教育领域有以下应用前景...

data: [ROLE_START] 产品经理

data: [ROLE_RESPONSE] 从产品和市场角度分析，AI教育产品需要考虑...

data: [COMPLETE]
```

#### 测试用例 2: 并行执行模式
**请求参数**:
```
chatId: 1
userMessage: 如何提升网站的用户体验？
context: 电商网站，主要用户是25-40岁的上班族
```

#### 测试用例 3: 单角色回答
**请求参数**:
```
chatId: 1
userMessage: 什么是RESTful API？
context: 技术学习
```

#### 测试用例 4: 错误处理 - 聊天室不存在
**请求参数**:
```
chatId: 999
userMessage: 测试消息
context: 测试上下文
```

**期望响应**:
```
data: [ERROR] 聊天室不存在: 999
```

### 4. 响应事件类型说明

| 事件类型 | 描述 | 示例 |
|----------|------|------|
| [ROLE_SELECTION] | 角色选择结果 | 包含选中的角色列表和执行模式 |
| [ROLE_START] | 角色开始回答 | 指示某个角色开始生成回答 |
| [ROLE_RESPONSE] | 角色回答内容 | 角色生成的具体回答文本 |
| [ROLE_ERROR] | 角色回答错误 | 某个角色回答过程中出现的错误 |
| [COMPLETE] | 协作完成 | 所有角色回答完成 |
| [ERROR] | 系统错误 | 系统级别的错误信息 |

### 5. 注意事项

1. **SSE连接**: 确保Postman支持SSE流式响应，建议使用Postman的"Send and Download"功能
2. **超时设置**: SSE连接超时时间为5分钟（300秒）
3. **并发测试**: 可以同时发起多个请求测试并发处理能力
4. **日志查看**: 测试时可以查看后端日志了解详细执行过程

### 6. 故障排除

#### 常见问题
1. **连接超时**: 检查网络连接和服务器状态
2. **角色选择失败**: 检查LLM配置和API密钥
3. **数据库错误**: 确认测试数据已正确插入
4. **权限问题**: 确认用户有访问聊天室的权限

#### 调试建议
1. 先测试简单的单角色场景
2. 检查后端日志中的详细错误信息
3. 验证数据库中的测试数据完整性
4. 确认LangChain4j配置正确

## 扩展测试

### 性能测试
- 测试大量并发SSE连接
- 测试长时间连接的稳定性
- 测试复杂角色选择场景的响应时间

### 边界测试
- 测试超长消息内容
- 测试特殊字符和多语言内容
- 测试网络中断后的重连机制