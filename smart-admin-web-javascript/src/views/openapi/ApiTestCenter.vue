<template>
  <div class="api-test-center">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <ExperimentOutlined class="title-icon" />
          API在线测试中心
        </h1>
        <p class="page-description">
          在线测试和调试开放平台API接口
        </p>
      </div>
      <div class="header-actions">
        <a-button type="primary" @click="saveTestCase">
          <SaveOutlined />
          保存用例
        </a-button>
        <a-button @click="loadTestSuite">
          <FolderOpenOutlined />
          加载测试集
        </a-button>
      </div>
    </div>

    <a-row :gutter="24">
      <!-- 左侧：API选择和配置 -->
      <a-col :span="8">
        <a-card title="API接口选择" class="api-selector-card">
          <!-- 服务选择 -->
          <a-form layout="vertical">
            <a-form-item label="选择微服务">
              <a-select
                v-model:value="selectedService"
                placeholder="请选择微服务"
                @change="handleServiceChange"
              >
                <a-select-option
                  v-for="service in serviceList"
                  :key="service.id"
                  :value="service.id"
                >
                  <ClusterOutlined />
                  {{ service.name }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="API接口">
              <a-select
                v-model:value="selectedApi"
                placeholder="请选择API接口"
                @change="handleApiChange"
                :loading="apiLoading"
              >
                <a-select-option
                  v-for="api in apiList"
                  :key="api.path"
                  :value="api.path"
                >
                  <ApiOutlined />
                  {{ api.summary }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="请求方法">
              <a-tag :color="getMethodColor(apiInfo.method)">
                {{ apiInfo.method }}
              </a-tag>
              <span class="api-path">{{ apiInfo.path }}</span>
            </a-form-item>
          </a-form>

          <!-- 快速测试用例 -->
          <a-divider>快速测试用例</a-divider>
          <a-list :data-source="quickTestCases" size="small">
            <template #renderItem="{ item }">
              <a-list-item
                :class="{ active: selectedTestCase?.id === item.id }"
                @click="loadTestCase(item)"
              >
                <a-list-item-meta>
                  <template #title>{{ item.name }}</template>
                  <template #description>{{ item.description }}</template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>

        <!-- 参数配置 -->
        <a-card title="请求参数配置" class="params-config-card" style="margin-top: 16px">
          <a-tabs v-model:activeKey="paramTab">
            <!-- 路径参数 -->
            <a-tab-pane key="path" tab="路径参数">
              <div v-if="apiInfo.parameters?.path?.length">
                <a-form
                  v-for="(param, index) in apiInfo.parameters.path"
                  :key="index"
                  layout="vertical"
                >
                  <a-form-item :label="param.name" :required="param.required">
                    <a-input
                      v-model:value="pathParams[param.name]"
                      :placeholder="`请输入${param.description || param.name}`"
                    />
                    <div class="param-desc">{{ param.description }}</div>
                  </a-form-item>
                </a-form>
              </div>
              <a-empty v-else description="无路径参数" />
            </a-tab-pane>

            <!-- 查询参数 -->
            <a-tab-pane key="query" tab="查询参数">
              <div v-if="apiInfo.parameters?.query?.length">
                <a-form
                  v-for="(param, index) in apiInfo.parameters.query"
                  :key="index"
                  layout="vertical"
                >
                  <a-form-item :label="param.name" :required="param.required">
                    <a-input
                      v-model:value="queryParams[param.name]"
                      :placeholder="`请输入${param.description || param.name}`"
                    />
                    <div class="param-desc">{{ param.description }}</div>
                  </a-form-item>
                </a-form>
              </div>
              <a-empty v-else description="无查询参数" />
            </a-tab-pane>

            <!-- 请求体 -->
            <a-tab-pane key="body" tab="请求体" v-if="apiInfo.method !== 'GET'">
              <div v-if="apiInfo.requestBody">
                <a-form-item label="请求格式">
                  <a-radio-group v-model:value="bodyType" @change="handleBodyTypeChange">
                    <a-radio value="form">表单</a-radio>
                    <a-radio value="json">JSON</a-radio>
                    <a-radio value="raw">原始</a-radio>
                  </a-radio-group>
                </a-form-item>

                <!-- 表单格式 -->
                <div v-if="bodyType === 'form'">
                  <a-form
                    v-for="(field, index) in formFields"
                    :key="index"
                    layout="vertical"
                  >
                    <a-form-item :label="field.name">
                      <a-input
                        v-model:value="field.value"
                        :placeholder="field.description"
                      />
                    </a-form-item>
                  </a-form>
                </div>

                <!-- JSON格式 -->
                <div v-else-if="bodyType === 'json'">
                  <a-textarea
                    v-model:value="jsonBody"
                    :rows="12"
                    placeholder="请输入JSON格式的请求体"
                  />
                  <div class="json-tools">
                    <a-button size="small" @click="formatJson">格式化</a-button>
                    <a-button size="small" @click="validateJson">验证</a-button>
                  </div>
                </div>

                <!-- 原始格式 -->
                <div v-else>
                  <a-textarea
                    v-model:value="rawBody"
                    :rows="12"
                    placeholder="请输入原始请求体"
                  />
                </div>
              </div>
              <a-empty v-else description="无请求体" />
            </a-tab-pane>

            <!-- 请求头 -->
            <a-tab-pane key="headers" tab="请求头">
              <div class="headers-config">
                <div
                  v-for="(header, index) in headers"
                  :key="index"
                  class="header-item"
                >
                  <a-input
                    v-model:value="header.key"
                    placeholder="Header名称"
                    style="width: 45%"
                  />
                  <span style="margin: 0 8px">:</span>
                  <a-input
                    v-model:value="header.value"
                    placeholder="Header值"
                    style="width: 45%"
                  />
                  <a-button
                    type="text"
                    danger
                    size="small"
                    @click="removeHeader(index)"
                  >
                    <DeleteOutlined />
                  </a-button>
                </div>
                <a-button type="dashed" block @click="addHeader">
                  <PlusOutlined />
                  添加请求头
                </a-button>
              </div>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>

      <!-- 右侧：请求发送和响应展示 -->
      <a-col :span="16">
        <a-card title="API测试" class="api-test-card">
          <!-- 请求URL构建 -->
          <div class="request-url-builder">
            <a-input-group compact>
              <a-select
                v-model:value="requestMethod"
                style="width: 100px"
                :options="methodOptions"
              />
              <a-input
                v-model:value="requestUrl"
                style="flex: 1"
                readonly
                placeholder="API请求URL"
              />
              <a-button type="primary" :loading="sending" @click="sendRequest">
                <SendOutlined />
                发送请求
              </a-button>
            </a-input-group>
          </div>

          <!-- 响应结果 -->
          <div class="response-section">
            <a-tabs v-model:activeKey="responseTab">
              <a-tab-pane key="response" tab="响应结果">
                <div class="response-info">
                  <a-row :gutter="16">
                    <a-col :span="6">
                      <a-statistic
                        title="状态码"
                        :value="responseStatus"
                        :value-style="getStatusColor(responseStatus)"
                      />
                    </a-col>
                    <a-col :span="6">
                      <a-statistic
                        title="响应时间"
                        :value="responseTime"
                        suffix="ms"
                      />
                    </a-col>
                    <a-col :span="6">
                      <a-statistic
                        title="响应大小"
                        :value="responseSize"
                        suffix="B"
                      />
                    </a-col>
                    <a-col :span="6">
                      <a-statistic
                        title="请求时间"
                        :value="requestTime"
                        :value-style="{ fontSize: '14px' }"
                      />
                    </a-col>
                  </a-row>
                </div>

                <!-- 响应头 -->
                <div v-if="responseHeaders" class="response-headers">
                  <h4>响应头</h4>
                  <a-descriptions bordered size="small">
                    <a-descriptions-item
                      v-for="(value, key) in responseHeaders"
                      :key="key"
                      :label="key"
                    >
                      {{ value }}
                    </a-descriptions-item>
                  </a-descriptions>
                </div>

                <!-- 响应体 -->
                <div class="response-body">
                  <h4>响应体</h4>
                  <div class="body-actions">
                    <a-space>
                      <a-button size="small" @click="copyResponse">复制</a-button>
                      <a-button size="small" @click="downloadResponse">下载</a-button>
                      <a-button size="small" @click="formatResponseBody">格式化</a-button>
                    </a-space>
                  </div>
                  <pre class="response-content">{{ responseBody }}</pre>
                </div>
              </a-tab-pane>

              <a-tab-pane key="history" tab="请求历史">
                <a-list :data-source="requestHistory" size="small">
                  <template #renderItem="{ item }">
                    <a-list-item @click="loadHistoryItem(item)">
                      <a-list-item-meta>
                        <template #title>
                          {{ item.method }} {{ item.url }}
                        </template>
                        <template #description>
                          {{ item.time }} - {{ item.status }}
                        </template>
                      </a-list-item-meta>
                      <template #actions>
                        <a-button type="link" size="small">重放</a-button>
                      </template>
                    </a-list-item>
                  </template>
                </a-list>
              </a-tab-pane>
            </a-tabs>
          </div>
        </a-card>

        <!-- 代码示例 -->
        <a-card title="代码示例" class="code-example-card" style="margin-top: 16px">
          <a-tabs v-model:activeKey="codeTab">
            <a-tab-pane key="curl" tab="cURL">
              <pre><code>{{ curlExample }}</code></pre>
            </a-tab-pane>
            <a-tab-pane key="javascript" tab="JavaScript">
              <pre><code>{{ jsExample }}</code></pre>
            </a-tab-pane>
            <a-tab-pane key="python" tab="Python">
              <pre><code>{{ pythonExample }}</code></pre>
            </a-tab-pane>
            <a-tab-pane key="java" tab="Java">
              <pre><code>{{ javaExample }}</code></pre>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  ExperimentOutlined,
  SaveOutlined,
  FolderOpenOutlined,
  ClusterOutlined,
  ApiOutlined,
  DeleteOutlined,
  PlusOutlined,
  SendOutlined
} from '@ant-design/icons-vue'

// 响应式数据
const selectedService = ref('')
const selectedApi = ref('')
const apiLoading = ref(false)
const sending = ref(false)
const paramTab = ref('path')
const bodyType = ref('json')
const responseTab = ref('response')
const codeTab = ref('curl')

// 请求相关数据
const requestMethod = ref('POST')
const pathParams = reactive({})
const queryParams = reactive({})
const jsonBody = ref('')
const rawBody = ref('')
const headers = ref([
  { key: 'Authorization', value: 'Bearer YOUR_TOKEN_HERE' },
  { key: 'Content-Type', value: 'application/json' }
])

// 响应相关数据
const responseStatus = ref(0)
const responseTime = ref(0)
const responseSize = ref(0)
const requestTime = ref('')
const responseHeaders = ref(null)
const responseBody = ref('')

// API信息
const apiInfo = reactive({
  method: 'POST',
  path: '/open/api/v1/user/auth/login',
  summary: '用户登录认证',
  parameters: {
    path: [],
    query: [],
    body: null
  },
  requestBody: {
    description: '登录请求体',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            username: { type: 'string', description: '用户名' },
            password: { type: 'string', description: '密码' }
          }
        }
      }
    }
  }
})

