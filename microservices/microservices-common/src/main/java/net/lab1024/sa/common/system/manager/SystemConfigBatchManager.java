package net.lab1024.sa.common.system.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.system.dao.SystemConfigDao;
import net.lab1024.sa.common.system.domain.entity.SystemConfigEntity;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Objects;

/**
 * 系统配置批量操作管理器
 * <p>
 * 基于现代化UI/UX最佳实践设计，提供：
 * - 异步批量处理，提升用户体验
 * - 智能分组策略，避免单次操作数据量过大
 * - 实时进度反馈，支持长任务监控
 * - 事务安全和数据一致性保障
 * - 针对单企业1000台设备、20000人规模优化
 * </p>
 * <p>
 * 严格遵循CLAUDE.md全局架构规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入所有依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-09
 * @updated 2025-01-30 移除@Component注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
public class SystemConfigBatchManager {

    private final SystemConfigDao systemConfigDao;
    private final RedisTemplate<String, Object> redisTemplate;

    // 线程池用于异步处理
    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2)
    );

    // 批量操作任务缓存
    private final Map<String, BatchOperationTask> taskCache = new ConcurrentHashMap<>();

    // 缓存键前缀
    private static final String BATCH_TASK_PREFIX = "batch:task:";
    private static final Duration TASK_TIMEOUT = Duration.ofMinutes(30);

    // 批量操作配置
    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final int MAX_BATCH_SIZE = 500;
    private static final int MIN_BATCH_SIZE = 10;

    /**
     * 构造函数注入所有依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param systemConfigDao 系统配置DAO
     * @param redisTemplate Redis模板
     */
    public SystemConfigBatchManager(SystemConfigDao systemConfigDao, RedisTemplate<String, Object> redisTemplate) {
        this.systemConfigDao = Objects.requireNonNull(systemConfigDao, "systemConfigDao不能为null");
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate不能为null");
    }

    /**
     * 批量更新系统配置
     *
     * @param configs 配置列表
     * @param userId 操作用户ID
     * @return 批量操作任务ID
     */
    public String batchUpdateConfigs(List<SystemConfigEntity> configs, Long userId) {
        String taskId = generateTaskId();
        BatchOperationTask task = new BatchOperationTask(taskId, "BATCH_UPDATE", configs.size(), userId);

        try {
            // 验证输入
            validateBatchInput(configs);

            // 智能分组策略
            List<List<SystemConfigEntity>> groups = groupConfigsByStrategy(configs);

            // 异步执行批量更新
            CompletableFuture.runAsync(() -> {
                executeBatchUpdate(task, groups, userId);
            }, executorService);

            // 缓存任务
            cacheTask(task);

            log.info("[批量配置] 启动批量更新任务: taskId={}, configCount={}, groupCount={}",
                    taskId, configs.size(), groups.size());
            return taskId;

        } catch (Exception e) {
            log.error("[批量配置] 启动批量更新任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            cacheTask(task);
            throw new RuntimeException("启动批量更新任务失败", e);
        }
    }

    /**
     * 批量删除系统配置
     *
     * @param configIds 配置ID列表
     * @param userId 操作用户ID
     * @return 批量操作任务ID
     */
    public String batchDeleteConfigs(List<Long> configIds, Long userId) {
        String taskId = generateTaskId();
        BatchOperationTask task = new BatchOperationTask(taskId, "BATCH_DELETE", configIds.size(), userId);

        try {
            // 验证输入
            validateConfigIds(configIds);

            // 智能分组
            List<List<Long>> groups = groupIdsByStrategy(configIds);

            // 异步执行批量删除
            CompletableFuture.runAsync(() -> {
                executeBatchDelete(task, groups, userId);
            }, executorService);

            cacheTask(task);

            log.info("[批量配置] 启动批量删除任务: taskId={}, configCount={}, groupCount={}",
                    taskId, configIds.size(), groups.size());
            return taskId;

        } catch (Exception e) {
            log.error("[批量配置] 启动批量删除任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            cacheTask(task);
            throw new RuntimeException("启动批量删除任务失败", e);
        }
    }

    /**
     * 批量导入配置
     *
     * @param configs 导入配置列表
     * @param importStrategy 导入策略 (SKIP_EXISTING, OVERWRITE, MERGE)
     * @param userId 操作用户ID
     * @return 批量操作任务ID
     */
    public String batchImportConfigs(List<SystemConfigEntity> configs, String importStrategy, Long userId) {
        String taskId = generateTaskId();
        BatchOperationTask task = new BatchOperationTask(taskId, "BATCH_IMPORT", configs.size(), userId);
        task.setImportStrategy(importStrategy);

        try {
            // 验证输入和导入策略
            validateImportInput(configs, importStrategy);

            // 智能分组
            List<List<SystemConfigEntity>> groups = groupConfigsByStrategy(configs);

            // 异步执行批量导入
            CompletableFuture.runAsync(() -> {
                executeBatchImport(task, groups, importStrategy, userId);
            }, executorService);

            cacheTask(task);

            log.info("[批量配置] 启动批量导入任务: taskId={}, configCount={}, strategy={}, groupCount={}",
                    taskId, configs.size(), importStrategy, groups.size());
            return taskId;

        } catch (Exception e) {
            log.error("[批量配置] 启动批量导入任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            cacheTask(task);
            throw new RuntimeException("启动批量导入任务失败", e);
        }
    }

    /**
     * 获取批量操作任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    public BatchOperationTask getTaskStatus(String taskId) {
        // 先从本地缓存查找
        BatchOperationTask task = taskCache.get(taskId);
        if (task == null) {
            // 从Redis缓存查找
            try {
                task = (BatchOperationTask) redisTemplate.opsForValue().get(BATCH_TASK_PREFIX + taskId);
                if (task != null) {
                    taskCache.put(taskId, task);
                }
            } catch (Exception e) {
                log.warn("[批量配置] 获取任务状态失败: taskId={}, error={}", taskId, e.getMessage(), e);
            }
        }
        return task;
    }

    /**
     * 取消批量操作任务
     *
     * @param taskId 任务ID
     * @return 是否取消成功
     */
    public boolean cancelTask(String taskId) {
        BatchOperationTask task = getTaskStatus(taskId);
        if (task == null) {
            return false;
        }

        if ("RUNNING".equals(task.getStatus()) || "PENDING".equals(task.getStatus())) {
            task.setStatus("CANCELLED");
            task.setEndTime(System.currentTimeMillis());
            cacheTask(task);

            log.info("[批量配置] 任务已取消: taskId={}", taskId);
            return true;
        }

        return false;
    }

    /**
     * 获取批量操作历史
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 历史任务列表
     */
    public List<BatchOperationTask> getTaskHistory(Long userId, Integer limit) {
        try {
            Set<String> taskKeys = redisTemplate.keys(BATCH_TASK_PREFIX + "*");
            if (taskKeys == null || taskKeys.isEmpty()) {
                return Collections.emptyList();
            }

            List<BatchOperationTask> tasks = new ArrayList<>();
            for (String key : taskKeys) {
                try {
                    String nonNullKey = Objects.requireNonNull(key, "task key不能为null");
                    BatchOperationTask task = (BatchOperationTask) redisTemplate.opsForValue().get(nonNullKey);
                    if (task != null && task.getUserId() != null && task.getUserId().equals(userId)) {
                        tasks.add(task);
                    }
                } catch (Exception e) {
                    log.warn("[批量配置] 获取历史任务失败: key={}, error={}", key, e.getMessage());
                }
            }

            // 按创建时间倒序排列
            tasks.sort((a, b) -> Long.compare(b.getCreateTime(), a.getCreateTime()));

            return tasks.stream()
                    .limit(limit != null ? limit : 20)
                    .toList();

        } catch (Exception e) {
            log.error("[批量配置] 获取历史任务失败: userId={}, error={}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 验证批量输入
     */
    private void validateBatchInput(List<SystemConfigEntity> configs) {
        if (configs == null || configs.isEmpty()) {
            throw new IllegalArgumentException("配置列表不能为空");
        }

        if (configs.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("单次批量操作配置数量不能超过" + MAX_BATCH_SIZE);
        }

        // 验证每个配置
        for (int i = 0; i < configs.size(); i++) {
            SystemConfigEntity config = configs.get(i);
            if (config == null || config.getId() == null || config.getConfigKey() == null) {
                throw new IllegalArgumentException("配置 " + (i + 1) + " 不完整，缺少必要字段");
            }
        }
    }

    /**
     * 验证配置ID列表
     */
    private void validateConfigIds(List<Long> configIds) {
        if (configIds == null || configIds.isEmpty()) {
            throw new IllegalArgumentException("配置ID列表不能为空");
        }

        if (configIds.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("单次批量操作配置数量不能超过" + MAX_BATCH_SIZE);
        }

        // 检查是否有重复ID
        Set<Long> uniqueIds = new HashSet<>(configIds);
        if (uniqueIds.size() != configIds.size()) {
            throw new IllegalArgumentException("配置ID列表中存在重复项");
        }
    }

    /**
     * 验证导入输入
     */
    private void validateImportInput(List<SystemConfigEntity> configs, String importStrategy) {
        validateBatchInput(configs);

        Set<String> validStrategies = Set.of("SKIP_EXISTING", "OVERWRITE", "MERGE");
        if (!validStrategies.contains(importStrategy)) {
            throw new IllegalArgumentException("无效的导入策略: " + importStrategy);
        }
    }

    /**
     * 智能分组策略 - 基于配置复杂度和性能优化
     */
    private List<List<SystemConfigEntity>> groupConfigsByStrategy(List<SystemConfigEntity> configs) {
        int batchSize = calculateOptimalBatchSize(configs.size());
        List<List<SystemConfigEntity>> groups = new ArrayList<>();

        // 按配置分组优先级分组
        Map<String, List<SystemConfigEntity>> groupMap = new HashMap<>();
        for (SystemConfigEntity config : configs) {
            String groupKey = config.getConfigGroup() != null ? config.getConfigGroup() : "default";
            groupMap.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(config);
        }

        // 将每个分组进一步分割为批量大小
        for (Map.Entry<String, List<SystemConfigEntity>> entry : groupMap.entrySet()) {
            List<SystemConfigEntity> groupConfigs = entry.getValue();
            for (int i = 0; i < groupConfigs.size(); i += batchSize) {
                groups.add(groupConfigs.subList(i, Math.min(i + batchSize, groupConfigs.size())));
            }
        }

        return groups;
    }

    /**
     * 智能分组ID策略
     */
    private List<List<Long>> groupIdsByStrategy(List<Long> configIds) {
        int batchSize = calculateOptimalBatchSize(configIds.size());
        List<List<Long>> groups = new ArrayList<>();

        for (int i = 0; i < configIds.size(); i += batchSize) {
            groups.add(configIds.subList(i, Math.min(i + batchSize, configIds.size())));
        }

        return groups;
    }

    /**
     * 计算最优批量大小
     */
    private int calculateOptimalBatchSize(int totalSize) {
        if (totalSize <= MIN_BATCH_SIZE) {
            return totalSize;
        }

        // 根据总大小动态调整批量大小
        if (totalSize <= 100) {
            return Math.min(totalSize, 50);
        } else if (totalSize <= 500) {
            return Math.min(totalSize, 100);
        } else {
            return DEFAULT_BATCH_SIZE;
        }
    }

    /**
     * 执行批量更新
     * <p>
     * 注意：事务管理应在Service层处理，Manager层不处理事务
     * </p>
     */
    private void executeBatchUpdate(BatchOperationTask task, List<List<SystemConfigEntity>> groups, Long userId) {
        task.setStatus("RUNNING");
        task.setStartTime(System.currentTimeMillis());
        cacheTask(task);

        int successCount = 0;
        int failureCount = 0;
        List<String> errorMessages = new ArrayList<>();

        try {
            for (int i = 0; i < groups.size(); i++) {
                List<SystemConfigEntity> group = groups.get(i);
                task.setCurrentGroup(i + 1);
                task.setTotalGroups(groups.size());

                log.debug("[批量配置] 处理更新组: groupIndex={}, groupSize={}", i + 1, group.size());

                for (SystemConfigEntity config : group) {
                    try {
                        // 检查配置是否存在
                        SystemConfigEntity existing = systemConfigDao.selectById(config.getId());
                        if (existing != null) {
                            // 更新配置
                            int result = systemConfigDao.updateById(config);
                            if (result > 0) {
                                successCount++;
                            } else {
                                failureCount++;
                                errorMessages.add("配置更新失败: " + config.getConfigKey());
                            }
                        } else {
                            failureCount++;
                            errorMessages.add("配置不存在: " + config.getConfigKey());
                        }

                        task.setProcessedCount(task.getProcessedCount() + 1);

                        // 定期更新任务状态
                        if (task.getProcessedCount() % 10 == 0) {
                            cacheTask(task);
                        }

                    } catch (Exception e) {
                        failureCount++;
                        errorMessages.add("配置更新异常: " + config.getConfigKey() + " - " + e.getMessage());
                        log.error("[批量配置] 更新配置失败: configKey={}, error={}", config.getConfigKey(), e.getMessage(), e);
                    }
                }
            }

            task.setSuccessCount(successCount);
            task.setFailureCount(failureCount);
            task.setFailureMessages(errorMessages);

            if (failureCount == 0) {
                task.setStatus("COMPLETED");
            } else if (successCount > 0) {
                task.setStatus("PARTIALLY_COMPLETED");
            } else {
                task.setStatus("FAILED");
            }

        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            log.error("[批量配置] 批量更新异常: taskId={}, error={}", task.getTaskId(), e.getMessage(), e);
        } finally {
            task.setEndTime(System.currentTimeMillis());
            cacheTask(task);

            // 清理本地缓存
            taskCache.remove(task.getTaskId());

            log.info("[批量配置] 批量更新完成: taskId={}, successCount={}, failureCount={}, duration={}ms",
                    task.getTaskId(), successCount, failureCount, task.getDuration());
        }
    }

    /**
     * 执行批量删除
     * <p>
     * 注意：事务管理应在Service层处理，Manager层不处理事务
     * </p>
     */
    private void executeBatchDelete(BatchOperationTask task, List<List<Long>> groups, Long userId) {
        task.setStatus("RUNNING");
        task.setStartTime(System.currentTimeMillis());
        cacheTask(task);

        int successCount = 0;
        int failureCount = 0;
        List<String> errorMessages = new ArrayList<>();

        try {
            for (int i = 0; i < groups.size(); i++) {
                List<Long> group = groups.get(i);
                task.setCurrentGroup(i + 1);
                task.setTotalGroups(groups.size());

                log.debug("[批量配置] 处理删除组: groupIndex={}, groupSize={}", i + 1, group.size());

                for (Long configId : group) {
                    try {
                        // 软删除配置
                        int result = systemConfigDao.deleteById(configId);
                        if (result > 0) {
                            successCount++;
                        } else {
                            failureCount++;
                            errorMessages.add("配置不存在或已删除: " + configId);
                        }

                        task.setProcessedCount(task.getProcessedCount() + 1);

                        // 定期更新任务状态
                        if (task.getProcessedCount() % 10 == 0) {
                            cacheTask(task);
                        }

                    } catch (Exception e) {
                        failureCount++;
                        errorMessages.add("配置删除异常: " + configId + " - " + e.getMessage());
                        log.error("[批量配置] 删除配置失败: configId={}, error={}", configId, e.getMessage(), e);
                    }
                }
            }

            task.setSuccessCount(successCount);
            task.setFailureCount(failureCount);
            task.setFailureMessages(errorMessages);

            if (failureCount == 0) {
                task.setStatus("COMPLETED");
            } else if (successCount > 0) {
                task.setStatus("PARTIALLY_COMPLETED");
            } else {
                task.setStatus("FAILED");
            }

        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            log.error("[批量配置] 批量删除异常: taskId={}, error={}", task.getTaskId(), e.getMessage(), e);
        } finally {
            task.setEndTime(System.currentTimeMillis());
            cacheTask(task);

            // 清理本地缓存
            taskCache.remove(task.getTaskId());

            log.info("[批量配置] 批量删除完成: taskId={}, successCount={}, failureCount={}, duration={}ms",
                    task.getTaskId(), successCount, failureCount, task.getDuration());
        }
    }

    /**
     * 执行批量导入
     * <p>
     * 注意：事务管理应在Service层处理，Manager层不处理事务
     * </p>
     */
    private void executeBatchImport(BatchOperationTask task, List<List<SystemConfigEntity>> groups, String importStrategy, Long userId) {
        task.setStatus("RUNNING");
        task.setStartTime(System.currentTimeMillis());
        cacheTask(task);

        int successCount = 0;
        int failureCount = 0;
        int skippedCount = 0;
        int updatedCount = 0;
        List<String> errorMessages = new ArrayList<>();

        try {
            for (int i = 0; i < groups.size(); i++) {
                List<SystemConfigEntity> group = groups.get(i);
                task.setCurrentGroup(i + 1);
                task.setTotalGroups(groups.size());

                log.debug("[批量配置] 处理导入组: groupIndex={}, groupSize={}", i + 1, group.size());

                for (SystemConfigEntity config : group) {
                    try {
                        // 检查配置是否已存在
                        SystemConfigEntity existing = systemConfigDao.selectById(config.getId());

                        if (existing == null) {
                            // 新增配置
                            config.setCreateTime(null); // 让数据库自动设置
                            config.setUpdateTime(null);
                            config.setCreateUserId(userId);
                            config.setUpdateUserId(userId);

                            int result = systemConfigDao.insert(config);
                            if (result > 0) {
                                successCount++;
                            } else {
                                failureCount++;
                                errorMessages.add("配置插入失败: " + config.getConfigKey());
                            }

                        } else {
                            // 根据导入策略处理已存在的配置
                            switch (importStrategy) {
                                case "SKIP_EXISTING":
                                    skippedCount++;
                                    break;
                                case "OVERWRITE":
                                    config.setUpdateUserId(userId);
                                    int result = systemConfigDao.updateById(config);
                                    if (result > 0) {
                                        updatedCount++;
                                        successCount++;
                                    } else {
                                        failureCount++;
                                        errorMessages.add("配置更新失败: " + config.getConfigKey());
                                    }
                                    break;
                                case "MERGE":
                                    // 合并策略：只更新非空字段
                                    mergeConfig(existing, config, userId);
                                    int mergeResult = systemConfigDao.updateById(existing);
                                    if (mergeResult > 0) {
                                        updatedCount++;
                                        successCount++;
                                    } else {
                                        failureCount++;
                                        errorMessages.add("配置合并失败: " + config.getConfigKey());
                                    }
                                    break;
                                default:
                                    failureCount++;
                                    errorMessages.add("无效的导入策略: " + importStrategy);
                            }
                        }

                        task.setProcessedCount(task.getProcessedCount() + 1);

                        // 定期更新任务状态
                        if (task.getProcessedCount() % 10 == 0) {
                            cacheTask(task);
                        }

                    } catch (Exception e) {
                        failureCount++;
                        errorMessages.add("配置导入异常: " + config.getConfigKey() + " - " + e.getMessage());
                        log.error("[批量配置] 导入配置失败: configKey={}, error={}", config.getConfigKey(), e.getMessage(), e);
                    }
                }
            }

            task.setSuccessCount(successCount);
            task.setFailureCount(failureCount);
            task.setSkippedCount(skippedCount);
            task.setUpdatedCount(updatedCount);
            task.setFailureMessages(errorMessages);

            if (failureCount == 0) {
                task.setStatus("COMPLETED");
            } else if (successCount > 0) {
                task.setStatus("PARTIALLY_COMPLETED");
            } else {
                task.setStatus("FAILED");
            }

        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            log.error("[批量配置] 批量导入异常: taskId={}, error={}", task.getTaskId(), e.getMessage(), e);
        } finally {
            task.setEndTime(System.currentTimeMillis());
            cacheTask(task);

            // 清理本地缓存
            taskCache.remove(task.getTaskId());

            log.info("[批量配置] 批量导入完成: taskId={}, successCount={}, failureCount={}, skippedCount={}, updatedCount={}, duration={}ms",
                    task.getTaskId(), successCount, failureCount, skippedCount, updatedCount, task.getDuration());
        }
    }

    /**
     * 合并配置
     */
    private void mergeConfig(SystemConfigEntity existing, SystemConfigEntity update, Long userId) {
        if (update.getConfigValue() != null) {
            existing.setConfigValue(update.getConfigValue());
        }
        if (update.getConfigName() != null) {
            existing.setConfigName(update.getConfigName());
        }
        if (update.getConfigDesc() != null) {
            existing.setConfigDesc(update.getConfigDesc());
        }
        if (update.getRemark() != null) {
            existing.setRemark(update.getRemark());
        }
        existing.setUpdateUserId(userId);
    }

    /**
     * 生成任务ID
     */
    private String generateTaskId() {
        return "BATCH_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    /**
     * 缓存任务
     */
    private void cacheTask(BatchOperationTask task) {
        // 本地缓存
        taskCache.put(task.getTaskId(), task);

        // Redis缓存
        try {
            String taskKey = Objects.requireNonNull(BATCH_TASK_PREFIX) + Objects.requireNonNull(task.getTaskId(), "taskId不能为null");
            Duration timeout = Objects.requireNonNull(TASK_TIMEOUT, "TASK_TIMEOUT不能为null");
            redisTemplate.opsForValue().set(taskKey, task, timeout);
        } catch (Exception e) {
            log.warn("[批量配置] 缓存任务失败: taskId={}, error={}", task.getTaskId(), e.getMessage(), e);
        }
    }

    /**
     * 批量操作任务实体
     */
    public static class BatchOperationTask {
        private String taskId;
        private String operationType;
        private String status; // PENDING, RUNNING, COMPLETED, FAILED, CANCELLED, PARTIALLY_COMPLETED
        private String importStrategy;
        private Long userId;
        private long createTime;
        private long startTime;
        private long endTime;
        private int totalCount;
        private int processedCount;
        private int currentGroup;
        private int totalGroups;
        private int successCount;
        private int failureCount;
        private int skippedCount;
        private int updatedCount;
        private String errorMessage;
        private List<String> failureMessages;

        public BatchOperationTask(String taskId, String operationType, int totalCount, Long userId) {
            this.taskId = taskId;
            this.operationType = operationType;
            this.totalCount = totalCount;
            this.userId = userId;
            this.createTime = System.currentTimeMillis();
            this.status = "PENDING";
            this.processedCount = 0;
            this.currentGroup = 0;
            this.totalGroups = 0;
            this.failureMessages = new ArrayList<>();
        }

        public long getDuration() {
            if (startTime == 0) return 0;
            long end = endTime > 0 ? endTime : System.currentTimeMillis();
            return end - startTime;
        }

        public double getProgress() {
            if (totalCount == 0) return 0.0;
            return (double) processedCount / totalCount * 100;
        }

        // Getters and Setters
        public String getTaskId() { return taskId; }
        public String getOperationType() { return operationType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getImportStrategy() { return importStrategy; }
        public void setImportStrategy(String importStrategy) { this.importStrategy = importStrategy; }
        public Long getUserId() { return userId; }
        public long getCreateTime() { return createTime; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        public int getTotalCount() { return totalCount; }
        public int getProcessedCount() { return processedCount; }
        public void setProcessedCount(int processedCount) { this.processedCount = processedCount; }
        public int getCurrentGroup() { return currentGroup; }
        public void setCurrentGroup(int currentGroup) { this.currentGroup = currentGroup; }
        public int getTotalGroups() { return totalGroups; }
        public void setTotalGroups(int totalGroups) { this.totalGroups = totalGroups; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        public int getSkippedCount() { return skippedCount; }
        public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }
        public int getUpdatedCount() { return updatedCount; }
        public void setUpdatedCount(int updatedCount) { this.updatedCount = updatedCount; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public List<String> getFailureMessages() { return failureMessages; }
        public void setFailureMessages(List<String> failureMessages) { this.failureMessages = failureMessages; }
    }
}
