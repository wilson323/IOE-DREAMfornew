package net.lab1024.sa.common.organization.entity;

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
 * 对应数据库表: t_access_area_ext
 * 用于存储区域门禁相关的扩展配置信息
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_area_ext")
public class AreaAccessExtEntity extends BaseEntity {

    /**
     * 扩展ID（主键）
     */
    @TableId(value = "ext_id", type = IdType.AUTO)
    private Long extId;

    /**
     * 区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 门禁等级
     */
    @TableField("access_level")
    private Integer accessLevel;

    /**
     * 是否需要人脸：0-否 1-是
     */
    @TableField("require_face")
    private Integer requireFace;

    /**
     * 是否需要刷卡：0-否 1-是
     */
    @TableField("require_card")
    private Integer requireCard;

    /**
     * 是否需要密码：0-否 1-是
     */
    @TableField("require_password")
    private Integer requirePassword;

    /**
     * 时间规则ID
     */
    @TableField("time_rule_id")
    private Long timeRuleId;

    /**
     * 配置JSON
     */
    @TableField("config_json")
    private String configJson;

    /**
     * 获取扩展配置（兼容方法）
     * <p>
     * 注意：此方法用于兼容AccessVerificationManager中的getExtConfig()调用
     * 实际字段名为configJson，Lombok生成的getter为getConfigJson()
     * </p>
     *
     * @return 扩展配置JSON字符串
     */
    public String getExtConfig() {
        return configJson;
    }

    /**
     * 设置扩展配置（兼容方法）
     *
     * @param extConfig 扩展配置JSON字符串
     */
    public void setExtConfig(String extConfig) {
        this.configJson = extConfig;
    }

    /**
     * 验证方式: edge=设备端验证, backend=后台验证, hybrid=混合验证
     */
    @TableField("verification_mode")
    private String verificationMode;

    /**
     * 门禁模式
     */
    @TableField("access_mode")
    private String accessMode;
}
