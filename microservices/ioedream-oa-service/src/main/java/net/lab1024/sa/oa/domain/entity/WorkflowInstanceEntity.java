package net.lab1024.sa.oa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
     * 实例状态：1-运行中, 2-已完成, 3-已终止, 4-已挂起
     */
    private Integer status;

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

    // --- Added Fields for Service Compatibility ---

    /**
     * Flowable 实例ID
     */
    private String flowableInstanceId;

    /**
     * Flowable 流程定义ID
     */
    private String flowableProcessDefId;

    /**
     * 流程变量 (JSON)
     */
    private String variables;

    /**
     * 当前审批人ID
     */
    private Long currentApproverId;

    /**
     * 当前审批人姓名
     */
    private String currentApproverName;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 耗时(分钟)
     */
    private Long duration;

    /**
     * 结束原因
     */
    private String reason;

    /**
     * 删除标记
     */
    private Integer deletedFlag;

    // --- Compatibility Methods ---

    public String getProcessName() {
        return this.title;
    }

    public void setProcessName(String processName) {
        this.title = processName;
    }

    public Long getProcessDefinitionId() {
        return this.definitionId;
    }

    public void setProcessDefinitionId(Long processDefinitionId) {
        this.definitionId = processDefinitionId;
    }

    public String getProcessKey() {
        return this.definitionCode;
    }

    public void setProcessKey(String processKey) {
        this.definitionCode = processKey;
    }
}
