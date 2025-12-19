package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 移动端帮助结果
 * <p>
 * 封装移动端帮助响应结果
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端帮助结果")
public class MobileHelpResult {

    /**
     * 帮助类型
     */
    @Schema(description = "帮助类型", example = "FAQ")
    private String helpType;

    /**
     * 帮助内容列表
     */
    @Schema(description = "帮助内容列表")
    private List<Map<String, Object>> helpContent;
}
