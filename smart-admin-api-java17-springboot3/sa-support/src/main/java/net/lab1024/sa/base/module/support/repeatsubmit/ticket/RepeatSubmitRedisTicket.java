package net.lab1024.sa.base.module.support.repeatsubmit.ticket;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import net.lab1024.sa.base.module.support.repeatsubmit.RepeatSubmitTicketService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * Redis防重复提交票据服务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Slf4j
@Service
public class RepeatSubmitRedisTicket implements RepeatSubmitTicketService {

    private static final String REPEAT_SUBMIT_TICKET_PREFIX = "repeat_submit_ticket:";

    private final RedisTemplate<String, String> redisTemplate;

    public RepeatSubmitRedisTicket(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String generateTicket(HttpServletRequest request) {
        String url = request.getRequestURI();
        String params = SmartRequestUtil.getParamStr(request);
        String ip = SmartRequestUtil.getClientIP(request);

        // 生成基于URL、参数和IP的唯一ticket
        String ticket = url + ":" + params + ":" + ip;
        return REPEAT_SUBMIT_TICKET_PREFIX + ticket.hashCode();
    }

    @Override
    public boolean checkAndSetTicket(String ticket, long lockTime) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(ticket, "1", lockTime, TimeUnit.SECONDS);
            return result != null && result;
        } catch (Exception e) {
            log.error("设置防重复提交票据失败", e);
            return true; // 如果Redis异常，允许通过
        }
    }

    @Override
    public void clearTicket(String ticket) {
        try {
            redisTemplate.delete(ticket);
        } catch (Exception e) {
            log.error("清除防重复提交票据失败", e);
        }
    }
}