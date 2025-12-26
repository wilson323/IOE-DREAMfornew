<template>
  <div class="rule-performance-test">
    <!-- 页面标题 -->
    <a-page-header
      title="规则性能测试"
      sub-title="执行规则性能测试，分析响应时间和吞吐量"
      @back="handleBack"
    >
      <template #extra>
        <a-space>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
          <a-button type="primary" @click="showTestConfigModal">
            <template #icon><PlayCircleOutlined /></template>
            新建测试
          </a-button>
        </a-space>
      </template>
    </a-page-header>

    <a-divider />

    <!-- 测试列表 -->
    <a-card class="table-card" :bordered="false">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :row-selection="rowSelection"
        row-key="testId"
      >
        <!-- 测试名称 -->
        <template #testName="{ record }">
          <a @click="handleViewDetail(record)" style="cursor: pointer; color: #1890ff;">
            <ExperimentOutlined style="margin-right: 4px" />
            {{ record.testName }}
          </a>
        </template>

        <!-- 测试类型 -->
        <template #testType="{ record }">
          <a-tag v-if="record.testType === 'SINGLE'" color="blue">单规则</a-tag>
          <a-tag v-else-if="record.testType === 'BATCH'" color="purple">批量规则</a-tag>
          <a-tag v-else-if="record.testType === 'CONCURRENT'" color="orange">并发测试</a-tag>
          <a-tag v-else>-</a-tag>
        </template>

        <!-- 测试状态 -->
        <template #testStatus="{ record }">
          <a-tag v-if="record.testStatus === 'RUNNING'" color="processing">
            <SyncOutlined :spin="true" style="margin-right: 4px" />
            运行中
          </a-tag>
          <a-tag v-else-if="record.testStatus === 'COMPLETED'" color="success">已完成</a-tag>
          <a-tag v-else-if="record.testStatus === 'CANCELLED'" color="default">已取消</a-tag>
          <a-tag v-else-if="record.testStatus === 'ERROR'" color="error">错误</a-tag>
          <a-tag v-else>-</a-tag>
        </template>

        <!-- 成功率 -->
        <template #successRate="{ record }">
          <a-progress
            :percent="parseFloat(record.successRate || 0)"
            :status="record.successRate >= 95 ? 'success' : record.successRate >= 80 ? 'normal' : 'exception'"
            :stroke-width="8"
            style="width: 100px"
          />
        </template>

        <!-- 平均响应时间 -->
        <template #avgResponseTime="{ record }">
          <span :style="{ color: record.avgResponseTime > 100 ? '#ff4d4f' : record.avgResponseTime > 50 ? '#faad14' : '#52c41a' }">
            {{ record.avgResponseTime }} ms
          </span>
        </template>

        <!-- 吞吐量 -->
        <template #throughput="{ record }">
          <a-statistic
            :value="record.throughput"
            :precision="2"
            suffix="TPS"
            :value-style="{ fontSize: '14px' }"
          />
        </template>

        <!-- 测试时间 -->
        <template #startTime="{ record }">
          <span>{{ formatDateTime(record.startTime) }}</span>
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-space>
            <a-button
              type="link"
              size="small"
              @click="handleViewDetail(record)"
            >
              <template #icon><EyeOutlined /></template>
              查看详情
            </a-button>
            <a-button
              v-if="record.testStatus === 'RUNNING'"
              type="link"
              size="small"
              danger
              @click="handleCancel(record)"
            >
              <template #icon><StopOutlined /></template>
              取消
            </a-button>
            <a-popconfirm
              v-else
              title="确定要删除这条测试记录吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record)"
            >
              <a-button type="link" size="small" danger>
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>

      <!-- 批量操作 -->
      <template #footer>
        <a-space v-if="hasSelected">
          <span>已选择 {{ selectedRowKeys.length }} 项</span>
          <a-button type="link" danger @click="handleBatchDelete">
            批量删除
          </a-button>
        </a-space>
      </template>
    </a-card>

    <!-- 测试配置弹窗 -->
    <a-modal
      v-model:visible="testConfigVisible"
      title="性能测试配置"
      width="700px"
      @ok="handleExecuteTest"
      :confirm-loading="executing"
    >
      <a-form ref="testConfigFormRef" :model="testConfigForm" :rules="testConfigRules" layout="vertical">
        <a-form-item label="测试名称" name="testName">
          <a-input
            v-model:value="testConfigForm.testName"
            placeholder="请输入测试名称"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="测试类型" name="testType">
          <a-select
            v-model:value="testConfigForm.testType"
            placeholder="请选择测试类型"
          >
            <a-select-option value="SINGLE">单规则测试</a-select-option>
            <a-select-option value="BATCH">批量规则测试</a-select-option>
            <a-select-option value="CONCURRENT">并发测试</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="选择规则" name="ruleIds" v-if="testConfigForm.testType !== 'SINGLE'">
          <a-select
            v-model:value="testConfigForm.ruleIds"
            mode="multiple"
            placeholder="请选择要测试的规则"
            style="width: 100%"
            :options="ruleOptions"
            :field-names="{ label: 'label', value: 'value' }"
          >
          </a-select>
        </a-form-item>

        <a-form-item label="并发用户数" name="concurrentUsers">
          <a-input-number
            v-model:value="testConfigForm.concurrentUsers"
            :min="1"
            :max="100"
            placeholder="并发用户数"
            style="width: 200px"
          />
          <span style="margin-left: 8px">个用户</span>
          <a-tooltip title="并发用户数决定同时执行测试的线程数量">
            <QuestionCircleOutlined style="margin-left: 4px; color: #999;" />
          </a-tooltip>
        </a-form-item>

        <a-form-item label="每用户请求数" name="requestsPerUser">
          <a-input-number
            v-model:value="testConfigForm.requestsPerUser"
            :min="1"
            :max="10000"
            placeholder="每用户请求数"
            style="width: 200px"
          />
          <span style="margin-left: 8px">次</span>
          <a-tooltip title="每个用户执行的测试请求次数">
            <QuestionCircleOutlined style="margin-left: 4px; color: #999;" />
          </a-tooltip>
        </a-form-item>

        <a-form-item label="请求间隔" name="requestIntervalMs">
          <a-input-number
            v-model:value="testConfigForm.requestIntervalMs"
            :min="0"
            :max="5000"
            placeholder="请求间隔（毫秒）"
            style="width: 200px"
          />
          <span style="margin-left: 8px">ms</span>
        </a-form-item>

        <a-form-item label="超时时间" name="timeoutMs">
          <a-input-number
            v-model:value="testConfigForm.timeoutMs"
            :min="5000"
            :max="300000"
            placeholder="超时时间（毫秒）"
            style="width: 200px"
          />
          <span style="margin-left: 8px">ms</span>
        </a-form-item>

        <a-form-item label="测试场景" name="testScenario">
          <a-select
            v-model:value="testConfigForm.testScenario"
            placeholder="请选择测试场景"
            allow-clear
          >
            <a-select-option value="NORMAL">正常场景</a-select-option>
            <a-select-option value="LATE">迟到场景</a-select-option>
            <a-select-option value="EARLY">早退场景</a-select-option>
            <a-select-option value="OVERTIME">加班场景</a-select-option>
            <a-select-option value="ABSENT">缺勤场景</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="保存详细结果">
          <a-switch v-model:checked="testConfigForm.saveDetailedResults" />
          <span style="margin-left: 8px">是否保存每次请求的详细结果</span>
        </a-form-item>

        <!-- 预估信息 -->
        <a-alert
          message="测试预估"
          type="info"
          show-icon
        >
          <template #description>
            <p>总请求数: <strong>{{ totalRequests }}</strong></p>
            <p>预估测试时长: <strong>{{ estimatedDuration }}</strong></p>
          </template>
        </a-alert>
      </a-form>
    </a-modal>

    <!-- 测试详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="性能测试详情"
      width="80%"
      :footer="null"
    >
      <a-spin :spinning="detailLoading">
        <a-descriptions bordered :column="2" v-if="currentRecord">
          <!-- 基本信息 -->
          <a-descriptions-item label="测试名称" :span="2">
            {{ currentRecord.testName }}
          </a-descriptions-item>

          <a-descriptions-item label="测试类型">
            <a-tag v-if="currentRecord.testType === 'SINGLE'" color="blue">单规则</a-tag>
            <a-tag v-else-if="currentRecord.testType === 'BATCH'" color="purple">批量规则</a-tag>
            <a-tag v-else-if="currentRecord.testType === 'CONCURRENT'" color="orange">并发测试</a-tag>
          </a-descriptions-item>

          <a-descriptions-item label="测试状态">
            <a-tag v-if="currentRecord.testStatus === 'RUNNING'" color="processing">
              <SyncOutlined :spin="true" style="margin-right: 4px" />
              运行中
            </a-tag>
            <a-tag v-else-if="currentRecord.testStatus === 'COMPLETED'" color="success">已完成</a-tag>
            <a-tag v-else-if="currentRecord.testStatus === 'CANCELLED'" color="default">已取消</a-tag>
            <a-tag v-else-if="currentRecord.testStatus === 'ERROR'" color="error">错误</a-tag>
          </a-descriptions-item>

          <a-descriptions-item label="规则数量">
            {{ currentRecord.ruleCount }}
          </a-descriptions-item>

          <a-descriptions-item label="并发用户数">
            {{ currentRecord.concurrentUsers }}
          </a-descriptions-item>

          <!-- 请求统计 -->
          <a-descriptions-item label="总请求数">
            <a-statistic :value="currentRecord.totalRequests" />
          </a-descriptions-item>

          <a-descriptions-item label="成功请求数">
            <a-statistic :value="currentRecord.successRequests" style="color: #52c41a" />
          </a-descriptions-item>

          <a-descriptions-item label="失败请求数">
            <a-statistic :value="currentRecord.failedRequests" style="color: #ff4d4f" />
          </a-descriptions-item>

          <a-descriptions-item label="成功率">
            <a-progress
              :percent="parseFloat(currentRecord.successRate || 0)"
              :status="currentRecord.successRate >= 95 ? 'success' : currentRecord.successRate >= 80 ? 'normal' : 'exception'"
              :stroke-width="12"
            />
          </a-descriptions-item>

          <!-- 响应时间统计 -->
          <a-descriptions-item label="最小响应时间">
            {{ currentRecord.minResponseTime }} ms
          </a-descriptions-item>

          <a-descriptions-item label="最大响应时间">
            {{ currentRecord.maxResponseTime }} ms
          </a-descriptions-item>

          <a-descriptions-item label="平均响应时间">
            <span :style="{ color: currentRecord.avgResponseTime > 100 ? '#ff4d4f' : currentRecord.avgResponseTime > 50 ? '#faad14' : '#52c41a', fontSize: '16px', fontWeight: 'bold' }">
              {{ currentRecord.avgResponseTime }} ms
            </span>
          </a-descriptions-item>

          <a-descriptions-item label="P50响应时间">
            {{ currentRecord.p50ResponseTime }} ms
          </a-descriptions-item>

          <a-descriptions-item label="P95响应时间">
            {{ currentRecord.p95ResponseTime }} ms
          </a-descriptions-item>

          <a-descriptions-item label="P99响应时间">
            {{ currentRecord.p99ResponseTime }} ms
          </a-descriptions-item>

          <!-- 吞吐量 -->
          <a-descriptions-item label="吞吐量（TPS）">
            <a-statistic
              :value="currentRecord.throughput"
              :precision="2"
              suffix=" TPS"
              :value-style="{ color: '#3f8600', fontSize: '18px' }"
            />
          </a-descriptions-item>

          <a-descriptions-item label="QPS">
            <a-statistic
              :value="currentRecord.qps"
              :precision="2"
              suffix=" QPS"
              :value-style="{ color: '#3f8600', fontSize: '18px' }"
            />
          </a-descriptions-item>

          <!-- 执行信息 -->
          <a-descriptions-item label="测试开始时间">
            {{ formatDateTime(currentRecord.startTime) }}
          </a-descriptions-item>

          <a-descriptions-item label="测试结束时间">
            {{ formatDateTime(currentRecord.endTime) }}
          </a-descriptions-item>

          <a-descriptions-item label="测试时长" :span="2">
            {{ currentRecord.durationSeconds }} 秒
          </a-descriptions-item>
        </a-descriptions>

        <!-- 性能指标卡片 -->
        <a-row :gutter="16" style="margin-top: 24px;">
          <a-col :span="8">
            <a-card title="响应时间分布" size="small">
              <div class="response-time-chart">
                <a-statistic
                  title="P50"
                  :value="currentRecord.p50ResponseTime"
                  suffix="ms"
                  :value-style="{ color: '#1890ff' }"
                />
                <a-statistic
                  title="P95"
                  :value="currentRecord.p95ResponseTime"
                  suffix="ms"
                  :value-style="{ color: '#faad14' }"
                />
                <a-statistic
                  title="P99"
                  :value="currentRecord.p99ResponseTime"
                  suffix="ms"
                  :value-style="{ color: '#ff4d4f' }"
                />
              </div>
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card title="请求统计" size="small">
              <div class="request-stats">
                <a-statistic
                  title="总请求"
                  :value="currentRecord.totalRequests"
                />
                <a-statistic
                  title="成功"
                  :value="currentRecord.successRequests"
                  :value-style="{ color: '#52c41a' }"
                />
                <a-statistic
                  title="失败"
                  :value="currentRecord.failedRequests"
                  :value-style="{ color: '#ff4d4f' }"
                />
              </div>
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card title="性能指标" size="small">
              <div class="performance-metrics">
                <a-statistic
                  title="吞吐量"
                  :value="currentRecord.throughput"
                  :precision="2"
                  suffix="TPS"
                />
                <a-statistic
                  title="QPS"
                  :value="currentRecord.qps"
                  :precision="2"
                  suffix="QPS"
                />
                <a-statistic
                  title="成功率"
                  :value="currentRecord.successRate"
                  suffix="%"
                  :precision="2"
                />
              </div>
            </a-card>
          </a-col>
        </a-row>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  ReloadOutlined,
  PlayCircleOutlined,
  ExperimentOutlined,
  EyeOutlined,
  DeleteOutlined,
  StopOutlined,
  SyncOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons-vue';