// 服务列表
const serviceList = ref([
  { id: 'common-service', name: '公共业务服务', port: 8088 },
  { id: 'access-service', name: '门禁管理服务', port: 8090 },
  { id: 'attendance-service', name: '考勤管理服务', port: 8091 },
  { id: 'consume-service', name: '消费管理服务', port: 8094 },
  { id: 'visitor-service', name: '访客管理服务', port: 8095 },
  { id: 'video-service', name: '视频监控服务', port: 8092 },
  { id: 'data-analysis', name: '数据分析服务', port: 8088 }
])

// API列表
const apiList = ref([
  {
    path: '/open/api/v1/user/auth/login',
    method: 'POST',
    summary: '用户登录认证'
  },
  {
    path: '/open/api/v1/user/info',
    method: 'GET',
    summary: '获取用户信息'
  },
  {
    path: '/open/api/v1/access/verify',
    method: 'POST',
    summary: '门禁验证'
  },
  {
    path: '/open/api/v1/attendance/clock',
    method: 'POST',
    summary: '考勤打卡'
  }
])

// 快速测试用例
const quickTestCases = ref([
  {
    id: 1,
    name: '用户登录测试',
    description: '测试用户登录认证接口',
    service: 'common-service',
    api: '/open/api/v1/user/auth/login',
    method: 'POST',
    body: {
      username: 'admin',
      password: '123456'
    }
  },
  {
    id: 2,
    name: '门禁验证测试',
    description: '测试门禁权限验证接口',
    service: 'access-service',
    api: '/open/api/v1/access/verify',
    method: 'POST',
    body: {
      userId: '1001',
      deviceId: 'DEV001'
    }
  },
  {
    id: 3,
    name: '考勤打卡测试',
    description: '测试考勤打卡接口',
    service: 'attendance-service',
    api: '/open/api/v1/attendance/clock',
    method: 'POST',
    body: {
      clockType: 'on',
      deviceId: 'ATT001'
    }
  }
])

