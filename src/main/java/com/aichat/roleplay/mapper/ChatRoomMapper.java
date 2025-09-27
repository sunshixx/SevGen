package com.aichat.roleplay.mapper;

import com.aichat.roleplay.model.ChatRoom;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天室数据访问层接口
 */
@Mapper
public interface ChatRoomMapper extends BaseMapper<ChatRoom> {

    /**
     * 查询用户的所有聊天室
     *
     * @param userId 用户ID
     * @return 聊天室列表
     */
    @Select("SELECT * FROM chatroom WHERE user_id = #{userId} AND deleted = 0 ORDER BY updated_at DESC")
    List<ChatRoom> findByUserId(@Param("userId") Long userId);
}