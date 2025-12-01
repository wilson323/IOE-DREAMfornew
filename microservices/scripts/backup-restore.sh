#!/bin/bash

# ===================================================================
# IOE-DREAM 监控系统数据备份与恢复脚本
# 版本: v2.0.0
# 更新时间: 2025-11-29
# ===================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
MONITORING_DIR="$PROJECT_ROOT/monitoring"
BACKUP_DIR="$MONITORING_DIR/backups"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

# 备份配置
RETENTION_DAYS=30
PROMETHEUS_DATA_DIR="$MONITORING_DIR/data/prometheus"
GRAFANA_DATA_DIR="$MONITORING_DIR/data/grafana"
ELASTICSEARCH_DATA_DIR="$MONITORING_DIR/data/elasticsearch"

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

# 创建备份目录
create_backup_dirs() {
    mkdir -p "$BACKUP_DIR/prometheus"
    mkdir -p "$BACKUP_DIR/grafana"
    mkdir -p "$BACKUP_DIR/elasticsearch"
    mkdir -p "$BACKUP_DIR/configs"
    mkdir -p "$BACKUP_DIR/logs"
}

# 备份Prometheus数据
backup_prometheus() {
    log_info "备份Prometheus数据..."

    local backup_file="$BACKUP_DIR/prometheus/prometheus-data-$TIMESTAMP.tar.gz"

    if [ -d "$PROMETHEUS_DATA_DIR" ]; then
        cd "$PROMETHEUS_DATA_DIR"
        tar -czf "$backup_file" .

        # 验证备份文件
        if [ -f "$backup_file" ] && [ -s "$backup_file" ]; then
            local file_size=$(du -h "$backup_file" | cut -f1)
            log_success "Prometheus数据备份完成: $backup_file ($file_size)"
        else
            log_error "Prometheus数据备份失败"
            return 1
        fi
    else
        log_warning "Prometheus数据目录不存在，跳过备份"
    fi
}

# 备份Grafana数据
backup_grafana() {
    log_info "备份Grafana数据..."

    local backup_file="$BACKUP_DIR/grafana/grafana-data-$TIMESTAMP.tar.gz"

    if [ -d "$GRAFANA_DATA_DIR" ]; then
        cd "$GRAFANA_DATA_DIR"
        tar -czf "$backup_file" .

        # 验证备份文件
        if [ -f "$backup_file" ] && [ -s "$backup_file" ]; then
            local file_size=$(du -h "$backup_file" | cut -f1)
            log_success "Grafana数据备份完成: $backup_file ($file_size)"
        else
            log_error "Grafana数据备份失败"
            return 1
        fi
    else
        log_warning "Grafana数据目录不存在，跳过备份"
    fi
}

# 备份Elasticsearch数据
backup_elasticsearch() {
    log_info "备份Elasticsearch数据..."

    local backup_file="$BACKUP_DIR/elasticsearch/elasticsearch-data-$TIMESTAMP.tar.gz"

    if [ -d "$ELASTICSEARCH_DATA_DIR" ]; then
        cd "$ELASTICSEARCH_DATA_DIR"
        tar -czf "$backup_file" .

        # 验证备份文件
        if [ -f "$backup_file" ] && [ -s "$backup_file" ]; then
            local file_size=$(du -h "$backup_file" | cut -f1)
            log_success "Elasticsearch数据备份完成: $backup_file ($file_size)"
        else
            log_error "Elasticsearch数据备份失败"
            return 1
        fi
    else
        log_warning "Elasticsearch数据目录不存在，跳过备份"
    fi
}

# 备份配置文件
backup_configs() {
    log_info "备份配置文件..."

    local backup_file="$BACKUP_DIR/configs/configs-$TIMESTAMP.tar.gz"

    cd "$MONITORING_DIR"
    tar -czf "$backup_file" \
        prometheus/prometheus.yml \
        prometheus/rules/ \
        alertmanager/alertmanager.yml \
        grafana/provisioning/ \
        elasticsearch/elasticsearch.yml \
        logstash/config/ \
        kibana/kibana.yml \
        filebeat/filebeat.yml \
        docker-compose.yml

    # 验证备份文件
    if [ -f "$backup_file" ] && [ -s "$backup_file" ]; then
        local file_size=$(du -h "$backup_file" | cut -f1)
        log_success "配置文件备份完成: $backup_file ($file_size)"
    else
        log_error "配置文件备份失败"
        return 1
    fi
}

