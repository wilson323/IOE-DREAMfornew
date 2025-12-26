package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 设备导入批次实体
 * <p>
 * 记录批量导入设备任务的信息：
 * - 导入文件信息
 * - 导入统计数据
 * - 导入状态跟踪
 * - 错误信息汇总
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_import_batch")
@Schema(description = "设备导入批次实体")
public class DeviceImportBatchEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "导入批次ID")
    private Long batchId;

    @Schema(description = "批次名称", example = "2025年1月设备导入")
    private String batchName;

    @Schema(description = "导入文件名", example = "devices_20250130.xlsx")
    private String fileName;

    @Schema(description = "文件大小（字节）", example = "1048576")
    private Long fileSize;

    @Schema(description = "文件MD5值", example = "d41d8cd98f00b204e9800998ecf8427e")
    private String fileMd5;

    // ==================== 导入统计 ====================

    @Schema(description = "总记录数", example = "100")
    private Integer totalCount;

    @Schema(description = "成功数量", example = "95")
    private Integer successCount;

    @Schema(description = "失败数量", example = "3")
    private Integer failedCount;

    @Schema(description = "跳过数量（已存在）", example = "2")
    private Integer skippedCount;

    // ==================== 导入状态 ====================

    @Schema(description = "导入状态: 0-处理中 1-成功 2-部分失败 3-全部失败", example = "1")
    private Integer importStatus;

    @Schema(description = "状态消息", example = "导入完成")
    private String statusMessage;

    @Schema(description = "错误摘要（JSON格式）")
    private String errorSummary;

    // ==================== 时间信息 ====================

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "耗时（毫秒）", example = "5230")
    private Long durationMs;

    // ==================== 操作信息 ====================

    @Schema(description = "操作人类型: 1-管理员 2-普通用户", example = "1")
    private Integer operatorType;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "张三")
    private String operatorName;

    // ==================== 显式添加 getter/setter 方法以确保编译通过 ====================

    public Long getBatchId() {
        return batchId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getImportStatus() {
        return importStatus;
    }

    public void setImportStatus(Integer importStatus) {
        this.importStatus = importStatus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Integer getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(Integer operatorType) {
        this.operatorType = operatorType;
    }
}
