package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeRecordQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeRecordAddForm;
import net.lab1024.sa.consume.domain.vo.ConsumeRecordVO;
import net.lab1024.sa.consume.domain.vo.ConsumeStatisticsVO;

/**
 * 消费记录服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public interface ConsumeRecordService {

    /**
     * 分页查询消费记录
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<ConsumeRecordVO> queryRecords(ConsumeRecordQueryForm queryForm);

    /**
     * 获取消费记录详情
     *
     * @param recordId 记录ID
     * @return 记录详情
     */
    ConsumeRecordVO getRecordDetail(Long recordId);

    /**
     * 创建消费记录
     *
     * @param addForm 消费表单
     * @return 记录ID
     */
    Long addRecord(ConsumeRecordAddForm addForm);

    /**
     * 获取今日消费记录
     *
     * @param userId 用户ID（可选）
     * @return 今日消费记录
     */
    List<ConsumeRecordVO> getTodayRecords(Long userId);

    /**
     * 获取消费统计信息
     *
     * @param userId 用户ID（可选）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    ConsumeStatisticsVO getStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取消费趋势数据
     *
     * @param userId 用户ID（可选）
     * @param days 天数
     * @return 趋势数据
     */
    java.util.Map<String, Object> getConsumeTrend(Long userId, Integer days);

    /**
     * 导出消费记录
     *
     * @param queryForm 查询条件
     * @return 导出URL
     */
    String exportRecords(ConsumeRecordQueryForm queryForm);

    /**
     * 撤销消费记录
     *
     * @param recordId 记录ID
     * @param reason 撤销原因
     */
    void cancelRecord(Long recordId, String reason);

    /**
     * 退款处理
     *
     * @param recordId 记录ID
     * @param refundAmount 退款金额
     * @param reason 退款原因
     */
    void refundRecord(Long recordId, BigDecimal refundAmount, String reason);
}