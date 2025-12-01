#!/usr/bin/env python3
"""
IOE-DREAM 微服务测试数据生成器
====================================

功能:
- 生成真实的测试数据
- 支持多种业务场景
- 可配置数据规模和类型
- 支持批量数据生成

作者: IOE-DREAM测试团队
版本: v1.0.0
最后更新: 2025-11-29
"""

import json
import random
import string
import datetime
import uuid
from faker import Faker
from typing import Dict, List, Any, Optional
from dataclasses import dataclass, asdict
import argparse
import logging
import sys
import os

# 初始化Faker（中文支持）
fake = Faker('zh_CN')

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('test-data-generator.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

@dataclass
class TestDataConfig:
    """测试数据配置"""
    user_count: int = 1000
    device_count: int = 100
    transaction_count: int = 10000
    attendance_count: int = 5000
    access_record_count: int = 5000
    output_dir: str = "./generated-data"
    file_format: str = "json"  # json, csv, sql
    include_deleted: bool = False
    start_date: str = "2024-01-01"
    end_date: str = "2024-12-31"

class TestDataGenerator:
    """测试数据生成器"""

    def __init__(self, config: TestDataConfig):
        self.config = config
        self.faker = fake
        self.departments = ["技术部", "市场部", "人事部", "财务部", "运营部", "行政部", "安全部"]
        self.device_types = ["ACCESS_CONTROL", "CAMERA", "ATTENDANCE", "CONSUME", "FIRE_ALARM"]
        self.access_types = ["CARD", "FACE", "FINGERPRINT", "PASSWORD", "QR_CODE"]
        self.consume_types = ["MEAL", "SHOP", "TRANSPORT", "OTHER"]
        self.attendance_types = ["CHECK_IN", "CHECK_OUT", "BREAK_START", "BREAK_END"]

        # 确保输出目录存在
        os.makedirs(config.output_dir, exist_ok=True)

    def generate_users(self) -> List[Dict]:
        """生成用户数据"""
        logger.info(f"生成 {self.config.user_count} 个用户...")

        users = []
        for i in range(1, self.config.user_count + 1):
            gender = random.choice(['MALE', 'FEMALE'])
            first_name = self.faker.first_name_male() if gender == 'MALE' else self.faker.first_name_female()
            last_name = self.faker.last_name()

            user = {
                "user_id": f"user_{i:06d}",
                "username": f"user{i:06d}",
                "password": "testpass123",
                "real_name": f"{last_name}{first_name}",
                "gender": gender,
                "email": f"user{i:06d}@test.com",
                "phone": self.faker.phone_number(),
                "department": random.choice(self.departments),
                "position": random.choice(["员工", "主管", "经理", "总监", "工程师", "专员"]),
                "employee_id": f"emp{i:06d}",
                "card_number": f"card{i:06d}",
                "face_id": f"face_{uuid.uuid4().hex[:16]}",
                "fingerprint_id": f"fp_{uuid.uuid4().hex[:16]}" if random.random() > 0.3 else None,
                "status": random.choice(["ACTIVE", "INACTIVE", "SUSPENDED"]),
                "create_time": self._random_datetime(),
                "update_time": self._random_datetime(),
                "deleted_flag": random.choice([0, 1]) if self.config.include_deleted else 0
            }
            users.append(user)

        return users

    def generate_devices(self) -> List[Dict]:
        """生成设备数据"""
        logger.info(f"生成 {self.config.device_count} 个设备...")

        devices = []
        locations = ["大门", "侧门", "后门", "办公室", "食堂", "车间", "仓库", "会议室", "停车场"]

        for i in range(1, self.config.device_count + 1):
            device_type = random.choice(self.device_types)
            device_name = self._generate_device_name(device_type, i)

            device = {
                "device_id": f"device_{i:06d}",
                "device_name": device_name,
                "device_type": device_type,
                "device_model": random.choice(["X1000", "X2000", "X3000", "PRO100", "PRO200"]),
                "manufacturer": random.choice(["海康威视", "大华", "宇视", "索尼", "三星"]),
                "serial_number": f"SN{uuid.uuid4().hex[:12].upper()}",
                "ip_address": self._generate_ip_address(),
                "port": self._generate_port(device_type),
                "location": random.choice(locations),
                "building": f"{random.choice(['A', 'B', 'C'])}栋",
                "floor": f"{random.randint(1, 20)}层",
                "room": f"{random.randint(1, 999)}室",
                "status": random.choice(["ONLINE", "OFFLINE", "MAINTENANCE", "ERROR"]),
                "install_time": self._random_datetime(),
                "last_heartbeat": self._random_datetime(),
                "firmware_version": f"v{random.randint(1, 5)}.{random.randint(0, 9)}.{random.randint(0, 99)}",
                "create_time": self._random_datetime(),
                "update_time": self._random_datetime(),
                "deleted_flag": random.choice([0, 1]) if self.config.include_deleted else 0
            }
            devices.append(device)

        return devices

    def generate_access_records(self, users: List[Dict], devices: List[Dict]) -> List[Dict]:
        """生成门禁记录"""
        logger.info(f"生成 {self.config.access_record_count} 条门禁记录...")

        access_devices = [d for d in devices if d["device_type"] == "ACCESS_CONTROL"]
        if not access_devices:
            access_devices = devices  # 如果没有专门的门禁设备，使用所有设备

        records = []
        for i in range(self.config.access_record_count):
            user = random.choice(users)
            device = random.choice(access_devices)
            access_type = random.choice(self.access_types)
            allowed = random.random() > 0.05  # 95%的通过率

            record = {
                "record_id": f"access_{i:06d}",
                "user_id": user["user_id"],
                "device_id": device["device_id"],
                "access_type": access_type,
                "card_number": user["card_number"] if access_type in ["CARD", "QR_CODE"] else None,
                "face_id": user["face_id"] if access_type == "FACE" else None,
                "fingerprint_id": user["fingerprint_id"] if access_type == "FINGERPRINT" else None,
                "allowed": allowed,
                "deny_reason": None if allowed else random.choice(["权限不足", "设备故障", "认证失败", "时间限制"]),
                "door_status": random.choice(["OPENED", "CLOSED", "FORCED"]),
                "temperature": round(random.uniform(35.5, 37.5), 1) if random.random() > 0.8 else None,
                "mask_detected": random.choice([True, False]) if random.random() > 0.7 else None,
                "timestamp": self._random_datetime(),
                "create_time": datetime.datetime.now(),
                "deleted_flag": random.choice([0, 1]) if self.config.include_deleted else 0
            }
            records.append(record)

        return records

    def generate_consume_accounts(self, users: List[Dict]) -> List[Dict]:
        """生成消费账户"""
        logger.info("生成消费账户...")

        accounts = []
        for user in users:
            account_types = ["MEAL", "SHOP", "TRANSPORT"]

            for account_type in account_types:
                initial_balance = round(random.uniform(0, 5000), 2)
                current_balance = round(initial_balance - random.uniform(0, initial_balance * 0.8), 2)

                account = {
                    "account_id": f"{account_type}_{user['user_id']}",
                    "user_id": user["user_id"],
                    "account_type": account_type,
                    "account_name": f"{user['real_name']}的{self._get_account_type_name(account_type)}",
                    "initial_balance": initial_balance,
                    "current_balance": current_balance,
                    "total_recharge": round(initial_balance + random.uniform(0, 2000), 2),
                    "total_consume": round(initial_balance - current_balance, 2),
                    "daily_limit": round(random.uniform(100, 500), 2),
                    "status": random.choice(["ACTIVE", "FROZEN", "CANCELLED"]),
                    "create_time": self._random_datetime(),
                    "update_time": self._random_datetime(),
                    "deleted_flag": random.choice([0, 1]) if self.config.include_deleted else 0
                }
                accounts.append(account)

        return accounts

    def generate_transactions(self, users: List[Dict], accounts: List[Dict], devices: List[Dict]) -> List[Dict]:
        """生成消费交易记录"""
        logger.info(f"生成 {self.config.transaction_count} 条交易记录...")

        consume_devices = [d for d in devices if d["device_type"] == "CONSUME"]
        if not consume_devices:
            consume_devices = devices

        transactions = []

        for i in range(self.config.transaction_count):
            account = random.choice(accounts)
            user = next((u for u in users if u["user_id"] == account["user_id"]), None)
            if not user:
                continue

            device = random.choice(consume_devices)
            amount = round(random.uniform(1, 200), 2)
            success = random.random() > 0.02  # 98%成功率

            transaction = {
                "transaction_id": f"txn_{i:08d}",
                "account_id": account["account_id"],
                "user_id": user["user_id"],
                "device_id": device["device_id"],
                "amount": amount,
                "consume_type": account["account_type"],
                "description": self._generate_transaction_description(account["account_type"]),
                "merchant_name": f"{random.choice(['食堂', '超市', '书店', '咖啡厅'])}{random.randint(1, 10)}号店",
                "payment_method": random.choice(["CARD", "MOBILE", "CASH"]),
                "success": success,
                "failure_reason": None if success else random.choice(["余额不足", "账户冻结", "设备故障", "网络错误"]),
                "original_balance": round(account["current_balance"] + amount, 2) if success else account["current_balance"],
                "transaction_balance": round(account["current_balance"] - amount, 2) if success else account["current_balance"],
                "timestamp": self._random_datetime(),
                "operator": user["real_name"],
                "create_time": datetime.datetime.now(),
                "deleted_flag": random.choice([0, 1]) if self.config.include_deleted else 0
            }

            # 更新账户余额
            if success:
                account["current_balance"] = round(account["current_balance"] - amount, 2)

            transactions.append(transaction)

        return transactions

    def generate_attendance_records(self, users: List[Dict], devices: List[Dict]) -> List[Dict]:
        """生成考勤记录"""
        logger.info(f"生成 {self.config.attendance_count} 条考勤记录...")

        attendance_devices = [d for d in devices if d["device_type"] == "ATTENDANCE"]
        if not attendance_devices:
            attendance_devices = devices

        records = []
        work_days = self._generate_work_days()

        for work_day in work_days:
            for user in users:
                if random.random() > 0.95:  # 5%的人每天不打卡
                    continue

                device = random.choice(attendance_devices)

                # 上班打卡
                check_in_time = work_day.replace(hour=random.randint(8, 10), minute=random.randint(0, 59))
                check_in_record = {
                    "record_id": f"att_in_{len(records):08d}",
                    "user_id": user["user_id"],
                    "device_id": device["device_id"],
                    "attendance_type": "CHECK_IN",
                    "check_time": check_in_time,
                    "is_late": check_in_time.hour > 9 or (check_in_time.hour == 9 and check_in_time.minute > 10),
                    "device_name": device["device_name"],
                    "location": device["location"],
                    "create_time": datetime.datetime.now(),
                    "deleted_flag": random.choice([0, 1]) if self.config.include_deleted else 0
                }
                records.append(check_in_record)

                # 下班打卡
                if random.random() > 0.1:  # 90%的人会打卡下班
                    check_out_time = work_day.replace(hour=random.randint(17, 20), minute=random.randint(0, 59))
                    check_out_record = {
                        "record_id": f"att_out_{len(records):08d}",
                        "user_id": user["user_id"],
                        "device_id": device["device_id"],
                        "attendance_type": "CHECK_OUT",
                        "check_time": check_out_time,
                        "is_overtime": check_out_time.hour > 18,
                        "device_name": device["device_name"],
                        "location": device["location"],
                        "create_time": datetime.datetime.now(),
                        "deleted_flag": random.choice([0, 1]) if self.config.include_deleted else 0
                    }
                    records.append(check_out_record)

        return records[:self.config.attendance_count]

    def generate_video_devices(self) -> List[Dict]:
        """生成视频设备数据"""
        logger.info("生成视频设备数据...")

        video_devices = []
        locations = ["大门口", "办公楼", "停车场", "食堂", "仓库", "走廊", "电梯", "监控中心"]

        for i in range(1, 51):  # 生成50个视频设备
            location = random.choice(locations)

            device = {
                "device_id": f"video_{i:06d}",
                "device_name": f"{location}{i}号摄像头",
                "device_type": "CAMERA",
                "ip_address": self._generate_ip_address(),
                "port": 554,
                "username": "admin",
                "password": "admin123",
                "rtsp_url": f"rtsp://admin:admin123@{self._generate_ip_address()}:554/stream",
                "resolution": random.choice(["1080P", "720P", "4K"]),
                "frame_rate": random.choice([25, 30]),
                "location": location,
                "building": f"{random.choice(['A', 'B', 'C'])}栋",
                "floor": f"{random.randint(1, 20)}层",
                "status": random.choice(["ONLINE", "OFFLINE", "RECORDING", "ERROR"]),
                "record_enabled": random.choice([True, False]),
                "motion_detection": random.choice([True, False]),
                "create_time": self._random_datetime(),
                "update_time": self._random_datetime(),
                "deleted_flag": random.choice([0, 1]) if self.config.include_deleted else 0
            }
            video_devices.append(device)

        return video_devices

    def save_data(self, data: List[Dict], filename: str):
        """保存数据到文件"""
        filepath = os.path.join(self.config.output_dir, filename)

        if self.config.file_format.lower() == "json":
            with open(filepath, 'w', encoding='utf-8') as f:
                json.dump(data, f, ensure_ascii=False, indent=2, default=str)

        elif self.config.file_format.lower() == "csv":
            import csv
            if data:
                with open(filepath, 'w', newline='', encoding='utf-8') as f:
                    writer = csv.DictWriter(f, fieldnames=data[0].keys())
                    writer.writeheader()
                    writer.writerows(data)

        elif self.config.file_format.lower() == "sql":
            self._save_as_sql(data, filename)

        logger.info(f"数据已保存到: {filepath}")

    def _save_as_sql(self, data: List[Dict], filename: str):
        """保存为SQL插入语句"""
        filepath = os.path.join(self.config.output_dir, filename)
        table_name = filename.replace('.sql', '')

        with open(filepath, 'w', encoding='utf-8') as f:
            if data:
                # 获取字段名
                columns = list(data[0].keys())

                for record in data:
                    values = []
                    for col in columns:
                        value = record[col]
                        if value is None:
                            values.append('NULL')
                        elif isinstance(value, bool):
                            values.append('1' if value else '0')
                        elif isinstance(value, str):
                            # 转义单引号
                            escaped_value = str(value).replace("'", "''")
                            values.append(f"'{escaped_value}'")
                        elif isinstance(value, (int, float)):
                            values.append(str(value))
                        else:
                            # 处理datetime等对象
                            escaped_value = str(value).replace("'", "''")
                            values.append(f"'{escaped_value}'")

                    f.write(f"INSERT INTO {table_name} ({', '.join(columns)}) VALUES ({', '.join(values)});\n")

    def _random_datetime(self) -> datetime.datetime:
        """生成随机日期时间"""
        start = datetime.datetime.strptime(self.config.start_date, "%Y-%m-%d")
        end = datetime.datetime.strptime(self.config.end_date, "%Y-%m-%d")
        return self.faker.date_time_between(start_date=start, end_date=end)

    def _generate_work_days(self) -> List[datetime.datetime]:
        """生成工作日列表"""
        start = datetime.datetime.strptime(self.config.start_date, "%Y-%m-%d")
        end = datetime.datetime.strptime(self.config.end_date, "%Y-%m-%d")

        work_days = []
        current = start
        while current <= end:
            # 周一到周五
            if current.weekday() < 5:
                work_days.append(current)
            current += datetime.timedelta(days=1)

        return work_days

    def _generate_ip_address(self) -> str:
        """生成IP地址"""
        return f"192.168.{random.randint(1, 254)}.{random.randint(1, 254)}"

    def _generate_port(self, device_type: str) -> int:
        """根据设备类型生成端口"""
        port_ranges = {
            "ACCESS_CONTROL": (8000, 8099),
            "CAMERA": (554, 554),
            "ATTENDANCE": (9000, 9099),
            "CONSUME": (7000, 7099),
            "FIRE_ALARM": (6000, 6099)
        }

        range_start, range_end = port_ranges.get(device_type, (8000, 8099))
        return random.randint(range_start, range_end)

    def _generate_device_name(self, device_type: str, index: int) -> str:
        """生成设备名称"""
        type_names = {
            "ACCESS_CONTROL": "门禁控制器",
            "CAMERA": "网络摄像头",
            "ATTENDANCE": "考勤机",
            "CONSUME": "消费终端",
            "FIRE_ALARM": "火灾报警器"
        }

        return f"{type_names.get(device_type, '设备')}{index:03d}"

    def _get_account_type_name(self, account_type: str) -> str:
        """获取账户类型中文名"""
        type_names = {
            "MEAL": "餐费账户",
            "SHOP": "购物账户",
            "TRANSPORT": "交通账户"
        }
        return type_names.get(account_type, account_type)

    def _generate_transaction_description(self, consume_type: str) -> str:
        """生成交易描述"""
        descriptions = {
            "MEAL": ["早餐", "午餐", "晚餐", "夜宵", "点心", "饮料"],
            "SHOP": ["办公用品", "日用品", "零食", "饮料", "文具", "生活用品"],
            "TRANSPORT": ["公交", "地铁", "打车", "停车费", "加油费"]
        }

        items = descriptions.get(consume_type, ["消费"])
        return random.choice(items)

    def generate_all_data(self):
        """生成所有测试数据"""
        logger.info("开始生成测试数据...")

        # 生成基础数据
        users = self.generate_users()
        devices = self.generate_devices()
        video_devices = self.generate_video_devices()

        # 生成业务数据
        access_records = self.generate_access_records(users, devices)
        accounts = self.generate_consume_accounts(users)
        transactions = self.generate_transactions(users, accounts, devices)
        attendance_records = self.generate_attendance_records(users, devices)

        # 保存数据
        self.save_data(users, "users.json")
        self.save_data(devices, "devices.json")
        self.save_data(video_devices, "video_devices.json")
        self.save_data(access_records, "access_records.json")
        self.save_data(accounts, "consume_accounts.json")
        self.save_data(transactions, "transactions.json")
        self.save_data(attendance_records, "attendance_records.json")

        # 生成数据统计
        stats = {
            "generation_time": datetime.datetime.now().isoformat(),
            "config": asdict(self.config),
            "data_counts": {
                "users": len(users),
                "devices": len(devices),
                "video_devices": len(video_devices),
                "access_records": len(access_records),
                "consume_accounts": len(accounts),
                "transactions": len(transactions),
                "attendance_records": len(attendance_records)
            }
        }

        self.save_data(stats, "generation_stats.json")

        logger.info("测试数据生成完成！")
        logger.info(f"输出目录: {self.config.output_dir}")

        return stats

def main():
    """主函数"""
    parser = argparse.ArgumentParser(description='IOE-DREAM 微服务测试数据生成器')

    parser.add_argument('--users', type=int, default=1000, help='用户数量')
    parser.add_argument('--devices', type=int, default=100, help='设备数量')
    parser.add_argument('--transactions', type=int, default=10000, help='交易记录数量')
    parser.add_argument('--attendance', type=int, default=5000, help='考勤记录数量')
    parser.add_argument('--access', type=int, default=5000, help='门禁记录数量')
    parser.add_argument('--output', type=str, default='./generated-data', help='输出目录')
    parser.add_argument('--format', type=str, choices=['json', 'csv', 'sql'], default='json', help='输出格式')
    parser.add_argument('--include-deleted', action='store_true', help='包含已删除的数据')
    parser.add_argument('--start-date', type=str, default='2024-01-01', help='开始日期')
    parser.add_argument('--end-date', type=str, default='2024-12-31', help='结束日期')

    args = parser.parse_args()

    # 创建配置
    config = TestDataConfig(
        user_count=args.users,
        device_count=args.devices,
        transaction_count=args.transactions,
        attendance_count=args.attendance,
        access_record_count=args.access,
        output_dir=args.output,
        file_format=args.format,
        include_deleted=args.include_deleted,
        start_date=args.start_date,
        end_date=args.end_date
    )

    try:
        # 创建生成器
        generator = TestDataGenerator(config)

        # 生成数据
        stats = generator.generate_all_data()

        # 打印统计信息
        print("\n" + "="*50)
        print("数据生成统计")
        print("="*50)
        for key, value in stats["data_counts"].items():
            print(f"{key}: {value}")
        print("="*50)

        return 0

    except Exception as e:
        logger.error(f"数据生成失败: {str(e)}")
        return 1

if __name__ == "__main__":
    sys.exit(main())