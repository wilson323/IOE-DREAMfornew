package net.lab1024.sa.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费区域实体类
 * <p>
 * 用于管理消费区域信息，支持多级层级结构
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 * <p>
 * 业务场景：
 * - 区域管理
 * - 区域权限验证
 * - 区域消费统计
 * - 区域经营模式配置
 * </p>
 * <p>
 * 数据库表：POSID_AREA（业务文档中定义的表名）
 * 注意：根据CLAUDE.md规范，表名应使用t_consume_*格式，但业务文档中使用POSID_*格式
 * 实际使用时需要根据数据库表名调整@TableName注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consume_area")
public class ConsumeAreaEntity extends BaseEntity {

    /**
     * 区域ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 区域编号（唯一）
     */
    private String code;

    /**
     * 区域名称
     */
    private String name;

    /**
     * 父区域ID
     */
    private String parentId;

    /**
     * 完整路径（如：/园区/楼栋/楼层/区域）
     */
    private String fullPath;

    /**
     * 层级（1-顶级，2-二级，以此类推）
     */
    private Integer level;

    /**
     * 区域类型
     * <p>
     * 1-餐饮类型
     * 2-商店类型
     * 3-办公类型
     * 4-医疗类型
     * </p>
     */
    private Integer type;

    /**
     * 区域细分类型
     * <p>
     * 餐饮类型：1-食堂 2-餐厅 3-快餐店 4-咖啡厅
     * 商店类型：1-超市 2-便利店 3-专卖店
     * </p>
     */
    private Integer areaSubType;

    /**
     * 经营模式
     * <p>
     * 1-餐别制（食堂、员工餐厅）
     * 2-超市制（超市、便利店）
     * 3-混合模式（综合餐厅、美食广场）
     * </p>
     */
    private Integer manageMode;

    /**
     * 是否启用进销存管理
     */
    private Boolean inventoryFlag;

    /**
     * 营业时间（JSON格式）
     * <p>
     * 示例：{"weekday": "08:00-22:00", "weekend": "09:00-21:00"}
     * </p>
     */
    private String businessHours;

    /**
     * 扩展属性（JSON格式）
     */
    private String extendedAttributes;

    /**
     * GPS位置信息（JSON格式）
     * <p>
     * 示例：{"latitude": 39.9042, "longitude": 116.4074}
     * </p>
     */
    private String gpsLocation;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean available;
}
