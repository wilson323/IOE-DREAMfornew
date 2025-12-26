# IOE-DREAM P1/P2级TODO分析与实施方案

> **文档版本**: v1.0.0
> **创建日期**: 2025-01-30
> **配套文档**: GLOBAL_TODO_COMPREHENSIVE_ANALYSIS.md

---

## 🎯 P1级重要TODO（建议实现）

### 模块1: 消费模块业务完善（3项）

#### TODO-008: 账户余额跨服务集成

**文件位置**:
- `ioedream-consume-service/src/main/java/net/lab1024/sa/consume/manager/SubsidyGrantManager.java`

**当前状态**:
```java
// TODO: 调用账户服务，增加余额
// TODO: 调用账户服务，扣减余额
```

**业务需求分析**:
1. **余额同步**: 补贴发放、消费扣款时，需要同步更新账户余额
2. **跨服务调用**: 消费服务调用账户服务的API更新余额
3. **事务一致性**: 使用SAGA模式确保分布式事务一致性

**业务流程图（SAGA模式）**:
```
补贴发放流程（正向操作）
┌────────────────────┐
│  1. 创建补贴记录   │
│     状态=PROCESSING │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  2. 调用账户服务   │
│     增加余额        │
└──────┬─────────────┘
       │
   ┌───┴───┐
   │       │
  成功    失败
   │       │
   ▼       ▼
完成    补偿操作
         │
         ▼
   ┌─────────────────┐
   │ 3. 更新补贴状态  │
   │    FAILED       │
   └─────────────────┘

补偿操作（逆向操作）
┌────────────────────┐
│  1. 标记补贴失败   │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  2. 通知管理员     │
└────────────────────┘
```

**技术实现方案**:

```java
// 1. SAGA协调器
@Service
@Slf4j
public class SubsidySagaCoordinator {

    @Resource
    private SubsidyGrantManager subsidyGrantManager;

    @Resource
    private AccountBalanceSagaSteps accountBalanceSagaSteps;

    /**
     * 执行补贴发放SAGA
     */
    @Transactional(rollbackFor = Exception.class)
    public SagaResult executeSubsidyGrant(SubsidyGrantForm form) {
        log.info("[SAGA协调] 开始补贴发放: userId={}, amount={}",
                 form.getUserId(), form.getAmount());

        SagaResult sagaResult = new SagaResult();
        List<SagaStep> executedSteps = new ArrayList<>();

        try {
            // 步骤1: 创建补贴记录
            SubsidyEntity subsidy = subsidyGrantManager.createSubsidy(form);
            executedSteps.add(() -> subsidyGrantManager.deleteSubsidy(subsidy.getSubsidyId()));

            // 步骤2: 调用账户服务增加余额
            accountBalanceSagaSteps.increaseBalance(
                subsidy.getUserId(),
                subsidy.getAmount(),
                subsidy.getSubsidyId()
            );
            executedSteps.add(() -> accountBalanceSagaSteps.decreaseBalance(
                subsidy.getUserId(),
                subsidy.getAmount(),
                subsidy.getSubsidyId()
            ));

            // 步骤3: 更新补贴状态为成功
            subsidyGrantManager.updateSubsidyStatus(
                subsidy.getSubsidyId(),
                SubsidyStatus.SUCCESS
            );

            sagaResult.setSuccess(true);
            log.info("[SAGA协调] 补贴发放成功: subsidyId={}", subsidy.getSubsidyId());

        } catch (Exception e) {
            log.error("[SAGA协调] 补贴发放失败，开始补偿: error={}", e.getMessage(), e);

            // 执行补偿操作
            compensate(executedSteps);

            sagaResult.setSuccess(false);
            sagaResult.setErrorMessage(e.getMessage());
        }

        return sagaResult;
    }

    /**
     * 补偿操作
     */
    private void compensate(List<SagaStep> executedSteps) {
        log.warn("[SAGA协调] 开始补偿操作, 步骤数={}", executedSteps.size());

        // 逆序执行补偿
        Collections.reverse(executedSteps);

        for (int i = 0; i < executedSteps.size(); i++) {
            SagaStep step = executedSteps.get(i);
            try {
                log.info("[SAGA协调] 执行补偿步骤 {}/{}", i + 1, executedSteps.size());
                step.compensate();
            } catch (Exception e) {
                log.error("[SAGA协调] 补偿步骤失败 {}/{}: error={}",
                         i + 1, executedSteps.size(), e.getMessage(), e);
                // 继续执行后续补偿
            }
        }

        log.warn("[SAGA协调] 补偿操作完成");
    }
}

// 2. 账户余额SAGA步骤
@Component
@Slf4j
public class AccountBalanceSagaSteps {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 增加余额（正向操作）
     */
    public void increaseBalance(Long userId, BigDecimal amount,
                               String businessId) {
        try {
            log.info("[SAGA步骤] 调用账户服务增加余额: userId={}, amount={}",
                     userId, amount);

            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("amount", amount);
            params.put("businessType", "SUBSIDY_GRANT");
            params.put("businessId", businessId);

            ResponseDTO<Void> response = gatewayServiceClient.callCommonService(
                "/api/account/increase-balance",
                HttpMethod.POST,
                params,
                Void.class
            );

            if (!response.isSuccessful()) {
                throw new BusinessException("INCREASE_BALANCE_FAILED",
                    "增加余额失败: " + response.getMessage());
            }

            log.info("[SAGA步骤] 余额增加成功: userId={}, amount={}", userId, amount);

        } catch (Exception e) {
            log.error("[SAGA步骤] 增加余额异常: userId={}, error={}",
                     userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 减少余额（补偿操作）
     */
    public void decreaseBalance(Long userId, BigDecimal amount,
                               String businessId) {
        try {
            log.info("[SAGA补偿] 调用账户服务减少余额: userId={}, amount={}",
                     userId, amount);

            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("amount", amount);
            params.put("businessType", "SUBSIDY_GRANT_COMPENSATE");
            params.put("businessId", businessId);

            ResponseDTO<Void> response = gatewayServiceClient.callCommonService(
                "/api/account/decrease-balance",
                HttpMethod.POST,
                params,
                Void.class
            );

            if (!response.isSuccessful()) {
                log.error("[SAGA补偿] 减少余额失败: userId={}, error={}",
                         userId, response.getMessage());
                // 补偿失败记录告警
                alertCompensationFailure(userId, amount, businessId);
            } else {
                log.info("[SAGA补偿] 余额减少成功: userId={}, amount={}", userId, amount);
            }

        } catch (Exception e) {
            log.error("[SAGA补偿] 减少余额异常: userId={}, error={}",
                     userId, e.getMessage(), e);
            alertCompensationFailure(userId, amount, businessId);
        }
    }

    /**
     * 告警补偿失败
     */
    private void alertCompensationFailure(Long userId, BigDecimal amount,
                                         String businessId) {
        log.error("[SAGA补偿] 补偿失败需要人工介入: userId={}, amount={}, businessId={}",
                 userId, amount, businessId);

        // 发送告警通知
        // TODO: 调用告警服务
    }
}

// 3. SAGA步骤接口
@FunctionalInterface
public interface SagaStep {
    void compensate();
}
```

