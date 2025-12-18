package net.lab1024.sa.video.service;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.manager.VideoSystemIntegrationManager.SystemIntegrationStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 视频系统集成服务接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Service层负责业务逻辑和事务管理
 * - 调用Manager层进行复杂流程编排
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 * <p>
 * 核心职责：
 * - 系统集成状态查询
 * - 集成健康检查
 * - 系统监控指标获取
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VideoSystemIntegrationService {

    /**
     * 获取所有系统集成状态
     *
     * @return 系统集成状态Map
     */
    Map<String, SystemIntegrationStatus> getIntegrationStatus();

    /**
     * 获取指定系统的集成状态
     *
     * @param systemName 系统名称
     * @return 系统集成状态
     */
    SystemIntegrationStatus getSystemIntegrationStatus(String systemName);

    /**
     * 获取系统集成健康报告
     *
     * @return 健康报告Map
     */
    Map<String, Object> getIntegrationHealthReport();

    /**
     * 触发系统集成健康检查
     *
     * @return 健康检查结果
     */
    CompletableFuture<Map<String, Object>> triggerHealthCheck();

    /**
     * 获取系统监控指标
     *
     * @param systemName 系统名称（可选）
     * @return 监控指标Map
     */
    Map<String, Object> getSystemMetrics(String systemName);

    /**
     * 获取集成事件列表
     *
     * @param limit 限制数量
     * @return 事件列表
     */
    List<Map<String, Object>> getIntegrationEvents(Integer limit);

    /**
     * 处理系统集成事件
     *
     * @param eventType 事件类型
     * @param systemName 系统名称
     * @param eventData 事件数据
     * @return 处理结果
     */
    CompletableFuture<Void> handleIntegrationEvent(String eventType, String systemName, Object eventData);
}
