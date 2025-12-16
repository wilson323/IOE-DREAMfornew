package net.lab1024.sa.devicecomm.cache;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.gateway.DirectServiceClient;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.DeviceEntity;

/**
 * 鍗忚缂撳瓨鏈嶅姟瀹炵幇绫? * <p>
 * 浣跨敤Spring Cache娉ㄨВ锛園Cacheable銆丂CacheEvict銆丂CachePut锛夋浛浠rotocolCacheManager
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 缁熶竴浣跨敤Spring Cache + Caffeine + Redis
 * - 绂佹浣跨敤鑷畾涔塁acheManager
 * - 浣跨敤CompositeCacheManager锛圠1鏈湴缂撳瓨 + L2 Redis缂撳瓨锛? * </p>
 * <p>
 * 缂撳瓨閰嶇疆锛? * - 缂撳瓨鍚嶇О锛歱rotocol:device銆乸rotocol:device:code銆乸rotocol:user:card
 * - L1鏈湴缂撳瓨锛欳affeine锛?鍒嗛挓杩囨湡
 * - L2鍒嗗竷寮忕紦瀛橈細Redis锛?0鍒嗛挓杩囨湡
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ProtocolCacheServiceImpl implements ProtocolCacheService {

	    @Resource
	    private GatewayServiceClient gatewayServiceClient;

	    @Resource(required = false)
	    private DirectServiceClient directServiceClient;

	    @Value("${ioedream.direct-call.enabled:false}")
	    private boolean directEnabled;

    /**
     * 鏍规嵁璁惧ID鑾峰彇璁惧淇℃伅锛堜娇鐢⊿pring Cache锛?     * <p>
     * 缂撳瓨绛栫暐锛?     * - L1鏈湴缂撳瓨锛欳affeine锛?鍒嗛挓杩囨湡
     * - L2鍒嗗竷寮忕紦瀛橈細Redis锛?0鍒嗛挓杩囨湡
     * - 缂撳瓨閿細protocol:device:{deviceId}
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 璁惧瀹炰綋锛屽鏋滀笉瀛樺湪杩斿洖null
     */
    @Override
    @Observed(name = "protocol.cache.getDeviceById", contextualName = "protocol-cache-get-device-by-id")
    @Cacheable(value = "protocol:device", key = "#deviceId", unless = "#result == null")
    public DeviceEntity getDeviceById(Long deviceId) {
        if (deviceId == null) {
            return null;
        }

        log.debug("[鍗忚缂撳瓨] 缂撳瓨鏈懡涓紝浠庢暟鎹簱鏌ヨ璁惧淇℃伅锛宒eviceId={}", deviceId);

	        String path = "/api/v1/device/" + deviceId;

	        // 浼樺厛鐩磋繛鍏叡鏈嶅姟鏌ヨ璁惧淇℃伅锛堢儹璺緞锛夛紝澶辫触鍥為€€缃戝叧
	        try {
	            ResponseDTO<DeviceEntity> response = null;
	            if (directEnabled && directServiceClient != null && directServiceClient.isEnabled()) {
	                response = directServiceClient.callCommonService(
	                        path,
	                        HttpMethod.GET,
	                        null,
	                        DeviceEntity.class
	                );
	            }
	            if (response == null || !response.isSuccess() || response.getData() == null) {
	                response = gatewayServiceClient.callCommonService(
	                        path,
	                        HttpMethod.GET,
	                        null,
	                        DeviceEntity.class
	                );
	            }

	            if (response != null && response.isSuccess() && response.getData() != null) {
	                DeviceEntity device = response.getData();
                log.debug("[鍗忚缂撳瓨] 浠庢暟鎹簱鏌ヨ鍒拌澶囦俊鎭紝deviceId={}, deviceCode={}",
                        deviceId, device.getDeviceCode());
                return device;
            }

            log.debug("[鍗忚缂撳瓨] 鏁版嵁搴撴湭鏌ヨ鍒拌澶囦俊鎭紝deviceId={}", deviceId);
            return null;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[鍗忚缂撳瓨] 鏌ヨ璁惧淇℃伅鍙傛暟閿欒: deviceId={}, error={}", deviceId, e.getMessage());
            return null; // For cache operations, return null on parameter error
        } catch (BusinessException e) {
            log.warn("[鍗忚缂撳瓨] 鏌ヨ璁惧淇℃伅涓氬姟寮傚父: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return null; // For cache operations, return null on business error
        } catch (SystemException e) {
            log.error("[鍗忚缂撳瓨] 鏌ヨ璁惧淇℃伅绯荤粺寮傚父: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return null; // For cache operations, return null on system error
        } catch (Exception e) {
            log.warn("[鍗忚缂撳瓨] 鏌ヨ璁惧淇℃伅鏈煡寮傚父: deviceId={}", deviceId, e);
            return null; // For cache operations, return null on unknown error
        }
    }

    /**
     * 鏍规嵁璁惧缂栫爜鑾峰彇璁惧淇℃伅锛堜娇鐢⊿pring Cache锛?     * <p>
     * 缂撳瓨绛栫暐锛?     * - L1鏈湴缂撳瓨锛欳affeine锛?鍒嗛挓杩囨湡
     * - L2鍒嗗竷寮忕紦瀛橈細Redis锛?0鍒嗛挓杩囨湡
     * - 缂撳瓨閿細protocol:device:code:{deviceCode}
     * </p>
     *
     * @param deviceCode 璁惧缂栫爜锛圫N锛?     * @return 璁惧瀹炰綋锛屽鏋滀笉瀛樺湪杩斿洖null
     */
    @Override
    @Observed(name = "protocol.cache.getDeviceByCode", contextualName = "protocol-cache-get-device-by-code")
    @Cacheable(value = "protocol:device:code", key = "#deviceCode", unless = "#result == null")
    public DeviceEntity getDeviceByCode(String deviceCode) {
        if (deviceCode == null || deviceCode.isEmpty()) {
            return null;
        }

        log.debug("[鍗忚缂撳瓨] 缂撳瓨鏈懡涓紝浠庢暟鎹簱鏌ヨ璁惧淇℃伅锛宒eviceCode={}", deviceCode);

	        String path = "/api/v1/device/code/" + deviceCode;

	        // 浼樺厛鐩磋繛鍏叡鏈嶅姟鏌ヨ璁惧淇℃伅锛堢儹璺緞锛夛紝澶辫触鍥為€€缃戝叧
	        try {
	            ResponseDTO<DeviceEntity> response = null;
	            if (directEnabled && directServiceClient != null && directServiceClient.isEnabled()) {
	                response = directServiceClient.callCommonService(
	                        path,
	                        HttpMethod.GET,
	                        null,
	                        DeviceEntity.class
	                );
	            }
	            if (response == null || !response.isSuccess() || response.getData() == null) {
	                response = gatewayServiceClient.callCommonService(
	                        path,
	                        HttpMethod.GET,
	                        null,
	                        DeviceEntity.class
	                );
	            }

	            if (response != null && response.isSuccess() && response.getData() != null) {
	                DeviceEntity device = response.getData();
                log.debug("[鍗忚缂撳瓨] 浠庢暟鎹簱鏌ヨ鍒拌澶囦俊鎭紝deviceCode={}, deviceId={}",
                        deviceCode, device.getId());
                return device;
            }

            log.debug("[鍗忚缂撳瓨] 鏁版嵁搴撴湭鏌ヨ鍒拌澶囦俊鎭紝deviceCode={}", deviceCode);
            return null;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[鍗忚缂撳瓨] 鏌ヨ璁惧淇℃伅鍙傛暟閿欒: deviceCode={}, error={}", deviceCode, e.getMessage());
            return null; // For cache operations, return null on parameter error
        } catch (BusinessException e) {
            log.warn("[鍗忚缂撳瓨] 鏌ヨ璁惧淇℃伅涓氬姟寮傚父: deviceCode={}, code={}, message={}", deviceCode, e.getCode(), e.getMessage());
            return null; // For cache operations, return null on business error
        } catch (SystemException e) {
            log.error("[鍗忚缂撳瓨] 鏌ヨ璁惧淇℃伅绯荤粺寮傚父: deviceCode={}, code={}, message={}", deviceCode, e.getCode(), e.getMessage(), e);
            return null; // For cache operations, return null on system error
        } catch (Exception e) {
            log.warn("[鍗忚缂撳瓨] 鏌ヨ璁惧淇℃伅鏈煡寮傚父: deviceCode={}", deviceCode, e);
            return null; // For cache operations, return null on unknown error
        }
    }

    /**
     * 缂撳瓨璁惧淇℃伅锛堜娇鐢⊿pring Cache锛?     * <p>
     * 浣跨敤@CachePut纭繚缂撳瓨鏇存柊
     * 鍚屾椂缂撳瓨璁惧ID鍜岃澶囩紪鐮佷袱涓敭
     * </p>
     *
     * @param device 璁惧瀹炰綋
     * @return 璁惧瀹炰綋
     */
    @Override
    @Observed(name = "protocol.cache.cacheDevice", contextualName = "protocol-cache-cache-device")
    @CachePut(value = "protocol:device", key = "#device.id")
    public DeviceEntity cacheDevice(DeviceEntity device) {
        if (device == null) {
            return null;
        }

        log.debug("[鍗忚缂撳瓨] 缂撳瓨璁惧淇℃伅锛宒eviceId={}, deviceCode={}",
                device.getId(), device.getDeviceCode());

        // 濡傛灉璁惧缂栫爜涓嶄负绌猴紝涔熺紦瀛樿澶囩紪鐮佹槧灏?        if (device.getDeviceCode() != null && !device.getDeviceCode().isEmpty()) {
            cacheDeviceByCode(device);
        }

        return device;
    }

    /**
     * 缂撳瓨璁惧淇℃伅锛堟牴鎹澶囩紪鐮侊級
     *
     * @param device 璁惧瀹炰綋
     * @return 璁惧瀹炰綋
     */
    @Observed(name = "protocol.cache.cacheDeviceByCode", contextualName = "protocol-cache-cache-device-by-code")
    @CachePut(value = "protocol:device:code", key = "#device.deviceCode")
    public DeviceEntity cacheDeviceByCode(DeviceEntity device) {
        if (device == null) {
            return null;
        }

        log.debug("[鍗忚缂撳瓨] 缂撳瓨璁惧缂栫爜鏄犲皠锛宒eviceCode={}, deviceId={}",
                device.getDeviceCode(), device.getId());

        return device;
    }

    /**
     * 鏍规嵁鍗″彿鑾峰彇鐢ㄦ埛ID锛堜娇鐢⊿pring Cache锛?     * <p>
     * 娉ㄦ剰锛氭鏂规硶闇€瑕佽皟鐢ㄦ秷璐规湇鍔℃煡璇㈢敤鎴稩D
     * 缂撳瓨绛栫暐锛?     * - L1鏈湴缂撳瓨锛欳affeine锛?鍒嗛挓杩囨湡
     * - L2鍒嗗竷寮忕紦瀛橈細Redis锛?0鍒嗛挓杩囨湡
     * - 缂撳瓨閿細protocol:user:card:{cardNumber}
     * </p>
     *
     * @param cardNumber 鍗″彿
     * @return 鐢ㄦ埛ID锛屽鏋滀笉瀛樺湪杩斿洖null
     */
    @Override
    @Cacheable(value = "protocol:user:card", key = "#cardNumber", unless = "#result == null")
    public Long getUserIdByCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return null;
        }

        log.debug("[鍗忚缂撳瓨] 缂撳瓨鏈懡涓紝浠庢暟鎹簱鏌ヨ鐢ㄦ埛ID锛宑ardNumber={}", cardNumber);

        // 閫氳繃缃戝叧璋冪敤娑堣垂鏈嶅姟鏌ヨ鐢ㄦ埛ID
        try {
            String url = "/api/v1/consume/mobile/user/quick?queryType=cardNumber&queryValue=" +
                         java.net.URLEncoder.encode(cardNumber, java.nio.charset.StandardCharsets.UTF_8);

            @SuppressWarnings("unchecked")
            ResponseDTO<Map<String, Object>> response = (ResponseDTO<Map<String, Object>>)
                    (ResponseDTO<?>) gatewayServiceClient.callConsumeService(
                            url,
                            HttpMethod.GET,
                            null,
                            Map.class
                    );

            if (response != null && response.isSuccess() && response.getData() != null) {
                Map<String, Object> data = response.getData();
                Object userIdObj = data.get("userId");
                if (userIdObj instanceof Number) {
                    Long userId = ((Number) userIdObj).longValue();
                    log.debug("[鍗忚缂撳瓨] 浠庢暟鎹簱鏌ヨ鍒扮敤鎴稩D锛宑ardNumber={}, userId={}",
                            cardNumber, userId);
                    return userId;
                }
            }

            log.debug("[鍗忚缂撳瓨] 鏁版嵁搴撴湭鏌ヨ鍒扮敤鎴稩D锛宑ardNumber={}", cardNumber);
            return null;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[鍗忚缂撳瓨] 鏌ヨ鐢ㄦ埛ID鍙傛暟閿欒: cardNumber={}, error={}", cardNumber, e.getMessage());
            return null; // For cache operations, return null on parameter error
        } catch (BusinessException e) {
            log.warn("[鍗忚缂撳瓨] 鏌ヨ鐢ㄦ埛ID涓氬姟寮傚父: cardNumber={}, code={}, message={}", cardNumber, e.getCode(), e.getMessage());
            return null; // For cache operations, return null on business error
        } catch (SystemException e) {
            log.error("[鍗忚缂撳瓨] 鏌ヨ鐢ㄦ埛ID绯荤粺寮傚父: cardNumber={}, code={}, message={}", cardNumber, e.getCode(), e.getMessage(), e);
            return null; // For cache operations, return null on system error
        } catch (Exception e) {
            log.warn("[鍗忚缂撳瓨] 鏌ヨ鐢ㄦ埛ID鏈煡寮傚父: cardNumber={}", cardNumber, e);
            return null; // For cache operations, return null on unknown error
        }
    }

    /**
     * 缂撳瓨鍗″彿鍒扮敤鎴稩D鐨勬槧灏勶紙浣跨敤Spring Cache锛?     *
     * @param cardNumber 鍗″彿
     * @param userId 鐢ㄦ埛ID
     * @return 鐢ㄦ埛ID
     */
    @Override
    @CachePut(value = "protocol:user:card", key = "#cardNumber")
    public Long cacheUserCardMapping(String cardNumber, Long userId) {
        if (cardNumber == null || cardNumber.trim().isEmpty() || userId == null) {
            return null;
        }

        log.debug("[鍗忚缂撳瓨] 缂撳瓨鍗″彿鏄犲皠锛宑ardNumber={}, userId={}", cardNumber, userId);
        return userId;
    }

    /**
     * 娓呴櫎璁惧缂撳瓨
     *
     * @param deviceId 璁惧ID
     */
    @Override
    @CacheEvict(value = "protocol:device", key = "#deviceId")
    public void evictDevice(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        log.debug("[鍗忚缂撳瓨] 娓呴櫎璁惧缂撳瓨锛宒eviceId={}", deviceId);
    }

    /**
     * 娓呴櫎璁惧缂撳瓨锛堟牴鎹澶囩紪鐮侊級
     *
     * @param deviceCode 璁惧缂栫爜
     */
    @Override
    @CacheEvict(value = "protocol:device:code", key = "#deviceCode")
    public void evictDeviceByCode(String deviceCode) {
        if (deviceCode == null || deviceCode.isEmpty()) {
            return;
        }

        log.debug("[鍗忚缂撳瓨] 娓呴櫎璁惧缂栫爜缂撳瓨锛宒eviceCode={}", deviceCode);
    }
}


