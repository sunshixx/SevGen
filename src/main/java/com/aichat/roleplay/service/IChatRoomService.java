package com.aichat.roleplay.service;

import com.aichat.roleplay.dto.ChatRoomVO;
import com.aichat.roleplay.model.Role;

import java.util.List;

/**
 * 聊天室服务接口
 * 专门处理多agent聊天室业务逻辑
 */
public interface IChatRoomService {

    /**
     * 创建聊天室
     *
     * @param userId      用户ID
     * @param name        聊天室名称
     * @param description 聊天室描述
     * @param roleIds     角色ID列表
     * @return 聊天室VO
     */
    ChatRoomVO createChatRoom(Long userId, String name, String description, List<Long> roleIds);

    /**
     * 获取用户的聊天室列表
     *
     * @param userId 用户ID
     * @return 聊天室列表
     */
    List<ChatRoomVO> getUserChatRooms(Long userId);

    /**
     * 根据聊天室ID获取聊天室信息
     *
     * @param chatRoomId 聊天室ID
     * @return 聊天室信息
     */
    ChatRoomVO getChatRoomById(Long chatRoomId);

    /**
     * 更新聊天室信息
     *
     * @param chatRoomId  聊天室ID
     * @param name        聊天室名称
     * @param description 聊天室描述
     * @return 更新后的聊天室VO
     */
    ChatRoomVO updateChatRoom(Long chatRoomId, String name, String description);

    /**
     * 删除聊天室
     *
     * @param chatRoomId 聊天室ID
     * @return 是否删除成功
     */
    boolean deleteChatRoom(Long chatRoomId);

    /**
     * 向聊天室添加角色
     *
     * @param chatRoomId 聊天室ID
     * @param roleId     角色ID
     * @return 是否添加成功
     */
    boolean addRoleToChatRoom(Long chatRoomId, Long roleId);

    /**
     * 从聊天室移除角色
     *
     * @param chatRoomId 聊天室ID
     * @param roleId     角色ID
     * @return 是否移除成功
     */
    boolean removeRoleFromChatRoom(Long chatRoomId, Long roleId);

    /**
     * 获取聊天室中的角色列表
     *
     * @param chatRoomId 聊天室ID
     * @return 角色列表
     */
    List<Role> getChatRoomRoles(Long chatRoomId);
}