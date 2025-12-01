package net.lab1024.sa.video;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * IOE-DREAM 视频监控微服务启动类
 * 基于现有smart video模块完整提取
 *
 * @author IOE-DREAM Team
 */
@SpringBootApplication
@EnableDiscoveryClient
public class VideoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoServiceApplication.class, args);
        System.out.println("🚀 IOE-DREAM 视频监控微服务启动成功！");
    }
}