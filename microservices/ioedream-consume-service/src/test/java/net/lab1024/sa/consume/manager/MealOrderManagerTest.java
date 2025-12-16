package net.lab1024.sa.consume.manager;

import net.lab1024.sa.consume.dao.MealOrderDao;
import net.lab1024.sa.consume.dao.MealOrderItemDao;
import net.lab1024.sa.common.consume.entity.MealOrderEntity;
import net.lab1024.sa.common.consume.entity.MealOrderItemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 订餐管理器单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订餐管理器测试")
class MealOrderManagerTest {

    @Mock
    private MealOrderDao mealOrderDao;

    @Mock
    private MealOrderItemDao mealOrderItemDao;

    private MealOrderManager mealOrderManager;

    @BeforeEach
    void setUp() {
        mealOrderManager = new MealOrderManager(mealOrderDao, mealOrderItemDao);
    }

    @Test
    @DisplayName("生成订单编号 - 格式正确")
    void generateOrderNo_shouldReturnCorrectFormat() {
        String orderNo = mealOrderManager.generateOrderNo();

        assertNotNull(orderNo);
        assertTrue(orderNo.startsWith("MO"));
        assertEquals(18, orderNo.length()); // MO + 12位日期时间 + 4位序列号
    }

    @Test
    @DisplayName("创建订单 - 成功")
    void createOrder_shouldSucceed() {
        // 准备数据
        MealOrderEntity order = new MealOrderEntity();
        order.setUserId(1L);
        order.setAccountId(1L);
        order.setAreaId(1L);
        order.setMealTypeId(1L);

        MealOrderItemEntity item = new MealOrderItemEntity();
        item.setDishId(1L);
        item.setDishName("红烧肉");
        item.setPrice(new BigDecimal("15.00"));
        item.setQuantity(1);

        when(mealOrderDao.insert(any(MealOrderEntity.class))).thenAnswer(invocation -> {
            MealOrderEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        });
        when(mealOrderItemDao.insert(any(MealOrderItemEntity.class))).thenReturn(1);

        // 执行
        Long orderId = mealOrderManager.createOrder(order, List.of(item));

        // 验证
        assertNotNull(orderId);
        assertEquals(1L, orderId);
        assertNotNull(order.getOrderNo());
        assertEquals("PENDING", order.getStatus());
        assertEquals(new BigDecimal("15.00"), order.getAmount());

        verify(mealOrderDao, times(1)).insert(any(MealOrderEntity.class));
        verify(mealOrderItemDao, times(1)).insert(any(MealOrderItemEntity.class));
    }

    @Test
    @DisplayName("核销订单 - 订单不存在")
    void verifyOrder_orderNotFound_shouldReturnFalse() {
        when(mealOrderDao.selectById(1L)).thenReturn(null);

        boolean result = mealOrderManager.verifyOrder(1L, "QR_CODE", 1L);

        assertFalse(result);
        verify(mealOrderDao, never()).verifyOrder(anyLong(), any(), anyLong(), anyString());
    }

    @Test
    @DisplayName("核销订单 - 状态不正确")
    void verifyOrder_wrongStatus_shouldReturnFalse() {
        MealOrderEntity order = new MealOrderEntity();
        order.setId(1L);
        order.setStatus("COMPLETED");

        when(mealOrderDao.selectById(1L)).thenReturn(order);

        boolean result = mealOrderManager.verifyOrder(1L, "QR_CODE", 1L);

        assertFalse(result);
        verify(mealOrderDao, never()).verifyOrder(anyLong(), any(), anyLong(), anyString());
    }

    @Test
    @DisplayName("核销订单 - 成功")
    void verifyOrder_shouldSucceed() {
        MealOrderEntity order = new MealOrderEntity();
        order.setId(1L);
        order.setStatus("PENDING");
        order.setPickupStartTime(LocalDateTime.now().minusHours(1));
        order.setPickupEndTime(LocalDateTime.now().plusHours(1));

        when(mealOrderDao.selectById(1L)).thenReturn(order);
        when(mealOrderDao.verifyOrder(eq(1L), any(), eq(1L), eq("QR_CODE"))).thenReturn(1);

        boolean result = mealOrderManager.verifyOrder(1L, "QR_CODE", 1L);

        assertTrue(result);
        verify(mealOrderDao, times(1)).verifyOrder(eq(1L), any(), eq(1L), eq("QR_CODE"));
    }

    @Test
    @DisplayName("取消订单 - 成功")
    void cancelOrder_shouldSucceed() {
        MealOrderEntity order = new MealOrderEntity();
        order.setId(1L);
        order.setStatus("PENDING");

        when(mealOrderDao.selectById(1L)).thenReturn(order);
        when(mealOrderDao.cancelOrder(eq(1L), eq("用户取消"), any())).thenReturn(1);

        boolean result = mealOrderManager.cancelOrder(1L, "用户取消");

        assertTrue(result);
        verify(mealOrderDao, times(1)).cancelOrder(eq(1L), eq("用户取消"), any());
    }

    @Test
    @DisplayName("处理过期订单")
    void processExpiredOrders_shouldReturnCount() {
        when(mealOrderDao.batchExpireOrders(any(), any())).thenReturn(5);

        int count = mealOrderManager.processExpiredOrders();

        assertEquals(5, count);
        verify(mealOrderDao, times(1)).batchExpireOrders(any(), any());
    }
}
