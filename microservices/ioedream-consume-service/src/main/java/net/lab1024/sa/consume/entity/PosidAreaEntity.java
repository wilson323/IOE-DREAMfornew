package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.lab1024.sa.consume.domain.config.AreaConfig;
import net.lab1024.sa.consume.domain.config.FixedValueConfig;
import net.lab1024.sa.consume.handler.JSONTypeHandler;

import java.time.LocalDateTime;

/**
 * POSID区域表实体
 *
 * 对应表: POSID_AREA
 * 表说明: 区域配置表，包含fixed_value_config和area_config两个JSON字段
 *
 * 核心字段:
 * - area_id: 区域ID（主键）
 * - area_code: 区域编码（唯一）
 * - manage_mode: 管理模式（1-餐别制，2-超市制，3-混合）
 * - fixed_value_config: 定值配置（JSON字段）
 * - area_config: 区域权限配置（JSON字段）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Data
@TableName(value = "POSID_AREA", autoResultMap = true)
public class PosidAreaEntity {

    /**
     * 区域ID
     */
    @TableId(type = IdType.AUTO)
    private Long areaId;

    /**
     * 区域编码（唯一）
     */
    @TableField("area_code")
    private String areaCode;

    /**
     * 区域名称
     */
    @TableField("area_name")
    private String areaName;

    /**
     * 父区域ID
     */
    @TableField("parent_area_id")
    private Long parentAreaId;

    /**
     * 区域层级
     */
    @TableField("area_level")
    private Integer areaLevel;

    /**
     * 管理模式（1-餐别制，2-超市制，3-混合）
     */
    @TableField("manage_mode")
    private Integer manageMode;

    /**
     * 定值配置（JSON字段）
     * 存储各餐别的定值金额配置
     * 使用TypeHandler处理JSON与Java对象转换
     */
    @TableField(value = "fixed_value_config", typeHandler = JSONTypeHandler.class)
    private FixedValueConfig fixedValueConfig;

    /**
     * 区域权限配置（JSON字段）
     * 存储子区域权限、餐别权限、时间段权限等
     * 使用TypeHandler处理JSON与Java对象转换
     */
    @TableField(value = "area_config", typeHandler = JSONTypeHandler.class)
    private AreaConfig areaConfig;

    /**
     * 区域负责人ID
     */
    @TableField("manager_id")
    private Long managerId;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 区域地址
     */
    @TableField("area_address")
    private String areaAddress;

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
     * 营业开始时间
     */
    @TableField("business_start_time")
    private String businessStartTime;

    /**
     * 营业结束时间
     */
    @TableField("business_end_time")
    private String businessEndTime;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Integer enabled;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField("update_user_id")
    private Long updateUserId;

    /**
     * 删除标记
     */
    @TableField("deleted_flag")
    private Integer deletedFlag;
}
