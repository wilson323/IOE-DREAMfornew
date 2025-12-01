package net.lab1024.sa.admin.module.attendance.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRuleEntity;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceScheduleEntity;
import net.lab1024.sa.base.common.cache.CacheNamespace;
import net.lab1024.sa.base.common.cache.UnifiedCacheManager;

/**
 * 考勤系统统一缓存服务
 * <p>
 * 基于UnifiedCacheManager实现的考勤模块缓存服务，提供统一的缓存接口
 * 遵循缓存架构统一化规范：
 * - 使用ATTENDANCE命名空间
 * - 统一缓存键格式：iog:cache:ATTENDANCE:{namespace}:{key}
 * - 标准TTL配置：默认30分钟
 * - 支持异步操作和批量操作
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
public interface AttendanceCacheService {

    // ========== 考勤记录缓存 ==========

    /**
     * 获取考勤记录
     *
     * @param recordId 记录ID
     * @return 考勤记录
     */
    AttendanceRecordEntity getAttendanceRecord(Long recordId);

    /**
     * 异步获取考勤记录
     *
     * @param recordId 记录ID
     * @return 异步结果
     */
    CompletableFuture<AttendanceRecordEntity> getAttendanceRecordAsync(Long recordId);

    /**
     * 缓存考勤记录
     *
     * @param record 考勤记录
     */
    void cacheAttendanceRecord(AttendanceRecordEntity record);

    /**
     * 异步缓存考勤记录
     *
     * @param record 考勤记录
     * @return 异步结果
     */
    CompletableFuture<Void> cacheAttendanceRecordAsync(AttendanceRecordEntity record);

    /**
     * 获取用户当日考勤记录
     *
     * @param userId 用户ID
     * @param date   日期 yyyy-MM-dd
     * @return 考勤记录列表
     */
    List<AttendanceRecordEntity> getUserTodayAttendance(Long userId, String date);

    /**
     * 缓存用户当日考勤记录
     *
     * @param userId  用户ID
     * @param date    日期
     * @param records 考勤记录列表
     */
    void cacheUserTodayAttendance(Long userId, String date, List<AttendanceRecordEntity> records);

    /**
     * 获取设备当日考勤记录
     *
     * @param deviceId 设备ID
     * @param date     日期 yyyy-MM-dd
     * @return 考勤记录列表
     */
    List<AttendanceRecordEntity> getDeviceTodayAttendance(Long deviceId, String date);

    /**
     * 缓存设备当日考勤记录
     *
     * @param deviceId 设备ID
     * @param date     日期
     * @param records  考勤记录列表
     */
    void cacheDeviceTodayAttendance(Long deviceId, String date, List<AttendanceRecordEntity> records);

    // ========== 考勤规则缓存 ==========

    /**
     * 获取考勤规则
     *
     * @param ruleId 规则ID
     * @return 考勤规则
     */
    AttendanceRuleEntity getAttendanceRule(Long ruleId);

    /**
     * 缓存考勤规则
     *
     * @param rule 考勤规则
     */
    void cacheAttendanceRule(AttendanceRuleEntity rule);

    /**
     * 批量获取考勤规则
     *
     * @param ruleIds 规则ID列表
     * @return 考勤规则映射
     */
    Map<Long, AttendanceRuleEntity> batchGetAttendanceRules(List<Long> ruleIds);

    /**
     * 批量缓存考勤规则
     *
     * @param rules 考勤规则列表
     */
    void batchCacheAttendanceRules(List<AttendanceRuleEntity> rules);

    /**
     * 获取部门考勤规则列表
     *
     * @param departmentId 部门ID
     * @return 考勤规则列表
     */
    List<AttendanceRuleEntity> getDepartmentRules(Long departmentId);

    /**
     * 缓存部门考勤规则列表
     *
     * @param departmentId 部门ID
     * @param rules        考勤规则列表
     */
    void cacheDepartmentRules(Long departmentId, List<AttendanceRuleEntity> rules);

    // ========== 考勤排班缓存 ==========

    /**
     * 获取考勤排班
     *
     * @param scheduleId 排班ID
     * @return 考勤排班
     */
    AttendanceScheduleEntity getAttendanceSchedule(Long scheduleId);

    /**
     * 缓存考勤排班
     *
     * @param schedule 考勤排班
     */
    void cacheAttendanceSchedule(AttendanceScheduleEntity schedule);

    /**
     * 获取用户排班列表
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 排班列表
     */
    List<AttendanceScheduleEntity> getUserSchedules(Long userId, String startDate, String endDate);

    /**
     * 缓存用户排班列表
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param schedules 排班列表
     */
    void cacheUserSchedules(Long userId, String startDate, String endDate, List<AttendanceScheduleEntity> schedules);

    /**
     * 获取设备排班列表
     *
     * @param deviceId 设备ID
     * @param date     日期
     * @return 排班列表
     */
    List<AttendanceScheduleEntity> getDeviceSchedules(Long deviceId, String date);

    /**
     * 缓存设备排班列表
     *
     * @param deviceId  设备ID
     * @param date      日期
     * @param schedules 排班列表
     */
    void cacheDeviceSchedules(Long deviceId, String date, List<AttendanceScheduleEntity> schedules);

    // ========== 统计数据缓存 ==========

    /**
     * 获取考勤统计数据
     *
     * @param date 日期 yyyy-MM-dd
     * @param type 统计类型
     * @return 统计信息
     */
    Map<String, Object> getAttendanceStats(String date, String type);

    /**
     * 缓存考勤统计数据
     *
     * @param date  日期
     * @param type  统计类型
     * @param stats 统计信息
     */
    void cacheAttendanceStats(String date, String type, Map<String, Object> stats);

    /**
     * 获取用户考勤统计
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计信息
     */
    Map<String, Object> getUserAttendanceStats(Long userId, String startDate, String endDate);

    /**
     * 缓存用户考勤统计
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param stats     统计信息
     */
    void cacheUserAttendanceStats(Long userId, String startDate, String endDate, Map<String, Object> stats);

    // ========== 缓存管理操作 ==========

    /**
     * 清除用户相关缓存
     *
     * @param userId 用户ID
     */
    void clearUserCache(Long userId);

    /**
     * 清除设备相关缓存
     *
     * @param deviceId 设备ID
     */
    void clearDeviceCache(Long deviceId);

    /**
     * 清除规则相关缓存
     *
     * @param ruleId 规则ID
     */
    void clearRuleCache(Long ruleId);

    /**
     * 清除日期相关缓存
     *
     * @param date 日期
     */
    void clearDateCache(String date);

    /**
     * 清除排班相关缓存
     *
     * @param scheduleId 排班ID
     */
    void clearScheduleCache(Long scheduleId);

    /**
     * 预热缓存
     */
    void warmupCache();

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计
     */
    Map<String, Object> getCacheStatistics();

    /**
     * 清理ATTENDANCE命名空间下的所有缓存
     */
    void clearAllAttendanceCache();
}