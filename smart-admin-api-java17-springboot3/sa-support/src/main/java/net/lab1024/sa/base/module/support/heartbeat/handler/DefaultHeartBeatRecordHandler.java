package net.lab1024.sa.base.module.support.heartbeat.handler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.module.support.heartbeat.core.IHeartBeatRecordHandler;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 默认蹇冭烦记录处理鍣ㄥ疄鐜帮紙鍩轰簬Redis锛= *
 * @Author 1024========设计模式
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href==https://1024lab.net=>1024========/a>
 */
@Slf4j
@Component
public class DefaultHeartBeatRecordHandler implements IHeartBeatRecordHandler {

    /**
     * 蹇冭烦记录Redis閿墠缂€
     */
    private static final String HEART_BEAT_KEY_PREFIX = "heart_beat:";

    /**
     * 蹇冭烦记录迁移囨湡时间锛堝垎閽燂級
     */
    private static final long HEART_BEAT_EXPIRE_MINUTES = 10;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void handleHeartBeat(String clientId, String clientInfo) {
        String key = HEART_BEAT_KEY_PREFIX + clientId;
        try {
            // 瀛樺偍蹇冭烦记录锛屽寘鍚鎴风淇℃伅鍜屾椂闂存埑
            HeartBeatRecord record = new HeartBeatRecord(clientId, clientInfo, System.currentTimeMillis());
            redisTemplate.opsForValue().set(key, record, HEART_BEAT_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.debug("Heart beat recorded for client: {} with info: {}", clientId, clientInfo);
        } catch (Exception e) {
            log.error("Failed to record heart beat for client: " + clientId, e);
        }
    }

    @Override
    public void cleanExpiredHeartBeats() {
        // Redis鐨凾TL鏈哄埗内存氳嚜鍔ㄦ竻鐞嗚繃鏈熻褰曪紝迁移欓噷涓嶉渶瑕侀澶栧鐞=        log.debug(=Expired heart beat records cleaned by Redis TTL mechanism=);
    }

    @Override
    public long getActiveClientCount() {
        try {
            Set<String> keys = redisTemplate.keys(HEART_BEAT_KEY_PREFIX + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            log.error("Failed to get active client count", e);
            return 0;
        }
    }

    /**
     * 蹇冭烦记录=体类=     */
    public static class HeartBeatRecord {
        private String clientId;
        private String clientInfo;
        private long timestamp;

        public HeartBeatRecord() {
        }

        public HeartBeatRecord(String clientId, String clientInfo, long timestamp) {
            this.clientId = clientId;
            this.clientInfo = clientInfo;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientInfo() {
            return clientInfo;
        }

        public void setClientInfo(String clientInfo) {
            this.clientInfo = clientInfo;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
