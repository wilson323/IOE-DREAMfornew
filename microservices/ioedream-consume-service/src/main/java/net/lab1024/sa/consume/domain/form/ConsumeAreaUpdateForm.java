package net.lab1024.sa.consume.domain.form;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 消费区域更新表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeAreaUpdateForm {

    /**
     * 区域ID
     */
    @NotNull(message = "区域ID不能为空")
    private Long id;

    /**
     * 区域名称
     */
    @NotBlank(message = "区域名称不能为空")
    private String areaName;

    /**
     * 区域编码
     */
    @NotBlank(message = "区域编码不能为空")
    private String areaCode;

    /**
     * 父级区域ID
     */
    private Long parentId;

    /**
     * 区域类型
     */
    @NotNull(message = "区域类型不能为空")
    private Integer areaType;

    /**
     * 经营模式
     */
    @NotNull(message = "经营模式不能为空")
    private Integer manageMode;

    /**
     * 区域子类型
     */
    private Integer areaSubType;

    /**
     * 定值配置（JSON格式）
     */
    private String fixedValueConfig;

    /**
     * 餐别分类（JSON数组格式）
     */
    private String mealCategories;

    /**
     * 营业开始时间
     */
    private LocalTime openTime;

    /**
     * 营业结束时间
     */
    private LocalTime closeTime;

    /**
     * 区域地址
     */
    private String address;

    /**
     * 负责人姓名
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * GPS位置信息
     */
    private String gpsLocation;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 库存标记
     */
    private Integer inventoryFlag;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
