package net.lab1024.sa.attendance.decorator.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.decorator.IPunchExecutor;
import net.lab1024.sa.attendance.decorator.PunchDecorator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.Duration;

/**
 * 防作弊装饰器
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 防止同一用户短时间内多次打卡
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class AntiCheatingDecorator extends PunchDecorator {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造函数
     *
     * @param delegate 被装饰的执行器
     */
    public AntiCheatingDecorator(IPunchExecutor delegate) {
        super(delegate);
    }

    @Override
    public PunchResult execute(MobilePunchRequest request) {
        // 防作弊检测: 同一用户短时间内多次打卡
        String lockKey = "punch:lock:" + request.getUserId();
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofMinutes(1));

        if (Boolean.FALSE.equals(lockAcquired)) {
            log.warn("[防作弊装饰器] 打卡过于频繁, userId={}", request.getUserId());
            return PunchResult.failed("打卡过于频繁,请稍后再试");
        }

        try {
            return super.execute(request);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}
