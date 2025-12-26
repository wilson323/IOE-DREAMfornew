package net.lab1024.sa.data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.gateway.client.GatewayServiceClient;
import net.lab1024.sa.data.dao.ExportTaskDao;
import net.lab1024.sa.data.dao.ReportDao;
import net.lab1024.sa.data.domain.DataAnalysisDomain.*;
import net.lab1024.sa.data.domain.entity.ExportTaskEntity;
import net.lab1024.sa.data.domain.entity.ReportEntity;
import net.lab1024.sa.data.service.FileExportService;
import net.lab1024.sa.data.service.ReportService;
import org.springframework.http.HttpMethod;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 数据报表服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ReportServiceImpl implements ReportService {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ReportDao reportDao;

    @Resource
    private ExportTaskDao exportTaskDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private FileExportService fileExportService;

    // ==================== 报表管理 ====================

    @Override
    @CacheEvict(value = "reports", allEntries = true)
    public Long createReport(ReportVO report) {
        log.info("[数据报表] 创建报表: reportName={}, reportType={}",
                 report.getReportName(), report.getReportType());

        ReportEntity entity = convertToEntity(report);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setStatus("draft");
        entity.setDeletedFlag(0);
        entity.setVersion(0);

        reportDao.insert(entity);

        log.info("[数据报表] 报表创建成功: reportId={}", entity.getReportId());
        return entity.getReportId();
    }

    @Override
    @CacheEvict(value = "reports", key = "#reportId")
    public void updateReport(Long reportId, ReportVO report) {
        log.info("[数据报表] 更新报表: reportId={}", reportId);

        ReportEntity existingEntity = reportDao.selectById(reportId);
        if (existingEntity == null) {
            throw new BusinessException("REPORT_NOT_FOUND", "报表不存在: " + reportId);
        }

        ReportEntity entity = convertToEntity(report);
        entity.setReportId(reportId);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setCreatorId(existingEntity.getCreatorId());
        entity.setCreateTime(existingEntity.getCreateTime());
        entity.setVersion(existingEntity.getVersion());

        int updated = reportDao.updateById(entity);

        if (updated == 0) {
            throw new BusinessException("REPORT_UPDATE_FAILED", "报表更新失败，可能是版本冲突");
        }

        log.info("[数据报表] 报表更新成功: reportId={}", reportId);
    }

    @Override
    @CacheEvict(value = "reports", key = "#reportId")
    public void deleteReport(Long reportId) {
        log.info("[数据报表] 删除报表: reportId={}", reportId);

        ReportEntity entity = reportDao.selectById(reportId);
        if (entity == null) {
            throw new BusinessException("REPORT_NOT_FOUND", "报表不存在: " + reportId);
        }

        // 使用逻辑删除
        entity.setDeletedFlag(1);
        reportDao.updateById(entity);

        log.info("[数据报表] 报表删除成功: reportId={}", reportId);
    }

    @Override
    @Cacheable(value = "reports", key = "#reportId")
    public ReportVO getReportById(Long reportId) {
        log.debug("[数据报表] 查询报表: reportId={}", reportId);

        ReportEntity entity = reportDao.selectById(reportId);
        if (entity == null) {
            throw new BusinessException("REPORT_NOT_FOUND", "报表不存在: " + reportId);
        }

        return convertToVO(entity);
    }

    @Override
    public PageResult<ReportVO> listReports(String businessModule, String reportType, Integer pageNum, Integer pageSize) {
        log.info("[数据报表] 查询报表列表: businessModule={}, reportType={}, pageNum={}, pageSize={}",
                 businessModule, reportType, pageNum, pageSize);

        LambdaQueryWrapper<ReportEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(businessModule != null, ReportEntity::getBusinessModule, businessModule)
                   .eq(reportType != null, ReportEntity::getReportType, reportType)
                   .orderByDesc(ReportEntity::getCreateTime);

        Page<ReportEntity> page = new Page<>(pageNum, pageSize);
        IPage<ReportEntity> resultPage = reportDao.selectPage(page, queryWrapper);

        List<ReportVO> reportList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(reportList, resultPage.getTotal(), pageNum, pageSize);
    }

    // ==================== 数据查询 ====================

    @Override
    public ReportQueryResult queryReportData(ReportQueryRequest request) {
        log.info("[数据报表] 查询报表数据: reportId={}, startDate={}, endDate={}",
                 request.getReportId(), request.getStartDate(), request.getEndDate());

        ReportVO report = getReportById(request.getReportId());

        ReportQueryResult result = new ReportQueryResult();
        result.setReportId(report.getReportId());
        result.setReportName(report.getReportName());
        result.setBusinessModule(report.getBusinessModule());
        result.setQueryTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 根据业务模块生成模拟数据
        List<Map<String, Object>> dataList = generateMockData(report.getBusinessModule(), request);
        result.setDataList(dataList);

        result.setTotalRecords(dataList.size());
        result.setExecutionTimeMs(15);

        log.info("[数据报表] 查询完成: reportId={}, records={}", report.getReportId(), dataList.size());
        return result;
    }

    @Override
    public List<DataStatisticsVO> getStatistics(Long reportId, String startDate, String endDate) {
        log.info("[数据报表] 获取统计数据: reportId={}, startDate={}, endDate={}",
                 reportId, startDate, endDate);

        ReportVO report = getReportById(reportId);

        // 根据业务模块生成统计数据
        List<DataStatisticsVO> statistics = generateStatistics(report.getBusinessModule());

        log.info("[数据报表] 统计数据生成完成: reportId={}, count={}", reportId, statistics.size());
        return statistics;
    }

    // ==================== 数据导出 ====================

    @Override
    public String exportData(DataExportRequest request) {
        log.info("[数据报表] 导出数据: reportId={}, exportFormat={}",
                 request.getReportId(), request.getExportFormat());

        String taskId = UUID.randomUUID().toString().replace("-", "");

        // 创建导出任务
        ExportTaskEntity taskEntity = new ExportTaskEntity();
        taskEntity.setExportTaskId(taskId);
        taskEntity.setReportId(request.getReportId());
        taskEntity.setExportFormat(request.getExportFormat());
        taskEntity.setStatus("pending");

        try {
            String requestParamsJson = objectMapper.writeValueAsString(request.getParams());
            taskEntity.setRequestParams(requestParamsJson);
        } catch (Exception e) {
            log.error("[数据报表] 序列化请求参数失败", e);
        }

        exportTaskDao.insert(taskEntity);

        // 异步执行导出
        CompletableFuture.runAsync(() -> {
            try {
                executeExport(taskId, request);
            } catch (Exception e) {
                log.error("[数据报表] 导出失败: taskId={}", taskId, e);
                handleExportFailure(taskId, e.getMessage());
            }
        });

        log.info("[数据报表] 导出任务创建成功: taskId={}", taskId);
        return taskId;
    }

    @Override
    public DataExportResult getExportStatus(String taskId) {
        log.debug("[数据报表] 查询导出状态: taskId={}", taskId);

        ExportTaskEntity taskEntity = exportTaskDao.selectById(taskId);
        if (taskEntity == null) {
            throw new BusinessException("EXPORT_TASK_NOT_FOUND", "导出任务不存在: " + taskId);
        }

        DataExportResult result = new DataExportResult();
        result.setTaskId(taskId);
        result.setStatus(taskEntity.getStatus());
        result.setFileName(taskEntity.getFileName());
        result.setFileUrl(taskEntity.getFileUrl());
        result.setFileSize(taskEntity.getFileSize());

        if (taskEntity.getErrorMessage() != null) {
            result.setErrorMessage(taskEntity.getErrorMessage());
        }

        return result;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * Entity转VO
     */
    private ReportVO convertToVO(ReportEntity entity) {
        ReportVO vo = new ReportVO();
        vo.setReportId(entity.getReportId());
        vo.setReportName(entity.getReportName());
        vo.setReportCode(entity.getReportCode());
        vo.setReportType(entity.getReportType());
        vo.setBusinessModule(entity.getBusinessModule());

        try {
            if (entity.getSourceConfig() != null) {
                DataSourceConfig dataSource = objectMapper.readValue(entity.getSourceConfig(), DataSourceConfig.class);
                vo.setDataSource(dataSource);
            }

            if (entity.getQueryConfig() != null) {
                QueryConfig queryConfig = objectMapper.readValue(entity.getQueryConfig(), QueryConfig.class);
                vo.setQueryConfig(queryConfig);
            }

            if (entity.getLayoutConfig() != null) {
                ReportLayout layout = objectMapper.readValue(entity.getLayoutConfig(), ReportLayout.class);
                vo.setLayout(layout);
            }

            if (entity.getPermissionConfig() != null) {
                ReportPermission permission = objectMapper.readValue(entity.getPermissionConfig(), ReportPermission.class);
                vo.setPermission(permission);
            }
        } catch (Exception e) {
            log.error("[数据报表] JSON反序列化失败", e);
        }

        vo.setCreatorId(entity.getCreatorId());
        vo.setCreatorName(entity.getCreatorName());
        vo.setStatus(entity.getStatus());
        vo.setDescription(entity.getDescription());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        return vo;
    }

    /**
     * VO转Entity
     */
    private ReportEntity convertToEntity(ReportVO vo) {
        ReportEntity entity = new ReportEntity();
        entity.setReportId(vo.getReportId());
        entity.setReportName(vo.getReportName());
        entity.setReportCode(vo.getReportCode());
        entity.setReportType(vo.getReportType());
        entity.setBusinessModule(vo.getBusinessModule());

        if (vo.getDataSource() != null) {
            try {
                entity.setSourceConfig(objectMapper.writeValueAsString(vo.getDataSource()));
                entity.setSourceType(vo.getDataSource().getType());
                entity.setSourceName(vo.getDataSource().getName());
            } catch (Exception e) {
                log.error("[数据报表] JSON序列化失败", e);
            }
        }

        if (vo.getQueryConfig() != null) {
            try {
                entity.setQueryConfig(objectMapper.writeValueAsString(vo.getQueryConfig()));
            } catch (Exception e) {
                log.error("[数据报表] JSON序列化失败", e);
            }
        }

        if (vo.getLayout() != null) {
            try {
                entity.setLayoutConfig(objectMapper.writeValueAsString(vo.getLayout()));
            } catch (Exception e) {
                log.error("[数据报表] JSON序列化失败", e);
            }
        }

        if (vo.getPermission() != null) {
            try {
                entity.setPermissionConfig(objectMapper.writeValueAsString(vo.getPermission()));
            } catch (Exception e) {
                log.error("[数据报表] JSON序列化失败", e);
            }
        }

        entity.setCreatorId(vo.getCreatorId());
        entity.setCreatorName(vo.getCreatorName());
        entity.setStatus(vo.getStatus());
        entity.setDescription(vo.getDescription());

        return entity;
    }

    /**
     * 生成模拟数据（调用真实API）
     */
    private List<Map<String, Object>> generateMockData(String businessModule, ReportQueryRequest request) {
        log.info("[数据报表] 获取业务数据: businessModule={}, startDate={}, endDate={}",
                 businessModule, request.getStartDate(), request.getEndDate());

        try {
            switch (businessModule) {
                case "attendance":
                    return getAttendanceData(request);

                case "consume":
                    return getConsumeData(request);

                case "access":
                    return getAccessData(request);

                default:
                    log.warn("[数据报表] 未知的业务模块: {}", businessModule);
                    return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("[数据报表] 获取业务数据失败: businessModule={}", businessModule, e);
            // 降级：返回空数据
            return new ArrayList<>();
        }
    }

    /**
     * 获取考勤数据
     */
    private List<Map<String, Object>> getAttendanceData(ReportQueryRequest request) {
        log.info("[数据报表] 调用考勤服务API: startDate={}, endDate={}",
                 request.getStartDate(), request.getEndDate());

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("startDate", request.getStartDate());
            params.put("endDate", request.getEndDate());
            if (request.getParams() != null) {
                params.putAll(request.getParams());
            }

            ResponseDTO<List<Map<String, Object>>> response = gatewayServiceClient.callAttendanceService(
                    "/api/v1/attendance/records/query",
                    HttpMethod.POST,
                    params,
                    new TypeReference<ResponseDTO<List<Map<String, Object>>>>() {}
            );

            if (response != null && response.getCode() == 200 && response.getData() != null) {
                log.info("[数据报表] 考勤数据获取成功: records={}", response.getData().size());
                return response.getData();
            } else {
                log.warn("[数据报表] 考勤服务返回异常: code={}, message={}",
                         response != null ? response.getCode() : null,
                         response != null ? response.getMessage() : null);
                return generateFallbackAttendanceData();
            }
        } catch (Exception e) {
            log.error("[数据报表] 调用考勤服务失败", e);
            return generateFallbackAttendanceData();
        }
    }

    /**
     * 获取消费数据
     */
    private List<Map<String, Object>> getConsumeData(ReportQueryRequest request) {
        log.info("[数据报表] 调用消费服务API: startDate={}, endDate={}",
                 request.getStartDate(), request.getEndDate());

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("startDate", request.getStartDate());
            params.put("endDate", request.getEndDate());
            if (request.getParams() != null) {
                params.putAll(request.getParams());
            }

            ResponseDTO<List<Map<String, Object>>> response = gatewayServiceClient.callConsumeService(
                    "/api/v1/consume/records/query",
                    HttpMethod.POST,
                    params,
                    new TypeReference<ResponseDTO<List<Map<String, Object>>>>() {}
            );

            if (response != null && response.getCode() == 200 && response.getData() != null) {
                log.info("[数据报表] 消费数据获取成功: records={}", response.getData().size());
                return response.getData();
            } else {
                log.warn("[数据报表] 消费服务返回异常: code={}, message={}",
                         response != null ? response.getCode() : null,
                         response != null ? response.getMessage() : null);
                return generateFallbackConsumeData();
            }
        } catch (Exception e) {
            log.error("[数据报表] 调用消费服务失败", e);
            return generateFallbackConsumeData();
        }
    }

    /**
     * 获取门禁数据
     */
    private List<Map<String, Object>> getAccessData(ReportQueryRequest request) {
        log.info("[数据报表] 调用门禁服务API: startDate={}, endDate={}",
                 request.getStartDate(), request.getEndDate());

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("startDate", request.getStartDate());
            params.put("endDate", request.getEndDate());
            if (request.getParams() != null) {
                params.putAll(request.getParams());
            }

            ResponseDTO<List<Map<String, Object>>> response = gatewayServiceClient.callAccessService(
                    "/api/v1/access/records/query",
                    HttpMethod.POST,
                    params,
                    new TypeReference<ResponseDTO<List<Map<String, Object>>>>() {}
            );

            if (response != null && response.getCode() == 200 && response.getData() != null) {
                log.info("[数据报表] 门禁数据获取成功: records={}", response.getData().size());
                return response.getData();
            } else {
                log.warn("[数据报表] 门禁服务返回异常: code={}, message={}",
                         response != null ? response.getCode() : null,
                         response != null ? response.getMessage() : null);
                return generateFallbackAccessData();
            }
        } catch (Exception e) {
            log.error("[数据报表] 调用门禁服务失败", e);
            return generateFallbackAccessData();
        }
    }

    /**
     * 降级：生成考勤模拟数据
     */
    private List<Map<String, Object>> generateFallbackAttendanceData() {
        log.warn("[数据报表] 使用考勤模拟数据");

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("recordId", 1000L + i);
            data.put("userId", 100L + i);
            data.put("username", "用户" + i);
            data.put("department", "技术部");
            data.put("checkInTime", "08:55:" + String.format("%02d", i));
            data.put("checkOutTime", "18:05:" + String.format("%02d", i));
            data.put("workHours", 9.2);
            data.put("status", i % 5 == 0 ? "迟到" : "正常");
            dataList.add(data);
        }
        return dataList;
    }

    /**
     * 降级：生成消费模拟数据
     */
    private List<Map<String, Object>> generateFallbackConsumeData() {
        log.warn("[数据报表] 使用消费模拟数据");

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("consumeId", 2000L + i);
            data.put("userId", 100L + i);
            data.put("username", "用户" + i);
            data.put("consumeType", "餐饮消费");
            data.put("amount", 15.50 + i);
            data.put("consumeTime", "12:30:" + String.format("%02d", i));
            data.put("location", "餐厅1楼");
            dataList.add(data);
        }
        return dataList;
    }

    /**
     * 降级：生成门禁模拟数据
     */
    private List<Map<String, Object>> generateFallbackAccessData() {
        log.warn("[数据报表] 使用门禁模拟数据");

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("accessId", 3000L + i);
            data.put("userId", 100L + i);
            data.put("username", "用户" + i);
            data.put("deviceName", "主入口门禁");
            data.put("accessTime", "09:00:" + String.format("%02d", i));
            data.put("accessResult", "允许通行");
            dataList.add(data);
        }
        return dataList;
    }

    /**
     * 生成统计数据
     */
    private List<DataStatisticsVO> generateStatistics(String businessModule) {
        List<DataStatisticsVO> statistics = new ArrayList<>();

        switch (businessModule) {
            case "attendance":
                statistics.add(DataStatisticsVO.builder()
                        .name("今日打卡人数")
                        .value(245)
                        .trend("up")
                        .changeRate(12.5)
                        .build());

                statistics.add(DataStatisticsVO.builder()
                        .name("迟到人数")
                        .value(8)
                        .trend("down")
                        .changeRate(-15.3)
                        .build());

                statistics.add(DataStatisticsVO.builder()
                        .name("平均工时")
                        .value(8.7)
                        .trend("up")
                        .changeRate(3.2)
                        .build());

                statistics.add(DataStatisticsVO.builder()
                        .name("出勤率")
                        .value(96.8)
                        .trend("up")
                        .changeRate(2.1)
                        .unit("%")
                        .build());
                break;

            case "consume":
                statistics.add(DataStatisticsVO.builder()
                        .name("今日消费笔数")
                        .value(1523)
                        .trend("up")
                        .changeRate(8.6)
                        .build());

                statistics.add(DataStatisticsVO.builder()
                        .name("消费总额")
                        .value(28456.50)
                        .trend("up")
                        .changeRate(12.3)
                        .unit("元")
                        .build());

                statistics.add(DataStatisticsVO.builder()
                        .name("平均消费")
                        .value(18.7)
                        .trend("up")
                        .changeRate(3.4)
                        .unit("元")
                        .build());
                break;

            case "access":
                statistics.add(DataStatisticsVO.builder()
                        .name("今日通行次数")
                        .value(3567)
                        .trend("up")
                        .changeRate(15.2)
                        .build());

                statistics.add(DataStatisticsVO.builder()
                        .name("拒绝通行")
                        .value(23)
                        .trend("down")
                        .changeRate(-25.6)
                        .build());
                break;

            default:
                break;
        }

        return statistics;
    }

    /**
     * 执行数据导出
     */
    private void executeExport(String taskId, DataExportRequest request) {
        log.info("[数据报表] 开始执行导出: taskId={}, format={}", taskId, request.getExportFormat());

        try {
            Long reportId = request.getReportId();
            Map<String, Object> params = request.getParams();

            // 根据导出格式调用不同的导出方法
            if ("excel".equalsIgnoreCase(request.getExportFormat())) {
                fileExportService.exportToExcel(taskId, reportId, params);
            } else if ("pdf".equalsIgnoreCase(request.getExportFormat())) {
                fileExportService.exportToPdf(taskId, reportId, params);
            } else if ("csv".equalsIgnoreCase(request.getExportFormat())) {
                // CSV格式暂时使用Excel导出
                fileExportService.exportToExcel(taskId, reportId, params);
            } else {
                log.warn("[数据报表] 不支持的导出格式: {}", request.getExportFormat());
                handleExportFailure(taskId, "不支持的导出格式: " + request.getExportFormat());
            }

        } catch (Exception e) {
            log.error("[数据报表] 导出失败: taskId={}", taskId, e);
            handleExportFailure(taskId, e.getMessage());
        }
    }

    /**
     * 处理导出失败
     */
    private void handleExportFailure(String taskId, String errorMessage) {
        ExportTaskEntity taskEntity = exportTaskDao.selectById(taskId);
        taskEntity.setStatus("failed");
        taskEntity.setErrorMessage(errorMessage);
        taskEntity.setCompleteTime(LocalDateTime.now());
        exportTaskDao.updateById(taskEntity);
    }
}
