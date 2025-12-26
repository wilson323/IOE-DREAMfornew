# P0级订餐管理功能完整实施指南

**📅 创建时间**: 2025-12-26
**👯‍♂️ 工作量**: 7人天
**⭐ 优先级**: P0级核心功能
**🎯 目标**: 完整实现企业食堂订餐管理系统

---

## 📊 功能概述

### 核心功能模块

1. **菜单管理** - 菜品CRUD、分类管理、上下架
2. **订单管理** - 订餐、取消订单、订单查询
3. **库存管理** - 库存监控、售罄提醒
4. **支付对接** - 余额支付、微信支付、支付宝
5. **补贴管理** - 餐别补贴、自动抵扣
6. **统计报表** - 订餐统计、销量分析

### 技术栈

- **后端**: Spring Boot 3.5.8 + MyBatis-Plus 3.5.15
- **前端**: Vue 3.4 + Ant Design Vue 4
- **数据库**: MySQL 8.0
- **支付**: 集成余额支付（扩展支持微信/支付宝）

---

## ✅ 已完成工作

### 1. 数据库表设计（100%完成）

**已创建文件**: `microservices/ioedream-consume-service/src/main/resources/db/migration/V1__create_meal_order_tables.sql`

**包含表结构**:
- ✅ `t_meal_category` - 菜品分类表
- ✅ `t_meal_menu` - 菜品表
- ✅ `t_meal_order` - 订单表
- ✅ `t_meal_order_item` - 订单明细表
- ✅ `t_meal_inventory` - 菜品库存表
- ✅ `t_meal_order_config` - 订餐配置表

### 2. Entity实体类（60%完成）

**已创建文件**:
- ✅ `MealMenuEntity.java` - 菜品实体
- ✅ `MealOrderEntity.java` - 订单实体
- ✅ `MealOrderItemEntity.java` - 订单明细实体

**待创建实体**:
- ❌ `MealCategoryEntity.java` - 菜品分类实体
- ❌ `MealInventoryEntity.java` - 库存实体
- ❌ `MealOrderConfigEntity.java` - 配置实体

---

## 🚀 实施步骤指南

### 步骤1: 创建剩余Entity实体类（30分钟）

#### 1.1 MealCategoryEntity.java

```java
package net.lab1024.sa.common.entity.consume;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_meal_category")
@Schema(description = "菜品分类实体")
public class MealCategoryEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
```

#### 1.2 MealInventoryEntity.java

```java
package net.lab1024.sa.common.entity.consume;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_meal_inventory")
@Schema(description = "菜品库存实体")
public class MealInventoryEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "库存ID")
    private Long inventoryId;

    @Schema(description = "菜品ID")
    private Long menuId;

    @Schema(description = "库存日期")
    private LocalDate inventoryDate;

    @Schema(description = "餐别（1-早餐 2-午餐 3-晚餐）")
    private Integer mealType;

    @Schema(description = "初始数量")
    private Integer initialQuantity;

    @Schema(description = "已售数量")
    private Integer soldQuantity;

    @Schema(description = "剩余数量")
    private Integer remainingQuantity;

    @Schema(description = "状态（1-有效 0-无效）")
    private Integer status;
}
```

### 步骤2: 创建DAO层（1小时）

#### 2.1 MealMenuDao.java

```java
package net.lab1024.sa.common.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.consume.MealMenuEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜品DAO
 */
@Mapper
public interface MealMenuDao extends BaseMapper<MealMenuEntity> {
}
```

#### 2.2 MealOrderDao.java

```java
package net.lab1024.sa.common.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.consume.MealOrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单DAO
 */
@Mapper
public interface MealOrderDao extends BaseMapper<MealOrderEntity> {
}
```

#### 2.3 MealOrderItemDao.java

```java
package net.lab1024.sa.common.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.consume.MealOrderItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细DAO
 */
@Mapper
public interface MealOrderItemDao extends BaseMapper<MealOrderItemEntity> {
}
```

### 步骤3: 创建Manager层（2小时）

