package net.lab1024.sa.device.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.device.form.DeviceDataCollectionForm;
import net.lab1024.sa.device.domain.form.DeviceProtocolConfigForm;
import net.lab1024.sa.device.domain.vo.DeviceDataVO;
import net.lab1024.sa.device.domain.vo.DeviceProtocolVO;

/**
 * 设备通信服务接口
 * 基于现有设备通信模式重构，提供统一设备通信功能
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
public interface DeviceCommunicationService {

    /**
     * 发送设备控制命令
     */
    boolean sendCommand(Long deviceId, String command, Map<String, Object> parameters);

    /**
     * 发送设备配置更新
     */
    boolean sendConfigUpdate(Long deviceId, Map<String, Object> config);

    /**
     * 发送设备状态查询
     */
    Map<String, Object> queryDeviceStatus(Long deviceId);

    /**
     * 批量发送命令
     */
    Map<Long, Boolean> batchSendCommand(List<Long> deviceIds, String command, Map<String, Object> parameters);

    /**
     * 注册设备监听器
     */
    void registerDeviceListener(Long deviceId, DeviceMessageListener listener);

    /**
     * 注销设备监听器
     */
    void unregisterDeviceListener(Long deviceId);

    /**
     * 获取设备连接状态
     */
    boolean isDeviceConnected(Long deviceId);

    /**
     * 获取所有连接的设备
     */
    List<Long> getConnectedDevices();

    /**
     * 配置设备协议
     *
     * @param configForm 协议配置表单
     * @return 配置ID
     */
    Long configureDeviceProtocol(DeviceProtocolConfigForm configForm);

    /**
     * 获取协议配置列表
     *
     * @param deviceId     设备ID(可选)
     * @param protocolType 协议类型(可选)
     * @return 协议配置列表
     */
    List<DeviceProtocolVO> getProtocolConfigList(Long deviceId, String protocolType);

    /**
     * 启动数据采集
     *
     * @param collectionForm 数据采集表单
     */
    void startDataCollection(DeviceDataCollectionForm collectionForm);

    /**
     * 停止数据采集
     *
     * @param collectionId 采集任务ID
     */
    void stopDataCollection(Long collectionId);

    /**
     * 获取最新设备数据
     *
     * @param deviceId 设备ID
     * @param limit    限制数量
     * @return 设备数据列表
     */
    List<DeviceDataVO> getLatestDeviceData(Long deviceId, Integer limit);

    /**
     * 获取设备历史数据
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageNum   页码
     * @param pageSize  每页数量
     * @return 设备数据列表
     */
    List<DeviceDataVO> getDeviceDataHistory(Long deviceId, Long startTime, Long endTime, Integer pageNum,
            Integer pageSize);

    /**
     * 发送设备命令
     *
     * @param deviceId   设备ID
     * @param command    命令
     * @param parameters 参数
     * @return 执行结果
     */
    Map<String, Object> sendDeviceCommand(Long deviceId, String command, Map<String, Object> parameters);

    /**
     * 获取通信状态
     *
     * @param deviceId 设备ID
     * @return 通信状态信息
     */
    Map<String, Object> getCommunicationStatus(Long deviceId);

    /**
     * 获取通信统计概览
     *
     * @return 统计信息
     */
    Map<String, Object> getCommunicationStatisticsOverview();

    /**
     * 测试协议连接
     *
     * @param configForm 协议配置表单
     * @return 测试结果
     */
    Map<String, Object> testProtocolConnection(DeviceProtocolConfigForm configForm);

    /**
     * 获取支持的协议列表
     *
     * @return 支持的协议列表
     */
    List<Map<String, Object>> getSupportedProtocols();

    /**
     * 批量上传设备数据
     *
     * @param dataForms 数据表单列表
     * @return 上传结果统计
     */
    Map<String, Integer> batchUploadDeviceData(List<DeviceDataCollectionForm> dataForms);

    /**
     * 获取数据采集统计
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间(可选)
     * @param endTime   结束时间(可选)
     * @return 统计信息
     */
    Map<String, Object> getDataCollectionStatistics(Long deviceId, Long startTime, Long endTime);

    /**
     * 设备消息监听器接口
     */
    interface DeviceMessageListener {
        void onDeviceMessage(Long deviceId, String topic, Object message);

        void onDeviceConnected(Long deviceId);

        void onDeviceDisconnected(Long deviceId);

        void onDeviceError(Long deviceId, Exception error);
    }

    /**
     * 设备通信配置
     */
    class CommunicationConfig {
        private String protocol;
        private String host;
        private Integer port;
        private String username;
        private String password;
        private Integer timeout;
        private Integer retryAttempts;

        // Getters and Setters
        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public Integer getRetryAttempts() {
            return retryAttempts;
        }

        public void setRetryAttempts(Integer retryAttempts) {
            this.retryAttempts = retryAttempts;
        }
    }
}
