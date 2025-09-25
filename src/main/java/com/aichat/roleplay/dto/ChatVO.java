package com.aichat.roleplay.dto;

import com.aichat.roleplay.model.Chat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hxg111
 * @date 2025/9/25 21:11
 * @description: chatVO类，用于返回给前端
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatVO {
    private Long id;

    private Long userId;

    private Long roleId;

    private String title;

    private String updatedAt;

    /**
     * 将 Chat 对象转换为 ChatVO 对象
     * @param chat 源 Chat 对象
     * @return 转换后的 ChatVO 对象
     */
    public static ChatVO po2vo(Chat chat) {
        if (chat == null) {
            return null;
        }
        return ChatVO.builder()
                .id(chat.getId())
                .userId(chat.getUserId())
                .roleId(chat.getRoleId())
                .title(chat.getTitle())
                .updatedAt(chat.getUpdateTime().toString() )
                .build();
    }

    /**
     * 将 Chat 对象列表转换为 ChatVO 对象列表
     * @param chats 源 Chat 对象列表
     * @return 转换后的 ChatVO 对象列表
     */
    public static List<ChatVO> po2voList(List<Chat> chats) {
        if (chats == null) {
            return List.of();
        }
        return chats.stream()
                .map(ChatVO::po2vo)
                .collect(Collectors.toList());
    }

}
