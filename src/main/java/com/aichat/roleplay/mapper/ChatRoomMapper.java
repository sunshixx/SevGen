package com.aichat.roleplay.mapper;

import com.aichat.roleplay.model.ChatRoom;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天室数据访问层接口
 * 提供聊天室相关的数据库操作
 */
@Mapper
public interface ChatRoomMapper extends BaseMapper<ChatRoom> {

    /**
     * 根据聊天室ID查询所有角色
     *
     * @param chatRoomId 聊天室ID
     * @return 聊天室角色列表
     */
    @Select("SELECT * FROM chatroom WHERE chat_room_id = #{chatRoomId} AND deleted = 0 ORDER BY join_order ASC")
    List<ChatRoom> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * 根据用户ID查询所有聊天室
     *
     * @param userId 用户ID
     * @return 聊天室列表
     */
    @Select("SELECT * FROM chatroom WHERE user_id = #{userId} AND deleted = 0 ORDER BY updated_at DESC")
    List<ChatRoom> findByUserId(@Param("userId") Long userId);

    /**
     * 根据聊天室ID和角色ID查询记录
     *
     * @param chatRoomId 聊天室ID
     * @param roleId     角色ID
     * @return 聊天室角色记录
     */
    @Select("SELECT * FROM chatroom WHERE chat_room_id = #{chatRoomId} AND role_id = #{roleId} AND deleted = 0")
    ChatRoom findByChatRoomIdAndRoleId(@Param("chatRoomId") Long chatRoomId, @Param("roleId") Long roleId);

    /**
     * 查询用户的活跃聊天室
     *
     * @param userId 用户ID
     * @return 活跃聊天室列表
     */
    @Select("SELECT * FROM chatroom WHERE user_id = #{userId} AND is_active = 1 AND deleted = 0 ORDER BY updated_at DESC")
    List<ChatRoom> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询所有相关聊天室
     *
     * @param roleId 角色ID
     * @return 聊天室列表
     */
    @Select("SELECT * FROM chatroom WHERE role_id = #{roleId} AND deleted = 0 ORDER BY updated_at DESC")
    List<ChatRoom> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 更新角色在聊天室中的激活状态
     *
     * @param id       记录ID
     * @param isActive 是否激活
     */
    @Update("UPDATE chatroom SET is_active = #{isActive} WHERE id = #{id}")
    void updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    /**
     * 获取聊天室中角色的最大加入顺序
     *
     * @param chatRoomId 聊天室ID
     * @return 最大加入顺序
     */
    @Select("SELECT COALESCE(MAX(join_order), 0) FROM chatroom WHERE chat_room_id = #{chatRoomId} AND deleted = 0")
    Integer getMaxJoinOrder(@Param("chatRoomId") Long chatRoomId);

    /**
     * 统计聊天室中的角色数量
     *
     * @param chatRoomId 聊天室ID
     * @return 角色数量
     */
    @Select("SELECT COUNT(*) FROM chatroom WHERE chat_room_id = #{chatRoomId} AND deleted = 0")
    Long countByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}