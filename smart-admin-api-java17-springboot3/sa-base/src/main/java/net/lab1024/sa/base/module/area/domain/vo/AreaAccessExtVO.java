package net.lab1024.sa.base.module.area.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 区域门禁扩展视图对象
 * 用于展示门禁模块特有的区域配置信息
 *
 * 命名规范统一：符合{BaseDomain}{Module}ExtVO标准模式
 * 演进记录：从AccessAreaExtVO重构而来，保持向后兼容
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
@Schema(description = "区域门禁扩展视图对象")
public class AreaAccessExtVO {

    /**
     * 扩展ID
     */
    @Schema(description = "扩展ID", example = "1")
    private Long extId;

    /**
     * 基础区域ID
     */
    @Schema(description = "基础区域ID", example = "1")
    private Long areaId;

    /**
     * 区域编码
     * 来自基础区域表
     */
    @Schema(description = "区域编码", example = "BUILDING_A_1F")
    private String areaCode;

    /**
     * 区域名称
     * 来自基础区域表
     */
    @Schema(description = "区域名称", example = "A栋1楼")
    private String areaName;

    /**
     * 区域类型
     * 来自基础区域表
     */
    @Schema(description = "区域类型", example = "3")
    private Integer areaType;

    /**
     * 区域类型名称
     */
    @Schema(description = "区域类型名称", example = "楼层")
    private String areaTypeName;

    /**
     * 门禁级别
     * 1:普通 2:重要 3:核心
     */
    @Schema(description = "门禁级别", example = "1")
    private Integer accessLevel;

    /**
     * 门禁级别名称
     */
    @Schema(description = "门禁级别名称", example = "普通")
    private String accessLevelName;

    /**
     * 安全级别颜色
     * 用于前端显示
     */
    @Schema(description = "安全级别颜色", example = "#52c41a")
    private String securityLevelColor;

    /**
     * 门禁模式
     * 多种验证方式组合
     */
    @Schema(description = "门禁模式", example = "卡,指纹,人脸")
    private String accessMode;

    /**
     * 门禁模式数组
     * 方便前端处理
     */
    @Schema(description = "门禁模式数组", example = "[\"卡\", \"指纹\", \"人脸\"]")
    private String[] accessModes;

    /**
     * 关联设备数量
     */
    @Schema(description = "关联设备数量", example = "3")
    private Integer deviceCount;

    /**
     * 是否需要安保人员
     */
    @Schema(description = "是否需要安保人员", example = "false")
    private Boolean guardRequired;

    /**
     * 时间限制配置
     * JSON格式
     */
    @Schema(description = "时间限制配置", example = "{\"workdays\": {\"start\": \"08:00\", \"end\": \"18:00\"}}")
    private String timeRestrictions;

    /**
     * 是否允许访客
     */
    @Schema(description = "是否允许访客", example = "true")
    private Boolean visitorAllowed;

    /**
     * 是否为紧急通道
     */
    @Schema(description = "是否为紧急通道", example = "false")
    private Boolean emergencyAccess;

    /**
     * 是否启用监控
     */
    @Schema(description = "是否启用监控", example = "true")
    private Boolean monitoringEnabled;

    /**
     * 告警配置
     * JSON格式
     */
    @Schema(description = "告警配置", example = "{\"motion\": true, \"door_open\": true}")
    private String alertConfig;

    /**
     * 区域状态
     * 来自基础区域表
     */
    @Schema(description = "区域状态", example = "1")
    private Integer status;

    /**
     * 区域状态名称
     */
    @Schema(description = "区域状态名称", example = "正常")
    private String statusName;

    /**
     * 层级深度
     * 来自基础区域表
     */
    @Schema(description = "层级深度", example = "3")
    private Integer level;

    /**
     * 区域路径
     * 来自基础区域表
     */
    @Schema(description = "区域路径", example = "示例园区 > A栋 > A栋1楼")
    private String path;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-11-24T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-11-24T10:00:00")
    private LocalDateTime updateTime;

