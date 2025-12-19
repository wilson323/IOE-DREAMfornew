package net.lab1024.sa.oa.workflow.config;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flowable事件监听器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
public class FlowableEventLogger implements FlowableEventListener {

    private static final Logger log = LoggerFactory.getLogger(FlowableEventLogger.class);

    @Override
    public void onEvent(FlowableEvent event) {
        if (event.getType() == FlowableEngineEventType.PROCESS_STARTED) {
            log.info("[工作流事件] 流程启动");
        } else if (event.getType() == FlowableEngineEventType.PROCESS_COMPLETED) {
            log.info("[工作流事件] 流程完成");
        } else if (event.getType() == FlowableEngineEventType.TASK_CREATED) {
            log.info("[工作流事件] 任务创建");
        } else if (event.getType() == FlowableEngineEventType.TASK_COMPLETED) {
            log.info("[工作流事件] 任务完成");
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}
