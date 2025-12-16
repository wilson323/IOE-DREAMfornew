package net.lab1024.sa.attendance.engine.rule.loader;

import java.util.List;
import java.util.Map;

/**
 * 规则加载器接口
 * <p>
 * 负责从数据库或配置中心加载规则配置和定义
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface RuleLoader {

    /**
     * 加载所有启用的规则
     *
     * @return 启用的规则ID列表
     */
    List<Long> loadAllActiveRules();

    /**
     * 根据规则分类加载规则
     *
     * @param ruleCategory 规则分类
     * @return 规则ID列表
     */
    List<Long> getRulesByCategory(String ruleCategory);

    /**
     * 加载规则配置
     *
     * @param ruleId 规则ID
     * @return 规则配置Map
     */
    Map<String, Object> loadRuleConfig(Long ruleId);

    /**
     * 加载规则条件定义
     *
     * @param ruleId 规则ID
     * @return 规则条件表达式
     */
    String loadRuleCondition(Long ruleId);

    /**
     * 加载规则动作定义
     *
     * @param ruleId 规则ID
     * @return 规则动作配置
     */
    Map<String, Object> loadRuleAction(Long ruleId);

    /**
     * 检查规则是否存在
     *
     * @param ruleId 规则ID
     * @return 是否存在
     */
    boolean ruleExists(Long ruleId);

    /**
     * 获取规则版本
     *
     * @param ruleId 规则ID
     * @return 规则版本号
     */
    String getRuleVersion(Long ruleId);

    /**
     * 重新加载规则缓存
     *
     * @param ruleId 规则ID
     */
    void reloadRule(Long ruleId);

    /**
     * 重新加载所有规则缓存
     */
    void reloadAllRules();

    /**
     * 获取规则统计信息
     *
     * @return 统计信息Map
     */
    Map<String, Object> getRuleStatistics();

    /**
     * 检查规则是否在有效期内
     *
     * @param ruleId 规则ID
     * @return 是否有效
     */
    boolean isRuleEffective(Long ruleId);
}