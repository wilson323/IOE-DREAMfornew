package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.domain.form.RuleTestRequest;
import net.lab1024.sa.attendance.domain.vo.RuleTestResultVO;

import java.util.List;

/**
 * 规则测试服务接口
 * <p>
 * 提供规则测试工具相关业务功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface RuleTestService {

    /**
     * 测试单个规则
     *
     * @param ruleId 规则ID
     * @param testRequest 测试请求
     * @return 测试结果
     */
    RuleTestResultVO testRule(Long ruleId, RuleTestRequest testRequest);

    /**
     * 测试自定义规则（条件和动作）
     *
     * @param testRequest 测试请求（包含ruleCondition和ruleAction）
     * @return 测试结果
     */
    RuleTestResultVO testCustomRule(RuleTestRequest testRequest);

    /**
     * 批量测试规则
     *
     * @param ruleIds 规则ID列表
     * @param testRequest 测试请求
     * @return 测试结果列表
     */
    List<RuleTestResultVO> batchTestRules(List<Long> ruleIds, RuleTestRequest testRequest);

    /**
     * 模拟测试数据
     * 生成随机的测试场景数据
     *
     * @param scenario 测试场景：LATE-早退 OVERTIME-加班 ABSENT-缺勤 NORMAL-正常
     * @return 测试请求数据
     */
    RuleTestRequest generateTestData(String scenario);

    /**
     * 快速测试（使用默认数据）
     *
     * @param ruleCondition 规则条件
     * @param ruleAction 规则动作
     * @return 测试结果
     */
    RuleTestResultVO quickTest(String ruleCondition, String ruleAction);

    /**
     * 验证规则语法
     * 验证规则条件和动作的JSON格式和语法
     *
     * @param ruleCondition 规则条件
     * @param ruleAction 规则动作
     * @return 验证结果：valid-有效 invalid-无效
     */
    Boolean validateRuleSyntax(String ruleCondition, String ruleAction);

    /**
     * 获取测试场景列表
     * 返回可用的测试场景列表
     *
     * @return 测试场景列表
     */
    List<TestScenarioVO> getTestScenarios();

    /**
     * 测试场景VO
     */
    @lombok.Data
    class TestScenarioVO {
        /**
         * 场景编码
         */
        String scenarioCode;

        /**
         * 场景名称
         */
        String scenarioName;

        /**
         * 场景描述
         */
        String scenarioDescription;

        /**
         * 示例数据
         */
        String exampleData;
    }
}
