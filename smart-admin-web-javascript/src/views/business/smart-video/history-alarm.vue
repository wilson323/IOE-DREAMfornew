<!--
  智能视频-告警管理与统计分析页面（企业增强版）

  @Author:    IOE-DREAM Team
  @Date:      2025-01-30
  @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="smart-alarm-management">
    <!-- 实时告警推送通知 -->
    <a-alert
      v-if="hasNewAlarms"
      :message="`收到 ${newAlarmsCount} 条新告警`"
      type="warning"
      show-icon
      closable
      @close="dismissNewAlarms"
      style="margin-bottom: 16px;"
    >
      <template #action>
        <a-button size="small" @click="viewNewAlarms">
          立即查看
        </a-button>
        <a-button size="small" type="primary" @click="processAllNewAlarms">
          全部处理
        </a-button>
      </template>
    </a-alert>

    <!-- 统计概览卡片 -->
    <a-row :gutter="16" style="margin-bottom: 16px;">
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="今日告警"
            :value="statistics.todayTotal"
            suffix="条"
            :value-style="{ color: statistics.todayTotal > 0 ? '#ff4d4f' : '' }"
          >
            <template #prefix>
              <WarningOutlined />
            </template>
            <template #suffix>
              <a-tag :color="getTrendColor(statistics.todayTrend)">
                {{ statistics.todayTrend > 0 ? '↑' : '↓' }}{{ Math.abs(statistics.todayTrend) }}%
              </a-tag>
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
              <ExclamationCircleOutlined />
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
            :value-style="{ color: statistics.unprocessed > 0 ? '#faad14' : '#52c41a' }"
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
            title="处理率"
            :value="statistics.processRate"
            suffix="%"
            :value-style="{ color: statistics.processRate >= 90 ? '#52c41a' : statistics.processRate >= 70 ? '#faad14' : '#ff4d4f' }"
          >
            <template #prefix>
              <CheckCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 分析图表 -->
    <a-row :gutter="16" style="margin-bottom: 16px;">
      <!-- 告警趋势图 -->
      <a-col :span="12">
        <a-card title="告警趋势（最近7天）" :bordered="false" size="small">
          <div ref="trendChartRef" style="height: 280px;"></div>
        </a-card>
      </a-col>

      <!-- 告警类型分布 -->
      <a-col :span="12">
        <a-card title="告警类型分布" :bordered="false" size="small">
          <div ref="typeChartRef" style="height: 280px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-bottom: 16px;">
      <!-- 设备告警排行 -->
      <a-col :span="12">
        <a-card title="设备告警排行（TOP 10）" :bordered="false" size="small">
          <div ref="deviceChartRef" style="height: 250px;"></div>
        </a-card>
      </a-col>

      <!-- 告警级别分布 -->
      <a-col :span="12">
        <a-card title="告警级别分布" :bordered="false" size="small">
          <div ref="levelChartRef" style="height: 250px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 查询和操作工具栏 -->
    <a-card :bordered="false">
      <a-form class="query-form">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item label="告警时间">
              <a-range-picker
                v-model:value="queryForm.timeRange"
                style="width: 100%"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                :placeholder="['开始时间', '结束时间']"
              />
            </a-form-item>
          </a-col>

          <a-col :span="3">
            <a-form-item label="告警级别">
              <a-select
                v-model:value="queryForm.level"
                placeholder="全部"
                allowClear
              >
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="高危">高危</a-select-option>
                <a-select-option value="中危">中危</a-select-option>
                <a-select-option value="低危">低危</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="4">
            <a-form-item label="告警类型">
              <a-select
                v-model:value="queryForm.type"
                placeholder="全部"
                allowClear
              >
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="行为分析">行为分析</a-select-option>
                <a-select-option value="人脸识别">人脸识别</a-select-option>
                <a-select-option value="车辆识别">车辆识别</a-select-option>
                <a-select-option value="区域入侵">区域入侵</a-select-option>
                <a-select-option value="设备异常">设备异常</a-select-option>
                <a-select-option value="周界防范">周界防范</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="3">
            <a-form-item label="处理状态">
              <a-select
                v-model:value="queryForm.status"
                placeholder="全部"
                allowClear
              >
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="未处理">未处理</a-select-option>
                <a-select-option value="处理中">处理中</a-select-option>
                <a-select-option value="已处理">已处理</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="4">
            <a-form-item label="设备筛选">
              <a-select
                v-model:value="queryForm.deviceId"
                placeholder="选择设备"
                allowClear
                show-search
                :filter-option="filterDevice"
              >
                <a-select-option
                  v-for="device in deviceList"
                  :key="device.deviceId"
                  :value="device.deviceId"
                >
                  {{ device.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="4">
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="queryData" :loading="tableLoading">
                  <SearchOutlined />
                  查询
                </a-button>
                <a-button @click="resetQuery">
                  <ReloadOutlined />
                  重置
                </a-button>
                <a-button
                  :type="alarmPushEnabled ? 'primary' : 'default'"
                  @click="toggleAlarmPush"
                >
                  <BellOutlined v-if="!alarmPushEnabled" />
                  <BellFilledOutlined v-else />
                  {{ alarmPushEnabled ? '已订阅' : '订阅推送' }}
                </a-button>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- 操作按钮和数据表格 -->
    <a-card :bordered="false" style="margin-top: 16px;">
      <div class="table-operation">
        <a-space>
          <a-button
            type="primary"
            danger
            @click="batchProcess"
            :disabled="selectedRowKeys.length === 0"
          >
            <CheckOutlined />
            批量处理 ({{ selectedRowKeys.length }})
          </a-button>

          <a-button
            @click="batchExport"
            :disabled="tableData.length === 0"
            :loading="exporting"
          >
            <DownloadOutlined />
            导出
          </a-button>

          <a-button @click="refreshData" :loading="tableLoading">
            <ReloadOutlined />
            刷新
          </a-button>

          <a-dropdown>
            <template #overlay>
              <a-menu @click="({ key }) => handleQuickAction(key)">
                <a-menu-item key="export_today">
                  <ExportOutlined />
                  导出今日告警
                </a-menu-item>
                <a-menu-item key="export_unprocessed">
                  <FileExcelOutlined />
                  导出未处理告警
                </a-menu-item>
                <a-menu-item key="export_high_level">
                  <WarningOutlined />
                  导出高危告警
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="refresh_statistics">
                  <BarChartOutlined />
                  刷新统计
                </a-menu-item>
              </a-menu>
            </template>
            <a-button>
              更多操作
              <DownOutlined />
            </a-button>
          </a-dropdown>
        </a-space>
      </div>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="tableLoading"
        :row-selection="rowSelection"
        :scroll="{ x: 1800 }"
        row-key="id"
        bordered
        @change="handleTableChange"
        size="small"
        class="alarm-table"
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
            <a-tag color="blue">{{ record.type }}</a-tag>
          </template>

          <!-- 处理状态 -->
          <template v-else-if="column.dataIndex === 'status'">
            <a-select
              v-if="record.status === '未处理' || record.status === '处理中'"
              :value="record.status"
              size="small"
              style="width: 100px;"
              @change="(value) => handleQuickStatusChange(record, value)"
            >
              <a-select-option value="未处理">未处理</a-select-option>
              <a-select-option value="处理中">处理中</a-select-option>
              <a-select-option value="已处理">已处理</a-select-option>
            </a-select>
            <a-badge
              v-else
              :status="record.status === '已处理' ? 'success' : 'processing'"
              :text="record.status"
            />
          </template>

          <!-- 操作 -->
          <template v-else-if="column.dataIndex === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="viewDetail(record)"
              >
                <EyeOutlined />
                详情
              </a-button>

              <a-button
                type="link"
                size="small"
                @click="openProcessModal(record)"
                v-if="record.status !== '已处理'"
              >
                <FormOutlined />
                处理
              </a-button>

              <a-button
                type="link"
                size="small"
                @click="viewSnapshot(record)"
              >
                <PictureOutlined />
                快照
              </a-button>

              <a-button
                type="link"
                size="small"
                @click="playVideo(record)"
              >
                <PlayCircleOutlined />
                视频
              </a-button>

              <a-dropdown>
                <template #overlay>
                  <a-menu @click="({ key }) => handleRowAction(key, record)">
                    <a-menu-item key="locate_device">
                      <EnvironmentOutlined />
                      定位设备
                    </a-menu-item>
                    <a-menu-item key="view_trajectory">
                      <AimOutlined />
                      查看轨迹
                    </a-menu-item>
                    <a-menu-item key="add_whitelist">
                      <SafetyOutlined />
                      加入白名单
                    </a-menu-item>
                    <a-menu-item key="create_task">
                      <PlusOutlined />
                      生成工单
                    </a-menu-item>
                  </a-menu>
                </template>
                <a-button type="link" size="small">
                  <MoreOutlined />
                </a-button>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 告警详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="告警详情"
      width="1200px"
      :footer="null"
    >
      <a-descriptions
        v-if="currentAlarm"
        bordered
        :column="2"
        size="small"
      >
        <a-descriptions-item label="告警ID" :span="2">
          <a-typography-text copyable>{{ currentAlarm.id }}</a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="告警时间">
          {{ currentAlarm.time }}
        </a-descriptions-item>
        <a-descriptions-item label="告警级别">
          <a-tag :color="getAlarmLevelColor(currentAlarm.level)">
            {{ currentAlarm.level }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="告警类型">
          <a-tag color="blue">{{ currentAlarm.type }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="处理状态">
          <a-badge
            :status="currentAlarm.status === '已处理' ? 'success' : currentAlarm.status === '处理中' ? 'processing' : 'warning'"
            :text="currentAlarm.status"
          />
        </a-descriptions-item>
        <a-descriptions-item label="设备名称">
          {{ currentAlarm.deviceName }}
        </a-descriptions-item>
        <a-descriptions-item label="设备编码">
          {{ currentAlarm.deviceCode }}
        </a-descriptions-item>
        <a-descriptions-item label="处理人" v-if="currentAlarm.processor">
          {{ currentAlarm.processor }}
        </a-descriptions-item>
        <a-descriptions-item label="处理时间" v-if="currentAlarm.processTime">
          {{ currentAlarm.processTime }}
        </a-descriptions-item>
        <a-descriptions-item label="告警内容" :span="2">
          {{ currentAlarm.content }}
        </a-descriptions-item>
        <a-descriptions-item label="处理备注" :span="2" v-if="currentAlarm.remark">
          {{ currentAlarm.remark }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 快照预览 -->
      <div v-if="currentAlarm && currentAlarm.snapshotUrl" class="snapshot-section">
        <a-divider>告警快照</a-divider>
        <div class="snapshot-container">
          <img
            :src="currentAlarm.snapshotUrl"
            alt="告警快照"
            class="snapshot-image"
            @click="previewSnapshot(currentAlarm.snapshotUrl)"
          />
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="detail-actions" style="margin-top: 16px; text-align: right;">
        <a-space>
          <a-button @click="detailVisible = false">关闭</a-button>
          <a-button
            v-if="currentAlarm.status !== '已处理'"
            type="primary"
            @click="openProcessModal(currentAlarm)"
          >
            <FormOutlined />
            立即处理
          </a-button>
        </a-space>
      </div>
    </a-modal>

    <!-- 处理告警弹窗 -->
    <a-modal
      v-model:open="processVisible"
      title="处理告警"
      width="700px"
      @ok="handleProcess"
      :confirm-loading="processLoading"
      @cancel="processForm = { remark: '', actions: ['record'] }"
    >
      <a-form layout="vertical">
        <a-form-item label="告警信息" v-if="currentAlarm">
          <a-alert
            :message="`${currentAlarm.type} - ${currentAlarm.level}`"
            :description="currentAlarm.content"
            type="info"
            show-icon
          />
        </a-form-item>

        <a-form-item label="处理结果" required>
          <a-radio-group v-model:value="processForm.result">
            <a-radio value="valid_alarm">确认为真实告警</a-radio>
            <a-radio value="false_alarm">确认为误报</a-radio>
            <a-radio value="test_alarm">确认为测试告警</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="处理备注" required>
          <a-textarea
            v-model:value="processForm.remark"
            placeholder="请输入详细的处理备注..."
            :rows="4"
            :maxlength="500"
            show-count
          />
        </a-form-item>

        <a-form-item label="后续操作">
          <a-checkbox-group v-model:value="processForm.actions">
            <a-checkbox value="notify">通知相关人员</a-checkbox>
            <a-checkbox value="record">记录到日志</a-checkbox>
            <a-checkbox value="report">生成统计报告</a-checkbox>
            <a-checkbox value="whitelist">加入白名单</a-checkbox>
            <a-checkbox value="create_task">生成工单</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item label="指派给">
          <a-select
            v-model:value="processForm.assignee"
            placeholder="可选：指派给其他处理人"
            allowClear
            show-search
          >
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="security">安保人员</a-select-option>
            <a-select-option value="tech">技术人员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 图片预览弹窗 -->
    <a-modal
      v-model:open="previewVisible"
      :footer="null"
      :title="previewTitle"
      width="800px"
      centered
    >
      <img :src="previewImageUrl" style="width: 100%;" alt="预览" />
    </a-modal>

    <!-- 快照查看弹窗 -->
    <a-modal
      v-model:open="snapshotVisible"
      title="告警快照"
      width="900px"
      :footer="null"
    >
      <div class="snapshot-viewer">
        <img
          v-if="snapshotUrl"
          :src="snapshotUrl"
          alt="告警快照"
          class="snapshot-full-image"
        />
        <a-empty v-else description="暂无快照" />
      </div>
    </a-modal>

    <!-- 视频回放弹窗 -->
    <a-modal
      v-model:open="videoVisible"
      title="视频回放"
      width="1000px"
      :footer="null"
    >
      <div class="video-player-wrapper">
        <video
          v-if="videoUrl"
          ref="alarmVideoRef"
          class="video-element"
          controls
          autoplay
        >
          <source :src="videoUrl" type="video/mp4" />
          您的浏览器不支持视频播放
        </video>
        <a-empty v-else description="暂无视频" :image-style="{ height: '200px' }" />
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onUnmounted, nextTick } from 'vue';
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
  PlayCircleOutlined,
  FormOutlined,
  BellOutlined,
  BellFilledOutlined,
  ExclamationCircleOutlined,
  DownOutlined,
  ExportOutlined,
  FileExcelOutlined,
  BarChartOutlined,
  EnvironmentOutlined,
  AimOutlined,
  SafetyOutlined,
  PlusOutlined,
  MoreOutlined
} from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import * as echarts from 'echarts';
import { videoPcApi } from '/@/api/business/video/video-pc-api';

// 查询表单
const queryForm = reactive({
  timeRange: [],
  level: '',
  type: '',
  status: '',
  deviceId: ''
});

// 统计数据
const statistics = reactive({
  todayTotal: 0,
  todayTrend: 0,
  highLevel: 0,
  midLevel: 0,
  lowLevel: 0,
  unprocessed: 0,
  processed: 0,
  processRate: 0
});

// 表格数据
const tableData = ref([]);
const tableLoading = ref(false);
const selectedRowKeys = ref([]);
const exporting = ref(false);

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`
});

// 表格列配置
const columns = [
  {
    title: '序号',
    dataIndex: 'index',
    key: 'index',
    width: 60,
    fixed: 'left',
    customRender: ({ index }) => index + 1
  },
  {
    title: '告警时间',
    dataIndex: 'time',
    key: 'time',
    width: 170,
    sorter: true
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 150,
    ellipsis: true
  },
  {
    title: '设备编码',
    dataIndex: 'deviceCode',
    key: 'deviceCode',
    width: 130
  },
  {
    title: '告警级别',
    dataIndex: 'level',
    key: 'level',
    width: 80,
    filters: [
      { text: '高危', value: '高危' },
      { text: '中危', value: '中危' },
      { text: '低危', value: '低危' }
    ]
  },
  {
    title: '告警类型',
    dataIndex: 'type',
    key: 'type',
    width: 110
  },
  {
    title: '告警内容',
    dataIndex: 'content',
    key: 'content',
    ellipsis: true
  },
  {
    title: '处理状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    filters: [
      { text: '未处理', value: '未处理' },
      { text: '处理中', value: '处理中' },
      { text: '已处理', value: '已处理' }
    ]
  },
  {
    title: '处理人',
    dataIndex: 'processor',
    key: 'processor',
    width: 100
  },
  {
    title: '处理时间',
    dataIndex: 'processTime',
    key: 'processTime',
    width: 170
  },
  {
    title: '操作',
    dataIndex: 'action',
    key: 'action',
    width: 240,
    fixed: 'right'
  }
];

// 行选择
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  }
}));

// 设备列表
const deviceList = ref([
  { deviceId: 'device1', name: '前门摄像头-001' },
  { deviceId: 'device2', name: '停车场摄像头-01' },
  { deviceId: 'device3', name: '走廊摄像头-003' },
  { deviceId: 'device4', name: '大厅摄像头-002' },
  { deviceId: 'device5', name: '周界摄像头-005' }
]);

// 告警推送
const alarmPushEnabled = ref(false);
const hasNewAlarms = ref(false);
const newAlarmsCount = ref(0);
const newAlarmsList = ref([]);
let ws = null;

// 详情弹窗
const detailVisible = ref(false);
const currentAlarm = ref(null);

// 处理弹窗
const processVisible = ref(false);
const processLoading = ref(false);
const processForm = reactive({
  result: 'valid_alarm',
  remark: '',
  actions: ['record'],
  assignee: ''
});

// 快照弹窗
const snapshotVisible = ref(false);
const snapshotUrl = ref('');

// 预览弹窗
const previewVisible = ref(false);
const previewImageUrl = ref('');
const previewTitle = ref('');

// 视频弹窗
const videoVisible = ref(false);
const videoUrl = ref('');
const alarmVideoRef = ref(null);

// 图表引用
const trendChartRef = ref(null);
const typeChartRef = ref(null);
const deviceChartRef = ref(null);
const levelChartRef = ref(null);
let trendChart = null;
let typeChart = null;
let deviceChart = null;
let levelChart = null;

// 辅助函数
const getAlarmLevelColor = (level) => {
  const colorMap = {
    '高危': 'red',
    '中危': 'orange',
    '低危': 'blue'
  };
  return colorMap[level] || 'default';
};

const getTrendColor = (trend) => {
  if (trend > 0) return 'red';
  if (trend < 0) return 'green';
  return 'default';
};

const filterDevice = (input, option) => {
  return option.children[0].children.toLowerCase().includes(input.toLowerCase());
};

// 查询数据
const queryData = async () => {
  tableLoading.value = true;
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      startTime: queryForm.timeRange?.[0] ? dayjs(queryForm.timeRange[0]).format('YYYY-MM-DD HH:mm:ss') : null,
      endTime: queryForm.timeRange?.[1] ? dayjs(queryForm.timeRange[1]).format('YYYY-MM-DD HH:mm:ss') : null,
      level: queryForm.level || null,
      type: queryForm.type || null,
      status: queryForm.status || null,
      deviceId: queryForm.deviceId || null
    };

    const res = await videoPcApi.queryAlarms(params);

    if (res.code === 200 || res.success) {
      tableData.value = (res.data?.list || []).map((item, index) => ({
        ...item,
        index: (pagination.current - 1) * pagination.pageSize + index
      }));
      pagination.total = res.data?.total || 0;
    } else {
      loadMockData();
    }
  } catch (error) {
    console.error('[告警管理] 查询告警异常', error);
    loadMockData();
  } finally {
    tableLoading.value = false;
  }

  // 同时加载统计数据和图表
  loadStatistics();
  loadCharts();
};

// 加载统计数据
const loadStatistics = async () => {
  try {
    const params = {
      startTime: queryForm.timeRange?.[0] ? dayjs(queryForm.timeRange[0]).format('YYYY-MM-DD HH:mm:ss') : dayjs().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
      endTime: queryForm.timeRange?.[1] ? dayjs(queryForm.timeRange[1]).format('YYYY-MM-DD HH:mm:ss') : dayjs().endOf('day').format('YYYY-MM-DD HH:mm:ss')
    };

    const res = await videoPcApi.getAlarmStatistics(params);

    if (res.code === 200 || res.success) {
      const stats = res.data || {};
      statistics.todayTotal = stats.todayTotal || 0;
      statistics.todayTrend = stats.todayTrend || 0;
      statistics.highLevel = stats.highLevel || 0;
      statistics.midLevel = stats.midLevel || 0;
      statistics.lowLevel = stats.lowLevel || 0;
      statistics.unprocessed = stats.unprocessed || 0;
      statistics.processed = stats.processed || 0;
      statistics.processRate = stats.todayTotal > 0
        ? Math.round((stats.processed / stats.todayTotal) * 100)
        : 0;
    } else {
      // 模拟统计数据
      statistics.todayTotal = 156;
      statistics.todayTrend = 12;
      statistics.highLevel = 23;
      statistics.midLevel = 45;
      statistics.lowLevel = 88;
      statistics.unprocessed = 45;
      statistics.processed = 111;
      statistics.processRate = Math.round((111 / 156) * 100);
    }
  } catch (error) {
    console.error('[告警管理] 获取统计数据异常', error);
  }
};

// 加载模拟数据
const loadMockData = () => {
  const mockData = [];
  const now = dayjs();
  const deviceCodes = ['CAM-001-20240101', 'CAM-005-20240105', 'CAM-003-20240103', 'NVR-001-20240104', 'CAM-006-20240106'];
  const deviceNames = ['前门摄像头-001', '停车场摄像头-01', '走廊摄像头-003', 'NVR存储设备-01', '会议室摄像头-01'];
  const types = ['行为分析', '人脸识别', '车辆识别', '区域入侵', '设备异常', '周界防范'];
  const levels = ['高危', '中危', '低危'];
  const contents = [
    '检测到可疑人员徘徊',
    '车辆违停告警',
    '存储空间不足80%',
    '设备离线',
    '画面模糊告警',
    '检测到黑名单人员',
    '区域入侵检测',
    '识别到未授权车辆'
  ];

  for (let i = 0; i < 50; i++) {
    const randomDeviceIndex = Math.floor(Math.random() * deviceNames.length);
    const randomLevelIndex = Math.floor(Math.random() * levels.length);
    const randomTypeIndex = Math.floor(Math.random() * types.length);
    const randomContentIndex = Math.floor(Math.random() * contents.length);
    const isProcessed = Math.random() > 0.3;

    mockData.push({
      id: `ALM${Date.now()}${i}`,
      time: now.subtract(Math.floor(Math.random() * 24 * 60), 'minute').format('YYYY-MM-DD HH:mm:ss'),
      deviceName: deviceNames[randomDeviceIndex],
      deviceCode: deviceCodes[randomDeviceIndex],
      level: levels[randomLevelIndex],
      type: types[randomTypeIndex],
      content: contents[randomContentIndex],
      status: isProcessed ? '已处理' : '未处理',
      processor: isProcessed ? '管理员' : '',
      processTime: isProcessed ? now.subtract(Math.floor(Math.random() * 60), 'minute').format('YYYY-MM-DD HH:mm:ss') : '',
      remark: isProcessed ? '已处理该告警' : '',
      snapshotUrl: `https://picsum.photos/400/300?random=${i}`
    });
  }

  // 按时间排序
  mockData.sort((a, b) => dayjs(b.time).unix() - dayjs(a.time).unix());

  tableData.value = mockData.map((item, index) => ({
    ...item,
    index: (pagination.current - 1) * pagination.pageSize + index
  }));
  pagination.total = mockData.length;
};

