#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
考勤模块缓存优化实现
支持Redis缓存、多级缓存策略和智能缓存失效
"""

import json
import redis
import hashlib
import logging
from typing import Any, Optional, Dict, List, Union
from datetime import datetime, timedelta
import threading
import time

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class AttendanceCacheManager:
    """考勤模块缓存管理器"""

    def __init__(self, host='localhost', port=6379, db=1, password=None):
        """
        初始化缓存管理器

        Args:
            host: Redis主机地址
            port: Redis端口
            db: 数据库编号
            password: 密码
        """
        self.redis_client = redis.Redis(
            host=host,
            port=port,
            db=db,
            password=password,
            decode_responses=True
        )

        # 内存缓存（二级缓存）
        self.memory_cache = {}
        self.memory_cache_lock = threading.Lock()

        # 缓存配置
        self.cache_config = {
            'default_ttl': 3600,  # 默认1小时
            'short_ttl': 300,     # 短期缓存5分钟
            'long_ttl': 86400,     # 长期缓存24小时

            # 各种数据的TTL配置
            'attendance_record': 1800,      # 打卡记录缓存30分钟
            'employee_schedule': 3600,       # 员工排班缓存1小时
            'attendance_statistics': 7200,  # 考勤统计缓存2小时
            'department_stats': 3600,        # 部门统计缓存1小时
            'attendance_rules': 86400,       # 考勤规则缓存24小时
            'today_attendance': 300,         # 今日考勤缓存5分钟
            'calendar_data': 1800,            # 日历数据缓存30分钟
        }

        # 缓存键前缀
        self.key_prefix = 'attendance:'

        logger.info("考勤缓存管理器初始化完成")

    def _generate_cache_key(self, key: str, prefix: str = None) -> str:
        """生成缓存键"""
        if prefix:
            return f"{self.key_prefix}{prefix}:{key}"
        return f"{self.key_prefix}{key}"

    def _get_ttl(self, cache_type: str) -> int:
        """获取TTL"""
        return self.cache_config.get(cache_type, self.cache_config['default_ttl'])

    def set(self, key: str, value: Any, ttl: int = None, cache_type: str = None) -> bool:
        """
        设置缓存

        Args:
            key: 缓存键
            value: 缓存值
            ttl: 过期时间（秒）
            cache_type: 缓存类型

        Returns:
            bool: 是否设置成功
        """
        try:
            if ttl is None and cache_type:
                ttl = self._get_ttl(cache_type)
            elif ttl is None:
                ttl = self.cache_config['default_ttl']

            cache_key = self._generate_cache_key(key)

            # 序列化值
            if not isinstance(value, str):
                if isinstance(value, (dict, list)):
                    value = json.dumps(value, ensure_ascii=False)
                else:
                    value = str(value)

            # 设置Redis缓存
            redis_result = self.redis_client.setex(cache_key, ttl, value)

            # 设置内存缓存
            with self.memory_cache_lock:
                self.memory_cache[cache_key] = {
                    'value': value,
                    'expire_time': time.time() + ttl
                }

            return redis_result

        except Exception as e:
            logger.error(f"设置缓存失败: {e}")
            return False

    def get(self, key: str, cache_type: str = None) -> Optional[Any]:
        """
        获取缓存值

        Args:
            key: 缓存键
            cache_type: 缓存类型

        Returns:
            Any: 缓存值，如果不存在返回None
        """
        try:
            cache_key = self._generate_cache_key(key)

            # 先查内存缓存
            with self.memory_cache_lock:
                if cache_key in self.memory_cache:
                    cache_item = self.memory_cache[cache_key]
                    if cache_item['expire_time'] > time.time():
                        return self._deserialize_value(cache_item['value'])
                    else:
                        # 内存缓存过期，删除
                        del self.memory_cache[cache_key]

            # 查Redis缓存
            value = self.redis_client.get(cache_key)
            if value is not None:
                # 同步到内存缓存
                ttl = self._get_ttl(cache_type) if cache_type else self.cache_config['default_ttl']
                with self.memory_cache_lock:
                    self.memory_cache[cache_key] = {
                        'value': value,
                        'expire_time': time.time() + ttl
                    }

                return self._deserialize_value(value)

            return None

        except Exception as e:
            logger.error(f"获取缓存失败: {e}")
            return None

    def _deserialize_value(self, value: str) -> Any:
        """反序列化值"""
        try:
            # 尝试解析JSON
            return json.loads(value)
        except (json.JSONDecodeError, ValueError):
            # 如果不是JSON，返回原值
            return value

    def delete(self, key: str) -> bool:
        """
        删除缓存

        Args:
            key: 缓存键

        Returns:
            bool: 是否删除成功
        """
        try:
            cache_key = self._generate_cache_key(key)

            # 删除Redis缓存
            redis_result = self.redis_client.delete(cache_key)

            # 删除内存缓存
            with self.memory_cache_lock:
                if cache_key in self.memory_cache:
                    del self.memory_cache[cache_key]

            return redis_result

        except Exception as e:
            logger.error(f"删除缓存失败: {e}")
            return False

    def clear_pattern(self, pattern: str) -> int:
        """
        批量删除缓存

        Args:
            pattern: 匹配模式

        Returns:
            int: 删除的键数量
        """
        try:
            cache_pattern = self._generate_cache_key(pattern)
            keys = self.redis_client.keys(cache_pattern)

            if keys:
                deleted_count = self.redis_client.delete(*keys)

                # 清理内存缓存
                with self.memory_cache_lock:
                    keys_to_delete = [k for k in self.memory_cache.keys() if any(key in k for key in keys)]
                    for k in keys_to_delete:
                        del self.memory_cache[k]

                return deleted_count

            return 0

        except Exception as e:
            logger.error(f"批量删除缓存失败: {e}")
            return 0

    def exists(self, key: str) -> bool:
        """
        检查缓存是否存在

        Args:
            key: 缓存键

        Returns:
            bool: 是否存在
        """
        try:
            cache_key = self._generate_cache_key(key)

            # 先查内存缓存
            with self.memory_cache_lock:
                if cache_key in self.memory_cache:
                    cache_item = self.memory_cache[cache_key]
                    return cache_item['expire_time'] > time.time()

            # 查Redis缓存
            return self.redis_client.exists(cache_key) > 0

        except Exception as e:
            logger.error(f"检查缓存存在性失败: {e}")
            return False

    def expire(self, key: str, ttl: int) -> bool:
        """
        设置缓存过期时间

        Args:
            key: 缓存键
            ttl: 过期时间（秒）

        Returns:
            bool: 是否设置成功
        """
        try:
            cache_key = self._generate_cache_key(key)

            # 设置Redis过期时间
            redis_result = self.redis_client.expire(cache_key, ttl)

            # 更新内存缓存过期时间
            with self.memory_cache_lock:
                if cache_key in self.memory_cache:
                    self.memory_cache[cache_key]['expire_time'] = time.time() + ttl

            return redis_result

        except Exception as e:
            logger.error(f"设置缓存过期时间失败: {e}")
            return False

    def get_cache_stats(self) -> Dict[str, Any]:
        """
        获取缓存统计信息

        Returns:
            Dict: 统计信息
        """
        try:
            redis_info = self.redis_client.info()

            with self.memory_cache_lock:
                memory_cache_size = len(self.memory_cache)

            return {
                'redis_memory_used': redis_info.get('used_memory_human'),
                'redis_connected_clients': redis_info.get('connected_clients'),
                'redis_keyspace_hits': redis_info.get('keyspace_hits', 0),
                'redis_keyspace_misses': redis_info.get('keyspace_misses', 0),
                'memory_cache_size': memory_cache_size,
                'hit_rate': self._calculate_hit_rate(redis_info)
            }

        except Exception as e:
            logger.error(f"获取缓存统计失败: {e}")
            return {}

    def _calculate_hit_rate(self, redis_info: Dict) -> float:
        """计算缓存命中率"""
        hits = redis_info.get('keyspace_hits', 0)
        misses = redis_info.get('keyspace_misses', 0)
        total = hits + misses

        if total > 0:
            return (hits / total) * 100
        return 0.0


class AttendanceCacheService:
    """考勤业务缓存服务"""

    def __init__(self, cache_manager: AttendanceCacheManager = None):
        """
        初始化缓存服务

        Args:
            cache_manager: 缓存管理器实例
        """
        self.cache_manager = cache_manager or AttendanceCacheManager()

        # 缓存键模式
        self.key_patterns = {
            'attendance_record': 'record:{employee_id}:{date}',
            'employee_schedule': 'schedule:{employee_id}',
            'attendance_statistics': 'stats:{type}:{params_hash}',
            'department_stats': 'dept_stats:{dept_id}:{date_range}',
            'today_attendance': 'today:{date}',
            'attendance_rules': 'rules:{employee_id}',
            'calendar_data': 'calendar:{year}:{month}'
        }

    def cache_attendance_record(self, employee_id: int, date: str, record_data: Dict) -> bool:
        """缓存考勤记录"""
        key = self.key_patterns['attendance_record'].format(
            employee_id=employee_id,
            date=date
        )
        return self.cache_manager.set(key, record_data, cache_type='attendance_record')

    def get_attendance_record(self, employee_id: int, date: str) -> Optional[Dict]:
        """获取考勤记录缓存"""
        key = self.key_patterns['attendance_record'].format(
            employee_id=employee_id,
            date=date
        )
        return self.cache_manager.get(key, 'attendance_record')

    def cache_employee_schedule(self, employee_id: int, schedule_data: List[Dict]) -> bool:
        """缓存员工排班"""
        key = self.key_patterns['employee_schedule'].format(employee_id=employee_id)
        return self.cache_manager.set(key, schedule_data, cache_type='employee_schedule')

    def get_employee_schedule(self, employee_id: int) -> Optional[List[Dict]]:
        """获取员工排班缓存"""
        key = self.key_patterns['employee_schedule'].format(employee_id=employee_id)
        return self.cache_manager.get(key, 'employee_schedule')

    def cache_attendance_statistics(self, stats_type: str, params: Dict, stats_data: Dict) -> bool:
        """缓存考勤统计数据"""
        # 生成参数哈希作为键的一部分
        params_str = json.dumps(params, sort_keys=True)
        params_hash = hashlib.md5(params_str.encode()).hexdigest()

        key = self.key_patterns['attendance_statistics'].format(
            type=stats_type,
            params_hash=params_hash
        )
        return self.cache_manager.set(key, stats_data, cache_type='attendance_statistics')

    def get_attendance_statistics(self, stats_type: str, params: Dict) -> Optional[Dict]:
        """获取考勤统计数据缓存"""
        params_str = json.dumps(params, sort_keys=True)
        params_hash = hashlib.md5(params_str.encode()).hexdigest()

        key = self.key_patterns['attendance_statistics'].format(
            type=stats_type,
            params_hash=params_hash
        )
        return self.cache_manager.get(key, 'attendance_statistics')

    def cache_department_stats(self, dept_id: int, date_range: str, stats_data: Dict) -> bool:
        """缓存部门统计数据"""
        key = self.key_patterns['department_stats'].format(
            dept_id=dept_id,
            date_range=date_range
        )
        return self.cache_manager.set(key, stats_data, cache_type='department_stats')

    def get_department_stats(self, dept_id: int, date_range: str) -> Optional[Dict]:
        """获取部门统计数据缓存"""
        key = self.key_patterns['department_stats'].format(
            dept_id=dept_id,
            date_range=date_range
        )
        return self.cache_manager.get(key, 'department_stats')

    def cache_today_attendance(self, date: str, attendance_data: Dict) -> bool:
        """缓存今日考勤数据"""
        key = self.key_patterns['today_attendance'].format(date=date)
        return self.cache_manager.set(key, attendance_data, cache_type='today_attendance')

    def get_today_attendance(self, date: str) -> Optional[Dict]:
        """获取今日考勤数据缓存"""
        key = self.key_patterns['today_attendance'].format(date=date)
        return self.cache_manager.get(key, 'today_attendance')

    def cache_attendance_rules(self, employee_id: int, rules_data: List[Dict]) -> bool:
        """缓存考勤规则"""
        key = self.key_patterns['attendance_rules'].format(employee_id=employee_id)
        return self.cache_manager.set(key, rules_data, cache_type='attendance_rules')

    def get_attendance_rules(self, employee_id: int) -> Optional[List[Dict]]:
        """获取考勤规则缓存"""
        key = self.key_patterns['attendance_rules'].format(employee_id=employee_id)
        return self.cache_manager.get(key, 'attendance_rules')

    def cache_calendar_data(self, year: int, month: int, calendar_data: List[Dict]) -> bool:
        """缓存日历数据"""
        key = self.key_patterns['calendar_data'].format(year=year, month=month)
        return self.cache_manager.set(key, calendar_data, cache_type='calendar_data')

    def get_calendar_data(self, year: int, month: int) -> Optional[List[Dict]]:
        """获取日历数据缓存"""
        key = self.key_patterns['calendar_data'].format(year=year, month=month)
        return self.cache_manager.get(key, 'calendar_data')

    def invalidate_employee_cache(self, employee_id: int) -> int:
        """失效员工相关缓存"""
        patterns_to_clear = [
            f"record:{employee_id}:*",
            f"schedule:{employee_id}",
            f"rules:{employee_id}"
        ]

        cleared_count = 0
        for pattern in patterns_to_clear:
            cleared_count += self.cache_manager.clear_pattern(pattern)

        return cleared_count

    def invalidate_date_range_cache(self, start_date: str, end_date: str) -> int:
        """失效日期范围相关缓存"""
        # 清理今日考勤缓存
        if start_date <= datetime.now().strftime('%Y-%m-%d') <= end_date:
            self.cache_manager.delete('today:' + datetime.now().strftime('%Y-%m-%d'))

        # 清理统计缓存（由于参数复杂，直接清空所有统计缓存）
        cleared_count = self.cache_manager.clear_pattern('stats:*')

        return cleared_count

    def warm_up_cache(self) -> Dict[str, int]:
        """缓存预热

        Returns:
            Dict: 预热结果统计
        """
        results = {
            'success_count': 0,
            'error_count': 0,
            'total_count': 0
        }

        try:
            # 预热考勤规则缓存
            # 这里应该从数据库加载常用数据并缓存
            logger.info("开始缓存预热...")

            # 示例：预热考勤规则
            rules_data = [
                {
                    'rule_id': 1,
                    'rule_name': '标准工作制',
                    'work_start_time': '09:00',
                    'work_end_time': '18:00'
                }
            ]

            if self.cache_manager.set('rules:warmup', rules_data, cache_type='attendance_rules'):
                results['success_count'] += 1
            else:
                results['error_count'] += 1

            results['total_count'] += 1

            logger.info(f"缓存预热完成: 成功{results['success_count']}, 失败{results['error_count']}")

        except Exception as e:
            logger.error(f"缓存预热失败: {e}")
            results['error_count'] += 1
            results['total_count'] += 1

        return results


def main():
    """主函数 - 演示缓存功能"""
    print("🚀 考勤模块缓存优化演示")

    # 初始化缓存管理器
    cache_manager = AttendanceCacheManager()
    cache_service = AttendanceCacheService(cache_manager)

    # 测试基本缓存操作
    print("\n📝 测试基本缓存操作...")

    # 设置缓存
    test_data = {
        'employee_id': 1,
        'employee_name': '张三',
        'punch_time': '2025-11-17 09:00:00',
        'punch_type': '上班'
    }

    if cache_service.cache_attendance_record(1, '2025-11-17', test_data):
        print("✅ 缓存设置成功")
    else:
        print("❌ 缓存设置失败")

    # 获取缓存
    cached_data = cache_service.get_attendance_record(1, '2025-11-17')
    if cached_data:
        print(f"✅ 缓存获取成功: {cached_data['employee_name']}")
    else:
        print("❌ 缓存获取失败")

    # 演示批量操作
    print("\n📊 演示批量缓存操作...")

    # 批量设置员工排班
    schedule_data = [
        {
            'date': '2025-11-18',
            'schedule_type': 'FIXED',
            'work_start_time': '09:00',
            'work_end_time': '18:00'
        },
        {
            'date': '2025-11-19',
            'schedule_type': 'FLEXIBLE',
            'work_start_time': '10:00',
            'work_end_time': '19:00'
        }
    ]

    if cache_service.cache_employee_schedule(1, schedule_data):
        print("✅ 批量排班缓存设置成功")
        print(f"   缓存了 {len(schedule_data)} 条排班记录")
    else:
        print("❌ 批量排班缓存设置失败")

    # 演示缓存失效
    print("\n🗑️  演示缓存失效...")

    invalidated_count = cache_service.invalidate_employee_cache(1)
    print(f"✅ 失效员工缓存: {invalidated_count} 个")

    # 获取缓存统计
    print("\n📈 缓存统计信息:")
    stats = cache_manager.get_cache_stats()
    for key, value in stats.items():
        print(f"   {key}: {value}")

    # 演示缓存预热
    print("\n🔥 演示缓存预热...")
    warmup_results = cache_service.warm_up_cache()
    print(f"预热结果: {warmup_results}")

    print("\n🎉 缓存优化演示完成！")


if __name__ == "__main__":
    main()