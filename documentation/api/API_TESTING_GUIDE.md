# API测试指南

## 📋 测试概述

本文档提供IOE-DREAM系统API接口的完整测试指南，包括环境准备、测试用例、自动化测试和性能测试。

---

## 🌍 测试环境

### 环境配置

| 环境 | 地址 | 描述 | 用途 |
|------|------|------|------|
| 开发环境 | http://localhost:8080 | 本地开发环境 | 功能测试 |
| 测试环境 | https://test.ioe-dream.com | 内部测试环境 | 集成测试 |
| 预生产环境 | https://staging.ioe-dream.com | 生产前验证 | UAT测试 |
| 生产环境 | https://api.ioe-dream.com | 正式生产环境 | 监控测试 |

### 环境变量

```bash
# 开发环境
export API_BASE_URL=http://localhost:8080
export JWT_TOKEN=dev_jwt_token_here

# 测试环境
export API_BASE_URL=https://test.ioe-dream.com
export JWT_TOKEN=test_jwt_token_here

# 生产环境
export API_BASE_URL=https://api.ioe-dream.com
export JWT_TOKEN=prod_jwt_token_here
```

---

## 🧪 测试工具

### 1. Postman测试集合

#### 安装配置
```bash
# 下载Postman
https://www.postman.com/downloads/

# 导入测试集合
# 文件: documentation/api/postman/IOE-DREAM_API_Collection.json
```

#### 环境变量配置

```json
{
  "name": "IOE-DREAM API Environment",
  "values": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080",
      "enabled": true
    },
    {
      "key": "jwtToken",
      "value": "",
      "enabled": true
    },
    {
      "key": "contentType",
      "value": "application/json",
      "enabled": true
    }
  ]
}
```

### 2. cURL测试命令

#### 基础配置
```bash
# 设置公共头
HEADER_AUTH="Authorization: Bearer ${JWT_TOKEN}"
HEADER_CONTENT="Content-Type: application/json"
HEADER_ACCEPT="Accept: application/json"

# 基础请求函数
api_request() {
    local method=$1
    local endpoint=$2
    local data=$3

    curl -X ${method} \
         "${API_BASE_URL}${endpoint}" \
         -H "${HEADER_AUTH}" \
         -H "${HEADER_CONTENT}" \
         -H "${HEADER_ACCEPT}" \
         ${data:+-d "${data}"} \
         -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\n"
}
```

### 3. Python测试脚本

```python
# requirements.txt
# requests==2.28.2
# pytest==7.2.0
# pytest-html==3.1.1
# jsonschema==4.17.3

import requests
import pytest
import json
from typing import Dict, Any

class IOEDreamAPIClient:
    def __init__(self, base_url: str, jwt_token: str = None):
        self.base_url = base_url
        self.session = requests.Session()
        if jwt_token:
            self.session.headers.update({
                'Authorization': f'Bearer {jwt_token}',
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            })

    def login(self, username: str, password: str) -> Dict[str, Any]:
        """用户登录"""
        response = self.session.post(
            f"{self.base_url}/api/v1/auth/login",
            json={
                "username": username,
                "password": password
            }
        )
        response.raise_for_status()
        data = response.json()
        if data['code'] == 200:
            token = data['data']['token']
            self.session.headers['Authorization'] = f'Bearer {token}'
        return data

    def get(self, endpoint: str, params: Dict = None) -> Dict[str, Any]:
        """GET请求"""
        response = self.session.get(
            f"{self.base_url}{endpoint}",
            params=params
        )
        response.raise_for_status()
        return response.json()

    def post(self, endpoint: str, data: Dict = None) -> Dict[str, Any]:
        """POST请求"""
        response = self.session.post(
            f"{self.base_url}{endpoint}",
            json=data
        )
        response.raise_for_status()
        return response.json()
```

---

## 🧪 核心功能测试

### 1. 认证授权测试

#### 1.1 用户登录测试

