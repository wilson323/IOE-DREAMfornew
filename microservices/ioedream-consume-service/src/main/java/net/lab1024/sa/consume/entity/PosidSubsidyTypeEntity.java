package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POSID补贴类型表实体
 *
 * 对应表: POSID_SUBSIDY_TYPE
 * 表说明: 补贴类型配置表
 *
 * 核心字段:
 * - subsidy_type_id: 补贴类型ID（主键）
 * - type_code: 类型编码（唯一）
 * - type_name: 类型名称
 * - priority: 优先级（数字越小优先级越高）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Data
@TableName("POSID_SUBSIDY_TYPE")
public class PosidSubsidyTypeEntity {

    /**
     * 补贴类型ID
     */
    @TableId(type = IdType.AUTO)
    private Long subsidyTypeId;

    /**
     * 类型编码（唯一）
     * 示例：MEAL-餐补，TRAFFIC-交通补，COMMUNICATION-通讯补
     */
    @TableField("type_code")
    private String typeCode;

    /**
     * 类型名称
     */
    @TableField("type_name")
    private String typeName;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 优先级（数字越小优先级越高）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 是否可累计
     */
    @TableField("accumulative")
    private Integer accumulative;

    /**
     * 是否可转让
     */
    @TableField("transferable")
    private Integer transferable;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Integer enabled;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField("update_user_id")
    private Long updateUserId;

    /**
     * 删除标记
     */
    @TableField("deleted_flag")
    private Integer deletedFlag;
}
