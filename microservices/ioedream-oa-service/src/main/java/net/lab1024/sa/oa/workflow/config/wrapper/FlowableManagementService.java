package net.lab1024.sa.oa.workflow.config.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.ManagementService;
import org.flowable.engine.impl.ProcessEngineImpl;
import org.flowable.engine.impl.persistence.entity.PropertyEntity;
import org.flowable.job.api.Job;
import org.flowable.job.api.TimerJob;
import org.flowable.job.api.HistoryJob;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Flowable管理服务包装器
 * <p>
 * 提供工作流引擎管理和监控的完整功能封装
 * 包括引擎信息、定时任务、异步作业、性能监控等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Service
public class FlowableManagementService {

    private final ManagementService managementService;

    public FlowableManagementService(ManagementService managementService) {
        this.managementService = managementService;
        log.info("[Flowable] ManagementService包装器初始化完成");
    }

    /**
     * 获取引擎名称
     *
     * @return 引擎名称
     */
    public String getEngineName() {
        try {
            return managementService.getEngineName();
        } catch (Exception e) {
            log.error("[Flowable] 获取引擎名称失败: error={}", e.getMessage(), e);
            return "unknown";
        }
    }

    /**
     * 获取引擎版本
     *
     * @return 引擎版本
     */
    public String getEngineVersion() {
        try {
            return managementService.getEngineVersion();
        } catch (Exception e) {
            log.error("[Flowable] 获取引擎版本失败: error={}", e.getMessage(), e);
            return "unknown";
        }
    }

    /**
     * 获取表元数据
     *
     * @return 表元数据
     */
    public Set<String> getDatabaseSchema() {
        try {
            return managementService.getTableNames();
        } catch (Exception e) {
            log.error("[Flowable] 获取表元数据失败: error={}", e.getMessage(), e);
            return Set.of();
        }
    }

