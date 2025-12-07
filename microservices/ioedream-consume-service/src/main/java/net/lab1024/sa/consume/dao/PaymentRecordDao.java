package net.lab1024.sa.consume.dao;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.consume.domain.entity.PaymentRecordEntity;

/**
 * 支付记录DAO接口
 * <p>
 * 用于支付记录的数据访问操作
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解标识
 * - 继承BaseMapper<Entity>
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@org.apache.ibatis.annotations.Mapper
public interface PaymentRecordDao extends BaseMapper<PaymentRecordEntity> {

    /**
     * 根据支付订单号查询支付记录
     *
     * @param paymentId 支付订单号
     * @return 支付记录
     */
    PaymentRecordEntity selectByPaymentId(@Param("paymentId") String paymentId);

    /**
     * 根据支付记录ID查询支付记录
     *
     * @param id 支付记录ID
     * @return 支付记录
     */
    PaymentRecordEntity selectById(@Param("id") Long id);

    /**
     * 根据用户ID查询支付记录列表
     *
     * @param userId 用户ID
     * @return 支付记录列表
     */
    java.util.List<PaymentRecordEntity> selectByUserId(@Param("userId") Long userId);
}