```python
def test_user_login():
    client = IOEDreamAPIClient(API_BASE_URL)

    # 测试正常登录
    response = client.login("admin", "admin123")
    assert response['code'] == 200
    assert 'token' in response['data']
    assert 'refreshToken' in response['data']

    # 测试错误密码
    with pytest.raises(requests.HTTPError):
        client.login("admin", "wrong_password")

    # 测试不存在的用户
    with pytest.raises(requests.HTTPError):
        client.login("nonexistent", "password")
```

#### 1.2 Token刷新测试

```python
def test_token_refresh():
    client = IOEDreamAPIClient(API_BASE_URL)
    login_response = client.login("admin", "admin123")
    refresh_token = login_response['data']['refreshToken']

    # 使用refresh_token获取新token
    response = client.session.post(
        f"{client.base_url}/api/v1/auth/refresh",
        json={"refreshToken": refresh_token}
    )

    data = response.json()
    assert data['code'] == 200
    assert 'token' in data['data']
```

### 2. 用户管理测试

#### 2.1 用户查询测试

```python
def test_user_query():
    client = IOEDreamAPIClient(API_BASE_URL, JWT_TOKEN)

    # 查询用户列表
    response = client.get("/api/v1/users/query", {
        "pageNum": 1,
        "pageSize": 20,
        "username": "admin"
    })

    assert response['code'] == 200
    assert 'list' in response['data']
    assert 'total' in response['data']

    # 查询用户详情
    user_id = response['data']['list'][0]['userId']
    detail_response = client.get(f"/api/v1/users/{user_id}")

    assert detail_response['code'] == 200
    assert detail_response['data']['userId'] == user_id
```

#### 2.2 用户创建测试

```python
def test_user_create():
    client = IOEDreamAPIClient(API_BASE_URL, JWT_TOKEN)

    user_data = {
        "username": "testuser_" + str(int(time.time())),
        "realName": "测试用户",
        "email": "test@example.com",
        "phone": "13800138000",
        "gender": 1,
        "deptId": 100,
        "status": 1,
        "roleIds": [2]
    }

    response = client.post("/api/v1/users/add", user_data)
    assert response['code'] == 200
    assert isinstance(response['data'], int)  # 返回用户ID
```

### 3. 门禁管理测试

#### 3.1 门禁设备测试

```python
def test_access_devices():
    client = IOEDreamAPIClient(API_BASE_URL, JWT_TOKEN)

    # 查询设备列表
    response = client.get("/api/v1/access/devices/query", {
        "pageNum": 1,
        "pageSize": 10
    })

    assert response['code'] == 200
    assert isinstance(response['data']['list'], list)

    # 如果有设备，测试设备详情
    if response['data']['list']:
        device_id = response['data']['list'][0]['deviceId']
        detail_response = client.get(f"/api/v1/access/devices/{device_id}")
        assert detail_response['code'] == 200
```

#### 3.2 门禁记录测试

```python
def test_access_records():
    client = IOEDreamAPIClient(API_BASE_URL, JWT_TOKEN)

    # 查询门禁记录
    response = client.get("/api/v1/access/records/query", {
        "startDate": "2025-12-21",
        "endDate": "2025-12-21",
        "pageNum": 1,
        "pageSize": 20
    })

    assert response['code'] == 200
    assert 'list' in response['data']
```

### 4. 考勤管理测试

#### 4.1 考勤打卡测试

```python
def test_attendance_clock():
    client = IOEDreamAPIClient(API_BASE_URL, JWT_TOKEN)

    # 模拟打卡请求
    clock_data = {
        "userId": 1001,
        "deviceId": "ATT001",
        "location": "A栋办公室",
        "attendanceType": "CLOCK_IN",
        "photo": "base64_photo_data_here",
        "gpsLocation": {
            "latitude": 39.9042,
            "longitude": 116.4074
        }
    }

    response = client.post("/api/v1/attendance/records/clock", clock_data)
    assert response['code'] == 200
```

#### 4.2 考勤统计测试

```python
def test_attendance_statistics():
    client = IOEDreamAPIClient(API_BASE_URL, JWT_TOKEN)

    # 查询考勤统计
    response = client.get("/api/v1/attendance/statistics", {
        "startDate": "2025-12-01",
        "endDate": "2025-12-31",
        "userId": 1001
    })

    assert response['code'] == 200
    assert 'workDays' in response['data']
    assert 'actualDays' in response['data']
```

