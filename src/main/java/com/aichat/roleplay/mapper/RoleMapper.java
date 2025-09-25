package com.aichat.roleplay.mapper;

import com.aichat.roleplay.model.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色数据访问层接口
 * 遵循SOLID原则中的接口隔离原则和依赖倒置原则
 * 继承MyBatis-Plus的BaseMapper提供基础CRUD操作
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 查询所有公开角色
     *
     * @return 公开角色列表
     */
    @Select("SELECT * FROM roles WHERE is_public = 1 AND deleted = 0 ORDER BY created_at DESC")
    List<Role> findPublicRoles();

    /**
     * 根据分类查询角色
     *
     * @param category 角色分类
     * @return 角色列表
     */
    @Select("SELECT * FROM roles WHERE category = #{category} AND deleted = 0 ORDER BY created_at DESC")
    List<Role> findByCategory(@Param("category") String category);

    /**
     * 根据名称模糊查询角色
     *
     * @param name 角色名称关键词
     * @return 角色列表
     */
    @Select("SELECT * FROM roles WHERE name LIKE CONCAT('%', #{name}, '%') AND deleted = 0 ORDER BY created_at DESC")
    List<Role> findByNameContaining(@Param("name") String name);

    /**
     * 检查角色名称是否存在
     *
     * @param name 角色名称
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM roles WHERE name = #{name} AND deleted = 0")
    boolean existsByName(@Param("name") String name);

    @Select("SELECT * FROM roles WHERE id = #{roleId} AND deleted = 0")
    Role findById(Long roleId);
}