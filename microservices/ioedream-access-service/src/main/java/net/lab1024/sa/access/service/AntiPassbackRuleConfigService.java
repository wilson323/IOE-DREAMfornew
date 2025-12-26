package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.AntiPassbackRuleConfigForm;
import net.lab1024.sa.access.domain.vo.AntiPassbackRuleConfigVO;

import java.util.List;

/**
 * 反潜回规则配置服务接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义
 * - 使用标准返回类型（Controller层包装ResponseDTO）
 * </p>
 * <p>
 * 核心职责：
 * - 提供反潜回规则配置管理API
 * - 支持规则CRUD操作
 * - 提供规则验证和测试功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface AntiPassbackRuleConfigService {

    /**
     * 创建反潜回规则配置
     *
     * @param form 规则配置表单
     * @return 规则ID
     */
    Long createRule(AntiPassbackRuleConfigForm form);

    /**
     * 更新反潜回规则配置
     *
     * @param ruleId 规则ID
     * @param form 规则配置表单
     * @return 是否成功
     */
    void updateRule(Long ruleId, AntiPassbackRuleConfigForm form);

    /**
     * 删除反潜回规则配置
     *
     * @param ruleId 规则ID
     * @return 是否成功
     */
    void deleteRule(Long ruleId);

    /**
     * 获取规则配置详情
     *
     * @param ruleId 规则ID
     * @return 规则配置
     */
    AntiPassbackRuleConfigVO getRuleById(Long ruleId);

    /**
     * 查询规则配置列表
     *
     * @param areaId 区域ID（可选）
     * @return 规则配置列表
     */
    List<AntiPassbackRuleConfigVO> queryRules(Long areaId);

    /**
     * 启用/禁用规则
     *
     * @param ruleId 规则ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    void toggleRule(Long ruleId, Boolean enabled);

    /**
     * 测试规则配置
     * <p>
     * 模拟通行场景测试规则是否生效
     * </p>
     *
     * @param ruleId 规则ID
     * @param testScenario 测试场景
     * @return 测试结果
     */
    Object testRule(Long ruleId, String testScenario);
}
