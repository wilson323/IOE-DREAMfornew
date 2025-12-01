package net.lab1024.sa.admin.module.consume.service.ordering;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.OrderingDao;
import net.lab1024.sa.admin.module.consume.dao.MenuDao;
import net.lab1024.sa.admin.module.consume.dao.OrderingItemDao;
import net.lab1024.sa.admin.module.consume.domain.entity.OrderingEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.MenuEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.OrderingItemEntity;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 点餐服务
 * 支持点餐消费模式，包括菜单管理、订单处理等功能
 *
 * @author SmartAdmin
 * @since 2025-11-17
 */
@Slf4j
@Service
public class OrderingService {

    @Resource
    private OrderingDao orderingDao;

    @Resource
    private MenuDao menuDao;

    @Resource
    private OrderingItemDao orderingItemDao;

    /**
     * 创建点餐订单
     *
     * @param createRequest 创建订单请求
     * @return 订单信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<OrderingEntity> createOrdering(CreateOrderingRequest createRequest) {
        try {
            // 1. 验证菜单项是否可用
            List<MenuEntity> menuList = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (OrderingItemRequest item : createRequest.getItems()) {
                MenuEntity menu = menuDao.selectById(item.getMenuId());
                if (menu == null) {
                    return ResponseDTO.error("菜单项不存在: " + item.getMenuId());
                }

                if (!"AVAILABLE".equals(menu.getStatus())) {
                    return ResponseDTO.error("菜单项不可用: " + menu.getMenuName());
                }

                // 检查库存
                if (menu.getStock() != null && menu.getStock() < item.getQuantity()) {
                    return ResponseDTO.error("菜单项库存不足: " + menu.getMenuName());
                }

                menuList.add(menu);

                // 计算小计
                BigDecimal itemSubtotal = menu.getPrice().multiply(new BigDecimal(item.getQuantity()));
                totalAmount = totalAmount.add(itemSubtotal);
            }

            // 2. 应用折扣
            BigDecimal finalAmount = totalAmount;
            if (createRequest.getDiscountRate() != null && createRequest.getDiscountRate().compareTo(BigDecimal.ZERO) > 0) {
                finalAmount = totalAmount.multiply(createRequest.getDiscountRate());
            }

            // 3. 创建订单
            OrderingEntity ordering = new OrderingEntity();
            ordering.setOrderNo(generateOrderNo());
            ordering.setUserId(createRequest.getUserId());
            ordering.setTableNo(createRequest.getTableNo());
            ordering.setTotalAmount(totalAmount);
            ordering.setDiscountAmount(totalAmount.subtract(finalAmount));
            ordering.setFinalAmount(finalAmount);
            ordering.setStatus("PENDING");
            ordering.setOrderType(createRequest.getOrderType());
            ordering.setRemark(createRequest.getRemark());
            ordering.setCreateTime(LocalDateTime.now());

            orderingDao.insert(ordering);

            // 4. 创建订单项
            for (int i = 0; i < createRequest.getItems().size(); i++) {
                OrderingItemRequest itemRequest = createRequest.getItems().get(i);
                MenuEntity menu = menuList.get(i);

                OrderingItemEntity orderingItem = new OrderingItemEntity();
                orderingItem.setOrderingId(ordering.getId());
                orderingItem.setMenuId(itemRequest.getMenuId());
                orderingItem.setMenuName(menu.getMenuName());
                orderingItem.setQuantity(itemRequest.getQuantity());
                orderingItem.setUnitPrice(menu.getPrice());
                orderingItem.setSubtotal(menu.getPrice().multiply(new BigDecimal(itemRequest.getQuantity())));
                orderingItem.setRemark(itemRequest.getRemark());
                orderingItem.setCreateTime(LocalDateTime.now());

                orderingItemDao.insert(orderingItem);

                // 扣减菜单库存
                menuDao.deductStock(itemRequest.getMenuId(), itemRequest.getQuantity());
            }

            log.info("创建点餐订单成功, orderNo: {}, totalAmount: {}", ordering.getOrderNo(), finalAmount);
            return ResponseDTO.ok(ordering);

        } catch (Exception e) {
            log.error("创建点餐订单异常", e);
            return ResponseDTO.error("创建订单失败");
        }
    }

    /**
     * 更新订单状态
     *
     * @param orderingId 订单ID
     * @param status 订单状态
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateOrderingStatus(Long orderingId, String status) {
        try {
            OrderingEntity ordering = orderingDao.selectById(orderingId);
            if (ordering == null) {
                return ResponseDTO.error("订单不存在");
            }

            // 验证状态转换是否合法
            if (!isValidStatusTransition(ordering.getStatus(), status)) {
                return ResponseDTO.error("状态转换不合法");
            }

            int updateCount = orderingDao.updateStatus(orderingId, status);
            if (updateCount <= 0) {
                return ResponseDTO.error("更新订单状态失败");
            }

            // 如果订单取消，恢复库存
            if ("CANCELLED".equals(status)) {
                restoreOrderingStock(orderingId);
            }

            log.info("更新订单状态成功, orderingId: {}, status: {}", orderingId, status);
            return ResponseDTO.ok("更新成功");

        } catch (Exception e) {
            log.error("更新订单状态异常, orderingId: {}, status: {}", orderingId, status, e);
            return ResponseDTO.error("更新订单状态失败");
        }
    }

    /**
     * 取消订单
     *
     * @param orderingId 订单ID
     * @param cancelReason 取消原因
     * @return 取消结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> cancelOrdering(Long orderingId, String cancelReason) {
        try {
            OrderingEntity ordering = orderingDao.selectById(orderingId);
            if (ordering == null) {
                return ResponseDTO.error("订单不存在");
            }

            if (!"PENDING".equals(ordering.getStatus())) {
                return ResponseDTO.error("只能取消待处理的订单");
            }

            int updateCount = orderingDao.cancelOrdering(orderingId, cancelReason);
            if (updateCount <= 0) {
                return ResponseDTO.error("取消订单失败");
            }

            // 恢复库存
            restoreOrderingStock(orderingId);

            log.info("取消订单成功, orderingId: {}, cancelReason: {}", orderingId, cancelReason);
            return ResponseDTO.ok("取消成功");

        } catch (Exception e) {
            log.error("取消订单异常, orderingId: {}", orderingId, e);
            return ResponseDTO.error("取消订单失败");
        }
    }

    /**
     * 根据订单ID查询订单详情
     *
     * @param orderingId 订单ID
     * @return 订单详情
     */
    public ResponseDTO<OrderingDetailVO> getOrderingDetail(Long orderingId) {
        try {
            OrderingEntity ordering = orderingDao.selectById(orderingId);
            if (ordering == null) {
                return ResponseDTO.error("订单不存在");
            }

            // 查询订单项
            List<OrderingItemEntity> items = orderingItemDao.selectByOrderingId(orderingId);

            // 构建详情对象
            OrderingDetailVO detailVO = new OrderingDetailVO();
            SmartBeanUtil.copyProperties(ordering, detailVO);
            detailVO.setItems(items);

            return ResponseDTO.ok(detailVO);

        } catch (Exception e) {
            log.error("查询订单详情异常, orderingId: {}", orderingId, e);
            return ResponseDTO.error("查询订单详情失败");
        }
    }