# 创建备份索引
create_backup_index() {
    log_info "创建备份索引..."

    local index_file="$BACKUP_DIR/backup-index-$TIMESTAMP.json"

    cat > "$index_file" << EOF
{
    "backup_time": "$TIMESTAMP",
    "backup_date": "$(date '+%Y-%m-%d %H:%M:%S')",
    "backup_host": "$(hostname)",
    "backup_type": "full",
    "components": {
        "prometheus": {
            "backed_up": $([ -f "$BACKUP_DIR/prometheus/prometheus-data-$TIMESTAMP.tar.gz" ] && echo true || echo false),
            "file": "prometheus-data-$TIMESTAMP.tar.gz"
        },
        "grafana": {
            "backed_up": $([ -f "$BACKUP_DIR/grafana/grafana-data-$TIMESTAMP.tar.gz" ] && echo true || echo false),
            "file": "grafana-data-$TIMESTAMP.tar.gz"
        },
        "elasticsearch": {
            "backed_up": $([ -f "$BACKUP_DIR/elasticsearch/elasticsearch-data-$TIMESTAMP.tar.gz" ] && echo true || echo false),
            "file": "elasticsearch-data-$TIMESTAMP.tar.gz"
        },
        "configs": {
            "backed_up": $([ -f "$BACKUP_DIR/configs/configs-$TIMESTAMP.tar.gz" ] && echo true || echo false),
            "file": "configs-$TIMESTAMP.tar.gz"
        }
    }
}
EOF

    log_success "备份索引创建完成: $index_file"
}

# 清理过期备份
cleanup_old_backups() {
    log_info "清理 $RETENTION_DAYS 天前的备份文件..."

    local deleted_count=0

    # 清理Prometheus备份
    find "$BACKUP_DIR/prometheus" -name "prometheus-data-*.tar.gz" -mtime +$RETENTION_DAYS -delete 2>/dev/null
    deleted_count=$((deleted_count + $(find "$BACKUP_DIR/prometheus" -name "prometheus-data-*.tar.gz" -mtime +$RETENTION_DAYS | wc -l)))

    # 清理Grafana备份
    find "$BACKUP_DIR/grafana" -name "grafana-data-*.tar.gz" -mtime +$RETENTION_DAYS -delete 2>/dev/null
    deleted_count=$((deleted_count + $(find "$BACKUP_DIR/grafana" -name "grafana-data-*.tar.gz" -mtime +$RETENTION_DAYS | wc -l)))

    # 清理Elasticsearch备份
    find "$BACKUP_DIR/elasticsearch" -name "elasticsearch-data-*.tar.gz" -mtime +$RETENTION_DAYS -delete 2>/dev/null
    deleted_count=$((deleted_count + $(find "$BACKUP_DIR/elasticsearch" -name "elasticsearch-data-*.tar.gz" -mtime +$RETENTION_DAYS | wc -l)))

    # 清理配置备份
    find "$BACKUP_DIR/configs" -name "configs-*.tar.gz" -mtime +$RETENTION_DAYS -delete 2>/dev/null
    deleted_count=$((deleted_count + $(find "$BACKUP_DIR/configs" -name "configs-*.tar.gz" -mtime +$RETENTION_DAYS | wc -l)))

    # 清理索引文件
    find "$BACKUP_DIR" -name "backup-index-*.json" -mtime +$RETENTION_DAYS -delete 2>/dev/null

    if [ $deleted_count -gt 0 ]; then
        log_success "已清理 $deleted_count 个过期备份文件"
    else
        log_info "没有需要清理的过期备份文件"
    fi
}

# 执行完整备份
perform_full_backup() {
    log_info "开始执行完整备份..."

    create_backup_dirs
    backup_prometheus
    backup_grafana
    backup_elasticsearch
    backup_configs
    create_backup_index
    cleanup_old_backups

    log_success "完整备份执行完成"
}

