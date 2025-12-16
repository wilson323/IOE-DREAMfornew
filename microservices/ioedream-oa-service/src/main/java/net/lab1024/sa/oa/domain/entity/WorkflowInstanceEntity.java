package net.lab1024.sa.oa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流实例实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
@TableName("t_workflow_instance")
public class WorkflowInstanceEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 实例编号
     */
    private String instanceNo;

    /**
     * 流程定义ID
     */
    private Long definitionId;

    /**
     * 流程定义编码
     */
    private String definitionCode;

    /**
     * 流程标题
     */
    private String title;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 发起人姓名
     */
    private String initiatorName;

    /**
     * 当前节点ID
     */
    private String currentNodeId;

    /**
     * 当前节点名称
     */
    private String currentNodeName;

    /**
     * 实例状态：RUNNING-运行中, APPROVED-已通过, REJECTED-已拒绝, CANCELLED-已取消, TIMEOUT-已超时
     */
    private String status;

    /**
     * 发起时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 表单数据JSON
     */
    private String formDataJson;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
