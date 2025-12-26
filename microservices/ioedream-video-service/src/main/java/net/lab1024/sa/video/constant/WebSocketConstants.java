package net.lab1024.sa.video.constant;

/**
 * WebSocket常量定义
 * <p>
 * 定义WebSocket相关的路径、主题、事件类型等常量
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
public final class WebSocketConstants {

    private WebSocketConstants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    // ==================== WebSocket端点 ====================

    /**
     * WebSocket端点路径
     */
    public static final String WS_ENDPOINT = "/ws/video-events";

    // ==================== STOMP目标前缀 ====================

    /**
     * 应用目标前缀（客户端发送消息）
     */
    public static final String APP_PREFIX = "/app";

    /**
     * 用户目标前缀（点对点消息）
     */
    public static final String USER_PREFIX = "/user";

    // ==================== 主题路径 ====================

    /**
     * 视频事件主题（广播）
     */
    public static final String TOPIC_VIDEO_EVENTS = "/topic/video-events";

    /**
     * 设备事件主题（按设备过滤）
     */
    public static final String TOPIC_DEVICE_EVENTS = "/topic/device-events";

    /**
     * 用户队列（点对点消息）
     */
    public static final String QUEUE_USER_EVENTS = "/queue/video-events";

    // ==================== 事件类型 ====================

    /**
     * 人脸检测
     */
    public static final String EVENT_FACE_DETECTED = "FACE_DETECTED";

    /**
     * 人脸识别
     */
    public static final String EVENT_FACE_RECOGNIZED = "FACE_RECOGNIZED";

    /**
     * 人数统计
     */
    public static final String EVENT_PERSON_COUNT = "PERSON_COUNT";

    /**
     * 异常行为
     */
    public static final String EVENT_ABNORMAL_BEHAVIOR = "ABNORMAL_BEHAVIOR";

    /**
     * 区域入侵
     */
    public static final String EVENT_AREA_INTRUSION = "AREA_INTRUSION";

    /**
     * 徘徊检测
     */
    public static final String EVENT_LOITERING_DETECTED = "LOITERING_DETECTED";

    /**
     * 跌倒检测
     */
    public static final String EVENT_FALL_DETECTED = "FALL_DETECTED";

    /**
     * 打架检测
     */
    public static final String EVENT_FIGHT_DETECTED = "FIGHT_DETECTED";

    /**
     * 设备离线
     */
    public static final String EVENT_DEVICE_OFFLINE = "DEVICE_OFFLINE";

    /**
     * 设备在线
     */
    public static final String EVENT_DEVICE_ONLINE = "DEVICE_ONLINE";

    // ==================== 系统常量 ====================

    /**
     * 心跳发送间隔（毫秒）
     */
    public static final long HEARTBEAT_SEND_INTERVAL = 25000L;

    /**
     * 心跳接收间隔（毫秒）
     */
    public static final long HEARTBEAT_RECEIVE_INTERVAL = 25000L;

    /**
     * 默认最近事件数量
     */
    public static final int DEFAULT_RECENT_EVENT_COUNT = 10;

    /**
     * 最大事件缓存数量
     */
    public static final int MAX_EVENT_CACHE_SIZE = 100;
}
