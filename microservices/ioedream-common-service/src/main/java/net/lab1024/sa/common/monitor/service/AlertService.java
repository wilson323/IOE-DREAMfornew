package net.lab1024.sa.common.monitor.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.monitor.domain.dto.AlertRuleAddDTO;
import net.lab1024.sa.common.monitor.domain.dto.AlertRuleQueryDTO;
import net.lab1024.sa.common.monitor.domain.vo.AlertRuleVO;

/**
 * 告警服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
public interface AlertService {

    /**
     * 添加告警规则
     *
     * @param addDTO 告警规则添加DTO
     * @return 告警规则ID
     */
    Long addAlertRule(AlertRuleAddDTO addDTO);

    /**
     * 分页查询告警规则
     *
     * @param queryDTO 查询DTO
     * @return 分页结果
     */
    PageResult<AlertRuleVO> queryAlertRulePage(AlertRuleQueryDTO queryDTO);

    /**
     * 获取告警规则详情
     *
     * @param ruleId 规则ID
     * @return 告警规则VO
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
}

