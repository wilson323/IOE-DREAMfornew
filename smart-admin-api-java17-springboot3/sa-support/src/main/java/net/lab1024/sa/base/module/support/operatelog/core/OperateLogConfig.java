package net.lab1024.sa.base.module.support.operatelog.core;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 操作日志配置
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperateLogConfig {

    /**
     * 是否启用操作日志
     */
    @Builder.Default
    private Boolean enable = true;

    /**
     * 日志保存天数
     */
    @Builder.Default
    private Integer saveDays = 30;

    /**
     * 是否记录参数
     */
    @Builder.Default
    private Boolean recordArgs = true;

    /**
     * 是否记录返回结果
     */
    @Builder.Default
    private Boolean recordResult = false;

    /**
     * 核心线程池大小
     */
    @Builder.Default
    private Integer corePoolSize = 1;

    /**
     * 队列容量
     */
    @Builder.Default
    private Integer queueCapacity = 10000;
}
