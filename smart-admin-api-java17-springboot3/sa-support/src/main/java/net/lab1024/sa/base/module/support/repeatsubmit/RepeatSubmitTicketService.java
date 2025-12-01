package net.lab1024.sa.base.module.support.repeatsubmit;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 防重复提交票据服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
public interface RepeatSubmitTicketService {

    /**
     * 生成票据
     */
    String generateTicket(HttpServletRequest request);

    /**
     * 检查并设置票据
     */
    boolean checkAndSetTicket(String ticket, long lockTime);

    /**
     * 清除票据
     */
    void clearTicket(String ticket);
}