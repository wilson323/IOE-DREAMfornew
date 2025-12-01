#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
考勤模块性能监控脚本
实时监控系统性能指标，包括数据库查询性能、缓存命中率、API响应时间等
"""

import json
import redis
import logging
import time
import psutil
import pymysql
from datetime import datetime, timedelta
from typing import Dict, List, Any
import threading
import subprocess
import requests

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('attendance-performance-monitoring.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

class AttendancePerformanceMonitor:
    """考勤模块性能监控器"""

    def __init__(self, db_config: Dict[str, Any], redis_config: Dict[str, Any]):
        """
        初始化性能监控器

        Args:
            db_config: 数据库配置
            redis_config: Redis配置
        """
        self.db_config = db_config
        self.redis_config = redis_config
        self.monitoring_data = {
            'timestamp': [],
            'cpu_percent': [],
            'memory_percent': [],
            'db_query_time': [],
            'redis_hit_rate': [],
            'api_response_time': [],
            'active_connections': []
        }

        # 连接数据库
        try:
            self.db_connection = pymysql.connect(
                host=db_config['host'],
                port=db_config['port'],
                user=db_config['user'],
                password=db_config['password'],
                database=db_config['database'],
                charset='utf8mb4'
            )
            logger.info("数据库连接成功")
        except Exception as e:
            logger.error(f"数据库连接失败: {e}")
            self.db_connection = None

        # 连接Redis
        try:
            self.redis_client = redis.Redis(
                host=redis_config['host'],
                port=redis_config['port'],
                db=redis_config['db'],
                password=redis_config['password'],
                decode_responses=True
            )
            self.redis_client.ping()
            logger.info("Redis连接成功")
        except Exception as e:
            logger.error(f"Redis连接失败: {e}")
            self.redis_client = None

        # API监控配置
        self.api_endpoints = [
            'http://localhost:1024/api/attendance/today-punch',
            'http://localhost:1024/api/attendance/records',
            'http://localhost:1024/api/attendance/statistics',
            'http://localhost:1024/api/attendance/schedule'
        ]

    def monitor_system_resources(self) -> Dict[str, float]:
        """监控系统资源使用情况"""
        try:
            cpu_percent = psutil.cpu_percent(interval=1)
            memory = psutil.virtual_memory()
            memory_percent = memory.percent
            disk = psutil.disk_usage('/')
            disk_percent = (disk.used / disk.total) * 100

            return {
                'cpu_percent': cpu_percent,
                'memory_percent': memory_percent,
                'disk_percent': disk_percent
            }
        except Exception as e:
            logger.error(f"系统资源监控失败: {e}")
            return {
                'cpu_percent': 0,
                'memory_percent': 0,
                'disk_percent': 0
            }

    def monitor_database_performance(self) -> Dict[str, Any]:
        """监控数据库性能"""
        if not self.db_connection:
            return {'query_time': 0, 'active_connections': 0}

        try:
            cursor = self.db_connection.cursor()

            # 测试查询性能
            start_time = time.time()
            cursor.execute("SELECT 1")
            cursor.fetchone()
            query_time = (time.time() - start_time) * 1000  # 转换为毫秒

            # 获取活跃连接数
            cursor.execute("SHOW STATUS LIKE 'Threads_connected'")
            result = cursor.fetchone()
            active_connections = int(result[1]) if result else 0

            cursor.close()

            return {
                'query_time': query_time,
                'active_connections': active_connections
            }
        except Exception as e:
            logger.error(f"数据库性能监控失败: {e}")
            return {
                'query_time': 0,
                'active_connections': 0
            }

    def monitor_redis_performance(self) -> Dict[str, Any]:
        """监控Redis性能"""
        if not self.redis_client:
            return {'hit_rate': 0, 'memory_used': 0}

        try:
            info = self.redis_client.info()

            # 计算命中率
            hits = info.get('keyspace_hits', 0)
            misses = info.get('keyspace_misses', 0)
            total = hits + misses
            hit_rate = (hits / total * 100) if total > 0 else 0

            # 内存使用情况
            memory_used = info.get('used_memory_human', '0M')

            return {
                'hit_rate': hit_rate,
                'memory_used': memory_used
            }
        except Exception as e:
            logger.error(f"Redis性能监控失败: {e}")
            return {
                'hit_rate': 0,
                'memory_used': 0
            }

    def monitor_api_performance(self) -> Dict[str, float]:
        """监控API性能"""
        response_times = []

        for endpoint in self.api_endpoints:
            try:
                start_time = time.time()
                response = requests.get(endpoint, timeout=5)
                response_time = (time.time() - start_time) * 1000  # 转换为毫秒
                response_times.append(response_time)
            except Exception as e:
                logger.warning(f"API监控失败 {endpoint}: {e}")
                response_times.append(0)

        avg_response_time = sum(response_times) / len(response_times) if response_times else 0

        return {
            'avg_response_time': avg_response_time,
            'max_response_time': max(response_times) if response_times else 0
        }

    def collect_monitoring_data(self) -> Dict[str, Any]:
        """收集所有监控数据"""
        timestamp = datetime.now().isoformat()

        system_metrics = self.monitor_system_resources()
        db_metrics = self.monitor_database_performance()
        redis_metrics = self.monitor_redis_performance()
        api_metrics = self.monitor_api_performance()

        return {
            'timestamp': timestamp,
            'system': system_metrics,
            'database': db_metrics,
            'redis': redis_metrics,
            'api': api_metrics
        }

    def save_monitoring_data(self, data: Dict[str, Any]):
        """保存监控数据到文件"""
        try:
            filename = f"attendance-monitoring-data-{datetime.now().strftime('%Y%m%d')}.json"
            with open(filename, 'a', encoding='utf-8') as f:
                f.write(json.dumps(data, ensure_ascii=False) + '\n')
            logger.info(f"监控数据已保存到 {filename}")
        except Exception as e:
            logger.error(f"保存监控数据失败: {e}")

    def generate_performance_report(self, data_list: List[Dict[str, Any]]) -> str:
        """生成性能报告"""
        if not data_list:
            return "无监控数据"

        # 计算平均值
        avg_cpu = sum([d['system']['cpu_percent'] for d in data_list]) / len(data_list)
        avg_memory = sum([d['system']['memory_percent'] for d in data_list]) / len(data_list)
        avg_db_time = sum([d['database']['query_time'] for d in data_list]) / len(data_list)
        avg_redis_hit = sum([d['redis']['hit_rate'] for d in data_list]) / len(data_list)
        avg_api_time = sum([d['api']['avg_response_time'] for d in data_list]) / len(data_list)

        report = f"""
