package net.lab1024.sa.access.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 区域门禁扩展实体类
 * <p>
 * 用于管理区域的门禁扩展信息，包括门禁级别、模式、验证方式等
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 * <p>
 * 业务场景：
 * - 门禁区域扩展配置
 * - 门禁级别管理
 * - 验证方式配置
 * </p>
 * <p>
 * 数据库表：t_access_area_ext（根据DAO中的SQL推断）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_area_ext")
@SuppressWarnings("PMD.ShortVariable")
public class AreaAccessExtEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 扩展ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列ext_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "ext_id", type = IdType.AUTO)
    private Long id;

    /**
     * 区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 门禁级别
     */
    @TableField("access_level")
    private Integer accessLevel;

    /**
     * 门禁模式
     */
    @TableField("access_mode")
    private String accessMode;

    /**
     * 验证方式
     */
    @TableField("verification_mode")
    private String verificationMode;

    /**
     * 设备数量
     */
    @TableField("device_count")
    private Integer deviceCount;

    /**
     * 扩展配置（JSON格式）
     */
    @TableField("ext_config")
    private String extConfig;
}


