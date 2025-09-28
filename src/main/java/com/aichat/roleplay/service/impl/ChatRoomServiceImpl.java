package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.dto.ChatRoomVO;
import com.aichat.roleplay.mapper.ChatRoomMapper;
import com.aichat.roleplay.mapper.ChatroomMessageMapper;
import com.aichat.roleplay.model.ChatRoom;
import com.aichat.roleplay.model.ChatroomMessage;
import com.aichat.roleplay.service.IChatRoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天室服务实现类
 * 实现聊天室相关的业务逻辑
 */
@Slf4j
@Service
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomMapper, ChatRoom> implements IChatRoomService {

    @Autowired
    private ChatRoomMapper chatRoomMapper;

    @Autowired
    private ChatroomMessageMapper chatroomMessageMapper;

    @Override
    @Transactional
    public ChatRoom createChatRoom(ChatRoom chatRoom) {
        log.info("创建新聊天室: {}", chatRoom);
        // 设置默认值
        if (chatRoom.getTitle() == null || chatRoom.getTitle().trim().isEmpty()) {
            chatRoom.setTitle("新的聊天室");
        }
        if (chatRoom.getIsActive() == null) {
            chatRoom.setIsActive(true);
        }
        if (chatRoom.getJoinOrder() == null) {
            // 获取当前聊天室的最大加入顺序
            Integer maxOrder = chatRoomMapper.getMaxJoinOrder(chatRoom.getChatRoomId());
            chatRoom.setJoinOrder(maxOrder + 1);
        }
        
        // 保存聊天室
        boolean success = save(chatRoom);
        if (success) {
            log.info("聊天室创建成功，ID: {}", chatRoom.getId());
            return chatRoom;
        } else {
            log.error("聊天室创建失败");
            throw new RuntimeException("聊天室创建失败");
        }
    }

    @Override
    @Transactional
    public ChatRoom addRoleToChatRoom(Long chatRoomId, Long userId, Long roleId) {
        log.info("向聊天室 {} 添加角色 {}", chatRoomId, roleId);
        
        // 检查是否已存在相同的聊天室-角色组合
        ChatRoom existing = chatRoomMapper.findByChatRoomIdAndRoleId(chatRoomId, roleId);
        if (existing != null) {
            log.warn("聊天室 {} 中已存在角色 {}", chatRoomId, roleId);
            return existing;
        }
        
        // 获取加入顺序
        Integer maxOrder = chatRoomMapper.getMaxJoinOrder(chatRoomId);
        
        // 创建新的聊天室角色记录
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomId(chatRoomId)
                .userId(userId)
                .roleId(roleId)
                .title("新的聊天室")
                .isActive(true)
                .joinOrder(maxOrder + 1)
                .deleted(0)
                .build();
        
        boolean success = save(chatRoom);
        if (success) {
            log.info("角色添加成功，记录ID: {}", chatRoom.getId());
            return chatRoom;
        } else {
            log.error("角色添加失败");
            throw new RuntimeException("角色添加失败");
        }
    }

    @Override
    public List<ChatRoom> getRolesByChatRoomId(Long chatRoomId) {
        log.debug("查询聊天室 {} 的所有角色", chatRoomId);
        return chatRoomMapper.findByChatRoomId(chatRoomId);
    }

    @Override
    public ChatRoom getByChatRoomId(Long chatRoomId) {
        log.debug("根据聊天室ID {} 获取聊天室信息", chatRoomId);
        List<ChatRoom> chatRooms = chatRoomMapper.findByChatRoomId(chatRoomId);
        if (chatRooms.isEmpty()) {
            log.warn("聊天室 {} 不存在", chatRoomId);
            return null;
        }
        // 返回第一条记录作为聊天室基本信息
        return chatRooms.get(0);
    }

    @Override
    public List<ChatRoom> getChatRoomsByUserId(Long userId) {
        log.debug("查询用户 {} 的所有聊天室", userId);
        return chatRoomMapper.findByUserId(userId);
    }

    @Override
    public List<ChatRoom> getActiveChatRoomsByUserId(Long userId) {
        log.debug("查询用户 {} 的活跃聊天室", userId);
        return chatRoomMapper.findActiveByUserId(userId);
    }

    @Override
    public List<ChatRoom> getChatRoomsByRoleId(Long roleId) {
        log.debug("查询角色 {} 相关的聊天室", roleId);
        return chatRoomMapper.findByRoleId(roleId);
    }

    @Override
    @Transactional
    public ChatRoom updateChatRoom(ChatRoom chatRoom) {
        log.info("更新聊天室信息: {}", chatRoom);
        
        boolean success = updateById(chatRoom);
        if (success) {
            log.info("聊天室更新成功，ID: {}", chatRoom.getId());
            return chatRoom;
        } else {
            log.error("聊天室更新失败");
            throw new RuntimeException("聊天室更新失败");
        }
    }

    @Override
    @Transactional
    public boolean updateRoleActiveStatus(Long id, Boolean isActive) {
        log.info("更新记录 {} 的激活状态为: {}", id, isActive);
        
        try {
            chatRoomMapper.updateActiveStatus(id, isActive);
            log.info("激活状态更新成功");
            return true;
        } catch (Exception e) {
            log.error("激活状态更新失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteChatRoomRole(Long id) {
        log.info("删除聊天室角色记录: {}", id);
        
        boolean success = removeById(id);
        if (success) {
            log.info("聊天室角色记录删除成功");
        } else {
            log.error("聊天室角色记录删除失败");
        }
        return success;
    }

    @Override
    @Transactional
    public boolean deleteChatRoom(Long chatRoomId, Long userId) {
        log.info("物理删除聊天室: {}, 用户ID: {}", chatRoomId, userId);
        
        try {
            // 1. 权限验证：检查用户是否有权限删除该聊天室
            if (!hasPermission(chatRoomId, userId)) {
                log.error("用户 {} 没有权限删除聊天室 {}", userId, chatRoomId);
                return false;
            }
            
            // 2. 物理删除聊天室相关的所有消息
            List<ChatroomMessage> messages = chatroomMessageMapper.findByChatRoomId(chatRoomId);
            if (!messages.isEmpty()) {
                // 物理删除消息记录
                for (ChatroomMessage message : messages) {
                    chatroomMessageMapper.deleteById(message.getId());
                }
                log.info("物理删除聊天室 {} 的 {} 条消息", chatRoomId, messages.size());
            }
            
            // 3. 物理删除聊天室中的所有角色记录
            List<ChatRoom> chatRoomRoles = chatRoomMapper.findByChatRoomId(chatRoomId);
            if (!chatRoomRoles.isEmpty()) {
                // 物理删除聊天室角色记录
                for (ChatRoom chatRoomRole : chatRoomRoles) {
                    chatRoomMapper.deleteById(chatRoomRole.getId());
                }
                log.info("物理删除聊天室 {} 的 {} 个角色记录", chatRoomId, chatRoomRoles.size());
            }
            
            log.info("聊天室 {} 物理删除成功", chatRoomId);
            return true;
            
        } catch (Exception e) {
            log.error("物理删除聊天室失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public ChatRoom getChatRoomByRoomIdAndRoleId(Long chatRoomId, Long roleId) {
        log.debug("查询聊天室 {} 中的角色 {}", chatRoomId, roleId);
        return chatRoomMapper.findByChatRoomIdAndRoleId(chatRoomId, roleId);
    }

    @Override
    public Long countRolesByChatRoomId(Long chatRoomId) {
        log.debug("统计聊天室 {} 的角色数量", chatRoomId);
        return chatRoomMapper.countByChatRoomId(chatRoomId);
    }

    @Override
    public boolean hasPermission(Long chatRoomId, Long userId) {
        log.debug("检查用户 {} 是否有权限访问聊天室 {}", userId, chatRoomId);
        List<ChatRoom> userChatRooms = chatRoomMapper.findByUserId(userId);
        return userChatRooms.stream()
                .anyMatch(chatRoom -> chatRoom.getChatRoomId().equals(chatRoomId));
    }

    @Override
    public List<ChatRoomVO> getChatRoomList() {
        log.debug("获取聊天室列表（包含参与人数和活跃状态）");
        
        // 获取所有聊天室（去重）
        List<ChatRoom> allChatRooms = list();
        Map<Long, ChatRoom> uniqueChatRooms = allChatRooms.stream()
                .collect(Collectors.toMap(
                    ChatRoom::getChatRoomId,
                    chatRoom -> chatRoom,
                    (existing, replacement) -> existing
                ));
        
        // 计算每个聊天室的参与人数
        Map<Long, Long> participantCountMap = new HashMap<>();
        for (Long chatRoomId : uniqueChatRooms.keySet()) {
            Long count = chatRoomMapper.countByChatRoomId(chatRoomId);
            participantCountMap.put(chatRoomId, count);
        }
        
        // 计算每个聊天室的活跃状态（基于最后一次消息时间）
        Map<Long, Boolean> activeStatusMap = new HashMap<>();
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        
        for (Long chatRoomId : uniqueChatRooms.keySet()) {
            ChatroomMessage latestMessage = chatroomMessageMapper.findLatestByChatRoomId(chatRoomId);
            boolean isActive = false;
            if (latestMessage != null && latestMessage.getSentAt() != null) {
                isActive = latestMessage.getSentAt().isAfter(thirtyMinutesAgo);
            }
            activeStatusMap.put(chatRoomId, isActive);
        }
        
        // 转换为VO对象
        List<ChatRoom> chatRoomList = uniqueChatRooms.values().stream()
                .collect(Collectors.toList());
        
        return ChatRoomVO.po2voList(chatRoomList, participantCountMap, activeStatusMap);
    }
}