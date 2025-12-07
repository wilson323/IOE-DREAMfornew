package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门禁记录创建表单
 * <p>
 * 用于设备协议推送门禁记录
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * - 使用@Schema注解描述字段
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "门禁记录创建表单")
public class AccessRecordAddForm {

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 设备编号
     */
    @Schema(description = "设备编号", example = "DEV001")
    private String deviceCode;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    /**
     * 通行时间（时间戳或LocalDateTime）
     */
    @Schema(description = "通行时间（时间戳或LocalDateTime）", example = "2025-01-30T08:00:00")
    private Object passTime;

    /**
     * 通行类型
     * <p>
     * 0-进入
     * 1-离开
     * </p>
     */
    @Schema(description = "通行类型：0-进入，1-离开", example = "0")
    private Integer passType;

    /**
     * 门号
     */
    @Schema(description = "门号", example = "1")
    private Integer doorNo;

    /**
     * 通行方式
     * <p>
     * 0-卡片
     * 1-人脸
     * 2-指纹
     * </p>
     */
    @Schema(description = "通行方式：0-卡片，1-人脸，2-指纹", example = "1")
    private Integer passMethod;

    /**
     * 通行结果
     * <p>
     * 1-成功
     * 0-失败
     * </p>
     */
    @Schema(description = "通行结果：1-成功，0-失败", example = "1")
    private Integer accessResult;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "设备自动推送")
    private String remark;
}

