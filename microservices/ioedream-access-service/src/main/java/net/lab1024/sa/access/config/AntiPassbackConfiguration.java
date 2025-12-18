package net.lab1024.sa.access.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import net.lab1024.sa.access.dao.AccessRecordDao;
import net.lab1024.sa.access.manager.AntiPassbackManager;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

/**
 * 闂ㄧ鍙嶆綔鍥為厤锟?
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锟?
 * - 浣跨敤@Configuration娉ㄨВ鏍囪瘑閰嶇疆锟?
 * - 閰嶇疆Resilience4j瀹归敊鏈哄埗
 * - 閰嶇疆Redis杩炴帴鍜屽簭鍒楀寲
 * - 璁剧疆鍚堢悊鐨勮秴鏃跺拰閲嶈瘯鍙傛暟
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
public class AntiPassbackConfiguration {

    /**
     * 閰嶇疆Redis妯℃澘
     */
    @Bean
    public RedisTemplate<String, Object> antiPassbackRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 浣跨敤String搴忓垪鍖栧櫒
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 浣跨敤JSON搴忓垪鍖栧櫒
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 閰嶇疆鍙嶆綔鍥炴湇鍔＄啍鏂櫒
     */
    @Bean("antiPassbackService")
    public CircuitBreaker antiPassbackCircuitBreaker() {
        return CircuitBreaker.of("antiPassbackService", CircuitBreakerConfig.custom()
                .failureRateThreshold(50)           // 澶辫触鐜囬槇锟?0%
                .waitDurationInOpenState(Duration.ofSeconds(30))  // 鐔旀柇寮€鍚椂锟?0锟?
                .slidingWindowSize(20)              // 婊戝姩绐楀彛澶у皬20
                .minimumNumberOfCalls(10)           // 鏈€灏戣皟鐢ㄦ锟?0
                .slowCallDurationThreshold(Duration.ofSeconds(3))  // 鎱㈣皟鐢ㄩ槇锟?锟?
                .slowCallRateThreshold(50)          // 鎱㈣皟鐢ㄧ巼闃堬拷?0%
                .build());
    }

    /**
     * 閰嶇疆鍙嶆綔鍥炴湇鍔￠檺娴佸櫒
     */
    @Bean("antiPassbackService")
    public RateLimiter antiPassbackRateLimiter() {
        return RateLimiter.of("antiPassbackService", RateLimiterConfig.custom()
                .limitForPeriod(100)               // 姣忎釜鍛ㄦ湡鍏佽100涓锟?
                .limitRefreshPeriod(Duration.ofSeconds(1))  // 鍒锋柊鍛ㄦ湡1锟?
                .timeoutDuration(Duration.ofSeconds(5))      // 绛夊緟瓒呮椂5锟?
                .build());
    }

    /**
     * 閰嶇疆鍙嶆綔鍥炴湇鍔￠噸璇曞櫒
     */
    @Bean("antiPassbackService")
    public Retry antiPassbackRetry() {
        return Retry.of("antiPassbackService", RetryConfig.custom()
                .maxAttempts(3)                     // 鏈€澶ч噸璇曟锟?
                .waitDuration(Duration.ofMillis(500)) // 閲嶈瘯闂撮殧500姣
                .retryExceptions(Exception.class)   // 閲嶈瘯寮傚父绫诲瀷
                .build());
    }

    /**
     * 閰嶇疆鍙嶆綔鍥炴湇鍔℃椂闂撮檺鍒跺櫒
     */
    @Bean("antiPassbackService")
    public TimeLimiter antiPassbackTimeLimiter() {
        return TimeLimiter.of("antiPassbackService", TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))  // 瓒呮椂鏃堕棿10锟?
                .cancelRunningFuture(true)         // 鍙栨秷杩愯涓殑Future
                .build());
    }

    /**
     * 娉ㄥ唽鍙嶆綔鍥炵鐞嗗櫒涓篠pring Bean
     * <p>
     * 閲囩敤閫傞厤鍣ㄦā寮忥細閫氳繃GatewayServiceClient璋冪敤common-service鐨勬湇鍔?
     * 閬靛惊CLAUDE.md瑙勮寖锛歁anager绫婚€氳繃鏋勯€犲嚱鏁版敞鍏ヤ緷璧栵紝涓嶄娇鐢⊿pring娉ㄨВ
     * </p>
     *
     * @param accessRecordDao 璁块棶璁板綍DAO
     * @param redisTemplate Redis妯℃澘
     * @param gatewayServiceClient 缃戝叧鏈嶅姟瀹㈡埛绔紙鐢ㄤ簬璋冪敤common-service锛?
     * @return 鍙嶆綔鍥炵鐞嗗櫒瀹炰緥
     */
    @Bean
    public AntiPassbackManager antiPassbackManager(
            AccessRecordDao accessRecordDao,
            RedisTemplate<String, Object> redisTemplate,
            GatewayServiceClient gatewayServiceClient) {
        return new AntiPassbackManager(accessRecordDao, redisTemplate, gatewayServiceClient);
    }
}
