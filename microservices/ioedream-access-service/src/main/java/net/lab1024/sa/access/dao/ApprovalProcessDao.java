package net.lab1024.sa.access.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.access.entity.ApprovalProcessEntity;

/**
 * 审批流程DAO
 * 严格遵循repowiki规范:
 * - 使用MyBatis-Plus BaseMapper
 * - 接口以Dao结尾
 * - 只负责数据访问，不包含业务逻辑
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 */
@Mapper
public interface ApprovalProcessDao extends BaseMapper<ApprovalProcessEntity> {

    /**
     * 根据流程编号查询流程
     *
     * @param processNo 流程编号
     * @return 流程实体
     */
    @Select("SELECT * FROM access_approval_process WHERE process_no = #{processNo} AND deleted_flag = 0")
    ApprovalProcessEntity selectByProcessNo(@Param("processNo") String processNo);

    /**
     * 根据申请人查询流程列表
     *
     * @param applicantId 申请人ID
     * @param status      状态(可选)
     * @param startTime   开始时间(可选)
     * @param endTime     结束时间(可选)
     * @return 流程列表
     */
    @Select("<script>" +
            "SELECT * FROM access_approval_process WHERE applicant_id = #{applicantId} AND deleted_flag = 0" +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "<if test='startTime != null'> AND created_time >= #{startTime} </if>" +
            "<if test='endTime != null'> AND created_time &lt;= #{endTime} </if>" +
            "ORDER BY created_time DESC" +
            "</script>")
    List<ApprovalProcessEntity> selectByApplicantId(@Param("applicantId") Long applicantId,
            @Param("status") String status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据审批人查询待审批流程
     *
     * @param approverId 审批人ID
     * @return 待审批流程列表
     */
    @Select("SELECT p.* FROM access_approval_process p " +
            "INNER JOIN access_approval_step s ON p.process_id = s.process_id " +
            "WHERE s.approver_id = #{approverId} AND s.step_status = 'PENDING' " +
            "AND p.status IN ('PENDING', 'IN_PROGRESS') AND p.deleted_flag = 0 " +
            "ORDER BY p.priority DESC, p.created_time ASC")
    List<ApprovalProcessEntity> selectPendingByApproverId(@Param("approverId") Long approverId);

    /**
     * 根据状态查询流程数量
     *
     * @param status 状态
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM access_approval_process WHERE status = #{status} AND deleted_flag = 0")
    Integer countByStatus(@Param("status") String status);

    /**
     * 根据区域查询流程数量
     *
     * @param areaId 区域ID
     * @param status 状态(可选)
     * @return 数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM access_approval_process WHERE area_id = #{areaId} AND deleted_flag = 0" +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "</script>")
    Integer countByAreaId(@Param("areaId") Long areaId,
            @Param("status") String status);

    /**
     * 更新流程状态
     *
     * @param processId 流程ID
     * @param status    新状态
     * @param version   版本号
     * @return 影响行数
     */
    @Update("UPDATE access_approval_process SET status = #{status}, updated_time = NOW(), version = version + 1 " +
            "WHERE process_id = #{processId} AND version = #{version}")
    Integer updateStatus(@Param("processId") Long processId,
            @Param("status") String status,
            @Param("version") Integer version);

    /**
     * 更新流程当前步骤
     *
     * @param processId   流程ID
     * @param currentStep 当前步骤
     * @param version     版本号
     * @return 影响行数
     */
    @Update("UPDATE access_approval_process SET current_step = #{currentStep}, updated_time = NOW(), version = version + 1 "
            +
            "WHERE process_id = #{processId} AND version = #{version}")
    Integer updateCurrentStep(@Param("processId") Long processId,
            @Param("currentStep") Integer currentStep,
            @Param("version") Integer version);

    /**
     * 完成审批流程
     *
     * @param processId         流程ID
     * @param status            最终状态
     * @param approvalComment   审批意见
     * @param finalApproverId   最终审批人ID
     * @param finalApproverName 最终审批人姓名
     * @param approvalTime      审批时间
     * @param version           版本号
     * @return 影响行数
     */
    @Update("UPDATE access_approval_process SET " +
            "status = #{status}, " +
            "approval_comment = #{approvalComment}, " +
            "final_approver_id = #{finalApproverId}, " +
            "final_approver_name = #{finalApproverName}, " +
            "approval_time = #{approvalTime}, " +
            "updated_time = NOW(), " +
            "version = version + 1 " +
            "WHERE process_id = #{processId} AND version = #{version}")
    Integer completeApproval(@Param("processId") Long processId,
            @Param("status") String status,
            @Param("approvalComment") String approvalComment,
            @Param("finalApproverId") Long finalApproverId,
            @Param("finalApproverName") String finalApproverName,
            @Param("approvalTime") LocalDateTime approvalTime,
            @Param("version") Integer version);

    /**
     * 软删除流程
     *
     * @param processId 流程ID
     * @param updatedBy 更新人ID
     * @return 影响行数
     */
    @Update("UPDATE access_approval_process SET deleted_flag = 1, updated_by = #{updatedBy}, updated_time = NOW() " +
            "WHERE process_id = #{processId}")
    Integer softDelete(@Param("processId") Long processId,
            @Param("updatedBy") Long updatedBy);

    /**
     * 查询超时的流程
     *
     * @param currentTime 当前时间
     * @return 超时流程列表
     */
    @Select("SELECT p.* FROM access_approval_process p " +
            "INNER JOIN access_approval_step s ON p.process_id = s.process_id " +
            "WHERE s.step_status = 'PENDING' " +
            "AND s.timeout_time < #{currentTime} " +
            "AND p.status IN ('PENDING', 'IN_PROGRESS') " +
            "AND p.deleted_flag = 0")
    List<ApprovalProcessEntity> selectTimeoutProcesses(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 统计各状态的流程数量
     *
     * @return 统计结果
     */
    @Select("SELECT status, COUNT(*) as count FROM access_approval_process WHERE deleted_flag = 0 GROUP BY status")
    List<java.util.Map<String, Object>> countByStatusGroup();

    /**
     * 查询即将过期的流程
     *
     * @param currentTime 当前时间
     * @param hours       小时数
     * @return 即将过期流程列表
     */
    @Select("SELECT * FROM access_approval_process WHERE " +
            "status IN ('PENDING', 'IN_PROGRESS') " +
            "AND expire_time IS NOT NULL " +
            "AND expire_time BETWEEN #{currentTime} AND DATE_ADD(#{currentTime}, INTERVAL #{hours} HOUR) " +
            "AND deleted_flag = 0 " +
            "ORDER BY expire_time ASC")
    List<ApprovalProcessEntity> selectExpiringSoon(@Param("currentTime") LocalDateTime currentTime,
            @Param("hours") Integer hours);
}
