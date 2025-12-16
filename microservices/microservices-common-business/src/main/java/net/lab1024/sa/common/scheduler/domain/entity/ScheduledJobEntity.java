package net.lab1024.sa.common.scheduler.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 定时任务实体
 * <p>
 * 对应数据库表：t_scheduled_job
 * 严格遵循CLAUDE.md规范:
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 完整的字段映射和注释
 * </p>
 * <p>
 * 业务场景：
 * - 定时任务配置管理
 * - 任务调度执行
 * - 任务监控和统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_scheduled_job")
public class ScheduledJobEntity extends BaseEntity {

    /**
     * 任务ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列job_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "job_id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    @TableField("job_name")
    private String jobName;

    /**
     * 任务分组
     */
    @TableField("job_group")
    private String jobGroup;

    /**
     * 任务执行类（完整类名）
     */
    @TableField("job_class")
    private String jobClass;

    /**
     * Cron表达式
     */
    @TableField("cron_expression")
    private String cronExpression;

    /**
     * 任务参数（JSON格式）
     */
    @TableField("job_params")
    private String jobParams;

    /**
     * 任务描述
     */
    @TableField("job_description")
    private String jobDescription;

    /**
     * 状态
     * <p>
     * 1-启用 2-暂停 3-停止
     * </p>
     */
    @TableField("status")
    private Integer status;

    /**
     * 优先级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 最大重试次数
     */
    @TableField("max_retry")
    private Integer maxRetry;

    /**
     * 重试间隔（秒）
     */
    @TableField("retry_interval")
    private Integer retryInterval;

    /**
     * 超时时间（秒）
     */
    @TableField("timeout")
    private Integer timeout;

    /**
     * 是否允许并发
     * <p>
     * 0-否 1-是
     * </p>
     */
    @TableField("concurrent")
    private Integer concurrent;

    /**
     * 错过执行策略
     * <p>
     * 1-立即执行 2-执行一次 3-放弃执行
     * </p>
     */
    @TableField("misfire_policy")
    private Integer misfirePolicy;

    /**
     * 最后执行时间
     */
    @TableField("last_execution_time")
    private LocalDateTime lastExecutionTime;

    /**
     * 下次执行时间
     */
    @TableField("next_execution_time")
    private LocalDateTime nextExecutionTime;

    /**
     * 执行次数
     */
    @TableField("execution_count")
    private Long executionCount;

    /**
     * 失败次数
     */
    @TableField("failure_count")
    private Long failureCount;
}
