package net.lab1024.sa.base.module.area.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 触发设备同步表单
 * 用于触发指定区域或人员的设备同步操作
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@Schema(description = "触发设备同步表单")
public class TriggerSyncForm {

    /**
     * 同步类型
     * AREA-按区域同步，PERSON-按人员同步，DEVICE-按设备同步
     */
    @NotNull(message = "同步类型不能为空")
    @Schema(description = "同步类型", example = "AREA", allowableValues = {"AREA", "PERSON", "DEVICE"})
    private String syncType;

    /**
     * 区域ID列表
     * 当syncType为AREA时必填
     */
    @Schema(description = "区域ID列表", example = "[1, 2, 3]")
    private List<Long> areaIds;

    /**
     * 人员ID列表
     * 当syncType为PERSON时必填
     */
    @Schema(description = "人员ID列表", example = "[101, 102, 103]")
    private List<Long> personIds;

    /**
     * 设备ID列表
     * 当syncType为DEVICE时必填
     */
    @Schema(description = "设备ID列表", example = "[201, 202, 203]")
    private List<Long> deviceIds;

    /**
     * 业务类型
     * 指定要同步的业务类型，如ACCESS、CONSUME、ATTENDANCE等
     * 空表示同步所有业务类型
     */
    @Schema(description = "业务类型", example = "ACCESS")
    private String businessType;

    /**
     * 数据类型
     * PERSON-人员信息，BIOMETRIC-生物特征，CONFIG-配置信息，ALL-全部信息
     */
    @Schema(description = "数据类型", example = "BIOMETRIC", allowableValues = {"PERSON", "BIOMETRIC", "CONFIG", "ALL"})
    private String dataType = "ALL";

    /**
     * 是否强制同步
     * true-强制同步所有数据，false-仅同步变更的数据
     */
    @Schema(description = "是否强制同步", example = "false")
    private Boolean forceSync = false;

    /**
     * 是否异步执行
     * true-异步执行，false-同步执行
     */
    @Schema(description = "是否异步执行", example = "true")
    private Boolean async = true;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "触发门禁设备同步")
    private String remark;
}