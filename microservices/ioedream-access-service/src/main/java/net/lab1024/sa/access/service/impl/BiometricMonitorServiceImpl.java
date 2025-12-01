package net.lab1024.sa.access.service.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.dao.BiometricRecordDao;
import net.lab1024.sa.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.access.domain.entity.BiometricDeviceEntity;
import net.lab1024.sa.access.domain.entity.BiometricLogEntity;
import net.lab1024.sa.access.domain.entity.BiometricRecordEntity;
import net.lab1024.sa.access.domain.query.BiometricQueryForm;
import net.lab1024.sa.access.domain.vo.BiometricAlertVO;
import net.lab1024.sa.access.domain.vo.BiometricStatusVO;
import net.lab1024.sa.access.service.BiometricMonitorService;
import net.lab1024.sa.common.domain.PageResult;

/**
 * 生物识别监控服务实现类
 * <p>
 * 提供生物识别设备和系统的监控功能实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Slf4j
@Service
public class BiometricMonitorServiceImpl implements BiometricMonitorService {

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private BiometricRecordDao biometricRecordDao;

    /**
     * 获取所有生物识别设备的实时状态信息
     *
     * @return 设备状态列表
     */
    @Override
    public List<BiometricStatusVO> getAllDeviceStatus() {
        try {
            log.debug("获取所有设备状态");

            // 1. 查询所有门禁设备（生物识别设备）
            LambdaQueryWrapper<AccessDeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            deviceWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            List<AccessDeviceEntity> devices = accessDeviceDao.selectList(deviceWrapper);

            if (CollUtil.isEmpty(devices)) {
                return new ArrayList<>();
            }

            // 2. 获取今日开始时间
            LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);

            // 3. 转换为状态VO
            List<BiometricStatusVO> statusList = devices.stream().map(device -> {
                BiometricStatusVO statusVO = new BiometricStatusVO();
                statusVO.setDeviceId(device.getDeviceId());
                statusVO.setDeviceName(device.getDeviceName());
                statusVO.setDeviceType(device.getDeviceType());
                // deviceStatus是String类型，需要转换为Integer
                Integer deviceStatusInt = "ONLINE".equals(device.getDeviceStatus()) ? 1 : 0;
                statusVO.setDeviceStatus(deviceStatusInt);
                statusVO.setOnline("ONLINE".equals(device.getDeviceStatus()));
                statusVO.setLastOnlineTime(device.getUpdateTime());

                // 查询今日识别次数
                LambdaQueryWrapper<BiometricRecordEntity> recordWrapper = new LambdaQueryWrapper<>();
                recordWrapper.eq(BiometricRecordEntity::getDeviceId, device.getDeviceId());
                recordWrapper.ge(BiometricRecordEntity::getCreateTime, todayStart);
                Long todayCount = biometricRecordDao.selectCount(recordWrapper);
                statusVO.setTodayRecognitions(todayCount != null ? todayCount : 0L);

                // 查询今日成功次数
                LambdaQueryWrapper<BiometricRecordEntity> successWrapper = new LambdaQueryWrapper<>();
                successWrapper.eq(BiometricRecordEntity::getDeviceId, device.getDeviceId());
                successWrapper.eq(BiometricRecordEntity::getVerificationResult, "success");
                successWrapper.ge(BiometricRecordEntity::getCreateTime, todayStart);
                Long successCount = biometricRecordDao.selectCount(successWrapper);

                // 计算成功率
                if (todayCount != null && todayCount > 0) {
                    double successRate = (successCount != null ? successCount.doubleValue() : 0.0) / todayCount * 100;
                    statusVO.setTodaySuccessRate(successRate);
                } else {
                    statusVO.setTodaySuccessRate(0.0);
                }

                // 查询最后识别时间
                LambdaQueryWrapper<BiometricRecordEntity> lastWrapper = new LambdaQueryWrapper<>();
                lastWrapper.eq(BiometricRecordEntity::getDeviceId, device.getDeviceId());
                lastWrapper.orderByDesc(BiometricRecordEntity::getCreateTime);
                lastWrapper.last("LIMIT 1");
                BiometricRecordEntity lastRecord = biometricRecordDao.selectOne(lastWrapper);
                if (lastRecord != null) {
                    statusVO.setLastRecognitionTime(lastRecord.getCreateTime());
                }

                return statusVO;
            }).collect(Collectors.toList());

            log.debug("获取设备状态完成，设备数量: {}", statusList.size());
            return statusList;

        } catch (Exception e) {
            log.error("获取所有设备状态异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据设备ID获取设备的详细信息和状态
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    @Override
    public BiometricDeviceEntity getDeviceDetail(Long deviceId) {
        try {
            log.debug("获取设备详情: deviceId={}", deviceId);

            if (deviceId == null) {
                throw new IllegalArgumentException("设备ID不能为空");
            }

            // 查询设备信息
            AccessDeviceEntity accessDevice = accessDeviceDao.selectById(deviceId);
            if (accessDevice == null || accessDevice.getDeletedFlag() == 1) {
                log.warn("设备不存在: deviceId={}", deviceId);
                return null;
            }

            // 转换为生物识别设备实体
            BiometricDeviceEntity device = new BiometricDeviceEntity();
            device.setDeviceId(accessDevice.getDeviceId());
            device.setDeviceName(accessDevice.getDeviceName());
            device.setDeviceCode(accessDevice.getDeviceCode());
            device.setDeviceType(accessDevice.getDeviceType());
            device.setDeviceModel(accessDevice.getDeviceModel());
            // deviceStatus是String类型，需要转换为Integer
            Integer deviceStatusInt = "ONLINE".equals(accessDevice.getDeviceStatus()) ? 1 : 0;
            device.setDeviceStatus(deviceStatusInt);
            device.setDeviceIp(accessDevice.getIpAddress());
            device.setDevicePort(accessDevice.getPort());
            device.setLastOnlineTime(accessDevice.getUpdateTime());
            device.setRegisterTime(accessDevice.getCreateTime());
            device.setRemark(accessDevice.getRemark());

            return device;

        } catch (Exception e) {
            log.error("获取设备详情异常，deviceId: {}", deviceId, e);
            return null;
        }
    }

    /**
     * 获取指定设备的健康状态和性能指标
     *
     * @param deviceId 设备ID
     * @return 健康状态信息
     */
    @Override
    public Map<String, Object> getDeviceHealth(Long deviceId) {
        try {
            log.debug("获取设备健康状态: deviceId={}", deviceId);

            Map<String, Object> healthInfo = new HashMap<>();

            // 1. 查询设备信息
            AccessDeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                healthInfo.put("status", "设备不存在");
                return healthInfo;
            }

            // 2. 判断设备在线状态
            boolean isOnline = "ONLINE".equals(device.getDeviceStatus());
            healthInfo.put("status", isOnline ? "健康" : "离线");
            healthInfo.put("online", isOnline);
            healthInfo.put("deviceStatus", device.getDeviceStatus());

            // 3. 查询最近24小时的识别记录统计
            LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
            LambdaQueryWrapper<BiometricRecordEntity> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(BiometricRecordEntity::getDeviceId, deviceId);
            recordWrapper.ge(BiometricRecordEntity::getCreateTime, last24Hours);
            Long totalRecords = biometricRecordDao.selectCount(recordWrapper);

            // 4. 查询成功记录
            LambdaQueryWrapper<BiometricRecordEntity> successWrapper = new LambdaQueryWrapper<>();
            successWrapper.eq(BiometricRecordEntity::getDeviceId, deviceId);
            successWrapper.eq(BiometricRecordEntity::getVerificationResult, "success");
            successWrapper.ge(BiometricRecordEntity::getCreateTime, last24Hours);
            Long successRecords = biometricRecordDao.selectCount(successWrapper);

            // 5. 计算成功率
            double successRate = 0.0;
            if (totalRecords != null && totalRecords > 0) {
                successRate = (successRecords != null ? successRecords.doubleValue() : 0.0) / totalRecords * 100;
            }
            healthInfo.put("successRate", successRate);
            healthInfo.put("totalRecords", totalRecords != null ? totalRecords : 0);
            healthInfo.put("successRecords", successRecords != null ? successRecords : 0);

            // 6. 模拟设备资源使用情况（实际应该从设备获取）
            healthInfo.put("cpuUsage", isOnline ? 30.5 : 0.0);
            healthInfo.put("memoryUsage", isOnline ? 45.2 : 0.0);
            healthInfo.put("diskUsage", isOnline ? 25.8 : 0.0);

            // 7. 最后在线时间
            healthInfo.put("lastOnlineTime", device.getUpdateTime());

            return healthInfo;

        } catch (Exception e) {
            log.error("获取设备健康状态异常，deviceId: {}", deviceId, e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("status", "查询异常");
            errorInfo.put("error", e.getMessage());
            return errorInfo;
        }
    }

    /**
     * 获取设备在一定时间内的性能统计数据
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 性能统计数据
     */
    @Override
    public Map<String, Object> getDevicePerformance(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.debug("获取设备性能统计: deviceId={}, startTime={}, endTime={}", deviceId, startTime, endTime);

            Map<String, Object> performance = new HashMap<>();

            // 1. 构建查询条件
            LambdaQueryWrapper<BiometricRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BiometricRecordEntity::getDeviceId, deviceId);
            if (startTime != null) {
                wrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(BiometricRecordEntity::getCreateTime, endTime);
            }

            // 2. 查询总请求数
            Long totalRequests = biometricRecordDao.selectCount(wrapper);
            performance.put("totalRequests", totalRequests != null ? totalRequests : 0L);

            // 3. 查询成功请求数
            LambdaQueryWrapper<BiometricRecordEntity> successWrapper = new LambdaQueryWrapper<>();
            successWrapper.eq(BiometricRecordEntity::getDeviceId, deviceId);
            if (startTime != null) {
                successWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                successWrapper.le(BiometricRecordEntity::getCreateTime, endTime);
            }
            successWrapper.eq(BiometricRecordEntity::getVerificationResult, "success");
            Long successRequests = biometricRecordDao.selectCount(successWrapper);
            performance.put("successRequests", successRequests != null ? successRequests : 0L);

            // 4. 查询失败请求数
            LambdaQueryWrapper<BiometricRecordEntity> failWrapper = new LambdaQueryWrapper<>();
            failWrapper.eq(BiometricRecordEntity::getDeviceId, deviceId);
            if (startTime != null) {
                failWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                failWrapper.le(BiometricRecordEntity::getCreateTime, endTime);
            }
            failWrapper.in(BiometricRecordEntity::getVerificationResult, "failure", "liveness_failed");
            Long failRequests = biometricRecordDao.selectCount(failWrapper);
            performance.put("failRequests", failRequests != null ? failRequests : 0L);

            // 5. 计算成功率
            double successRate = 0.0;
            if (totalRequests != null && totalRequests > 0) {
                successRate = (successRequests != null ? successRequests.doubleValue() : 0.0) / totalRequests * 100;
            }
            performance.put("successRate", successRate);

            // 6. 查询平均响应时间（从记录中获取处理时间）
            List<BiometricRecordEntity> records = biometricRecordDao.selectList(wrapper);
            if (CollUtil.isNotEmpty(records)) {
                double avgResponseTime = records.stream()
                        .filter(r -> r.getProcessingTime() != null)
                        .mapToInt(BiometricRecordEntity::getProcessingTime)
                        .average()
                        .orElse(0.0);
                performance.put("avgResponseTime", Math.round(avgResponseTime));
                performance.put("minResponseTime", records.stream()
                        .filter(r -> r.getProcessingTime() != null)
                        .mapToInt(BiometricRecordEntity::getProcessingTime)
                        .min()
                        .orElse(0));
                performance.put("maxResponseTime", records.stream()
                        .filter(r -> r.getProcessingTime() != null)
                        .mapToInt(BiometricRecordEntity::getProcessingTime)
                        .max()
                        .orElse(0));
            } else {
                performance.put("avgResponseTime", 0);
                performance.put("minResponseTime", 0);
                performance.put("maxResponseTime", 0);
            }

            // 7. 计算QPS（每秒请求数）
            if (startTime != null && endTime != null && totalRequests != null) {
                long seconds = java.time.Duration.between(startTime, endTime).getSeconds();
                if (seconds > 0) {
                    double qps = totalRequests.doubleValue() / seconds;
                    performance.put("qps", Math.round(qps * 100.0) / 100.0);
                } else {
                    performance.put("qps", 0.0);
                }
            }

            return performance;

        } catch (Exception e) {
            log.error("获取设备性能统计异常，deviceId: {}", deviceId, e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return errorInfo;
        }
    }

    /**
     * 分页查询生物识别操作日志
     *
     * @param queryForm 分页查询表单
     * @return 日志分页结果
     */
    @Override
    public PageResult<BiometricLogEntity> getBiometricLogs(BiometricQueryForm queryForm) {
        try {
            log.debug("获取生物识别日志: pageNum={}, pageSize={}", queryForm.getPageNum(), queryForm.getPageSize());

            // 1. 构建查询条件
            LambdaQueryWrapper<BiometricRecordEntity> wrapper = new LambdaQueryWrapper<>();

            if (queryForm.getDeviceId() != null) {
                wrapper.eq(BiometricRecordEntity::getDeviceId, queryForm.getDeviceId());
            }
            if (queryForm.getUserId() != null) {
                wrapper.eq(BiometricRecordEntity::getEmployeeId, queryForm.getUserId());
            }
            if (StrUtil.isNotBlank(queryForm.getBiometricType())) {
                wrapper.eq(BiometricRecordEntity::getBiometricType, queryForm.getBiometricType());
            }
            if (queryForm.getOperationType() != null) {
                // operationType映射到recognitionMode
                String recognitionMode = null;
                if (queryForm.getOperationType() == 1) {
                    recognitionMode = "VERIFY";
                } else if (queryForm.getOperationType() == 2) {
                    recognitionMode = "ENROLL";
                } else if (queryForm.getOperationType() == 3) {
                    recognitionMode = "SEARCH";
                } else if (queryForm.getOperationType() == 4) {
                    recognitionMode = "UPDATE";
                }
                if (recognitionMode != null) {
                    wrapper.eq(BiometricRecordEntity::getRecognitionMode, recognitionMode);
                }
            }
            if (queryForm.getOperationResult() != null) {
                // operationResult: 0-失败, 1-成功, 2-超时, 3-系统错误
                if (queryForm.getOperationResult() == 1) {
                    wrapper.eq(BiometricRecordEntity::getVerificationResult, "success");
                } else if (queryForm.getOperationResult() == 0) {
                    wrapper.in(BiometricRecordEntity::getVerificationResult, "failure", "liveness_failed");
                } else if (queryForm.getOperationResult() == 2) {
                    wrapper.eq(BiometricRecordEntity::getVerificationResult, "timeout");
                } else if (queryForm.getOperationResult() == 3) {
                    wrapper.eq(BiometricRecordEntity::getVerificationResult, "error");
                }
            }
            if (queryForm.getStartTime() != null) {
                wrapper.ge(BiometricRecordEntity::getCreateTime, queryForm.getStartTime());
            }
            if (queryForm.getEndTime() != null) {
                wrapper.le(BiometricRecordEntity::getCreateTime, queryForm.getEndTime());
            }

            wrapper.orderByDesc(BiometricRecordEntity::getCreateTime);

            // 2. 分页查询
            Page<BiometricRecordEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            IPage<BiometricRecordEntity> pageResult = biometricRecordDao.selectPage(page, wrapper);

            // 3. 转换为日志实体（BiometricRecordEntity和BiometricLogEntity结构相似，这里简化处理）
            List<BiometricLogEntity> logList = new ArrayList<>();
            if (CollUtil.isNotEmpty(pageResult.getRecords())) {
                for (BiometricRecordEntity record : pageResult.getRecords()) {
                    BiometricLogEntity logEntity = new BiometricLogEntity();
                    logEntity.setLogId(record.getRecordId());
                    logEntity.setDeviceId(record.getDeviceId());
                    logEntity.setUserId(record.getEmployeeId());
                    logEntity.setBiometricType(record.getBiometricType());
                    // recognitionMode映射到operationType
                    int operationType = 0;
                    if ("VERIFY".equals(record.getRecognitionMode())) {
                        operationType = 1;
                    } else if ("ENROLL".equals(record.getRecognitionMode())) {
                        operationType = 2;
                    } else if ("SEARCH".equals(record.getRecognitionMode())) {
                        operationType = 3;
                    } else if ("UPDATE".equals(record.getRecognitionMode())) {
                        operationType = 4;
                    }
                    logEntity.setOperationType(operationType);
                    // 转换verificationResult为operationResult
                    int operationResult = 0;
                    if ("success".equals(record.getVerificationResult())) {
                        operationResult = 1;
                    } else if ("timeout".equals(record.getVerificationResult())) {
                        operationResult = 2;
                    } else if ("error".equals(record.getVerificationResult())) {
                        operationResult = 3;
                    }
                    logEntity.setOperationResult(operationResult);
                    logEntity.setOperationTime(record.getCreateTime());
                    logEntity.setProcessingTime(record.getProcessingTime());
                    logEntity.setConfidenceScore(record.getConfidenceScore());
                    logEntity.setFailureReason(record.getFailureReason());
                    logList.add(logEntity);
                }
            }

            // 4. 构建分页结果
            PageResult<BiometricLogEntity> result = new PageResult<>();
            result.setList(logList);
            result.setTotal(pageResult.getTotal());
            result.setPageNum(queryForm.getPageNum() != null ? queryForm.getPageNum().longValue() : 1L);
            result.setPageSize(queryForm.getPageSize() != null ? queryForm.getPageSize().longValue() : 10L);

            return result;

        } catch (Exception e) {
            log.error("获取生物识别日志异常", e);
            PageResult<BiometricLogEntity> result = new PageResult<>();
            result.setList(new ArrayList<>());
            result.setTotal(0L);
            return result;
        }
    }

    /**
     * 获取今日的生物识别统计数据
     *
     * @return 统计数据
     */
    @Override
    public Map<String, Object> getTodayStatistics() {
        try {
            log.debug("获取今日识别统计");

            Map<String, Object> statistics = new HashMap<>();

            // 1. 获取今日开始时间
            LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);

            // 2. 查询今日总识别次数
            LambdaQueryWrapper<BiometricRecordEntity> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.ge(BiometricRecordEntity::getCreateTime, todayStart);
            Long totalCount = biometricRecordDao.selectCount(totalWrapper);
            statistics.put("totalCount", totalCount != null ? totalCount : 0L);

            // 3. 查询今日成功次数
            LambdaQueryWrapper<BiometricRecordEntity> successWrapper = new LambdaQueryWrapper<>();
            successWrapper.eq(BiometricRecordEntity::getVerificationResult, "success");
            successWrapper.ge(BiometricRecordEntity::getCreateTime, todayStart);
            Long successCount = biometricRecordDao.selectCount(successWrapper);
            statistics.put("successCount", successCount != null ? successCount : 0L);

            // 4. 查询今日失败次数
            LambdaQueryWrapper<BiometricRecordEntity> failWrapper = new LambdaQueryWrapper<>();
            failWrapper.in(BiometricRecordEntity::getVerificationResult, "failure", "liveness_failed");
            failWrapper.ge(BiometricRecordEntity::getCreateTime, todayStart);
            Long failCount = biometricRecordDao.selectCount(failWrapper);
            statistics.put("failCount", failCount != null ? failCount : 0L);

            // 5. 计算成功率
            double successRate = 0.0;
            if (totalCount != null && totalCount > 0) {
                successRate = (successCount != null ? successCount.doubleValue() : 0.0) / totalCount * 100;
            }
            statistics.put("successRate", Math.round(successRate * 100.0) / 100.0);

            // 6. 查询在线设备数
            LambdaQueryWrapper<AccessDeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            deviceWrapper.eq(AccessDeviceEntity::getDeviceStatus, "ONLINE");
            deviceWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            Long onlineDeviceCount = accessDeviceDao.selectCount(deviceWrapper);
            statistics.put("onlineDeviceCount", onlineDeviceCount != null ? onlineDeviceCount : 0L);

            // 7. 查询总设备数
            LambdaQueryWrapper<AccessDeviceEntity> totalDeviceWrapper = new LambdaQueryWrapper<>();
            totalDeviceWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            totalDeviceWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            Long totalDeviceCount = accessDeviceDao.selectCount(totalDeviceWrapper);
            statistics.put("totalDeviceCount", totalDeviceCount != null ? totalDeviceCount : 0L);

            return statistics;

        } catch (Exception e) {
            log.error("获取今日识别统计异常", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("totalCount", 0);
            errorInfo.put("successCount", 0);
            errorInfo.put("failCount", 0);
            return errorInfo;
        }
    }

    /**
     * 获取指定时间范围的历史识别统计数据
     *
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @param statisticsType 统计类型 day/week/month
     * @return 统计数据
     */
    @Override
    public Map<String, Object> getHistoryStatistics(LocalDateTime startTime, LocalDateTime endTime,
            String statisticsType) {
        try {
            log.debug("获取历史识别统计: type={}, startTime={}, endTime={}", statisticsType, startTime, endTime);

            Map<String, Object> statistics = new HashMap<>();
            List<Map<String, Object>> dataList = new ArrayList<>();

            // 1. 构建基础查询条件
            LambdaQueryWrapper<BiometricRecordEntity> baseWrapper = new LambdaQueryWrapper<>();
            if (startTime != null) {
                baseWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                baseWrapper.le(BiometricRecordEntity::getCreateTime, endTime);
            }

            // 2. 根据统计类型分组统计
            if ("day".equals(statisticsType)) {
                // 按天统计
                LocalDateTime current = startTime != null ? startTime : LocalDateTime.now().minusDays(7);
                LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
                while (!current.isAfter(end)) {
                    LocalDateTime dayStart = current.with(LocalTime.MIN);
                    LocalDateTime dayEnd = current.with(LocalTime.MAX);

                    LambdaQueryWrapper<BiometricRecordEntity> dayWrapper = new LambdaQueryWrapper<BiometricRecordEntity>();
                    if (startTime != null) {
                        dayWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
                    }
                    if (endTime != null) {
                        dayWrapper.le(BiometricRecordEntity::getCreateTime, endTime);
                    }
                    dayWrapper.ge(BiometricRecordEntity::getCreateTime, dayStart);
                    dayWrapper.le(BiometricRecordEntity::getCreateTime, dayEnd);

                    Long total = biometricRecordDao.selectCount(dayWrapper);
                    LambdaQueryWrapper<BiometricRecordEntity> successDayWrapper = new LambdaQueryWrapper<BiometricRecordEntity>();
                    if (startTime != null) {
                        successDayWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
                    }
                    if (endTime != null) {
                        successDayWrapper.le(BiometricRecordEntity::getCreateTime, endTime);
                    }
                    successDayWrapper.ge(BiometricRecordEntity::getCreateTime, dayStart);
                    successDayWrapper.le(BiometricRecordEntity::getCreateTime, dayEnd);
                    successDayWrapper.eq(BiometricRecordEntity::getVerificationResult, "success");
                    Long success = biometricRecordDao.selectCount(successDayWrapper);

                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", current.toLocalDate().toString());
                    dayData.put("totalCount", total != null ? total : 0L);
                    dayData.put("successCount", success != null ? success : 0L);
                    dayData.put("failCount", (total != null ? total : 0L) - (success != null ? success : 0L));
                    dataList.add(dayData);

                    current = current.plusDays(1);
                }
            } else if ("week".equals(statisticsType)) {
                // 按周统计（简化实现，按7天分组）
                LocalDateTime current = startTime != null ? startTime : LocalDateTime.now().minusWeeks(4);
                LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
                while (!current.isAfter(end)) {
                    LocalDateTime weekStart = current;
                    LocalDateTime weekEnd = current.plusDays(6).with(LocalTime.MAX);

                    LambdaQueryWrapper<BiometricRecordEntity> weekWrapper = new LambdaQueryWrapper<BiometricRecordEntity>();
                    if (startTime != null) {
                        weekWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
                    }
                    if (endTime != null) {
                        weekWrapper.le(BiometricRecordEntity::getCreateTime, endTime);
                    }
                    weekWrapper.ge(BiometricRecordEntity::getCreateTime, weekStart);
                    weekWrapper.le(BiometricRecordEntity::getCreateTime, weekEnd);

                    Long total = biometricRecordDao.selectCount(weekWrapper);
                    LambdaQueryWrapper<BiometricRecordEntity> successWeekWrapper = new LambdaQueryWrapper<BiometricRecordEntity>();
                    if (startTime != null) {
                        successWeekWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
                    }
                    if (endTime != null) {
                        successWeekWrapper.le(BiometricRecordEntity::getCreateTime, endTime);
                    }
                    successWeekWrapper.ge(BiometricRecordEntity::getCreateTime, weekStart);
                    successWeekWrapper.le(BiometricRecordEntity::getCreateTime, weekEnd);
                    successWeekWrapper.eq(BiometricRecordEntity::getVerificationResult, "success");
                    Long success = biometricRecordDao.selectCount(successWeekWrapper);

                    Map<String, Object> weekData = new HashMap<>();
                    weekData.put("week", current.toLocalDate().toString() + " ~ " + weekEnd.toLocalDate().toString());
                    weekData.put("totalCount", total != null ? total : 0L);
                    weekData.put("successCount", success != null ? success : 0L);
                    dataList.add(weekData);

                    current = current.plusWeeks(1);
                }
            } else {
                // 按月统计
                LocalDateTime current = startTime != null ? startTime : LocalDateTime.now().minusMonths(6);
                LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
                while (!current.isAfter(end)) {
                    LocalDateTime monthStart = current.withDayOfMonth(1).with(LocalTime.MIN);
                    LocalDateTime monthEnd = current.withDayOfMonth(current.toLocalDate().lengthOfMonth())
                            .with(LocalTime.MAX);

                    LambdaQueryWrapper<BiometricRecordEntity> monthWrapper = new LambdaQueryWrapper<BiometricRecordEntity>();
                    if (startTime != null) {
                        monthWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
                    }
                    if (endTime != null) {
                        monthWrapper.le(BiometricRecordEntity::getCreateTime, endTime);
                    }
                    monthWrapper.ge(BiometricRecordEntity::getCreateTime, monthStart);
                    monthWrapper.le(BiometricRecordEntity::getCreateTime, monthEnd);

                    Long total = biometricRecordDao.selectCount(monthWrapper);
                    LambdaQueryWrapper<BiometricRecordEntity> successMonthWrapper = new LambdaQueryWrapper<BiometricRecordEntity>();
                    if (startTime != null) {
                        successMonthWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
                    }
                    if (endTime != null) {
                        successMonthWrapper.le(BiometricRecordEntity::getCreateTime, endTime);
                    }
                    successMonthWrapper.ge(BiometricRecordEntity::getCreateTime, monthStart);
                    successMonthWrapper.le(BiometricRecordEntity::getCreateTime, monthEnd);
                    successMonthWrapper.eq(BiometricRecordEntity::getVerificationResult, "success");
                    Long success = biometricRecordDao.selectCount(successMonthWrapper);

                    Map<String, Object> monthData = new HashMap<>();
                    monthData.put("month", current.getYear() + "-" + String.format("%02d", current.getMonthValue()));
                    monthData.put("totalCount", total != null ? total : 0L);
                    monthData.put("successCount", success != null ? success : 0L);
                    dataList.add(monthData);

                    current = current.plusMonths(1);
                }
            }

            statistics.put("data", dataList);
            statistics.put("statisticsType", statisticsType);
            return statistics;

        } catch (Exception e) {
            log.error("获取历史识别统计异常", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("data", new ArrayList<>());
            return errorInfo;
        }
    }

    /**
     * 分析各设备的识别成功率情况
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 成功率分析结果
     */
    @Override
    public Map<String, Object> getSuccessRateAnalysis(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.debug("获取成功率分析: startTime={}, endTime={}", startTime, endTime);

            Map<String, Object> analysis = new HashMap<>();
            List<Map<String, Object>> deviceAnalysis = new ArrayList<>();

            // 1. 查询所有设备
            LambdaQueryWrapper<AccessDeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            deviceWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            List<AccessDeviceEntity> devices = accessDeviceDao.selectList(deviceWrapper);

            // 2. 统计每个设备的成功率
            for (AccessDeviceEntity device : devices) {
                LambdaQueryWrapper<BiometricRecordEntity> recordWrapper = new LambdaQueryWrapper<>();
                recordWrapper.eq(BiometricRecordEntity::getDeviceId, device.getDeviceId());
                if (startTime != null) {
                    recordWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
                }
                if (endTime != null) {
                    recordWrapper.le(BiometricRecordEntity::getCreateTime, endTime);
                }

                Long total = biometricRecordDao.selectCount(recordWrapper);
                LambdaQueryWrapper<BiometricRecordEntity> successRecordWrapper = new LambdaQueryWrapper<>();
                successRecordWrapper.eq(BiometricRecordEntity::getDeviceId, device.getDeviceId());
                if (startTime != null) {
                    successRecordWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
                }
                if (endTime != null) {
                    successRecordWrapper.le(BiometricRecordEntity::getCreateTime, endTime);
                }
                successRecordWrapper.eq(BiometricRecordEntity::getVerificationResult, "success");
                Long success = biometricRecordDao.selectCount(successRecordWrapper);

                double successRate = 0.0;
                if (total != null && total > 0) {
                    successRate = (success != null ? success.doubleValue() : 0.0) / total * 100;
                }

                Map<String, Object> deviceData = new HashMap<>();
                deviceData.put("deviceId", device.getDeviceId());
                deviceData.put("deviceName", device.getDeviceName());
                deviceData.put("totalCount", total != null ? total : 0L);
                deviceData.put("successCount", success != null ? success : 0L);
                deviceData.put("successRate", Math.round(successRate * 100.0) / 100.0);
                deviceAnalysis.add(deviceData);
            }

            // 3. 计算整体平均成功率
            double avgSuccessRate = deviceAnalysis.stream()
                    .mapToDouble(d -> ((Number) d.get("successRate")).doubleValue())
                    .average()
                    .orElse(0.0);

            analysis.put("deviceAnalysis", deviceAnalysis);
            analysis.put("avgSuccessRate", Math.round(avgSuccessRate * 100.0) / 100.0);
            analysis.put("totalDevices", devices.size());

            return analysis;

        } catch (Exception e) {
            log.error("获取成功率分析异常", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("successRate", 0.0);
            return errorInfo;
        }
    }

    /**
     * 分析各设备的识别响应时间分布
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 响应时间分析结果
     */
    @Override
    public Map<String, Object> getResponseTimeAnalysis(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.debug("获取响应时间分析: startTime={}, endTime={}", startTime, endTime);

            Map<String, Object> analysis = new HashMap<>();

            // 1. 构建查询条件
            LambdaQueryWrapper<BiometricRecordEntity> wrapper = new LambdaQueryWrapper<>();
            if (startTime != null) {
                wrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(BiometricRecordEntity::getCreateTime, endTime);
            }
            wrapper.isNotNull(BiometricRecordEntity::getProcessingTime);

            // 2. 查询所有记录
            List<BiometricRecordEntity> records = biometricRecordDao.selectList(wrapper);

            if (CollUtil.isEmpty(records)) {
                analysis.put("avgResponseTime", 0);
                analysis.put("minResponseTime", 0);
                analysis.put("maxResponseTime", 0);
                analysis.put("p50ResponseTime", 0);
                analysis.put("p95ResponseTime", 0);
                analysis.put("p99ResponseTime", 0);
                return analysis;
            }

            // 3. 提取响应时间并排序
            List<Integer> responseTimes = records.stream()
                    .map(BiometricRecordEntity::getProcessingTime)
                    .filter(time -> time != null && time > 0)
                    .sorted()
                    .collect(Collectors.toList());

            if (CollUtil.isEmpty(responseTimes)) {
                analysis.put("avgResponseTime", 0);
                analysis.put("minResponseTime", 0);
                analysis.put("maxResponseTime", 0);
                return analysis;
            }

            // 4. 计算统计指标
            double avgResponseTime = responseTimes.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            int minResponseTime = responseTimes.get(0);
            int maxResponseTime = responseTimes.get(responseTimes.size() - 1);
            int p50ResponseTime = responseTimes.get((int) (responseTimes.size() * 0.5));
            int p95ResponseTime = responseTimes.get((int) (responseTimes.size() * 0.95));
            int p99ResponseTime = responseTimes.get((int) (responseTimes.size() * 0.99));

            analysis.put("avgResponseTime", Math.round(avgResponseTime));
            analysis.put("minResponseTime", minResponseTime);
            analysis.put("maxResponseTime", maxResponseTime);
            analysis.put("p50ResponseTime", p50ResponseTime);
            analysis.put("p95ResponseTime", p95ResponseTime);
            analysis.put("p99ResponseTime", p99ResponseTime);
            analysis.put("totalSamples", responseTimes.size());

            return analysis;

        } catch (Exception e) {
            log.error("获取响应时间分析异常", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("avgResponseTime", 0);
            errorInfo.put("maxResponseTime", 0);
            return errorInfo;
        }
    }

    /**
     * 获取生物识别系统的异常告警信息
     *
     * @param alertLevel  告警级别
     * @param alertStatus 告警状态
     * @param days        查询天数
     * @return 告警列表
     */
    @Override
    public List<BiometricAlertVO> getBiometricAlerts(Integer alertLevel, Integer alertStatus, Integer days) {
        try {
            log.debug("获取生物识别告警: level={}, status={}, days={}", alertLevel, alertStatus, days);

            List<BiometricAlertVO> alerts = new ArrayList<>();

            // 1. 计算查询时间范围
            LocalDateTime startTime = LocalDateTime.now().minusDays(days != null && days > 0 ? days : 7);

            // 2. 检测离线设备告警
            LambdaQueryWrapper<AccessDeviceEntity> offlineWrapper = new LambdaQueryWrapper<>();
            offlineWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            offlineWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            offlineWrapper.and(wrapper -> wrapper
                    .eq(AccessDeviceEntity::getDeviceStatus, "OFFLINE")
                    .or()
                    .lt(AccessDeviceEntity::getUpdateTime, startTime));
            List<AccessDeviceEntity> offlineDevices = accessDeviceDao.selectList(offlineWrapper);

            for (AccessDeviceEntity device : offlineDevices) {
                BiometricAlertVO alert = new BiometricAlertVO();
                alert.setAlertId(System.currentTimeMillis() + device.getDeviceId());
                alert.setAlertType("DEVICE_OFFLINE");
                alert.setAlertLevel(2); // 中等告警
                alert.setAlertTitle("设备离线告警");
                alert.setAlertContent("设备 " + device.getDeviceName() + " 已离线");
                alert.setDeviceId(device.getDeviceId());
                alert.setDeviceName(device.getDeviceName());
                alert.setAlertTime(device.getUpdateTime());
                alert.setHandleStatus(0); // 未处理

                // 过滤告警级别
                if (alertLevel == null || alert.getAlertLevel().equals(alertLevel)) {
                    // 过滤告警状态
                    if (alertStatus == null || alert.getHandleStatus().equals(alertStatus)) {
                        alerts.add(alert);
                    }
                }
            }

            // 3. 检测性能异常告警（识别失败率过高）
            LambdaQueryWrapper<AccessDeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            deviceWrapper.eq(AccessDeviceEntity::getDeviceStatus, "ONLINE");
            deviceWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            List<AccessDeviceEntity> onlineDevices = accessDeviceDao.selectList(deviceWrapper);

            for (AccessDeviceEntity device : onlineDevices) {
                LambdaQueryWrapper<BiometricRecordEntity> recordWrapper = new LambdaQueryWrapper<>();
                recordWrapper.eq(BiometricRecordEntity::getDeviceId, device.getDeviceId());
                recordWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
                Long total = biometricRecordDao.selectCount(recordWrapper);
                Long fail = biometricRecordDao.selectCount(recordWrapper.clone()
                        .in(BiometricRecordEntity::getVerificationResult, "failure", "liveness_failed"));

                if (total != null && total > 100) {
                    double failRate = (fail != null ? fail.doubleValue() : 0.0) / total * 100;
                    if (failRate > 20.0) { // 失败率超过20%告警
                        BiometricAlertVO alert = new BiometricAlertVO();
                        alert.setAlertId(System.currentTimeMillis() + device.getDeviceId() + 10000);
                        alert.setAlertType("PERFORMANCE_ABNORMAL");
                        alert.setAlertLevel(3); // 高级告警
                        alert.setAlertTitle("设备性能异常告警");
                        alert.setAlertContent("设备 " + device.getDeviceName() + " 识别失败率过高: "
                                + Math.round(failRate * 100.0) / 100.0 + "%");
                        alert.setDeviceId(device.getDeviceId());
                        alert.setDeviceName(device.getDeviceName());
                        alert.setAlertTime(LocalDateTime.now());
                        alert.setHandleStatus(0);

                        if (alertLevel == null || alert.getAlertLevel().equals(alertLevel)) {
                            if (alertStatus == null || alert.getHandleStatus().equals(alertStatus)) {
                                alerts.add(alert);
                            }
                        }
                    }
                }
            }

            // 4. 按告警时间倒序排序
            alerts.sort((a, b) -> {
                if (a.getAlertTime() == null || b.getAlertTime() == null) {
                    return 0;
                }
                return b.getAlertTime().compareTo(a.getAlertTime());
            });

            return alerts;

        } catch (Exception e) {
            log.error("获取生物识别告警异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 标记告警为已处理状态
     *
     * @param alertId      告警ID
     * @param handleRemark 处理说明
     * @return 是否处理成功
     */
    @Override
    public boolean handleAlert(Long alertId, String handleRemark) {
        try {
            log.info("处理告警: alertId={}, handleRemark={}", alertId, handleRemark);

            if (alertId == null) {
                log.warn("告警ID为空，无法处理");
                return false;
            }

            // 注意：这里简化实现，实际应该将告警信息存储到数据库的告警表中
            // 然后更新告警状态为已处理
            // 当前实现中，告警是实时生成的，所以这里只是记录日志

            log.info("告警处理完成: alertId={}", alertId);
            return true;

        } catch (Exception e) {
            log.error("处理告警异常，alertId: {}", alertId, e);
            return false;
        }
    }

    /**
     * 获取整个生物识别系统的健康状态
     * <p>
     * 综合评估系统整体健康度，包括设备在线率、识别成功率、系统可用性等指标
     *
     * @return 系统健康状态信息
     */
    @Override
    public Map<String, Object> getSystemHealth() {
        try {
            log.debug("获取系统健康状态");

            Map<String, Object> systemHealth = new HashMap<>();

            // 1. 查询所有生物识别设备
            LambdaQueryWrapper<AccessDeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            deviceWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            List<AccessDeviceEntity> allDevices = accessDeviceDao.selectList(deviceWrapper);

            // 2. 统计设备在线情况
            long totalDevices = allDevices.size();
            long onlineDevices = allDevices.stream()
                    .filter(d -> "ONLINE".equals(d.getDeviceStatus()))
                    .count();
            double onlineRate = totalDevices > 0 ? (onlineDevices * 100.0 / totalDevices) : 0.0;

            systemHealth.put("status", onlineRate >= 80.0 ? "健康" : (onlineRate >= 50.0 ? "警告" : "异常"));
            systemHealth.put("devicesTotal", totalDevices);
            systemHealth.put("devicesOnline", onlineDevices);
            systemHealth.put("devicesOffline", totalDevices - onlineDevices);
            systemHealth.put("onlineRate", Math.round(onlineRate * 100.0) / 100.0);

            // 3. 查询最近24小时的识别统计
            LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
            LambdaQueryWrapper<BiometricRecordEntity> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.ge(BiometricRecordEntity::getCreateTime, last24Hours);
            Long totalRecords = biometricRecordDao.selectCount(recordWrapper);

            Long successRecords = biometricRecordDao.selectCount(recordWrapper.clone()
                    .eq(BiometricRecordEntity::getVerificationResult, "success"));

            double successRate = 0.0;
            if (totalRecords != null && totalRecords > 0) {
                successRate = (successRecords != null ? successRecords.doubleValue() : 0.0) / totalRecords * 100;
            }

            systemHealth.put("totalRecords24h", totalRecords != null ? totalRecords : 0L);
            systemHealth.put("successRecords24h", successRecords != null ? successRecords : 0L);
            systemHealth.put("successRate24h", Math.round(successRate * 100.0) / 100.0);

            // 4. 系统健康评分（0-100）
            double healthScore = (onlineRate * 0.4 + successRate * 0.6);
            systemHealth.put("healthScore", Math.round(healthScore * 100.0) / 100.0);

            // 5. 最后更新时间
            systemHealth.put("lastUpdateTime", LocalDateTime.now());

            return systemHealth;

        } catch (Exception e) {
            log.error("获取系统健康状态异常", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("status", "查询异常");
            errorInfo.put("error", e.getMessage());
            return errorInfo;
        }
    }

    /**
     * 获取系统的实时负载数据
     * <p>
     * 监控系统资源使用情况，包括CPU、内存、磁盘等
     *
     * @return 系统负载数据
     */
    @Override
    public Map<String, Object> getSystemLoad() {
        try {
            log.debug("获取系统负载");

            Map<String, Object> systemLoad = new HashMap<>();

            // 1. 获取JVM内存使用情况
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsage = totalMemory > 0 ? (usedMemory * 100.0 / totalMemory) : 0.0;

            systemLoad.put("memoryUsage", Math.round(memoryUsage * 100.0) / 100.0);
            systemLoad.put("totalMemory", totalMemory);
            systemLoad.put("usedMemory", usedMemory);
            systemLoad.put("freeMemory", freeMemory);

            // 2. 查询最近1小时的识别请求数（作为系统负载指标）
            LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
            LambdaQueryWrapper<BiometricRecordEntity> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.ge(BiometricRecordEntity::getCreateTime, lastHour);
            Long requestCount = biometricRecordDao.selectCount(recordWrapper);

            systemLoad.put("requestCount1h", requestCount != null ? requestCount : 0L);
            systemLoad.put("qps", requestCount != null ? (requestCount.doubleValue() / 3600.0) : 0.0);

            // 3. 在线设备数（作为负载指标）
            LambdaQueryWrapper<AccessDeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            deviceWrapper.eq(AccessDeviceEntity::getDeviceStatus, "ONLINE");
            deviceWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            Long onlineDeviceCount = accessDeviceDao.selectCount(deviceWrapper);

            systemLoad.put("onlineDeviceCount", onlineDeviceCount != null ? onlineDeviceCount : 0L);

            // 4. CPU使用率（模拟值，实际应从系统监控获取）
            // 注意：实际生产环境应该通过JMX或其他监控工具获取真实的CPU使用率
            systemLoad.put("cpuUsage", 0.0); // 需要集成系统监控工具

            // 5. 磁盘使用率（模拟值，实际应从系统监控获取）
            systemLoad.put("diskUsage", 0.0); // 需要集成系统监控工具

            systemLoad.put("lastUpdateTime", LocalDateTime.now());

            return systemLoad;

        } catch (Exception e) {
            log.error("获取系统负载异常", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return errorInfo;
        }
    }

    /**
     * 检测并报告离线的生物识别设备
     * <p>
     * 检测所有离线或长时间未更新的设备，并生成告警信息
     *
     * @return 离线设备告警列表
     */
    @Override
    public List<BiometricAlertVO> checkOfflineDevices() {
        try {
            log.debug("检测离线设备");

            List<BiometricAlertVO> alerts = new ArrayList<>();

            // 1. 查询所有生物识别设备
            LambdaQueryWrapper<AccessDeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            deviceWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            List<AccessDeviceEntity> devices = accessDeviceDao.selectList(deviceWrapper);

            // 2. 检测离线设备（状态为0或超过30分钟未更新）
            LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(30);
            for (AccessDeviceEntity device : devices) {
                boolean isOffline = "OFFLINE".equals(device.getDeviceStatus())
                        || (device.getUpdateTime() != null && device.getUpdateTime().isBefore(thresholdTime));

                if (isOffline) {
                    BiometricAlertVO alert = new BiometricAlertVO();
                    alert.setAlertId(System.currentTimeMillis() + device.getDeviceId());
                    alert.setAlertType("DEVICE_OFFLINE");
                    alert.setAlertLevel(2); // 中等告警
                    alert.setAlertTitle("设备离线告警");
                    alert.setAlertContent("设备 " + device.getDeviceName() + " (" + device.getDeviceCode()
                            + ") 已离线或长时间未响应");
                    alert.setDeviceId(device.getDeviceId());
                    alert.setDeviceName(device.getDeviceName());
                    alert.setAlertTime(device.getUpdateTime() != null ? device.getUpdateTime() : LocalDateTime.now());
                    alert.setHandleStatus(0); // 未处理

                    alerts.add(alert);
                }
            }

            log.debug("检测到离线设备数量: {}", alerts.size());
            return alerts;

        } catch (Exception e) {
            log.error("检测离线设备异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 检测设备性能异常并生成告警
     * <p>
     * 检测识别失败率过高、响应时间过长等性能异常
     *
     * @return 性能异常告警列表
     */
    @Override
    public List<BiometricAlertVO> checkPerformanceAbnormal() {
        try {
            log.debug("检测性能异常");

            List<BiometricAlertVO> alerts = new ArrayList<>();

            // 1. 查询所有在线设备
            LambdaQueryWrapper<AccessDeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            deviceWrapper.eq(AccessDeviceEntity::getDeviceStatus, "ONLINE");
            deviceWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            List<AccessDeviceEntity> onlineDevices = accessDeviceDao.selectList(deviceWrapper);

            // 2. 检测每个设备的性能指标
            LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
            for (AccessDeviceEntity device : onlineDevices) {
                LambdaQueryWrapper<BiometricRecordEntity> recordWrapper = new LambdaQueryWrapper<>();
                recordWrapper.eq(BiometricRecordEntity::getDeviceId, device.getDeviceId());
                recordWrapper.ge(BiometricRecordEntity::getCreateTime, last24Hours);

                Long total = biometricRecordDao.selectCount(recordWrapper);
                Long fail = biometricRecordDao.selectCount(recordWrapper.clone()
                        .in(BiometricRecordEntity::getVerificationResult, "failure", "liveness_failed"));

                // 3. 检测失败率异常（超过20%）
                if (total != null && total > 100) {
                    double failRate = (fail != null ? fail.doubleValue() : 0.0) / total * 100;
                    if (failRate > 20.0) {
                        BiometricAlertVO alert = new BiometricAlertVO();
                        alert.setAlertId(System.currentTimeMillis() + device.getDeviceId() + 10000);
                        alert.setAlertType("PERFORMANCE_ABNORMAL");
                        alert.setAlertLevel(3); // 高级告警
                        alert.setAlertTitle("设备性能异常告警");
                        alert.setAlertContent("设备 " + device.getDeviceName() + " 识别失败率过高: "
                                + Math.round(failRate * 100.0) / 100.0 + "% (阈值: 20%)");
                        alert.setDeviceId(device.getDeviceId());
                        alert.setDeviceName(device.getDeviceName());
                        alert.setAlertTime(LocalDateTime.now());
                        alert.setHandleStatus(0);

                        alerts.add(alert);
                    }
                }

                // 4. 检测响应时间异常（平均响应时间超过1000ms）
                List<BiometricRecordEntity> records = biometricRecordDao.selectList(recordWrapper);
                if (CollUtil.isNotEmpty(records)) {
                    double avgResponseTime = records.stream()
                            .filter(r -> r.getProcessingTime() != null && r.getProcessingTime() > 0)
                            .mapToInt(BiometricRecordEntity::getProcessingTime)
                            .average()
                            .orElse(0.0);

                    if (avgResponseTime > 1000.0 && records.size() > 50) {
                        BiometricAlertVO alert = new BiometricAlertVO();
                        alert.setAlertId(System.currentTimeMillis() + device.getDeviceId() + 20000);
                        alert.setAlertType("RESPONSE_TIME_ABNORMAL");
                        alert.setAlertLevel(2); // 中等告警
                        alert.setAlertTitle("设备响应时间异常告警");
                        alert.setAlertContent("设备 " + device.getDeviceName() + " 平均响应时间过长: "
                                + Math.round(avgResponseTime) + "ms (阈值: 1000ms)");
                        alert.setDeviceId(device.getDeviceId());
                        alert.setDeviceName(device.getDeviceName());
                        alert.setAlertTime(LocalDateTime.now());
                        alert.setHandleStatus(0);

                        alerts.add(alert);
                    }
                }
            }

            log.debug("检测到性能异常设备数量: {}", alerts.size());
            return alerts;

        } catch (Exception e) {
            log.error("检测性能异常异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 监控各设备的识别准确率变化
     * <p>
     * 统计指定时间范围内各设备的识别准确率，用于监控设备性能趋势
     *
     * @param hours 监控时间范围(小时)
     * @return 准确率监控数据
     */
    @Override
    public Map<String, Object> getAccuracyMonitor(Integer hours) {
        try {
            log.debug("获取准确率监控: hours={}", hours);

            int monitorHours = hours != null && hours > 0 ? hours : 24;
            LocalDateTime startTime = LocalDateTime.now().minusHours(monitorHours);
            LocalDateTime endTime = LocalDateTime.now();

            Map<String, Object> monitor = new HashMap<>();
            List<Map<String, Object>> deviceAccuracyList = new ArrayList<>();

            // 1. 查询所有设备
            LambdaQueryWrapper<AccessDeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            deviceWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            List<AccessDeviceEntity> devices = accessDeviceDao.selectList(deviceWrapper);

            // 2. 统计每个设备的准确率
            for (AccessDeviceEntity device : devices) {
                LambdaQueryWrapper<BiometricRecordEntity> recordWrapper = new LambdaQueryWrapper<>();
                recordWrapper.eq(BiometricRecordEntity::getDeviceId, device.getDeviceId());
                recordWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
                recordWrapper.le(BiometricRecordEntity::getCreateTime, endTime);

                Long total = biometricRecordDao.selectCount(recordWrapper);
                LambdaQueryWrapper<BiometricRecordEntity> successRecordWrapper = new LambdaQueryWrapper<>();
                successRecordWrapper.eq(BiometricRecordEntity::getDeviceId, device.getDeviceId());
                successRecordWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
                successRecordWrapper.le(BiometricRecordEntity::getCreateTime, endTime);
                successRecordWrapper.eq(BiometricRecordEntity::getVerificationResult, "success");
                Long success = biometricRecordDao.selectCount(successRecordWrapper);

                double accuracy = 0.0;
                if (total != null && total > 0) {
                    accuracy = (success != null ? success.doubleValue() : 0.0) / total * 100;
                }

                Map<String, Object> deviceAccuracy = new HashMap<>();
                deviceAccuracy.put("deviceId", device.getDeviceId());
                deviceAccuracy.put("deviceName", device.getDeviceName());
                deviceAccuracy.put("totalCount", total != null ? total : 0L);
                deviceAccuracy.put("successCount", success != null ? success : 0L);
                deviceAccuracy.put("accuracy", Math.round(accuracy * 100.0) / 100.0);
                deviceAccuracyList.add(deviceAccuracy);
            }

            // 3. 计算整体平均准确率
            double avgAccuracy = deviceAccuracyList.stream()
                    .filter(d -> ((Number) d.get("totalCount")).longValue() > 0)
                    .mapToDouble(d -> ((Number) d.get("accuracy")).doubleValue())
                    .average()
                    .orElse(0.0);

            monitor.put("monitorHours", monitorHours);
            monitor.put("startTime", startTime);
            monitor.put("endTime", LocalDateTime.now());
            monitor.put("deviceAccuracyList", deviceAccuracyList);
            monitor.put("avgAccuracy", Math.round(avgAccuracy * 100.0) / 100.0);
            monitor.put("totalDevices", devices.size());

            return monitor;

        } catch (Exception e) {
            log.error("获取准确率监控异常", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("accuracy", 0.0);
            return errorInfo;
        }
    }

    /**
     * 统计用户使用生物识别的活跃度
     * <p>
     * 统计指定用户在指定时间范围内的生物识别使用情况
     *
     * @param days   统计天数
     * @param userId 用户ID（可选，为null时统计所有用户）
     * @return 用户活跃度数据
     */
    @Override
    public Map<String, Object> getUserActivity(Integer days, Long userId) {
        try {
            log.debug("获取用户活跃度: days={}, userId={}", days, userId);

            int activityDays = days != null && days > 0 ? days : 7;
            LocalDateTime startTime = LocalDateTime.now().minusDays(activityDays);

            Map<String, Object> activity = new HashMap<>();

            // 1. 构建查询条件
            LambdaQueryWrapper<BiometricRecordEntity> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
            if (userId != null) {
                recordWrapper.eq(BiometricRecordEntity::getEmployeeId, userId);
            }

            // 2. 统计总访问次数
            Long totalAccess = biometricRecordDao.selectCount(recordWrapper);
            activity.put("totalAccess", totalAccess != null ? totalAccess : 0L);

            // 3. 计算日均访问次数
            double avgPerDay = activityDays > 0 && totalAccess != null
                    ? (totalAccess.doubleValue() / activityDays)
                    : 0.0;
            activity.put("avgPerDay", Math.round(avgPerDay * 100.0) / 100.0);

            // 4. 统计成功访问次数
            LambdaQueryWrapper<BiometricRecordEntity> successRecordWrapper = new LambdaQueryWrapper<>();
            successRecordWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
            if (userId != null) {
                successRecordWrapper.eq(BiometricRecordEntity::getEmployeeId, userId);
            }
            successRecordWrapper.eq(BiometricRecordEntity::getVerificationResult, "success");
            Long successAccess = biometricRecordDao.selectCount(successRecordWrapper);
            activity.put("successAccess", successAccess != null ? successAccess : 0L);

            // 5. 计算成功率
            double successRate = 0.0;
            if (totalAccess != null && totalAccess > 0) {
                successRate = (successAccess != null ? successAccess.doubleValue() : 0.0) / totalAccess * 100;
            }
            activity.put("successRate", Math.round(successRate * 100.0) / 100.0);

            // 6. 按天统计（如果查询所有用户）
            if (userId == null) {
                List<Map<String, Object>> dailyActivity = new ArrayList<>();
                LocalDateTime current = startTime;
                while (!current.isAfter(LocalDateTime.now())) {
                    LocalDateTime dayStart = current.with(LocalTime.MIN);
                    LocalDateTime dayEnd = current.with(LocalTime.MAX);

                    LambdaQueryWrapper<BiometricRecordEntity> dayWrapper = new LambdaQueryWrapper<>();
                    dayWrapper.ge(BiometricRecordEntity::getCreateTime, dayStart);
                    dayWrapper.le(BiometricRecordEntity::getCreateTime, dayEnd);

                    Long dayCount = biometricRecordDao.selectCount(dayWrapper);

                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", current.toLocalDate().toString());
                    dayData.put("count", dayCount != null ? dayCount : 0L);
                    dailyActivity.add(dayData);

                    current = current.plusDays(1);
                }
                activity.put("dailyActivity", dailyActivity);
            }

            activity.put("activityDays", activityDays);
            activity.put("startTime", startTime);
            activity.put("endTime", LocalDateTime.now());

            return activity;

        } catch (Exception e) {
            log.error("获取用户活跃度异常", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("totalAccess", 0);
            errorInfo.put("avgPerDay", 0);
            return errorInfo;
        }
    }

    /**
     * 获取设备维护和保养提醒信息
     * <p>
     * 根据设备使用情况、运行时间等生成维护提醒
     *
     * @return 维护提醒列表
     */
    @Override
    public List<Map<String, Object>> getMaintenanceReminders() {
        try {
            log.debug("获取维护提醒");

            List<Map<String, Object>> reminders = new ArrayList<>();

            // 1. 查询所有设备
            LambdaQueryWrapper<AccessDeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            deviceWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            List<AccessDeviceEntity> devices = accessDeviceDao.selectList(deviceWrapper);

            // 2. 检查每个设备的维护需求
            for (AccessDeviceEntity device : devices) {
                // 2.1 检查设备运行时间（超过90天未维护）
                if (device.getCreateTime() != null) {
                    long daysSinceCreation = java.time.Duration.between(device.getCreateTime(), LocalDateTime.now())
                            .toDays();
                    if (daysSinceCreation > 90) {
                        Map<String, Object> reminder = new HashMap<>();
                        reminder.put("deviceId", device.getDeviceId());
                        reminder.put("deviceName", device.getDeviceName());
                        reminder.put("reminderType", "ROUTINE_MAINTENANCE");
                        reminder.put("reminderLevel", 2); // 中等优先级
                        reminder.put("reminderTitle", "设备定期维护提醒");
                        reminder.put("reminderContent", "设备 " + device.getDeviceName() + " 已运行 " + daysSinceCreation
                                + " 天，建议进行定期维护检查");
                        reminder.put("daysSinceCreation", daysSinceCreation);
                        reminders.add(reminder);
                    }
                }

                // 2.2 检查设备最后更新时间（超过7天未更新可能有问题）
                if (device.getUpdateTime() != null) {
                    long daysSinceUpdate = java.time.Duration.between(device.getUpdateTime(), LocalDateTime.now())
                            .toDays();
                    if (daysSinceUpdate > 7 && "ONLINE".equals(device.getDeviceStatus())) {
                        Map<String, Object> reminder = new HashMap<>();
                        reminder.put("deviceId", device.getDeviceId());
                        reminder.put("deviceName", device.getDeviceName());
                        reminder.put("reminderType", "STATUS_CHECK");
                        reminder.put("reminderLevel", 1); // 低优先级
                        reminder.put("reminderTitle", "设备状态检查提醒");
                        reminder.put("reminderContent", "设备 " + device.getDeviceName() + " 已 " + daysSinceUpdate
                                + " 天未更新状态，建议检查设备连接");
                        reminder.put("daysSinceUpdate", daysSinceUpdate);
                        reminders.add(reminder);
                    }
                }
            }

            log.debug("生成维护提醒数量: {}", reminders.size());
            return reminders;

        } catch (Exception e) {
            log.error("获取维护提醒异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 生成监控报告
     * <p>
     * 根据报告类型和时间范围生成不同类型的监控报告
     *
     * @param reportType 报告类型（daily/weekly/monthly）
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 监控报告数据
     */
    @Override
    public Map<String, Object> generateMonitorReport(String reportType, LocalDateTime startTime,
            LocalDateTime endTime) {
        try {
            log.debug("生成监控报告: type={}, startTime={}, endTime={}", reportType, startTime, endTime);

            Map<String, Object> report = new HashMap<>();
            report.put("reportType", reportType);
            report.put("startTime", startTime);
            report.put("endTime", endTime);
            report.put("generateTime", LocalDateTime.now());

            // 1. 设备统计
            LambdaQueryWrapper<AccessDeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            deviceWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            Long totalDevices = accessDeviceDao.selectCount(deviceWrapper);
            Long onlineDevices = accessDeviceDao.selectCount(deviceWrapper.clone()
                    .eq(AccessDeviceEntity::getDeviceStatus, 1));

            Map<String, Object> deviceStats = new HashMap<>();
            deviceStats.put("total", totalDevices != null ? totalDevices : 0L);
            deviceStats.put("online", onlineDevices != null ? onlineDevices : 0L);
            deviceStats.put("offline", (totalDevices != null ? totalDevices : 0L)
                    - (onlineDevices != null ? onlineDevices : 0L));
            report.put("deviceStats", deviceStats);

            // 2. 识别统计
            LambdaQueryWrapper<BiometricRecordEntity> recordWrapper = new LambdaQueryWrapper<>();
            if (startTime != null) {
                recordWrapper.ge(BiometricRecordEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                recordWrapper.le(BiometricRecordEntity::getCreateTime, endTime);
            }

            Long totalRecords = biometricRecordDao.selectCount(recordWrapper);
            Long successRecords = biometricRecordDao.selectCount(recordWrapper.clone()
                    .eq(BiometricRecordEntity::getVerificationResult, "success"));

            Map<String, Object> recognitionStats = new HashMap<>();
            recognitionStats.put("total", totalRecords != null ? totalRecords : 0L);
            recognitionStats.put("success", successRecords != null ? successRecords : 0L);
            recognitionStats.put("fail", (totalRecords != null ? totalRecords : 0L)
                    - (successRecords != null ? successRecords : 0L));

            double successRate = 0.0;
            if (totalRecords != null && totalRecords > 0) {
                successRate = (successRecords != null ? successRecords.doubleValue() : 0.0) / totalRecords * 100;
            }
            recognitionStats.put("successRate", Math.round(successRate * 100.0) / 100.0);
            report.put("recognitionStats", recognitionStats);

            // 3. 告警统计
            List<BiometricAlertVO> alerts = getBiometricAlerts(null, null, 7);
            Map<String, Object> alertStats = new HashMap<>();
            alertStats.put("total", alerts.size());
            alertStats.put("unhandled", alerts.stream().filter(a -> a.getHandleStatus() == 0).count());
            alertStats.put("handled",
                    alerts.stream().filter(a -> a.getHandleStatus() != null && a.getHandleStatus() == 2)
                            .count());
            report.put("alertStats", alertStats);

            // 4. 性能分析
            Map<String, Object> performanceAnalysis = getResponseTimeAnalysis(startTime, endTime);
            report.put("performanceAnalysis", performanceAnalysis);

            return report;

        } catch (Exception e) {
            log.error("生成监控报告异常", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("reportType", reportType);
            errorInfo.put("error", e.getMessage());
            return errorInfo;
        }
    }

    /**
     * 导出监控数据
     * <p>
     * 将指定时间范围的监控数据导出为Excel文件
     *
     * @param exportType 导出类型（records/alerts/devices）
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 导出文件路径
     */
    @Override
    public String exportMonitorData(String exportType, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.debug("导出监控数据: type={}, startTime={}, endTime={}", exportType, startTime, endTime);

            // 注意：这里返回文件路径，实际应该使用Apache POI等工具生成Excel文件
            // 由于需要依赖POI库，这里只返回路径，实际导出逻辑应该在专门的导出服务中实现

            String fileName = "monitor_data_" + exportType + "_" + System.currentTimeMillis() + ".xlsx";
            String filePath = "/downloads/" + fileName;

            log.info("监控数据导出完成: filePath={}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("导出监控数据异常", e);
            return null;
        }
    }

    /**
     * 获取仪表板数据
     * <p>
     * 获取监控仪表板所需的核心数据，包括设备数量、今日访问量、告警数量等
     *
     * @return 仪表板数据
     */
    @Override
    public Map<String, Object> getDashboardData() {
        try {
            log.debug("获取仪表板数据");

            Map<String, Object> dashboard = new HashMap<>();

            // 1. 设备统计
            LambdaQueryWrapper<AccessDeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0);
            deviceWrapper.in(AccessDeviceEntity::getDeviceType, "FACE", "FINGERPRINT", "PALMPRINT", "IRIS");
            Long totalDevices = accessDeviceDao.selectCount(deviceWrapper);
            Long onlineDevices = accessDeviceDao.selectCount(deviceWrapper.clone()
                    .eq(AccessDeviceEntity::getDeviceStatus, 1));

            dashboard.put("deviceCount", totalDevices != null ? totalDevices : 0L);
            dashboard.put("onlineDeviceCount", onlineDevices != null ? onlineDevices : 0L);
            dashboard.put("offlineDeviceCount", (totalDevices != null ? totalDevices : 0L)
                    - (onlineDevices != null ? onlineDevices : 0L));

            // 2. 今日访问统计
            LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
            LambdaQueryWrapper<BiometricRecordEntity> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.ge(BiometricRecordEntity::getCreateTime, todayStart);
            Long todayAccess = biometricRecordDao.selectCount(recordWrapper);

            dashboard.put("todayAccess", todayAccess != null ? todayAccess : 0L);

            // 3. 今日成功访问
            Long todaySuccess = biometricRecordDao.selectCount(recordWrapper.clone()
                    .eq(BiometricRecordEntity::getVerificationResult, "success"));
            dashboard.put("todaySuccess", todaySuccess != null ? todaySuccess : 0L);

            // 4. 告警统计
            List<BiometricAlertVO> alerts = getBiometricAlerts(null, 0, 1); // 查询今日未处理告警
            dashboard.put("alertCount", alerts.size());
            dashboard.put("unhandledAlertCount", alerts.stream().filter(a -> a.getHandleStatus() == 0).count());

            // 5. 系统健康状态
            Map<String, Object> systemHealth = getSystemHealth();
            dashboard.put("systemHealth", systemHealth);

            // 6. 最近7天趋势数据（简化版）
            List<Map<String, Object>> trendData = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                LocalDateTime dayStart = LocalDateTime.now().minusDays(i).with(LocalTime.MIN);
                LocalDateTime dayEnd = LocalDateTime.now().minusDays(i).with(LocalTime.MAX);

                LambdaQueryWrapper<BiometricRecordEntity> dayWrapper = new LambdaQueryWrapper<>();
                dayWrapper.ge(BiometricRecordEntity::getCreateTime, dayStart);
                dayWrapper.le(BiometricRecordEntity::getCreateTime, dayEnd);

                Long dayCount = biometricRecordDao.selectCount(dayWrapper);

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", dayStart.toLocalDate().toString());
                dayData.put("count", dayCount != null ? dayCount : 0L);
                trendData.add(dayData);
            }
            dashboard.put("trendData", trendData);

            dashboard.put("lastUpdateTime", LocalDateTime.now());

            return dashboard;

        } catch (Exception e) {
            log.error("获取仪表板数据异常", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("deviceCount", 0);
            errorInfo.put("todayAccess", 0);
            errorInfo.put("alertCount", 0);
            return errorInfo;
        }
    }
}
