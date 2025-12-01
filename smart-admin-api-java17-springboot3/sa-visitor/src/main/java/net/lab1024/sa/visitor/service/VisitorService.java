package net.lab1024.sa.visitor.service;

import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.visitor.domain.entity.VisitorReservationEntity;
import net.lab1024.sa.visitor.domain.entity.VisitorRecordEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 访客服务接口
 * <p>
 * 严格遵循repowiki规范：
 * - 定义完整的业务方法
 * - 使用ResponseDTO统一返回格式
 * - 提供分页查询接口
 * - 包含业务验证方法
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
public interface VisitorService {

    // ====================== 预约管理 ======================

    /**
     * 创建访客预约
     *
     * @param reservation 预约信息
     * @return 预约结果
     */
    ResponseDTO<String> createReservation(VisitorReservationEntity reservation);

    /**
     * 更新访客预约
     *
     * @param reservation 预约信息
     * @return 更新结果
     */
    ResponseDTO<String> updateReservation(VisitorReservationEntity reservation);

    /**
     * 删除访客预约
     *
     * @param reservationId 预约ID
     * @return 删除结果
     */
    ResponseDTO<String> deleteReservation(Long reservationId);

    /**
     * 分页查询访客预约
     *
     * @param pageParam 分页参数
     * @param keyword   关键字（访客姓名、手机号）
     * @param approvalStatus 审批状态
     * @param visitDate  访问日期
     * @return 分页结果
     */
    ResponseDTO<PageResult<VisitorReservationEntity>> getReservationPage(PageParam pageParam,
                                                                         String keyword,
                                                                         Integer approvalStatus,
                                                                         LocalDate visitDate);

    /**
     * 根据ID查询访客预约
     *
     * @param reservationId 预约ID
     * @return 预约信息
     */
    ResponseDTO<VisitorReservationEntity> getReservationById(Long reservationId);

    /**
     * 根据预约编号查询访客预约
     *
     * @param reservationCode 预约编号
     * @return 预约信息
     */
    ResponseDTO<VisitorReservationEntity> getReservationByCode(String reservationCode);

    /**
     * 审批访客预约
     *
     * @param reservationId   预约ID
     * @param approvalStatus  审批状态
     * @param approvalComment 审批意见
     * @param approverId      审批人ID
     * @param approverName    审批人姓名
     * @return 审批结果
     */
    ResponseDTO<String> approveReservation(Long reservationId,
                                           Integer approvalStatus,
                                           String approvalComment,
                                           Long approverId,
                                           String approverName);

    /**
     * 生成访客二维码
     *
     * @param reservationId 预约ID
     * @return 二维码信息
     */
    ResponseDTO<String> generateQrCode(Long reservationId);

    /**
     * 验证访客二维码
     *
     * @param qrCode 二维码内容
     * @return 验证结果
     */
    ResponseDTO<VisitorReservationEntity> validateQrCode(String qrCode);

    /**
     * 取消预约
     *
     * @param reservationId 预约ID
     * @param cancelReason   取消原因
     * @return 取消结果
     */
    ResponseDTO<String> cancelReservation(Long reservationId, String cancelReason);

    // ====================== 访问记录管理 ======================

    /**
     * 创建访问记录
     *
     * @param record 访问记录
     * @return 创建结果
     */
    ResponseDTO<String> createAccessRecord(VisitorRecordEntity record);

    /**
     * 分页查询访问记录
     *
     * @param pageParam    分页参数
     * @param keyword      关键字（访客姓名、手机号）
     * @param visitAreaId  访问区域ID
     * @param accessResult 通行结果
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 分页结果
     */
    ResponseDTO<PageResult<VisitorRecordEntity>> getAccessRecordPage(PageParam pageParam,
                                                                    String keyword,
                                                                    Long visitAreaId,
                                                                    Integer accessResult,
                                                                    LocalDate startDate,
                                                                    LocalDate endDate);