// 请求历史
const requestHistory = ref([])

// 表单字段
const formFields = ref([])

// 计算属性
const requestUrl = computed(() => {
  const service = serviceList.value.find(s => s.id === selectedService.value)
  const baseUrl = service ? `http://localhost:${service.port}` : 'http://localhost:8088'
  return `${baseUrl}${apiInfo.path}`
})

const methodOptions = [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' },
  { label: 'PATCH', value: 'PATCH' }
]

// 代码示例
const curlExample = computed(() => {
  let body = ''
  if (apiInfo.method !== 'GET') {
    body = `\n-d '${jsonBody.value || rawBody.value}' \\\\n`
  }

  return `curl -X ${apiInfo.method} \\\n  '${requestUrl.value}' \\\n${body}  -H 'Authorization: Bearer YOUR_TOKEN' \\\n  -H 'Content-Type: application/json'`
})

const jsExample = computed(() => {
  return `const response = await fetch('${requestUrl.value}', {
  method: '${apiInfo.method}',
  headers: {
    'Authorization': 'Bearer YOUR_TOKEN',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(${jsonBody.value || '{}'})
});

const data = await response.json();
console.log(data);`
})

const pythonExample = computed(() => {
  return `import requests

url = '${requestUrl.value}'
headers = {
    'Authorization': 'Bearer YOUR_TOKEN',
    'Content-Type': 'application/json'
}

response = requests.${apiInfo.method.toLowerCase()}(url,
    headers=headers,
    json=${jsonBody.value || '{}'}
)

print(response.json())`
})

