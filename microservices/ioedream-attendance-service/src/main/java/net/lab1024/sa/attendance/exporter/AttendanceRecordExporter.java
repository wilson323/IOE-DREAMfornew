package net.lab1024.sa.attendance.exporter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.common.entity.attendance.AttendanceRecordEntity;
import net.lab1024.sa.attendance.exporter.template.ExportTemplateManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 考勤记录导出器
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * 负责考勤记录的多格式导出功能
 * </p>
 * <p>
 * 核心职责：
 * - 支持Excel、CSV、PDF三种导出格式
 * - 支持同步/异步导出模式
 * - 提供自定义字段选择
 * - 记录导出历史
 * - 大数据量分页导出
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class AttendanceRecordExporter {

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Resource
    private ExportTemplateManager templateManager;

    // 导出格式枚举
    public enum ExportFormat {
        EXCEL("Excel", "application/vnd.ms-excel"),
        CSV("CSV", "text/csv"),
        PDF("PDF", "application/pdf");

        private final String displayName;
        private final String contentType;

        ExportFormat(String displayName, String contentType) {
            this.displayName = displayName;
            this.contentType = contentType;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getContentType() {
            return contentType;
        }
    }

    // 导出历史记录
    private final Map<String, ExportHistory> exportHistory = new ConcurrentHashMap<>();

    // 分页导出大小
    private static final int BATCH_SIZE = 1000;

    /**
     * 同步导出考勤记录
     *
     * @param queryForm 查询条件
     * @param format    导出格式
     * @param fields    自定义字段
     * @return 导出结果
     */
    public ExportResult exportAttendanceRecords(AttendanceExportQuery queryForm,
                                                ExportFormat format,
                                                List<String> fields) {
        String exportId = generateExportId();

        log.info("[考勤导出] 开始导出考勤记录: exportId={}, format={}", exportId, format);

        try {
            // 1. 查询数据
            List<AttendanceRecordEntity> records = queryAttendanceRecords(queryForm);

            if (records.isEmpty()) {
                log.warn("[考勤导出] 没有找到符合条件的数据: exportId={}", exportId);
                return ExportResult.empty(exportId, format);
            }

            log.info("[考勤导出] 查询到记录: exportId={}, count={}", exportId, records.size());

            // 2. 转换为导出数据
            List<Map<String, Object>> exportData = convertToExportData(records, fields);

            // 3. 根据格式导出
            byte[] data = exportByFormat(exportData, format, fields);

            // 4. 记录导出历史
            ExportHistory history = new ExportHistory();
            history.setExportId(exportId);
            history.setFormat(format);
            history.setRecordCount(records.size());
            history.setExportTime(LocalDateTime.now());
            history.setFields(fields);
            exportHistory.put(exportId, history);

            log.info("[考勤导出] 导出成功: exportId={}, format={}, size={}bytes",
                    exportId, format, data.length);

            return ExportResult.success(exportId, format, data, records.size());

        } catch (Exception e) {
            log.error("[考勤导出] 导出失败: exportId={}, error={}", exportId, e.getMessage(), e);
            return ExportResult.failure(exportId, format, e.getMessage());
        }
    }

    /**
     * 异步导出考勤记录
     *
     * @param queryForm 查询条件
     * @param format    导出格式
     * @param fields    自定义字段
     * @return 异步导出结果
     */
    @Async
    public CompletableFuture<ExportResult> exportAttendanceRecordsAsync(AttendanceExportQuery queryForm,
                                                                       ExportFormat format,
                                                                       List<String> fields) {
        String exportId = generateExportId();

        log.info("[考勤导出] 开始异步导出: exportId={}, format={}", exportId, format);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 执行同步导出
                ExportResult result = exportAttendanceRecords(queryForm, format, fields);
                log.info("[考勤导出] 异步导出完成: exportId={}", exportId);
                return result;
            } catch (Exception e) {
                log.error("[考勤导出] 异步导出失败: exportId={}", exportId, e);
                return ExportResult.failure(exportId, format, e.getMessage());
            }
        });
    }

    /**
     * 分页批量导出（大数据量）
     *
     * @param queryForm 查询条件
     * @param format    导出格式
     * @param fields    自定义字段
     * @return 批量导出结果
     */
    public BatchExportResult exportBatchAttendanceRecords(AttendanceExportQuery queryForm,
                                                          ExportFormat format,
                                                          List<String> fields) {
        String batchExportId = generateExportId();

        log.info("[考勤导出] 开始批量导出: batchExportId={}, format={}", batchExportId, format);

        try {
            // 1. 先查询总数
            long totalCount = countAttendanceRecords(queryForm);
            int totalPages = (int) Math.ceil((double) totalCount / BATCH_SIZE);

            log.info("[考勤导出] 总记录数: batchExportId={}, total={}", batchExportId, totalCount);

            // 2. 分批导出
            List<byte[]> batchData = new ArrayList<>();
            int totalExported = 0;

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                log.info("[考勤导出] 正在导出第{}/{}批: batchExportId={}",
                        pageNum, totalPages, batchExportId);

                // 分页查询
                IPage<AttendanceRecordEntity> page = new Page<>(pageNum, BATCH_SIZE);
                LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = buildQueryWrapper(queryForm);
                IPage<AttendanceRecordEntity> pageResult = attendanceRecordDao.selectPage(page, queryWrapper);

                // 转换并导出
                List<Map<String, Object>> exportData = convertToExportData(
                        pageResult.getRecords(),
                        fields
                );
                byte[] batchBytes = exportByFormat(exportData, format, fields);
                batchData.add(batchBytes);

                totalExported += pageResult.getRecords().size();
            }

            // 3. 合并批次数据
            byte[] mergedData = mergeBatchData(batchData, format);

            // 4. 记录导出历史
            ExportHistory history = new ExportHistory();
            history.setExportId(batchExportId);
            history.setFormat(format);
            history.setRecordCount((int) totalCount);
            history.setExportTime(LocalDateTime.now());
            history.setFields(fields);
            history.setBatchExport(true);
            history.setBatchCount(totalPages);
            exportHistory.put(batchExportId, history);

            log.info("[考勤导出] 批量导出成功: batchExportId={}, total={}, batches={}",
                    batchExportId, totalCount, totalPages);

            return BatchExportResult.success(batchExportId, format, mergedData, (int) totalCount, totalPages);

        } catch (Exception e) {
            log.error("[考勤导出] 批量导出失败: batchExportId={}, error={}",
                    batchExportId, e.getMessage(), e);
            return BatchExportResult.failure(batchExportId, format, e.getMessage());
        }
    }

    /**
     * 查询考勤记录
     */
    private List<AttendanceRecordEntity> queryAttendanceRecords(AttendanceExportQuery queryForm) {
        LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = buildQueryWrapper(queryForm);

        // 限制最大导出数量（单次导出最多10万条）
        queryWrapper.last("LIMIT 100000");

        return attendanceRecordDao.selectList(queryWrapper);
    }

    /**
     * 统计考勤记录数量
     */
    private long countAttendanceRecords(AttendanceExportQuery queryForm) {
        LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = buildQueryWrapper(queryForm);
        return attendanceRecordDao.selectCount(queryWrapper);
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<AttendanceRecordEntity> buildQueryWrapper(AttendanceExportQuery queryForm) {
        LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 用户ID过滤
        if (queryForm.getUserId() != null) {
            queryWrapper.eq(AttendanceRecordEntity::getUserId, queryForm.getUserId());
        }

        // 部门ID过滤
        if (queryForm.getDepartmentId() != null) {
            queryWrapper.eq(AttendanceRecordEntity::getDepartmentId, queryForm.getDepartmentId());
        }

        // 日期范围过滤
        if (queryForm.getStartDate() != null) {
            queryWrapper.ge(AttendanceRecordEntity::getAttendanceDate, queryForm.getStartDate());
        }
        if (queryForm.getEndDate() != null) {
            queryWrapper.le(AttendanceRecordEntity::getAttendanceDate, queryForm.getEndDate());
        }

        // 状态过滤
        if (queryForm.getStatus() != null) {
            queryWrapper.eq(AttendanceRecordEntity::getAttendanceStatus, queryForm.getStatus());
        }

        // 设备过滤
        if (queryForm.getDeviceId() != null) {
            queryWrapper.eq(AttendanceRecordEntity::getDeviceId, queryForm.getDeviceId());
        }

        // 未删除
        queryWrapper.eq(AttendanceRecordEntity::getDeletedFlag, 0);

        // 排序
        queryWrapper.orderByDesc(AttendanceRecordEntity::getAttendanceDate)
                .orderByDesc(AttendanceRecordEntity::getPunchTime);

        return queryWrapper;
    }

    /**
     * 转换为导出数据
     */
    private List<Map<String, Object>> convertToExportData(List<AttendanceRecordEntity> records,
                                                           List<String> fields) {
        List<Map<String, Object>> exportData = new ArrayList<>();

        for (AttendanceRecordEntity record : records) {
            Map<String, Object> row = new HashMap<>();

            // 如果指定了字段，只导出指定字段
            if (fields != null && !fields.isEmpty()) {
                for (String field : fields) {
                    Object value = getFieldValue(record, field);
                    row.put(field, value);
                }
            } else {
                // 默认导出所有字段
                row.put("recordId", record.getRecordId());
                row.put("userId", record.getUserId());
                row.put("userName", record.getUserName());
                row.put("departmentId", record.getDepartmentId());
                row.put("departmentName", record.getDepartmentName());
                row.put("attendanceDate", record.getAttendanceDate());
                row.put("punchTime", record.getPunchTime());
                row.put("deviceId", record.getDeviceId());
                row.put("deviceName", record.getDeviceName());
                row.put("location", record.getLocation());
                row.put("status", record.getAttendanceStatus());
                row.put("createTime", record.getCreateTime());
            }

            exportData.add(row);
        }

        return exportData;
    }

    /**
     * 获取字段值
     */
    private Object getFieldValue(AttendanceRecordEntity record, String fieldName) {
        return switch (fieldName) {
            case "recordId" -> record.getRecordId();
            case "userId" -> record.getUserId();
            case "userName" -> record.getUserName();
            case "departmentId" -> record.getDepartmentId();
            case "departmentName" -> record.getDepartmentName();
            case "attendanceDate" -> record.getAttendanceDate();
            case "punchTime" -> record.getPunchTime();
            case "deviceId" -> record.getDeviceId();
            case "deviceName" -> record.getDeviceName();
            case "location" -> record.getLocation();
            case "status" -> record.getAttendanceStatus();
            case "createTime" -> record.getCreateTime();
            default -> null;
        };
    }

    /**
     * 根据格式导出
     */
    private byte[] exportByFormat(List<Map<String, Object>> data,
                                  ExportFormat format,
                                  List<String> fields) throws IOException {
        return switch (format) {
            case EXCEL -> templateManager.exportToExcel(data, fields);
            case CSV -> templateManager.exportToCsv(data, fields);
            case PDF -> templateManager.exportToPdf(data, fields);
        };
    }

    /**
     * 合并批次数据
     */
    private byte[] mergeBatchData(List<byte[]> batchData, ExportFormat format) throws IOException {
        if (batchData.size() == 1) {
            return batchData.get(0);
        }

        return switch (format) {
            case EXCEL -> templateManager.mergeExcelFiles(batchData);
            case CSV -> templateManager.mergeCsvFiles(batchData);
            case PDF -> templateManager.mergePdfFiles(batchData);
        };
    }

    /**
     * 生成导出ID
     */
    private String generateExportId() {
        return "EXPORT-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 获取导出历史
     */
    public ExportHistory getExportHistory(String exportId) {
        return exportHistory.get(exportId);
    }

    /**
     * 获取所有导出历史
     */
    public Collection<ExportHistory> getAllExportHistory() {
        return exportHistory.values();
    }

    /**
     * 清除导出历史
     */
    public void clearExportHistory() {
        log.info("[考勤导出] 清除导出历史");
        exportHistory.clear();
    }

    // ==================== 内部类 ====================

    /**
     * 导出查询条件
     */
    public static class AttendanceExportQuery {
        private Long userId;
        private Long departmentId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
        private Long deviceId;

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Long getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(Long deviceId) {
            this.deviceId = deviceId;
        }
    }

    /**
     * 导出结果
     */
    public static class ExportResult {
        protected String exportId;
        protected ExportFormat format;
        protected boolean success;
        protected byte[] data;
        protected int recordCount;
        protected String errorMessage;
        protected long dataSize;

        public static ExportResult success(String exportId, ExportFormat format,
                                          byte[] data, int recordCount) {
            ExportResult result = new ExportResult();
            result.exportId = exportId;
            result.format = format;
            result.success = true;
            result.data = data;
            result.recordCount = recordCount;
            result.dataSize = data.length;
            return result;
        }

        public static ExportResult failure(String exportId, ExportFormat format,
                                           String errorMessage) {
            ExportResult result = new ExportResult();
            result.exportId = exportId;
            result.format = format;
            result.success = false;
            result.errorMessage = errorMessage;
            return result;
        }

        public static ExportResult empty(String exportId, ExportFormat format) {
            ExportResult result = new ExportResult();
            result.exportId = exportId;
            result.format = format;
            result.success = true;
            result.recordCount = 0;
            result.data = new byte[0];
            return result;
        }

        // Getters
        public String getExportId() {
            return exportId;
        }

        public ExportFormat getFormat() {
            return format;
        }

        public boolean isSuccess() {
            return success;
        }

        public byte[] getData() {
            return data;
        }

        public int getRecordCount() {
            return recordCount;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public long getDataSize() {
            return dataSize;
        }
    }

    /**
     * 批量导出结果
     */
    public static class BatchExportResult extends ExportResult {
        private int batchCount;

        public static BatchExportResult success(String exportId, ExportFormat format,
                                                byte[] data, int recordCount, int batchCount) {
            BatchExportResult result = new BatchExportResult();
            result.exportId = exportId;
            result.format = format;
            result.success = true;
            result.data = data;
            result.recordCount = recordCount;
            result.batchCount = batchCount;
            result.dataSize = data.length;
            return result;
        }

        public static BatchExportResult failure(String exportId, ExportFormat format,
                                                String errorMessage) {
            BatchExportResult result = new BatchExportResult();
            result.exportId = exportId;
            result.format = format;
            result.success = false;
            result.errorMessage = errorMessage;
            return result;
        }

        public int getBatchCount() {
            return batchCount;
        }
    }

    /**
     * 导出历史
     */
    public static class ExportHistory {
        private String exportId;
        private ExportFormat format;
        private int recordCount;
        private LocalDateTime exportTime;
        private List<String> fields;
        private boolean batchExport;
        private int batchCount;

        // Getters and Setters
        public String getExportId() {
            return exportId;
        }

        public void setExportId(String exportId) {
            this.exportId = exportId;
        }

        public ExportFormat getFormat() {
            return format;
        }

        public void setFormat(ExportFormat format) {
            this.format = format;
        }

        public int getRecordCount() {
            return recordCount;
        }

        public void setRecordCount(int recordCount) {
            this.recordCount = recordCount;
        }

        public LocalDateTime getExportTime() {
            return exportTime;
        }

        public void setExportTime(LocalDateTime exportTime) {
            this.exportTime = exportTime;
        }

        public List<String> getFields() {
            return fields;
        }

        public void setFields(List<String> fields) {
            this.fields = fields;
        }

        public boolean isBatchExport() {
            return batchExport;
        }

        public void setBatchExport(boolean batchExport) {
            this.batchExport = batchExport;
        }

        public int getBatchCount() {
            return batchCount;
        }

        public void setBatchCount(int batchCount) {
            this.batchCount = batchCount;
        }
    }
}
