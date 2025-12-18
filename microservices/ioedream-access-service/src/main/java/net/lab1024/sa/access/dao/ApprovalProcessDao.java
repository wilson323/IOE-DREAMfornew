package net.lab1024.sa.access.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.access.entity.ApprovalProcessEntity;

/**
 * 瀹℃壒娴佺▼DAO
 * 涓ユ牸閬靛惊repowiki瑙勮寖:
 * - 浣跨敤MyBatis-Plus BaseMapper
 * - 鎺ュ彛浠ao缁撳熬
 * - 鍙礋璐ｆ暟鎹闂紝涓嶅寘鍚笟鍔￠€昏緫
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 */
@Mapper
public interface ApprovalProcessDao extends BaseMapper<ApprovalProcessEntity> {

    /**
     * 鏍规嵁娴佺▼缂栧彿鏌ヨ娴佺▼
     *
     * @param processNo 娴佺▼缂栧彿
     * @return 娴佺▼瀹炰綋
     */
    @Select("SELECT * FROM access_approval_process WHERE process_no = #{processNo} AND deleted_flag = 0")
    ApprovalProcessEntity selectByProcessNo(@Param("processNo") String processNo);

    /**
     * 鏍规嵁鐢宠浜烘煡璇㈡祦绋嬪垪琛?     *
     * @param applicantId 鐢宠浜篒D
     * @param status      鐘舵€?鍙€?
     * @param startTime   寮€濮嬫椂闂?鍙€?
     * @param endTime     缁撴潫鏃堕棿(鍙€?
     * @return 娴佺▼鍒楄〃
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
     * 鏍规嵁瀹℃壒浜烘煡璇㈠緟瀹℃壒娴佺▼
     *
     * @param approverId 瀹℃壒浜篒D
     * @return 寰呭鎵规祦绋嬪垪琛?     */
    @Select("SELECT p.* FROM access_approval_process p " +
            "INNER JOIN access_approval_step s ON p.process_id = s.process_id " +
            "WHERE s.approver_id = #{approverId} AND s.step_status = 'PENDING' " +
            "AND p.status IN ('PENDING', 'IN_PROGRESS') AND p.deleted_flag = 0 " +
            "ORDER BY p.priority DESC, p.created_time ASC")
    List<ApprovalProcessEntity> selectPendingByApproverId(@Param("approverId") Long approverId);

    /**
     * 鏍规嵁鐘舵€佹煡璇㈡祦绋嬫暟閲?     *
     * @param status 鐘舵€?     * @return 鏁伴噺
     */
    @Select("SELECT COUNT(*) FROM access_approval_process WHERE status = #{status} AND deleted_flag = 0")
    Integer countByStatus(@Param("status") String status);

    /**
     * 鏍规嵁鍖哄煙鏌ヨ娴佺▼鏁伴噺
     *
     * @param areaId 鍖哄煙ID
     * @param status 鐘舵€?鍙€?
     * @return 鏁伴噺
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM access_approval_process WHERE area_id = #{areaId} AND deleted_flag = 0" +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "</script>")
    Integer countByAreaId(@Param("areaId") Long areaId,
            @Param("status") String status);

    /**
     * 鏇存柊娴佺▼鐘舵€?     *
     * @param processId 娴佺▼ID
     * @param status    鏂扮姸鎬?     * @param version   鐗堟湰鍙?     * @return 褰卞搷琛屾暟
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE access_approval_process SET status = #{status}, updated_time = NOW(), version = version + 1 " +
            "WHERE process_id = #{processId} AND version = #{version}")
    Integer updateStatus(@Param("processId") Long processId,
            @Param("status") String status,
            @Param("version") Integer version);

    /**
     * 鏇存柊娴佺▼褰撳墠姝ラ
     *
     * @param processId   娴佺▼ID
     * @param currentStep 褰撳墠姝ラ
     * @param version     鐗堟湰鍙?     * @return 褰卞搷琛屾暟
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE access_approval_process SET current_step = #{currentStep}, updated_time = NOW(), version = version + 1 "
            +
            "WHERE process_id = #{processId} AND version = #{version}")
    Integer updateCurrentStep(@Param("processId") Long processId,
            @Param("currentStep") Integer currentStep,
            @Param("version") Integer version);

    /**
     * 瀹屾垚瀹℃壒娴佺▼
     *
     * @param processId         娴佺▼ID
     * @param status            鏈€缁堢姸鎬?     * @param approvalComment   瀹℃壒鎰忚
     * @param finalApproverId   鏈€缁堝鎵逛汉ID
     * @param finalApproverName 鏈€缁堝鎵逛汉濮撳悕
     * @param approvalTime      瀹℃壒鏃堕棿
     * @param version           鐗堟湰鍙?     * @return 褰卞搷琛屾暟
     */
    @Transactional(rollbackFor = Exception.class)
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
     * 杞垹闄ゆ祦绋?     *
     * @param processId 娴佺▼ID
     * @param updatedBy 鏇存柊浜篒D
     * @return 褰卞搷琛屾暟
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE access_approval_process SET deleted_flag = 1, updated_by = #{updatedBy}, updated_time = NOW() " +
            "WHERE process_id = #{processId}")
    Integer softDelete(@Param("processId") Long processId,
            @Param("updatedBy") Long updatedBy);

    /**
     * 鏌ヨ瓒呮椂鐨勬祦绋?     *
     * @param currentTime 褰撳墠鏃堕棿
     * @return 瓒呮椂娴佺▼鍒楄〃
     */
    @Select("SELECT p.* FROM access_approval_process p " +
            "INNER JOIN access_approval_step s ON p.process_id = s.process_id " +
            "WHERE s.step_status = 'PENDING' " +
            "AND s.timeout_time < #{currentTime} " +
            "AND p.status IN ('PENDING', 'IN_PROGRESS') " +
            "AND p.deleted_flag = 0")
    List<ApprovalProcessEntity> selectTimeoutProcesses(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 缁熻鍚勭姸鎬佺殑娴佺▼鏁伴噺
     *
     * @return 缁熻缁撴灉
     */
    @Select("SELECT status, COUNT(*) as count FROM access_approval_process WHERE deleted_flag = 0 GROUP BY status")
    List<java.util.Map<String, Object>> countByStatusGroup();

    /**
     * 鏌ヨ鍗冲皢杩囨湡鐨勬祦绋?     *
     * @param currentTime 褰撳墠鏃堕棿
     * @param hours       灏忔椂鏁?     * @return 鍗冲皢杩囨湡娴佺▼鍒楄〃
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

