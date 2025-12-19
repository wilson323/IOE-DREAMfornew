package net.lab1024.sa.oa.workflow.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import net.lab1024.sa.oa.workflow.service.WorkflowEngineService;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 工作流引擎指标收集器
 * <p>
 * 收集Flowable工作流引擎的性能指标，包括：
 * 1. 流程部署指标
 * 2. 流程实例指标
 * 3. 任务处理指标
 * 4. 性能响应时间指标
 * 5. 错误率指标
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Component("workflowEngineMetricsCollector")
public class WorkflowEngineMetricsCollector {

    @Resource
    private MeterRegistry meterRegistry;

    @Resource
    private WorkflowEngineService workflowEngineService;

    // 流程部署计数器
    private Counter processDeploymentCounter;

    // 流程启动计数器
    private Counter processStartCounter;

    // 流程完成计数器
    private Counter processCompleteCounter;

    // 任务创建计数器
    private Counter taskCreateCounter;

    // 任务完成计数器
    private Counter taskCompleteCounter;

    // 任务分配计数器
    private Counter taskAssignCounter;

    // 错误计数器
    private Counter errorCounter;

    // 流程部署耗时
    private Timer processDeploymentTimer;

    // 流程启动耗时
    private Timer processStartTimer;

    // 任务完成耗时
    private Timer taskCompleteTimer;

    // 当前运行流程实例数
    private final AtomicLong runningProcessInstances = new AtomicLong(0);

    // 当前活跃任务数
    private final AtomicLong activeTasks = new AtomicLong(0);

    @PostConstruct
    public void init() {
        log.info("[工作流指标] 初始化工作流引擎指标收集器");

        // 初始化计数器
        processDeploymentCounter = Counter.builder("workflow.process.deployment.count")
                .description("流程部署总数")
                .register(meterRegistry);

        processStartCounter = Counter.builder("workflow.process.start.count")
                .description("流程启动总数")
                .register(meterRegistry);

        processCompleteCounter = Counter.builder("workflow.process.complete.count")
                .description("流程完成总数")
                .register(meterRegistry);

        taskCreateCounter = Counter.builder("workflow.task.create.count")
                .description("任务创建总数")
                .register(meterRegistry);

        taskCompleteCounter = Counter.builder("workflow.task.complete.count")
                .description("任务完成总数")
                .register(meterRegistry);

        taskAssignCounter = Counter.builder("workflow.task.assign.count")
                .description("任务分配总数")
                .register(meterRegistry);

        errorCounter = Counter.builder("workflow.error.count")
                .description("工作流错误总数")
                .register(meterRegistry);

        // 初始化计时器
        processDeploymentTimer = Timer.builder("workflow.process.deployment.duration")
                .description("流程部署耗时")
                .register(meterRegistry);

        processStartTimer = Timer.builder("workflow.process.start.duration")
                .description("流程启动耗时")
                .register(meterRegistry);

        taskCompleteTimer = Timer.builder("workflow.task.complete.duration")
                .description("任务完成耗时")
                .register(meterRegistry);

        // 注册仪表盘指标
        Gauge.builder("workflow.process.running.instances", runningProcessInstances, AtomicLong::get)
                .description("当前运行流程实例数")
                .register(meterRegistry);

        Gauge.builder("workflow.task.active.count", activeTasks, AtomicLong::get)
                .description("当前活跃任务数")
                .register(meterRegistry);

        log.info("[工作流指标] 工作流引擎指标收集器初始化完成");
    }

    /**
     * 记录流程部署指标
     */
    public void recordProcessDeployment(String processKey) {
        try {
            processDeploymentCounter.increment();

            // 使用带标签的Counter
            Counter.builder("workflow.process.deployment.count.tagged")
                    .tag("process_key", processKey)
                    .register(meterRegistry)
                    .increment();

            log.debug("[工作流指标] 记录流程部署指标: processKey={}", processKey);
        } catch (Exception e) {
            log.warn("[工作流指标] 记录流程部署指标失败", e);
        }
    }

    /**
     * 记录流程部署耗时
     */
    public void recordProcessDeploymentTime(String processKey, long durationMs) {
        try {
            processDeploymentTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

            log.debug("[工作流指标] 记录流程部署耗时: processKey={}, duration={}ms", processKey, durationMs);
        } catch (Exception e) {
            log.warn("[工作流指标] 记录流程部署耗时失败", e);
        }
    }

    /**
     * 记录流程启动指标
     */
    public void recordProcessStart(String processKey, String businessKey) {
        try {
            processStartCounter.increment();

            // 使用带标签的Counter
            Counter.builder("workflow.process.start.count.tagged")
                    .tag("process_key", processKey)
                    .tag("business_key", businessKey != null ? businessKey : "unknown")
                    .register(meterRegistry)
                    .increment();

            log.debug("[工作流指标] 记录流程启动指标: processKey={}, businessKey={}", processKey, businessKey);
        } catch (Exception e) {
            log.warn("[工作流指标] 记录流程启动指标失败", e);
        }
    }

