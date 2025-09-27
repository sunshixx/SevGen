package com.aichat.roleplay.util;

import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 角色Prompt工程服务
 * 根据不同角色类型提供定制化的prompt策略
 */
@Service
public class RolePromptEngineering {

    private static final Logger logger = LoggerFactory.getLogger(RolePromptEngineering.class);

    @Autowired
    private RagService ragService;

    /**
     * 根据角色类型构建优化的prompt
     *
     * @param role 角色信息
     * @param userMessage 用户消息
     * @param chatHistory 聊天历史
     * @return 优化后的完整prompt
     */
    public String buildOptimizedPrompt(Role role, String userMessage, String chatHistory) {
        StringBuilder promptBuilder = new StringBuilder();
        
        // 根据角色分类选择不同的prompt策略
        RoleType roleType = determineRoleType(role);
        
        switch (roleType) {
            case HISTORICAL_FIGURE:
                buildHistoricalFigurePrompt(promptBuilder, role);
                break;
            case FICTIONAL_CHARACTER:
                buildFictionalCharacterPrompt(promptBuilder, role);
                break;
            case PROFESSIONAL_EXPERT:
                buildProfessionalExpertPrompt(promptBuilder, role);
                break;
            case COMPANION_FRIEND:
                buildCompanionFriendPrompt(promptBuilder, role);
                break;
            case TEACHER_MENTOR:
                buildTeacherMentorPrompt(promptBuilder, role);
                break;
            default:
                buildGenericRolePrompt(promptBuilder, role);
        }
        
        // 添加RAG知识背景（在角色设定后，行为规范前）
        addRagKnowledgeBackground(promptBuilder, role, userMessage);
        
        // 添加通用的行为规范
        addCommonBehaviorGuidelines(promptBuilder);
        
        // 添加聊天历史
        if (StringUtils.hasText(chatHistory)) {
            promptBuilder.append("\n对话历史：\n");
            promptBuilder.append(chatHistory).append("\n");
        }
        
            // 添加用户问题（单独一行，避免meta指令混入）
            promptBuilder.append("\n用户问题：").append(userMessage).append("\n");
        
            // 添加回复指令（单独一行，便于LLM区分）
            promptBuilder.append("\n[指令] 现在请以").append(role.getName()).append("的身份回复用户的问题。只输出").append(role.getName()).append("的回复内容，不要重复用户的问题。\n");

        return promptBuilder.toString();
    }

    /**
     * 根据角色信息判断角色类型
     */
    private RoleType determineRoleType(Role role) {
        String category = role.getCategory();
        String name = role.getName().toLowerCase();
        String description = role.getDescription().toLowerCase();
        String characterPrompt = role.getCharacterPrompt().toLowerCase();
        
        // 历史人物判断
        if ("历史人物".equals(category) || 
            containsKeywords(name + description + characterPrompt, 
                "历史", "古代", "朝代", "皇帝", "诗人", "哲学家", "思想家")) {
            return RoleType.HISTORICAL_FIGURE;
        }
        
        // 虚拟角色判断
        if ("虚拟角色".equals(category) || 
            containsKeywords(name + description + characterPrompt,
                "动漫", "游戏", "小说", "漫画", "二次元", "角色")) {
            return RoleType.FICTIONAL_CHARACTER;
        }
        
        // 专业专家判断
        if ("专业专家".equals(category) || 
            containsKeywords(name + description + characterPrompt,
                "专家", "教授", "医生", "律师", "工程师", "分析师", "顾问")) {
            return RoleType.PROFESSIONAL_EXPERT;
        }
        
        // 陪伴朋友判断
        if ("陪伴聊天".equals(category) || 
            containsKeywords(name + description + characterPrompt,
                "朋友", "伙伴", "陪伴", "聊天", "倾听", "安慰")) {
            return RoleType.COMPANION_FRIEND;
        }
        
        // 导师老师判断
        if ("教育指导".equals(category) || 
            containsKeywords(name + description + characterPrompt,
                "老师", "导师", "教练", "指导", "教育", "学习")) {
            return RoleType.TEACHER_MENTOR;
        }
        
        return RoleType.GENERIC;
    }
    
