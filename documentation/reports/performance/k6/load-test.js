import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// 自定义指标
export let errorRate = new Rate('errors');

// 测试配置
export let options = {
  stages: [
    // 预热阶段
    { duration: '2m', target: 50 },
    // 正常负载
    { duration: '5m', target: 100 },
    // 高负载
    { duration: '10m', target: 500 },
    // 峰值负载
    { duration: '5m', target: 1000 },
    // 降负载
    { duration: '5m', target: 200 },
    // 恢复正常
    { duration: '3m', target: 50 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95%的请求响应时间小于500ms
    http_req_duration: ['p(99)<1000'], // 99%的请求响应时间小于1秒
    errors: ['rate<0.1'], // 错误率小于10%
  },
};

const BASE_URL = 'http://localhost:8100';

// 测试数据
const users = [
  { username: 'admin', password: 'admin123' },
  { username: 'user1', password: 'password1' },
  { username: 'user2', password: 'password2' },
];

// 获取认证令牌
function getAuthToken() {
  const user = users[Math.floor(Math.random() * users.length)];
  const response = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    username: user.username,
    password: user.password
  }), {
    headers: { 'Content-Type': 'application/json' },
  });

  const success = check(response, {
    '登录成功': (r) => r.status === 200,
    '返回token': (r) => JSON.parse(r.body).data.accessToken !== undefined,
  });

  errorRate.add(!success);

  if (success) {
    return JSON.parse(response.body).data.accessToken;
  }
  return null;
}

export function setup() {
  console.log('压力测试开始...');
  return { token: getAuthToken() };
}

export default function(data) {
  let token = data.token;

  // 重新获取token（如果需要）
  if (Math.random() < 0.1) { // 10%概率重新获取token
    token = getAuthToken();
    data.token = token;
  }

  if (!token) {
    console.error('无法获取认证令牌');
    return;
  }

  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  // 测试不同接口
  const scenarios = [
    // 认证服务测试
    () => {
      const response = http.get(`${BASE_URL}/api/auth/userinfo`, { headers });
      return check(response, {
        '用户信息获取成功': (r) => r.status === 200,
      });
    },

    // 消费服务测试
    () => {
      const response = http.get(`${BASE_URL}/api/consume/account/1`, { headers });
      return check(response, {
        '账户信息获取成功': (r) => r.status === 200 || r.status === 404,
      });
    },

    // 设备服务测试
    () => {
      const response = http.get(`${BASE_URL}/api/device/list`, { headers });
      return check(response, {
        '设备列表获取成功': (r) => r.status === 200,
      });
    },

    // 访客服务测试
    () => {
      const response = http.get(`${BASE_URL}/api/visitor/list`, { headers });
      return check(response, {
        '访客列表获取成功': (r) => r.status === 200,
      });
    },

    // 健康检查
    () => {
      const response = http.get(`${BASE_URL}/actuator/health`);
      return check(response, {
        '健康检查成功': (r) => r.status === 200,
      });
    },
  ];

  // 随机选择测试场景
  const scenario = scenarios[Math.floor(Math.random() * scenarios.length)];
  const success = scenario();

  errorRate.add(!success);
  sleep(1);
}

export function teardown(data) {
  console.log('压力测试完成');
}