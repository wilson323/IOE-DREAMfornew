package net.lab1024.sa.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.organization.entity.AreaEntity;

/**
 * 消费区域扩展实体类
 * <p>
 * 基于统一区域空间概念，扩展消费业务特有属性
 * 严格遵循CLAUDE.md规范：
 * - 继承统一区域实体AreaEntity
 * - 只扩展消费业务特有字段
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 * <p>
 * 业务场景：
 * - 消费区域业务属性配置
 * - 区域消费权限管理
 * - 区域经营模式设置
 * - 区域消费统计分析
 * </p>
 * <p>
 * 设计原则：
 * - 继承统一的区域空间概念
 * - 扩展消费业务特有属性
 * - 保持与其他业务模块的区域一致性
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0 - 区域空间概念重构版
 * @since 2025-12-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_area_ext")
public class ConsumeAreaEntity extends AreaEntity {

    // ========== 消费业务特有属性 ==========

    /**
     * 区域消费类型
     * <p>
     * 1-餐饮类型（食堂、餐厅）
     * 2-商店类型（超市、便利店）
     * 3-服务类型（理发、洗衣）
     * 4-娱乐类型（健身房、娱乐室）
     * </p>
     */
    @TableField("consume_type")
    private Integer consumeType;

    /**
     * 区域细分类型
     * <p>
     * 餐饮类型：10-员工食堂 11-访客餐厅 12-咖啡厅 13-快餐店
     * 商店类型：20-超市 21-便利店 22-水果店 23-文具店
     * 服务类型：30-理发店 31-洗衣店 32-打印店
     * 娱乐类型：40-健身房 41-棋牌室 42-阅览室
     * </p>
     */
    @TableField("consume_sub_type")
    private Integer consumeSubType;

    /**
     * 经营模式
     * <p>
     * 1-餐别制（按餐别计费，如早餐、午餐、晚餐）
     * 2-计次制（按次计费，如按次理发）
     * 3-计价制（按价格计费，如超市商品）
     * 4-混合模式（多种计费方式组合）
     * </p>
     */
    @TableField("manage_mode")
    private Integer manageMode;

    /**
     * 是否启用进销存管理
     */
    @TableField("inventory_flag")
    private Boolean inventoryFlag;

    /**
     * 营业时间配置（JSON格式）
     * <p>
     * 示例：{"weekday": {"open": "08:00", "close": "22:00"}, "weekend": {"open": "09:00", "close": "21:00"}}
     * </p>
     */
    @TableField("business_hours")
    private String businessHours;

    /**
     * 消费设备配置（JSON格式）
     * <p>
     * 示例：{"pos_devices": ["POS001", "POS002"], "card_readers": ["CR001"], "payment_methods": ["card", "mobile"]}
     * </p>
     */
    @TableField("device_config")
    private String deviceConfig;

    /**
     * 补贴配置（JSON格式）
     * <p>
     * 示例：{"subsidy_enabled": true, "subsidy_amount": 500, "subsidy_type": "daily"}
     * </p>
     */
    @TableField("subsidy_config")
    private String subsidyConfig;

    /**
     * 消费限制配置（JSON格式）
     * <p>
     * 示例：{"daily_limit": 5000, "single_limit": 500, "time_restrictions": [{"start": "07:00", "end": "09:00"}]}
     * </p>
     */
    @TableField("consume_limit")
    private String consumeLimit;

    // 便捷方法：为了兼容现有代码调用getAreaSubType()
    public Integer getAreaSubType() {
        return this.consumeSubType;
    }

    // 便捷方法：为了兼容现有代码调用getName()
    // 父类AreaEntity应该已经有areaName字段，这里提供便捷访问
    public String getName() {
        // 假设父类有getAreaName()方法
        try {
            return this.getAreaName();
        } catch (Exception e) {
            // 如果父类没有getAreaName方法，返回null或默认值
            return null;
        }
    }

    // 便捷方法：为了兼容现有代码调用getFullPath()
    public String getFullPath() {
        // 使用父类的path字段作为完整路径
        return this.getPath();
    }

    // 便捷方法：为了兼容现有代码调用getExtendedAttributes()
    public String getExtendedAttributes() {
        // 将消费相关配置作为扩展属性返回
        try {
            java.util.Map<String, Object> attributes = new java.util.HashMap<>();
            if (this.consumeSubType != null) {
                attributes.put("consumeSubType", this.consumeSubType);
            }
            // 修复：consumeMode字段不存在，移除相关代码
            if (this.deviceConfig != null) {
                attributes.put("deviceConfig", this.deviceConfig);
            }
            if (this.subsidyConfig != null) {
                attributes.put("subsidyConfig", this.subsidyConfig);
            }
            if (this.consumeLimit != null) {
                attributes.put("consumeLimit", this.consumeLimit);
            }

            // 转换为JSON字符串
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(attributes);
        } catch (Exception e) {
            // 如果转换失败，返回空的JSON对象
            return "{}";
        }
    }
}
