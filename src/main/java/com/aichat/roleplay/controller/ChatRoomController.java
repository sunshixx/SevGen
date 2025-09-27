package com.aichat.roleplay.controller;

import com.aichat.roleplay.common.ApiResponse;
import com.aichat.roleplay.context.UserContext;
import com.aichat.roleplay.dto.ChatRoomVO;
import com.aichat.roleplay.model.ChatRoom;
import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.model.User;
import com.aichat.roleplay.service.IChatRoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 聊天室控制器
 * 专门处理多agent聊天室相关功能
 */
@RestController
@RequestMapping("/api/chatrooms")
public class ChatRoomController {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomController.class);

    @Autowired
    private IChatRoomService chatRoomService;

    /**
     * 创建聊天室
     */
    @PostMapping
    public ApiResponse<Long> createChatRoom(@RequestParam String name, 
                                           @RequestParam(required = false) String description,
                                           @RequestParam List<Long> roleIds) {
        User user = getCurrentUser();
        log.info("创建聊天室，名称: {}, 角色数量: {}, 用户: {}", name, roleIds.size(), user.getUsername());

        try {
            ChatRoomVO chatRoomVO = chatRoomService.createChatRoom(user.getId(), name, description, roleIds);
            return ApiResponse.success("聊天室创建成功", chatRoomVO.getId());
        } catch (Exception e) {
            log.error("创建聊天室失败", e);
            return ApiResponse.error("创建聊天室失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的聊天室列表
     */
    @GetMapping
    public ApiResponse<List<ChatRoomVO>> getUserChatRooms() {
        User user = getCurrentUser();
        log.info("获取用户聊天室列表，用户: {}", user.getUsername());

        try {
            List<ChatRoomVO> chatRooms = chatRoomService.getUserChatRooms(user.getId());
            return ApiResponse.success("获取聊天室列表成功", chatRooms);
        } catch (Exception e) {
            log.error("获取聊天室列表失败", e);
            return ApiResponse.error("获取聊天室列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据聊天室ID获取聊天室信息
     */
    @GetMapping("/{chatRoomId}")
    public ApiResponse<ChatRoomVO> getChatRoomById(@PathVariable Long chatRoomId) {
        User user = getCurrentUser();
        log.info("获取聊天室信息，聊天室ID: {}, 用户: {}", chatRoomId, user.getUsername());

        try {
            ChatRoomVO chatRoom = chatRoomService.getChatRoomById(chatRoomId);
            if (chatRoom != null) {
                return ApiResponse.success("获取聊天室信息成功", chatRoom);
            } else {
                return ApiResponse.error("聊天室不存在");
            }
        } catch (Exception e) {
            log.error("获取聊天室信息失败", e);
            return ApiResponse.error("获取聊天室信息失败: " + e.getMessage());
        }
    }

    /**
     * 向聊天室添加角色
     */
    @PostMapping("/{chatRoomId}/roles")
    public ApiResponse<String> addRoleToRoom(@PathVariable Long chatRoomId, @RequestParam Long roleId) {
        User user = getCurrentUser();
        log.info("向聊天室添加角色，聊天室ID: {}, 角色ID: {}, 用户: {}", chatRoomId, roleId, user.getUsername());

        try {
            chatRoomService.addRoleToChatRoom(chatRoomId, roleId);
            return ApiResponse.success("角色添加成功");
        } catch (Exception e) {
            log.error("向聊天室添加角色失败", e);
            return ApiResponse.error("向聊天室添加角色失败: " + e.getMessage());
        }
    }

    /**
     * 从聊天室移除角色
     */
    @DeleteMapping("/{chatRoomId}/roles/{roleId}")
    public ApiResponse<String> removeRoleFromRoom(@PathVariable Long chatRoomId, @PathVariable Long roleId) {
        User user = getCurrentUser();
        log.info("从聊天室移除角色，聊天室ID: {}, 角色ID: {}, 用户: {}", chatRoomId, roleId, user.getUsername());

        try {
            chatRoomService.removeRoleFromChatRoom(chatRoomId, roleId);
            return ApiResponse.success("角色移除成功");
        } catch (Exception e) {
            log.error("从聊天室移除角色失败", e);
            return ApiResponse.error("从聊天室移除角色失败: " + e.getMessage());
        }
    }

    /**
     * 获取聊天室中的角色列表
     */
    @GetMapping("/{chatRoomId}/roles")
    public ApiResponse<List<Long>> getChatRoomRoles(@PathVariable Long chatRoomId) {
        User user = getCurrentUser();
        log.info("获取聊天室角色列表，聊天室ID: {}, 用户: {}", chatRoomId, user.getUsername());

        try {
            List<Role> roles = chatRoomService.getChatRoomRoles(chatRoomId);
            List<Long> roleIds = roles.stream().map(Role::getId).collect(java.util.stream.Collectors.toList());
            return ApiResponse.success("获取聊天室角色列表成功", roleIds);
        } catch (Exception e) {
            log.error("获取聊天室角色列表失败", e);
            return ApiResponse.error("获取聊天室角色列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除聊天室
     */
    @DeleteMapping("/{chatRoomId}")
    public ApiResponse<String> deleteChatRoom(@PathVariable Long chatRoomId) {
        User user = getCurrentUser();
        log.info("删除聊天室，聊天室ID: {}, 用户: {}", chatRoomId, user.getUsername());

        try {
            chatRoomService.deleteChatRoom(chatRoomId);
            return ApiResponse.success("聊天室删除成功");
        } catch (Exception e) {
            log.error("删除聊天室失败", e);
            return ApiResponse.error("删除聊天室失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户未登录或登录已过期");
        }
        return user;
    }
}