<template>
  <div class="mock-config-container">
    <!-- 页面头部 -->
    <a-card :bordered="false" class="header-card">
      <a-row :gutter="16" align="middle">
        <a-col :span="12">
          <div class="page-title">
            <ExperimentOutlined style="font-size: 24px; margin-right: 8px;" />
            <span>Mock服务配置</span>
          </div>
        </a-col>
        <a-col :span="12" style="text-align: right;">
          <a-space>
            <a-button type="primary" @click="showCreateModal">
              <template #icon><PlusOutlined /></template>
              新建配置
            </a-button>
            <a-button @click="loadConfigs">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- Mock配置列表 -->
    <a-card :bordered="false" class="table-card">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="ALL" tab="全部配置">
          <a-table
            :columns="columns"
            :data-source="mockConfigs"
            :loading="loading"
            :pagination="{ pageSize: 10 }"
            row-key="configId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'configName'">
                <a-space>
                  <ExperimentOutlined style="font-size: 18px; color: #1890ff;" />
                  <span>{{ record.configName }}</span>
                </a-space>
              </template>
              <template v-else-if="column.key === 'configType'">
                <a-tag :color="getConfigTypeColor(record.configType)">
                  {{ getConfigTypeText(record.configType) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'mockScenario'">
                <a-tag :color="getScenarioColor(record.mockScenario)">
                  {{ getScenarioText(record.mockScenario) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'mockStatus'">
                <a-switch
                  :checked="record.mockStatus === 'ENABLED'"
                  @change="(checked) => handleStatusChange(checked, record)"
                />
              </template>
              <template v-else-if="column.key === 'usageCount'">
                <a-statistic
                  :value="record.usageCount"
                  :value-style="{ fontSize: '14px' }"
                >
                  <template #suffix>
                    <span style="font-size: 12px; color: #999;">次</span>
                  </template>
                </a-statistic>
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a @click="generateData(record)">生成数据</a>
                  <a @click="editConfig(record)">编辑</a>
                  <a @click="viewData(record)">查看数据</a>
                  <a @click="deleteConfig(record.configId)" style="color: #ff4d4f;">删除</a>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <a-tab-pane key="EMPLOYEE" tab="员工数据">
          <a-table
            :columns="columns"
            :data-source="filteredConfigs('EMPLOYEE')"
            :loading="loading"
            :pagination="{ pageSize: 10 }"
            row-key="configId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'configName'">
                <a-space>
                  <ExperimentOutlined style="font-size: 18px; color: #1890ff;" />
                  <span>{{ record.configName }}</span>
                </a-space>
              </template>
              <template v-else-if="column.key === 'mockScenario'">
                <a-tag :color="getScenarioColor(record.mockScenario)">
                  {{ getScenarioText(record.mockScenario) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'mockStatus'">
                <a-switch
                  :checked="record.mockStatus === 'ENABLED'"
                  @change="(checked) => handleStatusChange(checked, record)"
                />
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a @click="generateData(record)">生成数据</a>
                  <a @click="editConfig(record)">编辑</a>
                  <a @click="deleteConfig(record.configId)" style="color: #ff4d4f;">删除</a>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <a-tab-pane key="DEPARTMENT" tab="部门数据">
          <a-table
            :columns="columns"
            :data-source="filteredConfigs('DEPARTMENT')"
            :loading="loading"
            :pagination="{ pageSize: 10 }"
            row-key="configId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'configName'">
                <a-space>
                  <ExperimentOutlined style="font-size: 18px; color: #1890ff;" />
                  <span>{{ record.configName }}</span>
                </a-space>
              </template>
              <template v-else-if="column.key === 'mockScenario'">
                <a-tag :color="getScenarioColor(record.mockScenario)">
                  {{ getScenarioText(record.mockScenario) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'mockStatus'">
                <a-switch
                  :checked="record.mockStatus === 'ENABLED'"
                  @change="(checked) => handleStatusChange(checked, record)"
                />
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a @click="generateData(record)">生成数据</a>
                  <a @click="editConfig(record)">编辑</a>
                  <a @click="deleteConfig(record.configId)" style="color: #ff4d4f;">删除</a>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <a-tab-pane key="SHIFT" tab="班次数据">
          <a-table
            :columns="columns"
            :data-source="filteredConfigs('SHIFT')"
            :loading="loading"
            :pagination="{ pageSize: 10 }"
            row-key="configId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'configName'">
                <a-space>
                  <ExperimentOutlined style="font-size: 18px; color: #1890ff;" />
                  <span>{{ record.configName }}</span>
                </a-space>
              </template>
              <template v-else-if="column.key === 'mockScenario'">
                <a-tag :color="getScenarioColor(record.mockScenario)">
                  {{ getScenarioText(record.mockScenario) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'mockStatus'">
                <a-switch
                  :checked="record.mockStatus === 'ENABLED'"
                  @change="(checked) => handleStatusChange(checked, record)"
                />
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a @click="generateData(record)">生成数据</a>
                  <a @click="editConfig(record)">编辑</a>
                  <a @click="deleteConfig(record.configId)" style="color: #ff4d4f;">删除</a>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <a-tab-pane key="ATTENDANCE" tab="考勤数据">
          <a-table
            :columns="columns"
            :data-source="filteredConfigs('ATTENDANCE')"
            :loading="loading"
            :pagination="{ pageSize: 10 }"
            row-key="configId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'configName'">
                <a-space>
                  <ExperimentOutlined style="font-size: 18px; color: #1890ff;" />
                  <span>{{ record.configName }}</span>
                </a-space>
              </template>
              <template v-else-if="column.key === 'mockScenario'">
                <a-tag :color="getScenarioColor(record.mockScenario)">
                  {{ getScenarioText(record.mockScenario) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'mockStatus'">
                <a-switch
                  :checked="record.mockStatus === 'ENABLED'"
                  @change="(checked) => handleStatusChange(checked, record)"
                />
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a @click="generateData(record)">生成数据</a>
                  <a @click="editConfig(record)">编辑</a>
                  <a @click="deleteConfig(record.configId)" style="color: #ff4d4f;">删除</a>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 创建/编辑配置弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑Mock配置' : '新建Mock配置'"
      :width="800"
      @ok="handleModalOk"
      :confirm-loading="modalLoading"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="配置名称" required>
          <a-input v-model:value="formState.configName" placeholder="请输入配置名称" />
        </a-form-item>

        <a-form-item label="配置类型" required>
          <a-select v-model:value="formState.configType" placeholder="请选择配置类型">
            <a-select-option value="EMPLOYEE">员工数据</a-select-option>
            <a-select-option value="DEPARTMENT">部门数据</a-select-option>
            <a-select-option value="SHIFT">班次数据</a-select-option>
            <a-select-option value="ATTENDANCE">考勤数据</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="Mock场景" required>
          <a-select v-model:value="formState.mockScenario" placeholder="请选择Mock场景">
            <a-select-option value="NORMAL">正常场景</a-select-option>
            <a-select-option value="EDGE_CASE">边界场景</a-select-option>
            <a-select-option value="ERROR">异常场景</a-select-option>
            <a-select-option value="PERFORMANCE">性能场景</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="数据量">
          <a-input-number
            v-model:value="formState.dataCount"
            :min="1"
            :max="10000"
            style="width: 100%;"
            placeholder="生成数据数量"
          />
        </a-form-item>

        <a-form-item label="返回延迟（毫秒）">
          <a-input-number
            v-model:value="formState.responseDelayMs"
            :min="0"
            :max="10000"
            style="width: 100%;"
            placeholder="模拟网络延迟"
          />
        </a-form-item>

        <a-form-item label="启用随机延迟">
          <a-switch v-model:checked="formState.enableRandomDelay" />
        </a-form-item>

        <a-form-item v-if="formState.enableRandomDelay" label="随机延迟范围">
          <a-input-number
            v-model:value="formState.randomDelayMinMs"
            :min="0"
            :max="1000"
            placeholder="最小值"
            style="width: 45%; margin-right: 10%;"
          />
          <a-input-number
            v-model:value="formState.randomDelayMaxMs"
            :min="0"
            :max="5000"
            placeholder="最大值"
            style="width: 45%;"
          />
        </a-form-item>

        <a-form-item label="配置描述">
          <a-textarea
            v-model:value="formState.configDescription"
            :rows="3"
            placeholder="请输入配置描述"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Mock数据预览弹窗 -->
    <a-modal
      v-model:open="dataPreviewVisible"
      title="Mock数据预览"
      :width="1000"
      :footer="null"
    >
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="配置名称">{{ currentData?.configName }}</a-descriptions-item>
        <a-descriptions-item label="数据类型">
          <a-tag :color="getConfigTypeColor(currentData?.mockDataType)">
            {{ getConfigTypeText(currentData?.mockDataType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Mock场景">
          <a-tag :color="getScenarioColor(currentData?.mockScenario)">
            {{ getScenarioText(currentData?.mockScenario) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="数据数量">{{ currentData?.dataCount }}</a-descriptions-item>
        <a-descriptions-item label="生成耗时">{{ currentData?.generationTimeMs }}ms</a-descriptions-item>
      </a-descriptions>

      <a-divider>数据内容</a-divider>
      <pre style="max-height: 400px; overflow-y: auto; background: #f5f5f5; padding: 16px; border-radius: 4px;">{{ JSON.stringify(currentData?.data, null, 2) }}</pre>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  ExperimentOutlined,
  PlusOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue';
import mockConfigApi from '/@/api/business/attendance/mock-config-api';

// 数据状态
const loading = ref(false);
const modalLoading = ref(false);
const mockConfigs = ref([]);
const activeTab = ref('ALL');
const isEdit = ref(false);
const modalVisible = ref(false);
const dataPreviewVisible = ref(false);
const currentData = ref(null);
const formRef = ref(null);

// 表单状态
const formState = reactive({
  configId: null,
  configName: '',
  configType: 'EMPLOYEE',
  mockScenario: 'NORMAL',
  dataCount: 100,
  responseDelayMs: 100,
  enableRandomDelay: false,
  randomDelayMinMs: 50,
  randomDelayMaxMs: 200,
  configDescription: ''
});

// 表格列定义
const columns = [
  {
    title: '配置名称',
    dataIndex: 'configName',
    key: 'configName',
    width: '25%'
  },
  {
    title: '配置类型',
    dataIndex: 'configType',
    key: 'configType',
    width: '15%'
  },
  {
    title: 'Mock场景',
    dataIndex: 'mockScenario',
    key: 'mockScenario',
    width: '15%'
  },
  {
    title: '状态',
    dataIndex: 'mockStatus',
    key: 'mockStatus',
    width: '10%',
    align: 'center'
  },
  {
    title: '使用次数',
    dataIndex: 'usageCount',
    key: 'usageCount',
    width: '10%',
    align: 'center'
  },
  {
    title: '操作',
    key: 'actions',
    width: '25%',
    align: 'center'
  }
];

// 加载Mock配置列表
const loadConfigs = async () => {
  loading.value = true;
  try {
    const res = await mockConfigApi.getAllMockConfigs();
    if (res.code === 200) {
      mockConfigs.value = res.data || [];
    } else {
      message.error(res.message || '加载Mock配置失败');
    }
  } catch (error) {
    console.error('加载Mock配置失败:', error);
    message.error('加载Mock配置失败');
  } finally {
    loading.value = false;
  }
};

// 过滤配置
const filteredConfigs = (type) => {
  if (type === 'ALL') {
    return mockConfigs.value;
  }
  return mockConfigs.value.filter(config => config.configType === type);
};

// Tab切换
const handleTabChange = (key) => {
  activeTab.value = key;
};

// 显示创建弹窗
const showCreateModal = () => {
  isEdit.value = false;
  Object.assign(formState, {
    configId: null,
    configName: '',
    configType: 'EMPLOYEE',
    mockScenario: 'NORMAL',
    dataCount: 100,
    responseDelayMs: 100,
    enableRandomDelay: false,
    randomDelayMinMs: 50,
    randomDelayMaxMs: 200,
    configDescription: ''
  });
  modalVisible.value = true;
};

// 编辑配置
const editConfig = (record) => {
  isEdit.value = true;
  Object.assign(formState, {
    configId: record.configId,
    configName: record.configName,
    configType: record.configType,
    mockScenario: record.mockScenario,
    dataCount: 100,
    responseDelayMs: record.responseDelayMs,
    enableRandomDelay: false,
    randomDelayMinMs: 50,
    randomDelayMaxMs: 200,
    configDescription: record.configDescription
  });
  modalVisible.value = true;
};

// 处理弹窗确认
const handleModalOk = async () => {
  modalLoading.value = true;
  try {
    const params = {
      configName: formState.configName,
      configType: formState.configType,
      mockScenario: formState.mockScenario,
      generationRules: JSON.stringify({ dataCount: formState.dataCount }),
      responseDelayMs: formState.responseDelayMs,
      enableRandomDelay: formState.enableRandomDelay,
      randomDelayMinMs: formState.randomDelayMinMs,
      randomDelayMaxMs: formState.randomDelayMaxMs,
      configDescription: formState.configDescription
    };

    if (isEdit.value) {
      params.configId = formState.configId;
      const res = await mockConfigApi.updateMockConfig(params);
      if (res.code === 200) {
        message.success('更新成功');
        modalVisible.value = false;
        await loadConfigs();
      } else {
        message.error(res.message || '更新失败');
      }
    } else {
      const res = await mockConfigApi.createMockConfig(params);
      if (res.code === 200) {
        message.success('创建成功');
        modalVisible.value = false;
        await loadConfigs();
      } else {
        message.error(res.message || '创建失败');
      }
    }
  } catch (error) {
    console.error('操作失败:', error);
    message.error('操作失败');
  } finally {
    modalLoading.value = false;
  }
};

// 生成数据
const generateData = async (record) => {
  try {
    message.loading('正在生成Mock数据...', 0);
    const res = await mockConfigApi.generateMockData(record.configId);
    message.destroy();

    if (res.code === 200) {
      message.success(`成功生成 ${res.data.dataCount} 条Mock数据`);
      currentData.value = res.data;
      dataPreviewVisible.value = true;
    } else {
      message.error(res.message || '生成Mock数据失败');
    }
  } catch (error) {
    message.destroy();
    console.error('生成Mock数据失败:', error);
    message.error('生成Mock数据失败');
  }
};

// 查看数据
const viewData = (record) => {
  currentData.value = record;
  dataPreviewVisible.value = true;
};

// 删除配置
const deleteConfig = (configId) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除此Mock配置吗？',
    onOk: async () => {
      try {
        const res = await mockConfigApi.deleteMockConfig(configId);
        if (res.code === 200) {
          message.success('删除成功');
          await loadConfigs();
        } else {
          message.error(res.message || '删除失败');
        }
      } catch (error) {
        console.error('删除失败:', error);
        message.error('删除失败');
      }
    }
  });
};

// 状态切换
const handleStatusChange = async (checked, record) => {
  try {
    const api = checked ? mockConfigApi.enableMockConfig : mockConfigApi.disableMockConfig;
    const res = await api(record.configId);
    if (res.code === 200) {
      record.mockStatus = checked ? 'ENABLED' : 'DISABLED';
      message.success(checked ? '已启用' : '已禁用');
    } else {
      message.error(res.message || '操作失败');
    }
  } catch (error) {
    console.error('状态切换失败:', error);
    message.error('状态切换失败');
  }
};

// 辅助方法：获取配置类型颜色
const getConfigTypeColor = (type) => {
  const colors = {
    'EMPLOYEE': 'blue',
    'DEPARTMENT': 'green',
    'SHIFT': 'orange',
    'ATTENDANCE': 'purple'
  };
  return colors[type] || 'default';
};

// 辅助方法：获取配置类型文本
const getConfigTypeText = (type) => {
  const texts = {
    'EMPLOYEE': '员工数据',
    'DEPARTMENT': '部门数据',
    'SHIFT': '班次数据',
    'ATTENDANCE': '考勤数据'
  };
  return texts[type] || type;
};

// 辅助方法：获取场景颜色
const getScenarioColor = (scenario) => {
  const colors = {
    'NORMAL': 'green',
    'EDGE_CASE': 'orange',
    'ERROR': 'red',
    'PERFORMANCE': 'blue'
  };
  return colors[scenario] || 'default';
};

// 辅助方法：获取场景文本
const getScenarioText = (scenario) => {
  const texts = {
    'NORMAL': '正常场景',
    'EDGE_CASE': '边界场景',
    'ERROR': '异常场景',
    'PERFORMANCE': '性能场景'
  };
  return texts[scenario] || scenario;
};

// 生命周期
onMounted(() => {
  loadConfigs();
});
</script>

<style scoped lang="less">
.mock-config-container {
  padding: 16px;

  .header-card {
    margin-bottom: 16px;
  }

  .page-title {
    font-size: 20px;
    font-weight: 500;
    color: #333;
    display: flex;
    align-items: center;
  }

  .table-card {
    :deep(.ant-tabs-nav) {
      margin-bottom: 16px;
    }
  }
}
</style>
