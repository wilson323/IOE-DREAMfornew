package net.lab1024.sa.admin.module.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import net.lab1024.sa.admin.module.access.domain.entity.VisitorReservationEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * 访客预约数据访问对象
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
public interface VisitorReservationDao extends BaseMapper<VisitorReservationEntity> {

    /**
     * 根据访客手机号和访问日期查询预约数量
     *
     * @param visitorPhone 访客手机号
     * @param visitDate     访问日期
     * @param excludeId     排除的预约ID
     * @return 预约数量
     */
    int countByPhoneAndDate(@Param("visitorPhone") String visitorPhone,
                            @Param("visitDate") LocalDate visitDate,
                            @Param("excludeId") Long excludeId);

    /**
     * 根据审批状态查询预约列表
     *
     * @param approvalStatus 审批状态
     * @param limit          查询数量限制
     * @return 预约列表
     */
    List<VisitorReservationEntity> selectByApprovalStatus(@Param("approvalStatus") Integer approvalStatus,
                                                          @Param("limit") Integer limit);

    /**
     * 根据接待人ID查询预约列表
     *
     * @param hostUserId 接待人ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 预约列表
     */
    List<VisitorReservationEntity> selectByHostUserId(@Param("hostUserId") Long hostUserId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 查询即将到期的预约
     *
     * @param hours 小时数
     * @param limit  查询数量限制
     * @return 预约列表
     */
    List<VisitorReservationEntity> selectExpiringSoon(@Param("hours") Integer hours,
                                                      @Param("limit") Integer limit);

    /**
     * 批量更新预约状态
     *
     * @param reservationIds 预约ID列表
     * @param approvalStatus 审批状态
     * @return 更新数量
     */
    int batchUpdateApprovalStatus(@Param("reservationIds") List<Long> reservationIds,
                                  @Param("approvalStatus") Integer approvalStatus);

    /**
     * 查询访客访问统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计结果
     */
    List<VisitorReservationEntity> selectVisitorStatistics(@Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    /**
     * 查询热门接待人
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     查询数量限制
     * @return 接待人列表
     */
    List<VisitorReservationEntity> selectPopularHosts(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate,
                                                     @Param("limit") Integer limit);

    /**
     * 清理过期的预约数据
     *
     * @param expireDays 过期天数
     * @return 清理数量
     */
    int cleanExpiredReservations(@Param("expireDays") Integer expireDays);
}