**开发规范**:
- ✅ 使用SAGA模式保证分布式事务一致性
- ✅ 每个步骤都要实现补偿操作
- ✅ 补偿操作要按逆序执行
- ✅ 记录详细的SAGA执行日志
- ✅ 补偿失败要告警通知

**注意事项**:
1. **幂等性**: 所有操作要保证幂等性
2. **超时处理**: 跨服务调用要设置超时时间
3. **重试机制**: 失败操作要有合理的重试策略
4. **监控告警**: SAGA执行失败要及时告警

**验收标准**:
- ✅ 补贴发放成功，余额正确增加
- ✅ 发放失败时，补偿操作能正确回滚
- ✅ SAGA执行过程有完整日志
- ✅ 补偿失败有告警通知
- ✅ 单元测试覆盖率≥85%

---

#### TODO-009: 报表导出功能实现

**文件位置**:
- `ioedream-consume-service/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeReportServiceImpl.java`

**当前状态**:
```java
// TODO: 实际的报表导出逻辑
```

**业务需求分析**:
1. **多格式导出**: 支持Excel、PDF、CSV等多种格式
2. **大数据量**: 支持百万级数据导出，分批处理避免内存溢出
3. **异步处理**: 大数据量导出使用异步方式，完成后通知用户下载
4. **模板管理**: 使用预定义模板，确保导出格式统一

**业务流程图**:
```
┌────────────────────┐
│  用户点击导出      │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  查询导出数据量    │
└──────┬─────────────┘
       │
   ┌───┴───┐
   │       │
  小量大   大批量
   │       │
   ▼       ▼
同步导出  异步导出
   │       │
   │       ▼
   │   ┌────────────────┐
   │   │ 创建导出任务    │
   │   │ 状态=PROCESSING │
   │   └────┬───────────┘
   │        │
   │        ▼
   │   ┌────────────────┐
   │   │ 分批查询数据    │
   │   │ 每批10000条    │
   │   └────┬───────────┘
   │        │
   │        ▼
   │   ┌────────────────┐
   │   │ 分批写入文件    │
   │   │ 避免内存溢出    │
   │   └────┬───────────┘
   │        │
   │        ▼
   │   ┌────────────────┐
   │   │ 上传到文件服务  │
   │   └────┬───────────┘
   │        │
   │        ▼
   │   ┌────────────────┐
   │   │ 更新任务状态    │
   │   │ SUCCESS        │
   │   └────┬───────────┘
   │        │
   │        ▼
   │   ┌────────────────┐
   │   │ 通知用户下载    │
   │   └────────────────┘
   │
   ▼
┌────────────────────┐
│  返回导出文件      │
└────────────────────┘
```

**技术实现方案**:

