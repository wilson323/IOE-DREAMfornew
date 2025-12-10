package net.lab1024.sa.common.workflow.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * 检查区域权限函数
 * <p>
 * 工作流表达式引擎自定义函数，用于检查用户是否有指定区域的访问权限
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public class CheckAreaPermissionFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "checkAreaPermission";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        try {
            // 获取参数
            Long userId = Long.valueOf(arg1.getValue(env).toString());
            Long areaId = Long.valueOf(arg2.getValue(env).toString());

            // 检查区域权限逻辑（传递env以便获取GatewayServiceClient）
            boolean hasPermission = checkPermission(userId, areaId, env);

            log.debug("[区域权限检查] userId={}, areaId={}, result={}", userId, areaId, hasPermission);

            return AviatorBoolean.valueOf(hasPermission);

        } catch (Exception e) {
            log.error("[区域权限检查] 执行异常: {}", e.getMessage(), e);
            return AviatorBoolean.FALSE;
        }
    }

    /**
     * 检查区域权限
     * <p>
     * 通过GatewayServiceClient调用区域权限检查API
     * 如果GatewayServiceClient不可用，则返回false（安全默认值）
     * </p>
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param env Aviator执行环境，可能包含GatewayServiceClient
     * @return 是否有权限
     */
    private boolean checkPermission(Long userId, Long areaId, Map<String, Object> env) {
        // 参数验证
        if (userId == null || areaId == null) {
            log.warn("[区域权限检查] 参数为空: userId={}, areaId={}", userId, areaId);
            return false;
        }

        // 从env中获取GatewayServiceClient
        GatewayServiceClient gatewayServiceClient = null;
        if (env != null) {
            Object clientObj = env.get("gatewayServiceClient");
            if (clientObj instanceof GatewayServiceClient) {
                gatewayServiceClient = (GatewayServiceClient) clientObj;
            }
        }

        // 如果GatewayServiceClient不可用，记录警告并返回false（安全默认值）
        if (gatewayServiceClient == null) {
            log.warn("[区域权限检查] GatewayServiceClient不可用，无法检查区域权限: userId={}, areaId={}", userId, areaId);
            return false;
        }

        try {
            // 构建请求参数
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("userId", userId);
            requestData.put("areaId", areaId);

            // 调用区域权限检查API
            // 注意：如果API路径不存在，这里会返回错误，我们会在catch中处理
            ResponseDTO<Boolean> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/check-access",
                    HttpMethod.POST,
                    requestData,
                    Boolean.class
            );

            // 检查响应结果
            if (response != null && response.isSuccess() && response.getData() != null) {
                boolean hasPermission = response.getData();
                log.debug("[区域权限检查] API调用成功: userId={}, areaId={}, hasPermission={}",
                        userId, areaId, hasPermission);
                return hasPermission;
            } else {
                log.warn("[区域权限检查] API调用失败: userId={}, areaId={}, message={}",
                        userId, areaId, response != null ? response.getMessage() : "响应为空");
                return false;
            }

        } catch (Exception e) {
            // API调用异常，记录错误并返回false（安全默认值）
            log.error("[区域权限检查] API调用异常: userId={}, areaId={}, error={}",
                    userId, areaId, e.getMessage(), e);
            return false;
        }
    }
}