package net.lab1024.sa.visitor.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.dao.VisitorAppointmentDao;
import net.lab1024.sa.common.visitor.entity.VisitorAppointmentEntity;
import net.lab1024.sa.visitor.domain.vo.VisitorAppointmentDetailVO;
import net.lab1024.sa.visitor.domain.vo.VisitorVO;
import net.lab1024.sa.visitor.service.VisitorQueryService;

/**
 * 访客查询服务实现类
 * <p>
 * 实现访客信息查询的核心业务功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解标识服务类
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 遵循四层架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class VisitorQueryServiceImpl implements VisitorQueryService {

    @Resource
    private VisitorAppointmentDao visitorAppointmentDao;

    /**
     * 查询访客记录列表
     * <p>
     * 根据手机号分页查询访客预约记录
     * 实现步骤：
     * 1. 构建查询条件（手机号、未删除）
     * 2. 执行分页查询
     * 3. 转换为VO列表
     * </p>
     *
     * @param phone 手机号
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 访客记录列表
     */
    @Override
    @Observed(name = "visitor.query.records", contextualName = "visitor-query-records")
    public ResponseDTO<?> queryVisitorRecords(String phone, Integer pageNum, Integer pageSize) {
        log.debug("[访客查询] 查询访客记录，phone={}, pageNum={}, pageSize={}", phone, pageNum, pageSize);

        // 参数验证
        if (!StringUtils.hasText(phone)) {
            log.warn("[访客查询] 手机号不能为空");
            return ResponseDTO.error("PARAM_ERROR", "手机号不能为空");
        }

        // 构建查询条件
        LambdaQueryWrapper<VisitorAppointmentEntity> wrapper = Wrappers.lambdaQuery(VisitorAppointmentEntity.class)
                .eq(VisitorAppointmentEntity::getPhoneNumber, phone)
                .eq(VisitorAppointmentEntity::getDeletedFlag, false)
                .orderByDesc(VisitorAppointmentEntity::getCreateTime);

        // 执行分页查询
        Integer queryPageNum = pageNum != null && pageNum > 0 ? pageNum : 1;
        Integer queryPageSize = pageSize != null && pageSize > 0 ? pageSize : 20;
        Page<VisitorAppointmentEntity> page = new Page<>(queryPageNum, queryPageSize);
        Page<VisitorAppointmentEntity> pageResult = visitorAppointmentDao.selectPage(page, wrapper);

        // 转换为VO列表
        List<VisitorAppointmentDetailVO> voList = pageResult.getRecords().stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<VisitorAppointmentDetailVO> result = new PageResult<>();
        result.setList(voList);
        result.setTotal(pageResult.getTotal());
        result.setPageNum(queryPageNum);
        result.setPageSize(queryPageSize);
        result.setPages((int) pageResult.getPages());

        log.debug("[访客查询] 查询完成，总数={}, 当前页={}", result.getTotal(), queryPageNum);
        return ResponseDTO.ok(result);
    }

    /**
     * 查询预约记录
     * <p>
     * 根据用户ID和状态查询预约记录
     * 实现步骤：
     * 1. 构建查询条件（用户ID、状态、未删除）
     * 2. 执行查询
     * 3. 转换为VO列表
     * </p>
     *
     * @param userId 用户ID（被访人ID）
     * @param status 状态（可选）
     * @return 预约记录列表
     */
    @Override
    @Observed(name = "visitor.query.appointments", contextualName = "visitor-query-appointments")
    public ResponseDTO<?> queryAppointments(Long userId, Integer status) {
        log.debug("[访客查询] 查询预约记录，userId={}, status={}", userId, status);

        // 参数验证
        if (userId == null) {
            log.warn("[访客查询] 用户ID不能为空");
            return ResponseDTO.error("PARAM_ERROR", "用户ID不能为空");
        }

        // 构建查询条件
        LambdaQueryWrapper<VisitorAppointmentEntity> wrapper = Wrappers.lambdaQuery(VisitorAppointmentEntity.class)
                .eq(VisitorAppointmentEntity::getVisitUserId, userId)
                .eq(status != null, VisitorAppointmentEntity::getStatus, convertStatusToString(status))
                .eq(VisitorAppointmentEntity::getDeletedFlag, false)
                .orderByDesc(VisitorAppointmentEntity::getAppointmentStartTime);

        // 执行查询
        List<VisitorAppointmentEntity> entities = visitorAppointmentDao.selectList(wrapper);

        // 转换为VO列表
        List<VisitorAppointmentDetailVO> voList = entities.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());

        log.debug("[访客查询] 查询完成，记录数={}", voList.size());
        return ResponseDTO.ok(voList);
    }

    /**
     * 根据身份证号查询访客信息
     * <p>
     * 实现步骤：
     * 1. 根据身份证号查询预约记录
     * 2. 转换为访客VO
     * </p>
     *
     * @param idNumber 身份证号
     * @return 访客信息
     */
    @Override
    @Observed(name = "visitor.query.byIdNumber", contextualName = "visitor-query-by-id-number")
    public ResponseDTO<?> getVisitorByIdNumber(String idNumber) {
        log.debug("[访客查询] 根据身份证号查询访客，idNumber={}", idNumber);

        // 参数验证
        if (!StringUtils.hasText(idNumber)) {
            log.warn("[访客查询] 身份证号不能为空");
            return ResponseDTO.error("PARAM_ERROR", "身份证号不能为空");
        }

        // 构建查询条件
        LambdaQueryWrapper<VisitorAppointmentEntity> wrapper = Wrappers.lambdaQuery(VisitorAppointmentEntity.class)
                .eq(VisitorAppointmentEntity::getIdCardNumber, idNumber)
                .eq(VisitorAppointmentEntity::getDeletedFlag, false)
                .orderByDesc(VisitorAppointmentEntity::getCreateTime)
                .last("LIMIT 1");

        // 执行查询
        VisitorAppointmentEntity entity = visitorAppointmentDao.selectOne(wrapper);

        if (entity == null) {
            log.debug("[访客查询] 未找到访客记录，idNumber={}", idNumber);
            return ResponseDTO.ok(null);
        }

        // 转换为VO
        VisitorVO vo = convertToVisitorVO(entity);

        log.debug("[访客查询] 查询完成，visitorName={}", vo.getVisitorName());
        return ResponseDTO.ok(vo);
    }

    /**
     * 根据被访人ID查询访客列表
     * <p>
     * 实现步骤：
     * 1. 根据被访人ID查询预约记录
     * 2. 限制返回数量
     * 3. 转换为访客VO列表
     * </p>
     *
     * @param visiteeId 被访人ID
     * @param limit 限制数量
     * @return 访客列表
     */
    @Override
    @Observed(name = "visitor.query.byVisiteeId", contextualName = "visitor-query-by-visitee-id")
    public ResponseDTO<?> getVisitorsByVisiteeId(Long visiteeId, Integer limit) {
        log.debug("[访客查询] 根据被访人ID查询访客列表，visiteeId={}, limit={}", visiteeId, limit);

        // 参数验证
        if (visiteeId == null) {
            log.warn("[访客查询] 被访人ID不能为空");
            return ResponseDTO.error("PARAM_ERROR", "被访人ID不能为空");
        }

        // 构建查询条件
        LambdaQueryWrapper<VisitorAppointmentEntity> wrapper = Wrappers.lambdaQuery(VisitorAppointmentEntity.class)
                .eq(VisitorAppointmentEntity::getVisitUserId, visiteeId)
                .eq(VisitorAppointmentEntity::getDeletedFlag, false)
                .orderByDesc(VisitorAppointmentEntity::getAppointmentStartTime);

        // 限制数量
        Integer queryLimit = limit != null && limit > 0 ? limit : 20;
        wrapper.last("LIMIT " + queryLimit);

        // 执行查询
        List<VisitorAppointmentEntity> entities = visitorAppointmentDao.selectList(wrapper);

        // 转换为VO列表
        List<VisitorVO> voList = entities.stream()
                .map(this::convertToVisitorVO)
                .collect(Collectors.toList());

        log.debug("[访客查询] 查询完成，记录数={}", voList.size());
        return ResponseDTO.ok(voList);
    }

    /**
     * 转换为预约详情VO
     *
     * @param entity 预约实体
     * @return 预约详情VO
     */
    private VisitorAppointmentDetailVO convertToDetailVO(VisitorAppointmentEntity entity) {
        VisitorAppointmentDetailVO vo = new VisitorAppointmentDetailVO();
        vo.setAppointmentId(entity.getAppointmentId());
        vo.setVisitorName(entity.getVisitorName());
        vo.setPhoneNumber(entity.getPhoneNumber());
        vo.setIdCardNumber(entity.getIdCardNumber());
        vo.setVisitUserId(entity.getVisitUserId());
        vo.setVisitUserName(entity.getVisitUserName());
        vo.setAppointmentStartTime(entity.getAppointmentStartTime());
        vo.setAppointmentEndTime(entity.getAppointmentEndTime());
        vo.setVisitPurpose(entity.getVisitPurpose());
        vo.setAppointmentStatus(convertStatusToInteger(entity.getStatus()));
        vo.setCheckInTime(entity.getCheckInTime());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    /**
     * 转换为访客VO
     *
     * @param entity 预约实体
     * @return 访客VO
     */
    private VisitorVO convertToVisitorVO(VisitorAppointmentEntity entity) {
        VisitorVO vo = new VisitorVO();
        vo.setVisitorId(entity.getAppointmentId()); // 使用预约ID作为访客ID
        vo.setVisitorName(entity.getVisitorName());
        vo.setIdNumber(entity.getIdCardNumber());
        vo.setPhone(entity.getPhoneNumber());
        vo.setVisitPurpose(entity.getVisitPurpose());
        vo.setVisiteeId(entity.getVisitUserId());
        vo.setVisiteeName(entity.getVisitUserName());
        vo.setStatus(convertStatusToInteger(entity.getStatus()));
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    /**
     * 状态转换：字符串转整数
     *
     * @param status 状态字符串
     * @return 状态整数
     */
    private Integer convertStatusToInteger(String status) {
        if (status == null) {
            return 0;
        }
        return switch (status) {
            case "PENDING" -> 0;
            case "APPROVED" -> 1;
            case "REJECTED" -> 2;
            case "CHECKED_IN" -> 3;
            case "CHECKED_OUT" -> 4;
            case "CANCELLED" -> 5;
            default -> 0;
        };
    }

    /**
     * 状态转换：整数转字符串
     *
     * @param status 状态整数
     * @return 状态字符串
     */
    private String convertStatusToString(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case 0 -> "PENDING";
            case 1 -> "APPROVED";
            case 2 -> "REJECTED";
            case 3 -> "CHECKED_IN";
            case 4 -> "CHECKED_OUT";
            case 5 -> "CANCELLED";
            default -> "PENDING";
        };
    }
}
