package net.lab1024.sa.oa.workflow.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flowable初始化后处理器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
public class FlowableInitializationProcessor {

    private static final Logger log = LoggerFactory.getLogger(FlowableInitializationProcessor.class);

    @PostConstruct
    public void init() {
        log.info("[Flowable 7.2] 引擎初始化完成");
    }
}
