package net.lab1024.sa.base.module.support.operatelog.core;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 操作日志切面
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Slf4j
@Aspect
public class OperateLogAspect {

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint point, Object operateLog) throws Throwable {
        // 简单实现，直接执行方法
        try {
            return point.proceed();
        } catch (Exception e) {
            log.error("操作日志执行异常", e);
            throw e;
        }
    }
}