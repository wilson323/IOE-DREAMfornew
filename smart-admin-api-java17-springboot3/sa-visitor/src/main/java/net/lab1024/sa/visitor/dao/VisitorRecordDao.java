package net.lab1024.sa.visitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.visitor.domain.entity.VisitorRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 访客记录DAO接口
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseMapper基础接口
 * - 使用@Mapper注解
 * - 提供自定义查询方法
 * - 参数使用@Param注解
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Mapper
public interface VisitorRecordDao extends BaseMapper<VisitorRecordEntity> {

    /**
     * 根据访客手机号查询访问记录
     *
     * @param visitorPhone 访客手机号
     * @param limit        查询数量限制
     * @return 访问记录列表
     */
    @Select("SELECT * FROM t_visitor_record WHERE visitor_phone = #{visitorPhone} AND deleted_flag = 0 ORDER BY access_time DESC LIMIT #{limit}")
    List<VisitorRecordEntity> selectByVisitorPhone(@Param("visitorPhone") String visitorPhone, @Param("limit") Integer limit);

    /**
     * 根据预约ID查询访问记录
     *
     * @param reservationId 预约ID
     * @return 访问记录列表
     */
    @Select("SELECT * FROM t_visitor_record WHERE reservation_id = #{reservationId} AND deleted_flag = 0 ORDER BY access_time DESC")
    List<VisitorRecordEntity> selectByReservationId(@Param("reservationId") Long reservationId);

