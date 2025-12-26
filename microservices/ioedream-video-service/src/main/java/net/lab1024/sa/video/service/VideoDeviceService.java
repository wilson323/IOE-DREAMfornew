package net.lab1024.sa.video.service;

import jakarta.validation.Valid;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.form.VideoDeviceAddForm;
import net.lab1024.sa.video.domain.form.VideoDeviceQueryForm;
import net.lab1024.sa.video.domain.form.VideoDeviceUpdateForm;
import net.lab1024.sa.video.domain.vo.VideoDeviceVO;

import java.util.List;

/**
 * 视频设备服务接口
 * <p>
 * 提供视频设备管理相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口在ioedream-video-service中
 * - 提供统一的业务接口
 * - 支持设备查询和管理
 * - 包含完整的CRUD操作
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
    PageResult<VideoDeviceVO> queryDevices(@Valid VideoDeviceQueryForm queryForm);

    /**
     * 查询设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    ResponseDTO<VideoDeviceVO> getDeviceDetail(Long deviceId);

    /**
     * 新增视频设备
     *
     * @param addForm 新增表单
     * @return 操作结果
     */
    ResponseDTO<VideoDeviceVO> addDevice(@Valid VideoDeviceAddForm addForm);

    /**
     * 更新视频设备
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    ResponseDTO<Void> updateDevice(@Valid VideoDeviceUpdateForm updateForm);

    /**
     * 删除视频设备
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteDevice(Long deviceId);

    /**
     * 批量删除视频设备
     *
     * @param deviceIds 设备ID列表
     * @return 操作结果
     */
    ResponseDTO<Void> batchDeleteDevices(List<Long> deviceIds);

    /**
     * 启用/禁用设备
     *
     * @param deviceId 设备ID
     * @param enabledFlag 启用标志：0-禁用，1-启用
     * @return 操作结果
     */
    ResponseDTO<Void> toggleDeviceStatus(Long deviceId, Integer enabledFlag);

    /**
     * 设备上线
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<Void> deviceOnline(Long deviceId);

    /**
     * 设备离线
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<Void> deviceOffline(Long deviceId);

    /**
     * 检测设备连通性
     *
     * @param deviceId 设备ID
     * @return 检测结果
     */
    ResponseDTO<Boolean> checkDeviceConnectivity(Long deviceId);

    /**
     * 重启设备
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<Void> restartDevice(Long deviceId);

    /**
     * 根据区域ID查询设备列表
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    ResponseDTO<List<VideoDeviceVO>> getDevicesByAreaId(Long areaId);

    /**
     * 查询在线设备列表
     *
     * @return 在线设备列表
     */
    ResponseDTO<List<VideoDeviceVO>> getOnlineDevices();

    /**
     * 查询离线设备列表
     *
     * @return 离线设备列表
     */
    ResponseDTO<List<VideoDeviceVO>> getOfflineDevices();

    /**
     * 查询支持PTZ的设备列表
     *
     * @return PTZ设备列表
     */
    ResponseDTO<List<VideoDeviceVO>> getPTZDevices();

    /**
     * 查询支持AI的设备列表
     *
     * @return AI设备列表
     */
    ResponseDTO<List<VideoDeviceVO>> getAIDevices();

    /**
     * 获取设备统计信息
     *
     * @return 统计信息
     */
    ResponseDTO<Object> getDeviceStatistics();

    /**
     * 同步设备时间
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<Void> syncDeviceTime(Long deviceId);

    /**
     * 获取设备配置信息
     *
     * @param deviceId 设备ID
     * @return 配置信息
     */
    ResponseDTO<Object> getDeviceConfig(Long deviceId);

    /**
     * 更新设备配置信息
     *
     * @param deviceId 设备ID
     * @param config 配置信息
     * @return 操作结果
     */
    ResponseDTO<Void> updateDeviceConfig(Long deviceId, Object config);
}

