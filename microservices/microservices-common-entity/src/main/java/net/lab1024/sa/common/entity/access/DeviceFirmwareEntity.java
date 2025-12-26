package net.lab1024.sa.common.entity.access;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 设备固件实体类
 * <p>
 * 管理设备固件版本信息，支持固件升级和版本管理
 * 提供固件上传、版本控制、下载统计等功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 固件版本管理
 * - 设备固件升级
 * - 固件下载统计
 * - 版本兼容性管理
 * - 固件发布流程
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device_firmware")
@Schema(description = "设备固件实体")
public class DeviceFirmwareEntity extends BaseEntity {

    /**
     * 固件ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "固件ID")
    private Long firmwareId;

    /**
     * 固件名称
     */
    @Schema(description = "固件名称")
    private String firmwareName;

    /**
     * 固件版本号（如v1.0.0、v2.1.3）
     */
    @Schema(description = "固件版本号")
    private String firmwareVersion;

    /**
     * 设备类型（1-门禁设备 2-考勤设备 3-消费设备 4-视频设备）
     */
    @Schema(description = "设备类型（1-门禁设备 2-考勤设备 3-消费设备 4-视频设备）")
    private Integer deviceType;

    /**
     * 设备类型名称
     */
    @Schema(description = "设备类型名称")
    private String deviceTypeName;

    /**
     * 设备型号（支持多个型号，用逗号分隔）
     */
    @Schema(description = "设备型号")
    private String deviceModel;

    /**
     * 适用设备型号列表（JSON数组，如["MODEL-A", "MODEL-B"]）
     */
    @Schema(description = "适用设备型号列表（JSON数组）")
    private String compatibleModels;

    /**
     * 固件文件路径
     */
    @Schema(description = "固件文件路径")
    private String firmwareFile;

    /**
     * 固件文件大小（字节）
     */
    @Schema(description = "固件文件大小（字节）")
    private Long fileSize;

    /**
     * 固件文件MD5校验值
     */
    @Schema(description = "固件文件MD5校验值")
    private String fileMd5;

    /**
     * 最小硬件版本要求（如v1.0.0）
     */
    @Schema(description = "最小硬件版本要求")
    private String minHardwareVersion;

    /**
     * 最大硬件版本限制（如v2.0.0）
     */
    @Schema(description = "最大硬件版本限制")
    private String maxHardwareVersion;

    /**
     * 最小固件版本要求（用于增量升级）
     */
    @Schema(description = "最小固件版本要求")
    private String minFirmwareVersion;

    /**
     * 发布日期
     */
    @Schema(description = "发布日期")
    private LocalDateTime releaseDate;

    /**
     * 发布人ID
     */
    @Schema(description = "发布人ID")
    private Long publisherId;

    /**
     * 发布人姓名
     */
    @Schema(description = "发布人姓名")
    private String publisherName;

    /**
     * 固件状态（1-测试中 2-正式发布 3-已停用 4-已下架）
     */
    @Schema(description = "固件状态（1-测试中 2-正式发布 3-已停用 4-已下架）")
    private Integer firmwareStatus;

    /**
     * 固件状态名称
     */
    @Schema(description = "固件状态名称")
    private String firmwareStatusName;

    /**
     * 是否启用（0-禁用 1-启用）
     */
    @Schema(description = "是否启用")
    private Integer enabled;

    /**
     * 是否强制升级（0-否 1-是）
     */
    @Schema(description = "是否强制升级")
    private Integer forceUpgrade;

    /**
     * 下载次数
     */
    @Schema(description = "下载次数")
    private Integer downloadCount;

    /**
     * 升级次数
     */
    @Schema(description = "升级次数")
    private Integer upgradeCount;

    /**
     * 升级成功次数
     */
    @Schema(description = "升级成功次数")
    private Integer upgradeSuccessCount;

    /**
     * 升级失败次数
     */
    @Schema(description = "升级失败次数")
    private Integer upgradeFailCount;

    /**
     * 发布说明
     */
    @Schema(description = "发布说明")
    private String releaseNotes;

    /**
     * 固件描述
     */
    @Schema(description = "固件描述")
    private String description;

    /**
     * 升级指南（Markdown格式）
     */
    @Schema(description = "升级指南（Markdown格式）")
    private String upgradeGuide;

    /**
     * 已知问题（JSON格式）
     */
    @Schema(description = "已知问题（JSON格式）")
    private String knownIssues;