const javaExample = computed(() => {
  return `// 使用OkHttp
OkHttpClient client = new OkHttpClient();

RequestBody body = RequestBody.create(
    MediaType.get("application/json"),
    JSON.stringify(${jsonBody.value || '{}'})
);

Request request = new Request.Builder()
    .url("${requestUrl.value}")
    .method("${apiInfo.method}", body)
    .addHeader("Authorization", "Bearer YOUR_TOKEN")
    .addHeader("Content-Type", "application/json")
    .build();

Response response = client.newCall(request).execute();
String responseBody = response.body().string();`
})

// 方法定义
const getServiceColor = (method) => {
  const colors = {
    GET: '#52c41a',
    POST: '#1890ff',
    PUT: '#faad14',
    DELETE: '#ff4d4f',
    PATCH: '#722ed1'
  }
  return colors[method] || '#8c8c8c'
}

const getMethodColor = (method) => {
  return { color: getServiceColor(method) }
}

const getStatusColor = (status) => {
  if (status >= 200 && status < 300) {
    return { color: '#52c41a' }
  } else if (status >= 400 && status < 500) {
    return { color: '#faad14' }
  } else if (status >= 500) {
    return { color: '#ff4d4f' }
  }
  return { color: '#8c8c8c' }
}

const handleServiceChange = () => {
  selectedApi.value = ''
  apiList.value = []
  // 根据服务加载对应的API列表
  loadServiceApis()
}

const handleApiChange = () => {
  // 根据API加载详细信息
  loadApiDetails()
}

