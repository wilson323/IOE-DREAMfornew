package net.lab1024.sa.oa.workflow.form.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 表单设计视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDesignVO {

    /**
     * 表单ID
     */
    private Long formId;

    /**
     * 表单名称
     */
    private String formName;

    /**
     * 表单描述
     */
    private String formDescription;

    /**
     * 表单分类
     */
    private String formCategory;

    /**
     * 表单版本
     */
    private String version;

    /**
     * 表单状态（draft, published, deprecated）
     */
    private String status;

    /**
     * 组件数量
     */
    private Integer componentCount;

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
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 使用次数
     */
    private Long usageCount;
}
