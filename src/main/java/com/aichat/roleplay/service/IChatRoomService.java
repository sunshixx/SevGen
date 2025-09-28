package com.aichat.roleplay.service;

import com.aichat.roleplay.model.ChatRoom;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface IChatRoomService extends IService<ChatRoom> {

    /**
     * 创建新的聊天室
     *
     * @param chatRoom 聊天室信息
     * @return 创建的聊天室
     */
    ChatRoom createChatRoom(ChatRoom chatRoom);

    /**
     * 向聊天室添加角色
     *
     * @param chatRoomId 聊天室ID
     * @param userId     用户ID
     * @param roleId     角色ID
     * @return 添加的聊天室角色记录
     */
    ChatRoom addRoleToChatRoom(Long chatRoomId, Long userId, Long roleId);

    /**
     * 根据聊天室ID获取所有角色
     *
     * @param chatRoomId 聊天室ID
     * @return 聊天室角色列表
     */
    List<ChatRoom> getRolesByChatRoomId(Long chatRoomId);

    /**
     * 根据用户ID获取所有聊天室
     *
     * @param userId 用户ID
     * @return 聊天室列表
     */
    List<ChatRoom> getChatRoomsByUserId(Long userId);

    /**
     * 获取用户的活跃聊天室
     *
     * @param userId 用户ID
     * @return 活跃聊天室列表
     */
    List<ChatRoom> getActiveChatRoomsByUserId(Long userId);

    /**
     * 根据角色ID获取相关聊天室
     *
     * @param roleId 角色ID
     * @return 聊天室列表
     */
    List<ChatRoom> getChatRoomsByRoleId(Long roleId);

    /**
     * 更新聊天室信息
     *
     * @param chatRoom 聊天室信息
     * @return 更新后的聊天室
     */
    ChatRoom updateChatRoom(ChatRoom chatRoom);

    /**
     * 根据聊天室ID获取聊天室信息（获取第一条记录）
     *
     * @param chatRoomId 聊天室ID
     * @return 聊天室信息
     */
    ChatRoom getByChatRoomId(Long chatRoomId);

    /**
     * 更新角色在聊天室中的激活状态
     *
     * @param id       记录ID
     * @param isActive 是否激活
     * @return 是否更新成功
     */
    boolean updateRoleActiveStatus(Long id, Boolean isActive);

    /**
     * 删除聊天室角色记录
     *
     * @param id 记录ID
     * @return 是否删除成功
     */
    boolean deleteChatRoomRole(Long id);

    /**
     * 根据聊天室ID和角色ID查询记录
     *
     * @param chatRoomId 聊天室ID
     * @param roleId     角色ID
     * @return 聊天室角色记录
     */
    ChatRoom getChatRoomByRoomIdAndRoleId(Long chatRoomId, Long roleId);

    /**
     * 统计聊天室中的角色数量
     *
     * @param chatRoomId 聊天室ID
     * @return 角色数量
     */
    Long countRolesByChatRoomId(Long chatRoomId);

    /**
     * 检查用户是否有权限访问聊天室
     *
     * @param chatRoomId 聊天室ID
     * @param userId     用户ID
     * @return 是否有权限
     */
    boolean hasPermission(Long chatRoomId, Long userId);

    /**
     * 获取聊天室列表（包含参与人数和活跃状态）
     *
     * @return 聊天室列表
     */
    List<com.aichat.roleplay.dto.ChatRoomVO> getChatRoomList();
}