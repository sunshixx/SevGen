package com.aichat.roleplay.mapper;

import com.aichat.roleplay.model.ChatroomMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天室消息数据访问层接口
 * 继承MyBatis-Plus的BaseMapper提供基础CRUD操作
 */
@Mapper
public interface ChatroomMessageMapper extends BaseMapper<ChatroomMessage> {

    /**
     * 查询聊天室的所有消息
     *
     * @param chatRoomId 聊天室ID
     * @return 消息列表
     */
    @Select("SELECT * FROM chatroom_messages WHERE chat_room_id = #{chatRoomId} AND deleted = 0 ORDER BY sent_at ASC")
    List<ChatroomMessage> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * 分页查询聊天室的消息
     *
     * @param chatRoomId    聊天室ID
     * @param lastMessageId 最后一条消息ID（用于游标分页）
     * @param pageSize      每页数量
     * @return 消息列表
     */
    @Select({
        "<script>",
        "SELECT * FROM chatroom_messages",
        "WHERE chat_room_id = #{chatRoomId}",
        "AND deleted = 0",
        "<if test='lastMessageId != null'>",
        "AND id &lt; #{lastMessageId}",
        "</if>",
        "ORDER BY sent_at DESC",
        "LIMIT #{pageSize}",
        "</script>"
    })
    List<ChatroomMessage> findByChatRoomIdPage(@Param("chatRoomId") Long chatRoomId,
                                               @Param("lastMessageId") Long lastMessageId,
                                               @Param("pageSize") int pageSize);

    /**
     * 查询聊天室的未读消息
     *
     * @param chatRoomId 聊天室ID
     * @param userId     用户ID
     * @return 未读消息列表
     */
    @Select("SELECT * FROM chatroom_messages WHERE chat_room_id = #{chatRoomId} AND is_read = 0 AND deleted = 0 AND (sender_type = 'ai' OR user_id != #{userId}) ORDER BY sent_at ASC")
    List<ChatroomMessage> findUnreadByChatRoomId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);

    /**
     * 标记单条消息为已读
     *
     * @param messageId 消息ID
     */
    @Update("UPDATE chatroom_messages SET is_read = 1 WHERE id = #{messageId}")
    void markAsRead(@Param("messageId") Long messageId);

    /**
     * 标记聊天室所有消息为已读（针对特定用户）
     *
     * @param chatRoomId 聊天室ID
     * @param userId     用户ID
     */
    @Update("UPDATE chatroom_messages SET is_read = 1 WHERE chat_room_id = #{chatRoomId} AND is_read = 0 AND (sender_type = 'ai' OR user_id != #{userId})")
    void markAllAsReadByChatRoomId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);

    /**
     * 获取聊天室最新消息
     *
     * @param chatRoomId 聊天室ID
     * @return 最新消息
     */
    @Select("SELECT * FROM chatroom_messages WHERE chat_room_id = #{chatRoomId} AND deleted = 0 ORDER BY sent_at DESC LIMIT 1")
    ChatroomMessage findLatestByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * 统计聊天室消息数量
     *
     * @param chatRoomId 聊天室ID
     * @return 消息数量
     */
    @Select("SELECT COUNT(*) FROM chatroom_messages WHERE chat_room_id = #{chatRoomId} AND deleted = 0")
    Long countByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * 查询特定角色在聊天室的消息
     *
     * @param chatRoomId 聊天室ID
     * @param roleId     角色ID
     * @return 消息列表
     */
    @Select("SELECT * FROM chatroom_messages WHERE chat_room_id = #{chatRoomId} AND role_id = #{roleId} AND deleted = 0 ORDER BY sent_at ASC")
    List<ChatroomMessage> findByChatRoomIdAndRoleId(@Param("chatRoomId") Long chatRoomId, @Param("roleId") Long roleId);

    /**
     * 查询特定用户在聊天室的消息
     *
     * @param chatRoomId 聊天室ID
     * @param userId     用户ID
     * @return 消息列表
     */
    @Select("SELECT * FROM chatroom_messages WHERE chat_room_id = #{chatRoomId} AND user_id = #{userId} AND deleted = 0 ORDER BY sent_at ASC")
    List<ChatroomMessage> findByChatRoomIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);

    /**
     * 获取聊天室最近N条消息（用于构建聊天历史）
     *
     * @param chatRoomId 聊天室ID
     * @param limit      消息数量限制
     * @return 消息列表
     */
    @Select("SELECT * FROM chatroom_messages WHERE chat_room_id = #{chatRoomId} AND deleted = 0 ORDER BY sent_at DESC LIMIT #{limit}")
    List<ChatroomMessage> findRecentByChatRoomId(@Param("chatRoomId") Long chatRoomId, @Param("limit") int limit);
}