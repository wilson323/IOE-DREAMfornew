<template>
  <div class="rule-test-history">
    <!-- 页面标题 -->
    <a-page-header
      title="规则测试历史"
      sub-title="查看和管理规则测试历史记录"
      @back="handleBack"
    >
      <template #extra>
        <a-space>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
          <a-button type="primary" @click="showBatchDeleteModal" :disabled="!hasSelected">
            批量删除
          </a-button>
          <a-dropdown>
            <a-button>
              更多操作 <DownOutlined />
            </a-button>
            <template #overlay>
              <a-menu @click="handleMenuClick">
                <a-menu-item key="import">
                  <ImportOutlined />
                  导入测试数据
                </a-menu-item>
                <a-menu-item key="export">
                  <ExportOutlined />
                  导出历史数据
                </a-menu-item>
                <a-menu-item key="template">
                  <DownloadOutlined />
                  下载配置模板
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="cleanup">
                  <DeleteOutlined />
                  清理过期历史
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-space>
      </template>
    </a-page-header>

    <a-divider />

    <!-- 搜索表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="规则名称">
          <a-input
            v-model:value="queryForm.ruleName"
            placeholder="请输入规则名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="测试结果">
          <a-select
            v-model:value="queryForm.testResult"
            placeholder="请选择测试结果"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="MATCH">匹配</a-select-option>
            <a-select-option value="NOT_MATCH">不匹配</a-select-option>
            <a-select-option value="ERROR">错误</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="测试场景">
          <a-select
            v-model:value="queryForm.testScenario"
            placeholder="请选择测试场景"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="LATE">迟到</a-select-option>
            <a-select-option value="EARLY">早退</a-select-option>
            <a-select-option value="OVERTIME">加班</a-select-option>
            <a-select-option value="ABSENT">缺勤</a-select-option>
            <a-select-option value="NORMAL">正常</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="测试用户">
          <a-input
            v-model:value="queryForm.testUserName"
            placeholder="请输入测试用户名"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>

        <a-form-item label="时间范围">
          <a-range-picker
            v-model:value="dateRange"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            :placeholder="['开始时间', '结束时间']"
            style="width: 380px"
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon><RedoOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="总测试次数"
            :value="statistics.totalTests"
            :value-style="{ color: '#3f8600' }"
          >
            <template #prefix>
              <ExperimentOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="匹配成功"
            :value="statistics.matchedTests"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <CheckCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="匹配失败"
            :value="statistics.notMatchedTests"
            :value-style="{ color: '#faad14' }"
          >
            <template #prefix>
              <CloseCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="测试错误"
            :value="statistics.errorTests"
            :value-style="{ color: '#f5222d' }"
          >
            <template #prefix>
              <ExclamationCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 数据表格 -->
    <a-card class="table-card" style="margin-top: 16px" :bordered="false">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        row-key="historyId"
        @change="handleTableChange"
      >
        <!-- 规则名称 -->
        <template #ruleName="{ record }">
          <a @click="handleViewDetail(record)">{{ record.ruleName }}</a>
        </template>

        <!-- 测试结果标签 -->
        <template #testResult="{ record }">
          <a-tag v-if="record.testResult === 'MATCH'" color="success">
            <CheckCircleOutlined /> 匹配
          </a-tag>
          <a-tag v-else-if="record.testResult === 'NOT_MATCH'" color="warning">
            <CloseCircleOutlined /> 不匹配
          </a-tag>
          <a-tag v-else color="error">
            <ExclamationCircleOutlined /> 错误
          </a-tag>
        </template>

        <!-- 测试场景标签 -->
        <template #testScenario="{ record }">
          <a-tag v-if="record.testScenario === 'LATE'" color="orange">迟到</a-tag>
          <a-tag v-else-if="record.testScenario === 'EARLY'" color="blue">早退</a-tag>
          <a-tag v-else-if="record.testScenario === 'OVERTIME'" color="purple">加班</a-tag>
          <a-tag v-else-if="record.testScenario === 'ABSENT'" color="red">缺勤</a-tag>
          <a-tag v-else-if="record.testScenario === 'NORMAL'" color="green">正常</a-tag>
          <a-tag v-else>-</a-tag>
        </template>

        <!-- 条件匹配 -->
        <template #conditionMatched="{ record }">
          <a-tag v-if="record.conditionMatched" color="green">是</a-tag>
          <a-tag v-else color="red">否</a-tag>
        </template>

        <!-- 执行时间 -->
        <template #executionTime="{ record }">
          <span>{{ record.executionTime }} ms</span>
        </template>

        <!-- 测试时间 -->
        <template #testTimestamp="{ record }">
          <span>{{ formatDateTime(record.testTimestamp) }}</span>
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleViewDetail(record)">
              <template #icon><EyeOutlined /></template>
              查看详情
            </a-button>
            <a-popconfirm
              title="确定要删除这条测试历史吗？"
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
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="测试历史详情"
      width="80%"
      :footer="null"
    >
      <a-descriptions bordered :column="2" v-if="currentRecord">
        <a-descriptions-item label="测试ID">{{ currentRecord.testId }}</a-descriptions-item>
        <a-descriptions-item label="规则名称">{{ currentRecord.ruleName }}</a-descriptions-item>
        <a-descriptions-item label="测试结果">
          <a-tag v-if="currentRecord.testResult === 'MATCH'" color="success">匹配</a-tag>
          <a-tag v-else-if="currentRecord.testResult === 'NOT_MATCH'" color="warning">不匹配</a-tag>
          <a-tag v-else color="error">错误</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="条件匹配">
          <a-tag v-if="currentRecord.conditionMatched" color="green">是</a-tag>
          <a-tag v-else color="red">否</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="执行时间">{{ currentRecord.executionTime }} ms</a-descriptions-item>
        <a-descriptions-item label="测试场景">
          <a-tag v-if="currentRecord.testScenario === 'LATE'" color="orange">迟到</a-tag>
          <a-tag v-else-if="currentRecord.testScenario === 'EARLY'" color="blue">早退</a-tag>
          <a-tag v-else-if="currentRecord.testScenario === 'OVERTIME'" color="purple">加班</a-tag>
          <a-tag v-else-if="currentRecord.testScenario === 'ABSENT'" color="red">缺勤</a-tag>
          <a-tag v-else-if="currentRecord.testScenario === 'NORMAL'" color="green">正常</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="测试用户">{{ currentRecord.testUserName }}</a-descriptions-item>
        <a-descriptions-item label="测试时间">{{ formatDateTime(currentRecord.testTimestamp) }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ formatDateTime(currentRecord.createTime) }}</a-descriptions-item>

        <!-- 规则条件 -->
        <a-descriptions-item label="规则条件" :span="2">
          <pre class="json-code">{{ formatJson(currentRecord.ruleCondition) }}</pre>
        </a-descriptions-item>

        <!-- 规则动作 -->
        <a-descriptions-item label="规则动作" :span="2">
          <pre class="json-code">{{ formatJson(currentRecord.ruleAction) }}</pre>
        </a-descriptions-item>

        <!-- 测试输入数据 -->
        <a-descriptions-item label="测试输入" :span="2">
          <pre class="json-code">{{ formatJson(currentRecord.testInputData) }}</pre>
        </a-descriptions-item>

        <!-- 测试输出数据 -->
        <a-descriptions-item label="测试输出" :span="2">
          <pre class="json-code">{{ formatJson(currentRecord.testOutputData) }}</pre>
        </a-descriptions-item>

        <!-- 执行的动作 -->
        <a-descriptions-item v-if="currentRecord.executedActions && currentRecord.executedActions.length" label="执行动作" :span="2">
          <a-timeline>
            <a-timeline-item v-for="(action, index) in currentRecord.executedActions" :key="index">
              <div>
                <strong>{{ action.actionName }}</strong>
                <a-tag :color="action.executionStatus === 'SUCCESS' ? 'green' : 'red'">
                  {{ action.executionStatus }}
                </a-tag>
              </div>
              <div>值: {{ formatJson(action.actionValue) }}</div>
              <div>消息: {{ action.executionMessage }}</div>
              <div type="secondary" style="font-size: 12px">
                {{ formatDateTime(action.executionTimestamp) }}
              </div>
            </a-timeline-item>
          </a-timeline>
        </a-descriptions-item>

        <!-- 执行日志 -->
        <a-descriptions-item v-if="currentRecord.executionLogs && currentRecord.executionLogs.length" label="执行日志" :span="2">
          <a-timeline>
            <a-timeline-item v-for="(log, index) in currentRecord.executionLogs" :key="index">
              <div>
                <a-tag :color="getLogLevelColor(log.logLevel)">{{ log.logLevel }}</a-tag>
                <span>{{ log.logMessage }}</span>
              </div>
              <div v-if="log.logData" type="secondary" style="font-size: 12px">
                数据: {{ formatJson(log.logData) }}
              </div>
              <div type="secondary" style="font-size: 12px">
                {{ formatDateTime(log.logTimestamp) }}
              </div>
            </a-timeline-item>
          </a-timeline>
        </a-descriptions-item>

        <!-- 错误信息 -->
        <a-descriptions-item v-if="currentRecord.errorMessage" label="错误信息" :span="2">
          <a-alert :message="currentRecord.errorMessage" type="error" show-icon />
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 清理过期历史弹窗 -->
    <a-modal
      v-model:visible="cleanupVisible"
      title="清理过期测试历史"
      @ok="handleCleanup"
    >
      <a-form layout="vertical">
        <a-form-item label="保留天数">
          <a-input-number
            v-model:value="cleanupDays"
            :min="1"
            :max="365"
            placeholder="请输入保留天数"
            style="width: 200px"
          />
          <span style="margin-left: 8px">天</span>
        </a-form-item>
        <a-alert
          message="警告"
          description="此操作将删除指定天数之前的所有测试历史记录，操作不可恢复！"
          type="warning"
          show-icon
        />
      </a-form>
    </a-modal>

    <!-- 导入测试数据弹窗 -->
    <a-modal
      v-model:visible="importVisible"
      title="导入测试历史数据"
      width="600px"
      :confirm-loading="importLoading"
      @ok="handleImport"
    >
      <a-form layout="vertical">
        <a-form-item label="选择JSON文件">
          <a-upload
            :file-list="importFile ? [importFile] : []"
            :before-upload="() => false"
            @change="handleFileChange"
            accept=".json"
            :max-count="1"
          >
            <a-button>
              <UploadOutlined />
              选择文件
            </a-button>
          </a-upload>
          <div class="upload-tip">
            <a-typography-text type="secondary">
              支持导入JSON格式的测试历史数据文件。您可以通过"下载配置模板"获取模板文件。
            </a-typography-text>
          </div>
        </a-form-item>

        <a-alert
          message="导入说明"
          type="info"
          show-icon
        >
          <template #description>
            <ul>
              <li>导入的测试历史将生成新的历史ID</li>
              <li>如果导入的数据中包含已存在的测试ID，将创建新的记录</li>
              <li>导入过程会验证数据格式，格式错误的记录将被跳过</li>
              <li>建议先下载模板文件，按照模板格式准备数据</li>
            </ul>
          </template>
        </a-alert>

        <a-divider />

        <a-form-item label="快速开始">
          <a-space direction="vertical" style="width: 100%">
            <a-button block @click="downloadTemplate">
              <DownloadOutlined />
              下载配置模板
            </a-button>
            <a-typography-text type="secondary" style="font-size: 12px">
              下载模板文件，参考模板格式准备您的测试历史数据
            </a-typography-text>
          </a-space>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  ReloadOutlined,
  SearchOutlined,
  RedoOutlined,
  DeleteOutlined,
  EyeOutlined,
  DownOutlined,
  ExportOutlined,
  ImportOutlined,
  UploadOutlined,
  DownloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  ExperimentOutlined
} from '@ant-design/icons-vue';
import ruleTestHistoryApi from '/@/api/business/attendance/rule-test-history-api';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';

