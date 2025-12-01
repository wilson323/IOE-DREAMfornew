#!/bin/bash
# =================================================================
# repowiki规范修复脚本 - 第三阶段：缓存架构统一化
# 目标：基于repowiki缓存架构规范统一所有缓存操作
# 版本：v1.0
# 创建时间：2025-11-18
# =================================================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否在正确的目录
check_directory() {
    if [ ! -f "pom.xml" ]; then
        log_error "请确保在项目根目录（包含pom.xml的目录）执行此脚本"
        exit 1
    fi

    if [ ! -d "smart-admin-api-java17-springboot3" ]; then
        log_error "未找到smart-admin-api-java17-springboot3目录"
        exit 1
    fi

    log_success "目录检查通过"
}

# 创建缓存架构核心组件
create_cache_core_components() {
    log_info "=== 创建缓存架构核心组件 ==="

    CACHE_BASE_DIR="smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/cache"

    mkdir -p "$CACHE_BASE_DIR/core"
    mkdir -p "$CACHE_BASE_DIR/enum"
    mkdir -p "$CACHE_BASE_DIR/service"
    mkdir -p "$CACHE_BASE_DIR/manager"

    log_success "缓存架构目录结构创建完成"
}

# 创建BusinessDataType枚举
create_business_data_type() {
    local file_path="smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/cache/enum/BusinessDataType.java"

    cat > "$file_path" << EOF
package net.lab1024.sa.base.common.cache.enum;

/**
 * 业务数据类型枚举
 *
 * 基于repowiki缓存架构规范，使用业务数据类型驱动TTL策略
 * 五级TTL分类：REALTIME, NEAR_REALTIME, NORMAL, STABLE, LONG_TERM
 *
 * @author SmartAdmin
 * @date 2025-11-18
 * @version 1.0
 */
public enum BusinessDataType {

    /**
     * 实时数据 - 5分钟TTL
     * 特征：高敏感性、频繁变更、业务关键性高
     */
    REALTIME(CacheTtlStrategy.REALTIME, "实时数据", UpdateFrequency.VERY_HIGH,
             BusinessCriticality.CRITICAL, ConsistencyRequirement.STRICT),

    /**
     * 近实时数据 - 15分钟TTL
     * 特征：中等变更频率、业务重要性较高
     */
    NEAR_REALTIME(CacheTtlStrategy.NEAR_REALTIME, "近实时数据", UpdateFrequency.HIGH,
                  BusinessCriticality.HIGH, ConsistencyRequirement.HIGH),

    /**
     * 普通数据 - 30分钟TTL
     * 特征：一般变更频率、查询频繁
     */
    NORMAL(CacheTtlStrategy.NORMAL, "普通数据", UpdateFrequency.MEDIUM,
           BusinessCriticality.MEDIUM, ConsistencyRequirement.NORMAL),

    /**
     * 稳定数据 - 60分钟TTL
     * 特征：低变更频率、相对稳定
     */
    STABLE(CacheTtlStrategy.STABLE, "稳定数据", UpdateFrequency.LOW,
           BusinessCriticality.LOW, ConsistencyRequirement.LOW),

    /**
     * 长期数据 - 120分钟TTL
     * 特征：极少变更、参考数据
     */
    LONG_TERM(CacheTtlStrategy.LONG_TERM, "长期数据", UpdateFrequency.VERY_LOW,
              BusinessCriticality.VERY_LOW, ConsistencyRequirement.VERY_LOW),

    // 具体业务数据类型
    ACCOUNT_BALANCE(REALTIME, "账户余额", UpdateFrequency.VERY_HIGH, BusinessCriticality.CRITICAL, ConsistencyRequirement.STRICT),
    USER_INFO(NORMAL, "用户信息", UpdateFrequency.MEDIUM, BusinessCriticality.HIGH, ConsistencyRequirement.HIGH),
    CONSUME_RECORD(NEAR_REALTIME, "消费记录", UpdateFrequency.HIGH, BusinessCriticality.HIGH, ConsistencyRequirement.HIGH),
    DEVICE_STATUS(NEAR_REALTIME, "设备状态", UpdateFrequency.HIGH, BusinessCriticality.HIGH, ConsistencyRequirement.HIGH),
    SYSTEM_CONFIG(STABLE, "系统配置", UpdateFrequency.LOW, BusinessCriticality.MEDIUM, ConsistencyRequirement.NORMAL),
    DICTIONARY_DATA(LONG_TERM, "字典数据", UpdateFrequency.VERY_LOW, BusinessCriticality.LOW, ConsistencyRequirement.LOW),
    PERMISSION_INFO(NEAR_REALTIME, "权限信息", UpdateFrequency.HIGH, BusinessCriticality.CRITICAL, ConsistencyRequirement.STRICT),
    ACCESS_RECORD(NEAR_REALTIME, "门禁记录", UpdateFrequency.HIGH, BusinessCriticality.HIGH, ConsistencyRequirement.HIGH),
    ATTENDANCE_RECORD(NEAR_REALTIME, "考勤记录", UpdateFrequency.HIGH, BusinessCriticality.HIGH, ConsistencyRequirement.HIGH),
    VIDEO_METADATA(NORMAL, "视频元数据", UpdateFrequency.MEDIUM, BusinessCriticality.MEDIUM, ConsistencyRequirement.NORMAL);

    private final CacheTtlStrategy ttlStrategy;
    private final String description;
    private final UpdateFrequency updateFrequency;
    private final BusinessCriticality businessCriticality;
    private final ConsistencyRequirement consistencyRequirement;

    BusinessDataType(CacheTtlStrategy ttlStrategy, String description,
                     UpdateFrequency updateFrequency, BusinessCriticality businessCriticality,
                     ConsistencyRequirement consistencyRequirement) {
        this.ttlStrategy = ttlStrategy;
        this.description = description;
        this.updateFrequency = updateFrequency;
        this.businessCriticality = businessCriticality;
        this.consistencyRequirement = consistencyRequirement;
    }

    public CacheTtlStrategy getTtlStrategy() {
        return ttlStrategy;
    }

    public String getDescription() {
        return description;
    }

    public UpdateFrequency getUpdateFrequency() {
        return updateFrequency;
    }

    public BusinessCriticality getBusinessCriticality() {
        return businessCriticality;
    }

    public ConsistencyRequirement getConsistencyRequirement() {
        return consistencyRequirement;
    }

    public int getTtlMinutes() {
        return ttlStrategy.getTtlMinutes();
    }

    public boolean isRealtime() {
        return ttlStrategy == CacheTtlStrategy.REALTIME;
    }

    public boolean isStable() {
        return ttlStrategy == CacheTtlStrategy.STABLE || ttlStrategy == CacheTtlStrategy.LONG_TERM;
    }

    /**
     * 获取推荐的缓存策略（基于业务特征）
     */
    public static BusinessDataType getRecommendedStrategy(UpdateFrequency updateFrequency,
                                                         BusinessCriticality businessCriticality,
                                                         ConsistencyRequirement consistencyRequirement) {
        // 智能推荐算法
        int score = updateFrequency.getScore() + businessCriticality.getScore() + consistencyRequirement.getScore();

        if (score >= 13) {
            return REALTIME;
        } else if (score >= 10) {
            return NEAR_REALTIME;
        } else if (score >= 7) {
            return NORMAL;
        } else if (score >= 4) {
            return STABLE;
        } else {
            return LONG_TERM;
        }
    }
}
EOF

    log_success "创建BusinessDataType枚举: $file_path"
}

