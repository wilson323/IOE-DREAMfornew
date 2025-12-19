package net.lab1024.sa.consume.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费产品实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_product")
@Schema(description = "消费产品实体")
public class ConsumeProductEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 产品ID
     */
    @TableField("product_id")
    @Schema(description = "产品ID")
    private Long productId;
    /**
     * 产品编码
     */
    @TableField("product_code")
    @Schema(description = "产品编码")
    private String productCode;
    /**
     * 产品名称
     */
    @TableField("product_name")
    @Schema(description = "产品名称")
    private String productName;
    /**
     * 产品分类
     */
    @TableField("category")
    @Schema(description = "产品分类")
    private String category;
    /**
     * 价格
     */
    @TableField("price")
    @Schema(description = "价格")
    private java.math.BigDecimal price;
    /**
     * 库存
     */
    @TableField("stock")
    @Schema(description = "库存")
    private Integer stock;
    /**
     * 状态
     */
    @TableField("status")
    @Schema(description = "状态")
    private Integer status;
    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义
}