# 恢复Prometheus数据
restore_prometheus() {
    local backup_file=$1

    log_info "恢复Prometheus数据..."

    if [ ! -f "$backup_file" ]; then
        log_error "备份文件不存在: $backup_file"
        return 1
    fi

    # 停止Prometheus服务
    log_info "停止Prometheus服务..."
    cd "$MONITORING_DIR"
    docker-compose stop prometheus

    # 清理现有数据
    if [ -d "$PROMETHEUS_DATA_DIR" ]; then
        rm -rf "$PROMETHEUS_DATA_DIR"/*
    fi

    # 恢复数据
    log_info "恢复数据文件..."
    tar -xzf "$backup_file" -C "$PROMETHEUS_DATA_DIR"

    # 重启Prometheus服务
    log_info "重启Prometheus服务..."
    docker-compose start prometheus

    log_success "Prometheus数据恢复完成"
}

# 恢复Grafana数据
restore_grafana() {
    local backup_file=$1

    log_info "恢复Grafana数据..."

    if [ ! -f "$backup_file" ]; then
        log_error "备份文件不存在: $backup_file"
        return 1
    fi

    # 停止Grafana服务
    log_info "停止Grafana服务..."
    cd "$MONITORING_DIR"
    docker-compose stop grafana

    # 清理现有数据
    if [ -d "$GRAFANA_DATA_DIR" ]; then
        rm -rf "$GRAFANA_DATA_DIR"/*
    fi

    # 恢复数据
    log_info "恢复数据文件..."
    tar -xzf "$backup_file" -C "$GRAFANA_DATA_DIR"

    # 重启Grafana服务
    log_info "重启Grafana服务..."
    docker-compose start grafana

    log_success "Grafana数据恢复完成"
}

# 恢复Elasticsearch数据
restore_elasticsearch() {
    local backup_file=$1

    log_info "恢复Elasticsearch数据..."

    if [ ! -f "$backup_file" ]; then
        log_error "备份文件不存在: $backup_file"
        return 1
    fi

    # 停止Elasticsearch服务
    log_info "停止Elasticsearch服务..."
    cd "$MONITORING_DIR"
    docker-compose stop elasticsearch

    # 清理现有数据
    if [ -d "$ELASTICSEARCH_DATA_DIR" ]; then
        rm -rf "$ELASTICSEARCH_DATA_DIR"/*
    fi

    # 恢复数据
    log_info "恢复数据文件..."
    tar -xzf "$backup_file" -C "$ELASTICSEARCH_DATA_DIR"

    # 重启Elasticsearch服务
    log_info "重启Elasticsearch服务..."
    docker-compose start elasticsearch

    log_success "Elasticsearch数据恢复完成"
}

# 列出可用备份
list_backups() {
    log_info "列出可用备份文件..."

    echo ""
    echo "Prometheus备份:"
    ls -lh "$BACKUP_DIR/prometheus/"*.tar.gz 2>/dev/null || echo "  无备份文件"

    echo ""
    echo "Grafana备份:"
    ls -lh "$BACKUP_DIR/grafana/"*.tar.gz 2>/dev/null || echo "  无备份文件"

    echo ""
    echo "Elasticsearch备份:"
    ls -lh "$BACKUP_DIR/elasticsearch/"*.tar.gz 2>/dev/null || echo "  无备份文件"

    echo ""
    echo "配置备份:"
    ls -lh "$BACKUP_DIR/configs/"*.tar.gz 2>/dev/null || echo "  无备份文件"
}

# 显示帮助信息
show_help() {
    echo "IOE-DREAM 监控系统数据备份与恢复脚本"
    echo ""
    echo "用法: $0 [选项] [参数]"
    echo ""
    echo "备份选项:"
    echo "  -b, --backup              执行完整备份"
    echo "  -p, --prometheus          只备份Prometheus数据"
    echo "  -g, --grafana             只备份Grafana数据"
    echo "  -e, --elasticsearch       只备份Elasticsearch数据"
    echo "  -c, --configs             只备份配置文件"
    echo ""
    echo "恢复选项:"
    echo "  -rp, --restore-prometheus <backup_file>  恢复Prometheus数据"
    echo "  -rg, --restore-grafana <backup_file>     恢复Grafana数据"
    echo "  -re, --restore-elasticsearch <backup_file> 恢复Elasticsearch数据"
    echo ""
    echo "管理选项:"
    echo "  -l, --list                列出可用备份"
    echo "  --cleanup                 清理过期备份"
    echo "  -h, --help                显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 -b                     执行完整备份"
    echo "  $0 -rp backup.tar.gz      恢复Prometheus数据"
    echo "  $0 -l                     列出所有备份"
}

# 主函数
main() {
    case "${1:-}" in
        -b|--backup)
            perform_full_backup
            ;;
        -p|--prometheus)
            create_backup_dirs
            backup_prometheus
            ;;
        -g|--grafana)
            create_backup_dirs
            backup_grafana
            ;;
        -e|--elasticsearch)
            create_backup_dirs
            backup_elasticsearch
            ;;
        -c|--configs)
            create_backup_dirs
            backup_configs
            ;;
        -rp|--restore-prometheus)
            restore_prometheus "$2"
            ;;
        -rg|--restore-grafana)
            restore_grafana "$2"
            ;;
        -re|--restore-elasticsearch)
            restore_elasticsearch "$2"
            ;;
        -l|--list)
            list_backups
            ;;
        --cleanup)
            cleanup_old_backups
            ;;
        -h|--help|"")
            show_help
            ;;
        *)
            log_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"