```java
// 1. 报表导出服务
@Service
@Slf4j
public class ConsumeReportServiceImpl implements ConsumeReportService {

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private ExportTaskManager exportTaskManager;

    @Resource
    private ExcelExportService excelExportService;

    @Resource
    private PdfExportService pdfExportService;

    /**
     * 导出消费报表（同步）
     */
    @Override
    public ExportResult exportReportSync(ConsumeReportQueryForm form) {
        log.info("[报表导出] 同步导出开始: form={}", form);

        try {
            // 1. 查询数据量
            Long totalCount = consumeRecordDao.countByQuery(form);

            if (totalCount == null || totalCount == 0) {
                log.warn("[报表导出] 无数据可导出");
                return ExportResult.empty();
            }

            // 2. 判断是否异步导出（超过10000条）
            if (totalCount > 10000) {
                log.info("[报表导出] 数据量较大({}), 转为异步导出", totalCount);
                return exportReportAsync(form, totalCount);
            }

            // 3. 同步导出（小数据量）
            List<ConsumeRecordEntity> records = consumeRecordDao.selectByQuery(form);

            // 4. 根据格式导出
            byte[] fileData;
            String fileExtension;

            if ("excel".equalsIgnoreCase(form.getExportFormat())) {
                fileData = excelExportService.exportConsumeRecords(records);
                fileExtension = "xlsx";

            } else if ("pdf".equalsIgnoreCase(form.getExportFormat())) {
                fileData = pdfExportService.exportConsumeRecords(records);
                fileExtension = "pdf";

            } else {
                throw new BusinessException("UNSUPPORTED_FORMAT",
                    "不支持的导出格式: " + form.getExportFormat());
            }

            // 5. 构建导出结果
            ExportResult result = new ExportResult();
            result.setFileData(fileData);
            result.setFileName("消费报表_" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "." + fileExtension);
            result.setFileSize(fileData.length);
            result.setExportType("SYNC");

            log.info("[报表导出] 同步导出完成: fileName={}, size={}",
                     result.getFileName(), result.getFileSize());

            return result;

        } catch (Exception e) {
            log.error("[报表导出] 同步导出异常: error={}", e.getMessage(), e);
            throw new SystemException("EXPORT_FAILED", "导出失败", e);
        }
    }

    /**
     * 导出消费报表（异步）
     */
    @Override
    public ExportResult exportReportAsync(ConsumeReportQueryForm form, Long totalCount) {
        log.info("[报表导出] 异步导出开始: totalCount={}", totalCount);

        try {
            // 1. 创建导出任务
            ExportTaskEntity task = new ExportTaskEntity();
            task.setTaskName("消费报表导出");
            task.setTaskType("CONSUME_REPORT");
            task.setExportFormat(form.getExportFormat());
            task.setStatus("PROCESSING");
            task.setTotalCount(totalCount);
            task.setProcessedCount(0L);
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            exportTaskManager.save(task);

            log.info("[报表导出] 导出任务已创建: taskId={}", task.getTaskId());

            // 2. 异步执行导出
            CompletableFuture.runAsync(() -> {
                executeExportTask(task, form);
            });

            // 3. 返回任务信息
            ExportResult result = new ExportResult();
            result.setTaskId(task.getTaskId());
            result.setStatus("PROCESSING");
            result.setMessage("导出任务已创建，请稍后下载");
            result.setExportType("ASYNC");

            log.info("[报表导出] 异步导出任务已提交: taskId={}", task.getTaskId());

            return result;

        } catch (Exception e) {
            log.error("[报表导出] 异步导出异常: error={}", e.getMessage(), e);
            throw new SystemException("EXPORT_ASYNC_FAILED", "创建异步导出任务失败", e);
        }
    }

    /**
     * 执行导出任务
     */
    private void executeExportTask(ExportTaskEntity task, ConsumeReportQueryForm form) {
        log.info("[报表导出] 开始执行导出任务: taskId={}", task.getTaskId());

        String tempFilePath = null;

        try {
            // 1. 准备导出参数
            form.setPageSize(10000); // 每批10000条
            Long totalCount = task.getTotalCount();
            int totalPages = (int) Math.ceil(totalCount / (double) form.getPageSize());

            log.info("[报表导出] 数据分批: totalCount={}, totalPages={}",
                     totalCount, totalPages);

            // 2. 创建临时文件
            tempFilePath = createTempFile(task.getTaskId(), task.getExportFormat());

            // 3. 分批查询和写入
            try (OutputStream outputStream = new FileOutputStream(tempFilePath)) {

                for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                    log.info("[报表导出] 正在处理第 {}/{} 批", pageNum, totalPages);

                    // 查询当前批次数据
                    form.setPageNum(pageNum);
                    List<ConsumeRecordEntity> batchRecords = consumeRecordDao.selectByQuery(form);

                    // 写入文件
                    if ("excel".equalsIgnoreCase(task.getExportFormat())) {
                        excelExportService.writeBatch(outputStream, batchRecords, pageNum == 1);
                    } else if ("pdf".equalsIgnoreCase(task.getExportFormat())) {
                        pdfExportService.writeBatch(outputStream, batchRecords, pageNum == 1);
                    }

                    // 更新进度
                    exportTaskManager.updateProgress(task.getTaskId(),
                        (long) batchRecords.size());

                    log.info("[报表导出] 第 {}/{} 批处理完成", pageNum, totalPages);
                }

                // 完成写入
                outputStream.flush();
            }

            // 4. 上传文件到文件服务
            String fileUrl = uploadFileToStorage(tempFilePath, task.getTaskId());

            // 5. 更新任务状态为成功
            exportTaskManager.updateTaskStatus(task.getTaskId(), "SUCCESS", fileUrl);

            log.info("[报表导出] 导出任务执行成功: taskId={}, fileUrl={}",
                     task.getTaskId(), fileUrl);

        } catch (Exception e) {
            log.error("[报表导出] 导出任务执行失败: taskId={}, error={}",
                     task.getTaskId(), e.getMessage(), e);

            // 更新任务状态为失败
            exportTaskManager.updateTaskStatus(task.getTaskId(), "FAILED", null);

        } finally {
            // 删除临时文件
            if (tempFilePath != null) {
                try {
                    Files.deleteIfExists(Paths.get(tempFilePath));
                    log.debug("[报表导出] 临时文件已删除: {}", tempFilePath);
                } catch (Exception e) {
                    log.warn("[报表导出] 删除临时文件失败: {}", tempFilePath, e);
                }
            }
        }
    }

    /**
     * 创建临时文件
     */
    private String createTempFile(String taskId, String format) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = "export_" + taskId + "_" +
                         System.currentTimeMillis() + "." + format;
        Path tempFilePath = Paths.get(tempDir, fileName);

        Files.createFile(tempFilePath);

        log.debug("[报表导出] 临时文件已创建: {}", tempFilePath);
        return tempFilePath.toString();
    }

    /**
     * 上传文件到存储服务
     */
    private String uploadFileToStorage(String filePath, String taskId) {
        try {
            log.info("[报表导出] 上传文件到存储: filePath={}", filePath);

            File file = new File(filePath);
            String fileName = "消费报表_" + taskId + "_" +
                             System.currentTimeMillis() + "." +
                             FilenameUtils.getExtension(filePath);

            // 调用文件服务上传
            // 这里简化处理，实际应该通过GatewayServiceClient调用文件服务

            String fileUrl = "/files/" + fileName; // 模拟URL

            log.info("[报表导出] 文件上传成功: fileUrl={}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("[报表导出] 上传文件异常: error={}", e.getMessage(), e);
            throw new SystemException("UPLOAD_FILE_FAILED", "上传文件失败", e);
        }
    }
}

// 2. Excel导出服务
@Service
@Slf4j
public class ExcelExportService {

    @Resource
    private ExcelTemplateManager excelTemplateManager;

    /**
     * 导出消费记录到Excel（同步）
     */
    public byte[] exportConsumeRecords(List<ConsumeRecordEntity> records) {
        try {
            log.info("[Excel导出] 开始导出: recordCount={}", records.size());

            // 1. 创建工作簿
            try (Workbook workbook = new SXSSFWorkbook(1000)) {

                // 2. 获取模板
                Sheet sheet = workbook.createSheet("消费记录");

                // 3. 写入表头
                writeHeader(sheet);

                // 4. 写入数据
                writeData(sheet, records);

                // 5. 写入到字节数组
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    workbook.write(outputStream);

                    byte[] result = outputStream.toByteArray();
                    log.info("[Excel导出] 导出完成: size={}", result.length);
                    return result;
                }
            }

        } catch (Exception e) {
            log.error("[Excel导出] 导出异常: error={}", e.getMessage(), e);
            throw new SystemException("EXCEL_EXPORT_FAILED", "Excel导出失败", e);
        }
    }

    /**
     * 分批写入Excel（异步）
     */
    public void writeBatch(OutputStream outputStream,
                          List<ConsumeRecordEntity> batchRecords,
                          boolean isFirstBatch) {
        try {
            log.debug("[Excel导出] 写入批次: isFirstBatch={}, size={}",
                     isFirstBatch, batchRecords.size());

            // TODO: 实现分批写入逻辑
            // 这里需要维护SXSSFWorkbook的状态

        } catch (Exception e) {
            log.error("[Excel导出] 批次写入异常: error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 写入表头
     */
    private void writeHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);

        String[] headers = {
            "消费ID", "用户ID", "用户名", "消费金额", "消费时间",
            "消费类型", "设备ID", "区域ID", "状态", "创建时间"
        };

        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * 写入数据
     */
    private void writeData(Sheet sheet, List<ConsumeRecordEntity> records) {
        int rowNum = 1;

        for (ConsumeRecordEntity record : records) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(record.getConsumeId());
            row.createCell(1).setCellValue(record.getUserId());
            row.createCell(2).setCellValue(record.getUserName());
            row.createCell(3).setCellValue(record.getAmount().doubleValue());
            row.createCell(4).setCellValue(record.getConsumeTime().toString());
            row.createCell(5).setCellValue(record.getConsumeType());
            row.createCell(6).setCellValue(record.getDeviceId());
            row.createCell(7).setCellValue(record.getAreaId());
            row.createCell(8).setCellValue(record.getStatus());
            row.createCell(9).setCellValue(record.getCreateTime().toString());
        }
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        // 背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }
}

// 3. 导出任务Manager
@Component
@Slf4j
public class ExportTaskManager {

    @Resource
    private ExportTaskDao exportTaskDao;

    /**
     * 保存导出任务
     */
    public ExportTaskEntity save(ExportTaskEntity task) {
        exportTaskDao.insert(task);
        return task;
    }

    /**
     * 更新任务进度
     */
    public void updateProgress(Long taskId, Long processedCount) {
        try {
            ExportTaskEntity task = new ExportTaskEntity();
            task.setTaskId(taskId);
            task.setProcessedCount(processedCount);
            task.setUpdateTime(LocalDateTime.now());

            exportTaskDao.updateById(task);

        } catch (Exception e) {
            log.error("[导出任务] 更新进度异常: taskId={}, error={}",
                     taskId, e.getMessage(), e);
        }
    }

    /**
     * 更新任务状态
     */
    public void updateTaskStatus(Long taskId, String status, String fileUrl) {
        try {
            ExportTaskEntity task = new ExportTaskEntity();
            task.setTaskId(taskId);
            task.setStatus(status);
            task.setFileUrl(fileUrl);
            task.setUpdateTime(LocalDateTime.now());

            if ("SUCCESS".equals(status)) {
                task.setFinishedTime(LocalDateTime.now());
            }

            exportTaskDao.updateById(task);

            log.info("[导出任务] 任务状态已更新: taskId={}, status={}", taskId, status);

        } catch (Exception e) {
            log.error("[导出任务] 更新状态异常: taskId={}, error={}",
                     taskId, e.getMessage(), e);
        }
    }
}
```

