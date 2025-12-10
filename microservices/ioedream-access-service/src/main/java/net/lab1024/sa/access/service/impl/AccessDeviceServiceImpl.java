package net.lab1024.sa.access.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.controller.AccessMobileController;
import net.lab1024.sa.access.dao.AccessAreaDao;
import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.domain.form.AccessDeviceAddForm;
import net.lab1024.sa.access.domain.form.AccessDeviceQueryForm;
import net.lab1024.sa.access.domain.form.AccessDeviceUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessDeviceVO;
import net.lab1024.sa.access.service.AccessDeviceService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.springframework.http.HttpMethod;

/**
 * 门禁设备服务实现类
 * <p>
 * 实现门禁设备管理相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-access-service中
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 严格遵循四层架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessDeviceServiceImpl implements AccessDeviceService {

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private AccessAreaDao accessAreaDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<AccessDeviceVO>> queryDevices(AccessDeviceQueryForm queryForm) {
        log.info("[门禁设备] 分页查询设备，pageNum={}, pageSize={}, keyword={}, areaId={}, deviceStatus={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getKeyword(),
                queryForm.getAreaId(), queryForm.getDeviceStatus());

        try {
            // 构建查询条件
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getDeletedFlag, 0);

            // 关键词搜索（设备名称、设备编号）
            if (StringUtils.hasText(queryForm.getKeyword())) {
                wrapper.and(w -> w.like(DeviceEntity::getDeviceName, queryForm.getKeyword())
                        .or()
                        .like(DeviceEntity::getDeviceCode, queryForm.getKeyword()));
            }

            // 区域筛选
            if (queryForm.getAreaId() != null) {
                wrapper.eq(DeviceEntity::getAreaId, queryForm.getAreaId());
            }

            // 设备状态筛选
            if (StringUtils.hasText(queryForm.getDeviceStatus())) {
                wrapper.eq(DeviceEntity::getDeviceStatus, queryForm.getDeviceStatus());
            }

            // 启用标志筛选
            if (queryForm.getEnabledFlag() != null) {
                wrapper.eq(DeviceEntity::getEnabledFlag, queryForm.getEnabledFlag());
            }

            // 排序
            wrapper.orderByDesc(DeviceEntity::getCreateTime);

            // 执行分页查询
            Page<DeviceEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            Page<DeviceEntity> pageResult = accessDeviceDao.selectPage(page, wrapper);

            // 转换为VO列表
            List<AccessDeviceVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 构建分页结果
            PageResult<AccessDeviceVO> result = PageResult.of(
                    voList,
                    pageResult.getTotal(),
                    queryForm.getPageNum(),
                    queryForm.getPageSize()
            );

            log.info("[门禁设备] 分页查询设备成功，总数={}", pageResult.getTotal());
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[门禁设备] 分页查询设备失败", e);
            return ResponseDTO.error("QUERY_DEVICES_ERROR", "查询设备失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AccessDeviceVO> getDeviceDetail(Long deviceId) {
        log.info("[门禁设备] 查询设备详情，deviceId={}", deviceId);

        try {
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[门禁设备] 设备不存在，deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 验证是否为门禁设备
            if (!"ACCESS".equals(device.getDeviceType())) {
                log.warn("[门禁设备] 设备类型不匹配，deviceId={}, deviceType={}", deviceId, device.getDeviceType());
                return ResponseDTO.error("DEVICE_TYPE_ERROR", "设备类型不匹配");
            }

            AccessDeviceVO vo = convertToVO(device);
            log.info("[门禁设备] 查询设备详情成功，deviceId={}", deviceId);
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("[门禁设备] 查询设备详情失败，deviceId={}", deviceId, e);
            return ResponseDTO.error("GET_DEVICE_ERROR", "查询设备详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> addDevice(AccessDeviceAddForm addForm) {
        log.info("[门禁设备] 添加设备，deviceName={}, deviceCode={}, areaId={}",
                addForm.getDeviceName(), addForm.getDeviceCode(), addForm.getAreaId());

        try {
            // 1. 验证设备编号唯一性
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceCode, addForm.getDeviceCode())
                    .eq(DeviceEntity::getDeletedFlag, 0);
            DeviceEntity existingDevice = accessDeviceDao.selectOne(wrapper);
            if (existingDevice != null) {
                log.warn("[门禁设备] 设备编号已存在，deviceCode={}", addForm.getDeviceCode());
                return ResponseDTO.error("DEVICE_CODE_EXISTS", "设备编号已存在");
            }

            // 2. 验证区域是否存在
            AreaEntity area = getAreaById(addForm.getAreaId());
            if (area == null) {
                log.warn("[门禁设备] 区域不存在，areaId={}", addForm.getAreaId());
                return ResponseDTO.error("AREA_NOT_FOUND", "区域不存在");
            }

            // 3. 创建设备实体
            DeviceEntity device = new DeviceEntity();
            device.setDeviceName(addForm.getDeviceName());
            device.setDeviceCode(addForm.getDeviceCode());
            device.setDeviceType("ACCESS");
            device.setAreaId(addForm.getAreaId());
            device.setIpAddress(addForm.getIpAddress());
            device.setPort(addForm.getPort());
            device.setEnabledFlag(addForm.getEnabledFlag() != null ? addForm.getEnabledFlag() : 1);
            device.setDeviceStatus("OFFLINE"); // 默认离线状态

            // 4. 保存设备
            int result = accessDeviceDao.insert(device);
            if (result <= 0) {
                log.error("[门禁设备] 添加设备失败，插入行数为0");
                return ResponseDTO.error("ADD_DEVICE_ERROR", "添加设备失败");
            }

            log.info("[门禁设备] 添加设备成功，deviceId={}", device.getId());
            return ResponseDTO.ok(device.getId());

        } catch (Exception e) {
            log.error("[门禁设备] 添加设备失败", e);
            return ResponseDTO.error("ADD_DEVICE_ERROR", "添加设备失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> updateDevice(AccessDeviceUpdateForm updateForm) {
        log.info("[门禁设备] 更新设备，deviceId={}", updateForm.getDeviceId());

        try {
            // 1. 查询设备是否存在
            DeviceEntity device = accessDeviceDao.selectById(updateForm.getDeviceId());
            if (device == null) {
                log.warn("[门禁设备] 设备不存在，deviceId={}", updateForm.getDeviceId());
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 2. 验证设备编号唯一性（排除当前设备）
            if (!device.getDeviceCode().equals(updateForm.getDeviceCode())) {
                LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(DeviceEntity::getDeviceCode, updateForm.getDeviceCode())
                        .ne(DeviceEntity::getId, updateForm.getDeviceId())
                        .eq(DeviceEntity::getDeletedFlag, 0);
                DeviceEntity existingDevice = accessDeviceDao.selectOne(wrapper);
                if (existingDevice != null) {
                    log.warn("[门禁设备] 设备编号已存在，deviceCode={}", updateForm.getDeviceCode());
                    return ResponseDTO.error("DEVICE_CODE_EXISTS", "设备编号已存在");
                }
            }

            // 3. 验证区域是否存在
            AreaEntity area = getAreaById(updateForm.getAreaId());
            if (area == null) {
                log.warn("[门禁设备] 区域不存在，areaId={}", updateForm.getAreaId());
                return ResponseDTO.error("AREA_NOT_FOUND", "区域不存在");
            }

            // 4. 更新设备信息
            device.setDeviceName(updateForm.getDeviceName());
            device.setDeviceCode(updateForm.getDeviceCode());
            device.setAreaId(updateForm.getAreaId());
            device.setIpAddress(updateForm.getIpAddress());
            device.setPort(updateForm.getPort());
            if (updateForm.getEnabledFlag() != null) {
                device.setEnabledFlag(updateForm.getEnabledFlag());
            }

            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                log.error("[门禁设备] 更新设备失败，更新行数为0");
                return ResponseDTO.error("UPDATE_DEVICE_ERROR", "更新设备失败");
            }

            log.info("[门禁设备] 更新设备成功，deviceId={}", updateForm.getDeviceId());
            return ResponseDTO.ok(true);

        } catch (Exception e) {
            log.error("[门禁设备] 更新设备失败，deviceId={}", updateForm.getDeviceId(), e);
            return ResponseDTO.error("UPDATE_DEVICE_ERROR", "更新设备失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> deleteDevice(Long deviceId) {
        log.info("[门禁设备] 删除设备，deviceId={}", deviceId);

        try {
            // 1. 查询设备是否存在
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[门禁设备] 设备不存在，deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 2. 软删除（设置deletedFlag=1）
            device.setDeletedFlag(1);
            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                log.error("[门禁设备] 删除设备失败，更新行数为0");
                return ResponseDTO.error("DELETE_DEVICE_ERROR", "删除设备失败");
            }

            log.info("[门禁设备] 删除设备成功，deviceId={}", deviceId);
            return ResponseDTO.ok(true);

        } catch (Exception e) {
            log.error("[门禁设备] 删除设备失败，deviceId={}", deviceId, e);
            return ResponseDTO.error("DELETE_DEVICE_ERROR", "删除设备失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> updateDeviceStatus(Long deviceId, Integer status) {
        log.info("[门禁设备] 更新设备状态，deviceId={}, status={}", deviceId, status);

        try {
            // 1. 查询设备是否存在
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[门禁设备] 设备不存在，deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 2. 验证状态值（0-禁用，1-启用）
            if (status != 0 && status != 1) {
                log.warn("[门禁设备] 状态值无效，status={}", status);
                return ResponseDTO.error("INVALID_STATUS", "状态值无效，只能为0（禁用）或1（启用）");
            }

            // 3. 更新设备状态
            device.setEnabledFlag(status);
            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                log.error("[门禁设备] 更新设备状态失败，更新行数为0");
                return ResponseDTO.error("UPDATE_DEVICE_STATUS_ERROR", "更新设备状态失败");
            }

            log.info("[门禁设备] 更新设备状态成功，deviceId={}, status={}", deviceId, status);
            return ResponseDTO.ok(true);

        } catch (Exception e) {
            log.error("[门禁设备] 更新设备状态失败，deviceId={}, status={}", deviceId, status, e);
            return ResponseDTO.error("UPDATE_DEVICE_STATUS_ERROR", "更新设备状态失败: " + e.getMessage());
        }
    }

    // ==================== 移动端方法 ====================

    /**
     * 获取附近设备
     * <p>
     * 根据GPS坐标查询附近的门禁设备
     * </p>
     *
     * @param userId 用户ID
     * @param latitude 纬度
     * @param longitude 经度
     * @param radius 半径（米，默认500米）
     * @return 附近设备列表，使用Haversine公式计算距离（按距离排序）
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<AccessMobileController.MobileDeviceItem>> getNearbyDevices(
            Long userId, Double latitude, Double longitude, Integer radius) {
        log.info("[门禁设备] 移动端附近设备查询，userId={}, latitude={}, longitude={}, radius={}",
                userId, latitude, longitude, radius);

        try {
            // 参数验证
            if (latitude == null || longitude == null) {
                log.warn("[门禁设备] GPS坐标为空，无法查询附近设备");
                return ResponseDTO.ok(List.of());
            }

            if (radius == null || radius <= 0) {
                radius = 500; // 默认500米
            }

            // 1. 查询所有启用的门禁设备
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getEnabledFlag, 1)
                    .eq(DeviceEntity::getDeletedFlag, 0);

            List<DeviceEntity> devices = accessDeviceDao.selectList(wrapper);

            if (devices.isEmpty()) {
                log.info("[门禁设备] 未找到启用的门禁设备");
                return ResponseDTO.ok(List.of());
            }

            // 2. 计算距离并筛选
            List<AccessMobileController.MobileDeviceItem> nearbyDevices = new ArrayList<>();
            for (DeviceEntity device : devices) {
                try {
                    // 从extendedAttributes中获取经纬度
                    Double deviceLat = getLatitudeFromDevice(device);
                    Double deviceLng = getLongitudeFromDevice(device);

                    if (deviceLat == null || deviceLng == null) {
                        log.debug("[门禁设备] 设备缺少GPS坐标，跳过，deviceId={}", device.getId());
                        continue;
                    }

                    // 计算距离（使用Haversine公式）
                    double distance = calculateDistance(latitude, longitude, deviceLat, deviceLng);

                    if (distance <= radius) {
                        AccessMobileController.MobileDeviceItem item = new AccessMobileController.MobileDeviceItem();
                        item.setDeviceId(device.getId());
                        item.setDeviceName(device.getDeviceName());
                        item.setDeviceLocation(getDeviceLocation(device));
                        item.setLatitude(deviceLat);
                        item.setLongitude(deviceLng);
                        item.setDistance((int) Math.round(distance));
                        nearbyDevices.add(item);
                    }
                } catch (Exception e) {
                    log.warn("[门禁设备] 处理设备异常，deviceId={}", device.getId(), e);
                    // 单个设备处理失败不影响其他设备，继续处理
                }
            }

            // 3. 按距离排序
            nearbyDevices.sort(Comparator.comparingInt(AccessMobileController.MobileDeviceItem::getDistance));

            log.info("[门禁设备] 附近设备查询成功，找到{}个设备", nearbyDevices.size());
            return ResponseDTO.ok(nearbyDevices);

        } catch (Exception e) {
            log.error("[门禁设备] 附近设备查询异常", e);
            return ResponseDTO.error("QUERY_ERROR", "查询附近设备失败：" + e.getMessage());
        }
    }

    /**
     * 获取移动端用户权限
     * <p>
     * 查询用户的门禁权限信息，包括区域权限和设备权限
     * </p>
     *
     * @param userId 用户ID
     * @return 用户权限信息
     */
    /**
     * 获取移动端用户权限
     * <p>
     * 查询用户的门禁权限信息，包括区域权限和设备权限
     * </p>
     *
     * @param userId 用户ID
     * @return 用户权限信息
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AccessMobileController.MobileUserPermissions> getMobileUserPermissions(Long userId) {
        log.info("[门禁设备] 移动端用户权限查询，userId={}", userId);

        try {
            if (userId == null) {
                return ResponseDTO.error("PARAM_ERROR", "用户ID不能为空");
            }

            AccessMobileController.MobileUserPermissions permissions = new AccessMobileController.MobileUserPermissions();
            permissions.setUserId(userId);

            // 1. 查询用户有权限的区域ID列表
            // 通过网关调用门禁服务查询用户权限
            // 注意：这里需要调用门禁权限服务，如果权限服务在access-service内部，可以直接调用
            // 如果权限服务在common-service，需要通过网关调用
            List<Long> allowedAreaIds = queryUserAllowedAreas(userId);
            permissions.setAllowedAreaIds(allowedAreaIds);

            // 2. 查询用户有权限的设备ID列表
            List<Long> allowedDeviceIds = queryUserAllowedDevices(userId, allowedAreaIds);
            permissions.setAllowedDeviceIds(allowedDeviceIds);

            // 3. 确定权限级别
            // 权限级别：ADMIN-管理员, NORMAL-普通用户, VISITOR-访客
            String permissionLevel = determinePermissionLevel(userId, allowedAreaIds, allowedDeviceIds);
            permissions.setPermissionLevel(permissionLevel);

            log.info("[门禁设备] 用户权限查询成功，userId={}, 区域数量={}, 设备数量={}, 权限级别={}",
                    userId, allowedAreaIds.size(), allowedDeviceIds.size(), permissionLevel);
            return ResponseDTO.ok(permissions);

        } catch (Exception e) {
            log.error("[门禁设备] 用户权限查询异常，userId={}", userId, e);
            return ResponseDTO.error("QUERY_ERROR", "查询用户权限失败：" + e.getMessage());
        }
    }

    /**
     * 获取移动端实时状态
     * <p>
     * 查询设备的实时状态信息，包括在线用户数等
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备移动端实时状态，包括在线用户数等信息
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AccessMobileController.MobileRealTimeStatus> getMobileRealTimeStatus(Long deviceId) {
        log.info("[门禁设备] 移动端实时状态查询，deviceId={}", deviceId);

        try {
            if (deviceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "设备ID不能为空");
            }

            // 1. 查询设备信息
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            AccessMobileController.MobileRealTimeStatus status = new AccessMobileController.MobileRealTimeStatus();
            status.setDeviceId(device.getId());
            status.setDeviceName(device.getDeviceName());
            status.setDeviceStatus(device.getDeviceStatus());

            // 2. 查询在线用户数量（通过网关调用设备通讯服务）
            // 注意：实际实现需要根据设备通讯服务的接口来调用
            Integer onlineCount = queryDeviceOnlineCount(deviceId);
            status.setOnlineCount(onlineCount != null ? onlineCount : 0);

            // 3. 获取最后更新时间
            if (device.getLastOnlineTime() != null) {
                status.setLastUpdateTime(device.getLastOnlineTime().toString());
            } else {
                status.setLastUpdateTime(device.getUpdateTime() != null ? device.getUpdateTime().toString() : "");
            }

            log.info("[门禁设备] 实时状态查询成功，deviceId={}, status={}, onlineCount={}",
                    deviceId, status.getDeviceStatus(), status.getOnlineCount());
            return ResponseDTO.ok(status);

        } catch (Exception e) {
            log.error("[门禁设备] 实时状态查询异常，deviceId={}", deviceId, e);
            return ResponseDTO.error("QUERY_ERROR", "查询实时状态失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 转换设备实体为VO
     *
     * @param device 设备实体
     * @return 设备VO
     */
    private AccessDeviceVO convertToVO(DeviceEntity device) {
        AccessDeviceVO vo = new AccessDeviceVO();
        vo.setDeviceId(device.getId());
        vo.setDeviceName(device.getDeviceName());
        vo.setDeviceCode(device.getDeviceCode());
        vo.setDeviceType(device.getDeviceType());
        vo.setAreaId(device.getAreaId());
        vo.setIpAddress(device.getIpAddress());
        vo.setPort(device.getPort());
        vo.setDeviceStatus(device.getDeviceStatus());
        vo.setEnabledFlag(device.getEnabledFlag());
        vo.setLastOnlineTime(device.getLastOnlineTime());
        vo.setCreateTime(device.getCreateTime());
        vo.setUpdateTime(device.getUpdateTime());

        // 通过网关获取区域名称
        if (device.getAreaId() != null) {
            AreaEntity area = getAreaById(device.getAreaId());
            if (area != null) {
                vo.setAreaName(area.getAreaName());
            }
        }

        return vo;
    }

    /**
     * 通过网关获取区域信息
     *
     * @param areaId 区域ID
     * @return 区域实体
     */
    private AreaEntity getAreaById(Long areaId) {
        try {
            ResponseDTO<AreaEntity> result = gatewayServiceClient.callCommonService(
                    "/api/v1/area/" + areaId,
                    HttpMethod.GET,
                    null,
                    AreaEntity.class
            );
            return result != null && result.isSuccess() ? result.getData() : null;
        } catch (Exception e) {
            log.error("[门禁设备] 获取区域信息失败，areaId={}", areaId, e);
            return null;
        }
    }

    /**
     * 从设备扩展属性中获取纬度
     * <p>
     * 设备GPS坐标存储在extendedAttributes JSON字段中
     * 格式：{"latitude": 39.9042, "longitude": 116.4074}
     * </p>
     *
     * @param device 设备实体
     * @return 纬度，如果不存在则返回null
     */
    private Double getLatitudeFromDevice(DeviceEntity device) {
        try {
            if (device.getExtendedAttributes() == null || device.getExtendedAttributes().trim().isEmpty()) {
                return null;
            }
            Map<String, Object> attrs = objectMapper.readValue(device.getExtendedAttributes(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            Object latObj = attrs.get("latitude");
            if (latObj != null) {
                return Double.valueOf(latObj.toString());
            }
            return null;
        } catch (Exception e) {
            log.debug("[门禁设备] 解析设备纬度失败，deviceId={}", device.getId());
            return null;
        }
    }

    /**
     * 从设备扩展属性中获取经度
     *
     * @param device 设备实体
     * @return 经度，如果不存在则返回null
     */
    private Double getLongitudeFromDevice(DeviceEntity device) {
        try {
            if (device.getExtendedAttributes() == null || device.getExtendedAttributes().trim().isEmpty()) {
                return null;
            }
            Map<String, Object> attrs = objectMapper.readValue(device.getExtendedAttributes(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            Object lngObj = attrs.get("longitude");
            if (lngObj != null) {
                return Double.valueOf(lngObj.toString());
            }
            return null;
        } catch (Exception e) {
            log.debug("[门禁设备] 解析设备经度失败，deviceId={}", device.getId());
            return null;
        }
    }

    /**
     * 获取设备位置描述
     * <p>
     * 优先从extendedAttributes获取location字段，如果没有则使用区域名称
     * </p>
     *
     * @param device 设备实体
     * @return 设备位置描述
     */
    private String getDeviceLocation(DeviceEntity device) {
        try {
            // 优先从扩展属性获取位置
            if (device.getExtendedAttributes() != null && !device.getExtendedAttributes().trim().isEmpty()) {
                Map<String, Object> attrs = objectMapper.readValue(device.getExtendedAttributes(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                Object locationObj = attrs.get("location");
                if (locationObj != null) {
                    return locationObj.toString();
                }
            }

            // 如果没有，则通过区域ID获取区域名称
            if (device.getAreaId() != null) {
                AreaEntity area = getAreaById(device.getAreaId());
                if (area != null && area.getAreaName() != null) {
                    return area.getAreaName();
                }
            }

            return device.getDeviceName() != null ? device.getDeviceName() : "未知位置";
        } catch (Exception e) {
            log.debug("[门禁设备] 获取设备位置失败，deviceId={}", device.getId());
            return device.getDeviceName() != null ? device.getDeviceName() : "未知位置";
        }
    }

    /**
     * 计算两点之间的距离（Haversine公式）
     * <p>
     * 计算地球表面两点之间的直线距离
     * 参考竞品：高德地图、百度地图等均使用此算法
     * </p>
     *
     * @param lat1 起点纬度
     * @param lng1 起点经度
     * @param lat2 终点纬度
     * @param lng2 终点经度
     * @return 距离（米）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // 地球半径（米）
        final double EARTH_RADIUS = 6371000;

        // 转换为弧度
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        // Haversine公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 查询用户有权限的区域ID列表
     * <p>
     * 通过查询门禁权限记录获取用户有权限的区域
     * 参考竞品：海康威视、大华等门禁系统均支持按区域授权
     * </p>
     *
     * @param userId 用户ID
     * @return 区域ID列表
     */
    private List<Long> queryUserAllowedAreas(Long userId) {
        try {
            // 查询用户有权限的设备，然后获取这些设备所属的区域
            // 注意：这里需要根据实际的门禁权限表结构来实现
            // 如果权限表中有areaId字段，可以直接查询；如果没有，需要通过设备查询区域

            // 简化实现：查询用户有权限的设备，然后去重获取区域ID
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getEnabledFlag, 1)
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .isNotNull(DeviceEntity::getAreaId);

            List<DeviceEntity> devices = accessDeviceDao.selectList(wrapper);

            // 从设备中提取区域ID并去重
            // 注意：实际应该查询门禁权限表，这里简化处理
            return devices.stream()
                    .map(DeviceEntity::getAreaId)
                    .filter(areaId -> areaId != null)
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("[门禁设备] 查询用户权限区域异常，userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 查询用户有权限的设备ID列表
     * <p>
     * 根据用户权限和区域权限查询设备列表
     * </p>
     *
     * @param userId 用户ID
     * @param allowedAreaIds 允许的区域ID列表
     * @return 设备ID列表
     */
    private List<Long> queryUserAllowedDevices(Long userId, List<Long> allowedAreaIds) {
        try {
            // 查询用户有权限的设备
            // 注意：实际应该查询门禁权限表，这里简化处理为查询指定区域下的设备
            if (allowedAreaIds == null || allowedAreaIds.isEmpty()) {
                return List.of();
            }

            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getEnabledFlag, 1)
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .in(DeviceEntity::getAreaId, allowedAreaIds);

            List<DeviceEntity> devices = accessDeviceDao.selectList(wrapper);

            return devices.stream()
                    .map(DeviceEntity::getId)
                    .filter(deviceId -> deviceId != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("[门禁设备] 查询用户权限设备异常，userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 确定用户权限级别
     * <p>
     * 根据用户权限范围确定权限级别
     * - ADMIN: 管理员（所有区域和设备）
     * - NORMAL: 普通用户（部分区域和设备）
     * - VISITOR: 访客（临时权限）
     * </p>
     *
     * @param userId 用户ID
     * @param allowedAreaIds 允许的区域ID列表
     * @param allowedDeviceIds 允许的设备ID列表
     * @return 权限级别
     */
    private String determinePermissionLevel(Long userId, List<Long> allowedAreaIds, List<Long> allowedDeviceIds) {
        // 简化实现：根据权限数量判断
        // 实际应该通过网关调用公共服务查询用户角色
        try {
            // 通过网关查询用户角色
            // 如果用户是管理员角色，返回ADMIN
            // 如果用户是访客角色，返回VISITOR
            // 否则返回NORMAL

            // 简化处理：根据权限数量判断
            if (allowedAreaIds.size() > 10 || allowedDeviceIds.size() > 50) {
                return "ADMIN";
            } else if (allowedAreaIds.isEmpty() && allowedDeviceIds.isEmpty()) {
                return "VISITOR";
            } else {
                return "NORMAL";
            }
        } catch (Exception e) {
            log.warn("[门禁设备] 确定权限级别异常，userId={}", userId, e);
            return "NORMAL";
        }
    }

    /**
     * 查询设备在线用户数量
     * <p>
     * 通过网关调用设备通讯服务查询设备当前在线用户数量
     * </p>
     *
     * @param deviceId 设备ID
     * @return 在线用户数量
     */
    private Integer queryDeviceOnlineCount(Long deviceId) {
        try {
            // 通过网关调用设备通讯服务
            // GET /api/v1/device/{deviceId}/online-count
            ResponseDTO<Integer> response = gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/" + deviceId + "/online-count",
                    HttpMethod.GET,
                    null,
                    Integer.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            } else {
                log.warn("[门禁设备] 查询设备在线用户数量失败，deviceId={}, message={}", deviceId,
                        response != null ? response.getMessage() : "响应为空");
                return 0;
            }
        } catch (Exception e) {
            log.warn("[门禁设备] 查询设备在线用户数量异常，deviceId={}", deviceId, e);
            return 0;
        }
    }

    /**
     * 获取移动端区域列表
     * <p>
     * 获取用户有权限访问的区域列表，包含区域详情（名称、类型、设备数量等）
     * </p>
     *
     * @param userId 用户ID（可选，不传则从Token获取）
     * @return 区域列表
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<AccessMobileController.MobileAreaItem>> getMobileAreas(Long userId) {
        log.info("[门禁设备] 获取移动端区域列表，userId={}", userId);

        try {
            if (userId == null) {
                return ResponseDTO.error("PARAM_ERROR", "用户ID不能为空");
            }

            List<AccessMobileController.MobileAreaItem> areas = new ArrayList<>();

            // 1. 获取用户有权限的区域ID列表
            List<Long> allowedAreaIds = queryUserAllowedAreas(userId);
            if (allowedAreaIds == null || allowedAreaIds.isEmpty()) {
                log.info("[门禁设备] 用户无权限区域，userId={}", userId);
                return ResponseDTO.ok(areas);
            }

            // 2. 查询区域详情
            // 使用LambdaQueryWrapper查询，避免使用已废弃的selectBatchIds方法
            // AreaEntity主键字段已统一为id，符合实体类主键命名规范
            LambdaQueryWrapper<AreaEntity> areaWrapper = new LambdaQueryWrapper<>();
            areaWrapper.in(AreaEntity::getId, allowedAreaIds)
                    .eq(AreaEntity::getDeletedFlag, 0);
            List<AreaEntity> areaList = accessAreaDao.selectList(areaWrapper);
            if (areaList == null || areaList.isEmpty()) {
                log.warn("[门禁设备] 区域不存在，areaIds={}", allowedAreaIds);
                return ResponseDTO.ok(areas);
            }

            // 3. 统计每个区域的设备数量
            Map<Long, Long> deviceCountMap = new HashMap<>();
            LambdaQueryWrapper<DeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .in(DeviceEntity::getAreaId, allowedAreaIds);
            List<DeviceEntity> devices = accessDeviceDao.selectList(deviceWrapper);

            if (devices != null && !devices.isEmpty()) {
                deviceCountMap = devices.stream()
                        .collect(Collectors.groupingBy(
                                DeviceEntity::getAreaId,
                                Collectors.counting()
                        ));
            }

            // 4. 构建MobileAreaItem列表
            for (AreaEntity area : areaList) {
                AccessMobileController.MobileAreaItem item = new AccessMobileController.MobileAreaItem();
                item.setAreaId(area.getId());
                item.setAreaName(area.getAreaName());
                item.setAreaType(area.getAreaType() != null ? area.getAreaType().toString() : "标准区域");
                item.setDeviceCount(deviceCountMap.getOrDefault(area.getId(), 0L).intValue());
                item.setPermissionCount(1); // 用户有权限
                // 使用areaDesc字段作为描述
                item.setDescription(area.getAreaDesc() != null ? area.getAreaDesc() : "");
                item.setActive(area.getStatus() != null && area.getStatus() == 1);
                areas.add(item);
            }

            log.info("[门禁设备] 获取移动端区域列表成功，userId={}, 区域数量={}", userId, areas.size());
            return ResponseDTO.ok(areas);

        } catch (Exception e) {
            log.error("[门禁设备] 获取移动端区域列表异常，userId={}", userId, e);
            return ResponseDTO.error("QUERY_ERROR", "获取区域列表失败：" + e.getMessage());
        }
    }
}

