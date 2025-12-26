package net.lab1024.sa.video.service.impl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.manager.VideoSystemIntegrationManager;
import net.lab1024.sa.video.manager.VideoSystemIntegrationManager.SystemIntegrationStatus;
import net.lab1024.sa.video.service.VideoSystemIntegrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 视频系统集成服务实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解
 * - 使用@Resource依赖注入
 * - 调用Manager层进行复杂流程编排
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VideoSystemIntegrationServiceImpl implements VideoSystemIntegrationService {

    @Resource
    private VideoSystemIntegrationManager videoSystemIntegrationManager;

    @Override
    public Map<String, SystemIntegrationStatus> getIntegrationStatus() {
        log.info("[系统集成服务] 获取所有系统集成状态");
        return videoSystemIntegrationManager.getIntegrationStatus();
    }

    @Override
    public SystemIntegrationStatus getSystemIntegrationStatus(String systemName) {
        log.info("[系统集成服务] 获取指定系统状态: systemName={}", systemName);
        return videoSystemIntegrationManager.getSystemIntegrationStatus(systemName);
    }

    @Override
    public Map<String, Object> getIntegrationHealthReport() {
        log.info("[系统集成服务] 获取集成健康报告");
        return videoSystemIntegrationManager.getIntegrationHealthReport();
    }

    @Override
    public CompletableFuture<Map<String, Object>> triggerHealthCheck() {
        log.info("[系统集成服务] 触发系统集成健康检查");
        return videoSystemIntegrationManager.performIntegrationHealthCheck()
                .thenApply(v -> {
                    Map<String, Object> report = videoSystemIntegrationManager.getIntegrationHealthReport();
                    return report;
                });
    }

    @Override
    public Map<String, Object> getSystemMetrics(String systemName) {
        log.info("[系统集成服务] 获取系统监控指标: systemName={}", systemName);
        SystemIntegrationStatus status = videoSystemIntegrationManager.getSystemIntegrationStatus(systemName);
        if (status == null) {
            return Map.of("error", "系统不存在: " + systemName);
        }
        return status.getMetrics() != null ? status.getMetrics() : Map.of();
    }

    @Override
    public List<Map<String, Object>> getIntegrationEvents(Integer limit) {
        log.info("[系统集成服务] 获取集成事件列表: limit={}", limit);
        // 从Manager获取集成事件（如果Manager有该方法）
        // 目前返回空列表，待Manager实现后补充
        return List.of();
    }

    @Override
    public CompletableFuture<Void> handleIntegrationEvent(String eventType, String systemName, Object eventData) {
        log.info("[系统集成服务] 处理集成事件: type={}, system={}", eventType, systemName);
        return videoSystemIntegrationManager.handleIntegrationEvent(eventType, systemName, eventData);
    }
}
