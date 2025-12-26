# IOE-DREAM API测试示例

**📅 更新时间**: 2025-12-20
**🎯 文档目标**: 提供完整的API测试示例和最佳实践
**👥 目标用户**: 前端开发人员、测试工程师、第三方集成开发人员

---

## 🚀 快速开始

### 1️⃣ 启动服务并验证API

```bash
#!/bin/bash
# API快速测试脚本

echo "🚀 IOE-DREAM API 快速测试"
echo "================================"

# 服务地址列表
SERVICES=(
    "8080:API网关"
    "8088:公共业务服务"
    "8087:设备通讯服务"
    "8089:OA办公服务"
    "8090:门禁管理服务"
    "8091:考勤管理服务"
    "8092:视频监控服务"
    "8093:数据库管理服务"
    "8094:消费管理服务"
    "8095:访客管理服务"
    "8096:生物模板服务"
)

# 检查服务状态
echo "📊 检查服务状态..."
for service in "${SERVICES[@]}"; do
    IFS=':' read -r port name <<< "$service"
    if curl -s --connect-timeout 3 "http://localhost:$port/actuator/health" > /dev/null; then
        echo "✅ $name (端口$port): 运行正常"
    else
        echo "❌ $name (端口$port): 服务未启动"
    fi
done

echo ""
echo "🔍 验证API文档可访问性..."
for service in "${SERVICES[@]}"; do
    IFS=':' read -r port name <<< "$service"
    if curl -s --connect-timeout 3 "http://localhost:$port/swagger-ui/index.html" > /dev/null; then
        echo "✅ $name API文档: http://localhost:$port/swagger-ui/index.html"
    else
        echo "❌ $name API文档: 无法访问"
    fi
done
```

### 2️⃣ 基础API测试

```bash
#!/bin/bash
# 基础API功能测试

BASE_URL="http://localhost:8088"

echo "🧪 基础API功能测试"
echo "================================"

# 1. 获取验证码
echo "1️⃣ 测试获取验证码..."
CAPTCHA_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/auth/getCaptcha")
echo "验证码响应: $CAPTCHA_RESPONSE"

# 解析验证码信息
CAPTCHA_KEY=$(echo $CAPTCHA_RESPONSE | jq -r '.data.captchaKey')
CAPTCHA_CODE=$(echo $CAPTCHA_RESPONSE | jq -r '.data.captchaCode')
echo "验证码Key: $CAPTCHA_KEY"
echo "验证码Code: $CAPTCHA_CODE"

# 2. 用户登录测试
echo ""
echo "2️⃣ 测试用户登录..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{
        \"username\": \"admin\",
        \"password\": \"admin123\",
        \"captchaKey\": \"$CAPTCHA_KEY\",
        \"captchaCode\": \"$CAPTCHA_CODE\"
    }")
echo "登录响应: $LOGIN_RESPONSE"

# 解析Token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token')
echo "获取Token: ${TOKEN:0:20}..."

# 3. 验证Token
echo ""
echo "3️⃣ 测试Token验证..."
VALIDATE_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/auth/validateToken" \
    -H "Authorization: Bearer $TOKEN")
echo "Token验证响应: $VALIDATE_RESPONSE"

# 4. 获取用户信息
echo ""
echo "4️⃣ 测试获取用户信息..."
USER_INFO_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/auth/userInfo" \
    -H "Authorization: Bearer $TOKEN")
echo "用户信息响应: $USER_INFO_RESPONSE"

# 5. 退出登录
echo ""
echo "5️⃣ 测试退出登录..."
LOGOUT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/logout" \
    -H "Authorization: Bearer $TOKEN")
echo "退出登录响应: $LOGOUT_RESPONSE"

echo ""
echo "✅ 基础API测试完成"
```

---

## 📝 详细API测试示例

### 🔐 认证接口测试

#### 1. 获取验证码

```bash
# 请求
curl -X GET "http://localhost:8088/api/v1/auth/getCaptcha" \
  -H "Content-Type: application/json"

# 响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaKey": "captcha:550e8400-e29b-41d4-a716-446655440000",
    "captchaCode": "1234"
  },
  "timestamp": 1642123456789
}
```

#### 2. 用户登录

```bash
# 请求
curl -X POST "http://localhost:8088/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "captchaKey": "captcha:550e8400-e29b-41d4-a716-446655440000",
    "captchaCode": "1234"
  }'

# 响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
    "refreshToken": "refresh_token_here",
    "expiresIn": 7200,
    "userInfo": {
      "userId": 1,
      "username": "admin",
      "realName": "管理员",
      "roles": ["ADMIN"],
      "permissions": ["USER_MANAGE", "SYSTEM_CONFIG"]
    }
  },
  "timestamp": 1642123456789
}
```

