package net.lab1024.sa.base.module.area.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 导入人员区域关联表单
 * 用于批量导入人员区域关联数据
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@Schema(description = "导入人员区域关联表单")
public class ImportPersonAreaRelationForm {

    /**
     * 文件上传ID
     */
    @NotBlank(message = "文件不能为空")
    @Schema(description = "文件上传ID", example = "upload_123456789")
    private String fileId;

    /**
     * 是否覆盖已存在的数据
     */
    @Schema(description = "是否覆盖已存在的数据", example = "false")
    private Boolean overwrite = false;

    /**
     * 是否跳过验证
     */
    @Schema(description = "是否跳过验证", example = "false")
    private Boolean skipValidation = false;

    /**
     * 批量大小
     * 每批处理的记录数量
     */
    @Min(value = 1, message = "批量大小不能小于1")
    @Max(value = 1000, message = "批量大小不能大于1000")
    @Schema(description = "批量大小", example = "100")
    private Integer batchSize = 100;

    /**
     * 失败时是否继续
     * true-遇到错误继续处理下一条，false-遇到错误立即停止
     */
    @Schema(description = "失败时是否继续", example = "true")
    private Boolean continueOnError = true;

    /**
     * 最大错误数量
     * 当错误数量超过此值时停止处理
     */
    @Min(value = 1, message = "最大错误数量不能小于1")
    @Max(value = 1000, message = "最大错误数量不能大于1000")
    @Schema(description = "最大错误数量", example = "50")
    private Integer maxErrorCount = 50;

    /**
     * 默认关联类型
     * 当导入数据中没有关联类型时使用此默认值
     */
    @Schema(description = "默认关联类型", example = "PRIMARY")
    private String defaultRelationType = "PRIMARY";

    /**
     * 默认优先级
     * 当导入数据中没有优先级时使用此默认值
     */
    @Min(value = 1, message = "默认优先级不能小于1")
    @Max(value = 10, message = "默认优先级不能大于10")
    @Schema(description = "默认优先级", example = "5")
    private Integer defaultPriority = 5;

    /**
     * 默认有效期天数
     * 当导入数据中只有生效时间没有失效时间时，使用此天数计算失效时间
     */
    @Min(value = 1, message = "默认有效期天数不能小于1")
    @Max(value = 3650, message = "默认有效期天数不能大于3650")
    @Schema(description = "默认有效期天数", example = "365")
    private Integer defaultExpireDays = 365;

    /**
     * 是否自动同步设备
     * 导入成功后是否自动触发设备同步
     */
    @Schema(description = "是否自动同步设备", example = "true")
    private Boolean autoSyncDevices = true;

    /**
     * 同步设备类型
     * 自动同步时指定的设备类型列表
     */
    @Schema(description = "同步设备类型", example = "[\"ACCESS\", \"ATTENDANCE\"]")
    private String syncDeviceTypes;

    /**
     * 数据格式
     * EXCEL-Excel文件，CSV-CSV文件
     */
    @Schema(description = "数据格式", example = "EXCEL")
    private String dataFormat = "EXCEL";

    /**
     * 编码格式
     * 文件编码格式
     */
    @Schema(description = "编码格式", example = "UTF-8")
    private String encoding = "UTF-8";

    /**
     * 是否包含表头
     */
    @Schema(description = "是否包含表头", example = "true")
    private Boolean hasHeader = true;

    /**
     * 字段映射配置
     * JSON格式的字段映射关系
     */
    @Schema(description = "字段映射配置", example = "{\"人员编号\": \"personCode\", \"区域编码\": \"areaCode\"}")
    private String fieldMapping;

    /**
     * 验证规则配置
     * JSON格式的验证规则配置
     */
    @Schema(description = "验证规则配置", example = "{\"personCode\": {\"required\": true, \"maxLength\": 50}}")
    private String validationRules;

    /**
     * 干运行模式
     * true-只验证不实际导入，false-实际导入数据
     */
    @Schema(description = "干运行模式", example = "false")
    private Boolean dryRun = false;

    /**
     * 操作来源
     */
    @Schema(description = "操作来源", example = "BATCH_IMPORT")
    private String operateSource = "BATCH_IMPORT";

    /**
     * 备注
     */
    @Schema(description = "备注", example = "批量导入员工区域权限")
    private String remark;

    /**
     * 是否覆盖已存在的关联
     */
    public boolean shouldOverwrite() {
        return overwrite != null && overwrite;
    }

    /**
     * 是否跳过验证
     */
    public boolean shouldSkipValidation() {
        return skipValidation != null && skipValidation;
    }

    /**
     * 是否继续处理错误
     */
    public boolean shouldContinueOnError() {
        return continueOnError != null && continueOnError;
    }

    /**
     * 是否自动同步设备
     */
    public boolean shouldAutoSyncDevices() {
        return autoSyncDevices != null && autoSyncDevices;
    }

    /**
     * 是否包含表头
     */
    public boolean hasFileHeader() {
        return hasHeader != null && hasHeader;
    }

    /**
     * 是否为干运行模式
     */
    public boolean isDryRunMode() {
        return dryRun != null && dryRun;
    }

    /**
     * 获取实际的批量大小
     */
    public Integer getActualBatchSize() {
        return batchSize != null && batchSize > 0 ? batchSize : 100;
    }

    /**
     * 获取实际的最大错误数量
     */
    public Integer getActualMaxErrorCount() {
        return maxErrorCount != null && maxErrorCount > 0 ? maxErrorCount : 50;
    }

    /**
     * 获取实际的默认优先级
     */
    public Integer getActualDefaultPriority() {
        return defaultPriority != null && defaultPriority > 0 ? defaultPriority : 5;
    }

    /**
     * 获取实际的默认有效期天数
     */
    public Integer getActualDefaultExpireDays() {
        return defaultExpireDays != null && defaultExpireDays > 0 ? defaultExpireDays : 365;
    }
}