package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.consume.domain.form.ConsumeRecordQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeRecordAddForm;
import net.lab1024.sa.consume.domain.vo.ConsumeRecordVO;
import net.lab1024.sa.consume.domain.vo.ConsumeStatisticsVO;
import net.lab1024.sa.consume.exception.ConsumeAccountException;
import net.lab1024.sa.consume.manager.ConsumeCacheManager;
import net.lab1024.sa.consume.manager.ConsumeRecordManager;
import net.lab1024.sa.consume.service.ConsumeAccountService;
import net.lab1024.sa.consume.service.ConsumeRecordService;
import net.lab1024.sa.consume.service.ConsumeWebSocketService;

/**
 * 消费记录服务实现
 * <p>
 * 提供消费记录的完整业务功能实现，包括：
 * - 在线消费：实时扣减账户余额并创建消费记录
 * - 离线消费：创建离线记录，由定时任务异步同步
 * - 退款处理：增加账户余额并更新消费记录
 * - 消费统计：提供多维度消费数据统计
 * </p>
 *
 * <p>技术特性：</p>
 * <ul>
 *   <li>集成Seata分布式事务保证数据一致性</li>
 *   <li>调用ConsumeAccountService进行账户余额操作</li>
 *   <li>调用ConsumeRecordManager进行业务编排</li>
 *   <li>支持在线/离线双模式</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
public class ConsumeRecordServiceImpl implements ConsumeRecordService {

    private final ConsumeRecordManager recordManager;
    private final ConsumeRecordDao recordDao;
    private final ConsumeAccountService accountService;
    private final ConsumeWebSocketService webSocketService;
    private final ConsumeCacheManager cacheManager;

    /**
     * 构造函数注入依赖
     */
    public ConsumeRecordServiceImpl(ConsumeRecordManager recordManager,
                                    ConsumeRecordDao recordDao,
                                    ConsumeAccountService accountService,
                                    ConsumeWebSocketService webSocketService,
                                    ConsumeCacheManager cacheManager) {
        this.recordManager = recordManager;
        this.recordDao = recordDao;
        this.accountService = accountService;
        this.webSocketService = webSocketService;
        this.cacheManager = cacheManager;
    }

    @Override
    public PageResult<ConsumeRecordVO> queryRecords(ConsumeRecordQueryForm queryForm) {
        log.info("[消费记录服务] 分页查询消费记录: queryForm={}", queryForm);

        try {
            // 使用MyBatis-Plus分页查询
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<ConsumeRecordVO> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                    queryForm.getPageNum() != null ? queryForm.getPageNum() : 1,
                    queryForm.getPageSize() != null ? queryForm.getPageSize() : 20
                );

            com.baomidou.mybatisplus.core.metadata.IPage<ConsumeRecordVO> pageResult =
                recordDao.selectPage(page, queryForm);

            // 转换为PageResult
            PageResult<ConsumeRecordVO> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum(queryForm.getPageNum() != null ? queryForm.getPageNum() : 1);
            result.setPageSize(queryForm.getPageSize() != null ? queryForm.getPageSize() : 20);
            result.setPages((int) pageResult.getPages());

            return result;
        } catch (Exception e) {
            log.error("[消费记录服务] 分页查询失败", e);
            throw ConsumeAccountException.queryFailed("查询消费记录失败: " + e.getMessage());
        }
    }

    @Override
    public ConsumeRecordVO getRecordDetail(Long recordId) {
        log.info("[消费记录服务] 获取消费记录详情: recordId={}", recordId);

        try {
            ConsumeRecordVO record = recordDao.selectRecordById(recordId);
            if (record == null) {
                log.warn("[消费记录服务] 消费记录不存在: recordId={}", recordId);
                return null;
            }

            log.info("[消费记录服务] 消费记录详情获取成功: recordId={}", recordId);
            return record;

        } catch (Exception e) {
            log.error("[消费记录服务] 获取消费记录详情失败: recordId={}", recordId, e);
            throw ConsumeAccountException.queryFailed("获取消费记录详情失败: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional(name = "create-online-consume-record", rollbackFor = Exception.class)
    public Long addRecord(ConsumeRecordAddForm addForm) {
        log.info("[消费记录服务] 创建消费记录: accountId={}, amount={}",
                addForm.getAccountId(), addForm.getAmount());

        try {
            // 判断消费类型（在线/离线）
            // 注意：当前Form没有offlineFlag字段，默认为在线消费（需要实时扣款）
            // 离线消费应通过专门的离线消费API处理
            boolean isOffline = false; // 默认在线消费

            if (!isOffline) {
                // ========== 在线消费：需要扣减账户余额 ==========
                log.info("[消费记录服务] 在线消费：扣减账户余额");

                // 扣减账户余额（调用AccountService，内部会调用远程AccountServiceClient）
                boolean deductSuccess = accountService.deductAmount(
                        addForm.getAccountId(),
                        addForm.getAmount(),
                        addForm.getConsumeTypeName()
                );

                if (!deductSuccess) {
                    log.error("[消费记录服务] 扣减账户余额失败: accountId={}", addForm.getAccountId());
                    throw ConsumeAccountException.deductFailed("扣减账户余额失败");
                }

                // 创建在线消费记录
                Long recordId = recordManager.createOnlineRecord(
                        addForm.getAccountId(),
                        addForm.getUserId(),
                        addForm.getUserName(),
                        addForm.getDeviceId(),
                        addForm.getDeviceName(),
                        addForm.getMerchantId(),
                        addForm.getMerchantName(),
                        addForm.getAmount(),
                        addForm.getOriginalAmount(),
                        addForm.getDiscountAmount(),
                        addForm.getConsumeType(),
                        addForm.getConsumeTypeName(),
                        addForm.getPaymentMethod(),
                        addForm.getOrderNo(),
                        addForm.getTransactionNo(),
                        addForm.getConsumeLocation()
                );

                log.info("[消费记录服务] 在线消费记录创建成功: recordId={}, orderNo={}",
                        recordId, addForm.getOrderNo());

                // 清除用户分析缓存
                cacheManager.evictUserAnalysisCache(addForm.getUserId());

                // 推送WebSocket实时通知
                try {
                    // 获取消费后的账户余额
                    BigDecimal balance = accountService.getAccountBalance(addForm.getAccountId());

                    // 如果余额低于预警阈值，发送余额预警
                    BigDecimal warningThreshold = BigDecimal.valueOf(50);
                    if (balance.compareTo(warningThreshold) < 0) {
                        webSocketService.pushBalanceWarning(
                                addForm.getUserId(),
                                balance,
                                warningThreshold
                        );
                    }

                    // 发送消费成功通知
                    webSocketService.pushConsumeSuccess(
                            addForm.getUserId(),
                            addForm.getAmount(),
                            addForm.getMerchantName(),
                            balance
                    );

                    log.info("[消费记录服务] WebSocket通知推送成功: userId={}", addForm.getUserId());
                } catch (Exception e) {
                    log.error("[消费记录服务] WebSocket通知推送失败: userId={}", addForm.getUserId(), e);
                    // 不影响主流程
                }

                return recordId;

            } else {
                // ========== 离线消费：只创建记录，不扣减余额 ==========
                log.info("[消费记录服务] 离线消费：创建离线记录，等待同步");

                // 创建离线消费记录（syncStatus=0，待同步）
                Long recordId = recordManager.createOfflineRecord(
                        addForm.getAccountId(),
                        addForm.getUserId(),
                        addForm.getUserName(),
                        addForm.getDeviceId(),
                        addForm.getDeviceName(),
                        addForm.getMerchantId(),
                        addForm.getMerchantName(),
                        addForm.getAmount(),
                        addForm.getConsumeType(),
                        addForm.getConsumeTypeName(),
                        addForm.getPaymentMethod(),
                        addForm.getOrderNo(),
                        addForm.getConsumeTime() != null ? addForm.getConsumeTime() : LocalDateTime.now(),
                        addForm.getConsumeLocation()
                );

                log.info("[消费记录服务] 离线消费记录创建成功: recordId={}, orderNo={}, 等待同步",
                        recordId, addForm.getOrderNo());

                // 清除用户分析缓存
                cacheManager.evictUserAnalysisCache(addForm.getUserId());

                return recordId;
            }

        } catch (ConsumeAccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("[消费记录服务] 创建消费记录异常", e);
            throw ConsumeAccountException.createFailed("创建消费记录失败: " + e.getMessage());
        }
    }

    @Override
    public List<ConsumeRecordVO> getTodayRecords(Long userId) {
        log.info("[消费记录服务] 获取今日消费记录: userId={}", userId);

        try {
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

            List<ConsumeRecordEntity> entities = recordDao.selectByTimeRange(todayStart, todayEnd);

            if (userId != null) {
                // 过滤指定用户的记录
                entities = entities.stream()
                        .filter(record -> userId.equals(record.getUserId()))
                        .toList();
            }

            // 转换Entity到VO
            List<ConsumeRecordVO> records = entities.stream()
                    .map(this::convertEntityToVO)
                    .toList();

            log.info("[消费记录服务] 今日消费记录查询成功: userId={}, count={}", userId, records.size());
            return records;

        } catch (Exception e) {
            log.error("[消费记录服务] 获取今日消费记录失败: userId={}", userId, e);
            throw ConsumeAccountException.queryFailed("获取今日消费记录失败: " + e.getMessage());
        }
    }

    /**
     * 将Entity转换为VO
     * 注意：Entity使用Integer状态字段，VO使用String描述字段，需要转换
     */
    private ConsumeRecordVO convertEntityToVO(ConsumeRecordEntity entity) {
        ConsumeRecordVO vo = new ConsumeRecordVO();
        vo.setRecordId(entity.getRecordId());
        vo.setAccountId(entity.getAccountId());
        vo.setUserId(entity.getUserId());
        vo.setUserName(entity.getUserName());
        vo.setAmount(entity.getAmount());
        vo.setConsumeType(entity.getConsumeType());
        vo.setConsumeTypeName(entity.getConsumeTypeName());
        vo.setPaymentMethod(entity.getPaymentMethod());
        vo.setOrderNo(entity.getOrderNo());
        vo.setTransactionNo(entity.getTransactionNo());
        vo.setConsumeTime(entity.getConsumeTime());
        vo.setConsumeLocation(entity.getConsumeLocation());

        // 转换状态：Integer -> String描述
        vo.setStatus(convertTransactionStatus(entity.getTransactionStatus()));
        vo.setRefundStatus(convertRefundStatus(entity.getRefundStatus()));

        return vo;
    }

    /**
     * 转换交易状态：Integer -> String
     */
    private String convertTransactionStatus(Integer status) {
        if (status == null) return "UNKNOWN";
        return switch (status) {
            case 1 -> "SUCCESS";
            case 2 -> "FAILED";
            case 3 -> "PENDING";
            default -> "UNKNOWN";
        };
    }

    /**
     * 转换退款状态：Integer -> String
     */
    private String convertRefundStatus(Integer refundStatus) {
        if (refundStatus == null) return "NO_REFUND";
        return switch (refundStatus) {
            case 0 -> "NO_REFUND";
            case 1 -> "PARTIAL_REFUND";
            case 2 -> "FULL_REFUND";
            default -> "UNKNOWN";
        };
    }

    @Override
    public ConsumeStatisticsVO getStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("[消费记录服务] 获取消费统计信息: userId={}, startDate={}, endDate={}",
                userId, startDate, endDate);

        try {
            // 1. 查询时间范围内的消费记录
            List<ConsumeRecordEntity> records = recordDao.selectByTimeRange(startDate, endDate);

            // 2. 过滤用户（如果指定）
            if (userId != null) {
                records = records.stream()
                        .filter(record -> userId.equals(record.getUserId()))
                        .toList();
            }

            // 3. 统计基础数据
            int totalCount = records.size();
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal avgAmount = BigDecimal.ZERO;
            BigDecimal maxAmount = BigDecimal.ZERO;
            BigDecimal minAmount = null;

            for (ConsumeRecordEntity record : records) {
                totalAmount = totalAmount.add(record.getAmount());

                if (maxAmount.compareTo(record.getAmount()) < 0) {
                    maxAmount = record.getAmount();
                }

                if (minAmount == null || minAmount.compareTo(record.getAmount()) > 0) {
                    minAmount = record.getAmount();
                }
            }

            if (totalCount > 0) {
                avgAmount = totalAmount.divide(BigDecimal.valueOf(totalCount), 2, java.math.RoundingMode.HALF_UP);
            }

            // 4. 构建统计结果（使用ConsumeStatisticsVO的现有字段）
            ConsumeStatisticsVO statistics = ConsumeStatisticsVO.builder()
                    .totalCount(totalCount)
                    .totalAmount(totalAmount)
                    .avgAmount(avgAmount)
                    .maxAmount(maxAmount)
                    .minAmount(minAmount)
                    .build();

            log.info("[消费记录服务] 消费统计查询成功: totalAmount={}, totalCount={}", totalAmount, totalCount);
            return statistics;

        } catch (Exception e) {
            log.error("[消费记录服务] 获取消费统计失败", e);
            throw ConsumeAccountException.queryFailed("获取消费统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getConsumeTrend(Long userId, Integer days) {
        log.info("[消费记录服务] 获取消费趋势数据: userId={}, days={}", userId, days);

        try {
            Map<String, Object> trend = new HashMap<>();

            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);

            List<ConsumeRecordEntity> records = recordDao.selectByTimeRange(startDate, endDate);

            // 过滤用户（如果指定）
            if (userId != null) {
                records = records.stream()
                        .filter(record -> userId.equals(record.getUserId()))
                        .toList();
            }

            // 按日期分组统计
            Map<String, BigDecimal> dailyAmount = new HashMap<>();
            Map<String, Integer> dailyCount = new HashMap<>();

            for (ConsumeRecordEntity record : records) {
                String dateKey = record.getConsumeTime().toLocalDate().toString();
                dailyAmount.put(dateKey, dailyAmount.getOrDefault(dateKey, BigDecimal.ZERO).add(record.getAmount()));
                dailyCount.put(dateKey, dailyCount.getOrDefault(dateKey, 0) + 1);
            }

            trend.put("startDate", startDate);
            trend.put("endDate", endDate);
            trend.put("dailyAmount", dailyAmount);
            trend.put("dailyCount", dailyCount);
            trend.put("totalRecords", records.size());

            log.info("[消费记录服务] 消费趋势查询成功: userId={}, days={}, totalRecords={}", userId, days, records.size());
            return trend;

        } catch (Exception e) {
            log.error("[消费记录服务] 获取消费趋势失败", e);
            throw ConsumeAccountException.queryFailed("获取消费趋势失败: " + e.getMessage());
        }
    }

    @Override
    public String exportRecords(ConsumeRecordQueryForm queryForm) {
        log.info("[消费记录服务] 导出消费记录: queryForm={}", queryForm);

        try {
            // TODO: 实现导出逻辑（使用EasyExcel）
            log.warn("[消费记录服务] 导出功能尚未实现");
            return "导出功能尚未实现";

        } catch (Exception e) {
            log.error("[消费记录服务] 导出消费记录失败", e);
            throw ConsumeAccountException.exportFailed("导出消费记录失败: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional(name = "cancel-consume-record", rollbackFor = Exception.class)
    public void cancelRecord(Long recordId, String reason) {
        log.info("[消费记录服务] 撤销消费记录: recordId={}, reason={}", recordId, reason);

        try {
            // 1. 查询消费记录
            ConsumeRecordEntity record = recordDao.selectById(recordId);
            if (record == null) {
                throw ConsumeAccountException.recordNotFound("消费记录不存在");
            }

            // 2. 判断是否可以撤销
            if (record.getRefundStatus() != null && record.getRefundStatus() > 0) {
                throw ConsumeAccountException.cancelFailed("记录已退款，无法撤销");
            }

            // 3. 增加账户余额（撤销消费）
            boolean refundSuccess = accountService.refundAmount(
                    record.getAccountId(),
                    record.getAmount(),
                    "撤销消费: " + reason
            );

            if (!refundSuccess) {
                log.error("[消费记录服务] 撤销失败：退款失败: recordId={}", recordId);
                throw ConsumeAccountException.cancelFailed("撤销失败：退款失败");
            }

            // 4. 更新消费记录状态
            record.setRefundStatus(2); // 全额退款
            record.setRefundAmount(record.getAmount());
            record.setRefundTime(LocalDateTime.now());
            record.setRefundReason("撤销: " + reason);
            record.setConsumeStatus(2); // 已退款

            recordDao.updateById(record);

            // 清除用户分析缓存
            cacheManager.evictUserAnalysisCache(record.getUserId());

            log.info("[消费记录服务] 消费记录撤销成功: recordId={}, orderNo={}", recordId, record.getOrderNo());

        } catch (ConsumeAccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("[消费记录服务] 撤销消费记录异常: recordId={}", recordId, e);
            throw ConsumeAccountException.cancelFailed("撤销消费记录异常: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional(name = "refund-consume-record", rollbackFor = Exception.class)
    public void refundRecord(Long recordId, BigDecimal refundAmount, String reason) {
        log.info("[消费记录服务] 退款处理: recordId={}, refundAmount={}, reason={}", recordId, refundAmount, reason);

        try {
            // 1. 查询消费记录
            ConsumeRecordEntity record = recordDao.selectById(recordId);
            if (record == null) {
                throw ConsumeAccountException.recordNotFound("消费记录不存在");
            }

            // 2. 验证退款金额
            if (refundAmount.compareTo(record.getAmount()) > 0) {
                throw ConsumeAccountException.refundFailed("退款金额超过消费金额");
            }

            // 3. 处理退款（调用Manager层）
            boolean success = recordManager.processRefund(recordId, refundAmount, reason);

            if (!success) {
                log.error("[消费记录服务] 退款处理失败: recordId={}", recordId);
                throw ConsumeAccountException.refundFailed("退款处理失败");
            }

            // 4. 增加账户余额
            boolean refundSuccess = accountService.refundAmount(
                    record.getAccountId(),
                    refundAmount,
                    reason
            );

            if (!refundSuccess) {
                log.error("[消费记录服务] 退款失败：增加余额失败: recordId={}", recordId);
                throw ConsumeAccountException.refundFailed("退款失败：增加余额失败");
            }

            // 清除用户分析缓存
            cacheManager.evictUserAnalysisCache(record.getUserId());

            log.info("[消费记录服务] 退款处理成功: recordId={}, refundAmount={}, orderNo={}",
                    recordId, refundAmount, record.getOrderNo());

        } catch (ConsumeAccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("[消费记录服务] 退款处理异常: recordId={}", recordId, e);
            throw ConsumeAccountException.refundFailed("退款处理异常: " + e.getMessage());
        }
    }
}
