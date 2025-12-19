package net.lab1024.sa.attendance.realtime.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 事件验证结果
 * <p>
 * 封装事件验证的完整结果信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventValidationResult {

    /**
     * 验证是否通过
     */
    private Boolean valid;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 验证失败的原因代码
     */
    private String errorCode;

    /**
     * 验证详情
     */
    private String validationDetails;
}
