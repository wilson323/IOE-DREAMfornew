package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.access.domain.entity.AccessEventEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁事件数据访问层
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Mapper注解标注DAO接口
 * - 继承BaseMapper获得基础CRUD能力
 * - 提供复杂查询的业务方法
 * - 支持分页和统计功能
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface AccessEventDao extends BaseMapper<AccessEventEntity> {

    /**
     * 统计设备总数
     *
     * @return 设备总数
     */
    Long countTotalDevices();

    /**
     * 统计在线设备数量
     *
     * @return 在线设备数量
     */
    Long countOnlineDevices();

    /**
     * 分页查询门禁事件
     *
     * @param page 分页对象
     * @param deviceId 设备ID（可选）
     * @param userId 用户ID（可选）
     * @param verifyResult 验证结果（可选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    Page<AccessEventEntity> selectEventPage(
            Page<AccessEventEntity> page,
            @Param("deviceId") String deviceId,
            @Param("userId") Long userId,
            @Param("verifyResult") String verifyResult,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 根据时间范围查询事件统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> selectEventStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询指定区域的访问统计
     *
     * @param areaIds 区域ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> selectAreaStatistics(
            @Param("areaIds") List<Long> areaIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询设备访问统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> selectDeviceStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询验证方式统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> selectVerifyMethodStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询最近的事件记录
     *
     * @param limit 限制数量
     * @return 事件列表
     */
    List<AccessEventEntity> selectRecentEvents(@Param("limit") Integer limit);

    /**
     * 查询用户最近访问记录
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 访问记录列表
     */
    List<AccessEventEntity> selectUserRecentEvents(
            @Param("userId") Long userId,
            @Param("limit") Integer limit
    );

    /**
     * 查询设备最近事件
     *
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 事件列表
     */
    List<AccessEventEntity> selectDeviceRecentEvents(
            @Param("deviceId") String deviceId,
            @Param("limit") Integer limit
    );

    /**
     * 统计失败访问次数
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 失败次数
     */
    Integer countFailedAccess(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询黑名单访问记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 记录列表
     */
    List<AccessEventEntity> selectBlacklistEvents(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询异常访问记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 记录列表
     */
    List<AccessEventEntity> selectAbnormalEvents(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 删除过期事件记录
     *
     * @param cutoffTime 截止时间
     * @return 删除数量
     */
    int deleteExpiredEvents(@Param("cutoffTime") LocalDateTime cutoffTime);
}