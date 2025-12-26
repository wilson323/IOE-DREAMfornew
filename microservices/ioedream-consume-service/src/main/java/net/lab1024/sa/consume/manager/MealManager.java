package net.lab1024.sa.consume.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.consume.MealMenuEntity;
import net.lab1024.sa.common.entity.consume.MealOrderEntity;
import net.lab1024.sa.common.entity.consume.MealOrderItemEntity;
import net.lab1024.sa.common.entity.consume.MealInventoryEntity;
import net.lab1024.sa.consume.dao.MealMenuDao;
import net.lab1024.sa.consume.dao.MealOrderDao;
import net.lab1024.sa.consume.dao.MealOrderItemDao;
import net.lab1024.sa.consume.dao.MealInventoryDao;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 订餐管理器 - 业务编排层
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@Component
public class MealManager {

    @Resource
    private MealMenuDao mealMenuDao;

    @Resource
    private MealOrderDao mealOrderDao;

    @Resource
    private MealOrderItemDao mealOrderItemDao;

    @Resource
    private MealInventoryDao mealInventoryDao;

    /**
     * 查询可用菜品列表
     *
     * @param orderDate 订餐日期
     * @param mealType 餐别（1-早餐 2-午餐 3-晚餐）
     * @return 可用菜品列表
     */
    public List<MealMenuEntity> getAvailableMenus(LocalDate orderDate, Integer mealType) {
        log.info("[订餐管理] 查询可用菜品: orderDate={}, mealType={}", orderDate, mealType);

        String dayOfWeek = String.valueOf(orderDate.getDayOfWeek().getValue());

        LambdaQueryWrapper<MealMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MealMenuEntity::getStatus, 1)
                .like(MealMenuEntity::getAvailableDays, dayOfWeek)
                .orderByAsc(MealMenuEntity::getSortOrder);

        return mealMenuDao.selectList(queryWrapper);
    }

    /**
     * 创建订单
     *
     * @param userId 用户ID
     * @param orderDate 订餐日期
     * @param mealType 餐别
     * @param menuIds 菜品ID列表
     * @param quantities 数量列表
     * @return 订单ID
     */
    public Long createOrder(Long userId, LocalDate orderDate, Integer mealType,
                             List<Long> menuIds, List<Integer> quantities) {
        log.info("[订餐管理] 创建订单: userId={}, orderDate={}, mealType={}, menuIds={}",
                userId, orderDate, mealType, menuIds);

        // 1. 查询菜品信息
        List<MealMenuEntity> menus = mealMenuDao.selectBatchIds(menuIds);

        // 2. 计算订单金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < menus.size(); i++) {
            MealMenuEntity menu = menus.get(i);
            Integer quantity = quantities.get(i);
            totalAmount = totalAmount.add(menu.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        // 3. 创建订单
        MealOrderEntity order = new MealOrderEntity();
        order.setUserId(userId);
        order.setOrderDate(orderDate);
        order.setMealType(mealType);
        order.setTotalAmount(totalAmount);
        order.setActualAmount(totalAmount);
        order.setOrderStatus(1); // 待支付
        mealOrderDao.insert(order);

        // 4. 创建订单明细
        for (int i = 0; i < menus.size(); i++) {
            MealMenuEntity menu = menus.get(i);
            Integer quantity = quantities.get(i);

            MealOrderItemEntity item = new MealOrderItemEntity();
            item.setOrderId(order.getOrderId());
            item.setMenuId(menu.getMenuId());
            item.setMenuName(menu.getMenuName());
            item.setMenuCode(menu.getMenuCode());
            item.setMenuImage(menu.getMenuImage());
            item.setUnitPrice(menu.getPrice());
            item.setQuantity(quantity);
            item.setSubtotal(menu.getPrice().multiply(BigDecimal.valueOf(quantity)));

            mealOrderItemDao.insert(item);

            // 5. 更新库存
            updateInventory(menu.getMenuId(), orderDate, mealType, quantity);
        }

        log.info("[订餐管理] 订单创建成功: orderId={}", order.getOrderId());
        return order.getOrderId();
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param cancelReason 取消原因
     */
    public void cancelOrder(Long orderId, String cancelReason) {
        log.info("[订餐管理] 取消订单: orderId={}, reason={}", orderId, cancelReason);

        MealOrderEntity order = mealOrderDao.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        order.setOrderStatus(4); // 已取消
        order.setCancelReason(cancelReason);
        order.setCancelTime(java.time.LocalDateTime.now());

        mealOrderDao.updateById(order);

        log.info("[订餐管理] 订单已取消: orderId={}", orderId);
    }

    /**
     * 更新库存
     *
     * @param menuId 菜品ID
     * @param orderDate 订餐日期
     * @param mealType 餐别
     * @param quantity 数量
     */
    public void updateInventory(Long menuId, LocalDate orderDate, Integer mealType, Integer quantity) {
        log.info("[订餐管理] 更新库存: menuId={}, orderDate={}, mealType={}, quantity={}",
                menuId, orderDate, mealType, quantity);

        LambdaQueryWrapper<MealInventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MealInventoryEntity::getMenuId, menuId)
                .eq(MealInventoryEntity::getInventoryDate, orderDate)
                .eq(MealInventoryEntity::getMealType, mealType);

        MealInventoryEntity inventory = mealInventoryDao.selectOne(queryWrapper);
        if (inventory == null) {
            // 创建新库存记录
            inventory = new MealInventoryEntity();
            inventory.setMenuId(menuId);
            inventory.setInventoryDate(orderDate);
            inventory.setMealType(mealType);
            inventory.setInitialQuantity(quantity);
            inventory.setSoldQuantity(0);
            inventory.setRemainingQuantity(quantity);
            inventory.setStatus(1);
            mealInventoryDao.insert(inventory);
        } else {
            // 更新现有库存
            inventory.setSoldQuantity(inventory.getSoldQuantity() + quantity);
            inventory.setRemainingQuantity(inventory.getRemainingQuantity() - quantity);
            mealInventoryDao.updateById(inventory);
        }

        log.info("[订餐管理] 库存更新成功: remaining={}", inventory.getRemainingQuantity());
    }
}