**开发规范**:
- ✅ 小数据量（<10000条）同步导出
- ✅ 大数据量（≥10000条）异步导出
- ✅ 使用流式处理避免内存溢出
- ✅ 导出完成后删除临时文件
- ✅ 异步导出要记录任务进度

**注意事项**:
1. **内存管理**: 大数据量导出使用SXSSFWorkbook，避免OOM
2. **临时文件**: 导出过程中的临时文件要及时清理
3. **进度通知**: 异步导出要实时更新进度
4. **文件存储**: 导出文件要上传到文件存储服务
5. **权限控制**: 导出功能要验证用户权限

**验收标准**:
- ✅ 支持Excel、PDF格式导出
- ✅ 支持大数据量异步导出
- ✅ 导出进度实时更新
- ✅ 导出完成后能正常下载
- ✅ 单元测试覆盖率≥80%

---

#### TODO-010: 消费统计功能实现

**文件位置**:
- `ioedream-consume-service/src/main/java/net/lab1024/sa/consume/statistics/ConsumeStatisticsService.java`

**当前状态**:
```java
// TODO: 实现统计逻辑
// 1. 查询消费记录总数
// 2. 统计消费金额
// 3. 按类型统计
// 4. 返回统计结果
```

**业务需求分析**:
1. **多维度统计**: 按时间、区域、类型等多个维度统计
2. **聚合计算**: 支持SUM、AVG、COUNT、MAX、MIN等聚合函数
3. **趋势分析**: 支持同比、环比等趋势计算
4. **缓存优化**: 统计结果缓存，避免重复计算

