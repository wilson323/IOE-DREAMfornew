import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export let errorRate = new Rate('errors');

export let options = {
  stages: [
    { duration: '1m', target: 20 },
    { duration: '3m', target: 50 },
    { duration: '5m', target: 100 },
    { duration: '2m', target: 200 },
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'],
    http_req_duration: ['p(99)<500'],
    errors: ['rate<0.05'],
  },
};

const BASE_URL = 'http://localhost:8100';

// API测试用例
export default function() {
  // 1. 登录测试
  const loginResponse = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    username: 'admin',
    password: 'admin123'
  }), {
    headers: { 'Content-Type': 'application/json' },
  });

  const loginSuccess = check(loginResponse, {
    '登录状态码为200': (r) => r.status === 200,
    '登录响应时间<500ms': (r) => r.timings.duration < 500,
    '返回token': (r) => JSON.parse(r.body).success === true,
  });

  errorRate.add(!loginSuccess);

  if (loginSuccess) {
    const token = JSON.parse(loginResponse.body).data.accessToken;
    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    };

    // 2. 用户信息测试
    const userResponse = http.get(`${BASE_URL}/api/auth/userinfo`, { headers });
    check(userResponse, {
      '用户信息获取成功': (r) => r.status === 200,
      '用户信息响应时间<300ms': (r) => r.timings.duration < 300,
    });

    // 3. 设备列表测试
    const deviceResponse = http.get(`${BASE_URL}/api/device/list`, { headers });
    check(deviceResponse, {
      '设备列表获取成功': (r) => r.status === 200,
      '设备列表响应时间<400ms': (r) => r.timings.duration < 400,
    });

    // 4. 消费记录测试
    const consumeResponse = http.get(`${BASE_URL}/api/consume/record/list?pageNum=1&pageSize=10`, { headers });
    check(consumeResponse, {
      '消费记录获取成功': (r) => r.status === 200,
      '消费记录响应时间<600ms': (r) => r.timings.duration < 600,
    });

    // 5. 访客列表测试
    const visitorResponse = http.get(`${BASE_URL}/api/visitor/list`, { headers });
    check(visitorResponse, {
      '访客列表获取成功': (r) => r.status === 200,
      '访客列表响应时间<400ms': (r) => r.timings.duration < 400,
    });
  }

  sleep(1);
}