package net.lab1024.sa.consume.service.integration.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 鏀粯澶勭悊鍣ㄦ敞鍐岃〃
 * 绠＄悊澶氱鏀粯鏂瑰紡鐨勫鐞嗗櫒
 *
 * @author IOE-DREAM鍥㈤槦
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class PaymentProcessorRegistry {

    private final Map<PaymentMethod, PaymentProcessor> processors = new HashMap<>();
    private final Map<String, PaymentProcessor> processorMap = new HashMap<>();

    @Resource(required = false)
    private List<PaymentProcessor> paymentProcessors;

    @PostConstruct
    public void init() {
        if (paymentProcessors != null) {
            for (PaymentProcessor processor : paymentProcessors) {
                registerProcessor(processor);
            }
        }
        log.info("[鏀粯澶勭悊鍣╙ 鍒濆鍖栧畬鎴愶紝娉ㄥ唽澶勭悊鍣ㄦ暟閲? {}", processors.size());
    }

    /**
     * 娉ㄥ唽鏀粯澶勭悊鍣?
     *
     * @param processor 鏀粯澶勭悊鍣?
     */
    public void registerProcessor(PaymentProcessor processor) {
        PaymentMethod method = processor.getSupportedPaymentMethod();
        processors.put(method, processor);
        processorMap.put(method.name(), processor);
        log.info("[鏀粯澶勭悊鍣╙ 娉ㄥ唽鏀粯澶勭悊鍣? {} -> {}", method, processor.getClass().getSimpleName());
    }

    /**
     * 鑾峰彇鏀粯澶勭悊鍣?
     *
     * @param paymentMethod 鏀粯鏂瑰紡
     * @return 鏀粯澶勭悊鍣?
     */
    public PaymentProcessor getProcessor(PaymentMethod paymentMethod) {
        return processors.get(paymentMethod);
    }

    /**
     * 鏍规嵁鍚嶇О鑾峰彇鏀粯澶勭悊鍣?
     *
     * @param methodName 鏀粯鏂瑰紡鍚嶇О
     * @return 鏀粯澶勭悊鍣?
     */
    public PaymentProcessor getProcessor(String methodName) {
        return processorMap.get(methodName.toUpperCase());
    }

    /**
     * 鑾峰彇鎵€鏈夋敮鎸佺殑鏀粯鏂瑰紡
     *
     * @return 鏀粯鏂瑰紡鍒楄〃
     */
    public PaymentMethod[] getSupportedPaymentMethods() {
        return processors.keySet().toArray(new PaymentMethod[0]);
    }

    /**
     * 妫€鏌ユ槸鍚︽敮鎸佹寚瀹氭敮浠樻柟寮?
     *
     * @param paymentMethod 鏀粯鏂瑰紡
     * @return 鏄惁鏀寔
     */
    public boolean isSupported(PaymentMethod paymentMethod) {
        return processors.containsKey(paymentMethod);
    }

    /**
     * 鑾峰彇鎵€鏈夋敮浠樺鐞嗗櫒
     *
     * @return 鏀粯澶勭悊鍣ㄦ槧灏?
     */
    public Map<PaymentMethod, PaymentProcessor> getAllProcessors() {
        return new HashMap<>(processors);
    }
}
