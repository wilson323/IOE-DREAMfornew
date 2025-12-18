package net.lab1024.sa.access.access.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 门禁权限申请实体类
 * <p>
 * 用于记录门禁权限申请信息，支持工作流审批
 * 严格遵守CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("access_permission_apply")
public class AccessPermissionApplyEntity extends BaseEntity {

    /**
     * 权限申请ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 申请编号（业务key，唯一）
     */
    private String applyNo;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 申请类型
     * <p>
     * NORMAL-普通权限申请
     * EMERGENCY-紧急权限申请
     * </p>
     */
    private String applyType;

    /**
     * 申请原因
     */
    private String applyReason;

    /**
     * 申请开始时间
     */
    private LocalDateTime startTime;

    /**
     * 申请结束时间
     */
    private LocalDateTime endTime;

    /**
     * 申请状态
     * <p>
     * PENDING-待审批
     * APPROVED-已通过
     * REJECTED-已拒绝
     * CANCELLED-已取消
     * </p>
     */
    private String status;

    /**
     * 审批意见
     */
    private String approvalComment;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 工作流实例ID
     * <p>
     * 关联OA工作流模块的流程实例ID
     * 用于查询审批状态、审批历史等
     * </p>
     */
    @TableField("workflow_instance_id")
    private Long workflowInstanceId;
}
