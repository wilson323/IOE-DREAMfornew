package net.lab1024.sa.monitor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统日志实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_system_log")
public class SystemLogEntity {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /**
     * 日志级别 (DEBUG、INFO、WARN、ERROR、FATAL)
     */
    private String logLevel;

    /**
     * 日志类型 (APPLICATION、SYSTEM、SECURITY、AUDIT、PERFORMANCE)
     */
    private String logType;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 实例ID
     */
    private String instanceId;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 行号
     */
    private Integer lineNumber;

    /**
     * 日志消息
     */
    private String message;

    /**
     * 异常信息
     */
    private String exceptionInfo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 追踪ID
     */
    private String traceId;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应状态
     */
    private Integer responseStatus;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 线程名称
     */
    private String threadName;

    /**
     * 日志时间
     */
    private LocalDateTime logTime;

    /**
     * 主机名
     */
    private String hostname;

    /**
     * 标签
     */
    private String tags;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 删除标记
     */
    private Integer deletedFlag;
}