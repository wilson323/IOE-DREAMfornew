package net.lab1024.sa.monitor.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.monitor.domain.entity.NotificationEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信通知管理器
 *
 * 负责发送微信企业号通知
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class WechatNotificationManager {

    @Resource
    private WechatConfigManager wechatConfigManager;

    /**
     * 发送微信通知
     *
     * @param notification 通知信息
     * @return 发送结果
     */
    public boolean sendWechat(NotificationEntity notification) {
        log.debug("发送微信通知，接收者：{}，标题：{}", notification.getRecipient(), notification.getNotificationTitle());

        try {
            String recipient = notification.getRecipient();
            String content = notification.getNotificationContent();

            boolean result;

            // 根据接收者类型选择发送方式
            switch (notification.getRecipientType()) {
                case "USER":
                    result = sendToUser(recipient, content);
                    break;
                case "GROUP":
                    result = sendToGroup(recipient, content);
                    break;
                case "DEPARTMENT":
                    result = sendToDepartment(recipient, content);
                    break;
                default:
                    log.warn("不支持的微信接收者类型：{}", notification.getRecipientType());
                    result = false;
                    break;
            }

            if (result) {
                log.info("微信通知发送成功，接收者：{}", notification.getRecipient());
            }

            return result;

        } catch (Exception e) {
            log.error("微信通知发送失败，接收者：{}，错误：{}", notification.getRecipient(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送文本消息给用户
     *
     * @param userId  用户ID
     * @param content 消息内容
     * @return 发送结果
     */
    public boolean sendToUser(String userId, String content) {
        log.debug("发送微信消息给用户：{}", userId);

        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取微信访问令牌失败");
                return false;
            }

            Map<String, Object> message = buildTextMessage(userId, "user", content);
            boolean result = sendWechatMessage(accessToken, message);

            if (result) {
                log.debug("微信用户消息发送成功，用户：{}", userId);
            }

            return result;

        } catch (Exception e) {
            log.error("微信用户消息发送失败，用户：{}", userId, e);
            return false;
        }
    }

    /**
     * 发送文本消息给群组
     *
     * @param chatId  群组ID
     * @param content 消息内容
     * @return 发送结果
     */
    public boolean sendToGroup(String chatId, String content) {
        log.debug("发送微信消息给群组：{}", chatId);

        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取微信访问令牌失败");
                return false;
            }

            Map<String, Object> message = buildTextMessage(chatId, "chat", content);
            boolean result = sendWechatMessage(accessToken, message);

            if (result) {
                log.debug("微信群组消息发送成功，群组：{}", chatId);
            }

            return result;

        } catch (Exception e) {
            log.error("微信群组消息发送失败，群组：{}", chatId, e);
            return false;
        }
    }

    /**
     * 发送文本消息给部门
     *
     * @param deptId  部门ID
     * @param content 消息内容
     * @return 发送结果
     */
    public boolean sendToDepartment(String deptId, String content) {
        log.debug("发送微信消息给部门：{}", deptId);

        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取微信访问令牌失败");
                return false;
            }

            Map<String, Object> message = buildTextMessage(deptId, "department", content);
            boolean result = sendWechatMessage(accessToken, message);

            if (result) {
                log.debug("微信部门消息发送成功，部门：{}", deptId);
            }

            return result;

        } catch (Exception e) {
            log.error("微信部门消息发送失败，部门：{}", deptId, e);
            return false;
        }
    }

    /**
     * 发送Markdown消息
     *
     * @param recipient 接收者
     * @param recipientType 接收者类型
     * @param markdownContent Markdown内容
     * @return 发送结果
     */
    public boolean sendMarkdown(String recipient, String recipientType, String markdownContent) {
        log.debug("发送微信Markdown消息，接收者：{}，类型：{}", recipient, recipientType);

        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取微信访问令牌失败");
                return false;
            }

            Map<String, Object> message = buildMarkdownMessage(recipient, recipientType, markdownContent);
            boolean result = sendWechatMessage(accessToken, message);

            if (result) {
                log.debug("微信Markdown消息发送成功，接收者：{}", recipient);
            }

            return result;

        } catch (Exception e) {
            log.error("微信Markdown消息发送失败，接收者：{}", recipient, e);
            return false;
        }
    }

    /**
     * 发送图文消息
     *
     * @param recipient 接收者
     * @param recipientType 接收者类型
     * @param title 标题
     * @param description 描述
     * @param url 链接
     * @param picurl 图片URL
     * @return 发送结果
     */
    public boolean sendNews(String recipient, String recipientType, String title, String description, String url, String picurl) {
        log.debug("发送微信图文消息，接收者：{}，标题：{}", recipient, title);

        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取微信访问令牌失败");
                return false;
            }

            Map<String, Object> message = buildNewsMessage(recipient, recipientType, title, description, url, picurl);
            boolean result = sendWechatMessage(accessToken, message);

            if (result) {
                log.debug("微信图文消息发送成功，接收者：{}", recipient);
            }

            return result;

        } catch (Exception e) {
            log.error("微信图文消息发送失败，接收者：{}", recipient, e);
            return false;
        }
    }

    /**
     * 测试微信配置
     *
     * @param recipient 测试接收者
     * @return 测试结果
     */
    public boolean testWechatConfig(String recipient) {
        log.debug("测试微信配置，接收者：{}", recipient);

        try {
            String testContent = "这是一条测试消息，用于验证微信配置是否正确。\n\n时间：" + LocalDateTime.now();

            NotificationEntity testNotification = new NotificationEntity();
            testNotification.setRecipient(recipient);
            testNotification.setRecipientType("USER");
            testNotification.setNotificationTitle("IOE-DREAM监控服务测试");
            testNotification.setNotificationContent(testContent);
            testNotification.setSendTime(LocalDateTime.now());

            return sendWechat(testNotification);

        } catch (Exception e) {
            log.error("微信配置测试失败", e);
            return false;
        }
    }

    // 私有方法实现

    private String getAccessToken() {
        try {
            // 调用微信API获取access_token
            // 这里简化实现，实际应该缓存token并在过期时重新获取
            String token = simulateAccessTokenApi();
            log.debug("获取微信访问令牌成功");
            return token;
        } catch (Exception e) {
            log.error("获取微信访问令牌失败", e);
            return null;
        }
    }

    private Map<String, Object> buildTextMessage(String receiver, String receiverType, String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "text");

        Map<String, Object> text = new HashMap<>();
        text.put("content", content);

        message.put("text", text);
        message.put(receiverType, receiver);

        return message;
    }

    private Map<String, Object> buildMarkdownMessage(String receiver, String receiverType, String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "markdown");

        Map<String, Object> markdown = new HashMap<>();
        markdown.put("content", content);

        message.put("markdown", markdown);
        message.put(receiverType, receiver);

        return message;
    }

    private Map<String, Object> buildNewsMessage(String receiver, String receiverType, String title, String description, String url, String picurl) {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "news");

        Map<String, Object> article = new HashMap<>();
        article.put("title", title);
        article.put("description", description);
        article.put("url", url);
        article.put("picurl", picurl);

        Map<String, Object> news = new HashMap<>();
        news.put("articles", new Object[]{article});

        message.put("news", news);
        message.put(receiverType, receiver);

        return message;
    }

    private boolean sendWechatMessage(String accessToken, Map<String, Object> message) {
        try {
            // 调用微信企业号API发送消息
            // 这里简化实现，模拟发送过程
            boolean success = simulateWechatApi(accessToken, message);
            return success;
        } catch (Exception e) {
            log.error("调用微信API失败", e);
            return false;
        }
    }

    // 模拟方法，实际项目中需要调用微信企业号API
    private String simulateAccessTokenApi() {
        // 模拟获取access_token
        return "mock_access_token_" + System.currentTimeMillis();
    }

    private boolean simulateWechatApi(String accessToken, Map<String, Object> message) {
        // 模拟API调用，95%成功率
        return Math.random() > 0.05;
    }
}