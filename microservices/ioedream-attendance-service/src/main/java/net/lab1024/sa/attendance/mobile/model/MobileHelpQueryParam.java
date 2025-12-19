package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 移动端帮助查询参数
 * <p>
 * 封装移动端帮助查询的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端帮助查询参数")
public class MobileHelpQueryParam {

    /**
     * 帮助类型
     */
    @Schema(description = "帮助类型", example = "FAQ", allowableValues = {"FAQ", "TUTORIAL", "CONTACT"})
    private String helpType;

    /**
     * 关键词
     */
    @Schema(description = "关键词", example = "打卡")
    private String keyword;
}


