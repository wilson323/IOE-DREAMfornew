package net.lab1024.sa.common.monitor.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.monitor.domain.entity.SystemLogEntity;

/**
 * 系统日志DAO
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
public interface SystemLogDao extends BaseMapper<SystemLogEntity> {

    /**
     * 根据日志级别查询日志
     *
     * @param logLevel  日志级别
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 日志列表
     */
    List<SystemLogEntity> selectByLogLevel(@Param("logLevel") String logLevel,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") Integer limit);

    /**
     * 根据服务名称查询日志
     *
     * @param serviceName 服务名称
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param limit       限制数量
     * @return 日志列表
     */
    List<SystemLogEntity> selectByServiceName(@Param("serviceName") String serviceName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") Integer limit);

    /**
     * 根据用户ID查询日志
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 日志列表
     */
    List<SystemLogEntity> selectByUserId(@Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") Integer limit);

    /**
     * 根据请求ID查询日志
     *
     * @param requestId 请求ID
     * @return 日志列表
     */
    List<SystemLogEntity> selectByRequestId(@Param("requestId") String requestId);

    /**
     * 根据追踪ID查询日志
     *
     * @param traceId 追踪ID
     * @return 日志列表
     */
    List<SystemLogEntity> selectByTraceId(@Param("traceId") String traceId);

    /**
     * 搜索日志内容
     *
     * @param keyword   搜索关键词
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 日志列表
     */
    List<SystemLogEntity> searchLogs(@Param("keyword") String keyword,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") Integer limit);

    /**
     * 统计日志数量按级别
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countLogsByLevel(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计日志数量按类型
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countLogsByType(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计日志数量按服务
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countLogsByService(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询错误日志趋势
     *
     * @param hours 小时数
     * @return 趋势数据
     */
    List<Map<String, Object>> selectErrorLogTrends(@Param("hours") Integer hours);

    /**
     * 批量插入日志
     *
     * @param logList 日志列表
     * @return 插入行数
     */
    int batchInsert(@Param("logList") List<SystemLogEntity> logList);

    /**
     * 删除历史日志数据
     *
     * @param beforeTime 时间点之前的记录将被删除
     * @return 删除行数
     */
    int deleteHistoryLogs(@Param("beforeTime") LocalDateTime beforeTime);
}