    /**
     * 根据用户ID查询订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @return 订单列表
     */
    public ResponseDTO<List<OrderingEntity>> getOrderingByUserId(Long userId, String status) {
        try {
            List<OrderingEntity> orderingList;
            if (status != null && !status.isEmpty()) {
                orderingList = orderingDao.selectByUserIdAndStatus(userId, status);
            } else {
                orderingList = orderingDao.selectByUserId(userId);
            }
            return ResponseDTO.ok(orderingList);
        } catch (Exception e) {
            log.error("查询用户订单列表异常, userId: {}, status: {}", userId, status, e);
            return ResponseDTO.error("查询订单列表失败");
        }
    }

    /**
     * 获取菜单列表
     *
     * @param categoryId 分类ID（可选）
     * @return 菜单列表
     */
    public ResponseDTO<List<MenuEntity>> getMenuList(Long categoryId) {
        try {
            List<MenuEntity> menuList;
            if (categoryId != null) {
                menuList = menuDao.selectByCategoryId(categoryId);
            } else {
                menuList = menuDao.selectAvailableMenus();
            }
            return ResponseDTO.ok(menuList);
        } catch (Exception e) {
            log.error("获取菜单列表异常, categoryId: {}", categoryId, e);
            return ResponseDTO.error("获取菜单列表失败");
        }
    }

