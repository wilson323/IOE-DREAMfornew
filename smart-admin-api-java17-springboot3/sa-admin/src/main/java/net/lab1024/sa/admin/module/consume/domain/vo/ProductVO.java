package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品VO
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 提供完整的展示字段
 * - 包含格式化后的显示信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Data
@Accessors(chain = true)
public class ProductVO {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品编号
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品条码
     */
    private String barcode;

    /**
     * 商品简称
     */
    private String shortName;

    /**
     * 商品英文名称
     */
    private String englishName;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品详情
     */
    private String detailInfo;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 商品分类名称
     */
    private String categoryName;

    /**
     * 商品分类路径
     */
    private String categoryPath;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 商品规格
     */
    private String specification;

    /**
     * 商品型号
     */
    private String model;

    /**
     * 商品单位
     */
    private String unit;

    /**
     * 标准售价
     */
    private BigDecimal standardPrice;

    /**
     * 销售价格
     */
    private BigDecimal salePrice;

    /**
     * 会员价格
     */
    private BigDecimal memberPrice;

    /**
     * 进货价格
     */
    private BigDecimal purchasePrice;

    /**
     * 成本价格
     */
    private BigDecimal costPrice;

    /**
     * 批发价格
     */
    private BigDecimal wholesalePrice;

    /**
     * 折扣率
     */
    private BigDecimal discountRate;

    /**
     * 最低售价
     */
    private BigDecimal minimumPrice;

    /**
     * 最高售价
     */
    private BigDecimal maximumPrice;

    /**
     * 库存数量
     */
    private Integer stockQuantity;

    /**
     * 安全库存
     */
    private Integer safetyStock;

    /**
     * 最小库存
     */
    private Integer minStock;

    /**
     * 最大库存
     */
    private Integer maxStock;

    /**
     * 库存状态代码
     */
    private String stockStatusCode;

    /**
     * 库存状态名称
     */
    private String stockStatusName;

    /**
     * 预警库存
     */
    private Integer alertStock;

    /**
     * 可用库存
     */
    private Integer availableStock;

    /**
     * 冻结库存
     */
    private Integer lockedStock;

    /**
     * 销售数量
     */
    private Integer salesQuantity;

    /**
     * 今日销量
     */
    private Integer todaySales;

    /**
     * 本周销量
     */
    private Integer weeklySales;

    /**
     * 本月销量
     */
    private Integer monthlySales;

    /**
     * 累计销量
     */
    private Integer totalSales;

    /**
     * 销售金额
     */
    private BigDecimal salesAmount;

    /**
     * 商品图片URL
     */
    private String imageUrl;

    /**
     * 商品图片列表
     */
    private List<String> imageUrls;

    /**
     * 主图片URL
     */
    private String mainImageUrl;

    /**
     * 商品状态代码
     */
    private String statusCode;

    /**
     * 商品状态名称
     */
    private String statusName;

    /**
     * 上架状态
     */
    private Boolean isOnShelf;

    /**
     * 推荐状态
     */
    private Boolean isRecommended;

    /**
     * 热销状态
     */
    private Boolean isHotSale;

    /**
     * 新品状态
     */
    private Boolean isNewProduct;

    /**
     * 限时特惠
     */
    private Boolean isSpecialOffer;

    /**
     * 积分商品
     */
    private Boolean isPointsProduct;

    /**
     * 预售商品
     */
    private Boolean isPreSale;

    /**
     * 虚拟商品
     */
    private Boolean isVirtualProduct;

    /**
     * 商品重量
     */
    private BigDecimal weight;

    /**
     * 商品体积
     */
    private BigDecimal volume;

    /**
     * 商品尺寸
     */
    private String dimensions;

    /**
     * 商品颜色
     */
    private String color;

    /**
     * 商品材质
     */
    private String material;

    /**
     * 商品产地
     */
    private String origin;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 商品标签
     */
    private List<String> tags;

    /**
     * 商品属性
     */
    private String attributes;

    /**
     * 商品关键词
     */
    private String keywords;

    /**
     * 保质期天数
     */
    private Integer shelfLife;

    /**
     * 生产日期
     */
    private LocalDateTime productionDate;

    /**
     * 有效期至
     */
    private LocalDateTime expiryDate;

    /**
     * 质量等级
     */
    private String qualityGrade;

    /**
     * 认证信息
     */
    private String certificationInfo;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    private String updateUserName;

    /**
     * 删除标识（0-未删除，1-已删除）
     */
    private Integer deletedFlag;
}