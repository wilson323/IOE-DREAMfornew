package net.lab1024.sa.admin.module.consume.domain.dto;

import lombok.Data;
import net.lab1024.sa.admin.module.consume.domain.entity.ProductEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 消费验证结果DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
public class ConsumeValidationResult {

    /**
     * 是否验证通过
     */
    private boolean valid;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 商品信息
     */
    private ProductEntity productInfo;

    /**
     * 有效商品列表（订餐模式使用）
     */
    private List<ProductEntity> validItems;

    /**
     * 计算出的金额
     */
    private BigDecimal calculatedAmount;

    /**
     * 单价（计量模式使用）
     */
    private BigDecimal unitPrice;

    /**
     * 推荐项目（智能模式使用）
     */
    private List<Map<String, Object>> recommendedItems;

    /**
     * 建议（智能模式使用）
     */
    private List<Map<String, Object>> suggestions;

    /**
     * 模式匹配得分（智能模式使用）
     */
    private Double patternMatchScore;
}