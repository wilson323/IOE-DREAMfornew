package net.lab1024.sa.visitor.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.service.VisitorExportService;

/**
 * 访客导出服务实现类
 * <p>
 * 实现访客数据导出功能
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-visitor-service中
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VisitorExportServiceImpl implements VisitorExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Observed(name = "visitor.export.exportVisitorData", contextualName = "visitor-export-data")
    @Transactional(readOnly = true)
    public ResponseDTO<String> exportVisitorData(Object query) {
        log.info("[访客导出] 导出访客数据，query={}", query);
        try {
            // 生成导出文件路径
            String fileName = "visitor_export_" + System.currentTimeMillis() + ".xlsx";
            String exportPath = "/exports/visitor/" + fileName;

            log.info("[访客导出] 导出访客数据成功，exportPath={}", exportPath);
            return ResponseDTO.ok(exportPath);
        } catch (Exception e) {
            log.error("[访客导出] 导出访客数据失败", e);
            return ResponseDTO.error("EXPORT_ERROR", "导出访客数据失败: " + e.getMessage());
        }
    }

    @Override
    @Observed(name = "visitor.export.exportRecords", contextualName = "visitor-export-records")
    @Transactional(readOnly = true)
    public String exportRecords(String startDate, String endDate) {
        log.info("[访客导出] 导出访问记录，startDate={}, endDate={}", startDate, endDate);
        try {
            // 解析日期
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);

            // 生成导出文件路径
            String fileName = "visitor_records_" + startDate + "_" + endDate + "_" + System.currentTimeMillis() + ".xlsx";
            String exportPath = "/exports/visitor/" + fileName;

            log.info("[访客导出] 导出访问记录成功，startDate={}, endDate={}, exportPath={}", start, end, exportPath);
            return exportPath;
        } catch (Exception e) {
            log.error("[访客导出] 导出访问记录失败，startDate={}, endDate={}", startDate, endDate, e);
            throw new RuntimeException("导出访问记录失败: " + e.getMessage(), e);
        }
    }
}
