package net.lab1024.sa.visitor.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.visitor.service.VisitorService;
import net.lab1024.sa.visitor.domain.vo.VisitorVO;
import net.lab1024.sa.common.visitor.entity.VisitorAppointmentEntity;
import net.lab1024.sa.visitor.dao.VisitorAppointmentDao;
import jakarta.annotation.Resource;

/**
 * 访客服务实现类
 * <p>
 * 实现访客管理的核心业务功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解标识服务类
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 遵循四层架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 访客信息查询
 * - 访客状态管理
 * - 访客记录统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VisitorServiceImpl implements VisitorService {

    @Resource
    private VisitorAppointmentDao visitorAppointmentDao;

    /**
     * 获取访客信息
     *
     * @param visitorId 访客ID
     * @return 访客信息
     */
    @Override
    @Observed(name = "visitor.getInfo", contextualName = "visitor-get-info")
    @Transactional(readOnly = true)
    public ResponseDTO<?> getVisitorInfo(Long visitorId) {
        log.info("[访客服务] 获取访客信息，visitorId={}", visitorId);

        try {
            // 参数验证
            if (visitorId == null) {
                throw new ParamException("PARAM_ERROR", "访客ID不能为空");
            }

            // 查询访客预约信息
            VisitorAppointmentEntity appointment = visitorAppointmentDao.selectById(visitorId);
            if (appointment == null) {
                log.warn("[访客服务] 访客预约信息不存在，visitorId={}", visitorId);
                throw new BusinessException("VISITOR_NOT_FOUND", "访客信息不存在");
            }

            // 转换为VO对象
            VisitorVO visitorVO = new VisitorVO();
            visitorVO.setVisitorId(appointment.getAppointmentId());
            visitorVO.setVisitorName(appointment.getVisitorName());
            visitorVO.setPhone(appointment.getPhoneNumber());
            visitorVO.setCompanyName(null); // VisitorAppointmentEntity中无此字段，如需可从其他表关联查询
            visitorVO.setVisitPurpose(appointment.getVisitPurpose());
            visitorVO.setVisiteeId(appointment.getVisitUserId());
            visitorVO.setVisiteeName(appointment.getVisitUserName());
            // 将String类型的status转换为Integer（0-待审核 1-已通过 2-已拒绝 3-已访问 4-已离开）
            Integer status = convertStatusToInteger(appointment.getStatus());
            visitorVO.setStatus(status);
            visitorVO.setCreateTime(appointment.getCreateTime());
            visitorVO.setUpdateTime(appointment.getUpdateTime());

            log.info("[访客服务] 获取访客信息成功，visitorId={}", visitorId);
            return ResponseDTO.ok(visitorVO);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客服务] 获取访客信息参数错误，visitorId={}", visitorId, e);
            throw new ParamException("GET_VISITOR_INFO_PARAM_ERROR", "查询参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[访客服务] 获取访客信息业务异常，visitorId={}", visitorId, e);
            throw e;
        } catch (Exception e) {
            log.error("[访客服务] 获取访客信息系统异常，visitorId={}", visitorId, e);
            throw new SystemException("GET_VISITOR_INFO_ERROR", "获取访客信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将String类型的状态转换为Integer类型
     *
     * @param status String类型的状态（PENDING/APPROVED/REJECTED/CANCELLED/CHECKED_IN/CHECKED_OUT）
     * @return Integer类型的状态（0-待审核 1-已通过 2-已拒绝 3-已访问 4-已离开）
     */
    private Integer convertStatusToInteger(String status) {
        if (status == null) {
            return 0; // 默认为待审核
        }
        return switch (status.toUpperCase()) {
            case "PENDING" -> 0; // 待审核
            case "APPROVED" -> 1; // 已通过
            case "REJECTED" -> 2; // 已拒绝
            case "CHECKED_IN" -> 3; // 已访问（已签到）
            case "CHECKED_OUT" -> 4; // 已离开（已签退）
            case "CANCELLED" -> 2; // 已取消视为已拒绝
            default -> 0; // 未知状态默认为待审核
        };
    }
}