**业务流程图**:
```
┌────────────────────┐
│  统计请求          │
│  - 时间范围        │
│  - 维度            │
│  - 指标            │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  检查缓存          │
└──────┬─────────────┘
       │
   ┌───┴───┐
   │       │
  命中    未命中
   │       │
   ▼       ▼
返回缓存  查询数据库
          │
          ▼
   ┌────────────────┐
   │ 按维度分组统计  │
   │ - GROUP BY     │
   │ - 聚合函数     │
   └────┬───────────┘
          │
          ▼
   ┌────────────────┐
   │ 计算趋势指标    │
   │ - 同比          │
   │ - 环比          │
   └────┬───────────┘
          │
          ▼
   ┌────────────────┐
   │ 存入缓存        │
   │ 5分钟过期       │
   └────┬───────────┘
          │
          ▼
┌────────────────────┐
│  返回统计结果      │
└────────────────────┘
```

**技术实现方案**:

```java
// 1. 消费统计服务
@Service
@Slf4j
public class ConsumeStatisticsService {

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private ConsumeStatisticsCacheManager statisticsCacheManager;

    /**
     * 统计消费数据（按时间范围）
     */
    public ConsumeStatisticsVO statisticsByTimeRange(LocalDateTime startTime,
                                                     LocalDateTime endTime) {
        log.info("[消费统计] 按时间范围统计: startTime={}, endTime={}",
                 startTime, endTime);

        try {
            // 1. 检查缓存
            String cacheKey = buildCacheKey("time_range", startTime, endTime);
            ConsumeStatisticsVO cachedResult = statisticsCacheManager.get(cacheKey);
            if (cachedResult != null) {
                log.info("[消费统计] 缓存命中: cacheKey={}", cacheKey);
                return cachedResult;
            }

            // 2. 查询统计数据
            ConsumeStatisticsVO statistics = new ConsumeStatisticsVO();

            // 2.1 总消费次数
            Long totalCount = consumeRecordDao.countByTimeRange(startTime, endTime);
            statistics.setTotalCount(totalCount);

            // 2.2 总消费金额
            BigDecimal totalAmount = consumeRecordDao.sumAmountByTimeRange(startTime, endTime);
            statistics.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);

            // 2.3 平均消费金额
            BigDecimal avgAmount = totalCount != null && totalCount > 0 ?
                statistics.getTotalAmount().divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
            statistics.setAvgAmount(avgAmount);

            // 2.4 按消费类型统计
            List<TypeStatisticsVO> typeStatistics =
                consumeRecordDao.statisticsByType(startTime, endTime);
            statistics.setTypeStatistics(typeStatistics);

            // 2.5 按区域统计
            List<AreaStatisticsVO> areaStatistics =
                consumeRecordDao.statisticsByArea(startTime, endTime);
            statistics.setAreaStatistics(areaStatistics);

            // 2.6 按时间段统计（小时）
            List<TimeSlotStatisticsVO> timeSlotStatistics =
                consumeRecordDao.statisticsByTimeSlot(startTime, endTime);
            statistics.setTimeSlotStatistics(timeSlotStatistics);

            // 2.7 趋势分析（同比、环比）
            calculateTrend(statistics, startTime, endTime);

            // 3. 存入缓存（5分钟）
            statisticsCacheManager.set(cacheKey, statistics, 300);

            log.info("[消费统计] 统计完成: totalCount={}, totalAmount={}",
                     totalCount, totalAmount);

            return statistics;

        } catch (Exception e) {
            log.error("[消费统计] 统计异常: error={}", e.getMessage(), e);
            throw new SystemException("STATISTICS_FAILED", "统计失败", e);
        }
    }

    /**
     * 统计用户消费数据
     */
    public UserConsumeStatisticsVO statisticsByUser(Long userId,
                                                    LocalDateTime startTime,
                                                    LocalDateTime endTime) {
        log.info("[消费统计] 按用户统计: userId={}, startTime={}, endTime={}",
                 userId, startTime, endTime);

        try {
            UserConsumeStatisticsVO statistics = new UserConsumeStatisticsVO();
            statistics.setUserId(userId);
            statistics.setStartTime(startTime);
            statistics.setEndTime(endTime);

            // 1. 查询用户消费记录
            List<ConsumeRecordEntity> records =
                consumeRecordDao.selectByUserAndTimeRange(userId, startTime, endTime);

            // 2. 统计数据
            statistics.setTotalCount((long) records.size());

            BigDecimal totalAmount = records.stream()
                .map(ConsumeRecordEntity::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.setTotalAmount(totalAmount);

            // 3. 平均消费
            if (!records.isEmpty()) {
                statistics.setAvgAmount(totalAmount.divide(
                    new BigDecimal(records.size()), 2, RoundingMode.HALF_UP));
            } else {
                statistics.setAvgAmount(BigDecimal.ZERO);
            }

            // 4. 最大消费、最小消费
            statistics.setMaxAmount(records.stream()
                .map(ConsumeRecordEntity::getAmount)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO));

            statistics.setMinAmount(records.stream()
                .map(ConsumeRecordEntity::getAmount)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO));

            // 5. 按消费类型统计
            Map<String, List<ConsumeRecordEntity>> groupedByType =
                records.stream()
                    .collect(Collectors.groupingBy(ConsumeRecordEntity::getConsumeType));

            List<UserTypeStatisticsVO> typeStatistics = groupedByType.entrySet()
                .stream()
                .map(entry -> {
                    UserTypeStatisticsVO typeStat = new UserTypeStatisticsVO();
                    typeStat.setConsumeType(entry.getKey());
                    typeStat.setCount((long) entry.getValue().size());
                    typeStat.setAmount(entry.getValue().stream()
                        .map(ConsumeRecordEntity::getAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return typeStat;
                })
                .collect(Collectors.toList());

            statistics.setTypeStatistics(typeStatistics);

            // 6. 消费趋势（按天）
            calculateUserTrend(statistics, records);

            log.info("[消费统计] 用户统计完成: userId={}, totalCount={}",
                     userId, statistics.getTotalCount());

            return statistics;

        } catch (Exception e) {
            log.error("[消费统计] 用户统计异常: userId={}, error={}",
                     userId, e.getMessage(), e);
            throw new SystemException("USER_STATISTICS_FAILED", "用户统计失败", e);
        }
    }

    /**
     * 统计区域消费数据
     */
    public AreaConsumeStatisticsVO statisticsByArea(Long areaId,
                                                    LocalDateTime startTime,
                                                    LocalDateTime endTime) {
        log.info("[消费统计] 按区域统计: areaId={}, startTime={}, endTime={}",
                 areaId, startTime, endTime);

        try {
            // 类似用户统计逻辑
            // TODO: 实现区域统计逻辑
            return new AreaConsumeStatisticsVO();

        } catch (Exception e) {
            log.error("[消费统计] 区域统计异常: areaId={}, error={}",
                     areaId, e.getMessage(), e);
            throw new SystemException("AREA_STATISTICS_FAILED", "区域统计失败", e);
        }
    }

    /**
     * 计算趋势指标（同比、环比）
     */
    private void calculateTrend(ConsumeStatisticsVO statistics,
                               LocalDateTime startTime,
                               LocalDateTime endTime) {
        try {
            Duration duration = Duration.between(startTime, endTime);
            long days = duration.toDays();

            // 1. 计算环比（上一个周期）
            LocalDateTime prevStartTime = startTime.minus(days, ChronoUnit.DAYS);
            LocalDateTime prevEndTime = endTime.minus(days, ChronoUnit.DAYS);

            ConsumeStatisticsVO prevStatistics = buildBaseStatistics(prevStartTime, prevEndTime);

            // 环比增长率
            if (prevStatistics.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal momRate = statistics.getTotalAmount()
                    .subtract(prevStatistics.getTotalAmount())
                    .divide(prevStatistics.getTotalAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
                statistics.setMomRate(momRate);
            }

            // 2. 计算同比（去年同期）
            LocalDateTime lastYearStartTime = startTime.minus(1, ChronoUnit.YEARS);
            LocalDateTime lastYearEndTime = endTime.minus(1, ChronoUnit.YEARS);

            ConsumeStatisticsVO lastYearStatistics =
                buildBaseStatistics(lastYearStartTime, lastYearEndTime);

            // 同比增长率
            if (lastYearStatistics.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal yoyRate = statistics.getTotalAmount()
                    .subtract(lastYearStatistics.getTotalAmount())
                    .divide(lastYearStatistics.getTotalAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
                statistics.setYoyRate(yoyRate);
            }

            log.debug("[消费统计] 趋势计算完成: momRate={}, yoyRate={}",
                     statistics.getMomRate(), statistics.getYoyRate());

        } catch (Exception e) {
            log.error("[消费统计] 趋势计算异常: error={}", e.getMessage(), e);
        }
    }

    /**
     * 计算用户消费趋势
     */
    private void calculateUserTrend(UserConsumeStatisticsVO statistics,
                                   List<ConsumeRecordEntity> records) {
        try {
            // 按天分组统计
            Map<LocalDate, List<ConsumeRecordEntity>> groupedByDate =
                records.stream()
                    .collect(Collectors.groupingBy(
                        record -> record.getConsumeTime().toLocalDate()
                    ));

            List<DailyTrendVO> dailyTrends = groupedByDate.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    DailyTrendVO trend = new DailyTrendVO();
                    trend.setDate(entry.getKey());
                    trend.setCount((long) entry.getValue().size());
                    trend.setAmount(entry.getValue().stream()
                        .map(ConsumeRecordEntity::getAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return trend;
                })
                .collect(Collectors.toList());

            statistics.setDailyTrends(dailyTrends);

        } catch (Exception e) {
            log.error("[消费统计] 用户趋势计算异常: error={}", e.getMessage(), e);
        }
    }

    /**
     * 构建基础统计数据
     */
    private ConsumeStatisticsVO buildBaseStatistics(LocalDateTime startTime,
                                                    LocalDateTime endTime) {
        ConsumeStatisticsVO statistics = new ConsumeStatisticsVO();

        Long totalCount = consumeRecordDao.countByTimeRange(startTime, endTime);
        statistics.setTotalCount(totalCount);

        BigDecimal totalAmount = consumeRecordDao.sumAmountByTimeRange(startTime, endTime);
        statistics.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);

        return statistics;
    }

    /**
     * 构建缓存key
     */
    private String buildCacheKey(String type, Object... params) {
        StringBuilder keyBuilder = new StringBuilder("consume:statistics:");
        keyBuilder.append(type);

        for (Object param : params) {
            keyBuilder.append(":").append(param);
        }

        return keyBuilder.toString();
    }
}

// 2. DAO层统计方法
@Mapper
public interface ConsumeRecordDao extends BaseMapper<ConsumeRecordEntity> {

    /**
     * 统计指定时间范围内的记录数
     */
    Long countByTimeRange(@Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的总金额
     */
    BigDecimal sumAmountByTimeRange(@Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 按消费类型统计
     */
    List<TypeStatisticsVO> statisticsByType(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 按区域统计
     */
    List<AreaStatisticsVO> statisticsByArea(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 按时间段统计（小时）
     */
    List<TimeSlotStatisticsVO> statisticsByTimeSlot(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户在时间范围内的消费记录
     */
    List<ConsumeRecordEntity> selectByUserAndTimeRange(@Param("userId") Long userId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);
}
```

