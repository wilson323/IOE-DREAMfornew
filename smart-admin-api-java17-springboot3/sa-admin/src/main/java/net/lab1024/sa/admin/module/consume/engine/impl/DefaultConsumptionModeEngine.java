package net.lab1024.sa.admin.module.consume.engine.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumptionModeEngine;
import net.lab1024.sa.admin.module.consume.engine.mode.ConsumptionMode;
import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 默认消费模式引擎实现
 * 提供线程安全的模式注册、发现和处理功能
 *
 * @author SmartAdmin Team
 * @date 2025/11/16
 */
@Slf4j
@Component
public class DefaultConsumptionModeEngine implements ConsumptionModeEngine {

    private final Map<String, ConsumptionMode> modeRegistry = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> deviceSupportedModes = new ConcurrentHashMap<>();
    private final Map<Long, String> deviceDefaultModes = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private EngineStatus engineStatus = EngineStatus.INITIALIZING;

    @Override
    public ConsumeResult process(ConsumeRequest request) {
        if (!validate(request)) {
            return ConsumeResult.failure("INVALID_REQUEST", "无效的消费请求");
        }

        try {
            ConsumptionMode mode = getMode(request.getConsumptionMode());
            if (mode == null) {
                return ConsumeResult.failure("UNSUPPORTED_MODE",
                    "不支持的消费模式: " + request.getConsumptionMode());
            }

            long startTime = System.currentTimeMillis();
            ConsumeResult result = mode.process(request);
            long processingTime = System.currentTimeMillis() - startTime;

            result.setProcessingTime(processingTime);
            result.setConsumptionMode(request.getConsumptionMode());

            log.info("消费处理完成 - 模式: {}, 金额: {}, 耗时: {}ms",
                    request.getConsumptionMode(), result.getActualAmount(), processingTime);

            return result;

        } catch (Exception e) {
            log.error("消费处理异常", e);
            return ConsumeResult.failure("PROCESSING_ERROR", "消费处理异常: " + e.getMessage());
        }
    }

    @Override
    public boolean validate(ConsumeRequest request) {
        if (request == null) {
            return false;
        }

        try {
            ConsumptionMode mode = getMode(request.getConsumptionMode());
            if (mode == null) {
                return false;
            }

            return mode.validate(request).isValid();

        } catch (Exception e) {
            log.error("消费请求验证异常", e);
            return false;
        }
    }

    @Override
    public void registerMode(ConsumptionMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("消费模式不能为空");
        }

        lock.writeLock().lock();
        try {
            String modeCode = mode.getModeCode();
            if (modeRegistry.containsKey(modeCode)) {
                log.warn("消费模式已存在，将被覆盖: {}", modeCode);
            }

            modeRegistry.put(modeCode, mode);
            log.info("注册消费模式: {} - {}", modeCode, mode.getModeName());

        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void unregisterMode(String modeCode) {
        if (modeCode == null || modeCode.trim().isEmpty()) {
            return;
        }

        lock.writeLock().lock();
        try {
            ConsumptionMode removed = modeRegistry.remove(modeCode);
            if (removed != null) {
                log.info("注销消费模式: {} - {}", modeCode, removed.getModeName());
                // 清理设备配置中的该模式
                cleanupDeviceMode(modeCode);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<ConsumptionMode> getSupportedModes() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(modeRegistry.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public ConsumptionMode getMode(String modeCode) {
        if (modeCode == null || modeCode.trim().isEmpty()) {
            return null;
        }

        lock.readLock().lock();
        try {
            return modeRegistry.get(modeCode);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isModeSupported(String modeCode) {
        return getMode(modeCode) != null;
    }

    @Override
    public Set<String> getDeviceSupportedModes(Long deviceId) {
        if (deviceId == null) {
            return Collections.emptySet();
        }

        lock.readLock().lock();
        try {
            Set<String> modes = deviceSupportedModes.get(deviceId);
            return modes != null ? new HashSet<>(modes) : Collections.emptySet();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String getDefaultMode(Long deviceId) {
        if (deviceId == null) {
            return null;
        }

        lock.readLock().lock();
        try {
            return deviceDefaultModes.get(deviceId);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void setDeviceSupportedModes(Long deviceId, Set<String> supportedModes) {
        if (deviceId == null) {
            return;
        }

        lock.writeLock().lock();
        try {
            if (supportedModes == null || supportedModes.isEmpty()) {
                deviceSupportedModes.remove(deviceId);
            } else {
                // 只保留引擎中存在的模式
                Set<String> validModes = new HashSet<>();
                for (String mode : supportedModes) {
                    if (isModeSupported(mode)) {
                        validModes.add(mode);
                    }
                }
                deviceSupportedModes.put(deviceId, validModes);
                log.info("设置设备 {} 支持的消费模式: {}", deviceId, validModes);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void setDeviceDefaultMode(Long deviceId, String defaultMode) {
        if (deviceId == null || defaultMode == null) {
            return;
        }

        lock.writeLock().lock();
        try {
            if (isModeSupported(defaultMode)) {
                deviceDefaultModes.put(deviceId, defaultMode);
                log.info("设置设备 {} 的默认消费模式: {}", deviceId, defaultMode);
            } else {
                log.warn("不支持的默认消费模式: {}", defaultMode);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean validateModeConfig(String modeCode, Map<String, Object> config) {
        ConsumptionMode mode = getMode(modeCode);
        if (mode == null) {
            return false;
        }
        return mode.validateConfig(config);
    }

    @Override
    public Map<String, Object> getModeConfigTemplate(String modeCode) {
        ConsumptionMode mode = getMode(modeCode);
        if (mode == null) {
            return Collections.emptyMap();
        }
        return mode.getConfigTemplate();
    }

    @Override
    public EngineStatus getEngineStatus() {
        return engineStatus;
    }

    /**
     * 清理设备配置中的指定模式
     *
     * @param modeCode 模式代码
     */
    private void cleanupDeviceMode(String modeCode) {
        deviceSupportedModes.values().forEach(modes -> modes.remove(modeCode));
        deviceDefaultModes.entrySet().removeIf(entry -> modeCode.equals(entry.getValue()));
    }

    /**
     * 引擎初始化
     */
    @PostConstruct
    public void initialize() {
        engineStatus = EngineStatus.RUNNING;
        log.info("消费模式引擎初始化完成，当前支持模式: {}",
                 modeRegistry.keySet());
    }

    /**
     * 引擎销毁
     */
    @PreDestroy
    public void destroy() {
        engineStatus = EngineStatus.STOPPED;

        // 销毁所有注册的模式
        modeRegistry.values().forEach(mode -> {
            try {
                mode.destroy();
            } catch (Exception e) {
                log.warn("销毁消费模式时发生异常: {}", mode.getModeCode(), e);
            }
        });

        modeRegistry.clear();
        deviceSupportedModes.clear();
        deviceDefaultModes.clear();

        log.info("消费模式引擎已销毁");
    }
}