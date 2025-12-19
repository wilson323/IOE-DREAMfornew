package net.lab1024.sa.common.visitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.visitor.entity.LogisticsReservationEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
// 已移除Repository导入，统一使用@Mapper注解

import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流预约DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Mapper
// @Repository  // 禁止使用@Repository注解，统一使用@Mapper
public interface LogisticsReservationDao extends BaseMapper<LogisticsReservationEntity> {

    /**
     * 根据预约编号查询
     *
     * @param reservationCode 预约编号
     * @return 预约实体
     */
    @Select("SELECT * FROM t_logistics_reservation WHERE reservation_code = #{reservationCode} " +
            "AND deleted_flag = 0")
    LogisticsReservationEntity selectByCode(@Param("reservationCode") String reservationCode);

    /**
     * 根据车牌号查询预约
     *
     * @param plateNumber 车牌号
     * @param limit 限制数量
     * @return 预约列表
     */
    @Select("SELECT * FROM t_logistics_reservation WHERE plate_number = #{plateNumber} " +
            "AND deleted_flag = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<LogisticsReservationEntity> selectByPlateNumber(
            @Param("plateNumber") String plateNumber,
            @Param("limit") Integer limit);

    /**
     * 根据司机姓名查询预约
     *
     * @param driverName 司机姓名
     * @param limit 限制数量
     * @return 预约列表
     */
    @Select("SELECT * FROM t_logistics_reservation WHERE driver_name = #{driverName} " +
            "AND deleted_flag = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<LogisticsReservationEntity> selectByDriverName(
            @Param("driverName") String driverName,
            @Param("limit") Integer limit);

    /**
     * 根据联系电话查询预约
     *
     * @param contactPhone 联系电话
     * @param limit 限制数量
     * @return 预约列表
     */
    @Select("SELECT * FROM t_logistics_reservation WHERE contact_phone = #{contactPhone} " +
            "AND deleted_flag = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<LogisticsReservationEntity> selectByContactPhone(
            @Param("contactPhone") String contactPhone,
            @Param("limit") Integer limit);

    /**
     * 根据预约类型查询
     *
     * @param reservationType 预约类型
     * @param status 预约状态（可选）
     * @param limit 限制数量
     * @return 预约列表
     */
    @Select("<script>" +
            "SELECT * FROM t_logistics_reservation " +
            "WHERE reservation_type = #{reservationType} " +
            "AND deleted_flag = 0 " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "ORDER BY create_time DESC " +
            "<if test='limit != null and limit > 0'>LIMIT #{limit}</if>" +
            "</script>")
    List<LogisticsReservationEntity> selectByReservationType(
            @Param("reservationType") String reservationType,
            @Param("status") String status,
            @Param("limit") Integer limit);

    /**
     * 根据状态查询预约
     *
     * @param status 预约状态
     * @param limit 限制数量
     * @return 预约列表
     */
    @Select("SELECT * FROM t_logistics_reservation WHERE status = #{status} " +
            "AND deleted_flag = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<LogisticsReservationEntity> selectByStatus(
            @Param("status") String status,
            @Param("limit") Integer limit);

    /**
     * 根据作业区域查询预约
     *
     * @param operationAreaId 作业区域ID
     * @param limit 限制数量
     * @return 预约列表
     */
    @Select("SELECT * FROM t_logistics_reservation WHERE operation_area_id = #{operationAreaId} " +
            "AND deleted_flag = 0 ORDER BY expected_arrive_date, expected_arrive_time_start LIMIT #{limit}")
    List<LogisticsReservationEntity> selectByOperationArea(
            @Param("operationAreaId") Long operationAreaId,
            @Param("limit") Integer limit);

