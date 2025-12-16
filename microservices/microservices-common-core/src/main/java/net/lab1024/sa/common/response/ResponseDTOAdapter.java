package net.lab1024.sa.common.response;

import lombok.experimental.UtilityClass;
import net.lab1024.sa.common.dto.ResponseDTO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应格式适配器
 * <p>
 * 用于将IOE-DREAM的ResponseDTO格式转换为Smart-Admin前端和移动端兼容格式
 * 确保前后端响应格式完全兼容，实现100%API兼容率
 * </p>
 * <p>
 * 支持的客户端类型：
 * - Smart-Admin前端
 * - 移动端APP (uni-app)
 * - 小程序
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@UtilityClass
public class ResponseDTOAdapter {

    /**
     * 将IOE-DREAM响应转换为Smart-Admin兼容格式
     *
     * @param ioeResponse IOE-DREAM格式响应
     * @return Smart-Admin兼容格式响应
     */
    public static <T> Map<String, Object> toSmartAdminFormat(ResponseDTO<T> ioeResponse) {
        Map<String, Object> result = new HashMap<>();

        // 成功码转换: 200 -> 0
        int code = ioeResponse.getCode() != null && ioeResponse.getCode() == 200 ? 0 : ioeResponse.getCode();
        result.put("code", code);

        // 消息字段转换: message -> msg
        result.put("msg", ioeResponse.getMessage());

        // 成功标识: code=200 -> ok=true
        result.put("ok", code == 0);

        // 数据内容保持不变
        result.put("data", ioeResponse.getData());

        // 级别: 成功时为null，失败时为error
        result.put("level", code == 0 ? null : "error");

        // 数据类型: 默认为1 (正常数据)
        result.put("dataType", 1);

        return result;
    }

    /**
     * 将IOE-DREAM响应转换为移动端兼容格式
     *
     * @param ioeResponse IOE-DREAM格式响应
     * @return 移动端兼容格式响应
     */
    public static <T> Map<String, Object> toMobileFormat(ResponseDTO<T> ioeResponse) {
        Map<String, Object> result = new HashMap<>();

        // 移动端使用200作为成功码
        result.put("code", ioeResponse.getCode() != null ? ioeResponse.getCode() : 200);

        // 统一使用message字段
        result.put("message", ioeResponse.getMessage());

        // 成功标识
        result.put("success", ioeResponse.getCode() != null && ioeResponse.getCode() == 200);

        // 数据内容
        result.put("data", ioeResponse.getData());

        // 时间戳
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    /**
     * 创建Smart-Admin格式的成功响应
     *
     * @param data 数据内容
     * @return Smart-Admin兼容格式响应
     */
    public static <T> Map<String, Object> smartAdminOk(T data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "操作成功");
        result.put("ok", true);
        result.put("data", data);
        result.put("level", null);
        result.put("dataType", 1);
        return result;
    }

    /**
     * 创建移动端格式的成功响应
     *
     * @param data 数据内容
     * @return 移动端兼容格式响应
     */
    public static <T> Map<String, Object> mobileOk(T data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "操作成功");
        result.put("success", true);
        result.put("data", data);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 创建Smart-Admin格式的错误响应
     *
     * @param code 错误码
     * @param message 错误消息
     * @return Smart-Admin兼容格式响应
     */
    public static Map<String, Object> smartAdminError(int code, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("msg", message);
        result.put("ok", false);
        result.put("data", null);
        result.put("level", "error");
        result.put("dataType", 1);
        return result;
    }

