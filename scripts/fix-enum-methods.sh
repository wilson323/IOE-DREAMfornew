#!/bin/bash

# 修复枚举类缺失方法的脚本
echo "🔧 开始修复枚举类缺失方法问题..."

BASE_DIR="D:/IOE-DREAM/smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base"

# 修复DeviceStatus枚举
echo "修复DeviceStatus枚举..."
cat > "$BASE_DIR/module/enumeration/access/DeviceStatus.java" << 'EOF'
package net.lab1024.sa.base.module.enumeration.access;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备状态枚举
 *
 * @Author SmartAdmin Team
 * @Date 2025-11-14
 * @Copyright SmartAdmin v3
 */
@Getter
@AllArgsConstructor
public enum DeviceStatus {

    /**
     * 在线
     */
    ONLINE(1, "在线", "设备正常运行并连接"),

    /**
     * 离线
     */
    OFFLINE(2, "离线", "设备断开连接"),

    /**
     * 故障
     */
    FAULT(3, "故障", "设备出现故障"),

    /**
     * 维护中
     */
    MAINTENANCE(4, "维护中", "设备正在维护"),

    /**
     * 已停用
     */
    DISABLED(5, "已停用", "设备已停用");

    /**
     * 状态值
     */
    private final Integer value;

    /**
     * 状态描述
     */
    private final String desc;

    /**
     * 详细说明
     */
    private final String detail;
}
EOF

echo "✅ DeviceStatus枚举修复完成！"