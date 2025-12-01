/*
 * 商品分类实体类
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-19
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.lab1024.sa.base.common.entity.BaseEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 商品分类实体
 *
 * @author SmartAdmin Team
 * @date 2025/01/19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("t_product_category")
public class ProductCategoryEntity extends BaseEntity {

    /**
     * 分类ID
     */
    @TableId
    private Long categoryId;

    /**
     * 分类编码
     */
    @NotBlank(message = "分类编码不能为空")
    @Size(max = 50, message = "分类编码长度不能超过50个字符")
    private String categoryCode;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100个字符")
    private String categoryName;

    /**
     * 上级分类ID
     */
    private Long parentId;

    /**
     * 分类层级 (1-一级, 2-二级, 3-三级)
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 分类图标
     */
    @Size(max = 200, message = "分类图标长度不能超过200个字符")
    private String icon;

    /**
     * 分类描述
     */
    @Size(max = 500, message = "分类描述长度不能超过500个字符")
    private String description;

    /**
     * 状态 (0-禁用, 1-启用)
     */
    private Integer status;

    /**
     * 是否显示 (0-否, 1-是)
     */
    private Integer isShow;
}