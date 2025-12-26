package net.lab1024.sa.consume.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeSubsidyQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeSubsidyVO;
import net.lab1024.sa.common.entity.consume.ConsumeSubsidyEntity;

/**
 * 消费补贴数据访问对象
 * <p>
 * 遵循MyBatis-Plus规范，使用@Mapper注解而非@Repository
 * 提供补贴的完整数据访问操作
 * 支持余额管理、使用记录统计和过期处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Mapper
public interface ConsumeSubsidyDao extends BaseMapper<ConsumeSubsidyEntity> {

    /**
     * 分页查询补贴
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<ConsumeSubsidyVO> queryPage(@Param("queryForm") ConsumeSubsidyQueryForm queryForm);

    /**
     * 根据补贴ID查询详细信息
     *
     * @param subsidyId 补贴ID
     * @return 补贴详情
     */
    ConsumeSubsidyVO selectDetailById(@Param("subsidyId") Long subsidyId);

    /**
     * 根据补贴编码查询补贴
     *
     * @param subsidyCode 补贴编码
     * @return 补贴信息
     */
    ConsumeSubsidyEntity selectByCode(@Param("subsidyCode") String subsidyCode);

    /**
     * 查询用户的有效补贴列表
     *
     * @param userId 用户ID
     * @return 有效补贴列表
     */
    List<ConsumeSubsidyVO> selectValidByUserId(@Param("userId") Long userId);

    /**
     * 查询用户可用补贴（可使用的补贴）
     *
     * @param userId 用户ID
     * @return 可用补贴列表
     */
    List<ConsumeSubsidyVO> selectUsableByUserId(@Param("userId") Long userId);

    /**
     * 根据补贴类型查询补贴
     *
     * @param subsidyType 补贴类型
     * @param userId 用户ID（可选）
     * @return 补贴列表
     */
    List<ConsumeSubsidyVO> selectByType(@Param("subsidyType") Integer subsidyType, @Param("userId") Long userId);

    /**
     * 查询即将过期的补贴
     *
     * @param days 天数
     * @param userId 用户ID（可选）
     * @return 即将过期补贴列表
     */
    List<ConsumeSubsidyVO> selectExpiringSoon(@Param("days") Integer days, @Param("userId") Long userId);

    /**
     * 查询已过期的补贴
     *
     * @param beforeDate 日期（可选）
     * @param userId 用户ID（可选）
     * @return 已过期补贴列表
     */
    List<ConsumeSubsidyVO> selectExpired(@Param("beforeDate") LocalDateTime beforeDate, @Param("userId") Long userId);

    /**
     * 查询即将用完的补贴
     *
     * @param percentage 使用比例阈值（如0.8表示80%）
     * @param userId 用户ID（可选）
     * @return 即将用完补贴列表
     */
    List<ConsumeSubsidyVO> selectNearlyDepleted(@Param("percentage") BigDecimal percentage, @Param("userId") Long userId);

    /**
     * 检查补贴编码是否存在
     *
     * @param subsidyCode 补贴编码
     * @param excludeId 排除的补贴ID（用于更新时检查）
     * @return 存在数量
     */
    int countByCode(@Param("subsidyCode") String subsidyCode, @Param("excludeId") Long excludeId);

    /**
     * 更新补贴使用金额
     *
     * @param subsidyId 补贴ID
     * @param amount 使用金额
     * @param dailyAmount 当日使用金额
     * @return 更新行数
     */
    int updateUsedAmount(@Param("subsidyId") Long subsidyId,
                        @Param("amount") BigDecimal amount,
                        @Param("dailyAmount") BigDecimal dailyAmount);

    /**
     * 更新补贴状态
     *
     * @param subsidyId 补贴ID
     * @param status 新状态
     * @return 更新行数
     */
    int updateStatus(@Param("subsidyId") Long subsidyId, @Param("status") Integer status);

    /**
     * 批量更新补贴状态
     *
     * @param subsidyIds 补贴ID列表
     * @param status 新状态
     * @return 更新行数
     */
    int batchUpdateStatus(@Param("subsidyIds") java.util.List<Long> subsidyIds, @Param("status") Integer status);

    /**
     * 重置每日使用金额（每日执行）
     *
     * @param date 日期
     * @return 重置行数
     */
    int resetDailyUsedAmount(@Param("date") LocalDateTime date);

    /**
     * 自动过期补贴处理
     *
     * @param currentTime 当前时间
     * @return 过期行数
     */
    int autoExpireSubsidies(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询补贴统计信息
     *
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param userId 用户ID（可选）
     * @param departmentId 部门ID（可选）
     * @return 统计信息
     */
    Map<String, Object> getSubsidyStatistics(@Param("startDate") String startDate,
                                              @Param("endDate") String endDate,
                                              @Param("userId") Long userId,
                                              @Param("departmentId") Long departmentId);

    /**
     * 按补贴类型统计
     *
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 类型统计
     */
    List<Map<String, Object>> countBySubsidyType(@Param("startDate") String startDate,
                                                 @Param("endDate") String endDate);

    /**
     * 按部门统计补贴
     *
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 部门统计
     */
    List<Map<String, Object>> countByDepartment(@Param("startDate") String startDate,
                                                @Param("endDate") String endDate);

    /**
     * 按补贴周期统计
     *
     * @return 周期统计
     */
    List<Map<String, Object>> countByPeriod();

    /**
     * 查询用户补贴汇总
     *
     * @param userId 用户ID
     * @param includeExpired 是否包含已过期
     * @return 用户补贴汇总
     */
    Map<String, Object> getUserSubsidySummary(@Param("userId") Long userId,
                                             @Param("includeExpired") Boolean includeExpired);

    /**
     * 查询补贴使用记录
     *
     * @param subsidyId 补贴ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 使用记录数量
     */
    Long countUsageRecords(@Param("subsidyId") Long subsidyId,
                          @Param("startDate") String startDate,
                          @Param("endDate") String endDate);

    /**
     * 查询补贴总使用金额
     *
     * @param subsidyId 补贴ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 总使用金额
     */
    BigDecimal sumUsedAmount(@Param("subsidyId") Long subsidyId,
                           @Param("startDate") String startDate,
                           @Param("endDate") String endDate);

    /**
     * 检查补贴是否存在冲突（同一用户同类型同周期）
     *
     * @param userId 用户ID
     * @param subsidyType 补贴类型
     * @param subsidyPeriod 补贴周期
     * @param effectiveDate 生效日期
     * @param excludeId 排除的补贴ID
     * @return 冲突数量
     */
    int countConflictingSubsidies(@Param("userId") Long userId,
                                 @Param("subsidyType") Integer subsidyType,
                                 @Param("subsidyPeriod") Integer subsidyPeriod,
                                 @Param("effectiveDate") LocalDateTime effectiveDate,
                                 @Param("excludeId") Long excludeId);

    /**
     * 查询自动续期补贴
     *
     * @param currentTime 当前时间
     * @return 需要续期的补贴列表
     */
    List<ConsumeSubsidyEntity> selectAutoRenewSubsidies(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询待审批补贴
     *
     * @param approverId 审批人ID（可选）
     * @return 待审批补贴列表
     */
    List<ConsumeSubsidyVO> selectPendingApproval(@Param("approverId") Long approverId);

    /**
     * 按使用限制统计
     *
     * @return 使用限制统计
     */
    List<Map<String, Object>> countByUsageLimit();

    /**
     * 查询近期的补贴发放记录
     *
     * @param days 天数
     * @param limit 限制数量
     * @return 发放记录列表
     */
    List<ConsumeSubsidyVO> selectRecentIssued(@Param("days") Integer days, @Param("limit") Integer limit);

    /**
     * 验证补贴是否可删除
     *
     * @param subsidyId 补贴ID
     * @return 关联记录数量
     */
    Map<String, Long> countRelatedRecords(@Param("subsidyId") Long subsidyId);

    /**
     * 查询补贴余额分布
     *
     * @param userId 用户ID（可选）
     * @return 余额分布统计
     */
    List<Map<String, Object>> getBalanceDistribution(@Param("userId") Long userId);

    /**
     * 更新补贴发放信息
     *
     * @param subsidyId 补贴ID
     * @param issueDate 发放日期
     * @param status 状态
     * @param issuerId 发放人ID
     * @param issuerName 发放人姓名
     * @return 更新行数
     */
    int updateIssueInfo(@Param("subsidyId") Long subsidyId,
                       @Param("issueDate") LocalDateTime issueDate,
                       @Param("status") Integer status,
                       @Param("issuerId") Long issuerId,
                       @Param("issuerName") String issuerName);

    /**
     * 批量发放补贴
     *
     * @param subsidyIds 补贴ID列表
     * @param issueDate 发放日期
     * @param issuerId 发放人ID
     * @param issuerName 发放人姓名
     * @return 发放行数
     */
    int batchIssueSubsidies(@Param("subsidyIds") java.util.List<Long> subsidyIds,
                           @Param("issueDate") LocalDateTime issueDate,
                           @Param("issuerId") Long issuerId,
                           @Param("issuerName") String issuerName);
}