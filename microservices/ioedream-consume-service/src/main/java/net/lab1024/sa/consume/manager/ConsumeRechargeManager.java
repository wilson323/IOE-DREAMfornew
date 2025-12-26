package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.lab1024.sa.consume.dao.ConsumeRechargeDao;
import net.lab1024.sa.common.entity.consume.ConsumeRechargeEntity;
import net.lab1024.sa.consume.domain.vo.ConsumeRechargeStatisticsVO;
import net.lab1024.sa.consume.exception.ConsumeRechargeException;

import lombok.extern.slf4j.Slf4j;

/**
 * 消费充值业务管理器
 * <p>
 * 严格遵循CLAUDE.md规范： - 纯Java类，不使用Spring注解 - 通过构造函数注入依赖 - 负责复杂的业务逻辑编排 - 处理充值记录管理、统计分析和业务规则验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
public class ConsumeRechargeManager {

    private final ConsumeRechargeDao consumeRechargeDao;

    /**
     * 构造函数注入依赖
     *
     * @param consumeRechargeDao
     *            充值记录数据访问对象
     */
    public ConsumeRechargeManager (ConsumeRechargeDao consumeRechargeDao) {
        this.consumeRechargeDao = consumeRechargeDao;
    }

    /**
     * 验证充值记录唯一性
     *
     * @param transactionNo
     *            交易流水号
     * @param thirdPartyNo
     *            第三方交易号
     * @param excludeId
     *            排除的记录ID
     * @return 是否唯一
     */
    public boolean isTransactionUnique (String transactionNo, String thirdPartyNo, Long excludeId) {
        if (transactionNo != null && !transactionNo.trim ().isEmpty ()) {
            int count = consumeRechargeDao.countByTransactionNo (transactionNo.trim (), excludeId);
            if (count > 0) {
                return false;
            }
        }

        if (thirdPartyNo != null && !thirdPartyNo.trim ().isEmpty ()) {
            int count = consumeRechargeDao.countByThirdPartyNo (thirdPartyNo.trim (), excludeId);
            if (count > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 生成交易流水号
     *
     * @param userId
     *            用户ID
     * @param rechargeWay
     *            充值方式
     * @return 交易流水号
     */
    public String generateTransactionNo (Long userId, Integer rechargeWay) {
        LocalDateTime now = LocalDateTime.now ();
        String timestamp = now.toString ().replace ("-", "").replace ("T", "").replace (":", "").substring (0, 14);
        String random = UUID.randomUUID ().toString ().substring (0, 8).toUpperCase ();
        return String.format ("RCG%s%s%d%s", timestamp, rechargeWay, userId, random);
    }

    /**
     * 生成批次号
     *
     * @return 批次号
     */
    public String generateBatchNo () {
        LocalDateTime now = LocalDateTime.now ();
        String timestamp = now.toString ().replace ("-", "").replace ("T", "").replace (":", "").substring (0, 14);
        String random = UUID.randomUUID ().toString ().substring (0, 6).toUpperCase ();
        return String.format ("BATCH%s%s", timestamp, random);
    }

    /**
     * 验证充值金额
     *
     * @param amount
     *            充值金额
     * @return 验证结果
     */
    public Map<String, Object> validateRechargeAmount (BigDecimal amount) {
        Map<String, Object> result = new HashMap<> ();
        List<String> errors = new ArrayList<> ();
        List<String> warnings = new ArrayList<> ();

        if (amount == null) {
            errors.add ("充值金额不能为空");
            result.put ("valid", false);
            result.put ("errors", errors);
            result.put ("warnings", warnings);
            return result;
        }

        // 检查金额范围
        if (amount.compareTo (BigDecimal.ZERO) <= 0) {
            errors.add ("充值金额必须大于0");
        }

        if (amount.compareTo (new BigDecimal ("0.01")) < 0) {
            errors.add ("充值金额不能小于0.01元");
        }

        if (amount.compareTo (new BigDecimal ("10000")) > 0) {
            warnings.add ("充值金额较大，建议分批充值");
        }

        if (amount.compareTo (new BigDecimal ("50000")) > 0) {
            errors.add ("单次充值金额不能超过50000元");
        }

        // 检查小数位数
        if (amount.scale () > 2) {
            warnings.add ("充值金额小数位数超过2位，将四舍五入处理");
        }

        result.put ("valid", errors.isEmpty ());
        result.put ("errors", errors);
        result.put ("warnings", warnings);
        result.put ("validatedAmount", amount.setScale (2, RoundingMode.HALF_UP));

        return result;
    }

    /**
     * 验证充值业务规则
     *
     * @param rechargeRecord
     *            充值记录
     * @throws ConsumeRechargeException
     *             业务规则验证失败时抛出
     */
    public void validateRechargeRules (ConsumeRechargeEntity rechargeRecord) {
        List<String> errors = rechargeRecord.validateBusinessRules ();

        if (!errors.isEmpty ()) {
            throw ConsumeRechargeException.validationFailed (errors);
        }

        // 验证交易流水号唯一性
        if (!isTransactionUnique (rechargeRecord.getTransactionNo (), rechargeRecord.getThirdPartyNo (),
                rechargeRecord.getRecordId ())) {
            throw ConsumeRechargeException.duplicateTransaction (rechargeRecord.getTransactionNo (),
                    rechargeRecord.getThirdPartyNo ());
        }

        // 验证充值金额
        Map<String, Object> amountValidation = validateRechargeAmount (rechargeRecord.getRechargeAmount ());
        if (!(Boolean) amountValidation.get ("valid")) {
            @SuppressWarnings("unchecked")
            List<String> validationErrors = (List<String>) amountValidation.get ("errors");
            throw ConsumeRechargeException.invalidAmount (String.join (", ", validationErrors));
        }

        // 检查是否为异常充值
        if (isAbnormalRecharge (rechargeRecord)) {
            log.warn ("检测到异常充值记录: recordId={}, amount={}, way={}, channel={}", rechargeRecord.getRecordId (),
                    rechargeRecord.getRechargeAmount (), rechargeRecord.getRechargeWay (),
                    rechargeRecord.getRechargeChannel ());
        }
    }

    /**
     * 检查是否为异常充值
     *
     * @param rechargeRecord
     *            充值记录
     * @return 是否异常
     */
    public boolean isAbnormalRecharge (ConsumeRechargeEntity rechargeRecord) {
        // 大额充值检查
        if (rechargeRecord.getRechargeAmount ().compareTo (new BigDecimal ("10000")) > 0) {
            return true;
        }

        // 非正常时间充值检查（凌晨2-6点）
        if (rechargeRecord.getRechargeTime () != null) {
            int hour = rechargeRecord.getRechargeTime ().getHour ();
            if (hour >= 2 && hour <= 6) {
                return true;
            }
        }

        // 异常充值方式检查
        if (rechargeRecord.getRechargeWay () == 1) { // 现金充值大额
            if (rechargeRecord.getRechargeAmount ().compareTo (new BigDecimal ("5000")) > 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * 计算充值统计数据
     *
     * @param userId
     *            用户ID（可选）
     * @param startDate
     *            开始日期
     * @param endDate
     *            结束日期
     * @return 统计数据
     */
    public ConsumeRechargeStatisticsVO calculateStatistics (Long userId, LocalDateTime startDate,
            LocalDateTime endDate) {
        ConsumeRechargeStatisticsVO statistics = ConsumeRechargeStatisticsVO.builder ().build ();

        try {
            Map<String, Object> basicStats = consumeRechargeDao.getBasicStatistics (userId, startDate, endDate);

            statistics.setTotalCount ( ((Number) basicStats.get ("totalCount")).intValue ());
            statistics.setTotalAmount ((BigDecimal) basicStats.get ("totalAmount"));
            statistics.setSuccessCount ( ((Number) basicStats.get ("successCount")).longValue ());
            statistics.setSuccessAmount ((BigDecimal) basicStats.get ("successAmount"));
            statistics.setFailureCount ( ((Number) basicStats.get ("failureCount")).longValue ());
            statistics.setProcessingCount ( ((Number) basicStats.get ("processingCount")).longValue ());

            // 计算成功率
            if (statistics.getTotalCount () > 0) {
                BigDecimal successRate = BigDecimal.valueOf (statistics.getSuccessCount ())
                        .divide (BigDecimal.valueOf (statistics.getTotalCount ()), 4, RoundingMode.HALF_UP)
                        .multiply (new BigDecimal ("100"));
                statistics.setSuccessRate (successRate);
            }

            // 获取平均充值金额
            Map<String, Object> avgStats = consumeRechargeDao.getAverageStatistics (userId, startDate, endDate);
            statistics.setAverageAmount ((BigDecimal) avgStats.get ("averageAmount"));
            statistics.setMaxAmount ((BigDecimal) avgStats.get ("maxAmount"));
            statistics.setMinAmount ((BigDecimal) avgStats.get ("minAmount"));

            // 获取今日统计
            Map<String, Object> todayStats = consumeRechargeDao.getTodayStatistics (userId);
            statistics.setTodayCount ( ((Number) todayStats.get ("todayCount")).intValue ());
            statistics.setTodayAmount ((BigDecimal) todayStats.get ("todayAmount"));

            // 获取本月统计
            Map<String, Object> monthStats = consumeRechargeDao.getMonthStatistics (userId);
            statistics.setMonthCount ( ((Number) monthStats.get ("monthCount")).intValue ());
            statistics.setMonthAmount ((BigDecimal) monthStats.get ("monthAmount"));

            statistics.setStatisticsTime (LocalDateTime.now ());

            return statistics;

        } catch (Exception e) {
            log.error ("计算充值统计数据失败: userId={}, startDate={}, endDate={}, error={}", userId, startDate, endDate,
                    e.getMessage (), e);
            throw ConsumeRechargeException.databaseError ("计算统计数据", e.getMessage ());
        }
    }

    /**
     * 获取充值方式统计
     *
     * @param startDate
     *            开始日期
     * @param endDate
     *            结束日期
     * @return 方式统计
     */
    public List<Map<String, Object>> getRechargeWayStatistics (LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return consumeRechargeDao.getRechargeWayStatistics (startDate, endDate);
        } catch (Exception e) {
            log.error ("获取充值方式统计失败: startDate={}, endDate={}, error={}", startDate, endDate, e.getMessage (), e);
            throw ConsumeRechargeException.databaseError ("获取充值方式统计", e.getMessage ());
        }
    }

    /**
     * 获取充值趋势数据
     *
     * @param days
     *            天数
     * @return 趋势数据
     */
    public List<Map<String, Object>> getRechargeTrend (Integer days) {
        try {
            // 默认7天
            int trendDays = (days == null || days <= 0) ? 7 : days;
            LocalDateTime endDate = LocalDateTime.now ();
            LocalDateTime startDate = endDate.minusDays (trendDays - 1);
            return consumeRechargeDao.getRechargeTrend (startDate, endDate);
        } catch (Exception e) {
            log.error ("获取充值趋势数据失败: days={}, error={}", days, e.getMessage (), e);
            throw ConsumeRechargeException.databaseError ("获取充值趋势数据", e.getMessage ());
        }
    }

    /**
     * 批量充值预处理
     *
     * @param userIds
     *            用户ID列表
     * @param amount
     *            充值金额
     * @param rechargeWay
     *            充值方式
     * @param operatorId
     *            操作员ID
     * @return 批量处理结果
     */
    public Map<String, Object> prepareBatchRecharge (List<Long> userIds, BigDecimal amount, Integer rechargeWay,
            Long operatorId) {
        Map<String, Object> result = new HashMap<> ();
        List<String> errors = new ArrayList<> ();
        List<Map<String, Object>> userRecharges = new ArrayList<> ();

        if (userIds == null || userIds.isEmpty ()) {
            result.put ("success", false);
            result.put ("message", "用户ID列表为空");
            return result;
        }

        // 验证充值金额
        Map<String, Object> amountValidation = validateRechargeAmount (amount);
        if (!(Boolean) amountValidation.get ("valid")) {
            @SuppressWarnings("unchecked")
            List<String> validationErrors = (List<String>) amountValidation.get ("errors");
            result.put ("success", false);
            result.put ("message", "充值金额验证失败: " + String.join (", ", validationErrors));
            return result;
        }

        BigDecimal totalAmount = amount.multiply (new BigDecimal (userIds.size ()));
        String batchNo = generateBatchNo ();

        // 检查每个用户的有效性
        for (Long userId : userIds) {
            try {
                Map<String, Object> userRecharge = new HashMap<> ();
                userRecharge.put ("userId", userId);
                userRecharge.put ("amount", amount);
                userRecharge.put ("rechargeWay", rechargeWay);
                userRecharge.put ("batchNo", batchNo);

                // 这里可以添加用户存在性检查、账户状态检查等
                // userRecharge.put("userExists", checkUserExists(userId));
                // userRecharge.put("accountActive", checkAccountActive(userId));

                userRecharges.add (userRecharge);
            } catch (Exception e) {
                errors.add ("用户ID " + userId + " 预处理失败: " + e.getMessage ());
            }
        }

        result.put ("success", errors.isEmpty ());
        result.put ("message", errors.isEmpty () ? "批量充值预处理成功" : "部分用户预处理失败");
        result.put ("batchNo", batchNo);
        result.put ("userCount", userRecharges.size ());
        result.put ("totalAmount", totalAmount);
        result.put ("userRecharges", userRecharges);
        result.put ("errors", errors);
        result.put ("timestamp", LocalDateTime.now ());

        return result;
    }

    /**
     * 检查充值记录是否可以冲正
     *
     * @param recordId
     *            充值记录ID
     * @return 检查结果
     */
    public Map<String, Object> checkRechargeReversibility (Long recordId) {
        Map<String, Object> result = new HashMap<> ();
        List<String> restrictions = new ArrayList<> ();

        ConsumeRechargeEntity record = consumeRechargeDao.selectById (recordId);
        if (record == null) {
            result.put ("canReverse", false);
            result.put ("reason", "充值记录不存在");
            return result;
        }

        // 检查充值状态
        if (!record.isSuccess ()) {
            restrictions.add ("只能冲正成功的充值记录");
        }

        // 检查是否已冲正
        if (record.isReversed ()) {
            restrictions.add ("该充值记录已冲正");
        }

        // 检查时间限制（30天内可冲正）
        if (record.getRechargeTime () != null) {
            LocalDateTime now = LocalDateTime.now ();
            LocalDateTime limit = record.getRechargeTime ().plusDays (30);
            if (now.isAfter (limit)) {
                restrictions.add ("充值记录超过30天，无法冲正");
            }
        }

        // 检查账户余额
        if (record.getAfterBalance () != null && record.getRechargeAmount () != null) {
            if (record.getAfterBalance ().compareTo (record.getRechargeAmount ()) < 0) {
                restrictions.add ("当前账户余额不足，无法冲正");
            }
        }

        // 检查是否有后续消费记录
        int subsequentTransactions = consumeRechargeDao.countSubsequentTransactions (record.getUserId (),
                record.getRechargeTime ());
        if (subsequentTransactions > 0) {
            restrictions.add ("该充值记录已有后续消费，冲正可能影响正常业务");
        }

        result.put ("canReverse", restrictions.isEmpty ());
        result.put ("record", record);
        result.put ("restrictions", restrictions);
        result.put ("subsequentTransactions", subsequentTransactions);
        result.put ("checkTime", LocalDateTime.now ());

        return result;
    }

    /**
     * 执行充值冲正
     *
     * @param recordId
     *            充值记录ID
     * @param reason
     *            冲正原因
     * @param operatorId
     *            操作员ID
     * @return 冲正结果
     */
    public Map<String, Object> executeRechargeReversal (Long recordId, String reason, Long operatorId) {
        Map<String, Object> result = new HashMap<> ();

        // 先检查是否可以冲正
        Map<String, Object> checkResult = checkRechargeReversibility (recordId);
        if (!(Boolean) checkResult.get ("canReverse")) {
            result.put ("success", false);
            result.put ("message", "充值记录无法冲正");
            result.put ("restrictions", checkResult.get ("restrictions"));
            return result;
        }

        @SuppressWarnings("unchecked")
        ConsumeRechargeEntity record = (ConsumeRechargeEntity) checkResult.get ("record");

        try {
            // 更新原记录状态
            record.setRechargeStatus (4); // 已冲正
            record.setReverseReason (reason);
            record.setReverseTime (LocalDateTime.now ());
            record.setUpdateUserId (operatorId);

            int updatedRows = consumeRechargeDao.updateById (record);
            if (updatedRows <= 0) {
                result.put ("success", false);
                result.put ("message", "更新原充值记录状态失败");
                return result;
            }

            // 创建冲正记录
            ConsumeRechargeEntity reversalRecord = new ConsumeRechargeEntity ();
            reversalRecord.setUserId (record.getUserId ());
            reversalRecord.setUserName (record.getUserName ());
            reversalRecord.setRechargeAmount (record.getRechargeAmount ().negate ());
            reversalRecord.setBeforeBalance (record.getAfterBalance ());
            reversalRecord.setAfterBalance (record.getBeforeBalance ());
            reversalRecord.setRechargeWay (record.getRechargeWay ());
            reversalRecord.setRechargeChannel (record.getRechargeChannel ());
            reversalRecord.setTransactionNo (generateTransactionNo (record.getUserId (), record.getRechargeWay ()));
            reversalRecord.setRechargeStatus (1); // 成功
            reversalRecord.setRechargeTime (LocalDateTime.now ());
            reversalRecord.setArrivalTime (LocalDateTime.now ());
            reversalRecord.setRechargeDescription ("充值冲正：" + reason);
            reversalRecord.setBatchNo (record.getBatchNo ());
            reversalRecord.setCreateUserId (operatorId);
            reversalRecord.setUpdateUserId (operatorId);

            int insertedRows = consumeRechargeDao.insert (reversalRecord);
            if (insertedRows <= 0) {
                result.put ("success", false);
                result.put ("message", "创建冲正记录失败");
                return result;
            }

            result.put ("success", true);
            result.put ("message", "充值冲正成功");
            result.put ("originalRecordId", recordId);
            result.put ("reversalRecordId", reversalRecord.getRecordId ());
            result.put ("reversalAmount", record.getRechargeAmount ());
            result.put ("reversalTime", LocalDateTime.now ());

            return result;

        } catch (Exception e) {
            log.error ("执行充值冲正失败: recordId={}, reason={}, error={}", recordId, reason, e.getMessage (), e);
            throw ConsumeRechargeException.databaseError ("执行充值冲正", e.getMessage ());
        }
    }

    /**
     * 获取用户充值排行
     *
     * @param startDate
     *            开始日期
     * @param endDate
     *            结束日期
     * @param limit
     *            限制数量
     * @return 排行数据
     */
    public List<Map<String, Object>> getUserRechargeRanking (LocalDateTime startDate, LocalDateTime endDate,
            Integer limit) {
        try {
            return consumeRechargeDao.getUserRechargeRanking (startDate, endDate, limit);
        } catch (Exception e) {
            log.error ("获取用户充值排行失败: startDate={}, endDate={}, limit={}, error={}", startDate, endDate, limit,
                    e.getMessage (), e);
            throw ConsumeRechargeException.databaseError ("获取用户充值排行", e.getMessage ());
        }
    }
}
