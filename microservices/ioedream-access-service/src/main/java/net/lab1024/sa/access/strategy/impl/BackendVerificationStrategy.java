package net.lab1024.sa.access.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.manager.AccessVerificationManager;
import net.lab1024.sa.access.strategy.VerificationModeStrategy;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 后台验证策略实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解注册为Spring Bean
 * - 使用@Resource依赖注入
 * - 实现VerificationModeStrategy接口
 * </p>
 * <p>
 * 核心职责：
 * - 实现后台验证逻辑
 * - 集成反潜/互锁/多人验证
 * - 返回安防PUSH协议V4.8格式的响应
 * </p>
 * <p>
 * 验证流程：
 * 1. 反潜验证
 * 2. 互锁验证
 * 3. 时间段验证
 * 4. 黑名单验证
 * 5. 多人验证（如需要）
 * 6. 返回验证结果和控制指令
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class BackendVerificationStrategy implements VerificationModeStrategy {

    @Resource
    private AccessVerificationManager accessVerificationManager;

    @Override
    public VerificationResult verify(AccessVerificationRequest request) {
        log.info("[后台验证] 开始验证: userId={}, deviceId={}, areaId={}, event={}",
                request.getUserId(), request.getDeviceId(), request.getAreaId(), request.getEvent());

        try {
            // 1. 反潜验证（传递areaId参数以读取反潜配置）
            if (!accessVerificationManager.verifyAntiPassback(
                    request.getUserId(), request.getDeviceId(), request.getInOutStatus(), request.getAreaId())) {
                log.warn("[后台验证] 反潜验证失败: userId={}", request.getUserId());
                return VerificationResult.failed("ANTI_PASSBACK_VIOLATION", "反潜验证失败,请从正确的门进出");
            }

            // 2. 互锁验证（传递areaId参数以读取互锁配置）
            if (!accessVerificationManager.verifyInterlock(request.getDeviceId(), request.getAreaId())) {
                log.warn("[后台验证] 互锁验证失败: deviceId={}", request.getDeviceId());
                return VerificationResult.failed("INTERLOCK_VIOLATION", "互锁门禁冲突,请等待");
            }

            // 3. 时间段验证
            if (!accessVerificationManager.verifyTimePeriod(
                    request.getUserId(), request.getDeviceId(), request.getVerifyTime())) {
                log.warn("[后台验证] 时间段验证失败: userId={}", request.getUserId());
                return VerificationResult.failed("INVALID_TIME_PERIOD", "非有效时间段");
            }

            // 4. 黑名单验证
            if (accessVerificationManager.isBlacklisted(request.getUserId())) {
                log.warn("[后台验证] 黑名单验证失败: userId={}", request.getUserId());
                return VerificationResult.failed("BLACKLIST", "用户已被列入黑名单");
            }

            // 5. 多人验证（如需要）
            if (accessVerificationManager.isMultiPersonRequired(request.getAreaId())) {
                VerificationResult multiPersonResult = accessVerificationManager.verifyMultiPerson(request);
                if (!multiPersonResult.isSuccess()) {
                    log.warn("[后台验证] 多人验证等待中: userId={}", request.getUserId());
                    return multiPersonResult;
                }
            }

            // 6. 构建控制指令
            String controlCommand = buildControlCommand(request);

            // 7. 记录反潜验证结果
            accessVerificationManager.recordAntiPassback(
                    request.getUserId(),
                    request.getDeviceId(),
                    request.getAreaId(),
                    request.getInOutStatus(),
                    request.getVerifyType()
            );

            log.info("[后台验证] 验证通过: userId={}", request.getUserId());
            return VerificationResult.success("验证通过,欢迎进入", controlCommand, "backend");

        } catch (Exception e) {
            log.error("[后台验证] 验证异常: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            return VerificationResult.failed("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Override
    public boolean supports(String mode) {
        return "backend".equals(mode);
    }

    @Override
    public String getStrategyName() {
        return "BackendVerificationStrategy";
    }

    /**
     * 构建控制指令
     * <p>
     * 格式：CONTROL DEVICE AABBCCDDEE
     * AA: 命令码（01=开门, 02=关门, 03=常开, 04=常闭）
     * BB: 门号（01-04）
     * CC: 延时时间（秒, 00-99）
     * DD: 保留
     * EE: 保留
     * </p>
     *
     * @param request 验证请求
     * @return 控制指令（十六进制字符串）
     */
    private String buildControlCommand(AccessVerificationRequest request) {
        // 01: 开门命令
        // 01: 1号门（如果doorNumber为null，默认1号门）
        // 03: 延时3秒
        int doorNumber = request.getDoorNumber() != null ? request.getDoorNumber() : 1;
        return String.format("0101%02d0300", doorNumber);
    }
}
