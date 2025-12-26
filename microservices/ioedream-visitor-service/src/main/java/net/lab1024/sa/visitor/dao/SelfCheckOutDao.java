package net.lab1024.sa.visitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.visitor.SelfCheckOutEntity;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 自助签离数据访问接口
 * <p>
 * 提供自助签离记录的数据库操作方法
 * 严格遵循CLAUDE.md全局架构规范和DAO设计标准
 * </p>
 *
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>签离记录CRUD</li>
 *   <li>访客码查询</li>
 *   <li>终端签离查询</li>
 *   <li>超时记录查询</li>
 *   <li>统计查询</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Mapper
public interface SelfCheckOutDao extends BaseMapper<SelfCheckOutEntity> {

    /**
     * 根据访客码查询签离记录
     *
     * @param visitorCode 访客码
     * @return 签离记录，不存在返回null
     */
    default SelfCheckOutEntity selectByVisitorCode(String visitorCode) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfCheckOutEntity>()
                .eq(SelfCheckOutEntity::getVisitorCode, visitorCode)
                .orderByDesc(SelfCheckOutEntity::getCheckOutTime)
                .last("LIMIT 1"));
    }

    /**
     * 根据登记ID查询签离记录
     *
     * @param registrationId 登记ID
     * @return 签离记录，不存在返回null
     */
    default SelfCheckOutEntity selectByRegistrationId(Long registrationId) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfCheckOutEntity>()
                .eq(SelfCheckOutEntity::getRegistrationId, registrationId));
    }

    /**
     * 查询指定终端的签离记录
     *
     * @param terminalId 终端ID
     * @param limit 限制数量
     * @return 签离记录列表
     */
    default List<SelfCheckOutEntity> selectByTerminalId(String terminalId, Integer limit) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfCheckOutEntity>()
                .eq(SelfCheckOutEntity::getTerminalId, terminalId)
                .orderByDesc(SelfCheckOutEntity::getCheckOutTime)
                .last("LIMIT " + (limit != null ? limit : 100)));
    }

    /**
     * 查询超时签离记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 超时签离记录列表
     */
    default List<SelfCheckOutEntity> selectOvertimeRecords(LocalDate startDate, LocalDate endDate) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfCheckOutEntity>()
                .eq(SelfCheckOutEntity::getIsOvertime, 1)
                .ge(SelfCheckOutEntity::getCheckOutTime, startDate.atStartOfDay())
                .le(SelfCheckOutEntity::getCheckOutTime, endDate.atTime(23, 59, 59))
                .orderByDesc(SelfCheckOutEntity::getOvertimeDuration));
    }

    /**
     * 查询未归还访客卡的记录
     *
     * @return 未归还访客卡的记录列表
     */
    default List<SelfCheckOutEntity> selectUnreturnedCards() {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfCheckOutEntity>()
                .eq(SelfCheckOutEntity::getCardReturnStatus, 0)
                .orderByDesc(SelfCheckOutEntity::getCheckOutTime));
    }

    /**
     * 查询指定日期的签离记录
     *
     * @param date 日期
     * @return 签离记录列表
     */
    default List<SelfCheckOutEntity> selectByDate(LocalDate date) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfCheckOutEntity>()
                .ge(SelfCheckOutEntity::getCheckOutTime, date.atStartOfDay())
                .le(SelfCheckOutEntity::getCheckOutTime, date.atTime(23, 59, 59))
                .orderByDesc(SelfCheckOutEntity::getCheckOutTime));
    }

    /**
     * 查询指定被访人的签离记录
     *
     * @param intervieweeId 被访人ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 签离记录列表
     */
    default List<SelfCheckOutEntity> selectByInterviewee(Long intervieweeId,
                                                         LocalDate startDate,
                                                         LocalDate endDate) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfCheckOutEntity>()
                .eq(SelfCheckOutEntity::getIntervieweeId, intervieweeId)
                .ge(SelfCheckOutEntity::getCheckOutTime, startDate.atStartOfDay())
                .le(SelfCheckOutEntity::getCheckOutTime, endDate.atTime(23, 59, 59))
                .orderByDesc(SelfCheckOutEntity::getCheckOutTime));
    }

    /**
     * 统计指定日期的签离数量
     *
     * @param date 日期
     * @return 签离数量
     */
    default Integer countByDate(LocalDate date) {
        return Math.toIntExact(selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfCheckOutEntity>()
                .ge(SelfCheckOutEntity::getCheckOutTime, date.atStartOfDay())
                .le(SelfCheckOutEntity::getCheckOutTime, date.atTime(23, 59, 59))));
    }

    /**
     * 统计超时签离数量
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 超时签离数量
     */
    default Integer countOvertime(LocalDate startDate, LocalDate endDate) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfCheckOutEntity> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(SelfCheckOutEntity::getIsOvertime, 1)
                   .ge(SelfCheckOutEntity::getCheckOutTime, startDate.atStartOfDay())
                   .le(SelfCheckOutEntity::getCheckOutTime, endDate.atTime(23, 59, 59));
        return Math.toIntExact(selectCount(queryWrapper));
    }

    /**
     * 统计未归还访客卡数量
     *
     * @return 未归还数量
     */
    default Integer countUnreturnedCards() {
        return Math.toIntExact(selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfCheckOutEntity>()
                .eq(SelfCheckOutEntity::getCardReturnStatus, 0)));
    }

    /**
     * 查询平均访问时长
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 平均访问时长（分钟）
     */
    default Double getAverageDuration(LocalDate startDate, LocalDate endDate) {
        List<SelfCheckOutEntity> records = selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelfCheckOutEntity>()
                .isNotNull(SelfCheckOutEntity::getVisitDuration)
                .ge(SelfCheckOutEntity::getCheckOutTime, startDate.atStartOfDay())
                .le(SelfCheckOutEntity::getCheckOutTime, endDate.atTime(23, 59, 59)));

        if (records.isEmpty()) {
            return 0.0;
        }

        return records.stream()
                .mapToInt(SelfCheckOutEntity::getVisitDuration)
                .average()
                .orElse(0.0);
    }
}