    /**
     * 回滚版本号（如果此版本有问题，可回滚到此版本）
     */
    @Schema(description = "回滚版本号")
    private String rollbackVersion;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;

    // ==================== 业务方法 ====================

    /**
     * 检查固件是否启用
     *
     * @return true-启用，false-禁用
     */
    public boolean isEnabled() {
        return enabled != null && enabled == 1;
    }

    /**
     * 检查是否正式发布
     *
     * @return true-正式发布，false-非正式发布
     */
    public boolean isReleased() {
        return firmwareStatus != null && firmwareStatus == 2;
    }

    /**
     * 检查是否强制升级
     *
     * @return true-强制升级，false-可选升级
     */
    public boolean isForceUpgrade() {
        return forceUpgrade != null && forceUpgrade == 1;
    }

    /**
     * 检查是否兼容指定设备型号
     *
     * @param deviceModel 设备型号
     * @return true-兼容，false-不兼容
     */
    public boolean isCompatibleModel(String deviceModel) {
        if (deviceModel == null || deviceModel.isEmpty()) {
            return false;
        }
        if (this.deviceModel == null || this.deviceModel.isEmpty()) {
            return true; // 未限制型号
        }

        // 支持逗号分隔的多个型号
        String[] models = this.deviceModel.split(",");
        for (String model : models) {
            if (model.trim().equals(deviceModel)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查版本兼容性
     *
     * @param currentVersion 当前固件版本
     * @return true-兼容，false-不兼容
     */
    public boolean isVersionCompatible(String currentVersion) {
        if (currentVersion == null || currentVersion.isEmpty()) {
            return true; // 无版本信息，默认兼容
        }

        // 检查最小版本要求
        if (minFirmwareVersion != null && !minFirmwareVersion.isEmpty()) {
            if (!compareVersion(currentVersion, minFirmwareVersion) >= 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 比较版本号
     *
     * @param version1 版本1
     * @param version2 版本2
     * @return >0表示version1更新，=0表示相同，<0表示version1更旧
     */
    private int compareVersion(String version1, String version2) {
        try {
            String[] v1 = version1.replace("v", "").split("\\.");
            String[] v2 = version2.replace("v", "").split("\\.");

            int maxLength = Math.max(v1.length, v2.length);
            for (int i = 0; i < maxLength; i++) {
                int num1 = i < v1.length ? Integer.parseInt(v1[i]) : 0;
                int num2 = i < v2.length ? Integer.parseInt(v2[i]) : 0;
                if (num1 != num2) {
                    return num1 - num2;
                }
            }
            return 0;
        } catch (Exception e) {
            // 版本号格式错误，按字符串比较
            return version1.compareTo(version2);
        }
    }

    /**
     * 检查是否比指定版本新
     *
     * @param version 版本号
     * @return true-更新，false-更旧或相同
     */
    public boolean isNewerThan(String version) {
        return compareVersion(this.firmwareVersion, version) > 0;
    }

    /**
     * 获取升级成功率
     *
     * @return 成功率（0-1之间的小数）
     */
    public double getUpgradeSuccessRate() {
        if (upgradeCount == null || upgradeCount == 0) {
            return 0.0;
        }
        int successCount = upgradeSuccessCount != null ? upgradeSuccessCount : 0;
        return (double) successCount / upgradeCount;
    }

    /**
     * 获取文件大小（可读格式）
     *
     * @return 文件大小字符串（如"10.5 MB"）
     */
    public String getFileSizeReadable() {
        if (fileSize == null) {
            return "未知";
        }

        final long KB = 1024;
        final long MB = KB * 1024;
        final long GB = MB * 1024;

        if (fileSize >= GB) {
            return String.format("%.2f GB", fileSize / (double) GB);
        } else if (fileSize >= MB) {
            return String.format("%.2f MB", fileSize / (double) MB);
        } else if (fileSize >= KB) {
            return String.format("%.2f KB", fileSize / (double) KB);
        } else {
            return fileSize + " B";
        }
    }

    /**
     * 获取固件状态名称
     *
     * @return 状态名称
     */
    public String getFirmwareStatusName() {
        if (firmwareStatus == null) {
            return "未知";
        }
        switch (firmwareStatus) {
            case 1: return "测试中";
            case 2: return "正式发布";
            case 3: return "已停用";
            case 4: return "已下架";
            default: return "未知";
        }
    }
}
