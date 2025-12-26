package net.lab1024.sa.visitor.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.visitor.entity.SelfServiceRegistrationEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 自助登记终端服务接口
 * <p>
 * 提供访客自助登记的完整业务功能
 * 严格遵循CLAUDE.md全局架构规范和Service设计标准
 * </p>
 * <p>
 * <strong>核心功能：</strong>
 * </p>
 * <ul>
 *   <li>自助登记创建</li>
 *   <li>身份证识别</li>
 *   <li>人脸采集</li>
 *   <li>访客码生成</li>
 *   <li>访客卡管理</li>
 *   <li>审批管理</li>
 *   <li>签到签离</li>
 *   <li>统计查询</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface SelfServiceRegistrationService {

    /**
     * 创建自助登记
     * <p>
     * 访客在自助终端填写信息并提交登记申请
     * </p>
     *
     * @param registration 登记信息
     * @return 创建后的登记记录
     */
    SelfServiceRegistrationEntity createRegistration(SelfServiceRegistrationEntity registration);

    /**
     * 根据访客码查询登记记录
     *
     * @param visitorCode 访客码
     * @return 登记记录
     */
    SelfServiceRegistrationEntity getRegistrationByVisitorCode(String visitorCode);

    /**
     * 根据登记编号查询记录
     *
     * @param registrationCode 登记编号
     * @return 登记记录
     */
    SelfServiceRegistrationEntity getRegistrationByCode(String registrationCode);

    /**
     * 查询待审批的登记记录
     *
     * @param intervieweeId 被访人ID（可选）
     * @param limit 限制数量
     * @return 待审批记录列表
     */
    List<SelfServiceRegistrationEntity> getPendingApprovals(Long intervieweeId, Integer limit);

    /**
     * 审批登记申请
     * <p>
     * 被访人或管理员审批访客登记申请
     * </p>
     *
     * @param registrationId 登记ID
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param approved 是否批准
     * @param approvalComment 审批意见
     * @return 审批后的记录
     */
    SelfServiceRegistrationEntity approveRegistration(Long registrationId,
                                                     Long approverId,
                                                     String approverName,
                                                     Boolean approved,
                                                     String approvalComment);

    /**
     * 访客签到
     * <p>
     * 访客使用访客码或访客卡在自助终端签到
     * </p>
     *
     * @param visitorCode 访客码
     * @return 签到后的记录
     */
    SelfServiceRegistrationEntity checkIn(String visitorCode);

    /**
     * 访客签离
     * <p>
     * 访客在自助终端签离，归还访客卡
     * </p>
     *
     * @param visitorCode 访客码
     * @return 签离后的记录
     */
    SelfServiceRegistrationEntity checkOut(String visitorCode);

    /**
     * 查询在场访客
     * <p>
     * 查询已签到但未签离的访客
     * </p>
     *
     * @return 在场访客列表
     */
    List<SelfServiceRegistrationEntity> getActiveVisitors();

    /**
     * 查询超时未签离访客
     *
     * @return 超时访客列表
     */
    List<SelfServiceRegistrationEntity> getOverdueVisitors();

    /**
     * 分页查询登记记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param visitorName 访客姓名（可选）
     * @param phone 手机号（可选）
     * @param status 登记状态（可选）
     * @param visitDateStart 访问日期开始（可选）
     * @param visitDateEnd 访问日期结束（可选）
     * @return 分页结果
     */
    PageResult<SelfServiceRegistrationEntity> queryPage(Integer pageNum,
                                                        Integer pageSize,
                                                        String visitorName,
                                                        String phone,
                                                        Integer status,
                                                        LocalDate visitDateStart,
                                                        LocalDate visitDateEnd);

    /**
     * 查询指定被访人的登记记录
     *
     * @param intervieweeId 被访人ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 登记记录列表
     */
    List<SelfServiceRegistrationEntity> getByInterviewee(Long intervieweeId,
                                                         LocalDate startDate,
                                                         LocalDate endDate);

    /**
     * 查询指定终端的登记记录
     *
     * @param terminalId 终端ID
     * @param limit 限制数量
     * @return 登记记录列表
     */
    List<SelfServiceRegistrationEntity> getByTerminal(String terminalId, Integer limit);

    /**
     * 获取自助登记统计信息
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    Map<String, Object> getStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 取消登记
     * <p>
     * 取消未签到或未审批的登记申请
     * </p>
     *
     * @param registrationId 登记ID
     * @param cancelReason 取消原因
     * @return 取消后的记录
     */
    SelfServiceRegistrationEntity cancelRegistration(Long registrationId, String cancelReason);

    /**
     * 更新访客卡打印状态
     *
     * @param registrationId 登记ID
     * @param printStatus 打印状态
     * @param visitorCard 访客卡号
     * @return 更新后的记录
     */
    SelfServiceRegistrationEntity updateCardPrintStatus(Long registrationId,
                                                        Integer printStatus,
                                                        String visitorCard);

    /**
     * 批量审批
     * <p>
     * 批量审批多个登记申请
     * </p>
     *
     * @param registrationIds 登记ID列表
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param approved 是否批准
     * @param approvalComment 审批意见
     * @return 审批结果
     */
    Map<String, Object> batchApprove(List<Long> registrationIds,
                                     Long approverId,
                                     String approverName,
                                     Boolean approved,
                                     String approvalComment);
}
