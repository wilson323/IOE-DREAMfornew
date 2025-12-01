package net.lab1024.sa.base.module.area.domain.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.alibaba.fastjson2.JSON;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 人员区域关联实体类
 * 管理人员与区域的归属关系和设备同步配置
 *
 * 职责：区域管理模块中的人员区域分配和设备同步管理
 * 与AreaPersonEntity分离：
 * - AreaPersonEntity：RBAC权限控制（数据权限）
 * - PersonAreaRelationEntity：业务关系管理（区域分配、设备同步）
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 * @updated 2025-11-25 明确职责分离，基于现有功能增强
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_person_area_relation")
public class PersonAreaRelationEntity extends BaseEntity {

    /**
     * 关联ID
     */
    @TableId("relation_id")
    private Long relationId;

    /**
     * 人员ID
     */
    @TableField("person_id")
    private Long personId;

    /**
     * 人员类型
     * EMPLOYEE-员工, VISITOR-访客, CONTRACTOR-外包
     */
    @TableField("person_type")
    private String personType;

    /**
     * 人员姓名（冗余字段）
     */
    @TableField("person_name")
    private String personName;

    /**
     * 人员编号（冗余字段）
     */
    @TableField("person_code")
    private String personCode;

    /**
     * 区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 关联类型
     * PRIMARY-主归属, SECONDARY-次要归属, TEMPORARY-临时
     */
    @TableField("relation_type")
    private String relationType;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间（NULL表示永久有效）
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 同步状态
     * 0-待同步, 1-同步中, 2-同步完成, 3-同步失败
     */
    @TableField("sync_status")
    private Integer syncStatus;

    /**
     * 最后同步时间
     */
    @TableField("last_sync_time")
    private LocalDateTime lastSyncTime;

    /**
     * 需要同步的设备类型列表
     * JSON格式：["ACCESS", "ATTENDANCE", "CONSUME", "VIDEO"]
     */
    @TableField("sync_device_types")
    private String syncDeviceTypes;

    /**
     * 同步配置
     * JSON格式存储各设备类型的特定配置
     */
    @TableField("sync_config")
    private String syncConfig;

    /**
     * 优先级（1-10，数字越小优先级越高）
     */
    @TableField("priority_level")
    private Integer priorityLevel;

    /**
     * 是否自动续期
     */
    @TableField("auto_renew")
    private Boolean autoRenew;

    /**
     * 自动续期天数
     */
    @TableField("renew_days")
    private Integer renewDays;

    /**
     * 状态
     * 0-停用, 1-启用
     */
    @TableField("status")
    private Integer status;

    // ==================== 新增：数据域功能（来自AreaPersonEntity） ====================

    /**
     * 数据域(AREA|DEPT|SELF|CUSTOM)
     * 用于RBAC权限控制，指定人员的数据访问范围
     * AREA: 指定区域权限
     * DEPT: 部门权限
     * SELF: 仅本人权限
     * CUSTOM: 自定义权限
     */
    @TableField("data_scope")
    private String dataScope;

    /**
     * 访问级别
     * 数字越大权限要求越高
     */
    @TableField("access_level")
    private Integer accessLevel;

    /**
     * 特殊权限配置（JSON格式）
     * 存储个性化的权限配置信息
     */
    @TableField("special_permissions")
    private String specialPermissions;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;

    // ==================== 非数据库字段 ====================

    /**
     * 人员姓名（关联查询）
     */
    @TableField(exist = false)
    private String personNameFromDb;

    /**
     * 区域名称（关联查询）
     */
    @TableField(exist = false)
    private String areaName;

    /**
     * 区域编码（关联查询）
     */
    @TableField(exist = false)
    private String areaCode;

    /**
     * 区域类型（关联查询）
     */
    @TableField(exist = false)
    private Integer areaType;

    /**
     * 区域路径（关联查询）
     */
    @TableField(exist = false)
    private String areaPath;

    /**
     * 同步设备类型列表（解析后）
     */
    @TableField(exist = false)
    private List<String> syncDeviceTypeList;

    /**
     * 同步配置映射（解析后）
     */
    @TableField(exist = false)
    private Map<String, Object> syncConfigMap;

    // ==================== 业务方法 ====================

