package net.lab1024.sa.admin.module.smart.video.service;

import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.admin.module.smart.video.domain.form.VideoDeviceAddForm;
import net.lab1024.sa.admin.module.smart.video.domain.form.VideoDeviceUpdateForm;
import net.lab1024.sa.admin.module.smart.video.domain.form.VideoDeviceQueryForm;
import net.lab1024.sa.admin.module.smart.video.domain.vo.VideoDeviceVO;

/**
 * 视频设备服务接口
 *
 * <p>提供视频设备的业务逻辑处理，包括：</p>
 * <ul>
 *   <li>设备的增删改查操作</li>
 *   <li>设备状态监控和管理</li>
 *   <li>云台控制功能</li>
 *   <li>设备录像管理</li>
 *   <li>设备分组和权限控制</li>
 * </ul>
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
public interface VideoDeviceService {

    /**
     * 新增视频设备
     *
     * @param addForm 设备新增表单
     * @return 操作结果
     * @throws Exception 业务异常
     */
    String add(VideoDeviceAddForm addForm) throws Exception;

    /**
     * 更新视频设备
     *
     * @param updateForm 设备更新表单
     * @return 操作结果
     * @throws Exception 业务异常
     */
    String update(VideoDeviceUpdateForm updateForm) throws Exception;

    /**
     * 删除视频设备
     *
     * @param deviceId 设备ID
     * @return 操作结果
     * @throws Exception 业务异常
     */
    String delete(Long deviceId) throws Exception;

    /**
     * 分页查询视频设备
     *
     * @param queryForm 查询表单
     * @return 分页结果
     * @throws Exception 业务异常
     */
    PageResult<VideoDeviceVO> page(VideoDeviceQueryForm queryForm) throws Exception;

    /**
     * 根据ID查询设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     * @throws Exception 业务异常
     */
    VideoDeviceVO detail(Long deviceId) throws Exception;

    /**
     * 云台控制
     *
     * @param deviceId 设备ID
     * @param command 控制命令（UP, DOWN, LEFT, RIGHT, ZOOM_IN, ZOOM_OUT）
     * @param speed 控制速度
     * @return 控制结果
     * @throws Exception 业务异常
     */
    String ptzControl(Long deviceId, String command, Integer speed) throws Exception;

    /**
     * 获取设备实时状态
     *
     * @param deviceId 设备ID
     * @return 设备状态信息
     * @throws Exception 业务异常
     */
    String getDeviceStatus(Long deviceId) throws Exception;

    /**
     * 启动设备录像
     *
     * @param deviceId 设备ID
     * @return 录像ID
     * @throws Exception 业务异常
     */
    Long startRecording(Long deviceId) throws Exception;

    /**
     * 停止设备录像
     *
     * @param recordId 录像ID
     * @return 操作结果
     * @throws Exception 业务异常
     */
    String stopRecording(Long recordId) throws Exception;
}