package net.lab1024.sa.devicecomm.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.devicecomm.domain.form.DeviceQueryForm;
import net.lab1024.sa.devicecomm.domain.vo.DeviceListVO;
import net.lab1024.sa.devicecomm.protocol.client.DeviceProtocolClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 设备同步服务
 * <p>
 * 提供设备用户同步、权限管理、健康检查、性能指标收集等功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解
 * - 使用@Resource注入依赖
 * - 完整的异常处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceSyncService {

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private DeviceProtocolClient deviceProtocolClient;

    /**
     * 设备分页查询（管理端使用）
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<DeviceListVO> queryDevices(DeviceQueryForm queryForm) {
        int pageNum = queryForm.getPageNum() != null && queryForm.getPageNum() > 0 ? queryForm.getPageNum() : 1;
        int pageSize = queryForm.getPageSize() != null && queryForm.getPageSize() > 0 ? queryForm.getPageSize() : 20;

        QueryWrapper<DeviceEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(queryForm.getDeviceCode())) {
            wrapper.like("device_code", queryForm.getDeviceCode().trim());
        }
        if (StringUtils.hasText(queryForm.getDeviceName())) {
            wrapper.like("device_name", queryForm.getDeviceName().trim());
        }
        if (StringUtils.hasText(queryForm.getDeviceType())) {
            wrapper.eq("device_type", queryForm.getDeviceType().trim());
        }
        wrapper.orderByDesc("device_id");

        Page<DeviceEntity> page = new Page<>(pageNum, pageSize);
        Page<DeviceEntity> pageResult = deviceDao.selectPage(page, wrapper);

        List<DeviceListVO> list = pageResult.getRecords().stream().map(this::toDeviceListVO).toList();
        return PageResult.of(list, pageResult.getTotal(), pageNum, pageSize);
    }

    /**
     * 删除设备（管理端使用）
     *
     * @param deviceId 设备ID
     */
    public void deleteDevice(Long deviceId) {
        DeviceEntity device = deviceDao.selectById(deviceId);
        if (device == null) {
            throw new BusinessException("DEVICE_NOT_FOUND", "设备不存在: " + deviceId);
        }

        int deleted = deviceDao.deleteById(deviceId);
        if (deleted <= 0) {
            throw new SystemException("DEVICE_DELETE_FAILED", "删除设备失败: " + deviceId);
        }
    }

    private DeviceListVO toDeviceListVO(DeviceEntity entity) {
        DeviceListVO vo = new DeviceListVO();
        vo.setDeviceId(entity.getId());
        vo.setDeviceCode(entity.getDeviceCode());
        vo.setDeviceName(entity.getDeviceName());
        vo.setDeviceType(entity.getDeviceType());

        String location = entity.getIpAddress();
        if (location != null && entity.getPort() != null) {
            location = location + ":" + entity.getPort();
        }
        vo.setLocation(location);

        String deviceStatus = entity.getDeviceStatus();
        vo.setStatus("ONLINE".equalsIgnoreCase(deviceStatus) ? 1 : 0);
        vo.setLastOnlineTime(entity.getLastOnlineTime());
        return vo;
    }

    // 已移除：设备用户列表本地缓存（已迁移到@Cacheable注解）
    // 缓存逻辑使用Spring Cache的@Cacheable注解处理

    /**
     * 同步用户信息到设备
     * <p>
     * 将用户信息同步到指定设备
     * 支持异步处理和状态回调
     * </p>
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @param userInfo 用户详细信息（可选）
     * @return 同步结果
     */
    @CacheEvict(value = "device:users", key = "#deviceId")
    public Map<String, Object> syncUserInfo(String deviceId, Long userId, Map<String, Object> userInfo) {
        log.info("[设备同步服务] 同步用户信息, deviceId={}, userId={}", deviceId, userId);

        try {
            // 1. 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[设备同步服务] 设备不存在, deviceId={}", deviceId);
                throw new BusinessException("DEVICE_NOT_FOUND", "设备不存在: " + deviceId);
            }

            // 2. 构建用户同步数据
            Map<String, Object> syncData = new HashMap<>();
            syncData.put("deviceId", deviceId);
            syncData.put("userId", userId);
            syncData.put("userInfo", userInfo != null ? userInfo : new HashMap<>());
            syncData.put("syncTime", LocalDateTime.now());
            syncData.put("timestamp", System.currentTimeMillis());

            // 3. 实际设备同步逻辑（根据设备协议实现）
            // 这里需要根据具体的设备协议实现同步逻辑
            // 例如：调用设备SDK、发送TCP/UDP消息等
            boolean syncSuccess = deviceProtocolClient.performDeviceUserSync(device, userId, syncData);

            // 4. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("deviceId", deviceId);
            result.put("userId", userId);
            result.put("syncStatus", syncSuccess ? "SUCCESS" : "FAILED");
            result.put("syncTime", LocalDateTime.now());

            // 注意：缓存清除由@CacheEvict注解处理

            log.info("[设备同步服务] 用户信息同步完成, deviceId={}, userId={}, success={}",
                    deviceId, userId, syncSuccess);

            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 同步用户信息参数错误: deviceId={}, userId={}, error={}", deviceId, userId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 同步用户信息业务异常: deviceId={}, userId={}, code={}, message={}", deviceId, userId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[设备同步服务] 同步用户信息系统异常: deviceId={}, userId={}, code={}, message={}", deviceId, userId, e.getCode(), e.getMessage(), e);
            throw new SystemException("SYNC_USER_INFO_SYSTEM_ERROR", "同步用户信息失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[设备同步服务] 同步用户信息未知异常: deviceId={}, userId={}", deviceId, userId, e);
            throw new SystemException("SYNC_USER_INFO_SYSTEM_ERROR", "同步用户信息失败：" + e.getMessage(), e);
        }
    }

    /**
     * 撤销用户在设备上的权限
     * <p>
     * 从指定设备上撤销用户的权限
     * 支持幂等性操作
     * </p>
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @return 撤销结果
     */
    @CacheEvict(value = "device:users", key = "#deviceId")
    public Map<String, Object> revokeUserPermission(String deviceId, Long userId) {
        log.info("[设备同步服务] 撤销用户权限, deviceId={}, userId={}", deviceId, userId);

        try {
            // 1. 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[设备同步服务] 设备不存在, deviceId={}", deviceId);
                throw new BusinessException("DEVICE_NOT_FOUND", "设备不存在: " + deviceId);
            }

            // 2. 实际设备权限撤销逻辑（根据设备协议实现）
            boolean revokeSuccess = deviceProtocolClient.performDeviceUserRevoke(device, userId);

            // 3. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("deviceId", deviceId);
            result.put("userId", userId);
            result.put("revokeStatus", revokeSuccess ? "SUCCESS" : "FAILED");
            result.put("revokeTime", LocalDateTime.now());

            // 注意：缓存清除由@CacheEvict注解处理

            log.info("[设备同步服务] 用户权限撤销完成, deviceId={}, userId={}, success={}",
                    deviceId, userId, revokeSuccess);

            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 撤销用户权限参数错误: deviceId={}, userId={}, error={}", deviceId, userId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 撤销用户权限业务异常: deviceId={}, userId={}, code={}, message={}", deviceId, userId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[设备同步服务] 撤销用户权限系统异常: deviceId={}, userId={}, code={}, message={}", deviceId, userId, e.getCode(), e.getMessage(), e);
            throw new SystemException("REVOKE_USER_PERMISSION_SYSTEM_ERROR", "撤销用户权限失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[设备同步服务] 撤销用户权限未知异常: deviceId={}, userId={}", deviceId, userId, e);
            throw new SystemException("REVOKE_USER_PERMISSION_SYSTEM_ERROR", "撤销用户权限失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取设备上的用户列表
     * <p>
     * 查询指定设备上的所有用户ID列表
     * 使用@Cacheable注解进行缓存（5分钟过期）
     * </p>
     *
     * @param deviceId 设备ID
     * @return 用户ID列表
     */
    @Cacheable(value = "device:users", key = "#deviceId", unless = "#result == null || #result.isEmpty()")
    public List<String> getDeviceUsers(String deviceId) {
        log.debug("[设备同步服务] 获取设备用户列表, deviceId={}", deviceId);

        try {
            // 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[设备同步服务] 设备不存在: deviceId={}", deviceId);
                throw new BusinessException("DEVICE_NOT_FOUND", "设备不存在: " + deviceId);
            }

            // 实际设备用户列表查询逻辑（根据设备协议实现）
            List<String> users = deviceProtocolClient.performDeviceUserQuery(device);

            log.debug("[设备同步服务] 获取设备用户列表成功, deviceId={}, userCount={}", deviceId, users.size());
            return users;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 获取设备用户列表参数错误: deviceId={}, error={}", deviceId, e.getMessage());
            return Collections.emptyList(); // For read-only operations, return empty list on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 获取设备用户列表业务异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return Collections.emptyList(); // For read-only operations, return empty list on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 获取设备用户列表系统异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return Collections.emptyList(); // For read-only operations, return empty list on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 获取设备用户列表未知异常: deviceId={}", deviceId, e);
            // 降级处理：返回空列表
            return Collections.emptyList();
        }
    }

    /**
     * 同步业务属性到设备
     * <p>
     * 将业务属性同步到指定设备
     * 支持部分属性更新和版本控制
     * </p>
     *
     * @param deviceId 设备ID
     * @param attributes 业务属性
     * @return 同步结果
     */
    public Map<String, Object> syncBusinessAttributes(String deviceId, Map<String, Object> attributes) {
        log.info("[设备同步服务] 同步业务属性, deviceId={}, attributeCount={}", deviceId, attributes != null ? attributes.size() : 0);

        try {
            // 1. 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[设备同步服务] 设备不存在, deviceId={}", deviceId);
                throw new BusinessException("DEVICE_NOT_FOUND", "设备不存在: " + deviceId);
            }

            if (attributes == null || attributes.isEmpty()) {
                log.warn("[设备同步服务] 业务属性为空，跳过同步, deviceId={}", deviceId);
                return Map.of("deviceId", deviceId, "syncStatus", "SKIPPED", "reason", "attributes为空");
            }

            // 2. 实际设备业务属性同步逻辑（根据设备协议实现）
            boolean syncSuccess = deviceProtocolClient.performDeviceAttributeSync(device, attributes);

            // 3. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("deviceId", deviceId);
            result.put("syncStatus", syncSuccess ? "SUCCESS" : "FAILED");
            result.put("attributeCount", attributes.size());
            result.put("syncTime", LocalDateTime.now());

            log.info("[设备同步服务] 业务属性同步完成, deviceId={}, success={}", deviceId, syncSuccess);

            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 同步业务属性参数错误: deviceId={}, error={}", deviceId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 同步业务属性业务异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[设备同步服务] 同步业务属性系统异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            throw new SystemException("SYNC_BUSINESS_ATTRIBUTES_SYSTEM_ERROR", "同步业务属性失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[设备同步服务] 同步业务属性未知异常: deviceId={}", deviceId, e);
            throw new SystemException("SYNC_BUSINESS_ATTRIBUTES_SYSTEM_ERROR", "同步业务属性失败：" + e.getMessage(), e);
        }
    }

    /**
     * 设备健康检查
     * <p>
     * 检查指定设备的健康状态
     * 支持超时控制（默认3秒）
     * </p>
     *
     * @param deviceId 设备ID
     * @return 健康检查结果
     */
    public Map<String, Object> checkDeviceHealth(String deviceId) {
        log.debug("[设备同步服务] 设备健康检查, deviceId={}", deviceId);

        try {
            // 1. 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                return Map.of(
                        "deviceId", deviceId,
                        "healthStatus", "UNKNOWN",
                        "message", "设备不存在"
                );
            }

            // 2. 实际设备健康检查逻辑（根据设备协议实现）
            Map<String, Object> healthStatus = deviceProtocolClient.performDeviceHealthCheck(device);

            log.debug("[设备同步服务] 设备健康检查完成, deviceId={}, status={}", deviceId, healthStatus.get("healthStatus"));
            return healthStatus;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 设备健康检查参数错误: deviceId={}, error={}", deviceId, e.getMessage());
            return Map.of("deviceId", deviceId, "healthStatus", "ERROR", "message", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 设备健康检查业务异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return Map.of("deviceId", deviceId, "healthStatus", "ERROR", "message", e.getMessage());
        } catch (SystemException e) {
            log.error("[设备同步服务] 设备健康检查系统异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return Map.of("deviceId", deviceId, "healthStatus", "ERROR", "message", "健康检查失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[设备同步服务] 设备健康检查未知异常: deviceId={}", deviceId, e);
            return Map.of("deviceId", deviceId, "healthStatus", "ERROR", "message", "健康检查失败：" + e.getMessage());
        }
    }

    /**
     * 获取设备性能指标
     * <p>
     * 获取指定设备的性能指标（CPU、内存、网络延迟、响应时间、错误率等）
     * </p>
     *
     * @param deviceId 设备ID
     * @return 性能指标
     */
    public Map<String, Object> getDeviceMetrics(String deviceId) {
        log.debug("[设备同步服务] 获取设备性能指标, deviceId={}", deviceId);

        try {
            // 1. 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[设备同步服务] 设备不存在, deviceId={}", deviceId);
                throw new BusinessException("DEVICE_NOT_FOUND", "设备不存在: " + deviceId);
            }

            // 2. 实际设备性能指标收集逻辑（根据设备协议实现）
            Map<String, Object> metrics = deviceProtocolClient.performDeviceMetricsCollection(device);

            log.debug("[设备同步服务] 获取设备性能指标完成, deviceId={}", deviceId);
            return metrics;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 获取设备性能指标参数错误: deviceId={}, error={}", deviceId, e.getMessage());
            return Map.of("deviceId", deviceId, "cpuUsage", 0.0, "memoryUsage", 0L, "networkLatency", 0.0, "responseTime", 0, "errorRate", 0);
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 获取设备性能指标业务异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return Map.of("deviceId", deviceId, "cpuUsage", 0.0, "memoryUsage", 0L, "networkLatency", 0.0, "responseTime", 0, "errorRate", 0);
        } catch (SystemException e) {
            log.error("[设备同步服务] 获取设备性能指标系统异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            // 降级处理：返回默认值
            return Map.of("deviceId", deviceId, "cpuUsage", 0.0, "memoryUsage", 0L, "networkLatency", 0.0, "responseTime", 0, "errorRate", 0);
        } catch (Exception e) {
            log.error("[设备同步服务] 获取设备性能指标未知异常: deviceId={}", deviceId, e);
            // 降级处理：返回默认值
            return Map.of("deviceId", deviceId, "cpuUsage", 0.0, "memoryUsage", 0L, "networkLatency", 0.0, "responseTime", 0, "errorRate", 0);
        }
    }

    /**
     * 处理设备心跳
     * <p>
     * 接收设备心跳请求，用于设备响应性检查
     * </p>
     *
     * @param deviceId 设备ID
     * @param heartbeatData 心跳数据
     * @return 心跳响应
     */
    public Map<String, Object> processHeartbeat(String deviceId, Map<String, Object> heartbeatData) {
        log.debug("[设备同步服务] 处理设备心跳, deviceId={}", deviceId);

        try {
            // 1. 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                return Map.of("status", "ERROR", "message", "设备不存在");
            }

            // 2. 更新设备最后在线时间
            device.setLastOnlineTime(LocalDateTime.now());
            deviceDao.updateById(device);

            // 3. 构建心跳响应
            Map<String, Object> response = new HashMap<>();
            response.put("status", "OK");
            response.put("deviceId", deviceId);
            response.put("timestamp", System.currentTimeMillis());
            response.put("serverTime", LocalDateTime.now());

            return response;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 处理设备心跳参数错误: deviceId={}, error={}", deviceId, e.getMessage());
            return Map.of("status", "ERROR", "message", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 处理设备心跳业务异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return Map.of("status", "ERROR", "message", e.getMessage());
        } catch (SystemException e) {
            log.error("[设备同步服务] 处理设备心跳系统异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return Map.of("status", "ERROR", "message", "处理心跳失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[设备同步服务] 处理设备心跳未知异常: deviceId={}", deviceId, e);
            return Map.of("status", "ERROR", "message", "处理心跳失败：" + e.getMessage());
        }
    }

}
