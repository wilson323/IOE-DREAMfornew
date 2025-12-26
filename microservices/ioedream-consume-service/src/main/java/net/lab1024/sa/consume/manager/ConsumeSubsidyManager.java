package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.consume.dao.ConsumeSubsidyDao;
import net.lab1024.sa.consume.domain.vo.ConsumeSubsidyVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSubsidyStatisticsVO;
import net.lab1024.sa.common.entity.consume.ConsumeSubsidyEntity;
import net.lab1024.sa.consume.exception.ConsumeSubsidyException;

/**
 * 消费补贴业务管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 负责复杂的业务逻辑编排
 * - 处理余额管理、使用计算和业务规则验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
public class ConsumeSubsidyManager {

    private final ConsumeSubsidyDao consumeSubsidyDao;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数注入依赖
     *
     * @param consumeSubsidyDao 补贴数据访问对象
     * @param objectMapper JSON处理工具
     */
    public ConsumeSubsidyManager(ConsumeSubsidyDao consumeSubsidyDao, ObjectMapper objectMapper) {
        this.consumeSubsidyDao = consumeSubsidyDao;
        this.objectMapper = objectMapper;
    }

    /**
     * 验证补贴编码唯一性
     *
     * @param subsidyCode 补贴编码
     * @param excludeId 排除的补贴ID
     * @return 是否唯一
     */
    public boolean isSubsidyCodeUnique(String subsidyCode, Long excludeId) {
        if (subsidyCode == null || subsidyCode.trim().isEmpty()) {
            return false;
        }

        int count = consumeSubsidyDao.countByCode(subsidyCode.trim(), excludeId);
        return count == 0;
    }

    /**
     * 检查补贴是否可使用
     *
     * @param subsidy 补贴信息
     * @param currentTime 当前时间
     * @return 是否可使用
     */
    public boolean isSubsidyUsable(ConsumeSubsidyEntity subsidy, LocalDateTime currentTime) {
        if (subsidy == null || currentTime == null) {
            return false;
        }

        // 检查基本状态
        if (!subsidy.isUsable()) {
            return false;
        }

        // 检查时间有效性
        if (subsidy.getEffectiveDate() != null && currentTime.isBefore(subsidy.getEffectiveDate())) {
            return false;
        }

        if (subsidy.getExpiryDate() != null && currentTime.isAfter(subsidy.getExpiryDate())) {
            return false;
        }

        // 检查剩余金额
        if (!subsidy.hasRemaining()) {
            return false;
        }

        // 检查每日限额
        if (isDailyLimitReached(subsidy, currentTime)) {
            return false;
        }

        return true;
    }

    /**
     * 检查每日限额是否已达到
     */
    private boolean isDailyLimitReached(ConsumeSubsidyEntity subsidy, LocalDateTime currentTime) {
        if (!subsidy.hasDailyLimit()) {
            return false;
        }

        // 检查是否是同一天
        if (subsidy.getDailyUsageDate() != null) {
            LocalDate lastUsageDate = subsidy.getDailyUsageDate();
            LocalDate currentDate = currentTime.toLocalDate();

            if (!lastUsageDate.equals(currentDate)) {
                return false; // 不是同一天，每日限额重置
            }
        }

        // 检查已使用金额
        BigDecimal dailyUsed = subsidy.getDailyUsedAmount() != null ? subsidy.getDailyUsedAmount() : BigDecimal.ZERO;
        BigDecimal dailyLimit = subsidy.getDailyLimit();

        return dailyUsed.compareTo(dailyLimit) >= 0;
    }

    /**
     * 计算补贴可用金额
     *
     * @param subsidy 补贴信息
     * @param requestedAmount 请求金额
     * @return 可用金额
     */
    public BigDecimal calculateAvailableAmount(ConsumeSubsidyEntity subsidy, BigDecimal requestedAmount) {
        if (subsidy == null || !subsidy.isUsable() || requestedAmount == null) {
            return BigDecimal.ZERO;
        }

        // 检查剩余金额限制
        BigDecimal availableByRemaining = subsidy.getRemainingAmount();
        if (availableByRemaining == null || availableByRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 检查每日限额限制
        BigDecimal availableByDaily = requestedAmount;
        if (subsidy.hasDailyLimit()) {
            BigDecimal dailyRemaining = calculateDailyRemaining(subsidy);
            availableByDaily = requestedAmount.min(dailyRemaining);
        }

        return availableByRemaining.min(availableByDaily);
    }

    /**
     * 计算每日剩余金额
     */
    private BigDecimal calculateDailyRemaining(ConsumeSubsidyEntity subsidy) {
        if (!subsidy.hasDailyLimit()) {
            return BigDecimal.valueOf(Double.MAX_VALUE);
        }

        BigDecimal dailyUsed = subsidy.getDailyUsedAmount() != null ? subsidy.getDailyUsedAmount() : BigDecimal.ZERO;
        BigDecimal dailyLimit = subsidy.getDailyLimit();

        return dailyLimit.subtract(dailyUsed).max(BigDecimal.ZERO);
    }

    /**
     * 使用补贴
     *
     * @param subsidyId 补贴ID
     * @param amount 使用金额
     * @return 使用结果
     */
    public Map<String, Object> useSubsidy(Long subsidyId, BigDecimal amount) {
        Map<String, Object> result = new HashMap<>();

        if (subsidyId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            result.put("success", false);
            result.put("message", "参数错误");
            return result;
        }

        try {
            // 查询补贴信息
            ConsumeSubsidyEntity subsidy = consumeSubsidyDao.selectById(subsidyId);
            if (subsidy == null) {
                result.put("success", false);
                result.put("message", "补贴不存在");
                return result;
            }

            // 检查是否可使用
            if (!isSubsidyUsable(subsidy, LocalDateTime.now())) {
                result.put("success", false);
                result.put("message", "补贴不可使用");
                return result;
            }

            // 计算可用金额
            BigDecimal availableAmount = calculateAvailableAmount(subsidy, amount);
            if (availableAmount.compareTo(BigDecimal.ZERO) <= 0) {
                result.put("success", false);
                result.put("message", "可用金额不足");
                return result;
            }

            // 实际使用金额
            BigDecimal actualUsedAmount = amount.min(availableAmount);

            // 更新使用金额
            int updatedRows = consumeSubsidyDao.updateUsedAmount(
                subsidyId, actualUsedAmount, actualUsedAmount);

            if (updatedRows <= 0) {
                result.put("success", false);
                result.put("message", "更新使用金额失败");
                return result;
            }

            // 检查是否已用完
            ConsumeSubsidyEntity updatedSubsidy = consumeSubsidyDao.selectById(subsidyId);
            if (updatedSubsidy != null && !updatedSubsidy.hasRemaining()) {
                consumeSubsidyDao.updateStatus(subsidyId, 4); // 标记为已使用
            }

            result.put("success", true);
            result.put("message", "使用成功");
            result.put("usedAmount", actualUsedAmount);
            result.put("remainingAmount", updatedSubsidy != null ? updatedSubsidy.getRemainingAmount() : BigDecimal.ZERO);

            return result;

        } catch (Exception e) {
            log.error("使用补贴异常: subsidyId={}, amount={}, error={}", subsidyId, amount, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "使用补贴异常: " + e.getMessage());
            return result;
        }
    }

    /**
     * 验证补贴业务规则
     *
     * @param subsidy 补贴信息
     * @throws ConsumeSubsidyException 业务规则验证失败时抛出
     */
    public void validateSubsidyRules(ConsumeSubsidyEntity subsidy) {
        List<String> errors = subsidy.validateBusinessRules();

        if (!errors.isEmpty()) {
            throw ConsumeSubsidyException.validationFailed(errors);
        }

        // 验证补贴编码唯一性
        if (!isSubsidyCodeUnique(subsidy.getSubsidyCode(), subsidy.getSubsidyId())) {
            throw ConsumeSubsidyException.duplicateCode(subsidy.getSubsidyCode());
        }

        // 检查是否存在冲突补贴
        int conflictCount = consumeSubsidyDao.countConflictingSubsidies(
            subsidy.getUserId(),
            subsidy.getSubsidyType(),
            subsidy.getSubsidyPeriod(),
            subsidy.getEffectiveDate(),
            subsidy.getSubsidyId()
        );

        if (conflictCount > 0) {
            throw ConsumeSubsidyException.conflictingSubsidy("同一用户已存在相同类型和周期的补贴");
        }
    }

    /**
     * 检查补贴是否可以删除
     *
     * @param subsidyId 补贴ID
     * @return 检查结果，包含是否可删除和相关记录数
     */
    public Map<String, Object> checkDeleteSubsidy(Long subsidyId) {
        Map<String, Object> result = new HashMap<>();

        ConsumeSubsidyEntity subsidy = consumeSubsidyDao.selectById(subsidyId);
        if (subsidy == null) {
            result.put("canDelete", false);
            result.put("reason", "补贴不存在");
            return result;
        }

        // 已使用或已生效的补贴不能删除
        if (subsidy.isUsed() || subsidy.isActive()) {
            result.put("canDelete", false);
            result.put("reason", "已使用或已生效的补贴不能删除，只能作废");
            return result;
        }

        // 检查是否有关联的业务记录
        Map<String, Long> relatedRecords = consumeSubsidyDao.countRelatedRecords(subsidyId);
        result.put("canDelete", true);
        result.put("relatedRecords", relatedRecords);

        return result;
    }

    /**
     * 重置每日使用金额（每日任务）
     *
     * @param date 日期
     * @return 重置数量
     */
    public int resetDailyUsage() {
        try {
            LocalDateTime today = LocalDateTime.now();
            return consumeSubsidyDao.resetDailyUsedAmount(today);
        } catch (Exception e) {
            log.error("重置每日使用金额异常: error={}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 自动过期补贴处理
     *
     * @return 过期数量
     */
    public int autoExpireSubsidies() {
        try {
            LocalDateTime now = LocalDateTime.now();
            return consumeSubsidyDao.autoExpireSubsidies(now);
        } catch (Exception e) {
            log.error("自动过期补贴处理异常: error={}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 获取用户补贴汇总
     *
     * @param userId 用户ID
     * @return 补贴汇总信息
     */
    public Map<String, Object> getUserSubsidySummary(Long userId) {
        Map<String, Object> summary = consumeSubsidyDao.getUserSubsidySummary(userId, false);

        if (summary == null) {
            summary = new HashMap<>();
        }

        // 获取即将过期的补贴
        List<ConsumeSubsidyVO> expiringSoon = consumeSubsidyDao.selectExpiringSoon(7, userId);
        summary.put("expiringSoonCount", expiringSoon.size());
        summary.put("expiringSoon", expiringSoon);

        // 获取即将用完的补贴
        List<ConsumeSubsidyVO> nearlyDepleted = consumeSubsidyDao.selectNearlyDepleted(new BigDecimal("0.8"), userId);
        summary.put("nearlyDepletedCount", nearlyDepleted.size());
        summary.put("nearlyDepleted", nearlyDepleted);

        return summary;
    }

    /**
     * 批量发放补贴
     *
     * @param subsidyIds 补贴ID列表
     * @param issuerId 发放人ID
     * @param issuerName 发放人姓名
     * @return 发放结果
     */
    public Map<String, Object> batchIssueSubsidies(List<Long> subsidyIds, Long issuerId, String issuerName) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        if (subsidyIds == null || subsidyIds.isEmpty()) {
            result.put("success", false);
            result.put("message", "补贴ID列表为空");
            result.put("errors", errors);
            return result;
        }

        // 验证补贴状态
        for (Long subsidyId : subsidyIds) {
            try {
                ConsumeSubsidyEntity subsidy = consumeSubsidyDao.selectById(subsidyId);
                if (subsidy == null) {
                    errors.add("补贴不存在: " + subsidyId);
                    continue;
                }

                if (!subsidy.isPending()) {
                    errors.add("补贴状态不是待发放: " + subsidy.getSubsidyName());
                    continue;
                }
            } catch (Exception e) {
                errors.add("验证补贴失败: " + subsidyId);
            }
        }

        if (!errors.isEmpty()) {
            result.put("success", false);
            result.put("message", "补贴发放验证失败");
            result.put("errors", errors);
            return result;
        }

        // 执行批量发放
        try {
            LocalDateTime issueDate = LocalDateTime.now();
            int issuedCount = consumeSubsidyDao.batchIssueSubsidies(subsidyIds, issueDate, issuerId, issuerName);

            result.put("success", true);
            result.put("message", "补贴批量发放成功");
            result.put("issuedCount", issuedCount);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "补贴批量发放失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取补贴统计数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userId 用户ID（可选）
     * @param departmentId 部门ID（可选）
     * @return 统计数据
     */
    public Map<String, Object> getSubsidyStatistics(String startDate, String endDate, Long userId, Long departmentId) {
        Map<String, Object> statistics = consumeSubsidyDao.getSubsidyStatistics(startDate, endDate, userId, departmentId);

        if (statistics == null) {
            statistics = new HashMap<>();
        }

        // 按类型统计
        List<Map<String, Object>> typeStats = consumeSubsidyDao.countBySubsidyType(startDate, endDate);
        statistics.put("typeStatistics", typeStats);

        // 按部门统计
        List<Map<String, Object>> deptStats = consumeSubsidyDao.countByDepartment(startDate, endDate);
        statistics.put("departmentStatistics", deptStats);

        // 按周期统计
        List<Map<String, Object>> periodStats = consumeSubsidyDao.countByPeriod();
        statistics.put("periodStatistics", periodStats);

        // 按使用限制统计
        List<Map<String, Object>> usageLimitStats = consumeSubsidyDao.countByUsageLimit();
        statistics.put("usageLimitStatistics", usageLimitStats);

        // 余额分布统计
        List<Map<String, Object>> balanceStats = consumeSubsidyDao.getBalanceDistribution(userId);
        statistics.put("balanceDistribution", balanceStats);

        return statistics;
    }

    /**
     * 检查自动续期补贴
     *
     * @return 需要续期的补贴列表
     */
    public List<ConsumeSubsidyEntity> checkAutoRenewSubsidies() {
        try {
            LocalDateTime now = LocalDateTime.now();
            return consumeSubsidyDao.selectAutoRenewSubsidies(now);
        } catch (Exception e) {
            log.error("检查自动续期补贴异常: error={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 解析商户ID列表
     */
    public List<String> parseApplicableMerchants(String merchantsJson) throws JsonProcessingException {
        if (merchantsJson == null || merchantsJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(merchantsJson, new TypeReference<List<String>>() {});
    }

    /**
     * 解析使用时间段
     */
    public List<String> parseUsageTimePeriods(String periodsJson) throws JsonProcessingException {
        if (periodsJson == null || periodsJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(periodsJson, new TypeReference<List<String>>() {});
    }

    /**
     * 解析续期规则
     */
    public Map<String, Object> parseRenewalRule(String ruleJson) throws JsonProcessingException {
        if (ruleJson == null || ruleJson.trim().isEmpty()) {
            return new HashMap<>();
        }
        return objectMapper.readValue(ruleJson, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 获取补贴余额分布统计
     *
     * @param userId 用户ID（可选）
     * @return 余额分布统计
     */
    public List<Map<String, Object>> getBalanceDistribution(Long userId) {
        return consumeSubsidyDao.getBalanceDistribution(userId);
    }

    /**
     * 计算补贴使用率
     *
     * @param subsidyAmount 补贴总额
     * @param usedAmount 已使用金额
     * @return 使用率（百分比）
     */
    public BigDecimal calculateUsageRate(BigDecimal subsidyAmount, BigDecimal usedAmount) {
        if (subsidyAmount == null || subsidyAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal used = usedAmount != null ? usedAmount : BigDecimal.ZERO;
        return used.divide(subsidyAmount, 4, RoundingMode.HALF_UP)
                 .multiply(new BigDecimal("100"));
    }

    /**
     * 检查补贴是否即将过期
     *
     * @param subsidy 补贴信息
     * @param days 天数阈值
     * @return 是否即将过期
     */
    public boolean isExpiringSoon(ConsumeSubsidyEntity subsidy, int days) {
        if (subsidy == null || subsidy.getExpiryDate() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(days);

        return subsidy.getExpiryDate().isBefore(threshold) && subsidy.getExpiryDate().isAfter(now);
    }

    /**
     * 检查补贴是否即将用完
     *
     * @param subsidy 补贴信息
     * @param threshold 使用率阈值（如0.8表示80%）
     * @return 是否即将用完
     */
    public boolean isNearlyDepleted(ConsumeSubsidyEntity subsidy, BigDecimal threshold) {
        if (subsidy == null || !subsidy.hasRemaining()) {
            return false;
        }

        BigDecimal usageRate = subsidy.getUsagePercentage();
        return usageRate.compareTo(threshold.multiply(new BigDecimal("100"))) >= 0;
    }

    // ==================== 缺失的业务方法实现 ====================

    /**
     * 实体转VO
     */
    public ConsumeSubsidyVO convertToVO(ConsumeSubsidyEntity entity) {
        if (entity == null) {
            return null;
        }

        ConsumeSubsidyVO vo = new ConsumeSubsidyVO();
        // CON-001: 实现Entity到VO的属性映射
        vo.setSubsidyId(entity.getSubsidyId());
        vo.setSubsidyCode(entity.getSubsidyCode());
        vo.setSubsidyName(entity.getSubsidyName());
        vo.setSubsidyType(entity.getSubsidyType());
        vo.setSubsidyTypeName(entity.getSubsidyTypeDescription());
        vo.setUserId(entity.getUserId());
        vo.setUserName(entity.getUserName());
        vo.setSubsidyPeriod(entity.getSubsidyPeriod());
        vo.setSubsidyPeriodName(entity.getSubsidyPeriodDescription());
        vo.setSubsidyAmount(entity.getSubsidyAmount());
        vo.setUsedAmount(entity.getUsedAmount());
        vo.setRemainingAmount(entity.getRemainingAmount());
        vo.setStatus(entity.getSubsidyStatus());
        vo.setStatusName(entity.getStatusDescription());
        vo.setEffectiveDate(entity.getEffectiveDate());
        vo.setExpiryDate(entity.getExpiryDate());
        vo.setIssueDate(entity.getIssueDate());
        vo.setIssuerId(entity.getIssuerId());
        vo.setIssuerName(entity.getIssuerName());
        vo.setApplicableMerchants(entity.getApplicableMerchants());
        vo.setUsageTimePeriods(entity.getUsageTimePeriods());
        vo.setDailyLimit(entity.getDailyLimit());
        vo.setDailyUsedAmount(entity.getDailyUsedAmount());
        vo.setDailyUsageDate(entity.getDailyUsageDate() != null ?
            LocalDateTime.of(entity.getDailyUsageDate(), LocalDateTime.now().toLocalTime()) : null);
        vo.setMinConsumptionAmount(BigDecimal.ZERO); // Entity中暂无此字段
        vo.setSubsidyRate(BigDecimal.ONE); // Entity中暂无此字段
        vo.setAutoRenew(entity.getAutoRenew());
        vo.setRenewalRule(entity.getRenewalRule());
        vo.setSubsidySource(1); // 默认公司发放
        vo.setRemark(entity.getRemark());
        vo.setCreateUserId(entity.getCreateUserId());
        vo.setUpdateUserId(entity.getUpdateUserId());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        // 计算字段
        vo.setUsagePercentage(entity.getUsagePercentage());
        vo.setIsUsable(entity.isUsable());
        vo.setIsExpiringSoon(entity.isExpiringSoon(7));
        vo.setIsNearlyDepleted(entity.isNearlyDepleted(new BigDecimal("0.8")));

        // 计算剩余天数
        if (entity.getExpiryDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(entity.getExpiryDate())) {
                vo.setRemainingDays(0);
            } else {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                    now.toLocalDate(),
                    entity.getExpiryDate().toLocalDate()
                );
                vo.setRemainingDays((int) days);
            }
        }

        // 计算今日可使用金额
        if (entity.hasDailyLimit()) {
            vo.setTodayAvailableAmount(entity.getDailyRemainingLimit().min(entity.getRemainingAmount()));
        } else {
            vo.setTodayAvailableAmount(entity.getRemainingAmount());
        }

        log.debug("[补贴管理] Entity转VO成功: subsidyId={}, subsidyCode={}",
            entity.getSubsidyId(), entity.getSubsidyCode());
        return vo;
    }

    /**
     * 实体列表转VO列表
     */
    public List<ConsumeSubsidyVO> convertToVOList(List<ConsumeSubsidyEntity> entities) {
        List<ConsumeSubsidyVO> voList = new ArrayList<>();
        if (entities != null) {
            for (ConsumeSubsidyEntity entity : entities) {
                voList.add(convertToVO(entity));
            }
        }
        return voList;
    }

    /**
     * Form转实体
     */
    public ConsumeSubsidyEntity convertToEntity(Object form) {
        // CON-002: 实现Form到Entity的转换
        if (form == null) {
            log.warn("[补贴管理] Form为空，返回空Entity");
            return new ConsumeSubsidyEntity();
        }

        ConsumeSubsidyEntity entity = new ConsumeSubsidyEntity();

        // 处理ConsumeSubsidyAddForm
        if (form instanceof net.lab1024.sa.consume.domain.form.ConsumeSubsidyAddForm) {
            net.lab1024.sa.consume.domain.form.ConsumeSubsidyAddForm addForm =
                (net.lab1024.sa.consume.domain.form.ConsumeSubsidyAddForm) form;

            entity.setSubsidyCode(addForm.getSubsidyCode());
            entity.setSubsidyName(addForm.getSubsidyName());
            entity.setUserId(addForm.getUserId());
            entity.setSubsidyType(addForm.getSubsidyType());
            entity.setSubsidyPeriod(addForm.getSubsidyPeriod());
            entity.setSubsidyAmount(addForm.getSubsidyAmount());
            entity.setUsedAmount(BigDecimal.ZERO);
            entity.setSubsidyStatus(1); // 待发放状态
            entity.setEffectiveDate(addForm.getEffectiveDate());
            entity.setExpiryDate(addForm.getExpiryDate());
            entity.setApplicableMerchants(addForm.getApplicableMerchants());
            entity.setUsageTimePeriods(addForm.getUsageTimePeriods());
            entity.setDailyLimit(addForm.getDailyLimit());
            entity.setDailyUsedAmount(BigDecimal.ZERO);
            entity.setMaxDiscountRate(addForm.getMaxDiscountRate());
            entity.setAutoRenew(addForm.getAutoRenew());
            entity.setRenewalRule(addForm.getRenewalRule());
            entity.setRemark(addForm.getRemark());
            entity.setIssueDate(LocalDateTime.now());

            log.debug("[补贴管理] AddForm转Entity成功: subsidyCode={}, userId={}",
                addForm.getSubsidyCode(), addForm.getUserId());
        }
        // 处理其他Form类型可以在这里添加
        else {
            log.warn("[补贴管理] 不支持的Form类型: {}", form.getClass().getSimpleName());
        }

        return entity;
    }

    /**
     * 更新补贴信息
     * CON-003: 实现更新逻辑
     */
    public void updateSubsidy(ConsumeSubsidyEntity entity, Object updateForm) {
        log.info("[补贴管理] 开始更新补贴信息，ID: {}", entity.getSubsidyId());

        if (entity == null || updateForm == null) {
            log.warn("[补贴管理] 更新参数为空");
            return;
        }

        // 处理ConsumeSubsidyUpdateForm
        if (updateForm instanceof net.lab1024.sa.consume.domain.form.ConsumeSubsidyUpdateForm) {
            net.lab1024.sa.consume.domain.form.ConsumeSubsidyUpdateForm form =
                (net.lab1024.sa.consume.domain.form.ConsumeSubsidyUpdateForm) updateForm;

            // 验证版本号（乐观锁）
            if (!entity.getVersion().equals(form.getVersion())) {
                throw new ConsumeSubsidyException(
                    ConsumeSubsidyException.ErrorCode.BUSINESS_RULE_VIOLATION,
                    "数据已被其他用户修改，请刷新后重试");
            }

            // 验证是否可以更新
            if (!entity.isPending()) {
                throw new ConsumeSubsidyException(
                    ConsumeSubsidyException.ErrorCode.OPERATION_NOT_SUPPORTED,
                    "只有待发放状态的补贴可以更新");
            }

            // 更新字段（只更新非空字段）
            if (form.getSubsidyCode() != null) {
                entity.setSubsidyCode(form.getSubsidyCode());
            }
            if (form.getSubsidyName() != null) {
                entity.setSubsidyName(form.getSubsidyName());
            }
            if (form.getSubsidyType() != null) {
                entity.setSubsidyType(form.getSubsidyType());
            }
            if (form.getSubsidyPeriod() != null) {
                entity.setSubsidyPeriod(form.getSubsidyPeriod());
            }
            if (form.getEffectiveDate() != null) {
                entity.setEffectiveDate(form.getEffectiveDate());
            }
            if (form.getExpiryDate() != null) {
                entity.setExpiryDate(form.getExpiryDate());
            }
            if (form.getApplicableMerchants() != null) {
                entity.setApplicableMerchants(form.getApplicableMerchants());
            }
            if (form.getUsageTimePeriods() != null) {
                entity.setUsageTimePeriods(form.getUsageTimePeriods());
            }
            if (form.getDailyLimit() != null) {
                entity.setDailyLimit(form.getDailyLimit());
            }
            if (form.getMaxDiscountRate() != null) {
                entity.setMaxDiscountRate(form.getMaxDiscountRate());
            }
            if (form.getAutoRenew() != null) {
                entity.setAutoRenew(form.getAutoRenew());
            }
            if (form.getRenewalRule() != null) {
                entity.setRenewalRule(form.getRenewalRule());
            }
            if (form.getRemark() != null) {
                entity.setRemark(form.getRemark());
            }

            entity.setUpdateTime(LocalDateTime.now());

            log.info("[补贴管理] 补贴信息更新成功: subsidyId={}, subsidyCode={}",
                entity.getSubsidyId(), entity.getSubsidyCode());
        } else {
            log.warn("[补贴管理] 不支持的UpdateForm类型: {}", updateForm.getClass().getSimpleName());
        }
    }

    /**
     * 发放补贴
     */
    public Map<String, Object> distributeSubsidy(Long subsidyId, List<Long> userIds) {
        Map<String, Object> result = new HashMap<>();

        if (subsidyId == null || userIds == null || userIds.isEmpty()) {
            result.put("success", false);
            result.put("message", "参数错误");
            return result;
        }

        try {
            log.info("[补贴管理] 开始发放补贴，ID: {}, 用户数量: {}", subsidyId, userIds.size());

            // 查询补贴信息
            ConsumeSubsidyEntity subsidy = consumeSubsidyDao.selectById(subsidyId);
            if (subsidy == null) {
                result.put("success", false);
                result.put("message", "补贴不存在");
                return result;
            }

            // 验证补贴状态
            if (!subsidy.isPending()) {
                result.put("success", false);
                result.put("message", "补贴状态不是待发放");
                return result;
            }

            LocalDateTime now = LocalDateTime.now();
            int successCount = 0;
            int failureCount = 0;
            List<String> failedUsers = new ArrayList<>();
            List<Map<String, Object>> issuedRecords = new ArrayList<>();

            // 为每个用户创建补贴记录
            for (Long userId : userIds) {
                try {
                    // 创建用户专属的补贴记录
                    ConsumeSubsidyEntity userSubsidy = new ConsumeSubsidyEntity();
                    userSubsidy.setSubsidyCode(generateUserSubsidyCode(subsidy.getSubsidyCode(), userId));
                    userSubsidy.setSubsidyName(subsidy.getSubsidyName());
                    userSubsidy.setUserId(userId);
                    userSubsidy.setSubsidyType(subsidy.getSubsidyType());
                    userSubsidy.setSubsidyPeriod(subsidy.getSubsidyPeriod());
                    userSubsidy.setSubsidyAmount(subsidy.getSubsidyAmount());
                    userSubsidy.setUsedAmount(BigDecimal.ZERO);
                    userSubsidy.setSubsidyStatus(2); // 已发放状态
                    userSubsidy.setEffectiveDate(subsidy.getEffectiveDate());
                    userSubsidy.setExpiryDate(subsidy.getExpiryDate());
                    userSubsidy.setIssueDate(now);
                    userSubsidy.setIssuerId(subsidy.getIssuerId());
                    userSubsidy.setApplicableMerchants(subsidy.getApplicableMerchants());
                    userSubsidy.setUsageTimePeriods(subsidy.getUsageTimePeriods());
                    userSubsidy.setDailyLimit(subsidy.getDailyLimit());
                    userSubsidy.setDailyUsedAmount(BigDecimal.ZERO);
                    userSubsidy.setMaxDiscountRate(subsidy.getMaxDiscountRate());
                    userSubsidy.setAutoRenew(subsidy.getAutoRenew());
                    userSubsidy.setRenewalRule(subsidy.getRenewalRule());
                    userSubsidy.setRemark(subsidy.getRemark());
                    userSubsidy.setCreateTime(now);
                    userSubsidy.setUpdateTime(now);

                    // 插入用户补贴记录
                    int insertResult = consumeSubsidyDao.insert(userSubsidy);
                    if (insertResult > 0) {
                        successCount++;
                        Map<String, Object> record = new HashMap<>();
                        record.put("userId", userId);
                        record.put("subsidyId", userSubsidy.getSubsidyId());
                        record.put("subsidyCode", userSubsidy.getSubsidyCode());
                        record.put("amount", userSubsidy.getSubsidyAmount());
                        issuedRecords.add(record);
                    } else {
                        failureCount++;
                        failedUsers.add("用户ID: " + userId);
                    }
                } catch (Exception e) {
                    failureCount++;
                    failedUsers.add("用户ID: " + userId + " - " + e.getMessage());
                    log.error("[补贴管理] 发放补贴失败: userId={}, error={}", userId, e.getMessage());
                }
            }

            // 更新原补贴状态为已发放
            if (successCount > 0) {
                subsidy.setSubsidyStatus(2); // 已发放
                subsidy.setUpdateTime(now);
                consumeSubsidyDao.updateById(subsidy);
            }

            result.put("success", failureCount == 0);
            result.put("message", String.format("发放完成，成功: %d, 失败: %d", successCount, failureCount));
            result.put("totalCount", userIds.size());
            result.put("successCount", successCount);
            result.put("failureCount", failureCount);
            result.put("failedUsers", failedUsers);
            result.put("issuedRecords", issuedRecords);

            log.info("[补贴管理] 补贴发放完成: subsidyId={}, 成功={}, 失败={}",
                subsidyId, successCount, failureCount);

        } catch (Exception e) {
            log.error("[补贴管理] 发放失败: subsidyId={}, error={}", subsidyId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "发放失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 生成用户补贴编码
     */
    private String generateUserSubsidyCode(String baseCode, Long userId) {
        return baseCode + "_U" + userId + "_" + System.currentTimeMillis();
    }

    /**
     * 批量发放补贴
     * CON-005: 实现批量发放逻辑
     */
    public Map<String, Object> batchDistributeSubsidy(Object addForm, List<Long> userIds) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("[补贴批量发放] 开始批量发放，用户数量: {}", userIds == null ? 0 : userIds.size());

            // 参数验证
            if (addForm == null) {
                result.put("success", false);
                result.put("message", "补贴信息不能为空");
                return result;
            }

            if (userIds == null || userIds.isEmpty()) {
                result.put("success", false);
                result.put("message", "用户列表不能为空");
                return result;
            }

            // 转换Form到Entity
            ConsumeSubsidyEntity templateEntity = convertToEntity(addForm);
            if (templateEntity == null) {
                result.put("success", false);
                result.put("message", "补贴信息转换失败");
                return result;
            }

            LocalDateTime now = LocalDateTime.now();
            int successCount = 0;
            int failureCount = 0;
            List<String> failedUsers = new ArrayList<>();
            List<Map<String, Object>> issuedRecords = new ArrayList<>();

            // 为每个用户创建补贴记录
            for (Long userId : userIds) {
                try {
                    // 创建用户专属的补贴记录
                    ConsumeSubsidyEntity userSubsidy = new ConsumeSubsidyEntity();

                    // 复制模板信息
                    userSubsidy.setSubsidyCode(generateUserSubsidyCode(templateEntity.getSubsidyCode(), userId));
                    userSubsidy.setSubsidyName(templateEntity.getSubsidyName());
                    userSubsidy.setSubsidyType(templateEntity.getSubsidyType());
                    userSubsidy.setUserId(userId);
                    userSubsidy.setSubsidyAmount(templateEntity.getSubsidyAmount());
                    userSubsidy.setUsedAmount(BigDecimal.ZERO);
                    userSubsidy.setIssueDate(now);
                    userSubsidy.setEffectiveDate(templateEntity.getEffectiveDate() != null ?
                        templateEntity.getEffectiveDate() : now);
                    userSubsidy.setExpiryDate(templateEntity.getExpiryDate());
                    userSubsidy.setSubsidyStatus(2); // 已发放
                    userSubsidy.setRemark(templateEntity.getRemark());
                    userSubsidy.setCreateTime(now);
                    userSubsidy.setUpdateTime(now);
                    userSubsidy.setDeletedFlag(0); // Integer类型，0表示未删除
                    userSubsidy.setVersion(0);

                    // 插入用户补贴记录
                    int insertResult = consumeSubsidyDao.insert(userSubsidy);
                    if (insertResult > 0) {
                        successCount++;

                        // 记录发放详情
                        Map<String, Object> issuedRecord = new HashMap<>();
                        issuedRecord.put("subsidyId", userSubsidy.getSubsidyId());
                        issuedRecord.put("subsidyCode", userSubsidy.getSubsidyCode());
                        issuedRecord.put("userId", userId);
                        issuedRecord.put("amount", userSubsidy.getSubsidyAmount());
                        issuedRecord.put("effectiveDate", userSubsidy.getEffectiveDate());
                        issuedRecord.put("expiryDate", userSubsidy.getExpiryDate());
                        issuedRecords.add(issuedRecord);

                        log.debug("[补贴批量发放] 发放成功: userId={}, subsidyId={}, subsidyCode={}",
                            userId, userSubsidy.getSubsidyId(), userSubsidy.getSubsidyCode());
                    } else {
                        failureCount++;
                        failedUsers.add("用户ID: " + userId + " - 插入失败");
                    }
                } catch (Exception e) {
                    failureCount++;
                    failedUsers.add("用户ID: " + userId + " - " + e.getMessage());
                    log.error("[补贴批量发放] 发放失败: userId={}, error={}", userId, e.getMessage(), e);
                }
            }

            // 构建返回结果
            result.put("success", failureCount == 0);
            result.put("message", String.format("批量发放完成，成功: %d, 失败: %d", successCount, failureCount));
            result.put("totalUsers", userIds.size());
            result.put("successCount", successCount);
            result.put("failureCount", failureCount);
            result.put("failedUsers", failedUsers);
            result.put("issuedRecords", issuedRecords);
            result.put("issueTime", now);

            log.info("[补贴批量发放] 批量发放完成: 总数={}, 成功={}, 失败={}",
                userIds.size(), successCount, failureCount);

        } catch (Exception e) {
            log.error("[补贴批量发放] 批量发放失败: error={}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "批量发放失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 作废补贴
     * CON-006: 实现作废逻辑
     */
    public Map<String, Object> cancelSubsidy(Long subsidyId) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("[补贴作废] 开始作废补贴，ID: {}", subsidyId);

            // 参数验证
            if (subsidyId == null) {
                result.put("success", false);
                result.put("message", "补贴ID不能为空");
                return result;
            }

            // 查询补贴信息
            ConsumeSubsidyEntity entity = consumeSubsidyDao.selectById(subsidyId);
            if (entity == null) {
                result.put("success", false);
                result.put("message", "补贴不存在");
                return result;
            }

            // 验证当前状态
            if (entity.getSubsidyStatus() == 5) {
                result.put("success", false);
                result.put("message", "补贴已作废，无需重复操作");
                return result;
            }

            // 验证是否可以作废
            // 只有待发放(1)和已发放(2)状态可以作废
            if (entity.getSubsidyStatus() != 1 && entity.getSubsidyStatus() != 2) {
                result.put("success", false);
                result.put("message", "只有待发放或已发放状态的补贴可以作废");
                return result;
            }

            // 如果已发放且有使用金额，不允许作废
            if (entity.getSubsidyStatus() == 2 && entity.getUsedAmount() != null
                && entity.getUsedAmount().compareTo(BigDecimal.ZERO) > 0) {
                result.put("success", false);
                result.put("message", "补贴已使用，无法作废");
                return result;
            }

            LocalDateTime now = LocalDateTime.now();

            // 更新状态为已作废(5)
            entity.setSubsidyStatus(5); // 已作废
            entity.setUpdateTime(now);

            int updateResult = consumeSubsidyDao.updateById(entity);
            if (updateResult > 0) {
                result.put("success", true);
                result.put("message", "作废成功");
                result.put("subsidyId", entity.getSubsidyId());
                result.put("subsidyCode", entity.getSubsidyCode());
                result.put("cancelTime", now);

                log.info("[补贴作废] 补贴作废成功: subsidyId={}, subsidyCode={}",
                    entity.getSubsidyId(), entity.getSubsidyCode());
            } else {
                result.put("success", false);
                result.put("message", "作废失败：更新数据库失败");
            }

        } catch (Exception e) {
            log.error("[补贴作废] 作废失败: subsidyId={}, error={}", subsidyId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "作废失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 延期补贴
     * CON-007: 实现延期逻辑
     */
    public Map<String, Object> extendSubsidy(Long subsidyId, Integer days) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("[补贴延期] 开始延期补贴，ID: {}, 延期天数: {}", subsidyId, days);

            // 参数验证
            if (subsidyId == null) {
                result.put("success", false);
                result.put("message", "补贴ID不能为空");
                return result;
            }

            if (days == null || days <= 0) {
                result.put("success", false);
                result.put("message", "延期天数必须大于0");
                return result;
            }

            // 查询补贴信息
            ConsumeSubsidyEntity entity = consumeSubsidyDao.selectById(subsidyId);
            if (entity == null) {
                result.put("success", false);
                result.put("message", "补贴不存在");
                return result;
            }

            // 验证是否有过期时间
            if (entity.getExpiryDate() == null) {
                result.put("success", false);
                result.put("message", "补贴没有过期时间，无法延期");
                return result;
            }

            // 验证当前状态
            if (entity.getSubsidyStatus() == 5) { // 5=已作废
                result.put("success", false);
                result.put("message", "已作废的补贴无法延期");
                return result;
            }

            // 验证是否已过期
            LocalDateTime now = LocalDateTime.now();
            if (entity.getExpiryDate().isBefore(now)) {
                result.put("success", false);
                result.put("message", "补贴已过期，无法延期");
                return result;
            }

            // 保存旧的过期时间用于日志
            LocalDateTime oldExpiryDate = entity.getExpiryDate();

            // 计算新的过期时间
            LocalDateTime newExpiryDate = entity.getExpiryDate().plusDays(days);
            entity.setExpiryDate(newExpiryDate);
            entity.setUpdateTime(now);

            // 更新数据库
            int updateResult = consumeSubsidyDao.updateById(entity);
            if (updateResult > 0) {
                result.put("success", true);
                result.put("message", String.format("延期成功，延期 %d 天", days));
                result.put("subsidyId", entity.getSubsidyId());
                result.put("subsidyCode", entity.getSubsidyCode());
                result.put("oldExpiryDate", oldExpiryDate);
                result.put("newExpiryDate", newExpiryDate);
                result.put("extendedDays", days);

                log.info("[补贴延期] 补贴延期成功: subsidyId={}, subsidyCode={}, 旧过期时间={}, 新过期时间={}, 延期天数={}",
                    entity.getSubsidyId(), entity.getSubsidyCode(), oldExpiryDate, newExpiryDate, days);
            } else {
                result.put("success", false);
                result.put("message", "延期失败：更新数据库失败");
            }

        } catch (Exception e) {
            log.error("[补贴延期] 延期失败: subsidyId={}, days={}, error={}", subsidyId, days, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "延期失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取补贴统计信息
     * CON-008: 实现统计逻辑
     */
    public ConsumeSubsidyStatisticsVO getSubsidyStatistics() {
        log.info("[补贴统计] 开始获取补贴统计信息");

        ConsumeSubsidyStatisticsVO statistics = new ConsumeSubsidyStatisticsVO();

        try {
            LocalDateTime now = LocalDateTime.now();

            // 查询所有补贴记录
            List<ConsumeSubsidyEntity> allSubsidies = consumeSubsidyDao.selectList(
                new LambdaQueryWrapper<ConsumeSubsidyEntity>()
                    .eq(ConsumeSubsidyEntity::getDeletedFlag, 0)
            );

            if (allSubsidies == null || allSubsidies.isEmpty()) {
                log.info("[补贴统计] 无补贴记录");
                return statistics;
            }

            // 统计总量
            int totalCount = allSubsidies.size();
            statistics.setTotalSubsidyCount((long) totalCount);

            // 按状态统计
            long pendingCount = allSubsidies.stream()
                .filter(s -> s.getSubsidyStatus() == 1).count();
            long issuedCount = allSubsidies.stream()
                .filter(s -> s.getSubsidyStatus() == 2).count();
            long expiredCount = allSubsidies.stream()
                .filter(s -> s.getSubsidyStatus() == 3).count();
            long usedCount = allSubsidies.stream()
                .filter(s -> s.getSubsidyStatus() == 4).count();
            long cancelledCount = allSubsidies.stream()
                .filter(s -> s.getSubsidyStatus() == 5).count();

            statistics.setIssuedSubsidyCount(issuedCount);
            statistics.setUsedSubsidyCount(usedCount);
            statistics.setExpiredSubsidyCount(expiredCount);
            statistics.setCancelledSubsidyCount(cancelledCount);

            // 金额统计
            BigDecimal totalAmount = allSubsidies.stream()
                .map(ConsumeSubsidyEntity::getSubsidyAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.setTotalSubsidyAmount(totalAmount);

            BigDecimal totalUsedAmount = allSubsidies.stream()
                .map(ConsumeSubsidyEntity::getUsedAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.setUsedSubsidyAmount(totalUsedAmount);

            BigDecimal totalRemainingAmount = totalAmount.subtract(totalUsedAmount);
            statistics.setRemainingSubsidyAmount(totalRemainingAmount);

            // 使用率
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal usageRate = totalUsedAmount.divide(totalAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
                statistics.setOverallUsageRate(usageRate);
            } else {
                statistics.setOverallUsageRate(BigDecimal.ZERO);
            }

            // 今日发放统计
            LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
            long todayIssuedCount = allSubsidies.stream()
                .filter(s -> s.getIssueDate() != null && !s.getIssueDate().isBefore(todayStart))
                .count();
            statistics.setMonthlyIssuedSubsidies(todayIssuedCount);

            // 本月发放统计
            LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
            long monthIssuedCount = allSubsidies.stream()
                .filter(s -> s.getIssueDate() != null && !s.getIssueDate().isBefore(monthStart))
                .count();
            statistics.setMonthlyIssuedSubsidies(monthIssuedCount);

            // 本月发放金额
            BigDecimal monthIssuedAmount = allSubsidies.stream()
                .filter(s -> s.getIssueDate() != null && !s.getIssueDate().isBefore(monthStart))
                .map(ConsumeSubsidyEntity::getSubsidyAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.setMonthlySubsidyAmount(monthIssuedAmount);

            // 本月使用金额
            BigDecimal monthUsedAmount = allSubsidies.stream()
                .filter(s -> s.getUsedAmount() != null && s.getUsedAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(ConsumeSubsidyEntity::getUsedAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.setMonthlyUsedAmount(monthUsedAmount);

            // 即将过期统计（7天内过期）
            LocalDateTime sevenDaysLater = now.plusDays(7);
            long expiringSoonCount = allSubsidies.stream()
                .filter(s -> s.getExpiryDate() != null
                    && s.getExpiryDate().isAfter(now)
                    && !s.getExpiryDate().isAfter(sevenDaysLater)
                    && s.getSubsidyStatus() == 2)
                .count();
            statistics.setExpiringSoonCount(expiringSoonCount);

            // 计算即将过期金额
            BigDecimal expiringSoonAmount = allSubsidies.stream()
                .filter(s -> s.getExpiryDate() != null
                    && s.getExpiryDate().isAfter(now)
                    && !s.getExpiryDate().isAfter(sevenDaysLater)
                    && s.getSubsidyStatus() == 2)
                .map(s -> {
                    BigDecimal remaining = s.getSubsidyAmount();
                    if (s.getUsedAmount() != null) {
                        remaining = remaining.subtract(s.getUsedAmount());
                    }
                    return remaining;
                })
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.setExpiringSoonAmount(expiringSoonAmount);

            // 统计时间
            statistics.setStatisticsTime(now);

            log.info("[补贴统计] 补贴统计完成: 总数={}, 总金额={}, 使用率={}%",
                totalCount, totalAmount, statistics.getOverallUsageRate());

        } catch (Exception e) {
            log.error("[补贴统计] 统计失败: error={}", e.getMessage(), e);
        }

        return statistics;
    }
}