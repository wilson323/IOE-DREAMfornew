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
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.vo.ConsumeRecordVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionDetailVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO;
import net.lab1024.sa.consume.manager.ConsumeDeviceManager;
import net.lab1024.sa.consume.manager.ConsumeExecutionManager;
import net.lab1024.sa.consume.service.ConsumeCacheService;
import net.lab1024.sa.consume.service.ConsumeService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.util.CursorPagination;
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
    private ConsumeCacheService consumeCacheService;

    @Resource
    private net.lab1024.sa.consume.service.AccountService accountService;

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
                        transaction.getBalanceAfter().divide(
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
            net.lab1024.sa.consume.domain.entity.AccountEntity account;
            try {
                account = accountService.getByUserId(request.getUserId());
            } catch (Exception e) {
                // AccountService.getByUserId可能抛出BusinessException，统一捕获处理
                log.warn("[消费服务] 账户查询失败，userId={}, error={}", request.getUserId(), e.getMessage());
                return net.lab1024.sa.common.dto.ResponseDTO.error("ACCOUNT_NOT_FOUND", "账户不存在：" + e.getMessage());
            }

            // 2. 更新账户余额（使用AccountService的addBalance方法）
            String rechargeReason = "充值：" + (request.getRemark() != null ? request.getRemark() : "账户充值");
            boolean success = accountService.addBalance(account.getAccountId(), request.getRechargeAmount(), rechargeReason);
            if (!success) {
                log.error("[消费服务] 账户余额更新失败，accountId={}, amount={}", account.getAccountId(), request.getRechargeAmount());
                return net.lab1024.sa.common.dto.ResponseDTO.error("BALANCE_UPDATE_FAILED", "账户余额更新失败");
            }

            // 3. 记录充值流水（通过ConsumeTransactionDao记录充值交易）
            // 注意：这里可以创建充值交易记录，但根据业务需求，充值可能不需要创建消费交易记录
            // 如果需要记录充值流水，可以创建专门的充值记录表或使用交易记录表

            log.info("[消费服务] 账户充值成功，userId={}, accountId={}, amount={}",
                    request.getUserId(), account.getAccountId(), request.getRechargeAmount());
            return net.lab1024.sa.common.dto.ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[消费服务] 账户充值失败，userId={}, amount={}", request.getUserId(), request.getRechargeAmount(), e);
            return net.lab1024.sa.common.dto.ResponseDTO.error("RECHARGE_FAILED", "充值失败：" + e.getMessage());
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
            Page<net.lab1024.sa.consume.domain.entity.ConsumeTransactionEntity> page =
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

            // 调用DAO进行分页查询
            IPage<net.lab1024.sa.consume.domain.entity.ConsumeTransactionEntity> transactionPage =
                consumeTransactionDao.queryTransactions(
                    page,
                    queryDTO.getUserId(),
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

        } catch (Exception e) {
            log.error("[消费服务] 分页查询消费记录失败", e);
            return net.lab1024.sa.common.dto.ResponseDTO.error("QUERY_FAILED", "查询失败：" + e.getMessage());
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
            // 注意：ConsumeTransactionEntity的id是String类型，但CursorPagination内部使用反射处理
            CursorPagination.CursorPageResult<ConsumeTransactionEntity> entityResult =
                CursorPagination.queryByTimeCursor(
                    consumeTransactionDao,
                    queryWrapper,
                    CursorPagination.CursorPageRequest.<ConsumeTransactionEntity>builder()
                        .pageSize(pageSize)
                        .lastTime(lastTime)
                        .desc(true)
                        .build()
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

        } catch (Exception e) {
            log.error("[消费服务] 游标分页查询消费记录失败", e);
            return ResponseDTO.error("QUERY_FAILED", "查询失败：" + e.getMessage());
        }
    }

    /**
     * 将消费交易Entity转换为消费记录VO
     *
     * @param transaction 消费交易实体
     * @return 消费记录VO
     */
    private net.lab1024.sa.consume.domain.vo.ConsumeRecordVO convertTransactionToRecordVO(
            net.lab1024.sa.consume.domain.entity.ConsumeTransactionEntity transaction) {
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

            log.info("[消费服务] 执行消费成功，transactionNo={}", result.getTransactionNo());
            return net.lab1024.sa.common.dto.ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[消费服务] 执行消费失败，userId={}, amount={}", request.getUserId(), request.getAmount(), e);
            return net.lab1024.sa.common.dto.ResponseDTO.error("CONSUME_FAILED", "消费失败：" + e.getMessage());
        }
    }
}
