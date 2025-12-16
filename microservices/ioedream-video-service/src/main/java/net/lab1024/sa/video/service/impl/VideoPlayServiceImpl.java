package net.lab1024.sa.video.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.video.domain.vo.VideoDeviceVO;
import net.lab1024.sa.video.service.VideoPlayService;
import net.lab1024.sa.video.service.VideoDeviceService;

/**
 * 视频播放服务实现类
 * <p>
 * 实现视频播放相关业务功能
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
public class VideoPlayServiceImpl implements VideoPlayService {

    private static final String DEFAULT_FALLBACK_HOST = "localhost";

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private VideoDeviceService videoDeviceService;

    @Override
    @Observed(name = "video.play.getStream", contextualName = "video-play-get-stream")
    @Transactional(readOnly = true)
    public Map<String, Object> getVideoStream(Long deviceId, Long channelId, String streamType) {
        log.info("[视频播放] 获取视频流地址，deviceId={}, channelId={}, streamType={}", deviceId, channelId, streamType);

        try {
            // 参数验证
            if (deviceId == null) {
                log.warn("[视频播放] 设备ID不能为空");
                throw new ParamException("DEVICE_ID_REQUIRED", "设备ID不能为空");
            }

            // 查询设备信息
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[视频播放] 设备不存在，deviceId={}", deviceId);
                throw new BusinessException("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 验证是否为视频设备
            if (!"CAMERA".equals(device.getDeviceType())) {
                log.warn("[视频播放] 设备类型不匹配，deviceId={}, deviceType={}", deviceId, device.getDeviceType());
                throw new BusinessException("DEVICE_TYPE_MISMATCH", "设备类型不匹配");
            }

            // 构建视频流地址
            // 默认使用RTSP协议，实际应根据设备配置选择协议
            String protocol = "RTSP"; // 可以从设备扩展属性中获取
            String streamUrl = buildStreamUrl(device, channelId, streamType, protocol);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("streamUrl", streamUrl);
            result.put("streamType", streamType != null ? streamType : "MAIN");
            result.put("protocol", protocol);
            result.put("deviceId", deviceId);
            result.put("deviceName", device.getDeviceName());
            result.put("deviceIp", device.getIpAddress());
            result.put("devicePort", device.getPort());
            if (channelId != null) {
                result.put("channelId", channelId);
            }

            log.info("[视频播放] 获取视频流地址成功，deviceId={}, streamUrl={}", deviceId, streamUrl);
            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[视频播放] 获取视频流地址参数错误: deviceId={}, error={}", deviceId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[视频播放] 获取视频流地址业务异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[视频播放] 获取视频流地址系统异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            throw new SystemException("GET_VIDEO_STREAM_SYSTEM_ERROR", "获取视频流地址失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[视频播放] 获取视频流地址未知异常: deviceId={}", deviceId, e);
            throw new SystemException("GET_VIDEO_STREAM_SYSTEM_ERROR", "获取视频流地址失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "video.play.getSnapshot", contextualName = "video-play-get-snapshot")
    @Transactional(readOnly = true)
    public Map<String, Object> getSnapshot(Long deviceId, Long channelId) {
        log.info("[视频播放] 获取视频截图，deviceId={}, channelId={}", deviceId, channelId);

        try {
            // 参数验证
            if (deviceId == null) {
                log.warn("[视频播放] 设备ID不能为空");
                throw new ParamException("DEVICE_ID_REQUIRED", "设备ID不能为空");
            }

            // 查询设备信息
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[视频播放] 设备不存在，deviceId={}", deviceId);
                throw new BusinessException("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 验证是否为视频设备
            if (!"CAMERA".equals(device.getDeviceType())) {
                log.warn("[视频播放] 设备类型不匹配，deviceId={}, deviceType={}", deviceId, device.getDeviceType());
                throw new BusinessException("DEVICE_TYPE_MISMATCH", "设备类型不匹配");
            }

            // 构建截图URL
            // 实际应从视频设备获取截图，这里返回截图接口地址
            String snapshotUrl = buildSnapshotUrl(device, channelId);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("snapshotUrl", snapshotUrl);
            result.put("deviceId", deviceId);
            result.put("deviceName", device.getDeviceName());
            result.put("timestamp", System.currentTimeMillis());
            if (channelId != null) {
                result.put("channelId", channelId);
            }

            log.info("[视频播放] 获取视频截图成功，deviceId={}, snapshotUrl={}", deviceId, snapshotUrl);
            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[视频播放] 获取视频截图参数错误: deviceId={}, error={}", deviceId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[视频播放] 获取视频截图业务异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[视频播放] 获取视频截图系统异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            throw new SystemException("GET_SNAPSHOT_SYSTEM_ERROR", "获取视频截图失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[视频播放] 获取视频截图未知异常: deviceId={}", deviceId, e);
            throw new SystemException("GET_SNAPSHOT_SYSTEM_ERROR", "获取视频截图失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "video.play.getMobileDeviceList", contextualName = "video-play-get-mobile-device-list")
    @Transactional(readOnly = true)
    public List<VideoDeviceVO> getMobileDeviceList(String areaId, String deviceType, Integer status) {
        log.info("[视频播放] 获取移动端设备列表，areaId={}, deviceType={}, status={}", areaId, deviceType, status);

        try {
            // 构建查询条件
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "CAMERA")
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .eq(DeviceEntity::getEnabledFlag, 1); // 只查询启用的设备

            // 区域筛选
            if (StringUtils.hasText(areaId)) {
                try {
                    Long areaIdLong = Long.parseLong(areaId);
                    wrapper.eq(DeviceEntity::getAreaId, areaIdLong);
                } catch (NumberFormatException e) {
                    log.warn("[视频播放] 区域ID格式错误，areaId={}", areaId);
                    throw new ParamException("INVALID_AREA_ID_FORMAT", "区域ID格式错误, areaId=" + areaId, e);
                }
            }

            // 设备类型筛选（如果指定）
            if (StringUtils.hasText(deviceType)) {
                // 可以根据设备扩展属性筛选
                // 这里暂时不处理，因为设备类型已经在上面固定为CAMERA
            }

            // 设备状态筛选
            if (status != null) {
                String deviceStatus = null;
                if (status == 1) {
                    deviceStatus = "ONLINE";
                } else if (status == 2) {
                    deviceStatus = "OFFLINE";
                } else if (status == 3) {
                    deviceStatus = "MAINTAIN";
                }
                if (deviceStatus != null) {
                    wrapper.eq(DeviceEntity::getDeviceStatus, deviceStatus);
                }
            }

            // 排序
            wrapper.orderByDesc(DeviceEntity::getCreateTime);

            // 执行查询（移动端通常不需要分页，返回所有符合条件的设备）
            List<DeviceEntity> devices = deviceDao.selectList(wrapper);

            // 转换为VO列表
            List<VideoDeviceVO> voList = new ArrayList<>();
            for (DeviceEntity device : devices) {
                VideoDeviceVO vo = convertToVO(device);
                voList.add(vo);
            }

            log.info("[视频播放] 获取移动端设备列表成功，设备数量={}", voList.size());
            return voList;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[视频播放] 获取移动端设备列表参数错误: error={}", e.getMessage());
            return new ArrayList<>(); // For read-only operations, return empty list on parameter error
        } catch (BusinessException e) {
            log.warn("[视频播放] 获取移动端设备列表业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return new ArrayList<>(); // For read-only operations, return empty list on business error
        } catch (SystemException e) {
            log.error("[视频播放] 获取移动端设备列表系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return new ArrayList<>(); // For read-only operations, return empty list on system error
        } catch (Exception e) {
            log.error("[视频播放] 获取移动端设备列表未知异常", e);
            return new ArrayList<>(); // For read-only operations, return empty list on unknown error
        }
    }

    /**
     * 构建视频流地址
     *
     * @param device 设备实体
     * @param channelId 通道ID
     * @param streamType 流类型
     * @param protocol 协议类型
     * @return 视频流地址
     */
    private String buildStreamUrl(DeviceEntity device, Long channelId, String streamType, String protocol) {
        String resolvedStreamType = (streamType == null || streamType.isEmpty()) ? "MAIN" : streamType;
        Long resolvedChannelId = channelId != null ? channelId : 1L;

        // 根据协议构建流地址
        switch (protocol) {
            case "RTSP":
                // RTSP格式：rtsp://username:password@ip:port/path
                // 实际应从设备配置中获取用户名和密码
                String rtspPath = String.format("/Streaming/Channels/%d%02d", resolvedChannelId,
                        "MAIN".equals(resolvedStreamType) ? 1 : 2);
                return String.format("rtsp://%s:%d%s",
                        device.getIpAddress() != null ? device.getIpAddress() : DEFAULT_FALLBACK_HOST,
                        device.getPort() != null ? device.getPort() : 554,
                        rtspPath);
            case "RTMP":
                return String.format("rtmp://%s:%d/live/%d",
                        device.getIpAddress() != null ? device.getIpAddress() : DEFAULT_FALLBACK_HOST,
                        device.getPort() != null ? device.getPort() : 1935,
                        resolvedChannelId);
            case "HLS":
                return String.format("http://%s:%d/hls/%d.m3u8",
                        device.getIpAddress() != null ? device.getIpAddress() : DEFAULT_FALLBACK_HOST,
                        device.getPort() != null ? device.getPort() : 80,
                        resolvedChannelId);
            default:
                // 默认使用RTSP
                return buildStreamUrl(device, resolvedChannelId, resolvedStreamType, "RTSP");
        }
    }

    /**
     * 构建截图URL
     *
     * @param device 设备实体
     * @param channelId 通道ID
     * @return 截图URL
     */
    private String buildSnapshotUrl(DeviceEntity device, Long channelId) {
        Long resolvedChannelId = channelId != null ? channelId : 1L;

        // 构建截图接口地址
        // 实际应从视频设备获取截图，这里返回截图接口地址
        return String.format("http://%s:%d/ISAPI/Streaming/channels/%d/picture",
                device.getIpAddress() != null ? device.getIpAddress() : DEFAULT_FALLBACK_HOST,
                device.getPort() != null ? device.getPort() : 80,
                resolvedChannelId);
    }

    /**
     * 转换Entity为VO
     *
     * @param entity 设备实体
     * @return 设备VO
     */
    private VideoDeviceVO convertToVO(DeviceEntity entity) {
        VideoDeviceVO vo = new VideoDeviceVO();
        vo.setDeviceId(entity.getId());
        vo.setDeviceCode(entity.getDeviceCode());
        vo.setDeviceName(entity.getDeviceName());
        vo.setDeviceType(entity.getDeviceType());
        vo.setAreaId(entity.getAreaId());
        vo.setDeviceIp(entity.getIpAddress());
        vo.setDevicePort(entity.getPort());
        vo.setEnabledFlag(entity.getEnabledFlag() != null ? entity.getEnabledFlag() : 1);
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        // 转换设备状态
        String deviceStatus = entity.getDeviceStatus();
        if ("ONLINE".equals(deviceStatus)) {
            vo.setDeviceStatus(1);
            vo.setDeviceStatusDesc("在线");
        } else if ("OFFLINE".equals(deviceStatus)) {
            vo.setDeviceStatus(2);
            vo.setDeviceStatusDesc("离线");
        } else if ("MAINTAIN".equals(deviceStatus)) {
            vo.setDeviceStatus(3);
            vo.setDeviceStatusDesc("故障");
        } else {
            vo.setDeviceStatus(2);
            vo.setDeviceStatusDesc("离线");
        }

        return vo;
    }
}

