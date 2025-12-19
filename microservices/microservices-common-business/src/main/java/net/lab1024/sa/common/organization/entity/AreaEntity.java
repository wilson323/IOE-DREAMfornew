package net.lab1024.sa.common.organization.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 区域实体类
 * <p>
 * 统一区域管理实体，支持多层级区域结构和权限管理
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_area")
public class AreaEntity extends BaseEntity {

    /**
     * 区域ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long areaId;

    /**
     * 区域编码
     */
    @TableField("area_code")
    private String areaCode;

    /**
     * 区域名称
     */
    @TableField("area_name")
    private String areaName;

    /**
     * 区域类型：1-园区 2-建筑 3-楼层 4-房间 5-区域 6-点位
     */
    @TableField("area_type")
    private Integer areaType;

    /**
     * 父区域ID
     */
    @TableField("parent_area_id")
    private Long parentAreaId;

    /**
     * 区域层级：1-一级 2-二级 3-三级 4-四级 5-五级
     */
    @TableField("area_level")
    private Integer areaLevel;

    /**
     * 区域路径（如：1/2/3）
     */
    @TableField("area_path")
    private String areaPath;

    /**
     * 区域状态：1-正常 2-停用 3-装修 4-关闭
     */
    @TableField("area_status")
    private Integer areaStatus;

    /**
     * 区域用途：1-办公 2-生产 3-仓储 4-公共 5-特殊
     */
    @TableField("area_purpose")
    private Integer areaPurpose;

    /**
     * 建筑面积（平方米）
     */
    @TableField("area_size")
    private Double areaSize;

    /**
     * 容纳人数
     */
    @TableField("capacity")
    private Integer capacity;

    /**
     * 当前人数
     */
    @TableField("current_count")
    private Integer currentCount;

    /**
     * 反潜回类型：NONE-无反潜回 HARD-硬反潜回 SOFT-软反潜回 AREA-区域反潜回 GLOBAL-全局反潜回
     */
    @TableField("anti_passback_type")
    private String antiPassbackType;

    /**
     * 负责人ID
     */
    @TableField("manager_id")
    private Long managerId;

    /**
     * 负责人姓名
     */
    @TableField("manager_name")
    private String managerName;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 区域地址
     */
    @TableField("address")
    private String address;

    /**
     * 经度
     */
    @TableField("longitude")
    private Double longitude;

    /**
     * 纬度
     */
    @TableField("latitude")
    private Double latitude;

    /**
     * 地图缩放级别
     */
    @TableField("map_zoom")
    private Integer mapZoom;

    /**
     * 区域图片
     */
    @TableField("area_image")
    private String areaImage;

    /**
     * 区域描述
     */
    @TableField("description")
    private String description;

    /**
     * 访问权限：1-公开 2-限制 3-私有
     */
    @TableField("access_permission")
    private Integer accessPermission;

    /**
     * 允许访问的角色（JSON数组）
     */
    @TableField("allowed_roles")
    private String allowedRoles;

    /**
     * 允许访问的人员（JSON数组）
     */
    @TableField("allowed_users")
    private String allowedUsers;

    /**
     * 工作时间开始
     */
    @TableField("work_time_start")
    private String workTimeStart;

    /**
     * 工作时间结束
     */
    @TableField("work_time_end")
    private String workTimeEnd;

    /**
     * 工作日：1-周一 2-周二 3-周三 4-周四 5-周五 6-周六 7-周日
     */
    @TableField("work_days")
    private String workDays;

    /**
     * 安防等级：1-普通 2-重要 3-核心 4-机密
     */
    @TableField("security_level")
    private Integer securityLevel;

    /**
     * 环境温度要求
     */
    @TableField("temperature_requirement")
    private String temperatureRequirement;

    /**
     * 湿度要求
     */
    @TableField("humidity_requirement")
    private String humidityRequirement;

    /**
     * 消防等级：1-一般 2-重点 3-特级
     */
    @TableField("fire_level")
    private Integer fireLevel;

    /**
     * 消防负责人
     */
    @TableField("fire_manager")
    private String fireManager;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;

    /**
     * 获取状态（兼容getStatus()调用）
     */
    public Integer getStatus() {
        return this.areaStatus;
    }

    /**
     * 设置状态（兼容setStatus()调用）
     */
    public void setStatus(Integer status) {
        this.areaStatus = status;
    }

    /**
     * 获取ID（兼容getId()调用）
     */
    public Long getId() {
        return this.areaId;
    }

    /**
     * 设置ID（兼容setId()调用）
     */
    public void setId(Long id) {
        this.areaId = id;
    }

    /**
     * 获取父ID（兼容getParentId()调用）
     */
    public Long getParentId() {
        return this.parentAreaId;
    }

    /**
     * 设置父ID（兼容setParentId()调用）
     */
    public void setParentId(Long parentId) {
        this.parentAreaId = parentId;
    }

    /**
     * 子区域列表（非数据库字段，用于构建树形结构）
     */
    @TableField(exist = false)
    private java.util.List<AreaEntity> children;

    /**
     * 获取子区域
     */
    public java.util.List<AreaEntity> getChildren() {
        return this.children;
    }

    /**
     * 设置子区域
     */
    public void setChildren(java.util.List<?> children) {
        if (children == null) {
            this.children = null;
        } else {
            this.children = new java.util.ArrayList<>();
            for (Object child : children) {
                if (child instanceof AreaEntity) {
                    this.children.add((AreaEntity) child);
                }
            }
        }
    }

    /**
     * 获取路径（兼容getPath()调用）
     */
    public String getPath() {
        return this.areaPath;
    }

    /**
     * 设置路径（兼容setPath()调用）
     */
    public void setPath(String path) {
        this.areaPath = path;
    }
}
