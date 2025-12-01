package net.lab1024.sa.report.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报表模板实体类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_report_template")
public class ReportTemplateEntity {

    /**
     * 模板ID
     */
    @TableId(value = "template_id", type = IdType.AUTO)
    private Long templateId;

    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 模板编码
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 模板描述
     */
    @TableField("description")
    private String description;

    /**
     * 模板类型
     * TABLE-表格, CHART-图表, MIX-混合
     */
    @TableField("template_type")
    private String templateType;

    /**
     * 数据源配置
     */
    @TableField("data_source_config")
    private String dataSourceConfig;

    /**
     * SQL查询语句
     */
    @TableField("sql_query")
    private String sqlQuery;

    /**
     * 参数配置(JSON格式)
     */
    @TableField("parameter_config")
    private String parameterConfig;

    /**
     * 图表配置(JSON格式)
     */
    @TableField("chart_config")
    private String chartConfig;

    /**
     * 样式配置(JSON格式)
     */
    @TableField("style_config")
    private String styleConfig;

    /**
     * 缓存配置(秒)
     */
    @TableField("cache_ttl")
    private Integer cacheTtl;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Integer enabled;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建人ID
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private Long createUserId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private Long updateUserId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记 0-未删除 1-已删除
     */
    @TableField("deleted_flag")
    @TableLogic
    private Integer deletedFlag;
}