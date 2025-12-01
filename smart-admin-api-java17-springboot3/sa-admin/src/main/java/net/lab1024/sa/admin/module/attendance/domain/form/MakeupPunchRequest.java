package net.lab1024.sa.admin.module.attendance.domain.form;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 补卡请求表单
 *
 * @author SmartAdmin Team
 * @date 2025-11-17
 */
@Data
public class MakeupPunchRequest {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 补卡日期
     */
    @NotNull(message = "补卡日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime punchDate;

    /**
     * 补卡类型：1-上班打卡 2-下班打卡
     */
    @NotNull(message = "补卡类型不能为空")
    private Integer punchType;

    /**
     * 补卡原因
     */
    @NotBlank(message = "补卡原因不能为空")
    private String reason;

    /**
     * 实际打卡时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualPunchTime;

    /**
     * 备注
     */
    private String remark;
}