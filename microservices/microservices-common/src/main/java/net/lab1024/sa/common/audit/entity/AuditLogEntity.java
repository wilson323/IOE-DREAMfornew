package net.lab1024.sa.common.audit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 审计日志实体类
 * 对应数据库表 t_audit_log
 * 继承 BaseEntity 复用审计字段
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_audit_log")
public class AuditLogEntity extends BaseEntity {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    @TableField("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("module_name")
    private String moduleName;

    @TableField("operation_type")
    private Integer operationType;

    @TableField("operation")
    private String operation;

    @TableField("operation_desc")
    private String operationDesc;

    @TableField("resource_type")
    private String resourceType;

    @TableField("resource_id")
    private String resourceId;

    @TableField("request_method")
    private String requestMethod;

    @TableField("request_url")
    private String requestUrl;

    @TableField("request_params")
    private String requestParams;

    @TableField("response_data")
    private String responseData;

    @TableField("result_status")
    private Integer resultStatus;

    @TableField("error_message")
    private String errorMessage;

    @TableField("risk_level")
    private Integer riskLevel;

    @TableField("client_ip")
    private String clientIp;

    @TableField("user_agent")
    private String userAgent;

    @TableField("trace_id")
    private String traceId;

    @TableField("execution_time")
    private Long executionTime;
}