    /**
     * 搜索菜单
     *
     * @param keyword 关键词
     * @return 菜单列表
     */
    public ResponseDTO<List<MenuEntity>> searchMenu(String keyword) {
        try {
            List<MenuEntity> menuList = menuDao.searchMenu(keyword);
            return ResponseDTO.ok(menuList);
        } catch (Exception e) {
            log.error("搜索菜单异常, keyword: {}", keyword, e);
            return ResponseDTO.error("搜索菜单失败");
        }
    }

    /**
     * 获取推荐菜单
     *
     * @param limit 限制数量
     * @return 推荐菜单列表
     */
    public ResponseDTO<List<MenuEntity>> getRecommendMenu(Integer limit) {
        try {
            List<MenuEntity> menuList = menuDao.selectRecommendMenus(limit);
            return ResponseDTO.ok(menuList);
        } catch (Exception e) {
            log.error("获取推荐菜单异常", e);
            return ResponseDTO.error("获取推荐菜单失败");
        }
    }

    /**
     * 获取订单统计
     *
     * @param userId 用户ID（可选）
     * @return 统计信息
     */
    public ResponseDTO<Map<String, Object>> getOrderingStatistics(Long userId) {
        try {
            Map<String, Object> statistics = new HashMap<>();

            if (userId != null) {
                // 用户订单统计
                Integer totalOrders = orderingDao.countByUserId(userId);
                BigDecimal totalAmount = orderingDao.sumAmountByUserId(userId);
                statistics.put("totalOrders", totalOrders);
                statistics.put("totalAmount", totalAmount);

                Integer pendingOrders = orderingDao.countByUserIdAndStatus(userId, "PENDING");
                Integer completedOrders = orderingDao.countByUserIdAndStatus(userId, "COMPLETED");
                statistics.put("pendingOrders", pendingOrders);
                statistics.put("completedOrders", completedOrders);
            } else {
                // 全局订单统计
                Integer totalOrders = orderingDao.countAll();
                BigDecimal totalAmount = orderingDao.sumAllAmount();
                statistics.put("totalOrders", totalOrders);
                statistics.put("totalAmount", totalAmount);

                Map<String, Integer> statusCount = orderingDao.countByStatus();
                statistics.put("statusCount", statusCount);
            }

            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("获取订单统计异常, userId: {}", userId, e);
            return ResponseDTO.error("获取统计信息失败");
        }
    }

    /**
     * 恢复订单库存
     *
     * @param orderingId 订单ID
     */
    private void restoreOrderingStock(Long orderingId) {
        try {
            List<OrderingItemEntity> items = orderingItemDao.selectByOrderingId(orderingId);
            for (OrderingItemEntity item : items) {
                menuDao.restoreStock(item.getMenuId(), item.getQuantity());
            }
            log.info("恢复订单库存成功, orderingId: {}", orderingId);
        } catch (Exception e) {
            log.error("恢复订单库存异常, orderingId: {}", orderingId, e);
        }
    }

    /**
     * 验证状态转换是否合法
     *
     * @param fromStatus 原状态
     * @param toStatus 目标状态
     * @return 是否合法
     */
    private boolean isValidStatusTransition(String fromStatus, String toStatus) {
        // 这里可以定义状态转换规则
        // 例如：PENDING -> PROCESSING -> COMPLETED
        // 或 PENDING -> CANCELLED
        return true; // 简化处理，实际应该有严格的状态机验证
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 创建订单请求
     */
    public static class CreateOrderingRequest {
        private Long userId;
        private String tableNo;
        private String orderType; // DINE_IN, TAKEAWAY, DELIVERY
        private List<OrderingItemRequest> items;
        private BigDecimal discountRate;
        private String remark;
        // getters and setters
    }

    /**
     * 订单项请求
     */
    public static class OrderingItemRequest {
        private Long menuId;
        private Integer quantity;
        private String remark;
        // getters and setters
    }

    /**
     * 订单详情VO
     */
    public static class OrderingDetailVO extends OrderingEntity {
        private List<OrderingItemEntity> items;
        // getter and setter
    }
}