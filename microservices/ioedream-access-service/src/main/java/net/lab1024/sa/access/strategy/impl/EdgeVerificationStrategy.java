package net.lab1024.sa.access.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.strategy.VerificationModeStrategy;
import org.springframework.stereotype.Component;

/**
 * 设备端验证策略实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解注册为Spring Bean
 * - 实现VerificationModeStrategy接口
 * </p>
 * <p>
 * 核心职责：
 * - 处理设备端验证后的记录接收
 * - 验证记录的有效性
 * - 支持离线验证记录
 * </p>
 * <p>
 * 工作流程：
 * 1. 设备端已完成识别+验证+开门
 * 2. 设备批量上传通行记录
 * 3. 软件端接收记录并存储
 * 4. 进行异常检测和统计分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class EdgeVerificationStrategy implements VerificationModeStrategy {

    @Override
    public VerificationResult verify(AccessVerificationRequest request) {
        log.info("[设备端验证] 接收通行记录: userId={}, deviceId={}, areaId={}, event={}",
                request.getUserId(), request.getDeviceId(), request.getAreaId(), request.getEvent());

        // 设备端验证模式下，设备已经完成验证和开门
        // 软件端只需要接收记录，不需要返回控制指令
        // 这里主要用于记录验证和异常检测

        try {
            // 1. 验证记录有效性
            if (request.getUserId() == null || request.getDeviceId() == null) {
                log.warn("[设备端验证] 记录无效: userId={}, deviceId={}", request.getUserId(), request.getDeviceId());
                return VerificationResult.failed("INVALID_RECORD", "记录数据无效");
            }

            // 2. 记录已由设备端验证通过，软件端确认接收
            log.info("[设备端验证] 记录接收成功: userId={}", request.getUserId());
            return VerificationResult.success("记录接收成功", null, "edge");

        } catch (Exception e) {
            log.error("[设备端验证] 记录处理异常: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            return VerificationResult.failed("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Override
    public boolean supports(String mode) {
        return "edge".equals(mode);
    }

    @Override
    public String getStrategyName() {
        return "EdgeVerificationStrategy";
    }
}
