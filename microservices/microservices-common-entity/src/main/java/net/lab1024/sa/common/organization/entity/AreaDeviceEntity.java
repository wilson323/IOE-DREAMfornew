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
 * 区域设备关联实体
 * <p>
 * 对应数据库表: t_area_device_relation
 * 用于管理设备在区域中的部署和业务属性
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_area_device_relation")
public class AreaDeviceEntity extends BaseEntity {

    @TableId(value = "relation_id", type = IdType.ASSIGN_ID)
    private String relationId;

    @TableField("area_id")
    private Long areaId;

    @TableField("device_id")
    private String deviceId;

    @TableField("device_code")
    private String deviceCode;

    @TableField("device_name")
    private String deviceName;

    @TableField("device_type")
    private Integer deviceType; // 设备类型 (1-门禁 2-考勤 3-消费 4-视频 5-访客 6-报警 7-显示 8-网络)

    @TableField("device_sub_type")
    private Integer deviceSubType; // 设备子类型

    @TableField("business_module")
    private String businessModule; // 业务模块 (access/attendance/consume/visitor/video/oa/device_comm)

    @TableField("location_desc")
    private String locationDesc; // 位置描述

    @TableField("install_location")
    private String installLocation; // 安装位置(JSON格式)

    @TableField("business_attributes")
    private String businessAttributes; // 业务属性(JSON格式)

    @TableField("relation_status")
    private Integer relationStatus; // 关联状态 (1-正常 2-维护 3-故障 4-离线 5-停用)

    @TableField("priority")
    private Integer priority; // 优先级 (1-主设备 2-辅助设备 3-备用设备 9-测试设备)

    @TableField("enabled")
    private Integer enabled; // 是否启用 (0-否 1-是)

    @TableField("effective_time")
    private LocalDateTime effectiveTime; // 生效时间

    @TableField("expire_time")
    private LocalDateTime expireTime; // 失效时间

    @TableField("remark")
    private String remark; // 备注

    /**
     * 获取主键ID（兼容代码中的 getId() 调用）
     *
     * @return 关联ID
     */
    public String getId() {
        return relationId;
    }

    /**
     * 设置主键ID（兼容代码中的 setId() 调用）
     *
     * @param id 关联ID
     */
    public void setId(String id) {
        this.relationId = id;
    }

    /**
     * 获取最后用户同步时间（兼容方法）
     * <p>
     * 注意：此方法需要从DeviceEntity关联获取，这里返回null
     * 实际实现中应该通过deviceId关联DeviceEntity获取lastSyncTime
     * </p>
     *
     * @return 最后用户同步时间
     */
    public LocalDateTime getLastUserSyncTime() {
        // 实际实现中应该通过deviceId关联DeviceEntity获取lastSyncTime
        // 这里返回null，由Service层处理关联查询
        return null;
    }
}
