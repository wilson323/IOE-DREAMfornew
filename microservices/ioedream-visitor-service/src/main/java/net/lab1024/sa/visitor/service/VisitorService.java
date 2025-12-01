package net.lab1024.sa.visitor.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.visitor.domain.entity.VisitorEntity;
import net.lab1024.sa.visitor.domain.form.VisitorForm;
import net.lab1024.sa.visitor.domain.query.VisitorQueryVO;
import net.lab1024.sa.visitor.domain.vo.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客管理服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
public interface VisitorService {

    /**
     * 创建访客
     *
     * @param createVO 创建请求
     * @return 创建结果
     */
    ResponseDTO<Long> createVisitor(VisitorCreateVO createVO);

    /**
     * 更新访客信息
     *
     * @param visitorId 访客ID
     * @param updateVO 更新请求
     * @return 更新结果
     */
    ResponseDTO<Void> updateVisitor(Long visitorId, VisitorUpdateVO updateVO);

    /**
     * 删除访客
     *
     * @param visitorId 访客ID
     * @return 删除结果
     */
    ResponseDTO<Void> deleteVisitor(Long visitorId);

    /**
     * 获取访客详情
     *
     * @param visitorId 访客ID
     * @return 访客详情
     */
    ResponseDTO<VisitorDetailVO> getVisitorDetail(Long visitorId);

    /**
     * 搜索访客列表
     *
     * @param searchVO 搜索条件
     * @return 访客列表
     */
    ResponseDTO<List<VisitorVO>> searchVisitors(VisitorSearchVO searchVO);

    /**
     * 分页查询访客
     *
     * @param queryVO 查询条件
     * @return 分页结果
     */
    ResponseDTO<PageResult<VisitorVO>> queryVisitors(VisitorQueryVO queryVO);

    /**
     * 审批访客申请
     *
     * @param approvalVO 审批请求
     * @return 审批结果
     */
    ResponseDTO<Void> approveVisitor(VisitorApprovalVO approvalVO);

    /**
     * 批量审批访客
     *
     * @param batchApprovalVO 批量审批请求
     * @return 审批结果
     */
    ResponseDTO<Void> batchApproveVisitors(VisitorBatchApprovalVO batchApprovalVO);

    /**
     * 访客签到
     *
     * @param checkinVO 签到请求
     * @return 签到结果
     */
    ResponseDTO<Void> visitorCheckin(VisitorCheckinVO checkinVO);

    /**
     * 访客签退
     *
     * @param checkoutVO 签退请求
     * @return 签退结果
     */
    ResponseDTO<Void> visitorCheckout(VisitorCheckoutVO checkoutVO);

    /**
     * 获取访客统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    ResponseDTO<VisitorStatisticsVO> getVisitorStatistics(LocalDateTime startTime,
                                                           LocalDateTime endTime);

    /**
     * 获取即将到期的访客预约
     *
     * @param hours 小时数
     * @return 预约列表
     */
    ResponseDTO<List<VisitorVO>> getExpiringVisitors(Integer hours);

    /**
     * 获取正在访问的访客
     *
     * @return 访客列表
     */
    ResponseDTO<List<VisitorVO>> getActiveVisitors();

    /**
     * 更新访客状态
     *
     * @param visitorId 访客ID
     * @param status 状态
     * @return 更新结果
     */
    ResponseDTO<Void> updateVisitorStatus(Long visitorId, Integer status);

    /**
     * 导出访客信息
     *
     * @param searchVO 搜索条件
     * @return 导出结果
     */
    ResponseDTO<String> exportVisitors(VisitorSearchVO searchVO);

    /**
     * 根据身份证号查询访客
     *
     * @param idNumber 身份证号
     * @return 访客信息
     */
    ResponseDTO<VisitorVO> getVisitorByIdNumber(String idNumber);

    /**
     * 获取被访人相关的访客记录
     *
     * @param visiteeId 被访人ID
     * @param limit 限制数量
     * @return 访客列表
     */
    ResponseDTO<List<VisitorVO>> getVisitorsByVisiteeId(Long visiteeId, Integer limit);

    // ========== 以下是Controller需要的额外方法 ==========

    /**
     * 根据ID获取访客信息
     */
    VisitorVO getById(Long id);

    /**
     * 新增访客
     */
    Long add(VisitorForm form);

    /**
     * 更新访客信息
     */
    Boolean update(VisitorForm form);

    /**
     * 删除访客
     */
    Boolean delete(Long id);

    /**
     * 批量删除访客
     */
    Boolean batchDelete(List<Long> ids);

    /**
     * 根据手机号查询访客
     */
    List<VisitorVO> getByPhone(String phone);

    /**
     * 查询当前在访客列表
     */
    List<VisitorVO> getCurrentVisitors();

    /**
     * 获取访客统计信息
     */
    Object getStatistics();
}