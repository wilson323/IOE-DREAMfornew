package net.lab1024.sa.attendance.domain.vo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 数据透视表结果
 * 考勤模块数据透视表分析的返回结果
 *
 * @author SmartAdmin Team
 * @date 2025-11-24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PivotTableResult {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 透视表数据
     */
    private List<List<Object>> data;

    /**
     * 行标题
     */
    private List<String> rowHeaders;

    /**
     * 列标题
     */
    private List<String> columnHeaders;

    /**
     * 数据汇总
     */
    private Map<String, Object> summary;

    /**
     * 生成时间
     */
    private java.time.LocalDateTime generateTime;

    /**
     * 数据行数
     */
    private Integer rowCount;

    /**
     * 数据列数
     */
    private Integer columnCount;

    /**
     * 创建失败结果
     *
     * @param message 失败消息
     * @return PivotTableResult
     */
    public static PivotTableResult failure(String message) {
        PivotTableResult result = new PivotTableResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
}
