package net.lab1024.sa.common.export.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 导出结果封装类
 * <p>
 * 封装脱敏后的数据和元信息，供Controller层使用
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResult<T> {

    /**
     * 脱敏后的数据列表
     */
    private List<T> maskedData;

    /**
     * 模型类
     */
    private Class<?> modelClass;

    /**
     * 原始数据数量
     */
    private int originalCount;

    /**
     * 脱敏后数据数量
     */
    private int maskedCount;

    /**
     * 脱敏成功数量
     */
    private int successCount;

    /**
     * 脱敏跳过数量（失败时保留原数据）
     */
    private int skippedCount;

    /**
     * 导出时间戳
     */
    private LocalDateTime exportTime;

    /**
     * 建议的文件名（不含扩展名）
     */
    private String suggestedFileName;

    /**
     * 建议的工作表名称
     */
    private String suggestedSheetName;

    /**
     * 是否全部脱敏成功
     */
    private boolean allSuccess;

    /**
     * 创建成功的导出结果
     *
     * @param maskedData     脱敏后的数据
     * @param modelClass      模型类
     * @param originalCount   原始数量
     * @param successCount    成功数量
     * @param skippedCount    跳过数量
     * @param <T>             数据类型
     * @return 导出结果
     */
    public static <T> ExportResult<T> success(
            List<T> maskedData,
            Class<?> modelClass,
            int originalCount,
            int successCount,
            int skippedCount) {

        String modelName = modelClass.getSimpleName();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        return ExportResult.<T>builder()
                .maskedData(maskedData)
                .modelClass(modelClass)
                .originalCount(originalCount)
                .maskedCount(maskedData != null ? maskedData.size() : 0)
                .successCount(successCount)
                .skippedCount(skippedCount)
                .exportTime(LocalDateTime.now())
                .suggestedFileName(String.format("%s_%s", modelName, timestamp))
                .suggestedSheetName(modelName.replace("Entity", "").replace("VO", "").replace("DTO", ""))
                .allSuccess(skippedCount == 0)
                .build();
    }

    /**
     * 创建失败的结果
     *
     * @param modelClass 模型类
     * @param error      错误信息
     * @param <T>        数据类型
     * @return 导出结果
     */
    public static <T> ExportResult<T> failure(Class<?> modelClass, String error) {
        return ExportResult.<T>builder()
                .modelClass(modelClass)
                .originalCount(0)
                .maskedCount(0)
                .successCount(0)
                .skippedCount(0)
                .exportTime(LocalDateTime.now())
                .suggestedFileName("export_failed")
                .suggestedSheetName("Sheet1")
                .allSuccess(false)
                .build();
    }

    /**
     * 获取导出统计信息
     *
     * @return 统计信息字符串
     */
    public String getStatistics() {
        return String.format("总数: %d, 成功: %d, 跳过: %d, 完全成功: %s",
                originalCount, successCount, skippedCount, allSuccess);
    }

    /**
     * 是否有数据
     *
     * @return true如果有数据
     */
    public boolean hasData() {
        return maskedData != null && !maskedData.isEmpty();
    }

    /**
     * 获取完整文件名（带扩展名）
     *
     * @param fileExtension 文件扩展名（如 ".xlsx", ".csv"）
     * @return 完整文件名
     */
    public String getFullFileName(String fileExtension) {
        return suggestedFileName + fileExtension;
    }
}
