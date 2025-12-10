package net.lab1024.sa.common.monitor.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.monitor.domain.entity.AlertEntity;

/**
 * 告警DAO
 * 整合自ioedream-monitor-service
 *
 * 符合CLAUDE.md规范：
 * - 使用@Mapper注解（强制）
 * - 继承BaseMapper提供基础CRUD
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自monitor-service）
 */
@Mapper
public interface AlertDao extends BaseMapper<AlertEntity> {

    List<AlertEntity> selectActiveAlerts();

    List<AlertEntity> selectByServiceName(@Param("serviceName") String serviceName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<AlertEntity> selectByAlertLevel(@Param("alertLevel") String alertLevel,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<AlertEntity> selectRecentAlerts(@Param("hours") Integer hours);

    List<Map<String, Object>> countAlertsByLevel(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<Map<String, Object>> countAlertsByType(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<Map<String, Object>> countAlertsByService(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    int updateResolveInfo(@Param("alertId") Long alertId,
            @Param("resolveTime") LocalDateTime resolveTime,
            @Param("resolveUserId") Long resolveUserId,
            @Param("resolution") String resolution);

    int deleteHistoryAlerts(@Param("beforeTime") LocalDateTime beforeTime);
}
