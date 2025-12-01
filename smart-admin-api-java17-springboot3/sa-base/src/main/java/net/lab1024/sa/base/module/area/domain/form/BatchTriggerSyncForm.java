package net.lab1024.sa.base.module.area.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量触发设备同步表单
 * 用于批量触发多个区域或人员的设备同步操作
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@Schema(description = "批量触发设备同步表单")
public class BatchTriggerSyncForm {

    /**
     * 同步配置列表
     * 不能为空
     */
    @NotEmpty(message = "同步配置列表不能为空")
    @Valid
    @Schema(description = "同步配置列表")
    private List<SyncConfig> syncConfigs;

    /**
     * 同步配置
     */
    @Data
    @Schema(description = "同步配置")
    public static class SyncConfig {

        /**
         * 同步类型
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
         */
        @Schema(description = "业务类型", example = "ACCESS")
        private String businessType;

        /**
         * 数据类型
         */
        @Schema(description = "数据类型", example = "BIOMETRIC")
        private String dataType;
    }
}