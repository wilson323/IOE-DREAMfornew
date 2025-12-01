package net.lab1024.sa.base.listener;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 日志变量监听器
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
@Slf4j
public class LogVariableListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        // 此监听器用于在应用启动前设置日志相关的系统属性
        log.info("LogVariableListener initialized");
    }
}
