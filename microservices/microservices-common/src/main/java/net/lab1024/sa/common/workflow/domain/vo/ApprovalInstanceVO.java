package net.lab1024.sa.common.workflow.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 审批流程实例视图对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - VO类用于数据传输，不包含业务逻辑
 * - 字段命名清晰，符合业务语义
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
public class ApprovalInstanceVO {

    /**
     * 实例ID
     */
    private Long instanceId;

    /**
     * 流程定义ID
     */
    private Long processDefinitionId;

    /**
     * 流程编码
     */
    private String processKey;

    /**
     * 流程名称
     */
    private String processName;

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
     * 状态（1-运行中 2-已完成 3-已终止 4-已挂起）
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 持续时间（毫秒）
     */
    private Long duration;

    /**
     * 原因/备注
     */
    private String reason;
}

