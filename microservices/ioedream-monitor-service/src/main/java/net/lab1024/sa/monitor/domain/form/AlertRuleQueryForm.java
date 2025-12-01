package net.lab1024.sa.monitor.domain.form;

import lombok.Data;

/**
 * 告警规则查询表单
 *
 * 用于分页查询告警规则列表
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class AlertRuleQueryForm {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 20;

    /**
     * 规则名称（模糊查询）
     */
    private String ruleName;

    /**
     * 监控指标
     */
    private String metricName;

    /**
     * 监控类型
     */
    private String monitorType;

    /**
     * 告警级别 (INFO、WARNING、ERROR、CRITICAL)
     */
    private String alertLevel;

    /**
     * 规则状态 (ENABLED、DISABLED)
     */
    private String status;

    /**
     * 适用服务（模糊查询）
     */
    private String applicableServices;

    /**
     * 适用环境
     */
    private String applicableEnvironments;

    /**
     * 标签（模糊查询）
     */
    private String tags;

    /**
     * 关键词搜索（规则名称、规则描述、监控指标）
     */
    private String keyword;
}
