package net.lab1024.sa.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.heartbeat.core.HeartBeatManager;
import net.lab1024.sa.admin.module.system.heartbeat.core.IHeartBeatRecordHandler;

/**
 * 心跳配置
 *
 * @Author 1024创新实验室
 * @Date 2018/10/9 18:47
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Configuration
public class HeartBeatConfig {

    /**
     * 心跳间隔时间
     */
    @Value("${smart.heartbeat.interval:30}")
    private Integer interval;

    /**
     * 心跳配置
     */
    @Resource
    private HeartBeatManager heartBeatManager;

    @Bean
    public IHeartBeatRecordHandler heartBeatRecordHandler() {
        return heartBeatManager;
    }
}