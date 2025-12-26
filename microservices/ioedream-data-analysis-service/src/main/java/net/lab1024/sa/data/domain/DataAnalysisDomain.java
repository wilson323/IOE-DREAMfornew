package net.lab1024.sa.data.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据分析领域对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public class DataAnalysisDomain {

    /**
     * 数据报表VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportVO {
        /**
         * 报表ID
         */
        private Long reportId;

        /**
         * 报表名称
         */
        private String reportName;

        /**
         * 报表编码
         */
        private String reportCode;

        /**
         * 报表类型
         * - list: 列表报表
         * - summary: 汇总报表
         * - chart: 图表报表
         * - dashboard: 仪表板
         */
        private String reportType;

        /**
         * 业务模块
         * - attendance: 考勤
         * - consume: 消费
         * - access: 门禁
         * - visitor: 访客
         * - video: 视频
         */
        private String businessModule;

        /**
         * 数据源配置
         */
        private DataSourceConfig dataSource;

        /**
         * 查询配置
         */
        private QueryConfig queryConfig;

        /**
         * 报表布局
         */
        private ReportLayout layout;

        /**
         * 权限配置
         */
        private ReportPermission permission;

        /**
         * 创建人ID
         */
        private Long creatorId;

        /**
         * 创建人姓名
         */
        private String creatorName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 更新时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;

        /**
         * 报表状态
         * - draft: 草稿
         * - active: 启用
         * - archived: 归档
         */
        private String status;

        /**
         * 报表描述
         */
        private String description;
    }

    /**
     * 数据源配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSourceConfig {
        /**
         * 数据源类型
         * - database: 数据库
         * - api: API接口
         * - elasticsearch: ES
         * - redis: Redis缓存
         */
        private String sourceType;

        /**
         * 表名或API路径
         */
        private String sourceName;

        /**
         * 字段映射
         */
        private Map<String, FieldMapping> fields;

        /**
         * 数据源连接配置
         */
        private Map<String, Object> connectionConfig;
    }

    /**
     * 字段映射
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldMapping {
        /**
         * 字段名称
         */
        private String fieldName;

        /**
         * 字段类型
         * - string: 字符串
         * - number: 数字
         * - date: 日期
         * - boolean: 布尔
         */
        private String fieldType;

        /**
         * 字段标题
         */
        private String fieldLabel;

        /**
         * 是否可见
         */
        private Boolean visible;

        /**
         * 是否可排序
         */
        private Boolean sortable;

        /**
         * 是否可筛选
         */
        private Boolean filterable;

        /**
         * 数据格式
         */
        private String format;

        /**
         * 聚合方式
         * - none: 不聚合
         * - sum: 求和
         * - avg: 平均
         * - count: 计数
         * - max: 最大值
         * - min: 最小值
         */
        private String aggregation;
    }

    /**
     * 查询配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryConfig {
        /**
         * 查询条件
         */
        private List<QueryCondition> conditions;

        /**
         * 分组字段
         */
        private List<String> groupBy;

        /**
         * 排序配置
         */
        private List<SortConfig> orderBy;

        /**
         * 分页配置
         */
        private PageConfig pageConfig;

        /**
         * 缓存配置
         */
        private CacheConfig cacheConfig;
    }

    /**
     * 查询条件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryCondition {
        /**
         * 字段名
         */
        private String fieldName;

        /**
         * 操作符
         * - eq: 等于
         * - ne: 不等于
         * - gt: 大于
         * - gte: 大于等于
         * - lt: 小于
         * - lte: 小于等于
         * - like: 模糊匹配
         * - in: 包含
         * - between: 区间
         */
        private String operator;

        /**
         * 条件值
         */
        private Object value;

        /**
         * 逻辑关系
         * - and: 并且
         * - or: 或者
         */
        private String logic;
    }

    /**
     * 排序配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortConfig {
        /**
         * 字段名
         */
        private String fieldName;

        /**
         * 排序方向
         * - asc: 升序
         * - desc: 降序
         */
        private String direction;
    }

    /**
     * 分页配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageConfig {
        /**
         * 是否启用分页
         */
        private Boolean enabled;

        /**
         * 默认页码
         */
        private Integer defaultPageNum;

        /**
         * 默认页大小
         */
        private Integer defaultPageSize;

        /**
         * 页大小选项
         */
        private List<Integer> pageSizeOptions;
    }

    /**
     * 缓存配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheConfig {
        /**
         * 是否启用缓存
         */
        private Boolean enabled;

        /**
         * 缓存时间（秒）
         */
        private Integer ttl;

        /**
         * 缓存键前缀
         */
        private String keyPrefix;
    }

    /**
     * 报表布局
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportLayout {
        /**
         * 列配置（列表报表）
         */
        private List<ColumnConfig> columns;

        /**
         * 图表配置（图表报表）
         */
        private ChartConfig chart;

        /**
         * 仪表板配置（仪表板）
         */
        private DashboardConfig dashboard;

        /**
         * 样式配置
         */
        private Map<String, Object> styles;
    }

    /**
     * 列配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnConfig {
        /**
         * 字段名
         */
        private String fieldName;

        /**
         * 列标题
         */
        private String columnTitle;

        /**
         * 列宽度
         */
        private Integer width;

        /**
         * 对齐方式
         * - left: 左对齐
         * - center: 居中
         * - right: 右对齐
         */
        private String align;

        /**
         * 是否固定列
         */
        private Boolean fixed;

        /**
         * 列位置
         */
        private Integer order;
    }

    /**
     * 图表配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartConfig {
        /**
         * 图表类型
         * - line: 折线图
         * - bar: 柱状图
         * - pie: 饼图
         * - area: 面积图
         * - scatter: 散点图
         * - gauge: 仪表盘
         * - funnel: 漏斗图
         */
        private String chartType;

        /**
         * X轴字段
         */
        private String xAxisField;

        /**
         * Y轴字段
         */
        private String yAxisField;

        /**
         * 系列配置
         */
        private List<ChartSeries> series;

        /**
         * 图表选项
         */
        private Map<String, Object> options;
    }

    /**
     * 图表系列
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartSeries {
        /**
         * 系列名称
         */
        private String name;

        /**
         * 字段名
         */
        private String field;

        /**
         * 系列类型
         */
        private String type;

        /**
         * 颜色
         */
        private String color;

        /**
         * Y轴位置
         * - left: 左轴
         * - right: 右轴
         */
        private String yAxis;
    }

    /**
     * 仪表板配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardConfig {
        /**
         * 组件列表
         */
        private List<DashboardComponent> components;

        /**
         * 布局类型
         * - grid: 网格布局
         * - free: 自由布局
         */
        private String layoutType;

        /**
         * 刷新间隔（秒）
         */
        private Integer refreshInterval;
    }

    /**
     * 仪表板组件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardComponent {
        /**
         * 组件ID
         */
        private String componentId;

        /**
         * 组件类型
         * - chart: 图表
         * - metric: 指标卡
         * - table: 数据表格
         * - text: 文本
         */
        private String componentType;

        /**
         * 组件标题
         */
        private String title;

        /**
         * 位置配置
         */
        private PositionConfig position;

        /**
         * 尺寸配置
         */
        private SizeConfig size;

        /**
         * 数据配置
         */
        private Map<String, Object> dataConfig;

        /**
         * 样式配置
         */
        private Map<String, Object> styles;
    }

    /**
     * 位置配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionConfig {
        /**
         * X坐标
         */
        private Integer x;

        /**
         * Y坐标
         */
        private Integer y;
    }

    /**
     * 尺寸配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SizeConfig {
        /**
         * 宽度
         */
        private Integer width;

        /**
         * 高度
         */
        private Integer height;
    }

    /**
     * 报表权限
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportPermission {
        /**
         * 可见角色
         */
        private List<Long> visibleRoles;

        /**
         * 可编辑角色
         */
        private List<Long> editableRoles;

        /**
         * 可导出角色
         */
        private List<Long> exportableRoles;

        /**
         * 数据权限
         */
        private DataPermission dataPermission;
    }

    /**
     * 数据权限
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPermission {
        /**
         * 权限类型
         * - all: 全部数据
         * - department: 本部门
         * - self: 仅本人
         * - custom: 自定义
         */
        private String permissionType;

        /**
         * 权限配置
         */
        private Map<String, Object> config;
    }

    /**
     * 报表查询请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportQueryRequest {
        /**
         * 报表ID
         */
        @NotNull(message = "报表ID不能为空")
        private Long reportId;

        /**
         * 查询参数
         */
        private Map<String, Object> params;

        /**
         * 页码
         */
        private Integer pageNum;

        /**
         * 页大小
         */
        private Integer pageSize;

        /**
         * 排序字段
         */
        private String sortField;

        /**
         * 排序方向
         */
        private String sortDirection;
    }

    /**
     * 报表查询结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportQueryResult {
        /**
         * 报表ID
         */
        private Long reportId;

        /**
         * 报表名称
         */
        private String reportName;

        /**
         * 报表类型
         */
        private String reportType;

        /**
         * 数据列表
         */
        private List<Map<String, Object>> dataList;

        /**
         * 总记录数
         */
        private Long totalRecords;

        /**
         * 汇总数据
         */
        private Map<String, Object> summary;

        /**
         * 图表数据
         */
        private ChartData chartData;

        /**
         * 执行时间（毫秒）
         */
        private Long executionTime;

        /**
         * 查询时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime queryTime;
    }

    /**
     * 图表数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        /**
         * X轴数据
         */
        private List<Object> xAxisData;

        /**
         * 系列数据
         */
        private List<SeriesData> seriesData;

        /**
         * 图表选项
         */
        private Map<String, Object> options;
    }

    /**
     * 系列数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeriesData {
        /**
         * 系列名称
         */
        private String name;

        /**
         * 数据值
         */
        private List<Object> data;

        /**
         * 系列类型
         */
        private String type;
    }

    /**
     * 仪表板VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardVO {
        /**
         * 仪表板ID
         */
        private Long dashboardId;

        /**
         * 仪表板名称
         */
        private String dashboardName;

        /**
         * 仪表板编码
         */
        private String dashboardCode;

        /**
         * 布局配置
         */
        private DashboardConfig layout;

        /**
         * 组件数据
         */
        private Map<String, Object> componentData;

        /**
         * 刷新时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime refreshTime;
    }

    /**
     * 数据导出请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataExportRequest {
        /**
         * 报表ID
         */
        @NotNull(message = "报表ID不能为空")
        private Long reportId;

        /**
         * 导出格式
         * - excel: Excel
         * - pdf: PDF
         * - csv: CSV
         */
        @NotBlank(message = "导出格式不能为空")
        private String exportFormat;

        /**
         * 查询参数
         */
        private Map<String, Object> params;

        /**
         * 文件名
         */
        private String fileName;

        /**
         * 是否包含表头
         */
        private Boolean includeHeader;

        /**
         * 是否包含汇总
         */
        private Boolean includeSummary;
    }

    /**
     * 数据导出结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataExportResult {
        /**
         * 导出任务ID
         */
        private String exportTaskId;

        /**
         * 文件URL
         */
        private String fileUrl;

        /**
         * 文件名
         */
        private String fileName;

        /**
         * 文件大小（字节）
         */
        private Long fileSize;

        /**
         * 导出状态
         * - pending: 待处理
         * - processing: 处理中
         * - completed: 已完成
         * - failed: 失败
         */
        private String status;

        /**
         * 完成时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime completeTime;

        /**
         * 错误信息
         */
        private String errorMessage;
    }

    /**
     * 数据统计VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataStatisticsVO {
        /**
         * 统计项名称
         */
        private String name;

        /**
         * 统计值
         */
        private Object value;

        /**
         * 趋势
         * - up: 上升
         * - down: 下降
         * - flat: 持平
         */
        private String trend;

        /**
         * 变化率
         */
        private Double changeRate;

        /**
         * 对比值
         */
        private Object compareValue;

        /**
         * 统计时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime statisticsTime;
    }

    /**
     * 自定义查询请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomQueryRequest {
        /**
         * 查询名称
         */
        private String queryName;

        /**
         * 数据源
         */
        private String dataSource;

        /**
         * 查询字段
         */
        private List<String> fields;

        /**
         * 查询条件
         */
        private List<QueryCondition> conditions;

        /**
         * 分组字段
         */
        private List<String> groupBy;

        /**
         * 排序配置
         */
        private List<SortConfig> orderBy;

        /**
         * 分页配置
         */
        private PageConfig pageConfig;

        /**
         * 聚合配置
         */
        private Map<String, String> aggregations;
    }
}
