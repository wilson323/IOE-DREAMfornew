package net.lab1024.sa.base.common.device.domain.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 透视表结果类
 * 用于封装数据透视表的处理结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PivotTableResult {

    /**
     * 透视表是否成功生成
     */
    @Builder.Default
    private boolean success = false;

    /**
     * 错误信息（失败时使用）
     */
    private String errorMessage;

    /**
     * 行标题列表
     */
    @Builder.Default
    private List<String> rowHeaders = List.of();

    /**
     * 列标题列表
     */
    @Builder.Default
    private List<String> columnHeaders = List.of();

    /**
     * 透视表数据矩阵
     * 格式：Map<行索引, Map<列索引, 数值>>
     */
    @Builder.Default
    private Map<Integer, Map<Integer, Object>> dataMatrix = Map.of();

    /**
     * 数据内容（兼容原内部类）
     */
    private Map<String, Map<String, Object>> data;

    /**
     * 行头（兼容原内部类）
     */
    private List<String> rows;

    /**
     * 列头（兼容原内部类）
     */
    private List<String> columns;

    /**
     * 总计行数据
     */
    @Builder.Default
    private Map<Integer, Object> totalRow = Map.of();

    /**
     * 总计列数据
     */
    @Builder.Default
    private Map<Integer, Object> totalColumn = Map.of();

    /**
     * 总计值（右下角）
     */
    private Object grandTotal;

    /**
     * 数据行数
     */
    @Builder.Default
    private long totalRows = 0L;

    /**
     * 数据列数
     */
    @Builder.Default
    private long totalColumns = 0L;

    /**
     * 生成耗时（毫秒）
     */
    @Builder.Default
    private long processingTime = 0L;

    /**
     * 创建失败结果
     *
     * @param errorMessage 错误信息
     * @return 失败结果
     */
    public static PivotTableResult failure(String errorMessage) {
        return PivotTableResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 创建成功结果
     *
     * @param rowHeaders 行标题
     * @param columnHeaders 列标题
     * @param dataMatrix 数据矩阵
     * @return 成功结果
     */
    public static PivotTableResult success(List<String> rowHeaders, List<String> columnHeaders,
                                         Map<Integer, Map<Integer, Object>> dataMatrix) {
        return PivotTableResult.builder()
                .success(true)
                .rowHeaders(rowHeaders)
                .columnHeaders(columnHeaders)
                .dataMatrix(dataMatrix)
                .totalRows(rowHeaders.size())
                .totalColumns(columnHeaders.size())
                .build();
    }

    /**
     * 获取指定位置的值
     *
     * @param rowIndex 行索引
     * @param columnIndex 列索引
     * @return 单元格值
     */
    public Object getValue(int rowIndex, int columnIndex) {
        if (dataMatrix != null && dataMatrix.containsKey(rowIndex)) {
            return dataMatrix.get(rowIndex).get(columnIndex);
        }
        return null;
    }

    /**
     * 设置指定位置的值
     *
     * @param rowIndex 行索引
     * @param columnIndex 列索引
     * @param value 单元格值
     */
    public void setValue(int rowIndex, int columnIndex, Object value) {
        if (dataMatrix != null) {
            dataMatrix.computeIfAbsent(rowIndex, k -> Map.of()).put(columnIndex, value);
        }
    }

    /**
     * 获取消息（兼容方法）
     * @return 消息内容
     */
    public String getMessage() {
        return errorMessage;
    }

    /**
     * 设置消息（兼容方法）
     * @param message 消息内容
     */
    public void setMessage(String message) {
        this.errorMessage = message;
    }
}