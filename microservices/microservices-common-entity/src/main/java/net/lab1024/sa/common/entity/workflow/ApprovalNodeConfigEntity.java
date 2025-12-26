package net.lab1024.sa.common.entity.workflow;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 审批节点配置实体
 * <p>
 * 存储审批节点的配置信息，定义审批流程中的各个审批节点
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 * <p>
 * <strong>主要功能：</strong></p>
 * <ul>
 *   <li>审批节点配置管理</li>
 *   <li>审批节点顺序管理</li>
 *   <li>审批人配置</li>
 *   <li>审批规则配置</li>
 *   <li>审批条件配置</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>配置部门经理审批节点</li>
 *   <li>配置HR审批节点</li>
 *   <li>配置财务审批节点</li>
 *   <li>配置总经理审批节点</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-27
 * @see net.lab1024.sa.oa.workflow.dao.ApprovalNodeConfigDao 审批节点配置DAO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_common_approval_node_config")
@Schema(description = "审批节点配置实体")
public class ApprovalNodeConfigEntity extends BaseEntity {

    /**
     * 审批节点配置ID（主键）
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "审批节点配置ID", example = "1001")
    private Long id;

    /**
     * 审批配置ID
     * <p>
     * 关联ApprovalConfigEntity.id
     * </p>
     */
    @NotNull
    @TableField("approval_config_id")
    @Schema(description = "审批配置ID", example = "1001")
    private Long approvalConfigId;

    /**
     * 节点编码
     * <p>
     * 节点的唯一标识
     * </p>
     */
    @NotBlank
    @Size(max = 100)
    @TableField("node_code")
    @Schema(description = "节点编码", example = "MANAGER_APPROVAL")
    private String nodeCode;

    /**
     * 节点名称
     * <p>
     * 节点的显示名称
     * </p>
     */
    @NotBlank
    @Size(max = 200)
    @TableField("node_name")
    @Schema(description = "节点名称", example = "部门经理审批")
    private String nodeName;

    /**
     * 节点顺序
     * <p>
     * 节点在审批流程中的顺序
     * 从1开始，按顺序递增
     * </p>
     */
    @NotNull
    @TableField("node_order")
    @Schema(description = "节点顺序", example = "1")
    private Integer nodeOrder;

    /**
     * 节点类型
     * <p>
     * USER_NODE-用户审批节点
     * DEPT_NODE-部门审批节点
     * ROLE_NODE-角色审批节点
     * CONDITION_NODE-条件审批节点
     * PARALLEL_NODE-并行审批节点
     * </p>
     */
    @NotBlank
    @Size(max = 50)
    @TableField("node_type")
    @Schema(description = "节点类型", example = "USER_NODE")
    private String nodeType;

    /**
     * 审批人类型
     * <p>
     * SPECIFIC_USER-指定用户
     * DEPT_LEADER-部门负责人
     * ROLE_MEMBER-角色成员
     * SUPERIOR-上级领导
     * INITIATOR_SELECTOR-发起人选择
     * </p>
     */
    @Size(max = 50)
    @TableField("assignee_type")
    @Schema(description = "审批人类型", example = "DEPT_LEADER")
    private String assigneeType;

    /**
     * 审批人配置（JSON格式）
     * <p>
     * 存储审批人的配置信息
     * 示例：{"userIds":[1001,1002],"roleIds":[10,20]}
     * </p>
     */
    @TableField("assignee_config")
    @Schema(description = "审批人配置（JSON格式）")
    private String assigneeConfig;

    /**
     * 审批方式
     * <p>
     * OR会签-任一人通过即可
     * AND会签-所有人都必须通过
     * SEQUENTIAL-顺序审批
     * </p>
     */
    @Size(max = 50)
    @TableField("approval_mode")
    @Schema(description = "审批方式", example = "OR")
    private String approvalMode;

    /**
     * 是否必填
     * <p>
     * true-必须审批
     * false-可选审批
     * </p>
     */
    @TableField("required")
    @Schema(description = "是否必填", example = "true")
    private Boolean required;

    /**
     * 是否自动通过
     * <p>
     * true-自动通过
     * false-需要人工审批
     * </p>
     */
    @TableField("auto_approve")
    @Schema(description = "是否自动通过", example = "false")
    private Boolean autoApprove;

    /**
     * 超时时间（小时）
     * <p>
     * 节点审批的超时时间
     * 超时后可触发提醒或自动处理
     * </p>
     */
    @TableField("timeout_hours")
    @Schema(description = "超时时间（小时）", example = "24")
    private Integer timeoutHours;

    /**
     * 排序
     * <p>
     * 用于同一顺序下的多个节点的排序
     * </p>
     */
    @TableField("sort_order")
    @Schema(description = "排序", example = "100")
    private Integer sortOrder;

    /**
     * 节点描述
     */
    @Size(max = 1000)
    @TableField("description")
    @Schema(description = "节点描述", example = "部门经理审批节点")
    private String description;

    /**
     * 审批条件（JSON格式）
     * <p>
     * 节点的执行条件配置
     * 示例：{"amount":{">":5000,"<":10000}}
     * </p>
     */
    @TableField("approval_condition")
    @Schema(description = "审批条件（JSON格式）")
    private String approvalCondition;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性（JSON格式）")
    private String extendedAttributes;

    /**
     * 节点类型枚举
     */
    public enum NodeType {
        USER_NODE("USER_NODE", "用户审批节点"),
        DEPT_NODE("DEPT_NODE", "部门审批节点"),
        ROLE_NODE("ROLE_NODE", "角色审批节点"),
        CONDITION_NODE("CONDITION_NODE", "条件审批节点"),
        PARALLEL_NODE("PARALLEL_NODE", "并行审批节点");

        private final String code;
        private final String description;

        NodeType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static NodeType fromCode(String code) {
            for (NodeType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid node type code: " + code);
        }
    }

    /**
     * 审批人类型枚举
     */
    public enum AssigneeType {
        SPECIFIC_USER("SPECIFIC_USER", "指定用户"),
        DEPT_LEADER("DEPT_LEADER", "部门负责人"),
        ROLE_MEMBER("ROLE_MEMBER", "角色成员"),
        SUPERIOR("SUPERIOR", "上级领导"),
        INITIATOR_SELECTOR("INITIATOR_SELECTOR", "发起人选择");

        private final String code;
        private final String description;

        AssigneeType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static AssigneeType fromCode(String code) {
            for (AssigneeType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid assignee type code: " + code);
        }
    }

    /**
     * 审批方式枚举
     */
    public enum ApprovalMode {
        OR("OR", "或会签"),
        AND("AND", "且会签"),
        SEQUENTIAL("SEQUENTIAL", "顺序审批");

        private final String code;
        private final String description;

        ApprovalMode(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static ApprovalMode fromCode(String code) {
            for (ApprovalMode mode : values()) {
                if (mode.code.equals(code)) {
                    return mode;
                }
            }
            throw new IllegalArgumentException("Invalid approval mode code: " + code);
        }
    }
}