    /**
     * 根据访客手机号查询访问记录
     *
     * @param visitorPhone 访客手机号
     * @param limit        查询数量限制
     * @return 访问记录列表
     */
    ResponseDTO<List<VisitorRecordEntity>> getAccessRecordsByPhone(String visitorPhone, Integer limit);

    /**
     * 根据预约ID查询访问记录
     *
     * @param reservationId 预约ID
     * @return 访问记录列表
     */
    ResponseDTO<List<VisitorRecordEntity>> getAccessRecordsByReservation(Long reservationId);

    /**
     * 查询今日访问记录
     *
     * @return 今日访问记录列表
     */
    ResponseDTO<List<VisitorRecordEntity>> getTodayAccessRecords();

    // ====================== 统计分析 ======================

    /**
     * 获取访客统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据
     */
    ResponseDTO<Map<String, Object>> getVisitorStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取访问趋势数据
     *
     * @param days 天数
     * @return 趋势数据
     */
    ResponseDTO<List<Map<String, Object>>> getAccessTrend(Integer days);

    /**
     * 获取区域访问统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 区域统计
     */
    ResponseDTO<List<Map<String, Object>>> getAreaAccessStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取设备使用统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 设备统计
     */
    ResponseDTO<List<Map<String, Object>>> getDeviceUsageStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取异常访问数据
     *
     * @param timeWindow 时间窗口（分钟）
     * @param threshold  阈值
     * @return 异常数据
     */
    ResponseDTO<List<Map<String, Object>>> getAbnormalAccessData(Integer timeWindow, Integer threshold);

    // ====================== 业务验证 ======================

    /**
     * 验证访客访问权限
     *
     * @param reservationId 预约ID
     * @param deviceId       设备ID
     * @param areaId         区域ID
     * @return 验证结果
     */
    ResponseDTO<Map<String, Object>> validateAccessPermission(Long reservationId, Long deviceId, Long areaId);

    /**
     * 检查访客黑名单
     *
     * @param visitorName  访客姓名
     * @param visitorPhone 访客手机号
     * @param visitorIdCard 访客身份证号
     * @return 检查结果
     */
    ResponseDTO<Map<String, Object>> checkVisitorBlacklist(String visitorName, String visitorPhone, String visitorIdCard);

    /**
     * 检查重复预约
     *
     * @param visitorPhone 访客手机号
     * @param visitDate    访问日期
     * @param excludeId    排除的预约ID
     * @return 检查结果
     */
    ResponseDTO<Boolean> checkDuplicateReservation(String visitorPhone, LocalDate visitDate, Long excludeId);

    /**
     * 检查访问时间是否有效
     *
     * @param reservationId 预约ID
     * @return 检查结果
     */
    ResponseDTO<Boolean> checkAccessTimeValid(Long reservationId);

    // ====================== 定时任务 ======================

    /**
     * 处理过期预约
     *
     * @return 处理结果
     */
    ResponseDTO<String> processExpiredReservations();

    /**
     * 处理即将过期的二维码
     *
     * @return 处理结果
     */
    ResponseDTO<String> processExpiringQrCodes();

    /**
     * 清理历史访问记录
     *
     * @param beforeDays 天数
     * @return 清理结果
     */
    ResponseDTO<String> cleanHistoryRecords(Integer beforeDays);

    // ====================== 导出功能 ======================

    /**
     * 导出访客预约数据
     *
     * @param startDate      开始日期
     * @param endDate        结束日期
     * @param approvalStatus 审批状态
     * @return 导出文件路径
     */
    ResponseDTO<String> exportReservationData(LocalDate startDate, LocalDate endDate, Integer approvalStatus);

    /**
     * 导出访问记录数据
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param visitAreaId  访问区域ID
     * @param accessResult 通行结果
     * @return 导出文件路径
     */
    ResponseDTO<String> exportAccessRecordData(LocalDate startDate, LocalDate endDate, Long visitAreaId, Integer accessResult);
}