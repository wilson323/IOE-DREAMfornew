package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.DeviceDiscoveryRequestForm;
import net.lab1024.sa.access.domain.vo.DiscoveredDeviceVO;
import net.lab1024.sa.access.domain.vo.DeviceDiscoveryResultVO;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * 门禁设备自动发现服务接口
 * <p>
 * 功能说明：
 * - TCP/UDP多播扫描，自动发现网络中的门禁设备
 * - 支持3种发现协议：ONVIF、私有协议、SNMP
 * - 扫描超时控制在3分钟内
 * - 自动去重，保留设备信息最完整的记录
 * </p>
 * <p>
 * 性能目标：
 * - 1000台设备扫描时间 < 3分钟
 * - 单个设备发现 < 200ms
 * - 并发扫描支持
 * </p>
 * <p>
 * 技术实现：
 * - UDP多播发现（239.255.255.250:1900）
 * - TCP单播验证（端口80/8000/8080）
 * - Redis缓存扫描结果（30分钟）
 * - 异步扫描机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface DeviceDiscoveryService {

    /**
     * 启动设备自动发现
     * <p>
     * 执行TCP/UDP多播扫描，发现网络中的门禁设备
     * </p>
     *
     * @param requestForm 发现请求表单
     * @return 发现结果（包含发现的设备列表）
     */
    ResponseDTO<DeviceDiscoveryResultVO> discoverDevices(DeviceDiscoveryRequestForm requestForm);

    /**
     * 停止设备自动发现
     * <p>
     * 中止正在进行的扫描任务
     * </p>
     *
     * @param scanId 扫描任务ID
     * @return 操作结果
     */
    ResponseDTO<Void> stopDiscovery(String scanId);

    /**
     * 查询发现进度
     * <p>
     * 获取当前扫描任务的进度信息
     * </p>
     *
     * @param scanId 扫描任务ID
     * @return 进度信息
     */
    ResponseDTO<DeviceDiscoveryResultVO> getDiscoveryProgress(String scanId);

    /**
     * 批量添加发现的设备
     * <p>
     * 将发现的设备批量添加到系统中
     * </p>
     *
     * @param devices 设备列表
     * @return 添加结果（成功数量、失败数量）
     */
    ResponseDTO<DeviceDiscoveryResultVO> batchAddDevices(List<DiscoveredDeviceVO> devices);

    /**
     * 导出发现结果
     * <p>
     * 将发现的设备导出为CSV文件（Base64编码）
     * </p>
     *
     * @param scanId 扫描任务ID
     * @return 导出数据（包含fileName、fileData、fileSize、deviceCount）
     */
    ResponseDTO<Map<String, Object>> exportDiscoveryResult(String scanId);
}
