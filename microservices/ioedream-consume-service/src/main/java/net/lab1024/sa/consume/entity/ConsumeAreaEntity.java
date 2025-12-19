package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费区域实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_area")
@Schema(description = "消费区域实体")
public class ConsumeAreaEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 区域ID
     */
    @TableField("area_id")
    @Schema(description = "区域ID")
    private Long areaId;
    /**
     * 区域名称
     */
    @TableField("area_name")
    @Schema(description = "区域名称")
    private String areaName;
    /**
     * 区域编码
     */
    @TableField("area_code")
    @Schema(description = "区域编码")
    private String areaCode;
    /**
     * 区域类型
     */
    @TableField("area_type")
    @Schema(description = "区域类型")
    private Integer areaType;
    /**
     * 状态
     */
    @TableField("status")
    @Schema(description = "状态")
    private Integer status;
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    // 缺失字段 - 根据错误日志添加
    /**
     * 区域名称 (兼容字段)
     */
    @TableField("name")
    @Schema(description = "区域名称(兼容)")
    private String name;

    /**
     * 管理模式
     */
    @TableField("manage_mode")
    @Schema(description = "管理模式")
    private Integer manageMode;

    /**
     * 管理模式 (String兼容)
     */
    @TableField("manage_mode_str")
    @Schema(description = "管理模式(String)")
    private String manageModeStr;

    /**
     * 区域描述
     */
    @TableField("description")
    @Schema(description = "区域描述")
    private String description;

    /**
     * 容纳人数
     */
    @TableField("capacity")
    @Schema(description = "容纳人数")
    private Integer capacity;

    /**
     * 营业时间开始
     */
    @TableField("business_hours_start")
    @Schema(description = "营业时间开始")
    private String businessHoursStart;

    /**
     * 营业时间结束
     */
    @TableField("business_hours_end")
    @Schema(description = "营业时间结束")
    private String businessHoursEnd;

    /**
     * 区域状态
     */
    @TableField("area_status")
    @Schema(description = "区域状态")
    private Integer areaStatus;

    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义

    // 缺失的getter/setter方法 - 根据错误日志添加
    public String getName() {
        return this.name != null ? this.name : this.areaName;
    }

    public void setName(String name) {
        this.name = name;
        this.areaName = name;
    }

    public Integer getManageMode() {
        return this.manageMode;
    }

    public void setManageMode(Integer manageMode) {
        this.manageMode = manageMode;
    }

    public String getManageModeStr() {
        return this.manageModeStr;
    }

    public void setManageModeStr(String manageModeStr) {
        this.manageModeStr = manageModeStr;
    }

    // 缺失字段 - 根据错误日志添加
    /**
     * 父级区域ID
     */
    @TableField("parent_id")
    @Schema(description = "父级区域ID")
    private Long parentId;

    /**
     * 完整路径
     */
    @TableField("full_path")
    @Schema(description = "完整路径")
    private String fullPath;

    /**
     * 区域子类型
     */
    @TableField("area_sub_type")
    @Schema(description = "区域子类型")
    private Integer areaSubType;

    /**
     * 扩展属性 (JSON格式)
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性")
    private String extendedAttributes;

    /**
     * GPS位置信息
     * <p>
     * 兼容推荐服务对区域位置的解析需求。
     * 推荐的存储格式：`latitude,longitude`，例如：`31.2304,121.4737`。
     * </p>
     */
    @TableField("gps_location")
    @Schema(description = "GPS位置信息（latitude,longitude）")
    private String gpsLocation;

    /**
     * 层级
     * <p>
     * 区域在层级结构中的层级，1为顶级区域
     * </p>
     */
    @TableField("level")
    @Schema(description = "层级（1为顶级）")
    private Integer level;

    /**
     * 定值配置（JSON格式）
     * <p>
     * 存储餐别制模式下的定值金额配置
     * 示例：{"breakfast": {"amount": 5.00, "unit": "元"}, "lunch": {"amount": 12.00,
     * "unit": "元"}}
     * </p>
     */
    @TableField("fixed_value_config")
    @Schema(description = "定值配置JSON")
    private String fixedValueConfig;

    /**
     * 餐别分类（JSON数组格式）
     * <p>
     * 存储允许的餐别分类ID列表
     * 示例：["1", "2", "3"]
     * </p>
     */
    @TableField("meal_categories")
    @Schema(description = "允许餐别分类IDs JSON数组")
    private String mealCategories;

    /**
     * 营业开始时间
     * <p>
     * TIME类型，格式：HH:mm:ss
     * </p>
     */
    @TableField("open_time")
    @Schema(description = "营业开始时间")
    private java.time.LocalTime openTime;

    /**
     * 营业结束时间
     * <p>
     * TIME类型，格式：HH:mm:ss
     * </p>
     */
    @TableField("close_time")
    @Schema(description = "营业结束时间")
    private java.time.LocalTime closeTime;

    /**
     * 区域地址
     */
    @TableField("address")
    @Schema(description = "区域地址")
    private String address;

    /**
     * 负责人姓名
     */
    @TableField("contact_name")
    @Schema(description = "负责人姓名")
    private String contactName;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    @Schema(description = "联系电话")
    private String contactPhone;

    /**
     * 排序号
     * <p>
     * 用于区域列表排序，数值越小越靠前
     * </p>
     */
    @TableField("sort_order")
    @Schema(description = "排序号")
    private Integer sortOrder;

    /**
     * 库存标记
     * <p>
     * 0-否，1-是。是否启用库存管理
     * </p>
     */
    @TableField("inventory_flag")
    @Schema(description = "是否启用库存：0否1是")
    private Integer inventoryFlag;

    // 缺失的getter/setter方法 - 根据错误日志添加
    public Long getParentId() {
        return this.parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getFullPath() {
        return this.fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public Integer getAreaSubType() {
        return this.areaSubType;
    }

    public void setAreaSubType(Integer areaSubType) {
        this.areaSubType = areaSubType;
    }

    public String getExtendedAttributes() {
        return this.extendedAttributes;
    }

    public void setExtendedAttributes(String extendedAttributes) {
        this.extendedAttributes = extendedAttributes;
    }

    /**
     * 获取GPS定位信息
     *
     * @return GPS定位信息（lng,lat）
     */
    public String getGpsLocation() {
        return this.gpsLocation;
    }

    /**
     * 设置GPS定位信息
     *
     * @param gpsLocation GPS定位信息（lng,lat）
     */
    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }
}
