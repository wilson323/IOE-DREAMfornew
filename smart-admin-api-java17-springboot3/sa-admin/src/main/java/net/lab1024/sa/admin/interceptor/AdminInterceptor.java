package net.lab1024.sa.admin.interceptor;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.lab1024.sa.admin.module.system.login.domain.RequestEmployee;
import net.lab1024.sa.admin.module.system.login.service.LoginService;
import net.lab1024.sa.base.common.annotation.NoNeedLogin;
import net.lab1024.sa.base.common.code.SystemErrorCode;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import net.lab1024.sa.base.common.util.SmartResponseUtil;

/**
 * admin 拦截器
 *
 * @author 1024Lab
 * @date 2023/7/26 20:20:33
 */

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdminInterceptor.class);

    @Resource
    private LoginService loginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {

        // 直接放行 OPTIONS 预检请求
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
            return false;
        }

        boolean isHandler = handler instanceof HandlerMethod;
        if (!isHandler) {
            return true;
        }

        try {
            // 1) 根据 token 获取用户

            String tokenValue = StpUtil.getTokenValue();
            String loginId = (String) StpUtil.getLoginIdByToken(tokenValue);
            RequestEmployee requestEmployee = loginService.getLoginEmployee(loginId, request);

            // 2) 校验登录

            Method method = ((HandlerMethod) handler).getMethod();
            NoNeedLogin noNeedLogin = ((HandlerMethod) handler).getMethodAnnotation(NoNeedLogin.class);
            if (noNeedLogin != null) {
                updateActiveTimeout(requestEmployee);
                SmartRequestUtil.setRequestUser(requestEmployee);
                return true;
            }

            if (requestEmployee == null) {
                SmartResponseUtil.write(response,
                        ResponseDTO.error(UserErrorCode.LOGIN_STATE_INVALID));
                return false;
            }

            // 更新活跃
            updateActiveTimeout(requestEmployee);

            // 3) 校验权限

            SmartRequestUtil.setRequestUser(requestEmployee);
            if (SaAnnotationStrategy.instance.isAnnotationPresent.apply(method, SaIgnore.class)) {
                return true;
            }

            // 超级管理员跳过权限校验
            if (requestEmployee.getAdministratorFlag()) {
                return true;
            }

            SaAnnotationStrategy.instance.checkMethodAnnotation.accept(method);

        } catch (SaTokenException e) {
            // sa-token 异常状态码
            int code = e.getCode();
            if (code == 11041 || code == 11051) {
                SmartResponseUtil.write(response, ResponseDTO.error(UserErrorCode.NO_PERMISSION));
            } else if (code == 11016) {
                SmartResponseUtil.write(response,
                        ResponseDTO.error(UserErrorCode.LOGIN_ACTIVE_TIMEOUT));
            } else if (code >= 11011 && code <= 11015) {
                SmartResponseUtil.write(response,
                        ResponseDTO.error(UserErrorCode.LOGIN_STATE_INVALID));
            } else {
                SmartResponseUtil.write(response, ResponseDTO.error(UserErrorCode.PARAM_ERROR));
            }
            return false;
        } catch (Throwable e) {
            SmartResponseUtil.write(response, ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR));
            log.error(e.getMessage(), e);
            return false;
        }

        // 通过校验
        return true;
    }

    /**
     * 更新活跃时间
     */
    private void updateActiveTimeout(RequestEmployee requestEmployee) {
        if (requestEmployee == null) {
            return;
        }
        StpUtil.updateLastActiveToNow();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {
        // 清理上下文
        SmartRequestUtil.remove();
    }
}
