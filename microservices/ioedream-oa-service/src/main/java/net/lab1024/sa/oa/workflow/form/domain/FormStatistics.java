package net.lab1024.sa.oa.workflow.form.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表单统计数据
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormStatistics {

    /**
     * 表单ID
     */
    private Long formId;

    /**
     * 表单名称
     */
    private String formName;

    /**
     * 使用次数
     */
    private Long usageCount;

    /**
     * 提交次数
     */
    private Long submitCount;

    /**
     * 验证失败次数
     */
    private Long validationFailCount;

    /**
     * 平均完成时间（秒）
     */
    private Double avgCompleteTime;

    /**
     * 最短完成时间（秒）
     */
    private Long minCompleteTime;

    /**
     * 最长完成时间（秒）
     */
    private Long maxCompleteTime;

    /**
     * 最后使用时间
     */
    private Long lastUsedTime;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 版本数量
     */
    private Integer versionCount;

    /**
     * 组件数量
     */
    private Integer componentCount;

    /**
     * 逻辑规则数量
     */
    private Integer logicRuleCount;

    /**
     * 验证规则数量
     */
    private Integer validationRuleCount;
}
