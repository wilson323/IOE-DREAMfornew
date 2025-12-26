package net.lab1024.sa.data.service;

import net.lab1024.sa.common.dto.PageResult;
import net.lab1024.sa.data.domain.DataAnalysisDomain.*;

import java.util.List;
import java.util.Map;

/**
 * 仪表板服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface DashboardService {

    // ==================== 仪表板管理 ====================

    /**
     * 创建仪表板
     *
     * @param dashboard 仪表板配置
     * @return 仪表板ID
     */
    Long createDashboard(DashboardVO dashboard);

    /**
     * 更新仪表板
     *
     * @param dashboardId 仪表板ID
     * @param dashboard   仪表板配置
     */
    void updateDashboard(Long dashboardId, DashboardVO dashboard);

    /**
     * 删除仪表板
     *
     * @param dashboardId 仪表板ID
     */
    void deleteDashboard(Long dashboardId);

    /**
     * 获取仪表板详情
     *
     * @param dashboardId 仪表板ID
     * @return 仪表板详情
     */
    DashboardVO getDashboardDetail(Long dashboardId);

    /**
     * 获取仪表板列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 仪表板列表
     */
    PageResult<DashboardVO> getDashboardList(Integer pageNum, Integer pageSize);

    /**
     * 复制仪表板
     *
     * @param dashboardId       仪表板ID
     * @param newDashboardName 新仪表板名称
     * @return 新仪表板ID
     */
    Long copyDashboard(Long dashboardId, String newDashboardName);

    // ==================== 仪表板数据 ====================

    /**
     * 获取仪表板数据
     *
     * @param dashboardId 仪表板ID
     * @param params      查询参数
     * @return 仪表板数据
     */
    Map<String, Object> getDashboardData(Long dashboardId, Map<String, Object> params);

    /**
     * 刷新仪表板数据
     *
     * @param dashboardId 仪表板ID
     * @return 仪表板数据
     */
    Map<String, Object> refreshDashboardData(Long dashboardId);

    /**
     * 获取组件数据
     *
     * @param dashboardId  仪表板ID
     * @param componentId  组件ID
     * @param params       查询参数
     * @return 组件数据
     */
    Map<String, Object> getComponentData(Long dashboardId, String componentId,
                                         Map<String, Object> params);

    /**
     * 批量获取组件数据
     *
     * @param dashboardId  仪表板ID
     * @param componentIds 组件ID列表
     * @return 组件数据映射
     */
    Map<String, Object> batchGetComponentData(Long dashboardId, List<String> componentIds);

    // ==================== 仪表板模板 ====================

    /**
     * 获取仪表板模板
     *
     * @param businessModule 业务模块
     * @return 仪表板模板
     */
    List<DashboardVO> getDashboardTemplates(String businessModule);

    /**
     * 应用仪表板模板
     *
     * @param templateId    模板ID
     * @param dashboardName 新仪表板名称
     * @return 新仪表板ID
     */
    Long applyDashboardTemplate(Long templateId, String dashboardName);

    /**
     * 保存为仪表板模板
     *
     * @param dashboardId 仪表板ID
     * @param templateName 模板名称
     * @return 模板ID
     */
    Long saveAsDashboardTemplate(Long dashboardId, String templateName);

    // ==================== 仪表板布局 ====================

    /**
     * 更新仪表板布局
     *
     * @param dashboardId 仪表板ID
     * @param layout       布局配置
     */
    void updateDashboardLayout(Long dashboardId, DashboardConfig layout);

    /**
     * 添加仪表板组件
     *
     * @param dashboardId 仪表板ID
     * @param component    组件配置
     * @return 组件ID
     */
    String addDashboardComponent(Long dashboardId, DashboardComponent component);

    /**
     * 更新仪表板组件
     *
     * @param dashboardId 仪表板ID
     * @param componentId 组件ID
     * @param component   组件配置
     */
    void updateDashboardComponent(Long dashboardId, String componentId,
                                  DashboardComponent component);

    /**
     * 删除仪表板组件
     *
     * @param dashboardId 仪表板ID
     * @param componentId 组件ID
     */
    void removeDashboardComponent(Long dashboardId, String componentId);

    // ==================== 仪表板权限 ====================

    /**
     * 设置仪表板权限
     *
     * @param dashboardId 仪表板ID
     * @param permission   权限配置
     */
    void setDashboardPermission(Long dashboardId, ReportPermission permission);

    /**
     * 检查仪表板权限
     *
     * @param dashboardId     仪表板ID
     * @param permissionType  权限类型
     * @return 是否有权限
     */
    Boolean checkDashboardPermission(Long dashboardId, String permissionType);

    // ==================== 仪表板分享 ====================

    /**
     * 生成仪表板分享链接
     *
     * @param dashboardId 仪表板ID
     * @param expireHours 有效期（小时）
     * @return 分享链接
     */
    String generateShareLink(Long dashboardId, Integer expireHours);

    /**
     * 通过分享链接访问仪表板
     *
     * @param shareToken 分享令牌
     * @return 仪表板数据
     */
    Map<String, Object> accessDashboardByShare(String shareToken);
}
