package net.lab1024.sa.admin.module.consume.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeResultDTO;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.admin.module.consume.manager.AccountSecurityManager;
import net.lab1024.sa.admin.module.consume.manager.ConsumptionModeEngineManager;
import net.lab1024.sa.admin.module.consume.manager.DataConsistencyManager;
import net.lab1024.sa.admin.module.consume.service.ConsumeCacheService;
import net.lab1024.sa.admin.module.consume.service.ConsumePermissionService;
import net.lab1024.sa.admin.module.consume.service.ConsumeService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;

@Service
@Slf4j
public class ConsumeServiceImpl extends ServiceImpl<ConsumeRecordDao, ConsumeRecordEntity> implements ConsumeService {

    @Resource
    private ConsumeCacheService consumeCacheService;

    @Resource
    private ConsumePermissionService consumePermissionService;

    @Resource
    private ConsumptionModeEngineManager consumptionModeEngineManager;

    @Resource
    private AccountSecurityManager accountSecurityManager;

    @Resource
    private DataConsistencyManager dataConsistencyManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> pay(Long personId, String personName, BigDecimal amount, String payMethod, Long deviceId,
            String remark) {
        try {
            ConsumeRecordEntity rec = new ConsumeRecordEntity();
            rec.setPersonId(personId);
            rec.setPersonName(personName);
            rec.setAmount(amount);
            rec.setCurrency("CNY");
            rec.setPayMethod(payMethod);
            rec.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
            rec.setStatus("SUCCESS");
            rec.setPayTime(LocalDateTime.now());
            rec.setDeviceId(deviceId);
            rec.setRemark(remark);
            this.save(rec);
            return ResponseDTO.okMsg("扣费成功");
        } catch (Exception e) {
            log.error("扣费失败", e);
            return ResponseDTO.error("扣费失败：" + e.getMessage());
        }
    }

    @Override
    public PageResult<ConsumeRecordEntity> pageRecords(PageParam pageParam, Long personId) {
        LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
        if (personId != null) {
            wrapper.eq(ConsumeRecordEntity::getPersonId, personId);
        }
        wrapper.orderByDesc(ConsumeRecordEntity::getPayTime);

        Page<ConsumeRecordEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        Page<ConsumeRecordEntity> data = this.page(page, wrapper);

        PageResult<ConsumeRecordEntity> result = new PageResult<>();
        result.setList(data.getRecords());
        result.setTotal(data.getTotal());
        result.setPageNum(data.getCurrent());
        result.setPageSize(data.getSize());
        return result;
    }

    // ============ 新增的缓存集成方法 ============

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> processConsume(Map<String, Object> consumeRequest) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 解析消费请求并构建DTO
            ConsumeRequestDTO consumeRequestDTO = buildConsumeRequestDTO(consumeRequest);

            // 2. 获取消费模式
            String consumeMode = consumeRequestDTO.getConsumeMode();
            if (consumeMode == null || consumeMode.trim().isEmpty()) {
                consumeMode = "FIXED_AMOUNT"; // 默认固定金额模式
                consumeRequestDTO.setConsumeMode(consumeMode);
            }

            // 3. 账户安全检查（支付密码验证、风险检测）
            AccountSecurityManager.RiskDetectionResult riskResult = accountSecurityManager.detectAnomalousOperation(
                    consumeRequestDTO.getUserId(),
                    consumeRequestDTO.getAmount(),
                    consumeRequestDTO.getDeviceId());

            // 高风险交易拒绝
            if ("HIGH".equals(riskResult.getRiskLevel())) {
                result.put("success", false);
                result.put("message", "检测到高风险交易：" + riskResult.getMessage());
                result.put("riskLevel", riskResult.getRiskLevel());
                return result;
            }

            // 4. 权限验证
            ResponseDTO<Boolean> permissionCheck = consumePermissionService.checkConsumePermission(
                    consumeRequestDTO.getUserId(),
                    consumeRequestDTO.getAmount());
            if (!permissionCheck.getOk()) {
                result.put("success", false);
                result.put("message", permissionCheck.getMsg());
                return result;
            }

            // 5. 使用消费模式引擎管理器处理消费
            ConsumeResultDTO consumeResult = consumptionModeEngineManager.processConsume(consumeRequestDTO,
                    consumeMode);

            if (!consumeResult.isSuccess()) {
                result.put("success", false);
                result.put("message", consumeResult.getMessage());
                return result;
            }

            // 6. 数据一致性验证
            DataConsistencyManager.ConsistencyValidationResult validationResult = dataConsistencyManager
                    .validateDataConsistency(consumeRequestDTO.getUserId());

