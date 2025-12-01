package {{package}};

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.page.PageParam;
import net.lab1024.sa.base.common.page.PageResult;
import net.lab1024.sa.base.common.util.SmartExcelUtil;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import net.lab1024.sa.base.common.util.SmartBeanUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * {{EntityName}}导出控制器
 * <p>
 * 专门处理数据导出相关功能，支持多种导出格式和自定义导出配置
 * 严格遵循repowiki架构规范和权限控制要求
 * </p>
 *
 * @author {{author}}
 * @since {{date}}
 */
@Slf4j
@RestController
@RequestMapping("/api/{{apiPath}}/export")
@Tag(name = "{{EntityName}}数据导出", description = "{{EntityName}}模块数据导出相关接口")
@Validated
@SaCheckLogin
public class {{EntityName}}ExportController {

    @Resource
    private {{EntityName}}Service {{entityName}}Service;

    @Resource
    private {{EntityName}}Manager {{entityName}}Manager;

    // ==================== Excel导出功能 ====================

    /**
     * 导出{{EntityName}}数据到Excel
     *
     * @param queryForm 查询表单
     * @param response HTTP响应
     */
    @PostMapping("/excel")
    @Operation(summary = "导出{{EntityName}}Excel", description = "根据查询条件导出{{EntityName}}数据到Excel文件")
    @SaCheckPermission("{{apiPath}}:export")
    public void exportToExcel(@Valid @RequestBody {{EntityName}}QueryForm queryForm,
                              HttpServletResponse response) {
        try {
            log.info("开始导出{{EntityName}}数据到Excel");

            // 1. 设置导出限制
            int maxExportSize = 10000; // 最大导出数量限制
            if (queryForm.getPageSize() != null && queryForm.getPageSize() > maxExportSize) {
                queryForm.setPageSize(maxExportSize);
                log.warn("导出数据量超过限制，已限制为: {}", maxExportSize);
            }

            // 2. 查询导出数据
            ResponseDTO<List<{{EntityName}}VO>> queryResponse = {{entityName}}Service.queryList(queryForm);
            if (!queryResponse.getOk() || queryResponse.getData() == null) {
                log.error("查询导出数据失败: {}", queryResponse.getMsg());
                response.setStatus(500);
                return;
            }

            List<{{EntityName}}VO> exportData = queryResponse.getData();
            log.info("查询到导出数据，数量: {}", exportData.size());

            if (exportData.isEmpty()) {
                log.warn("没有数据可以导出");
                response.setStatus(204); // No Content
                return;
            }

            // 3. 转换为导出DTO
            List<{{EntityName}}ExportDTO> exportDTOs = exportData.stream()
                    .map(this::convertToExportDTO)
                    .collect(Collectors.toList());

            // 4. 执行Excel导出
            String fileName = String.format("{{EntityName}}数据_%s.xlsx",
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            SmartExcelUtil.downloadExcel(response, {{EntityName}}ExportDTO.class, exportDTOs, fileName);

            log.info("{{EntityName}}数据Excel导出完成，文件名: {}, 数据量: {}", fileName, exportDTOs.size());

        } catch (Exception e) {
            log.error("导出{{EntityName}}Excel失败", e);
            try {
                response.setStatus(500);
                response.getWriter().write("导出失败：" + e.getMessage());
            } catch (Exception ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

    /**
     * 导出{{EntityName}}模板Excel
     *
     * @param response HTTP响应
     */
    @GetMapping("/template")
    @Operation(summary = "导出{{EntityName}}Excel模板", description = "导出{{EntityName}}数据导入的Excel模板文件")
    @SaCheckPermission("{{apiPath}}:export")
    public void exportTemplate(HttpServletResponse response) {
        try {
            log.info("开始导出{{EntityName}}Excel模板");

            // 创建模板数据（空数据或示例数据）
            List<{{EntityName}}ImportTemplateDTO> templateData = new ArrayList<>();

            // 可以添加一行示例数据
            {{EntityName}}ImportTemplateDTO exampleData = new {{EntityName}}ImportTemplateDTO();
            exampleData.setBusinessName("示例业务名称");
            exampleData.setBusinessType("示例业务类型");
            exampleData.setRemark("示例备注信息");
            templateData.add(exampleData);

            String fileName = String.format("{{EntityName}}导入模板_%s.xlsx",
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            SmartExcelUtil.downloadExcel(response, {{EntityName}}ImportTemplateDTO.class, templateData, fileName);

            log.info("{{EntityName}}Excel模板导出完成，文件名: {}", fileName);

        } catch (Exception e) {
            log.error("导出{{EntityName}}Excel模板失败", e);
            try {
                response.setStatus(500);
                response.getWriter().write("模板导出失败：" + e.getMessage());
            } catch (Exception ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

    // ==================== CSV导出功能 ====================

    /**
     * 导出{{EntityName}}数据到CSV
     *
     * @param queryForm 查询表单
     * @param response HTTP响应
     */
    @PostMapping("/csv")
    @Operation(summary = "导出{{EntityName}}CSV", description = "根据查询条件导出{{EntityName}}数据到CSV文件")
    @SaCheckPermission("{{apiPath}}:export")
    public void exportToCsv(@Valid @RequestBody {{EntityName}}QueryForm queryForm,
                            HttpServletResponse response) {
        try {
            log.info("开始导出{{EntityName}}数据到CSV");

            // 1. 设置导出限制
            int maxExportSize = 10000;
            if (queryForm.getPageSize() != null && queryForm.getPageSize() > maxExportSize) {
                queryForm.setPageSize(maxExportSize);
            }

            // 2. 查询导出数据
            ResponseDTO<List<{{EntityName}}VO>> queryResponse = {{entityName}}Service.queryList(queryForm);
            if (!queryResponse.getOk() || queryResponse.getData() == null) {
                log.error("查询导出数据失败: {}", queryResponse.getMsg());
                response.setStatus(500);
                return;
            }

            List<{{EntityName}}VO> exportData = queryResponse.getData();
            log.info("查询到导出数据，数量: {}", exportData.size());

            if (exportData.isEmpty()) {
                log.warn("没有数据可以导出");
                response.setStatus(204);
                return;
            }

            // 3. 设置响应头
            String fileName = String.format("{{EntityName}}数据_%s.csv",
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            response.setCharacterEncoding("UTF-8");

            // 4. 写入CSV数据
            try (java.io.PrintWriter writer = response.getWriter()) {
                // 写入BOM以支持中文
                writer.write('\ufeff');

                // 写入表头
                writer.println("ID,业务键,业务名称,业务类型,状态,优先级,创建时间,更新时间,创建人,备注");

                // 写入数据行
                for ({{EntityName}}VO vo : exportData) {
                    String csvLine = String.format("%d,%s,%s,%s,%s,%d,%s,%s,%s,%s",
                            vo.get{{EntityName}}Id(),
                            escapeCsvValue(vo.getBusinessKey()),
                            escapeCsvValue(vo.getBusinessName()),
                            escapeCsvValue(vo.getBusinessType()),
                            escapeCsvValue(vo.getStatus()),
                            vo.getPriority() != null ? vo.getPriority() : 0,
                            vo.getCreateTime() != null ? vo.getCreateTime().toString() : "",
                            vo.getUpdateTime() != null ? vo.getUpdateTime().toString() : "",
                            escapeCsvValue(vo.getCreateUserName()),
                            escapeCsvValue(vo.getRemark())
                    );
                    writer.println(csvLine);
                }

                writer.flush();
            }

            log.info("{{EntityName}}数据CSV导出完成，文件名: {}, 数据量: {}", fileName, exportData.size());

        } catch (Exception e) {
            log.error("导出{{EntityName}}CSV失败", e);
            try {
                response.setStatus(500);
                response.getWriter().write("CSV导出失败：" + e.getMessage());
            } catch (Exception ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

    // ==================== JSON导出功能 ====================

    /**
     * 导出{{EntityName}}数据到JSON
     *
     * @param queryForm 查询表单
     * @return JSON数据
     */
    @PostMapping("/json")
    @Operation(summary = "导出{{EntityName}}JSON", description = "根据查询条件导出{{EntityName}}数据为JSON格式")
    @SaCheckPermission("{{apiPath}}:export")
    public ResponseDTO<Map<String, Object>> exportToJson(@Valid @RequestBody {{EntityName}}QueryForm queryForm) {
        try {
            log.info("开始导出{{EntityName}}数据到JSON");

            // 1. 设置导出限制
            int maxExportSize = 5000; // JSON导出限制更严格
            if (queryForm.getPageSize() != null && queryForm.getPageSize() > maxExportSize) {
                queryForm.setPageSize(maxExportSize);
            }

            // 2. 查询导出数据
            ResponseDTO<List<{{EntityName}}VO>> queryResponse = {{entityName}}Service.queryList(queryForm);
            if (!queryResponse.getOk()) {
                return ResponseDTO.error("查询导出数据失败：" + queryResponse.getMsg());
            }

            List<{{EntityName}}VO> exportData = queryResponse.getData() != null ? queryResponse.getData() : new ArrayList<>();
            log.info("查询到导出数据，数量: {}", exportData.size());

            // 3. 构建导出结果
            Map<String, Object> exportResult = new HashMap<>();
            exportResult.put("exportTime", LocalDateTime.now());
            exportResult.put("totalCount", exportData.size());
            exportResult.put("queryConditions", queryForm);
            exportResult.put("data", exportData);
            exportResult.put("fileName", String.format("{{EntityName}}数据_%s.json",
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))));

            log.info("{{EntityName}}数据JSON导出完成，数据量: {}", exportData.size());
            return ResponseDTO.ok(exportResult);

        } catch (Exception e) {
            log.error("导出{{EntityName}}JSON失败", e);
            return ResponseDTO.error("JSON导出失败：" + e.getMessage());
        }
    }

    // ==================== 批量导出功能 ====================

    /**
     * 批量导出{{EntityName}}数据
     *
     * @param exportRequest 批量导出请求
     * @param response HTTP响应
     */
    @PostMapping("/batch")
    @Operation(summary = "批量导出{{EntityName}}数据", description = "根据多个查询条件批量导出{{EntityName}}数据")
    @SaCheckPermission("{{apiPath}}:export")
    public void batchExport(@Valid @RequestBody {{EntityName}}BatchExportRequest exportRequest,
                            HttpServletResponse response) {
        try {
            log.info("开始批量导出{{EntityName}}数据，查询条件数量: {}",
                    exportRequest.getQueryForms() != null ? exportRequest.getQueryForms().size() : 0);

            if (exportRequest.getQueryForms() == null || exportRequest.getQueryForms().isEmpty()) {
                response.setStatus(400);
                response.getWriter().write("批量导出条件不能为空");
                return;
            }

            // 1. 验证批量导出限制
            int maxBatchSize = 5; // 最大批量数量
            int maxTotalSize = 50000; // 最大总数据量

            if (exportRequest.getQueryForms().size() > maxBatchSize) {
                response.setStatus(400);
                response.getWriter().write("批量导出条件数量不能超过" + maxBatchSize);
                return;
            }

            // 2. 收集所有导出数据
            List<{{EntityName}}VO> allExportData = new ArrayList<>();

            for (int i = 0; i < exportRequest.getQueryForms().size(); i++) {
                {{EntityName}}QueryForm queryForm = exportRequest.getQueryForms().get(i);

                // 限制单个查询的数据量
                int singleQueryLimit = maxTotalSize / exportRequest.getQueryForms().size();
                queryForm.setPageSize(Math.min(queryForm.getPageSize() != null ? queryForm.getPageSize() : 1000, singleQueryLimit));

                ResponseDTO<List<{{EntityName}}VO>> queryResponse = {{entityName}}Service.queryList(queryForm);
                if (queryResponse.getOk() && queryResponse.getData() != null) {
                    allExportData.addAll(queryResponse.getData());
                }

                // 检查总数据量限制
                if (allExportData.size() > maxTotalSize) {
                    allExportData = allExportData.subList(0, maxTotalSize);
                    log.warn("批量导出数据量超过限制，已截断为: {}", maxTotalSize);
                    break;
                }
            }

            log.info("批量导出查询完成，总数据量: {}", allExportData.size());

            if (allExportData.isEmpty()) {
                response.setStatus(204);
                return;
            }

            // 3. 转换为导出DTO并导出
            List<{{EntityName}}ExportDTO> exportDTOs = allExportData.stream()
                    .map(this::convertToExportDTO)
                    .collect(Collectors.toList());

            String fileName = String.format("{{EntityName}}批量数据_%s.xlsx",
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            SmartExcelUtil.downloadExcel(response, {{EntityName}}ExportDTO.class, exportDTOs, fileName);

            log.info("{{EntityName}}批量数据导出完成，文件名: {}, 数据量: {}", fileName, exportDTOs.size());

        } catch (Exception e) {
            log.error("批量导出{{EntityName}}数据失败", e);
            try {
                response.setStatus(500);
                response.getWriter().write("批量导出失败：" + e.getMessage());
            } catch (Exception ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

    // ==================== 自定义导出功能 ====================

    /**
     * 自定义字段导出
     *
     * @param customExportRequest 自定义导出请求
     * @param response HTTP响应
     */
    @PostMapping("/custom")
    @Operation(summary = "自定义字段导出", description = "根据用户选择的字段导出{{EntityName}}数据")
    @SaCheckPermission("{{apiPath}}:export")
    public void customExport(@Valid @RequestBody {{EntityName}}CustomExportRequest customExportRequest,
                             HttpServletResponse response) {
        try {
            log.info("开始自定义导出{{EntityName}}数据，选择字段: {}",
                    customExportRequest.getSelectedFields());

            if (customExportRequest.getSelectedFields() == null || customExportRequest.getSelectedFields().isEmpty()) {
                response.setStatus(400);
                response.getWriter().write("请选择要导出的字段");
                return;
            }

            // 1. 查询数据
            ResponseDTO<List<{{EntityName}}VO>> queryResponse = {{entityName}}Service.queryList(customExportRequest.getQueryForm());
            if (!queryResponse.getOk() || queryResponse.getData() == null) {
                log.error("查询导出数据失败: {}", queryResponse.getMsg());
                response.setStatus(500);
                return;
            }

            List<{{EntityName}}VO> exportData = queryResponse.getData();
            log.info("查询到导出数据，数量: {}", exportData.size());

            if (exportData.isEmpty()) {
                response.setStatus(204);
                return;
            }

            // 2. 根据选择字段构建自定义数据
            List<Map<String, Object>> customExportData = new ArrayList<>();
            for ({{EntityName}}VO vo : exportData) {
                Map<String, Object> rowData = new HashMap<>();
                for (String field : customExportRequest.getSelectedFields()) {
                    Object value = getFieldValueByFieldName(vo, field);
                    rowData.add(getFieldDisplayName(field), value);
                }
                customExportData.add(rowData);
            }

            // 3. 导出自定义格式（这里简化为CSV，实际可以根据需要支持更多格式）
            String fileName = String.format("{{EntityName}}自定义数据_%s.csv",
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            response.setCharacterEncoding("UTF-8");

            try (java.io.PrintWriter writer = response.getWriter()) {
                writer.write('\ufeff'); // BOM for UTF-8

                // 写入表头
                if (!customExportData.isEmpty()) {
                    writer.println(String.join(",", customExportRequest.getSelectedFields().stream()
                            .map(this::getFieldDisplayName)
                            .collect(Collectors.toList())));
                }

                // 写入数据
                for (Map<String, Object> rowData : customExportData) {
                    String csvLine = rowData.values().stream()
                            .map(value -> escapeCsvValue(String.valueOf(value)))
                            .collect(Collectors.joining(","));
                    writer.println(csvLine);
                }

                writer.flush();
            }

            log.info("{{EntityName}}自定义数据导出完成，文件名: {}, 数据量: {}", fileName, exportData.size());

        } catch (Exception e) {
            log.error("自定义导出{{EntityName}}数据失败", e);
            try {
                response.setStatus(500);
                response.getWriter().write("自定义导出失败：" + e.getMessage());
            } catch (Exception ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

    // ==================== 导出状态查询 ====================

    /**
     * 查询导出状态
     *
     * @param exportId 导出ID
     * @return 导出状态
     */
    @GetMapping("/status/{exportId}")
    @Operation(summary = "查询导出状态", description = "根据导出ID查询导出任务的执行状态")
    @SaCheckPermission("{{apiPath}}:export")
    public ResponseDTO<Map<String, Object>> getExportStatus(@PathVariable String exportId) {
        try {
            // 这里可以实现导出任务状态查询逻辑
            // 例如：从Redis或数据库查询导出任务状态

            Map<String, Object> statusInfo = new HashMap<>();
            statusInfo.put("exportId", exportId);
            statusInfo.put("status", "COMPLETED"); // PENDING, RUNNING, COMPLETED, FAILED
            statusInfo.put("progress", 100);
            statusInfo.put("downloadUrl", "/api/{{apiPath}}/export/download/" + exportId);
            statusInfo.put("createTime", LocalDateTime.now());
            statusInfo.put("completeTime", LocalDateTime.now());

            return ResponseDTO.ok(statusInfo);

        } catch (Exception e) {
            log.error("查询导出状态失败，exportId: {}", exportId, e);
            return ResponseDTO.error("查询导出状态失败");
        }
    }

    // ==================== 私有工具方法 ====================

    /**
     * 转换为导出DTO
     */
    private {{EntityName}}ExportDTO convertToExportDTO({{EntityName}}VO vo) {
        if (vo == null) {
            return null;
        }

        {{EntityName}}ExportDTO exportDTO = new {{EntityName}}ExportDTO();
        SmartBeanUtil.copyProperties(vo, exportDTO);

        // 可以在这里添加额外的转换逻辑
        // 例如：状态名称转换、时间格式化等

        return exportDTO;
    }

    /**
     * 转义CSV值
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }

        // 如果包含逗号、引号或换行符，需要用引号包围并转义内部引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    /**
     * 根据字段名获取字段值
     */
    private Object getFieldValueByFieldName({{EntityName}}VO vo, String fieldName) {
        try {
            switch (fieldName) {
                case "{{entityName}}Id":
                    return vo.get{{EntityName}}Id();
                case "businessKey":
                    return vo.getBusinessKey();
                case "businessName":
                    return vo.getBusinessName();
                case "businessType":
                    return vo.getBusinessType();
                case "status":
                    return vo.getStatus();
                case "priority":
                    return vo.getPriority();
                case "createTime":
                    return vo.getCreateTime();
                case "updateTime":
                    return vo.getUpdateTime();
                case "createUserName":
                    return vo.getCreateUserName();
                case "remark":
                    return vo.getRemark();
                default:
                    return "";
            }
        } catch (Exception e) {
            log.warn("获取字段值失败，字段: {}", fieldName, e);
            return "";
        }
    }

    /**
     * 获取字段显示名称
     */
    private String getFieldDisplayName(String fieldName) {
        switch (fieldName) {
            case "{{entityName}}Id":
                return "ID";
            case "businessKey":
                return "业务键";
            case "businessName":
                return "业务名称";
            case "businessType":
                return "业务类型";
            case "status":
                return "状态";
            case "priority":
                return "优先级";
            case "createTime":
                return "创建时间";
            case "updateTime":
                return "更新时间";
            case "createUserName":
                return "创建人";
            case "remark":
                return "备注";
            default:
                return fieldName;
        }
    }
}

// ==================== 导出相关DTO类 ====================

/**
 * {{EntityName}}导出DTO
 */
class {{EntityName}}ExportDTO {
    // 导出字段定义
    private Long {{entityName}}Id;
    private String businessKey;
    private String businessName;
    private String businessType;
    private String status;
    private Integer priority;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private String createUserName;
    private String remark;

    // getters and setters...
    public Long get{{EntityName}}Id() { return {{entityName}}Id; }
    public void set{{EntityName}}Id(Long {{entityName}}Id) { this.{{entityName}}Id = {{entityName}}Id; }
    // 其他getter/setter方法...
}

/**
 * {{EntityName}}导入模板DTO
 */
class {{EntityName}}ImportTemplateDTO {
    private String businessName;
    private String businessType;
    private Integer priority;
    private String remark;

    // getters and setters...
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    // 其他getter/setter方法...
}

/**
 * {{EntityName}}批量导出请求
 */
class {{EntityName}}BatchExportRequest {
    private List<{{EntityName}}QueryForm> queryForms;
    private String exportFormat; // EXCEL, CSV, JSON

    // getters and setters...
    public List<{{EntityName}}QueryForm> getQueryForms() { return queryForms; }
    public void setQueryForms(List<{{EntityName}}QueryForm> queryForms) { this.queryForms = queryForms; }
    public String getExportFormat() { return exportFormat; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }
}

/**
 * {{EntityName}}自定义导出请求
 */
class {{EntityName}}CustomExportRequest {
    private {{EntityName}}QueryForm queryForm;
    private List<String> selectedFields;
    private String exportFormat; // EXCEL, CSV

    // getters and setters...
    public {{EntityName}}QueryForm getQueryForm() { return queryForm; }
    public void setQueryForm({{EntityName}}QueryForm queryForm) { this.queryForm = queryForm; }
    public List<String> getSelectedFields() { return selectedFields; }
    public void setSelectedFields(List<String> selectedFields) { this.selectedFields = selectedFields; }
    public String getExportFormat() { return exportFormat; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }
}

/**
 * {{EntityName}}审批表单（在Manager模板中引用）
 */
class {{EntityName}}ApprovalForm {
    private String approvalResult; // APPROVED, REJECTED
    private String approvalComment;
    private Long approverId;

    // getters and setters...
    public String getApprovalResult() { return approvalResult; }
    public void setApprovalResult(String approvalResult) { this.approvalResult = approvalResult; }
    public String getApprovalComment() { return approvalComment; }
    public void setApprovalComment(String approvalComment) { this.approvalComment = approvalComment; }
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
}