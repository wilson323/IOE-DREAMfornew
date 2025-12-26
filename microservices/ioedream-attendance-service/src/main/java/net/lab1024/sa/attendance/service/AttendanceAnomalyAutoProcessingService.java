package net.lab1024.sa.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.entity.attendance.AttendanceAnomalyEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceAnomalyAutoProcessQueryForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceAnomalyAutoProcessVO;
import net.lab1024.sa.common.domain.PageResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤异常自动处理服务接口
 * <p>
 * 提供考勤异常的智能自动处理功能，包括：
 * 1. 智能分类：根据异常类型、严重程度、历史记录等自动分类
 * 2. 自动处理：对低风险异常自动批准或修正
 * 3. 智能路由：将复杂异常路由到合适的处理人
 * 4. 批量处理：定时批量处理待处理的异常
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface AttendanceAnomalyAutoProcessingService {

    /**
     * 智能分类异常
     * <p>
     * 根据异常的多个维度进行智能分类：
     * - 异常类型（缺卡/迟到/早退/旷工）
     * - 严重程度（普通/严重/紧急）
     * - 异常时长（分钟数）
     * - 员工历史记录（出勤率、违规次数）
     * - 时间因素（工作日/周末、工作时间/非工作时间）
     * </p>
     *
     * @param anomaly 异常记录
     * @return 分类结果（AUTO_APPROVE-自动批准, MANUAL_REVIEW-人工审核, AUTO_REJECT-自动拒绝, ESCALATE-升级处理）
     */
    String categorizeAnomaly(AttendanceAnomalyEntity anomaly);

    /**
     * 自动处理单个异常
     * <p>
     * 根据分类结果自动处理异常：
     * - AUTO_APPROVE: 自动批准并修正考勤记录
     * - MANUAL_REVIEW: 标记为待人工审核，路由到合适的管理员
     * - AUTO_REJECT: 自动拒绝（不符合申诉条件）
     * - ESCALATE: 升级到高级管理员或HR部门
     * </p>
     *
     * @param anomalyId 异常ID
     * @return 是否处理成功
     */
    Boolean autoProcessAnomaly(Long anomalyId);

    /**
     * 批量自动处理异常
     * <p>
     * 定时任务调用，批量处理待处理的异常
     * </p>
     *
     * @param attendanceDate 考勤日期
     * @return 处理的异常数量
     */
    Integer batchAutoProcess(LocalDate attendanceDate);

    /**
     * 分页查询自动处理记录
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<AttendanceAnomalyAutoProcessVO> queryAutoProcessPage(AttendanceAnomalyAutoProcessQueryForm queryForm);

    /**
     * 获取自动处理规则配置
     *
     * @param anomalyType 异常类型（可选）
     * @return 规则配置列表
     */
    List<Map<String, Object>> getAutoProcessingRules(String anomalyType);

    /**
     * 更新自动处理规则
     *
     * @param ruleId       规则ID
     * @param ruleConfig   规则配置（JSON格式）
     * @param enabled      是否启用
     * @return 是否更新成功
     */
    Boolean updateAutoProcessingRule(Long ruleId, String ruleConfig, Boolean enabled);

    /**
     * 获取异常的自动处理建议
     * <p>
     * 为管理员提供自动处理的建议和理由
     * </p>
     *
     * @param anomalyId 异常ID
     * @return 建议信息（包含处理动作、理由、置信度）
     */
    Map<String, Object> getAutoProcessSuggestion(Long anomalyId);

    /**
     * 手动覆盖自动处理决定
     * <p>
     * 管理员可以手动修改自动处理的决定
     * </p>
     *
     * @param anomalyId    异常ID
     * @param newDecision  新的决定（APPROVE/REJECT/ESCALATE）
     * @param comment      处理意见
     * @return 是否覆盖成功
     */
    Boolean overrideAutoProcessDecision(Long anomalyId, String newDecision, String comment);

    /**
     * 统计自动处理效果
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据（处理率、准确率、节省时间等）
     */
    Map<String, Object> statisticsAutoProcessEffect(LocalDate startDate, LocalDate endDate);

    /**
     * 获取员工的异常历史模式
     * <p>
     * 分析员工的历史异常记录，识别模式：
     * - 违规频率
     * - 常见异常类型
     * - 改进趋势
     * - 风险等级
     * </p>
     *
     * @param userId 用户ID
     * @param months 统计月数
     * @return 历史模式分析结果
     */
    Map<String, Object> analyzeEmployeeAnomalyPattern(Long userId, Integer months);
}
