package net.lab1024.sa.monitor.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.monitor.domain.form.AlertRuleAddForm;
import net.lab1024.sa.monitor.domain.form.AlertRuleQueryForm;
import net.lab1024.sa.monitor.domain.vo.AlertRuleVO;

import java.util.List;
import java.util.Map;

/**
 * 告警管理服务接口
 *
 * 负责告警规则管理、告警通知、告警历史等功能
 *
 * @author IOE-DREAM Team
 */
public interface AlertService {

    /**
     * 添加告警规则
     *
     * @param addForm 告警规则添加表单
     * @return 规则ID
     */
    Long addAlertRule(AlertRuleAddForm addForm);

    /**
     * 分页查询告警规则
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<AlertRuleVO> queryAlertRulePage(AlertRuleQueryForm queryForm);

    /**
     * 获取告警规则详情
     *
     * @param ruleId 规则ID
     * @return 规则详情
     */
    AlertRuleVO getAlertRuleDetail(Long ruleId);

    /**
     * 启用告警规则
     *
     * @param ruleId 规则ID
     */
    void enableAlertRule(Long ruleId);

    /**
     * 禁用告警规则
     *
     * @param ruleId 规则ID
     */
    void disableAlertRule(Long ruleId);

    /**
     * 删除告警规则
     *
     * @param ruleId 规则ID
     */
    void deleteAlertRule(Long ruleId);

    /**
     * 获取告警历史
     *
     * @param pageNum   页码
     * @param pageSize  页大小
     * @param severity  严重级别（可选）
     * @param status    状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 分页结果
     */
    PageResult<Map<String, Object>> getAlertHistory(Integer pageNum, Integer pageSize, String severity, String status, Long startTime, Long endTime);

    /**
     * 获取活跃告警统计
     *
     * @return 统计信息
     */
    Map<String, Object> getActiveAlertCount();

    /**
     * 获取告警统计
     *
     * @param days 统计天数（可选）
     * @return 统计信息
     */
    Map<String, Object> getAlertStatistics(Integer days);

    /**
     * 测试通知
     *
     * @param notificationType 通知类型
     * @param recipients       接收人列表（可选）
     * @return 测试结果
     */
    Map<String, Object> testNotification(String notificationType, List<String> recipients);

    /**
     * 获取通知渠道
     *
     * @return 通知渠道列表
     */
    List<Map<String, Object>> getNotificationChannels();

    /**
     * 批量解决告警
     *
     * @param alertIds   告警ID列表
     * @param resolution 解决说明
     * @return 解决结果统计
     */
    Map<String, Integer> batchResolveAlerts(List<Long> alertIds, String resolution);

    /**
     * 获取告警趋势
     *
     * @param days 时间范围（天）
     * @return 趋势数据
     */
    List<Map<String, Object>> getAlertTrends(Integer days);
}