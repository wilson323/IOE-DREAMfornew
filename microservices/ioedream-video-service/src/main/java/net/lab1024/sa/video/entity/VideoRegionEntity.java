package net.lab1024.sa.video.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 视频区域管理实体类
 * <p>
 * 视频监控区域管理实体，支持层级化区域结构和设备关联
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_region")
public class VideoRegionEntity extends BaseEntity {

    /**
     * 区域ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long regionId;

    /**
     * 区域编码
     */
    @TableField("region_code")
    private String regionCode;

    /**
     * 区域名称
     */
    @TableField("region_name")
    private String regionName;

    /**
     * 父区域ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 区域层级：1-根区域 2-一级区域 3-二级区域 4-三级区域
     */
    @TableField("region_level")
    private Integer regionLevel;

    /**
     * 区域路径（完整路径，用/分隔）
     */
    @TableField("region_path")
    private String regionPath;

    /**
     * 区域类型：1-园区 2-楼栋 3-楼层 4-房间 5-出入口 6-周界 7-停车场 8-公共区域
     */
    @TableField("region_type")
    private Integer regionType;

    /**
     * 区域描述
     */
    @TableField("description")
    private String description;

    /**
     * 区域负责人
     */
    @TableField("manager")
    private String manager;

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
     * 区域面积（平方米）
     */
    @TableField("area_size")
    private Double areaSize;

    /**
     * 区域平面图路径
     */
    @TableField("floor_plan")
    private String floorPlan;

    /**
     * 区域缩略图路径
     */
    @TableField("thumbnail_path")
    private String thumbnailPath;

    /**
     * 设备数量
     */
    @TableField("device_count")
    private Integer deviceCount;

    /**
     * 在线设备数量
     */
    @TableField("online_device_count")
    private Integer onlineDeviceCount;

    /**
     * 摄像头数量
     */
    @TableField("camera_count")
    private Integer cameraCount;

    /**
     * 报警器数量
     */
    @TableField("alarm_count")
    private Integer alarmCount;

    /**
     * 门禁点数量
     */
    @TableField("access_point_count")
    private Integer accessPointCount;

    /**
     * 安全级别：1-低 2-中 3-高 4-极高
     */
    @TableField("security_level")
    private Integer securityLevel;

    /**
     * 访问权限：1-公开 2-内部 3-保密 4-机密
     */
    @TableField("access_permission")
    private Integer accessPermission;

    /**
     * 状态：1-正常 2-维修 3-停用 4-拆除
     */
    @TableField("status")
    private Integer status;

    /**
     * 建设时间
     */
    @TableField("build_time")
    private LocalDateTime buildTime;

    /**
     * 启用时间
     */
    @TableField("enable_time")
    private LocalDateTime enableTime;

    /**
     * 区域配置（JSON格式）
     */
    @TableField("region_config")
    private String regionConfig;

    /**
     * 监控时间配置（JSON格式）
     */
    @TableField("monitor_schedule")
    private String monitorSchedule;

    /**
     * 巡检路线配置（JSON格式）
     */
    @TableField("patrol_route")
    private String patrolRoute;

    /**
     * 应急预案配置（JSON格式）
     */
    @TableField("emergency_plan")
    private String emergencyPlan;

    /**
     * 告警规则配置（JSON格式）
     */
    @TableField("alarm_rules")
    private String alarmRules;

    /**
     * 区域标签（JSON数组）
     */
    @TableField("tags")
    private String tags;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否叶子节点：0-否 1-是
     */
    @TableField("is_leaf")
    private Integer isLeaf;

    /**
     * 子区域数量
     */
    @TableField("child_count")
    private Integer childCount;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建人姓名
     */
    @TableField("created_by_name")
    private String createdByName;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;

    /**
     * 更新人姓名
     */
    @TableField("updated_by_name")
    private String updatedByName;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}
