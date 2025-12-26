package net.lab1024.sa.common.monitor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 告警规则实体
 * <p>
 * 对应数据库表: t_alert_rule
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_alert_rule")
public class AlertRuleEntity extends BaseEntity {

    @TableId(value = "rule_id", type = IdType.AUTO)
    private Long ruleId;

    @TableField("rule_name")
    private String ruleName;

    @TableField("rule_type")
    private String ruleType;

    @TableField("rule_config")
    private String ruleConfig; // JSON格式，存储详细配置

    @TableField("alert_level")
    private Integer alertLevel;

    @TableField("notify_channels")
    private String notifyChannels;

    @TableField("status")
    private Integer status;

    @TableField("remark")
    private String remark;

    @TableField("last_check_time")
    private LocalDateTime lastCheckTime;

    // 兼容BaseEntity的id字段
    public Long getId() {
        return ruleId;
    }

    public void setId(Long id) {
        this.ruleId = id;
    }
}