    /**
     * 是否为高级别门禁
     * 计算属性
     */
    @Schema(description = "是否为高级别门禁", example = "false")
    private Boolean isHighSecurity;

    /**
     * 是否为核心门禁
     * 计算属性
     */
    @Schema(description = "是否为核心门禁", example = "false")
    private Boolean isCoreSecurity;

    /**
     * 获取门禁级别名称
     */
    public String getAccessLevelName() {
        if (this.accessLevel == null) {
            return "未知";
        }
        switch (this.accessLevel) {
            case 1:
                return "普通";
            case 2:
                return "重要";
            case 3:
                return "核心";
            default:
                return "未知";
        }
    }

    /**
     * 获取安全级别颜色
     */
    public String getSecurityLevelColor() {
        if (this.accessLevel == null) {
            return "#666666"; // 灰色
        }
        switch (this.accessLevel) {
            case 1:
                return "#52c41a"; // 绿色
            case 2:
                return "#faad14"; // 橙色
            case 3:
                return "#f5222d"; // 红色
            default:
                return "#666666"; // 灰色
        }
    }

    /**
     * 获取区域状态名称
     */
    public String getStatusName() {
        if (this.status == null) {
            return "未知";
        }
        switch (this.status) {
            case 0:
                return "停用";
            case 1:
                return "正常";
            case 2:
                return "维护中";
            default:
                return "未知";
        }
    }

    /**
     * 获取区域类型名称
     */
    public String getAreaTypeName() {
        if (this.areaType == null) {
            return "未知";
        }
        switch (this.areaType) {
            case 1:
                return "园区";
            case 2:
                return "建筑";
            case 3:
                return "楼层";
            case 4:
                return "房间";
            case 5:
                return "区域";
            case 6:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 判断是否为高级别门禁
     */
    public Boolean getIsHighSecurity() {
        return this.accessLevel != null && this.accessLevel >= 2;
    }

    /**
     * 判断是否为核心门禁
     */
    public Boolean getIsCoreSecurity() {
        return this.accessLevel != null && this.accessLevel >= 3;
    }

    /**
     * 获取门禁模式数组
     */
    public String[] getAccessModes() {
        if (this.accessMode == null || this.accessMode.trim().isEmpty()) {
            return new String[0];
        }
        return this.accessMode.split(",");
    }

    /**
     * 格式化设备数量显示
     */
    public String getDeviceCountDisplay() {
        if (this.deviceCount == null || this.deviceCount == 0) {
            return "无设备";
        } else if (this.deviceCount == 1) {
            return "1台设备";
        } else {
            return this.deviceCount + "台设备";
        }
    }

    /**
     * 获取安全级别图标
     * 用于前端显示
     */
    public String getSecurityLevelIcon() {
        if (this.accessLevel == null) {
            return "shield-line";
        }
        switch (this.accessLevel) {
            case 1:
                return "shield-line"; // 普通防护
            case 2:
                return "shield-check-line"; // 重要防护
            case 3:
                return "shield-star-line"; // 核心防护
            default:
                return "shield-line";
        }
    }

    /**
     * 获取状态颜色
     * 用于前端显示
     */
    public String getStatusColor() {
        if (this.status == null) {
            return "#666666";
        }
        switch (this.status) {
            case 0:
                return "#f5222d"; // 红色 - 停用
            case 1:
                return "#52c41a"; // 绿色 - 正常
            case 2:
                return "#faad14"; // 橙色 - 维护中
            default:
                return "#666666";
        }
    }

    @Override
    public String toString() {
        return "AccessAreaExtVO{" +
                "extId" + extId +
                ", areaId" + areaId +
                ", areaCode='" + areaCode + '\'' +
                ", areaName='" + areaName + '\'' +
                ", accessLevel" + accessLevel +
                ", accessLevelName='" + getAccessLevelName() + '\'' +
                ", deviceCount" + deviceCount +
                ", guardRequired" + guardRequired +
                ", visitorAllowed" + visitorAllowed +
                ", emergencyAccess" + emergencyAccess +
                ", monitoringEnabled" + monitoringEnabled +
                '}';
    }
}