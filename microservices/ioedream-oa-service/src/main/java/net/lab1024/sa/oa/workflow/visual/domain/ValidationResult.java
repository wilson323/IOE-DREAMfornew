package net.lab1024.sa.oa.workflow.visual.domain;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 验证结果类
 * <p>
 * 封装工作流配置验证的结果，包括错误列表、警告列表等
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    /**
     * 是否验证通过
     */
    private Boolean valid;

    /**
     * 错误列表
     */
    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();

    /**
     * 警告列表
     */
    @Builder.Default
    private List<ValidationError> warnings = new ArrayList<>();

    /**
     * 信息列表
     */
    @Builder.Default
    private List<ValidationError> infos = new ArrayList<>();

    /**
     * 验证时间
     */
    private LocalDateTime validationTime;

    /**
     * 验证耗时（毫秒）
     */
    private Long validationDuration;

    /**
     * 验证对象
     */
    private String validationTarget;

    /**
     * 验证类型
     */
    private String validationType;

    /**
     * 验证人ID
     */
    private Long validatorUserId;

    /**
     * 验证人姓名
     */
    private String validatorUserName;

    /**
     * 扩展信息
     */
    private Object extendedInfo;

    /**
     * 创建成功结果
     */
    public static ValidationResult success() {
        return ValidationResult.builder()
                .valid(true)
                .validationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     */
    public static ValidationResult fail(List<ValidationError> errors) {
        return ValidationResult.builder()
                .valid(false)
                .errors(errors)
                .validationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 添加错误
     */
    public void addError(ValidationError error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
        this.valid = false;
    }

    /**
     * 添加警告
     */
    public void addWarning(ValidationError warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    /**
     * 添加信息
     */
    public void addInfo(ValidationError info) {
        if (this.infos == null) {
            this.infos = new ArrayList<>();
        }
        this.infos.add(info);
    }

    /**
     * 是否有错误
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * 是否有警告
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * 获取错误总数
     */
    public int getErrorCount() {
        return errors == null ? 0 : errors.size();
    }

    /**
     * 获取警告总数
     */
    public int getWarningCount() {
        return warnings == null ? 0 : warnings.size();
    }

    /**
     * 获取所有消息
     */
    public List<String> getAllMessages() {
        List<String> messages = new ArrayList<>();

        if (errors != null) {
            for (ValidationError error : errors) {
                messages.add("[ERROR] " + error.getErrorMessage());
            }
        }

        if (warnings != null) {
            for (ValidationError warning : warnings) {
                messages.add("[WARN] " + warning.getErrorMessage());
            }
        }

        if (infos != null) {
            for (ValidationError info : infos) {
                messages.add("[INFO] " + info.getErrorMessage());
            }
        }

        return messages;
    }

    /**
     * 合并多个验证结果
     */
    public static ValidationResult merge(ValidationResult... results) {
        ValidationResult merged = ValidationResult.builder()
                .valid(true)
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .infos(new ArrayList<>())
                .validationTime(LocalDateTime.now())
                .build();

        for (ValidationResult result : results) {
            if (result.getErrors() != null) {
                merged.getErrors().addAll(result.getErrors());
            }
            if (result.getWarnings() != null) {
                merged.getWarnings().addAll(result.getWarnings());
            }
            if (result.getInfos() != null) {
                merged.getInfos().addAll(result.getInfos());
            }
            if (!result.getValid()) {
                merged.setValid(false);
            }
        }

        return merged;
    }
}