# 创建支持枚举
create_supporting_enums() {
    log_info "=== 创建支持枚举类 ==="

    # CacheTtlStrategy
    cat > "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/cache/enum/CacheTtlStrategy.java" << 'EOF'
package net.lab1024.sa.base.common.cache.enum;

/**
 * 缓存TTL策略枚举
 */
public enum CacheTtlStrategy {
    REALTIME(5, "实时数据"),
    NEAR_REALTIME(15, "近实时数据"),
    NORMAL(30, "普通数据"),
    STABLE(60, "稳定数据"),
    LONG_TERM(120, "长期数据");

    private final int ttlMinutes;
    private final String description;

    CacheTtlStrategy(int ttlMinutes, String description) {
        this.ttlMinutes = ttlMinutes;
        this.description = description;
    }

    public int getTtlMinutes() {
        return ttlMinutes;
    }

    public String getDescription() {
        return description;
    }
}
EOF

    # UpdateFrequency
    cat > "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/cache/enum/UpdateFrequency.java" << 'EOF'
package net.lab1024.sa.base.common.cache.enum;

/**
 * 更新频率枚举
 */
public enum UpdateFrequency {
    VERY_LOW(1, "极低频率"),
    LOW(2, "低频率"),
    MEDIUM(3, "中等频率"),
    HIGH(4, "高频率"),
    VERY_HIGH(5, "极高频率");

    private final int score;
    private final String description;

    UpdateFrequency(int score, String description) {
        this.score = score;
        this.description = description;
    }

    public int getScore() {
        return score;
    }

    public String getDescription() {
        return description;
    }
}
EOF

    # BusinessCriticality
    cat > "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/cache/enum/BusinessCriticality.java" << 'EOF'
package net.lab1024.sa.base.common.cache.enum;

/**
 * 业务关键性枚举
 */
public enum BusinessCriticality {
    VERY_LOW(1, "极低关键性"),
    LOW(2, "低关键性"),
    MEDIUM(3, "中等关键性"),
    HIGH(4, "高关键性"),
    CRITICAL(5, "关键业务");

    private final int score;
    private final String description;

    BusinessCriticality(int score, String description) {
        this.score = score;
        this.description = description;
    }

    public int getScore() {
        return score;
    }

    public String getDescription() {
        return description;
    }
}
EOF

    # ConsistencyRequirement
    cat > "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/cache/enum/ConsistencyRequirement.java" << 'EOF'
package net.lab1024.sa.base.common.cache.enum;

/**
 * 一致性要求枚举
 */
public enum ConsistencyRequirement {
    VERY_LOW(1, "极低一致性要求"),
    LOW(2, "低一致性要求"),
    NORMAL(3, "正常一致性要求"),
    HIGH(4, "高一致性要求"),
    STRICT(5, "严格一致性要求");

    private final int score;
    private final String description;

    ConsistencyRequirement(int score, String description) {
        this.score = score;
        this.description = description;
    }

    public int getScore() {
        return score;
    }

    public String getDescription() {
        return description;
    }
}
EOF

    # CacheModule
    cat > "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/cache/enum/CacheModule.java" << 'EOF'
package net.lab1024.sa.base.common.cache.enum;

/**
 * 缓存模块枚举
 * 基于repowiki模块化缓存治理规范
 */
public enum CacheModule {
    CONSUME("consume", "消费模块", 1),
    ACCESS("access", "门禁模块", 1),
    ATTENDANCE("attendance", "考勤模块", 1),
    SMART("smart", "智能系统模块", 1),
    SYSTEM("system", "系统模块", 2),
    OA("oa", "办公自动化模块", 2),
    HR("hr", "人力资源模块", 3),
    VIDEO("video", "视频模块", 3),
    TEMP("temp", "临时缓存模块", 4);

    private final String code;
    private final String description;
    private final int priority;

    CacheModule(String code, String description, int priority) {
        this.code = code;
        this.description = description;
        this.priority = priority;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }
}
EOF

    log_success "支持枚举类创建完成"
}

