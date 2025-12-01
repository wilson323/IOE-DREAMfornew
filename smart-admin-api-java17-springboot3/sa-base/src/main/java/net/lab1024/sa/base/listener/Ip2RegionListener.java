package net.lab1024.sa.base.listener;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

import lombok.extern.slf4j.Slf4j;

/**
 * IP2Region监听器
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
@Slf4j
public class Ip2RegionListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        // 此监听器用于初始化IP2Region相关配置
        log.info("Ip2RegionListener initialized");
    }
}
