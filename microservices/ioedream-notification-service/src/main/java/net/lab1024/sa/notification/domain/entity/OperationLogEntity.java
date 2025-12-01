package net.lab1024.sa.notification.domain.entity;

import java.util.Map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 操作日志实体
 *
 * @author IOE-DREAM Team
 * @since 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_operation_log", autoResultMap = true)
public class OperationLogEntity extends BaseEntity {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    @TableField("user_id")
    private Long userId;

    @TableField("user_name")
    private String userName;

    @TableField("operation_type")
    private String operationType;

    @TableField("operation_desc")
    private String operationDesc;

    @TableField("request_method")
    private String requestMethod;

    @TableField("request_url")
    private String requestUrl;

    @TableField(value = "request_params", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> requestParams;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("execution_time")
    private Long executionTime;

    @TableField("status")
    private Integer status;

    @TableField("error_message")
    private String errorMessage;
}