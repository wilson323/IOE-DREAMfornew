#!/bin/bash
# =====================================================
# IOE-DREAM P0级优化启动脚本
# 包含G1GC参数优化和性能监控
# =====================================================

SERVICE_NAME=$1
MEMORY_PROFILE=$2  # small/medium/large/custom

# 颜色输出函数
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# =====================================================
# JVM参数配置 - 根据内存类型选择
# =====================================================

case $MEMORY_PROFILE in
    "large")
        # 大内存环境 (32GB内存服务器)
        print_info "使用大内存配置 (16GB堆内存)"
        JAVA_OPTS="-Xms16g -Xmx16g \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=150 \
        -XX:G1HeapRegionSize=32m \
        -XX:G1NewSizePercent=35 \
        -XX:G1MaxNewSizePercent=50 \
        -XX:G1MixedGCCountTarget=12 \
        -XX:G1MixedGCLiveThresholdPercent=80 \
        -XX:G1OldCSetRegionThresholdPercent=15 \
        -XX:G1ReservePercent=25 \
        -XX:SurvivorRatio=6 \
        -XX:MaxTenuringThreshold=20 \
        -XX:InitiatingHeapOccupancyPercent=40 \
        -XX:+G1UseAdaptiveIHOP \
        -XX:+ParallelRefProcEnabled \
        -XX:+UseStringDeduplication \
        -XX:StringDeduplicationAgeThreshold=5"
        ;;

    "medium")
        # 中等内存环境 (16GB内存服务器) - 生产环境推荐
        print_info "使用中等内存配置 (8GB堆内存)"
        JAVA_OPTS="-Xms8g -Xmx8g \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=200 \
        -XX:G1HeapRegionSize=16m \
        -XX:G1NewSizePercent=30 \
        -XX:G1MaxNewSizePercent=40 \
        -XX:G1MixedGCCountTarget=8 \
        -XX:G1MixedGCLiveThresholdPercent=85 \
        -XX:G1OldCSetRegionThresholdPercent=10 \
        -XX:G1ReservePercent=20 \
        -XX:SurvivorRatio=8 \
        -XX:MaxTenuringThreshold=15 \
        -XX:InitiatingHeapOccupancyPercent=45 \
        -XX:+G1UseAdaptiveIHOP \
        -XX:+ParallelRefProcEnabled \
        -XX:+UseStringDeduplication \
        -XX:StringDeduplicationAgeThreshold=3"
        ;;

    "small")
        # 小内存环境 (8GB内存服务器)
        print_info "使用小内存配置 (4GB堆内存)"
        JAVA_OPTS="-Xms4g -Xmx4g \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=100 \
        -XX:G1HeapRegionSize=8m \
        -XX:G1NewSizePercent=40 \
        -XX:G1MaxNewSizePercent=50 \
        -XX:InitiatingHeapOccupancyPercent=50 \
        -XX:+ParallelRefProcEnabled \
        -XX:+UseStringDeduplication \
        -XX:StringDeduplicationAgeThreshold=3"
        ;;

    "dev")
        # 开发环境
        print_info "使用开发环境配置 (2GB堆内存)"
        JAVA_OPTS="-Xms2g -Xmx4g \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=100 \
        -XX:G1HeapRegionSize=8m \
        -XX:G1NewSizePercent=40 \
        -XX:G1MaxNewSizePercent=50 \
        -XX:InitiatingHeapOccupancyPercent=50 \
        -XX:+ParallelRefProcEnabled"
        ;;

    *)
        print_warning "未指定内存类型，使用默认中等内存配置"
        MEMORY_PROFILE="medium"
        JAVA_OPTS="-Xms8g -Xmx8g \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=200 \
        -XX:G1HeapRegionSize=16m \
        -XX:InitiatingHeapOccupancyPercent=45 \
        -XX:+ParallelRefProcEnabled"
        ;;
esac

# =====================================================
# 通用JVM参数 - 所有配置都包含
# =====================================================

COMMON_OPTS="-XX:+PrintGC \
-XX:+PrintGCDetails \
-XX:+PrintGCTimeStamps \
-XX:+PrintGCApplicationStoppedTime \
-XX:+PrintGCDateStamps \
-XX:+UseGCLogFileRotation \
-XX:NumberOfGCLogFiles=5 \
-XX:GCLogFileSize=10M \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/var/log/ioedream/heapdump-${SERVICE_NAME}.hprof \
-Xloggc:/var/log/ioedream/gc-${SERVICE_NAME}.log \
-Dfile.encoding=UTF-8 \
-Duser.timezone=Asia/Shanghai \
-Djava.security.egd=file:/dev/./urandom \
-Dspring.profiles.active=prod,performance-optimized"

# 监控参数
MONITORING_OPTS="-XX:+UnlockDiagnosticVMOptions \
-XX:+PrintReferenceGC \
-XX:+PrintHeapAtGC \
-XX:+PrintGCApplicationConcurrentTime \
-XX:+PrintTenuringDistribution \
-XX:+LogVMOutput \
-XX:+PrintGCDetails"

# 最终JVM参数
FINAL_JAVA_OPTS="$JAVA_OPTS $COMMON_OPTS $MONITORING_OPTS"

# =====================================================
# 应用程序配置
# =====================================================

APP_JAR="/opt/ioedream/${SERVICE_NAME}.jar"
PID_FILE="/var/run/ioedream/${SERVICE_NAME}.pid"
LOG_DIR="/var/log/ioedream"

# 创建必要的目录
mkdir -p $LOG_DIR
mkdir -p $(dirname $PID_FILE)
mkdir -p $(dirname $(echo $FINAL_JAVA_OPTS | grep -o 'HeapDumpPath=[^ ]*' | cut -d= -f2))

# =====================================================
# 启动函数
# =====================================================

