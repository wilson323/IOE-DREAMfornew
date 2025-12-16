package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费补贴发放记录实体类
 * <p>
 * 用于记录补贴发放历史，支持发放审计和统计
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
@TableName("consume_subsidy_issue_record")
public class ConsumeSubsidyIssueRecordEntity extends BaseEntity {

    /**
     * 发放记录ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 补贴账户ID
     */
    private String subsidyAccountId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 发放金额
     */
    private BigDecimal issueAmount;

    /**
     * 发放时间
     */
    private LocalDateTime issueTime;

    /**
     * 发放状态
     * <p>
     * 1-待发放
     * 2-已发放
     * 3-发放失败
     * </p>
     */
    private Integer issueStatus;

    /**
     * 发放方式
     * <p>
     * AUTO-自动发放
     * MANUAL-手动发放
     * </p>
     */
    private String issueMethod;

    /**
     * 发放原因
     */
    private String issueReason;

    /**
     * 备注
     */
    private String remark;
}



