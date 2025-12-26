package net.lab1024.sa.visitor.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.entity.visitor.SelfCheckOutEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 自助签离服务接口
 * <p>
 * 提供访客自助签离的完整业务功能
 * 严格遵循CLAUDE.md全局架构规范和Service设计标准
 * </p>
 * <p>
 * <strong>核心功能：</strong>
 * </p>
 * <ul>
 *   <li>自助签离</li>
 *   <li>人工签离</li>
 *   <li>访客卡归还管理</li>
 *   <li>满意度评价</li>
 *   <li>超时统计</li>
 *   <li>访问时长分析</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface SelfCheckOutService {

    /**
     * 执行自助签离
     * <p>
     * 访客在自助终端通过访客码完成签离
     * </p>
     *
     * @param visitorCode 访客码
     * @param terminalId 终端ID
     * @param terminalLocation 终端位置
     * @param cardReturnStatus 访客卡归还状态
     * @param visitorCard 访客卡号
     * @return 签离记录
     */
    SelfCheckOutEntity performCheckOut(String visitorCode,
                                       String terminalId,
                                       String terminalLocation,
                                       Integer cardReturnStatus,
                                       String visitorCard);

    /**
     * 人工签离
     * <p>
     * 管理员手动执行访客签离
     * </p>
     *
     * @param visitorCode 访客码
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param reason 签离原因
     * @return 签离记录
     */
    SelfCheckOutEntity manualCheckOut(String visitorCode,
                                      Long operatorId,
                                      String operatorName,
                                      String reason);

    /**
     * 根据访客码查询签离记录
     *
     * @param visitorCode 访客码
     * @return 签离记录
     */
    SelfCheckOutEntity getCheckOutByVisitorCode(String visitorCode);

    /**
     * 根据登记ID查询签离记录
     *
     * @param registrationId 登记ID
     * @return 签离记录
     */
    SelfCheckOutEntity getCheckOutByRegistrationId(Long registrationId);

    /**
     * 查询指定终端的签离记录
     *
     * @param terminalId 终端ID
     * @param limit 限制数量
     * @return 签离记录列表
     */
    List<SelfCheckOutEntity> getCheckOutByTerminal(String terminalId, Integer limit);

    /**
     * 查询超时签离记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 超时签离记录列表
     */
    List<SelfCheckOutEntity> getOvertimeRecords(LocalDate startDate, LocalDate endDate);

    /**
     * 查询未归还访客卡的记录
     *
     * @return 未归还访客卡的记录列表
     */
    List<SelfCheckOutEntity> getUnreturnedCards();

    /**
     * 分页查询签离记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param visitorName 访客姓名（可选）
     * @param intervieweeName 被访人姓名（可选）
     * @param isOvertime 是否超时（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页结果
     */
    PageResult<SelfCheckOutEntity> queryPage(Integer pageNum,
                                              Integer pageSize,
                                              String visitorName,
                                              String intervieweeName,
                                              Integer isOvertime,
                                              LocalDate startDate,
                                              LocalDate endDate);

    /**
     * 查询指定被访人的签离记录
     *
     * @param intervieweeId 被访人ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 签离记录列表
     */
    List<SelfCheckOutEntity> getByInterviewee(Long intervieweeId,
                                              LocalDate startDate,
                                              LocalDate endDate);

    /**
     * 查询指定日期的签离记录
     *
     * @param date 日期
     * @return 签离记录列表
     */
    List<SelfCheckOutEntity> getByDate(LocalDate date);

    /**
     * 获取签离统计信息
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    Map<String, Object> getStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 更新满意度评价
     *
     * @param checkOutId 签离记录ID
     * @param satisfactionScore 满意度评分（1-5分）
     * @param visitorFeedback 访客反馈
     */
    void updateSatisfaction(Long checkOutId,
                            Integer satisfactionScore,
                            String visitorFeedback);

    /**
     * 更新访客卡归还状态
     *
     * @param checkOutId 签离记录ID
     * @param cardReturnStatus 归还状态（0-未归还 1-已归还 2-卡遗失）
     * @param visitorCard 访客卡号
     */
    void updateCardReturnStatus(Long checkOutId,
                               Integer cardReturnStatus,
                               String visitorCard);

    /**
     * 获取访问时长统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时长统计信息
     */
    Map<String, Object> getDurationStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取满意度统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 满意度统计信息
     */
    Map<String, Object> getSatisfactionStatistics(LocalDate startDate, LocalDate endDate);
}
