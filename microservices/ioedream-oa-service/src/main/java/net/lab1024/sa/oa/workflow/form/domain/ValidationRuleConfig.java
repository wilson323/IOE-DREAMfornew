package net.lab1024.sa.oa.workflow.form.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 验证规则配置
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRuleConfig {

    /**
     * 字段ID
     */
    @NotBlank
    private String fieldId;

    /**
     * 规则类型
     */
    @NotBlank
    private String ruleType;

    /**
     * 规则参数
     */
    private Map<String, Object> parameters;

    /**
     * 错误提示消息
     */
    private String errorMessage;

    /**
     * 是否启用
     */
    @NotNull
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 规则优先级
     */
    private Integer priority;

    /**
     * 触发时机（blur, change, submit）
     */
    private String trigger;
}
