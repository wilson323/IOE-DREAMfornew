package net.lab1024.sa.common.organization.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 人员区域关联实体类
 * <p>
 * 用于管理人员与区域的关联关系，支持权限控制和时间范围
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 * <p>
 * 业务场景：
 * - 门禁区域权限管理
 * - 人员区域归属管理
 * - 权限有效期管理
 * </p>
 * <p>
 * 数据库表：t_common_area_person（根据DAO中的SQL推断）
 * 注意：根据文档，可能是t_area_user或t_person_area表
 * 实际使用时需要根据数据库表名调整@TableName注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_area_person")
public class AreaPersonEntity extends BaseEntity {

    /**
     * 关联ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列relation_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "relation_id", type = IdType.AUTO)
    private Long id;

    /**
     * 区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 人员ID
     */
    @TableField("person_id")
    private Long personId;

    /**
     * 访问级别
     */
    @TableField("access_level")
    private Integer accessLevel;

    /**
     * 访问时间配置（JSON格式）
     */
    @TableField("access_time_config")
    private String accessTimeConfig;

    /**
     * 访问原因
     */
    @TableField("access_reason")
    private String accessReason;

    /**
     * 授权人ID
     */
    @TableField("grant_user_id")
    private Long grantUserId;

    /**
     * 授权时间
     */
    @TableField("grant_time")
    private LocalDateTime grantTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 有效开始时间
     */
    @TableField("valid_start_time")
    private LocalDateTime validStartTime;

    /**
     * 有效结束时间
     */
    @TableField("valid_end_time")
    private LocalDateTime validEndTime;

    /**
     * 状态（1-有效，0-已失效）
     */
    @TableField("status")
    private Integer status;
}