# 考勤模块性能监控报告

**报告时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
**监控时长**: {len(data_list)} 个数据点

## 系统资源使用情况
- **平均CPU使用率**: {avg_cpu:.2f}%
- **平均内存使用率**: {avg_memory:.2f}%
- **数据库查询平均耗时**: {avg_db_time:.2f}ms
- **Redis缓存命中率**: {avg_redis_hit:.2f}%
- **API平均响应时间**: {avg_api_time:.2f}ms

## 性能建议
"""

        # 根据监控数据生成建议
        if avg_cpu > 80:
            report += "- ⚠️ CPU使用率较高，建议检查系统负载\n"
        if avg_memory > 80:
            report += "- ⚠️ 内存使用率较高，建议检查内存泄漏\n"
        if avg_db_time > 100:
            report += "- ⚠️ 数据库查询较慢，建议优化SQL语句\n"
        if avg_redis_hit < 80:
            report += "- ⚠️ Redis缓存命中率较低，建议优化缓存策略\n"
        if avg_api_time > 500:
            report += "- ⚠️ API响应时间较长，建议优化接口性能\n"

        if all([avg_cpu < 80, avg_memory < 80, avg_db_time < 100, avg_redis_hit > 80, avg_api_time < 500]):
            report += "- ✅ 系统性能良好，各项指标正常\n"

        report += f"""