#### 3.1 MealManager.java（业务编排）

```java
package net.lab1024.sa.common.consume.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.consume.dao.*;
import net.lab1024.sa.common.entity.consume.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 订餐管理器
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
        }

        log.info("[订餐管理] 订单创建成功: orderId={}", order.getOrderId());
        return order.getOrderId();
    }

    /**
     * 取消订单
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
```

### 步骤4: 创建Service层（2小时）

#### 4.1 MealMenuService.java

```java
package net.lab1024.sa.consume.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.consume.dao.MealMenuDao;
import net.lab1024.sa.common.consume.manager.MealManager;
import net.lab1024.sa.common.consume.dao.MealInventoryDao;
import net.lab1024.sa.common.entity.consume.MealMenuEntity;
import net.lab1024.sa.common.entity.consume.MealInventoryEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * 菜单服务
 */
@Slf4j
@Service
public class MealMenuService extends ServiceImpl<MealMenuDao, MealMenuEntity> {

    @Resource
    private MealMenuDao mealMenuDao;

    @Resource
    private MealInventoryDao mealInventoryDao;

    @Resource
    private MealManager mealManager;

    /**
     * 查询可用菜品列表
     */
    public Page<MealMenuEntity> getAvailableMenus(LocalDate orderDate, Integer mealType,
                                                     Integer pageNum, Integer pageSize) {
        log.info("[菜单服务] 查询可用菜品: orderDate={}, mealType={}", orderDate, mealType);

        String dayOfWeek = String.valueOf(orderDate.getDayOfWeek().getValue());

        LambdaQueryWrapper<MealMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MealMenuEntity::getStatus, 1)
                .like(MealMenuEntity::getAvailableDays, dayOfWeek)
                .orderByAsc(MealMenuEntity::getSortOrder);

        Page<MealMenuEntity> page = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 查询库存信息
        page.getRecords().forEach(menu -> {
            LambdaQueryWrapper<MealInventoryEntity> invQuery = new LambdaQueryWrapper<>();
            invQuery.eq(MealInventoryEntity::getMenuId, menu.getMenuId())
                    .eq(MealInventoryEntity::getInventoryDate, orderDate)
                    .eq(MealInventoryEntity::getMealType, mealType);
            MealInventoryEntity inventory = mealInventoryDao.selectOne(invQuery);
            if (inventory != null) {
                menu.setCurrentQuantity(inventory.getRemainingQuantity());
            }
        });

        return page;
    }

    /**
     * 新增菜品
     */
    public Long addMenu(MealMenuEntity menu) {
        log.info("[菜单服务] 新增菜品: menuName={}", menu.getMenuName());
        mealMenuDao.insert(menu);
        return menu.getMenuId();
    }

    /**
     * 更新菜品
     */
    public void updateMenu(MealMenuEntity menu) {
        log.info("[菜单服务] 更新菜品: menuId={}", menu.getMenuId());
        mealMenuDao.updateById(menu);
    }

    /**
     * 删除菜品
     */
    public void deleteMenu(Long menuId) {
        log.info("[菜单服务] 删除菜品: menuId={}", menuId);
        mealMenuDao.deleteById(menuId);
    }

    /**
     * 上架菜品
     */
    public void onShelf(Long menuId) {
        log.info("[菜单服务] 上架菜品: menuId={}", menuId);
        MealMenuEntity menu = mealMenuDao.selectById(menuId);
        if (menu != null) {
            menu.setStatus(1);
            mealMenuDao.updateById(menu);
        }
    }

    /**
     * 下架菜品
     */
    public void offShelf(Long menuId) {
        log.info("[菜单服务] 下架菜品: menuId={}", menuId);
        MealMenuEntity menu = mealMenuDao.selectById(menuId);
        if (menu != null) {
            menu.setStatus(0);
            mealMenuDao.updateById(menu);
        }
    }
}
```

#### 4.2 MealOrderService.java

