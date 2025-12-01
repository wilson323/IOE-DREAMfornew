package net.lab1024.sa.base.module.support.repeatsubmit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 防重复提交AOP
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Slf4j
@Aspect
@Component
public class RepeatSubmitAspect {

    private final RepeatSubmitTicketService ticketService;

    public RepeatSubmitAspect(RepeatSubmitTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Around("@annotation(repeatSubmit)")
    public Object around(ProceedingJoinPoint point, RepeatSubmit repeatSubmit) throws Throwable {
        // 如果重复提交功能未启用，直接执行方法
        if (!repeatSubmit.enable()) {
            return point.proceed();
        }

        HttpServletRequest request = getRequest();
        if (request == null) {
            log.warn("HttpServletRequest is null, skipping repeat submit check");
            return point.proceed();
        }

        String ticket = ticketService.generateTicket(request);
        boolean isAllowed = ticketService.checkAndSetTicket(ticket, repeatSubmit.lockTime());

        if (!isAllowed) {
            throw new RuntimeException("请勿重复提交");
        }

        try {
            return point.proceed();
        } finally {
            // 执行完成后清除ticket
            ticketService.clearTicket(ticket);
        }
    }

    private HttpServletRequest getRequest() {
        return SmartRequestUtil.getRequest();
    }
}