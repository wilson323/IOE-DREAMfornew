package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.entity.consume.ConsumeRecordEntity;
import net.lab1024.sa.consume.domain.form.ConsumeRecordQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消费记录DAO
 * <p>
 * 提供消费记录的数据访问操作
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Mapper
public interface ConsumeRecordDao extends BaseMapper<ConsumeRecordEntity> {

    /**
     * 分页查询消费记录
     *
     * @param page 分页对象
     * @param queryForm 查询条件
     * @return 分页结果
     */
    IPage<ConsumeRecordVO> selectPage(Page<?> page, @Param("queryForm") ConsumeRecordQueryForm queryForm);

    /**
     * 根据ID查询消费记录VO
     *
     * @param recordId 记录ID
     * @return 消费记录VO
     */
    @Select("SELECT * FROM v_consume_record" +
            " WHERE record_id = #{recordId}" +
            "   AND deleted_flag = 0")
    ConsumeRecordVO selectRecordById(@Param("recordId") Long recordId);

    /**
     * 根据订单号查询消费记录
     *
     * @param orderNo 订单号
     * @return 消费记录
     */
    @Select("SELECT * FROM t_consume_record" +
            " WHERE order_no = #{orderNo}" +
            "   AND deleted_flag = 0" +
            " LIMIT 1")
    ConsumeRecordEntity selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据交易流水号查询消费记录
     *
     * @param transactionNo 交易流水号
     * @return 消费记录
     */
    @Select("SELECT * FROM t_consume_record" +
            " WHERE transaction_no = #{transactionNo}" +
            "   AND deleted_flag = 0" +
            " LIMIT 1")
    ConsumeRecordEntity selectByTransactionNo(@Param("transactionNo") String transactionNo);

    /**
     * 查询用户的消费记录
     *
     * @param userId 用户ID
     * @return 消费记录列表
     */
    @Select("SELECT * FROM t_consume_record" +
            " WHERE user_id = #{userId}" +
            "   AND deleted_flag = 0" +
            " ORDER BY consume_time DESC")
    List<ConsumeRecordEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询账户的消费记录
     *
     * @param accountId 账户ID
     * @return 消费记录列表
     */
    @Select("SELECT * FROM t_consume_record" +
            " WHERE account_id = #{accountId}" +
            "   AND deleted_flag = 0" +
            " ORDER BY consume_time DESC")
    List<ConsumeRecordEntity> selectByAccountId(@Param("accountId") Long accountId);

    /**
     * 查询待同步的离线消费记录
     *
     * @param limit 限制数量
     * @return 待同步的消费记录列表
     */
    @Select("SELECT * FROM t_consume_record" +
            " WHERE offline_flag = 1" +
            "   AND sync_status = 0" +
            "   AND deleted_flag = 0" +
            " ORDER BY consume_time ASC" +
            " LIMIT #{limit}")
    List<ConsumeRecordEntity> selectPendingSyncRecords(@Param("limit") int limit);

    /**
     * 查询指定时间范围的消费记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费记录列表
     */
    @Select("SELECT * FROM t_consume_record" +
            " WHERE consume_time >= #{startTime}" +
            "   AND consume_time <= #{endTime}" +
            "   AND deleted_flag = 0" +
            " ORDER BY consume_time DESC")
    List<ConsumeRecordEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户总消费金额
     *
     * @param userId 用户ID
     * @return 总消费金额
     */
    @Select("SELECT SUM(amount) FROM t_consume_record" +
            " WHERE user_id = #{userId}" +
            "   AND transaction_status = 1" +
            "   AND deleted_flag = 0")
    java.math.BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    /**
     * 统计账户总消费金额
     *
     * @param accountId 账户ID
     * @return 总消费金额
     */
    @Select("SELECT SUM(amount) FROM t_consume_record" +
            " WHERE account_id = #{accountId}" +
            "   AND transaction_status = 1" +
            "   AND deleted_flag = 0")
    java.math.BigDecimal sumAmountByAccountId(@Param("accountId") Long accountId);

    /**
     * 查询已退款的消费记录
     *
     * @param userId 用户ID
     * @return 已退款记录列表
     */
    @Select("SELECT * FROM t_consume_record" +
            " WHERE user_id = #{userId}" +
            "   AND refund_status > 0" +
            "   AND deleted_flag = 0" +
            " ORDER BY refund_time DESC")
    List<ConsumeRecordEntity> selectRefundedRecords(@Param("userId") Long userId);

    /**
     * 统计消费记录数量（按支付方式）
     *
     * @param paymentMethod 支付方式
     * @return 记录数量
     */
    @Select("SELECT COUNT(*) FROM t_consume_record" +
            " WHERE payment_method = #{paymentMethod}" +
            "   AND deleted_flag = 0")
    int countByPaymentMethod(@Param("paymentMethod") String paymentMethod);

    /**
     * 查询设备的消费记录
     *
     * @param deviceId 设备ID
     * @return 消费记录列表
     */
    @Select("SELECT * FROM t_consume_record" +
            " WHERE device_id = #{deviceId}" +
            "   AND deleted_flag = 0" +
            " ORDER BY consume_time DESC")
    List<ConsumeRecordEntity> selectByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 查询用户指定时间范围的消费记录（用于导出）
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费记录列表
     */
    @Select("SELECT * FROM t_consume_record" +
            " WHERE user_id = #{userId}" +
            "   AND create_time >= #{startTime}" +
            "   AND create_time <= #{endTime}" +
            "   AND deleted_flag = 0" +
            " ORDER BY create_time DESC")
    List<ConsumeRecordEntity> selectRecordsByUserAndTime(@Param("userId") Long userId,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);
}
