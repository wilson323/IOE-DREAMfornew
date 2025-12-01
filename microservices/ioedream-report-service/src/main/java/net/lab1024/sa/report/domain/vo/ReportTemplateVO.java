package net.lab1024.sa.report.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报表模板VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class ReportTemplateVO {

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板类型
     */
    private String templateType;

    /**
     * 数据源配置
     */
    private String dataSourceConfig;

    /**
     * SQL查询语句
     */
    private String sqlQuery;

    /**
     * 参数配置
     */
    private String parameterConfig;

    /**
     * 图表配置
     */
    private String chartConfig;

    /**
     * 样式配置
     */
    private String styleConfig;

    /**
     * 缓存时间
     */
    private Integer cacheTtl;

    /**
     * 是否启用
     */
    private Integer enabled;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}