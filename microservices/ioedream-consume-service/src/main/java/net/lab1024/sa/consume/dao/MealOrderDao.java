package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.consume.entity.MealOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订餐订单DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Mapper
public interface MealOrderDao extends BaseMapper<MealOrderEntity> {

    /**
     * 根据用户ID查询订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    @Select("<script>" +
            "SELECT * FROM t_meal_order WHERE user_id = #{userId} AND deleted = 0 " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "ORDER BY create_time DESC" +
            "</script>")
    List<MealOrderEntity> selectByUserId(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 根据订单编号查询订单
     *
     * @param orderNo 订单编号
     * @return 订单实体
     */
    @Select("SELECT * FROM t_meal_order WHERE order_no = #{orderNo} AND deleted = 0")
    MealOrderEntity selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 更新订单状态
     *
     * @param orderId 订单ID
     * @param status 新状态
     * @param updateTime 更新时间
     * @return 影响行数
     */
    @Update("UPDATE t_meal_order SET status = #{status}, update_time = #{updateTime} WHERE id = #{orderId}")
    int updateStatus(@Param("orderId") Long orderId, @Param("status") String status, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 核销订单（取餐）
     *
     * @param orderId 订单ID
     * @param pickupTime 取餐时间
     * @param pickupDeviceId 取餐设备ID
     * @param pickupMethod 取餐方式
     * @return 影响行数
     */
    @Update("UPDATE t_meal_order SET status = 'COMPLETED', pickup_time = #{pickupTime}, " +
            "pickup_device_id = #{pickupDeviceId}, pickup_method = #{pickupMethod}, update_time = #{pickupTime} " +
            "WHERE id = #{orderId} AND status = 'PENDING'")
    int verifyOrder(@Param("orderId") Long orderId, @Param("pickupTime") LocalDateTime pickupTime,
                    @Param("pickupDeviceId") Long pickupDeviceId, @Param("pickupMethod") String pickupMethod);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param cancelReason 取消原因
     * @param cancelTime 取消时间
     * @return 影响行数
     */
    @Update("UPDATE t_meal_order SET status = 'CANCELLED', cancel_reason = #{cancelReason}, " +
            "cancel_time = #{cancelTime}, update_time = #{cancelTime} WHERE id = #{orderId} AND status = 'PENDING'")
    int cancelOrder(@Param("orderId") Long orderId, @Param("cancelReason") String cancelReason,
                    @Param("cancelTime") LocalDateTime cancelTime);

    /**
     * 统计指定日期的订餐数量
     *
     * @param areaId 区域ID
     * @param mealTypeId 餐别ID
     * @param orderDate 订餐日期
     * @return 订餐数量
     */
    @Select("SELECT COUNT(*) FROM t_meal_order WHERE area_id = #{areaId} AND meal_type_id = #{mealTypeId} " +
            "AND DATE(order_date) = DATE(#{orderDate}) AND status != 'CANCELLED' AND deleted = 0")
    int countByDateAndMealType(@Param("areaId") Long areaId, @Param("mealTypeId") Long mealTypeId,
                               @Param("orderDate") LocalDateTime orderDate);

    /**
     * 查询过期未取餐的订单
     *
     * @param expireTime 过期时间
     * @return 过期订单列表
     */
    @Select("SELECT * FROM t_meal_order WHERE status = 'PENDING' AND pickup_end_time < #{expireTime} AND deleted = 0")
    List<MealOrderEntity> selectExpiredOrders(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 批量更新过期订单状态
     *
     * @param expireTime 过期时间
     * @param updateTime 更新时间
     * @return 影响行数
     */
    @Update("UPDATE t_meal_order SET status = 'EXPIRED', update_time = #{updateTime} " +
            "WHERE status = 'PENDING' AND pickup_end_time < #{expireTime}")
    int batchExpireOrders(@Param("expireTime") LocalDateTime expireTime, @Param("updateTime") LocalDateTime updateTime);
}
