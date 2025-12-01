package net.lab1024.sa.attendance.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 异常申请审批数据传输对象
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-24
 */
@Data
@Accessors(chain = true)
public class ExceptionApprovalDTO {

    /**
     * 申请ID
     */
    @NotNull(message = "申请ID不能为空")
    private Long applicationId;

    /**
     * 工作流任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private String taskId;

    /**
     * 审批人ID
     */
    @NotNull(message = "审批人ID不能为空")
    private Long approverId;

    /**
     * 审批结果
     */
    @NotBlank(message = "审批结果不能为空")
    @Size(max = 20, message = "审批结果长度不能超过20个字符")
    private String approvalResult;

    /**
     * 审批意见
     */
    @Size(max = 500, message = "审批意见长度不能超过500个字符")
    private String approvalComments;

    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalTime;

    /**
     * 是否需要下一个审批人
     */
    private Boolean needNextApprover = false;

    /**
     * 下一个审批人ID
     */
    private Long nextApproverId;
}