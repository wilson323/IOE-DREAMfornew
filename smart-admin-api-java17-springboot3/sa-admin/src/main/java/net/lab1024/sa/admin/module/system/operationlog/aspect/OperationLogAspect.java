package net.lab1024.sa.admin.module.system.operationlog.aspect;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.system.operationlog.annotation.OperationLog;
import net.lab1024.sa.admin.module.system.operationlog.dao.OperationLogDao;
import net.lab1024.sa.admin.module.system.operationlog.domain.OperationLogEntity;

/**
 * 操作日志鍒囬潰
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    @Resource
    private OperationLogDao operationLogDao;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();

        OperationLogEntity logEntity = new OperationLogEntity();

        try {
            logEntity.setUserId(StpUtil.getLoginIdAsLong());
            logEntity.setUserName(
                    StpUtil.getLoginIdDefaultNull() != null ? StpUtil.getLoginIdDefaultNull().toString() : "anonymous");
        } catch (Exception e) {
            logEntity.setUserName("anonymous");
        }

        logEntity.setOperationType(operationLog.operationType());
        logEntity.setOperationDesc(operationLog.operationDesc());
        logEntity.setRequestMethod(request.getMethod());
        logEntity.setRequestUrl(request.getRequestURI());
        logEntity.setIpAddress(getClientIpAddress(request));
        logEntity.setUserAgent(request.getHeader("User-Agent"));

        try {
            Object[] args = joinPoint.getArgs();
            Map<String, Object> requestParams = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                requestParams.put("param" + i, args[i]);
            }
            logEntity.setRequestParams(requestParams);

            Object result = joinPoint.proceed();

            logEntity.setStatus(1);

            return result;

        } catch (Exception e) {
            logEntity.setStatus(0);
            logEntity.setErrorMessage(e.getMessage());

            log.error("操作异常: {}", e.getMessage(), e);
            throw e;

        } finally {
            logEntity.setExecutionTime(System.currentTimeMillis() - startTime);

            saveLogAsync(logEntity);
        }
    }

    @Async("cacheExecutor")
    public void saveLogAsync(OperationLogEntity logEntity) {
        CompletableFuture.runAsync(() -> {
            try {
                operationLogDao.insert(logEntity);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        });
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED",
                "HTTP_VIA", "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0];
                }
                return ip.trim();
            }
        }

        return request.getRemoteAddr();
    }
}

