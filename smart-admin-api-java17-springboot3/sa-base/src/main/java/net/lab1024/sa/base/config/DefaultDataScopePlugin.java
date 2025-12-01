package net.lab1024.sa.base.config;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;

import net.lab1024.sa.base.common.domain.DataScopePlugin;

/**
 * 默认数据范围插件实现（空实现）
 * 用于Spring Boot启动时的Bean注入，实际数据权限功能通过其他方式实现
 */
@Component
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = { java.sql.Connection.class,
                Integer.class })
})
public class DefaultDataScopePlugin extends DataScopePlugin {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 空实现，直接执行原方法
        // 实际的数据权限控制通过AOP和切面实现
        return invocation.proceed();
    }
}