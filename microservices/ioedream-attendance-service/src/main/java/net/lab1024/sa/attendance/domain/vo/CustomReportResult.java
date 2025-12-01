package net.lab1024.sa.attendance.domain.vo;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义报表结果
 * 考勤模块自定义报表的返回结果
 *
 * @author SmartAdmin Team
 * @date 2025-11-24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomReportResult {

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
     * 报表描述
     */
    private String description;

    /**
     * 报表数据列表
     */
    private List<Map<String, Object>> dataList;

    /**
     * 报表数据（与dataList相同，用于兼容）
     */
    private List<Map<String, Object>> data;

    /**
     * 报表列定义
     */
    private List<ReportColumn> columns;

    /**
     * 统计信息
     */
    private ReportStatistics statistics;

    /**
     * 统计信息（与statistics相同，用于兼容）
     */
    private Map<String, Object> summary;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据总数（与totalCount相同，用于兼容）
     */
    private Integer totalRecords;

    /**
     * 生成时间
     */
    private java.time.LocalDateTime generateTime;

    /**
     * 数据总数
     */
    private Integer totalCount;

    /**
     * 报表参数
     */
    private Map<String, Object> parameters;

    /**
     * 报表列定义内部类
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReportColumn {

        /**
         * 字段名
         */
        private String field;

        /**
         * 列标题
         */
        private String title;

        /**
         * 数据类型
         */
        private String dataType;

        /**
         * 列宽度
         */
        private Integer width;

        /**
         * 是否可排序
         */
        private Boolean sortable;

        /**
         * 是否显示
         */
        private Boolean visible;

        /**
         * 格式化表达式
         */
        private String format;
    }

    /**
     * 报表统计信息内部类
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReportStatistics {

        /**
         * 总记录数
         */
        private Integer totalRecords;

        /**
         * 正常出勤次数
         */
        private Integer normalCount;

        /**
         * 异常出勤次数
         */
        private Integer abnormalCount;

        /**
         * 迟到次数
         */
        private Integer lateCount;

        /**
         * 早退次数
         */
        private Integer earlyCount;

        /**
         * 缺勤次数
         */
        private Integer absentCount;

        /**
         * 出勤率
         */
        private Double attendanceRate;

        /**
         * 统计周期开始时间
         */
        private java.time.LocalDateTime startTime;

        /**
         * 统计周期结束时间
         */
        private java.time.LocalDateTime endTime;
    }

    /**
     * 判断是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success != null && success;
    }

    /**
     * 创建失败结果
     *
     * @param message 失败消息
     * @return CustomReportResult
     */
    public static CustomReportResult failure(String message) {
        CustomReportResult result = new CustomReportResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
}