    /**
     * 关键词匹配辅助方法
     */
    private boolean containsKeywords(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建历史人物专用prompt
     */
    private void buildHistoricalFigurePrompt(StringBuilder promptBuilder, Role role) {
        promptBuilder.append("你是：").append(role.getName()).append("\n");
        promptBuilder.append("背景：").append(role.getCharacterPrompt()).append("\n\n");
        promptBuilder.append("对话要求：\n");
        promptBuilder.append("1. 直接承认身份：\"我是").append(role.getName()).append("\"\n");
        promptBuilder.append("2. 用现代日常语言对话，不要用古文或过于正式的表达\n");
        promptBuilder.append("3. 可以分享相关的历史背景，但语言要简单易懂\n");
        promptBuilder.append("4. 保持友好自然的交流方式\n");
    }

    /**
     * 构建虚拟角色专用prompt
     */
    private void buildFictionalCharacterPrompt(StringBuilder promptBuilder, Role role) {
        promptBuilder.append("你是虚拟角色：").append(role.getName()).append("\n");
        promptBuilder.append("角色设定：").append(role.getCharacterPrompt()).append("\n\n");
        promptBuilder.append("虚拟角色对话特点：\n");
        promptBuilder.append("1. 直接承认自己是").append(role.getName()).append("，不回避身份\n");
        promptBuilder.append("2. 保持角色的性格特点和说话习惯\n");
        promptBuilder.append("3. 可以引用角色的背景故事和经历\n");
        promptBuilder.append("4. 用角色独有的口头禅或表达方式\n");
        promptBuilder.append("5. 避免过分戏剧化，保持自然对话\n");
    }

    /**
     * 构建专业专家专用prompt
     */
    private void buildProfessionalExpertPrompt(StringBuilder promptBuilder, Role role) {
        promptBuilder.append("你是专业专家：").append(role.getName()).append("\n");
        promptBuilder.append("专业背景：").append(role.getCharacterPrompt()).append("\n\n");
        promptBuilder.append("专业专家对话特点：\n");
        promptBuilder.append("1. 明确表明身份\"我是").append(role.getName()).append("\"\n");
        promptBuilder.append("2. 提供专业、准确的信息和建议\n");
        promptBuilder.append("3. 用专业但易懂的语言解释复杂概念\n");
        promptBuilder.append("4. 必要时可以要求更多信息以提供准确建议\n");
        promptBuilder.append("5. 保持专业严谨，但语气友好平易近人\n");
    }

    /**
     * 构建陪伴朋友专用prompt
     */
    private void buildCompanionFriendPrompt(StringBuilder promptBuilder, Role role) {
        promptBuilder.append("你是朋友角色：").append(role.getName()).append("\n");
        promptBuilder.append("朋友特点：").append(role.getCharacterPrompt()).append("\n\n");
        promptBuilder.append("陪伴朋友对话特点：\n");
        promptBuilder.append("1. 自然地介绍自己\"我是").append(role.getName()).append("，你的朋友\"\n");
        promptBuilder.append("2. 用温暖、理解的语气与用户对话\n");
        promptBuilder.append("3. 主动关心用户的感受和需求\n");
        promptBuilder.append("4. 提供情感支持和积极建议\n");
        promptBuilder.append("5. 保持轻松友好的对话氛围\n");
    }

    /**
     * 构建导师老师专用prompt
     */
    private void buildTeacherMentorPrompt(StringBuilder promptBuilder, Role role) {
        promptBuilder.append("你是导师角色：").append(role.getName()).append("\n");
        promptBuilder.append("教学专长：").append(role.getCharacterPrompt()).append("\n\n");
        promptBuilder.append("导师老师对话特点：\n");
        promptBuilder.append("1. 清楚表明身份\"我是").append(role.getName()).append("老师/导师\"\n");
        promptBuilder.append("2. 用启发式提问引导学生思考\n");
        promptBuilder.append("3. 提供清晰、有条理的解释\n");
        promptBuilder.append("4. 鼓励学生主动学习和探索\n");
        promptBuilder.append("5. 保持耐心和鼓励性的教学态度\n");
    }

    /**
     * 构建通用角色prompt
     */
    private void buildGenericRolePrompt(StringBuilder promptBuilder, Role role) {
        promptBuilder.append("你是角色：").append(role.getName()).append("\n");
        if (StringUtils.hasText(role.getCharacterPrompt())) {
            promptBuilder.append("角色设定：").append(role.getCharacterPrompt()).append("\n");
        }
        promptBuilder.append("\n直接承认自己是").append(role.getName()).append("，保持角色特色\n");
    }

    /**
     * 添加RAG知识背景
     * 根据角色ID和用户消息检索相关知识内容
     */
    private void addRagKnowledgeBackground(StringBuilder promptBuilder, Role role, String userMessage) {
        try {
            // 使用角色ID检索相关知识内容，限制返回3个最相关的结果
            String ragContent = ragService.getRoleRelevantContent(role.getId(), userMessage, 3);
            
            if (StringUtils.hasText(ragContent)) {
                logger.debug("为角色 {} 检索到RAG知识内容，长度: {}", role.getName(), ragContent.length());
                promptBuilder.append("\n【角色知识背景】：\n");
                promptBuilder.append(ragContent).append("\n");
            } else {
                logger.debug("角色 {} 未找到相关RAG知识内容", role.getName());
            }
        } catch (Exception e) {
            // RAG检索失败不应影响正常对话流程
            logger.warn("为角色 {} 检索RAG知识时发生错误: {}", role.getName(), e.getMessage(), e);
        }
    }

    /**
     * 添加通用行为准则
     */
    private void addCommonBehaviorGuidelines(StringBuilder promptBuilder) {
        promptBuilder.append("\n【重要对话规则】：\n");
        promptBuilder.append("1. 身份表达：被问及身份时直接说\"我是[角色名]\"，不要回避或哲学化\n");
        promptBuilder.append("2. 简洁回复：所有回复都要简洁明了，一般控制在1-2句话以内\n");
        promptBuilder.append("3. 语言风格：像正常人一样对话，简洁自然，不要文绉绉或过于戏剧化\n");
        promptBuilder.append("4. 内容相关：直接回答用户的问题，不要答非所问\n");
        promptBuilder.append("5. 避免描述：不要用*动作*、心理描写、环境渲染等格式\n");
        promptBuilder.append("6. 保持专注：围绕角色特点回答，但用正常的日常语言\n");
        promptBuilder.append("7. 态度自然：不卑不亢，像朋友一样交流\n");
        promptBuilder.append("8. 如果被问到公式、原理、推导、原理细节等问题，请用 markdown 详细分步展示数学/逻辑推导，并分段解释原理，不受简洁回复限制。遇到此类问题时，务必系统、完整、详细地讲解，直到用户主动打断为止。\n");
        promptBuilder.append("9. 如果用户直接要求代码示例，请直接输出完整代码（用 markdown 代码块），无需追问或解释，除非用户主动要求说明。\n");
    }

    /**
     * 角色类型枚举
     */
    private enum RoleType {
        HISTORICAL_FIGURE,    // 历史人物
        FICTIONAL_CHARACTER,  // 虚拟角色
        PROFESSIONAL_EXPERT,  // 专业专家
        COMPANION_FRIEND,     // 陪伴朋友
        TEACHER_MENTOR,       // 导师老师
        GENERIC              // 通用角色
    }
}