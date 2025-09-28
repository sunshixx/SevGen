package com.aichat.roleplay.dto;

import com.aichat.roleplay.model.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天室视图对象
 * 用于返回聊天室列表数据，包含参与人数和活跃状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomVO {
    private Long id;
    private Long chatRoomId;
    private String name;
    private String description;
    private Boolean isActive;
    private Long participantCount;
    private String createTime;
    private String updateTime;

    /**
     * 将ChatRoom实体转换为ChatRoomVO
     *
     * @param chatRoom 聊天室实体
     * @param participantCountMap 参与人数映射
     * @param activeStatusMap 活跃状态映射
     * @return ChatRoomVO对象
     */
    public static ChatRoomVO po2vo(ChatRoom chatRoom, 
                                 Map<Long, Long> participantCountMap,
                                 Map<Long, Boolean> activeStatusMap) {
        if (chatRoom == null) {
            return null;
        }
        
        ChatRoomVO vo = new ChatRoomVO();
        vo.setId(chatRoom.getId());
        vo.setChatRoomId(chatRoom.getChatRoomId());
        vo.setName(chatRoom.getTitle());
        vo.setDescription(chatRoom.getDescription());
        vo.setCreateTime(chatRoom.getCreateTime() != null ? chatRoom.getCreateTime().toString() : null);
        vo.setUpdateTime(chatRoom.getUpdateTime() != null ? chatRoom.getUpdateTime().toString() : null);
        
        // 设置参与人数
        Long participantCount = participantCountMap.get(chatRoom.getChatRoomId());
        vo.setParticipantCount(participantCount != null ? participantCount : 0L);
        
        // 设置活跃状态
        Boolean isActive = activeStatusMap.get(chatRoom.getChatRoomId());
        vo.setIsActive(isActive != null ? isActive : false);
        
        return vo;
    }

    /**
     * 将ChatRoom实体列表转换为ChatRoomVO列表
     *
     * @param chatRooms 聊天室实体列表
     * @param participantCountMap 参与人数映射
     * @param activeStatusMap 活跃状态映射
     * @return ChatRoomVO列表
     */
    public static List<ChatRoomVO> po2voList(List<ChatRoom> chatRooms, 
                                           Map<Long, Long> participantCountMap,
                                           Map<Long, Boolean> activeStatusMap) {
        if (chatRooms == null || chatRooms.isEmpty()) {
            return new ArrayList<>();
        }
        
        return chatRooms.stream()
                .map(chatRoom -> po2vo(chatRoom, participantCountMap, activeStatusMap))
                .collect(Collectors.toList());
    }
}