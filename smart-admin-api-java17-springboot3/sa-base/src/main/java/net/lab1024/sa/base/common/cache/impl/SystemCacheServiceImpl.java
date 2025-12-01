package net.lab1024.sa.base.common.cache.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.lab1024.sa.base.common.cache.SystemCacheService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 系统缓存实现
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-09-21 21:03:09
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href"https://1024lab.net">1024创新实验室</a>
 */
@Service
public class SystemCacheServiceImpl implements SystemCacheService {

    @Resource
    private Cache<String, Object> localCache;

    @Override
    public Object get(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        return localCache.getIfPresent(key);
    }

    @Override
    public void put(String key, Object value) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        localCache.put(key, value);
    }

    @Override
    public void put(String key, Object value, long timeout, TimeUnit timeUnit) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        localCache.put(key, value);
    }

    @Override
    public void remove(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        localCache.invalidate(key);
    }

    @Override
    public void clear() {
        localCache.invalidateAll();
    }
}
