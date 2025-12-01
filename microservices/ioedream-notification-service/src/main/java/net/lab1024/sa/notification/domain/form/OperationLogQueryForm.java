package net.lab1024.sa.notification.domain.form;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 操作日志查询Form
 *
 * @author IOE-DREAM Team
 * @since 2025-11-29
 */
@Data
public class OperationLogQueryForm {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 状态（1:成功, 0:失败）
     */
    private Integer status;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    @Min(value = 1, message = "页大小必须大于0")
    private Integer pageSize = 20;
}