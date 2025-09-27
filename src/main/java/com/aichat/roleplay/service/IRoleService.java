package com.aichat.roleplay.service;

import com.aichat.roleplay.model.Role;

import java.util.List;
import java.util.Optional;

/**
 * 角色服务接口
 * 遵循SOLID原则中的接口隔离原则
 * 定义角色相关的业务操作
 */
public interface IRoleService {

    /**
     * 获取所有公开角色
     *
     * @return 公开角色列表
     */
    List<Role> getAllPublicRoles();

    /**
     * 根据分类获取角色
     *
     * @param category 分类
     * @return 角色列表
     */
    List<Role> getRolesByCategory(String category);

    /**
     * 根据ID获取角色
     *
     * @param id 角色ID
     * @return 角色信息
     */
    Optional<Role> getRoleById(Long id);

    /**
     * 根据名称获取角色
     *
     * @param name 角色名称
     * @return 角色信息
     */
    Optional<Role> getRoleByName(String name);

    /**
     * 搜索角色
     *
     * @param query 搜索关键词
     * @return 角色列表
     */
    List<Role> searchRoles(String query);

    /**
     * 创建角色
     *
     * @param role 角色信息
     * @return 创建的角色
     */
    Role createRole(Role role);

    /**
     * 更新角色
     *
     * @param role 角色信息
     * @return 更新后的角色
     */
    Role updateRole(Role role);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 检查角色名称是否存在
     *
     * @param name 角色名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 生成角色响应
     *
     * @param roleName 角色名称
     * @param prompt 提示词
     * @param context 对话上下文
     * @return 角色响应
     */
    String generateResponse(String roleName, String prompt, String context);
}