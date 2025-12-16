package net.lab1024.sa.visitor.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.domain.form.VisitorAppointmentQueryForm;
import net.lab1024.sa.visitor.domain.form.VisitorMobileForm;
import net.lab1024.sa.visitor.domain.vo.VisitorAppointmentDetailVO;

/**
 * 访客预约服务接口
 * <p>
 * 访客预约管理核心业务服务
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义核心业务方法
 * - 实现类在service.impl包中
 * - 使用@Resource依赖注入
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VisitorAppointmentService {

    /**
     * 获取预约详情
     *
     * @param appointmentId 预约ID
     * @return 预约详情
     */
    ResponseDTO<VisitorAppointmentDetailVO> getAppointmentDetail(Long appointmentId);

    /**
     * 创建预约
     *
     * @param form 预约表单
     * @return 预约结果
     */
    ResponseDTO<Long> createAppointment(VisitorMobileForm form);

    /**
     * 更新预约状态（由审批结果监听器调用）
     *
     * @param appointmentId 预约ID
     * @param status 审批状态
     * @param approvalComment 审批意见
     */
    void updateAppointmentStatus(Long appointmentId, String status, String approvalComment);

    /**
     * 分页查询访客预约
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<VisitorAppointmentDetailVO> queryAppointments(VisitorAppointmentQueryForm queryForm);
}

