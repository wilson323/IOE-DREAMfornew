package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 微信通知结果VO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@Schema(description = "微信通知结果")
public class WechatNotificationResult {

    @Schema(description = "通知是否发送成功", example = "true")
    private Boolean success;

    @Schema(description = "消息ID", example = "msg123456789")
    private String messageId;

    @Schema(description = "错误码", example = "0")
    private String errorCode;

    @Schema(description = "错误消息", example = "发送成功")
    private String errorMessage;

    @Schema(description = "发送时间", example = "2023-11-17T10:30:00")
    private LocalDateTime sendTime;

    @Schema(description = "用户OpenID", example = "ox1234567890")
    private String openId;

    @Schema(description = "模板ID", example = "template123")
    private String templateId;

    @Schema(description = "模板参数", example = "{amount: '100.00'}")
    private Map<String, Object> templateData;

    @Schema(description = "跳转链接", example = "https://example.com/detail")
    private String redirectUrl;

    @Schema(description = "小程序页面路径", example = "/pages/detail/index")
    private String miniprogramPage;
}