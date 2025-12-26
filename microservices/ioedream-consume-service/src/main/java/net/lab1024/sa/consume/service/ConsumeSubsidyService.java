package net.lab1024.sa.consume.service;

import net.lab1024.sa.common.domain.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.consume.domain.form.ConsumeSubsidyAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeSubsidyQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeSubsidyUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeSubsidyVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSubsidyStatisticsVO;

import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消费补贴服务接口
 * <p>
 * 完整的企业级实现，包含：
 * - 补贴信息管理
 * - 补贴发放与使用
 * - 补贴统计分析
 * - 补贴审核流程
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Tag(name = "消费补贴服务", description = "消费补贴相关业务接口")
public interface ConsumeSubsidyService {

    // ==================== 补贴信息管理 ====================

    /**
     * 分页查询补贴列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询补贴列表", description = "根据查询条件分页查询补贴信息")
    PageResult<ConsumeSubsidyVO> querySubsidyPage(ConsumeSubsidyQueryForm queryForm);

    /**
     * 获取补贴详情
     *
     * @param subsidyId 补贴ID
     * @return 补贴详情
     */
    @Operation(summary = "获取补贴详情", description = "根据ID获取补贴详细信息")
    ConsumeSubsidyVO getSubsidyById(Long subsidyId);

    /**
     * 创建补贴
     *
     * @param addForm 补贴信息
     * @return 创建结果
     */
    @Operation(summary = "创建补贴", description = "创建新的补贴记录")
    Long addSubsidy(ConsumeSubsidyAddForm addForm);

    /**
     * 发放补贴给指定用户
     *
     * @param subsidyId 补贴ID
     * @param userIds 用户ID列表
     */
    @Operation(summary = "发放补贴", description = "向指定用户发放补贴")
    void distributeSubsidy(Long subsidyId, List<Long> userIds);

    /**
     * 批量发放补贴
     *
     * @param addForm 补贴新增表单
     * @param userIds 用户ID列表
     */
    @Operation(summary = "批量发放补贴", description = "创建补贴并批量发放给指定用户")
    void batchDistributeSubsidy(ConsumeSubsidyAddForm addForm, List<Long> userIds);

    /**
     * 停用补贴
     *
     * @param subsidyId 补贴ID
     */
    @Operation(summary = "停用补贴", description = "停用指定的消费补贴")
    void disableSubsidy(Long subsidyId);

    /**
     * 启用补贴
     *
     * @param subsidyId 补贴ID
     */
    @Operation(summary = "启用补贴", description = "启用指定的消费补贴")
    void enableSubsidy(Long subsidyId);

    /**
     * 获取补贴统计信息
     *
     * @param userId 用户ID（可选）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    @Operation(summary = "获取补贴统计信息", description = "获取消费补贴的统计信息")
    ConsumeSubsidyStatisticsVO getStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 导出补贴记录
     *
     * @param queryForm 查询条件
     * @return 导出结果
     */
    @Operation(summary = "导出补贴记录", description = "导出补贴记录到Excel文件")
    String exportSubsidies(ConsumeSubsidyQueryForm queryForm);

    /**
     * 获取补贴使用记录
     *
     * @param subsidyId 补贴ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 使用记录
     */
    @Operation(summary = "获取补贴使用记录", description = "获取指定补贴的使用记录")
    PageResult<Map<String, Object>> getSubsidyUsage(Long subsidyId, Integer pageNum, Integer pageSize);

    /**
     * 手动调整补贴金额
     *
     * @param subsidyId 补贴ID
     * @param adjustmentAmount 调整金额
     * @param reason 调整原因
     */
    @Operation(summary = "调整补贴金额", description = "手动调整指定补贴的金额")
    void adjustSubsidyAmount(Long subsidyId, BigDecimal adjustmentAmount, String reason);

    /**
     * 获取待发放补贴列表
     *
     * @return 待发放补贴列表
     */
    @Operation(summary = "获取待发放补贴", description = "获取待发放的消费补贴列表")
    List<ConsumeSubsidyVO> getPendingSubsidies();

