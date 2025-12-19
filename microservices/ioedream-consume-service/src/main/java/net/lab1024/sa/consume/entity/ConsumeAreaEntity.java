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
 * 消费区域实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_area")
@Schema(description = "消费区域实体")
public class ConsumeAreaEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 区域ID
     */
    @TableField("area_id")
    @Schema(description = "区域ID")
    private Long areaId;
    /**
     * 区域名称
     */
    @TableField("area_name")
    @Schema(description = "区域名称")
    private String areaName;
    /**
     * 区域编码
     */
    @TableField("area_code")
    @Schema(description = "区域编码")
    private String areaCode;
    /**
     * 区域类型
     */
    @TableField("area_type")
    @Schema(description = "区域类型")
    private Integer areaType;
    /**
     * 状态
     */
    @TableField("status")
    @Schema(description = "状态")
    private Integer status;
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义
}