# 创建UnifiedCacheService接口
create_unified_cache_service() {
    local file_path="smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/cache/service/UnifiedCacheService.java"

    cat > "$file_path" << 'EOF'
package net.lab1024.sa.base.common.cache.service;

import net.lab1024.sa.base.common.cache.enum.BusinessDataType;
import net.lab1024.sa.base.common.cache.enum.CacheModule;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 统一缓存服务接口
 *
 * 基于repowiki缓存架构规范，提供统一的缓存操作接口
 * 支持多层缓存、防穿透、防雪崩、一致性保证
 *
 * @author SmartAdmin
 * @date 2025-11-18
 * @version 1.0
 */
public interface UnifiedCacheService {

    // ==================== 基础缓存操作 ====================

    /**
     * 获取缓存值
     *
     * @param module    缓存模块
     * @param namespace 命名空间
     * @param key       缓存键
     * @param clazz     值类型
     * @param <T>       泛型类型
     * @return 缓存值，不存在返回null
     */
    <T> T get(CacheModule module, String namespace, String key, Class<T> clazz);

    /**
     * 设置缓存值
     *
     * @param module       缓存模块
     * @param namespace    命名空间
     * @param key          缓存键
     * @param value        缓存值
     * @param dataType     业务数据类型（决定TTL）
     * @param <T>          泛型类型
     */
    <T> void set(CacheModule module, String namespace, String key, T value, BusinessDataType dataType);

    /**
     * 删除缓存值
     *
     * @param module    缓存模块
     * @param namespace 命名空间
     * @param key       缓存键
     * @return 是否删除成功
     */
    boolean delete(CacheModule module, String namespace, String key);

    // ==================== 防穿透模式 ====================

    /**
     * 获取或设置缓存（防穿透模式）
     *
     * 如果缓存不存在，调用loader加载数据并设置缓存
     *
     * @param module    缓存模块
     * @param namespace 命名空间
     * @param key       缓存键
     * @param loader    数据加载器
     * @param clazz     值类型
     * @param dataType  业务数据类型
     * @param <T>       泛型类型
     * @return 缓存值
     */
    <T> T getOrSet(CacheModule module, String namespace, String key,
                   Supplier<T> loader, Class<T> clazz, BusinessDataType dataType);

    // ==================== 批量操作 ====================

    /**
     * 批量获取缓存
     *
     * @param module    缓存模块
     * @param namespace 命名空间
     * @param keys      缓存键集合
     * @param clazz     值类型
     * @param <T>       泛型类型
     * @return 键值对映射
     */
    <T> Map<String, T> mGet(CacheModule module, String namespace, Set<String> keys, Class<T> clazz);

    /**
     * 批量设置缓存
     *
     * @param module   缓存模块
     * @param namespace 命名空间
     * @param keyValues 键值对映射
     * @param dataType  业务数据类型
     * @param <T>       泛型类型
     */
    <T> void mSet(CacheModule module, String namespace, Map<String, T> keyValues, BusinessDataType dataType);

    /**
     * 批量删除缓存
     *
     * @param module    缓存模块
     * @param namespace 命名空间
     * @param keys      缓存键集合
     * @return 删除成功的键数量
     */
    int mDelete(CacheModule module, String namespace, Set<String> keys);

    // ==================== 异步操作 ====================

    /**
     * 异步获取缓存
     *
     * @param module    缓存模块
     * @param namespace 命名空间
     * @param key       缓存键
     * @param clazz     值类型
     * @param <T>       泛型类型
     * @return CompletableFuture
     */
    <T> CompletableFuture<T> getAsync(CacheModule module, String namespace, String key, Class<T> clazz);

    /**
     * 异步设置缓存
     *
     * @param module    缓存模块
     * @param namespace 命名空间
     * @param key       缓存键
     * @param value     缓存值
     * @param dataType  业务数据类型
     * @param <T>       泛型类型
     * @return CompletableFuture
     */
    <T> CompletableFuture<Void> setAsync(CacheModule module, String namespace, String key,
                                        T value, BusinessDataType dataType);

    // ==================== 模块级操作 ====================

    /**
     * 清除模块缓存
     *
     * @param module 缓存模块
     */
    void clearModule(CacheModule module);

    /**
     * 清除命名空间缓存
     *
     * @param module    缓存模块
     * @param namespace 命名空间
     */
    void clearNamespace(CacheModule module, String namespace);

    /**
     * 获取模块下的所有缓存键
     *
     * @param module 缓存模块
     * @return 缓存键列表
     */
    List<String> getModuleKeys(CacheModule module);

    // ==================== 缓存统计 ====================

    /**
     * 获取缓存统计信息
     *
     * @param module 缓存模块
     * @return 统计信息
     */
    CacheStatistics getStatistics(CacheModule module);

    /**
     * 检查缓存是否存在
     *
     * @param module    缓存模块
     * @param namespace 命名空间
     * @param key       缓存键
     * @return 是否存在
     */
    boolean exists(CacheModule module, String namespace, String key);

    /**
     * 设置缓存过期时间
     *
     * @param module    缓存模块
     * @param namespace 命名空间
     * @param key       缓存键
     * @param dataType  业务数据类型
     * @return 是否设置成功
     */
    boolean expire(CacheModule module, String namespace, String key, BusinessDataType dataType);

    /**
     * 获取缓存剩余过期时间
     *
     * @param module    缓存模块
     * @param namespace 命名空间
     * @param key       缓存键
     * @return 剩余时间（秒），-1表示永不过期，-2表示不存在
     */
    long getTtl(CacheModule module, String namespace, String key);
}
EOF

    log_success "创建UnifiedCacheService接口: $file_path"
}