**开发规范**:
- ✅ 使用缓存避免重复计算
- ✅ 统计查询使用SQL聚合函数，提高性能
- ✅ 趋势计算要处理边界情况（除零等）
- ✅ 缓存过期时间要合理（5分钟）
- ✅ 大数据量统计要分页处理

**注意事项**:
1. **性能优化**: 复杂统计查询要创建索引
2. **缓存策略**: 统计结果缓存但不过期时间要短
3. **精度处理**: 金额计算要使用BigDecimal，注意精度
4. **空值处理**: 聚合结果可能为null，要判空
5. **时区问题**: 时间范围统计要注意时区

**验收标准**:
- ✅ 统计结果准确无误
- ✅ 支持多维度统计
- ✅ 趋势计算正确
- ✅ 缓存机制生效
- ✅ 单元测试覆盖率≥85%

---

## 📊 P2级优化TODO（可选实现）

### 模块1: 智能推荐算法（3项）

#### TODO-011: 基于历史偏好的推荐

**文件位置**:
- `ioedream-consume-service/src/main/java/net/lab1024/sa/consume/strategy/impl/IntelligenceStrategy.java:124`

**当前状态**:
```java
// TODO: 实现基于历史偏好的推荐逻辑
```

**业务需求分析**:
1. **个性化推荐**: 根据用户历史消费记录，推荐符合其偏好的商品
2. **协同过滤**: 使用协同过滤算法实现推荐
3. **实时更新**: 用户消费后，实时更新推荐结果
4. **冷启动处理**: 新用户无历史记录时的推荐策略