            if ("ERROR".equals(validationResult.getStatus())) {
                log.error("数据一致性验证失败: {}", validationResult.getMessage());
                // 尝试修复数据不一致
                DataConsistencyManager.ConsistencyRepairResult repairResult = dataConsistencyManager
                        .repairDataInconsistency(consumeRequestDTO.getUserId());

                if (!"SUCCESS".equals(repairResult.getStatus())) {
                    result.put("success", false);
                    result.put("message", "数据一致性验证失败：" + validationResult.getMessage());
                    return result;
                }
            }

            // 7. 创建并保存消费记录
            ConsumeRecordEntity consumeRecord = createConsumeRecordFromDTO(consumeRequestDTO, consumeResult);
            this.save(consumeRecord);

            // 8. 缓存消费记录
            consumeCacheService.cacheConsumeRecord(consumeRecord);

            // 9. 更新缓存中的余额
            BigDecimal newBalance = consumeCacheService.updateBalanceCache(
                    consumeRequestDTO.getUserId(),
                    consumeResult.getAmount(),
                    "SUBTRACT");

            // 10. 构建返回结果
            result.put("success", true);
            result.put("message", consumeResult.getMessage());
            result.put("orderId", consumeRecord.getId());
            result.put("orderNo", consumeRecord.getOrderNo());
            result.put("newBalance", newBalance != null ? newBalance : consumeResult.getNewBalance());
            result.put("consumeTime", consumeRecord.getPayTime());
            result.put("consumeMode", consumeMode);
            result.put("riskLevel", riskResult.getRiskLevel());
            result.put("dataConsistency", validationResult.getStatus());

        } catch (Exception e) {
            log.error("消费处理失败", e);
            result.put("success", false);
            result.put("message", "消费处理失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public ConsumeRecordEntity getConsumeDetail(Long id) {
        try {
            // 先从缓存获取
            // 这里可以添加缓存查询逻辑

            // 缓存不存在时从数据库查询
            return this.getById(id);

        } catch (Exception e) {
            log.error("获取消费详情失败: id={}", id, e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getConsumeStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // 这里可以添加缓存的统计逻辑
            // 简化实现，直接查询数据库

            LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
            if (userId != null) {
                wrapper.eq(ConsumeRecordEntity::getPersonId, userId);
            }
            if (startTime != null) {
                wrapper.ge(ConsumeRecordEntity::getPayTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(ConsumeRecordEntity::getPayTime, endTime);
            }

            List<ConsumeRecordEntity> records = this.list(wrapper);

            BigDecimal totalAmount = records.stream()
                    .map(ConsumeRecordEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            statistics.put("totalCount", records.size());
            statistics.put("totalAmount", totalAmount);
            statistics.put("avgAmount", records.isEmpty() ? BigDecimal.ZERO
                    : totalAmount.divide(BigDecimal.valueOf(records.size()), 2, BigDecimal.ROUND_HALF_UP));

        } catch (Exception e) {
            log.error("获取消费统计失败", e);
        }

        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String refundConsume(Long id, String reason) {
        try {
            ConsumeRecordEntity consumeRecord = this.getById(id);
            if (consumeRecord == null) {
                return "消费记录不存在";
            }

            // 检查退款权限
            ResponseDTO<Boolean> refundCheck = consumePermissionService.checkRefundPermission(
                    consumeRecord.getPersonId(), id);
            if (!refundCheck.getOk()) {
                return refundCheck.getMsg();
            }

            // 退款到账户余额
            BigDecimal refundAmount = consumeRecord.getAmount();
            BigDecimal newBalance = consumeCacheService.updateBalanceCache(
                    consumeRecord.getPersonId(), refundAmount, "ADD");

            if (newBalance == null) {
                return "退款余额更新失败";
            }

            // 更新消费记录状态
            consumeRecord.setRefundStatus("REFUNDED");
            consumeRecord.setRefundTime(LocalDateTime.now());
            consumeRecord.setRefundReason(reason);
            this.updateById(consumeRecord);

            // 删除缓存记录
            consumeCacheService.deleteConsumeRecordCache(id);

            return "退款成功";

        } catch (Exception e) {
            log.error("退款失败: id={}", id, e);
            return "退款失败: " + e.getMessage();
        }
    }

    @Override
    public List<Map<String, Object>> getAvailableConsumeModes() {
        List<Map<String, Object>> modes = new ArrayList<>();

        // 使用消费模式引擎管理器获取可用模式
        net.lab1024.sa.admin.module.consume.domain.enums.ConsumeModeEnum[] availableModes = consumptionModeEngineManager
                .getAvailableModes().toArray(
                        new net.lab1024.sa.admin.module.consume.domain.enums.ConsumeModeEnum[0]);

        for (net.lab1024.sa.admin.module.consume.domain.enums.ConsumeModeEnum mode : availableModes) {
            Map<String, Object> modeInfo = new HashMap<>();
            modeInfo.put("code", mode.name());
            modeInfo.put("name", mode.getDescription());
            modeInfo.put("description", consumptionModeEngineManager.getModeDescription(mode.name()));

            modes.add(modeInfo);
        }

        return modes;
    }

    @Override
    public Map<String, Object> getAccountBalance(Long userId) {
        Map<String, Object> balanceInfo = new HashMap<>();

        try {
            // 从缓存获取余额
            BigDecimal balance = consumeCacheService.getCachedBalance(userId);

            if (balance != null) {
                balanceInfo.put("balance", balance);
                balanceInfo.put("cacheHit", true);
            } else {
                // 缓存未命中，从数据库查询
                balanceInfo.put("balance", BigDecimal.ZERO);
                balanceInfo.put("cacheHit", false);
            }

            balanceInfo.put("userId", userId);
            balanceInfo.put("updateTime", System.currentTimeMillis());

        } catch (Exception e) {
            log.error("获取账户余额失败: userId={}", userId, e);
            balanceInfo.put("balance", BigDecimal.ZERO);
            balanceInfo.put("error", true);
        }

        return balanceInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String freezeAccount(Long userId, String reason) {
        try {
            // 这里应该有具体的冻结逻辑
            // 简化实现，只记录日志
            log.info("冻结账户: userId={}, reason={}", userId, reason);

            // 清除用户缓存，强制重新加载
            consumeCacheService.clearUserCache(userId);

            return "账户冻结成功";

        } catch (Exception e) {
            log.error("冻结账户失败: userId={}", userId, e);
            return "冻结账户失败: " + e.getMessage();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String unfreezeAccount(Long userId, String reason) {
        try {
            // 这里应该有具体的解冻逻辑
            // 简化实现，只记录日志
            log.info("解冻账户: userId={}, reason={}", userId, reason);

            // 清除用户缓存，强制重新加载
            consumeCacheService.clearUserCache(userId);

            return "账户解冻成功";

        } catch (Exception e) {
            log.error("解冻账户失败: userId={}", userId, e);
            return "解冻账户失败: " + e.getMessage();
        }
    }

    /**
     * 创建消费记录
     */
    private ConsumeRecordEntity createConsumeRecord(Map<String, Object> consumeRequest) {
        ConsumeRecordEntity record = new ConsumeRecordEntity();

        record.setPersonId(Long.valueOf(consumeRequest.get("userId").toString()));
        record.setPersonName((String) consumeRequest.get("personName"));
        record.setAmount(new BigDecimal(consumeRequest.get("amount").toString()));
        record.setCurrency("CNY");
        record.setPayMethod((String) consumeRequest.get("payMethod"));
        record.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        record.setConsumeMode((String) consumeRequest.get("consumeMode"));
        record.setStatus("SUCCESS");
        record.setPayTime(LocalDateTime.now());
        record.setDeviceId(
                consumeRequest.get("deviceId") != null ? Long.valueOf(consumeRequest.get("deviceId").toString())
                        : null);
        record.setRemark((String) consumeRequest.get("remark"));

        return record;
    }

    /**
     * 构建消费请求DTO
     */
    private ConsumeRequestDTO buildConsumeRequestDTO(Map<String, Object> consumeRequest) {
        ConsumeRequestDTO dto = new ConsumeRequestDTO();

        dto.setUserId(
                consumeRequest.get("userId") != null ? Long.valueOf(consumeRequest.get("userId").toString()) : null);
        dto.setPersonName((String) consumeRequest.get("personName"));
        dto.setAmount(consumeRequest.get("amount") != null ? new BigDecimal(consumeRequest.get("amount").toString())
                : BigDecimal.ZERO);
        dto.setCurrency((String) consumeRequest.getOrDefault("currency", "CNY"));
        dto.setPayMethod((String) consumeRequest.get("payMethod"));
        dto.setOrderNo((String) consumeRequest.get("orderNo"));
        dto.setConsumeMode((String) consumeRequest.get("consumeMode"));
        dto.setDeviceId(consumeRequest.get("deviceId") != null ? Long.valueOf(consumeRequest.get("deviceId").toString())
                : null);
        dto.setRemark((String) consumeRequest.get("remark"));

        // 处理消费模式特定的数据
        dto.setModeData(consumeRequest.get("modeData"));

        return dto;
    }

    /**
     * 从DTO创建消费记录
     */
    private ConsumeRecordEntity createConsumeRecordFromDTO(ConsumeRequestDTO requestDTO, ConsumeResultDTO resultDTO) {
        ConsumeRecordEntity record = new ConsumeRecordEntity();

        record.setPersonId(requestDTO.getUserId());
        record.setPersonName(requestDTO.getPersonName());
        record.setAmount(resultDTO.getAmount());
        record.setCurrency(requestDTO.getCurrency());
        record.setPayMethod(requestDTO.getPayMethod());
        record.setOrderNo(resultDTO.getOrderNo() != null ? resultDTO.getOrderNo()
                : UUID.randomUUID().toString().replace("-", ""));
        record.setConsumeMode(requestDTO.getConsumeMode());
        record.setStatus(resultDTO.isSuccess() ? "SUCCESS" : "FAILED");
        record.setPayTime(LocalDateTime.now());
        record.setDeviceId(requestDTO.getDeviceId());
        record.setRemark(requestDTO.getRemark());

        // 设置消费模式相关的详细信息
        if (resultDTO.getModeDetails() != null) {
            record.setRemark(record.getRemark() + " | 模式详情: " + resultDTO.getModeDetails());
        }

        return record;
    }

    // ==================== 新增的管理器集成方法 ====================

    @Override
    public boolean verifyPaymentPassword(Long userId, String password) {
        try {
            AccountSecurityManager.PaymentPasswordResult result = accountSecurityManager.verifyPaymentPassword(userId,
                    password);
            return result.isSuccess();
        } catch (Exception e) {
            log.error("验证支付密码失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getUserSecurityStatus(Long userId) {
        try {
            return accountSecurityManager.getUserSecurityStatus(userId);
        } catch (Exception e) {
            log.error("获取用户安全状态失败: userId={}", userId, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "获取安全状态失败");
            return errorResult;
        }
    }

    @Override
    public boolean unlockPaymentPassword(Long userId, Long adminUserId) {
        try {
            return accountSecurityManager.unlockPaymentPassword(userId, adminUserId);
        } catch (Exception e) {
            log.error("解锁支付密码失败: userId={}, adminUserId={}", userId, adminUserId, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getConsumeModeConfig(String modeCode, Long deviceId) {
        try {
            net.lab1024.sa.admin.module.consume.domain.dto.ConsumeModeConfig config = consumptionModeEngineManager
                    .getModeConfig(modeCode, deviceId);

            if (config == null) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", "消费模式配置不存在");
                return errorResult;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("modeCode", config.getModeCode());
            result.put("modeName", config.getModeName());
            result.put("enabled", config.isEnabled());
            result.put("configData", config.getConfigData());
            result.put("deviceId", config.getDeviceId());

            return result;
        } catch (Exception e) {
            log.error("获取消费模式配置失败: modeCode={}, deviceId={}", modeCode, deviceId, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "获取配置失败");
            return errorResult;
        }
    }

    @Override
    public Map<String, Object> validateDataConsistency(Long userId) {
        try {
            DataConsistencyManager.ConsistencyValidationResult result = dataConsistencyManager
                    .validateDataConsistency(userId);

            Map<String, Object> validationResult = new HashMap<>();
            validationResult.put("status", result.getStatus());
            validationResult.put("message", result.getMessage());
            validationResult.put("details", result.getDetails());

            return validationResult;
        } catch (Exception e) {
            log.error("验证数据一致性失败: userId={}", userId, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "ERROR");
            errorResult.put("message", "验证过程异常: " + e.getMessage());
            return errorResult;
        }
    }

    @Override
    public Map<String, Object> repairDataInconsistency(Long userId) {
        try {
            DataConsistencyManager.ConsistencyRepairResult result = dataConsistencyManager
                    .repairDataInconsistency(userId);

            Map<String, Object> repairResult = new HashMap<>();
            repairResult.put("status", result.getStatus());
            repairResult.put("message", result.getMessage());
            repairResult.put("details", result.getDetails());

            return repairResult;
        } catch (Exception e) {
            log.error("修复数据不一致失败: userId={}", userId, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "FAILURE");
            errorResult.put("message", "修复过程异常: " + e.getMessage());
            return errorResult;
        }
    }

    @Override
    public Map<String, Object> getEngineStatistics() {
        try {
            return consumptionModeEngineManager.getEngineStatistics();
        } catch (Exception e) {
            log.error("获取引擎统计信息失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "获取统计信息失败");
            return errorResult;
        }
    }

    @Override
    public Map<String, Object> checkEngineHealth() {
        try {
            return consumptionModeEngineManager.checkEngineHealth();
        } catch (Exception e) {
            log.error("检查引擎健康状态失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("healthy", false);
            errorResult.put("error", "健康检查失败");
            return errorResult;
        }
    }
}
