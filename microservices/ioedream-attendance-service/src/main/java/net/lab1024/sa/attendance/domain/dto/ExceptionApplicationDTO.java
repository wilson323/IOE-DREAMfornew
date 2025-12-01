package net.lab1024.sa.attendance.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 异常申请数据传输对象
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-24
 */
@Data
@Accessors(chain = true)
public class ExceptionApplicationDTO {

    /**
     * 申请ID（更新时使用）
     */
    private Long applicationId;

    /**
     * 员工ID
     */
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    /**
     * 异常类型
     */
    @NotBlank(message = "异常类型不能为空")
    @Size(max = 50, message = "异常类型长度不能超过50个字符")
    private String exceptionType;

    /**
     * 异常日期
     */
    @NotNull(message = "异常日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate exceptionDate;

    /**
     * 开始时间（某些异常类型需要）
     */
    @JsonFormat(pattern = "HH:mm:ss")
    private java.time.LocalTime startTime;

    /**
     * 结束时间（某些异常类型需要）
     */
    @JsonFormat(pattern = "HH:mm:ss")
    private java.time.LocalTime endTime;

    /**
     * 申请原因
     */
    @NotBlank(message = "申请原因不能为空")
    @Size(max = 500, message = "申请原因长度不能超过500个字符")
    private String reason;

    /**
     * 附件（可选）
     */
    private String attachment;

    /**
     * 申请状态（更新时使用）
     */
    private String applicationStatus;

    /**
     * 创建时间（更新时使用）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间（更新时使用）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}