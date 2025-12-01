package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 账户安全事件VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "账户安全事件")
public class AccountSecurityEvent {

    @Schema(description = "事件ID")
    private Long eventId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "事件类型：1-登录异常，2-消费异常，3-密码错误，4-账户冻结")
    private Integer eventType;

    @Schema(description = "事件描述")
    private String eventDescription;

    @Schema(description = "事件时间")
    private LocalDateTime eventTime;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "处理状态：1-未处理，2-已处理，3-忽略")
    private Integer handleStatus;

    // 手动添加的getter/setter方法 (Lombok失效备用)
















}
