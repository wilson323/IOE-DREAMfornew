package net.lab1024.sa.visitor.manager;

import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.visitor.domain.entity.VisitorEntity;
import net.lab1024.sa.visitor.domain.vo.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客管理器接口
 * 负责复杂的业务逻辑编排和外部服务调用
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
public interface VisitorManager {

    /**
     * 完整的访客预约流程
     * 包含验证、创建预约、发送通知等
     *
     * @param appointmentVO 预约信息
     * @return 预约结果
     */
    ResponseDTO<Long> completeAppointmentProcess(VisitorAppointmentVO appointmentVO);

    /**
     * 访客审批工作流
     * 包含权限验证、状态流转、通知发送等
     *
     * @param visitorId 访客ID
     * @param approvalResult 审批结果
     * @param approverId 审批人ID
     * @param comment 审批意见
     * @return 审批结果
     */
    ResponseDTO<Void> processApprovalWorkflow(Long visitorId, Boolean approvalResult, Long approverId, String comment);

    /**
     * 访客签到验证流程
     * 包含身份验证、权限检查、记录创建等
     *
     * @param visitorId 访客ID
     * @param verificationMethod 验证方式
     * @param verificationData 验证数据
     * @return 签到结果
     */
    ResponseDTO<Boolean> processCheckinWorkflow(Long visitorId, String verificationMethod, String verificationData);

    /**
     * 访客黑名单检查
     * 检查访客是否在黑名单中
     *
     * @param visitor 访客信息
     * @return 检查结果
     */
    ResponseDTO<Boolean> checkBlacklist(VisitorEntity visitor);

    /**
     * 发送访客通知
     * 包含审批结果通知、访问提醒等
     *
     * @param visitorId 访客ID
     * @param notificationType 通知类型
     * @param message 通知内容
     * @return 发送结果
     */
    ResponseDTO<Void> sendVisitorNotification(Long visitorId, String notificationType, String message);

    /**
     * 访客数据同步
     * 同步访客数据到外部系统
     *
     * @param visitorId 访客ID
     * @return 同步结果
     */
    ResponseDTO<Void> syncVisitorData(Long visitorId);

    /**
     * 批量处理即将到期的访客
     * 自动处理即将到期的访客预约
     *
     * @return 处理结果
     */
    ResponseDTO<Integer> batchProcessExpiringVisitors();

    /**
     * 生成访客报告
     * 生成访客访问报告和统计分析
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param reportType 报告类型
     * @return 报告路径
     */
    ResponseDTO<String> generateVisitorReport(LocalDateTime startTime, LocalDateTime endTime, String reportType);

    /**
     * 访客权限验证
     * 验证访客是否有权限访问指定区域
     *
     * @param visitorId 访客ID
     * @param areaId 区域ID
     * @return 验证结果
     */
    ResponseDTO<Boolean> validateVisitorAccess(Long visitorId, Long areaId);

    /**
     * 更新访客访问统计
     * 更新访客的访问次数、最后访问时间等统计信息
     *
     * @param visitorId 访客ID
     * @return 更新结果
     */
    ResponseDTO<Void> updateVisitorStatistics(Long visitorId);

    /**
     * 访客满意度调查
     * 发送和处理访客满意度调查
     *
     * @param visitorId 访客ID
     * @param surveyType 调查类型
     * @return 发送结果
     */
    ResponseDTO<Void> processSatisfactionSurvey(Long visitorId, String surveyType);

    /**
     * 访客异常行为检测
     * 检测访客的异常行为模式
     *
     * @param visitorId 访客ID
     * @return 检测结果
     */
    ResponseDTO<String> detectAbnormalBehavior(Long visitorId);
}