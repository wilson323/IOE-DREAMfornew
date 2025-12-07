package net.lab1024.sa.video.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.video.domain.form.VideoDeviceQueryForm;
import net.lab1024.sa.video.domain.vo.VideoDeviceVO;

/**
 * 视频设备服务接口
 * <p>
 * 提供视频设备管理相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口在ioedream-video-service中
 * - 提供统一的业务接口
 * - 支持设备查询和管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VideoDeviceService {

    /**
     * 分页查询视频设备
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<VideoDeviceVO> queryDevices(VideoDeviceQueryForm queryForm);

    /**
     * 查询设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    VideoDeviceVO getDeviceDetail(Long deviceId);
}

