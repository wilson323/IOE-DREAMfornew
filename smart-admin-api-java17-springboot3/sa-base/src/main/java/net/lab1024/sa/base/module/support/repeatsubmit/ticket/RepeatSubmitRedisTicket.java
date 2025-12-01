package net.lab1024.sa.base.module.support.repeatsubmit.ticket;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.function.Function;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 重复提交票据占位实现。
 */
public class RepeatSubmitRedisTicket {
    public RepeatSubmitRedisTicket(RedisTemplate<String, Object> redisTemplate,
                                   Function<HttpServletRequest, String> ticketSupplier) {
    }
}
