package net.lab1024.sa.consume.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.consume.entity.ConsumeSubsidyIssueRecordEntity;

/**
 * 消费补贴发放记录DAO接口
 * <p>
 * 提供补贴发放记录数据的访问操作
 * 支持发放历史查询、统计、审计等功能
 * 严格遵循企业级财务管理要求，确保数据安全和会计可追溯
 *
 * @author IOE-DREAM Team
 * @since 2025-12-02
 * @version 1.0.0
 */
@Mapper
public interface ConsumeSubsidyIssueRecordDao extends BaseMapper<ConsumeSubsidyIssueRecordEntity> {

        /**
         * 根据补贴账户ID查询发放记录
         *
         * @param subsidyAccountId 补贴账户ID
         * @return 发放记录列表
         */
        @Transactional(readOnly = true)
        default List<ConsumeSubsidyIssueRecordEntity> selectBySubsidyAccountId(String subsidyAccountId) {
                LambdaQueryWrapper<ConsumeSubsidyIssueRecordEntity> wrapper = new LambdaQueryWrapper<>();
                // 类型转换：String转Long（如果subsidyAccountId是数字字符串）
                Long subsidyAccountIdLong = null;
                if (subsidyAccountId != null && !subsidyAccountId.isEmpty()) {
                        try {
                                subsidyAccountIdLong = Long.parseLong(subsidyAccountId);
                        } catch (NumberFormatException e) {
                                // 如果不是数字，可能需要使用其他字段查询
                                // DAO层不记录日志，由Service层处理
                                throw new IllegalArgumentException("补贴账户ID格式无效: " + subsidyAccountId, e);
                        }
                }
                // 使用subsidyAccountId字段查询（Long类型）
                wrapper.eq(subsidyAccountIdLong != null, ConsumeSubsidyIssueRecordEntity::getSubsidyAccountId,
                                subsidyAccountIdLong)
                                .eq(ConsumeSubsidyIssueRecordEntity::getDeletedFlag, 0)
                                .orderByDesc(ConsumeSubsidyIssueRecordEntity::getIssueTime);
                return selectList(wrapper);
        }

        /**
         * 根据用户ID查询发放记录
         *
         * @param userId    用户ID
         * @param startTime 开始时间
         * @param endTime   结束时间
         * @return 发放记录列表
         */
        @Transactional(readOnly = true)
        @Select("SELECT * FROM consume_subsidy_issue_record " +
                        "WHERE user_id = #{userId} " +
                        "AND deleted = FALSE " +
                        "AND (#{startTime} IS NULL OR issue_time >= #{startTime}) " +
                        "AND (#{endTime} IS NULL OR issue_time <= #{endTime}) " +
                        "ORDER BY issue_time DESC")
        List<ConsumeSubsidyIssueRecordEntity> selectByUserIdAndTimeRange(
                        @Param("userId") String userId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * 查询今日发放记录
         *
         * @param subsidyAccountId 补贴账户ID（可选）
         * @return 今日发放记录列表
         */
        @Transactional(readOnly = true)
        @Select("SELECT * FROM consume_subsidy_issue_record " +
                        "WHERE DATE(issue_time) = CURDATE() " +
                        "AND deleted = FALSE " +
                        "AND (#{subsidyAccountId} IS NULL OR subsidy_account_id = #{subsidyAccountId}) " +
                        "ORDER BY issue_time DESC")
        List<ConsumeSubsidyIssueRecordEntity> selectTodayRecords(
                        @Param("subsidyAccountId") String subsidyAccountId);

        /**
         * 查询指定日期范围内的发放记录
         *
         * @param startTime 开始时间
         * @param endTime   结束时间
         * @return 发放记录列表
         */
        @Transactional(readOnly = true)
        @Select("SELECT * FROM consume_subsidy_issue_record " +
                        "WHERE issue_time >= #{startTime} AND issue_time <= #{endTime} " +
                        "AND deleted = FALSE " +
                        "ORDER BY issue_time DESC")
        List<ConsumeSubsidyIssueRecordEntity> selectByTimeRange(
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * 统计指定账户的今日发放总额
         *
         * @param subsidyAccountId 补贴账户ID
         * @return 今日发放总额
         */
        @Transactional(readOnly = true)
        @Select("SELECT COALESCE(SUM(issue_amount), 0) " +
                        "FROM consume_subsidy_issue_record " +
                        "WHERE subsidy_account_id = #{subsidyAccountId} " +
                        "AND DATE(issue_time) = CURDATE() " +
                        "AND deleted = FALSE " +
                        "AND issue_status = 2")
        java.math.BigDecimal sumTodayIssueAmount(@Param("subsidyAccountId") String subsidyAccountId);
}
