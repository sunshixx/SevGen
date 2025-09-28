package com.aichat.roleplay.mapper;

import com.aichat.roleplay.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 用户数据访问层接口
 * 遵循SOLID原则中的接口隔离原则和依赖倒置原则
 * 继承MyBatis-Plus的BaseMapper提供基础CRUD操作
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND deleted = 0")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = 0")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username} AND deleted = 0")
    boolean existsByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email} AND deleted = 0")
    boolean existsByEmail(@Param("email") String email);

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return 更新影响的行数
     */
    @org.apache.ibatis.annotations.Update("UPDATE users SET avatar = #{avatarUrl}, updated_at = CURRENT_TIMESTAMP WHERE id = #{userId} AND deleted = 0")
    int updateUserAvatar(@Param("userId") Long userId, @Param("avatarUrl") String avatarUrl);
}