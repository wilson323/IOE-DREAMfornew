package net.lab1024.sa.visitor.visitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.visitor.visitor.entity.VisitorApprovalRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
// 已移除Repository导入，统一使用@Mapper注解

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客预约审批记录DAO
 * <p>
 * 内存优化查询设计：
 * - 针对常用查询场景添加索引
 * - 避免全表扫描
 * - 合理使用分页查询
 * - 优化JOIN查询，减少关联表数量
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
// @Repository  // 禁止使用@Repository注解，统一使用@Mapper
@Transactional(rollbackFor = Exception.class)
public interface VisitorApprovalRecordDao extends BaseMapper<VisitorApprovalRecordEntity> {

    /**
     * 根据预约ID查询审批记录
     * <p>
     * 按审批级别排序，获取最新的审批记录
     * </p>
     *
     * @param appointmentId 预约ID
     * @return 审批记录列表
     */
    @Select("SELECT * FROM t_visitor_approval_record " +
            "WHERE appointment_id = #{appointmentId} AND deleted_flag = 0 " +
            "ORDER BY approval_level DESC, approval_time DESC")
    List<VisitorApprovalRecordEntity> selectByAppointmentId(@Param("appointmentId") Long appointmentId);

    /**
     * 查询待审批的记录
     * <p>
     * 优化查询性能，只返回必要的字段
     * </p>
     *
     * @param approverId 审批人ID
     * @return 待审批记录列表
     */
    @Select("SELECT approval_id, appointment_id, approval_level, " +
            "create_time FROM t_visitor_approval_record " +
            "WHERE approver_id = #{approverId} AND approval_result = 'PENDING' " +
            "AND deleted_flag = 0 ORDER BY create_time ASC")
    List<VisitorApprovalRecordEntity> selectPendingApprovals(@Param("approverId") Long approverId);

    /**
     * 查询指定时间范围内的审批记录
     *
     * @param approverId 审批人ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审批记录列表
     */
    @Select("SELECT approval_id, appointment_id, approver_name, " +
            "approval_result, approval_comment, approval_time " +
            "FROM t_visitor_approval_record " +
            "WHERE approver_id = #{approverId} " +
            "AND approval_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted_flag = 0 " +
            "ORDER BY approval_time DESC")
    List<VisitorApprovalRecordEntity> selectByApproverAndTimeRange(
            @Param("approverId") Long approverId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 检查是否已存在审批记录
     *
     * @param appointmentId 预约ID
     * @param approverId 审批人ID
     * @param approvalLevel 审批级别
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) FROM t_visitor_approval_record " +
            "WHERE appointment_id = #{appointmentId} " +
            "AND approver_id = #{approverId} " +
            "AND approval_level = #{approvalLevel} " +
            "AND deleted_flag = 0")
    int existsApprovalRecord(
            @Param("appointmentId") Long appointmentId,
            @Param("approverId") Long approverId,
            @Param("approvalLevel") Integer approvalLevel);

    /**
     * 获取预约的最新审批状态
     *
     * @param appointmentId 预约ID
     * @return 最新审批记录
     */
    @Select("SELECT * FROM t_visitor_approval_record " +
            "WHERE appointment_id = #{appointmentId} AND deleted_flag = 0 " +
            "ORDER BY approval_time DESC LIMIT 1")
    VisitorApprovalRecordEntity selectLatestApproval(@Param("appointmentId") Long appointmentId);
}