const loadServiceApis = async () => {
  apiLoading.value = true
  try {
    // 模拟API加载
    await new Promise(resolve => setTimeout(resolve, 500))
    // 这里应该调用实际的API来获取服务下的接口列表
  } catch (error) {
    message.error('加载API列表失败：' + error.message)
  } finally {
    apiLoading.value = false
  }
}

const loadApiDetails = async () => {
  // 加载API详细信息，包括参数、请求体等
  console.log('加载API详情：', selectedApi.value)
}

const handleBodyTypeChange = () => {
  // 处理请求体类型变化
  if (bodyType.value === 'form') {
    // 解析JSON为表单字段
    try {
      const json = JSON.parse(jsonBody.value)
      formFields.value = Object.keys(json).map(key => ({
        name: key,
        value: json[key],
        description: key
      }))
    } catch (error) {
      formFields.value = []
    }
  }
}

const sendRequest = async () => {
  sending.value = true
  const startTime = Date.now()

  try {
    // 构建请求
    const url = buildRequestUrl()
    const options = buildRequestOptions()

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 1000))

    // 模拟响应
    responseStatus.value = 200
    responseTime.value = Date.now() - startTime
    responseSize.value = Math.floor(Math.random() * 5000)
    requestTime.value = new Date().toLocaleString()
    responseHeaders.value = {
      'Content-Type': 'application/json',
      'X-Request-ID': 'req-' + Date.now()
    }
    responseBody.value = JSON.stringify({
      code: 200,
      message: 'success',
      data: {
        userId: 1001,
        username: 'admin',
        token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'
      },
      timestamp: Date.now()
    }, null, 2)

    // 添加到历史记录
    addToHistory()

    message.success('请求发送成功')
  } catch (error) {
    responseStatus.value = 500
    responseTime.value = Date.now() - startTime
    responseBody.value = JSON.stringify({
      code: 500,
      message: error.message,
      timestamp: Date.now()
    }, null, 2)

    message.error('请求失败：' + error.message)
  } finally {
    sending.value = false
  }
}

const buildRequestUrl = () => {
  let url = requestUrl.value

  // 添加查询参数
  const queryParamsStr = Object.keys(queryParams)
    .filter(key => queryParams[key])
    .map(key => `${key}=${encodeURIComponent(queryParams[key])}`)
    .join('&')

  if (queryParamsStr) {
    url += (url.includes('?') ? '&' : '?') + queryParamsStr
  }

  return url
}

const buildRequestOptions = () => {
  const options = {
    method: apiInfo.method,
    headers: headers.value.reduce((acc, header) => {
      if (header.key && header.value) {
        acc[header.key] = header.value
      }
      return acc
    }, {})
  }

  if (apiInfo.method !== 'GET') {
    if (bodyType.value === 'json') {
      options.body = jsonBody.value
      options.headers['Content-Type'] = 'application/json'
    } else if (bodyType.value === 'raw') {
      options.body = rawBody.value
    }
  }

  return options
}

const addToHistory = () => {
  const historyItem = {
    id: Date.now(),
    method: apiInfo.method,
    url: requestUrl.value,
    status: responseStatus.value,
    time: new Date().toLocaleString(),
    requestBody: jsonBody.value || rawBody.value,
    responseBody: responseBody.value
  }

  requestHistory.value.unshift(historyItem)
  if (requestHistory.value.length > 20) {
    requestHistory.value = requestHistory.value.slice(0, 20)
  }
}

const loadTestCase = (testCase) => {
  selectedService.value = testCase.service
  selectedApi.value = testCase.api
  requestMethod.value = testCase.method
  jsonBody.value = JSON.stringify(testCase.body, null, 2)

  // 加载对应的API详情
  loadServiceApis().then(() => {
    loadApiDetails()
  })
}

const loadHistoryItem = (item) => {
  // 重新加载历史请求的配置
  jsonBody.value = item.requestBody
  sendRequest()
}

const addHeader = () => {
  headers.value.push({ key: '', value: '' })
}

const removeHeader = (index) => {
  headers.value.splice(index, 1)
}

const formatJson = () => {
  try {
    const parsed = JSON.parse(jsonBody.value)
    jsonBody.value = JSON.stringify(parsed, null, 2)
    message.success('JSON格式化成功')
  } catch (error) {
    message.error('JSON格式错误：' + error.message)
  }
}