    /**
     * 判断关联是否有效
     */
    public boolean isActive() {
        if (!Integer.valueOf(1).equals(status)) {
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
    public boolean needsSync() {
        return !Integer.valueOf(2).equals(syncStatus);
    }

    /**
     * 判断是否即将过期（7天内）
     */
    public boolean isExpiringSoon() {
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
    public boolean isExpired() {
        if (expireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (!Integer.valueOf(1).equals(status)) {
            return "停用";
        }

        if (expireTime == null) {
            return "永久";
        }

        if (isExpired()) {
            return "已过期";
        }

        if (isExpiringSoon()) {
            return "即将过期";
        }

        return "正常";
    }

    /**
     * 获取同步状态描述
     */
    public String getSyncStatusDesc() {
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
     * 获取人员类型描述
     */
    public String getPersonTypeDesc() {
        switch (personType) {
            case "EMPLOYEE":
                return "员工";
            case "VISITOR":
                return "访客";
            case "CONTRACTOR":
                return "外包";
            default:
                return "未知";
        }
    }

    /**
     * 获取关联类型描述
     */
    public String getRelationTypeDesc() {
        switch (relationType) {
            case "PRIMARY":
                return "主归属";
            case "SECONDARY":
                return "次要归属";
            case "TEMPORARY":
                return "临时";
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
     * 解析同步设备类型列表
     */
    public List<String> getSyncDeviceTypeList() {
        if (syncDeviceTypeList != null) {
            return syncDeviceTypeList;
        }

        try {
            if (syncDeviceTypes != null && !syncDeviceTypes.trim().isEmpty()) {
                syncDeviceTypeList = JSON.parseArray(syncDeviceTypes, String.class);
            } else {
                syncDeviceTypeList = List.of(); // 空列表
            }
        } catch (Exception e) {
            log.error("解析同步设备类型失败: {}", syncDeviceTypes, e);
            syncDeviceTypeList = List.of();
        }

        return syncDeviceTypeList;
    }

    /**
     * 设置同步设备类型列表
     */
    public void setSyncDeviceTypeList(List<String> deviceTypes) {
        this.syncDeviceTypeList = deviceTypes;
        try {
            if (deviceTypes == null || deviceTypes.isEmpty()) {
                this.syncDeviceTypes = null;
            } else {
                this.syncDeviceTypes = JSON.toJSONString(deviceTypes);
            }
        } catch (Exception e) {
            log.error("序列化同步设备类型失败", e);
            this.syncDeviceTypes = null;
        }
    }

    /**
     * 解析同步配置映射
     */
    public Map<String, Object> getSyncConfigMap() {
        if (syncConfigMap != null) {
            return syncConfigMap;
        }

        try {
            if (syncConfig != null && !syncConfig.trim().isEmpty()) {
                syncConfigMap = JSON.parseObject(syncConfig, Map.class);
            } else {
                syncConfigMap = Map.of();
            }
        } catch (Exception e) {
            log.error("解析同步配置失败: {}", syncConfig, e);
            syncConfigMap = Map.of();
        }

        return syncConfigMap;
    }

    /**
     * 设置同步配置映射
     */
    public void setSyncConfigMap(Map<String, Object> configMap) {
        this.syncConfigMap = configMap;
        try {
            if (configMap == null || configMap.isEmpty()) {
                this.syncConfig = null;
            } else {
                this.syncConfig = JSON.toJSONString(configMap);
            }
        } catch (Exception e) {
            log.error("序列化同步配置失败", e);
            this.syncConfig = null;
        }
    }

    /**
     * 添加设备类型到同步列表
     */
    public void addSyncDeviceType(String deviceType) {
        List<String> deviceTypes = getSyncDeviceTypeList();
        if (!deviceTypes.contains(deviceType)) {
            deviceTypes.add(deviceType);
            setSyncDeviceTypeList(deviceTypes);
        }
    }

    /**
     * 从同步列表移除设备类型
     */
    public void removeSyncDeviceType(String deviceType) {
        List<String> deviceTypes = getSyncDeviceTypeList();
        deviceTypes.remove(deviceType);
        setSyncDeviceTypeList(deviceTypes);
    }

    /**
     * 检查是否需要同步指定设备类型
     */
    public boolean needsSyncDeviceType(String deviceType) {
        List<String> deviceTypes = getSyncDeviceTypeList();
        return deviceTypes.contains(deviceType);
    }

    /**
     * 获取同步配置中的设备特定配置
     */
    public Map<String, Object> getDeviceSyncConfig(String deviceType) {
        Map<String, Object> configMap = getSyncConfigMap();
        Object deviceConfig = configMap.get(deviceType);
        if (deviceConfig instanceof Map) {
            return (Map<String, Object>) deviceConfig;
        }
        return Map.of();
    }

    /**
     * 设置设备的同步配置
     */
    public void setDeviceSyncConfig(String deviceType, Map<String, Object> deviceConfig) {
        Map<String, Object> configMap = getSyncConfigMap();
        configMap.put(deviceType, deviceConfig);
        setSyncConfigMap(configMap);
    }

    /**
     * 更新同步状态
     */
    public void updateSyncStatus(Integer newStatus, String errorMessage) {
        this.syncStatus = newStatus;
        this.lastSyncTime = LocalDateTime.now();

        // 如果是失败状态，在配置中记录错误信息
        if (Integer.valueOf(3).equals(newStatus) && errorMessage != null) {
            Map<String, Object> configMap = getSyncConfigMap();
            configMap.put("lastError", Map.of(
                "message", errorMessage,
                "time", this.lastSyncTime.toString()
            ));
            setSyncConfigMap(configMap);
        }
    }

    /**
     * 自动续期（如果有配置）
     */
    public boolean autoRenew() {
        if (!Boolean.TRUE.equals(autoRenew) || renewDays == null || renewDays <= 0) {
            return false;
        }

        if (expireTime == null) {
            return true; // 永久有效，无需续期
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expireTime)) {
            // 已过期，从当前时间开始续期
            this.expireTime = now.plusDays(renewDays);
        } else {
            // 未过期，在原到期时间基础上续期
            this.expireTime = this.expireTime.plusDays(renewDays);
        }

        return true;
    }

    @Override
    public String toString() {
        return "PersonAreaRelationEntity{" +
                "relationId" + relationId +
                ", personId" + personId +
                ", personType='" + personType + '\'' +
                ", personCode='" + personCode + '\'' +
                ", areaId" + areaId +
                ", relationType='" + relationType + '\'' +
                ", effectiveTime" + effectiveTime +
                ", expireTime" + expireTime +
                ", syncStatus" + syncStatus +
                ", status" + status +
                ", priorityLevel" + priorityLevel +
                ", active" + isActive() +
                '}';
    }
}