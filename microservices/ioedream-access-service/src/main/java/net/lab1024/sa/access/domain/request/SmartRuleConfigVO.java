package net.lab1024.sa.access.domain.request;

import java.util.Map;

import lombok.Data;

/**
 * 智能规则配置VO
 * <p>
 * 用于创建和更新智能门禁规则的配置对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
public class SmartRuleConfigVO {

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
     * 规则参数
     */
    private Map<String, Object> ruleParams;

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
    private java.time.LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private java.time.LocalDateTime expireTime;
}
