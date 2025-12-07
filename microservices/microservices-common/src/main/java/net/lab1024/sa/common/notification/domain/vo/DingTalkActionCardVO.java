package net.lab1024.sa.common.notification.domain.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 钉钉ActionCard消息视图对象
 * <p>
 * 用于构建钉钉交互式卡片消息
 * 严格遵循CLAUDE.md规范:
 * - VO类用于数据传输
 * - 完整的Swagger文档注解
 * </p>
 * <p>
 * ActionCard消息格式：
 * - 标题、文本内容
 * - 单个按钮或按钮组
 * - 支持跳转链接
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "钉钉ActionCard消息视图对象")
public class DingTalkActionCardVO {

    @Schema(description = "卡片标题", example = "系统通知")
    private String title;

    @Schema(description = "卡片文本内容", example = "您有一条新的系统通知")
    private String text;

    @Schema(description = "单个按钮（singleBtn模式）")
    private ActionCardButton singleBtn;

    @Schema(description = "按钮列表（btns模式）")
    private List<ActionCardButton> btns;

    @Schema(description = "按钮排列方式：0-竖直排列 1-横向排列", example = "0")
    private Integer btnOrientation;

    /**
     * ActionCard按钮
     */
    @Data
    @Schema(description = "ActionCard按钮")
    public static class ActionCardButton {

        @Schema(description = "按钮标题", example = "查看详情")
        private String title;

        @Schema(description = "按钮跳转链接", example = "https://example.com/detail")
        private String actionURL;
    }
}
