package net.lab1024.sa.oa.workflow.form.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 表单版本
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormVersion {

    /**
     * 版本ID
     */
    private Long versionId;

    /**
     * 表单ID
     */
    private Long formId;

    /**
     * 版本号
     */
    private String version;

    /**
     * 版本描述
     */
    private String description;

    /**
     * 版本状态（draft, published, deprecated）
     */
    private String status;

    /**
     * 变更内容
     */
    private String changelog;

    /**
     * 表单设计JSON
     */
    private String formDesignJson;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 是否为当前版本
     */
    private Boolean current;
}