import rulePerformanceTestApi from '/@/api/business/attendance/rule-performance-test-api';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';

const router = useRouter();

// 表格数据
const tableData = ref([]);
const loading = ref(false);

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条记录`
});

// 行选择
const selectedRowKeys = ref([]);
const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  }
};

const hasSelected = computed(() => selectedRowKeys.value.length > 0);

// 表格列定义
const columns = [
  {
    title: '测试名称',
    key: 'testName',
    width: 250,
    slots: { customRender: 'testName' }
  },
  {
    title: '测试类型',
    key: 'testType',
    width: 120,
    slots: { customRender: 'testType' }
  },
  {
    title: '状态',
    key: 'testStatus',
    width: 120,
    slots: { customRender: 'testStatus' }
  },
  {
    title: '成功率',
    key: 'successRate',
    width: 150,
    slots: { customRender: 'successRate' }
  },
  {
    title: '平均响应时间',
    key: 'avgResponseTime',
    width: 150,
    slots: { customRender: 'avgResponseTime' }
  },
  {
    title: '吞吐量',
    key: 'throughput',
    width: 150,
    slots: { customRender: 'throughput' }
  },
  {
    title: '测试时间',
    key: 'startTime',
    width: 180,
    slots: { customRender: 'startTime' }
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    slots: { customRender: 'action' }
  }
];

// 测试配置弹窗
const testConfigVisible = ref(false);
const testConfigFormRef = ref();
const executing = ref(false);

const testConfigForm = reactive({
  testName: '',
  testType: 'CONCURRENT',
  ruleIds: [],
  concurrentUsers: 10,
  requestsPerUser: 100,
  requestIntervalMs: 100,
  timeoutMs: 60000,
  testScenario: 'NORMAL',
  saveDetailedResults: true
});

const testConfigRules = {
  testName: [
    { required: true, message: '请输入测试名称', trigger: 'blur' }
  ],
  testType: [
    { required: true, message: '请选择测试类型', trigger: 'change' }
  ],
  concurrentUsers: [
    { required: true, message: '请输入并发用户数', trigger: 'blur' }
  ],
  requestsPerUser: [
    { required: true, message: '请输入每用户请求数', trigger: 'blur' }
  ]
};

// 预估信息
const totalRequests = computed(() => {
  return testConfigForm.concurrentUsers * testConfigForm.requestsPerUser;
});

const estimatedDuration = computed(() => {
  const total = totalRequests.value;
  const interval = testConfigForm.requestIntervalMs || 0;
  // 估算：假设每次请求平均50ms + 间隔
  const avgTime = 50 + interval;
  const totalMs = total * avgTime;
  const seconds = Math.ceil(totalMs / 1000);

  if (seconds < 60) {
    return `${seconds}秒`;
  } else if (seconds < 3600) {
    return `${Math.ceil(seconds / 60)}分钟`;
  } else {
    return `${Math.ceil(seconds / 3600)}小时`;
  }
});

// 详情弹窗
const detailVisible = ref(false);
const detailLoading = ref(false);
const currentRecord = ref(null);

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const res = await rulePerformanceTestApi.getRecentTests(100);
    if (res.code === 200) {
      tableData.value = res.data || [];
      pagination.total = res.data?.length || 0;
    } else {
      message.error(res.message || '加载数据失败');
    }
  } catch (error) {
    console.error('加载性能测试失败:', error);
    message.error('加载数据失败');
  } finally {
    loading.value = false;
  }
};

// 显示测试配置弹窗
const showTestConfigModal = () => {
  testConfigVisible.value = true;
};

// 执行性能测试
const handleExecuteTest = async () => {
  try {
    await testConfigFormRef.value.validate();
    executing.value = true;

    const res = await rulePerformanceTestApi.executePerformanceTest(testConfigForm);
    if (res.code === 200) {
      message.success('性能测试执行成功');
      testConfigVisible.value = false;
      // 重置表单
      resetTestConfigForm();
      loadData();
    } else {
      message.error(res.message || '执行失败');
    }
  } catch (error) {
    console.error('执行性能测试失败:', error);
    message.error('执行失败');
  } finally {
    executing.value = false;
  }
};

// 重置表单
const resetTestConfigForm = () => {
  testConfigForm.testName = '';
  testConfigForm.testType = 'CONCURRENT';
  testConfigForm.ruleIds = [];
  testConfigForm.concurrentUsers = 10;
  testConfigForm.requestsPerUser = 100;
  testConfigForm.requestIntervalMs = 100;
  testConfigForm.timeoutMs = 60000;
  testConfigForm.testScenario = 'NORMAL';
  testConfigForm.saveDetailedResults = true;
};

// 查看详情
const handleViewDetail = async (record) => {
  detailVisible.value = true;
  detailLoading.value = true;
  currentRecord.value = record;

  try {
    const res = await rulePerformanceTestApi.getTestDetail(record.testId);
    if (res.code === 200) {
      currentRecord.value = res.data;
    } else {
      message.error(res.message || '加载详情失败');
    }
  } catch (error) {
    console.error('加载详情失败:', error);
    message.error('加载详情失败');
  } finally {
    detailLoading.value = false;
  }
};

// 取消测试
const handleCancel = async (record) => {
  try {
    await rulePerformanceTestApi.cancelTest(record.testId);
    message.success('测试已取消');
    loadData();
  } catch (error) {
    console.error('取消测试失败:', error);
    message.error('取消失败');
  }
};

// 删除测试
const handleDelete = async (record) => {
  try {
    await rulePerformanceTestApi.deleteTest(record.testId);
    message.success('删除成功');
    loadData();
  } catch (error) {
    console.error('删除失败:', error);
    message.error('删除失败');
  }
};

// 批量删除
const handleBatchDelete = () => {
  Modal.confirm({
    title: '批量删除确认',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条测试记录吗？`,
    onOk: async () => {
      try {
        const res = await rulePerformanceTestApi.batchDeleteTests(selectedRowKeys.value);
        if (res.code === 200) {
          message.success(`成功删除 ${selectedRowKeys.value.length} 条记录`);
          selectedRowKeys.value = [];
          loadData();
        } else {
          message.error(res.message || '批量删除失败');
        }
      } catch (error) {
        console.error('批量删除失败:', error);
        message.error('批量删除失败');
      }
    }
  });
};

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  // loadData(); // 如果需要后端分页，取消注释
};

// 刷新
const handleRefresh = () => {
  loadData();
};

// 返回
const handleBack = () => {
  router.back();
};

// 格式化日期时间
const formatDateTime = (dateTime) => {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '-';
};

// 组件挂载
onMounted(() => {
  loadData();
});
</script>

<style scoped lang="less">
.rule-performance-test {
  padding: 16px;

  .table-card {
    background: #fff;
  }

  .response-time-chart,
  .request-stats,
  .performance-metrics {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
}
</style>
