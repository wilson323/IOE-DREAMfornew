package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.AlertRuleForm;
import net.lab1024.sa.access.domain.form.AlertRuleQueryForm;
import net.lab1024.sa.access.domain.vo.AlertRuleVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * 告警规则服务接口
 * <p>
 * 提供告警规则的完整生命周期管理：
 * - 规则CRUD操作（创建、查询、更新、删除）
 * - 规则启用/禁用
 * - 规则优先级管理
 * - 规则表达式验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AlertRuleService {

    /**
     * 创建告警规则
     *
     * @param ruleForm 规则表单
     * @return 规则ID
     */
    ResponseDTO<Long> createRule(AlertRuleForm ruleForm);

    /**
     * 更新告警规则
     *
     * @param ruleForm 规则表单
     * @return 是否成功
     */
    ResponseDTO<Void> updateRule(AlertRuleForm ruleForm);

    /**
     * 删除告警规则
     *
     * @param ruleId 规则ID
     * @return 是否成功
     */
    ResponseDTO<Void> deleteRule(Long ruleId);

    /**
     * 查询告警规则详情
     *
     * @param ruleId 规则ID
     * @return 规则详情
     */
    ResponseDTO<AlertRuleVO> getRule(Long ruleId);

    /**
     * 分页查询告警规则列表
     *
     * @param queryForm 查询表单
     * @return 规则列表
     */
    ResponseDTO<PageResult<AlertRuleVO>> queryRules(AlertRuleQueryForm queryForm);

    /**
     * 查询所有启用的规则（按优先级排序）
     *
     * @return 规则列表
     */
    ResponseDTO<List<AlertRuleVO>> queryEnabledRules();

    /**
     * 启用/禁用告警规则
     *
     * @param ruleId 规则ID
     * @param enabled 启用状态（0-禁用 1-启用）
     * @return 是否成功
     */
    ResponseDTO<Void> toggleRule(Long ruleId, Integer enabled);

    /**
     * 验证规则表达式
     *
     * @param ruleForm 规则表单
     * @return 验证结果（valid=true表示表达式有效）
     */
    ResponseDTO<Boolean> validateRuleExpression(AlertRuleForm ruleForm);

    /**
     * 批量启用/禁用告警规则
     *
     * @param ruleIds 规则ID列表
     * @param enabled  启用状态（0-禁用 1-启用）
     * @return 操作结果
     */
    ResponseDTO<Void> batchToggleRules(List<Long> ruleIds, Integer enabled);
}