    /**
     * 根据访问区域查询访问记录
     *
     * @param visitAreaId 访问区域ID
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return 访问记录列表
     */
    @Select("SELECT * FROM t_visitor_record WHERE visit_area_id = #{visitAreaId} AND DATE(access_time) >= #{startDate} AND DATE(access_time) <= #{endDate} AND deleted_flag = 0 ORDER BY access_time DESC")
    List<VisitorRecordEntity> selectByAreaIdAndDateRange(@Param("visitAreaId") Long visitAreaId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    /**
     * 根据门禁设备查询访问记录
     *
     * @param accessDeviceId 门禁设备ID
     * @param startDate      开始日期
     * @param endDate        结束日期
     * @return 访问记录列表
     */
    @Select("SELECT * FROM t_visitor_record WHERE access_device_id = #{accessDeviceId} AND DATE(access_time) >= #{startDate} AND DATE(access_time) <= #{endDate} AND deleted_flag = 0 ORDER BY access_time DESC")
    List<VisitorRecordEntity> selectByDeviceIdAndDateRange(@Param("accessDeviceId") Long accessDeviceId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    /**
     * 查询指定日期的访问记录
     *
     * @param accessDate 访问日期
     * @return 访问记录列表
     */
    @Select("SELECT * FROM t_visitor_record WHERE DATE(access_time) = #{accessDate} AND deleted_flag = 0 ORDER BY access_time DESC")
    List<VisitorRecordEntity> selectByDate(@Param("accessDate") LocalDate accessDate);

    /**
     * 查询指定时间范围内的访问记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 访问记录列表
     */
    @Select("SELECT * FROM t_visitor_record WHERE access_time >= #{startTime} AND access_time <= #{endTime} AND deleted_flag = 0 ORDER BY access_time DESC")
    List<VisitorRecordEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 查询今日访问记录
     *
     * @return 今日访问记录列表
     */
    @Select("SELECT * FROM t_visitor_record WHERE DATE(access_time) = CURDATE() AND deleted_flag = 0 ORDER BY access_time DESC")
    List<VisitorRecordEntity> selectTodayRecords();

    /**
     * 查询访问失败的记录
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 访问失败记录列表
     */
    @Select("SELECT * FROM t_visitor_record WHERE access_result = 1 AND DATE(access_time) >= #{startDate} AND DATE(access_time) <= #{endDate} AND deleted_flag = 0 ORDER BY access_time DESC")
    List<VisitorRecordEntity> selectFailedRecords(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * 统计指定日期的访问数量
     *
     * @param accessDate 访问日期
     * @return 访问数量
     */
    @Select("SELECT COUNT(*) FROM t_visitor_record WHERE DATE(access_time) = #{accessDate} AND deleted_flag = 0")
    Integer countByDate(@Param("accessDate") LocalDate accessDate);

    /**
     * 统计指定日期的成功访问数量
     *
     * @param accessDate 访问日期
     * @return 成功访问数量
     */
    @Select("SELECT COUNT(*) FROM t_visitor_record WHERE access_result = 0 AND DATE(access_time) = #{accessDate} AND deleted_flag = 0")
    Integer countSuccessByDate(@Param("accessDate") LocalDate accessDate);

    /**
     * 统计指定日期的失败访问数量
     *
     * @param accessDate 访问日期
     * @return 失败访问数量
     */
    @Select("SELECT COUNT(*) FROM t_visitor_record WHERE access_result = 1 AND DATE(access_time) = #{accessDate} AND deleted_flag = 0")
    Integer countFailedByDate(@Param("accessDate") LocalDate accessDate);

    /**
     * 统计各区域的访问数量
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 区域访问统计
     */
    @Select("SELECT visit_area_id, visit_area_name, COUNT(*) as visit_count " +
            "FROM t_visitor_record " +
            "WHERE DATE(access_time) >= #{startDate} AND DATE(access_time) <= #{endDate} AND deleted_flag = 0 " +
            "GROUP BY visit_area_id, visit_area_name " +
            "ORDER BY visit_count DESC")
    List<Map<String, Object>> selectAreaStatistics(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * 统计各设备的访问数量
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 设备访问统计
     */
    @Select("SELECT access_device_id, access_device_name, COUNT(*) as access_count " +
            "FROM t_visitor_record " +
            "WHERE DATE(access_time) >= #{startDate} AND DATE(access_time) <= #{endDate} AND access_device_id IS NOT NULL AND deleted_flag = 0 " +
            "GROUP BY access_device_id, access_device_name " +
            "ORDER BY access_count DESC")
    List<Map<String, Object>> selectDeviceStatistics(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 统计各通行方式的访问数量
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 通行方式统计
     */
    @Select("SELECT access_method, COUNT(*) as method_count " +
            "FROM t_visitor_record " +
            "WHERE DATE(access_time) >= #{startDate} AND DATE(access_time) <= #{endDate} AND deleted_flag = 0 " +
            "GROUP BY access_method " +
            "ORDER BY method_count DESC")
    List<Map<String, Object>> selectMethodStatistics(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 查询访问频率异常的访客（短时间多次访问）
     *
     * @param timeWindow 时间窗口（分钟）
     * @param threshold  阈值
     * @return 异常访问记录
     */
    @Select("SELECT visitor_phone, visitor_name, COUNT(*) as access_count " +
            "FROM t_visitor_record " +
            "WHERE access_time >= DATE_SUB(NOW(), INTERVAL #{timeWindow} MINUTE) AND deleted_flag = 0 " +
            "GROUP BY visitor_phone, visitor_name " +
            "HAVING COUNT(*) >= #{threshold} " +
            "ORDER BY access_count DESC")
    List<Map<String, Object>> selectAbnormalAccess(@Param("timeWindow") Integer timeWindow,
                                                   @Param("threshold") Integer threshold);

    /**
     * 查询最近7天的访问趋势
     *
     * @return 访问趋势数据
     */
    @Select("SELECT DATE(access_time) as access_date, " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN access_result = 0 THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN access_result = 1 THEN 1 ELSE 0 END) as failed_count " +
            "FROM t_visitor_record " +
            "WHERE access_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) AND deleted_flag = 0 " +
            "GROUP BY DATE(access_time) " +
            "ORDER BY access_date ASC")
    List<Map<String, Object>> selectRecentTrend();

    /**
     * 查询访客的首次访问记录
     *
     * @param visitorPhone 访客手机号
     * @return 首次访问记录
     */
    @Select("SELECT * FROM t_visitor_record WHERE visitor_phone = #{visitorPhone} AND deleted_flag = 0 ORDER BY access_time ASC LIMIT 1")
    VisitorRecordEntity selectFirstVisit(@Param("visitorPhone") String visitorPhone);

    /**
     * 查询访客的最近访问记录
     *
     * @param visitorPhone 访客手机号
     * @return 最近访问记录
     */
    @Select("SELECT * FROM t_visitor_record WHERE visitor_phone = #{visitorPhone} AND deleted_flag = 0 ORDER BY access_time DESC LIMIT 1")
    VisitorRecordEntity selectLastVisit(@Param("visitorPhone") String visitorPhone);
}