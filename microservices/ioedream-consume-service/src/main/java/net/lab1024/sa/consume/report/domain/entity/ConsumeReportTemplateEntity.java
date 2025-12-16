package net.lab1024.sa.consume.report.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费报表模板实体类
 * <p>
 * 用于管理报表模板配置，支持自定义报表
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 * <p>
 * 业务场景：
 * - 报表模板管理
 * - 自定义报表配置
 * - 报表参数配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_report_template")
public class ConsumeReportTemplateEntity extends BaseEntity {

    /**
     * 模板ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板类型
     * <p>
     * DAILY-日报
     * MONTHLY-月报
     * CUSTOM-自定义
     * </p>
     */
    private String templateType;

    /**
     * 报表配置（JSON格式）
     * <p>
     * 包含报表字段、统计维度、图表配置等
     * </p>
     */
    private String reportConfig;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}



