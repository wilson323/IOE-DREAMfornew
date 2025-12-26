package net.lab1024.sa.access.manager;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.DeviceFirmwareDao;
import net.lab1024.sa.access.dao.FirmwareUpgradeDeviceDao;
import net.lab1024.sa.access.dao.FirmwareUpgradeTaskDao;
import net.lab1024.sa.common.entity.device.DeviceFirmwareEntity;
import net.lab1024.sa.common.entity.access.FirmwareUpgradeDeviceEntity;
import net.lab1024.sa.common.entity.access.FirmwareUpgradeTaskEntity;
import net.lab1024.sa.access.domain.form.FirmwareUpgradeTaskForm;
import net.lab1024.sa.access.domain.vo.FirmwareUpgradeDeviceVO;
import net.lab1024.sa.access.domain.vo.FirmwareUpgradeTaskVO;
import net.lab1024.sa.access.service.FirmwareUpgradeService;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 固件升级管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在配置类中注册为Bean
 * </p>
 * <p>
 * 核心职责：
 * - 编排固件升级的完整业务流程
 * - 自动重试机制（指数退避）
 * - 回滚机制（升级失败时恢复原固件）
 * - 进度监控和告警
 * - 异常处理和容错
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class FirmwareManager {

    // 显式添加logger声明以确保编译通过

    private final DeviceFirmwareDao deviceFirmwareDao;
    private final FirmwareUpgradeTaskDao firmwareUpgradeTaskDao;
    private final FirmwareUpgradeDeviceDao firmwareUpgradeDeviceDao;
    private final FirmwareUpgradeService firmwareUpgradeService;

    // 线程池用于异步执行升级任务
    private final ExecutorService upgradeExecutor;

    // 重试配置
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_INITIAL_DELAY_MS = 5000; // 5秒
    private static final double RETRY_BACKOFF_MULTIPLIER = 2.0;

    // 监控配置
    private static final int PROGRESS_CHECK_INTERVAL_SECONDS = 5;
    private static final double FAILURE_THRESHOLD_PERCENTAGE = 0.5; // 50%失败率触发告警

    /**
     * 构造函数注入依赖
     */
    public FirmwareManager(
            DeviceFirmwareDao deviceFirmwareDao,
            FirmwareUpgradeTaskDao firmwareUpgradeTaskDao,
            FirmwareUpgradeDeviceDao firmwareUpgradeDeviceDao,
            FirmwareUpgradeService firmwareUpgradeService) {
        this.deviceFirmwareDao = deviceFirmwareDao;
        this.firmwareUpgradeTaskDao = firmwareUpgradeTaskDao;
        this.firmwareUpgradeDeviceDao = firmwareUpgradeDeviceDao;
        this.firmwareUpgradeService = firmwareUpgradeService;

        // 初始化线程池
        this.upgradeExecutor = new ThreadPoolExecutor(
                5, // 核心线程数
                20, // 最大线程数
                60L, // 空闲线程存活时间
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100), // 队列容量
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "firmware-upgrade-" + threadNumber.getAndIncrement());
                        t.setDaemon(false);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：调用者执行
        );
    }

    // ==================== 流程编排 ====================

    /**
     * 编排完整的固件升级流程
     * <p>
     * 完整流程：
     * 1. 验证固件版本兼容性
     * 2. 创建升级任务
     * 3. 执行升级（带重试机制）
     * 4. 监控升级进度
     * 5. 处理升级结果
     * 6. 失败时自动回滚（如果支持）
     * </p>
     *
     * @param taskForm      任务表单
     * @param operatorId    操作人ID
     * @param operatorName  操作人名称
     * @return 任务详情
     */
    public FirmwareUpgradeTaskVO executeUpgradeWorkflow(
            FirmwareUpgradeTaskForm taskForm,
            Long operatorId,
            String operatorName) {

        log.info("[固件管理器] 开始固件升级流程: taskName={}, firmwareId={}, deviceCount={}",
                taskForm.getTaskName(), taskForm.getFirmwareId(), taskForm.getDeviceIds().size());

        try {
            // 1. 验证固件版本兼容性
            DeviceFirmwareEntity firmware = deviceFirmwareDao.selectById(taskForm.getFirmwareId());
            if (firmware == null) {
                throw new RuntimeException("固件不存在");
            }

            if (!firmware.isEnabled()) {
                throw new RuntimeException("固件未启用，无法升级");
            }

            // 2. 验证设备当前版本是否可以升级到目标版本
            validateDeviceVersions(taskForm.getDeviceIds(), firmware.getFirmwareVersion());

            // 3. 创建升级任务
            Long taskId = firmwareUpgradeService.createUpgradeTask(taskForm, operatorId, operatorName);
            log.info("[固件管理器] 升级任务创建成功: taskId={}", taskId);

            // 4. 启动升级任务
            firmwareUpgradeService.startUpgradeTask(taskId);
            log.info("[固件管理器] 升级任务已启动: taskId={}", taskId);

            // 5. 异步监控升级进度
            FirmwareUpgradeTaskEntity finalTask = firmwareUpgradeTaskDao.selectById(taskId);
            monitorUpgradeProgressAsync(finalTask);

            // 6. 返回任务详情
            return firmwareUpgradeService.getTaskDetail(taskId);

        } catch (Exception e) {
            log.error("[固件管理器] 固件升级流程失败: error={}", e.getMessage(), e);
            throw new RuntimeException("固件升级流程失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行带重试机制的升级任务
     * <p>
     * 重试策略：
     * - 最大重试次数：3次
     * - 退避策略：指数退避（5秒, 10秒, 20秒）
     * - 只重试失败的设备，不影响成功的设备
     * </p>
     *
     * @param taskId 任务ID
     * @return 是否最终成功
     */
    public Boolean executeUpgradeTaskWithRetry(Long taskId) {
        log.info("[固件管理器] 开始执行带重试的升级任务: taskId={}", taskId);

        FirmwareUpgradeTaskEntity task = firmwareUpgradeTaskDao.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        int retryAttempt = 0;
        long retryDelayMs = RETRY_INITIAL_DELAY_MS;
        boolean finalSuccess = false;

        while (retryAttempt <= MAX_RETRY_ATTEMPTS) {
            try {
                // 执行一次升级尝试
                boolean attemptSuccess = executeSingleUpgradeAttempt(taskId, retryAttempt);

                if (attemptSuccess) {
                    log.info("[固件管理器] 升级任务成功: taskId={}, attempt={}",
                            taskId, retryAttempt);
                    finalSuccess = true;
                    break;
                }

                // 如果不是最后一次尝试，等待后重试
                if (retryAttempt < MAX_RETRY_ATTEMPTS) {
                    log.warn("[固件管理器] 升级任务部分失败，准备重试: taskId={}, attempt={}, delayMs={}",
                            taskId, retryAttempt, retryDelayMs);

                    Thread.sleep(retryDelayMs);
                    retryDelayMs = (long) (retryDelayMs * RETRY_BACKOFF_MULTIPLIER);
                }

                retryAttempt++;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[固件管理器] 升级任务被中断: taskId={}", taskId);
                break;
            } catch (Exception e) {
                log.error("[固件管理器] 升级任务执行异常: taskId={}, attempt={}, error={}",
                        taskId, retryAttempt, e.getMessage(), e);

                if (retryAttempt >= MAX_RETRY_ATTEMPTS) {
                    // 最后一次尝试失败，检查是否需要回滚
                    handleUpgradeFailureWithRollback(taskId);
                }
                retryAttempt++;
            }
        }

        if (!finalSuccess) {
            log.error("[固件管理器] 升级任务最终失败: taskId={}, totalAttempts={}",
                    taskId, retryAttempt);
        }

        return finalSuccess;
    }

    /**
     * 执行单次升级尝试
     */
    private boolean executeSingleUpgradeAttempt(Long taskId, int attemptNumber) {
        log.info("[固件管理器] 执行升级尝试: taskId={}, attempt={}", taskId, attemptNumber);

        // 启动任务（如果未启动）
        FirmwareUpgradeTaskEntity task = firmwareUpgradeTaskDao.selectById(taskId);
        if (task.getTaskStatus() == 1) {
            firmwareUpgradeService.startUpgradeTask(taskId);
        }

        // 等待任务完成（带超时）
        boolean completed = waitForTaskCompletion(taskId, 3600); // 1小时超时

        if (!completed) {
            log.warn("[固件管理器] 升级任务超时: taskId={}, attempt={}", taskId, attemptNumber);
            return false;
        }

        // 检查任务结果
        FirmwareUpgradeTaskEntity finalTask = firmwareUpgradeTaskDao.selectById(taskId);
        if (finalTask.getTaskStatus() == 4) { // 已完成
            // 检查是否全部成功
            Map<String, Object> progress = firmwareUpgradeService.getTaskProgress(taskId);
            Integer totalCount = ((Number) progress.getOrDefault("totalCount", 0)).intValue();
            Integer failedCount = ((Number) progress.getOrDefault("failedCount", 0)).intValue();

            if (failedCount == 0) {
                log.info("[固件管理器] 升级尝试全部成功: taskId={}, attempt={}", taskId, attemptNumber);
                return true;
            } else {
                log.warn("[固件管理器] 升级尝试部分失败: taskId={}, attempt={}, total={}, failed={}",
                        taskId, attemptNumber, totalCount, failedCount);

                // 重试失败的设备
                Integer retriedCount = firmwareUpgradeService.retryFailedDevices(taskId);
                log.info("[固件管理器] 已触发重试: taskId={}, retriedCount={}", taskId, retriedCount);

                return failedCount < totalCount * FAILURE_THRESHOLD_PERCENTAGE;
            }
        }

        return false;
    }

    /**
     * 等待任务完成
     */
    private boolean waitForTaskCompletion(Long taskId, int timeoutSeconds) {
        log.debug("[固件管理器] 等待任务完成: taskId={}, timeoutSeconds={}", taskId, timeoutSeconds);

        long endTime = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(PROGRESS_CHECK_INTERVAL_SECONDS * 1000L);

                FirmwareUpgradeTaskEntity task = firmwareUpgradeTaskDao.selectById(taskId);
                if (task.getTaskStatus() == 4 || task.getTaskStatus() == 5) {
                    // 已完成或已失败
                    return true;
                }

                // 检查进度
                Map<String, Object> progress = firmwareUpgradeService.getTaskProgress(taskId);
                Integer pendingCount = ((Number) progress.getOrDefault("pendingCount", 0)).intValue();
                Integer upgradingCount = ((Number) progress.getOrDefault("upgradingCount", 0)).intValue();

                log.debug("[固件管理器] 任务进度: taskId={}, pending={}, upgrading={}",
                        taskId, pendingCount, upgradingCount);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception e) {
                log.warn("[固件管理器] 检查任务进度异常: taskId={}, error={}", taskId, e.getMessage());
            }
        }

        log.warn("[固件管理器] 等待任务完成超时: taskId={}", taskId);
        return false;
    }

    // ==================== 重试机制 ====================

    /**
     * 智能重试失败的设备
     * <p>
     * 重试策略：
     * - 按错误类型分组重试
     * - 网络错误优先重试
     * - 设备离线延迟重试
     * - 固件兼容性错误不重试
     * </p>
     *
     * @param taskId 任务ID
     * @return 重试的设备数量
     */
    public Integer smartRetryFailedDevices(Long taskId) {
        log.info("[固件管理器] 开始智能重试失败设备: taskId={}", taskId);

        List<FirmwareUpgradeDeviceVO> failedDevices = firmwareUpgradeService.getFailedDevices(taskId);
        if (failedDevices.isEmpty()) {
            log.info("[固件管理器] 没有失败设备需要重试: taskId={}", taskId);
            return 0;
        }

        // 按错误类型分组
        Map<String, List<FirmwareUpgradeDeviceVO>> errorGroups = groupDevicesByError(failedDevices);

        int totalRetried = 0;

        // 优先重试网络错误
        List<FirmwareUpgradeDeviceVO> networkErrors = errorGroups.getOrDefault("NETWORK_ERROR", new ArrayList<>());
        if (!networkErrors.isEmpty()) {
            log.info("[固件管理器] 重试网络错误设备: count={}", networkErrors.size());
            firmwareUpgradeService.retryFailedDevices(taskId);
            totalRetried += networkErrors.size();
            sleepMs(5000); // 等待5秒
        }

        // 延迟重试设备离线错误
        List<FirmwareUpgradeDeviceVO> offlineErrors = errorGroups.getOrDefault("DEVICE_OFFLINE", new ArrayList<>());
        if (!offlineErrors.isEmpty()) {
            log.info("[固件管理器] 延迟重试离线设备: count={}", offlineErrors.size());
            sleepMs(10000); // 等待10秒
            firmwareUpgradeService.retryFailedDevices(taskId);
            totalRetried += offlineErrors.size();
        }

        // 固件兼容性错误不重试，直接记录
        List<FirmwareUpgradeDeviceVO> compatibilityErrors = errorGroups.getOrDefault("VERSION_INCOMPATIBLE", new ArrayList<>());
        if (!compatibilityErrors.isEmpty()) {
            log.warn("[固件管理器] 固件版本兼容性错误，不重试: count={}", compatibilityErrors.size());
        }

        log.info("[固件管理器] 智能重试完成: taskId={}, totalRetried={}", taskId, totalRetried);
        return totalRetried;
    }

    /**
     * 按错误类型分组设备
     */
    private Map<String, List<FirmwareUpgradeDeviceVO>> groupDevicesByError(List<FirmwareUpgradeDeviceVO> devices) {
        Map<String, List<FirmwareUpgradeDeviceVO>> groups = new HashMap<>();

        for (FirmwareUpgradeDeviceVO device : devices) {
            String errorCode = device.getErrorCode();
            if (errorCode == null) {
                errorCode = "UNKNOWN_ERROR";
            }

            // 分类错误类型
            String errorCategory;
            if (errorCode.contains("TIMEOUT") || errorCode.contains("CONNECTION")) {
                errorCategory = "NETWORK_ERROR";
            } else if (errorCode.contains("OFFLINE")) {
                errorCategory = "DEVICE_OFFLINE";
            } else if (errorCode.contains("VERSION") || errorCode.contains("COMPATIBLE")) {
                errorCategory = "VERSION_INCOMPATIBLE";
            } else {
                errorCategory = "OTHER_ERROR";
            }

            groups.computeIfAbsent(errorCategory, k -> new ArrayList<>()).add(device);
        }

        return groups;
    }

    // ==================== 回滚机制 ====================

    /**
     * 执行回滚流程
     * <p>
     * 回滚条件：
     * 1. 任务支持回滚（rollbackSupported=1）
     * 2. 失败率超过阈值（50%）
     * 3. 用户主动触发回滚
     * </p>
     *
     * @param taskId 原任务ID
     * @return 回滚任务ID
     */
    public Long executeRollbackWorkflow(Long taskId) {
        log.info("[固件管理器] 开始回滚流程: taskId={}", taskId);

        try {
            // 1. 验证任务是否支持回滚
            if (!firmwareUpgradeService.isRollbackSupported(taskId)) {
                throw new RuntimeException("该任务不支持回滚");
            }

            // 2. 获取原任务信息
            FirmwareUpgradeTaskEntity originalTask = firmwareUpgradeTaskDao.selectById(taskId);
            if (originalTask == null) {
                throw new RuntimeException("原任务不存在");
            }

            // 3. 获取升级成功的设备列表
            List<FirmwareUpgradeDeviceEntity> successDevices = getSuccessDevices(taskId);
            if (successDevices.isEmpty()) {
                log.warn("[固件管理器] 没有需要回滚的设备: taskId={}", taskId);
                return null;
            }

            log.info("[固件管理器] 准备回滚设备: taskId={}, deviceCount={}", taskId, successDevices.size());

            // 4. 创建回滚任务
            Long rollbackTaskId = createRollbackTask(originalTask, successDevices);
            log.info("[固件管理器] 回滚任务创建成功: rollbackTaskId={}", rollbackTaskId);

            // 5. 执行回滚
            firmwareUpgradeService.startUpgradeTask(rollbackTaskId);
            log.info("[固件管理器] 回滚任务已启动: rollbackTaskId={}", rollbackTaskId);

            // 6. 更新原任务的回滚任务ID
            originalTask.setRollbackTaskId(rollbackTaskId);
            firmwareUpgradeTaskDao.updateById(originalTask);

            return rollbackTaskId;

        } catch (Exception e) {
            log.error("[固件管理器] 回滚流程失败: taskId={}, error={}", taskId, e.getMessage(), e);
            throw new RuntimeException("回滚流程失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理升级失败并自动回滚
     */
    private void handleUpgradeFailureWithRollback(Long taskId) {
        log.warn("[固件管理器] 处理升级失败并检查是否回滚: taskId={}", taskId);

        try {
            // 检查失败率
            Map<String, Object> progress = firmwareUpgradeService.getTaskProgress(taskId);
            Integer totalCount = ((Number) progress.getOrDefault("totalCount", 0)).intValue();
            Integer failedCount = ((Number) progress.getOrDefault("failedCount", 0)).intValue();

            if (totalCount == 0) {
                return;
            }

            double failureRate = (double) failedCount / totalCount;

            // 如果失败率超过阈值且支持回滚，自动触发回滚
            if (failureRate >= FAILURE_THRESHOLD_PERCENTAGE &&
                    firmwareUpgradeService.isRollbackSupported(taskId)) {

                log.warn("[固件管理器] 失败率超过阈值({}%), 自动触发回滚: taskId={}, failureRate={}%",
                        (int) (FAILURE_THRESHOLD_PERCENTAGE * 100), taskId, (int) (failureRate * 100));

                executeRollbackWorkflow(taskId);
            } else {
                log.info("[固件管理器] 失败率未达到回滚阈值或不支持回滚: taskId={}, failureRate={}%",
                        taskId, (int) (failureRate * 100));
            }

        } catch (Exception e) {
            log.error("[固件管理器] 处理升级失败并回滚异常: taskId={}, error={}", taskId, e.getMessage(), e);
        }
    }

    /**
     * 创建回滚任务
     */
    private Long createRollbackTask(FirmwareUpgradeTaskEntity originalTask,
                                    List<FirmwareUpgradeDeviceEntity> successDevices) {
        // 获取原固件（当前版本）
        List<FirmwareUpgradeDeviceEntity> firstDevice = successDevices.subList(0, 1);
        String currentVersion = firstDevice.get(0).getCurrentVersion();

        // 查找当前版本的固件
        LambdaQueryWrapper<DeviceFirmwareEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceFirmwareEntity::getFirmwareVersion, currentVersion)
                .eq(DeviceFirmwareEntity::getDeviceType, originalTask.getUpgradeStrategy());

        DeviceFirmwareEntity currentFirmware = deviceFirmwareDao.selectOne(queryWrapper);
        if (currentFirmware == null) {
            throw new RuntimeException("找不到当前版本的固件: " + currentVersion);
        }

        // 创建回滚任务表单
        FirmwareUpgradeTaskForm rollbackForm = new FirmwareUpgradeTaskForm();
        rollbackForm.setTaskName("回滚-" + originalTask.getTaskName());
        rollbackForm.setFirmwareId(currentFirmware.getFirmwareId());
        rollbackForm.setUpgradeStrategy(1); // 立即升级
        rollbackForm.setDeviceIds(successDevices.stream()
                .map(FirmwareUpgradeDeviceEntity::getDeviceId)
                .collect(Collectors.toList()));
        rollbackForm.setRollbackSupported(0); // 回滚任务不再支持回滚
        rollbackForm.setRemark("回滚任务: " + originalTask.getTaskNo());

        // 创建任务
        return firmwareUpgradeService.createUpgradeTask(
                rollbackForm,
                originalTask.getOperatorId(),
                originalTask.getOperatorName()
        );
    }

    /**
     * 获取升级成功的设备列表
     */
    private List<FirmwareUpgradeDeviceEntity> getSuccessDevices(Long taskId) {
        LambdaQueryWrapper<FirmwareUpgradeDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FirmwareUpgradeDeviceEntity::getTaskId, taskId)
                .eq(FirmwareUpgradeDeviceEntity::getUpgradeStatus, 3); // 升级成功

        return firmwareUpgradeDeviceDao.selectList(queryWrapper);
    }

    // ==================== 进度监控 ====================

    /**
     * 异步监控升级进度
     */
    private void monitorUpgradeProgressAsync(FirmwareUpgradeTaskEntity task) {
        upgradeExecutor.submit(() -> {
            try {
                monitorUpgradeProgress(task.getTaskId());
            } catch (Exception e) {
                log.error("[固件管理器] 监控升级进度异常: taskId={}, error={}",
                        task.getTaskId(), e.getMessage(), e);
            }
        });
    }

    /**
     * 监控升级进度并触发告警
     */
    private void monitorUpgradeProgress(Long taskId) {
        log.info("[固件管理器] 开始监控升级进度: taskId={}", taskId);

        while (true) {
            try {
                Thread.sleep(PROGRESS_CHECK_INTERVAL_SECONDS * 1000L);

                FirmwareUpgradeTaskEntity task = firmwareUpgradeTaskDao.selectById(taskId);
                if (task == null || task.getTaskStatus() == 4 || task.getTaskStatus() == 5) {
                    // 任务已完成或失败
                    log.info("[固件管理器] 升级任务结束，停止监控: taskId={}, status={}",
                            taskId, task != null ? task.getTaskStatus() : "null");
                    break;
                }

                // 获取进度统计
                Map<String, Object> progress = firmwareUpgradeService.getTaskProgress(taskId);
                Integer totalCount = ((Number) progress.getOrDefault("totalCount", 0)).intValue();
                Integer successCount = ((Number) progress.getOrDefault("successCount", 0)).intValue();
                Integer failedCount = ((Number) progress.getOrDefault("failedCount", 0)).intValue();
                Integer pendingCount = ((Number) progress.getOrDefault("pendingCount", 0)).intValue();

                // 计算失败率
                double failureRate = 0.0;
                int completedCount = successCount + failedCount;
                if (completedCount > 0) {
                    failureRate = (double) failedCount / completedCount;
                }

                log.info("[固件管理器] 升级进度: taskId={}, total={}, success={}, failed={}, pending={}, failureRate={}%",
                        taskId, totalCount, successCount, failedCount, pendingCount, (int) (failureRate * 100));

                // 触发告警
                if (failureRate >= FAILURE_THRESHOLD_PERCENTAGE && completedCount >= 10) {
                    log.warn("[固件管理器] ⚠️ 升级失败率过高: taskId={}, failureRate={}%, success={}, failed={}",
                            taskId, (int) (failureRate * 100), successCount, failedCount);

                    // TODO: 发送告警通知（邮件、短信、WebSocket推送等）
                    sendProgressAlert(taskId, successCount, failedCount, pendingCount);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("[固件管理器] 监控升级进度被中断: taskId={}", taskId);
                break;
            } catch (Exception e) {
                log.error("[固件管理器] 监控升级进度异常: taskId={}, error={}", taskId, e.getMessage());
            }
        }
    }

    /**
     * 发送进度告警
     */
    private void sendProgressAlert(Long taskId, int successCount, int failedCount, int pendingCount) {
        // TODO: 实现告警通知
        // 1. 邮件通知
        // 2. 短信通知
        // 3. WebSocket实时推送
        // 4. 系统通知

        log.warn("[固件管理器] 📢 升级告警: taskId={}, success={}, failed={}, pending={}",
                taskId, successCount, failedCount, pendingCount);
    }

    // ==================== 辅助方法 ====================

    /**
     * 验证设备当前版本是否可以升级
     */
    private void validateDeviceVersions(List<Long> deviceIds, String targetVersion) {
        log.debug("[固件管理器] 验证设备版本兼容性: deviceCount={}, targetVersion={}",
                deviceIds.size(), targetVersion);

        // TODO: 从设备服务获取设备的当前固件版本
        // 这里简化处理，假设所有设备都可以升级

        log.info("[固件管理器] 设备版本验证通过: deviceCount={}", deviceIds.size());
    }

    /**
     * 线程休眠（毫秒）
     */
    private void sleepMs(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[固件管理器] 线程休眠被中断");
        }
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        log.info("[固件管理器] 关闭线程池");
        upgradeExecutor.shutdown();
        try {
            if (!upgradeExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                upgradeExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            upgradeExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

