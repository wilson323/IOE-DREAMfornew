package net.lab1024.sa.admin.module.attendance.domain.vo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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
     * 报表列定义
     */
    private List<ReportColumn> columns;

    /**
     * 统计信息
     */
    private ReportStatistics statistics;

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
     * 创建失败结果
     *
     * @param errorMessage 错误信息
     * @return 失败结果
     */
    public static CustomReportResult failure(String errorMessage) {
        return CustomReportResult.builder()
                .reportName("生成失败")
                .reportType("ERROR")
                .description(errorMessage)
                .generateTime(java.time.LocalDateTime.now())
                .totalCount(0)
                .build();
    }

    // ==================== 兼容性setter方法 ====================

    /**
     * 设置报表数据
     * 兼容性方法
     *
     * @param data 报表数据
     */
    public void setData(List<Map<String, Object>> data) {
        this.dataList = data;
    }

    /**
     * 设置报表列定义
     * 兼容性方法
     *
     * @param columns 报表列定义
     */
    public void setColumns(List<ReportColumn> columns) {
        this.columns = columns;
    }

    /**
     * 设置报表汇总信息
     * 兼容性方法
     *
     * @param summary 汇总信息
     */
    public void setSummary(Map<String, Object> summary) {
        if (summary != null) {
            // 将汇总信息转换为统计信息
            if (this.statistics == null) {
                this.statistics = new ReportStatistics();
            }

            if (summary.containsKey("totalRecords")) {
                this.statistics.setTotalRecords((Integer) summary.get("totalRecords"));
            }
            if (summary.containsKey("normalCount")) {
                this.statistics.setNormalCount((Integer) summary.get("normalCount"));
            }
            if (summary.containsKey("abnormalCount")) {
                this.statistics.setAbnormalCount((Integer) summary.get("abnormalCount"));
            }
            if (summary.containsKey("lateCount")) {
                this.statistics.setLateCount((Integer) summary.get("lateCount"));
            }
            if (summary.containsKey("earlyCount")) {
                this.statistics.setEarlyCount((Integer) summary.get("earlyCount"));
            }
            if (summary.containsKey("absentCount")) {
                this.statistics.setAbsentCount((Integer) summary.get("absentCount"));
            }
            if (summary.containsKey("attendanceRate")) {
                this.statistics.setAttendanceRate(((Number) summary.get("attendanceRate")).doubleValue());
            }
        }
    }
}