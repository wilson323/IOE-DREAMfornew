package net.lab1024.sa.consume.domain.vo;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 消费区域视图对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 用于Controller层返回数据
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "消费区域VO")
public class ConsumeAreaVO {

    /**
     * 区域ID
     */
    @Schema(description = "区域ID")
    private Long id;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称")
    private String areaName;

    /**
     * 区域编码
     */
    @Schema(description = "区域编码")
    private String areaCode;

    /**
     * 父级区域ID
     */
    @Schema(description = "父级区域ID")
    private Long parentId;

    /**
     * 完整路径
     */
    @Schema(description = "完整路径")
    private String fullPath;

    /**
     * 层级
     */
    @Schema(description = "层级")
    private Integer level;

    /**
     * 区域类型
     */
    @Schema(description = "区域类型：1-餐饮，2-超市，3-自助")
    private Integer areaType;

    /**
     * 区域类型名称
     */
    @Schema(description = "区域类型名称")
    private String areaTypeName;

    /**
     * 经营模式
     */
    @Schema(description = "经营模式：1-餐别制，2-超市制，3-混合模式")
    private Integer manageMode;

    /**
     * 经营模式名称
     */
    @Schema(description = "经营模式名称")
    private String manageModeName;

    /**
     * 区域子类型
     */
    @Schema(description = "区域子类型")
    private Integer areaSubType;

    /**
     * 定值配置（JSON格式）
     */
    @Schema(description = "定值配置JSON")
    private String fixedValueConfig;

    /**
     * 餐别分类（JSON数组格式）
     */
    @Schema(description = "允许餐别分类IDs JSON数组")
    private String mealCategories;

    /**
     * 营业开始时间
     */
    @Schema(description = "营业开始时间")
    private LocalTime openTime;

    /**
     * 营业结束时间
     */
    @Schema(description = "营业结束时间")
    private LocalTime closeTime;

    /**
     * 区域地址
     */
    @Schema(description = "区域地址")
    private String address;

    /**
     * 负责人姓名
     */
    @Schema(description = "负责人姓名")
    private String contactName;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String contactPhone;

    /**
     * GPS位置信息
     */
    @Schema(description = "GPS位置信息")
    private String gpsLocation;

    /**
     * 排序号
     */
    @Schema(description = "排序号")
    private Integer sortOrder;

    /**
     * 库存标记
     */
    @Schema(description = "是否启用库存：0否1是")
    private Integer inventoryFlag;

    /**
     * 状态
     */
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    /**
     * 状态名称
     */
    @Schema(description = "状态名称")
    private String statusName;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private java.time.LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private java.time.LocalDateTime updateTime;
}