const router = useRouter();

// 查询表单
const queryForm = reactive({
  ruleName: '',
  testResult: '',
  testScenario: '',
  testUserName: '',
  startTime: null,
  endTime: null
});

// 日期范围
const dateRange = ref([]);

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

// 统计数据
const statistics = reactive({
  totalTests: 0,
  matchedTests: 0,
  notMatchedTests: 0,
  errorTests: 0
});

// 表格列定义
const columns = [
  {
    title: '规则名称',
    dataIndex: 'ruleName',
    key: 'ruleName',
    width: 200,
    slots: { customRender: 'ruleName' }
  },
  {
    title: '测试结果',
    dataIndex: 'testResult',
    key: 'testResult',
    width: 120,
    slots: { customRender: 'testResult' }
  },
  {
    title: '测试场景',
    dataIndex: 'testScenario',
    key: 'testScenario',
    width: 100,
    slots: { customRender: 'testScenario' }
  },
  {
    title: '条件匹配',
    dataIndex: 'conditionMatched',
    key: 'conditionMatched',
    width: 100,
    align: 'center',
    slots: { customRender: 'conditionMatched' }
  },
  {
    title: '执行时间',
    dataIndex: 'executionTime',
    key: 'executionTime',
    width: 120,
    slots: { customRender: 'executionTime' }
  },
  {
    title: '测试用户',
    dataIndex: 'testUserName',
    key: 'testUserName',
    width: 120
  },
  {
    title: '测试时间',
    dataIndex: 'testTimestamp',
    key: 'testTimestamp',
    width: 180,
    slots: { customRender: 'testTimestamp' }
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    slots: { customRender: 'action' }
  }
];

