package com.aichat.roleplay.mapper;

import com.aichat.roleplay.model.ChatRoomRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天室角色关联数据访问层接口
 */
@Mapper
public interface ChatRoomRoleMapper extends BaseMapper<ChatRoomRole> {

    /**
     * 根据聊天室ID和角色ID查找关联记录
     *
     * @param chatRoomId 聊天室ID
     * @param roleId     角色ID
     * @return 关联记录
     */
    @Select("SELECT * FROM chat_room_roles WHERE chat_room_id = #{chatRoomId} AND role_id = #{roleId} AND deleted = 0")
    ChatRoomRole findByChatRoomIdAndRoleId(@Param("chatRoomId") Long chatRoomId, @Param("roleId") Long roleId);

    /**
     * 根据聊天室ID查找所有角色ID
     *
     * @param chatRoomId 聊天室ID
     * @return 角色ID列表
     */
    @Select("SELECT role_id FROM chat_room_roles WHERE chat_room_id = #{chatRoomId} AND deleted = 0")
    List<Long> findRoleIdsByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * 删除聊天室的所有角色关联
     *
     * @param chatRoomId 聊天室ID
     */
    @Delete("UPDATE chat_room_roles SET deleted = 1 WHERE chat_room_id = #{chatRoomId}")
    void deleteByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}