### 5. 消费管理测试

#### 5.1 账户管理测试

```python
def test_consume_account():
    client = IOEDreamAPIClient(API_BASE_URL, JWT_TOKEN)

    # 查询账户列表
    response = client.get("/api/v1/consume/accounts/query", {
        "pageNum": 1,
        "pageSize": 20
    })

    assert response['code'] == 200
    assert isinstance(response['data']['list'], list)

    # 查询账户余额
    if response['data']['list']:
        account_id = response['data']['list'][0]['accountId']
        balance_response = client.get(f"/api/v1/consume/accounts/{account_id}/balance")
        assert balance_response['code'] == 200
        assert 'balance' in balance_response['data']
```

#### 5.2 消费记录测试

```python
def test_consume_records():
    client = IOEDreamAPIClient(API_BASE_URL, JWT_TOKEN)

    # 创建消费记录
    consume_data = {
        "accountId": 1001,
        "userId": 1001,
        "deviceId": "POS001",
        "merchantId": 2001,
        "amount": 25.50,
        "consumeType": "MEAL",
        "paymentMethod": "BALANCE",
        "consumeLocation": "一楼餐厅"
    }

    response = client.post("/api/v1/consume/records/add", consume_data)
    assert response['code'] == 200

    # 查询消费记录
    query_response = client.get("/api/v1/consume/records/query", {
        "pageNum": 1,
        "pageSize": 20,
        "userId": 1001
    })

    assert query_response['code'] == 200
```

### 6. 视频监控测试

#### 6.1 视频设备测试

```python
def test_video_devices():
    client = IOEDreamAPIClient(API_BASE_URL, JWT_TOKEN)

    # 查询视频设备
    response = client.get("/api/v1/video/devices/query", {
        "pageNum": 1,
        "pageSize": 20
    })

    assert response['code'] == 200
    assert isinstance(response['data']['list'], list)
```

---

## 🚀 自动化测试

### 1. pytest测试套件

#### 测试配置文件

```python
# conftest.py
import pytest
import os
from ioedream_api_client import IOEDreamAPIClient

@pytest.fixture(scope="session")
def api_client():
    base_url = os.getenv("API_BASE_URL", "http://localhost:8080")
    username = os.getenv("API_USERNAME", "admin")
    password = os.getenv("API_PASSWORD", "admin123")

    client = IOEDreamAPIClient(base_url)
    client.login(username, password)

    yield client

@pytest.fixture
def test_user_data():
    return {
        "username": f"testuser_{int(time.time())}",
        "realName": "测试用户",
        "email": "test@example.com",
        "phone": "13800138000",
        "gender": 1,
        "deptId": 100,
        "status": 1,
        "roleIds": [2]
    }
```

#### 测试用例组织

```python
# test_auth.py
def test_login_success(api_client):
    response = api_client.login("admin", "admin123")
    assert response['code'] == 200
    assert 'token' in response['data']

def test_login_failure(api_client):
    with pytest.raises(requests.HTTPError):
        api_client.login("admin", "wrong_password")

# test_users.py
class TestUserManagement:
    def test_create_user(self, api_client, test_user_data):
        response = api_client.post("/api/v1/users/add", test_user_data)
        assert response['code'] == 200
        user_id = response['data']
        assert isinstance(user_id, int)

    def test_get_user_list(self, api_client):
        response = api_client.get("/api/v1/users/query")
        assert response['code'] == 200
        assert 'list' in response['data']

    def test_get_user_detail(self, api_client):
        response = api_client.get("/api/v1/users/query")
        if response['data']['list']:
            user_id = response['data']['list'][0]['userId']
            detail_response = api_client.get(f"/api/v1/users/{user_id}")
            assert detail_response['code'] == 200
```

### 2. 运行测试

