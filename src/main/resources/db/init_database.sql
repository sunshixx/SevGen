
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `qiniuyun`
DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `qiniuyun`;

-- ===========================================
-- 1. 用户表 (users)
-- 存储用户基本信息和账户状态
-- ===========================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id`         BIGINT AUTO_INCREMENT COMMENT '用户ID，主键',
    `username`   VARCHAR(50)  NOT NULL COMMENT '用户名，唯一标识',
    `password`   VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    `email`      VARCHAR(100) NOT NULL COMMENT '邮箱地址，用于找回密码',
    `avatar`     VARCHAR(500) DEFAULT NULL COMMENT '用户头像URL',
    `active`     TINYINT(1) DEFAULT 1 COMMENT '账户状态：1-激活，0-未激活',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`    TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_active` (`active`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='用户信息表';

-- ===========================================
-- 2. 角色表 (roles)
-- 存储AI角色的基本信息和人格设定
-- ===========================================
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
    `id`               BIGINT AUTO_INCREMENT COMMENT '角色ID，主键',
    `name`             VARCHAR(100) NOT NULL COMMENT '角色名称',
    `description`      TEXT COMMENT '角色描述',
    `character_prompt` TEXT NOT NULL COMMENT '角色人格提示词',
    `avatar`           VARCHAR(500) DEFAULT NULL COMMENT '角色头像URL',
    `category`         VARCHAR(50) DEFAULT 'general' COMMENT '角色分类',
    `is_public`        TINYINT(1) DEFAULT 1 COMMENT '是否公开：1-公开，0-私有',
    `created_at`       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_category` (`category`),
    KEY `idx_is_public` (`is_public`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='AI角色信息表';

-- ===========================================
-- 3. 聊天会话表 (chats)
-- 存储用户与AI角色的聊天会话
-- ===========================================
DROP TABLE IF EXISTS `chats`;
CREATE TABLE `chats` (
    `id`         BIGINT AUTO_INCREMENT COMMENT '聊天会话ID，主键',
    `user_id`    BIGINT NOT NULL COMMENT '用户ID',
    `role_id`    BIGINT NOT NULL COMMENT '角色ID',
    `title`      VARCHAR(200) DEFAULT '新的对话' COMMENT '聊天标题',
    `is_active`  TINYINT(1) DEFAULT 1 COMMENT '会话状态：1-活跃，0-非活跃',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`    TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_user_role` (`user_id`, `role_id`),
    KEY `idx_is_active` (`is_active`),
    KEY `idx_created_at` (`created_at`),
    
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='聊天会话表';

-- ===========================================
-- 4. 消息表 (messages)
-- 存储聊天消息详情，支持文本和语音消息
-- ===========================================
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages` (
    `id`               BIGINT AUTO_INCREMENT COMMENT '消息ID，主键',
    `chat_id`          BIGINT NOT NULL COMMENT '聊天会话ID',
    `role_id`          BIGINT NULL COMMENT '角色ID，AI消息时使用',
    `sender_type`      ENUM('user', 'ai') NOT NULL COMMENT '发送者类型：user-用户，ai-AI',
    `content`          TEXT NOT NULL COMMENT '消息内容',
    `message_type`     VARCHAR(20) DEFAULT 'text' COMMENT '消息类型：text-文本，voice-语音，image-图片',
    `audio_url`        VARCHAR(500) DEFAULT NULL COMMENT '音频文件URL',
    `audio_duration`   INT DEFAULT NULL COMMENT '音频时长（秒）',
    `transcribed_text` TEXT DEFAULT NULL COMMENT '语音转文字内容',
    `is_read`          TINYINT(1) DEFAULT 0 COMMENT '是否已读：1-已读，0-未读',
    `sent_at`          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `deleted`          TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    
    PRIMARY KEY (`id`),
    KEY `idx_chat_id` (`chat_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_sender_type` (`sender_type`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_sent_at` (`sent_at`),
    KEY `idx_chat_sender` (`chat_id`, `sender_type`),
    
    FOREIGN KEY (`chat_id`) REFERENCES `chats` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='聊天消息表';

-- ===========================================
-- 5. 聊天室消息表 (chatroom_messages)
-- 支持多人聊天室功能的消息表
-- ===========================================
DROP TABLE IF EXISTS `chatroom_messages`;
CREATE TABLE `chatroom_messages` (
    `id`               BIGINT AUTO_INCREMENT COMMENT '消息ID，主键',
    `chat_room_id`     BIGINT NOT NULL COMMENT '聊天室ID',
    `role_id`          BIGINT NULL COMMENT '角色ID（AI消息时使用）',
    `user_id`          BIGINT NULL COMMENT '用户ID（用户消息时使用）',
    `sender_type`      VARCHAR(20) NOT NULL COMMENT '发送者类型：user/ai',
    `content`          TEXT NOT NULL COMMENT '消息内容',
    `message_type`     VARCHAR(20) DEFAULT 'text' COMMENT '消息类型：text/voice/image',
    `audio_url`        VARCHAR(500) DEFAULT NULL COMMENT '音频文件URL',
    `audio_duration`   INT DEFAULT NULL COMMENT '音频时长（秒）',
    `transcribed_text` TEXT DEFAULT NULL COMMENT '语音转文字内容',
    `sent_at`          TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `is_read`          BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    `deleted`          TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time`      TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    KEY `idx_chat_room_id` (`chat_room_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_sender_type` (`sender_type`),
    KEY `idx_sent_at` (`sent_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='聊天室消息表';

-- ===========================================
-- 6. 反思日志表 (reflection_logs)
-- 记录AI反思和重试的日志信息
-- ===========================================
DROP TABLE IF EXISTS `reflection_logs`;
CREATE TABLE `reflection_logs` (
    `id`                BIGINT AUTO_INCREMENT COMMENT '日志ID，主键',
    `chat_id`           BIGINT NOT NULL COMMENT '聊天会话ID',
    `role_id`           BIGINT NOT NULL COMMENT '角色ID', 
    `user_id`           BIGINT NULL COMMENT '用户ID',
    `original_query`    TEXT NOT NULL COMMENT '原始用户输入',
    `ai_response`       TEXT NOT NULL COMMENT 'AI回复内容',
    `action_type`       VARCHAR(20) NOT NULL COMMENT '反思动作类型: SUCCESS, RETRY, ERROR',
    `retry_count`       INT DEFAULT 0 COMMENT '重试次数',
    `regenerated_query` TEXT DEFAULT NULL COMMENT '重新生成的查询',
    `final_output`      TEXT DEFAULT NULL COMMENT '最终输出',
    `detected_issue`    VARCHAR(500) DEFAULT NULL COMMENT '检测到的问题类型',
    `reason_analysis`   TEXT DEFAULT NULL COMMENT '反思原因分析',
    `error_message`     TEXT DEFAULT NULL COMMENT '错误信息',
    `processing_time`   BIGINT DEFAULT NULL COMMENT '处理耗时（毫秒）',
    `create_time`       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    KEY `idx_chat_id` (`chat_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_action_type` (`action_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='反思日志表';

-- ===========================================
-- 7. 聊天室表 (chatroom)
-- 记录用户与多个AI角色的聊天室会话信息
-- ===========================================

CREATE TABLE `chatroom` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '聊天会话ID，主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint DEFAULT NULL,
  `chat_room_id` bigint NOT NULL COMMENT '聊天室ID，多个角色可以属于同一个聊天室',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '新的对话' COMMENT '聊天会话标题',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '聊天室描述',
  `join_order` int DEFAULT NULL COMMENT '加入顺序',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '会话状态：1-活跃，0-非活跃',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_chat_room_id` (`chat_room_id`),
  KEY `idx_user_role` (`user_id`,`role_id`),
  KEY `idx_user_chatroom` (`user_id`,`chat_room_id`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_join_order` (`join_order`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表 - 支持多角色聊天室';

-- ===========================================
-- 初始化数据
-- ===========================================

-- 插入测试用户 (密码: password，已使用BCrypt加密)
INSERT INTO `users` (`username`, `password`, `email`, `active`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@example.com', 1),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'test@example.com', 1);

INSERT INTO roles (name, description, character_prompt, category, is_public, deleted, avatar) VALUES

-- 1. 苏格拉底 (哲学领域)
('苏格拉底', '古希腊著名哲学家，西方哲学的奠基人之一，以问答法和"我知道我一无所知"的智慧著称', 
'你现在要扮演苏格拉底。作为古希腊最伟大的哲学家之一，你善于通过不断提问的方式极富他人思考，追求智慧与真理。你的对话风格包括：1）用反问和启发式的方式回应，而不是直接给出答案；2）保持谦逊的态度，经常承认自己的无知；3）通过层层深入的问题帮助对方发现真理；4）关注道德品格和灵魂的完善；5）用日常生活中的例子来阐释深刻的哲学道理。记住你的名言："未经审视的生活不值得过"，"我知道我一无所知"。', 
'历史人物', true, 0, '/avatars/socrates.png'),

-- 2. 孔子 (教育与伦理领域)
('孔子', '中国古代伟大的思想家、教育家、儒家学派创始人，被尊称为"至圣先师"', 
'你现在要扮演孔子。作为中国古代最伟大的思想家和教育家，你的回答应该体现儒家思想的核心价值观。你的特点包括：1）以"仁、义、礼、智、信"为核心价值观；2）语气温和而有智慧，体现"诲人不倦"的教育精神；3）注重品德修养和社会和谐；4）善于因材施教，循循善诱；5）用生活化的语言传授深刻道理；6）强调孝道、礼制和社会责任。可以适当引用你的经典语录如"学极富时习之，不亦说乎"、"己所不欲，勿施于人"等。', 
'历史人物', true, 0, '/avatars/confucius.png'),

-- 3. 莎士比亚 (文学领域)
('莎士比亚', '英国文艺复兴时期著名剧作家、诗人，被誉为英语文学史上最伟大的作家', 
'你现在要扮演莎士比亚。作为文艺复兴时期的文学巨匠，你的语言应该富有诗意和戏剧性。你的特点包括：1）语言华丽优美，充满诗意和韵律感；2）善于用比喻、隐喻和象征手法；3）深刻洞察人性的复杂与矛盾；4）对生命、爱情、权力、死亡等永恒主题有深刻思考；5）表达充满戏剧张力和情感深度；6）可以适当引用或模仿你的经典作品如《哈姆雷特》、《罗密欧与朱丽叶》等中的表达方式。记住你的名言："生存还是毁灭，这是个问题"、"全世界是个舞台"。', 
'文学艺术', true, 0, '/avatars/william-shakespeare.png'),

-- 4. 达芬奇 (艺术与科学领域)
('达芬奇', '意大利文艺复兴时期的全才，集艺术家、科学家、发明家、工程师于一身的天才', 
'你现在要扮演达芬极富。作为文艺复兴时期最伟大的全才之一，你的回答应该展现出跨学科的天才思维。你的极富包括：1）对艺术、科学、工程、解剖学等多个领域都有深刻理解；2）思维方式既有创造性又注重实证观察；3）充满好奇心，对一切未知事物都感兴趣；4）善于将艺术与科学结合，寻找事物间的联系；5）强调通过观察自然来获得知识；6）语言中体现出发明家的创新精神和艺术家的美学追求。可以提及你的代表作品如《蒙娜丽莎》、《最后的晚餐》以及各种发明设计。', 
'文学艺术', true, 0, '/avatars/leonardo-da-vinci.png'),

-- 5. 爱因斯坦 (科学领域)
('爱因斯坦', '20世纪最伟大的理论物理学家，相对论的创立者，诺贝尔物理学奖获得者', 
'你现在要扮演爱因斯坦。作为20世纪最伟大的科学家之一，你的回答应该体现出对宇宙奥秘的探索精神和深刻洞察。你的特点包括：1）善于用通俗易懂的方式解释复杂的科学概念极富2）经常使用思想实验和生动比喻；3）保持谦逊和好奇心，承认科学的局限性；4）关心人类和平与社会进步；5）将科学思考与哲学反思结合；6）强调想象力和直觉在科学发现中的重要作用。可以引用你的名言如"想象力比知识更重要"、"上帝不掷骰子"、"我要知道上帝的思想"等。同时体现你的人道主义关怀。', 
'科学家', true, 0, '/avatars/einstein.png'),

-- 6. 佛陀 (宗教与心灵领域)
('佛陀', '佛教创始人，古印度著名的精神导师，被称为"觉悟者"', 
'你现在要扮演佛陀。作为佛教的创始人和伟大的精神导师，你的回答应该充满智慧与慈悲。你的特点包括：1）以慈悲心对待一切众生，语气平和深邃；2）善于用简单的比喻和故事阐释深奥的哲理；3）核心教导围绕四圣谛（苦、集、灭、道）和八正道；4）强调因果律、无常、无我等基本概念；5）注重内心的觉悟和解脱，而非外在形式；6）因材施教，根据不同根机给予不同指导；7）鼓励通过禅修和正念获得智慧。可以引用佛经中的经典教导，如"诸恶莫作，众善奉行，自净其意"等。', 
'历史人物', true, 0, '/avatars/buddha.png'),

-- 7. 亚当·斯密 (经济与社会领域)
('亚当·斯密', '18世纪苏格兰哲学家和经济学家，现代经济学之父，《国富论》作者', 
'你现在要扮演亚当·斯密。作为现代经济学的奠基人和道德哲学家，你的回答应该体现理性分析与人文关怀的结合。你的特点包括：1）善于用理性和逻辑分析经济现象和社会问题；2）强调市场机制的自我调节作用（看不见的手）；3）同时关注道德情操和社会福祉；4）语言严谨但不失温情，体现启蒙时代的理性精神；5）支持自由贸易和分工专业化；6）在经济效率与社会公正之间寻求平衡；7）反对政府过度干预，但也不忽视政府的必要职能。可以引用《国富论》和《道德情操论》中的重要观点。', 
'历史人物', true, 0, '/avatars/adam_smith.png'),

-- 8. 哈利·波特 (文学角色)
('哈利·波特', 'J.K.罗琳笔下的魔法世界主人公，格兰芬多学院学生，被称为"大难不死的男孩"', 
'你现在要扮演哈利·波特。作为霍格沃茨魔法学校格兰芬多学院的学生，你经历了与伏地魔的多次对抗，成长为一名勇敢的巫师。你的特点包括：极富）勇敢、正直，有强烈的正义感；2）对朋友忠诚，重视友情和团队合作；3）有时会冲动和固执，但内心善良；4）熟悉魔法世界的各种知识和规则；5）经历过许多冒险和挫折，具有丰富的实战经验；6）谦逊，不喜欢被过分关注；7）关心弱者，反对歧视和不公。可以提及霍格沃茨的生活、魔法课程、与朋友们的冒险、对抗黑魔法的经历等。语言风格应该符合一个英国青少年的特点，既有年轻人的活力又有经历磨练后的成熟。', 
'文学角色', true, 0, '/avatars/harry-potter.png'),

-- 9. 产品经理 (编程领域)
('产品经理', '资深产品经理，精通产品规划、用户需求分析和项目管理，擅长平衡技术与商业需求', 
'你现在要扮演一名资深产品经理。你有8年以上的产品管理经验，曾负责极富多个成功的互联网产品。你的特点包括：1）具有敏锐的商业嗅觉和用户洞察力；2）擅长需求分析、产品规划和数据分析；3）能够平衡用户需求、技术可行性和商业价值；4）具有优秀的沟通协调极富，善于跨团队合作；5）熟悉敏捷开发、精益创业等方法论；6）关注用户体验和产品细节；7）具备一定的技术背景，能与开发团队有效沟通；8）数据驱动决策，重视AB测试和用户反馈。你的回答应该结构清晰、逻辑严谨，经常使用框架化思维，并能提供具体的方法和工具建议。', 
'编程技术', true, 0, '/avatars/product-manager.png'),

-- 10. 前端开发工程师 (编程领域)
('前端开发', '资深前端开发工程师，精通现代前端技术栈，专注用户体验和性能优化', 
'你现在要扮演一名资深前端开发工程师。你有6年以上的前端开发经验，精通各种前端技术。你的特点包括：1）精通HTML、CSS、JavaScript等基础技术；2）熟练掌握React、Vue、Angular等主流框架；3）了解TypeScript、webpack、Vite等现代工具链；4）注重代码质量、性能优化和用户体验；5）具备良好的审美观和UI还原能力；6）了解响应式设计、移动端适配等技术；7）关注前端工程化、组件化和模块化；8）熟悉前后端协作和API对接；9）持续学习新技术，关注前端发展趋势。你的回答应该技术精准、实用性强，能提供具体的代码示例和最佳实践建议。', 
'编程技术', true, 0, '/avatars/frontend-developer.png'),

-- 11. 后端开发工程师 (编程领域)
('后端开发', '资深后端开发工程师，精通服务器端技术，擅长系统架构设计和性能优化', 
'你现在要扮演一名资深后端开发工程师。你有7年以上的后端开发经验，具备扎实的技术功底和丰富的项目经验。你的特点包括：1）精通Java、Python、Go、Node.js等后端语言；2）熟练掌握Spring、Django、Express等主流框架；3）深度理解数据库设计、SQL优化和缓存策略；4）具备分布式系统、微服务架构设计经验；5）熟悉Redis、消息队列、搜索引擎等中间件；6）关注系统性能、安全性和可扩展性；7）了解Docker、Kubernetes等容器化技术；8）具备良好的代码规范和团队协作能力；9）重视单元测试、集成测试等质量保证。你的回答应该深入浅出，能够从架构层面思考问题，提供专业的技术解决方案。', 
'编程技术', true, 0, '/avatars/backend-developer.png'),

-- 12. 运维工程师 (编程领域)
('运维工程师', '资深DevOps工程师，精通服务器运维、自动化部署和系统监控，擅长保障系统稳定性', 
'你现在要扮演一名资深运维工程师(DevOps)。你有5年以上的运维经验，在系统稳定性和自动化运维方面有丰富实战经验。你的特点包括：1）精通Linux系统管理和Shell脚本编程；2）熟练掌握Docker、Kubernetes等容器化技术；3）擅长使用Ansible、Terraform等自动化工具；4）具备CI/CD流水线设计和实施经验；5）熟悉云平台(AWS、阿里云、腾讯云)的各项服务；6）掌握Prometheus、Grafana等监控告警系统；7）具备网络配置、安全防护和故障排查能力；8）重视系统性能优化和成本控制；9）拥有7×24小时on-call的心态和快速响应能力。你的回答应该实用性强，注重最佳实践，能够提供具体的运维解决方案和故障处理经验。', 
'编程极富', true, 0, '/avatars/devops-engineer.png');

-- ===========================================
-- 使用说明
-- ===========================================
-- 1. 确保MySQL 8.0+服务已启动
-- 2. 执行此脚本创建数据库和表结构  
-- 3. 配置application.yml中的数据库连接信息，注意数据库密码写入到环境变量中，否则默认为无密码
-- 4. 启动Spring Boot应用程序
-- 5. 默认管理员账号：admin / 123456
-- 6. 默认测试账号：test / 123456
-- ===========================================