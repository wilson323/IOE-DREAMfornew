package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.domain.form.RuleTestHistoryQueryForm;
import net.lab1024.sa.attendance.domain.vo.RuleTestHistoryDetailVO;
import net.lab1024.sa.attendance.domain.vo.RuleTestHistoryVO;
import net.lab1024.sa.common.domain.PageResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则测试历史服务接口
 * <p>
 * 提供规则测试历史管理功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface RuleTestHistoryService {

    /**
     * 保存测试历史
     *
     * @param historyDetail 测试历史详情
     * @return 历史记录ID
     */
    Long saveHistory(RuleTestHistoryDetailVO historyDetail);

    /**
     * 分页查询测试历史
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<RuleTestHistoryVO> queryHistoryPage(RuleTestHistoryQueryForm queryForm);

    /**
     * 查询历史详情
     *
     * @param historyId 历史ID
     * @return 历史详情
     */
    RuleTestHistoryDetailVO getHistoryDetail(Long historyId);

    /**
     * 查询规则的所有测试历史
     *
     * @param ruleId 规则ID
     * @return 测试历史列表
     */
    List<RuleTestHistoryVO> getRuleTestHistory(Long ruleId);

    /**
     * 查询最近的测试历史
     *
     * @param limit 限制数量
     * @return 测试历史列表
     */
    List<RuleTestHistoryVO> getRecentHistory(Integer limit);

    /**
     * 统计规则测试次数
     *
     * @param ruleId 规则ID
     * @return 测试次数
     */
    Integer countRuleTests(Long ruleId);

    /**
     * 删除测试历史
     *
     * @param historyId 历史ID
     */
    void deleteHistory(Long historyId);

    /**
     * 批量删除测试历史
     *
     * @param historyIds 历史ID列表
     */
    void batchDeleteHistory(List<Long> historyIds);

    /**
     * 清理过期历史
     *
     * @param days 保留天数
     * @return 清理数量
     */
    Integer cleanExpiredHistory(Integer days);

    /**
     * 导出测试历史为JSON
     *
     * @param historyIds 历史ID列表
     * @return JSON字符串
     */
    String exportHistoryToJson(List<Long> historyIds);

    /**
     * 从JSON导入测试历史
     *
     * @param jsonData JSON数据
     * @return 导入数量
     */
    Integer importHistoryFromJson(String jsonData);

    /**
     * 导出测试配置模板
     *
     * @return JSON模板
     */
    String exportTestTemplate();
}
