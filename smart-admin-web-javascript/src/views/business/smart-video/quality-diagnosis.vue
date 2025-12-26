<template>
  <div class="quality-diagnosis">
    <!-- 页面头部 -->
    <a-card title="视频质量诊断" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-space>
            <a-button type="primary" @click="batchDiagnosis">
              <template #icon><ScanOutlined /></template>
              批量诊断
            </a-button>
            <a-button @click="exportReport">
              <template #icon><DownloadOutlined /></template>
              导出报告
            </a-button>
          </a-space>
        </a-col>
        <a-col :span="12" style="text-align: right">
          <a-space>
            <a-select
              v-model:value="qualityFilter"
              style="width: 150px"
              @change="handleQualityFilterChange"
            >
              <a-select-option value="all">全部</a-select-option>
              <a-select-option value="excellent">优秀</a-select-option>
              <a-select-option value="good">良好</a-select-option>
              <a-select-option value="fair">一般</a-select-option>
              <a-select-option value="poor">较差</a-select-option>
            </a-select>
            <a-input-search
              v-model:value="searchText"
              placeholder="搜索设备名称"
              style="width: 200px"
              @search="handleSearch"
            />
            <a-button @click="refreshList">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="设备总数"
            :value="statistics.totalCount"
            :value-style="{ color: '#1890ff' }"
          >
            <template #suffix>
              <VideoCameraOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="优秀设备"
            :value="statistics.excellentCount"
            :value-style="{ color: '#52c41a' }"
          >
            <template #suffix>
              <LikeOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="问题设备"
            :value="statistics.problemCount"
            :value-style="{ color: '#ff4d4f' }"
          >
            <template #suffix>
              <ExclamationCircleOutlined />
            </template>
          </a-statistic>
      </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="平均分数"
            :value="statistics.avgScore"
            suffix="分"
            :precision="1"
            :value-style="{ color: '#faad14' }"
          >
            <template #suffix>
              <StarOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 设备列表 -->
    <a-card title="设备列表" :bordered="false" style="margin-top: 16px">
      <a-table
        :columns="columns"
        :data-source="deviceList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="deviceId"
      >
        <!-- 设备信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'deviceInfo'">
            <a-space direction="vertical" :size="2">
              <div style="font-weight: 500">{{ record.deviceName }}</div>
              <div style="color: #8c8c8c; font-size: 12px">
                IP: {{ record.ipAddress }} | 区域: {{ record.areaName }}
              </div>
            </a-space>
          </template>

          <!-- 健康评分 -->
          <template v-else-if="column.key === 'qualityScore'">
            <a-progress
              :percent="record.qualityScore"
              :status="getScoreStatus(record.qualityScore)"
              :format="(percent) => `${percent}分`"
            />
          </template>

          <!-- 质量等级 -->
          <template v-else-if="column.key === 'qualityLevel'">
            <a-tag :color="getLevelColor(record.qualityLevel)">
              {{ getLevelLabel(record.qualityLevel) }}
            </a-tag>
          </template>

          <!-- 质量指标 -->
          <template v-else-if="column.key === 'metrics'">
            <a-space direction="vertical" :size="2">
              <div style="font-size: 12px">
                <span style="color: #8c8c8c">清晰度：</span>
                <a-progress
                  :percent="record.clarity"
                  :show-info="false"
                  :stroke-color="getMetricColor(record.clarity)"
                  :size="['small']"
                  style="width: 80px; display: inline-block"
                />
                <span>{{ record.clarity }}%</span>
              </div>
              <div style="font-size: 12px">
                <span style="color: #8c8c8c">亮度：</span>
                <a-progress
                  :percent="record.brightness"
                  :show-info="false"
                  :stroke-color="getMetricColor(record.brightness)"
                  :size="['small']"
                  style="width: 80px; display: inline-block"
                />
                <span>{{ record.brightness }}%</span>
              </div>
              <div style="font-size: 12px">
                <span style="color: #8c8c8c">噪点：</span>
                <a-tag :color="record.noise > 30 ? 'red' : 'green'">
                  {{ record.noise }}%
                </a-tag>
                <span style="color: #8c8c8c; margin-left: 8px">丢帧：</span>
                <a-tag :color="record.frameLoss > 5 ? 'red' : 'green'">
                  {{ record.frameLoss }}%
                </a-tag>
              </div>
            </a-space>
          </template>

          <!-- 最后诊断时间 -->
          <template v-else-if="column.key === 'lastDiagnosisTime'">
            {{ formatDateTime(record.lastDiagnosisTime) }}
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="diagnose(record)"
              >
                立即诊断
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="viewTrend(record)"
              >
                趋势图
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="viewDetail(record)"
              >
                详情
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 质量详情模态框 -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="质量诊断详情"
      width="1000px"
      :footer="null"
    >
      <a-descriptions
        v-if="currentDevice"
        title="设备信息"
        :column="3"
        bordered
        style="margin-bottom: 24px"
      >
        <a-descriptions-item label="设备名称">
          {{ currentDevice.deviceName }}
        </a-descriptions-item>
        <a-descriptions-item label="IP地址">
          {{ currentDevice.ipAddress }}
        </a-descriptions-item>
        <a-descriptions-item label="所属区域">
          {{ currentDevice.areaName }}
        </a-descriptions-item>
        <a-descriptions-item label="设备型号">
          {{ currentDevice.deviceModel }}
        </a-descriptions-item>
        <a-descriptions-item label="固件版本">
          {{ currentDevice.firmwareVersion }}
        </a-descriptions-item>
        <a-descriptions-item label="最后诊断">
          {{ formatDateTime(currentDevice.lastDiagnosisTime) }}
        </a-descriptions-item>
      </a-descriptions>

      <a-divider orientation="left">质量指标</a-divider>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-card title="综合评分" size="small">
            <a-progress
              type="circle"
              :percent="currentDevice?.qualityScore || 0"
              :status="getScoreStatus(currentDevice?.qualityScore)"
              :format="(percent) => `${percent}分`"
            />
            <div style="margin-top: 16px; text-align: center">
              <a-tag :color="getLevelColor(currentDevice?.qualityLevel)" size="large">
                {{ getLevelLabel(currentDevice?.qualityLevel) }}
              </a-tag>
            </div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="详细指标" size="small">
            <a-space direction="vertical" style="width: 100%">
              <div>
                <span>清晰度</span>
                <a-progress
                  :percent="currentDevice?.clarity || 0"
                  :stroke-color="getMetricColor(currentDevice?.clarity)"
                />
              </div>
              <div>
                <span>亮度</span>
                <a-progress
                  :percent="currentDevice?.brightness || 0"
                  :stroke-color="getMetricColor(currentDevice?.brightness)"
                />
              </div>
              <div>
                <span>噪点</span>
                <a-progress
                  :percent="currentDevice?.noise || 0"
                  :stroke-color="getMetricColor(100 - (currentDevice?.noise || 0))"
                />
              </div>
              <div>
                <span>丢帧率</span>
                <a-progress
                  :percent="currentDevice?.frameLoss || 0"
                  :stroke-color="getMetricColor(100 - (currentDevice?.frameLoss || 0))"
                />
              </div>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <!-- 优化建议 -->
      <a-divider orientation="left">优化建议</a-divider>
      <a-list
        v-if="currentDevice && currentDevice.suggestions"
        :data-source="currentDevice.suggestions"
        size="small"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #title>
                <a-tag :color="item.priority === 'high' ? 'red' : 'orange'">
                  {{ item.priority === 'high' ? '紧急' : '建议' }}
                </a-tag>
                {{ item.title }}
              </template>
              <template #description>{{ item.description }}</template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
    </a-modal>

    <!-- 质量趋势模态框 -->
    <a-modal
      v-model:visible="trendModalVisible"
      title="质量趋势分析"
      width="900px"
      :footer="null"
    >
      <div v-if="currentDevice" style="margin-bottom: 16px">
        <a-space>
          <span>设备：{{ currentDevice.deviceName }}</span>
          <span>IP：{{ currentDevice.ipAddress }}</span>
          <span>时间范围：最近7天</span>
        </a-space>
      </div>
      <div ref="trendChart" style="height: 400px; background: #fafafa; border-radius: 4px; display: flex; align-items: center; justify-content: center;">
        <!-- TODO: 集成ECharts显示趋势图 -->
        <div style="text-align: center; color: #8c8c8c">
          <LineChartOutlined style="font-size: 48px; margin-bottom: 16px" />
          <div>趋势图表</div>
          <div style="font-size: 12px">需要集成ECharts组件</div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import {
  ScanOutlined,
  DownloadOutlined,
  ReloadOutlined,
  VideoCameraOutlined,
  LikeOutlined,
  ExclamationCircleOutlined,
  StarOutlined,
  LineChartOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';

// 数据状态
const loading = ref(false);
const searchText = ref('');
const qualityFilter = ref('all');
const deviceList = ref([]);
const currentDevice = ref(null);

// 模态框状态
const detailModalVisible = ref(false);
const trendModalVisible = ref(false);

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条记录`
});

// 统计信息
const statistics = ref({
  totalCount: 0,
  excellentCount: 0,
  goodCount: 0,
  fairCount: 0,
  poorCount: 0,
  problemCount: 0,
  avgScore: 0
});

// 表格列定义
const columns = [
  {
    title: '设备信息',
    key: 'deviceInfo',
    width: 250,
    fixed: 'left'
  },
  {
    title: '健康评分',
    key: 'qualityScore',
    width: 150
  },
  {
    title: '质量等级',
    key: 'qualityLevel',
    width: 120
  },
  {
    title: '质量指标',
    key: 'metrics',
    width: 300
  },
  {
    title: '最后诊断',
    key: 'lastDiagnosisTime',
    width: 150
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
];

// 生命周期
onMounted(() => {
  fetchDeviceList();
  fetchStatistics();
});

// 获取设备列表
const fetchDeviceList = async () => {
  loading.value = true;
  try {
    // TODO: 调用实际API
    // const response = await videoApi.getQualityDiagnosisList({
    //   pageNum: pagination.current,
    //   pageSize: pagination.pageSize,
    //   searchText: searchText.value,
    //   qualityFilter: qualityFilter.value
    // });

    // 模拟数据
    const mockData = {
      records: [
        {
          deviceId: '1',
          deviceName: 'A栋大门摄像头',
          ipAddress: '192.168.1.101',
          areaName: 'A栋大厅',
          deviceModel: 'Hikvision DS-2CD2T45D',
          firmwareVersion: 'V5.5.0',
          qualityScore: 92,
          qualityLevel: 'excellent',
          clarity: 95,
          brightness: 90,
          noise: 8,
          frameLoss: 2,
          lastDiagnosisTime: '2025-12-26 10:00:00',
          suggestions: [
            {
              priority: 'low',
              title: '定期清洁镜头',
              description: '建议每月清洁一次镜头，确保画面清晰度'
            }
          ]
        },
        {
          deviceId: '2',
          deviceName: 'B栋电梯摄像头',
          ipAddress: '192.168.1.102',
          areaName: 'B栋电梯',
          deviceModel: 'Dahua DH-HAC-HFW',
          firmwareVersion: 'V4.2.1',
          qualityScore: 68,
          qualityLevel: 'fair',
          clarity: 75,
          brightness: 70,
          noise: 35,
          frameLoss: 8,
          lastDiagnosisTime: '2025-12-26 09:00:00',
          suggestions: [
            {
              priority: 'high',
              title: '噪点偏高',
              description: '图像噪点达到35%，建议检查摄像机设置或更换设备'
            },
            {
              priority: 'high',
              title: '丢帧率偏高',
              description: '丢帧率8%，建议检查网络带宽和传输质量'
            },
            {
              priority: 'low',
              title: '亮度调整',
              description: '当前亮度70%，可根据环境光线适当调整'
            }
          ]
        },
        {
          deviceId: '3',
          deviceName: '停车场入口摄像头',
          ipAddress: '192.168.1.103',
          areaName: '停车场',
          deviceModel: 'Uniview IPC312',
          firmwareVersion: 'V3.1.0',
          qualityScore: 45,
          qualityLevel: 'poor',
          clarity: 50,
          brightness: 45,
          noise: 55,
          frameLoss: 15,
          lastDiagnosisTime: '2025-12-26 08:00:00',
          suggestions: [
            {
              priority: 'high',
              title: '清晰度严重不足',
              description: '清晰度仅50%，可能存在镜头污染或对焦问题'
            },
            {
              priority: 'high',
              title: '噪点过高',
              description: '噪点达到55%，建议检查光照条件或摄像机设置'
            },
            {
              priority: 'high',
              title: '丢帧率严重',
              description: '丢帧率15%，网络传输存在严重问题，需要立即处理'
            },
            {
              priority: 'low',
              title: '亮度偏低',
              description: '建议增加照明设备或调整摄像机曝光参数'
            }
          ]
        }
      ],
      total: 3
    };

    deviceList.value = mockData.records;
    pagination.total = mockData.total;
  } catch (error) {
    message.error('获取设备列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 获取统计信息
const fetchStatistics = async () => {
  try {
    // TODO: 调用实际API
    // const response = await videoApi.getQualityStatistics();

    // 模拟数据
    statistics.value = {
      totalCount: 125,
      excellentCount: 68,
      goodCount: 32,
      fairCount: 15,
      poorCount: 10,
      problemCount: 25,
      avgScore: 78.5
    };
  } catch (error) {
    message.error('获取统计信息失败：' + error.message);
  }
};

// 立即诊断
const diagnose = async (record) => {
  try {
    // TODO: 调用实际API
    // await videoApi.diagnoseDevice(record.deviceId);

    message.loading('正在诊断设备：' + record.deviceName, 0);

    // 模拟诊断过程
    setTimeout(() => {
      message.destroy();
      message.success('诊断完成');
      fetchDeviceList();
      fetchStatistics();
    }, 2000);
  } catch (error) {
    message.error('诊断失败：' + error.message);
  }
};

// 批量诊断
const batchDiagnosis = () => {
  message.info('批量诊断功能开发中...');
};

// 导出报告
const exportReport = () => {
  message.info('导出报告功能开发中...');
};

// 查看详情
const viewDetail = (record) => {
  currentDevice.value = record;
  detailModalVisible.value = true;
};

// 查看趋势
const viewTrend = (record) => {
  currentDevice.value = record;
  trendModalVisible.value = true;
};

// 质量筛选变更
const handleQualityFilterChange = (value) => {
  pagination.current = 1;
  fetchDeviceList();
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  fetchDeviceList();
};

// 刷新列表
const refreshList = () => {
  fetchDeviceList();
  fetchStatistics();
};

// 表格变化
const handleTableChange = (pag, filters, sorter) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchDeviceList();
};

// 工具方法
const getScoreStatus = (score) => {
  if (score >= 90) return 'success';
  if (score >= 75) return 'normal';
  if (score >= 60) return 'exception';
  return 'exception';
};

const getLevelColor = (level) => {
  const colorMap = {
    'excellent': 'green',  // 优秀
    'good': 'blue',       // 良好
    'fair': 'orange',     // 一般
    'poor': 'red'         // 较差
  };
  return colorMap[level] || 'default';
};

const getLevelLabel = (level) => {
  const labelMap = {
    'excellent': '优秀',
    'good': '良好',
    'fair': '一般',
    'poor': '较差'
  };
  return labelMap[level] || '未知';
};

const getMetricColor = (percent) => {
  if (percent >= 80) return '#52c41a';
  if (percent >= 60) return '#faad14';
  return '#ff4d4f';
};

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-';
  return dateTimeStr;
};
</script>

<style scoped lang="less">
.quality-diagnosis {
  padding: 16px;

  :deep(.ant-card) {
    .ant-card-head {
      border-bottom: 1px solid #f0f0f0;
    }
  }

  :deep(.ant-progress) {
    .ant-progress-text {
      font-size: 12px;
    }
  }

  :deep(.ant-descriptions) {
    .ant-descriptions-item-label {
      font-weight: 500;
    }
  }
}
</style>
