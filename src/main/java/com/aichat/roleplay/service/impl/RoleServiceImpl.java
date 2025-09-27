package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.mapper.RoleMapper;
import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.service.IAiChatService;
import com.aichat.roleplay.service.IRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 角色服务实现类
 * 遵循SOLID原则中的单一职责原则和依赖倒置原则
 */
@Service
@Transactional
public class RoleServiceImpl implements IRoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleMapper roleMapper;
    private final IAiChatService aiChatService;

    @Autowired
    public RoleServiceImpl(RoleMapper roleMapper, IAiChatService aiChatService) {
        this.roleMapper = roleMapper;
        this.aiChatService = aiChatService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllPublicRoles() {
        log.debug("获取所有公开角色");
        return roleMapper.findPublicRoles();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getRolesByCategory(String category) {
        log.debug("根据分类获取角色: {}", category);
        return roleMapper.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> getRoleById(Long id) {
        log.debug("根据ID获取角色: {}", id);
        Role role = roleMapper.selectById(id);
        return Optional.ofNullable(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> getRoleByName(String name) {
        log.debug("根据名称获取角色: {}", name);
        List<Role> roles = roleMapper.findByNameContaining(name);
        return roles.stream().filter(role -> name.equals(role.getName())).findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> searchRoles(String query) {
        log.debug("搜索角色: {}", query);
        return roleMapper.findByNameContaining(query);
    }

    @Override
    public Role createRole(Role role) {
        log.info("创建角色: {}", role.getName());

        // 验证角色名称是否已存在
        if (roleMapper.existsByName(role.getName())) {
            throw new RuntimeException("角色名称已存在");
        }

        // 设置默认值
        role.setDeleted(0);
        if (role.getIsPublic() == null) {
            role.setIsPublic(true);
        }

        int result = roleMapper.insert(role);
        if (result > 0) {
            log.info("角色创建成功，角色ID: {}", role.getId());
            return role;
        } else {
            throw new RuntimeException("角色创建失败");
        }
    }

    @Override
    public Role updateRole(Role role) {
        log.info("更新角色: {}", role.getId());

        // 检查角色是否存在
        if (roleMapper.selectById(role.getId()) == null) {
            throw new RuntimeException("角色不存在");
        }

        int result = roleMapper.updateById(role);
        if (result > 0) {
            log.info("角色更新成功，角色ID: {}", role.getId());
            return role;
        } else {
            throw new RuntimeException("角色更新失败");
        }
    }

    @Override
    public void deleteRole(Long id) {
        log.info("删除角色: {}", id);

        // 使用逻辑删除
        int result = roleMapper.deleteById(id);
        if (result > 0) {
            log.info("角色删除成功，角色ID: {}", id);
        } else {
            throw new RuntimeException("角色删除失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        log.debug("检查角色名称是否存在: {}", name);
        return roleMapper.existsByName(name);
    }
}