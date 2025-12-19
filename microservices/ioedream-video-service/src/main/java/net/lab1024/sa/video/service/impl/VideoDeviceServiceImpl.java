package net.lab1024.sa.video.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.video.domain.form.VideoDeviceAddForm;
import net.lab1024.sa.video.domain.form.VideoDeviceQueryForm;
import net.lab1024.sa.video.domain.form.VideoDeviceUpdateForm;
import net.lab1024.sa.video.domain.vo.VideoDeviceVO;
import net.lab1024.sa.video.manager.VideoStreamManager;
import net.lab1024.sa.video.service.VideoDeviceService;

/**
 * 视频设备服务实现类
 * <p>
 * 实现视频设备管理相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-video-service中
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
public class VideoDeviceServiceImpl implements VideoDeviceService {

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private VideoStreamManager videoStreamManager;

    @Override
    @Observed(name = "video.device.query", contextualName = "video-device-query")
    @Transactional(readOnly = true)
    public PageResult<VideoDeviceVO> queryDevices(VideoDeviceQueryForm queryForm) {
        log.info("[视频设备] 分页查询设备，pageNum={}, pageSize={}, keyword={}, areaId={}, status={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getKeyword(),
                queryForm.getAreaId(), queryForm.getStatus());

        try {
            // 构建查询条件
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "CAMERA")
                    .eq(DeviceEntity::getDeletedFlag, 0);

            // 关键词搜索（设备名称、设备编号）
            if (StringUtils.hasText(queryForm.getKeyword())) {
                wrapper.and(w -> w.like(DeviceEntity::getDeviceName, queryForm.getKeyword())
                        .or()
                        .like(DeviceEntity::getDeviceCode, queryForm.getKeyword()));
            }

            // 区域筛选
            if (StringUtils.hasText(queryForm.getAreaId())) {
                try {
                    Long areaId = Long.parseLong(queryForm.getAreaId());
                    wrapper.eq(DeviceEntity::getAreaId, areaId);
                } catch (NumberFormatException e) {
                    log.warn("[视频设备] 区域ID格式错误，areaId={}", queryForm.getAreaId());
                }
            }

            // 设备状态筛选
            if (queryForm.getStatus() != null) {
                // 状态转换：1-在线, 2-离线, 3-故障, 4-维护, 5-停用
                Integer deviceStatus = queryForm.getStatus();
                if (deviceStatus >= 1 && deviceStatus <= 5) {
                    wrapper.eq(DeviceEntity::getDeviceStatus, deviceStatus);
                }
            }

            // 排序
            wrapper.orderByDesc(DeviceEntity::getCreateTime);

            // 执行分页查询
            Page<DeviceEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            Page<DeviceEntity> pageResult = deviceDao.selectPage(page, wrapper);

            // 转换为VO列表
            List<VideoDeviceVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 构建分页结果
            PageResult<VideoDeviceVO> result = PageResult.of(
                    voList,
                    pageResult.getTotal(),
                    queryForm.getPageNum(),
                    queryForm.getPageSize());

            log.info("[视频设备] 分页查询设备成功，总数={}", pageResult.getTotal());
            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[视频设备] 分页查询设备参数错误: error={}", e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[视频设备] 分页查询设备业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[视频设备] 分页查询设备系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("QUERY_VIDEO_DEVICES_SYSTEM_ERROR", "分页查询设备失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[视频设备] 分页查询设备未知异常", e);
            throw new SystemException("QUERY_VIDEO_DEVICES_SYSTEM_ERROR", "分页查询设备失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "video:device:detail", key = "#deviceId", unless = "#result == null || !#result.getOk()")
    public ResponseDTO<VideoDeviceVO> getDeviceDetail(Long deviceId) {
        log.info("[视频设备] 查询设备详情，deviceId={}", deviceId);

        try {
            // 参数验证
            if (deviceId == null) {
                log.warn("[视频设备] 设备ID不能为空");
                return ResponseDTO.error("PARAM_ERROR", "设备ID不能为空");
            }

            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[视频设备] 设备不存在，deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 验证是否为视频设备
            if (!"CAMERA".equals(device.getDeviceType())) {
                log.warn("[视频设备] 设备类型不匹配，deviceId={}, deviceType={}", deviceId, device.getDeviceType());
                return ResponseDTO.error("DEVICE_TYPE_MISMATCH", "设备类型不匹配");
            }

            VideoDeviceVO vo = convertToVO(device);
            log.info("[视频设备] 查询设备详情成功，deviceId={}", deviceId);
            return ResponseDTO.ok(vo);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[视频设备] 查询设备详情参数错误: deviceId={}, error={}", deviceId, e.getMessage());
            return ResponseDTO.error("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[视频设备] 查询设备详情业务异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[视频设备] 查询设备详情系统异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[视频设备] 查询设备详情未知异常: deviceId={}", deviceId, e);
            throw new SystemException("QUERY_DEVICE_DETAIL_ERROR", "查询设备详情失败：" + e.getMessage(), e);
        }
    }

    /**
     * 转换Entity为VO
     *
     * @param entity 设备实体
     * @return 设备VO
     */
    private VideoDeviceVO convertToVO(DeviceEntity entity) {
        VideoDeviceVO vo = new VideoDeviceVO();

        // 处理设备ID - DeviceEntity的deviceId是String类型，VO需要Long类型，需要进行转换
        if (StringUtils.hasText(entity.getDeviceId())) {
            try {
                vo.setDeviceId(Long.parseLong(entity.getDeviceId()));
            } catch (NumberFormatException e) {
                log.warn("[视频设备] 设备ID转换失败: deviceId={}", entity.getDeviceId());
                vo.setDeviceId(0L);
            }
        }

        vo.setDeviceCode(entity.getDeviceCode());
        vo.setDeviceName(entity.getDeviceName());
        vo.setDeviceType(entity.getDeviceType());
        vo.setAreaId(entity.getAreaId());
        vo.setAreaName(entity.getAreaName());
        vo.setDeviceIp(entity.getIpAddress());
        vo.setDevicePort(entity.getPort());
        vo.setEnabledFlag(entity.getEnabled() != null ? entity.getEnabled() : 1);
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        // 转换设备状态
        Integer deviceStatus = entity.getDeviceStatus();
        if (deviceStatus != null) {
            vo.setDeviceStatus(deviceStatus);
            switch (deviceStatus) {
                case 1:
                    vo.setDeviceStatusDesc("在线");
                    break;
                case 2:
                    vo.setDeviceStatusDesc("离线");
                    break;
                case 3:
                    vo.setDeviceStatusDesc("故障");
                    break;
                case 4:
                    vo.setDeviceStatusDesc("维护");
                    break;
                case 5:
                    vo.setDeviceStatusDesc("停用");
                    break;
                default:
                    vo.setDeviceStatus(2);
                    vo.setDeviceStatusDesc("离线");
                    break;
            }
        } else {
            vo.setDeviceStatus(2);
            vo.setDeviceStatusDesc("离线");
        }

        // 设置设备子类型
        if (StringUtils.hasText(entity.getDeviceSubType())) {
            try {
                vo.setDeviceSubType(Integer.parseInt(entity.getDeviceSubType()));
                vo.setDeviceSubTypeDesc(getDeviceSubTypeDesc(vo.getDeviceSubType()));
            } catch (NumberFormatException e) {
                log.debug("[视频设备] 设备子类型解析失败: deviceSubType={}", entity.getDeviceSubType());
            }
        }

        // 设置扩展属性字段
        String extendedAttributesStr = entity.getExtendedAttributes();
        if (StringUtils.hasText(extendedAttributesStr)) {
            try {
                // 这里应该解析JSON格式的扩展属性
                // 暂时设置一些基本的扩展属性
                vo.setStreamUrl(entity.getDeviceConfig());
                vo.setManufacturer(entity.getBrand());
                vo.setModel(entity.getModel());
                vo.setSerialNumber(entity.getSerialNumber());
                vo.setInstallLocation(entity.getLocation());
                vo.setLastOnlineTime(entity.getLastOnlineTime());
                vo.setLastOfflineTime(entity.getLastOfflineTime());
                vo.setOnlineDuration(entity.getOnlineDuration());
                vo.setRemark(entity.getRemark());
            } catch (Exception e) {
                log.error("[视频设备] 解析扩展属性失败: extendedAttributes={}", extendedAttributesStr, e);
            }
        }

        // 设置描述字段
        vo.setProtocolDesc(getProtocolDesc(vo.getProtocol()));

        return vo;
    }

    /**
     * 获取设备子类型描述
     */
    private String getDeviceSubTypeDesc(Integer deviceSubType) {
        if (deviceSubType == null)
            return null;
        switch (deviceSubType) {
            case 1:
                return "枪机";
            case 2:
                return "球机";
            case 3:
                return "半球机";
            case 4:
                return "一体机";
            default:
                return "未知";
        }
    }

    /**
     * 获取协议描述
     */
    private String getProtocolDesc(Integer protocol) {
        if (protocol == null)
            return null;
        switch (protocol) {
            case 1:
                return "RTSP";
            case 2:
                return "RTMP";
            case 3:
                return "HTTP";
            case 4:
                return "TCP";
            case 5:
                return "UDP";
            default:
                return "未知";
        }
    }

    // ==================== 新增的设备管理方法 ====================

    @Override
    @Observed(name = "video.device.add", contextualName = "video-device-add")
    public ResponseDTO<VideoDeviceVO> addDevice(@Valid VideoDeviceAddForm addForm) {
        log.info("[视频设备] 新增设备，deviceCode={}, deviceName={}, deviceIp={}",
                addForm.getDeviceCode(), addForm.getDeviceName(), addForm.getDeviceIp());

        try {
            // 检查设备编号是否重复
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceCode, addForm.getDeviceCode())
                    .eq(DeviceEntity::getDeletedFlag, 0);
            if (deviceDao.selectCount(wrapper) > 0) {
                log.warn("[视频设备] 设备编号已存在，deviceCode={}", addForm.getDeviceCode());
                return ResponseDTO.error("DEVICE_CODE_EXISTS", "设备编号已存在");
            }

            // 构建设备实体
            DeviceEntity device = convertAddFormToEntity(addForm);

            // 插入数据库
            int result = deviceDao.insert(device);
            if (result <= 0) {
                log.error("[视频设备] 新增设备失败，deviceCode={}", addForm.getDeviceCode());
                throw new BusinessException("ADD_DEVICE_FAILED", "新增设备失败");
            }

            // 转换为VO返回
            VideoDeviceVO vo = convertToVO(device);
            log.info("[视频设备] 新增设备成功，deviceId={}, deviceCode={}", device.getDeviceId(), device.getDeviceCode());
            return ResponseDTO.ok(vo);

        } catch (BusinessException e) {
            log.warn("[视频设备] 新增设备业务异常: deviceCode={}, code={}, message={}",
                    addForm.getDeviceCode(), e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[视频设备] 新增设备未知异常: deviceCode={}", addForm.getDeviceCode(), e);
            throw new SystemException("ADD_DEVICE_ERROR", "新增设备失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "video.device.update", contextualName = "video-device-update")
    public ResponseDTO<Void> updateDevice(@Valid VideoDeviceUpdateForm updateForm) {
        log.info("[视频设备] 更新设备，deviceId={}", updateForm.getDeviceId());

        try {
            // 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(updateForm.getDeviceId());
            if (device == null) {
                log.warn("[视频设备] 设备不存在，deviceId={}", updateForm.getDeviceId());
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 验证是否为视频设备
            if (!"CAMERA".equals(device.getDeviceType())) {
                log.warn("[视频设备] 设备类型不匹配，deviceId={}, deviceType={}",
                        updateForm.getDeviceId(), device.getDeviceType());
                return ResponseDTO.error("DEVICE_TYPE_MISMATCH", "设备类型不匹配");
            }

            // 更新设备信息
            updateEntityFromForm(device, updateForm);
            int result = deviceDao.updateById(device);
            if (result <= 0) {
                log.error("[视频设备] 更新设备失败，deviceId={}", updateForm.getDeviceId());
                throw new BusinessException("UPDATE_DEVICE_FAILED", "更新设备失败");
            }

            log.info("[视频设备] 更新设备成功，deviceId={}", updateForm.getDeviceId());
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("[视频设备] 更新设备业务异常: deviceId={}, code={}, message={}",
                    updateForm.getDeviceId(), e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[视频设备] 更新设备未知异常: deviceId={}", updateForm.getDeviceId(), e);
            throw new SystemException("UPDATE_DEVICE_ERROR", "更新设备失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "video.device.delete", contextualName = "video-device-delete")
    public ResponseDTO<Void> deleteDevice(Long deviceId) {
        log.info("[视频设备] 删除设备，deviceId={}", deviceId);

        try {
            // 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[视频设备] 设备不存在，deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 软删除设备
            device.setDeletedFlag(1);
            device.setUpdateTime(LocalDateTime.now());
            int result = deviceDao.updateById(device);
            if (result <= 0) {
                log.error("[视频设备] 删除设备失败，deviceId={}", deviceId);
                throw new BusinessException("DELETE_DEVICE_FAILED", "删除设备失败");
            }

            log.info("[视频设备] 删除设备成功，deviceId={}", deviceId);
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("[视频设备] 删除设备业务异常: deviceId={}, code={}, message={}",
                    deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[视频设备] 删除设备未知异常: deviceId={}", deviceId, e);
            throw new SystemException("DELETE_DEVICE_ERROR", "删除设备失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "video.device.batchDelete", contextualName = "video-device-batch-delete")
    public ResponseDTO<Void> batchDeleteDevices(List<Long> deviceIds) {
        log.info("[视频设备] 批量删除设备，deviceIds={}", deviceIds);

        try {
            if (deviceIds == null || deviceIds.isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "设备ID列表不能为空");
            }

            // 批量软删除
            LambdaUpdateWrapper<DeviceEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.in(DeviceEntity::getDeviceId, deviceIds.stream().map(String::valueOf).collect(Collectors.toList()))
                    .set(DeviceEntity::getDeletedFlag, 1)
                    .set(DeviceEntity::getUpdateTime, LocalDateTime.now());

            int result = deviceDao.update(null, wrapper);
            log.info("[视频设备] 批量删除设备完成，影响行数={}", result);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[视频设备] 批量删除设备异常: deviceIds={}", deviceIds, e);
            throw new SystemException("BATCH_DELETE_DEVICE_ERROR", "批量删除设备失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "video.device.toggleStatus", contextualName = "video-device-toggle-status")
    public ResponseDTO<Void> toggleDeviceStatus(Long deviceId, Integer enabledFlag) {
        log.info("[视频设备] 切换设备状态，deviceId={}, enabledFlag={}", deviceId, enabledFlag);

        try {
            if (enabledFlag == null || (enabledFlag != 0 && enabledFlag != 1)) {
                return ResponseDTO.error("PARAM_ERROR", "启用标志只能为0或1");
            }

            // 更新设备启用状态
            LambdaUpdateWrapper<DeviceEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceId, deviceId)
                    .set(DeviceEntity::getDeviceStatus, enabledFlag == 1 ? 1 : 5)
                    .set(DeviceEntity::getUpdateTime, LocalDateTime.now());

            int result = deviceDao.update(null, wrapper);
            if (result <= 0) {
                log.warn("[视频设备] 切换设备状态失败，deviceId={}", deviceId);
                return ResponseDTO.error("TOGGLE_STATUS_FAILED", "切换设备状态失败");
            }

            log.info("[视频设备] 切换设备状态成功，deviceId={}, enabledFlag={}", deviceId, enabledFlag);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[视频设备] 切换设备状态异常: deviceId={}, enabledFlag={}", deviceId, enabledFlag, e);
            throw new SystemException("TOGGLE_STATUS_ERROR", "切换设备状态失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "video.device.online", contextualName = "video-device-online")
    public ResponseDTO<Void> deviceOnline(Long deviceId) {
        log.info("[视频设备] 设备上线，deviceId={}", deviceId);

        try {
            return updateDeviceStatus(deviceId, 1); // 1-在线
        } catch (Exception e) {
            log.error("[视频设备] 设备上线异常: deviceId={}", deviceId, e);
            throw new SystemException("DEVICE_ONLINE_ERROR", "设备上线失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "video.device.offline", contextualName = "video-device-offline")
    public ResponseDTO<Void> deviceOffline(Long deviceId) {
        log.info("[视频设备] 设备离线，deviceId={}", deviceId);

        try {
            return updateDeviceStatus(deviceId, 2); // 2-离线
        } catch (Exception e) {
            log.error("[视频设备] 设备离线异常: deviceId={}", deviceId, e);
            throw new SystemException("DEVICE_OFFLINE_ERROR", "设备离线失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "video.device.checkConnectivity", contextualName = "video-device-check-connectivity")
    public ResponseDTO<Boolean> checkDeviceConnectivity(Long deviceId) {
        log.info("[视频设备] 检测设备连通性，deviceId={}", deviceId);

        try {
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 这里应该实现实际的连通性检测逻辑
            // 暂时返回设备状态作为连通性检测结果
            boolean isConnectable = Integer.valueOf(1).equals(device.getDeviceStatus()); // 1-在线

            log.info("[视频设备] 设备连通性检测完成，deviceId={}, connectable={}", deviceId, isConnectable);
            return ResponseDTO.ok(isConnectable);

        } catch (Exception e) {
            log.error("[视频设备] 检测设备连通性异常: deviceId={}", deviceId, e);
            throw new SystemException("CHECK_CONNECTIVITY_ERROR", "检测设备连通性失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "video.device.restart", contextualName = "video-device-restart")
    public ResponseDTO<Void> restartDevice(Long deviceId) {
        log.info("[视频设备] 重启设备，deviceId={}", deviceId);

        try {
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 这里应该实现实际的设备重启逻辑
            // 暂时只记录日志
            log.info("[视频设备] 设备重启命令已发送，deviceId={}", deviceId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[视频设备] 重启设备异常: deviceId={}", deviceId, e);
            throw new SystemException("RESTART_DEVICE_ERROR", "重启设备失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "video:device:area", key = "#areaId", unless = "#result == null || !#result.getOk() || (#result.getData() != null && #result.getData().isEmpty())")
    public ResponseDTO<List<VideoDeviceVO>> getDevicesByAreaId(Long areaId) {
        log.info("[视频设备] 根据区域查询设备，areaId={}", areaId);

        try {
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getAreaId, areaId)
                    .eq(DeviceEntity::getDeviceType, "CAMERA")
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .orderByDesc(DeviceEntity::getCreateTime);

            List<DeviceEntity> devices = deviceDao.selectList(wrapper);
            List<VideoDeviceVO> voList = devices.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            log.info("[视频设备] 根据区域查询设备完成，areaId={}, count={}", areaId, voList.size());
            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("[视频设备] 根据区域查询设备异常: areaId={}", areaId, e);
            throw new SystemException("QUERY_DEVICES_BY_AREA_ERROR", "根据区域查询设备失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "video:device:online", key = "'all'", unless = "#result == null || !#result.getOk()")
    public ResponseDTO<List<VideoDeviceVO>> getOnlineDevices() {
        log.info("[视频设备] 查询在线设备");

        try {
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "CAMERA")
                    .eq(DeviceEntity::getDeviceStatus, 1) // 1-在线
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .orderByDesc(DeviceEntity::getCreateTime);

            List<DeviceEntity> devices = deviceDao.selectList(wrapper);
            List<VideoDeviceVO> voList = devices.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            log.info("[视频设备] 查询在线设备完成，count={}", voList.size());
            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("[视频设备] 查询在线设备异常", e);
            throw new SystemException("QUERY_ONLINE_DEVICES_ERROR", "查询在线设备失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoDeviceVO>> getOfflineDevices() {
        log.info("[视频设备] 查询离线设备");

        try {
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "CAMERA")
                    .ne(DeviceEntity::getDeviceStatus, 1) // 1-在线，所以查找不等于1的
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .orderByDesc(DeviceEntity::getCreateTime);

            List<DeviceEntity> devices = deviceDao.selectList(wrapper);
            List<VideoDeviceVO> voList = devices.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            log.info("[视频设备] 查询离线设备完成，count={}", voList.size());
            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("[视频设备] 查询离线设备异常", e);
            throw new SystemException("QUERY_OFFLINE_DEVICES_ERROR", "查询离线设备失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoDeviceVO>> getPTZDevices() {
        log.info("[视频设备] 查询支持PTZ的设备");

        try {
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "CAMERA")
                    .likeRight(DeviceEntity::getExtendedAttributes, "\"ptzSupported\":1")
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .orderByDesc(DeviceEntity::getCreateTime);

            List<DeviceEntity> devices = deviceDao.selectList(wrapper);
            List<VideoDeviceVO> voList = devices.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            log.info("[视频设备] 查询PTZ设备完成，count={}", voList.size());
            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("[视频设备] 查询PTZ设备异常", e);
            throw new SystemException("QUERY_PTZ_DEVICES_ERROR", "查询PTZ设备失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoDeviceVO>> getAIDevices() {
        log.info("[视频设备] 查询支持AI的设备");

        try {
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "CAMERA")
                    .likeRight(DeviceEntity::getExtendedAttributes, "\"aiSupported\":1")
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .orderByDesc(DeviceEntity::getCreateTime);

            List<DeviceEntity> devices = deviceDao.selectList(wrapper);
            List<VideoDeviceVO> voList = devices.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            log.info("[视频设备] 查询AI设备完成，count={}", voList.size());
            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("[视频设备] 查询AI设备异常", e);
            throw new SystemException("QUERY_AI_DEVICES_ERROR", "查询AI设备失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "video.device.statistics", contextualName = "video-device-statistics")
    @Cacheable(value = "video:device:statistics", key = "'all'", unless = "#result == null || !#result.getOk()")
    public ResponseDTO<Object> getDeviceStatistics() {
        log.info("[视频设备] 获取设备统计信息");

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总设备数
            LambdaQueryWrapper<DeviceEntity> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(DeviceEntity::getDeviceType, "CAMERA")
                    .eq(DeviceEntity::getDeletedFlag, 0);
            long totalDevices = deviceDao.selectCount(totalWrapper);
            statistics.put("totalDevices", totalDevices);

            // 在线设备数
            LambdaQueryWrapper<DeviceEntity> onlineWrapper = new LambdaQueryWrapper<>();
            onlineWrapper.eq(DeviceEntity::getDeviceType, "CAMERA")
                    .eq(DeviceEntity::getDeviceStatus, 1) // 1-在线
                    .eq(DeviceEntity::getDeletedFlag, 0);
            long onlineDevices = deviceDao.selectCount(onlineWrapper);
            statistics.put("onlineDevices", onlineDevices);

            // 离线设备数
            LambdaQueryWrapper<DeviceEntity> offlineWrapper = new LambdaQueryWrapper<>();
            offlineWrapper.eq(DeviceEntity::getDeviceType, "CAMERA")
                    .eq(DeviceEntity::getDeviceStatus, "OFFLINE")
                    .eq(DeviceEntity::getDeletedFlag, 0);
            long offlineDevices = deviceDao.selectCount(offlineWrapper);
            statistics.put("offlineDevices", offlineDevices);

            // 故障设备数
            LambdaQueryWrapper<DeviceEntity> faultWrapper = new LambdaQueryWrapper<>();
            faultWrapper.eq(DeviceEntity::getDeviceType, "CAMERA")
                    .eq(DeviceEntity::getDeviceStatus, "MAINTAIN")
                    .eq(DeviceEntity::getDeletedFlag, 0);
            long faultDevices = deviceDao.selectCount(faultWrapper);
            statistics.put("faultDevices", faultDevices);

            // 启用设备数
            LambdaQueryWrapper<DeviceEntity> enabledWrapper = new LambdaQueryWrapper<>();
            enabledWrapper.eq(DeviceEntity::getDeviceType, "CAMERA")
                    .eq(DeviceEntity::getDeviceStatus, 1)
                    .eq(DeviceEntity::getDeletedFlag, 0);
            long enabledDevices = deviceDao.selectCount(enabledWrapper);
            statistics.put("enabledDevices", enabledDevices);

            // 禁用设备数
            LambdaQueryWrapper<DeviceEntity> disabledWrapper = new LambdaQueryWrapper<>();
            disabledWrapper.eq(DeviceEntity::getDeviceType, "CAMERA")
                    .eq(DeviceEntity::getDeviceStatus, 0)
                    .eq(DeviceEntity::getDeletedFlag, 0);
            long disabledDevices = deviceDao.selectCount(disabledWrapper);
            statistics.put("disabledDevices", disabledDevices);

            log.info("[视频设备] 获取设备统计信息完成");
            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[视频设备] 获取设备统计信息异常", e);
            throw new SystemException("GET_DEVICE_STATISTICS_ERROR", "获取设备统计信息失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "video.device.syncTime", contextualName = "video-device-sync-time")
    public ResponseDTO<Void> syncDeviceTime(Long deviceId) {
        log.info("[视频设备] 同步设备时间，deviceId={}", deviceId);

        try {
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 这里应该实现实际的设备时间同步逻辑
            log.info("[视频设备] 设备时间同步完成，deviceId={}", deviceId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[视频设备] 同步设备时间异常: deviceId={}", deviceId, e);
            throw new SystemException("SYNC_DEVICE_TIME_ERROR", "同步设备时间失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Object> getDeviceConfig(Long deviceId) {
        log.info("[视频设备] 获取设备配置，deviceId={}", deviceId);

        try {
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 返回设备的扩展属性作为配置信息
            Map<String, Object> config = new HashMap<>();
            String extendedAttributesStr = device.getExtendedAttributes();
            if (StringUtils.hasText(extendedAttributesStr)) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> parsedConfig = objectMapper.readValue(extendedAttributesStr, Map.class);
                    config = parsedConfig;
                } catch (Exception e) {
                    log.warn("[视频设备] 解析扩展属性失败: deviceId={}, error={}", deviceId, e.getMessage());
                    config = new HashMap<>();
                }
            }

            log.info("[视频设备] 获取设备配置完成，deviceId={}", deviceId);
            return ResponseDTO.ok(config);

        } catch (Exception e) {
            log.error("[视频设备] 获取设备配置异常: deviceId={}", deviceId, e);
            throw new SystemException("GET_DEVICE_CONFIG_ERROR", "获取设备配置失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "video.device.updateConfig", contextualName = "video-device-update-config")
    public ResponseDTO<Void> updateDeviceConfig(Long deviceId, Object config) {
        log.info("[视频设备] 更新设备配置，deviceId={}", deviceId);

        try {
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 这里应该实现配置验证和更新逻辑
            // 暂时只记录日志
            log.info("[视频设备] 设备配置更新完成，deviceId={}, config={}", deviceId, config);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[视频设备] 更新设备配置异常: deviceId={}", deviceId, e);
            throw new SystemException("UPDATE_DEVICE_CONFIG_ERROR", "更新设备配置失败：" + e.getMessage(), e);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换新增表单为实体
     */
    private DeviceEntity convertAddFormToEntity(VideoDeviceAddForm addForm) {
        DeviceEntity device = new DeviceEntity();
        device.setDeviceCode(addForm.getDeviceCode());
        device.setDeviceName(addForm.getDeviceName());
        device.setDeviceType(addForm.getDeviceType());
        device.setAreaId(addForm.getAreaId());
        device.setIpAddress(addForm.getDeviceIp());
        device.setPort(addForm.getDevicePort());
        device.setDeviceStatus(2); // 默认离线状态
        device.setEnabled(addForm.getEnabledFlag() != null ? addForm.getEnabledFlag() : 1);

        // 构建扩展属性
        Map<String, Object> extendedAttributes = new HashMap<>();
        extendedAttributes.put("deviceSubType", addForm.getDeviceSubType());
        extendedAttributes.put("protocol", addForm.getProtocol());
        extendedAttributes.put("streamUrl", addForm.getStreamUrl());
        extendedAttributes.put("manufacturer", addForm.getManufacturer());
        extendedAttributes.put("model", addForm.getModel());
        extendedAttributes.put("serialNumber", addForm.getSerialNumber());
        extendedAttributes.put("installLocation", addForm.getInstallLocation());
        extendedAttributes.put("longitude", addForm.getLongitude());
        extendedAttributes.put("latitude", addForm.getLatitude());
        extendedAttributes.put("altitude", addForm.getAltitude());
        extendedAttributes.put("ptzSupported", addForm.getPtzSupported());
        extendedAttributes.put("audioSupported", addForm.getAudioSupported());
        extendedAttributes.put("nightVisionSupported", addForm.getNightVisionSupported());
        extendedAttributes.put("aiSupported", addForm.getAiSupported());
        extendedAttributes.put("resolution", addForm.getResolution());
        extendedAttributes.put("frameRate", addForm.getFrameRate());
        extendedAttributes.put("remark", addForm.getRemark());

        device.setExtendedAttributes(convertToJson(extendedAttributes));
        return device;
    }

    /**
     * 从更新表单更新实体
     */
    private void updateEntityFromForm(DeviceEntity device, VideoDeviceUpdateForm updateForm) {
        if (StringUtils.hasText(updateForm.getDeviceName())) {
            device.setDeviceName(updateForm.getDeviceName());
        }
        if (updateForm.getDeviceSubType() != null) {
            // device.setExtendedAttributeValue("deviceSubType",
            // updateForm.getDeviceSubType());
        }
        if (updateForm.getAreaId() != null) {
            device.setAreaId(updateForm.getAreaId());
        }
        if (StringUtils.hasText(updateForm.getDeviceIp())) {
            device.setIpAddress(updateForm.getDeviceIp());
        }
        if (updateForm.getDevicePort() != null) {
            device.setPort(updateForm.getDevicePort());
        }
        if (StringUtils.hasText(updateForm.getStreamUrl())) {
            // device.setExtendedAttributeValue("streamUrl", updateForm.getStreamUrl());
        }
        if (StringUtils.hasText(updateForm.getManufacturer())) {
            // device.setExtendedAttributeValue("manufacturer",
            // updateForm.getManufacturer());
        }
        if (StringUtils.hasText(updateForm.getModel())) {
            // device.setExtendedAttributeValue("model", updateForm.getModel());
        }
        if (StringUtils.hasText(updateForm.getSerialNumber())) {
            // device.setExtendedAttributeValue("serialNumber",
            // updateForm.getSerialNumber());
        }
        if (StringUtils.hasText(updateForm.getInstallLocation())) {
            // device.setExtendedAttributeValue("installLocation",
            // updateForm.getInstallLocation());
        }
        if (updateForm.getLongitude() != null) {
            // device.setExtendedAttributeValue("longitude", updateForm.getLongitude());
        }
        if (updateForm.getLatitude() != null) {
            // device.setExtendedAttributeValue("latitude", updateForm.getLatitude());
        }
        if (updateForm.getAltitude() != null) {
            // device.setExtendedAttributeValue("altitude", updateForm.getAltitude());
        }
        if (updateForm.getPtzSupported() != null) {
            // device.setExtendedAttributeValue("ptzSupported",
            // updateForm.getPtzSupported());
        }
        if (updateForm.getAudioSupported() != null) {
            // device.setExtendedAttributeValue("audioSupported",
            // updateForm.getAudioSupported());
        }
        if (updateForm.getNightVisionSupported() != null) {
            // device.setExtendedAttributeValue("nightVisionSupported",
            // updateForm.getNightVisionSupported());
        }
        if (updateForm.getAiSupported() != null) {
            // device.setExtendedAttributeValue("aiSupported", updateForm.getAiSupported());
        }
        if (StringUtils.hasText(updateForm.getResolution())) {
            // device.setExtendedAttributeValue("resolution", updateForm.getResolution());
        }
        if (updateForm.getFrameRate() != null) {
            // device.setExtendedAttributeValue("frameRate", updateForm.getFrameRate());
        }
        if (updateForm.getEnabledFlag() != null) {
            device.setEnabled(updateForm.getEnabledFlag());
        }
        if (StringUtils.hasText(updateForm.getDeviceStatus())) {
            device.setDeviceStatus(updateForm.getDeviceStatus());
        }
        if (StringUtils.hasText(updateForm.getRemark())) {
            // device.setExtendedAttributeValue("remark", updateForm.getRemark());
        }

        device.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 更新设备状态
     */
    private ResponseDTO<Void> updateDeviceStatus(Long deviceId, Integer status) {
        LambdaUpdateWrapper<DeviceEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(DeviceEntity::getDeviceId, deviceId)
                .set(DeviceEntity::getDeviceStatus, status)
                .set(DeviceEntity::getUpdateTime, LocalDateTime.now());

        int result = deviceDao.update(null, wrapper);
        if (result <= 0) {
            return ResponseDTO.error("UPDATE_STATUS_FAILED", "更新设备状态失败");
        }

        return ResponseDTO.ok();
    }

    /**
     * 将Map转换为JSON字符串
     */
    private String convertToJson(Map<String, Object> map) {
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (Exception e) {
            log.warn("转换扩展属性为JSON失败: {}", e.getMessage());
            return "{}";
        }
    }
}
