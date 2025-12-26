package net.lab1024.sa.oa.workflow.config;

import lombok.extern.slf4j.Slf4j;


import jakarta.annotation.PostConstruct;

/**
 * Flowable初始化后处理器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
@Slf4j
public class FlowableInitializationProcessor {

    @PostConstruct
    public void init() {
        log.info("[Flowable 7.2] 引擎初始化完成");
    }
}
