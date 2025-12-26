package net.lab1024.sa.consume.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Builder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费卡操作记录视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Data
@Builder
@Schema(description = "消费卡操作记录视图对象")
public class ConsumeCardOperationVO {

    @Schema(description = "操作ID", example = "1001")
    private Long operationId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "卡号", example = "CARD20231201001")
    private String cardNo;

    @Schema(description = "操作类型", example = "UNLOCK")
    private String operationType;

    @Schema(description = "操作类型名称", example = "解锁")
    private String operationTypeName;

    @Schema(description = "操作前状态", example = "LOCKED")
    private String beforeStatus;

    @Schema(description = "操作前状态名称", example = "已锁定")
    private String beforeStatusName;

    @Schema(description = "操作后状态", example = "ACTIVE")
    private String afterStatus;

    @Schema(description = "操作后状态名称", example = "正常")
    private String afterStatusName;

    @Schema(description = "操作结果", example = "SUCCESS")
    private String operationResult;

    @Schema(description = "操作结果名称", example = "成功")
    private String operationResultName;

    @Schema(description = "失败原因", example = "验证码错误")
    private String failureReason;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "设备信息", example = "iPhone 14 Pro")
    private String deviceInfo;

    @Schema(description = "操作时间", example = "2025-12-24T10:30:00")
    private LocalDateTime operationTime;

    @Schema(description = "创建时间", example = "2025-12-24T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "备注", example = "用户自行解锁")
    private String remark;
}
