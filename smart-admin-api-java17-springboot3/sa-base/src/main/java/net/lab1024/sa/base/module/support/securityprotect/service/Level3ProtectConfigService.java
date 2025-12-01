package net.lab1024.sa.base.module.support.securityprotect.service;

/**
 * 三级等保配置服务占位接口。
 */
public interface Level3ProtectConfigService {
    default int getLoginActiveTimeoutSeconds() { return 0; }
}