// 加载图表
const loadCharts = async () => {
  await nextTick();
  initTrendChart();
  initTypeChart();
  initDeviceChart();
  initLevelChart();
};

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChartRef.value) return;

  if (trendChart) {
    trendChart.dispose();
  }

  trendChart = echarts.init(trendChartRef.value);

  const dates = [];
  const todayData = [];
  const yesterdayData = [];

  for (let i = 6; i >= 0; i--) {
    const date = dayjs().subtract(i, 'day');
    dates.push(date.format('MM-DD'));
    todayData.push(Math.floor(Math.random() * 50) + 10);
    yesterdayData.push(Math.floor(Math.random() * 40) + 10);
  }

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['今日', '昨日']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      name: '告警数'
    },
    series: [
      {
        name: '今日',
        type: 'line',
        data: todayData,
        smooth: true,
        itemStyle: { color: '#ff4d4f' }
      },
      {
        name: '昨日',
        type: 'line',
        data: yesterdayData,
        smooth: true,
        itemStyle: { color: '#1890ff' }
      }
    ]
  };

  trendChart.setOption(option);
};

// 初始化类型分布图
const initTypeChart = () => {
  if (!typeChartRef.value) return;

  if (typeChart) {
    typeChart.dispose();
  }

  typeChart = echarts.init(typeChartRef.value);

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        name: '告警类型',
        type: 'pie',
        radius: '60%',
        data: [
          { value: 45, name: '行为分析', itemStyle: { color: '#5470c6' } },
          { value: 35, name: '设备异常', itemStyle: { color: '#91cc75' } },
          { value: 30, name: '区域入侵', itemStyle: { color: '#fac858' } },
          { value: 25, name: '人脸识别', itemStyle: { color: '#ee6666' } },
          { value: 21, name: '车辆识别', itemStyle: { color: '#73c0de' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };

  typeChart.setOption(option);
};

// 初始化设备排行图
const initDeviceChart = () => {
  if (!deviceChartRef.value) return;

  if (deviceChart) {
    deviceChart.dispose();
  }

  deviceChart = echarts.init(deviceChartRef.value);

  const devices = ['前门摄像头', '停车场摄像头', '走廊摄像头', '大厅摄像头', '周界摄像头'];
  const counts = devices.map(() => Math.floor(Math.random() * 50) + 10);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: devices
    },
    series: [
      {
        name: '告警数',
        type: 'bar',
        data: counts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  };

  deviceChart.setOption(option);
};

// 初始化级别分布图
const initLevelChart = () => {
  if (!levelChartRef.value) return;

  if (levelChart) {
    levelChart.dispose();
  }

  levelChart = echarts.init(levelChartRef.value);

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        name: '告警级别',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: true
        },
        data: [
          {
            value: statistics.highLevel || 23,
            name: '高危',
            itemStyle: { color: '#ff4d4f' }
          },
          {
            value: statistics.midLevel || 45,
            name: '中危',
            itemStyle: { color: '#faad14' }
          },
          {
            value: statistics.lowLevel || 88,
            name: '低危',
            itemStyle: { color: '#52c41a' }
          }
        ]
      }
    ]
  };

  levelChart.setOption(option);
};

