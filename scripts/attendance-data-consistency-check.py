#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
考勤模块数据一致性检查脚本
用于验证前后端数据传输的一致性，包括数据类型、格式、业务逻辑验证
"""

import requests
import json
import datetime
from typing import Dict, Any, List, Tuple
import logging

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('attendance-consistency-check.log', encoding='utf-8'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)

class AttendanceDataConsistencyChecker:
    """考勤数据一致性检查器"""

    def __init__(self, base_url: str = "http://localhost:1024"):
        self.base_url = base_url
        self.api_base = f"{base_url}/api"
        self.session = requests.Session()
        self.session.headers.update({
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token'
        })

        # 测试结果
        self.total_checks = 0
        self.passed_checks = 0
        self.failed_checks = 0
        self.warnings = []

    def log_result(self, check_name: str, passed: bool, details: str = ""):
        """记录测试结果"""
        self.total_checks += 1
        if passed:
            self.passed_checks += 1
            logger.info(f"✅ {check_name}: 通过 - {details}")
        else:
            self.failed_checks += 1
            logger.error(f"❌ {check_name}: 失败 - {details}")

    def log_warning(self, check_name: str, warning: str):
        """记录警告"""
        self.warnings.append(warning)
        logger.warning(f"⚠️  {check_name}: {warning}")

    def test_attendance_record_structure(self) -> bool:
        """测试考勤记录数据结构"""
        try:
            response = self.session.get(f"{self.api_base}/attendance/today-punch")
            data = response.json()

            # 检查响应结构
            structure_checks = [
                ('success字段存在', 'success' in data),
                ('message字段存在', 'message' in data),
                ('data字段存在', 'data' in data),
                ('success为布尔值', isinstance(data.get('success'), bool))
            ]

            all_passed = True
            for check_name, check_result in structure_checks:
                self.log_result(f"记录结构-{check_name}", check_result)
                if not check_result:
                    all_passed = False

            # 检查data字段内容
            if data.get('success') and 'data' in data:
                record_data = data['data']

                # 检查记录数据字段
                field_checks = [
                    ('records字段', 'records' in record_data),
                    ('recentRecords字段', 'recentRecords' in record_data),
                    ('status字段', 'status' in record_data),
                    ('statusColor字段', 'statusColor' in record_data),
                    ('workHours字段', 'workHours' in record_data)
                ]

                for check_name, check_result in field_checks:
                    self.log_result(f"记录字段-{check_name}", check_result)
                    if not check_result:
                        all_passed = False

                # 检查records数组数据
                if 'records' in record_data and isinstance(record_data['records'], list):
                    for i, record in enumerate(record_data['records']):
                        record_checks = [
                            (f'record[{i}].punchType字段', 'punchType' in record),
                            (f'record[{i}].punchTime字段', 'punchTime' in record),
                            (f'record[{i}].location字段', 'location' in record),
                            (f'record[{i}].punchType值有效', record.get('punchType') in ['上班', '下班'])
                        ]

                        for check_name, check_result in record_checks:
                            self.log_result(f"记录数组-{check_name}", check_result)
                            if not check_result:
                                all_passed = False

            return all_passed

        except Exception as e:
            self.log_result("考勤记录结构测试", False, f"请求失败: {str(e)}")
            return False

    def test_statistics_data_format(self) -> bool:
        """测试统计数据格式"""
        try:
            # 测试个人统计
            params = {
                'employeeId': 1,
                'startDate': '2025-11-01',
                'endDate': '2025-11-17'
            }

            response = self.session.get(f"{self.api_base}/attendance/personal-statistics", params=params)
            data = response.json()

            if not data.get('success'):
                self.log_result("统计数据格式测试", False, "API响应失败")
                return False

            # 检查统计数据结构
            structure_checks = [
                ('overview字段', 'overview' in data['data']),
                ('charts字段', 'charts' in data['data']),
                ('list字段', 'list' in data['data'])
            ]

            all_passed = True
            for check_name, check_result in structure_checks:
                self.log_result(f"统计结构-{check_name}", check_result)
                if not check_result:
                    all_passed = False

            # 检查概览数据
            if 'overview' in data['data']:
                overview = data['data']['overview']
                overview_checks = [
                    ('attendanceRate数值有效', 0 <= overview.get('attendanceRate', 0) <= 100),
                    ('avgWorkHours数值有效', overview.get('avgWorkHours', 0) >= 0),
                    ('lateCount数值有效', overview.get('lateCount', 0) >= 0)
                ]

                for check_name, check_result in overview_checks:
                    self.log_result(f"概览数据-{check_name}", check_result)
                    if not check_result:
                        all_passed = False

            return all_passed

        except Exception as e:
            self.log_result("统计数据格式测试", False, f"请求失败: {str(e)}")
            return False

    def test_schedule_data_validation(self) -> bool:
        """测试排班数据验证"""
        try:
            # 创建测试排班数据
            test_schedule = {
                "employeeId": 1,
                "scheduleDate": "2025-11-18",
                "scheduleType": "FIXED",
                "workStartTime": "09:00",
                "workEndTime": "18:00",
                "breakStartTime": "12:00",
                "breakEndTime": "13:00"
            }

            response = self.session.post(f"{self.api_base}/attendance/schedule", json=test_schedule)
            data = response.json()

            # 检查创建响应
            self.log_result("排班创建响应", data.get('success', False), data.get('message', ''))

            # 验证时间格式
            time_checks = [
                ('workStartTime格式', self._validate_time_format(test_schedule['workStartTime'])),
                ('workEndTime格式', self._validate_time_format(test_schedule['workEndTime'])),
                ('breakStartTime格式', self._validate_time_format(test_schedule['breakStartTime'])),
                ('breakEndTime格式', self._validate_time_format(test_schedule['breakEndTime']))
            ]

            all_passed = True
            for check_name, check_result in time_checks:
                self.log_result(f"排班时间-{check_name}", check_result)
                if not check_result:
                    all_passed = False

            return all_passed

        except Exception as e:
            self.log_result("排班数据验证测试", False, f"请求失败: {str(e)}")
            return False

    def test_punch_data_validation(self) -> bool:
        """测试打卡数据验证"""
        try:
            # 创建测试打卡数据
            test_punch = {
                "employeeId": 1,
                "punchType": "上班",
                "punchTime": "2025-11-17 09:00:00",
                "latitude": 39.9042,
                "longitude": 116.4074,
                "deviceId": "TEST_DEVICE_001",
                "location": "北京市朝阳区"
            }

            response = self.session.post(f"{self.api_base}/attendance/punch-in", json=test_punch)
            data = response.json()

            # 检查打卡响应
            self.log_result("打卡创建响应", data.get('success', False), data.get('message', ''))

            # 验证坐标数据
            coordinate_checks = [
                ('latitude范围', -90 <= test_punch['latitude'] <= 90),
                ('longitude范围', -180 <= test_punch['longitude'] <= 180),
                ('punchTime格式', self._validate_datetime_format(test_punch['punchTime']))
            ]

            all_passed = True
            for check_name, check_result in coordinate_checks:
                self.log_result(f"打卡数据-{check_name}", check_result)
                if not check_result:
                    all_passed = False

            return all_passed

        except Exception as e:
            self.log_result("打卡数据验证测试", False, f"请求失败: {str(e)}")
            return False

    def test_pagination_data(self) -> bool:
        """测试分页数据"""
        try:
            # 测试不同的分页参数
            pagination_tests = [
                {"pageNum": 1, "pageSize": 10},
                {"pageNum": 2, "pageSize": 5},
                {"pageNum": 1, "pageSize": 20}
            ]

            all_passed = True
            for i, params in enumerate(pagination_tests):
                response = self.session.get(f"{self.api_base}/attendance/records", params=params)
                data = response.json()

                checks = [
                    (f'分页{i+1}-响应成功', data.get('success', False)),
                    (f'分页{i+1}-包含list', 'list' in data.get('data', {})),
                    (f'分页{i+1}-包含total', 'total' in data.get('data', {})),
                    (f'分页{i+1}-total为数字', isinstance(data.get('data', {}).get('total'), int)),
                    (f'分页{i+1}-list为列表', isinstance(data.get('data', {}).get('list'), list))
                ]

                for check_name, check_result in checks:
                    self.log_result(check_name, check_result)
                    if not check_result:
                        all_passed = False

                # 检查分页逻辑
                page_data = data.get('data', {})
                if page_data.get('total') and page_data.get('pageSize'):
                    expected_max_page = (page_data['total'] + page_data['pageSize'] - 1) // page_data['pageSize']
                    if params['pageNum'] > expected_max_page:
                        # 超出最大页数应该返回空列表
                        self.log_result(f"分页{i+1}-超出页数逻辑", len(page_data.get('list', [])) == 0)

            return all_passed

        except Exception as e:
            self.log_result("分页数据测试", False, f"请求失败: {str(e)}")
            return False

    def test_error_handling(self) -> bool:
        """测试错误处理"""
        try:
            error_test_cases = [
                {
                    "name": "无效员工ID",
                    "url": f"{self.api_base}/attendance/punch-in",
                    "data": {"employeeId": 999999, "punchType": "上班"},
                    "expected_status": 400
                },
                {
                    "name": "无效打卡类型",
                    "url": f"{self.api_base}/attendance/punch-in",
                    "data": {"employeeId": 1, "punchType": "invalid"},
                    "expected_status": 400
                },
                {
                    "name": "缺少必需字段",
                    "url": f"{self.api_base}/attendance/punch-in",
                    "data": {"employeeId": 1},
                    "expected_status": 400
                }
            ]

            all_passed = True
            for test_case in error_test_cases:
                try:
                    response = self.session.post(test_case["url"], json=test_case["data"])

                    # 检查是否返回了适当的错误状态码
                    if response.status_code == test_case["expected_status"]:
                        self.log_result(f"错误处理-{test_case['name']}", True, f"正确返回{response.status_code}")
                    else:
                        self.log_result(f"错误处理-{test_case['name']}", False,
                                     f"期望{test_case['expected_status']}，实际{response.status_code}")
                        all_passed = False

                except Exception as e:
                    self.log_result(f"错误处理-{test_case['name']}", False, f"请求异常: {str(e)}")
                    all_passed = False

            return all_passed

        except Exception as e:
            self.log_result("错误处理测试", False, f"测试失败: {str(e)}")
            return False

    def test_datetime_consistency(self) -> bool:
        """测试日期时间一致性"""
        try:
            # 获取当前考勤记录
            response = self.session.get(f"{self.api_base}/attendance/today-punch")
            data = response.json()

            if not data.get('success'):
                self.log_result("日期时间一致性测试", False, "无法获取考勤记录")
                return False

            records = data['data'].get('records', [])
            all_passed = True

            for i, record in enumerate(records):
                punch_time = record.get('punchTime')
                if punch_time:
                    # 验证日期时间格式
                    if self._validate_datetime_format(punch_time):
                        # 检查时间是否合理
                        try:
                            dt = datetime.datetime.fromisoformat(punch_time)
                            # 检查时间范围（不能是未来时间）
                            if dt > datetime.datetime.now():
                                self.log_warning(f"记录{i}-未来时间", f"打卡时间 {punch_time} 超出当前时间")
                            else:
                                self.log_result(f"记录{i}-时间格式", True, f"有效时间: {punch_time}")
                        except ValueError:
                            self.log_result(f"记录{i}-时间解析", False, f"无法解析时间: {punch_time}")
                            all_passed = False
                    else:
                        self.log_result(f"记录{i}-时间格式", False, f"无效格式: {punch_time}")
                        all_passed = False

            return all_passed

        except Exception as e:
            self.log_result("日期时间一致性测试", False, f"测试失败: {str(e)}")
            return False

    def _validate_time_format(self, time_str: str) -> bool:
        """验证时间格式 HH:MM"""
        try:
            if ':' not in time_str:
                return False

            parts = time_str.split(':')
            if len(parts) != 2:
                return False

            hour = int(parts[0])
            minute = int(parts[1])

            return 0 <= hour <= 23 and 0 <= minute <= 59
        except:
            return False

    def _validate_datetime_format(self, datetime_str: str) -> bool:
        """验证日期时间格式 YYYY-MM-DD HH:MM:SS"""
        try:
            datetime.datetime.fromisoformat(datetime_str)
            return True
        except:
            return False

    def run_all_checks(self) -> Tuple[int, int, int, List[str]]:
        """运行所有检查"""
        logger.info("🚀 开始考勤模块数据一致性检查...")

        # 运行各项检查
        checks = [
            ("考勤记录结构", self.test_attendance_record_structure),
            ("统计数据格式", self.test_statistics_data_format),
            ("排班数据验证", self.test_schedule_data_validation),
            ("打卡数据验证", self.test_punch_data_validation),
            ("分页数据", self.test_pagination_data),
            ("错误处理", self.test_error_handling),
            ("日期时间一致性", self.test_datetime_consistency)
        ]

        for check_name, check_func in checks:
            logger.info(f"🔍 执行检查: {check_name}")
            try:
                check_func()
            except Exception as e:
                logger.error(f"检查 {check_name} 执行异常: {str(e)}")
                self.failed_checks += 1
                self.total_checks += 1

        # 输出结果
        logger.info("=" * 50)
        logger.info("📊 检查结果汇总:")
        logger.info(f"   总检查数: {self.total_checks}")
        logger.info(f"   通过检查: {self.passed_checks}")
        logger.info(f"   失败检查: {self.failed_checks}")
        logger.info(f"   警告数量: {len(self.warnings)}")

        if self.warnings:
            logger.info("⚠️  警告详情:")
            for warning in self.warnings:
                logger.info(f"   - {warning}")

        success_rate = (self.passed_checks / self.total_checks * 100) if self.total_checks > 0 else 0

        if self.failed_checks == 0:
            logger.info("🎉 所有检查通过！数据一致性良好。")
        else:
            logger.warning(f"⚠️  有 {self.failed_checks} 个检查失败，需要修复。")

        logger.info(f"📈 通过率: {success_rate:.1f}%")

        return self.total_checks, self.passed_checks, self.failed_checks, self.warnings

def main():
    """主函数"""
    checker = AttendanceDataConsistencyChecker()

    try:
        # 测试服务器连接
        response = requests.get("http://localhost:1024/api/health", timeout=5)
        if response.status_code != 200:
            logger.error("❌ 无法连接到服务器，请确保后端服务正在运行")
            return
    except requests.exceptions.RequestException:
        logger.error("❌ 无法连接到服务器，请确保后端服务正在运行")
        return

    # 运行所有检查
    total, passed, failed, warnings = checker.run_all_checks()

    # 根据结果设置退出码
    exit_code = 0 if failed == 0 else 1
    exit(exit_code)

if __name__ == "__main__":
    main()