### 👥 用户管理接口测试

#### 1. 分页查询用户

```bash
# 请求
curl -X POST "http://localhost:8088/api/v1/user/query" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{
    "pageNum": 1,
    "pageSize": 20,
    "keyword": "admin"
  }'

# 响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "userId": 1,
        "username": "admin",
        "realName": "管理员",
        "email": "admin@example.com",
        "phone": "13800138000",
        "status": 1,
        "createTime": "2025-12-20T10:00:00",
        "updateTime": "2025-12-20T10:00:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1
  },
  "timestamp": 1642123456789
}
```

#### 2. 添加用户

```bash
# 请求
curl -X POST "http://localhost:8088/api/v1/user/add" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "realName": "测试用户",
    "email": "test@example.com",
    "phone": "13900139000",
    "departmentId": 1,
    "roles": [1]
  }'

# 响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 2,
    "username": "testuser"
  },
  "timestamp": 1642123456789
}
```

### 🚪 门禁管理接口测试

#### 1. 查询门禁设备

```bash
# 请求
curl -X POST "http://localhost:8090/api/v1/access/device/query" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{
    "pageNum": 1,
    "pageSize": 10,
    "deviceStatus": 1
  }'

# 响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "deviceId": "dev_001",
        "deviceName": "主门禁控制器",
        "deviceCode": "ACCESS_MAIN_001",
        "deviceType": "ACCESS_CONTROLLER",
        "deviceStatus": 1,
        "areaId": 1001,
        "areaName": "A栋1楼大厅",
        "onlineStatus": 1,
        "lastHeartbeat": "2025-12-20T10:30:00",
        "createTime": "2025-12-20T09:00:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "timestamp": 1642123456789
}
```

#### 2. 查询通行记录

```bash
# 请求
curl -X POST "http://localhost:8090/api/v1/access/record/query" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{
    "pageNum": 1,
    "pageSize": 20,
    "startDate": "2025-12-20",
    "endDate": "2025-12-20",
    "userId": 1
  }'

# 响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "recordId": "rec_123456",
        "userId": 1,
        "username": "admin",
        "deviceId": "dev_001",
        "deviceName": "主门禁控制器",
        "areaId": 1001,
        "areaName": "A栋1楼大厅",
        "passTime": "2025-12-20T10:30:00",
        "passType": "CARD",
        "passResult": "SUCCESS",
        "photoUrl": "http://domain.com/photos/pass_001.jpg"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1
  },
  "timestamp": 1642123456789
}
```

---

## 🔧 Postman测试集合

### 📥 导入测试集合

```json
{
  "info": {
    "name": "IOE-DREAM API Collection",
    "description": "IOE-DREAM智慧园区一卡通管理平台API测试集合",
    "version": "1.0.0"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8088",
      "type": "string"
    },
    {
      "key": "token",
      "value": "",
      "type": "string"
    }
  ],
  "item": [
    {
      "name": "认证模块",
      "item": [
        {
          "name": "获取验证码",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/v1/auth/getCaptcha",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ]
          }
        },
        {
          "name": "用户登录",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/v1/auth/login",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"username\": \"admin\",\n  \"password\": \"admin123\",\n  \"captchaKey\": \"{{captchaKey}}\",\n  \"captchaCode\": \"{{captchaCode}}\"\n}"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "if (pm.response.code === 200) {",
                  "  const response = pm.response.json();",
                  "  if (response.code === 200 && response.data && response.data.token) {",
                  "    pm.collectionVariables.set('token', response.data.token);",
                  "    pm.collectionVariables.set('refreshToken', response.data.refreshToken);",
                  "    console.log('Token已保存: ' + response.data.token.substring(0, 20) + '...');",
                  "  }",
                  "}"
                ]
              }
            }
          ]
        }
      ]
    },
    {
      "name": "用户管理",
      "item": [
        {
          "name": "查询用户列表",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/v1/user/query",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"pageNum\": 1,\n  \"pageSize\": 20,\n  \"keyword\": \"\"\n}"
            }
          }
        },
        {
          "name": "获取用户详情",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/v1/user/1",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ]
          }
        }
      ]
    }
  ]
}
```

---

## 📊 自动化测试脚本

### 🤖 Python测试脚本