**技术实现方案（简略）**:
- 使用协同过滤算法（用户-商品矩阵）
- 计算用户相似度（余弦相似度）
- 推荐相似用户喜欢的商品
- 新用户使用热门商品推荐

---

## 📈 实施优先级建议

### 第一阶段（2-3周）- P0级必须实现
1. JWT/Sa-Token解析器集成
2. 用户锁定状态数据库更新
3. 用户锁定通知服务集成
4. 生物识别逻辑实现
5. WebSocket实时推送和RabbitMQ消息
6. 临时访客中心验证逻辑
7. 常客边缘验证逻辑

### 第二阶段（3-4周）- P1级建议实现
1. 账户余额跨服务集成
2. 报表导出功能实现
3. 消费统计功能实现

### 第三阶段（2-3周）- P2级可选实现
1. 智能推荐算法优化
2. 性能优化
3. 代码质量提升

---

## 📝 开发规范总结

### 统一开发规范
1. **依赖注入**: 使用`@Resource`注解
2. **日志记录**: 使用`@Slf4j` + `log.info("[模块名] 消息: params={}", params)`
3. **异常处理**: 区分BusinessException和SystemException
4. **返回格式**: Service返回标准类型，Controller包装ResponseDTO
5. **缓存策略**: 合理使用缓存，设置过期时间
6. **异步处理**: 使用`@Async`或`CompletableFuture`
7. **事务管理**: 使用`@Transactional`，明确rollbackFor

