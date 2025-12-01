package net.lab1024.sa.base.module.support.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;
import net.lab1024.sa.base.module.support.rbac.PolicyEvaluator;
import net.lab1024.sa.base.module.support.rbac.RequireResource;

/**
 * Unified resource authentication aspect: intercept @RequireResource
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequireResourceAspect {

    private final PolicyEvaluator policyEvaluator;

    // Description: Example of getting AuthorizationContext from ThreadLocal or security context
    private static final ThreadLocal<AuthorizationContext> CTX = new ThreadLocal<>();

    public static void setContext(AuthorizationContext context) { CTX.set(context); }
    public static void clear() { CTX.remove(); }

    @Around("@annotation(net.lab1024.sa.base.module.support.rbac.RequireResource)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        RequireResource anno = method.getAnnotation(RequireResource.class);
        AuthorizationContext ctx = CTX.get();
        if (ctx == null) {
            throw new SecurityException("Authorization context missing for resource: " + anno.code());
        }

        // 设置资源编码和动作到上下文
        AuthorizationContext updatedContext = ctx.toBuilder()
                .resourceCode(anno.code())
                .requestedAction(anno.action())
                .build();

        var decision = policyEvaluator.evaluate(updatedContext);
        if (!decision.isAllowed()) {
            throw new SecurityException("Access denied for resource: " + anno.code() + ", reason: " + decision.getReason());
        }
        return pjp.proceed();
    }
}


