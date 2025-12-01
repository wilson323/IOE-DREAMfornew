package net.lab1024.sa.report.domain.analytics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 报表生成请求
 *
 * @author IOE-DREAM Team
 */
@Data
public class ReportGenerationRequest {

    /**
     * 报表名称
     */
    @NotBlank(message = "报表名称不能为空")
    private String reportName;

    /**
     * 报表类型
     */
    @NotBlank(message = "报表类型不能为空")
    private String reportType;

    /**
     * 报表格式：excel, pdf, chart
     */
    @NotBlank(message = "报表格式不能为空")
    private String reportFormat;

    /**
     * 数据源配置
     */
    @NotNull(message = "数据源配置不能为空")
    private DataSourceConfig dataSource;

    /**
     * 报表模板ID
     */
    private String templateId;

    /**
     * 查询参数
     */
    private Map<String, Object> queryParams;

    /**
     * 过滤条件
     */
    private List<FilterCondition> filters;

    /**
     * 排序条件
     */
    private List<SortCondition> sorts;

    /**
     * 分页配置
     */
    private PageConfig pageConfig;

    /**
     * 聚合配置
     */
    private List<AggregationConfig> aggregations;

    /**
     * 图表配置
     */
    private ChartConfig chartConfig;

    /**
     * 导出配置
     */
    private ExportConfig exportConfig;

    /**
     * 调度配置
     */
    private ScheduleConfig scheduleConfig;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 是否异步生成
     */
    private Boolean async = false;

    /**
     * 优先级
     */
    private Integer priority = 2;

    /**
     * 过期时间（分钟）
     */
    private Integer expireMinutes = 60;

    /**
     * 通知配置
     */
    private NotificationConfig notification;

    /**
     * 请求时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTime;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendProperties;

    @Data
    public static class DataSourceConfig {
        /**
         * 数据源类型：database, api, file, cache
         */
        private String type;

        /**
         * 数据源名称
         */
        private String name;

        /**
         * 连接配置
         */
        private Map<String, Object> connectionConfig;

        /**
         * 查询配置
         */
        private QueryConfig queryConfig;
    }

    @Data
    public static class QueryConfig {
        /**
         * SQL查询语句
         */
        private String sql;

        /**
         * 表名
         */
        private String tableName;

        /**
         * 字段列表
         */
        private List<String> fields;

        /**
         * 关联表配置
         */
        private List<JoinConfig> joins;

        /**
         * where条件
         */
        private String whereClause;

        /**
         * group by字段
         */
        private List<String> groupBy;

        /**
         * having条件
         */
        private String havingClause;
    }

    @Data
    public static class JoinConfig {
        /**
         * 关联表名
         */
        private String table;

        /**
         * 关联类型：inner, left, right, full
         */
        private String type;

        /**
         * 关联条件
         */
        private String condition;

        /**
         * 表别名
         */
        private String alias;
    }

    @Data
    public static class FilterCondition {
        /**
         * 字段名
         */
        private String field;

        /**
         * 操作符：eq, ne, gt, ge, lt, le, like, in, notIn
         */
        private String operator;

        /**
         * 值
         */
        private Object value;

        /**
         * 逻辑操作符：and, or
         */
        private String logicOperator = "and";

        /**
         * 括号级别
         */
        private Integer bracketLevel = 0;
    }

    @Data
    public static class SortCondition {
        /**
         * 字段名
         */
        private String field;

        /**
         * 排序方向：asc, desc
         */
        private String direction = "asc";

        /**
         * 排序优先级
         */
        private Integer priority = 0;
    }

    @Data
    public static class PageConfig {
        /**
         * 页码（从1开始）
         */
        private Integer pageNum = 1;

        /**
         * 每页大小
         */
        private Integer pageSize = 20;

        /**
         * 是否启用分页
         */
        private Boolean enable = false;
    }

    @Data
    public static class AggregationConfig {
        /**
         * 聚合字段
         */
        private String field;

        /**
         * 聚合类型：sum, avg, max, min, count, countDistinct
         */
        private String type;

        /**
         * 聚合别名
         */
        private String alias;

        /**
         * 分组字段
         */
        private List<String> groupBy;
    }

    @Data
    public static class ChartConfig {
        /**
         * 图表类型：bar, line, pie, area, scatter, radar
         */
        private String chartType;

        /**
         * X轴配置
         */
        private AxisConfig xAxis;

        /**
         * Y轴配置
         */
        private AxisConfig yAxis;

        /**
         * 系列配置
         */
        private List<SeriesConfig> series;

        /**
         * 图表标题
         */
        private String title;

        /**
         * 图例配置
         */
        private LegendConfig legend;

        /**
         * 主题配置
         */
        private String theme = "default";

        /**
         * 尺寸配置
         */
        private SizeConfig size;
    }

    @Data
    public static class AxisConfig {
        /**
         * 轴标题
         */
        private String title;

        /**
         * 数据类型：category, value, time
         */
        private String type = "value";

        /**
         * 最小值
         */
        private Object min;

        /**
         * 最大值
         */
        private Object max;

        /**
         * 是否显示网格线
         */
        private Boolean gridLines = true;
    }

    @Data
    public static class SeriesConfig {
        /**
         * 系列名称
         */
        private String name;

        /**
         * 数据字段
         */
        private String dataField;

        /**
         * 颜色
         */
        private String color;

        /**
         * 样式配置
         */
        private Map<String, Object> style;
    }

    @Data
    public static class LegendConfig {
        /**
         * 是否显示图例
         */
        private Boolean show = true;

        /**
         * 图例位置：top, bottom, left, right
         */
        private String position = "bottom";

        /**
         * 图例方向：horizontal, vertical
         */
        private String orientation = "horizontal";
    }

    @Data
    public static class SizeConfig {
        /**
         * 宽度
         */
        private Integer width = 800;

        /**
         * 高度
         */
        private Integer height = 600;
    }

    @Data
    public static class ExportConfig {
        /**
         * 文件名
         */
        private String fileName;

        /**
         * 文件扩展名
         */
        private String fileExtension;

        /**
         * 是否包含表头
         */
        private Boolean includeHeader = true;

        /**
         * 是否包含索引列
         */
        private Boolean includeIndex = false;

        /**
         * 数据格式化配置
         */
        private Map<String, FormatConfig> formatters;

        /**
         * 自定义样式配置
         */
        private Map<String, Object> styles;
    }

    @Data
    public static class FormatConfig {
        /**
         * 格式化类型：number, date, currency, percentage
         */
        private String type;

        /**
         * 格式化模式
         */
        private String pattern;

        /**
         * 小数位数
         */
        private Integer decimalPlaces;
    }

    @Data
    public static class ScheduleConfig {
        /**
         * 调度类型：once, daily, weekly, monthly, cron
         */
        private String type;

        /**
         * 调度表达式（cron格式）
         */
        private String cronExpression;

        /**
         * 执行时间配置
         */
        private Map<String, Object> scheduleParams;

        /**
         * 是否启用
         */
        private Boolean enabled = true;

        /**
         * 开始时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
    }

    @Data
    public static class NotificationConfig {
        /**
         * 是否启用通知
         */
        private Boolean enabled = false;

        /**
         * 通知渠道：email, webhook, sms
         */
        private List<String> channels;

        /**
         * 通知接收人
         */
        private List<String> recipients;

        /**
         * 通知模板
         */
        private String template;

        /**
         * 通知参数
         */
        private Map<String, Object> parameters;
    }
}