    /**
     * 记录流程启动耗时
     */
    public void recordProcessStartTime(String processKey, long durationMs) {
        try {
            processStartTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

            log.debug("[工作流指标] 记录流程启动耗时: processKey={}, duration={}ms", processKey, durationMs);
        } catch (Exception e) {
            log.warn("[工作流指标] 记录流程启动耗时失败", e);
        }
    }

    /**
     * 记录流程完成指标
     */
    public void recordProcessComplete(String processKey, String outcome) {
        try {
            processCompleteCounter.increment();

            // 使用带标签的Counter
            Counter.builder("workflow.process.complete.count.tagged")
                    .tag("process_key", processKey)
                    .tag("outcome", outcome != null ? outcome : "unknown")
                    .register(meterRegistry)
                    .increment();

            log.debug("[工作流指标] 记录流程完成指标: processKey={}, outcome={}", processKey, outcome);
        } catch (Exception e) {
            log.warn("[工作流指标] 记录流程完成指标失败", e);
        }
    }

    /**
     * 记录任务创建指标
     */
    public void recordTaskCreate(String taskName, String processKey) {
        try {
            taskCreateCounter.increment();
            activeTasks.incrementAndGet();

            // 使用带标签的Counter
            Counter.builder("workflow.task.create.count.tagged")
                    .tag("task_name", taskName != null ? taskName : "unknown")
                    .tag("process_key", processKey != null ? processKey : "unknown")
                    .register(meterRegistry)
                    .increment();

            log.debug("[工作流指标] 记录任务创建指标: taskName={}, processKey={}", taskName, processKey);
        } catch (Exception e) {
            log.warn("[工作流指标] 记录任务创建指标失败", e);
        }
    }

    /**
     * 记录任务完成指标
     */
    public void recordTaskComplete(String taskName, String outcome, long durationMs) {
        try {
            taskCompleteCounter.increment();
            activeTasks.decrementAndGet();

            // 使用带标签的Counter
            Counter.builder("workflow.task.complete.count.tagged")
                    .tag("task_name", taskName != null ? taskName : "unknown")
                    .tag("outcome", outcome != null ? outcome : "unknown")
                    .register(meterRegistry)
                    .increment();

            // 记录耗时
            taskCompleteTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

            log.debug("[工作流指标] 记录任务完成指标: taskName={}, outcome={}, duration={}ms", taskName, outcome, durationMs);
        } catch (Exception e) {
            log.warn("[工作流指标] 记录任务完成指标失败", e);
        }
    }

    /**
     * 记录任务分配指标
     */
    public void recordTaskAssign(String taskName, Long assigneeId) {
        try {
            taskAssignCounter.increment();

            // 使用带标签的Counter
            Counter.builder("workflow.task.assign.count.tagged")
                    .tag("task_name", taskName != null ? taskName : "unknown")
                    .tag("assignee_id", String.valueOf(assigneeId != null ? assigneeId : 0))
                    .register(meterRegistry)
                    .increment();

            log.debug("[工作流指标] 记录任务分配指标: taskName={}, assigneeId={}", taskName, assigneeId);
        } catch (Exception e) {
            log.warn("[工作流指标] 记录任务分配指标失败", e);
        }
    }

    /**
     * 记录错误指标
     */
    public void recordError(String operation, String errorType) {
        try {
            errorCounter.increment();

            // 使用带标签的Counter
            Counter.builder("workflow.error.count.tagged")
                    .tag("operation", operation != null ? operation : "unknown")
                    .tag("error_type", errorType != null ? errorType : "unknown")
                    .register(meterRegistry)
                    .increment();

            log.debug("[工作流指标] 记录错误指标: operation={}, errorType={}", operation, errorType);
        } catch (Exception e) {
            log.warn("[工作流指标] 记录错误指标失败", e);
        }
    }

    /**
     * 更新运行中的流程实例数量
     */
    public void updateRunningProcessInstances(long count) {
        runningProcessInstances.set(count);
    }

    /**
     * 更新活跃任务数量
     */
    public void updateActiveTasks(long count) {
        activeTasks.set(count);
    }

    /**
     * 获取当前运行中的流程实例数量
     */
    public long getRunningProcessInstances() {
        return runningProcessInstances.get();
    }

    /**
     * 获取当前活跃任务数量
     */
    public long getActiveTasks() {
        return activeTasks.get();
    }

    /**
     * 定期更新指标数据
     */
    public void refreshMetrics() {
        try {
            // 从工作流引擎获取实时数据
            if (workflowEngineService != null) {
                var stats = workflowEngineService.getEngineStatistics();
                if (stats != null) {
                    Object instances = stats.get("runningInstances");
                    Object tasks = stats.get("activeTasks");

                    if (instances instanceof Number) {
                        updateRunningProcessInstances(((Number) instances).longValue());
                    }
                    if (tasks instanceof Number) {
                        updateActiveTasks(((Number) tasks).longValue());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[工作流指标] 刷新指标数据失败", e);
        }
    }
}
