-- 聊天室多角色协作功能测试数据
-- 执行前请确保数据库连接正常

-- 1. 创建测试用户
INSERT INTO users (id, username, email, password, create_time, update_time, deleted) 
VALUES (100, 'collaboration_test_user', 'collab_test@example.com', '$2a$10$example_hashed_password', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 2. 创建测试角色
INSERT INTO roles (id, name, description, prompt, is_public, create_time, update_time, deleted) 
VALUES 
(101, '技术专家', '资深技术顾问，擅长技术架构和解决方案设计', 
 '你是一位拥有15年经验的资深技术专家。你擅长分析复杂的技术问题，提供切实可行的解决方案。请用专业但易懂的语言回答问题，并提供具体的技术建议。', 
 1, NOW(), NOW(), 0),

(102, '产品经理', '产品策略专家，专注用户需求和市场分析', 
 '你是一位经验丰富的产品经理，拥有多年的产品规划和用户研究经验。你善于从用户角度思考问题，关注市场趋势和商业价值。请提供实用的产品建议和策略分析。', 
 1, NOW(), NOW(), 0),

(103, 'UI/UX设计师', '用户体验设计专家，关注界面设计和用户体验', 
 '你是一位创意十足的UI/UX设计师，专注于用户体验设计和界面优化。你了解最新的设计趋势，善于平衡美观性和可用性。请从设计角度提供专业建议。', 
 1, NOW(), NOW(), 0),

(104, '数据分析师', '数据驱动决策专家，擅长数据分析和洞察', 
 '你是一位专业的数据分析师，擅长从数据中发现规律和洞察。你能够用数据支撑决策，提供量化的分析结果。请用数据思维分析问题并提供建议。', 
 1, NOW(), NOW(), 0),

(105, '市场营销专家', '营销策略专家，专注品牌推广和用户增长', 
 '你是一位资深的市场营销专家，在品牌建设和用户增长方面有丰富经验。你了解各种营销渠道和策略，善于制定有效的推广方案。请从营销角度提供专业建议。', 
 1, NOW(), NOW(), 0)

ON DUPLICATE KEY UPDATE 
name = VALUES(name), 
description = VALUES(description), 
prompt = VALUES(prompt);

-- 3. 创建测试聊天室
INSERT INTO chats (id, user_id, role_id, title, description, is_active, create_time, update_time, deleted) 
VALUES 
(201, 100, 101, '多角色协作测试聊天室', '用于测试多角色协作功能的聊天室', 1, NOW(), NOW(), 0),
(202, 100, 102, '产品设计讨论室', '产品设计和用户体验讨论', 1, NOW(), NOW(), 0),
(203, 100, 103, '技术方案评审室', '技术方案讨论和评审', 1, NOW(), NOW(), 0)

ON DUPLICATE KEY UPDATE 
title = VALUES(title), 
description = VALUES(description);

-- 4. 创建一些测试消息（可选）
INSERT INTO messages (id, chat_id, sender_type, content, create_time, update_time, deleted) 
VALUES 
(301, 201, 'USER', '大家好，我想了解一下如何设计一个高并发的电商系统', NOW(), NOW(), 0),
(302, 201, 'AI', '从技术角度来看，高并发电商系统需要考虑以下几个关键点...', NOW(), NOW(), 0),
(303, 202, 'USER', '我们的移动应用用户留存率较低，请帮忙分析原因', NOW(), NOW(), 0)

ON DUPLICATE KEY UPDATE content = VALUES(content);

-- 验证数据插入
SELECT '=== 测试用户 ===' as info;
SELECT id, username, email FROM users WHERE id = 100;

SELECT '=== 测试角色 ===' as info;
SELECT id, name, description FROM roles WHERE id BETWEEN 101 AND 105;

SELECT '=== 测试聊天室 ===' as info;
SELECT id, user_id, role_id, title, description FROM chats WHERE id BETWEEN 201 AND 203;

SELECT '=== 测试消息 ===' as info;
SELECT id, chat_id, sender_type, LEFT(content, 50) as content_preview FROM messages WHERE id BETWEEN 301 AND 303;

-- 提示信息
SELECT '测试数据准备完成！' as status, 
       '可以使用chatId=201进行Postman测试' as next_step;