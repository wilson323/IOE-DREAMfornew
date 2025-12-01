package net.lab1024.sa.admin.module.consume.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;

public interface ConsumeService extends IService<ConsumeRecordEntity> {

    ResponseDTO<String> pay(Long personId, String personName, BigDecimal amount, String payMethod, Long deviceId,
            String remark);

    PageResult<ConsumeRecordEntity> pageRecords(PageParam pageParam, Long personId);

    // 新增的缓存相关方法
    Map<String, Object> processConsume(Map<String, Object> consumeRequest);

    ConsumeRecordEntity getConsumeDetail(Long id);

    Map<String, Object> getConsumeStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    String refundConsume(Long id, String reason);

    List<Map<String, Object>> getAvailableConsumeModes();

    Map<String, Object> getAccountBalance(Long userId);

    String freezeAccount(Long userId, String reason);

    String unfreezeAccount(Long userId, String reason);

    // 新增的管理器集成方法

    /**
     * 验证支付密码
     */
    boolean verifyPaymentPassword(Long userId, String password);

    /**
     * 获取用户安全状态
     */
    Map<String, Object> getUserSecurityStatus(Long userId);

    /**
     * 解锁用户支付密码
     */
    boolean unlockPaymentPassword(Long userId, Long adminUserId);

    /**
     * 获取消费模式配置
     */
    Map<String, Object> getConsumeModeConfig(String modeCode, Long deviceId);

    /**
     * 验证数据一致性
     */
    Map<String, Object> validateDataConsistency(Long userId);

    /**
     * 修复数据不一致
     */
    Map<String, Object> repairDataInconsistency(Long userId);

    /**
     * 获取消费模式引擎统计信息
     */
    Map<String, Object> getEngineStatistics();

    /**
     * 检查引擎健康状态
     */
    Map<String, Object> checkEngineHealth();

    /**
     * 验证消费
     *
     * @param personId    人员ID
     * @param amount      消费金额
     * @param deviceId    设备ID（可选）
     * @param consumeMode 消费模式（可选）
     * @return 验证结果
     */
    Map<String, Object> validateConsume(Long personId, BigDecimal amount, Long deviceId, String consumeMode);

    /**
     * 批量消费
     *
     * @param consumeRequests 消费请求列表
     * @return 消费结果列表
     */
    List<Map<String, Object>> batchConsume(List<Map<String, Object>> consumeRequests);

    /**
     * 导出消费记录
     *
     * @param personId    人员ID（可选）
     * @param orderNo     订单号（可选）
     * @param deviceId    设备ID（可选）
     * @param consumeMode 消费模式（可选）
     * @param status      状态（可选）
     * @param startTime   开始时间（可选）
     * @param endTime     结束时间（可选）
     * @param format      导出格式（可选）
     * @return 导出文件路径
     */
    String exportRecords(Long personId, String orderNo, Long deviceId, String consumeMode,
            String status, LocalDateTime startTime, LocalDateTime endTime, String format);

    /**
     * 获取消费趋势
     *
     * @param timeDimension 时间维度（可选）
     * @param startTime     开始时间（可选）
     * @param endTime       结束时间（可选）
     * @param personId      人员ID（可选）
     * @return 消费趋势数据
     */
    List<Map<String, Object>> getConsumeTrend(String timeDimension, LocalDateTime startTime,
            LocalDateTime endTime, Long personId);

    /**
     * 取消消费
     *
     * @param id     消费记录ID
     * @param reason 取消原因（可选）
     * @return 取消结果消息
     */
    String cancelConsume(Long id, String reason);

    /**
     * 获取消费日志
     *
     * @param id 消费记录ID
     * @return 消费日志列表
     */
    List<Map<String, Object>> getConsumeLogs(Long id);

    /**
     * 同步消费数据
     *
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 同步结果消息
     */
    String syncConsumeData(LocalDateTime startTime, LocalDateTime endTime);
}
