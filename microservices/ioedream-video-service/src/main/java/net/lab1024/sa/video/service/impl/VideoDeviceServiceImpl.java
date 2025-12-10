package net.lab1024.sa.video.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.video.domain.form.VideoDeviceQueryForm;
import net.lab1024.sa.video.domain.vo.VideoDeviceVO;
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

    @Override
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
                // 状态转换：1-在线(ONLINE), 2-离线(OFFLINE), 3-故障(MAINTAIN)
                String deviceStatus = null;
                if (queryForm.getStatus() == 1) {
                    deviceStatus = "ONLINE";
                } else if (queryForm.getStatus() == 2) {
                    deviceStatus = "OFFLINE";
                } else if (queryForm.getStatus() == 3) {
                    deviceStatus = "MAINTAIN";
                }
                if (deviceStatus != null) {
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
                    queryForm.getPageSize()
            );

            log.info("[视频设备] 分页查询设备成功，总数={}", pageResult.getTotal());
            return result;

        } catch (Exception e) {
            log.error("[视频设备] 分页查询设备失败", e);
            return PageResult.of(new java.util.ArrayList<>(), 0L, queryForm.getPageNum(), queryForm.getPageSize());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VideoDeviceVO getDeviceDetail(Long deviceId) {
        log.info("[视频设备] 查询设备详情，deviceId={}", deviceId);

        try {
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[视频设备] 设备不存在，deviceId={}", deviceId);
                return null;
            }

            // 验证是否为视频设备
            if (!"CAMERA".equals(device.getDeviceType())) {
                log.warn("[视频设备] 设备类型不匹配，deviceId={}, deviceType={}", deviceId, device.getDeviceType());
                return null;
            }

            VideoDeviceVO vo = convertToVO(device);
            log.info("[视频设备] 查询设备详情成功，deviceId={}", deviceId);
            return vo;

        } catch (Exception e) {
            log.error("[视频设备] 查询设备详情失败，deviceId={}", deviceId, e);
            return null;
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
        vo.setDeviceId(entity.getId());
        vo.setDeviceCode(entity.getDeviceCode());
        vo.setDeviceName(entity.getDeviceName());
        vo.setDeviceType(entity.getDeviceType());
        vo.setAreaId(entity.getAreaId());
        // areaName需要从区域服务获取，这里暂时为空
        vo.setAreaName(null);
        vo.setDeviceIp(entity.getIpAddress());
        vo.setDevicePort(entity.getPort());
        vo.setEnabledFlag(entity.getEnabledFlag() != null ? entity.getEnabledFlag() : 1);
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        // 转换设备状态
        String deviceStatus = entity.getDeviceStatus();
        if ("ONLINE".equals(deviceStatus)) {
            vo.setDeviceStatus(1); // 在线
            vo.setDeviceStatusDesc("在线");
        } else if ("OFFLINE".equals(deviceStatus)) {
            vo.setDeviceStatus(2); // 离线
            vo.setDeviceStatusDesc("离线");
        } else if ("MAINTAIN".equals(deviceStatus)) {
            vo.setDeviceStatus(3); // 故障
            vo.setDeviceStatusDesc("故障");
        } else {
            vo.setDeviceStatus(2); // 默认离线
            vo.setDeviceStatusDesc("离线");
        }

        return vo;
    }
}

