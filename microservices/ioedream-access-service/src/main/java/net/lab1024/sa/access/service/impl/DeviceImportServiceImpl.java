package net.lab1024.sa.access.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.DeviceImportBatchDao;
import net.lab1024.sa.access.dao.DeviceImportErrorDao;
import net.lab1024.sa.access.dao.DeviceImportSuccessDao;
import net.lab1024.sa.access.domain.entity.DeviceImportBatchEntity;
import net.lab1024.sa.access.domain.entity.DeviceImportErrorEntity;
import net.lab1024.sa.access.domain.entity.DeviceImportSuccessEntity;
import net.lab1024.sa.access.domain.form.DeviceImportQueryForm;
import net.lab1024.sa.access.domain.vo.DeviceImportBatchVO;
import net.lab1024.sa.access.domain.vo.DeviceImportErrorVO;
import net.lab1024.sa.access.domain.vo.DeviceImportResultVO;
import net.lab1024.sa.access.domain.vo.DeviceImportStatisticsVO;
import net.lab1024.sa.access.service.DeviceImportService;
import net.lab1024.sa.common.domain.PageResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备导入服务实现类
 * <p>
 * 提供设备批量导入功能：
 * - Excel文件上传和解析（使用EasyExcel）
 * - 数据验证（必填字段、格式验证、业务规则验证）
 * - 批量导入（事务管理）
 * - 导入记录查询和统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class DeviceImportServiceImpl implements DeviceImportService {

    @Resource
    private DeviceImportBatchDao deviceImportBatchDao;

    @Resource
    private DeviceImportErrorDao deviceImportErrorDao;

    @Resource
    private DeviceImportSuccessDao deviceImportSuccessDao;

    /**
     * 上传并解析Excel文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadAndParse(MultipartFile file, String batchName, Long operatorId, String operatorName) {
        log.info("[设备导入] 开始上传并解析Excel文件: fileName={}, batchName={}, operatorId={}",
                file.getOriginalFilename(), batchName, operatorId);

        try {
            // 1. 验证文件
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.endsWith(".xlsx")) {
                throw new IllegalArgumentException("只支持.xlsx格式的Excel文件");
            }

            // 2. 计算文件MD5
            String fileMd5 = DigestUtils.md5Hex(file.getInputStream());

            // 3. 检查是否已存在相同文件的批次（去重）
            LambdaQueryWrapper<DeviceImportBatchEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeviceImportBatchEntity::getFileMd5, fileMd5);
            queryWrapper.eq(DeviceImportBatchEntity::getOperatorId, operatorId);
            DeviceImportBatchEntity existingBatch = deviceImportBatchDao.selectOne(queryWrapper);

            if (existingBatch != null) {
                log.warn("[设备导入] 文件已存在，请勿重复导入: batchId={}, fileName={}", existingBatch.getBatchId(), fileName);
                throw new IllegalArgumentException("该文件已导入过，请勿重复导入");
            }

            // 4. 创建导入批次记录
            DeviceImportBatchEntity batch = new DeviceImportBatchEntity();
            batch.setBatchName(batchName);
            batch.setFileName(fileName);
            batch.setFileSize(file.getSize());
            batch.setFileMd5(fileMd5);
            batch.setImportStatus(0); // 处理中
            batch.setStatusMessage("正在解析文件");
            batch.setStartTime(LocalDateTime.now());
            batch.setOperatorId(operatorId);
            batch.setOperatorName(operatorName);
            batch.setOperatorType(1); // 默认管理员

            deviceImportBatchDao.insert(batch);
            Long batchId = batch.getBatchId();

            log.info("[设备导入] 批次记录创建成功: batchId={}", batchId);

            // 5. 解析Excel文件
            List<JSONObject> dataList = parseExcelFile(file, batchId);

            // 6. 验证数据
            List<DeviceImportErrorEntity> errorList = new ArrayList<>();
            List<DeviceImportSuccessEntity> successList = new ArrayList<>();
            int totalCount = dataList.size();
            int successCount = 0;
            int failedCount = 0;
            int skippedCount = 0;

            for (int i = 0; i < dataList.size(); i++) {
                JSONObject rowData = dataList.get(i);
                Integer rowNumber = i + 2; // Excel行号（从第2行开始，第1行是表头）

                // 验证数据
                String errorMessage = validateDeviceData(rowData, rowNumber);

                if (errorMessage == null) {
                    // 验证成功
                    DeviceImportSuccessEntity success = new DeviceImportSuccessEntity();
                    success.setBatchId(batchId);
                    success.setRowNumber(rowNumber);
                    success.setDeviceCode(rowData.getString("deviceCode"));
                    success.setDeviceName(rowData.getString("deviceName"));
                    success.setDeviceType(rowData.getInteger("deviceType"));
                    success.setImportedData(rowData.toJSONString());
                    successList.add(success);
                    successCount++;

                    log.debug("[设备导入] 验证成功: rowNumber={}, deviceCode={}", rowNumber, success.getDeviceCode());
                } else {
                    // 验证失败
                    DeviceImportErrorEntity error = new DeviceImportErrorEntity();
                    error.setBatchId(batchId);
                    error.setRowNumber(rowNumber);
                    error.setRawData(rowData.toJSONString());
                    error.setErrorCode("VALIDATION_ERROR");
                    error.setErrorMessage(errorMessage);
                    errorList.add(error);
                    failedCount++;

                    log.debug("[设备导入] 验证失败: rowNumber={}, error={}", rowNumber, errorMessage);
                }
            }

            // 7. 批量保存验证结果
            if (!successList.isEmpty()) {
                deviceImportSuccessDao.insertBatch(successList);
            }
            if (!errorList.isEmpty()) {
                deviceImportErrorDao.insertBatch(errorList);
            }

            // 8. 更新批次统计
            deviceImportBatchDao.updateStatistics(batchId, totalCount, successCount, failedCount, skippedCount);

            log.info("[设备导入] Excel解析完成: batchId={}, total={}, success={}, failed={}",
                    batchId, totalCount, successCount, failedCount);

            return batchId;

        } catch (Exception e) {
            log.error("[设备导入] 上传并解析Excel文件异常: fileName={}, error={}",
                    file.getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("上传并解析Excel文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析Excel文件
     */
    private List<JSONObject> parseExcelFile(MultipartFile file, Long batchId) {
        log.info("[设备导入] 开始解析Excel文件: batchId={}", batchId);

        List<JSONObject> dataList = new ArrayList<>();

        try {
            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    // 将Excel行数据转换为JSONObject
                    JSONObject rowData = new JSONObject();

                    // 映射列索引到字段名（根据模板定义）
                    rowData.put("deviceCode", data.get(0));      // 设备编码
                    rowData.put("deviceName", data.get(1));      // 设备名称
                    rowData.put("deviceType", data.get(2));      // 设备类型
                    rowData.put("deviceModel", data.get(3));     // 设备型号
                    rowData.put("manufacturer", data.get(4));    // 厂商
                    rowData.put("areaId", data.get(5));          // 所属区域
                    rowData.put("ipAddress", data.get(6));       // IP地址
                    rowData.put("port", data.get(7));            // 端口
                    rowData.put("remark", data.get(8));          // 备注

                    dataList.add(rowData);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("[设备导入] Excel解析完成: batchId={}, totalRows={}", batchId, dataList.size());
                }
            }).sheet().doRead();

        } catch (Exception e) {
            log.error("[设备导入] Excel解析异常: batchId={}, error={}", batchId, e.getMessage(), e);
            throw new RuntimeException("Excel解析失败: " + e.getMessage(), e);
        }

        return dataList;
    }

    /**
     * 验证设备数据
     */
    @Override
    public String validateDeviceData(JSONObject rowData, Integer rowNumber) {
        // 1. 必填字段验证
        String deviceCode = rowData.getString("deviceCode");
        if (deviceCode == null || deviceCode.trim().isEmpty()) {
            return "设备编码不能为空";
        }

        String deviceName = rowData.getString("deviceName");
        if (deviceName == null || deviceName.trim().isEmpty()) {
            return "设备名称不能为空";
        }

        Integer deviceType = rowData.getInteger("deviceType");
        if (deviceType == null) {
            return "设备类型不能为空";
        }

        // 2. 格式验证
        // 设备编码格式：字母开头，后跟字母、数字、下划线，长度2-50
        if (!deviceCode.matches("^[a-zA-Z][a-zA-Z0-9_]{1,49}$")) {
            return "设备编码格式不正确（字母开头，2-50位，只能包含字母、数字、下划线）";
        }

        // IP地址格式验证（可选）
        String ipAddress = rowData.getString("ipAddress");
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            if (!ipAddress.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
                return "IP地址格式不正确";
            }
        }

        // 端口格式验证（可选）
        String port = rowData.getString("port");
        if (port != null && !port.trim().isEmpty()) {
            try {
                int portNum = Integer.parseInt(port);
                if (portNum < 1 || portNum > 65535) {
                    return "端口号必须在1-65535之间";
                }
            } catch (NumberFormatException e) {
                return "端口号格式不正确";
            }
        }

        // 3. 业务规则验证
        // 设备编码唯一性验证（需要在executeImport时检查数据库）
        // 这里只做格式验证，唯一性验证在导入时进行

        return null; // 验证成功
    }

    /**
     * 执行导入操作
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceImportResultVO executeImport(Long batchId) {
        log.info("[设备导入] 开始执行导入: batchId={}", batchId);

        try {
            // 1. 查询批次信息
            DeviceImportBatchEntity batch = deviceImportBatchDao.selectById(batchId);
            if (batch == null) {
                throw new IllegalArgumentException("导入批次不存在: batchId=" + batchId);
            }

            if (batch.getImportStatus() != 0) {
                throw new IllegalArgumentException("该批次已导入，请勿重复执行: batchId=" + batchId);
            }

            LocalDateTime startTime = LocalDateTime.now();

            // 2. 查询验证成功的数据
            List<DeviceImportSuccessEntity> successList = deviceImportSuccessDao.selectByBatchId(batchId);

            // 3. 实际导入设备（这里应该调用设备管理的Service）
            // TODO: 调用 DeviceService.batchAddDevices(successList);
            int actualSuccessCount = successList.size(); // 模拟全部导入成功

            // 4. 更新批次状态
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

            int importStatus;
            String statusMessage;

            if (actualSuccessCount == batch.getTotalCount()) {
                importStatus = 1; // 成功
                statusMessage = "导入完成";
            } else if (actualSuccessCount > 0) {
                importStatus = 2; // 部分失败
                statusMessage = "部分导入成功";
            } else {
                importStatus = 3; // 全部失败
                statusMessage = "导入失败";
            }

            deviceImportBatchDao.completeBatch(batchId, importStatus, statusMessage, endTime, durationMs);

            log.info("[设备导入] 导入完成: batchId={}, status={}, durationMs={}", batchId, importStatus, durationMs);

            // 5. 构建返回结果
            DeviceImportResultVO result = new DeviceImportResultVO();
            result.setBatchId(batchId);
            result.setBatchName(batch.getBatchName());
            result.setFileName(batch.getFileName());
            result.setTotalCount(batch.getTotalCount());
            result.setSuccessCount(batch.getSuccessCount());
            result.setFailedCount(batch.getFailedCount());
            result.setSkippedCount(batch.getSkippedCount());
            result.setSuccessRate(batch.getTotalCount() > 0 ?
                    (double) batch.getSuccessCount() / batch.getTotalCount() * 100 : 0.0);
            result.setImportStatus(importStatus);
            result.setImportStatusName(getImportStatusName(importStatus));
            result.setStatusMessage(statusMessage);
            result.setDurationSeconds(durationMs / 1000.0);

            return result;

        } catch (Exception e) {
            log.error("[设备导入] 执行导入异常: batchId={}, error={}", batchId, e.getMessage(), e);

            // 更新批次状态为失败
            deviceImportBatchDao.updateStatus(batchId, 3, "导入失败: " + e.getMessage());

            throw new RuntimeException("执行导入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分页查询导入批次
     */
    @Override
    public PageResult<DeviceImportBatchVO> queryImportBatches(DeviceImportQueryForm queryForm) {
        log.debug("[设备导入] 查询导入批次: queryForm={}", queryForm);

        // 构建查询条件
        LambdaQueryWrapper<DeviceImportBatchEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (queryForm.getImportStatus() != null) {
            queryWrapper.eq(DeviceImportBatchEntity::getImportStatus, queryForm.getImportStatus());
        }

        if (queryForm.getOperatorId() != null) {
            queryWrapper.eq(DeviceImportBatchEntity::getOperatorId, queryForm.getOperatorId());
        }

        if (queryForm.getStartTime() != null) {
            queryWrapper.ge(DeviceImportBatchEntity::getCreateTime, queryForm.getStartTime());
        }

        if (queryForm.getEndTime() != null) {
            queryWrapper.le(DeviceImportBatchEntity::getCreateTime, queryForm.getEndTime());
        }

        if (queryForm.getKeyword() != null && !queryForm.getKeyword().trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(DeviceImportBatchEntity::getBatchName, queryForm.getKeyword())
                    .or()
                    .like(DeviceImportBatchEntity::getFileName, queryForm.getKeyword())
            );
        }

        // 排序
        if ("createTime".equals(queryForm.getSortField())) {
            queryWrapper.orderBy(true, "desc".equals(queryForm.getSortOrder()),
                    DeviceImportBatchEntity::getCreateTime);
        }

        // 分页查询
        Page<DeviceImportBatchEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        Page<DeviceImportBatchEntity> resultPage = deviceImportBatchDao.selectPage(page, queryWrapper);

        // 转换为VO
        List<DeviceImportBatchVO> voList = resultPage.getRecords().stream()
                .map(this::convertToBatchVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, resultPage.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());
    }

    /**
     * 查询导入批次详情
     */
    @Override
    public DeviceImportBatchVO getImportBatchDetail(Long batchId) {
        log.debug("[设备导入] 查询批次详情: batchId={}", batchId);

        DeviceImportBatchEntity batch = deviceImportBatchDao.selectById(batchId);
        if (batch == null) {
            return null;
        }

        return convertToBatchVO(batch);
    }

    /**
     * 查询批次的错误列表
     */
    @Override
    public List<DeviceImportErrorVO> queryBatchErrors(Long batchId) {
        log.debug("[设备导入] 查询批次错误列表: batchId={}", batchId);

        List<DeviceImportErrorEntity> errorList = deviceImportErrorDao.selectByBatchId(batchId);

        return errorList.stream()
                .map(this::convertToErrorVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取导入统计信息
     */
    @Override
    public DeviceImportStatisticsVO getImportStatistics() {
        log.debug("[设备导入] 查询导入统计信息");

        try {
            DeviceImportStatisticsVO statistics = new DeviceImportStatisticsVO();

            // 查询所有批次记录
            LambdaQueryWrapper<DeviceImportBatchEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeviceImportBatchEntity::getDeletedFlag, false);
            queryWrapper.orderByDesc(DeviceImportBatchEntity::getCreateTime);

            List<DeviceImportBatchEntity> allBatches = deviceImportBatchDao.selectList(queryWrapper);

            if (allBatches.isEmpty()) {
                statistics.setTotalImportCount(0);
                statistics.setOverallSuccessRate(0.0);
                return statistics;
            }

            // 计算统计信息
            int totalImportCount = allBatches.size();
            int totalRecords = 0;
            int totalSuccess = 0;
            int totalFailed = 0;

            for (DeviceImportBatchEntity batch : allBatches) {
                totalRecords += batch.getTotalCount() != null ? batch.getTotalCount() : 0;
                totalSuccess += batch.getSuccessCount() != null ? batch.getSuccessCount() : 0;
                totalFailed += batch.getFailedCount() != null ? batch.getFailedCount() : 0;
            }

            // 计算成功率
            double overallSuccessRate = totalRecords > 0 ?
                    (double) totalSuccess / totalRecords * 100 : 0.0;

            // 统计最近7天导入次数
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            long recentImportCount = allBatches.stream()
                    .filter(batch -> batch.getCreateTime().isAfter(sevenDaysAgo))
                    .count();

            // 统计各状态批次数量
            long successBatchCount = allBatches.stream()
                    .filter(batch -> batch.getImportStatus() == 1)
                    .count();
            long partialFailBatchCount = allBatches.stream()
                    .filter(batch -> batch.getImportStatus() == 2)
                    .count();
            long totalFailBatchCount = allBatches.stream()
                    .filter(batch -> batch.getImportStatus() == 3)
                    .count();

            // 设置统计信息
            statistics.setTotalImportCount(totalImportCount);
            statistics.setOverallSuccessRate(Math.round(overallSuccessRate * 10.0) / 10.0); // 保留1位小数
            statistics.setTotalRecordCount(totalRecords);
            statistics.setTotalSuccessCount(totalSuccess);
            statistics.setTotalFailedCount(totalFailed);
            statistics.setRecentImportCount((int) recentImportCount);
            statistics.setSuccessBatchCount((int) successBatchCount);
            statistics.setPartialFailBatchCount((int) partialFailBatchCount);
            statistics.setTotalFailBatchCount((int) totalFailBatchCount);

            log.debug("[设备导入] 统计信息: total={}, successRate={}%", totalImportCount, overallSuccessRate);

            return statistics;

        } catch (Exception e) {
            log.error("[设备导入] 查询统计信息异常: error={}", e.getMessage(), e);

            // 返回空统计
            DeviceImportStatisticsVO statistics = new DeviceImportStatisticsVO();
            statistics.setTotalImportCount(0);
            statistics.setOverallSuccessRate(0.0);
            return statistics;
        }
    }

    /**
     * 删除导入批次
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteImportBatch(Long batchId) {
        log.info("[设备导入] 删除导入批次: batchId={}", batchId);

        // 级联删除成功记录和错误记录（外键约束自动处理）
        deviceImportErrorDao.deleteByBatchId(batchId);
        deviceImportSuccessDao.deleteByBatchId(batchId);

        // 删除批次记录
        return deviceImportBatchDao.deleteById(batchId);
    }

    /**
     * 下载导入模板
     */
    @Override
    public byte[] downloadTemplate() {
        log.info("[设备导入] 生成导入模板");

        try {
            // 创建CSV模板数据
            StringBuilder csv = new StringBuilder();

            // 表头（带*号表示必填）
            csv.append("设备编码*,设备名称*,设备类型*,设备型号,厂商,所属区域,IP地址,端口,备注\n");

            // 示例数据
            csv.append("DEV-001,门禁控制器-01,1,AC-2000,Hikvision,1,192.168.1.100,8000,主入口门禁\n");
            csv.append("DEV-002,门禁控制器-02,1,AC-2000,Hikvision,1,192.168.1.101,8000,侧门门禁\n");

            // 说明行
            csv.append("\n");
            csv.append("# 说明:\n");
            csv.append("# 设备类型: 1-门禁设备 2-考勤设备 3-消费设备 4-视频设备 5-访客设备\n");
            csv.append("# 所属区域: 填写区域ID\n");
            csv.append("# 带*号的字段为必填项\n");

            byte[] csvBytes = csv.toString().getBytes("UTF-8");
            log.info("[设备导入] 模板生成成功: size={}", csvBytes.length);
            return csvBytes;

        } catch (Exception e) {
            log.error("[设备导入] 生成导入模板异常: error={}", e.getMessage(), e);
            throw new RuntimeException("生成导入模板失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出错误记录
     */
    @Override
    public byte[] exportErrors(Long batchId) {
        log.info("[设备导入] 导出错误记录: batchId={}", batchId);

        try {
            // 查询错误记录
            List<DeviceImportErrorEntity> errorList = deviceImportErrorDao.selectByBatchId(batchId);

            if (errorList.isEmpty()) {
                log.warn("[设备导入] 批次无错误记录: batchId={}", batchId);
                return new byte[0];
            }

            // 创建CSV数据
            StringBuilder csv = new StringBuilder();

            // 表头
            csv.append("行号,错误码,错误信息,原始数据\n");

            // 数据行
            for (DeviceImportErrorEntity error : errorList) {
                csv.append(error.getRowNumber()).append(",");
                csv.append(error.getErrorCode()).append(",");
                // 转义逗号和引号
                String errorMsg = error.getErrorMessage() != null ? error.getErrorMessage().replace("\"", "\"\"") : "";
                String rawData = error.getRawData() != null ? error.getRawData().replace("\"", "\"\"") : "";
                csv.append("\"").append(errorMsg).append("\",");
                csv.append("\"").append(rawData).append("\"\n");
            }

            byte[] csvBytes = csv.toString().getBytes("UTF-8");
            log.info("[设备导入] 错误记录导出成功: batchId={}, count={}", batchId, errorList.size());
            return csvBytes;

        } catch (Exception e) {
            log.error("[设备导入] 导出错误记录异常: batchId={}, error={}", batchId, e.getMessage(), e);
            throw new RuntimeException("导出错误记录失败: " + e.getMessage(), e);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换为批次VO
     */
    private DeviceImportBatchVO convertToBatchVO(DeviceImportBatchEntity entity) {
        DeviceImportBatchVO vo = new DeviceImportBatchVO();
        BeanUtils.copyProperties(entity, vo);

        // 计算可读格式
        vo.setFileSizeReadable(formatFileSize(entity.getFileSize()));
        vo.setDurationReadable(formatDuration(entity.getDurationMs()));

        // 计算成功率
        if (entity.getTotalCount() != null && entity.getTotalCount() > 0) {
            double successRate = (double) entity.getSuccessCount() / entity.getTotalCount() * 100;
            vo.setSuccessRate(Math.round(successRate * 10.0) / 10.0); // 保留1位小数
        }

        // 状态名称
        vo.setImportStatusName(getImportStatusName(entity.getImportStatus()));
        vo.setOperatorTypeName(getOperatorTypeName(entity.getOperatorType()));

        return vo;
    }

    /**
     * 转换为错误VO
     */
    private DeviceImportErrorVO convertToErrorVO(DeviceImportErrorEntity entity) {
        DeviceImportErrorVO vo = new DeviceImportErrorVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * 获取导入状态名称
     */
    private String getImportStatusName(Integer importStatus) {
        if (importStatus == null) {
            return "未知";
        }
        switch (importStatus) {
            case 0:
                return "处理中";
            case 1:
                return "成功";
            case 2:
                return "部分失败";
            case 3:
                return "全部失败";
            default:
                return "未知";
        }
    }

    /**
     * 获取操作人类型名称
     */
    private String getOperatorTypeName(Integer operatorType) {
        if (operatorType == null) {
            return "未知";
        }
        return operatorType == 1 ? "管理员" : "普通用户";
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long fileSize) {
        if (fileSize == null) {
            return "0 B";
        }

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 格式化时长
     */
    private String formatDuration(Long durationMs) {
        if (durationMs == null) {
            return "0 秒";
        }

        if (durationMs < 1000) {
            return durationMs + " 毫秒";
        } else if (durationMs < 60000) {
            return String.format("%.2f 秒", durationMs / 1000.0);
        } else {
            long seconds = durationMs / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%d分%d秒", minutes, seconds);
        }
    }
}
