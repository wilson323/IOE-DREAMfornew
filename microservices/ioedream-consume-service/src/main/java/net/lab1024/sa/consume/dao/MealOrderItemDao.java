package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.entity.MealOrderItemEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订餐订单明细DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Mapper
public interface MealOrderItemDao extends BaseMapper<MealOrderItemEntity> {

    /**
     * 根据订单ID查询明细列表
     *
     * @param orderId 订单ID
     * @return 明细列表
     */
    @Select("SELECT * FROM t_meal_order_item WHERE order_id = #{orderId}")
    List<MealOrderItemEntity> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单ID删除明细
     *
     * @param orderId 订单ID
     * @return 影响行数
     */
    @Delete("DELETE FROM t_meal_order_item WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);
}