```bash
# 安装依赖
pip install -r requirements.txt

# 运行所有测试
pytest

# 运行特定测试文件
pytest test_users.py

# 运行特定测试类
pytest test_users.py::TestUserManagement

# 生成HTML测试报告
pytest --html=report.html --self-contained-html

# 生成覆盖率报告
pytest --cov=tests --cov-report=html

# 并行执行测试
pytest -n auto
```

### 3. CI/CD集成

#### GitHub Actions配置

```yaml
# .github/workflows/api-tests.yml
name: API Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  api-tests:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up Python
      uses: actions/setup-python@v4
      with:
        python-version: '3.9'

    - name: Install dependencies
      run: |
        pip install -r requirements.txt

    - name: Run API Tests
      env:
        API_BASE_URL: ${{ secrets.TEST_API_URL }}
        JWT_TOKEN: ${{ secrets.TEST_JWT_TOKEN }}
      run: |
        pytest --html=report.html --self-contained-html

    - name: Upload test report
      uses: actions/upload-artifact@v3
      with:
        name: test-report
        path: report.html
```

---

## 📊 性能测试

### 1. 基准测试

#### 使用Apache Bench

```bash
# 并发测试
ab -n 1000 -c 10 -H "Authorization: Bearer ${JWT_TOKEN}" \
   ${API_BASE_URL}/api/v1/users/query

# 压力测试
ab -n 10000 -c 50 -H "Authorization: Bearer ${JWT_TOKEN}" \
   ${API_BASE_URL}/api/v1/consume/records/query
```

#### 使用JMeter

```xml
<!-- JMeter测试计划配置 -->
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.5">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="IOE-DREAM API测试">
      <elementProp name="TestPlan.arguments">
        <collectionProp name="Arguments.arguments">
          <elementProp name="API_BASE_URL" elementType="Argument" testname="API_BASE_URL">
            <stringProp name="Argument.value">http://localhost:8080</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
  </hashTree>
</jmeterTestPlan>
```

### 2. 性能指标

#### 响应时间基准

| 接口类型 | 目标响应时间 | 最大响应时间 | 并发用户数 |
|---------|-------------|-------------|-----------|
| 查询接口 | <200ms | <500ms | 100 |
| 创建接口 | <500ms | <1000ms | 50 |
| 更新接口 | <300ms | <800ms | 50 |
| 批量操作 | <1000ms | <2000ms | 20 |

#### 资源使用率

| 资源类型 | 正常范围 | 告警阈值 | 临界阈值 |
|---------|---------|---------|---------|
| CPU使用率 | <70% | 80% | 90% |
| 内存使用率 | <75% | 85% | 95% |
| 磁盘使用率 | <80% | 90% | 95% |
| 网络带宽 | <70% | 85% | 95% |

### 3. 性能监控脚本

```python
# performance_monitor.py
import time
import requests
import statistics
from typing import List

class APITester:
    def __init__(self, base_url: str, jwt_token: str):
        self.base_url = base_url
        self.headers = {
            'Authorization': f'Bearer {jwt_token}',
            'Content-Type': 'application/json'
        }

    def measure_response_time(self, endpoint: str, iterations: int = 100) -> List[float]:
        """测量接口响应时间"""
        times = []

        for _ in range(iterations):
            start_time = time.time()
            try:
                response = requests.get(f"{self.base_url}{endpoint}", headers=self.headers)
                if response.status_code == 200:
                    times.append((time.time() - start_time) * 1000)  # 转换为毫秒
            except requests.RequestException:
                times.append(-1)  # 标记失败

        return [t for t in times if t > 0]

    def run_performance_test(self, endpoint: str, concurrency: int = 10, iterations: int = 100):
        """运行性能测试"""
        import threading
        import queue

        results = queue.Queue()

        def worker():
            times = self.measure_response_time(endpoint, iterations // concurrency)
            for t in times:
                results.put(t)

        threads = []
        for _ in range(concurrency):
            thread = threading.Thread(target=worker)
            thread.start()
            threads.append(thread)

        for thread in threads:
            thread.join()

        all_times = []
        while not results.empty():
            all_times.append(results.get())

        if all_times:
            avg_time = statistics.mean(all_times)
            min_time = min(all_times)
            max_time = max(all_times)
            p95_time = sorted(all_times)[int(len(all_times) * 0.95)]

            print(f"性能测试结果: {endpoint}")
            print(f"平均响应时间: {avg_time:.2f}ms")
            print(f"最小响应时间: {min_time:.2f}ms")
            print(f"最大响应时间: {max_time:.2f}ms")
            print(f"95%响应时间: {p95_time:.2f}ms")
            print(f"总请求数: {len(all_times)}")

        return all_times

# 使用示例
if __name__ == "__main__":
    tester = APITester("http://localhost:8080", "your_jwt_token")
    tester.run_performance_test("/api/v1/users/query", concurrency=10, iterations=1000)
```

