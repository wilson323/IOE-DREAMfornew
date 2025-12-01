package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;

/**
 * 菜单实体
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_menu")
public class MenuEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long menuId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单描述
     */
    private String description;

    /**
     * 菜单分类ID
     */
    private Long categoryId;

    /**
     * 菜单价格
     */
    private BigDecimal price;

    /**
     * 菜单图片URL
     */
    private String imageUrl;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 菜单状态：AVAILABLE-可用，UNAVAILABLE-不可用
     */
    private String status;

    /**
     * 是否推荐：0-不推荐，1-推荐
     */
    private Integer isRecommend;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 菜单标签（JSON格式）
     */
    private String tags;

    /**
     * 制作时间（分钟）
     */
    private Integer preparationTime;

    /**
     * 营养信息（JSON格式）
     */
    private String nutritionInfo;
}