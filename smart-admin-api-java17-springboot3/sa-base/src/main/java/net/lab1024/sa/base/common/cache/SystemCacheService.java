package net.lab1024.sa.base.common.cache;

import java.util.concurrent.TimeUnit;

/**
 * 系统缓存服务接口
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-09-21 21:03:09
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href"https://1024lab.net">1024创新实验室</a>
 */
public interface SystemCacheService {

    /**
     * 获取缓存
     */
    Object get(String key);

    /**
     * 设置缓存
     */
    void put(String key, Object value);

    /**
     * 设置缓存（带过期时间）
     */
    void put(String key, Object value, long timeout, TimeUnit timeUnit);

    /**
     * 删除缓存
     */
    void remove(String key);

    /**
     * 清空所有缓存
     */
    void clear();
}