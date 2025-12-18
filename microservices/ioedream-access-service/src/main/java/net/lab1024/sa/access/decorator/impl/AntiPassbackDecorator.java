package net.lab1024.sa.access.decorator.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.decorator.AccessFlowDecorator;
import net.lab1024.sa.access.decorator.IAccessFlowExecutor;
import net.lab1024.sa.access.domain.form.AccessRequest;
import net.lab1024.sa.access.template.AbstractAccessFlowTemplate;
import net.lab1024.sa.common.access.manager.AntiPassbackManager;

import jakarta.annotation.Resource;

/**
 * 反潜回装饰器
 * <p>
 * 增强通行流程，添加反潜回验证功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
public class AntiPassbackDecorator extends AccessFlowDecorator {

    @Resource
    private AntiPassbackManager antiPassbackManager;

    /**
     * 构造函数
     *
     * @param delegate 被装饰的执行器
     */
    public AntiPassbackDecorator(IAccessFlowExecutor delegate) {
        super(delegate);
    }

    @Override
    protected void beforeExecute(AccessRequest request) {
        // 反潜回验证
        if (request.getAccessType() == 0) { // 进入
            boolean canEnter = antiPassbackManager.canEnter(request.getUserId(), request.getAreaId());
            if (!canEnter) {
                log.warn("[反潜回装饰器] 用户{}在区域{}存在未离开记录，禁止进入",
                        request.getUserId(), request.getAreaId());
                throw new IllegalStateException("反潜回验证失败：存在未离开记录");
            }
        } else if (request.getAccessType() == 1) { // 离开
            boolean canLeave = antiPassbackManager.canLeave(request.getUserId(), request.getAreaId());
            if (!canLeave) {
                log.warn("[反潜回装饰器] 用户{}在区域{}不存在进入记录，禁止离开",
                        request.getUserId(), request.getAreaId());
                throw new IllegalStateException("反潜回验证失败：不存在进入记录");
            }
        }
    }
}