# 创建缓存统计类
create_cache_statistics() {
    local file_path="smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/cache/core/CacheStatistics.java"

    cat > "$file_path" << 'EOF'
package net.lab1024.sa.base.common.cache.core;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 缓存统计信息
 *
 * @author SmartAdmin
 * @date 2025-11-18
 * @version 1.0
 */
@Data
@Builder
public class CacheStatistics {

    /**
     * 总请求数
     */
    private long totalRequests;

    /**
     * 命中数
     */
    private long hitCount;

    /**
     * 未命中数
     */
    private long missCount;

    /**
     * 命中率（百分比）
     */
    private double hitRate;

    /**
     * 平均响应时间（毫秒）
     */
    private double avgResponseTime;

    /**
     * 错误数
     */
    private long errorCount;

    /**
     * 错误率（百分比）
     */
    private double errorRate;

    /**
     * 统计开始时间
     */
    private LocalDateTime startTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 缓存项数量
     */
    private long itemCount;

    /**
     * 内存使用量（字节）
     */
    private long memoryUsage;

    /**
     * 计算命中率
     */
    public void calculateHitRate() {
        if (totalRequests > 0) {
            this.hitRate = (double) hitCount / totalRequests * 100;
        } else {
            this.hitRate = 0.0;
        }
    }

    /**
     * 计算错误率
     */
    public void calculateErrorRate() {
        if (totalRequests > 0) {
            this.errorRate = (double) errorCount / totalRequests * 100;
        } else {
            this.errorRate = 0.0;
        }
    }

    /**
     * 更新统计信息
     */
    public void updateStatistics(boolean hit, long responseTime) {
        this.totalRequests++;
        this.lastUpdateTime = LocalDateTime.now();

        if (hit) {
            this.hitCount++;
        } else {
            this.missCount++;
        }

        // 更新平均响应时间
        this.avgResponseTime = (this.avgResponseTime * (totalRequests - 1) + responseTime) / totalRequests;

        // 重新计算命中率
        calculateHitRate();
    }

    /**
     * 记录错误
     */
    public void recordError() {
        this.errorCount++;
        this.lastUpdateTime = LocalDateTime.now();
        calculateErrorRate();
    }

    /**
     * 重置统计信息
     */
    public void reset() {
        this.totalRequests = 0;
        this.hitCount = 0;
        this.missCount = 0;
        this.hitRate = 0.0;
        this.avgResponseTime = 0.0;
        this.errorCount = 0;
        this.errorRate = 0.0;
        this.startTime = LocalDateTime.now();
        this.lastUpdateTime = LocalDateTime.now();
    }
}
EOF

    log_success "创建CacheStatistics类: $file_path"
}

