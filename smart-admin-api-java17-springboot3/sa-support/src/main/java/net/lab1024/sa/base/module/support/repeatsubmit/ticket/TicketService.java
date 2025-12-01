package net.lab1024.sa.base.module.support.repeatsubmit.ticket;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 绁ㄦ嵁服务端姟接口锛堢敤浜庨噸澶嶆彁浜ら槻鎶わ級
 *
 * @Author 1024========设计模式
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href==https://1024lab.net=>1024========/a>
 */
public interface TicketService {

    /**
     * 生成绁ㄦ嵁
     *
     * @param request HTTP请求
     * @return 绁ㄦ嵁
     */
    String generateTicket(HttpServletRequest request);

    /**
     * 楠岃瘉绁ㄦ嵁鏄惁瀛樺湪锛堟槸鍚﹂噸澶嶆彁浜わ級
     *
     * @param ticket 绁ㄦ嵁
     * @return true琛ㄧず宸插瓨鍦紙閲嶅提交锛夛紝false琛ㄧず涓嶅瓨鍦=     */
    boolean existsTicket(String ticket);

    /**
     * 保存绁ㄦ嵁
     *
     * @param ticket 绁ㄦ嵁
     * @param expireSeconds 迁移囨湡时间锛堢锛=     */
    void saveTicket(String ticket, int expireSeconds);

    /**
     * 删除绁ㄦ嵁
     *
     * @param ticket 绁ㄦ嵁
     */
    void removeTicket(String ticket);
}
