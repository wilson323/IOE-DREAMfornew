package net.lab1024.sa.visitor.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 黑名单访客响应
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "黑名单访客响应")
public class BlacklistVisitorResponse {

    @Schema(description = "黑名单访客列表")
    private List<BlacklistVisitorInfo> blacklistVisitors;

    @Data
    @Schema(description = "黑名单访客信息")
    public static class BlacklistVisitorInfo {
        @Schema(description = "黑名单ID")
        private Long blacklistId;

        @Schema(description = "访客姓名")
        private String visitorName;

        @Schema(description = "访客手机号")
        private String visitorPhone;

        @Schema(description = "访客身份证号")
        private String visitorIdCard;

        @Schema(description = "拉黑原因")
        private String blacklistReason;

        @Schema(description = "拉黑时间")
        private LocalDateTime blacklistTime;

        @Schema(description = "拉黑人")
        private String blacklistBy;
    }
}