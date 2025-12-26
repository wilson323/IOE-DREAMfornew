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
 * 设备导入成功记录实体
 * <p>
 * 记录批量导入设备成功的记录，用于追溯：
 * - 导入后的设备ID
 * - 行号
 * - 导入数据快照
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_import_success")
@Schema(description = "设备导入成功记录实体")
public class DeviceImportSuccessEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "成功ID")
    private Long successId;

    @Schema(description = "批次ID", required = true)
    private Long batchId;

    @Schema(description = "导入后的设备ID", example = "1001")
    private Long deviceId;

    @Schema(description = "行号（Excel行号）", required = true, example = "3")
    private Integer rowNumber;

    // ==================== 导入数据快照（用于追溯） ====================

    @Schema(description = "设备编码", required = true, example = "DEV001")
    private String deviceCode;

    @Schema(description = "设备名称", example = "1号门禁控制器")
    private String deviceName;

    @Schema(description = "设备类型: 1-门禁 2-考勤 3-消费 4-视频 5-访客 6-生物识别 7-对讲 8-报警 9-传感器", example = "1")
    private Integer deviceType;

    @Schema(description = "导入的数据（JSON格式，包含所有导入字段）")
    @TableField(value = "imported_data")
    private String importedData;

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

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getImportedData() {
        return importedData;
    }

    public void setImportedData(String importedData) {
        this.importedData = importedData;
    }
}
