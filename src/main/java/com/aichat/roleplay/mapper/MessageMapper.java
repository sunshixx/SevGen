package com.aichat.roleplay.mapper;

import com.aichat.roleplay.model.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 消息数据访问层接口
 * 遵循SOLID原则中的接口隔离原则和依赖倒置原则
 * 继承MyBatis-Plus的BaseMapper提供基础CRUD操作
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 查询聊天会话的所有消息
     *
     * @param chatId 聊天会话ID
     * @return 消息列表
     */
    @Select("SELECT * FROM messages WHERE chat_id = #{chatId} AND deleted = 0 ORDER BY sent_at ASC")
    List<Message> findByChatId(@Param("chatId") Long chatId);

    /**
     * 分页查询聊天会话的消息
     *
     * @param chatId       聊天会话ID
     * @param lastMessageId 最后一条消息ID
     * @param pageSize     每页数量
     * @return 消息列表
     */
    @Select("""
    SELECT * FROM messages
    WHERE chat_id = #{chatId}
      <if test="lastMessageId != null">
        AND id < #{lastMessageId}
      </if>
    ORDER BY id DESC
    LIMIT #{pageSize}
""")
    List<Message> findByChatIdPage(@Param("chatId") Long chatId,
                                   @Param("lastMessageId") Long lastMessageId,
                                   @Param("pageSize") int pageSize);

    /**
     * 查询聊天会话的未读消息
     *
     * @param chatId 聊天会话ID
     * @return 未读消息列表
     */
    @Select("SELECT * FROM messages WHERE chat_id = #{chatId} AND is_read = 0 AND deleted = 0 ORDER BY sent_at ASC")
    List<Message> findUnreadByChatId(@Param("chatId") Long chatId);

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     */
    @Update("UPDATE messages SET is_read = 1 WHERE id = #{messageId}")
    void markAsRead(@Param("messageId") Long messageId);

    /**
     * 标记聊天会话的所有消息为已读
     *
     * @param chatId 聊天会话ID
     */
    @Update("UPDATE messages SET is_read = 1 WHERE chat_id = #{chatId} AND is_read = 0")
    void markAllAsReadByChatId(@Param("chatId") Long chatId);

    /**
     * 查询聊天会话中最新的消息
     *
     * @param chatId 聊天会话ID
     * @return 最新消息
     */
    @Select("SELECT * FROM messages WHERE chat_id = #{chatId} AND deleted = 0 ORDER BY sent_at DESC LIMIT 1")
    Message findLatestByChatId(@Param("chatId") Long chatId);

    /**
     * 统计聊天会话的消息数量
     *
     * @param chatId 聊天会话ID
     * @return 消息数量
     */
    @Select("SELECT COUNT(*) FROM messages WHERE chat_id = #{chatId} AND deleted = 0")
    Long countByChatId(@Param("chatId") Long chatId);
}