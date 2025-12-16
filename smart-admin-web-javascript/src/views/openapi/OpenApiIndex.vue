<template>
  <div class="openapi-index">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <ApiOutlined class="title-icon" />
          开放平台API中心
        </h1>
        <p class="page-description">
          IOE-DREAM智慧园区开放平台API文档与服务管理
        </p>
      </div>
      <div class="header-actions">
        <a-button type="primary" @click="refreshApiList">
          <ReloadOutlined />
          刷新列表
        </a-button>
        <a-button @click="exportApiDoc">
          <ExportOutlined />
          导出文档
        </a-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card class="stats-card">
            <a-statistic
              title="API总数"
              :value="apiStats.totalApis"
              :prefix="h(ApiOutlined)"
              :value-style="{ color: '#1890ff' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stats-card">
            <a-statistic
              title="微服务数量"
              :value="apiStats.totalServices"
              :prefix="h(ClusterOutlined)"
              :value-style="{ color: '#52c41a' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stats-card">
            <a-statistic
              title="在线服务"
              :value="apiStats.onlineServices"
              :prefix="h(CheckCircleOutlined)"
              :value-style="{ color: '#52c41a' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stats-card">
            <a-statistic
              title="今日调用"
              :value="apiStats.todayCalls"
              :prefix="h(BarChartOutlined)"
              :value-style="{ color: '#faad14' }"
            />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 微服务API列表 -->
    <a-card title="微服务API列表" class="api-list-card">
      <template #extra>
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索API或服务名称"
          style="width: 300px"
          @search="handleSearch"
        />
      </template>

      <a-table
        :columns="apiColumns"
        :data-source="apiList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="serviceId"
      >
        <!-- 服务状态 -->
        <template #status="{ record }">
          <a-badge
            :status="record.status === 'online' ? 'success' : 'error'"
            :text="record.status === 'online' ? '在线' : '离线'"
          />
        </template>

        <!-- API数量 -->
        <template #apiCount="{ record }">
          <a-tag color="blue">{{ record.apiCount }}个接口</a-tag>
        </template>

        <!-- 最后更新时间 -->
        <template #updateTime="{ record }">
          <span>{{ formatTime(record.updateTime) }}</span>
        </template>

        <!-- 操作按钮 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="viewSwaggerDoc(record)">
              <FileTextOutlined />
              API文档
            </a-button>
            <a-button type="link" size="small" @click="testApi(record)">
              <PlayCircleOutlined />
              在线测试
            </a-button>
            <a-dropdown>
              <template #overlay>
                <a-menu @click="handleMenuClick($event, record)">
                  <a-menu-item key="monitor">
                    <MonitorOutlined />
                    监控面板
                  </a-menu-item>
                  <a-menu-item key="logs">
                    <FileTextOutlined />
                    调用日志
                  </a-menu-item>
                  <a-menu-item key="stats">
                    <BarChartOutlined />
                    统计分析
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button type="link" size="small">
                更多 <DownOutlined />
              </a-button>
            </a-dropdown>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- API分类导航 -->
    <a-row :gutter="16" class="category-section">
      <a-col :span="8">
        <a-card title="API功能分类" size="small">
          <a-menu mode="inline" :default-selected-keys="['all']" @click="handleCategoryClick">
            <a-menu-item key="all">
              <AppstoreOutlined />
              全部API
            </a-menu-item>
            <a-menu-item key="user">
              <UserOutlined />
              用户管理API
            </a-menu-item>
            <a-menu-item key="access">
              <SafetyCertificateOutlined />
              门禁管理API
            </a-menu-item>
            <a-menu-item key="attendance">
              <CalendarOutlined />
              考勤管理API
            </a-menu-item>
            <a-menu-item key="consume">
              <CreditCardOutlined />
              消费管理API
            </a-menu-item>
            <a-menu-item key="visitor">
              <TeamOutlined />
              访客管理API
            </a-menu-item>
            <a-menu-item key="video">
              <VideoCameraOutlined />
              视频监控API
            </a-menu-item>
            <a-menu-item key="data">
              <BarChartOutlined />
              数据分析API
            </a-menu-item>
          </a-menu>
        </a-card>
      </a-col>

      <a-col :span="16">
        <a-card title="快速访问" size="small">
          <a-row :gutter="16">
            <a-col :span="12">
              <h4>🔑 认证获取</h4>
              <p>获取API访问令牌，开始调用开放接口</p>
              <a-button type="primary" size="small" @click="showTokenModal">
                获取Token
              </a-button>
            </a-col>
            <a-col :span="12">
              <h4>📚 开发者指南</h4>
              <p>查看API使用指南和最佳实践</p>
              <a-button size="small" @click="showDeveloperGuide">
                查看指南
              </a-button>
            </a-col>
          </a-row>
        </a-card>

        <a-card title="热门API接口" size="small" style="margin-top: 16px">
          <a-list :data-source="popularApis" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    <a @click="viewApiDetail(item)">{{ item.name }}</a>
                  </template>
                  <template #description>
                    {{ item.description }}
                  </template>
                  <template #avatar>
                    <a-avatar :style="{ backgroundColor: item.color }">
                      {{ item.icon }}
                    </a-avatar>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <!-- Token获取弹窗 -->
    <a-modal
      v-model:visible="tokenModalVisible"
      title="获取API访问令牌"
      width="600px"
      @ok="handleTokenOk"
    >
      <a-form :model="tokenForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="应用ID" required>
          <a-input v-model:value="tokenForm.appId" placeholder="请输入应用ID" />
        </a-form-item>
        <a-form-item label="应用密钥" required>
          <a-input-password v-model:value="tokenForm.appSecret" placeholder="请输入应用密钥" />
        </a-form-item>
        <a-form-item label="有效期">
          <a-select v-model:value="tokenForm.expiry" placeholder="选择Token有效期">
            <a-select-option value="1h">1小时</a-select-option>
            <a-select-option value="24h">24小时</a-select-option>
            <a-select-option value="7d">7天</a-select-option>
            <a-select-option value="30d">30天</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="权限范围">
          <a-checkbox-group v-model:value="tokenForm.scopes">
            <a-checkbox value="user">用户管理</a-checkbox>
            <a-checkbox value="access">门禁管理</a-checkbox>
            <a-checkbox value="attendance">考勤管理</a-checkbox>
            <a-checkbox value="consume">消费管理</a-checkbox>
            <a-checkbox value="visitor">访客管理</a-checkbox>
            <a-checkbox value="video">视频监控</a-checkbox>
            <a-checkbox value="data">数据分析</a-checkbox>
          </a-checkbox-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  ApiOutlined,
  ReloadOutlined,
  ExportOutlined,
  ClusterOutlined,
  CheckCircleOutlined,
  BarChartOutlined,
  FileTextOutlined,
  PlayCircleOutlined,
  DownOutlined,
  MonitorOutlined,
  AppstoreOutlined,
  UserOutlined,
  SafetyCertificateOutlined,
  CalendarOutlined,
  CreditCardOutlined,
  TeamOutlined,
  VideoCameraOutlined
} from '@ant-design/icons-vue'

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const tokenModalVisible = ref(false)

