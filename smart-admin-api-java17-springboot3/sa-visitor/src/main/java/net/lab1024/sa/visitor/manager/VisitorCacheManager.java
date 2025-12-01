package net.lab1024.sa.visitor.manager;

import net.lab1024.sa.visitor.domain.entity.VisitorReservationEntity;
import net.lab1024.sa.visitor.domain.entity.VisitorRecordEntity;
import net.lab1024.sa.base.common.cache.BaseCacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 访客缓存管理器
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseCacheManager
 * - 统一缓存Key管理
 * - 合理的缓存过期时间
 * - 支持多级缓存
 *
 * @author IOE-DREAM Team
 * @version 3.0.0
 * @since 2025-11-27
 */
@Component
public class VisitorCacheManager extends BaseCacheManager {

    // 缓存Key前缀
    private static final String PREFIX = "visitor:";
    private static final String RESERVATION_PREFIX = PREFIX + "reservation:";
    private static final String RECORD_PREFIX = PREFIX + "record:";
    private static final String BLACKLIST_PREFIX = PREFIX + "blacklist:";

    // ====================== 预约缓存 ======================

    /**
     * 获取访客预约缓存
     *
     * @param reservationId 预约ID
     * @return 访客预约信息
     */
    public VisitorReservationEntity getReservation(Long reservationId) {
        Object value = get(RESERVATION_PREFIX + reservationId);
        return value != null ? (VisitorReservationEntity) value : null;
    }

    /**
     * 设置访客预约缓存
     *
     * @param reservation 访客预约信息
     */
    public void putReservation(VisitorReservationEntity reservation) {
        String cacheKey = RESERVATION_PREFIX + reservation.getReservationId();
        set(cacheKey, reservation, 30, TimeUnit.MINUTES);
    }

    /**
     * 清除访客预约缓存
     *
     * @param reservationId 预约ID
     */
    public void evictReservationCache(Long reservationId) {
        delete(RESERVATION_PREFIX + reservationId);
    }

    /**
     * 获取访客预约（通过预约编号）
     *
     * @param reservationCode 预约编号
     * @return 访客预约信息
     */
    public VisitorReservationEntity getReservationByCode(String reservationCode) {
        Object value = get(RESERVATION_PREFIX + "code:" + reservationCode);
        return value != null ? (VisitorReservationEntity) value : null;
    }

    /**
     * 设置访客预约（通过预约编号）
     *
     * @param reservationCode 预约编号
     * @param reservation     访客预约信息
     */
    public void putReservationByCode(String reservationCode, VisitorReservationEntity reservation) {
        String cacheKey = RESERVATION_PREFIX + "code:" + reservationCode;
        set(cacheKey, reservation, 30, TimeUnit.MINUTES);
    }

    /**
     * 清除访客预约（通过预约编号）
     *
     * @param reservationCode 预约编号
     */
    public void evictReservationByCode(String reservationCode) {
        delete(RESERVATION_PREFIX + "code:" + reservationCode);
    }

    /**
     * 获取待审批预约数量
     *
     * @return 待审批数量
     */
    public Integer getPendingApprovalCount() {
        Object value = get(RESERVATION_PREFIX + "pending:count");
        return value != null ? (Integer) value : 0;
    }

    /**
     * 设置待审批预约数量
     *
     * @param count 待审批数量
     */
    public void putPendingApprovalCount(Integer count) {
        set(RESERVATION_PREFIX + "pending:count", count, 5, TimeUnit.MINUTES);
    }

    // ====================== 访问记录缓存 ======================

    /**
     * 获取今日访问记录列表
     *
     * @return 今日访问记录列表
     */
    @SuppressWarnings("unchecked")
    public List<VisitorRecordEntity> getTodayAccessRecords() {
        Object value = get(RECORD_PREFIX + "today");
        return value != null ? (List<VisitorRecordEntity>) value : null;
    }

