package com.aichat.roleplay.controller;

import com.aichat.roleplay.dto.ChatRequest;
import com.aichat.roleplay.dto.ChatResponse;
import com.aichat.roleplay.service.IChatOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * 多角色聊天控制器
 */
@RestController
@RequestMapping("/api/chat")
public class MultiRoleChatController {

    private static final Logger log = LoggerFactory.getLogger(MultiRoleChatController.class);

    private final IChatOrchestrator chatOrchestrator;
    private final ExecutorService executorService;

    @Autowired
    public MultiRoleChatController(IChatOrchestrator chatOrchestrator, ExecutorService executorService) {
        this.chatOrchestrator = chatOrchestrator;
        this.executorService = executorService;
    }

    /**
     * 实时聊天API
     */
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestParam Long userId,
            @RequestParam String query,
            @RequestParam(required = false) String context) {
        
        log.debug("收到实时聊天请求: userId={}, query={}", userId, query);
        
        SseEmitter emitter = new SseEmitter();
        
        executorService.execute(() -> {
            try {
                // 构建请求
                ChatRequest request = new ChatRequest(userId, query, context);
                
                // 处理聊天请求
                ChatResponse response = chatOrchestrator.chat(request);
                
                // 发送角色响应
                for (ChatResponse.RoleResponse roleResponse : response.getResponses()) {
                    emitter.send(SseEmitter.event()
                            .name("role_response")
                            .data(roleResponse));
                }
                
                // 发送最终响应
                emitter.send(SseEmitter.event()
                        .name("final_response")
                        .data(response.getFinalResponse()));
                
                emitter.complete();
                
            } catch (IOException e) {
                log.error("发送SSE事件失败", e);
                emitter.completeWithError(e);
            } catch (Exception e) {
                log.error("处理聊天请求失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("处理请求失败: " + e.getMessage()));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });
        
        return emitter;
    }
}