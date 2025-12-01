package net.lab1024.sa.base.module.area.domain.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 人员区域关联视图对象
 * 用于前端显示人员区域关联信息
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
@Schema(description = "人员区域关联视图对象")
public class PersonAreaRelationVO {

    /**
     * 关联ID
     */
    @Schema(description = "关联ID", example = "1")
    private Long relationId;

    /**
     * 人员ID
     */
    @Schema(description = "人员ID", example = "1001")
    private Long personId;

    /**
     * 人员类型
     */
    @Schema(description = "人员类型", example = "EMPLOYEE")
    private String personType;

    /**
     * 人员类型描述
     */
    @Schema(description = "人员类型描述", example = "员工")
    private String personTypeDesc;

    /**
     * 人员姓名
     */
    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    /**
     * 人员编号
     */
    @Schema(description = "人员编号", example = "EMP001")
    private String personCode;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "101")
    private Long areaId;

    /**
     * 区域编码
     */
    @Schema(description = "区域编码", example = "BUILDING_A_1F")
    private String areaCode;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "A栋1楼")
    private String areaName;

    /**
     * 区域类型
     */
    @Schema(description = "区域类型", example = "3")
    private Integer areaType;

    /**
     * 区域类型描述
     */
    @Schema(description = "区域类型描述", example = "楼层")
    private String areaTypeDesc;

    /**
     * 区域路径
     */
    @Schema(description = "区域路径", example = "示例园区 > A栋 > A栋1楼")
    private String areaPath;

    /**
     * 关联类型
     */
    @Schema(description = "关联类型", example = "PRIMARY")
    private String relationType;

    /**
     * 关联类型描述
     */
    @Schema(description = "关联类型描述", example = "主归属")
    private String relationTypeDesc;

    /**
     * 生效时间
     */
    @Schema(description = "生效时间", example = "2025-01-01 09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    @Schema(description = "失效时间", example = "2025-12-31 18:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 同步状态
     */
    @Schema(description = "同步状态", example = "2")
    private Integer syncStatus;

    /**
     * 同步状态描述
     */
    @Schema(description = "同步状态描述", example = "同步完成")
    private String syncStatusDesc;

    /**
     * 最后同步时间
     */
    @Schema(description = "最后同步时间", example = "2025-11-24 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncTime;

    /**
     * 同步设备类型列表
     */
    @Schema(description = "同步设备类型列表", example = "[\"ACCESS\", \"ATTENDANCE\"]")
    private String syncDeviceTypes;

    /**
     * 同步设备类型描述列表
     */
    @Schema(description = "同步设备类型描述列表", example = "[\"门禁\", \"考勤\"]")
    private List<String> syncDeviceTypeDescList;

    /**
     * 优先级
     */
    @Schema(description = "优先级", example = "5")
    private Integer priorityLevel;

    /**
     * 优先级描述
     */
    @Schema(description = "优先级描述", example = "普通")
    private String priorityDesc;

    /**
     * 是否自动续期
     */
    @Schema(description = "是否自动续期", example = "false")
    private Boolean autoRenew;

    /**
     * 自动续期天数
     */
    @Schema(description = "自动续期天数", example = "30")
    private Integer renewDays;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述", example = "启用")
    private String statusDesc;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "日常办公区域权限")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-11-24 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-11-24 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ==================== 计算属性 ====================

    /**
     * 是否有效
     */
    @Schema(description = "是否有效", example = "true")
    private Boolean active;

    /**
     * 是否需要同步
     */
    @Schema(description = "是否需要同步", example = "false")
    private Boolean needsSync;

    /**
     * 是否即将过期
     */
    @Schema(description = "是否即将过期", example = "false")
    private Boolean expiringSoon;

    /**
     * 是否已过期
     */
    @Schema(description = "是否已过期", example = "false")
    private Boolean expired;

    /**
     * 剩余有效天数
     */
    @Schema(description = "剩余有效天数", example = "30")
    private Long remainingDays;

    /**
     * 同步设备数量
     */
    @Schema(description = "同步设备数量", example = "2")
    private Integer syncDeviceCount;

    /**
     * 状态颜色标识
     */
    @Schema(description = "状态颜色标识", example = "#52c41a")
    private String statusColor;

    /**
     * 优先级颜色标识
     */
    @Schema(description = "优先级颜色标识", example = "#faad14")
    private String priorityColor;

    // ==================== 业务方法 ====================

    /**
     * 获取人员类型描述
     */
    public String getPersonTypeDesc() {
        if (personType == null) {
            return "未知";
        }
        switch (personType) {
            case "EMPLOYEE":
                return "员工";
            case "VISITOR":
                return "访客";
            case "CONTRACTOR":
                return "外包";
            default:
                return personType;
        }
    }

    /**
     * 获取关联类型描述
     */
    public String getRelationTypeDesc() {
        if (relationType == null) {
            return "未知";
        }
        switch (relationType) {
            case "PRIMARY":
                return "主归属";
            case "SECONDARY":
                return "次要归属";
            case "TEMPORARY":
                return "临时";
            default:
                return relationType;
        }
    }

    /**
     * 获取区域类型描述
     */
    public String getAreaTypeDesc() {
        if (areaType == null) {
            return "未知";
        }
        switch (areaType) {
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
     * 获取同步状态描述
     */
    public String getSyncStatusDesc() {
        if (syncStatus == null) {
            return "未知";
        }
        switch (syncStatus) {
            case 0:
                return "待同步";
            case 1:
                return "同步中";
            case 2:
                return "同步完成";
            case 3:
                return "同步失败";
            default:
                return "未知";
        }
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "停用";
            case 1:
                return "启用";
            case 2:
                return "维护中";
            default:
                return "未知";
        }
    }

    /**
     * 获取优先级描述
     */
    public String getPriorityDesc() {
        if (priorityLevel == null) {
            return "普通";
        }

        if (priorityLevel <= 2) {
            return "极高";
        } else if (priorityLevel <= 4) {
            return "高";
        } else if (priorityLevel <= 6) {
            return "普通";
        } else if (priorityLevel <= 8) {
            return "低";
        } else {
            return "极低";
        }
    }

    /**
     * 判断是否有效
     */
    public Boolean getActive() {
        if (status == null || !status.equals(1)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (effectiveTime != null && now.isBefore(effectiveTime)) {
            return false;
        }

        if (expireTime != null && now.isAfter(expireTime)) {
            return false;
        }

        return true;
    }

    /**
     * 判断是否需要同步
     */
    public Boolean getNeedsSync() {
        return !Integer.valueOf(2).equals(syncStatus);
    }

    /**
     * 判断是否即将过期（7天内）
     */
    public Boolean getExpiringSoon() {
        if (expireTime == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);
        return expireTime.isBefore(sevenDaysLater) && expireTime.isAfter(now);
    }

    /**
     * 判断是否已过期
     */
    public Boolean getExpired() {
        if (expireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 获取剩余有效天数
     */
    public Long getRemainingDays() {
        if (expireTime == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expireTime)) {
            return 0L;
        }

        return java.time.Duration.between(now, expireTime).toDays();
    }

    /**
     * 获取同步设备数量
     */
    public Integer getSyncDeviceCount() {
        if (syncDeviceTypes == null || syncDeviceTypes.trim().isEmpty()) {
            return 0;
        }

        try {
            // 简单解析JSON数组
            return syncDeviceTypes.split(",").length;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取同步设备类型描述列表
     */
    public List<String> getSyncDeviceTypeDescList() {
        if (syncDeviceTypes == null || syncDeviceTypes.trim().isEmpty()) {
            return List.of();
        }

        try {
            String[] deviceTypes = syncDeviceTypes.split(",");
            List<String> descList = new ArrayList<>();
            for (String deviceType : deviceTypes) {
                descList.add(getDeviceTypeDesc(deviceType.trim()));
            }
            return descList;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 获取设备类型描述
     */
    private String getDeviceTypeDesc(String deviceType) {
        switch (deviceType) {
            case "ACCESS":
                return "门禁";
            case "ATTENDANCE":
                return "考勤";
            case "CONSUME":
                return "消费";
            case "VIDEO":
                return "视频";
            default:
                return deviceType;
        }
    }

    /**
     * 获取状态颜色标识
     */
    public String getStatusColor() {
        if (status == null) {
            return "#666666";
        }
        switch (status) {
            case 0:
                return "#f5222d"; // 红色 - 停用
            case 1:
                return "#52c41a"; // 绿色 - 启用
            case 2:
                return "#faad14"; // 橙色 - 维护中
            default:
                return "#666666"; // 灰色
        }
    }

    /**
     * 获取优先级颜色标识
     */
    public String getPriorityColor() {
        if (priorityLevel == null) {
            return "#666666";
        }
        switch (priorityLevel) {
            case 1:
            case 2:
                return "#f5222d"; // 红色 - 极高
            case 3:
            case 4:
                return "#fa8c16"; // 深橙色 - 高
            case 5:
            case 6:
                return "#faad14"; // 橙色 - 普通
            case 7:
            case 8:
                return "#52c41a"; // 绿色 - 低
            default:
                return "#1890ff"; // 蓝色 - 极低
        }
    }

    @Override
    public String toString() {
        return "PersonAreaRelationVO{" +
                "relationId" + relationId +
                ", personId" + personId +
                ", personName='" + personName + '\'' +
                ", areaName='" + areaName + '\'' +
                ", relationType='" + relationType + '\'' +
                ", active" + getActive() +
                ", needsSync" + getNeedsSync() +
                "}";
    }
}

