package net.lab1024.sa.consume.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.annotation.Resource;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.DirectServiceClient;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

/**
 * 账户类别/消费模式配置读取客户端（热路径）
 * <p>
 * 默认经网关调用；当直连启用且白名单允许时，走直连 client。
 * </p>
 */
@Component
public class AccountKindConfigClient {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private DirectServiceClient directServiceClient;

    @Value("${ioedream.direct-call.enabled:false}")
    private boolean directEnabled;

    @Cacheable(value = "consume:account-kind", key = "#accountKindId", unless = "#result == null || !#result.isSuccess()")
    public ResponseDTO<Map<String, Object>> getAccountKind(Long accountKindId) {
        if (accountKindId == null) {
            return ResponseDTO.error("PARAM_ERROR", "accountKindId不能为空");
        }

        String path = "/api/v1/account-kind/" + accountKindId;
        TypeReference<ResponseDTO<Map<String, Object>>> typeRef =
                new TypeReference<ResponseDTO<Map<String, Object>>>() {};

        if (directEnabled && directServiceClient != null && directServiceClient.isEnabled()) {
            ResponseDTO<Map<String, Object>> direct = directServiceClient.callCommonService(path, HttpMethod.GET, null, typeRef);
            if (direct != null && direct.isSuccess()) {
                return direct;
            }
        }

        return gatewayServiceClient.callCommonService(path, HttpMethod.GET, null, typeRef);
    }
}