```python
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IOE-DREAM API自动化测试脚本
"""

import requests
import json
import time
import unittest
from typing import Dict, Any

class IOEDreamAPITest(unittest.TestCase):
    """IOE-DREAM API测试类"""

    def setUp(self):
        """测试初始化"""
        self.base_url = "http://localhost:8088"
        self.session = requests.Session()
        self.token = None

    def tearDown(self):
        """测试清理"""
        self.session.close()

    def test_01_health_check(self):
        """健康检查"""
        response = self.session.get(f"{self.base_url}/actuator/health")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["status"], "UP")
        print("✅ 健康检查通过")

    def test_02_get_captcha(self):
        """获取验证码"""
        response = self.session.get(f"{self.base_url}/api/v1/auth/getCaptcha")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["code"], 200)
        self.assertIn("captchaKey", data["data"])
        self.assertIn("captchaCode", data["data"])

        # 保存验证码信息用于登录测试
        self.captcha_key = data["data"]["captchaKey"]
        self.captcha_code = data["data"]["captchaCode"]
        print("✅ 获取验证码通过")

    def test_03_user_login(self):
        """用户登录测试"""
        if not hasattr(self, 'captcha_key'):
            self.test_02_get_captcha()

        login_data = {
            "username": "admin",
            "password": "admin123",
            "captchaKey": self.captcha_key,
            "captchaCode": self.captcha_code
        }

        response = self.session.post(
            f"{self.base_url}/api/v1/auth/login",
            json=login_data,
            headers={"Content-Type": "application/json"}
        )

        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["code"], 200)
        self.assertIn("token", data["data"])

        # 保存Token用于后续测试
        self.token = data["data"]["token"]
        self.session.headers.update({"Authorization": f"Bearer {self.token}"})
        print("✅ 用户登录通过")

    def test_04_validate_token(self):
        """Token验证测试"""
        if not self.token:
            self.test_03_user_login()

        response = self.session.get(f"{self.base_url}/api/v1/auth/validateToken")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["code"], 200)
        self.assertTrue(data["data"])
        print("✅ Token验证通过")

    def test_05_get_user_info(self):
        """获取用户信息测试"""
        if not self.token:
            self.test_03_user_login()

        response = self.session.get(f"{self.base_url}/api/v1/auth/userInfo")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["code"], 200)
        self.assertIn("username", data["data"])
        self.assertIn("roles", data["data"])
        print("✅ 获取用户信息通过")

    def test_06_query_users(self):
        """查询用户列表测试"""
        if not self.token:
            self.test_03_user_login()

        query_data = {
            "pageNum": 1,
            "pageSize": 10,
            "keyword": ""
        }

        response = self.session.post(
            f"{self.base_url}/api/v1/user/query",
            json=query_data,
            headers={"Content-Type": "application/json"}
        )

        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["code"], 200)
        self.assertIn("list", data["data"])
        self.assertIn("total", data["data"])
        print(f"✅ 查询用户列表通过，共{data['data']['total']}条记录")

    def test_07_logout(self):
        """退出登录测试"""
        if not self.token:
            self.test_03_user_login()

        response = self.session.post(f"{self.base_url}/api/v1/auth/logout")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["code"], 200)
        print("✅ 退出登录通过")

def run_performance_test():
    """性能测试"""
    print("\n🚀 性能测试开始")
    print("=" * 50)

    base_url = "http://localhost:8088"
    session = requests.Session()

    # 测试接口响应时间
    start_time = time.time()
    response = session.get(f"{base_url}/actuator/health")
    response_time = time.time() - start_time

    print(f"健康检查响应时间: {response_time:.3f}s")

    if response_time > 1.0:
        print("⚠️ 响应时间较慢，建议优化")
    else:
        print("✅ 响应时间正常")

    session.close()

if __name__ == "__main__":
    print("🧪 IOE-DREAM API 自动化测试")
    print("=" * 50)

    # 运行单元测试
    unittest.main(argv=[''], exit=False, verbosity=2)

    # 运行性能测试
    run_performance_test()
```

### 🐳 Docker测试环境

```dockerfile
# Dockerfile for API Testing
FROM python:3.9-slim

WORKDIR /app

# 安装依赖
COPY requirements.txt .
RUN pip install -r requirements.txt

# 复制测试脚本
COPY api_test.py .

# 运行测试
CMD ["python", "api_test.py"]
```

