package com.aichat.roleplay.mapper;

import com.aichat.roleplay.model.Chat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话数据访问层接口
 * 遵循SOLID原则中的接口隔离原则和依赖倒置原则
 * 继承MyBatis-Plus的BaseMapper提供基础CRUD操作
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
}