// 详情弹窗
const detailVisible = ref(false);
const currentRecord = ref(null);

// 清理弹窗
const cleanupVisible = ref(false);
const cleanupDays = ref(90);

// 导入导出弹窗
const importVisible = ref(false);
const importFile = ref(null);
const importLoading = ref(false);
const exportSelectedOnly = ref(false);

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      ...queryForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    };

    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0].format('YYYY-MM-DD HH:mm:ss');
      params.endTime = dateRange.value[1].format('YYYY-MM-DD HH:mm:ss');
    }

    const res = await ruleTestHistoryApi.queryHistoryPage(params);
    if (res.code === 200) {
      tableData.value = res.data.list || [];
      pagination.total = res.data.total || 0;

      // 更新统计数据
      updateStatistics(res.data.list || []);
    } else {
      message.error(res.message || '加载数据失败');
    }
  } catch (error) {
    console.error('加载测试历史失败:', error);
    message.error('加载数据失败');
  } finally {
    loading.value = false;
  }
};

// 更新统计数据
const updateStatistics = (data) => {
  statistics.totalTests = data.length;
  statistics.matchedTests = data.filter(item => item.testResult === 'MATCH').length;
  statistics.notMatchedTests = data.filter(item => item.testResult === 'NOT_MATCH').length;
  statistics.errorTests = data.filter(item => item.testResult === 'ERROR').length;
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadData();
};