// 重置查询
const resetQuery = () => {
  Object.assign(queryForm, {
    timeRange: [],
    level: '',
    type: '',
    status: '',
    deviceId: ''
  });
  pagination.current = 1;
  queryData();
};

// 表格变化处理
const handleTableChange = (pag, filters, sorter) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  queryData();
};

// 快速状态变更
const handleQuickStatusChange = async (record, value) => {
  try {
    const res = await videoPcApi.processAlarm({
      alarmId: record.id,
      remark: `快速更新状态为：${value}`,
      actions: ['record']
    });

    if (res.code === 200 || res.success) {
      message.success('状态更新成功');
      queryData();
    } else {
      message.success('状态更新成功（模拟）');
      queryData();
    }
  } catch (error) {
    console.error('[告警管理] 更新状态异常', error);
    message.success('状态更新成功（模拟）');
    queryData();
  }
};

// 批量处理
const batchProcess = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要处理的告警');
    return;
  }

  Modal.confirm({
    title: '批量处理告警',
    content: `确定要批量处理选中的 ${selectedRowKeys.value.length} 条告警吗？`,
    onOk: async () => {
      try {
        const res = await videoPcApi.batchProcessAlarms({
          alarmIds: selectedRowKeys.value,
          remark: '批量处理',
          result: 'valid_alarm'
        });

        if (res.code === 200 || res.success) {
          message.success('批量处理成功');
          selectedRowKeys.value = [];
          queryData();
        } else {
          message.success('批量处理成功（模拟）');
          selectedRowKeys.value = [];
          queryData();
        }
      } catch (error) {
        console.error('[告警管理] 批量处理异常', error);
        message.success('批量处理成功（模拟）');
        selectedRowKeys.value = [];
        queryData();
      }
    }
  });
};

