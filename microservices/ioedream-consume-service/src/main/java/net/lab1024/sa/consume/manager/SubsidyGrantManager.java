package net.lab1024.sa.consume.manager;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.client.AccountServiceClient;
import net.lab1024.sa.consume.client.dto.BalanceIncreaseRequest;
import net.lab1024.sa.consume.client.dto.BalanceDecreaseRequest;
import net.lab1024.sa.consume.client.dto.BalanceChangeResult;

/**
 * 消费补贴发放管理器
 * <p>
 * 管理各类消费补贴的发放：
 * - 餐补发放（按月/按天）
 * - 节日补贴（春节、中秋等）
 * - 专项补贴（加班餐补、夜班补贴）
 * - 退款发放（误消费退款）
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
public class SubsidyGrantManager {

    // 补贴发放记录存储
    private final Map<String, SubsidyRecord> subsidyRecords = new ConcurrentHashMap<>();

    /**
     * 账户服务Client（用于余额管理）
     */
    @Resource
    private AccountServiceClient accountServiceClient;

    /**
     * 发放月度餐补
     *
     * @param userId 用户ID
     * @param year 年份
     * @param month 月份
     * @param amount 金额
     * @param发放人ID
     * @return 发放记录ID
     */
    @GlobalTransactional(name = "grant-monthly-meal-subsidy", rollbackFor = Exception.class)
    public String grantMonthlyMealSubsidy(Long userId, int year, int month,
                                          BigDecimal amount, Long operatorId) {
        log.info("[补贴发放] 发放月度餐补: userId={}, year={}, month={}, amount={}",
                userId, year, month, amount);

        // 检查是否已发放
        if (hasGrantedMonthlyMeal(userId, year, month)) {
            log.warn("[补贴发放] 用户{}在{}年{}月已发放过餐补", userId, year, month);
            return null;
        }

        SubsidyRecord record = createSubsidyRecord(userId, SubsidyType.MONTHLY_MEAL, amount, operatorId);
        record.setDescription(String.format("%d年%d月餐补", year, month));
        record.setGrantDate(LocalDate.of(year, month, 1));

        subsidyRecords.put(record.getRecordId(), record);

        // 实际发放到账户
        grantToUserAccount(userId, amount, record.getRecordId());

        log.info("[补贴发放] 月度餐补发放成功: recordId={}, userId={}, amount={}",
                record.getRecordId(), userId, amount);
        return record.getRecordId();
    }

    /**
     * 发放节日补贴
     *
     * @param userId 用户ID
     * @param festivalName 节日名称（SPRING_FESTIVAL-春节, MID_AUTUMN-中秋等）
     * @param amount 金额
     * @param operatorId 操作人ID
     * @return 发放记录ID
     */
    @GlobalTransactional(name = "grant-festival-subsidy", rollbackFor = Exception.class)
    public String grantFestivalSubsidy(Long userId, String festivalName,
                                       BigDecimal amount, Long operatorId) {
        log.info("[补贴发放] 发放节日补贴: userId={}, festival={}, amount={}",
                userId, festivalName, amount);

        SubsidyRecord record = createSubsidyRecord(userId, SubsidyType.FESTIVAL, amount, operatorId);
        record.setDescription(getFestivalDescription(festivalName));
        record.setGrantDate(LocalDate.now());

        subsidyRecords.put(record.getRecordId(), record);

        grantToUserAccount(userId, amount, record.getRecordId());

        log.info("[补贴发放] 节日补贴发放成功: recordId={}, festival={}",
                record.getRecordId(), festivalName);
        return record.getRecordId();
    }

    /**
     * 批量发放补贴
     *
     * @param userIds 用户ID列表
     * @param subsidyType 补贴类型
     * @param amount 金额
     * @param description 补贴描述
     * @param operatorId 操作人ID
     * @return 发放结果统计
     */
    @GlobalTransactional(name = "batch-grant-subsidy", rollbackFor = Exception.class)
    public BatchGrantResult batchGrantSubsidy(List<Long> userIds, SubsidyType subsidyType,
                                             BigDecimal amount, String description, Long operatorId) {
        log.info("[补贴发放] 批量发放补贴: count={}, type={}, amount={}",
                userIds.size(), subsidyType, amount);

        int successCount = 0;
        int failureCount = 0;
        List<String> recordIds = new ArrayList<>();

        for (Long userId : userIds) {
            try {
                SubsidyRecord record = createSubsidyRecord(userId, subsidyType, amount, operatorId);
                record.setDescription(description);
                record.setGrantDate(LocalDate.now());

                subsidyRecords.put(record.getRecordId(), record);
                grantToUserAccount(userId, amount, record.getRecordId());

                recordIds.add(record.getRecordId());
                successCount++;

            } catch (Exception e) {
                log.error("[补贴发放] 发放失败: userId={}, error={}", userId, e.getMessage(), e);
                failureCount++;
            }
        }

        BatchGrantResult result = new BatchGrantResult();
        result.setTotalCount(userIds.size());
        result.setSuccessCount(successCount);
        result.setFailureCount(failureCount);
        result.setRecordIds(recordIds);

        log.info("[补贴发放] 批量发放完成: total={}, success={}, failure={}",
                userIds.size(), successCount, failureCount);
        return result;
    }

    /**
     * 发放加班餐补
     *
     * @param userId 用户ID
     * @param overtimeDate 加班日期
     * @param overtimeHours 加班小时数
     * @param unitAmount 每小时补贴金额
     * @param operatorId 操作人ID
     * @return 发放记录ID
     */
    @GlobalTransactional(name = "grant-overtime-meal-subsidy", rollbackFor = Exception.class)
    public String grantOvertimeMealSubsidy(Long userId, LocalDate overtimeDate,
                                            int overtimeHours, BigDecimal unitAmount, Long operatorId) {
        log.info("[补贴发放] 发放加班餐补: userId={}, date={}, hours={}, unitAmount={}",
                userId, overtimeDate, overtimeHours, unitAmount);

        BigDecimal totalAmount = unitAmount.multiply(BigDecimal.valueOf(overtimeHours));

        SubsidyRecord record = createSubsidyRecord(userId, SubsidyType.OVERTIME_MEAL, totalAmount, operatorId);
        record.setDescription(String.format("加班餐补：%s加班%d小时", overtimeDate, overtimeHours));
        record.setGrantDate(LocalDate.now());

        subsidyRecords.put(record.getRecordId(), record);

        grantToUserAccount(userId, totalAmount, record.getRecordId());

        log.info("[补贴发放] 加班餐补发放成功: recordId={}, amount={}", record.getRecordId(), totalAmount);
        return record.getRecordId();
    }

    /**
     * 发放夜班补贴
     *
     * @param userId 用户ID
     * @param shiftDate 班次日期
     * @param amount 金额
     * @param operatorId 操作人ID
     * @return 发放记录ID
     */
    @GlobalTransactional(name = "grant-night-shift-subsidy", rollbackFor = Exception.class)
    public String grantNightShiftSubsidy(Long userId, LocalDate shiftDate,
                                         BigDecimal amount, Long operatorId) {
        log.info("[补贴发放] 发放夜班补贴: userId={}, date={}, amount={}",
                userId, shiftDate, amount);

        SubsidyRecord record = createSubsidyRecord(userId, SubsidyType.NIGHT_SHIFT, amount, operatorId);
        record.setDescription(String.format("夜班补贴：%s", shiftDate));
        record.setGrantDate(LocalDate.now());

        subsidyRecords.put(record.getRecordId(), record);

        grantToUserAccount(userId, amount, record.getRecordId());

        log.info("[补贴发放] 夜班补贴发放成功: recordId={}", record.getRecordId());
        return record.getRecordId();
    }

    /**
     * 发放退款（误消费退款）
     *
     * @param userId 用户ID
     * @param consumeRecordId 原消费记录ID
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @param operatorId 操作人ID
     * @return 发放记录ID
     */
    @GlobalTransactional(name = "grant-refund", rollbackFor = Exception.class)
    public String grantRefund(Long userId, String consumeRecordId,
                              BigDecimal refundAmount, String reason, Long operatorId) {
        log.info("[补贴发放] 发放退款: userId={}, consumeRecord={}, amount={}, reason={}",
                userId, consumeRecordId, refundAmount, reason);

        SubsidyRecord record = createSubsidyRecord(userId, SubsidyType.REFUND, refundAmount, operatorId);
        record.setDescription(String.format("退款：%s", reason));
        record.setGrantDate(LocalDate.now());
        record.setRelatedRecordId(consumeRecordId);

        subsidyRecords.put(record.getRecordId(), record);

        grantToUserAccount(userId, refundAmount, record.getRecordId());

        log.info("[补贴发放] 退款发放成功: recordId={}", record.getRecordId());
        return record.getRecordId();
    }

    /**
     * 查询补贴发放记录
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 发放记录列表
     */
    public List<SubsidyRecord> querySubsidyRecords(Long userId, LocalDateTime startTime,
                                                   LocalDateTime endTime) {
        log.info("[补贴发放] 查询发放记录: userId={}, startTime={}, endTime={}",
                userId, startTime, endTime);

        return subsidyRecords.values().stream()
                .filter(record -> record.getUserId().equals(userId))
                .filter(record -> record.getGrantTime().isAfter(startTime))
                .filter(record -> record.getGrantTime().isBefore(endTime))
                .sorted((r1, r2) -> r2.getGrantTime().compareTo(r1.getGrantTime()))
                .toList();
    }

    /**
     * 撤销补贴发放
     *
     * @param recordId 发放记录ID
     * @param operatorId 操作人ID
     * @param reason 撤销原因
     * @return 是否撤销成功
     */
    @GlobalTransactional(name = "revoke-subsidy", rollbackFor = Exception.class)
    public boolean revokeSubsidy(String recordId, Long operatorId, String reason) {
        log.info("[补贴发放] 撤销补贴: recordId={}, operatorId={}, reason={}",
                recordId, operatorId, reason);

        SubsidyRecord record = subsidyRecords.get(recordId);
        if (record == null) {
            log.warn("[补贴发放] 发放记录不存在: recordId={}", recordId);
            return false;
        }

        if (record.getStatus() != SubsidyStatus.GRANTED) {
            log.warn("[补贴发放] 补贴状态不允许撤销: recordId={}, status={}",
                    recordId, record.getStatus());
            return false;
        }

        // 从账户扣回补贴
        deductFromUserAccount(record.getUserId(), record.getAmount(), recordId);

        record.setStatus(SubsidyStatus.REVOKED);
        record.setRevokedBy(operatorId);
        record.setRevokedTime(LocalDateTime.now());
        record.setRevokeReason(reason);

        log.info("[补贴发放] 补贴已撤销: recordId={}", recordId);
        return true;
    }

    /**
     * 检查是否已发放月度餐补
     */
    private boolean hasGrantedMonthlyMeal(Long userId, int year, int month) {
        return subsidyRecords.values().stream()
                .anyMatch(record -> record.getUserId().equals(userId) &&
                        record.getSubsidyType() == SubsidyType.MONTHLY_MEAL &&
                        record.getGrantDate() != null &&
                        record.getGrantDate().getYear() == year &&
                        record.getGrantDate().getMonthValue() == month);
    }

    /**
     * 创建补贴记录
     */
    private SubsidyRecord createSubsidyRecord(Long userId, SubsidyType type,
                                              BigDecimal amount, Long operatorId) {
        SubsidyRecord record = new SubsidyRecord();
        record.setRecordId("SUB-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8));
        record.setUserId(userId);
        record.setSubsidyType(type);
        record.setAmount(amount);
        record.setStatus(SubsidyStatus.GRANTED);
        record.setGrantTime(LocalDateTime.now());
        record.setGrantedBy(operatorId);
        return record;
    }

    /**
     * 发放到用户账户
     * <p>
     * 调用账户服务增加用户余额，支持幂等性
     * </p>
     *
     * @param userId 用户ID
     * @param amount 增加金额
     * @param recordId 补贴发放记录ID（用作业务编号）
     * @throws RuntimeException 当账户服务调用失败时抛出
     */
    private void grantToUserAccount(Long userId, BigDecimal amount, String recordId) {
        log.info("[补贴发放] 开始发放到账户: userId={}, amount={}, recordId={}",
                userId, amount, recordId);

        try {
            // 构建请求
            BalanceIncreaseRequest request = new BalanceIncreaseRequest();
            request.setUserId(userId);
            request.setAmount(amount);
            request.setBusinessType("SUBSIDY_GRANT");
            request.setBusinessNo(recordId);
            request.setRemark("补贴发放");

            // 调用账户服务
            ResponseDTO<BalanceChangeResult> response = accountServiceClient.increaseBalance(request);

            // 处理响应
            if (response == null) {
                log.error("[补贴发放] 账户服务返回null: userId={}, amount={}, recordId={}",
                    userId, amount, recordId);
                throw new RuntimeException("账户服务返回空响应 [ACCOUNT_SERVICE_NULL_RESPONSE]");
            }

            if (!response.isSuccess()) {
                log.error("[补贴发放] 账户服务返回错误: userId={}, amount={}, recordId={}, code={}, message={}",
                    userId, amount, recordId, response.getCode(), response.getMessage());
                throw new RuntimeException("账户余额增加失败 [" + response.getCode() + "]: " + response.getMessage());
            }

            BalanceChangeResult result = response.getData();
            if (result == null || !result.getSuccess()) {
                String errorCode = result != null ? result.getErrorCode() : "BALANCE_INCREASE_FAILED";
                String errorMessage = result != null ? result.getErrorMessage() : "余额增加失败";
                log.error("[补贴发放] 余额增加失败: userId={}, amount={}, recordId={}, errorCode={}, errorMessage={}",
                    userId, amount, recordId, errorCode, errorMessage);
                throw new RuntimeException("余额增加失败 [" + errorCode + "]: " + errorMessage);
            }

            log.info("[补贴发放] 余额增加成功: userId={}, amount={}, recordId={}, transactionId={}, balanceAfter={}",
                userId, amount, recordId, result.getTransactionId(), result.getBalanceAfter());

        } catch (RuntimeException e) {
            // 重新抛出业务异常
            log.error("[补贴发放] 发放到账户异常: userId={}, amount={}, recordId={}, error={}",
                userId, amount, recordId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // 包装其他异常
            log.error("[补贴发放] 发放到账户系统异常: userId={}, amount={}, recordId={}, error={}",
                userId, amount, recordId, e.getMessage(), e);
            throw new RuntimeException("账户服务调用异常 [ACCOUNT_SERVICE_EXCEPTION]: " + e.getMessage(), e);
        }
    }

    /**
     * 从用户账户扣回
     * <p>
     * 调用账户服务扣减用户余额，支持幂等性
     * </p>
     *
     * @param userId 用户ID
     * @param amount 扣减金额
     * @param recordId 补贴发放记录ID（用作业务编号）
     * @throws RuntimeException 当账户服务调用失败时抛出
     */
    private void deductFromUserAccount(Long userId, BigDecimal amount, String recordId) {
        log.info("[补贴发放] 开始从账户扣回: userId={}, amount={}, recordId={}",
                userId, amount, recordId);

        try {
            // 构建请求
            BalanceDecreaseRequest request = new BalanceDecreaseRequest();
            request.setUserId(userId);
            request.setAmount(amount);
            request.setBusinessType("SUBSIDY_REVOKE");
            request.setBusinessNo(recordId);
            request.setRemark("补贴撤销");
            request.setCheckBalance(true); // 检查余额是否充足

            // 调用账户服务
            ResponseDTO<BalanceChangeResult> response = accountServiceClient.decreaseBalance(request);

            // 处理响应
            if (response == null) {
                log.error("[补贴发放] 账户服务返回null: userId={}, amount={}, recordId={}",
                    userId, amount, recordId);
                throw new RuntimeException("账户服务返回空响应 [ACCOUNT_SERVICE_NULL_RESPONSE]");
            }

            if (!response.isSuccess()) {
                log.error("[补贴发放] 账户服务返回错误: userId={}, amount={}, recordId={}, code={}, message={}",
                    userId, amount, recordId, response.getCode(), response.getMessage());
                throw new RuntimeException("账户余额扣减失败 [" + response.getCode() + "]: " + response.getMessage());
            }

            BalanceChangeResult result = response.getData();
            if (result == null || !result.getSuccess()) {
                log.error("[补贴发放] 余额扣减失败: userId={}, amount={}, recordId={}, errorCode={}, errorMessage={}",
                    userId, amount, recordId,
                    result != null ? result.getErrorCode() : "UNKNOWN",
                    result != null ? result.getErrorMessage() : "未知错误");

                // 判断错误类型
                String errorCode = result != null ? result.getErrorCode() : "UNKNOWN";
                String errorMessage = result != null ? result.getErrorMessage() : "余额扣减失败";

                if ("BALANCE_INSUFFICIENT".equals(errorCode)) {
                    throw new RuntimeException("账户余额不足，无法扣回补贴 [BALANCE_INSUFFICIENT]");
                } else {
                    throw new RuntimeException("余额扣减失败 [" + errorCode + "]: " + errorMessage);
                }
            }

            log.info("[补贴发放] 余额扣减成功: userId={}, amount={}, recordId={}, transactionId={}, balanceAfter={}",
                userId, amount, recordId, result.getTransactionId(), result.getBalanceAfter());

        } catch (RuntimeException e) {
            // 重新抛出业务异常
            log.error("[补贴发放] 从账户扣回异常: userId={}, amount={}, recordId={}, error={}",
                userId, amount, recordId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // 包装其他异常
            log.error("[补贴发放] 从账户扣回系统异常: userId={}, amount={}, recordId={}, error={}",
                userId, amount, recordId, e.getMessage(), e);
            throw new RuntimeException("账户服务调用异常: " + e.getMessage(), e);
        }
    }

    /**
     * 获取节日描述
     */
    private String getFestivalDescription(String festivalName) {
        return switch (festivalName) {
            case "SPRING_FESTIVAL" -> "春节补贴";
            case "MID_AUTUMN" -> "中秋补贴";
            case "DRAGON_BOAT" -> "端午补贴";
            case "NATIONAL_DAY" -> "国庆补贴";
            default -> festivalName + "补贴";
        };
    }

    /**
     * 补贴类型枚举
     */
    public enum SubsidyType {
        MONTHLY_MEAL("月度餐补"),
        FESTIVAL("节日补贴"),
        OVERTIME_MEAL("加班餐补"),
        NIGHT_SHIFT("夜班补贴"),
        REFUND("退款"),
        SPECIAL("专项补贴");

        private final String description;

        SubsidyType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 补贴状态枚举
     */
    public enum SubsidyStatus {
        GRANTED("已发放"),
        REVOKED("已撤销"),
        EXPIRED("已过期");

        private final String description;

        SubsidyStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 补贴发放记录
     */
    public static class SubsidyRecord {
        private String recordId;
        private Long userId;
        private SubsidyType subsidyType;
        private BigDecimal amount;
        private SubsidyStatus status;
        private String description;
        private LocalDate grantDate;
        private LocalDateTime grantTime;
        private Long grantedBy;
        private Long revokedBy;
        private LocalDateTime revokedTime;
        private String revokeReason;
        private String relatedRecordId;

        public String getRecordId() {
            return recordId;
        }

        public void setRecordId(String recordId) {
            this.recordId = recordId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public SubsidyType getSubsidyType() {
            return subsidyType;
        }

        public void setSubsidyType(SubsidyType subsidyType) {
            this.subsidyType = subsidyType;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public SubsidyStatus getStatus() {
            return status;
        }

        public void setStatus(SubsidyStatus status) {
            this.status = status;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public LocalDate getGrantDate() {
            return grantDate;
        }

        public void setGrantDate(LocalDate grantDate) {
            this.grantDate = grantDate;
        }

        public LocalDateTime getGrantTime() {
            return grantTime;
        }

        public void setGrantTime(LocalDateTime grantTime) {
            this.grantTime = grantTime;
        }

        public Long getGrantedBy() {
            return grantedBy;
        }

        public void setGrantedBy(Long grantedBy) {
            this.grantedBy = grantedBy;
        }

        public Long getRevokedBy() {
            return revokedBy;
        }

        public void setRevokedBy(Long revokedBy) {
            this.revokedBy = revokedBy;
        }

        public LocalDateTime getRevokedTime() {
            return revokedTime;
        }

        public void setRevokedTime(LocalDateTime revokedTime) {
            this.revokedTime = revokedTime;
        }

        public String getRevokeReason() {
            return revokeReason;
        }

        public void setRevokeReason(String revokeReason) {
            this.revokeReason = revokeReason;
        }

        public String getRelatedRecordId() {
            return relatedRecordId;
        }

        public void setRelatedRecordId(String relatedRecordId) {
            this.relatedRecordId = relatedRecordId;
        }

        @Override
        public String toString() {
            return "SubsidyRecord{" +
                    "recordId='" + recordId + '\'' +
                    ", userId=" + userId +
                    ", subsidyType=" + subsidyType +
                    ", amount=" + amount +
                    ", status=" + status +
                    ", description='" + description + '\'' +
                    ", grantDate=" + grantDate +
                    '}';
        }
    }

    /**
     * 批量发放结果
     */
    public static class BatchGrantResult {
        private int totalCount;
        private int successCount;
        private int failureCount;
        private List<String> recordIds;

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void setFailureCount(int failureCount) {
            this.failureCount = failureCount;
        }

        public List<String> getRecordIds() {
            return recordIds;
        }

        public void setRecordIds(List<String> recordIds) {
            this.recordIds = recordIds;
        }

        @Override
        public String toString() {
            return "BatchGrantResult{" +
                    "totalCount=" + totalCount +
                    ", successCount=" + successCount +
                    ", failureCount=" + failureCount +
                    '}';
        }
    }
}
