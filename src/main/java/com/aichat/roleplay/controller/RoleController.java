package com.aichat.roleplay.controller;

import com.aichat.roleplay.common.ApiResponse;
import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private IRoleService roleService;

    @GetMapping
    public ApiResponse<?> getAllPublicRoles() {
        List<Role> roles = roleService.getAllPublicRoles();
        return ApiResponse.success("获取角色列表成功", roles);
    }

    @GetMapping("/search")
    public ApiResponse<?> searchRoles(@RequestParam String query) {
        List<Role> roles = roleService.searchRoles(query);
        return ApiResponse.success("搜索角色成功", roles);
    }

    @GetMapping("/category/{category}")
    public ApiResponse<?> getRolesByCategory(@PathVariable String category) {
        List<Role> roles = roleService.getRolesByCategory(category);
        return ApiResponse.success("获取分类角色成功", roles);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return ApiResponse.success("获取角色详情成功", role);
    }

    @GetMapping("/name/{name}")
    public ApiResponse<?> getRoleByName(@PathVariable String name) {
        Role role = roleService.getRoleByName(name)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return ApiResponse.success("获取角色详情成功", role);
    }

    @PostMapping
    public ApiResponse<?> createRole(@RequestBody Role role) {
        Role createdRole = roleService.createRole(role);
        return ApiResponse.success("创建角色成功", createdRole);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        Role updatedRole = roleService.updateRole(role);
        return ApiResponse.success("更新角色成功", updatedRole);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success("删除角色成功");
    }
}