start_service() {
    print_info "正在启动服务: $SERVICE_NAME"
    print_info "内存配置: $MEMORY_PROFILE"
    print_info "JVM参数: $(echo $FINAL_JAVA_OPTS | wc -w) 个参数"

    if [ ! -f "$APP_JAR" ]; then
        print_error "JAR文件不存在: $APP_JAR"
        exit 1
    fi

    # 检查是否已经运行
    if [ -f "$PID_FILE" ] && kill -0 $(cat "$PID_FILE") 2>/dev/null; then
        print_warning "服务已经在运行中 (PID: $(cat $PID_FILE))"
        return 1
    fi

    # 启动服务
    print_info "执行命令: java $FINAL_JAVA_OPTS -jar $APP_JAR"

    # 后台启动
    nohup java $FINAL_JAVA_OPTS -jar $APP_JAR \
        > $LOG_DIR/${SERVICE_NAME}-startup.log 2>&1 &

    # 保存PID
    echo $! > $PID_FILE

    # 等待启动
    sleep 5

    if [ -f "$PID_FILE" ] && kill -0 $(cat "$PID_FILE") 2>/dev/null; then
        print_success "服务启动成功! PID: $(cat $PID_FILE)"
        print_info "日志文件: $LOG_DIR/${SERVICE_NAME}-startup.log"
        print_info "GC日志: /var/log/ioedream/gc-${SERVICE_NAME}.log"
        print_info "HeapDump路径: /var/log/ioedream/heapdump-${SERVICE_NAME}.hprof"
        print_info "Druid监控: http://localhost:8080/druid/"
        print_info "健康检查: http://localhost:8080/actuator/health"
        print_info "性能指标: http://localhost:8080/actuator/metrics"
        print_info "Prometheus: http://localhost:8080/actuator/prometheus"
    else
        print_error "服务启动失败!"
        print_error "请查看日志: $LOG_DIR/${SERVICE_NAME}-startup.log"
        exit 1
    fi
}

# =====================================================
# 停止函数
# =====================================================

stop_service() {
    print_info "正在停止服务: $SERVICE_NAME"

    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        print_info "找到进程 PID: $PID"

        if kill -0 $PID 2>/dev/null; then
            # 优雅停止
            print_info "发送TERM信号..."
            kill -TERM $PID

            # 等待15秒
            for i in {1..15}; do
                if ! kill -0 $PID 2>/dev/null; then
                    break
                fi
                sleep 1
                echo -n "."
            done
            echo

            # 如果还没停止，强制停止
            if kill -0 $PID 2>/dev/null; then
                print_warning "强制停止进程..."
                kill -KILL $PID
                sleep 2
            fi

            rm -f $PID_FILE
            print_success "服务已停止"
        else
            print_warning "进程不存在，清理PID文件"
            rm -f $PID_FILE
        fi
    else
        print_warning "PID文件不存在，尝试通过进程名停止"
        pkill -f "$SERVICE_NAME"
    fi
}

# =====================================================
# 重启函数
# =====================================================

restart_service() {
    print_info "重启服务: $SERVICE_NAME"
    stop_service
    sleep 2
    start_service
}

# =====================================================
# 状态检查函数
# =====================================================

check_status() {
    print_info "检查服务状态: $SERVICE_NAME"

    if [ -f "$PID_FILE" ] && kill -0 $(cat "$PID_FILE") 2>/dev/null; then
        PID=$(cat $PID_FILE)
        print_success "服务运行中 (PID: $PID)"

        # 显示内存使用情况
        if command -v ps >/dev/null 2>&1; then
            MEMORY=$(ps -p $PID -o rss= | tr -d ' ')
            MEMORY_MB=$((MEMORY / 1024))
            print_info "内存使用: ${MEMORY_MB}MB"
        fi

        # 显示GC统计
        if [ -f "/var/log/ioedream/gc-${SERVICE_NAME}.log" ]; then
            GC_COUNT=$(tail -100 "/var/log/ioedream/gc-${SERVICE_NAME}.log" | grep -c "GC pause")
            print_info "最近GC次数: $GC_COUNT"
        fi

        # 健康检查
        if command -v curl >/dev/null 2>&1; then
            HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health || echo "000")
            if [ "$HTTP_STATUS" = "200" ]; then
                print_success "健康检查通过"
            else
                print_warning "健康检查失败 (HTTP: $HTTP_STATUS)"
            fi
        fi
    else
        print_error "服务未运行"
        return 1
    fi
}

# =====================================================
# 主程序
# =====================================================

case "$3" in
    "start")
        start_service
        ;;
    "stop")
        stop_service
        ;;
    "restart")
        restart_service
        ;;
    "status")
        check_status
        ;;
    "help"|"-h"|"--help")
        echo "IOE-DREAM 优化启动脚本"
        echo ""
        echo "用法: $0 <服务名> <内存配置> <操作>"
        echo ""
        echo "服务名: ioedream-gateway-service, ioedream-common-service, 等"
        echo "内存配置: small, medium, large, dev"
        echo "操作: start, stop, restart, status"
        echo ""
        echo "示例:"
        echo "  $0 ioedream-gateway-service medium start"
        echo "  $0 ioedream-common-service large restart"
        echo "  $0 ioedream-access-service small status"
        echo ""
        echo "内存配置说明:"
        echo "  small  - 4GB堆内存 (适用于轻量服务)"
        echo "  medium - 8GB堆内存 (生产环境推荐)"
        echo "  large  - 16GB堆内存 (高负载服务)"
        echo "  dev    - 2-4GB堆内存 (开发环境)"
        ;;
    *)
        echo "用法: $0 <服务名> <内存配置> <操作>"
        echo "使用 '$0 help' 查看详细帮助"
        exit 1
        ;;
esac