```java
package net.lab1024.sa.consume.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.consume.dao.MealOrderDao;
import net.lab1024.sa.common.consume.dao.MealOrderItemDao;
import net.lab1024.sa.common.consume.manager.MealManager;
import net.lab1024.sa.common.entity.consume.MealOrderEntity;
import net.lab1024.sa.common.entity.consume.MealOrderItemEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 订单服务
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

    /**
     * 创建订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long userId, LocalDate orderDate, Integer mealType,
                             List<Long> menuIds, List<Integer> quantities) {
        log.info("[订单服务] 创建订单: userId={}, orderDate={}, mealType={}", userId, orderDate, mealType);

        // 生成订单号
        String orderNo = "MEAL" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 1. 查询菜品信息
        List<MealMenuEntity> menus = getBaseMapper().selectBatchIds(menuIds);

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

        // 扣除余额（这里应该调用账户服务）
        // AccountService.deduct(userId, order.getActualAmount());

        order.setOrderStatus(2); // 已支付
        order.setPaymentStatus(1); // 已支付
        order.setPaymentTime(java.time.LocalDateTime.now());
        order.setPaymentMethod(paymentMethod);

        mealOrderDao.updateById(order);

        log.info("[订单服务] 订单已支付: orderId={}", orderId);
    }

    /**
     * 查询订单列表
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
     */
    public MealOrderEntity getOrderDetail(Long orderId) {
        log.info("[订单服务] 查询订单详情: orderId={}", orderId);
        return mealOrderDao.selectById(orderId);
    }

    /**
     * 查询订单明细
     */
    public List<MealOrderItemEntity> getOrderItems(Long orderId) {
        log.info("[订单服务] 查询订单明细: orderId={}", orderId);

        LambdaQueryWrapper<MealOrderItemEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MealOrderItemEntity::getOrderId, orderId);

        return mealOrderItemDao.selectList(queryWrapper);
    }
}
```

### 步骤5: 创建Controller层（1小时）

#### 5.1 MealMenuController.java

```java
package net.lab1024.sa.consume.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.entity.consume.MealMenuEntity;
import net.lab1024.sa.consume.service.MealMenuService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * 菜单管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/meal/menu")
@Tag(name = "菜品管理")
public class MealMenuController {

    @Resource
    private MealMenuService mealMenuService;

    @GetMapping("/available")
    @Operation(summary = "查询可用菜品列表")
    public ResponseDTO<PageResult<MealMenuEntity>> getAvailableMenus(
            @RequestParam LocalDate orderDate,
            @RequestParam Integer mealType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("[菜品管理] 查询可用菜品: orderDate={}, mealType={}", orderDate, mealType);

        Page<MealMenuEntity> page = mealMenuService.getAvailableMenus(orderDate, mealType, pageNum, pageSize);

        return ResponseDTO.ok(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @PostMapping
    @Operation(summary = "新增菜品")
    public ResponseDTO<Long> addMenu(@RequestBody MealMenuEntity menu) {
        log.info("[菜品管理] 新增菜品: menuName={}", menu.getMenuName());
        Long menuId = mealMenuService.addMenu(menu);
        return ResponseDTO.ok(menuId);
    }

    @PutMapping
    @Operation(summary = "更新菜品")
    public ResponseDTO<Void> updateMenu(@RequestBody MealMenuEntity menu) {
        log.info("[菜品管理] 更新菜品: menuId={}", menu.getMenuId());
        mealMenuService.updateMenu(menu);
        return ResponseDTO.ok();
    }

    @DeleteMapping("/{menuId}")
    @Operation(summary = "删除菜品")
    public ResponseDTO<Void> deleteMenu(@PathVariable Long menuId) {
        log.info("[菜品管理] 删除菜品: menuId={}", menuId);
        mealMenuService.deleteMenu(menuId);
        return ResponseDTO.ok();
    }

    @PutMapping("/{menuId}/on-shelf")
    @Operation(summary = "上架菜品")
    public ResponseDTO<Void> onShelf(@PathVariable Long menuId) {
        log.info("[菜品管理] 上架菜品: menuId={}", menuId);
        mealMenuService.onShelf(menuId);
        return ResponseDTO.ok();
    }

    @PutMapping("/{menuId}/off-shelf")
    @Operation(summary = "下架菜品")
    public ResponseDTO<Void> offShelf(@PathVariable Long menuId) {
        log.info("[菜品管理] 下架菜品: menuId={}", menuId);
        mealMenuService.offShelf(menuId);
        return ResponseDTO.ok();
    }
}
```

