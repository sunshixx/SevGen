package com.aichat.roleplay.mapper;

import com.aichat.roleplay.model.Chat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话数据访问层接口
 * 遵循SOLID原则中的接口隔离原则和依赖倒置原则
 * 继承MyBatis-Plus的BaseMapper提供基础CRUD操作
 * 支持多角色聊天室概念
 */
@Mapper
public interface ChatMapper extends BaseMapper<Chat> {

    /**
     * 查询用户的所有聊天会话
     *
     * @param userId 用户ID
     * @return 聊天会话列表
     */
    @Select("SELECT * FROM chats WHERE user_id = #{userId} AND deleted = 0 ORDER BY updated_at DESC")
    List<Chat> findByUserId(@Param("userId") Long userId);

    /**
     * 分页查询用户所有聊天会话
     *
     * @param userId       用户ID
     * @param lastUpdatedAt 最后更新时间
     * @param pageSize     每页大小
     * @return 聊天会话列表
     */
    @Select({
        "<script>",
        "SELECT * FROM chats",
        "WHERE user_id = #{userId}",
        "AND deleted = 0",
        "<if test='lastUpdatedAt != null'>",
        "AND updated_at &lt; #{lastUpdatedAt}",
        "</if>",
        "ORDER BY updated_at DESC",
        "LIMIT #{pageSize}",
        "</script>"
    })
    List<Chat> findByUserIdPage(@Param("userId") Long userId,
                                @Param("lastUpdatedAt") LocalDateTime lastUpdatedAt,
                                @Param("pageSize") int pageSize);

    /**
     * 查询用户与特定角色的聊天会话
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 聊天会话列表
     */
    @Select("SELECT * FROM chats WHERE user_id = #{userId} AND role_id = #{roleId} AND deleted = 0 ORDER BY updated_at DESC")
    List<Chat> findByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 查询用户的活跃聊天会话
     *
     * @param userId 用户ID
     * @return 活跃聊天会话列表
     */
    @Select("SELECT * FROM chats WHERE user_id = #{userId} AND is_active = 1 AND deleted = 0 ORDER BY updated_at DESC")
    List<Chat> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询所有相关聊天会话
     *
     * @param roleId 角色ID
     * @return 聊天会话列表
     */
    @Select("SELECT * FROM chats WHERE role_id = #{roleId} AND deleted = 0 ORDER BY updated_at DESC")
    List<Chat> findByRoleId(@Param("roleId") Long roleId);

    // ========== 聊天室相关查询方法 ==========

    /**
     * 根据聊天室ID查询所有角色
     *
     * @param chatRoomId 聊天室ID
     * @return 聊天记录列表
     */
    @Select("SELECT * FROM chats WHERE chat_room_id = #{chatRoomId} AND deleted = 0 ORDER BY created_at ASC")
    List<Chat> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * 查询用户的所有聊天室ID
     *
     * @param userId 用户ID
     * @return 聊天室ID列表
     */
    @Select("SELECT DISTINCT chat_room_id FROM chats WHERE user_id = #{userId} AND deleted = 0 ORDER BY chat_room_id DESC")
    List<Long> findChatRoomIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据聊天室ID和角色ID查询特定记录
     *
     * @param chatRoomId 聊天室ID
     * @param roleId 角色ID
     * @return 聊天记录
     */
    @Select("SELECT * FROM chats WHERE chat_room_id = #{chatRoomId} AND role_id = #{roleId} AND deleted = 0 LIMIT 1")
    Chat findByChatRoomIdAndRoleId(@Param("chatRoomId") Long chatRoomId, @Param("roleId") Long roleId);

    /**
     * 获取聊天室的基本信息（取第一条记录）
     *
     * @param chatRoomId 聊天室ID
     * @return 聊天记录
     */
    @Select("SELECT * FROM chats WHERE chat_room_id = #{chatRoomId} AND deleted = 0 ORDER BY created_at ASC LIMIT 1")
    Chat findChatRoomInfo(@Param("chatRoomId") Long chatRoomId);

    /**
     * 删除聊天室中的所有记录（逻辑删除）
     *
     * @param chatRoomId 聊天室ID
     */
    @Delete("UPDATE chats SET deleted = 1 WHERE chat_room_id = #{chatRoomId}")
    void deleteByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * 从聊天室移除特定角色（逻辑删除）
     *
     * @param chatRoomId 聊天室ID
     * @param roleId 角色ID
     */
    @Delete("UPDATE chats SET deleted = 1 WHERE chat_room_id = #{chatRoomId} AND role_id = #{roleId}")
    void deleteRoleFromRoom(@Param("chatRoomId") Long chatRoomId, @Param("roleId") Long roleId);
}