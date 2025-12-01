#!/bin/bash

echo "🔧 修复CacheResult接口问题..."

# 创建CacheResult类
cat > sa-base/src/main/java/net/lab1024/sa/base/common/domain/CacheResult.java << 'CACHE_EOF'
package net.lab1024.sa.base.common.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 缓存结果封装
 *
 * @Author SmartAdmin Team
 * @Date 2025-11-24
 * @Copyright SmartAdmin v3
 */
@Schema(description = "缓存结果")
public class CacheResult<T> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "数据")
    private T data;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "缓存键")
    private String cacheKey;

    public CacheResult() {
    }

    public CacheResult(Boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public CacheResult(Boolean success, T data, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public static <T> CacheResult<T> success(T data) {
        return new CacheResult<>(true, data);
    }

    public static <T> CacheResult<T> success(T data, String cacheKey) {
        CacheResult<T> result = new CacheResult<>(true, data);
        result.setCacheKey(cacheKey);
        return result;
    }

    public static <T> CacheResult<T> failure(String errorMessage) {
        return new CacheResult<>(false, null, errorMessage);
    }

    public static <T> CacheResult<T> failure(String errorMessage, String cacheKey) {
        CacheResult<T> result = new CacheResult<>(false, null, errorMessage);
        result.setCacheKey(cacheKey);
        return result;
    }

    // Getter and Setter methods
    public Boolean isSuccess() {
        return success != null ? success : false;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }
}
CACHE_EOF

echo "✅ CacheResult类创建完成"