package net.lab1024.sa.consume.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeRechargeAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeRechargeCreateForm;
import net.lab1024.sa.consume.domain.form.ConsumeRechargeQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeRechargeOrderVO;
import net.lab1024.sa.consume.domain.vo.ConsumeRechargeRecordVO;
import net.lab1024.sa.consume.domain.vo.ConsumeRechargeStatisticsVO;

/**
 * 充值服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
public interface ConsumeRechargeService {

    /**
     * 分页查询充值记录
     *
     * @param queryForm 查询表单
     * @return 充值记录分页结果
     */
    PageResult<ConsumeRechargeRecordVO> queryRechargeRecords(ConsumeRechargeQueryForm queryForm);

    /**
     * 获取充值记录详情
     *
     * @param recordId 记录ID
     * @return 充值记录详情
     */
    ConsumeRechargeRecordVO getRechargeRecordDetail(Long recordId);

    /**
     * 添加充值记录
     *
     * @param addForm 充值表单
     * @return 记录ID
     */
    Long addRechargeRecord(ConsumeRechargeAddForm addForm);

    /**
     * 获取用户充值记录
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 充值记录分页结果
     */
    PageResult<ConsumeRechargeRecordVO> getUserRechargeRecords(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取今日充值记录
     *
     * @return 今日充值记录列表
     */
    List<ConsumeRechargeRecordVO> getTodayRechargeRecords();

    /**
     * 获取充值统计信息
     *
     * @param userId     用户ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 统计信息
     */
    ConsumeRechargeStatisticsVO getRechargeStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取充值方式统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计信息
     */
    Map<String, Object> getRechargeMethodStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取充值趋势
     *
     * @param days 天数
     * @return 趋势数据
     */
    Map<String, Object> getRechargeTrend(Integer days);

    /**
     * 导出充值记录
     *
     * @param queryForm 查询表单
     * @return 导出文件URL
     */
    String exportRechargeRecords(ConsumeRechargeQueryForm queryForm);

    /**
     * 批量充值
     *
     * @param userIds    用户ID列表
     * @param amount     充值金额
     * @param rechargeWay 充值方式
     * @param remark     备注
     * @return 充值结果
     */
    Map<String, Object> batchRecharge(List<Long> userIds, java.math.BigDecimal amount, String rechargeWay, String remark);

    /**
     * 核销充值记录
     *
     * @param recordId 记录ID
     * @return 是否成功
     */
    Boolean verifyRechargeRecord(Long recordId);

    /**
     * 冲正充值记录
     *
     * @param recordId 记录ID
     * @param reason   冲正原因
     */
    void reverseRechargeRecord(Long recordId, String reason);

    /**
     * 获取充值统计信息（旧接口，保持兼容）
     *
     * @param userId 用户ID
     * @return 统计信息
     * @deprecated 使用 {@link #getRechargeStatistics(Long, LocalDateTime, LocalDateTime)} 替代
     */
    @Deprecated
    Map<String, Object> getRechargeStatistics(Long userId);

    // ==================== 移动端API方法 ====================

    /**
     * 创建充值订单
     *
     * @param form 充值创建表单
     * @return 订单信息
     */
    Map<String, Object> createRechargeOrder(ConsumeRechargeCreateForm form);

    /**
     * 获取支付结果
     *
     * @param orderId 订单ID
     * @return 支付结果
     */
    Map<String, Object> getPaymentResult(String orderId);

    /**
     * 取消充值订单
     *
     * @param orderId 订单ID
     */
    void cancelRechargeOrder(String orderId);

    /**
     * 获取充值订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    ConsumeRechargeOrderVO getRechargeOrderDetail(String orderId);

    /**
     * 获取用户充值记录（移动端）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 充值记录
     */
    Map<String, Object> getRechargeRecords(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 处理微信支付回调
     *
     * @param notifyData 回调数据
     * @return 处理结果
     */
    boolean handleWechatPayCallback(String notifyData);

    /**
     * 处理支付宝回调
     *
     * @param notifyData 回调数据
     * @return 处理结果
     */
    boolean handleAlipayCallback(String notifyData);
}
