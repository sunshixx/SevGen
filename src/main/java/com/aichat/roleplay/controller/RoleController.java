package com.aichat.roleplay.controller;

import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private IRoleService roleService;

    @GetMapping
    public ResponseEntity<?> getAllPublicRoles() {
        List<Role> roles = roleService.getAllPublicRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchRoles(@RequestParam String query) {
        List<Role> roles = roleService.searchRoles(query);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getRolesByCategory(@PathVariable String category) {
        List<Role> roles = roleService.getRolesByCategory(category);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return ResponseEntity.ok(role);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<?> getRoleByName(@PathVariable String name) {
        Role role = roleService.getRoleByName(name)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return ResponseEntity.ok(role);
    }

    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        Role createdRole = roleService.createRole(role);
        return ResponseEntity.ok(createdRole);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        Role updatedRole = roleService.updateRole(role);
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(Map.of("message", "Role deleted successfully"));
    }
}