// API统计数据
const apiStats = reactive({
  totalApis: 98,
  totalServices: 7,
  onlineServices: 7,
  todayCalls: 12543
})

// API列表表格配置
const apiColumns = [
  {
    title: '服务名称',
    dataIndex: 'serviceName',
    key: 'serviceName',
    width: 200
  },
  {
    title: '服务描述',
    dataIndex: 'description',
    key: 'description'
  },
  {
    title: '端口',
    dataIndex: 'port',
    key: 'port',
    width: 80
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    slots: { customRender: 'status' }
  },
  {
    title: 'API数量',
    dataIndex: 'apiCount',
    key: 'apiCount',
    width: 120,
    slots: { customRender: 'apiCount' }
  },
  {
    title: '最后更新',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 150,
    slots: { customRender: 'updateTime' }
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    slots: { customRender: 'action' }
  }
]

// API列表数据
const apiList = ref([
  {
    serviceId: 'common-service',
    serviceName: '公共业务服务',
    description: '用户管理、权限管理、字典管理等公共功能',
    port: 8088,
    status: 'online',
    apiCount: 12,
    updateTime: '2025-12-16T10:30:00',
    docUrl: 'http://localhost:8088/doc.html'
  },
  {
    serviceId: 'access-service',
    serviceName: '门禁管理服务',
    description: '智能门禁控制、通行记录、权限验证',
    port: 8090,
    status: 'online',
    apiCount: 12,
    updateTime: '2025-12-16T10:30:00',
    docUrl: 'http://localhost:8090/doc.html'
  },
  {
    serviceId: 'attendance-service',
    serviceName: '考勤管理服务',
    description: '考勤打卡、排班管理、统计报表',
    port: 8091,
    status: 'online',
    apiCount: 12,
    updateTime: '2025-12-16T10:30:00',
    docUrl: 'http://localhost:8091/doc.html'
  },
  {
    serviceId: 'consume-service',
    serviceName: '消费管理服务',
    description: '账户管理、交易处理、充值退款',
    port: 8094,
    status: 'online',
    apiCount: 15,
    updateTime: '2025-12-16T10:30:00',
    docUrl: 'http://localhost:8094/doc.html'
  },
  {
    serviceId: 'visitor-service',
    serviceName: '访客管理服务',
    description: '访客预约、审批流程、轨迹追踪',
    port: 8095,
    status: 'online',
    apiCount: 15,
    updateTime: '2025-12-16T10:30:00',
    docUrl: 'http://localhost:8095/doc.html'
  },
  {
    serviceId: 'video-service',
    serviceName: '视频监控服务',
    description: '实时视频、录像回放、AI分析',
    port: 8092,
    status: 'online',
    apiCount: 16,
    updateTime: '2025-12-16T10:30:00',
    docUrl: 'http://localhost:8092/doc.html'
  },
  {
    serviceId: 'data-analysis',
    serviceName: '数据分析服务',
    description: '运营报表、趋势分析、智能预测',
    port: 8088,
    status: 'online',
    apiCount: 16,
    updateTime: '2025-12-16T10:30:00',
    docUrl: 'http://localhost:8088/openapi/doc.html'
  }
])

