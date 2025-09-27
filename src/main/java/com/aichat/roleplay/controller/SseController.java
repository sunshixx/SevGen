package com.aichat.roleplay.controller;

import com.aichat.roleplay.service.SseService;
import com.aichat.roleplay.service.IChatroomCollaborationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@RestController
@RequestMapping("/api/sse")
public class SseController {

    @Autowired
    private SseService sseService;
    
    @Autowired
    private IChatroomCollaborationService chatroomCollaborationService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream(@RequestParam Long chatId,
                                           @RequestParam Long roleId,
                                           @RequestParam String userMessage) {
        try {

            SseEmitter emitter = sseService.stream(chatId, roleId, userMessage);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(emitter);
        } catch (Exception e) {
            // 返回错误的SSE流
            SseEmitter errorEmitter = new SseEmitter();
            try {
                errorEmitter.send("data: [ERROR] " + e.getMessage() + "\n\n");
                errorEmitter.complete();
            } catch (Exception sendEx) {
                errorEmitter.completeWithError(sendEx);
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(errorEmitter);
        }
    }

    @GetMapping(value = "/collaborate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> collaborate(@RequestParam Long chatId,
                                                @RequestParam String userMessage,
                                                @RequestParam(required = false) String context) {
        try {
            SseEmitter emitter = chatroomCollaborationService.handleCollaborativeMessage(chatId, userMessage, context);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(emitter);
        } catch (Exception e) {
            // 返回错误的SSE流
            SseEmitter errorEmitter = new SseEmitter();
            try {
                errorEmitter.send("data: {\"type\":\"ERROR\",\"message\":\"" + e.getMessage() + "\"}\n\n");
                errorEmitter.complete();
            } catch (Exception sendEx) {
                errorEmitter.completeWithError(sendEx);
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(errorEmitter);
        }
    }

}