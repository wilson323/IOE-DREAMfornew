package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.OrderingItemEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单项 DAO
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Mapper
public interface OrderingItemDao extends BaseMapper<OrderingItemEntity> {

    /**
     * 根据订单ID查询订单项
     *
     * @param orderingId 订单ID
     * @return 订单项列表
     */
    List<OrderingItemEntity> selectByOrderingId(@Param("orderingId") Long orderingId);

    /**
     * 根据菜单ID查询订单项
     *
     * @param menuId 菜单ID
     * @return 订单项列表
     */
    List<OrderingItemEntity> selectByMenuId(@Param("menuId") Long menuId);
}