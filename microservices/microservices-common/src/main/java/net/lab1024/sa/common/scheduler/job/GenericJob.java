package net.lab1024.sa.common.scheduler.job;

import java.lang.reflect.Method;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import lombok.extern.slf4j.Slf4j;

/**
 * 通用任务执行类
 * <p>
 * 基于Quartz的Job接口实现
 * 支持通过JobDataMap传递任务参数
 * 严格遵循CLAUDE.md规范:
 * - 完整的异常处理和日志记录
 * - 任务执行状态跟踪
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class GenericJob implements Job {

    /**
     * 执行任务
     * <p>
     * 从JobDataMap获取任务参数，执行具体任务逻辑
     * </p>
     *
     * @param context Job执行上下文
     * @throws JobExecutionException 任务执行异常
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getKey().getName();
        String jobGroup = context.getJobDetail().getKey().getGroup();

        log.info("开始执行定时任务，任务名称：{}，任务组：{}", jobName, jobGroup);

        try {
            // 获取任务数据
            var jobDataMap = context.getJobDetail().getJobDataMap();
            log.debug("任务数据：{}", jobDataMap);

            // 1. 获取任务类名（从JobDataMap中获取）
            String taskClassName = jobDataMap.getString("taskClassName");
            if (taskClassName == null || taskClassName.isEmpty()) {
                log.warn("任务类名为空，任务名称：{}，任务组：{}，跳过执行", jobName, jobGroup);
                return;
            }

            // 2. 获取任务方法名（可选，默认为execute）
            String taskMethodName = jobDataMap.getString("taskMethodName");
            if (taskMethodName == null || taskMethodName.isEmpty()) {
                taskMethodName = "execute";
            }

            // 3. 获取任务参数（可选）
            @SuppressWarnings("unchecked")
            Map<String, Object> taskParams = (Map<String, Object>) jobDataMap.get("taskParams");

            // 4. 通过反射创建任务实例并执行
            executeTaskByReflection(taskClassName, taskMethodName, taskParams, jobName, jobGroup);

            log.info("定时任务执行完成，任务名称：{}，任务组：{}", jobName, jobGroup);

        } catch (Exception e) {
            log.error("定时任务执行失败，任务名称：{}，任务组：{}", jobName, jobGroup, e);
            throw new JobExecutionException("任务执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过反射执行任务
     *
     * @param taskClassName 任务类名
     * @param taskMethodName 任务方法名
     * @param taskParams 任务参数
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @throws Exception 执行异常
     */
    private void executeTaskByReflection(
            String taskClassName,
            String taskMethodName,
            Map<String, Object> taskParams,
            String jobName,
            String jobGroup) throws Exception {

        log.info("通过反射执行任务，类名：{}，方法名：{}，任务名称：{}", taskClassName, taskMethodName, jobName);

        try {
            // 1. 加载任务类
            Class<?> taskClass = Class.forName(taskClassName);
            log.debug("任务类加载成功：{}", taskClass.getName());

            // 2. 创建任务实例
            Object taskInstance = taskClass.getDeclaredConstructor().newInstance();
            log.debug("任务实例创建成功：{}", taskClass.getName());

            // 3. 查找执行方法
            Method executeMethod = null;
            if (taskParams != null && !taskParams.isEmpty()) {
                // 尝试查找带参数的方法
                try {
                    // 假设方法签名为：execute(Map<String, Object> params)
                    executeMethod = taskClass.getMethod(taskMethodName, Map.class);
                } catch (NoSuchMethodException e) {
                    log.debug("未找到带参数的方法，尝试查找无参数方法");
                }
            }

            // 如果没找到带参数的方法，尝试查找无参数方法
            if (executeMethod == null) {
                try {
                    executeMethod = taskClass.getMethod(taskMethodName);
                } catch (NoSuchMethodException e) {
                    log.error("未找到任务方法：{}，类名：{}", taskMethodName, taskClassName, e);
                    throw new JobExecutionException("未找到任务方法: " + taskMethodName);
                }
            }

            // 4. 执行任务方法
            log.debug("开始执行任务方法：{}，类名：{}", taskMethodName, taskClassName);
            if (executeMethod.getParameterCount() > 0) {
                // 带参数的方法
                executeMethod.invoke(taskInstance, taskParams);
            } else {
                // 无参数的方法
                executeMethod.invoke(taskInstance);
            }

            log.info("任务方法执行成功，类名：{}，方法名：{}", taskClassName, taskMethodName);

        } catch (ClassNotFoundException e) {
            log.error("任务类不存在：{}", taskClassName, e);
            throw new JobExecutionException("任务类不存在: " + taskClassName, e);
        } catch (NoSuchMethodException e) {
            log.error("任务方法不存在：{}，类名：{}", taskMethodName, taskClassName, e);
            throw new JobExecutionException("任务方法不存在: " + taskMethodName, e);
        } catch (Exception e) {
            log.error("执行任务方法异常，类名：{}，方法名：{}", taskClassName, taskMethodName, e);
            throw new JobExecutionException("执行任务方法异常: " + e.getMessage(), e);
        }
    }
}
