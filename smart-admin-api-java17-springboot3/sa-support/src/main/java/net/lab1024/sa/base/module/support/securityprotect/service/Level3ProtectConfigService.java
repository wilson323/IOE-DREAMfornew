package net.lab1024.sa.base.module.support.securityprotect.service;

/**
 * 三级防护配置服务接口
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
public interface Level3ProtectConfigService {

    /**
     * 获取防护配置是否启用
     *
     * @return true表示启用，false表示禁用
     */
    boolean isProtectionEnabled();

    /**
     * 获取最大访问频率（每分钟请求数）
     *
     * @return 每分钟最大请求数
     */
    int getMaxRequestsPerMinute();

    /**
     * 获取最大并发连接数
     *
     * @return 最大并发连接数
     */
    int getMaxConcurrentConnections();

    /**
     * 获取IP黑名单检查间隔（分钟）
     *
     * @return 检查间隔
     */
    int getBlackListCheckIntervalMinutes();

    /**
     * 检查IP是否在黑名单中
     *
     * @param ipAddress IP地址
     * @return true表示在黑名单中
     */
    boolean isIpInBlackList(String ipAddress);

    /**
     * 添加IP到黑名单
     *
     * @param ipAddress IP地址
     * @param durationMinutes 持续时间（分钟）
     */
    void addIpToBlackList(String ipAddress, int durationMinutes);

    /**
     * 从黑名单中移除IP
     *
     * @param ipAddress IP地址
     */
    void removeIpFromBlackList(String ipAddress);

    /**
     * 检查是否触发频率限制
     *
     * @param ipAddress IP地址
     * @param endpoint 接口路径
     * @return true表示触发限制
     */
    boolean isRateLimitExceeded(String ipAddress, String endpoint);

    /**
     * 记录访问次数
     *
     * @param ipAddress IP地址
     * @param endpoint 接口路径
     */
    void recordAccess(String ipAddress, String endpoint);

    /**
     * 获取登录活跃超时时间（秒）
     *
     * @return 超时时间（秒）
     */
    int getLoginActiveTimeoutSeconds();
}