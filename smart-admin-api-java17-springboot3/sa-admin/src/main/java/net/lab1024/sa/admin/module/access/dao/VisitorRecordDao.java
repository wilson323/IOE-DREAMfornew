package net.lab1024.sa.admin.module.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import net.lab1024.sa.admin.module.access.domain.entity.VisitorRecordEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 访客记录数据访问对象
 * <p>
 * 严格遵循repowiki规范：
 * - 使用MyBatis-Plus简化数据访问
 * - 继承BaseMapper提供基础CRUD操作
 * - 定义必要的自定义查询方法
 * - 支持分页和条件查询
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Mapper
public interface VisitorRecordDao extends BaseMapper<VisitorRecordEntity> {

    /**
     * 根据访客手机号查询访问记录
     *
     * @param visitorPhone 访客手机号
     * @param limit         查询数量限制
     * @return 访问记录列表
     */
    List<VisitorRecordEntity> selectByVisitorPhone(@Param("visitorPhone") String visitorPhone,
                                                   @Param("limit") Integer limit);

    /**
     * 根据预约ID查询访问记录
     *
     * @param reservationId 预约ID
     * @return 访问记录列表
     */
    List<VisitorRecordEntity> selectByReservationId(@Param("reservationId") Long reservationId);

    /**
     * 根据访问区域查询访问记录
     *
     * @param visitAreaId 访问区域ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param limit       查询数量限制
     * @return 访问记录列表
     */
    List<VisitorRecordEntity> selectByVisitArea(@Param("visitAreaId") Long visitAreaId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("limit") Integer limit);

    /**
     * 根据设备ID查询访问记录
     *
     * @param deviceId   设备ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param limit      查询数量限制
     * @return 访问记录列表
     */
    List<VisitorRecordEntity> selectByDeviceId(@Param("deviceId") Long deviceId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("limit") Integer limit);

    /**
     * 查询异常访问记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     查询数量限制
     * @return 异常访问记录列表
     */
    List<VisitorRecordEntity> selectAbnormalRecords(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("limit") Integer limit);

    /**
     * 统计访问记录数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param visitAreaId 访问区域ID
     * @param accessResult 通行结果
     * @return 统计数量
     */
    Long countRecords(@Param("startTime") LocalDateTime startTime,
                      @Param("endTime") LocalDateTime endTime,
                      @Param("visitAreaId") Long visitAreaId,
                      @Param("accessResult") Integer accessResult);

    /**
     * 按日期统计访问记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> selectDailyStatistics(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 按区域统计访问记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> selectAreaStatistics(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 按设备统计访问记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> selectDeviceStatistics(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查询访问高峰时段
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 高峰时段数据
     */
    List<Map<String, Object>> selectPeakHours(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 查询频繁访问的访客
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     查询数量限制
     * @return 访客列表
     */
    List<Map<String, Object>> selectFrequentVisitors(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime,
                                                    @Param("limit") Integer limit);

    /**
     * 查询今日首次访问的访客
     *
     * @return 首次访问的访客列表
     */
    List<VisitorRecordEntity> selectFirstTimeVisitorsToday();

    /**
     * 查询超时访问记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     查询数量限制
     * @return 超时访问记录列表
     */
    List<VisitorRecordEntity> selectOvertimeRecords(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("limit") Integer limit);

    /**
     * 清理过期的访问记录
     *
     * @param expireDays 过期天数
     * @return 清理数量
     */
    int cleanExpiredRecords(@Param("expireDays") Integer expireDays);

    /**
     * 批量插入访问记录
     *
     * @param records 访问记录列表
     * @return 插入数量
     */
    int batchInsert(@Param("records") List<VisitorRecordEntity> records);
}