---
**报告生成时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
**监控脚本版本**: v1.0.0
"""

        return report

    def start_monitoring(self, interval: int = 60, duration: int = 3600):
        """
        开始监控

        Args:
            interval: 监控间隔（秒）
            duration: 监控持续时间（秒）
        """
        logger.info(f"开始性能监控，间隔 {interval} 秒，持续 {duration} 秒")

        data_list = []
        start_time = time.time()

        try:
            while time.time() - start_time < duration:
                # 收集监控数据
                data = self.collect_monitoring_data()
                data_list.append(data)

                # 保存数据
                self.save_monitoring_data(data)

                # 打印当前状态
                logger.info(f"CPU: {data['system']['cpu_percent']:.1f}%, "
                           f"内存: {data['system']['memory_percent']:.1f}%, "
                           f"DB查询: {data['database']['query_time']:.2f}ms, "
                           f"Redis命中率: {data['redis']['hit_rate']:.1f}%, "
                           f"API响应: {data['api']['avg_response_time']:.2f}ms")

                # 等待下一个监控周期
                time.sleep(interval)

        except KeyboardInterrupt:
            logger.info("监控被用户中断")
        except Exception as e:
            logger.error(f"监控过程中发生错误: {e}")
        finally:
            # 生成性能报告
            if data_list:
                report = self.generate_performance_report(data_list)
                report_file = f"attendance-performance-report-{datetime.now().strftime('%Y%m%d_%H%M%S')}.md"
                with open(report_file, 'w', encoding='utf-8') as f:
                    f.write(report)
                logger.info(f"性能报告已生成: {report_file}")

    def check_performance_bottlenecks(self) -> Dict[str, Any]:
        """检查性能瓶颈"""
        bottlenecks = {
            'database': [],
            'redis': [],
            'system': [],
            'api': []
        }

        try:
            # 检查慢查询
            if self.db_connection:
                cursor = self.db_connection.cursor()
                cursor.execute("SHOW VARIABLES LIKE 'slow_query_log'")
                slow_log_enabled = cursor.fetchone()
                if slow_log_enabled and slow_log_enabled[1] == 'OFF':
                    bottlenecks['database'].append("慢查询日志未启用")

                cursor.execute("SHOW VARIABLES LIKE 'long_query_time'")
                long_query_time = cursor.fetchone()
                if long_query_time and float(long_query_time[1]) > 2:
                    bottlenecks['database'].append(f"慢查询阈值设置较高: {long_query_time[1]}秒")

                cursor.close()

            # 检查Redis配置
            if self.redis_client:
                info = self.redis_client.info()
                maxmemory = info.get('maxmemory', 0)
                if maxmemory == 0:
                    bottlenecks['redis'].append("Redis未设置最大内存限制")

            # 检查系统配置
            cpu_count = psutil.cpu_count()
            if cpu_count < 4:
                bottlenecks['system'].append(f"CPU核心数较少: {cpu_count}核")

            memory = psutil.virtual_memory()
            if memory.total < 4 * 1024 * 1024 * 1024:  # 4GB
                bottlenecks['system'].append(f"内存较小: {memory.total / (1024**3):.1f}GB")

        except Exception as e:
            logger.error(f"检查性能瓶颈时发生错误: {e}")

        return bottlenecks

def main():
    """主函数"""
    print("🚀 考勤模块性能监控工具")
    print("=" * 50)

    # 配置数据库和Redis连接信息
    db_config = {
        'host': 'localhost',
        'port': 33060,
        'user': 'root',
        'password': '',
        'database': 'smart_admin_v3'
    }

    redis_config = {
        'host': 'localhost',
        'port': 6389,
        'db': 1,
        'password': 'zkteco3100'
    }

    # 创建监控器实例
    monitor = AttendancePerformanceMonitor(db_config, redis_config)

    # 检查性能瓶颈
    print("\n🔍 检查性能瓶颈...")
    bottlenecks = monitor.check_performance_bottlenecks()

    has_bottlenecks = False
    for category, issues in bottlenecks.items():
        if issues:
            has_bottlenecks = True
            print(f"\n{category.upper()} 瓶颈:")
            for issue in issues:
                print(f"  ⚠️  {issue}")

    if not has_bottlenecks:
        print("✅ 未发现明显性能瓶颈")

    # 开始监控
    print("\n📊 开始性能监控...")
    print("按 Ctrl+C 停止监控")

    try:
        # 监控10分钟，每30秒收集一次数据
        monitor.start_monitoring(interval=30, duration=600)
    except KeyboardInterrupt:
        print("\n🛑 监控已停止")

    print("\n🎉 性能监控完成！")

if __name__ == "__main__":
    main()