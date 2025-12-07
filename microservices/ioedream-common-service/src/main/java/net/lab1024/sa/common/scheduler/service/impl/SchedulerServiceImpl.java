package net.lab1024.sa.common.scheduler.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.scheduler.dao.ScheduledJobDao;
import net.lab1024.sa.common.scheduler.domain.entity.ScheduledJobEntity;
import net.lab1024.sa.common.scheduler.service.SchedulerService;

/**
 * 任务调度服务实现类
 * <p>
 * 基于Quartz实现任务调度功能，支持集群模式
 * 严格遵循CLAUDE.md规范:
 * - 使用@Service注解标识
 * - 使用@Resource依赖注入（禁止@Autowired）
 * - 事务管理在Service层
 * - 完整的异常处理和日志记录
 * </p>
 * <p>
 * 企业级特性：
 * - Quartz集群模式支持
 * - 任务动态管理（创建、暂停、恢复、删除）
 * - 任务执行监控和统计
 * - 完善的错误处理和重试机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SchedulerServiceImpl implements SchedulerService {

    @Resource
    private Scheduler scheduler;

    @Resource
    private ScheduledJobDao scheduledJobDao;

    /**
     * 创建定时任务
     * <p>
     * 基于Quartz创建定时任务，支持Cron表达式
     * 任务信息存储到数据库，支持集群模式
     * </p>
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @param cronExpression Cron表达式
     * @param jobData 任务数据
     * @return 是否创建成功
     */
    @Override
    public boolean createScheduledJob(String jobName, String jobGroup, String cronExpression, Map<String, Object> jobData) {
        log.info("创建定时任务，任务名称：{}，任务组：{}，Cron表达式：{}", jobName, jobGroup, cronExpression);

        try {
            // 1. 检查任务是否已存在
            ScheduledJobEntity existingJob = scheduledJobDao.selectByJobNameAndGroup(jobName, jobGroup);
            if (existingJob != null) {
                log.warn("任务已存在，任务名称：{}，任务组：{}", jobName, jobGroup);
                return false;
            }

            // 2. 创建JobDetail
            JobKey jobKey = new JobKey(jobName, jobGroup);
            JobDetail jobDetail = JobBuilder.newJob(net.lab1024.sa.common.scheduler.job.GenericJob.class)
                    .withIdentity(jobKey)
                    .withDescription("定时任务：" + jobName)
                    .storeDurably(true)
                    .build();

            // 设置任务数据
            if (jobData != null && !jobData.isEmpty()) {
                jobDetail.getJobDataMap().putAll(jobData);
            }

            // 3. 创建Trigger
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(new TriggerKey(jobName + "_trigger", jobGroup))
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            // 4. 调度任务
            scheduler.scheduleJob(jobDetail, trigger);

            // 5. 保存任务信息到数据库
            ScheduledJobEntity jobEntity = new ScheduledJobEntity();
            jobEntity.setJobName(jobName);
            jobEntity.setJobGroup(jobGroup);
            jobEntity.setCronExpression(cronExpression);
            jobEntity.setStatus(1); // 1-启用
            jobEntity.setJobClass(net.lab1024.sa.common.scheduler.job.GenericJob.class.getName());
            
            // 将jobData转换为JSON存储
            if (jobData != null && !jobData.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                jobEntity.setJobParams(objectMapper.writeValueAsString(jobData));
            }

            scheduledJobDao.insert(jobEntity);

            log.info("定时任务创建成功，任务名称：{}，任务组：{}", jobName, jobGroup);
            return true;

        } catch (Exception e) {
            log.error("创建定时任务失败，任务名称：{}，任务组：{}", jobName, jobGroup, e);
            return false;
        }
    }

    /**
     * 暂停任务
     * <p>
     * 暂停指定任务的执行，任务仍保留在调度器中
     * </p>
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否暂停成功
     */
    @Override
    public boolean pauseJob(String jobName, String jobGroup) {
        log.info("暂停任务，任务名称：{}，任务组：{}", jobName, jobGroup);

        try {
            JobKey jobKey = new JobKey(jobName, jobGroup);
            scheduler.pauseJob(jobKey);

            // 更新数据库状态
            ScheduledJobEntity job = scheduledJobDao.selectByJobNameAndGroup(jobName, jobGroup);
            if (job != null) {
                job.setStatus(2); // 2-暂停
                scheduledJobDao.updateById(job);
            }

            log.info("任务暂停成功，任务名称：{}，任务组：{}", jobName, jobGroup);
            return true;

        } catch (SchedulerException e) {
            log.error("暂停任务失败，任务名称：{}，任务组：{}", jobName, jobGroup, e);
            return false;
        }
    }

    /**
     * 恢复任务
     * <p>
     * 恢复已暂停的任务，任务重新开始执行
     * </p>
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否恢复成功
     */
    @Override
    public boolean resumeJob(String jobName, String jobGroup) {
        log.info("恢复任务，任务名称：{}，任务组：{}", jobName, jobGroup);

        try {
            JobKey jobKey = new JobKey(jobName, jobGroup);
            scheduler.resumeJob(jobKey);

            // 更新数据库状态
            ScheduledJobEntity job = scheduledJobDao.selectByJobNameAndGroup(jobName, jobGroup);
            if (job != null) {
                job.setStatus(1); // 1-启用
                scheduledJobDao.updateById(job);
            }

            log.info("任务恢复成功，任务名称：{}，任务组：{}", jobName, jobGroup);
            return true;

        } catch (SchedulerException e) {
            log.error("恢复任务失败，任务名称：{}，任务组：{}", jobName, jobGroup, e);
            return false;
        }
    }

    /**
     * 删除任务
     * <p>
     * 从调度器中删除任务，同时更新数据库状态
     * </p>
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否删除成功
     */
    @Override
    public boolean deleteJob(String jobName, String jobGroup) {
        log.info("删除任务，任务名称：{}，任务组：{}", jobName, jobGroup);

        try {
            JobKey jobKey = new JobKey(jobName, jobGroup);
            scheduler.deleteJob(jobKey);

            // 软删除数据库记录
            ScheduledJobEntity job = scheduledJobDao.selectByJobNameAndGroup(jobName, jobGroup);
            if (job != null) {
                job.setDeletedFlag(1); // 软删除
                scheduledJobDao.updateById(job);
            }

            log.info("任务删除成功，任务名称：{}，任务组：{}", jobName, jobGroup);
            return true;

        } catch (SchedulerException e) {
            log.error("删除任务失败，任务名称：{}，任务组：{}", jobName, jobGroup, e);
            return false;
        }
    }

    /**
     * 立即执行任务
     * <p>
     * 立即触发任务执行，不受Cron表达式限制
     * </p>
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否执行成功
     */
    @Override
    public boolean triggerJob(String jobName, String jobGroup) {
        log.info("立即执行任务，任务名称：{}，任务组：{}", jobName, jobGroup);

        try {
            JobKey jobKey = new JobKey(jobName, jobGroup);
            scheduler.triggerJob(jobKey);

            log.info("任务立即执行成功，任务名称：{}，任务组：{}", jobName, jobGroup);
            return true;

        } catch (SchedulerException e) {
            log.error("立即执行任务失败，任务名称：{}，任务组：{}", jobName, jobGroup, e);
            return false;
        }
    }

    /**
     * 获取任务列表
     * <p>
     * 获取所有正在运行的任务信息
     * </p>
     *
     * @return 任务列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getJobList() {
        log.debug("获取任务列表");

        try {
            List<Map<String, Object>> jobList = new ArrayList<>();

            // 从数据库查询任务列表
            List<ScheduledJobEntity> jobs = scheduledJobDao.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ScheduledJobEntity>()
                            .eq(ScheduledJobEntity::getDeletedFlag, 0)
                            .orderByAsc(ScheduledJobEntity::getJobGroup)
                            .orderByAsc(ScheduledJobEntity::getJobName)
            );

            for (ScheduledJobEntity job : jobs) {
                Map<String, Object> jobMap = new HashMap<>();
                jobMap.put("jobId", job.getJobId());
                jobMap.put("jobName", job.getJobName());
                jobMap.put("jobGroup", job.getJobGroup());
                jobMap.put("cronExpression", job.getCronExpression());
                jobMap.put("status", job.getStatus());
                jobMap.put("lastExecutionTime", job.getLastExecutionTime());
                jobMap.put("nextExecutionTime", job.getNextExecutionTime());
                jobMap.put("executionCount", job.getExecutionCount());
                jobMap.put("failureCount", job.getFailureCount());

                jobList.add(jobMap);
            }

            log.debug("获取任务列表完成，数量：{}", jobList.size());
            return jobList;

        } catch (Exception e) {
            log.error("获取任务列表异常", e);
            throw new RuntimeException("获取任务列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取任务详情
     * <p>
     * 获取指定任务的详细信息
     * </p>
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 任务详情
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getJobDetail(String jobName, String jobGroup) {
        log.debug("获取任务详情，任务名称：{}，任务组：{}", jobName, jobGroup);

        try {
            // 从数据库查询任务信息
            ScheduledJobEntity job = scheduledJobDao.selectByJobNameAndGroup(jobName, jobGroup);
            if (job == null) {
                log.warn("任务不存在，任务名称：{}，任务组：{}", jobName, jobGroup);
                return null;
            }

            // 构建任务详情
            Map<String, Object> jobDetail = new HashMap<>();
            jobDetail.put("jobId", job.getJobId());
            jobDetail.put("jobName", job.getJobName());
            jobDetail.put("jobGroup", job.getJobGroup());
            jobDetail.put("jobClass", job.getJobClass());
            jobDetail.put("cronExpression", job.getCronExpression());
            jobDetail.put("jobParams", job.getJobParams());
            jobDetail.put("jobDescription", job.getJobDescription());
            jobDetail.put("status", job.getStatus());
            jobDetail.put("priority", job.getPriority());
            jobDetail.put("maxRetry", job.getMaxRetry());
            jobDetail.put("retryInterval", job.getRetryInterval());
            jobDetail.put("timeout", job.getTimeout());
            jobDetail.put("concurrent", job.getConcurrent());
            jobDetail.put("misfirePolicy", job.getMisfirePolicy());
            jobDetail.put("lastExecutionTime", job.getLastExecutionTime());
            jobDetail.put("nextExecutionTime", job.getNextExecutionTime());
            jobDetail.put("executionCount", job.getExecutionCount());
            jobDetail.put("failureCount", job.getFailureCount());

            // 尝试从Quartz获取运行时信息
            try {
                JobKey jobKey = new JobKey(jobName, jobGroup);
                JobDetail quartzJobDetail = scheduler.getJobDetail(jobKey);
                if (quartzJobDetail != null) {
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    if (!triggers.isEmpty()) {
                        Trigger trigger = triggers.get(0);
                        if (trigger instanceof CronTrigger cronTrigger) {
                            jobDetail.put("nextFireTime", cronTrigger.getNextFireTime());
                            jobDetail.put("previousFireTime", cronTrigger.getPreviousFireTime());
                        }
                    }
                }
            } catch (SchedulerException e) {
                log.warn("从Quartz获取任务运行时信息失败", e);
            }

            log.debug("获取任务详情完成，任务名称：{}", jobName);
            return jobDetail;

        } catch (Exception e) {
            log.error("获取任务详情异常，任务名称：{}，任务组：{}", jobName, jobGroup, e);
            throw new RuntimeException("获取任务详情失败: " + e.getMessage(), e);
        }
    }
}
