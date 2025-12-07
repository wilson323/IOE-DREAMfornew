package net.lab1024.sa.common.scheduler.service;

import java.util.List;
import java.util.Map;

/**
 * 任务调度服务接口
 * <p>
 * 提供定时任务管理、任务执行、任务监控等功能
 * 严格遵循CLAUDE.md规范:
 * - Service接口定义核心业务方法
 * - 实现类在service.impl包中
 * - 使用@Resource依赖注入
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface SchedulerService {

    /**
     * 创建定时任务
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @param cronExpression Cron表达式
     * @param jobData 任务数据
     * @return 是否创建成功
     */
    boolean createScheduledJob(String jobName, String jobGroup, String cronExpression, Map<String, Object> jobData);

    /**
     * 暂停任务
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否暂停成功
     */
    boolean pauseJob(String jobName, String jobGroup);

    /**
     * 恢复任务
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否恢复成功
     */
    boolean resumeJob(String jobName, String jobGroup);

    /**
     * 删除任务
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否删除成功
     */
    boolean deleteJob(String jobName, String jobGroup);

    /**
     * 立即执行任务
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否执行成功
     */
    boolean triggerJob(String jobName, String jobGroup);

    /**
     * 获取任务列表
     *
     * @return 任务列表
     */
    List<Map<String, Object>> getJobList();

    /**
     * 获取任务详情
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 任务详情
     */
    Map<String, Object> getJobDetail(String jobName, String jobGroup);
}