const validateJson = () => {
  try {
    JSON.parse(jsonBody.value)
    message.success('JSON格式正确')
  } catch (error) {
    message.error('JSON格式错误：' + error.message)
  }
}

const copyResponse = () => {
  navigator.clipboard.writeText(responseBody.value)
    .then(() => message.success('响应内容已复制'))
    .catch(() => message.error('复制失败'))
}

const downloadResponse = () => {
  const blob = new Blob([responseBody.value], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `response_${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(url)
  message.success('响应内容已下载')
}

const formatResponseBody = () => {
  try {
    const parsed = JSON.parse(responseBody.value)
    responseBody.value = JSON.stringify(parsed, null, 2)
    message.success('响应内容格式化成功')
  } catch (error) {
    message.warning('响应内容不是有效的JSON格式')
  }
}

const saveTestCase = () => {
  const testCase = {
    name: '自定义测试用例',
    description: '用户创建的测试用例',
    service: selectedService.value,
    api: selectedApi.value,
    method: requestMethod.value,
    body: jsonBody.value ? JSON.parse(jsonBody.value) : {},
    headers: headers.value
  }

  // 这里应该保存到后端
  console.log('保存测试用例：', testCase)
  message.success('测试用例已保存')
}

const loadTestSuite = () => {
  message.info('加载测试套件功能待实现')
}

// 监听API选择变化
watch([selectedService, selectedApi], () => {
  if (selectedService && selectedApi) {
    const service = serviceList.value.find(s => s.id === selectedService)
    const api = apiList.value.find(a => a.path === selectedApi)

    if (service && api) {
      apiInfo.method = api.method
      apiInfo.path = api.path
      apiInfo.summary = api.summary
      requestMethod.value = api.method
    }
  }
})

// 生命周期
onMounted(() => {
  // 初始化默认选择
  selectedService.value = 'common-service'
  loadServiceApis()
})
</script>

<style lang="less" scoped>
.api-test-center {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 24px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .header-content {
      .page-title {
        display: flex;
        align-items: center;
        font-size: 24px;
        font-weight: 600;
        margin: 0 0 8px 0;
        color: #262626;

        .title-icon {
          margin-right: 12px;
          font-size: 28px;
          color: #1890ff;
        }
      }

      .page-description {
        color: #8c8c8c;
        font-size: 14px;
        margin: 0;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .api-selector-card,
  .params-config-card,
  .api-test-card,
  .code-example-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    margin-bottom: 24px;

    .param-desc {
      font-size: 12px;
      color: #8c8c8c;
      margin-top: 4px;
    }
  }

  .api-path {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 12px;
    color: #666;
    margin-left: 8px;
  }

  .quick-test-cases {
    max-height: 300px;
    overflow-y: auto;

    .ant-list-item {
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background: #f5f5f5;
      }

      &.active {
        background: #e6f7ff;
        border-color: #1890ff;
      }
    }
  }

  .request-url-builder {
    margin-bottom: 24px;
  }

  .response-section {
    .response-info {
      margin-bottom: 24px;
    }

    .response-headers {
      margin-bottom: 24px;
    }

    .response-body {
      h4 {
        margin-bottom: 12px;
      }

      .body-actions {
        margin-bottom: 12px;
      }

      .response-content {
        background: #f6f8fa;
        border: 1px solid #e1e4e8;
        border-radius: 4px;
        padding: 16px;
        font-family: 'Monaco', 'Menlo', monospace;
        font-size: 12px;
        line-height: 1.5;
        max-height: 400px;
        overflow-y: auto;
        white-space: pre-wrap;
        word-break: break-all;
      }
    }
  }

  .headers-config {
    .header-item {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
    }
  }

  .json-tools {
    margin-top: 8px;
  }

  .code-example-card {
    pre {
      background: #f6f8fa;
      border: 1px solid #e1e4e8;
      border-radius: 4px;
      padding: 16px;
      font-family: 'Monaco', 'Menlo', monospace;
      font-size: 12px;
      line-height: 1.5;
      overflow-x: auto;
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .api-test-center {
    .ant-col {
      margin-bottom: 16px;
    }
  }
}
</style>