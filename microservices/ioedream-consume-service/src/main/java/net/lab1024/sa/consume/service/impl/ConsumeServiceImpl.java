package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.domain.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.domain.form.ConsumeTransactionForm;
import net.lab1024.sa.consume.domain.form.ConsumeTransactionQueryForm;
import net.lab1024.sa.consume.domain.request.ConsumeRequest;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceStatisticsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceVO;
import net.lab1024.sa.consume.domain.vo.ConsumeRealtimeStatisticsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionDetailVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO;
import net.lab1024.sa.consume.manager.ConsumeDeviceManager;
import net.lab1024.sa.consume.manager.ConsumeExecutionManager;
import net.lab1024.sa.consume.service.ConsumeCacheService;
import net.lab1024.sa.consume.service.ConsumeService;
import net.lab1024.sa.common.domain.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 消费服务实现类
 * <p>
 * 实现消费相关的核心业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-consume-service中
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 严格遵循四层架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 消费交易执行
 * - 设备管理
 * - 实时统计
 * - 消费记录查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeServiceImpl implements ConsumeService {

    @Resource
    private ConsumeExecutionManager consumeExecutionManager;

    @Resource
    private ConsumeDeviceManager consumeDeviceManager;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private ConsumeTransactionDao consumeTransactionDao;

    @Resource
    private ConsumeCacheService consumeCacheService;

    /**
     * 执行消费交易
     *
     * @param form 消费交易表单
     * @return 交易结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsumeTransactionResultVO executeTransaction(ConsumeTransactionForm form) {
        log.info("[消费服务] 开始执行消费交易，userId={}, deviceId={}, areaId={}",
                form.getUserId(), form.getDeviceId(), form.getAreaId());

        try {
            // 参数验证
            if (form.getUserId() == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            if (form.getDeviceId() == null) {
                throw new IllegalArgumentException("设备ID不能为空");
            }
            if (form.getAreaId() == null) {
                throw new IllegalArgumentException("区域ID不能为空");
            }

            // 通过执行管理器执行消费流程
            net.lab1024.sa.common.dto.ResponseDTO<?> response = consumeExecutionManager.executeConsumption(form);
            if (response == null || !response.isSuccess()) {
                ConsumeTransactionResultVO result = new ConsumeTransactionResultVO();
                result.setTransactionStatus(3); // 失败
                result.setErrorMessage(response != null ? response.getMessage() : "执行消费交易失败");
                return result;
            }

            // 查询交易记录
            ConsumeTransactionEntity transaction = null;
            if (form.getTransactionNo() != null) {
                transaction = consumeTransactionDao.selectByTransactionNo(form.getTransactionNo());
            }

            // 构建返回结果
            ConsumeTransactionResultVO result = new ConsumeTransactionResultVO();
            if (transaction != null) {
                result.setTransactionId(transaction.getId());
                result.setTransactionNo(transaction.getTransactionNo());
                result.setTransactionStatus(transaction.getTransactionStatus());
                result.setAmount(transaction.getAmount());
                result.setBalanceAfter(transaction.getBalanceAfter() != null ?
                        java.math.BigDecimal.valueOf(transaction.getBalanceAfter()).divide(
                                java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP) :
                        java.math.BigDecimal.ZERO);
                result.setTransactionTime(transaction.getTransactionTime());
            } else {
                result.setTransactionStatus(2); // 成功（假设）
            }

            log.info("[消费服务] 执行消费交易成功，transactionNo={}", result.getTransactionNo());
            return result;

        } catch (Exception e) {
            log.error("[消费服务] 执行消费交易失败", e);
            ConsumeTransactionResultVO result = new ConsumeTransactionResultVO();
            result.setTransactionStatus(3); // 失败
            result.setErrorMessage(e.getMessage());
            return result;
        }
    }

    /**
     * 执行消费请求
     *
     * @param request 消费请求
     * @return 交易详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsumeTransactionDetailVO executeConsume(ConsumeRequest request) {
        log.info("[消费服务] 开始执行消费请求，userId={}, deviceId={}, areaId={}",
                request.getUserId(), request.getDeviceId(), request.getAreaId());

        try {
            // 转换为表单对象
            ConsumeTransactionForm form = new ConsumeTransactionForm();
            form.setUserId(request.getUserId());
            form.setAccountId(request.getAccountId());
            form.setDeviceId(request.getDeviceId());
            form.setAreaId(request.getAreaId());
            form.setAmount(request.getAmount());
            form.setConsumeMode(request.getConsumeMode());

            // 执行交易
            ConsumeTransactionResultVO result = executeTransaction(form);
            if (result.getTransactionStatus() != 2) {
                ConsumeTransactionDetailVO detail = new ConsumeTransactionDetailVO();
                detail.setTransactionStatus(result.getTransactionStatus());
                return detail;
            }

            // 查询交易详情
            ConsumeTransactionEntity transaction = null;
            if (result.getTransactionNo() != null) {
                transaction = consumeTransactionDao.selectByTransactionNo(result.getTransactionNo());
            }

            // 构建返回详情
            ConsumeTransactionDetailVO detail = new ConsumeTransactionDetailVO();
            if (transaction != null) {
                detail.setTransactionId(transaction.getId());
                detail.setTransactionNo(transaction.getTransactionNo());
                detail.setUserId(transaction.getUserId());
                detail.setUserName(transaction.getUserName());
                detail.setAccountId(transaction.getAccountId());
                detail.setAreaId(transaction.getAreaId());
                detail.setAreaName(transaction.getAreaName());
                detail.setDeviceId(transaction.getDeviceId());
                detail.setDeviceName(transaction.getDeviceName());
                detail.setAmount(transaction.getAmount());
                detail.setConsumeTime(transaction.getConsumeTime());
                detail.setConsumeMode(transaction.getConsumeMode());
                detail.setConsumeType(transaction.getConsumeType());
                detail.setTransactionStatus(transaction.getTransactionStatus());
            }

            log.info("[消费服务] 执行消费请求成功，transactionNo={}", detail.getTransactionNo());
            return detail;

        } catch (Exception e) {
            log.error("[消费服务] 执行消费请求失败", e);
            ConsumeTransactionDetailVO detail = new ConsumeTransactionDetailVO();
            detail.setTransactionStatus(3); // 失败
            return detail;
        }
    }

    /**
     * 获取设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    @Override
    @Transactional(readOnly = true)
    public ConsumeDeviceVO getDeviceDetail(Long deviceId) {
        log.info("[消费服务] 获取设备详情，deviceId={}", deviceId);

        try {
            DeviceEntity device = consumeDeviceManager.getConsumeDeviceById(deviceId);
            if (device == null) {
                log.warn("[消费服务] 设备不存在，deviceId={}", deviceId);
                return new ConsumeDeviceVO();
            }

            ConsumeDeviceVO vo = new ConsumeDeviceVO();
            vo.setDeviceId(deviceId);
            vo.setDeviceName(device.getDeviceName());
            vo.setDeviceCode(device.getDeviceCode());
            vo.setAreaId(device.getAreaId() != null ? device.getAreaId().toString() : null);
            // 设备状态：1-在线 2-离线 3-故障（从deviceStatus字段转换）
            String deviceStatus = device.getDeviceStatus();
            if ("ONLINE".equals(deviceStatus)) {
                vo.setDeviceStatus(1); // 在线
            } else if ("OFFLINE".equals(deviceStatus)) {
                vo.setDeviceStatus(2); // 离线
            } else if ("MAINTAIN".equals(deviceStatus)) {
                vo.setDeviceStatus(3); // 故障/维护中
            } else {
                vo.setDeviceStatus(2); // 默认离线
            }

            log.info("[消费服务] 获取设备详情成功，deviceId={}", deviceId);
            return vo;

        } catch (Exception e) {
            log.error("[消费服务] 获取设备详情失败，deviceId={}", deviceId, e);
            return new ConsumeDeviceVO();
        }
    }

    /**
     * 获取设备状态统计
     *
     * @param areaId 区域ID
     * @return 设备统计信息
     */
    @Override
    @Transactional(readOnly = true)
    public ConsumeDeviceStatisticsVO getDeviceStatistics(String areaId) {
        log.info("[消费服务] 获取设备状态统计，areaId={}", areaId);

        try {
            List<DeviceEntity> devices = consumeDeviceManager.getConsumeDevices(areaId, null);
            if (devices == null) {
                devices = new ArrayList<>();
            }

            ConsumeDeviceStatisticsVO statistics = new ConsumeDeviceStatisticsVO();
            statistics.setTotalDevices(devices.size());

            int onlineCount = 0;
            int offlineCount = 0;
            int faultCount = 0;
            for (DeviceEntity device : devices) {
                String deviceStatus = device.getDeviceStatus();
                if (deviceStatus == null || deviceStatus.isEmpty()) {
                    offlineCount++;
                } else if ("ONLINE".equals(deviceStatus)) {
                    onlineCount++;
                } else if ("OFFLINE".equals(deviceStatus)) {
                    offlineCount++;
                } else if ("MAINTAIN".equals(deviceStatus)) {
                    faultCount++;
                } else {
                    offlineCount++;
                }
            }

            statistics.setOnlineDevices(onlineCount);
            statistics.setOfflineDevices(offlineCount);
            statistics.setFaultDevices(faultCount);
            statistics.setTodayTransactionCount(0L); // 需要从统计数据中获取
            statistics.setTodayTransactionAmount(java.math.BigDecimal.ZERO); // 需要从统计数据中获取

            log.info("[消费服务] 获取设备状态统计成功，areaId={}, totalDevices={}", areaId, devices.size());
            return statistics;

        } catch (Exception e) {
            log.error("[消费服务] 获取设备状态统计失败，areaId={}", areaId, e);
            return new ConsumeDeviceStatisticsVO();
        }
    }

    /**
     * 获取实时统计
     *
     * @param areaId 区域ID
     * @return 实时统计数据
     */
    @Override
    @Transactional(readOnly = true)
    public ConsumeRealtimeStatisticsVO getRealtimeStatistics(String areaId) {
        log.info("[消费服务] 获取实时统计，areaId={}", areaId);

        try {
            // 从缓存获取实时统计数据
            String cacheKey = "consume:realtime:statistics:" + areaId;
            ConsumeRealtimeStatisticsVO statistics = consumeCacheService.get(cacheKey, ConsumeRealtimeStatisticsVO.class);
            if (statistics != null) {
                return statistics;
            }

            // 如果缓存中没有，查询数据库并构建统计数据
            ConsumeRealtimeStatisticsVO result = new ConsumeRealtimeStatisticsVO();

            // 实现实时统计查询逻辑
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            LocalDateTime lastHourStart = now.minusHours(1);

            // 1. 查询今日交易记录
            List<ConsumeTransactionEntity> todayTransactions = consumeTransactionDao.selectByTimeRange(todayStart, now);
            if (areaId != null && !areaId.trim().isEmpty()) {
                todayTransactions = todayTransactions.stream()
                        .filter(t -> areaId.equals(t.getAreaId()))
                        .collect(Collectors.toList());
            }

            // 2. 计算今日统计
            BigDecimal todayAmount = todayTransactions.stream()
                    .filter(t -> t.getFinalMoney() != null)
                    .map(t -> BigDecimal.valueOf(t.getFinalMoney()).divide(BigDecimal.valueOf(100))) // 转换为元
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int todayCount = todayTransactions.size();
            int currentConsumeUserCount = (int) todayTransactions.stream()
                    .map(ConsumeTransactionEntity::getUserId)
                    .distinct()
                    .count();

            // 3. 查询最近1小时交易记录
            List<ConsumeTransactionEntity> lastHourTransactions = todayTransactions.stream()
                    .filter(t -> t.getConsumeTime() != null && t.getConsumeTime().isAfter(lastHourStart))
                    .collect(Collectors.toList());

            BigDecimal lastHourAmount = lastHourTransactions.stream()
                    .filter(t -> t.getFinalMoney() != null)
                    .map(t -> BigDecimal.valueOf(t.getFinalMoney()).divide(BigDecimal.valueOf(100))) // 转换为元
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int lastHourCount = lastHourTransactions.size();

            // 4. 查询在线设备数
            List<DeviceEntity> devices = consumeDeviceManager.getConsumeDevices(areaId, null);
            int onlineDeviceCount = (int) devices.stream()
                    .filter(d -> "ONLINE".equals(d.getDeviceStatus()))
                    .count();

            // 5. 设置统计结果
            result.setTodayAmount(todayAmount);
            result.setTodayCount(todayCount);
            result.setOnlineDeviceCount(onlineDeviceCount);
            result.setCurrentConsumeUserCount(currentConsumeUserCount);
            result.setLastHourAmount(lastHourAmount);
            result.setLastHourCount(lastHourCount);

            // 6. 将结果存入缓存（缓存30分钟）
            consumeCacheService.set(cacheKey, result, 30 * 60);

            return result;

        } catch (Exception e) {
            log.error("[消费服务] 获取实时统计失败，areaId={}", areaId, e);
            ConsumeRealtimeStatisticsVO result = new ConsumeRealtimeStatisticsVO();
            result.setTodayAmount(java.math.BigDecimal.ZERO);
            result.setTodayCount(0);
            return result;
        }
    }

    /**
     * 获取交易详情
     *
     * @param transactionNo 交易流水号
     * @return 交易详情
     */
    @Override
    @Transactional(readOnly = true)
    public ConsumeTransactionDetailVO getTransactionDetail(String transactionNo) {
        log.info("[消费服务] 获取交易详情，transactionNo={}", transactionNo);

        try {
            // 根据交易流水号查询交易记录
            ConsumeTransactionEntity transaction = consumeTransactionDao.selectByTransactionNo(transactionNo);
            if (transaction == null) {
                log.warn("[消费服务] 交易记录不存在，transactionNo={}", transactionNo);
                ConsumeTransactionDetailVO detail = new ConsumeTransactionDetailVO();
                detail.setTransactionStatus(3); // 失败
                return detail;
            }

            // 构建交易详情
            ConsumeTransactionDetailVO detail = new ConsumeTransactionDetailVO();
            detail.setTransactionId(transaction.getId());
            detail.setTransactionNo(transaction.getTransactionNo());
            detail.setUserId(transaction.getUserId());
            detail.setUserName(transaction.getUserName());
            detail.setAccountId(transaction.getAccountId());
            detail.setAreaId(transaction.getAreaId());
            detail.setAreaName(transaction.getAreaName());
            detail.setDeviceId(transaction.getDeviceId());
            detail.setDeviceName(transaction.getDeviceName());
            detail.setAmount(transaction.getAmount());
            detail.setConsumeTime(transaction.getConsumeTime());
            detail.setConsumeMode(transaction.getConsumeMode());
            detail.setConsumeType(transaction.getConsumeType());
            detail.setTransactionStatus(transaction.getTransactionStatus() != null ? transaction.getTransactionStatus() : 2); // 默认成功

            log.info("[消费服务] 获取交易详情成功，transactionNo={}", transactionNo);
            return detail;

        } catch (Exception e) {
            log.error("[消费服务] 获取交易详情失败，transactionNo={}", transactionNo, e);
            ConsumeTransactionDetailVO detail = new ConsumeTransactionDetailVO();
            detail.setTransactionStatus(3); // 失败
            return detail;
        }
    }

    /**
     * 分页查询消费交易记录
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public PageResult<ConsumeTransactionDetailVO> queryTransactions(ConsumeTransactionQueryForm queryForm) {
        log.info("[消费服务] 分页查询消费交易记录，pageNum={}, pageSize={}, userId={}, areaId={}, startDate={}, endDate={}, consumeMode={}, status={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getUserId(), queryForm.getAreaId(),
                queryForm.getStartDate(), queryForm.getEndDate(), queryForm.getConsumeMode(), queryForm.getStatus());

        try {
            // 转换日期为LocalDateTime
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            if (queryForm.getStartDate() != null) {
                startTime = queryForm.getStartDate().atStartOfDay();
            }
            if (queryForm.getEndDate() != null) {
                endTime = queryForm.getEndDate().atTime(23, 59, 59);
            }

            // 执行分页查询
            Page<ConsumeTransactionEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            IPage<ConsumeTransactionEntity> pageResult = consumeTransactionDao.queryTransactions(
                    page, queryForm.getUserId(), queryForm.getAreaId(), startTime, endTime,
                    queryForm.getConsumeMode(), queryForm.getStatus());

            // 转换为VO列表
            List<ConsumeTransactionDetailVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToDetailVO)
                    .collect(Collectors.toList());

            // 构建分页结果
            PageResult<ConsumeTransactionDetailVO> result = PageResult.of(
                    voList,
                    pageResult.getTotal(),
                    queryForm.getPageNum(),
                    queryForm.getPageSize()
            );

            log.info("[消费服务] 分页查询消费交易记录成功，总数={}", pageResult.getTotal());
            return result;

        } catch (Exception e) {
            log.error("[消费服务] 分页查询消费交易记录失败", e);
            // 返回空结果
            return PageResult.of(new ArrayList<>(), 0L, queryForm.getPageNum(), queryForm.getPageSize());
        }
    }

    /**
     * 转换Entity为DetailVO
     *
     * @param entity 交易实体
     * @return 交易详情VO
     */
    private ConsumeTransactionDetailVO convertToDetailVO(ConsumeTransactionEntity entity) {
        ConsumeTransactionDetailVO vo = new ConsumeTransactionDetailVO();
        vo.setTransactionId(entity.getId());
        vo.setTransactionNo(entity.getTransactionNo());
        vo.setUserId(entity.getUserId());
        vo.setUserName(entity.getUserName());
        vo.setAccountId(entity.getAccountId());
        vo.setAreaId(entity.getAreaId());
        vo.setAreaName(entity.getAreaName());
        vo.setDeviceId(entity.getDeviceId());
        vo.setDeviceName(entity.getDeviceName());
        vo.setAmount(entity.getAmount());
        vo.setConsumeTime(entity.getConsumeTime());
        vo.setConsumeMode(entity.getConsumeMode());
        vo.setConsumeType(entity.getConsumeType());
        // 转换交易状态：status字符串转transactionStatus整数
        if (entity.getStatus() != null) {
            if ("SUCCESS".equals(entity.getStatus())) {
                vo.setTransactionStatus(2); // 成功
            } else if ("FAILED".equals(entity.getStatus())) {
                vo.setTransactionStatus(3); // 失败
            } else if ("REFUND".equals(entity.getStatus())) {
                vo.setTransactionStatus(4); // 已退款
            } else {
                vo.setTransactionStatus(entity.getTransactionStatus() != null ? entity.getTransactionStatus() : 2);
            }
        } else {
            vo.setTransactionStatus(entity.getTransactionStatus() != null ? entity.getTransactionStatus() : 2);
        }
        return vo;
    }
}