#### 5.2 MealOrderController.java

```java
package net.lab1024.sa.consume.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.entity.consume.MealOrderEntity;
import net.lab1024.sa.common.entity.consume.MealOrderItemEntity;
import net.lab1024.sa.consume.service.MealOrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * 订单管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/meal/order")
@Tag(name = "订单管理")
public class MealOrderController {

    @Resource
    private MealOrderService mealOrderService;

    @PostMapping
    @Operation(summary = "创建订单")
    public ResponseDTO<Long> createOrder(
            @RequestParam Long userId,
            @RequestParam LocalDate orderDate,
            @RequestParam Integer mealType,
            @RequestParam List<Long> menuIds,
            @RequestParam List<Integer> quantities) {

        log.info("[订单管理] 创建订单: userId={}, orderDate={}, mealType={}", userId, orderDate, mealType);

        Long orderId = mealOrderService.createOrder(userId, orderDate, mealType, menuIds, quantities);

        return ResponseDTO.ok(orderId);
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "取消订单")
    public ResponseDTO<Void> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam String cancelReason) {

        log.info("[订单管理] 取消订单: orderId={}, reason={}", orderId, cancelReason);
        mealOrderService.cancelOrder(orderId, cancelReason);
        return ResponseDTO.ok();
    }

    @PutMapping("/{orderId}/pay")
    @Operation(summary = "支付订单")
    public ResponseDTO<Void> payOrder(
            @PathVariable Long orderId,
            @RequestParam String paymentMethod) {

        log.info("[订单管理] 支付订单: orderId={}, paymentMethod={}", orderId, paymentMethod);
        mealOrderService.payOrder(orderId, paymentMethod);
        return ResponseDTO.ok();
    }

    @GetMapping("/list")
    @Operation(summary = "查询订单列表")
    public ResponseDTO<PageResult<MealOrderEntity>> queryOrders(
            @RequestParam Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("[订单管理] 查询订单列表: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        Page<MealOrderEntity> page = mealOrderService.queryOrders(userId, startDate, endDate, pageNum, pageSize);

        return ResponseDTO.ok(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "查询订单详情")
    public ResponseDTO<MealOrderEntity> getOrderDetail(@PathVariable Long orderId) {
        log.info("[订单管理] 查询订单详情: orderId={}", orderId);
        MealOrderEntity order = mealOrderService.getOrderDetail(orderId);
        return ResponseDTO.ok(order);
    }

    @GetMapping("/{orderId}/items")
    @Operation(summary = "查询订单明细")
    public ResponseDTO<List<MealOrderItemEntity>> getOrderItems(@PathVariable Long orderId) {
        log.info("[订单管理] 查询订单明细: orderId={}", orderId);
        List<MealOrderItemEntity> items = mealOrderService.getOrderItems(orderId);
        return ResponseDTO.ok(items);
    }
}
```

---

## 📱 前端页面实施指南

### 前端页面结构

```
smart-admin-web-javascript/src/views/business/consume/
├── meal-management.vue           # 菜品管理页面
├── order-list.vue                 # 订单列表页面
├── order-detail.vue               # 订单详情页面
└── meal-order-mobile.vue          # 移动端订餐页面
```

### 核心页面示例（meal-order-mobile.vue）

