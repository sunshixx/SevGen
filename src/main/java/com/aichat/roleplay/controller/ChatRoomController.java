package com.aichat.roleplay.controller;

import com.aichat.roleplay.common.ApiResponse;
import com.aichat.roleplay.dto.ChatRoomVO;
import com.aichat.roleplay.model.ChatRoom;
import com.aichat.roleplay.service.IChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天室控制器
 * 提供聊天室相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final IChatRoomService chatRoomService;

    /**
     * 获取聊天室列表（包含参与人数和活跃状态）
     */
    @GetMapping("/list")
    public ApiResponse<List<ChatRoomVO>> getChatRoomList() {
        log.info("获取聊天室列表");
        
        try {
            List<ChatRoomVO> chatRoomList = chatRoomService.getChatRoomList();
            log.info("获取聊天室列表成功，数量: {}", chatRoomList.size());
            return ApiResponse.success("获取聊天室列表成功", chatRoomList);
        } catch (Exception e) {
            log.error("获取聊天室列表失败", e);
            return ApiResponse.error("获取聊天室列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据chatRoomId获取聊天室信息
     */
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatRoom> getChatRoomById(@PathVariable Long chatRoomId) {
        log.info("获取聊天室信息: {}", chatRoomId);
        
        try {
            ChatRoom chatRoom = chatRoomService.getByChatRoomId(chatRoomId);
            if (chatRoom != null) {
                return ResponseEntity.ok(chatRoom);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取聊天室信息失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取所有聊天室列表
     */
    @GetMapping
    public ResponseEntity<List<ChatRoom>> getAllChatRooms() {
        log.info("获取所有聊天室列表");
        
        try {
            List<ChatRoom> chatRooms = chatRoomService.list();
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            log.error("获取聊天室列表失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 创建新聊天室
     */
    @PostMapping
    public ResponseEntity<ChatRoom> createChatRoom(@Validated @RequestBody ChatRoom chatRoom) {
        log.info("创建聊天室请求: {}", chatRoom);
        
        try {
            // 从UserContext中获取当前用户ID
            Long currentUserId = com.aichat.roleplay.context.UserContext.getCurrentUserId();
            if (currentUserId == null) {
                log.error("创建聊天室失败: 用户未登录");
                return ResponseEntity.status(401).build();
            }
            
            // 生成唯一的chatRoomId（使用时间戳）
            chatRoom.setChatRoomId(System.currentTimeMillis());
            chatRoom.setUserId(currentUserId);
            
            ChatRoom created = chatRoomService.createChatRoom(chatRoom);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("创建聊天室失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 向聊天室添加角色
     */
    @PostMapping("/{chatRoomId}/roles/{roleId}")
    public ResponseEntity<ChatRoom> addRoleToChatRoom(
            @PathVariable Long chatRoomId,
            @PathVariable Long roleId,
            @RequestParam Long userId) {
        
        log.info("向聊天室 {} 添加角色 {}, 用户: {}", chatRoomId, roleId, userId);
        
        try {
            ChatRoom chatRoom = chatRoomService.addRoleToChatRoom(chatRoomId, userId, roleId);
            return ResponseEntity.ok(chatRoom);
        } catch (Exception e) {
            log.error("添加角色失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取聊天室的所有角色
     */
    @GetMapping("/{chatRoomId}/roles")
    public ResponseEntity<List<ChatRoom>> getRolesByChatRoomId(@PathVariable Long chatRoomId) {
        
        log.info("查询聊天室 {} 的角色", chatRoomId);
        
        try {
            List<ChatRoom> roles = chatRoomService.getRolesByChatRoomId(chatRoomId);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            log.error("查询聊天室角色失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取用户的所有聊天室
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatRoom>> getChatRoomsByUserId(@PathVariable Long userId) {
        
        log.info("查询用户 {} 的聊天室", userId);
        
        try {
            List<ChatRoom> chatRooms = chatRoomService.getChatRoomsByUserId(userId);
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            log.error("查询用户聊天室失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取用户的活跃聊天室
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<ChatRoom>> getActiveChatRoomsByUserId(@PathVariable Long userId) {
        
        log.info("查询用户 {} 的活跃聊天室", userId);
        
        try {
            List<ChatRoom> chatRooms = chatRoomService.getActiveChatRoomsByUserId(userId);
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            log.error("查询用户活跃聊天室失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 根据角色ID获取聊天室
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<ChatRoom>> getChatRoomsByRoleId(@PathVariable Long roleId) {
        
        log.info("查询角色 {} 的聊天室", roleId);
        
        try {
            List<ChatRoom> chatRooms = chatRoomService.getChatRoomsByRoleId(roleId);
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            log.error("查询角色聊天室失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新聊天室信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ChatRoom> updateChatRoom(
            @PathVariable Long id,
            @RequestBody ChatRoom chatRoom) {
        
        log.info("更新聊天室 {}: {}", id, chatRoom);
        
        try {
            chatRoom.setId(id);
            ChatRoom updated = chatRoomService.updateChatRoom(chatRoom);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("更新聊天室失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新角色激活状态
     */
    @PutMapping("/{id}/active")
    public ResponseEntity<Void> updateRoleActiveStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        
        log.info("更新记录 {} 激活状态为: {}", id, isActive);
        
        try {
            boolean success = chatRoomService.updateRoleActiveStatus(id, isActive);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("更新激活状态失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除聊天室角色记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChatRoomRole(@PathVariable Long id) {
        
        log.info("删除聊天室角色记录: {}", id);
        
        try {
            boolean success = chatRoomService.deleteChatRoomRole(id);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("删除聊天室角色失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除整个聊天室
     */
    @DeleteMapping("/room/{chatRoomId}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable Long chatRoomId) {
        
        log.info("删除聊天室: {}", chatRoomId);
        
        try {
            // 从UserContext中获取当前用户ID
            Long currentUserId = com.aichat.roleplay.context.UserContext.getCurrentUserId();
            if (currentUserId == null) {
                log.error("删除聊天室失败: 用户未登录");
                return ResponseEntity.status(401).build();
            }
            
            boolean success = chatRoomService.deleteChatRoom(chatRoomId, currentUserId);
            if (success) {
                log.info("聊天室 {} 删除成功", chatRoomId);
                return ResponseEntity.ok().build();
            } else {
                log.error("聊天室 {} 删除失败", chatRoomId);
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("删除聊天室失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 根据聊天室ID和角色ID查询记录
     */
    @GetMapping("/{chatRoomId}/role/{roleId}")
    public ResponseEntity<ChatRoom> getChatRoomByRoomIdAndRoleId(
            @PathVariable Long chatRoomId,
            @PathVariable Long roleId) {
        
        log.info("查询聊天室 {} 中的角色 {}", chatRoomId, roleId);
        
        try {
            ChatRoom chatRoom = chatRoomService.getChatRoomByRoomIdAndRoleId(chatRoomId, roleId);
            if (chatRoom != null) {
                return ResponseEntity.ok(chatRoom);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("查询聊天室角色失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 统计聊天室角色数量
     */
    @GetMapping("/{chatRoomId}/count")
    public ResponseEntity<Long> countRolesByChatRoomId(@PathVariable Long chatRoomId) {
        
        log.info("统计聊天室 {} 的角色数量", chatRoomId);
        
        try {
            Long count = chatRoomService.countRolesByChatRoomId(chatRoomId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("统计聊天室角色数量失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 检查用户权限
     */
    @GetMapping("/{chatRoomId}/permission/{userId}")
    public ResponseEntity<Boolean> hasPermission(
            @PathVariable Long chatRoomId,
            @PathVariable Long userId) {
        
        log.info("检查用户 {} 对聊天室 {} 的权限", userId, chatRoomId);
        
        try {
            boolean hasPermission = chatRoomService.hasPermission(chatRoomId, userId);
            return ResponseEntity.ok(hasPermission);
        } catch (Exception e) {
            log.error("检查用户权限失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
}