package net.lab1024.sa.oa.workflow.approval;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批记录数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
@Mapper
public interface ApprovalRecordDao extends BaseMapper<ApprovalRecordEntity> {

    @Select("SELECT * FROM t_workflow_approval_record " +
            "WHERE process_instance_id = #{processInstanceId} AND deleted_flag = 0 " +
            "ORDER BY approval_time ASC")
    List<ApprovalRecordEntity> findByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    @Select("SELECT * FROM t_workflow_approval_record " +
            "WHERE approver_id = #{approverId} AND deleted_flag = 0 " +
            "ORDER BY approval_time DESC")
    List<ApprovalRecordEntity> findByApproverId(@Param("approverId") Long approverId);
}
