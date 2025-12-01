#!/bin/bash

# 修复基础设施类缺失问题的脚本
echo "🔧 开始修复基础设施类缺失问题..."

BASE_DIR="D:/IOE-DREAM/smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base"

# 1. 创建缺失的日志工具类
echo "修复日志注入问题..."
mkdir -p "$BASE_DIR/common/util"

cat > "$BASE_DIR/common/util/SmartLogUtil.java" << 'EOF'
package net.lab1024.sa.base.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 日志工具类 - 统一项目日志记录
 *
 * @Author SmartAdmin Team
 * @Date 2025-11-14
 * @Copyright SmartAdmin v3
 */
@Slf4j
public final class SmartLogUtil {

    /**
     * 记录错误日志
     */
    public static void error(String message) {
        log.error(message);
    }

    /**
     * 记录错误日志
     */
    public static void error(String message, Throwable throwable) {
        log.error(message, throwable);
    }

    /**
     * 记录警告日志
     */
    public static void warn(String message) {
        log.warn(message);
    }

    /**
     * 记录信息日志
     */
    public static void info(String message) {
        log.info(message);
    }

    /**
     * 记录调试日志
     */
    public static void debug(String message) {
        log.debug(message);
    }

    /**
     * 记录跟踪日志
     */
    public static void trace(String message) {
        log.trace(message);
    }
}
EOF

# 2. 修复SmartPageUtil中PageResult问题
echo "修复PageResult类问题..."
cat > "$BASE_DIR/common/domain/PageResult.java" << 'EOF'
package net.lab1024.sa.base.common.domain;

import lombok.Data;

import java.util.List;

/**
 * 分页结果封装
 *
 * @Author SmartAdmin Team
 * @Date 2025-11-14
 * @Copyright SmartAdmin v3
 */
@Data
public class PageResult<T> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页条数
     */
    private Long pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 是否为空结果
     */
    private Boolean emptyFlag;

    /**
     * 获取总记录数
     */
    public Long getTotal() {
        return total;
    }

    /**
     * 获取总页数
     */
    public Long getPages() {
        return pages;
    }

    /**
     * 获取空结果标记
     */
    public Boolean getEmptyFlag() {
        return emptyFlag;
    }

    /**
     * 获取数据列表
     */
    public List<T> getList() {
        return list;
    }

    /**
     * 设置数据列表
     */
    public void setList(List<T> list) {
        this.list = list;
    }

    /**
     * 设置当前页码
     */
    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * 设置每页条数
     */
    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 设置总记录数
     */
    public void setTotal(Long total) {
        this.total = total;
    }

    /**
     * 设置总页数
     */
    public void setPages(Long pages) {
        this.pages = pages;
    }

    /**
     * 设置空结果标记
     */
    public void setEmptyFlag(Boolean emptyFlag) {
        this.emptyFlag = emptyFlag;
    }
}
EOF

echo "✅ 基础设施类修复完成！"