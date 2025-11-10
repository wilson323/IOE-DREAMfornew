<!--
  智能视频-历史告警页面

  @Author:    Claude Code
  @Date:      2024-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="history-alarm">
    <!-- 查询工具栏 -->
    <a-card :bordered="false">
      <a-form class="smart-query-table-form">
        <a-row :gutter="16">
          <a-col :span="7">
            <a-form-item label="告警时间">
              <a-range-picker
                v-model:value="queryForm.timeRange"
                style="width: 100%"
                :placeholder="['开始时间', '结束时间']"
              />
            </a-form-item>
          </a-col>

          <a-col :span="5">
            <a-form-item label="告警级别">
              <a-select
                v-model:value="queryForm.level"
                placeholder="请选择告警级别"
                allowClear
              >
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="高危">高危</a-select-option>
                <a-select-option value="中危">中危</a-select-option>
                <a-select-option value="低危">低危</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="5">
            <a-form-item label="告警类型">
              <a-select
                v-model:value="queryForm.type"
                placeholder="请选择告警类型"
                allowClear
              >
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="行为分析">行为分析</a-select-option>
                <a-select-option value="人脸识别">人脸识别</a-select-option>
                <a-select-option value="设备异常">设备异常</a-select-option>
                <a-select-option value="区域入侵">区域入侵</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="7">
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="queryData">
                  <template #icon><SearchOutlined /></template>
                  查询
                </a-button>
                <a-button @click="resetQuery">
                  <template #icon><ReloadOutlined /></template>
                  重置
                </a-button>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="今日告警总数"
            :value="statistics.todayTotal"
            suffix="条"
          >
            <template #prefix>
              <WarningOutlined style="color: #faad14" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="高危告警"
            :value="statistics.highLevel"
            suffix="条"
            :value-style="{ color: '#ff4d4f' }"
          >
            <template #prefix>
              <WarningOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="未处理"
            :value="statistics.unprocessed"
            suffix="条"
            :value-style="{ color: '#faad14' }"
          >
            <template #prefix>
              <ClockCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="已处理"
            :value="statistics.processed"
            suffix="条"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <CheckCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 操作按钮和表格 -->
    <a-card :bordered="false" class="smart-margin-top10">
      <div class="smart-query-table-operation">
        <a-space>
          <a-button
            danger
            @click="batchProcess"
            :disabled="selectedRowKeys.length === 0"
          >
            <template #icon><CheckOutlined /></template>
            批量处理
          </a-button>

          <a-button @click="exportData">
            <template #icon><DownloadOutlined /></template>
            导出
          </a-button>

          <a-button @click="refreshData">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </div>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="tableLoading"
        :row-selection="rowSelection"
        :scroll="{ x: 1400 }"
        row-key="id"
        bordered
        @change="handleTableChange"
        class="smart-margin-top10"
      >
        <template #bodyCell="{ column, record }">
          <!-- 告警级别 -->
          <template v-if="column.dataIndex === 'level'">
            <a-tag :color="getAlarmLevelColor(record.level)">
              {{ record.level }}
            </a-tag>
          </template>

          <!-- 告警类型 -->
          <template v-else-if="column.dataIndex === 'type'">
            <a-tag>{{ record.type }}</a-tag>
          </template>

          <!-- 处理状态 -->
          <template v-else-if="column.dataIndex === 'status'">
            <a-badge
              :status="record.status === '已处理' ? 'success' : 'warning'"
              :text="record.status"
            />
          </template>

          <!-- 操作 -->
          <template v-else-if="column.dataIndex === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewDetail(record)">
                <template #icon><EyeOutlined /></template>
                查看
              </a-button>

              <a-button
                type="link"
                size="small"
                @click="processAlarm(record)"
                v-if="record.status !== '已处理'"
              >
                <template #icon><CheckOutlined /></template>
                处理
              </a-button>

              <a-button type="link" size="small" @click="viewSnapshot(record)">
                <template #icon><PictureOutlined /></template>
                快照
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 告警详情弹窗 -->
    <a-modal
      :open="detailVisible"
      title="告警详情"
      width="900px"
      @cancel="detailVisible = false"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentAlarm">
        <a-descriptions-item label="告警时间">
          {{ currentAlarm.time }}
        </a-descriptions-item>
        <a-descriptions-item label="告警级别">
          <a-tag :color="getAlarmLevelColor(currentAlarm.level)">
            {{ currentAlarm.level }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="设备名称">
          {{ currentAlarm.deviceName }}
        </a-descriptions-item>
        <a-descriptions-item label="设备编码">
          {{ currentAlarm.deviceCode }}
        </a-descriptions-item>
        <a-descriptions-item label="告警类型">
          {{ currentAlarm.type }}
        </a-descriptions-item>
        <a-descriptions-item label="处理状态">
          <a-badge
            :status="currentAlarm.status === '已处理' ? 'success' : 'warning'"
            :text="currentAlarm.status"
          />
        </a-descriptions-item>
        <a-descriptions-item label="告警内容" :span="2">
          {{ currentAlarm.content }}
        </a-descriptions-item>
        <a-descriptions-item label="处理人" v-if="currentAlarm.processor">
          {{ currentAlarm.processor }}
        </a-descriptions-item>
        <a-descriptions-item label="处理时间" v-if="currentAlarm.processTime">
          {{ currentAlarm.processTime }}
        </a-descriptions-item>
        <a-descriptions-item label="处理备注" :span="2" v-if="currentAlarm.remark">
          {{ currentAlarm.remark }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined,
  DownloadOutlined,
  WarningOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  CheckOutlined,
  EyeOutlined,
  PictureOutlined,
} from '@ant-design/icons-vue';

// 查询表单
const queryForm = reactive({
  timeRange: [],
  level: '',
  type: '',
});

// 统计数据
const statistics = reactive({
  todayTotal: 156,
  highLevel: 23,
  unprocessed: 45,
  processed: 111,
});

// 表格数据
const tableData = ref([]);
const tableLoading = ref(false);
const selectedRowKeys = ref([]);

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`,
});

// 表格列配置
const columns = [
  {
    title: '序号',
    dataIndex: 'index',
    key: 'index',
    width: 80,
    customRender: ({ index }) => index + 1,
  },
  {
    title: '告警时间',
    dataIndex: 'time',
    key: 'time',
    width: 160,
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 180,
    ellipsis: true,
  },
  {
    title: '设备编码',
    dataIndex: 'deviceCode',
    key: 'deviceCode',
    width: 140,
  },
  {
    title: '告警级别',
    dataIndex: 'level',
    key: 'level',
    width: 100,
  },
  {
    title: '告警类型',
    dataIndex: 'type',
    key: 'type',
    width: 120,
  },
  {
    title: '告警内容',
    dataIndex: 'content',
    key: 'content',
    ellipsis: true,
  },
  {
    title: '处理状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '操作',
    dataIndex: 'action',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
];

// 行选择
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  },
}));

// Mock数据
const mockData = [
  {
    id: 1,
    time: '2024-11-05 15:32:10',
    deviceName: '前门摄像头-001',
    deviceCode: 'CAM-001-20240101',
    level: '高危',
    type: '行为分析',
    content: '检测到可疑人员徘徊',
    status: '未处理',
  },
  {
    id: 2,
    time: '2024-11-05 15:28:35',
    deviceName: '停车场摄像头-01',
    deviceCode: 'CAM-005-20240105',
    level: '中危',
    type: '区域入侵',
    content: '车辆违停告警',
    status: '已处理',
    processor: '张三',
    processTime: '2024-11-05 15:35:20',
    remark: '已通知车主挪车',
  },
  {
    id: 3,
    time: '2024-11-05 15:15:22',
    deviceName: 'NVR存储设备-01',
    deviceCode: 'NVR-001-20240104',
    level: '低危',
    type: '设备异常',
    content: '存储空间不足80%',
    status: '已处理',
    processor: '李四',
    processTime: '2024-11-05 15:25:10',
    remark: '已清理历史数据',
  },
  {
    id: 4,
    time: '2024-11-05 14:58:10',
    deviceName: '走廊摄像头-003',
    deviceCode: 'CAM-003-20240103',
    level: '中危',
    type: '设备异常',
    content: '设备离线',
    status: '未处理',
  },
  {
    id: 5,
    time: '2024-11-05 14:45:18',
    deviceName: '会议室摄像头-01',
    deviceCode: 'CAM-006-20240106',
    level: '低危',
    type: '设备异常',
    content: '画面模糊告警',
    status: '未处理',
  },
];

// 查询数据
const queryData = () => {
  tableLoading.value = true;

  setTimeout(() => {
    let filteredData = [...mockData];

    // 筛选条件
    if (queryForm.level) {
      filteredData = filteredData.filter(item => item.level === queryForm.level);
    }
    if (queryForm.type) {
      filteredData = filteredData.filter(item => item.type === queryForm.type);
    }

    tableData.value = filteredData;
    pagination.total = filteredData.length;
    tableLoading.value = false;
  }, 300);
};

// 重置查询
const resetQuery = () => {
  Object.assign(queryForm, {
    timeRange: [],
    level: '',
    type: '',
  });
  pagination.current = 1;
  queryData();
};

// 表格变化处理
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  queryData();
};

// 批量处理
const batchProcess = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要处理的告警');
    return;
  }

  Modal.confirm({
    title: '确认处理',
    content: `确定要批量处理选中的 ${selectedRowKeys.value.length} 条告警吗?`,
    onOk: () => {
      message.success('处理成功');
      selectedRowKeys.value = [];
      queryData();
    },
  });
};

// 导出数据
const exportData = () => {
  message.info('导出功能开发中...');
};

// 刷新数据
const refreshData = () => {
  queryData();
  message.success('刷新成功');
};

// 获取告警级别颜色
const getAlarmLevelColor = (level) => {
  const colorMap = {
    '高危': 'red',
    '中危': 'orange',
    '低危': 'blue',
  };
  return colorMap[level] || 'default';
};

// 告警详情
const detailVisible = ref(false);
const currentAlarm = ref(null);

// 查看详情
const viewDetail = (record) => {
  currentAlarm.value = record;
  detailVisible.value = true;
};

// 处理告警
const processAlarm = (record) => {
  Modal.confirm({
    title: '处理告警',
    content: `确定要处理告警: ${record.content}?`,
    onOk: () => {
      message.success('处理成功');
      queryData();
    },
  });
};

// 查看快照
const viewSnapshot = (record) => {
  message.info(`查看快照: ${record.deviceName}`);
};

// 初始化
onMounted(() => {
  queryData();
});
</script>

<style scoped lang="less">
.history-alarm {
  padding: 24px;
  background-color: #f0f2f5;

  .stat-card {
    :deep(.ant-card-body) {
      padding: 20px 24px;
    }
  }

  .smart-margin-top10 {
    margin-top: 10px;
  }

  .smart-query-table-form {
    margin-bottom: 0;
  }

  .smart-query-table-operation {
    margin-bottom: 16px;
  }

  :deep(.ant-card) {
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
  }

  :deep(.ant-statistic-title) {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.45);
  }

  :deep(.ant-statistic-content) {
    font-size: 20px;
    font-weight: 600;
  }
}
</style>
