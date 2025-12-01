package net.lab1024.sa.admin.module.attendance.domain.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 报表配置验证结果
 * 考勤模块报表配置验证的返回结果
 *
 * @author SmartAdmin Team
 * @date 2025-11-24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportConfigValidationResult {

    /**
     * 验证是否通过
     */
    private Boolean valid;

    /**
     * 验证错误信息列表
     */
    private List<String> errors;

    /**
     * 验证警告信息列表
     */
    private List<String> warnings;

    /**
     * 验证时间
     */
    private java.time.LocalDateTime validateTime;

    /**
     * 验证耗时（毫秒）
     */
    private Long validateDuration;
}
