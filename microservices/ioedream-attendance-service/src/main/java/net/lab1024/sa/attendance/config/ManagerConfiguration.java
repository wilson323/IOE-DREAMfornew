package net.lab1024.sa.attendance.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.manager.AttendanceManager;
import net.lab1024.sa.attendance.engine.rule.cache.RuleCacheManager;
import net.lab1024.sa.attendance.engine.rule.cache.impl.RuleCacheManagerImpl;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

/**
 * Manager配置类
 * <p>
 * 用于将考勤模块特有的Manager实现类注册为Spring Bean
 * </p>
 * <p>
 * 注意：公共Manager（WorkflowApprovalManager等）
 * 已由CommonBeanAutoConfiguration统一装配，无需在此重复定义
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-12-14 移除重复的公共Bean定义，改用统一自动装配
 * @updated 2025-12-17 添加RuleCacheManagerImpl Bean注册，修复Manager注解违规
 */
@Slf4j
@Configuration("attendanceManagerConfiguration")
public class ManagerConfiguration {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 注册AttendanceManager为Spring Bean
     * <p>
     * 考勤模块特有的Manager，用于处理考勤审批通过后的业务逻辑
     * 包括：用户信息获取、年假余额扣除、考勤统计更新等
     * </p>
     *
     * @return AttendanceManager实例
     */
    @Bean
    public AttendanceManager attendanceManager() {
        log.info("[AttendanceManager] 初始化考勤管理器");
        return new AttendanceManager(gatewayServiceClient);
    }

    /**
     * 注册RuleCacheManager为Spring Bean
     * <p>
     * 规则缓存管理器，用于考勤规则引擎的多级缓存实现
     * 包括：L1本地缓存 + L2 Redis缓存
     * </p>
     *
     * @return RuleCacheManager实例
     */
    @Bean("ruleCacheManager")
    public RuleCacheManager ruleCacheManager() {
        log.info("[RuleCacheManager] 初始化规则缓存管理器");
        return new RuleCacheManagerImpl(redisTemplate, objectMapper);
    }
}