---

## 🐛 错误处理测试

### 1. 异常场景测试

```python
def test_error_scenarios():
    client = IOEDreamAPIClient(API_BASE_URL, JWT_TOKEN)

    # 测试400错误 - 参数错误
    try:
        response = client.post("/api/v1/users/add", {})
        assert False, "应该抛出400错误"
    except requests.HTTPError as e:
        assert e.response.status_code == 400

    # 测试401错误 - 未认证
    unauthorized_client = IOEDreamAPIClient(API_BASE_URL)
    try:
        response = unauthorized_client.get("/api/v1/users/query")
        assert False, "应该抛出401错误"
    except requests.HTTPError as e:
        assert e.response.status_code == 401

    # 测试403错误 - 无权限
    try:
        response = client.post("/api/v1/system/config/update", {})
        assert False, "应该抛出403错误"
    except requests.HTTPError as e:
        assert e.response.status_code == 403

    # 测试404错误 - 资源不存在
    try:
        response = client.get("/api/v1/users/99999")
        assert False, "应该抛出404错误"
    except requests.HTTPError as e:
        assert e.response.status_code == 404

    # 测试500错误 - 服务器内部错误
    # 需要构造特定场景
```

### 2. 数据验证测试

```python
def test_data_validation():
    client = IOEDreamAPIClient(API_BASE_URL, JWT_TOKEN)

    # 测试邮箱格式验证
    invalid_email_data = {
        "username": "testuser",
        "email": "invalid_email_format",
        "realName": "测试用户"
    }

    try:
        response = client.post("/api/v1/users/add", invalid_email_data)
        assert False, "应该抛出验证错误"
    except requests.HTTPError as e:
        response_data = e.response.json()
        assert e.response.status_code == 400
        assert "email" in response_data["message"]

    # 测试手机号格式验证
    invalid_phone_data = {
        "username": "testuser",
        "phone": "invalid_phone",
        "realName": "测试用户"
    }

    try:
        response = client.post("/api/v1/users/add", invalid_phone_data)
        assert False, "应该抛出验证错误"
    except requests.HTTPError as e:
        response_data = e.response.json()
        assert e.response.status_code == 400
        assert "phone" in response_data["message"]
```

---

## 📋 测试报告

### 1. 测试结果汇总

```python
# test_report_generator.py
from datetime import datetime
import json

class TestReportGenerator:
    def __init__(self):
        self.results = {}
        self.start_time = datetime.now()

    def add_test_result(self, test_name: str, passed: bool, duration: float, error: str = None):
        self.results[test_name] = {
            "passed": passed,
            "duration": duration,
            "error": error
        }

    def generate_report(self, output_file: str = "test_report.json"):
        end_time = datetime.now()
        total_duration = (end_time - self.start_time).total_seconds()

        passed_tests = sum(1 for r in self.results.values() if r["passed"])
        total_tests = len(self.results)

        report = {
            "summary": {
                "total_tests": total_tests,
                "passed_tests": passed_tests,
                "failed_tests": total_tests - passed_tests,
                "pass_rate": f"{(passed_tests/total_tests*100):.1f}%" if total_tests > 0 else "0%",
                "total_duration": f"{total_duration:.2f}s",
                "start_time": self.start_time.isoformat(),
                "end_time": end_time.isoformat()
            },
            "results": self.results
        }

        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(report, f, indent=2, ensure_ascii=False)

        return report
```

### 2. HTML报告生成