// 热门API接口
const popularApis = ref([
  {
    name: '用户认证登录',
    description: '用户身份验证和JWT令牌获取',
    icon: '👤',
    color: '#1890ff',
    service: 'common-service',
    endpoint: '/open/api/v1/user/auth/login'
  },
  {
    name: '门禁验证',
    description: '门禁权限验证和通行控制',
    icon: '🚪',
    color: '#52c41a',
    service: 'access-service',
    endpoint: '/open/api/v1/access/verify'
  },
  {
    name: '考勤打卡',
    description: '员工考勤打卡和位置验证',
    icon: '⏰',
    color: '#faad14',
    service: 'attendance-service',
    endpoint: '/open/api/v1/attendance/clock'
  },
  {
    name: '消费交易',
    description: '一卡通消费和账户扣款',
    icon: '💳',
    color: '#f5222d',
    service: 'consume-service',
    endpoint: '/open/api/v1/consume/transaction/consume'
  },
  {
    name: '访客预约',
    description: '访客预约申请和审批流程',
    icon: '👥',
    color: '#722ed1',
    service: 'visitor-service',
    endpoint: '/open/api/v1/visitor/appointment'
  },
  {
    name: '实时视频流',
    description: '获取设备实时视频流地址',
    icon: '📹',
    color: '#13c2c2',
    service: 'video-service',
    endpoint: '/open/api/v1/video/stream/live/{deviceId}'
  }
])

// Token表单
const tokenForm = reactive({
  appId: '',
  appSecret: '',
  expiry: '24h',
  scopes: ['user', 'access', 'attendance']
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 7,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
})

// 方法定义
const formatTime = (timeString) => {
  if (!timeString) return '-'
  return new Date(timeString).toLocaleString('zh-CN')
}

const refreshApiList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    message.success('API列表已刷新')
  } catch (error) {
    message.error('刷新失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const exportApiDoc = () => {
  message.info('正在生成API文档...')
  // 实现文档导出逻辑
}

const handleSearch = () => {
  // 实现搜索逻辑
  console.log('搜索关键词：', searchKeyword.value)
}

const handleTableChange = (pag, filters, sorter) => {
  console.log('表格变化：', pag, filters, sorter)
}

const viewSwaggerDoc = (record) => {
  window.open(record.docUrl, '_blank')
}

const testApi = (record) => {
  message.info(`即将打开 ${record.serviceName} 的在线测试工具`)
  window.open(`${record.docUrl.replace('/doc.html', '/swagger-ui/index.html')}`, '_blank')
}

const handleMenuClick = (key, record) => {
  switch (key.key) {
    case 'monitor':
      message.info('打开监控面板')
      break
    case 'logs':
      message.info('查看调用日志')
      break
    case 'stats':
      message.info('查看统计分析')
      break
  }
}

const handleCategoryClick = ({ key }) => {
  message.info(`切换到分类：${key}`)
  // 实现分类过滤逻辑
}

const showTokenModal = () => {
  tokenModalVisible.value = true
}

const handleTokenOk = async () => {
  try {
    // 模拟Token生成
    const token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c'

    // 复制到剪贴板
    await navigator.clipboard.writeText(token)
    message.success('Token已生成并复制到剪贴板')
    tokenModalVisible.value = false
  } catch (error) {
    message.error('Token生成失败：' + error.message)
  }
}

const showDeveloperGuide = () => {
  message.info('打开开发者指南')
  // 实现开发者指南展示
}

const viewApiDetail = (api) => {
  message.info(`查看API详情：${api.name}`)
  // 实现API详情展示
}

// 生命周期
onMounted(() => {
  refreshApiList()
})
</script>

<style lang="less" scoped>
.openapi-index {
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

  .stats-cards {
    margin-bottom: 24px;

    .stats-card {
      text-align: center;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      :deep(.ant-statistic-title) {
        font-size: 14px;
        color: #8c8c8c;
      }

      :deep(.ant-statistic-content) {
        font-size: 24px;
        font-weight: 600;
      }
    }
  }

  .api-list-card {
    margin-bottom: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    :deep(.ant-table-thead > tr > th) {
      background: #fafafa;
      font-weight: 600;
    }

    :deep(.ant-table-tbody > tr:hover > td) {
      background: #f5f5f5;
    }
  }

  .category-section {
    margin-bottom: 24px;

    .ant-card {
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      :deep(.ant-menu-item) {
        margin: 4px 0;
        border-radius: 4px;

        &:hover {
          background: #f5f5f5;
        }

        &.ant-menu-item-selected {
          background: #e6f7ff;
          color: #1890ff;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .openapi-index {
    padding: 16px;

    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;

      .header-actions {
        width: 100%;
        justify-content: flex-end;
      }
    }

    .stats-cards {
      .ant-col {
        margin-bottom: 16px;
      }
    }
  }
}
</style>