### 安全规范
1. **敏感信息**: 日志中禁止记录密码、token等
2. **权限验证**: 所有接口都要验证用户权限
3. **参数校验**: 使用`@Valid`校验请求参数
4. **SQL注入**: 使用MyBatis-Plus，禁止SQL拼接

### 性能规范
1. **批量操作**: 使用批量插入/更新，减少数据库交互
2. **分页查询**: 大数据量查询必须分页
3. **索引优化**: 常用查询字段要创建索引
4. **缓存优化**: 热点数据要缓存

---

## 🔍 验收标准清单

每个TODO实现后必须满足：

### 功能验收
- ✅ 功能实现符合需求文档
- ✅ 业务逻辑正确无误
- ✅ 边界情况处理完善

### 技术验收
- ✅ 代码符合开发规范
- ✅ 日志记录完整清晰
- ✅ 异常处理合理
- ✅ 单元测试覆盖率≥80%

### 性能验收
- ✅ 接口响应时间<2s
- ✅ 数据库查询优化
- ✅ 缓存策略合理

### 安全验收
- ✅ 权限验证完整
- ✅ 参数校验完善
- ✅ 敏感信息脱敏

---

## 📚 相关文档引用

- **CLAUDE.md**: 项目全局架构规范
- **GLOBAL_TODO_ENTERPRISE_ACTION_PLAN.md**: TODO执行计划
- **四层架构详解**: 架构设计文档
- **API开发规范**: API设计标准

---

**文档维护**: 本文档应随TODO实现进度持续更新
**最后更新**: 2025-01-30
**维护人**: IOE-DREAM架构委员会
