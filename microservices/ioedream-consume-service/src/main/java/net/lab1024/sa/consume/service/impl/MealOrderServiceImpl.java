package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.dao.MealOrderDao;
import net.lab1024.sa.consume.dao.MealOrderItemDao;
import net.lab1024.sa.consume.domain.form.MealOrderCreateForm;
import net.lab1024.sa.consume.domain.form.MealOrderQueryForm;
import net.lab1024.sa.consume.domain.form.MealOrderVerifyForm;
import net.lab1024.sa.consume.domain.vo.MealOrderDetailVO;
import net.lab1024.sa.consume.domain.vo.MealOrderStatisticsVO;
import net.lab1024.sa.consume.domain.vo.MealOrderVO;
import net.lab1024.sa.consume.entity.MealOrderEntity;
import net.lab1024.sa.consume.entity.MealOrderItemEntity;
import net.lab1024.sa.consume.manager.MealOrderManager;
import net.lab1024.sa.consume.service.MealOrderService;

/**
 * 订餐服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
@Service
public class MealOrderServiceImpl implements MealOrderService {

    @Resource
    private MealOrderDao mealOrderDao;

    @Resource
    private MealOrderItemDao mealOrderItemDao;

    @Resource
    private MealOrderManager mealOrderManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> createOrder(MealOrderCreateForm form) {
        log.info("[订餐服务] 创建订单，userId={}, areaId={}, mealTypeId={}",
                form.getUserId(), form.getAreaId(), form.getMealTypeId());

        // 转换实体
        MealOrderEntity order = new MealOrderEntity();
        BeanUtils.copyProperties(form, order);

        // 转换明细
        List<MealOrderItemEntity> items = new ArrayList<>();
        if (form.getItems() != null) {
            for (MealOrderCreateForm.MealOrderItemForm itemForm : form.getItems()) {
                if (itemForm != null) {
                    MealOrderItemEntity item = new MealOrderItemEntity();
                    BeanUtils.copyProperties(itemForm, item);
                    item.setSubtotal(itemForm.getPrice().multiply(BigDecimal.valueOf(itemForm.getQuantity())));
                    items.add(item);
                }
            }
        }

        // 创建订单
        Long orderId = mealOrderManager.createOrder(order, items);
        return ResponseDTO.ok(orderId);
    }

    @Override
    public ResponseDTO<MealOrderDetailVO> getOrderDetail(Long orderId) {
        log.info("[订餐服务] 查询订单详情，orderId={}", orderId);

        MealOrderEntity order = mealOrderDao.selectById(orderId);
        if (order == null) {
            return ResponseDTO.error("ORDER_NOT_FOUND", "订单不存在");
        }

        MealOrderDetailVO vo = new MealOrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatusDesc(getStatusDesc(order.getStatus()));

        // 查询明细
        List<MealOrderItemEntity> items = mealOrderItemDao.selectByOrderId(orderId);
        List<MealOrderDetailVO.MealOrderItemVO> itemVOs = new ArrayList<>();
        for (MealOrderItemEntity item : items) {
            if (item != null) {
                MealOrderDetailVO.MealOrderItemVO itemVO = new MealOrderDetailVO.MealOrderItemVO();
                BeanUtils.copyProperties(item, itemVO);
                itemVOs.add(itemVO);
            }
        }
        vo.setItems(itemVOs);

        return ResponseDTO.ok(vo);
    }

    @Override
    public ResponseDTO<PageResult<MealOrderVO>> queryOrders(MealOrderQueryForm form) {
        log.info("[订餐服务] 分页查询订单，pageNum={}, pageSize={}", form.getPageNum(), form.getPageSize());

        // 简化实现：直接查询所有符合条件的订单
        List<MealOrderEntity> orders = mealOrderDao.selectByUserId(form.getUserId(), form.getStatus());

        List<MealOrderVO> voList = orders.stream().map(this::convertToVO).collect(Collectors.toList());

        PageResult<MealOrderVO> pageResult = new PageResult<>();
        pageResult.setList(voList);
        pageResult.setTotal((long) voList.size());
        pageResult.setPageNum(form.getPageNum());
        pageResult.setPageSize(form.getPageSize());

        return ResponseDTO.ok(pageResult);
    }

    @Override
    public ResponseDTO<List<MealOrderVO>> getUserOrders(Long userId, String status) {
        log.info("[订餐服务] 查询用户订单，userId={}, status={}", userId, status);

        List<MealOrderEntity> orders = mealOrderDao.selectByUserId(userId, status);
        List<MealOrderVO> voList = orders.stream().map(this::convertToVO).collect(Collectors.toList());

        return ResponseDTO.ok(voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> verifyOrder(MealOrderVerifyForm form) {
        log.info("[订餐服务] 核销订单，orderId={}, orderNo={}, pickupMethod={}",
                form.getOrderId(), form.getOrderNo(), form.getPickupMethod());

        Long orderId = form.getOrderId();
        if (orderId == null && form.getOrderNo() != null) {
            MealOrderEntity order = mealOrderDao.selectByOrderNo(form.getOrderNo());
            if (order != null) {
                orderId = order.getId();
            }
        }

        if (orderId == null) {
            return ResponseDTO.error("ORDER_NOT_FOUND", "订单不存在");
        }

        boolean success = mealOrderManager.verifyOrder(orderId, form.getPickupMethod(), form.getDeviceId());
        if (success) {
            return ResponseDTO.ok(true);
        }
        return ResponseDTO.error("VERIFY_FAILED", "核销失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> cancelOrder(Long orderId, String reason) {
        log.info("[订餐服务] 取消订单，orderId={}, reason={}", orderId, reason);

        boolean success = mealOrderManager.cancelOrder(orderId, reason);
        if (success) {
            return ResponseDTO.ok(true);
        }
        return ResponseDTO.error("CANCEL_FAILED", "取消失败");
    }

    @Override
    public ResponseDTO<MealOrderStatisticsVO> getStatistics(Long areaId, Long mealTypeId, String dateStr) {
        log.info("[订餐服务] 获取订餐统计，areaId={}, mealTypeId={}, date={}", areaId, mealTypeId, dateStr);

        LocalDateTime orderDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        int count = mealOrderManager.getOrderCount(areaId, mealTypeId, orderDate);

        MealOrderStatisticsVO vo = new MealOrderStatisticsVO();
        vo.setAreaId(areaId);
        vo.setMealTypeId(mealTypeId);
        vo.setDate(dateStr);
        vo.setTotalOrders(count);

        return ResponseDTO.ok(vo);
    }

    private MealOrderVO convertToVO(MealOrderEntity entity) {
        if (entity == null) {
            return null;
        }
        MealOrderVO vo = new MealOrderVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setStatusDesc(getStatusDesc(entity.getStatus()));
        return vo;
    }

    /**
     * 获取订单状态描述
     *
     * @param status 订单状态（整型状态码）
     * @return 状态中文描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "";
        }
        // 约定：1-待取餐 2-已完成 3-已取消 4-已过期（若后续有枚举，可在此集中映射）
        return switch (status) {
            case 1 -> "待取餐";
            case 2 -> "已完成";
            case 3 -> "已取消";
            case 4 -> "已过期";
            default -> String.valueOf(status);
        };
    }
}
