package net.lab1024.sa.oa.workflow.domain.form;

import lombok.Data;

/**
 * 审批任务查询表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Form类用于接收前端请求参数
 * - 包含完整的参数验证注解
 * - 支持分页查询参数
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
public class ApprovalTaskQueryForm {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 申请人ID（我的申请时使用）
     */
    private Long applicantId;

    /**
     * 状态（PENDING-待办, COMPLETED-已办, MY_APPLICATIONS-我的申请）
     */
    private String status;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 优先级（1-低 2-中 3-高）
     */
    private Integer priority;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}






