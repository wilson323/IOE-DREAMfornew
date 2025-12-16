package net.lab1024.sa.common.notification.domain.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 钉钉FeedCard消息视图对象
 * <p>
 * 用于构建钉钉多卡片消息
 * 严格遵循CLAUDE.md规范:
 * - VO类用于数据传输
 * - 完整的Swagger文档注解
 * </p>
 * <p>
 * FeedCard消息格式：
 * - 支持多个卡片链接
 * - 每个卡片包含标题、图片、跳转链接
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "钉钉FeedCard消息视图对象")
public class DingTalkFeedCardVO {

    @Schema(description = "卡片链接列表")
    private List<FeedCardLink> links;

    /**
     * FeedCard链接
     */
    @Data
    @Schema(description = "FeedCard链接")
    public static class FeedCardLink {

        @Schema(description = "链接标题", example = "系统通知")
        private String title;

        @Schema(description = "链接图片URL", example = "https://example.com/image.png")
        private String messageURL;

        @Schema(description = "链接跳转地址", example = "https://example.com/detail")
        private String picURL;
    }
}
