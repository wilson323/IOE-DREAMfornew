package net.lab1024.sa.attendance.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceAnomalyApplyDao;
import net.lab1024.sa.attendance.dao.AttendanceAnomalyDao;
import net.lab1024.sa.attendance.dao.AttendanceRuleConfigDao;
import net.lab1024.sa.attendance.entity.AttendanceAnomalyApplyEntity;
import net.lab1024.sa.attendance.entity.AttendanceAnomalyEntity;
import net.lab1024.sa.attendance.entity.AttendanceRuleConfigEntity;
import net.lab1024.sa.attendance.service.AttendanceAnomalyApplyService;
import net.lab1024.sa.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 考勤异常申请服务实现类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceAnomalyApplyServiceImpl implements AttendanceAnomalyApplyService {

    private final AttendanceAnomalyApplyDao applyDao;
    private final AttendanceAnomalyDao anomalyDao;
    private final AttendanceRuleConfigDao ruleConfigDao;

    public AttendanceAnomalyApplyServiceImpl(AttendanceAnomalyApplyDao applyDao,
                                             AttendanceAnomalyDao anomalyDao,
                                             AttendanceRuleConfigDao ruleConfigDao) {
        this.applyDao = applyDao;
        this.anomalyDao = anomalyDao;
        this.ruleConfigDao = ruleConfigDao;
    }

    @Override
    public Long createSupplementCardApply(AttendanceAnomalyApplyEntity apply) {
        log.info("[异常申请] 创建补卡申请: userId={}, date={}, type={}",
                apply.getApplicantId(), apply.getAttendanceDate(), apply.getPunchType());

        try {
            // 1. 检查是否允许补卡
            Boolean allowed = checkSupplementCardAllowed(apply.getApplicantId(), apply.getAttendanceDate());
            if (!allowed) {
                throw new BusinessException("SUPPLEMENT_NOT_ALLOWED", "本月补卡次数已用完");
            }

            // 2. 检查异常记录是否存在
            QueryWrapper<AttendanceAnomalyEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", apply.getApplicantId())
                    .eq("attendance_date", apply.getAttendanceDate())
                    .eq("anomaly_type", "MISSING_CARD")
                    .eq("punch_type", apply.getPunchType());
            AttendanceAnomalyEntity anomaly = anomalyDao.selectOne(queryWrapper);

            if (anomaly == null) {
                throw new BusinessException("ANOMALY_NOT_FOUND", "未找到对应的缺卡异常记录");
            }

            // 3. 检查异常状态
            if ("APPROVED".equals(anomaly.getAnomalyStatus())) {
                throw new BusinessException("ANOMALY_ALREADY_PROCESSED", "该异常已处理");
            }

            // 4. 生成申请单号
            apply.setApplyNo(generateApplyNo());
            apply.setApplyType("SUPPLEMENT_CARD");
            apply.setAnomalyId(anomaly.getAnomalyId());
            apply.setApplyStatus("PENDING");
            apply.setApplyTime(LocalDateTime.now());

            // 5. 保存申请
            applyDao.insert(apply);

            // 6. 更新异常记录状态
            anomaly.setAnomalyStatus("APPLIED");
            anomaly.setApplyId(apply.getApplyId());
            anomalyDao.updateById(anomaly);

            log.info("[异常申请] 补卡申请创建成功: applyId={}, applyNo={}",
                    apply.getApplyId(), apply.getApplyNo());

            return apply.getApplyId();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[异常申请] 创建补卡申请异常: userId={}, error={}",
                    apply.getApplicantId(), e.getMessage(), e);
            throw new BusinessException("APPLY_CREATE_ERROR", "创建补卡申请失败: " + e.getMessage());
        }
    }

    @Override
    public Long createLateExplanationApply(AttendanceAnomalyApplyEntity apply) {
        log.info("[异常申请] 创建迟到说明申请: userId={}, date={}",
                apply.getApplicantId(), apply.getAttendanceDate());

        return createExplanationApply(apply, "LATE", "LATE_EXPLANATION");
    }

    @Override
    public Long createEarlyExplanationApply(AttendanceAnomalyApplyEntity apply) {
        log.info("[异常申请] 创建早退说明申请: userId={}, date={}",
                apply.getApplicantId(), apply.getAttendanceDate());

        return createExplanationApply(apply, "EARLY", "EARLY_EXPLANATION");
    }

    @Override
    public Long createAbsentAppealApply(AttendanceAnomalyApplyEntity apply) {
        log.info("[异常申请] 创建旷工申诉申请: userId={}, date={}",
                apply.getApplicantId(), apply.getAttendanceDate());

        try {
            // 1. 检查异常记录是否存在
            QueryWrapper<AttendanceAnomalyEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", apply.getApplicantId())
                    .eq("attendance_date", apply.getAttendanceDate())
                    .eq("anomaly_type", "ABSENT");
            AttendanceAnomalyEntity anomaly = anomalyDao.selectOne(queryWrapper);

            if (anomaly == null) {
                throw new BusinessException("ANOMALY_NOT_FOUND", "未找到对应的旷工异常记录");
            }

            // 2. 生成申请单号
            apply.setApplyNo(generateApplyNo());
            apply.setApplyType("ABSENT_APPEAL");
            apply.setAnomalyId(anomaly.getAnomalyId());
            apply.setApplyStatus("PENDING");
            apply.setApplyTime(LocalDateTime.now());

            // 3. 保存申请
            applyDao.insert(apply);

            // 4. 更新异常记录状态
            anomaly.setAnomalyStatus("APPLIED");
            anomaly.setApplyId(apply.getApplyId());
            anomalyDao.updateById(anomaly);

            log.info("[异常申请] 旷工申诉申请创建成功: applyId={}", apply.getApplyId());

            return apply.getApplyId();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[异常申请] 创建旷工申诉异常: userId={}, error={}",
                    apply.getApplicantId(), e.getMessage(), e);
            throw new BusinessException("APPLY_CREATE_ERROR", "创建旷工申诉失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean cancelApply(Long applyId, Long userId) {
        log.info("[异常申请] 撤销申请: applyId={}, userId={}", applyId, userId);

        try {
            // 1. 查询申请记录
            AttendanceAnomalyApplyEntity apply = applyDao.selectById(applyId);
            if (apply == null) {
                throw new BusinessException("APPLY_NOT_FOUND", "申请记录不存在");
            }

            // 2. 验证申请人
            if (!apply.getApplicantId().equals(userId)) {
                throw new BusinessException("PERMISSION_DENIED", "只能撤销自己的申请");
            }

            // 3. 检查申请状态
            if (!"PENDING".equals(apply.getApplyStatus())) {
                throw new BusinessException("APPLY_CANNOT_CANCEL", "只能撤销待审批的申请");
            }

            // 4. 更新申请状态
            apply.setApplyStatus("CANCELLED");
            applyDao.updateById(apply);

            // 5. 恢复异常记录状态
            if (apply.getAnomalyId() != null) {
                AttendanceAnomalyEntity anomaly = anomalyDao.selectById(apply.getAnomalyId());
                if (anomaly != null) {
                    anomaly.setAnomalyStatus("PENDING");
                    anomaly.setApplyId(null);
                    anomalyDao.updateById(anomaly);
                }
            }

            log.info("[异常申请] 申请撤销成功: applyId={}", applyId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[异常申请] 撤销申请异常: applyId={}, error={}", applyId, e.getMessage(), e);
            throw new BusinessException("APPLY_CANCEL_ERROR", "撤销申请失败: " + e.getMessage());
        }
    }

    @Override
    public AttendanceAnomalyApplyEntity getApplyById(Long applyId) {
        log.info("[异常申请] 查询申请详情: applyId={}", applyId);
        return applyDao.selectById(applyId);
    }

    @Override
    public List<AttendanceAnomalyApplyEntity> getAppliesByUserId(Long userId) {
        log.info("[异常申请] 查询用户申请列表: userId={}", userId);
        return applyDao.selectByApplicantId(userId);
    }

    @Override
    public List<AttendanceAnomalyApplyEntity> getPendingApplies() {
        log.info("[异常申请] 查询待审批申请列表");
        return applyDao.selectPendingApplications();
    }

    @Override
    public Boolean checkSupplementCardAllowed(Long userId, LocalDate attendanceDate) {
        log.info("[异常申请] 检查补卡次数限制: userId={}, date={}", userId, attendanceDate);

        try {
            // 获取规则配置
            AttendanceRuleConfigEntity rule = ruleConfigDao.selectGlobalRule();
            if (rule == null) {
                log.warn("[异常申请] 未找到规则配置");
                return true;
            }

            // 检查是否启用补卡限制
            if (rule.getMissingCardCheckEnabled() == 0) {
                return true;
            }

            // 检查补卡次数限制
            Integer allowedTimes = rule.getAllowedSupplementTimes();
            if (allowedTimes == null || allowedTimes < 0) {
                // -1表示不限制
                return true;
            }

            if (allowedTimes == 0) {
                // 0表示不允许补卡
                return false;
            }

            // 统计本月已补卡次数
            YearMonth yearMonth = YearMonth.from(attendanceDate);
            Integer usedTimes = applyDao.countSupplementCardsByMonth(
                    userId,
                    yearMonth.getYear(),
                    yearMonth.getMonthValue()
            );

            log.info("[异常申请] 补卡次数检查: userId={}, used={}, allowed={}",
                    userId, usedTimes, allowedTimes);

            return usedTimes < allowedTimes;

        } catch (Exception e) {
            log.error("[异常申请] 检查补卡次数异常: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 创建说明申请（迟到/早退）
     */
    private Long createExplanationApply(AttendanceAnomalyApplyEntity apply,
                                         String anomalyType,
                                         String applyType) {
        try {
            // 1. 检查异常记录是否存在
            QueryWrapper<AttendanceAnomalyEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", apply.getApplicantId())
                    .eq("attendance_date", apply.getAttendanceDate())
                    .eq("anomaly_type", anomalyType);

            if ("LATE".equals(anomalyType)) {
                queryWrapper.eq("punch_type", "CHECK_IN");
            } else if ("EARLY".equals(anomalyType)) {
                queryWrapper.eq("punch_type", "CHECK_OUT");
            }

            AttendanceAnomalyEntity anomaly = anomalyDao.selectOne(queryWrapper);

            if (anomaly == null) {
                throw new BusinessException("ANOMALY_NOT_FOUND",
                        "未找到对应的" + ("LATE".equals(anomalyType) ? "迟到" : "早退") + "异常记录");
            }

            // 2. 生成申请单号
            apply.setApplyNo(generateApplyNo());
            apply.setApplyType(applyType);
            apply.setAnomalyId(anomaly.getAnomalyId());
            apply.setApplyStatus("PENDING");
            apply.setApplyTime(LocalDateTime.now());

            // 3. 保存申请
            applyDao.insert(apply);

            // 4. 更新异常记录状态
            anomaly.setAnomalyStatus("APPLIED");
            anomaly.setApplyId(apply.getApplyId());
            anomalyDao.updateById(anomaly);

            log.info("[异常申请] 说明申请创建成功: applyId={}, type={}", apply.getApplyId(), applyType);

            return apply.getApplyId();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[异常申请] 创建说明申请异常: userId={}, type={}, error={}",
                    apply.getApplicantId(), applyType, e.getMessage(), e);
            throw new BusinessException("APPLY_CREATE_ERROR", "创建说明申请失败: " + e.getMessage());
        }
    }

    /**
     * 生成申请单号
     * 格式: APPLY-YYYYMMDD-序号
     */
    private String generateApplyNo() {
        String dateStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 查询当天已有的申请数量
        QueryWrapper<AttendanceAnomalyApplyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("apply_no", "APPLY-" + dateStr);
        Long count = applyDao.selectCount(queryWrapper);

        return String.format("APPLY-%s-%06d", dateStr, count + 1);
    }
}
