package net.lab1024.sa.common.monitor.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.monitor.domain.entity.SystemMonitorEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统监控DAO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Mapper注解（禁止@Repository）
 * - 使用Dao后缀（禁止Repository后缀）
 * - 继承BaseMapper提供基础CRUD
 * - 使用MyBatis-Plus（禁止JPA）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Mapper
public interface SystemMonitorDao extends BaseMapper<SystemMonitorEntity> {

    /**
     * 根据服务名称查询监控数据
     *
     * @param serviceName 服务名称
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @return 监控数据列表
     */
    List<SystemMonitorEntity> selectByServiceName(@Param("serviceName") String serviceName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据监控类型查询监控数据
     *
     * @param monitorType 监控类型
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @return 监控数据列表
     */
    List<SystemMonitorEntity> selectByMonitorType(@Param("monitorType") String monitorType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最新的监控指标
     *
     * @param serviceName 服务名称
     * @param monitorType 监控类型
     * @return 最新监控数据
     */
    SystemMonitorEntity selectLatestMonitor(@Param("serviceName") String serviceName,
            @Param("monitorType") String monitorType);

    /**
     * 统计监控数据按类型
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countByMonitorType(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询监控指标统计
     *
     * @param serviceName 服务名称
     * @param monitorType 监控类型
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @return 统计结果
     */
    Map<String, Object> selectMetricStats(@Param("serviceName") String serviceName,
            @Param("monitorType") String monitorType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 删除历史监控数据
     *
     * @param beforeTime 时间点之前的记录将被删除
     * @return 删除行数
     */
    @Transactional(rollbackFor = Exception.class)
    int deleteHistoryData(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 查询告警状态监控数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 告警状态监控数据列表
     */
    List<SystemMonitorEntity> selectAlertStatusData(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 批量插入监控数据
     *
     * @param monitorList 监控数据列表
     * @return 插入行数
     */
    @Transactional(rollbackFor = Exception.class)
    int batchInsert(@Param("monitorList") List<SystemMonitorEntity> monitorList);
}
