package net.lab1024.sa.consume.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.consume.MealMenuEntity;
import net.lab1024.sa.common.entity.consume.MealOrderEntity;
import net.lab1024.sa.common.entity.consume.MealOrderItemEntity;
import net.lab1024.sa.consume.dao.MealOrderDao;
import net.lab1024.sa.consume.dao.MealOrderItemDao;
import net.lab1024.sa.consume.dao.MealMenuDao;
import net.lab1024.sa.consume.manager.MealManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 订餐订单服务 - 订单管理服务层
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@Service
public class MealOrderService extends ServiceImpl<MealOrderDao, MealOrderEntity> {

    @Resource
    private MealOrderDao mealOrderDao;

    @Resource
    private MealOrderItemDao mealOrderItemDao;

    @Resource
    private MealManager mealManager;

    @Resource
    private MealMenuDao mealMenuDao;

    /**
     * 创建订单
     *
     * @param userId 用户ID
     * @param orderDate 订餐日期
     * @param mealType 餐别（1-早餐 2-午餐 3-晚餐）
     * @param menuIds 菜品ID列表
     * @param quantities 数量列表
     * @return 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long userId, LocalDate orderDate, Integer mealType,
                             List<Long> menuIds, List<Integer> quantities) {
        log.info("[订单服务] 创建订单: userId={}, orderDate={}, mealType={}", userId, orderDate, mealType);

        // 生成订单号
        String orderNo = "MEAL" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

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
        order.setOrderNo(orderNo);
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
            mealManager.updateInventory(menu.getMenuId(), orderDate, mealType, quantity);
        }

        log.info("[订单服务] 订单创建成功: orderId={}, orderNo={}", order.getOrderId(), orderNo);
        return order.getOrderId();
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param cancelReason 取消原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String cancelReason) {
        log.info("[订单服务] 取消订单: orderId={}, reason={}", orderId, cancelReason);

        MealOrderEntity order = mealOrderDao.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 只有待支付和已支付的订单可以取消
        if (order.getOrderStatus() != 1 && order.getOrderStatus() != 2) {
            throw new RuntimeException("订单状态不允许取消");
        }

        mealManager.cancelOrder(orderId, cancelReason);

        // 恢复库存
        LambdaQueryWrapper<MealOrderItemEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MealOrderItemEntity::getOrderId, orderId);
        List<MealOrderItemEntity> items = mealOrderItemDao.selectList(queryWrapper);

        for (MealOrderItemEntity item : items) {
            mealManager.updateInventory(item.getMenuId(), order.getOrderDate(),
                    order.getMealType(), -item.getQuantity());
        }

        log.info("[订单服务] 订单已取消: orderId={}", orderId);
    }

    /**
     * 支付订单
     *
     * @param orderId 订单ID
     * @param paymentMethod 支付方式（balance-余额 wechat-微信 alipay-支付宝）
     */
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, String paymentMethod) {
        log.info("[订单服务] 支付订单: orderId={}, paymentMethod={}", orderId, paymentMethod);

        MealOrderEntity order = mealOrderDao.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getOrderStatus() != 1) {
            throw new RuntimeException("订单状态不允许支付");
        }

        // TODO: 扣除余额（这里应该调用账户服务）
        // AccountService.deduct(userId, order.getActualAmount());

        order.setOrderStatus(2); // 已支付
        order.setPaymentStatus(1); // 已支付
        order.setPaymentTime(java.time.LocalDateTime.now());
        order.setPaymentMethod(paymentMethod);

        mealOrderDao.updateById(order);

        log.info("[订单服务] 订单已支付: orderId={}", orderId);
    }

    /**
     * 查询订单列表（分页）
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 订单分页数据
     */
    public Page<MealOrderEntity> queryOrders(Long userId, LocalDate startDate, LocalDate endDate,
                                               Integer pageNum, Integer pageSize) {
        log.info("[订单服务] 查询订单列表: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        LambdaQueryWrapper<MealOrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MealOrderEntity::getUserId, userId)
                .ge(MealOrderEntity::getOrderDate, startDate)
                .le(MealOrderEntity::getOrderDate, endDate)
                .orderByDesc(MealOrderEntity::getCreateTime);

        return this.page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    /**
     * 查询订单详情
     *
     * @param orderId 订单ID
     * @return 订单实体
     */
    public MealOrderEntity getOrderDetail(Long orderId) {
        log.info("[订单服务] 查询订单详情: orderId={}", orderId);
        return mealOrderDao.selectById(orderId);
    }

    /**
     * 查询订单明细
     *
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    public List<MealOrderItemEntity> getOrderItems(Long orderId) {
        log.info("[订单服务] 查询订单明细: orderId={}", orderId);

        LambdaQueryWrapper<MealOrderItemEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MealOrderItemEntity::getOrderId, orderId);

        return mealOrderItemDao.selectList(queryWrapper);
    }
}
