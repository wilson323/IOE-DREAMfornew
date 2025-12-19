package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.domain.form.ConsumeTransactionForm;
import net.lab1024.sa.consume.domain.form.ConsumeTransactionQueryForm;
import net.lab1024.sa.consume.domain.request.ConsumeRequest;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceStatisticsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceVO;
import net.lab1024.sa.consume.domain.vo.ConsumeRealtimeStatisticsVO;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.vo.ConsumeRecordVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionDetailVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO;
import net.lab1024.sa.consume.manager.ConsumeDeviceManager;
import net.lab1024.sa.consume.manager.ConsumeExecutionManager;
import net.lab1024.sa.consume.service.ConsumeService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.util.CursorPagination;
import net.lab1024.sa.common.util.PageHelper;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    private net.lab1024.sa.consume.service.AccountService accountService;

    /**
     * 执行消费交易（关键业务方法）
     * <p>
     * 企业级容错和监控：
     * - Resilience4j熔断、重试、限流
     * - Micrometer性能监控
     * </p>
     *
     * @param form 消费交易表单
     * @return 交易结果
     */
    @Override
    @Observed(name = "consume.executeTransaction", contextualName = "consume-execute-transaction")
    @Transactional(rollbackFor = Exception.class)
    @CircuitBreaker(name = "consume-transaction-circuitbreaker", fallbackMethod = "executeTransactionFallback")
    @Retry(name = "consume-transaction-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "consume.transaction.execute", description = "消费交易执行耗时")
    @Counted(value = "consume.transaction.count", description = "消费交易执行次数")
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
                        transaction.getBalanceAfter().divide(
                                java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP) :
                        java.math.BigDecimal.ZERO);
                result.setTransactionTime(transaction.getTransactionTime());
            } else {
                result.setTransactionStatus(2); // 成功（假设）
            }

            log.info("[消费服务] 执行消费交易成功，transactionNo={}", result.getTransactionNo());
            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费服务] 执行消费交易参数错误: {}", e.getMessage());
            ConsumeTransactionResultVO result = new ConsumeTransactionResultVO();
            result.setTransactionStatus(3); // 失败
            result.setErrorMessage("参数错误: " + e.getMessage());
            return result;
        } catch (BusinessException e) {
            log.warn("[消费服务] 执行消费交易业务异常: code={}, message={}", e.getCode(), e.getMessage());
            ConsumeTransactionResultVO result = new ConsumeTransactionResultVO();
            result.setTransactionStatus(3); // 失败
            result.setErrorMessage(e.getMessage());
            return result;
        } catch (SystemException e) {
            log.error("[消费服务] 执行消费交易系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            ConsumeTransactionResultVO result = new ConsumeTransactionResultVO();
            result.setTransactionStatus(3); // 失败
            result.setErrorMessage("系统异常，请稍后重试");
            return result;
        } catch (Exception e) {
            log.error("[消费服务] 执行消费交易未知异常", e);
            // For methods returning VO objects, return failure result instead of throwing exception
            ConsumeTransactionResultVO result = new ConsumeTransactionResultVO();
            result.setTransactionStatus(3); // 失败
            result.setErrorMessage("系统异常，请稍后重试");
            return result;
        }
    }

    /**
     * 执行消费请求（关键业务方法）
     * <p>
     * 企业级容错和监控：
     * - Resilience4j熔断、重试、限流
     * - Micrometer性能监控
     * </p>
     *
     * @param request 消费请求
     * @return 交易详情
     */
    @Override
    @Observed(name = "consume.executeConsume", contextualName = "consume-execute-consume")
    @Transactional(rollbackFor = Exception.class)
    @CircuitBreaker(name = "consume-execute-circuitbreaker", fallbackMethod = "executeConsumeFallback")
    @Retry(name = "consume-execute-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "consume.execute", description = "消费请求执行耗时")
    @Counted(value = "consume.execute.count", description = "消费请求执行次数")
    public ConsumeTransactionDetailVO executeConsume(ConsumeRequest request) {
        log.info("[消费服务] 开始执行消费请求，userId={}, deviceId={}, areaId={}",
                request.getUserId(), request.getDeviceId(), request.getAreaId());

        try {
            // 转换为表单对象
            ConsumeTransactionForm form = new ConsumeTransactionForm();
            form.setUserId(request.getUserId());
            form.setAccountId(request.getAccountId());
            form.setDeviceId(request.getDeviceId()); // ConsumeRequest.deviceId已经是Long类型，直接使用
            form.setAreaId(request.getAreaId() != null ? String.valueOf(request.getAreaId()) : null);
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
                detail.setUserId(String.valueOf(transaction.getUserId()));
                detail.setUserName(transaction.getUserName());
                detail.setAccountId(String.valueOf(transaction.getAccountId()));
                detail.setAreaId(String.valueOf(transaction.getAreaId()));
                detail.setAreaName(transaction.getAreaName());
                detail.setDeviceId(String.valueOf(transaction.getDeviceId()));
                detail.setDeviceName(transaction.getDeviceName());
                detail.setAmount(transaction.getAmount());
                detail.setConsumeTime(transaction.getConsumeTime());
                detail.setConsumeMode(transaction.getConsumeMode());
                detail.setConsumeType(transaction.getConsumeType());
                detail.setTransactionStatus(transaction.getTransactionStatus());
            }

            log.info("[消费服务] 执行消费请求成功，transactionNo={}", detail.getTransactionNo());
            return detail;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费服务] 执行消费请求参数错误: {}", e.getMessage());
            ConsumeTransactionDetailVO detail = new ConsumeTransactionDetailVO();
            detail.setTransactionStatus(3); // 失败
            return detail;
        } catch (BusinessException e) {
            log.warn("[消费服务] 执行消费请求业务异常: code={}, message={}", e.getCode(), e.getMessage());
            ConsumeTransactionDetailVO detail = new ConsumeTransactionDetailVO();
            detail.setTransactionStatus(3); // 失败
            return detail;
        } catch (SystemException e) {
            log.error("[消费服务] 执行消费请求系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            ConsumeTransactionDetailVO detail = new ConsumeTransactionDetailVO();
            detail.setTransactionStatus(3); // 失败
            return detail;
        } catch (Exception e) {
            log.error("[消费服务] 执行消费请求未知异常", e);
            ConsumeTransactionDetailVO detail = new ConsumeTransactionDetailVO();
            detail.setTransactionStatus(3); // 失败
            return detail;
        }
    }

    /**
     * 执行消费请求降级方法
     * <p>
     * 当服务熔断或失败时，返回降级结果
     * </p>
     *
     * @param request 消费请求
     * @param ex 异常信息
     * @return 降级交易详情
     */
    public ConsumeTransactionDetailVO executeConsumeFallback(ConsumeRequest request, Exception ex) {
        log.warn("[消费服务] 执行消费请求降级，userId={}, deviceId={}, error={}",
                request.getUserId(), request.getDeviceId(), ex.getMessage());

        ConsumeTransactionDetailVO detail = new ConsumeTransactionDetailVO();
        detail.setTransactionStatus(3); // 失败
        return detail;
    }

    /**
     * 获取设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    @Override
    @Observed(name = "consume.getDeviceDetail", contextualName = "consume-get-device-detail")
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

        } catch (BusinessException e) {
            log.warn("[消费服务] 获取设备详情业务异常，deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return new ConsumeDeviceVO();
        } catch (SystemException e) {
            log.error("[消费服务] 获取设备详情系统异常，deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return new ConsumeDeviceVO();
        } catch (Exception e) {
            log.error("[消费服务] 获取设备详情未知异常，deviceId={}", deviceId, e);
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
    @Observed(name = "consume.getDeviceStatistics", contextualName = "consume-get-device-statistics")
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

        } catch (BusinessException e) {
            log.warn("[消费服务] 获取设备状态统计业务异常，areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage());
            return new ConsumeDeviceStatisticsVO();
        } catch (SystemException e) {
            log.error("[消费服务] 获取设备状态统计系统异常，areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage(), e);
            return new ConsumeDeviceStatisticsVO();
        } catch (Exception e) {
            log.error("[消费服务] 获取设备状态统计未知异常，areaId={}", areaId, e);
            return new ConsumeDeviceStatisticsVO();
        }
    }

    /**
     * 获取实时统计（使用Spring Cache）
     * <p>
     * 缓存策略：
     * - L1本地缓存：Caffeine，5分钟过期
     * - L2分布式缓存：Redis，30分钟过期
     * - 缓存键：consume:realtime:statistics:{areaId}
     * </p>
     *
     * @param areaId 区域ID
     * @return 实时统计数据
     */
    @Override
    @Observed(name = "consume.getRealtimeStatistics", contextualName = "consume-get-realtime-statistics")
    @Transactional(readOnly = true)
    @Cacheable(value = "consume:realtime:statistics", key = "#areaId", unless = "#result == null")
    @Timed(value = "consume.statistics.realtime", description = "实时统计查询耗时")
    @Counted(value = "consume.statistics.realtime.count", description = "实时统计查询次数")
    public ConsumeRealtimeStatisticsVO getRealtimeStatistics(String areaId) {
        log.info("[消费服务] 获取实时统计（缓存未命中），areaId={}", areaId);

        try {
            // 查询数据库并构建统计数据
            ConsumeRealtimeStatisticsVO result = new ConsumeRealtimeStatisticsVO();

            // 实现实时统计查询逻辑
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            LocalDateTime lastHourStart = now.minusHours(1);

            // 1. 查询今日交易记录
            List<ConsumeTransactionEntity> todayTransactions = consumeTransactionDao.selectByTimeRange(todayStart, now);
            if (areaId != null && !areaId.trim().isEmpty()) {
                // 将String类型的areaId转换为Long进行比较（实体类字段为Long类型）
                Long areaIdLong = null;
                try {
                    areaIdLong = Long.parseLong(areaId.trim());
                } catch (NumberFormatException e) {
                    log.warn("[消费服务] 无法将areaId转换为Long: {}", areaId);
                }
                final Long finalAreaId = areaIdLong;
                todayTransactions = todayTransactions.stream()
                        .filter(t -> finalAreaId == null || finalAreaId.equals(t.getAreaId()))
                        .collect(Collectors.toList());
            }

            // 2. 计算今日统计
            BigDecimal todayAmount = todayTransactions.stream()
                    .filter(t -> t.getFinalMoney() != null)
                    .map(t -> t.getFinalMoney().divide(BigDecimal.valueOf(100))) // 转换为元
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
                    .map(t -> t.getFinalMoney().divide(BigDecimal.valueOf(100))) // 转换为元
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

            // Spring Cache会自动缓存结果（通过@Cacheable注解）

            return result;

        } catch (BusinessException e) {
            log.warn("[消费服务] 获取实时统计业务异常，areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage());
            ConsumeRealtimeStatisticsVO result = new ConsumeRealtimeStatisticsVO();
            result.setTodayAmount(java.math.BigDecimal.ZERO);
            result.setTodayCount(0);
            return result;
        } catch (SystemException e) {
            log.error("[消费服务] 获取实时统计系统异常，areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage(), e);
            ConsumeRealtimeStatisticsVO result = new ConsumeRealtimeStatisticsVO();
            result.setTodayAmount(java.math.BigDecimal.ZERO);
            result.setTodayCount(0);
            return result;
        } catch (Exception e) {
            log.error("[消费服务] 获取实时统计未知异常，areaId={}", areaId, e);
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
    @Observed(name = "consume.getTransactionDetail", contextualName = "consume-get-transaction-detail")
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
            detail.setUserId(String.valueOf(transaction.getUserId()));
            detail.setUserName(transaction.getUserName());
            detail.setAccountId(String.valueOf(transaction.getAccountId()));
            detail.setAreaId(String.valueOf(transaction.getAreaId()));
            detail.setAreaName(transaction.getAreaName());
            detail.setDeviceId(String.valueOf(transaction.getDeviceId()));
            detail.setDeviceName(transaction.getDeviceName());
            detail.setAmount(transaction.getAmount());
            detail.setConsumeTime(transaction.getConsumeTime());
            detail.setConsumeMode(transaction.getConsumeMode());
            detail.setConsumeType(transaction.getConsumeType());
            detail.setTransactionStatus(transaction.getTransactionStatus() != null ? transaction.getTransactionStatus() : 2); // 默认成功

            log.info("[消费服务] 获取交易详情成功，transactionNo={}", transactionNo);
            return detail;

        } catch (BusinessException e) {
            log.warn("[消费服务] 获取交易详情业务异常，transactionNo={}, code={}, message={}", transactionNo, e.getCode(), e.getMessage());
            ConsumeTransactionDetailVO detail = new ConsumeTransactionDetailVO();
            detail.setTransactionStatus(3); // 失败
            return detail;
        } catch (SystemException e) {
            log.error("[消费服务] 获取交易详情系统异常，transactionNo={}, code={}, message={}", transactionNo, e.getCode(), e.getMessage(), e);
            ConsumeTransactionDetailVO detail = new ConsumeTransactionDetailVO();
            detail.setTransactionStatus(3); // 失败
            return detail;
        } catch (Exception e) {
            log.error("[消费服务] 获取交易详情未知异常，transactionNo={}", transactionNo, e);
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
    @Observed(name = "consume.queryTransactions", contextualName = "consume-query-transactions")
    @Transactional(readOnly = true)
    public PageResult<ConsumeTransactionDetailVO> queryTransactions(ConsumeTransactionQueryForm queryForm) {
        log.info("[消费服务] 分页查询消费交易记录，pageNum={}, pageSize={}, userId={}, transactionNo={}, deviceId={}, areaId={}, startDate={}, endDate={}, consumeMode={}, status={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getUserId(), queryForm.getTransactionNo(), queryForm.getDeviceId(),
                queryForm.getAreaId(), queryForm.getStartDate(), queryForm.getEndDate(), queryForm.getConsumeMode(), queryForm.getStatus());

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
                    page, queryForm.getUserId(), queryForm.getTransactionNo(), queryForm.getDeviceId(),
                    queryForm.getAreaId(), startTime, endTime, queryForm.getConsumeMode(), queryForm.getStatus());

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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费服务] 分页查询消费交易记录参数错误: {}", e.getMessage());
            return PageResult.of(new ArrayList<>(), 0L, queryForm.getPageNum(), queryForm.getPageSize());
        } catch (BusinessException e) {
            log.warn("[消费服务] 分页查询消费交易记录业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return PageResult.of(new ArrayList<>(), 0L, queryForm.getPageNum(), queryForm.getPageSize());
        } catch (SystemException e) {
            log.error("[消费服务] 分页查询消费交易记录系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return PageResult.of(new ArrayList<>(), 0L, queryForm.getPageNum(), queryForm.getPageSize());
        } catch (Exception e) {
            log.error("[消费服务] 分页查询消费交易记录未知异常", e);
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
        vo.setUserId(String.valueOf(entity.getUserId()));
        vo.setUserName(entity.getUserName());
        vo.setAccountId(String.valueOf(entity.getAccountId()));
        vo.setAreaId(String.valueOf(entity.getAreaId()));
        vo.setAreaName(entity.getAreaName());
        vo.setDeviceId(String.valueOf(entity.getDeviceId()));
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

    /**
     * 账户充值
     *
     * @param request 充值请求
     * @return 充值结果
     */
    @Override
    @Observed(name = "consume.recharge", contextualName = "consume-recharge")
    @Transactional(rollbackFor = Exception.class)
    public net.lab1024.sa.common.dto.ResponseDTO<Void> recharge(net.lab1024.sa.consume.domain.dto.RechargeRequestDTO request) {
        log.info("[消费服务] 账户充值，userId={}, amount={}", request.getUserId(), request.getRechargeAmount());

        try {
            // 参数验证
            if (request.getUserId() == null) {
                return net.lab1024.sa.common.dto.ResponseDTO.error("USER_ID_NULL", "用户ID不能为空");
            }
            if (request.getRechargeAmount() == null || request.getRechargeAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return net.lab1024.sa.common.dto.ResponseDTO.error("AMOUNT_INVALID", "充值金额必须大于0");
            }

            // 1. 验证账户存在
            net.lab1024.sa.consume.entity.AccountEntity account;
            try {
                account = accountService.getByUserId(request.getUserId());
            } catch (BusinessException e) {
                log.warn("[消费服务] 账户查询业务异常，userId={}, code={}, message={}", request.getUserId(), e.getCode(), e.getMessage());
                throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在：" + e.getMessage());
            } catch (SystemException e) {
                log.error("[消费服务] 账户查询系统异常，userId={}, code={}, message={}", request.getUserId(), e.getCode(), e.getMessage(), e);
                throw new SystemException("ACCOUNT_QUERY_FAILED", "账户查询失败：" + e.getMessage(), e);
            } catch (Exception e) {
                log.warn("[消费服务] 账户查询未知异常，userId={}, error={}", request.getUserId(), e.getMessage());
                throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在：" + e.getMessage());
            }

            // 2. 更新账户余额（使用AccountService的addBalance方法）
            String rechargeReason = "充值：" + (request.getRemark() != null ? request.getRemark() : "账户充值");
            boolean success = accountService.addBalance(account.getAccountId(), request.getRechargeAmount(), rechargeReason);
            if (!success) {
                log.error("[消费服务] 账户余额更新失败，accountId={}, amount={}", account.getAccountId(), request.getRechargeAmount());
                throw new BusinessException("BALANCE_UPDATE_FAILED", "账户余额更新失败");
            }

            // 3. 记录充值流水（通过ConsumeTransactionDao记录充值交易）
            // 注意：这里可以创建充值交易记录，但根据业务需求，充值可能不需要创建消费交易记录
            // 如果需要记录充值流水，可以创建专门的充值记录表或使用交易记录表

            log.info("[消费服务] 账户充值成功，userId={}, accountId={}, amount={}",
                    request.getUserId(), account.getAccountId(), request.getRechargeAmount());
            return net.lab1024.sa.common.dto.ResponseDTO.ok();

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费服务] 账户充值参数错误: {}", e.getMessage());
            throw new ParamException("RECHARGE_PARAM_ERROR", "充值参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费服务] 账户充值业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费服务] 账户充值系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费服务] 账户充值未知异常", e);
            throw new SystemException("RECHARGE_SYSTEM_ERROR", "充值失败：" + e.getMessage(), e);
        }
    }

    /**
     * 分页查询消费记录
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public net.lab1024.sa.common.dto.ResponseDTO<com.baomidou.mybatisplus.core.metadata.IPage<net.lab1024.sa.consume.domain.vo.ConsumeRecordVO>> queryConsumeRecordPage(net.lab1024.sa.consume.domain.dto.ConsumeQueryDTO queryDTO) {
        log.info("[消费服务] 分页查询消费记录，userId={}, pageNum={}, pageSize={}",
                queryDTO.getUserId(), queryDTO.getPageNum(), queryDTO.getPageSize());

        try {
            // 构建分页对象
            Page<net.lab1024.sa.consume.entity.ConsumeTransactionEntity> page =
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

            // 调用DAO进行分页查询
            IPage<net.lab1024.sa.consume.entity.ConsumeTransactionEntity> transactionPage =
                consumeTransactionDao.queryTransactions(
                    page,
                    queryDTO.getUserId(),
                    null,
                    null,
                    queryDTO.getAreaId() != null ? String.valueOf(queryDTO.getAreaId()) : null,
                    queryDTO.getStartTime(),
                    queryDTO.getEndTime(),
                    queryDTO.getConsumeType(),
                    queryDTO.getStatus() != null ? String.valueOf(queryDTO.getStatus()) : null
                );

            // 转换为VO分页结果
            Page<net.lab1024.sa.consume.domain.vo.ConsumeRecordVO> voPage =
                new Page<>(transactionPage.getCurrent(), transactionPage.getSize(), transactionPage.getTotal());

            // 转换Entity列表为VO列表
            List<net.lab1024.sa.consume.domain.vo.ConsumeRecordVO> voList = transactionPage.getRecords().stream()
                .map(this::convertTransactionToRecordVO)
                .collect(Collectors.toList());

            voPage.setRecords(voList);

            log.info("[消费服务] 分页查询消费记录成功，total={}, size={}", voPage.getTotal(), voList.size());
            return net.lab1024.sa.common.dto.ResponseDTO.ok(voPage);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费服务] 分页查询消费记录参数错误: {}", e.getMessage());
            throw new ParamException("QUERY_PARAM_ERROR", "查询参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费服务] 分页查询消费记录业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费服务] 分页查询消费记录系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费服务] 分页查询消费记录未知异常", e);
            throw new SystemException("QUERY_SYSTEM_ERROR", "查询失败：" + e.getMessage(), e);
        }
    }

    /**
     * 游标分页查询消费记录（推荐用于深度分页）
     *
     * @param pageSize 每页大小
     * @param lastTime 上一页最后一条记录的创建时间
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param consumeType 消费类型
     * @param status 状态
     * @return 游标分页结果
     */
    @Override
    @Observed(name = "consume.cursorPageConsumeRecords", contextualName = "consume-cursor-page-records")
    @Transactional(readOnly = true)
    public ResponseDTO<CursorPagination.CursorPageResult<ConsumeRecordVO>> cursorPageConsumeRecords(
            Integer pageSize, LocalDateTime lastTime,
            Long userId, Long areaId, LocalDateTime startTime, LocalDateTime endTime,
            String consumeType, Integer status) {
        log.info("[消费服务] 游标分页查询消费记录，pageSize={}, lastTime={}, userId={}",
                pageSize, lastTime, userId);

        try {
            // 1. 构建查询条件
            LambdaQueryWrapper<ConsumeTransactionEntity> queryWrapper = new LambdaQueryWrapper<>();

            if (userId != null) {
                queryWrapper.eq(ConsumeTransactionEntity::getUserId, userId);
            }

            if (areaId != null) {
                queryWrapper.eq(ConsumeTransactionEntity::getAreaId, areaId);
            }

            if (startTime != null) {
                queryWrapper.ge(ConsumeTransactionEntity::getCreateTime, startTime);
            }

            if (endTime != null) {
                queryWrapper.le(ConsumeTransactionEntity::getCreateTime, endTime);
            }

            if (consumeType != null) {
                queryWrapper.eq(ConsumeTransactionEntity::getConsumeType, consumeType);
            }

            if (status != null) {
                queryWrapper.eq(ConsumeTransactionEntity::getStatus, status);
            }

            // 2. 使用游标分页（基于时间）
            // 注意：ConsumeTransactionEntity的id是String类型，使用PageHelper的cursorPageByTime方法
            // PageHelper已经提供了更好的实现，支持SFunction参数
            CursorPagination.CursorPageResult<ConsumeTransactionEntity> entityResult =
                PageHelper.cursorPageByTime(
                    consumeTransactionDao,
                    queryWrapper,
                    pageSize,
                    lastTime,
                    ConsumeTransactionEntity::getCreateTime,  // 获取创建时间的Lambda表达式
                    entity -> {  // 获取ID的Lambda表达式（id是String类型，转换为Long）
                        try {
                            String idStr = entity.getId();
                            return idStr != null ? Long.parseLong(idStr) : null;
                        } catch (NumberFormatException e) {
                            log.debug("[消费交易查询] 交易ID不是数字格式，返回null: id={}", entity.getId());
                            return null;
                        }
                    }
                );

            // 3. 转换为VO列表
            List<ConsumeRecordVO> voList = entityResult.getList().stream()
                .map(this::convertTransactionToRecordVO)
                .collect(Collectors.toList());

            // 4. 构建VO游标分页结果
            CursorPagination.CursorPageResult<ConsumeRecordVO> voResult =
                CursorPagination.CursorPageResult.<ConsumeRecordVO>builder()
                    .list(voList)
                    .hasNext(entityResult.getHasNext())
                    .lastId(entityResult.getLastId())
                    .lastTime(entityResult.getLastTime())
                    .size(voList.size())
                    .build();

            log.info("[消费服务] 游标分页查询消费记录成功，size={}, hasNext={}",
                    voList.size(), voResult.getHasNext());

            return ResponseDTO.ok(voResult);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费服务] 游标分页查询消费记录参数错误: {}", e.getMessage());
            throw new ParamException("QUERY_PARAM_ERROR", "查询参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费服务] 游标分页查询消费记录业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费服务] 游标分页查询消费记录系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费服务] 游标分页查询消费记录未知异常", e);
            throw new SystemException("QUERY_SYSTEM_ERROR", "查询失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将消费交易Entity转换为消费记录VO
     *
     * @param transaction 消费交易实体
     * @return 消费记录VO
     */
    private net.lab1024.sa.consume.domain.vo.ConsumeRecordVO convertTransactionToRecordVO(
            net.lab1024.sa.consume.entity.ConsumeTransactionEntity transaction) {
        if (transaction == null) {
            return null;
        }

        net.lab1024.sa.consume.domain.vo.ConsumeRecordVO vo = new net.lab1024.sa.consume.domain.vo.ConsumeRecordVO();

        // 基本信息
        vo.setId(transaction.getId() != null && !transaction.getId().isEmpty()
                ? Long.parseLong(transaction.getId()) : null);
        vo.setTransactionNo(transaction.getTransactionNo());
        vo.setAccountId(transaction.getAccountId());
        vo.setUserId(transaction.getUserId());
        vo.setUserName(transaction.getUserName());

        // 金额信息
        vo.setAmount(transaction.getFinalMoney());
        vo.setOriginalAmount(transaction.getConsumeMoney());
        vo.setDiscountAmount(transaction.getDiscountMoney());
        vo.setSubsidyAmount(transaction.getAllowanceUsed());
        vo.setBalanceBefore(transaction.getBalanceBefore());
        vo.setBalanceAfter(transaction.getBalanceAfter());

        // 状态信息（transactionStatus是Integer类型）
        vo.setStatus(transaction.getTransactionStatus());
        if (vo.getStatus() != null) {
            switch (vo.getStatus()) {
                case 1: vo.setStatusDesc("待处理"); break;
                case 2: vo.setStatusDesc("成功"); break;
                case 3: vo.setStatusDesc("失败"); break;
                case 4: vo.setStatusDesc("已退款"); break;
                default: vo.setStatusDesc("未知"); break;
            }
        }

        // 消费方式信息
        vo.setConsumeMode(transaction.getConsumeMode());
        if (transaction.getConsumeMode() != null) {
            switch (transaction.getConsumeMode()) {
                case "FIXED": vo.setConsumeModeDesc("定值"); break;
                case "AMOUNT": vo.setConsumeModeDesc("金额"); break;
                case "PRODUCT": vo.setConsumeModeDesc("商品"); break;
                case "COUNT": vo.setConsumeModeDesc("计次"); break;
                default: vo.setConsumeModeDesc("未知"); break;
            }
        }

        // 消费类型信息
        vo.setConsumeType(transaction.getConsumeType());
        if (transaction.getConsumeType() != null) {
            switch (transaction.getConsumeType()) {
                case "CONSUME": vo.setConsumeTypeDesc("正常消费"); break;
                case "MAKEUP": vo.setConsumeTypeDesc("补单"); break;
                case "CORRECT": vo.setConsumeTypeDesc("纠错"); break;
                default: vo.setConsumeTypeDesc("未知"); break;
            }
        }

        // 设备信息
        vo.setDeviceNo(transaction.getDeviceName()); // 使用设备名称作为设备编号
        vo.setDeviceName(transaction.getDeviceName());

        // 区域信息
        vo.setAreaId(transaction.getAreaId());
        vo.setAreaName(transaction.getAreaName());

        // 时间信息
        vo.setConsumeTime(transaction.getTransactionTime());
        vo.setCreateTime(transaction.getCreateTime());
        vo.setUpdateTime(transaction.getUpdateTime());

        // 商品信息
        vo.setProductId(transaction.getProductId());
        vo.setProductName(transaction.getProductName());

        // 其他信息
        vo.setCurrency("CNY"); // 默认币种
        // 注意：ConsumeTransactionEntity没有isOffline、remark、extendAttrs字段，暂不设置

        return vo;
    }

    /**
     * 执行消费（兼容方法）
     *
     * @param request 消费请求DTO
     * @return 消费结果
     */
    @Override
    @Observed(name = "consume.consume", contextualName = "consume-consume")
    @Transactional(rollbackFor = Exception.class)
    public net.lab1024.sa.common.dto.ResponseDTO<net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO> consume(net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO request) {
        log.info("[消费服务] 执行消费（兼容方法），userId={}, amount={}", request.getUserId(), request.getAmount());

        try {
            // 参数验证
            if (request.getUserId() == null) {
                return net.lab1024.sa.common.dto.ResponseDTO.error("USER_ID_NULL", "用户ID不能为空");
            }
            if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return net.lab1024.sa.common.dto.ResponseDTO.error("AMOUNT_INVALID", "消费金额必须大于0");
            }

            // 转换为表单对象
            ConsumeTransactionForm form = new ConsumeTransactionForm();
            form.setUserId(request.getUserId());
            form.setAccountId(request.getAccountId());
            form.setDeviceId(request.getDeviceIdAsLong()); // ConsumeRequestDTO.deviceId是String类型，需要转换为Long
            form.setAreaId(request.getAreaId() != null ? String.valueOf(request.getAreaId()) : null);
            form.setAmount(request.getAmount());
            form.setConsumeMode(request.getConsumeMode());

            // 调用现有的executeTransaction方法
            ConsumeTransactionResultVO result = executeTransaction(form);

            // 兼容方法返回统一响应：当交易失败时返回 error，避免前端/调用方误判为成功
            if (result == null) {
                return net.lab1024.sa.common.dto.ResponseDTO.error("CONSUME_FAILED", "消费失败");
            }
            if (result.getTransactionStatus() != null && result.getTransactionStatus() != 2) {
                String errorMessage = result.getErrorMessage() != null ? result.getErrorMessage() : "消费失败";
                return net.lab1024.sa.common.dto.ResponseDTO.error("CONSUME_FAILED", errorMessage);
            }

            log.info("[消费服务] 执行消费成功，transactionNo={}", result.getTransactionNo());
            return net.lab1024.sa.common.dto.ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费服务] 执行消费参数错误: {}", e.getMessage());
            throw new ParamException("CONSUME_PARAM_ERROR", "消费参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费服务] 执行消费业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费服务] 执行消费系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费服务] 执行消费未知异常", e);
            throw new SystemException("CONSUME_SYSTEM_ERROR", "消费失败：" + e.getMessage(), e);
        }
    }
}



