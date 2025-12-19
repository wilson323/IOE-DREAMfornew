package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lab1024.sa.consume.dao.MealOrderDao;
import net.lab1024.sa.consume.dao.MealOrderItemDao;
import net.lab1024.sa.consume.entity.MealOrderEntity;
import net.lab1024.sa.consume.entity.MealOrderItemEntity;

/**
 * 订餐管理器
 * <p>
 * 负责订餐业务的复杂逻辑处理
 * 严格遵循CLAUDE.md规范：
 * - Manager层负责复杂业务逻辑
 * - 保持纯Java特性，不使用Spring注解
 * - 通过Configuration类注册为Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
public class MealOrderManager {

    private static final Logger log = LoggerFactory.getLogger(MealOrderManager.class);

    private final MealOrderDao mealOrderDao;
    private final MealOrderItemDao mealOrderItemDao;
    private final AtomicLong orderSequence = new AtomicLong(0);

    public MealOrderManager(MealOrderDao mealOrderDao, MealOrderItemDao mealOrderItemDao) {
        this.mealOrderDao = mealOrderDao;
        this.mealOrderItemDao = mealOrderItemDao;
    }

    /**
     * 生成订单编号
     * <p>
     * 格式：MO + 年月日时分秒 + 4位序列号
     * 例如：MO202512140830000001
     * </p>
     *
     * @return 订单编号
     */
    public String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        long seq = orderSequence.incrementAndGet() % 10000;
        return String.format("MO%s%04d", dateStr, seq);
    }

    /**
     * 创建订餐订单
     *
     * @param order 订单实体
     * @param items 订单明细列表
     * @return 订单ID
     */
    public Long createOrder(MealOrderEntity order, List<MealOrderItemEntity> items) {
        log.info("[订餐管理] 创建订单，userId={}, areaId={}, mealTypeId={}",
                order.getUserId(), order.getAreaId(), order.getMealTypeId());

        // 生成订单编号
        if (order.getOrderNo() == null) {
            order.setOrderNo(generateOrderNo());
        }

        // 计算订单金额
        BigDecimal totalAmount = calculateTotalAmount(items);
        order.setAmount(totalAmount);
        order.setActualAmount(totalAmount);

        // 设置默认值（状态：1-待支付 2-已支付 3-已取餐 4-已取消）
        order.setStatus(1); // 1-待支付
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(false);

        // 保存订单
        mealOrderDao.insert(order);
        Long orderId = order.getId();

        // 保存订单明细
        if (items != null && !items.isEmpty()) {
            for (MealOrderItemEntity item : items) {
                item.setOrderId(orderId);
                item.setCreateTime(LocalDateTime.now());
                mealOrderItemDao.insert(item);
            }
        }

        log.info("[订餐管理] 订单创建成功，orderId={}, orderNo={}, amount={}",
                orderId, order.getOrderNo(), totalAmount);
        return orderId;
    }

    /**
     * 计算订单总金额
     *
     * @param items 订单明细列表
     * @return 总金额
     */
    private BigDecimal calculateTotalAmount(List<MealOrderItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(item -> {
                    BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                    Integer quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                    return price.multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, (sum, amount) -> sum.add(amount));
    }

    /**
     * 核销订单（取餐）
     *
     * @param orderId      订单ID
     * @param pickupMethod 取餐方式
     * @param deviceId     设备ID
     * @return 是否成功
     */
    public boolean verifyOrder(Long orderId, String pickupMethod, Long deviceId) {
        log.info("[订餐管理] 核销订单，orderId={}, pickupMethod={}, deviceId={}",
                orderId, pickupMethod, deviceId);

        MealOrderEntity order = mealOrderDao.selectById(orderId);
        if (order == null) {
            log.warn("[订餐管理] 订单不存在，orderId={}", orderId);
            return false;
        }

        // 订单状态：1-待支付 2-已支付 3-已取餐 4-已取消
        // 只有状态为1（待支付）的订单才能核销
        Integer orderStatus = order.getStatus();
        if (orderStatus == null || !orderStatus.equals(1)) {
            log.warn("[订餐管理] 订单状态不正确，orderId={}, status={}", orderId, orderStatus);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(order.getPickupStartTime()) || now.isAfter(order.getPickupEndTime())) {
            log.warn("[订餐管理] 不在取餐时间范围内，orderId={}, now={}, start={}, end={}",
                    orderId, now, order.getPickupStartTime(), order.getPickupEndTime());
            return false;
        }

        int rows = mealOrderDao.verifyOrder(orderId, now, deviceId, pickupMethod);
        if (rows > 0) {
            log.info("[订餐管理] 订单核销成功，orderId={}", orderId);
            return true;
        }

        log.warn("[订餐管理] 订单核销失败，orderId={}", orderId);
        return false;
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param reason  取消原因
     * @return 是否成功
     */
    public boolean cancelOrder(Long orderId, String reason) {
        log.info("[订餐管理] 取消订单，orderId={}, reason={}", orderId, reason);

        MealOrderEntity order = mealOrderDao.selectById(orderId);
        if (order == null) {
            log.warn("[订餐管理] 订单不存在，orderId={}", orderId);
            return false;
        }

        if (order.getStatus() == null || order.getStatus() != 1) { // 1-待支付
            log.warn("[订餐管理] 订单状态不正确，无法取消，orderId={}, status={}", orderId, order.getStatus());
            return false;
        }

        int rows = mealOrderDao.cancelOrder(orderId, reason, LocalDateTime.now());
        if (rows > 0) {
            log.info("[订餐管理] 订单取消成功，orderId={}", orderId);
            return true;
        }

        log.warn("[订餐管理] 订单取消失败，orderId={}", orderId);
        return false;
    }

    /**
     * 处理过期订单
     *
     * @return 处理的订单数量
     */
    public int processExpiredOrders() {
        log.info("[订餐管理] 开始处理过期订单");
        LocalDateTime now = LocalDateTime.now();
        int rows = mealOrderDao.batchExpireOrders(now, now);
        log.info("[订餐管理] 过期订单处理完成，处理数量={}", rows);
        return rows;
    }

    /**
     * 获取订餐统计
     *
     * @param areaId     区域ID
     * @param mealTypeId 餐别ID
     * @param orderDate  订餐日期
     * @return 订餐数量
     */
    public int getOrderCount(Long areaId, Long mealTypeId, LocalDateTime orderDate) {
        return mealOrderDao.countByDateAndMealType(areaId, mealTypeId, orderDate);
    }
}
