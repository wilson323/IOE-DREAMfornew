package net.lab1024.sa.common.visitor.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.common.visitor.entity.LogisticsReservationEntity;
import net.lab1024.sa.common.visitor.dao.LogisticsReservationDao;
import net.lab1024.sa.common.util.SystemConfigUtil;

import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物流预约管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Manager类处理复杂业务流程编排
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 物流预约的业务逻辑处理
 * - 时间窗口验证和冲突检测
 * - 审批流程管理
 * - 货物信息处理
 * - 作业调度安排
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class LogisticsReservationManager {

    private final LogisticsReservationDao logisticsReservationDao;

    /**
     * ObjectMapper实例（线程安全，可复用）
     * <p>
     * ObjectMapper是线程安全的，设计用于复用
     * 避免每次调用getter方法时创建新实例，提升性能
     * </p>
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 构造函数
     *
     * @param logisticsReservationDao 物流预约DAO
     */
    public LogisticsReservationManager(LogisticsReservationDao logisticsReservationDao) {
        this.logisticsReservationDao = logisticsReservationDao;
    }

    /**
     * 检查预约是否有效
     * <p>
     * 判断物流预约是否在有效期内且状态正常
     * </p>
     *
     * @param reservation 物流预约实体
     * @return 是否有效
     */
    public boolean isValid(LogisticsReservationEntity reservation) {
        if (reservation == null) {
            return false;
        }

        // 检查预约状态
        if (!LogisticsReservationEntity.Status.APPROVED.equals(reservation.getStatus())) {
            return false;
        }

        // 检查是否已过期
        LocalDate expectedDate = reservation.getExpectedArriveDate();
        if (expectedDate != null && expectedDate.isBefore(LocalDate.now())) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否在有效时间窗口内
     * <p>
     * 判断当前时间是否在预约的有效时间窗口内
     * </p>
     *
     * @param reservation 物流预约实体
     * @return 是否在有效时间窗口内
     */
    public boolean isWithinTimeWindow(LogisticsReservationEntity reservation) {
        if (reservation == null) {
            return false;
        }

        LocalDate expectedDate = reservation.getExpectedArriveDate();
        LocalTime startTime = reservation.getExpectedArriveTimeStart();
        LocalTime endTime = reservation.getExpectedArriveTimeEnd();

        if (expectedDate == null || startTime == null || endTime == null) {
            return true; // 如果没有时间限制，默认允许
        }

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // 检查日期是否匹配
        if (!expectedDate.equals(currentDate)) {
            return false;
        }

        // 检查时间是否在窗口内
        if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) {
            return false;
        }

        return true;
    }

    /**
     * 检查时间窗口冲突
     * <p>
     * 检查新预约是否与现有预约存在时间冲突
     * </p>
     *
     * @param newReservation 新预约实体
     * @return 是否存在冲突
     */
    public boolean hasTimeConflict(LogisticsReservationEntity newReservation) {
        if (newReservation == null) {
            return false;
        }

        LocalDate expectedDate = newReservation.getExpectedArriveDate();
        LocalTime startTime = newReservation.getExpectedArriveTimeStart();
        LocalTime endTime = newReservation.getExpectedArriveTimeEnd();

        if (expectedDate == null || startTime == null || endTime == null) {
            return false;
        }

        try {
            // 查询同一天的已批准预约
            List<LogisticsReservationEntity> existingReservations =
                logisticsReservationDao.selectByTimeRange(
                    expectedDate.atStartOfDay(),
                    expectedDate.atTime(LocalTime.MAX),
                    null, null, 1000);

            for (LogisticsReservationEntity existing : existingReservations) {
                if (!LogisticsReservationEntity.Status.APPROVED.equals(existing.getStatus())) {
                    continue;
                }

                LocalTime existingStart = existing.getExpectedArriveTimeStart();
                LocalTime existingEnd = existing.getExpectedArriveTimeEnd();

                if (existingStart == null || existingEnd == null) {
                    continue;
                }

                // 检查时间重叠
                if (isTimeOverlap(startTime, endTime, existingStart, existingEnd)) {
                    return true; // 存在时间冲突
                }
            }

        } catch (Exception e) {
            log.debug("[物流预约] 检查时间窗口冲突失败: error={}", e.getMessage());
        }

        return false;
    }

    /**
     * 判断两个时间段是否重叠
     */
    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * 检查货物信息完整性
     *
     * @param reservation 物流预约实体
     * @return 是否完整
     */
    public boolean hasCompleteGoodsInfo(LogisticsReservationEntity reservation) {
        if (reservation == null) {
            return false;
        }

        // 检查货物类型
        if (!StringUtils.hasText(reservation.getGoodsType())) {
            return false;
        }

        // 检查重量或体积至少有一个
        if (reservation.getGoodsWeight() == null && reservation.getGoodsVolume() == null) {
            return false;
        }

        // 检查重量是否为负数
        if (reservation.getGoodsWeight() != null &&
            reservation.getGoodsWeight().compareTo(java.math.BigDecimal.ZERO) < 0) {
            return false;
        }

        // 检查体积是否为负数
        if (reservation.getGoodsVolume() != null &&
            reservation.getGoodsVolume().compareTo(java.math.BigDecimal.ZERO) < 0) {
            return false;
        }

        return true;
    }

    /**
     * 生成预约编号
     *
     * @return 预约编号
     */
    public String generateReservationCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "LR" + dateStr;

        try {
            // 查询当天最大序号
            List<LogisticsReservationEntity> todayReservations =
                logisticsReservationDao.selectByTimeRange(
                    LocalDate.now().atStartOfDay(),
                    LocalDate.now().atTime(LocalTime.MAX),
                    null, null, 1000);

            int maxSequence = 0;
            for (LogisticsReservationEntity reservation : todayReservations) {
                if (StringUtils.hasText(reservation.getReservationCode()) &&
                    reservation.getReservationCode().startsWith(prefix)) {
                    try {
                        String sequenceStr = reservation.getReservationCode().substring(prefix.length());
                        int sequence = Integer.parseInt(sequenceStr);
                        maxSequence = Math.max(maxSequence, sequence);
                    } catch (NumberFormatException e) {
                        log.debug("[物流预约] 忽略格式错误的预约编号: reservationCode={}", reservation.getReservationCode(), e);
                    }
                }
            }

            // 生成新序号
            int newSequence = maxSequence + 1;
            return prefix + String.format("%06d", newSequence);

        } catch (Exception e) {
            log.error("[物流预约] 生成预约编号失败: error={}", e.getMessage());
            // 返回默认编号
            return prefix + "000001";
        }
    }

    /**
     * 获取预约类型描述
     *
     * @param reservationType 预约类型
     * @return 类型描述
     */
    public String getReservationTypeDesc(String reservationType) {
        if (!StringUtils.hasText(reservationType)) {
            return "未知";
        }

        switch (reservationType) {
            case LogisticsReservationEntity.ReservationType.DELIVERY:
                return "送货";
            case LogisticsReservationEntity.ReservationType.PICKUP:
                return "提货";
            case LogisticsReservationEntity.ReservationType.TRANSFER:
                return "转运";
            default:
                return "未知";
        }
    }

    /**
     * 获取作业类型描述
     *
     * @param operationType 作业类型
     * @return 类型描述
     */
    public String getOperationTypeDesc(String operationType) {
        if (!StringUtils.hasText(operationType)) {
            return "未知";
        }

        switch (operationType) {
            case LogisticsReservationEntity.OperationType.LOADING:
                return "装载";
            case LogisticsReservationEntity.OperationType.UNLOADING:
                return "卸货";
            case LogisticsReservationEntity.OperationType.BOTH:
                return "装卸";
            default:
                return "未知";
        }
    }

    /**
     * 获取预约状态描述
     *
     * @param status 预约状态
     * @return 状态描述
     */
    public String getStatusDesc(String status) {
        if (!StringUtils.hasText(status)) {
            return "未知";
        }

        switch (status) {
            case LogisticsReservationEntity.Status.PENDING:
                return "待审核";
            case LogisticsReservationEntity.Status.APPROVED:
                return "已通过";
            case LogisticsReservationEntity.Status.REJECTED:
                return "已拒绝";
            case LogisticsReservationEntity.Status.CANCELLED:
                return "已取消";
            case LogisticsReservationEntity.Status.COMPLETED:
                return "已完成";
            case LogisticsReservationEntity.Status.EXPIRED:
                return "已过期";
            default:
                return "未知";
        }
    }

    /**
     * 解析扩展属性
     *
     * @param reservation 物流预约实体
     * @return 扩展属性Map
     */
    public Map<String, Object> parseExtendedAttributes(LogisticsReservationEntity reservation) {
        if (reservation == null || !StringUtils.hasText(reservation.getExtendedAttributes())) {
            return new HashMap<>();
        }

        try {
            return OBJECT_MAPPER.readValue(
                    reservation.getExtendedAttributes(),
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            log.debug("[物流预约] 解析扩展属性失败: error={}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 验证预约数据完整性
     *
     * @param reservation 物流预约实体
     * @return 验证结果
     */
    public boolean validateReservation(LogisticsReservationEntity reservation) {
        if (reservation == null) {
            return false;
        }

        // 必填字段验证
        if (!StringUtils.hasText(reservation.getDriverName())) {
            log.warn("[物流预约] 司机姓名不能为空");
            return false;
        }

        if (!StringUtils.hasText(reservation.getPlateNumber())) {
            log.warn("[物流预约] 车牌号不能为空");
            return false;
        }

        if (!StringUtils.hasText(reservation.getContactPhone())) {
            log.warn("[物流预约] 联系电话不能为空");
            return false;
        }

        if (!StringUtils.hasText(reservation.getReservationType())) {
            log.warn("[物流预约] 预约类型不能为空");
            return false;
        }

        if (reservation.getExpectedArriveDate() == null) {
            log.warn("[物流预约] 预计到达日期不能为空");
            return false;
        }

        // 时间窗口验证
        LocalTime startTime = reservation.getExpectedArriveTimeStart();
        LocalTime endTime = reservation.getExpectedArriveTimeEnd();

        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            log.warn("[物流预约] 开始时间不能晚于结束时间: start={}, end={}",
                    startTime, endTime);
            return false;
        }

        // 货物信息验证
        if (!hasCompleteGoodsInfo(reservation)) {
            log.warn("[物流预约] 货物信息不完整");
            return false;
        }

        return true;
    }

    /**
     * 更新预约状态
     *
     * @param reservation 物流预约实体
     * @param status 新状态
     * @param approveUser 审批人
     * @param approveRemark 审批意见
     */
    public void updateStatus(LogisticsReservationEntity reservation, String status,
                              String approveUser, String approveRemark) {
        if (reservation == null) {
            return;
        }

        reservation.setStatus(status);
        reservation.setApproveUser(approveUser);
        reservation.setApproveTime(LocalDateTime.now());
        reservation.setApproveRemark(approveRemark);

        // 更新到数据库
        try {
            logisticsReservationDao.updateById(reservation);
        } catch (Exception e) {
            log.error("[物流预约] 更新预约状态失败: id={}, status={}, error={}",
                    reservation.getReservationId(), status, e.getMessage());
        }
    }

    /**
     * 批量检查过期预约
     *
     * @return 过期的预约数量
     */
    public int checkExpiredReservations() {
        try {
            List<LogisticsReservationEntity> pendingReservations =
                logisticsReservationDao.selectByTimeRange(
                    LocalDate.MIN.atStartOfDay(),
                    LocalDate.now().minusDays(1).atTime(LocalTime.MAX),
                    null, null, 1000);

            int expiredCount = 0;
            for (LogisticsReservationEntity reservation : pendingReservations) {
                if (LogisticsReservationEntity.Status.APPROVED.equals(reservation.getStatus()) ||
                    LogisticsReservationEntity.Status.PENDING.equals(reservation.getStatus())) {

                    updateStatus(reservation, LogisticsReservationEntity.Status.EXPIRED,
                                "系统自动过期", "预约日期已过期");
                    expiredCount++;
                }
            }

            return expiredCount;
        } catch (Exception e) {
            log.error("[物流预约] 检查过期预约失败: error={}", e.getMessage());
            return 0;
        }
    }
}
