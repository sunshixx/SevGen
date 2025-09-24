package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.dto.ChatSessionVO;
import com.aichat.roleplay.mapper.ChatMapper;
import com.aichat.roleplay.mapper.RoleMapper;
import com.aichat.roleplay.model.Chat;
import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.service.IChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 聊天会话服务实现类
 * 遵循SOLID原则中的单一职责原则和依赖倒置原则
 * 使用模板方法模式处理聊天会话的生命周期管理
 */
@Service
@Transactional
public class ChatServiceImpl implements IChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatMapper chatMapper;
    private final RoleMapper roleMapper;

    /**
     * 构造函数注入，遵循依赖倒置原则
     *
     * @param chatMapper 聊天会话数据访问接口
     * @param roleMapper 角色数据访问接口
     */
    @Autowired
    public ChatServiceImpl(ChatMapper chatMapper, RoleMapper roleMapper) {
        this.chatMapper = chatMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public Chat createChat(Long userId, Long roleId) {
        log.info("创建聊天会话，用户ID: {}, 角色ID: {}", userId, roleId);

        // 验证角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 创建聊天会话
        Chat chat = Chat.builder()
                .userId(userId)
                .roleId(roleId)
                .title("与" + role.getName() + "的对话")
                .isActive(true)
                .deleted(0)
                .build();

        int result = chatMapper.insert(chat);
        if (result > 0) {
            log.info("聊天会话创建成功，会话ID: {}", chat.getId());
            return chat;
        } else {
            throw new RuntimeException("聊天会话创建失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chat> getUserChats(Long userId) {
        log.debug("获取用户所有聊天会话，用户ID: {}", userId);
        return chatMapper.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chat> getUserActiveChats(Long userId) {
        log.debug("获取用户活跃聊天会话，用户ID: {}", userId);
        return chatMapper.findActiveByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Chat> getChatById(Long id) {
        log.debug("根据ID获取聊天会话: {}", id);
        Chat chat = chatMapper.selectById(id);
        return Optional.ofNullable(chat);
    }

    @Override
    public Chat updateChat(Chat chat) {
        log.info("更新聊天会话，会话ID: {}", chat.getId());

        // 检查会话是否存在
        if (chatMapper.selectById(chat.getId()) == null) {
            throw new RuntimeException("聊天会话不存在");
        }

        int result = chatMapper.updateById(chat);
        if (result > 0) {
            log.info("聊天会话更新成功，会话ID: {}", chat.getId());
            return chat;
        } else {
            throw new RuntimeException("聊天会话更新失败");
        }
    }

    @Override
    public void deactivateChat(Long id) {
        log.info("停用聊天会话，会话ID: {}", id);

        Chat chat = chatMapper.selectById(id);
        if (chat == null) {
            throw new RuntimeException("聊天会话不存在");
        }

        chat.setIsActive(false);
        int result = chatMapper.updateById(chat);
        if (result > 0) {
            log.info("聊天会话停用成功，会话ID: {}", id);
        } else {
            throw new RuntimeException("聊天会话停用失败");
        }
    }

    @Override
    public void deleteChat(Long id) {
        log.info("删除聊天会话，会话ID: {}", id);

        // 使用逻辑删除
        int result = chatMapper.deleteById(id);
        if (result > 0) {
            log.info("聊天会话删除成功，会话ID: {}", id);
        } else {
            throw new RuntimeException("聊天会话删除失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chat> getUserChatsByRole(Long userId, Long roleId) {
        log.debug("获取用户与特定角色的聊天会话，用户ID: {}, 角色ID: {}", userId, roleId);
        return chatMapper.findByUserIdAndRoleId(userId, roleId);
    }
//    @Override
//    @Transactional(readOnly = true)
//    public ChatSessionVO create(String query){
//
//
//    }
}