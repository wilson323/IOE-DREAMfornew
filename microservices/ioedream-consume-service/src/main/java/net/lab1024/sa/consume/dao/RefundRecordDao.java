package net.lab1024.sa.consume.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.consume.domain.entity.RefundRecordEntity;

/**
 * 退款记录DAO接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Mapper
public interface RefundRecordDao extends BaseMapper<RefundRecordEntity> {

    /**
     * 根据退款单号查询退款记录
     *
     * @param refundId 退款单号
     * @return 退款记录
     */
    @Select("SELECT * FROM refund_record WHERE refund_id = #{refundId} AND deleted = 0")
    RefundRecordEntity selectByRefundId(@Param("refundId") String refundId);

    /**
     * 根据状态统计退款记录数量
     *
     * @param status 状态
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM refund_record WHERE status = #{status} AND deleted = 0")
    Long countByStatus(@Param("status") String status);

    /**
     * 根据用户ID查询退款记录
     *
     * @param userId 用户ID
     * @return 退款记录列表
     */
    @Select("SELECT * FROM refund_record WHERE person_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<RefundRecordEntity> selectByUserId(@Param("userId") Long userId);
}