    /**
     * 设置今日访问记录列表
     *
     * @param records 今日访问记录列表
     */
    public void putTodayAccessRecords(List<VisitorRecordEntity> records) {
        set(RECORD_PREFIX + "today", records, 10, TimeUnit.MINUTES);
    }

    /**
     * 清除今日访问记录缓存
     */
    public void evictTodayAccessRecords() {
        delete(RECORD_PREFIX + "today");
    }

    /**
     * 获取访客访问记录列表
     *
     * @param visitorPhone 访客手机号
     * @return 访问记录列表
     */
    @SuppressWarnings("unchecked")
    public List<VisitorRecordEntity> getVisitorRecords(String visitorPhone) {
        Object value = get(RECORD_PREFIX + "visitor:" + visitorPhone);
        return value != null ? (List<VisitorRecordEntity>) value : null;
    }

    /**
     * 设置访客访问记录列表
     *
     * @param visitorPhone 访客手机号
     * @param records      访问记录列表
     */
    public void putVisitorRecords(String visitorPhone, List<VisitorRecordEntity> records) {
        set(RECORD_PREFIX + "visitor:" + visitorPhone, records, 15, TimeUnit.MINUTES);
    }

    /**
     * 清除访客访问记录缓存
     *
     * @param visitorPhone 访客手机号
     */
    public void evictVisitorRecords(String visitorPhone) {
        delete(RECORD_PREFIX + "visitor:" + visitorPhone);
    }

    /**
     * 清除记录缓存
     *
     * @param recordId 记录ID
     */
    public void evictRecordCache(Long recordId) {
        if (recordId != null) {
            delete(RECORD_PREFIX + "id:" + recordId);
        }
        // 清除今日记录缓存
        delete(RECORD_PREFIX + "today");
    }

    // ====================== 黑名单缓存 ======================

    /**
     * 检查访客是否在黑名单中
     *
     * @param visitorPhone 访客手机号
     * @return 是否在黑名单中
     */
    public Boolean isVisitorBlacklisted(String visitorPhone) {
        Object value = get(BLACKLIST_PREFIX + visitorPhone);
        return value != null ? (Boolean) value : false;
    }

    /**
     * 设置访客黑名单状态
     *
     * @param visitorPhone 访客手机号
     * @param blacklisted  是否在黑名单中
     */
    public void putVisitorBlacklistStatus(String visitorPhone, Boolean blacklisted) {
        set(BLACKLIST_PREFIX + visitorPhone, blacklisted, 30, TimeUnit.MINUTES);
    }

    /**
     * 清除访客黑名单状态缓存
     *
     * @param visitorPhone 访客手机号
     */
    public void evictVisitorBlacklistStatus(String visitorPhone) {
        delete(BLACKLIST_PREFIX + visitorPhone);
    }

    // ====================== 批量操作 ======================

    /**
     * 批量清除预约相关缓存
     *
     * @param reservationId 预约ID
     * @param reservationCode 预约编号
     * @param visitorPhone    访客手机号
     */
    public void evictReservationRelatedCache(Long reservationId, String reservationCode, String visitorPhone) {
        evictReservationCache(reservationId);
        evictReservationByCode(reservationCode);
        evictVisitorRecords(visitorPhone);
        delete(RESERVATION_PREFIX + "pending:count"); // 清除待审批数量缓存
    }

    /**
     * 批量清除访问记录相关缓存
     *
     * @param visitorPhone 访客手机号
     * @param accessDate   访问日期
     */
    public void evictAccessRecordRelatedCache(String visitorPhone, String accessDate) {
        evictVisitorRecords(visitorPhone);
        evictTodayAccessRecords();
        delete(RECORD_PREFIX + "statistics:" + accessDate);
    }

    /**
     * 清除所有访客相关缓存
     */
    public void evictAllVisitorCache() {
        delete(RESERVATION_PREFIX + "pending:count");
        delete(RECORD_PREFIX + "today");
        delete(BLACKLIST_PREFIX + "all");
    }
}