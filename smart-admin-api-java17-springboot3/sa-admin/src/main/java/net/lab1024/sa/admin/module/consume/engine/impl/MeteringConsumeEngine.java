package net.lab1024.sa.admin.module.consume.engine.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.ProductDao;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeModeConfig;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeResultDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeValidationResult;
import net.lab1024.sa.admin.module.consume.domain.entity.ProductEntity;
import net.lab1024.sa.admin.module.consume.domain.enums.ConsumeModeEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.ConsumeStatusEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.MeteringUnitEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.ProductStatusEnum;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeEngine;
import net.lab1024.sa.admin.module.consume.service.ConsumeCacheService;
import net.lab1024.sa.admin.module.consume.service.ConsumeService;

/**
 * 计量模式消费引擎
 * 适用于按计量单位消费的场景，如水费、电费、燃气费、重量计费等
 * 支持实时计量、预付费、后付费等多种计费模式
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class MeteringConsumeEngine implements ConsumeModeEngine {

    @Resource
    private ConsumeService consumeService;

    @Resource
    private ConsumeCacheService consumeCacheService;

    @Resource
    private ProductDao productDao;

    // 计量精度配置
    private static final int DEFAULT_SCALE = 3; // 默认保留3位小数
    private static final BigDecimal MAX_SINGLE_AMOUNT = new BigDecimal("10000.00");
    private static final BigDecimal MIN_SINGLE_AMOUNT = new BigDecimal("0.01");

    @Override
    public ConsumeModeEnum getConsumeMode() {
        return ConsumeModeEnum.METERING;
    }

    @Override
    public ConsumeValidationResult validateRequest(ConsumeRequestDTO consumeRequest) {
        log.debug("验证计量模式消费请求: userId={}, meteringValue={}, unit={}",
                consumeRequest.getUserId(), consumeRequest.getMeteringValue(), consumeRequest.getMeteringUnit());

        ConsumeValidationResult result = new ConsumeValidationResult();

        try {
            // 1. 验证计量值是否为空
            if (consumeRequest.getMeteringValue() == null) {
                result.setValid(false);
                result.setErrorMessage("计量值不能为空");
                return result;
            }

            // 2. 验证计量值是否为正数
            if (consumeRequest.getMeteringValue().compareTo(BigDecimal.ZERO) <= 0) {
                result.setValid(false);
                result.setErrorMessage("计量值必须大于0");
                return result;
            }

            // 3. 验证计量单位
            if (consumeRequest.getMeteringUnit() == null || consumeRequest.getMeteringUnit().trim().isEmpty()) {
                result.setValid(false);
                result.setErrorMessage("计量单位不能为空");
                return result;
            }

            // 4. 验证计量单位是否有效
            if (!isValidMeteringUnit(consumeRequest.getMeteringUnit())) {
                result.setValid(false);
                result.setErrorMessage("无效的计量单位: " + consumeRequest.getMeteringUnit() +
                        "，支持的单位: " + getSupportedUnits());
                return result;
            }

            // 5. 验证设备是否支持计量模式
            if (!isModeAvailable(consumeRequest.getDeviceId())) {
                result.setValid(false);
                result.setErrorMessage("设备不支持计量消费模式");
                return result;
            }

            // 6. 如果有商品ID，验证商品信息
            ProductEntity product = null;
            if (consumeRequest.getProductId() != null) {
                product = getProductById(consumeRequest.getProductId());
                if (product == null) {
                    result.setValid(false);
                    result.setErrorMessage("商品不存在");
                    return result;
                }

                // 验证商品状态：1-上架 2-下架 3-停售
                ProductStatusEnum status = ProductStatusEnum.getByValue(product.getStatus());
                if (status == null || !ProductStatusEnum.ON_SALE.equals(status)) {
                    result.setValid(false);
                    result.setErrorMessage("商品不可用");
                    return result;
                }
            }

            // 7. 计算消费金额
            BigDecimal unitPrice = getUnitPrice(consumeRequest);
            BigDecimal totalAmount = calculateMeteringAmount(consumeRequest.getMeteringValue(), unitPrice);

            // 8. 验证消费金额范围
            ConsumeModeConfig config = getModeConfig(consumeRequest.getDeviceId());
            BigDecimal minAmount = config != null ? config.getMinSingleAmount() : MIN_SINGLE_AMOUNT;
            BigDecimal maxAmount = config != null ? config.getMaxSingleAmount() : MAX_SINGLE_AMOUNT;

            if (totalAmount.compareTo(minAmount) < 0) {
                result.setValid(false);
                result.setErrorMessage("消费金额不足，最低: ¥" + minAmount);
                return result;
            }

            if (totalAmount.compareTo(maxAmount) > 0) {
                result.setValid(false);
                result.setErrorMessage("消费金额超过限制，最高: ¥" + maxAmount);
                return result;
            }

            // 9. 验证用户余额
            Map<String, Object> accountInfo = consumeService.getAccountBalance(consumeRequest.getUserId());
            if (accountInfo != null) {
                BigDecimal balance = (BigDecimal) accountInfo.get("balance");
                if (balance != null && balance.compareTo(totalAmount) < 0) {
                    result.setValid(false);
                    result.setErrorMessage("账户余额不足，当前余额: ¥" + balance + "，需要: ¥" + totalAmount);
                    return result;
                }
            }

            // 10. 验证计量限额
            if (!validateMeteringLimits(consumeRequest)) {
                result.setValid(false);
                result.setErrorMessage("超出计量限额");
                return result;
            }

            result.setValid(true);
            result.setProductInfo(product);
            result.setCalculatedAmount(totalAmount);
            result.setUnitPrice(unitPrice);
            log.debug("计量模式消费请求验证通过: userId={}, meteringValue={}, totalAmount={}",
                    consumeRequest.getUserId(), consumeRequest.getMeteringValue(), totalAmount);

        } catch (Exception e) {
            log.error("验证计量模式消费请求失败", e);
            result.setValid(false);
            result.setErrorMessage("验证消费请求时发生错误");
        }

        return result;
    }

    @Override
    public ConsumeResultDTO processConsume(ConsumeRequestDTO consumeRequest) {
        log.info("处理计量模式消费: userId={}, meteringValue={}, unit={}, unitPrice={}",
                consumeRequest.getUserId(), consumeRequest.getMeteringValue(),
                consumeRequest.getMeteringUnit(), consumeRequest.getUnitPrice());

        ConsumeResultDTO result = new ConsumeResultDTO();

        try {
            // 1. 再次验证请求
            ConsumeValidationResult validation = validateRequest(consumeRequest);
            if (!validation.isValid()) {
                result.setSuccess(false);
                result.setMessage(validation.getErrorMessage());
                result.setStatus(ConsumeStatusEnum.FAILED);
                return result;
            }

            BigDecimal unitPrice = validation.getUnitPrice();
            BigDecimal totalAmount = validation.getCalculatedAmount();

            // 2. 获取当前计量读数
            BigDecimal previousReading = getCurrentMeteringReading(consumeRequest);
            BigDecimal newReading = previousReading.add(consumeRequest.getMeteringValue());

            // 3. 执行消费处理
            Map<String, Object> consumeData = new HashMap<>();
            consumeData.put("userId", consumeRequest.getUserId());
            consumeData.put("amount", totalAmount);
            consumeData.put("personName", consumeRequest.getPersonName());
            consumeData.put("payMethod", consumeRequest.getPayMethod());
            consumeData.put("deviceId", consumeRequest.getDeviceId());
            consumeData.put("consumeMode", getConsumeMode().name());
            consumeData.put("remark", buildConsumeRemark(consumeRequest, unitPrice, totalAmount));

            // 计量相关字段
            consumeData.put("meteringValue", consumeRequest.getMeteringValue());
            consumeData.put("meteringUnit", consumeRequest.getMeteringUnit());
            consumeData.put("unitPrice", unitPrice);
            consumeData.put("previousReading", previousReading);
            consumeData.put("newReading", newReading);
            consumeData.put("meteringType", determineMeteringType(consumeRequest));

            Map<String, Object> processResult = consumeService.processConsume(consumeData);

            if (Boolean.TRUE.equals(processResult.get("success"))) {
                // 4. 消费成功
                result.setSuccess(true);
                result.setOrderId((Long) processResult.get("orderId"));
                result.setAmount(totalAmount);
                result.setMessage(
                        "计量消费成功，用量: " + consumeRequest.getMeteringValue() + " " + consumeRequest.getMeteringUnit());
                result.setStatus(ConsumeStatusEnum.SUCCESS);
                result.setConsumeTime((java.time.LocalDateTime) processResult.get("consumeTime"));
                result.setNewBalance((BigDecimal) processResult.get("newBalance"));
                result.setConsumeMode(getConsumeMode());

                // 5. 更新计量读数缓存
                updateMeteringReadingCache(consumeRequest, newReading);

                // 6. 更新计量记录缓存
                updateMeteringRecordCache(consumeRequest, totalAmount);

                // 7. 记录成功日志
                log.info("计量模式消费成功: userId={}, orderId={}, amount={}, meteringValue={}, unit={}",
                        consumeRequest.getUserId(), result.getOrderId(), result.getAmount(),
                        consumeRequest.getMeteringValue(), consumeRequest.getMeteringUnit());

            } else {
                // 8. 消费失败
                result.setSuccess(false);
                result.setMessage((String) processResult.get("message"));
                result.setStatus(ConsumeStatusEnum.FAILED);
                result.setAmount(totalAmount);
                result.setConsumeMode(getConsumeMode());

                log.warn("计量模式消费失败: userId={}, message={}",
                        consumeRequest.getUserId(), result.getMessage());
            }

        } catch (Exception e) {
            log.error("处理计量模式消费异常: userId={}", consumeRequest.getUserId(), e);
            result.setSuccess(false);
            result.setMessage("计量消费处理异常");
            result.setStatus(ConsumeStatusEnum.FAILED);
            result.setConsumeMode(getConsumeMode());
        }

        return result;
    }

    @Override
    public BigDecimal calculateAmount(ConsumeRequestDTO consumeRequest) {
        try {
            if (consumeRequest.getMeteringValue() == null) {
                return BigDecimal.ZERO;
            }

            BigDecimal unitPrice = getUnitPrice(consumeRequest);
            return calculateMeteringAmount(consumeRequest.getMeteringValue(), unitPrice);
        } catch (Exception e) {
            log.error("计算计量消费金额失败", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public String getModeDescription() {
        return "计量模式 - 按实际用量计费，适用于水费、电费、燃气费、重量计费等场景";
    }

    @Override
    public boolean isModeAvailable(Long deviceId) {
        // 检查设备是否支持计量模式
        ConsumeModeConfig config = getModeConfig(deviceId);
        return config != null && config.isEnabled();
    }

    @Override
    public ConsumeModeConfig getModeConfig(Long deviceId) {
        try {
            // TODO: 从设备配置中获取计量模式配置
            // 这里简化实现，返回默认配置
            ConsumeModeConfig config = new ConsumeModeConfig();
            config.setModeType(ConsumeModeEnum.METERING);
            config.setEnabled(true);
            config.setMaxSingleAmount(MAX_SINGLE_AMOUNT);
            config.setMinSingleAmount(MIN_SINGLE_AMOUNT);
            config.setDailyLimit(new BigDecimal("1000.00"));
            config.setDescription("计量消费模式配置");
            return config;
        } catch (Exception e) {
            log.error("获取计量模式配置失败: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 获取单价
     */
    private BigDecimal getUnitPrice(ConsumeRequestDTO consumeRequest) {
        // 1. 如果请求中包含单价，使用请求中的单价
        if (consumeRequest.getUnitPrice() != null && consumeRequest.getUnitPrice().compareTo(BigDecimal.ZERO) > 0) {
            return consumeRequest.getUnitPrice();
        }

        // 2. 如果有商品ID，从商品中获取单价
        if (consumeRequest.getProductId() != null) {
            ProductEntity product = getProductById(consumeRequest.getProductId());
            if (product != null && product.getPrice() != null) {
                return product.getPrice();
            }
        }

        // 3. 根据计量类型获取默认单价
        return getDefaultUnitPrice(consumeRequest.getMeteringUnit());
    }

    /**
     * 获取默认单价
     */
    private BigDecimal getDefaultUnitPrice(String unit) {
        // TODO: 从配置中获取不同计量类型的默认单价
        // 这里提供一些示例单价
        switch (unit.toLowerCase()) {
            case "kwh":
                return new BigDecimal("0.60"); // 电费单价
            case "m3":
                return new BigDecimal("3.50"); // 水费单价
            case "kg":
                return new BigDecimal("8.00"); // 重量单价
            case "l":
                return new BigDecimal("7.00"); // 容积单价
            case "m":
                return new BigDecimal("2.00"); // 长度单价
            default:
                return new BigDecimal("1.00"); // 默认单价
        }
    }

    /**
     * 计算计量金额
     */
    private BigDecimal calculateMeteringAmount(BigDecimal meteringValue, BigDecimal unitPrice) {
        return meteringValue.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 验证计量单位是否有效
     */
    private boolean isValidMeteringUnit(String unit) {
        for (MeteringUnitEnum validUnit : MeteringUnitEnum.values()) {
            if (validUnit.getCode().equalsIgnoreCase(unit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取支持的计量单位
     */
    private String getSupportedUnits() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MeteringUnitEnum.values().length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(MeteringUnitEnum.values()[i].getCode());
        }
        return sb.toString();
    }

    /**
     * 获取当前计量读数
     */
    private BigDecimal getCurrentMeteringReading(ConsumeRequestDTO consumeRequest) {
        try {
            String cacheKey = String.format("metering_reading:%d:%s:%s",
                    consumeRequest.getUserId(), consumeRequest.getDeviceId(), consumeRequest.getMeteringUnit());
            Object cachedValue = consumeCacheService.getCachedValue(cacheKey);
            if (cachedValue instanceof BigDecimal) {
                return (BigDecimal) cachedValue;
            } else if (cachedValue != null) {
                return new BigDecimal(cachedValue.toString());
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("获取当前计量读数失败: userId={}, deviceId={}, unit={}",
                    consumeRequest.getUserId(), consumeRequest.getDeviceId(), consumeRequest.getMeteringUnit(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 更新计量读数缓存
     */
    private void updateMeteringReadingCache(ConsumeRequestDTO consumeRequest, BigDecimal newReading) {
        try {
            String cacheKey = String.format("metering_reading:%d:%s:%s",
                    consumeRequest.getUserId(), consumeRequest.getDeviceId(), consumeRequest.getMeteringUnit());
            consumeCacheService.setCachedValue(cacheKey, newReading, 7 * 24 * 60 * 60); // 缓存7天

            log.debug("更新计量读数缓存: userId={}, deviceId={}, unit={}, newReading={}",
                    consumeRequest.getUserId(), consumeRequest.getDeviceId(), consumeRequest.getMeteringUnit(),
                    newReading);
        } catch (Exception e) {
            log.error("更新计量读数缓存失败", e);
        }
    }

    /**
     * 更新计量记录缓存
     */
    private void updateMeteringRecordCache(ConsumeRequestDTO consumeRequest, BigDecimal amount) {
        try {
            String cacheKey = String.format("metering_record:%d:%s:%s",
                    consumeRequest.getUserId(), consumeRequest.getDeviceId(), java.time.LocalDate.now());

            // 获取今日累计用量
            Object cachedUsage = consumeCacheService.getCachedValue(cacheKey);
            BigDecimal todayUsage = BigDecimal.ZERO;
            if (cachedUsage instanceof BigDecimal) {
                todayUsage = (BigDecimal) cachedUsage;
            } else if (cachedUsage != null) {
                todayUsage = new BigDecimal(cachedUsage.toString());
            }

            // 更新累计用量
            BigDecimal totalUsage = todayUsage.add(consumeRequest.getMeteringValue());
            consumeCacheService.setCachedValue(cacheKey, totalUsage, 24 * 60 * 60); // 缓存24小时

            // 更新累计金额
            String amountCacheKey = String.format("metering_amount:%d:%s:%s",
                    consumeRequest.getUserId(), consumeRequest.getDeviceId(), java.time.LocalDate.now());
            Object cachedAmount = consumeCacheService.getCachedValue(amountCacheKey);
            BigDecimal todayAmount = BigDecimal.ZERO;
            if (cachedAmount instanceof BigDecimal) {
                todayAmount = (BigDecimal) cachedAmount;
            } else if (cachedAmount != null) {
                todayAmount = new BigDecimal(cachedAmount.toString());
            }
            consumeCacheService.setCachedValue(amountCacheKey, todayAmount.add(amount), 24 * 60 * 60);

            log.debug("更新计量记录缓存: userId={}, totalUsage={}, totalAmount={}",
                    consumeRequest.getUserId(), totalUsage, todayAmount.add(amount));
        } catch (Exception e) {
            log.error("更新计量记录缓存失败", e);
        }
    }

    /**
     * 验证计量限额
     */
    private boolean validateMeteringLimits(ConsumeRequestDTO consumeRequest) {
        try {
            // 1. 检查单次计量限制
            if (consumeRequest.getMeteringValue().compareTo(new BigDecimal("1000")) > 0) {
                return false; // 单次用量不超过1000单位
            }

            // 2. 检查今日累计限额
            String cacheKey = String.format("metering_record:%d:%s:%s",
                    consumeRequest.getUserId(), consumeRequest.getDeviceId(), java.time.LocalDate.now());
            Object cachedUsage = consumeCacheService.getCachedValue(cacheKey);
            if (cachedUsage != null) {
                BigDecimal todayUsage;
                if (cachedUsage instanceof BigDecimal) {
                    todayUsage = (BigDecimal) cachedUsage;
                } else {
                    todayUsage = new BigDecimal(cachedUsage.toString());
                }
                if (todayUsage.add(consumeRequest.getMeteringValue()).compareTo(new BigDecimal("5000")) > 0) {
                    return false; // 今日累计用量不超过5000单位
                }
            }

            return true;
        } catch (Exception e) {
            log.error("验证计量限额失败", e);
            return false;
        }
    }

    /**
     * 确定计量类型
     */
    private String determineMeteringType(ConsumeRequestDTO consumeRequest) {
        String unit = consumeRequest.getMeteringUnit().toLowerCase();
        if (unit.contains("kwh") || unit.contains("度")) {
            return "电费";
        } else if (unit.contains("m3") || unit.contains("吨")) {
            return "水费";
        } else if (unit.contains("kg") || unit.contains("斤")) {
            return "重量计费";
        } else if (unit.contains("l")) {
            return "容积计费";
        } else {
            return "其他计量";
        }
    }

    /**
     * 构建消费备注
     */
    private String buildConsumeRemark(ConsumeRequestDTO consumeRequest, BigDecimal unitPrice, BigDecimal totalAmount) {
        StringBuilder remark = new StringBuilder();
        remark.append("计量消费: ")
                .append(consumeRequest.getMeteringValue())
                .append(" ").append(consumeRequest.getMeteringUnit())
                .append(" × ¥").append(unitPrice)
                .append(" = ¥").append(totalAmount);

        if (consumeRequest.getRemark() != null && !consumeRequest.getRemark().trim().isEmpty()) {
            remark.append(" 备注: ").append(consumeRequest.getRemark());
        }

        return remark.toString();
    }

    /**
     * 根据商品ID获取商品信息
     */
    private ProductEntity getProductById(Long productId) {
        try {
            String cacheKey = "product:" + productId;
            Object cachedProduct = consumeCacheService.getCachedValue(cacheKey);
            ProductEntity product = null;
            if (cachedProduct instanceof ProductEntity) {
                product = (ProductEntity) cachedProduct;
            }

            if (product == null) {
                product = productDao.selectById(productId);
                if (product != null) {
                    consumeCacheService.setCachedValue(cacheKey, product, 1800); // 30分钟
                }
            }

            return product;
        } catch (Exception e) {
            log.error("根据商品ID获取商品信息失败: productId={}", productId, e);
            return null;
        }
    }

    /**
     * 获取今日计量统计
     */
    public Map<String, Object> getTodayMeteringStats(Long userId, Long deviceId) {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 获取各计量类型的今日用量
            for (MeteringUnitEnum unit : MeteringUnitEnum.values()) {
                String usageKey = String.format("metering_record:%d:%s:%s", userId, deviceId,
                        java.time.LocalDate.now());
                String amountKey = String.format("metering_amount:%d:%s:%s", userId, deviceId,
                        java.time.LocalDate.now());

                Object cachedUsage = consumeCacheService.getCachedValue(usageKey);
                Object cachedAmount = consumeCacheService.getCachedValue(amountKey);
                BigDecimal usage = null;
                BigDecimal amount = null;
                if (cachedUsage instanceof BigDecimal) {
                    usage = (BigDecimal) cachedUsage;
                } else if (cachedUsage != null) {
                    usage = new BigDecimal(cachedUsage.toString());
                }
                if (cachedAmount instanceof BigDecimal) {
                    amount = (BigDecimal) cachedAmount;
                } else if (cachedAmount != null) {
                    amount = new BigDecimal(cachedAmount.toString());
                }

                if (usage != null && amount != null) {
                    Map<String, Object> unitStats = new HashMap<>();
                    unitStats.put("usage", usage);
                    unitStats.put("amount", amount);
                    stats.put(unit.getCode(), unitStats);
                }
            }

            return stats;
        } catch (Exception e) {
            log.error("获取今日计量统计失败: userId={}, deviceId={}", userId, deviceId, e);
            return new HashMap<>();
        }
    }

    /**
     * 获取计量历史记录
     */
    public List<Map<String, Object>> getMeteringHistory(Long userId, Long deviceId, int days) {
        try {
            // TODO: 从数据库获取计量历史记录
            // 这里简化实现，返回空列表
            log.info("获取计量历史记录: userId={}, deviceId={}, days={}", userId, deviceId, days);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取计量历史记录失败", e);
            return new ArrayList<>();
        }
    }
}
