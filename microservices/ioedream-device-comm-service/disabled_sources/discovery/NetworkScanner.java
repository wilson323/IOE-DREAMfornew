package net.lab1024.sa.device.comm.discovery;

import java.util.List;

/**
 * 网络扫描器接口
 * <p>
 * 用于扫描网络中的设备
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
public interface NetworkScanner {

    /**
     * 获取扫描器类型
     *
     * @return 扫描器类型标识
     */
    String getScannerType();

    /**
     * 扫描指定网段
     *
     * @param subnet 子网地址(如: 192.168.1.0/24)
     * @return 发现的设备地址列表
     */
    List<String> scan(String subnet);

    /**
     * 扫描指定端口
     *
     * @param host 主机地址
     * @param ports 端口列表
     * @return 开放的端口列表
     */
    List<Integer> scanPorts(String host, List<Integer> ports);

    /**
     * 检查主机是否在线
     *
     * @param host 主机地址
     * @param timeout 超时时间(毫秒)
     * @return 是否在线
     */
    boolean isHostOnline(String host, int timeout);
}