```yaml
# docker-compose.yml for API Testing
version: '3.8'

services:
  api-test:
    build: .
    environment:
      - BASE_URL=http://host.docker.internal:8088
    depends_on:
      - ioedream-common-service
    volumes:
      - ./test-reports:/app/reports

  ioedream-common-service:
    image: ioedream-common-service:latest
    ports:
      - "8088:8088"
    environment:
      - SPRING_PROFILES_ACTIVE=test
```

---

## 📋 测试检查清单

### ✅ 功能测试清单

- [ ] 认证接口：获取验证码、登录、退出、Token验证
- [ ] 用户管理：增删改查、权限分配
- [ ] 组织架构：部门管理、区域管理
- [ ] 门禁管理：设备管理、通行记录、权限控制
- [ ] 考勤管理：打卡记录、排班管理、统计分析
- [ ] 消费管理：账户管理、消费记录、充值退款
- [ ] 访客管理：预约登记、访问记录、黑名单
- [ ] 视频监控：设备管理、实时监控、录像回放

### ✅ 性能测试清单

- [ ] 响应时间：所有接口响应时间 < 500ms
- [ ] 并发测试：支持1000+并发请求
- [ ] 稳定性测试：长时间运行无内存泄漏
- [ ] 压力测试：峰值负载下的系统表现

### ✅ 安全测试清单

- [ ] 认证机制：Token有效性验证
- [ ] 权限控制：越权访问防护
- [ ] 参数验证：恶意参数过滤
- [ ] SQL注入：SQL注入攻击防护
- [ ] XSS防护：跨站脚本攻击防护

---

## 📊 测试报告生成

### 📈 自动化测试报告

```python
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试报告生成器
"""

import json
import time
from datetime import datetime
from typing import List, Dict

class TestReportGenerator:
    """测试报告生成器"""

    def __init__(self):
        self.test_results = []
        self.start_time = time.time()

    def add_test_result(self, test_name: str, passed: bool, response_time: float,
                        error_msg: str = None):
        """添加测试结果"""
        self.test_results.append({
            "testName": test_name,
            "passed": passed,
            "responseTime": response_time,
            "errorMsg": error_msg,
            "timestamp": datetime.now().isoformat()
        })

    def generate_report(self) -> Dict:
        """生成测试报告"""
        end_time = time.time()
        total_time = end_time - self.start_time

        passed_count = sum(1 for r in self.test_results if r["passed"])
        failed_count = len(self.test_results) - passed_count

        avg_response_time = sum(r["responseTime"] for r in self.test_results) / len(self.test_results) if self.test_results else 0

        report = {
            "summary": {
                "totalTests": len(self.test_results),
                "passed": passed_count,
                "failed": failed_count,
                "passRate": f"{(passed_count / len(self.test_results) * 100):.1f}%" if self.test_results else "0%",
                "totalTime": f"{total_time:.2f}s",
                "avgResponseTime": f"{avg_response_time:.3f}s"
            },
            "testResults": self.test_results,
            "generatedAt": datetime.now().isoformat()
        }

        return report

    def save_report(self, filename: str = f"test_report_{int(time.time())}.json"):
        """保存测试报告"""
        report = self.generate_report()

        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(report, f, ensure_ascii=False, indent=2)

        print(f"📄 测试报告已保存: {filename}")
        return filename

# 使用示例
if __name__ == "__main__":
    generator = TestReportGenerator()

    # 模拟测试结果
    generator.add_test_result("用户登录", True, 0.234)
    generator.add_test_result("查询用户", True, 0.156)
    generator.add_test_result("添加用户", False, 0.345, "用户名已存在")

    # 生成报告
    report_file = generator.save_report()
    print(f"✅ 测试完成，报告已生成: {report_file}")
```

---

## 🔧 故障排除

### 🚨 常见问题及解决方案

#### 1. 服务启动失败

```bash
# 检查端口占用
netstat -tulpn | grep :8088

# 检查服务状态
curl -s http://localhost:8088/actuator/health

# 查看服务日志
docker logs ioedream-common-service
```

#### 2. API请求失败

```bash
# 检查网络连接
ping localhost

# 检查端口可达性
telnet localhost 8088

# 检查防火墙设置
sudo ufw status
```

#### 3. 认证失败

```bash
# 验证Token格式
echo "Bearer your_token_here" | cut -d' ' -f2 | wc -c

# 检查Token过期时间
echo "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" | base64 -d | jq .
```

---

**📚 文档维护**: 本文档将随着API的更新持续维护
**🔄 最后更新**: 2025-12-20 22:00
**📧 联系方式**: api-support@ioedream.com