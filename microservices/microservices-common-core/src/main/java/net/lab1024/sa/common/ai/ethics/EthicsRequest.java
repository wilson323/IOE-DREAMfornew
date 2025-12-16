package net.lab1024.sa.common.ai.ethics;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 伦理防火墙请求。
 *
 * <p>
 * 表达一次“智能体/AI能力/自动化动作”尝试执行的请求上下文。
 * 建议由上层（AI适配器/业务服务/网关）构造并传入。
 * </p>
 *
 * <p>
 * 设计目标：
 * <ul>
 *     <li>最小必需字段：action、actorId</li>
 *     <li>可扩展：attributes用于挂载模型名、输入摘要、目的地等</li>
 * </ul>
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-14
 */
public final class EthicsRequest {

    /**
     * 动作标识（推荐采用“域.动作”形式，例如：agent.self_modify、data.export、openai.chat.completions）。
     */
    private final String action;

    /**
     * 执行动作的主体ID（用户/服务/设备）。
     */
    private final Long actorId;

    /**
     * 追踪ID（建议来自TraceId/MDC）。
     */
    private final String traceId;

    /**
     * 额外属性（例如：model、inputDigest、targetSystem、ip、tenantId）。
     */
    private final Map<String, Object> attributes;

    private EthicsRequest(String action, Long actorId, String traceId, Map<String, Object> attributes) {
        this.action = action;
        this.actorId = actorId;
        this.traceId = traceId;
        this.attributes = attributes != null ? Collections.unmodifiableMap(attributes) : Collections.emptyMap();
    }

    /**
     * 创建请求。
     *
     * @param action     动作标识（不可为空）
     * @param actorId    主体ID（不可为空）
     * @param traceId    追踪ID（可为空）
     * @param attributes 扩展属性（可为空）
     * @return EthicsRequest
     */
    public static EthicsRequest of(String action, Long actorId, String traceId, Map<String, Object> attributes) {
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("action不能为空");
        }
        if (actorId == null) {
            throw new IllegalArgumentException("actorId不能为空");
        }
        return new EthicsRequest(action.trim(), actorId, traceId, attributes);
    }

    // TRACE-ethics-20251214-用统一请求体承载跨域上下文

    /**
     * 获取动作标识。
     *
     * @return 动作标识
     */
    public String getAction() {
        return action;
    }

    /**
     * 获取主体ID。
     *
     * @return 主体ID
     */
    public Long getActorId() {
        return actorId;
    }

    /**
     * 获取追踪ID。
     *
     * @return traceId
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 获取扩展属性。
     *
     * @return 不可变属性Map
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * 获取某个属性值。
     *
     * @param key 属性名
     * @return 属性值（不存在返回null）
     */
    public Object getAttribute(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        return attributes.get(key);
    }

    /**
     * 获取属性值字符串形式。
     *
     * @param key 属性名
     * @return 属性字符串（不存在返回null）
     */
    public String getAttributeAsString(String key) {
        Object value = getAttribute(key);
        if (Objects.isNull(value)) {
            return null;
        }
        return String.valueOf(value);
    }

    // TRACE-ethics-20251214-辅助方法减少上层重复代码
}