# 分析现有缓存代码
analyze_existing_cache_code() {
    log_info "=== 分析现有缓存代码使用情况 ==="

    BASE_DIR="smart-admin-api-java17-springboot3"

    # 查找直接使用Redis的文件
    redis_files=$(find "$BASE_DIR" -name "*.java" -exec grep -l "RedisTemplate\|StringRedisTemplate" {} \; 2>/dev/null || true)

    if [ -n "$redis_files" ]; then
        log_warning "发现直接使用Redis的文件:"
        for file in $redis_files; do
            echo "  - $file"
        done
        echo
    fi

    # 查找直接使用缓存工具的文件
    cache_util_files=$(find "$BASE_DIR" -name "*.java" -exec grep -l "RedisUtil\|CacheService" {} \; 2>/dev/null || true)

    if [ -n "$cache_util_files" ]; then
        log_warning "发现直接使用缓存工具的文件:"
        for file in $cache_util_files; do
            echo "  - $file"
        done
        echo
    fi

    # 统计需要迁移的文件
    total_files=$(echo "$redis_files $cache_util_files" | tr ' ' '\n' | sort -u | wc -l)
    log_info "总计需要迁移的文件数量: $total_files"

    return $total_files
}

# 生成迁移指南
generate_migration_guide() {
    local file_count=$1
    local report_file="repowiki_cache_migration_guide_$(date +%Y%m%d_%H%M%S).md"

    cat > "$report_file" << EOF
# repowiki缓存架构迁移指南

**迁移时间**: $(date)
**脚本版本**: v1.0
**需要迁移的文件数量**: $file_count

## 迁移步骤

### 第一步：了解新架构

基于repowiki缓存架构规范，新的缓存系统包含：

1. **核心组件**:
   - \`BusinessDataType\`: 业务数据类型驱动TTL
   - \`CacheModule\`: 模块化缓存治理
   - \`UnifiedCacheService\`: 统一缓存服务接口
   - \`CacheStatistics\`: 缓存统计监控

2. **TTL策略**:
   - REALTIME: 5分钟（账户余额、权限信息）
   - NEAR_REALTIME: 15分钟（设备状态、消费记录）
   - NORMAL: 30分钟（用户信息、基础配置）
   - STABLE: 60分钟（系统配置、权限模板）
   - LONG_TERM: 120分钟（字典数据、静态配置）

### 第二步：迁移现有代码

#### 旧代码模式：
\`\`\`java
@Resource
private RedisTemplate<String, Object> redisTemplate;

public UserVO getUser(Long userId) {
    String key = "user:" + userId;
    UserVO user = (UserVO) redisTemplate.opsForValue().get(key);
    if (user == null) {
        user = loadFromDatabase(userId);
        redisTemplate.opsForValue().set(key, user, 30, TimeUnit.MINUTES);
    }
    return user;
}
\`\`\`

#### 新代码模式：
\`\`\`java
@Resource
private UnifiedCacheService unifiedCacheService;

public UserVO getUser(Long userId) {
    return unifiedCacheService.getOrSet(
        CacheModule.CONSUME,
        "user",
        userId.toString(),
        () -> loadFromDatabase(userId),
        UserVO.class,
        BusinessDataType.USER_INFO
    );
}
\`\`\`

### 第三步：批量替换

使用以下命令进行批量替换：

\`\`\`bash
# 1. 备份文件
find . -name "*.java" -exec grep -l "RedisTemplate\|StringRedisTemplate" {} \; | xargs -I {} cp {} {}.backup

# 2. 替换依赖注入
find . -name "*.java" -exec sed -i 's/@Resource.*RedisTemplate/@Resource\n    private UnifiedCacheService unifiedCacheService;/g' {} \;
find . -name "*.java" -exec sed -i 's/@Resource.*StringRedisTemplate/@Resource\n    private UnifiedCacheService unifiedCacheService;/g' {} \;

# 3. 移除旧的import语句
find . -name "*.java" -exec sed -i '/import.*RedisTemplate/d' {} \;
find . -name "*.java" -exec sed -i '/import.*StringRedisTemplate/d' {} \;

# 4. 添加新的import语句
find . -name "*.java" -exec sed -i '/package net.lab1024.sa.admin/a\\nimport net.lab1024.sa.base.common.cache.service.UnifiedCacheService;\nimport net.lab1024.sa.base.common.cache.enum.CacheModule;\nimport net.lab1024.sa.base.common.cache.enum.BusinessDataType;' {} \;
\`\`\`

### 第四步：更新缓存逻辑

根据业务特性选择合适的BusinessDataType：

- **账户余额**: BusinessDataType.ACCOUNT_BALANCE
- **用户信息**: BusinessDataType.USER_INFO
- **消费记录**: BusinessDataType.CONSUME_RECORD
- **设备状态**: BusinessDataType.DEVICE_STATUS
- **系统配置**: BusinessDataType.SYSTEM_CONFIG
- **字典数据**: BusinessDataType.DICTIONARY_DATA
- **权限信息**: BusinessDataType.PERMISSION_INFO

### 第五步：验证迁移

1. 编译检查：
   \`\`\`bash
   mvn clean compile
   \`\`\`

2. 单元测试：
   \`\`\`bash
   mvn test
   \`\`\`

3. 功能测试：
   - 验证缓存功能正常
   - 验证TTL策略生效
   - 验证缓存命中率

## 常见问题

### Q: 如何设置不同的TTL？
A: 通过选择不同的BusinessDataType来设置TTL，系统会自动根据业务数据类型设置合适的过期时间。

### Q: 如何处理缓存穿透？
A: 使用getOrSet方法，系统会自动处理缓存穿透问题。

### Q: 如何批量操作缓存？
A: 使用mGet、mSet、mDelete方法进行批量操作。

### Q: 如何监控缓存性能？
A: 通过CacheStatistics类获取缓存统计信息，包括命中率、响应时间等。

## 最佳实践

1. **选择合适的BusinessDataType**: 根据业务特性选择合适的数据类型
2. **使用getOrSet模式**: 防止缓存穿透
3. **合理使用命名空间**: 按功能模块划分缓存
4. **监控缓存性能**: 定期检查缓存命中率和响应时间
5. **及时清理无效缓存**: 使用双删策略保证一致性

---
**指南生成时间**: $(date)
**基于**: repowiki缓存架构规范 v1.0
EOF

    log_success "迁移指南已生成: $report_file"
}

# 主函数
main() {
    echo "========================================"
    echo "  repowiki缓存架构修复脚本 - 第三阶段"
    echo "  版本: v1.0"
    echo "  目标: 缓存架构统一化"
    echo "========================================"
    echo

    # 执行修复步骤
    check_directory
    create_cache_core_components
    create_business_data_type
    create_supporting_enums
    create_unified_cache_service
    create_cache_statistics

    echo
    analyze_existing_cache_code
    file_count=$?

    echo
    generate_migration_guide $file_count

    echo
    echo "========================================"
    echo "  缓存架构核心组件创建完成！"
    echo "  下一步: 按照迁移指南迁移现有代码"
    echo "  运行: script_04_validate_and_deploy.sh"
    echo "========================================"
}

# 执行主函数
main "$@"