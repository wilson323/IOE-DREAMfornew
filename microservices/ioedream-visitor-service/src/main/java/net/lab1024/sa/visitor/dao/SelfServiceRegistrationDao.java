package net.lab1024.sa.visitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.visitor.SelfServiceRegistrationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 自助登记终端数据访问接口
 * <p>
 * 提供自助登记记录的数据库操作方法
 * 严格遵循CLAUDE.md全局架构规范和DAO设计标准
 * </p>
 *
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>自助登记记录CRUD</li>
 *   <li>访客码查询</li>
 *   <li>终端登记查询</li>
 *   <li>状态查询</li>
 *   <li>统计查询</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Mapper
public interface SelfServiceRegistrationDao extends BaseMapper<SelfServiceRegistrationEntity> {

    /**
     * 根据访客码查询登记记录
     *
     * @param visitorCode 访客码
     * @return 登记记录，不存在返回null
     */
    default SelfServiceRegistrationEntity selectByVisitorCode(String visitorCode) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                .eq(SelfServiceRegistrationEntity::getVisitorCode, visitorCode)
                .orderByDesc(SelfServiceRegistrationEntity::getCreateTime)
                .last("LIMIT 1"));
    }

    /**
     * 根据登记编号查询记录
     *
     * @param registrationCode 登记编号
     * @return 登记记录，不存在返回null
     */
    default SelfServiceRegistrationEntity selectByRegistrationCode(String registrationCode) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                .eq(SelfServiceRegistrationEntity::getRegistrationCode, registrationCode));
    }

    /**
     * 查询指定终端的登记记录
     *
     * @param terminalId 终端ID
     * @param limit 限制数量
     * @return 登记记录列表
     */
    default List<SelfServiceRegistrationEntity> selectByTerminalId(String terminalId, Integer limit) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                .eq(SelfServiceRegistrationEntity::getTerminalId, terminalId)
                .orderByDesc(SelfServiceRegistrationEntity::getCreateTime)
                .last("LIMIT " + (limit != null ? limit : 100)));
    }

    /**
     * 查询指定状态的登记记录
     *
     * @param status 登记状态
     * @param limit 限制数量
     * @return 登记记录列表
     */
    default List<SelfServiceRegistrationEntity> selectByStatus(Integer status, Integer limit) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                .eq(SelfServiceRegistrationEntity::getRegistrationStatus, status)
                .orderByDesc(SelfServiceRegistrationEntity::getCreateTime)
                .last("LIMIT " + (limit != null ? limit : 100)));
    }

    /**
     * 查询待审批的登记记录
     *
     * @param intervieweeId 被访人ID（可选）
     * @param limit 限制数量
     * @return 待审批记录列表
     */
    default List<SelfServiceRegistrationEntity> selectPendingApprovals(Long intervieweeId, Integer limit) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                        .eq(SelfServiceRegistrationEntity::getRegistrationStatus, 0)
                        .orderByDesc(SelfServiceRegistrationEntity::getCreateTime);

        if (intervieweeId != null) {
            wrapper.eq(SelfServiceRegistrationEntity::getIntervieweeId, intervieweeId);
        }

        return selectList(wrapper.last("LIMIT " + (limit != null ? limit : 100)));
    }

    /**
     * 查询指定日期的登记记录
     *
     * @param visitDate 访问日期
     * @return 登记记录列表
     */
    default List<SelfServiceRegistrationEntity> selectByVisitDate(LocalDate visitDate) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                .eq(SelfServiceRegistrationEntity::getVisitDate, visitDate)
                .orderByDesc(SelfServiceRegistrationEntity::getCreateTime));
    }

    /**
     * 查询在场访客（已签到未签离）
     *
     * @return 在场访客列表
     */
    default List<SelfServiceRegistrationEntity> selectActiveVisitors() {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                .eq(SelfServiceRegistrationEntity::getRegistrationStatus, 3)
                .isNotNull(SelfServiceRegistrationEntity::getCheckInTime)
                .isNull(SelfServiceRegistrationEntity::getCheckOutTime)
                .orderByDesc(SelfServiceRegistrationEntity::getCheckInTime));
    }

    /**
     * 查询超时未签离的访客
     *
     * @param currentTime 当前时间
     * @return 超时访客列表
     */
    default List<SelfServiceRegistrationEntity> selectOverdueVisitors(LocalDateTime currentTime) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                .eq(SelfServiceRegistrationEntity::getRegistrationStatus, 3)
                .isNotNull(SelfServiceRegistrationEntity::getCheckInTime)
                .isNull(SelfServiceRegistrationEntity::getCheckOutTime)
                .lt(SelfServiceRegistrationEntity::getExpectedLeaveTime, currentTime)
                .orderByDesc(SelfServiceRegistrationEntity::getExpectedLeaveTime));
    }

    /**
     * 查询指定被访人的登记记录
     *
     * @param intervieweeId 被访人ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 登记记录列表
     */
    default List<SelfServiceRegistrationEntity> selectByInterviewee(Long intervieweeId,
                                                                   LocalDate startDate,
                                                                   LocalDate endDate) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                .eq(SelfServiceRegistrationEntity::getIntervieweeId, intervieweeId)
                .ge(SelfServiceRegistrationEntity::getVisitDate, startDate)
                .le(SelfServiceRegistrationEntity::getVisitDate, endDate)
                .orderByDesc(SelfServiceRegistrationEntity::getVisitDate)
                .orderByDesc(SelfServiceRegistrationEntity::getCreateTime));
    }

    /**
     * 统计指定日期的登记数量
     *
     * @param visitDate 访问日期
     * @return 登记数量
     */
    default Integer countByVisitDate(LocalDate visitDate) {
        return Math.toIntExact(selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                .eq(SelfServiceRegistrationEntity::getVisitDate, visitDate)));
    }

    /**
     * 统计指定终端的登记数量
     *
     * @param terminalId 终端ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 登记数量
     */
    default Integer countByTerminal(String terminalId, LocalDate startDate, LocalDate endDate) {
        return Math.toIntExact(selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                .eq(SelfServiceRegistrationEntity::getTerminalId, terminalId)
                .ge(SelfServiceRegistrationEntity::getVisitDate, startDate)
                .le(SelfServiceRegistrationEntity::getVisitDate, endDate)));
    }

    /**
     * 统计待审批数量
     *
     * @param intervieweeId 被访人ID（可选）
     * @return 待审批数量
     */
    default Integer countPendingApprovals(Long intervieweeId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                        .eq(SelfServiceRegistrationEntity::getRegistrationStatus, 0);

        if (intervieweeId != null) {
            wrapper.eq(SelfServiceRegistrationEntity::getIntervieweeId, intervieweeId);
        }

        return Math.toIntExact(selectCount(wrapper));
    }

    /**
     * 统计在场访客数量
     *
     * @return 在场访客数量
     */
    default Integer countActiveVisitors() {
        return Math.toIntExact(selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                .eq(SelfServiceRegistrationEntity::getRegistrationStatus, 3)
                .isNotNull(SelfServiceRegistrationEntity::getCheckInTime)
                .isNull(SelfServiceRegistrationEntity::getCheckOutTime)));
    }
}