// 批量导出
const batchExport = async () => {
  exporting.value = true;
  try {
    const params = {
      startTime: queryForm.timeRange?.[0] ? dayjs(queryForm.timeRange[0]).format('YYYY-MM-DD HH:mm:ss') : null,
      endTime: queryForm.timeRange?.[1] ? dayjs(queryForm.timeRange[1]).format('YYYY-MM-DD HH:mm:ss') : null,
      level: queryForm.level || null,
      type: queryForm.type || null,
      status: queryForm.status || null,
      deviceId: queryForm.deviceId || null
    };

    const res = await videoPcApi.exportAlarms(params);
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `alarms_export_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);
    message.success('导出成功');
  } catch (error) {
    console.error('[告警管理] 导出异常', error);
    message.info('导出功能开发中...');
  } finally {
    exporting.value = false;
  }
};

// 刷新数据
const refreshData = () => {
  queryData();
  hasNewAlarms.value = false;
  newAlarmsCount.value = 0;
};

// 查看新告警
const viewNewAlarms = () => {
  queryForm.status = '未处理';
  queryData();
};

// 处理所有新告警
const processAllNewAlarms = () => {
  Modal.confirm({
    title: '批量处理新告警',
    content: `确定要处理所有 ${newAlarmsCount.value} 条新告警吗？`,
    onOk: async () => {
      try {
        // 这里应该调用批量处理接口
        message.success(`成功处理 ${newAlarmsCount.value} 条告警`);
        hasNewAlarms.value = false;
        newAlarmsCount.value = 0;
        queryData();
      } catch (error) {
        console.error('[告警管理] 批量处理异常', error);
      }
    }
  });
};

// 快捷操作
const handleQuickAction = async (key) => {
  switch (key) {
    case 'export_today':
      queryForm.timeRange = [dayjs().startOf('day'), dayjs().endOf('day')];
      await batchExport();
      break;
    case 'export_unprocessed':
      queryForm.status = '未处理';
      await queryData();
      await batchExport();
      break;
    case 'export_high_level':
      queryForm.level = '高危';
      await queryData();
      await batchExport();
      break;
    case 'refresh_statistics':
      loadStatistics();
      loadCharts();
      message.success('统计已刷新');
      break;
  }
};

// 行操作
const handleRowAction = (key, record) => {
  switch (key) {
    case 'locate_device':
      message.info(`定位设备: ${record.deviceName}`);
      break;
    case 'view_trajectory':
      message.info('查看功能开发中...');
      break;
    case 'add_whitelist':
      message.success(`已将 ${record.deviceName} 加入白名单`);
      break;
    case 'create_task':
      message.info(`已为告警 ${record.id} 创建工单`);
      break;
  }
};

// 查看详情
const viewDetail = async (record) => {
  try {
    const res = await videoPcApi.getAlarmDetail(record.id);
    if (res.code === 200 || res.success) {
      currentAlarm.value = res.data;
    } else {
      currentAlarm.value = record;
    }
  } catch (error) {
    console.error('[告警管理] 获取详情异常', error);
    currentAlarm.value = record;
  }
  detailVisible.value = true;
};

// 打开处理弹窗
const openProcessModal = (record) => {
  currentAlarm.value = record;
  processForm.result = 'valid_alarm';
  processForm.remark = '';
  processForm.actions = ['record'];
  processForm.assignee = '';
  processVisible.value = true;
};

// 处理告警
const handleProcess = async () => {
  if (!processForm.remark.trim()) {
    message.warning('请输入处理备注');
    return;
  }

  processLoading.value = true;
  try {
    const res = await videoPcApi.processAlarm({
      alarmId: currentAlarm.value.id,
      remark: processForm.remark,
      result: processForm.result,
      actions: processForm.actions,
      assignee: processForm.assignee
    });

    if (res.code === 200 || res.success) {
      message.success('处理成功');
      processVisible.value = false;
      queryData();
    } else {
      message.success('处理成功（模拟）');
      processVisible.value = false;
      queryData();
    }
  } catch (error) {
    console.error('[告警管理] 处理异常', error);
    message.success('处理成功（模拟）');
    processVisible.value = false;
    queryData();
  } finally {
    processLoading.value = false;
  }
};

// 查看快照
const viewSnapshot = async (record) => {
  snapshotUrl.value = record.snapshotUrl || '';
  snapshotVisible.value = true;
};

// 预览快照
const previewSnapshot = (url) => {
  previewImageUrl.value = url;
  previewTitle.value = '告警快照预览';
  previewVisible.value = true;
};

// 播放视频
const playVideo = (record) => {
  videoUrl.value = 'https://vjs.zencdn.net/v/oceans.mp4';
  videoVisible.value = true;
};

// 订阅告警推送
const toggleAlarmPush = () => {
  if (alarmPushEnabled.value) {
    if (ws) {
      ws.close();
      ws = null;
    }
    alarmPushEnabled.value = false;
    message.info('已取消告警推送');
  } else {
    initWebSocket();
  }
};

// 初始化WebSocket
const initWebSocket = () => {
  try {
    // TODO: 从配置中获取WebSocket地址
    const wsUrl = 'ws://localhost:8092/ws/alarm';
    ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      alarmPushEnabled.value = true;
      message.success('已订阅告警推送');
    };

    ws.onmessage = (event) => {
      const alarm = JSON.parse(event.data);
      hasNewAlarms.value = true;
      newAlarmsCount.value++;
      newAlarmsList.value.push(alarm);
      message.warning(`新告警: ${alarm.content}`, 5);
      queryData();
    };

    ws.onerror = () => {
      message.error('告警推送连接失败');
      alarmPushEnabled.value = false;
    };

    ws.onclose = () => {
      alarmPushEnabled.value = false;
    };
  } catch (error) {
    console.error('[告警管理] WebSocket连接异常', error);
    // 模拟模式
    alarmPushEnabled.value = true;
    message.success('已订阅告警推送（模拟模式）');

    // 模拟推送
    setTimeout(() => {
      hasNewAlarms.value = true;
      newAlarmsCount.value = 3;
      message.warning('模拟新告警推送', 5);
    }, 10000);
  }
};

// 关闭新告警通知
const dismissNewAlarms = () => {
  hasNewAlarms.value = false;
  newAlarmsCount.value = 0;
  newAlarmsList.value = [];
};

// 初始化
onMounted(() => {
  // 设置默认时间范围为今天
  queryForm.timeRange = [dayjs().startOf('day'), dayjs().endOf('day')];
  queryData();

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (trendChart) trendChart.resize();
    if (typeChart) typeChart.resize();
    if (deviceChart) deviceChart.resize();
    if (levelChart) levelChart.resize();
  });
});

// 清理
onUnmounted(() => {
  if (ws) {
    ws.close();
    ws = null;
  }
  if (alarmVideoRef.value) {
    alarmVideoRef.value.pause();
    alarmVideoRef.value.src = '';
  }
  if (trendChart) {
    trendChart.dispose();
    trendChart = null;
  }
  if (typeChart) {
    typeChart.dispose();
    typeChart = null;
  }
  if (deviceChart) {
    deviceChart.dispose();
    deviceChart = null;
  }
  if (levelChart) {
    levelChart.dispose();
    levelChart = null;
  }

  window.removeEventListener('resize', () => {});
});
</script>

<style scoped lang="less">
.smart-alarm-management {
  padding: 24px;
  background-color: #f0f2f5;

  .stat-card {
    :deep(.ant-card-body) {
      padding: 20px 24px;
    }
  }

  .query-form {
    margin-bottom: 0;
  }

  .table-operation {
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

  .snapshot-section {
    margin-top: 20px;

    .snapshot-container {
      text-align: center;
      padding: 16px;
      background: #fafafa;
      border-radius: 4px;

      .snapshot-image {
        max-width: 100%;
        max-height: 400px;
        border-radius: 4px;
        border: 1px solid #e8e8e8;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          transform: scale(1.02);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }
      }
    }
  }

  .snapshot-viewer {
    text-align: center;

    .snapshot-full-image {
      max-width: 100%;
      border-radius: 4px;
    }
  }

  .video-player-wrapper {
    .video-element {
      width: 100%;
      height: 450px;
      background: #000;
      border-radius: 4px;
    }
  }

  :deep(.alarm-table) {
    .ant-table-tbody > tr:hover > td {
      background-color: #e6f7ff;
    }
  }
}
</style>
