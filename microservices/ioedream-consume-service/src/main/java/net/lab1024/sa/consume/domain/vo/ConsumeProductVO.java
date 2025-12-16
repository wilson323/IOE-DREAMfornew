package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 商品列表VO（管理端）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Data
public class ConsumeProductVO {

    private String productId;

    private String productCode;

    private String productName;

    private BigDecimal price;

    private Integer stock;

    private String categoryName;

    /**
     * 1-上架；0-下架（前端页面约定）
     */
    private Integer status;

    private String imageUrl;
}