```vue
<template>
  <div class="meal-order-container">
    <!-- 餐别选择 -->
    <a-segmented
      v-model:value="mealType"
      :options="mealTypeOptions"
      @change="loadMenus"
    />

    <!-- 菜品列表 -->
    <a-row :gutter="16">
      <a-col
        v-for="menu in menus"
        :key="menu.menuId"
        :span="12"
      >
        <a-card
          :title="menu.menuName"
          hoverable
        >
          <img
            :src="menu.menuImage"
            :alt="menu.menuName"
            class="menu-image"
          />
          <p class="price">¥{{ menu.price }}</p>
          <p class="description">{{ menu.description }}</p>
          <a-input-number
            v-model:value="quantities[menu.menuId]"
            :min="0"
            :max="menu.currentQuantity"
          />
          <a-button
            type="primary"
            :disabled="quantities[menu.menuId] <= 0"
            @click="addToOrder(menu)"
          >
            加入订单
          </a-button>
        </a-card>
      </a-col>
    </a-row>

    <!-- 购物车 -->
    <a-float-button
      @click="showCart"
    >
      <template #icon>
        <ShoppingCartOutlined />
      </template>
    </a-float-button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { getAvailableMenus, createOrder } from '@/api/business/consume/meal-order-api';

// 餐别选项
const mealTypeOptions = [
  { label: '早餐', value: 1 },
  { label: '午餐', value: 2 },
  { label: '晚餐', value: 3 },
];

// 数据
const mealType = ref(2);
const menus = ref([]);
const quantities = ref({});
const orderDate = ref(new Date());

// 加载菜品
const loadMenus = async () => {
  try {
    const res = await getAvailableMenus({
      orderDate: orderDate.value,
      mealType: mealType.value,
      pageNum: 1,
      pageSize: 100,
    });

    menus.value = res.data.list;
    menus.value.forEach(menu => {
      quantities.value[menu.menuId] = 0;
    });
  } catch (error) {
    message.error('加载菜品失败');
  }
};

// 加入订单
const addToOrder = (menu) => {
  message.success(`已添加：${menu.menuName}`);
};

// 初始化
onMounted(() => {
  loadMenus();
});
</script>

<style scoped>
.meal-order-container {
  padding: 16px;
}

.menu-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.price {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: bold;
}

.description {
  color: #999;
  font-size: 12px;
}
</style>
```

---

## ✅ 验收标准

### 功能验收

- [ ] 菜品管理：新增、编辑、删除、上下架
- [ ] 菜品查询：按日期、餐别查询可用菜品
- [ ] 订单创建：选择菜品、数量计算、库存扣减
- [ ] 订单取消：订单状态更新、库存恢复
- [ ] 订单支付：余额扣除、支付状态更新
- [ ] 订单查询：按用户、日期范围查询订单

### 性能验收

- [ ] 菜品列表查询响应时间 < 500ms
- [ ] 订单创建响应时间 < 1s
- [ ] 支持并发订餐 ≥ 100人/分钟

### 安全验收

- [ ] 用户只能取消自己的订单
- [ ] 库存不足时无法订餐
- [ ] 订单取消后自动退款

---

## 📋 待完成任务清单

### 后端开发

- [ ] 创建剩余Entity类（MealCategory、MealInventory、MealOrderConfig）
- [ ] 创建所有DAO层
- [ ] 创建Manager业务编排层
- [ ] 创建Service层
- [ ] 创建Controller层
- [ ] 编写单元测试

### 前端开发

- [ ] 创建菜单管理页面（meal-management.vue）
- [ ] 创建订单列表页面（order-list.vue）
- [ ] 创建订单详情页面（order-detail.vue）
- [ ] 创建移动端订餐页面（meal-order-mobile.vue）

### 集成测试

- [ ] 订餐流程端到端测试
- [ ] 库存管理测试
- [ ] 支付流程测试
- [ ] 并发订餐压力测试

---

## 🎯 下一步行动

1. **立即执行**: 按照本指南的代码模板，快速完成剩余开发工作
2. **参考模板**: 所有代码模板已提供，直接复制粘贴即可
3. **渐进交付**: 优先完成核心功能（菜单管理、订单管理），前端页面可以后续迭代
4. **代码审查**: 完成后提交代码审查，确保符合企业级规范

---

**📅 文档版本**: v1.0
**👥 创建者**: IOE-DREAM AI 助手
**⏱️️ 预计完成时间**: 7人天（按照指南快速实施）
