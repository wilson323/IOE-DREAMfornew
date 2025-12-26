<!--
 * 门禁反潜回配置页面
 *
 * 功能：
 * 1. 反潜回配置管理：4种反潜回模式配置（全局、区域、软、硬）
 * 2. 区域关联配置：选择反潜回生效的区域
 * 3. 时间窗口配置：设置反潜回检测的时间范围
 * 4. 检测记录查询：查询和处理反潜回违规记录
 * 5. 统计报表：反潜回检测统计和趋势分析
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="anti-passback-config-page">
    <a-card :bordered="false">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <!-- 反潜回配置管理 -->
        <a-tab-pane key="config" tab="反潜回配置">
          <!-- 查询表单 -->
          <a-form class="smart-query-form" layout="inline" style="margin-bottom: 16px">
            <a-row class="smart-query-form-row">
              <a-form-item label="配置名称" class="smart-query-form-item">
                <a-input
                  style="width: 200px"
                  v-model:value="configQueryForm.configName"
                  placeholder="请输入配置名称"
                  allow-clear
                />
              </a-form-item>

              <a-form-item label="反潜回模式" class="smart-query-form-item">
                <a-select
                  v-model:value="configQueryForm.mode"
                  style="width: 150px"
                  :allow-clear="true"
                  placeholder="请选择模式"
                >
                  <a-select-option :value="1">全局反潜回</a-select-option>
                  <a-select-option :value="2">区域反潜回</a-select-option>
                  <a-select-option :value="3">软反潜回</a-select-option>
                  <a-select-option :value="4">硬反潜回</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="启用状态" class="smart-query-form-item">
                <a-select
                  v-model:value="configQueryForm.isEnabled"
                  style="width: 120px"
                  :allow-clear="true"
                  placeholder="请选择状态"
                >
                  <a-select-option :value="1">启用</a-select-option>
                  <a-select-option :value="0">禁用</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item class="smart-query-form-item smart-margin-left10">
                <a-space>
                  <a-button type="primary" @click="queryConfigList">
                    <template #icon><SearchOutlined /></template>
                    查询
                  </a-button>
                  <a-button @click="resetConfigQuery">
                    <template #icon><ReloadOutlined /></template>
                    重置
                  </a-button>
                </a-space>
              </a-form-item>
            </a-row>
          </a-form>

          <!-- 操作按钮 -->
          <a-space style="margin-bottom: 16px">
            <a-button type="primary" @click="handleAddConfig">
              <template #icon><PlusOutlined /></template>
              新增配置
            </a-button>
            <a-button @click="handleBatchEnable">
              <template #icon><CheckOutlined /></template>
              批量启用
            </a-button>
            <a-button @click="handleBatchDisable">
              <template #icon><StopOutlined /></template>
              批量禁用
            </a-button>
            <a-button danger @click="handleBatchDelete">
              <template #icon><DeleteOutlined /></template>
              批量删除
            </a-button>
          </a-space>

          <!-- 配置列表表格 -->
          <a-table
            :columns="configColumns"
            :data-source="configTableData"
            :pagination="configPagination"
            :loading="configLoading"
            :row-selection="configRowSelection"
            row-key="configId"
            @change="handleConfigTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'mode'">
                <a-tag :color="getModeColor(record.mode)">
                  {{ getModeText(record.mode) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'isEnabled'">
                <a-switch
                  :checked="record.isEnabled === 1"
                  @change="handleToggleEnabled(record)"
                />
              </template>
              <template v-else-if="column.key === 'timeWindow'">
                {{ formatTimeWindow(record) }}
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="handleViewConfig(record)">
                    详情
                  </a-button>
                  <a-button type="link" size="small" @click="handleEditConfig(record)">
                    编辑
                  </a-button>
                  <a-button type="link" size="small" @click="handleToggleEnabled(record)">
                    {{ record.isEnabled === 1 ? '禁用' : '启用' }}
                  </a-button>
                  <a-popconfirm
                    title="确定要删除该配置吗？"
                    @confirm="handleDeleteConfig(record)"
                  >
                    <a-button type="link" size="small" danger>删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 检测记录查询 -->
        <a-tab-pane key="record" tab="检测记录">
          <!-- 查询表单 -->
          <a-form class="smart-query-form" layout="inline" style="margin-bottom: 16px">
            <a-row class="smart-query-form-row">
              <a-form-item label="用户姓名" class="smart-query-form-item">
                <a-input
                  style="width: 150px"
                  v-model:value="recordQueryForm.userName"
                  placeholder="请输入用户姓名"
                  allow-clear
                />
              </a-form-item>

              <a-form-item label="检测结果" class="smart-query-form-item">
                <a-select
                  v-model:value="recordQueryForm.detectResult"
                  style="width: 120px"
                  :allow-clear="true"
                  placeholder="请选择结果"
                >
                  <a-select-option value="VIOLATION">违规</a-select-option>
                  <a-select-option value="ALLOW">正常</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="处理状态" class="smart-query-form-item">
                <a-select
                  v-model:value="recordQueryForm.handleStatus"
                  style="width: 120px"
                  :allow-clear="true"
                  placeholder="请选择状态"
                >
                  <a-select-option value="PENDING">待处理</a-select-option>
                  <a-select-option value="ALLOWED">已放行</a-select-option>
                  <a-select-option value="DENIED">已阻止</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="检测时间" class="smart-query-form-item">
                <a-range-picker
                  v-model:value="recordQueryForm.detectTimeRange"
                  style="width: 300px"
                  :ranges="timeRanges"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                />
              </a-form-item>

              <a-form-item class="smart-query-form-item smart-margin-left10">
                <a-space>
                  <a-button type="primary" @click="queryRecordList">
                    <template #icon><SearchOutlined /></template>
                    查询
                  </a-button>
                  <a-button @click="resetRecordQuery">
                    <template #icon><ReloadOutlined /></template>
                    重置
                  </a-button>
                </a-space>
              </a-form-item>
            </a-row>
          </a-form>

          <!-- 操作按钮 -->
          <a-space style="margin-bottom: 16px">
            <a-button @click="handleBatchAllow">
              <template #icon><CheckOutlined /></template>
              批量放行
            </a-button>
            <a-button danger @click="handleBatchDeny">
              <template #icon><CloseOutlined /></template>
              批量阻止
            </a-button>
            <a-button @click="exportRecords">
              <template #icon><ExportOutlined /></template>
              导出记录
            </a-button>
          </a-space>

          <!-- 检测记录表格 -->
          <a-table
            :columns="recordColumns"
            :data-source="recordTableData"
            :pagination="recordPagination"
            :loading="recordLoading"
            :row-selection="recordRowSelection"
            row-key="recordId"
            @change="handleRecordTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'detectResult'">
                <a-tag :color="record.detectResult === 'VIOLATION' ? 'red' : 'green'">
                  {{ record.detectResult === 'VIOLATION' ? '违规' : '正常' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'handleStatus'">
                <a-tag :color="getHandleStatusColor(record.handleStatus)">
                  {{ getHandleStatusText(record.handleStatus) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="handleViewRecord(record)">
                    详情
                  </a-button>
                  <a-dropdown>
                    <a-button type="link" size="small">
                      处理 <DownOutlined />
                    </a-button>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item @click="handleAllowRecord(record)">
                          <CheckOutlined /> 放行
                        </a-menu-item>
                        <a-menu-item @click="handleDenyRecord(record)">
                          <CloseOutlined /> 阻止
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 统计报表 -->
        <a-tab-pane key="statistics" tab="统计报表">
          <!-- 统计卡片 -->
          <a-row :gutter="16" style="margin-bottom: 24px">
            <a-col :span="6">
              <a-statistic
                title="今日检测次数"
                :value="statistics.todayDetectCount"
                :value-style="{ color: '#3f8600' }"
              >
                <template #suffix>次</template>
              </a-statistic>
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="今日违规次数"
                :value="statistics.todayViolationCount"
                :value-style="{ color: '#cf1322' }"
              >
                <template #suffix>次</template>
              </a-statistic>
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="违规率"
                :value="statistics.violationRate"
                :precision="2"
                suffix="%"
                :value-style="{ color: statistics.violationRate > 10 ? '#cf1322' : '#3f8600' }"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="有效配置数"
                :value="statistics.activeConfigCount"
                :value-style="{ color: '#1890ff' }"
              >
                <template #suffix>个</template>
              </a-statistic>
            </a-col>
          </a-row>

          <!-- 查询表单 -->
          <a-form class="smart-query-form" layout="inline" style="margin-bottom: 16px">
            <a-row class="smart-query-form-row">
              <a-form-item label="统计周期" class="smart-query-form-item">
                <a-select
                  v-model:value="statisticsQueryForm.period"
                  style="width: 150px"
                  @change="queryStatistics"
                >
                  <a-select-option value="today">今日</a-select-option>
                  <a-select-option value="week">最近7天</a-select-option>
                  <a-select-option value="month">最近30天</a-select-option>
                  <a-select-option value="custom">自定义</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="日期范围" class="smart-query-form-item" v-if="statisticsQueryForm.period === 'custom'">
                <a-range-picker
                  v-model:value="statisticsQueryForm.dateRange"
                  style="width: 300px"
                  @change="queryStatistics"
                />
              </a-form-item>

              <a-form-item class="smart-query-form-item smart-margin-left10">
                <a-space>
                  <a-button type="primary" @click="queryStatistics">
                    <template #icon><SearchOutlined /></template>
                    查询
                  </a-button>
                  <a-button @click="exportStatistics">
                    <template #icon><ExportOutlined /></template>
                    导出报表
                  </a-button>
                </a-space>
              </a-form-item>
            </a-row>
          </a-form>

          <!-- 违规趋势图 -->
          <a-card title="违规趋势" style="margin-bottom: 16px">
            <div id="violationTrendChart" style="height: 300px"></div>
          </a-card>

          <!-- 模式分布图 -->
          <a-row :gutter="16">
            <a-col :span="12">
              <a-card title="反潜回模式分布">
                <div id="modeDistributionChart" style="height: 300px"></div>
              </a-card>
            </a-col>
            <a-col :span="12">
              <a-card title="区域违规统计TOP10">
                <a-list
                  :data-source="statistics.topAreas"
                  size="small"
                >
                  <template #renderItem="{ item }">
                    <a-list-item>
                      <a-list-item-meta :title="item.areaName" />
                      <template #actions>
                        <a-statistic
                          :value="item.violationCount"
                          :value-style="{ color: '#cf1322' }"
                        />
                      </template>
                    </a-list-item>
                  </template>
                </a-list>
              </a-card>
            </a-col>
          </a-row>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 新增/编辑配置Modal -->
    <a-modal
      v-model:open="configModalVisible"
      :title="configModalTitle"
      width="800px"
      @ok="handleConfigModalOk"
      @cancel="handleConfigModalCancel"
    >
      <a-form
        ref="configFormRef"
        :model="configForm"
        :rules="configRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="配置名称" name="configName">
          <a-input
            v-model:value="configForm.configName"
            placeholder="请输入配置名称"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="反潜回模式" name="mode">
          <a-radio-group v-model:value="configForm.mode" @change="handleModeChange">
            <a-radio :value="1">全局反潜回</a-radio>
            <a-radio :value="2">区域反潜回</a-radio>
            <a-radio :value="3">软反潜回</a-radio>
            <a-radio :value="4">硬反潜回</a-radio>
          </a-radio-group>
          <div style="margin-top: 8px; color: #999;">
            {{ getModeDescription(configForm.mode) }}
          </div>
        </a-form-item>

        <a-form-item label="关联区域" name="areaId" v-if="configForm.mode === 2">
          <a-select
            v-model:value="configForm.areaId"
            style="width: 100%"
            placeholder="请选择关联区域"
            show-search
            :filter-option="filterAreaOption"
            :allow-clear="true"
          >
            <a-select-option
              v-for="area in areaList"
              :key="area.areaId"
              :value="area.areaId"
            >
              {{ area.areaName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="时间窗口" name="timeWindowType">
          <a-radio-group v-model:value="configForm.timeWindowType">
            <a-radio :value="1">全天24小时</a-radio>
            <a-radio :value="2">工作时间</a-radio>
            <a-radio :value="3">自定义</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item
          label="开始时间"
          name="startTime"
          v-if="configForm.timeWindowType === 3"
        >
          <a-time-picker
            v-model:value="configForm.startTime"
            format="HH:mm"
            placeholder="请选择开始时间"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item
          label="结束时间"
          name="endTime"
          v-if="configForm.timeWindowType === 3"
        >
          <a-time-picker
            v-model:value="configForm.endTime"
            format="HH:mm"
            placeholder="请选择结束时间"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="时间间隔（秒）" name="timeInterval">
          <a-input-number
            v-model:value="configForm.timeInterval"
            :min="0"
            :max="3600"
            style="width: 100%"
            placeholder="请输入时间间隔"
          />
          <div style="margin-top: 8px; color: #999;">
            同一用户在此时间内再次刷卡将被判定为反潜回违规
          </div>
        </a-form-item>

        <a-form-item label="优先级" name="priority">
          <a-radio-group v-model:value="configForm.priority">
            <a-radio :value="1">低</a-radio>
            <a-radio :value="2">中</a-radio>
            <a-radio :value="3">高</a-radio>
            <a-radio :value="4">紧急</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="描述" name="description">
          <a-textarea
            v-model:value="configForm.description"
            :rows="3"
            placeholder="请输入描述"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="启用状态" name="isEnabled">
          <a-switch
            v-model:checked="configForm.isEnabled"
            checked-children="启用"
            un-checked-children="禁用"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 查看详情Modal -->
    <a-modal
      v-model:open="detailModalVisible"
      title="配置详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions bordered :column="2">
        <a-descriptions-item label="配置名称">
          {{ currentConfig.configName }}
        </a-descriptions-item>
        <a-descriptions-item label="反潜回模式">
          <a-tag :color="getModeColor(currentConfig.mode)">
            {{ getModeText(currentConfig.mode) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="关联区域" v-if="currentConfig.mode === 2">
          {{ currentConfig.areaName || '未配置' }}
        </a-descriptions-item>
        <a-descriptions-item label="时间窗口">
          {{ formatTimeWindow(currentConfig) }}
        </a-descriptions-item>
        <a-descriptions-item label="时间间隔">
          {{ currentConfig.timeInterval }}秒
        </a-descriptions-item>
        <a-descriptions-item label="优先级">
          <a-tag :color="getPriorityColor(currentConfig.priority)">
            {{ getPriorityText(currentConfig.priority) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="启用状态">
          <a-tag :color="currentConfig.isEnabled === 1 ? 'green' : 'red'">
            {{ currentConfig.isEnabled === 1 ? '启用' : '禁用' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ currentConfig.createTime }}
        </a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">
          {{ currentConfig.description || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 处理记录Modal -->
    <a-modal
      v-model:open="handleModalVisible"
      title="处理反潜回记录"
      width="600px"
      @ok="handleHandleModalOk"
      @cancel="handleHandleModalCancel"
    >
      <a-form
        ref="handleFormRef"
        :model="handleForm"
        :rules="handleRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="处理结果" name="handleResult">
          <a-radio-group v-model:value="handleForm.handleResult">
            <a-radio value="ALLOW">放行</a-radio>
            <a-radio value="DENY">阻止</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="处理原因" name="handleReason">
          <a-textarea
            v-model:value="handleForm.handleReason"
            :rows="4"
            placeholder="请输入处理原因"
            allow-clear
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  CheckOutlined,
  StopOutlined,
  DeleteOutlined,
  DownOutlined,
  CloseOutlined,
  ExportOutlined
} from '@ant-design/icons-vue';
import { accessApi } from '/@/api/business/access/access-api';
import dayjs from 'dayjs';

// ==================== 数据定义 ====================

const activeTab = ref('config');

// 配置查询表单
const configQueryForm = reactive({
  configName: '',
  mode: undefined,
  isEnabled: undefined
});

// 配置表格数据
const configTableData = ref([]);
const configLoading = ref(false);
const configPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
});

// 配置行选择
const configRowSelection = {
  selectedRowKeys: [],
  onChange: (selectedRowKeys, selectedRows) => {
    configRowSelection.selectedRowKeys = selectedRowKeys;
  }
};

// 记录查询表单
const recordQueryForm = reactive({
  userName: '',
  detectResult: undefined,
  handleStatus: undefined,
  detectTimeRange: undefined
});

// 记录表格数据
const recordTableData = ref([]);
const recordLoading = ref(false);
const recordPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
});

// 记录行选择
const recordRowSelection = {
  selectedRowKeys: [],
  onChange: (selectedRowKeys, selectedRows) => {
    recordRowSelection.selectedRowKeys = selectedRowKeys;
  }
};

// 统计数据
const statistics = reactive({
  todayDetectCount: 0,
  todayViolationCount: 0,
  violationRate: 0,
  activeConfigCount: 0,
  topAreas: []
});

// 统计查询表单
const statisticsQueryForm = reactive({
  period: 'today',
  dateRange: undefined
});

// 配置Modal
const configModalVisible = ref(false);
const configModalTitle = ref('新增反潜回配置');
const configFormRef = ref();
const configForm = reactive({
  configId: undefined,
  configName: '',
  mode: 1,
  areaId: undefined,
  timeWindowType: 1,
  startTime: undefined,
  endTime: undefined,
  timeInterval: 30,
  priority: 2,
  description: '',
  isEnabled: true
});

// 配置表单验证规则
const configRules = {
  configName: [
    { required: true, message: '请输入配置名称', trigger: 'blur' }
  ],
  mode: [
    { required: true, message: '请选择反潜回模式', trigger: 'change' }
  ],
  areaId: [
    {
      validator: (rule, value) => {
        if (configForm.mode === 2 && !value) {
          return Promise.reject('请选择关联区域');
        }
        return Promise.resolve();
      },
      trigger: 'change'
    }
  ],
  timeInterval: [
    { required: true, message: '请输入时间间隔', trigger: 'blur' },
    { type: 'number', min: 0, max: 3600, message: '时间间隔应在0-3600秒之间', trigger: 'blur' }
  ]
};

// 详情Modal
const detailModalVisible = ref(false);
const currentConfig = ref({});

// 处理Modal
const handleModalVisible = ref(false);
const handleFormRef = ref();
const handleForm = reactive({
  recordId: undefined,
  handleResult: 'ALLOW',
  handleReason: ''
});

const handleRules = {
  handleReason: [
    { required: true, message: '请输入处理原因', trigger: 'blur' }
  ]
};

// 区域列表
const areaList = ref([]);

// 时间范围快捷选项
const timeRanges = {
  '今天': [dayjs().startOf('day'), dayjs().endOf('day')],
  '本周': [dayjs().startOf('week'), dayjs().endOf('week')],
  '本月': [dayjs().startOf('month'), dayjs().endOf('month')]
};

// ==================== 配置列表表格列定义 ====================

const configColumns = [
  {
    title: '配置ID',
    dataIndex: 'configId',
    key: 'configId',
    width: 100
  },
  {
    title: '配置名称',
    dataIndex: 'configName',
    key: 'configName',
    ellipsis: true
  },
  {
    title: '反潜回模式',
    dataIndex: 'mode',
    key: 'mode',
    width: 120
  },
  {
    title: '关联区域',
    dataIndex: 'areaName',
    key: 'areaName',
    ellipsis: true
  },
  {
    title: '时间窗口',
    dataIndex: 'timeWindow',
    key: 'timeWindow',
    width: 150
  },
  {
    title: '时间间隔',
    dataIndex: 'timeInterval',
    key: 'timeInterval',
    width: 100
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
    width: 80
  },
  {
    title: '启用状态',
    dataIndex: 'isEnabled',
    key: 'isEnabled',
    width: 80
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right'
  }
];

// ==================== 检测记录表格列定义 ====================

const recordColumns = [
  {
    title: '记录ID',
    dataIndex: 'recordId',
    key: 'recordId',
    width: 100
  },
  {
    title: '用户姓名',
    dataIndex: 'userName',
    key: 'userName',
    width: 100
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    ellipsis: true
  },
  {
    title: '检测时间',
    dataIndex: 'detectTime',
    key: 'detectTime',
    width: 150
  },
  {
    title: '检测结果',
    dataIndex: 'detectResult',
    key: 'detectResult',
    width: 80
  },
  {
    title: '处理状态',
    dataIndex: 'handleStatus',
    key: 'handleStatus',
    width: 80
  },
  {
    title: '处理人',
    dataIndex: 'handlerName',
    key: 'handlerName',
    width: 100
  },
  {
    title: '处理时间',
    dataIndex: 'handleTime',
    key: 'handleTime',
    width: 150
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right'
  }
];

// ==================== 方法定义 ====================

/**
 * 查询配置列表
 */
const queryConfigList = async () => {
  configLoading.value = true;
  try {
    const res = await accessApi.queryAntiPassbackConfigPage({
      pageNum: configPagination.current,
      pageSize: configPagination.pageSize,
      ...configQueryForm
    });
    if (res.code === 200) {
      configTableData.value = res.data.list || [];
      configPagination.total = res.data.total;
    } else {
      message.error(res.message || '查询失败');
    }
  } catch (e) {
    console.error('[反潜回配置] 查询失败:', e);
    message.error('查询失败，请稍后重试');
  } finally {
    configLoading.value = false;
  }
};

/**
 * 重置配置查询
 */
const resetConfigQuery = () => {
  Object.assign(configQueryForm, {
    configName: '',
    mode: undefined,
    isEnabled: undefined
  });
  queryConfigList();
};

/**
 * 新增配置
 */
const handleAddConfig = () => {
  configModalTitle.value = '新增反潜回配置';
  Object.assign(configForm, {
    configId: undefined,
    configName: '',
    mode: 1,
    areaId: undefined,
    timeWindowType: 1,
    startTime: undefined,
    endTime: undefined,
    timeInterval: 30,
    priority: 2,
    description: '',
    isEnabled: true
  });
  configModalVisible.value = true;
};

/**
 * 编辑配置
 */
const handleEditConfig = (record) => {
  configModalTitle.value = '编辑反潜回配置';
  Object.assign(configForm, {
    configId: record.configId,
    configName: record.configName,
    mode: record.mode,
    areaId: record.areaId,
    timeWindowType: record.timeWindowType,
    startTime: record.startTime ? dayjs(record.startTime, 'HH:mm') : undefined,
    endTime: record.endTime ? dayjs(record.endTime, 'HH:mm') : undefined,
    timeInterval: record.timeInterval,
    priority: record.priority,
    description: record.description,
    isEnabled: record.isEnabled === 1
  });
  configModalVisible.value = true;
};

/**
 * 查看配置详情
 */
const handleViewConfig = async (record) => {
  try {
    const res = await accessApi.getAntiPassbackConfig(record.configId);
    if (res.code === 200) {
      currentConfig.value = res.data;
      detailModalVisible.value = true;
    } else {
      message.error(res.message || '查询详情失败');
    }
  } catch (e) {
    console.error('[反潜回配置] 查询详情失败:', e);
    message.error('查询详情失败，请稍后重试');
  }
};

/**
 * 保存配置
 */
const handleConfigModalOk = async () => {
  try {
    await configFormRef.value.validate();
    const formData = { ...configForm };
    formData.isEnabled = formData.isEnabled ? 1 : 0;

    let res;
    if (formData.configId) {
      res = await accessApi.updateAntiPassbackConfig(formData);
    } else {
      res = await accessApi.createAntiPassbackConfig(formData);
    }

    if (res.code === 200) {
      message.success('保存成功');
      configModalVisible.value = false;
      queryConfigList();
    } else {
      message.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error('[反潜回配置] 保存失败:', e);
    message.error('保存失败，请稍后重试');
  }
};

/**
 * 取消配置Modal
 */
const handleConfigModalCancel = () => {
  configModalVisible.value = false;
};

/**
 * 切换启用状态
 */
const handleToggleEnabled = async (record) => {
  try {
    const action = record.isEnabled === 1 ? 'disable' : 'enable';
    const res = await accessApi[`${action}AntiPassbackConfig`](record.configId);
    if (res.code === 200) {
      message.success(`${action === 'enable' ? '启用' : '禁用'}成功`);
      queryConfigList();
    } else {
      message.error(res.message || '操作失败');
    }
  } catch (e) {
    console.error('[反潜回配置] 切换状态失败:', e);
    message.error('操作失败，请稍后重试');
  }
};

/**
 * 删除配置
 */
const handleDeleteConfig = async (record) => {
  try {
    const res = await accessApi.deleteAntiPassbackConfig(record.configId);
    if (res.code === 200) {
      message.success('删除成功');
      queryConfigList();
    } else {
      message.error(res.message || '删除失败');
    }
  } catch (e) {
    console.error('[反潜回配置] 删除失败:', e);
    message.error('删除失败，请稍后重试');
  }
};

/**
 * 批量启用
 */
const handleBatchEnable = async () => {
  if (configRowSelection.selectedRowKeys.length === 0) {
    message.warning('请至少选择一条记录');
    return;
  }

  Modal.confirm({
    title: '批量启用',
    content: `确定要启用选中的 ${configRowSelection.selectedRowKeys.length} 条配置吗？`,
    onOk: async () => {
      try {
        const promises = configRowSelection.selectedRowKeys.map(configId =>
          accessApi.enableAntiPassbackConfig(configId)
        );
        await Promise.all(promises);
        message.success('批量启用成功');
        configRowSelection.selectedRowKeys = [];
        queryConfigList();
      } catch (e) {
        console.error('[反潜回配置] 批量启用失败:', e);
        message.error('批量启用失败，请稍后重试');
      }
    }
  });
};

/**
 * 批量禁用
 */
const handleBatchDisable = async () => {
  if (configRowSelection.selectedRowKeys.length === 0) {
    message.warning('请至少选择一条记录');
    return;
  }

  Modal.confirm({
    title: '批量禁用',
    content: `确定要禁用选中的 ${configRowSelection.selectedRowKeys.length} 条配置吗？`,
    onOk: async () => {
      try {
        const promises = configRowSelection.selectedRowKeys.map(configId =>
          accessApi.disableAntiPassbackConfig(configId)
        );
        await Promise.all(promises);
        message.success('批量禁用成功');
        configRowSelection.selectedRowKeys = [];
        queryConfigList();
      } catch (e) {
        console.error('[反潜回配置] 批量禁用失败:', e);
        message.error('批量禁用失败，请稍后重试');
      }
    }
  });
};

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  if (configRowSelection.selectedRowKeys.length === 0) {
    message.warning('请至少选择一条记录');
    return;
  }

  Modal.confirm({
    title: '批量删除',
    content: `确定要删除选中的 ${configRowSelection.selectedRowKeys.length} 条配置吗？此操作不可恢复！`,
    onOk: async () => {
      try {
        const promises = configRowSelection.selectedRowKeys.map(configId =>
          accessApi.deleteAntiPassbackConfig(configId)
        );
        await Promise.all(promises);
        message.success('批量删除成功');
        configRowSelection.selectedRowKeys = [];
        queryConfigList();
      } catch (e) {
        console.error('[反潜回配置] 批量删除失败:', e);
        message.error('批量删除失败，请稍后重试');
      }
    }
  });
};

/**
 * 查询记录列表
 */
const queryRecordList = async () => {
  recordLoading.value = true;
  try {
    const params = {
      pageNum: recordPagination.current,
      pageSize: recordPagination.pageSize,
      ...recordQueryForm
    };

    if (recordQueryForm.detectTimeRange && recordQueryForm.detectTimeRange.length === 2) {
      params.startTime = recordQueryForm.detectTimeRange[0].format('YYYY-MM-DD HH:mm:ss');
      params.endTime = recordQueryForm.detectTimeRange[1].format('YYYY-MM-DD HH:mm:ss');
    }

    const res = await accessApi.queryAntiPassbackRecordPage(params);
    if (res.code === 200) {
      recordTableData.value = res.data.list || [];
      recordPagination.total = res.data.total;
    } else {
      message.error(res.message || '查询失败');
    }
  } catch (e) {
    console.error('[反潜回记录] 查询失败:', e);
    message.error('查询失败，请稍后重试');
  } finally {
    recordLoading.value = false;
  }
};

/**
 * 重置记录查询
 */
const resetRecordQuery = () => {
  Object.assign(recordQueryForm, {
    userName: '',
    detectResult: undefined,
    handleStatus: undefined,
    detectTimeRange: undefined
  });
  queryRecordList();
};

/**
 * 查看记录详情
 */
const handleViewRecord = (record) => {
  // TODO: 实现详情查看
  message.info('查看记录详情功能待实现');
};

/**
 * 放行记录
 */
const handleAllowRecord = (record) => {
  handleForm.recordId = record.recordId;
  handleForm.handleResult = 'ALLOW';
  handleModalVisible.value = true;
};

/**
 * 阻止记录
 */
const handleDenyRecord = (record) => {
  handleForm.recordId = record.recordId;
  handleForm.handleResult = 'DENY';
  handleModalVisible.value = true;
};

/**
 * 处理记录Modal确认
 */
const handleHandleModalOk = async () => {
  try {
    await handleFormRef.value.validate();
    const res = await accessApi.handleAntiPassbackRecord(
      handleForm.recordId,
      handleForm.handleResult,
      handleForm.handleReason
    );
    if (res.code === 200) {
      message.success('处理成功');
      handleModalVisible.value = false;
      queryRecordList();
    } else {
      message.error(res.message || '处理失败');
    }
  } catch (e) {
    console.error('[反潜回记录] 处理失败:', e);
    message.error('处理失败，请稍后重试');
  }
};

/**
 * 处理记录Modal取消
 */
const handleHandleModalCancel = () => {
  handleModalVisible.value = false;
};

/**
 * 批量放行
 */
const handleBatchAllow = () => {
  if (recordRowSelection.selectedRowKeys.length === 0) {
    message.warning('请至少选择一条记录');
    return;
  }

  handleForm.handleResult = 'ALLOW';
  handleForm.handleReason = '批量放行';
  handleBatchHandleRecords();
};

/**
 * 批量阻止
 */
const handleBatchDeny = () => {
  if (recordRowSelection.selectedRowKeys.length === 0) {
    message.warning('请至少选择一条记录');
    return;
  }

  handleForm.handleResult = 'DENY';
  handleForm.handleReason = '批量阻止';
  handleBatchHandleRecords();
};

/**
 * 批量处理记录
 */
const handleBatchHandleRecords = async () => {
  try {
    const res = await accessApi.batchHandleAntiPassbackRecords(
      recordRowSelection.selectedRowKeys,
      handleForm.handleResult,
      handleForm.handleReason
    );
    if (res.code === 200) {
      message.success(`批量处理成功，共处理 ${res.data} 条记录`);
      recordRowSelection.selectedRowKeys = [];
      queryRecordList();
    } else {
      message.error(res.message || '批量处理失败');
    }
  } catch (e) {
    console.error('[反潜回记录] 批量处理失败:', e);
    message.error('批量处理失败，请稍后重试');
  }
};

/**
 * 查询统计数据
 */
const queryStatistics = async () => {
  try {
    const params = {
      period: statisticsQueryForm.period
    };

    if (statisticsQueryForm.period === 'custom' && statisticsQueryForm.dateRange) {
      params.startDate = statisticsQueryForm.dateRange[0].format('YYYY-MM-DD');
      params.endDate = statisticsQueryForm.dateRange[1].format('YYYY-MM-DD');
    }

    const res = await accessApi.getAntiPassbackStatistics(params);
    if (res.code === 200) {
      Object.assign(statistics, res.data);
    } else {
      message.error(res.message || '查询统计失败');
    }
  } catch (e) {
    console.error('[反潜回统计] 查询失败:', e);
    message.error('查询统计失败，请稍后重试');
  }
};

/**
 * 导出记录
 */
const exportRecords = async () => {
  message.info('导出记录功能待实现');
};

/**
 * 导出统计报表
 */
const exportStatistics = async () => {
  message.info('导出统计报表功能待实现');
};

/**
 * 配置表格变化
 */
const handleConfigTableChange = (pagination) => {
  configPagination.current = pagination.current;
  configPagination.pageSize = pagination.pageSize;
  queryConfigList();
};

/**
 * 记录表格变化
 */
const handleRecordTableChange = (pagination) => {
  recordPagination.current = pagination.current;
  recordPagination.pageSize = pagination.pageSize;
  queryRecordList();
};

/**
 * Tab切换
 */
const handleTabChange = (activeKey) => {
  activeTab.value = activeKey;
  if (activeKey === 'config') {
    queryConfigList();
  } else if (activeKey === 'record') {
    queryRecordList();
  } else if (activeKey === 'statistics') {
    queryStatistics();
  }
};

/**
 * 模式变化
 */
const handleModeChange = (e) => {
  console.log('模式变化:', e.target.value);
};

/**
 * 区域搜索过滤
 */
const filterAreaOption = (input, option) => {
  return option.areaName.toLowerCase().includes(input.toLowerCase());
};

// ==================== 工具方法 ====================

/**
 * 获取模式文本
 */
const getModeText = (mode) => {
  const modeMap = {
    1: '全局反潜回',
    2: '区域反潜回',
    3: '软反潜回',
    4: '硬反潜回'
  };
  return modeMap[mode] || '-';
};

/**
 * 获取模式颜色
 */
const getModeColor = (mode) => {
  const colorMap = {
    1: 'blue',
    2: 'cyan',
    3: 'orange',
    4: 'red'
  };
  return colorMap[mode] || 'default';
};

/**
 * 获取模式描述
 */
const getModeDescription = (mode) => {
  const descMap = {
    1: '跨所有区域检测反潜回违规，适用于整个园区',
    2: '只在指定区域内检测反潜回违规',
    3: '检测到违规时记录告警但不阻止通行',
    4: '检测到违规时直接阻止通行'
  };
  return descMap[mode] || '';
};

/**
 * 格式化时间窗口
 */
const formatTimeWindow = (record) => {
  if (record.timeWindowType === 1) {
    return '全天24小时';
  } else if (record.timeWindowType === 2) {
    return '工作时间';
  } else if (record.timeWindowType === 3) {
    return `${record.startTime} - ${record.endTime}`;
  }
  return '-';
};

/**
 * 获取优先级文本
 */
const getPriorityText = (priority) => {
  const priorityMap = {
    1: '低',
    2: '中',
    3: '高',
    4: '紧急'
  };
  return priorityMap[priority] || '-';
};

/**
 * 获取优先级颜色
 */
const getPriorityColor = (priority) => {
  const colorMap = {
    1: 'green',
    2: 'blue',
    3: 'orange',
    4: 'red'
  };
  return colorMap[priority] || 'default';
};

/**
 * 获取处理状态文本
 */
const getHandleStatusText = (status) => {
  const statusMap = {
    'PENDING': '待处理',
    'ALLOWED': '已放行',
    'DENIED': '已阻止'
  };
  return statusMap[status] || '-';
};

/**
 * 获取处理状态颜色
 */
const getHandleStatusColor = (status) => {
  const colorMap = {
    'PENDING': 'orange',
    'ALLOWED': 'green',
    'DENIED': 'red'
  };
  return colorMap[status] || 'default';
};

// ==================== 生命周期 ====================

onMounted(() => {
  queryConfigList();
  queryStatistics();
});
</script>

<style scoped lang="less">
.anti-passback-config-page {
  padding: 16px;
}

.smart-query-form {
  background: #fafafa;
  padding: 16px;
  border-radius: 4px;
}

.smart-query-form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.smart-query-form-item {
  margin-bottom: 0;
}

.smart-margin-left10 {
  margin-left: 10px;
}
</style>