// 重置
const handleReset = () => {
  queryForm.ruleName = '';
  queryForm.testResult = '';
  queryForm.testScenario = '';
  queryForm.testUserName = '';
  queryForm.startTime = null;
  queryForm.endTime = null;
  dateRange.value = [];
  pagination.current = 1;
  loadData();
};

// 刷新
const handleRefresh = () => {
  loadData();
};

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadData();
};

// 查看详情
const handleViewDetail = async (record) => {
  try {
    const res = await ruleTestHistoryApi.getHistoryDetail(record.historyId);
    if (res.code === 200) {
      currentRecord.value = res.data;
      detailVisible.value = true;
    } else {
      message.error(res.message || '加载详情失败');
    }
  } catch (error) {
    console.error('加载详情失败:', error);
    message.error('加载详情失败');
  }
};

// 删除单条记录
const handleDelete = async (record) => {
  try {
    const res = await ruleTestHistoryApi.deleteHistory(record.historyId);
    if (res.code === 200) {
      message.success('删除成功');
      loadData();
    } else {
      message.error(res.message || '删除失败');
    }
  } catch (error) {
    console.error('删除失败:', error);
    message.error('删除失败');
  }
};

// 批量删除
const showBatchDeleteModal = () => {
  Modal.confirm({
    title: '批量删除确认',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条测试历史吗？`,
    onOk: async () => {
      try {
        const res = await ruleTestHistoryApi.batchDeleteHistory(selectedRowKeys.value);
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

// 更多操作菜单
const handleMenuClick = ({ key }) => {
  if (key === 'import') {
    showImportModal();
  } else if (key === 'export') {
    handleExport();
  } else if (key === 'template') {
    downloadTemplate();
  } else if (key === 'cleanup') {
    cleanupVisible.value = true;
  }
};

// 清理过期历史
const handleCleanup = async () => {
  try {
    const res = await ruleTestHistoryApi.cleanExpiredHistory(cleanupDays.value);
    if (res.code === 200) {
      message.success(`成功清理 ${res.data} 条过期记录`);
      cleanupVisible.value = false;
      loadData();
    } else {
      message.error(res.message || '清理失败');
    }
  } catch (error) {
    console.error('清理失败:', error);
    message.error('清理失败');
  }
};

// 导出数据
const handleExport = async () => {
  try {
    // 如果没有选中记录，提示用户
    if (!hasSelected.value) {
      Modal.confirm({
        title: '导出确认',
        content: '您未选中任何记录。是否导出所有符合当前筛选条件的测试历史？',
        onOk: async () => {
          await exportAllData();
        }
      });
      return;
    }

    // 导出选中的记录
    await exportSelectedData();
  } catch (error) {
    console.error('导出失败:', error);
    message.error('导出失败');
  }
};

// 导出选中的数据
const exportSelectedData = async () => {
  try {
    const res = await ruleTestHistoryApi.exportHistoryToJson(selectedRowKeys.value);
    if (res.code === 200) {
      downloadJsonFile(res.data, `测试历史_${dayjs().format('YYYYMMDD_HHmmss')}.json`);
      message.success(`成功导出 ${selectedRowKeys.value.length} 条记录`);
    } else {
      message.error(res.message || '导出失败');
    }
  } catch (error) {
    console.error('导出选中数据失败:', error);
    message.error('导出失败');
  }
};

// 导出所有数据
const exportAllData = async () => {
  try {
    // 获取所有符合条件的数据ID
    const allIds = tableData.value.map(item => item.historyId);
    if (allIds.length === 0) {
      message.warning('没有可导出的数据');
      return;
    }

    const res = await ruleTestHistoryApi.exportHistoryToJson(allIds);
    if (res.code === 200) {
      downloadJsonFile(res.data, `测试历史_${dayjs().format('YYYYMMDD_HHmmss')}.json`);
      message.success(`成功导出 ${allIds.length} 条记录`);
    } else {
      message.error(res.message || '导出失败');
    }
  } catch (error) {
    console.error('导出所有数据失败:', error);
    message.error('导出失败');
  }
};

// 下载JSON文件
const downloadJsonFile = (jsonData, filename) => {
  const blob = new Blob([jsonData], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
};

// 显示导入弹窗
const showImportModal = () => {
  importVisible.value = true;
};

// 处理文件选择
const handleFileChange = (info) => {
  if (info.fileList.length > 0) {
    importFile.value = info.fileList[0];
  } else {
    importFile.value = null;
  }
};

// 导入数据
const handleImport = async () => {
  if (!importFile.value) {
    message.warning('请选择要导入的JSON文件');
    return;
  }

  importLoading.value = true;
  try {
    // 读取文件内容
    const reader = new FileReader();
    reader.onload = async (e) => {
      try {
        const jsonData = e.target.result;
        const res = await ruleTestHistoryApi.importHistoryFromJson(jsonData);

        if (res.code === 200) {
          message.success(`成功导入 ${res.data} 条测试历史记录`);
          importVisible.value = false;
          importFile.value = null;
          loadData();
        } else {
          message.error(res.message || '导入失败');
        }
      } catch (error) {
        console.error('导入数据解析失败:', error);
        message.error('导入失败，请检查JSON文件格式');
      } finally {
        importLoading.value = false;
      }
    };

    reader.onerror = () => {
      message.error('文件读取失败');
      importLoading.value = false;
    };

    reader.readAsText(importFile.value.originFileObj);
  } catch (error) {
    console.error('导入失败:', error);
    message.error('导入失败');
    importLoading.value = false;
  }
};

// 下载模板
const downloadTemplate = async () => {
  try {
    const res = await ruleTestHistoryApi.exportTestTemplate();
    if (res.code === 200) {
      downloadJsonFile(res.data, `测试历史模板_${dayjs().format('YYYYMMDD_HHmmss')}.json`);
      message.success('模板下载成功');
    } else {
      message.error(res.message || '模板下载失败');
    }
  } catch (error) {
    console.error('下载模板失败:', error);
    message.error('模板下载失败');
  }
};

// 返回
const handleBack = () => {
  router.back();
};

// 格式化日期时间
const formatDateTime = (dateTime) => {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '-';
};

// 格式化JSON
const formatJson = (data) => {
  if (!data) return '-';
  if (typeof data === 'string') {
    try {
      return JSON.stringify(JSON.parse(data), null, 2);
    } catch (e) {
      return data;
    }
  }
  return JSON.stringify(data, null, 2);
};

// 获取日志级别颜色
const getLogLevelColor = (level) => {
  const colorMap = {
    'INFO': 'blue',
    'DEBUG': 'default',
    'WARN': 'orange',
    'ERROR': 'red'
  };
  return colorMap[level] || 'default';
};

// 组件挂载
onMounted(() => {
  loadData();
});
</script>

<style scoped lang="less">
.rule-test-history {
  padding: 16px;

  .search-card {
    margin-bottom: 16px;
  }

  .table-card {
    background: #fff;
  }

  .json-code {
    background: #f5f5f5;
    padding: 8px;
    border-radius: 4px;
    font-size: 12px;
    max-height: 300px;
    overflow: auto;
  }

  .upload-tip {
    margin-top: 8px;
  }
}
</style>