    /**
     * 批量操作补贴
     *
     * @param subsidyIds 补贴ID列表
     * @param operation 操作类型
     */
    @Operation(summary = "批量操作补贴", description = "对多个补贴执行批量操作")
    void batchOperateSubsidies(List<Long> subsidyIds, String operation);

    /**
     * 更新补贴
     *
     * @param updateForm 补贴信息
     * @return 更新结果
     */
    @Operation(summary = "更新补贴", description = "更新补贴信息")
    void updateSubsidy(ConsumeSubsidyUpdateForm updateForm);

    /**
     * 删除补贴
     *
     * @param subsidyId 补贴ID
     * @return 删除结果
     */
    @Operation(summary = "删除补贴", description = "删除补贴记录")
    void deleteSubsidy(Long subsidyId);

    /**
     * 批量删除补贴
     *
     * @param subsidyIds 补贴ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除补贴", description = "批量删除补贴记录")
    void batchDeleteSubsidy(List<Long> subsidyIds);

    // ==================== 补贴发放与使用 ====================

    /**
     * 发放补贴
     *
     * @param subsidyId 补贴ID
     * @return 发放结果
     */
    @Operation(summary = "发放补贴", description = "向用户发放补贴")
    void issueSubsidy(Long subsidyId);

    /**
     * 批量发放补贴
     *
     * @param subsidyIds 补贴ID列表
     * @return 发放结果
     */
    @Operation(summary = "批量发放补贴", description = "批量向用户发放补贴")
    Map<String, Object> batchIssueSubsidy(List<Long> subsidyIds);

    /**
     * 使用补贴
     *
     * @param subsidyId 补贴ID
     * @param amount    使用金额
     * @return 使用结果
     */
    @Operation(summary = "使用补贴", description = "使用补贴进行消费")
    Map<String, Object> useSubsidy(Long subsidyId, BigDecimal amount);

    /**
     * 作废补贴
     *
     * @param subsidyId 补贴ID
     * @return 作废结果
     */
    @Operation(summary = "作废补贴", description = "作废补贴")
    void cancelSubsidy(Long subsidyId);

    /**
     * 延期补贴
     *
     * @param subsidyId 补贴ID
     * @param days      延期天数
     * @return 延期结果
     */
    @Operation(summary = "延期补贴", description = "延长补贴有效期")
    void extendSubsidy(Long subsidyId, Integer days);

    // ==================== 补贴查询 ====================

    /**
     * 获取用户补贴列表
     *
     * @param userId 用户ID
     * @return 补贴列表
     */
    @Operation(summary = "获取用户补贴列表", description = "获取指定用户的补贴列表")
    List<ConsumeSubsidyVO> getUserSubsidies(Long userId);

    /**
     * 获取可用补贴列表
     *
     * @param userId 用户ID
     * @return 可用补贴列表
     */
    @Operation(summary = "获取可用补贴列表", description = "获取用户可用的补贴列表")
    List<ConsumeSubsidyVO> getAvailableSubsidies(Long userId);

    /**
     * 获取用户可用补贴（别名方法，兼容性）
     *
     * @param userId 用户ID
     * @return 可用补贴列表
     */
    @Operation(summary = "获取用户可用补贴", description = "获取用户当前可用的消费补贴")
    default List<ConsumeSubsidyVO> getUserAvailableSubsidies(Long userId) {
        return getAvailableSubsidies(userId);
    }

    /**
     * 获取即将过期的补贴
     *
     * @param days 预警天数
     * @return 即将过期的补贴列表
     */
    @Operation(summary = "获取即将过期的补贴", description = "获取指定天数内即将过期的补贴")
    List<ConsumeSubsidyVO> getExpiringSoonSubsidies(Integer days);

    /**
     * 获取即将用完的补贴
     *
     * @param usageRate 使用率阈值
     * @return 即将用完的补贴列表
     */
    @Operation(summary = "获取即将用完的补贴", description = "获取使用率超过阈值的补贴")
    List<ConsumeSubsidyVO> getNearlyDepletedSubsidies(BigDecimal usageRate);

    // ==================== 补贴统计分析 ====================

