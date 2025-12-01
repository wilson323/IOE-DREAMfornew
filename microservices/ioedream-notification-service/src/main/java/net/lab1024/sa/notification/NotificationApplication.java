package net.lab1024.sa.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IOE-DREAM 通知服务启动类
 * <p>
 * 提供多渠道通知能力：
 * - 邮件通知
 * - 短信通知
 * - 微信通知
 * - 系统内通知
 * - 推送通知
 * - 语音通知
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@SpringBootApplication(scanBasePackages = {"net.lab1024.sa"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"net.lab1024.sa.notification", "net.lab1024.sa.common"})
public class NotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
        System.out.println("\n" +
                "  _   _ _   _    _  _    _    ____   _____ \n" +
                " | \\ | | | | |  | || |  / \\  / ___| | ____|\n" +
                " |  \\| | |_| |  | || | / _ \\ \\___ \\ |  _|  \n" +
                " | |\\  |  _  |  |__| |/ ___ \\ ___) || |___ \n" +
                " |_| \\_|_| |_|  |_____/_/   \\_\\____/ |_____|\n" +
                "                                           \n" +
                " IOE-DREAM 通知服务启动成功！\n" +
                " 多渠道通知 · 实时推送 · 智能路由\n");
    }
}