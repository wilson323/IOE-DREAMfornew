package net.lab1024.sa.visitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.visitor.domain.entity.VisitorAppointmentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客预约数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@Mapper
@Repository
public interface VisitorAppointmentDao extends BaseMapper<VisitorAppointmentEntity> {

    /**
     * 查询访客预约列表
     *
     * @param visitorId 访客ID
     * @return 预约列表
     */
    List<VisitorAppointmentEntity> selectByVisitorId(@Param("visitorId") Long visitorId);

    /**
     * 查询待审核的预约
     *
     * @param approverId 审批人ID
     * @return 预约列表
     */
    List<VisitorAppointmentEntity> selectPendingApproval(@Param("approverId") Long approverId);

    /**
     * 查询指定时间段的预约
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 预约列表
     */
    List<VisitorAppointmentEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查询被访人相关的预约
     *
     * @param visiteeId 被访人ID
     * @param status 状态
     * @return 预约列表
     */
    List<VisitorAppointmentEntity> selectByVisiteeId(@Param("visiteeId") Long visiteeId,
                                                     @Param("status") Integer status);

    /**
     * 统计预约数量（按状态）
     *
     * @param status 状态
     * @return 数量
     */
    Long countByStatus(@Param("status") Integer status);

    /**
     * 批量审批预约
     *
     * @param appointmentIds 预约ID列表
     * @param status 状态
     * @param approverId 审批人ID
     * @param approvalComment 审批意见
     * @return 影响行数
     */
    int batchApprove(@Param("appointmentIds") List<Long> appointmentIds,
                      @Param("status") Integer status,
                      @Param("approverId") Long approverId,
                      @Param("approvalComment") String approvalComment);
}