    /**
     * 获取补贴统计信息
     *
     * @return 统计信息
     */
    @Operation(summary = "获取补贴统计信息", description = "获取补贴的统计汇总信息")
    ConsumeSubsidyStatisticsVO getSubsidyStatistics();

    /**
     * 获取补贴使用分析
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 使用分析结果
     */
    @Operation(summary = "获取补贴使用分析", description = "获取指定时间段内的补贴使用分析")
    Map<String, Object> getSubsidyUsageAnalysis(String startDate, String endDate);

    /**
     * 获取补贴余额分析
     *
     * @return 余额分析结果
     */
    @Operation(summary = "获取补贴余额分析", description = "获取当前补贴余额分布情况")
    Map<String, Object> getSubsidyBalanceAnalysis();

    /**
     * 获取用户补贴统计
     *
     * @param userId 用户ID
     * @return 用户补贴统计
     */
    @Operation(summary = "获取用户补贴统计", description = "获取指定用户的补贴统计信息")
    Map<String, Object> getUserSubsidyStatistics(Long userId);

    // ==================== 补贴审核管理 ====================

    /**
     * 获取待审核补贴（复用待发放方法）
     *
     * @return 待审核补贴列表
     */
    @Operation(summary = "获取待审核补贴", description = "获取待审核的补贴列表")
    default List<ConsumeSubsidyVO> getPendingSubsidiesForAudit() {
        return getPendingSubsidies();
    }

    /**
     * 审核通过补贴
     *
     * @param subsidyId 补贴ID
     * @return 审核结果
     */
    @Operation(summary = "审核通过补贴", description = "审核通过补贴申请")
    void approveSubsidy(Long subsidyId);

    /**
     * 审核驳回补贴
     *
     * @param subsidyId 补贴ID
     * @param remark    驳回原因
     * @return 审核结果
     */
    @Operation(summary = "审核驳回补贴", description = "审核驳回补贴申请")
    void rejectSubsidy(Long subsidyId, String remark);

    /**
     * 批量审核补贴
     *
     * @param subsidyIds 补贴ID列表
     * @param approved   是否通过
     * @param remark     审核备注
     * @return 审核结果
     */
    @Operation(summary = "批量审核补贴", description = "批量审核补贴申请")
    Map<String, Object> batchAuditSubsidy(List<Long> subsidyIds, Boolean approved, String remark);

    // ==================== 补贴数据导入导出 ====================

    /**
     * 导出补贴数据
     *
     * @param queryForm 查询条件
     * @param response  HTTP响应
     */
    @Operation(summary = "导出补贴数据", description = "导出补贴数据到Excel文件")
    void exportSubsidyData(ConsumeSubsidyQueryForm queryForm, HttpServletResponse response);

    /**
     * 获取补贴报表
     *
     * @param reportType 报表类型
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 报表数据
     */
    @Operation(summary = "获取补贴报表", description = "生成补贴相关报表")
    Map<String, Object> getSubsidyReport(String reportType, String startDate, String endDate);

    // ==================== 补贴配置管理 ====================

    /**
     * 获取补贴类型列表
     *
     * @return 补贴类型列表
     */
    @Operation(summary = "获取补贴类型列表", description = "获取所有可用的补贴类型")
    List<Map<String, Object>> getSubsidyTypes();

    /**
     * 获取补贴周期列表
     *
     * @return 补贴周期列表
     */
    @Operation(summary = "获取补贴周期列表", description = "获取所有可用的补贴周期")
    List<Map<String, Object>> getSubsidyPeriods();

    /**
     * 获取补贴状态列表
     *
     * @return 补贴状态列表
     */
    @Operation(summary = "获取补贴状态列表", description = "获取所有补贴状态")
    List<Map<String, Object>> getSubsidyStatuses();

    /**
     * 验证补贴配置
     *
     * @param addForm 补贴配置
     * @return 验证结果
     */
    @Operation(summary = "验证补贴配置", description = "验证补贴配置的合法性")
    Map<String, Object> validateSubsidyConfig(ConsumeSubsidyAddForm addForm);
}