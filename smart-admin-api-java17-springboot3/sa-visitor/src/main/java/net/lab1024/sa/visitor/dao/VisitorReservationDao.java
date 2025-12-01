package net.lab1024.sa.visitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.visitor.domain.entity.VisitorReservationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客预约DAO接口
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
public interface VisitorReservationDao extends BaseMapper<VisitorReservationEntity> {

    /**
     * 根据预约编号查询预约
     *
     * @param reservationCode 预约编号
     * @return 访客预约实体
     */
    @Select("SELECT * FROM t_visitor_reservation WHERE reservation_code = #{reservationCode} AND deleted_flag = 0")
    VisitorReservationEntity selectByReservationCode(@Param("reservationCode") String reservationCode);

    /**
     * 根据访客手机号查询预约列表
     *
     * @param visitorPhone 访客手机号
     * @param limit        查询数量限制
     * @return 访客预约列表
     */
    @Select("SELECT * FROM t_visitor_reservation WHERE visitor_phone = #{visitorPhone} AND deleted_flag = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<VisitorReservationEntity> selectByVisitorPhone(@Param("visitorPhone") String visitorPhone, @Param("limit") Integer limit);

    /**
     * 根据接待人ID查询预约列表
     *
     * @param hostUserId 接待人ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 访客预约列表
     */
    @Select("SELECT * FROM t_visitor_reservation WHERE host_user_id = #{hostUserId} AND visit_date >= #{startDate} AND visit_date <= #{endDate} AND deleted_flag = 0 ORDER BY visit_date DESC, visit_start_time DESC")
    List<VisitorReservationEntity> selectByHostUserIdAndDateRange(@Param("hostUserId") Long hostUserId,
                                                                   @Param("startDate") LocalDate startDate,
                                                                   @Param("endDate") LocalDate endDate);

    /**
     * 根据审批状态查询预约列表
     *
     * @param approvalStatus 审批状态
     * @param limit          查询数量限制
     * @return 访客预约列表
     */
    @Select("SELECT * FROM t_visitor_reservation WHERE approval_status = #{approvalStatus} AND deleted_flag = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<VisitorReservationEntity> selectByApprovalStatus(@Param("approvalStatus") Integer approvalStatus, @Param("limit") Integer limit);

    /**
     * 查询待审批的预约数量
     *
     * @return 待审批数量
     */
    @Select("SELECT COUNT(*) FROM t_visitor_reservation WHERE approval_status = 0 AND deleted_flag = 0")
    Integer countPendingApproval();

    /**
     * 查询指定日期的预约数量
     *
     * @param visitDate 访问日期
     * @return 预约数量
     */
    @Select("SELECT COUNT(*) FROM t_visitor_reservation WHERE visit_date = #{visitDate} AND approval_status = 1 AND deleted_flag = 0")
    Integer countByVisitDate(@Param("visitDate") LocalDate visitDate);

    /**
     * 查询指定接待人今日预约数量
     *
     * @param hostUserId 接待人ID
     * @param visitDate  访问日期
     * @return 预约数量
     */
    @Select("SELECT COUNT(*) FROM t_visitor_reservation WHERE host_user_id = #{hostUserId} AND visit_date = #{visitDate} AND approval_status = 1 AND deleted_flag = 0")
    Integer countByHostUserIdAndDate(@Param("hostUserId") Long hostUserId, @Param("visitDate") LocalDate visitDate);

    /**
     * 查询即将过期的预约（二维码过期时间在指定时间内）
     *
     * @param beforeTime 时间点
     * @param afterTime  时间点
     * @return 即将过期的预约列表
     */
    @Select("SELECT * FROM t_visitor_reservation WHERE qr_code_expire_time > #{beforeTime} AND qr_code_expire_time <= #{afterTime} AND approval_status = 1 AND access_status IN (0,1) AND deleted_flag = 0")
    List<VisitorReservationEntity> selectExpiringSoon(@Param("beforeTime") LocalDateTime beforeTime, @Param("afterTime") LocalDateTime afterTime);

    /**
     * 查询已过期的预约
     *
     * @param expireTime 过期时间点
     * @return 已过期的预约列表
     */
    @Select("SELECT * FROM t_visitor_reservation WHERE visit_date < #{expireDate} OR (visit_date = #{expireDate} AND visit_end_time < #{expireTime}) AND approval_status = 1 AND access_status != 3 AND deleted_flag = 0")
    List<VisitorReservationEntity> selectExpired(@Param("expireDate") LocalDate expireDate, @Param("expireTime") String expireTime);

    /**
     * 根据访客身份证号查询预约
     *
     * @param visitorIdCard 访客身份证号
     * @return 访客预约实体
     */
    @Select("SELECT * FROM t_visitor_reservation WHERE visitor_id_card = #{visitorIdCard} AND deleted_flag = 0 ORDER BY create_time DESC LIMIT 1")
    VisitorReservationEntity selectByIdCard(@Param("visitorIdCard") String visitorIdCard);

    /**
     * 查询指定时间范围内的预约统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 预约统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN approval_status = 0 THEN 1 ELSE 0 END) as pending_count, " +
            "SUM(CASE WHEN approval_status = 1 THEN 1 ELSE 0 END) as approved_count, " +
            "SUM(CASE WHEN approval_status = 2 THEN 1 ELSE 0 END) as rejected_count " +
            "FROM t_visitor_reservation WHERE visit_date >= #{startDate} AND visit_date <= #{endDate} AND deleted_flag = 0")
    java.util.Map<String, Object> selectStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询重复预约（同一访客同一日期）
     *
     * @param visitorPhone 访客手机号
     * @param visitDate    访问日期
     * @param excludeId    排除的预约ID
     * @return 重复预约数量
     */
    @Select("SELECT COUNT(*) FROM t_visitor_reservation WHERE visitor_phone = #{visitorPhone} AND visit_date = #{visitDate} AND approval_status = 1 AND reservation_id != #{excludeId} AND deleted_flag = 0")
    Integer countDuplicateReservations(@Param("visitorPhone") String visitorPhone,
                                      @Param("visitDate") LocalDate visitDate,
                                      @Param("excludeId") Long excludeId);

    /**
     * 查询今日待接待的预约列表
     *
     * @param hostUserId 接待人ID
     * @param visitDate  访问日期
     * @return 预约列表
     */
    @Select("SELECT * FROM t_visitor_reservation WHERE host_user_id = #{hostUserId} AND visit_date = #{visitDate} AND approval_status = 1 AND access_status IN (0,1) AND deleted_flag = 0 ORDER BY visit_start_time ASC")
    List<VisitorReservationEntity> selectTodayReceptions(@Param("hostUserId") Long hostUserId, @Param("visitDate") LocalDate visitDate);
}