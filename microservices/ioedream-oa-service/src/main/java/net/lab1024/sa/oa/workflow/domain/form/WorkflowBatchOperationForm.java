package net.lab1024.sa.oa.workflow.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;
import java.util.Map;

/**
 * 工作流批量操作请求表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@Schema(description = "工作流批量操作请求表单")
public class WorkflowBatchOperationForm {

    /**
     * 批量启动流程表单
     */
    @Data
    @Schema(description = "批量启动流程表单")
    public static class BatchStartForm {

        @NotBlank(message = "流程定义ID不能为空")
        @Schema(description = "流程定义ID", example = "leaveRequest:1:12345")
        private String processDefinitionId;

        @Size(max = 100, message = "批量启动名称长度不能超过100个字符")
        @Schema(description = "批量操作名称", example = "批量启动请假流程")
        private String batchName;

        @Schema(description = "批量操作描述")
        private String description;

        @NotNull(message = "启动数据不能为空")
        @Schema(description = "启动数据列表")
        private List<ProcessStartData> startDataList;

        @Schema(description = "失败策略", example = "CONTINUE")
        private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

        @Schema(description = "是否并行执行", example = "true")
        private Boolean parallelExecution = true;

        @Min(value = 1, message = "并行线程数至少为1")
        @Max(value = 20, message = "并行线程数不能超过20")
        @Schema(description = "并行线程数", example = "5")
        private Integer parallelThreadCount = 5;

        @Schema(description = "超时时间（秒）", example = "300")
        private Integer timeoutSeconds = 300;
    }

    /**
     * 流程启动数据
     */
    @Data
    @Schema(description = "流程启动数据")
    public static class ProcessStartData {

        @Schema(description = "业务ID", example = "BIZ001")
        private String businessId;

        @Schema(description = "业务标题", example = "张三的年假申请")
        private String businessTitle;

        @Schema(description = "启动人ID")
        private Long startUserId;

        @Schema(description = "启动参数")
        private Map<String, Object> startParameters;

        @Schema(description = "业务数据")
        private Map<String, Object> businessData;

        @Schema(description = "优先级", example = "NORMAL")
        private String priority;
    }

    /**
     * 批量完成任务表单
     */
    @Data
    @Schema(description = "批量完成任务表单")
    public static class BatchCompleteForm {

        @Size(max = 100, message = "批量操作名称长度不能超过100个字符")
        @Schema(description = "批量操作名称", example = "批量完成任务")
        private String batchName;

        @Schema(description = "批量操作描述")
        private String description;

        @NotNull(message = "任务数据不能为空")
        @Schema(description = "任务完成数据列表")
        private List<TaskCompleteData> completeDataList;

        @Schema(description = "失败策略", example = "CONTINUE")
        private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

        @Schema(description = "是否并行执行", example = "true")
        private Boolean parallelExecution = true;

        @Min(value = 1, message = "并行线程数至少为1")
        @Max(value = 20, message = "并行线程数不能超过20")
        @Schema(description = "并行线程数", example = "5")
        private Integer parallelThreadCount = 5;
    }

    /**
     * 任务完成数据
     */
    @Data
    @Schema(description = "任务完成数据")
    public static class TaskCompleteData {

        @NotBlank(message = "任务ID不能为空")
        @Schema(description = "任务ID", example = "12345")
        private String taskId;

        @Schema(description = "处理人ID")
        private Long assigneeId;

        @Schema(description = "完成意见")
        private String comment;

        @Schema(description = "任务变量")
        private Map<String, Object> taskVariables;

        @Schema(description = "本地变量")
        private Map<String, Object> localVariables;
    }

    /**
     * 批量审批表单
     */
    @Data
    @Schema(description = "批量审批表单")
    public static class BatchApproveForm {

        @Size(max = 100, message = "批量操作名称长度不能超过100个字符")
        @Schema(description = "批量操作名称", example = "批量审批任务")
        private String batchName;

        @Schema(description = "批量操作描述")
        private String description;

        @NotNull(message = "审批数据不能为空")
        @Schema(description = "审批数据列表")
        private List<TaskApprovalData> approvalDataList;

        @Schema(description = "失败策略", example = "CONTINUE")
        private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

        @Schema(description = "是否并行执行", example = "true")
        private Boolean parallelExecution = true;

        @Min(value = 1, message = "并行线程数至少为1")
        @Max(value = 20, message = "并行线程数不能超过20")
        @Schema(description = "并行线程数", example = "5")
        private Integer parallelThreadCount = 5;
    }

    /**
     * 任务审批数据
     */
    @Data
    @Schema(description = "任务审批数据")
    public static class TaskApprovalData {

        @NotBlank(message = "任务ID不能为空")
        @Schema(description = "任务ID", example = "12345")
        private String taskId;

        @Schema(description = "处理人ID")
        private Long assigneeId;

        @Schema(description = "审批意见")
        private String comment;

        @Schema(description = "审批变量")
        private Map<String, Object> approvalVariables;
    }

    /**
     * 批量拒绝表单
     */
    @Data
    @Schema(description = "批量拒绝表单")
    public static class BatchRejectForm {

        @Size(max = 100, message = "批量操作名称长度不能超过100个字符")
        @Schema(description = "批量操作名称", example = "批量拒绝任务")
        private String batchName;

        @Schema(description = "批量操作描述")
        private String description;

        @NotNull(message = "拒绝数据不能为空")
        @Schema(description = "拒绝数据列表")
        private List<TaskRejectData> rejectDataList;

        @Schema(description = "失败策略", example = "CONTINUE")
        private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

        @Schema(description = "是否并行执行", example = "true")
        private Boolean parallelExecution = true;

        @Min(value = 1, message = "并行线程数至少为1")
        @Max(value = 20, message = "并行线程数不能超过20")
        @Schema(description = "并行线程数", example = "5")
        private Integer parallelThreadCount = 5;
    }

    /**
     * 任务拒绝数据
     */
    @Data
    @Schema(description = "任务拒绝数据")
    public static class TaskRejectData {

        @NotBlank(message = "任务ID不能为空")
        @Schema(description = "任务ID", example = "12345")
        private String taskId;

        @Schema(description = "处理人ID")
        private Long assigneeId;

        @NotBlank(message = "拒绝原因不能为空")
        @Schema(description = "拒绝原因")
        private String rejectReason;

        @Schema(description = "拒绝变量")
        private Map<String, Object> rejectVariables;
    }

    /**
     * 批量分配表单
     */
    @Data
    @Schema(description = "批量分配表单")
    public static class BatchAssignForm {

        @Size(max = 100, message = "批量操作名称长度不能超过100个字符")
        @Schema(description = "批量操作名称", example = "批量分配任务")
        private String batchName;

        @Schema(description = "批量操作描述")
        private String description;

        @NotNull(message = "任务ID列表不能为空")
        @Schema(description = "任务ID列表")
        private List<String> taskIds;

        @NotNull(message = "分配人ID不能为空")
        @Schema(description = "分配人ID", example = "1001")
        private Long assigneeId;

        @Schema(description = "分配原因")
        private String assignReason;

        @Schema(description = "分配变量")
        private Map<String, Object> assignVariables;

        @Schema(description = "失败策略", example = "CONTINUE")
        private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

        @Schema(description = "是否并行执行", example = "true")
        private Boolean parallelExecution = true;
    }

    /**
     * 批量撤销表单
     */
    @Data
    @Schema(description = "批量撤销表单")
    public static class BatchCancelForm {

        @Size(max = 100, message = "批量操作名称长度不能超过100个字符")
        @Schema(description = "批量操作名称", example = "批量撤销流程")
        private String batchName;

        @Schema(description = "批量操作描述")
        private String description;

        @NotNull(message = "流程实例ID列表不能为空")
        @Schema(description = "流程实例ID列表")
        private List<String> processInstanceIds;

        @Schema(description = "撤销原因")
        private String cancelReason;

        @Schema(description = "失败策略", example = "CONTINUE")
        private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

        @Schema(description = "是否级联撤销", example = "false")
        private Boolean cascadeCancel = false;

        @Schema(description = "是否并行执行", example = "true")
        private Boolean parallelExecution = true;
    }

    /**
     * 批量挂起表单
     */
    @Data
    @Schema(description = "批量挂起表单")
    public static class BatchSuspendForm {

        @Size(max = 100, message = "批量操作名称长度不能超过100个字符")
        @Schema(description = "批量操作名称", example = "批量挂起流程")
        private String batchName;

        @Schema(description = "批量操作描述")
        private String description;

        @NotNull(message = "流程实例ID列表不能为空")
        @Schema(description = "流程实例ID列表")
        private List<String> processInstanceIds;

        @Schema(description = "挂起原因")
        private String suspendReason;

        @Schema(description = "失败策略", example = "CONTINUE")
        private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

        @Schema(description = "是否并行执行", example = "true")
        private Boolean parallelExecution = true;
    }

    /**
     * 批量激活表单
     */
    @Data
    @Schema(description = "批量激活表单")
    public static class BatchActivateForm {

        @Size(max = 100, message = "批量操作名称长度不能超过100个字符")
        @Schema(description = "批量操作名称", example = "批量激活流程")
        private String batchName;

        @Schema(description = "批量操作描述")
        private String description;

        @NotNull(message = "流程实例ID列表不能为空")
        @Schema(description = "流程实例ID列表")
        private List<String> processInstanceIds;

        @Schema(description = "激活原因")
        private String activateReason;

        @Schema(description = "失败策略", example = "CONTINUE")
        private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

        @Schema(description = "是否并行执行", example = "true")
        private Boolean parallelExecution = true;
    }

    /**
     * 批量删除表单
     */
    @Data
    @Schema(description = "批量删除表单")
    public static class BatchDeleteForm {

        @Size(max = 100, message = "批量操作名称长度不能超过100个字符")
        @Schema(description = "批量操作名称", example = "批量删除流程")
        private String batchName;

        @Schema(description = "批量操作描述")
        private String description;

        @NotNull(message = "流程实例ID列表不能为空")
        @Schema(description = "流程实例ID列表")
        private List<String> processInstanceIds;

        @Schema(description = "删除原因")
        private String deleteReason;

        @Schema(description = "失败策略", example = "CONTINUE")
        private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

        @Schema(description = "是否级联删除", example = "true")
        private Boolean cascadeDelete = true;

        @Schema(description = "是否物理删除", example = "false")
        private Boolean physicalDelete = false;

        @Schema(description = "是否并行执行", example = "true")
        private Boolean parallelExecution = true;
    }

    /**
     * 批量设置变量表单
     */
    @Data
    @Schema(description = "批量设置变量表单")
    public static class BatchSetVariablesForm {

        @Size(max = 100, message = "批量操作名称长度不能超过100个字符")
        @Schema(description = "批量操作名称", example = "批量设置流程变量")
        private String batchName;

        @Schema(description = "批量操作描述")
        private String description;

        @NotNull(message = "流程实例ID列表不能为空")
        @Schema(description = "流程实例ID列表")
        private List<String> processInstanceIds;

        @NotNull(message = "变量不能为空")
        @Schema(description = "要设置的变量")
        private Map<String, Object> variables;

        @Schema(description = "是否覆盖现有变量", example = "true")
        private Boolean overwriteExisting = true;

        @Schema(description = "失败策略", example = "CONTINUE")
        private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

        @Schema(description = "是否并行执行", example = "true")
        private Boolean parallelExecution = true;
    }

    /**
     * 批量添加评论表单
     */
    @Data
    @Schema(description = "批量添加评论表单")
    public static class BatchAddCommentsForm {

        @Size(max = 100, message = "批量操作名称长度不能超过100个字符")
        @Schema(description = "批量操作名称", example = "批量添加评论")
        private String batchName;

        @Schema(description = "批量操作描述")
        private String description;

        @NotNull(message = "评论数据不能为空")
        @Schema(description = "评论数据列表")
        private List<CommentData> commentDataList;

        @Schema(description = "失败策略", example = "CONTINUE")
        private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

        @Schema(description = "是否并行执行", example = "true")
        private Boolean parallelExecution = true;
    }

    /**
     * 评论数据
     */
    @Data
    @Schema(description = "评论数据")
    public static class CommentData {

        @NotBlank(message = "任务ID不能为空")
        @Schema(description = "任务ID", example = "12345")
        private String taskId;

        @NotNull(message = "评论人ID不能为空")
        @Schema(description = "评论人ID", example = "1001")
        private Long userId;

        @NotBlank(message = "评论内容不能为空")
        @Schema(description = "评论内容")
        private String comment;

        @Schema(description = "评论类型", example = "COMMENT")
        private String commentType;
    }

    /**
     * 批量添加附件表单
     */
    @Data
    @Schema(description = "批量添加附件表单")
    public static class BatchAddAttachmentsForm {

        @Size(max = 100, message = "批量操作名称长度不能超过100个字符")
        @Schema(description = "批量操作名称", example = "批量添加附件")
        private String batchName;

        @Schema(description = "批量操作描述")
        private String description;

        @NotNull(message = "附件数据不能为空")
        @Schema(description = "附件数据列表")
        private List<AttachmentData> attachmentDataList;

        @Schema(description = "失败策略", example = "CONTINUE")
        private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

        @Schema(description = "是否并行执行", example = "true")
        private Boolean parallelExecution = true;
    }

    /**
     * 附件数据
     */
    @Data
    @Schema(description = "附件数据")
    public static class AttachmentData {

        @NotBlank(message = "任务ID不能为空")
        @Schema(description = "任务ID", example = "12345")
        private String taskId;

        @NotNull(message = "上传人ID不能为空")
        @Schema(description = "上传人ID", example = "1001")
        private Long uploaderId;

        @NotBlank(message = "附件名称不能为空")
        @Schema(description = "附件名称")
        private String attachmentName;

        @NotBlank(message = "附件路径不能为空")
        @Schema(description = "附件路径")
        private String attachmentPath;

        @Schema(description = "附件大小")
        private Long attachmentSize;

        @Schema(description = "附件类型")
        private String attachmentType;

        @Schema(description = "附件描述")
        private String description;
    }

    /**
     * 批量历史查询表单
     */
    @Data
    @Schema(description = "批量历史查询表单")
    public static class BatchHistoryQueryForm {

        @Schema(description = "批量操作ID")
        private String batchId;

        @Schema(description = "操作类型")
        private String operationType;

        @Schema(description = "状态")
        private String status;

        @Schema(description = "开始时间")
        private String startTime;

        @Schema(description = "结束时间")
        private String endTime;

        @Schema(description = "操作人ID")
        private Long operatorId;

        @Min(value = 1, message = "页码至少为1")
        @Schema(description = "页码", example = "1")
        private Integer pageNum = 1;

        @Min(value = 1, message = "每页大小至少为1")
        @Max(value = 100, message = "每页大小不能超过100")
        @Schema(description = "每页大小", example = "20")
        private Integer pageSize = 20;
    }

    /**
     * 失败策略枚举
     */
    public enum FailureStrategy {
        /**
         * 遇到失败继续执行
         */
        CONTINUE("CONTINUE", "遇到失败继续执行"),

        /**
         * 遇到失败停止执行
         */
        STOP("STOP", "遇到失败停止执行"),

        /**
         * 遇到失败重试
         */
        RETRY("RETRY", "遇到失败重试"),

        /**
         * 遇到失败回滚
         */
        ROLLBACK("ROLLBACK", "遇到失败回滚");

        private final String code;
        private final String description;

        FailureStrategy(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}