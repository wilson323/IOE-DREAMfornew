package net.lab1024.sa.access.domain.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 门禁规则实体类
 * <p>
 * 用于存储智能门禁控制规则信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
public class AccessRuleEntity {

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则类型
     */
    private String ruleType;

    /**
     * 规则描述
     */
    private String ruleDescription;

    /**
     * 规则内容（JSON格式）
     */
    private String ruleContent;

    /**
     * 规则状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 更新人ID
     */
    private Long updateBy;

    /**
     * 备注信息
     */
    private String remark;
}
