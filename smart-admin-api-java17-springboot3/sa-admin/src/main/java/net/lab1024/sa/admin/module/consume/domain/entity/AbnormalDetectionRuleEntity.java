package net.lab1024.sa.admin.module.consume.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 异常检测规则实体
 *
 * @author SmartAdmin Team
 * @since 2025-11-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_abnormal_detection_rule")
public class AbnormalDetectionRuleEntity extends BaseEntity {

    /**
     * 规则ID
     */
    @TableId(type = IdType.AUTO)
    private Long ruleId;

    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    /**
     * 规则类型
     */
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 规则状态
     */
    @NotNull(message = "规则状态不能为空")
    private Integer status;

    /**
     * 规则配置
     */
    private String config;

    /**
     * 阈值参数
     */
    private String threshold;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}