```python
# html_report_generator.py
def generate_html_report(test_data: dict, output_file: str = "test_report.html"):
    html_template = """
    <!DOCTYPE html>
    <html>
    <head>
        <title>IOE-DREAM API测试报告</title>
        <style>
            body {{ font-family: Arial, sans-serif; margin: 20px; }}
            .header {{ background-color: #f0f0f0; padding: 20px; border-radius: 5px; }}
            .summary {{ margin: 20px 0; }}
            .test-result {{ margin: 10px 0; padding: 10px; border-left: 4px solid; }}
            .passed {{ border-color: #28a745; background-color: #d4edda; }}
            .failed {{ border-color: #dc3545; background-color: #f8d7da; }}
        </style>
    </head>
    <body>
        <div class="header">
            <h1>IOE-DREAM API测试报告</h1>
            <div class="summary">
                <h2>测试概要</h2>
                <p>总测试数: {total_tests}</p>
                <p>通过数: {passed_tests}</p>
                <p>失败数: {failed_tests}</p>
                <p>通过率: {pass_rate}</p>
                <p>总耗时: {total_duration}</p>
            </div>
        </div>
        <h2>测试详情</h2>
        {test_results}
    </body>
    </html>
    """

    # 生成测试结果HTML
    test_results_html = ""
    for test_name, result in test_data["results"].items():
        css_class = "passed" if result["passed"] else "failed"
        error_info = f"<p>错误: {result['error']}</p>" if result["error"] else ""

        test_results_html += f"""
        <div class="test-result {css_class}">
            <h3>{test_name}</h3>
            <p>耗时: {result['duration']:.2f}s</p>
            <p>状态: {'通过' if result['passed'] else '失败'}</p>
            {error_info}
        </div>
        """

    html_content = html_template.format(
        total_tests=test_data["summary"]["total_tests"],
        passed_tests=test_data["summary"]["passed_tests"],
        failed_tests=test_data["summary"]["failed_tests"],
        pass_rate=test_data["summary"]["pass_rate"],
        total_duration=test_data["summary"]["total_duration"],
        test_results=test_results_html
    )

    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(html_content)
```

---

## 🔧 持续集成

### 1. 测试环境自动化

```yaml
# docker-compose.test.yml
version: '3.8'
services:
  test-runner:
    build:
      context: .
      dockerfile: Dockerfile.test
    environment:
      - API_BASE_URL=http://api-server:8080
      - API_USERNAME=admin
      - API_PASSWORD=admin123
    depends_on:
      - api-server
      - database
      - redis
    volumes:
      - ./tests:/app/tests
      - ./reports:/app/reports

  api-server:
    image: ioe-dream/api:latest
    environment:
      - SPRING_PROFILES_ACTIVE=test
      - DATABASE_URL=jdbc:postgresql://database:5432/test
      - REDIS_URL=redis://redis:6379
    depends_on:
      - database
      - redis
```

### 2. 测试脚本

```bash
#!/bin/bash
# run_tests.sh

set -e

echo "🚀 开始API测试..."

# 环境检查
echo "📋 检查测试环境..."
curl -f ${API_BASE_URL}/health || {
    echo "❌ API服务不可用"
    exit 1
}

echo "✅ 环境检查通过"

# 登录获取token
echo "🔐 获取认证Token..."
TOKEN=$(curl -s -X POST "${API_BASE_URL}/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}' | \
    jq -r '.data.token')

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
    echo "❌ 获取Token失败"
    exit 1
fi

echo "✅ Token获取成功"

# 运行测试
echo "🧪 执行API测试..."
pytest tests/ --html=reports/report.html --self-contained-html \
    --api-base-url=${API_BASE_URL} \
    --jwt-token=${TOKEN}

echo "📊 测试完成，报告已生成"
echo "📄 查看报告: reports/report.html"
```

---

**文档维护**: IOE-DREAM技术团队
**最后更新**: 2025-12-21
**版本**: v2.0.0

🎉 **完整的API测试指南，涵盖功能测试、性能测试、自动化测试和持续集成，确保IOE-DREAM系统API质量和稳定性！**