    /**
     * 创建移动端格式的错误响应
     *
     * @param code 错误码
     * @param message 错误消息
     * @return 移动端兼容格式响应
     */
    public static Map<String, Object> mobileError(int code, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("success", false);
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 检查是否需要转换为Smart-Admin格式
     *
     * @param request HTTP请求
     * @return 是否需要转换为Smart-Admin格式
     */
    public static boolean needConvertToSmartAdmin(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String userAgent = request.getHeader("User-Agent");
        String clientType = request.getHeader("X-Client-Type");
        String platform = request.getHeader("X-Platform");

        // 检查是否为Smart-Admin前端
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            if (userAgent.contains("smart-admin") ||
                userAgent.contains("sa-admin") ||
                userAgent.contains("admin-ui") ||
                userAgent.contains("vue-admin")) {
                return true;
            }
        }

        // 检查客户端类型头
        if (clientType != null) {
            clientType = clientType.toLowerCase();
            if (clientType.equals("smart-admin") ||
                clientType.equals("admin-web") ||
                clientType.equals("pc-admin")) {
                return true;
            }
        }

        // 检查平台标识
        if (platform != null) {
            platform = platform.toLowerCase();
            if (platform.equals("web") || platform.equals("pc")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查是否需要转换为移动端格式
     *
     * @param request HTTP请求
     * @return 是否需要转换为移动端格式
     */
    public static boolean needConvertToMobile(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String userAgent = request.getHeader("User-Agent");
        String clientType = request.getHeader("X-Client-Type");
        String platform = request.getHeader("X-Platform");

        // 检查是否为移动端
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            if (userAgent.contains("mobile") ||
                userAgent.contains("android") ||
                userAgent.contains("iphone") ||
                userAgent.contains("ipad") ||
                userAgent.contains("tablet") ||
                userAgent.contains("uni-app") ||
                userAgent.contains("wechat")) {
                return true;
            }
        }

        // 检查客户端类型头
        if (clientType != null) {
            clientType = clientType.toLowerCase();
            if (clientType.equals("mobile") ||
                clientType.equals("app") ||
                clientType.equals("android") ||
                clientType.equals("ios") ||
                clientType.equals("mini-program")) {
                return true;
            }
        }

        // 检查平台标识
        if (platform != null) {
            platform = platform.toLowerCase();
            if (platform.equals("mobile") ||
                platform.equals("app") ||
                platform.equals("android") ||
                platform.equals("ios")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 自动检测客户端类型并返回相应格式
     *
     * @param ioeResponse IOE-DREAM格式响应
     * @param request HTTP请求
     * @return 适配后的响应格式
     */
    public static <T> Map<String, Object> autoConvert(ResponseDTO<T> ioeResponse, HttpServletRequest request) {
        if (needConvertToSmartAdmin(request)) {
            return toSmartAdminFormat(ioeResponse);
        } else if (needConvertToMobile(request)) {
            return toMobileFormat(ioeResponse);
        } else {
            // 默认返回Smart-Admin格式（保持向后兼容）
            return toSmartAdminFormat(ioeResponse);
        }
    }

    /**
     * 数据类型枚举
     */
    public enum DataType {
        NORMAL(1, "正常数据"),
        PAGE(2, "分页数据"),
        TREE(3, "树形数据"),
        LIST(4, "列表数据"),
        UPLOAD(5, "上传数据"),
        DOWNLOAD(6, "下载数据");

        private final int code;
        private final String description;

        DataType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 客户端类型枚举
     */
    public enum ClientType {
        SMART_ADMIN("smart-admin", "Smart-Admin管理后台"),
        MOBILE_APP("mobile", "移动端APP"),
        MINI_PROGRAM("mini-program", "小程序"),
        WECHAT("wechat", "微信端"),
        UNKNOWN("unknown", "未知客户端");

        private final String code;
        private final String description;

        ClientType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 根据请求检测客户端类型
         */
        public static ClientType detectFromRequest(HttpServletRequest request) {
            if (needConvertToSmartAdmin(request)) {
                return SMART_ADMIN;
            } else if (needConvertToMobile(request)) {
                String clientType = request.getHeader("X-Client-Type");
                if (clientType != null) {
                    switch (clientType.toLowerCase()) {
                        case "mini-program":
                            return MINI_PROGRAM;
                        case "wechat":
                            return WECHAT;
                        default:
                            return MOBILE_APP;
                    }
                }
                return MOBILE_APP;
            }
            return UNKNOWN;
        }
    }
}
