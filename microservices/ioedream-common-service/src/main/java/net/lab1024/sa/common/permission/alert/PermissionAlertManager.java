package net.lab1024.sa.common.permission.alert;
import net.lab1024.sa.common.permission.audit.PermissionAuditLogger;
import org.springframework.data.redis.core.RedisTemplate;
public class PermissionAlertManager {
    public PermissionAlertManager(PermissionAuditLogger logger, RedisTemplate<String, Object> redisTemplate) {}
}