package net.lab1024.sa.consume.service.impl;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.consume.report.domain.form.ReportParams;
import net.lab1024.sa.consume.report.manager.ConsumeReportManager;
import net.lab1024.sa.consume.service.ConsumeReportService;

/**
 * 消费报表服务实现类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-consume-service中
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 通过Service层调用Manager层
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeReportServiceImpl implements ConsumeReportService {

    @Resource
    private ConsumeReportManager consumeReportManager;

    @Override
    @Observed(name = "consume.report.generateReport", contextualName = "consume-report-generate")
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> generateReport(Long templateId, ReportParams params) {
        log.info("[消费报表] 生成报表，templateId={}", templateId);
        try {
            return consumeReportManager.generateReport(templateId, params);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费报表] 生成报表参数错误，templateId={}, error={}", templateId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费报表] 生成报表业务异常，templateId={}, code={}, message={}", templateId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费报表] 生成报表系统异常，templateId={}, code={}, message={}", templateId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费报表] 生成报表未知异常，templateId={}", templateId, e);
            throw new SystemException("GENERATE_REPORT_SYSTEM_ERROR", "系统异常，请稍后重试", e);
        }
    }

    @Override
    @Observed(name = "consume.report.exportReport", contextualName = "consume-report-export")
    @Transactional(readOnly = true)
    public ResponseDTO<String> exportReport(Long templateId, ReportParams params, String exportFormat) {
        log.info("[消费报表] 导出报表，templateId={}, exportFormat={}", templateId, exportFormat);
        try {
            return consumeReportManager.exportReport(templateId, params, exportFormat);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费报表] 导出报表参数错误，templateId={}, exportFormat={}, error={}", templateId, exportFormat, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费报表] 导出报表业务异常，templateId={}, exportFormat={}, code={}, message={}", templateId, exportFormat, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费报表] 导出报表系统异常，templateId={}, exportFormat={}, code={}, message={}", templateId, exportFormat, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费报表] 导出报表未知异常，templateId={}, exportFormat={}", templateId, exportFormat, e);
            throw new SystemException("EXPORT_REPORT_SYSTEM_ERROR", "系统异常，请稍后重试", e);
        }
    }

    @Override
    @Observed(name = "consume.report.getReportTemplates", contextualName = "consume-report-get-templates")
    @Transactional(readOnly = true)
    public ResponseDTO<?> getReportTemplates(String templateType) {
        log.info("[消费报表] 获取报表模板列表，templateType={}", templateType);
        try {
            return consumeReportManager.getReportTemplates(templateType);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费报表] 获取报表模板列表参数错误，templateType={}, error={}", templateType, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费报表] 获取报表模板列表业务异常，templateType={}, code={}, message={}", templateType, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费报表] 获取报表模板列表系统异常，templateType={}, code={}, message={}", templateType, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费报表] 获取报表模板列表未知异常，templateType={}", templateType, e);
            throw new SystemException("GET_TEMPLATES_SYSTEM_ERROR", "系统异常，请稍后重试", e);
        }
    }

    @Override
    @Observed(name = "consume.report.getReportStatistics", contextualName = "consume-report-get-statistics")
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> getReportStatistics(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Map<String, Object> dimensions) {
        log.info("[消费报表] 获取报表统计数据，startTime={}, endTime={}", startTime, endTime);
        try {
            return consumeReportManager.getReportStatistics(startTime, endTime, dimensions);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费报表] 获取报表统计数据参数错误，startTime={}, endTime={}, error={}", startTime, endTime, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费报表] 获取报表统计数据业务异常，startTime={}, endTime={}, code={}, message={}", startTime, endTime, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费报表] 获取报表统计数据系统异常，startTime={}, endTime={}, code={}, message={}", startTime, endTime, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费报表] 获取报表统计数据未知异常，startTime={}, endTime={}", startTime, endTime, e);
            throw new SystemException("GET_STATISTICS_SYSTEM_ERROR", "系统异常，请稍后重试", e);
        }
    }
}




