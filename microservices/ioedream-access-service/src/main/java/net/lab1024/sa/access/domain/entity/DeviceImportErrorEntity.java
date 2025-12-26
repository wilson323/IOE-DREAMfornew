package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 设备导入错误明细实体
 * <p>
 * 记录批量导入设备时的错误详情：
 * - 错误行号
 * - 原始数据
 * - 错误码和错误消息
 * - 验证错误详情
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_import_error")
@Schema(description = "设备导入错误明细实体")
public class DeviceImportErrorEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "错误ID")
    private Long errorId;

    @Schema(description = "批次ID", required = true)
    private Long batchId;

    @Schema(description = "行号（Excel行号）", required = true, example = "5")
    private Integer rowNumber;

    // ==================== 原始数据 ====================

    @Schema(description = "原始数据（JSON格式）")
    @TableField(value = "raw_data")
    private String rawData;

    // ==================== 错误信息 ====================

    @Schema(description = "错误码", example = "DEVICE_CODE_REQUIRED")
    private String errorCode;

    @Schema(description = "错误消息", example = "设备编码不能为空")
    private String errorMessage;

    @Schema(description = "错误字段", example = "deviceCode")
    private String errorField;

    // ==================== 数据验证结果 ====================

    @Schema(description = "验证错误详情（JSON格式）")
    @TableField(value = "validation_errors")
    private String validationErrors;

    // ==================== 显式添加 getter/setter 方法以确保编译通过 ====================

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
