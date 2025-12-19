package net.lab1024.sa.consume.report.domain.entity;

/**
 * 测试用报表模板实体（最小实现）
 * <p>
 * 仅用于 consume-service 测试编译与单元测试场景。
 * </p>
 */
public class ConsumeReportTemplateEntity {

    private Long id;
    private String templateName;
    private String templateType;
    private String reportConfig;
    private Boolean enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getReportConfig() {
        return reportConfig;
    }

    public void setReportConfig(String reportConfig) {
        this.reportConfig = reportConfig;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
