package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.attendance.entity.RuleTestHistoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则测试历史DAO接口
 * <p>
 * 提供规则测试历史数据访问操作
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Mapper
public interface RuleTestHistoryDao extends BaseMapper<RuleTestHistoryEntity> {

    /**
     * 查询规则测试历史列表
     *
     * @param ruleId 规则ID（可选）
     * @param testUserId 测试用户ID（可选）
     * @param testResult 测试结果（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 测试历史列表
     */
    List<RuleTestHistoryEntity> queryHistoryList(
            @Param("ruleId") Long ruleId,
            @Param("testUserId") Long testUserId,
            @Param("testResult") String testResult,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计规则测试次数
     *
     * @param ruleId 规则ID
     * @return 测试次数
     */
    Integer countByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 查询最近的测试历史
     *
     * @param limit 限制数量
     * @return 测试历史列表
     */
    List<RuleTestHistoryEntity> queryRecentHistory(@Param("limit") Integer limit);

    /**
     * 删除过期的测试历史
     *
     * @param beforeTime 删除此时间之前的历史
     * @return 删除数量
     */
    Integer deleteExpiredHistory(@Param("beforeTime") LocalDateTime beforeTime);
}