    /**
     * 检查表是否存在
     *
     * @param tableName 表名
     * @return 是否存在
     */
    public boolean tableExists(String tableName) {
        try {
            return managementService.tableExists(tableName);
        } catch (Exception e) {
            log.error("[Flowable] 检查表是否存在失败: tableName={}, error={}", tableName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取数据库表信息
     *
     * @param tableName 表名
     * @return 表信息
     */
    public Map<String, String> getTableInfo(String tableName) {
        try {
            return managementService.getTableMetaData(tableName);
        } catch (Exception e) {
            log.error("[Flowable] 获取数据库表信息失败: tableName={}, error={}", tableName, e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * 获取引擎配置属性
     *
     * @return 属性列表
     */
    public List<PropertyEntity> getEngineProperties() {
        try {
            return managementService.getProperties();
        } catch (Exception e) {
            log.error("[Flowable] 获取引擎配置属性失败: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取定时任务
     *
     * @return 定时任务列表
     */
    public List<Job> getTimerJobs() {
        try {
            return managementService.createTimerJobQuery().list();
        } catch (Exception e) {
            log.error("[Flowable] 获取定时任务失败: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取定时任务（分页）
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 定时任务列表
     */
    public List<Job> getTimerJobsPage(int pageNum, int pageSize) {
        try {
            return managementService.createTimerJobQuery()
                    .listPage(pageNum * pageSize, pageSize);
        } catch (Exception e) {
            log.error("[Flowable] 获取定时任务分页失败: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取异步作业
     *
     * @return 异步作业列表
     */
    public List<Job> getAsyncJobs() {
        try {
            return managementService.createAsyncJobQuery().list();
        } catch (Exception e) {
            log.error("[Flowable] 获取异步作业失败: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取异步作业（分页）
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 异步作业列表
     */
    public List<Job> getAsyncJobsPage(int pageNum, int pageSize) {
        try {
            return managementService.createAsyncJobQuery()
                    .listPage(pageNum * pageSize, pageSize);
        } catch (Exception e) {
            log.error("[Flowable] 获取异步作业分页失败: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取历史作业
     *
     * @return 历史作业列表
     */
    public List<HistoryJob> getHistoryJobs() {
        try {
            return managementService.createHistoryJobQuery().list();
        }   catch (Exception e) {
            log.error("[Flowable] 获取历史作业失败: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取历史作业（分页）
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 历史作业列表
     */
    public List<HistoryJob> getHistoryJobsPage(int pageNum, int pageSize) {
        try {
            return managementService.createHistoryJobQuery()
                    .listPage(pageNum * pageSize, pageSize);
        } catch (Exception e) {
            log.error("[Flowable] 获取历史作业分页失败: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 执行定时任务
     *
     * @param jobId 任务ID
     */
    public void executeTimerJob(String jobId) {
        log.info("[Flowable] 执行定时任务: jobId={}", jobId);

        try {
            managementService.executeTimerJob(jobId);
            log.info("[Flowable] 定时任务执行成功: jobId={}", jobId);

        } catch (Exception e) {
            log.error("[Flowable] 执行定时任务失败: jobId={}, error={}", jobId, e.getMessage(), e);
            throw new RuntimeException("执行定时任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除定时任务
     *
     * @param jobId 任务ID
     */
    public void deleteTimerJob(String jobId) {
        log.info("[Flowable] 删除定时任务: jobId={}", jobId);

        try {
            managementService.deleteTimerJob(jobId);
            log.info("[Flowable] 定时任务删除成功: jobId={}", jobId);

        } catch (Exception e) {
            log.error("[Flowable] 删除定时任务失败: jobId={}, error={}", jobId, e.getMessage(), e);
            throw new RuntimeException("删除定时任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取活跃作业数量
     *
     * @return 活跃作业数量
     */
    public long getActiveJobCount() {
        try {
            return managementService.createJobQuery().active().count();
        } catch (Exception e) {
            log.error("[Flowable] 获取活跃作业数量失败: error={}", e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 获取挂起作业数量
     *
     * @return 挂起作业数量
     */
    public long getSuspendedJobCount() {
        try {
            return managementService.createJobQuery().suspended().count();
        } catch (Exception e) {
            log.error("[Flowable] 获取挂起作业数量失败: error={}", e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 获取已死锁作业数量
     *
     * @return 已死锁作业数量
     */
    public long getDeadlockedJobCount() {
        try {
            return managementService.createJobQuery().withLockExpirationTime().count();
        } catch (Exception e) {
            log.error("[Flowable] 获取已死锁作业数量失败: error={}", e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 获取作业统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getJobStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总作业数
            long totalJobs = managementService.createJobQuery().count();
            statistics.put("totalJobs", totalJobs);

            // 活跃作业数
            long activeJobs = getActiveJobCount();
            statistics.put("activeJobs", activeJobs);

            // 挂起作业数
            long suspendedJobs = getSuspendedJobCount();
            statistics.put("suspendedJobs", suspendedJobs);

            // 已死锁作业数
            long deadlockedJobs = getDeadlockedJobCount();
            statistics.put("deadlockedJobs", deadlockedJobs);

            // 可执行作业数
            long executableJobs = managementService.createJobQuery().executable().count();
            statistics.put("executableJobs", executableJobs);

            // 定时任务数
            long timerJobs = managementService.createTimerJobQuery().count();
            statistics.put("timerJobs", timerJobs);

            // 异步作业数
            long asyncJobs = managementService.createAsyncJobQuery().count();
            statistics.put("asyncJobs", asyncJobs);

            return statistics;

        } catch (Exception e) {
            log.error("[Flowable] 获取作业统计信息失败: error={}", e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * 获取引擎健康状态
     *
     * @return 健康状态
     */
    public Map<String, Object> getEngineHealthStatus() {
        try {
            Map<String, Object> status = new HashMap<>();

            // 引擎基本信息
            status.put("name", getEngineName());
            status.put("version", getEngineVersion());
            status.put("healthy", true);
            status.put("timestamp", System.currentTimeMillis());

            // 数据库连接状态
            try {
                Set<String> tables = getDatabaseSchema();
                status.put("databaseConnected", !tables.isEmpty());
                status.put("tableCount", tables.size());
            } catch (Exception e) {
                status.put("databaseConnected", false);
                status.put("tableCount", 0);
            }

            // 作业状态
            Map<String, Object> jobStats = getJobStatistics();
            status.put("jobStatistics", jobStats);

            // 计算健康分数
            int healthScore = calculateHealthScore(status);
            status.put("healthScore", healthScore);
            status.put("healthStatus", getHealthStatusText(healthScore));

            return status;

        } catch (Exception e) {
            log.error("[Flowable] 获取引擎健康状态失败: error={}", e.getMessage(), e);
            Map<String, Object> status = Map.of();
            status.put("healthy", false);
            status.put("error", e.getMessage());
            return status;
        }
    }

    /**
     * 计算健康分数
     *
     * @param status 状态信息
     * @return 健康分数（0-100）
     */
    private int calculateHealthScore(Map<String, Object> status) {
        int score = 100;

        // 数据库连接检查
        if (Boolean.TRUE.equals(status.get("databaseConnected"))) {
            score += 0;
        } else {
            score -= 50;
        }

        // 作业数量检查
        if (status.containsKey("jobStatistics")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> jobStats = (Map<String, Object>) status.get("jobStatistics");

            long totalJobs = Long.parseLong(jobStats.get("totalJobs").toString());
            long deadlockedJobs = Long.parseLong(jobStats.get("deadlockedJobs").toString());

            // 如果死锁作业数超过总作业数的10%，扣分
            if (totalJobs > 0 && deadlockedJobs > totalJobs * 0.1) {
                score -= 20;
            }
        }

        return Math.max(0, Math.min(100, score));
    }

    /**
     * 获取健康状态文本
     *
     * @param score 健康分数
     * @return 状态文本
     */
    private String getHealthStatusText(int score) {
        if (score >= 90) {
            return "EXCELLENT";
        } else if (score >= 80) {
            return "GOOD";
        } else if (score >= 60) {
            return "FAIR";
        } else if (score >= 40) {
            return "POOR";
        } else {
            return "CRITICAL";
        }
    }
}