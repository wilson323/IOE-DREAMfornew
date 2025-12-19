package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 移动端位置信息结果
 * <p>
 * 封装移动端位置信息响应结果
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
@Schema(description = "移动端位置信息结果")
public class MobileLocationResult {

    /**
     * 员工ID
     */
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    /**
     * 纬度
     */
    @Schema(description = "纬度", example = "39.9042")
    private Double latitude;

    /**
     * 经度
     */
    @Schema(description = "经度", example = "116.4074")
    private Double longitude;

    /**
     * 地址
     */
    @Schema(description = "地址", example = "北京市朝阳区")
    private String address;
}