    /**
     * 根据时间范围查询预约
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param operationAreaId 作业区域ID（可选）
     * @param status 预约状态（可选）
     * @param limit 限制数量
     * @return 预约列表
     */
    @Select("<script>" +
            "SELECT * FROM t_logistics_reservation " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted_flag = 0 " +
            "<if test='operationAreaId != null'>AND operation_area_id = #{operationAreaId}</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "ORDER BY expected_arrive_date, expected_arrive_time_start " +
            "<if test='limit != null and limit > 0'>LIMIT #{limit}</if>" +
            "</script>")
    List<LogisticsReservationEntity> selectByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("operationAreaId") Long operationAreaId,
            @Param("status") String status,
            @Param("limit") Integer limit);

    /**
     * 查询当日有效的预约
     *
     * @param date 日期
     * @param limit 限制数量
     * @return 预约列表
     */
    @Select("SELECT * FROM t_logistics_reservation " +
            "WHERE DATE(expected_arrive_date) = #{date} " +
            "AND status = 'APPROVED' " +
            "AND deleted_flag = 0 " +
            "ORDER BY expected_arrive_time_start")
    List<LogisticsReservationEntity> selectTodayValidReservations(
            @Param("date") String date,
            @Param("limit") Integer limit);

    /**
     * 查询即将到期的预约
     *
     * @param hours 小时数
     * @param limit 限制数量
     * @return 预约列表
     */
    @Select("SELECT * FROM t_logistics_reservation " +
            "WHERE expected_arrive_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL #{hours} HOUR) " +
            "AND status = 'APPROVED' " +
            "AND deleted_flag = 0 " +
            "ORDER BY expected_arrive_date, expected_arrive_time_start " +
            "LIMIT #{limit}")
    List<LogisticsReservationEntity> selectUpcomingReservations(
            @Param("hours") Integer hours,
            @Param("limit") Integer limit);

    /**
     * 统计指定时间段的预约数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 预约状态（可选）
     * @return 预约数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_logistics_reservation " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted_flag = 0 " +
            "<if test='status != null'>AND status = #{status}</if>" +
            "</script>")
    Long countByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") String status);

    /**
     * 统计各状态的预约数量
     *
     * @return 状态统计Map
     */
    @Select("SELECT status, COUNT(*) as count FROM t_logistics_reservation " +
            "WHERE deleted_flag = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 根据被访人查询预约
     *
     * @param intervieweeId 被访人ID
     * @param limit 限制数量
     * @return 预约列表
     */
    @Select("SELECT * FROM t_logistics_reservation WHERE interviewee_id = #{intervieweeId} " +
            "AND deleted_flag = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<LogisticsReservationEntity> selectByIntervieweeId(
            @Param("intervieweeId") Long intervieweeId,
            @Param("limit") Integer limit);

    /**
     * 查询需要处理的预约（待审核、已通过等）
     *
     * @param statusList 状态列表
     * @param limit 限制数量
     * @return 预约列表
     */
    @Select("<script>" +
            "SELECT * FROM t_logistics_reservation " +
            "WHERE status IN " +
            "<foreach item='statusList' collection='statusList' item='status' open='(' separator=',' close=')'>" +
            "#{status}" +
            "</foreach>" +
            "AND deleted_flag = 0 " +
            "ORDER BY create_time ASC " +
            "<if test='limit != null and limit > 0'>LIMIT #{limit}</if>" +
            "</script>")
    List<LogisticsReservationEntity> selectForProcessing(
            @Param("statusList") List<String> statusList,
            @Param("limit") Integer limit);

    /**
     * 删除指定时间之前的记录
     *
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    @Transactional
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 批量更新预约状态
     *
     * @param reservationIds 预约ID列表
     * @param status 新状态
     * @param approveUser 审批人
     * @return 更新的记录数
     */
    @Transactional
    int updateStatusBatch(
            @Param("reservationIds") List<Long> reservationIds,
            @Param("status") String status,
            @Param("approveUser") String approveUser);

    /**
     * 查询重复的预约（相同车牌、相同日期）
     *
     * @param plateNumber 车牌号
     * @param arriveDate 到达日期
     * @param excludeId 排除的预约ID
     * @return 重复预约数量
     */
    @Select("SELECT COUNT(*) FROM t_logistics_reservation " +
            "WHERE plate_number = #{plateNumber} " +
            "AND expected_arrive_date = #{arriveDate} " +
            "AND status IN ('PENDING', 'APPROVED') " +
            "<if test='excludeId != null'>AND reservation_id != #{excludeId}</if> " +
            "AND deleted_flag = 0")
    int countDuplicateReservation(
            @Param("plateNumber") String plateNumber,
            @Param("arriveDate") String arriveDate,
            @Param("excludeId") Long excludeId);
}