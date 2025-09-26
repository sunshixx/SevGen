package com.aichat.roleplay.mapper;

import com.aichat.roleplay.model.ReflectionLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反思日志Mapper接口
 * 负责反思日志的数据库操作
 */
@Mapper
public interface ReflectionMapper extends BaseMapper<ReflectionLog> {
    
    /**
     * 根据聊天ID查询反思日志列表
     */
    @Select("SELECT * FROM reflection_logs WHERE chat_id = #{chatId} ORDER BY create_time DESC")
    List<ReflectionLog> findByChatId(@Param("chatId") Long chatId);
    
    /**
     * 根据用户ID查询反思日志列表
     */
    @Select("SELECT * FROM reflection_logs WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<ReflectionLog> findByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    
    /**
     * 查询指定时间范围内的反思日志
     */
    @Select("SELECT * FROM reflection_logs WHERE create_time >= #{startTime} AND create_time <= #{endTime} ORDER BY create_time DESC")
    List<ReflectionLog> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计重试次数分布
     */
    @Select("SELECT retry_count, COUNT(*) as count FROM reflection_logs WHERE action_type = 'RETRY' GROUP BY retry_count ORDER BY retry_count")
    List<ReflectionLog> getRetryStatistics();
    
    /**
     * 查询异常检测统计
     */
    @Select("SELECT detected_issue, COUNT(*) as count FROM reflection_logs WHERE detected_issue IS NOT NULL AND detected_issue != '' GROUP BY detected_issue ORDER BY count DESC")
    List<ReflectionLog> getAnomalyStatistics();
}