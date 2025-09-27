package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.dto.ChatRoomVO;
import com.aichat.roleplay.mapper.ChatRoomMapper;
import com.aichat.roleplay.mapper.ChatRoomRoleMapper;
import com.aichat.roleplay.mapper.RoleMapper;
import com.aichat.roleplay.model.ChatRoom;
import com.aichat.roleplay.model.ChatRoomRole;
import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.service.IChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天室服务实现类
 */
@Service
public class ChatRoomServiceImpl implements IChatRoomService {

    @Autowired
    private ChatRoomMapper chatRoomMapper;

    @Autowired
    private ChatRoomRoleMapper chatRoomRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    @Transactional
    public ChatRoomVO createChatRoom(Long userId, String name, String description, List<Long> roleIds) {
        // 为每个角色创建聊天室记录
        Long chatRoomId = System.currentTimeMillis(); // 使用时间戳作为聊天室ID
        
        if (roleIds != null && !roleIds.isEmpty()) {
            for (int i = 0; i < roleIds.size(); i++) {
                Long roleId = roleIds.get(i);
                ChatRoom chatRoom = ChatRoom.builder()
                        .userId(userId)
                        .roleId(roleId)
                        .chatRoomId(chatRoomId)
                        .title(name)
                        .description(description)
                        .joinOrder(i + 1)
                        .isActive(true)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .deleted(0)
                        .build();
                
                chatRoomMapper.insert(chatRoom);
            }
        }

        return getChatRoomById(chatRoomId);
    }

    @Override
    public ChatRoomVO getChatRoomById(Long chatRoomId) {
        // 查询该聊天室的所有记录
        List<ChatRoom> chatRooms = chatRoomMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ChatRoom>()
                .eq("chat_room_id", chatRoomId)
                .eq("deleted", 0)
        );
        
        if (chatRooms.isEmpty()) {
            return null;
        }

        // 获取第一条记录作为基础信息
        ChatRoom firstChatRoom = chatRooms.get(0);
        
        // 获取所有角色
        List<Long> roleIds = chatRooms.stream()
                .map(ChatRoom::getRoleId)
                .collect(Collectors.toList());
        
        List<Role> roles = roleIds.stream()
                .map(roleMapper::selectById)
                .filter(role -> role != null && role.getDeleted() == 0)
                .collect(Collectors.toList());

        return ChatRoomVO.builder()
                .id(chatRoomId)
                .name(firstChatRoom.getTitle())
                .description(firstChatRoom.getDescription())
                .createTime(firstChatRoom.getCreateTime())
                .updateTime(firstChatRoom.getUpdateTime())
                .roles(roles)
                .roleIds(roleIds)
                .messageCount(0L) // TODO: 实现消息计数
                .lastMessageTime(null) // TODO: 实现最后消息时间
                .build();
    }

    @Override
    public List<ChatRoomVO> getUserChatRooms(Long userId) {
        List<ChatRoom> chatRooms = chatRoomMapper.findByUserId(userId);
        return chatRooms.stream()
                .map(chatRoom -> getChatRoomById(chatRoom.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatRoomVO updateChatRoom(Long chatRoomId, String name, String description) {
        // 更新该聊天室的所有记录
        List<ChatRoom> chatRooms = chatRoomMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ChatRoom>()
                .eq("chat_room_id", chatRoomId)
                .eq("deleted", 0)
        );
        
        if (chatRooms.isEmpty()) {
            return null;
        }

        // 更新所有记录的标题和描述
        for (ChatRoom chatRoom : chatRooms) {
            chatRoom.setTitle(name);
            chatRoom.setDescription(description);
            chatRoom.setUpdateTime(LocalDateTime.now());
            chatRoomMapper.updateById(chatRoom);
        }

        return getChatRoomById(chatRoomId);
    }

    @Override
    @Transactional
    public boolean deleteChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomMapper.selectById(chatRoomId);
        if (chatRoom == null || chatRoom.getDeleted() != 0) {
            return false;
        }

        // 软删除聊天室
        chatRoom.setDeleted(1);
        chatRoom.setUpdateTime(LocalDateTime.now());
        chatRoomMapper.updateById(chatRoom);

        // 删除所有角色关联
        chatRoomRoleMapper.deleteByChatRoomId(chatRoomId);

        return true;
    }

    @Override
    @Transactional
    public boolean addRoleToChatRoom(Long chatRoomId, Long roleId) {
        // 检查聊天室是否存在
        ChatRoom chatRoom = chatRoomMapper.selectById(chatRoomId);
        if (chatRoom == null || chatRoom.getDeleted() != 0) {
            return false;
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() != 0) {
            return false;
        }

        // 检查关联是否已存在
        ChatRoomRole existingRelation = chatRoomRoleMapper.findByChatRoomIdAndRoleId(chatRoomId, roleId);
        if (existingRelation != null) {
            return false; // 已存在
        }

        // 创建新的关联
        ChatRoomRole chatRoomRole = ChatRoomRole.builder()
                .chatRoomId(chatRoomId)
                .roleId(roleId)
                .createTime(LocalDateTime.now())
                .deleted(0)
                .build();
        chatRoomRoleMapper.insert(chatRoomRole);

        return true;
    }

    @Override
    @Transactional
    public boolean removeRoleFromChatRoom(Long chatRoomId, Long roleId) {
        ChatRoomRole chatRoomRole = chatRoomRoleMapper.findByChatRoomIdAndRoleId(chatRoomId, roleId);
        if (chatRoomRole == null) {
            return false;
        }

        // 软删除关联
        chatRoomRole.setDeleted(1);
        chatRoomRoleMapper.updateById(chatRoomRole);

        return true;
    }

    @Override
    public List<Role> getChatRoomRoles(Long chatRoomId) {
        List<Long> roleIds = chatRoomRoleMapper.findRoleIdsByChatRoomId(chatRoomId);
        return roleIds.stream()
                .map(roleMapper::selectById)
                .filter(role -> role != null && role.getDeleted() == 0)
                .collect(Collectors.toList());
    }
}