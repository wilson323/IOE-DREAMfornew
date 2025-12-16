<!--
  * 全局联动管理页面
  * 企业级门禁系统全局联动控制功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-12-01
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="global-linkage-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <h2>全局联动管理</h2>
          <p>配置和管理门禁设备全局联动规则</p>
        </div>
        <div class="header-actions">
          <a-space>
            <a-button @click="handleAddRule">
              <template #icon><PlusOutlined /></template>
              添加联动规则
            </a-button>
            <a-button @click="handleBatchOperation">
              <template #icon><AppstoreOutlined /></template>
              批量操作
            </a-button>
            <a-button @click="refreshData" :loading="loading">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </div>
      </div>
    </div>

    <!-- 联动规则列表 -->
    <div class="rules-section">
      <a-card :bordered="false">
        <template #title>
          <span>联动规则列表</span>
          <a-tag :color="getRuleCountColor(ruleList.length)">{{ ruleList.length }} 条规则</a-tag>
        </template>

        <!-- 搜索和筛选 -->
        <div class="search-section">
          <a-row :gutter="16">
            <a-col :span="6">
              <a-input
                v-model:value="searchParams.ruleName"
                placeholder="搜索规则名称"
                @change="handleSearch"
              >
                <template #prefix>
                  <SearchOutlined />
                </template>
              </a-input>
            </a-col>
            <a-col :span="6">
              <a-select
                v-model:value="searchParams.ruleType"
                placeholder="规则类型"
                @change="handleSearch"
                allow-clear
              >
                <a-select-option value="">全部类型</a-select-option>
                <a-select-option value="DEVICE_STATUS">设备状态联动</a-select-option>
                <a-select-option value="ACCESS_RESULT">通行结果联动</a-select-option>
                <a-select-option value="TIME_BASED">时间触发联动</a-select-option>
                <a-select-option value="EMERGENCY">紧急情况联动</a-select-option>
              </a-select>
            </a-col>
            <a-col :span="6">
              <a-select
                v-model:value="searchParams.status"
                placeholder="规则状态"
                @change="handleSearch"
                allow-clear
              >
                <a-select-option value="">全部状态</a-select-option>
                <a-select-option value="ACTIVE">启用</a-select-option>
                <a-select-option value="INACTIVE">禁用</a-select-option>
              </a-select>
            </a-col>
            <a-col :span="6">
              <a-button type="primary" @click="handleSearch">
                <SearchOutlined />
                搜索
              </a-button>
            </a-col>
          </a-row>
        </div>

        <!-- 规则表格 -->
        <a-table
          :columns="ruleColumns"
          :data-source="ruleList"
          :loading="loading"
          :pagination="pagination"
          :row-key="record => record.ruleId"
          :row-selection="rowSelection"
          @change="handleTableChange"
          @expand="handleExpand"
        >
          <!-- 状态列 -->
          <template #status="{ record }">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>

          <!-- 操作列 -->
          <template #action="{ record }">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleToggleStatus(record)"
                :style="{ color: record.status === 'ACTIVE' ? '#f5222d' : '#52c41a' }"
              >
                {{ record.status === 'ACTIVE' ? '禁用' : '启用' }}
              </a-button>
              <a-button type="link" size="small" @click="handleTest(record)">
                测试
              </a-button>
              <a-popconfirm
                title="确定要删除这条规则吗？"
                @confirm="handleDelete(record)"
              >
                <a-button type="link" size="small" danger>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>

          <!-- 展开行 -->
          <template #expandedRowRender="{ record }">
            <div class="expanded-content">
              <a-descriptions :column="2" size="small" bordered>
                <a-descriptions-item label="触发条件">
                  {{ getTriggerConditionText(record.triggerCondition) }}
                </a-descriptions-item>
                <a-descriptions-item label="联动动作">
                  {{ getLinkageActionText(record.linkageAction) }}
                </a-descriptions-item>
                <a-descriptions-item label="执行顺序">
                  {{ record.executionOrder || 1 }}
                </a-descriptions-item>
                <a-descriptions-item label="延迟执行">
                  {{ record.delayMs || 0 }}ms
                </a-descriptions-item>
              </a-descriptions>

              <a-descriptions label="设备列表" :column="1" size="small" bordered>
                <template v-for="device in record.linkageDevices" :key="device.deviceId">
                  <a-descriptions-item :label="device.deviceName">
                    <a-tag size="small" :color="getDeviceStatusColor(device.status)">
                      {{ device.status }}
                    </a-tag>
                    <span class="device-location">{{ device.location }}</span>
                  </a-descriptions-item>
                </template>
              </a-descriptions>
            </div>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 联动历史 -->
    <div class="history-section">
      <a-card :bordered="false">
        <template #title>
          <span>联动执行历史</span>
          <a-button type="link" @click="viewAllHistory">查看全部</a-button>
        </template>

        <a-timeline>
          <a-timeline-item
            v-for="item in linkageHistory"
            :key="item.id"
            :color="getHistoryColor(item.status)"
          >
            <div class="history-item">
              <div class="history-header">
                <span class="rule-name">{{ item.ruleName }}</span>
                <a-tag :color="getHistoryColor(item.status)">{{ item.status }}</a-tag>
                <span class="history-time">{{ formatTime(item.executeTime) }}</span>
              </div>
              <div class="history-content">
                <p>{{ item.description }}</p>
                <div v-if="item.devices.length > 0" class="affected-devices">
                  <span class="device-label">影响设备:</span>
                  <a-tag
                    v-for="device in item.devices"
                    :key="device.deviceId"
                    size="small"
                    class="device-tag"
                  >
                    {{ device.deviceName }}
                  </a-tag>
                </div>
              </div>
            </div>
          </a-timeline-item>
        </a-timeline>
      </a-card>
    </div>

    <!-- 添加/编辑规则弹窗 -->
    <LinkageRuleModal
      v-model:visible="ruleModalVisible"
      :is-edit="isEditMode"
      :rule-data="currentRule"
      @success="handleRuleSuccess"
    />

    <!-- 批量操作弹窗 -->
    <BatchOperationModal
      v-model:visible="batchModalVisible"
      :selected-rows="selectedRowKeys"
      @success="handleBatchSuccess"
    />

    <!-- 执行测试弹窗 -->
    <RuleTestModal
      v-model:visible="testModalVisible"
      :rule-data="currentTestRule"
      @result="handleTestResult"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  AppstoreOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
} from '@ant-design/icons-vue';
import { useGlobalLinkageStore } from '/@/store/modules/business/global-linkage';
import LinkageRuleModal from '../components/LinkageRuleModal.vue';
import BatchOperationModal from '../components/BatchOperationModal.vue';
import RuleTestModal from '../components/RuleTestModal.vue';
import { formatDateTime, formatTime } from '/@/utils/format';

// Store
const globalLinkageStore = useGlobalLinkageStore();

// 响应式数据
const loading = ref(false);
const ruleList = computed(() => globalLinkageStore.ruleList);
const linkageHistory = computed(() => globalLinkageStore.linkageHistory);
const selectedRowKeys = ref([]);
const ruleModalVisible = ref(false);
const batchModalVisible = ref(false);
const testModalVisible = ref(false);
const isEdit = ref(false);
const currentRule = ref(null);
const currentTestRule = ref(null);

// 搜索参数
const searchParams = reactive({
  ruleName: '',
  ruleType: '',
  status: '',
});

// 分页配置
const pagination = computed(() => ({
  current: globalLinkageStore.queryParams.pageNum,
  pageSize: globalLinkageStore.queryParams.pageSize,
  total: globalLinkageStore.ruleTotal,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
}));

// 行选择配置
const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (selectedKeys) => {
    selectedRowKeys.value = selectedKeys;
  },
};

// 表格列定义
const ruleColumns = [
  {
    title: '规则名称',
    dataIndex: 'ruleName',
    key: 'ruleName',
    width: 200,
    fixed: 'left',
  },
  {
    title: '规则类型',
    dataIndex: 'ruleType',
    key: 'ruleType',
    width: 120,
    render: (type) => {
      const typeMap = {
        'DEVICE_STATUS': '设备状态',
        'ACCESS_RESULT': '通行结果',
        'TIME_BASED': '时间触发',
        'EMERGENCY': '紧急情况',
      };
      return typeMap[type] || type;
    },
  },
  {
    title: '触发条件',
    dataIndex: 'triggerCondition',
    key: 'triggerCondition',
    width: 150,
    ellipsis: true,
  },
  {
    title: '联动动作',
    dataIndex: 'linkageAction',
    key: 'linkageAction',
    width: 150,
    ellipsis: true,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80,
    slots: { customRender: 'status' },
  },
  {
    title: '执行次数',
    dataIndex: 'executionCount',
    key: 'executionCount',
    width: 100,
    sorter: true,
  },
  {
    title: '最后执行',
    dataIndex: 'lastExecuteTime',
    key: 'lastExecuteTime',
    width: 150,
    render: (time) => formatTime(time),
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 150,
    render: (time) => formatTime(time),
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
    slots: { customRender: 'action' },
  },
];

// 方法

/**
 * 获取规则列表
 */
const fetchRuleList = async () => {
  loading.value = true;
  try {
    await globalLinkageStore.fetchRuleList({
      ...searchParams,
    });
  } catch (error) {
    console.error('获取联动规则失败:', error);
  } finally {
    loading.value = false;
  }
};

/**
 * 获取联动历史
 */
const fetchLinkageHistory = async () => {
  try {
    await globalLinkageStore.fetchLinkageHistory({ pageSize: 10 });
  } catch (error) {
    console.error('获取联动历史失败:', error);
  }
};

/**
 * 刷新数据
 */
const refreshData = async () => {
  await Promise.all([
    fetchRuleList(),
    fetchLinkageHistory(),
  ]);
};

/**
 * 搜索
 */
const handleSearch = () => {
  globalLinkageStore.setQueryParams({ pageNum: 1 });
  fetchRuleList();
};

/**
 * 添加规则
 */
const handleAddRule = () => {
  isEdit.value = false;
  currentRule.value = null;
  ruleModalVisible.value = true;
};

/**
 * 编辑规则
 */
const handleEdit = (record) => {
  isEdit.value = true;
  currentRule.value = record;
  ruleModalVisible.value = true;
};

/**
 * 切换规则状态
 */
const handleToggleStatus = async (record) => {
  const newStatus = record.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
  await globalLinkageStore.updateRuleStatus(record.ruleId, newStatus);
};

/**
 * 测试规则
 */
const handleTest = (record) => {
  currentTestRule.value = record;
  testModalVisible.value = true;
};

/**
 * 删除规则
 */
const handleDelete = async (record) => {
  await globalLinkageStore.deleteRule(record.ruleId);
};

/**
 * 批量操作
 */
const handleBatchOperation = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要操作的规则');
    return;
  }
  batchModalVisible.value = true;
};

/**
 * 查看全部历史
 */
const viewAllHistory = () => {
  message.info('查看全部历史功能开发中');
};

/**
 * 表格变化
 */
const handleTableChange = (pagi, filters, sorter) => {
  globalLinkageStore.setQueryParams({
    pageNum: pagi.current,
    pageSize: pagi.pageSize,
  });
  fetchRuleList();
};

/**
 * 展开行
 */
const handleExpand = (expanded, record) => {
  // 展开行时的逻辑
  console.log('展开行:', expanded, record);
};

/**
 * 规则成功回调
 */
const handleRuleSuccess = () => {
  ruleModalVisible.value = false;
  currentRule.value = null;
  fetchRuleList();
};

/**
 * 批量操作成功回调
 */
const handleBatchSuccess = () => {
  batchModalVisible.value = false;
  selectedRowKeys.value = [];
  fetchRuleList();
};

/**
 * 测试结果回调
 */
const handleTestResult = (result) => {
  testModalVisible.value = false;
  currentTestRule.value = null;
};

// 辅助方法

/**
 * 获取状态颜色
 */
const getStatusColor = (status) => {
  return status === 'ACTIVE' ? 'green' : 'default';
};

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  return status === 'ACTIVE' ? '启用' : '禁用';
};

/**
 * 获取规则数量颜色
 */
const getRuleCountColor = (count) => {
  if (count === 0) return 'default';
  if (count < 5) return 'green';
  if (count < 10) return 'orange';
  return 'red';
};

/**
 * 获取触发条件文本
 */
const getTriggerConditionText = (condition) => {
  // 根据触发条件对象生成文本描述
  try {
    const conditionObj = JSON.parse(condition || '{}');
    const parts = [];

    if (conditionObj.deviceStatus) {
      parts.push(`设备状态: ${conditionObj.deviceStatus}`);
    }
    if (conditionObj.accessResult) {
      parts.push(`通行结果: ${conditionObj.accessResult}`);
    }
    if (conditionObj.timeCondition) {
      parts.push(`时间条件: ${conditionObj.timeCondition}`);
    }

    return parts.join(', ') || '无特定条件';
  } catch (error) {
    return condition || '解析失败';
  }
};

/**
 * 获取联动动作文本
 */
const getLinkageActionText = (action) => {
  try {
    const actionObj = JSON.parse(action || '{}');
    const parts = [];

    if (actionObj.openDoor) {
      parts.push('远程开门');
    }
    if (actionObj.closeDoor) {
      parts.push('远程关门');
    }
    if (actionObj.lockDevice) {
      parts.push('锁定设备');
    }
    if (actionObj.unlockDevice) {
      parts.push('解锁设备');
    }
    if (actionObj.sendAlert) {
      parts.push('发送告警');
    }

    return parts.join(', ') || '无特定动作';
  } catch (error) {
    return action || '解析失败';
  }
};

/**
 * 获取设备状态颜色
 */
const getDeviceStatusColor = (status) => {
  const colorMap = {
    'ONLINE': 'green',
    'OFFLINE': 'red',
    'FAULT': 'red',
    'MAINTENANCE': 'orange',
  };
  return colorMap[status] || 'default';
};

/**
 * 获取历史颜色
 */
const getHistoryColor = (status) => {
  const colorMap = {
    'SUCCESS': 'green',
    'FAILED': 'red',
    'PARTIAL': 'orange',
  };
  return colorMap[status] || 'gray';
};

// 生命周期
onMounted(async () => {
  await refreshData();
});
</script>

<style lang="less" scoped>
.global-linkage-management {
  padding: 24px;
  background: #f5f5f5;

  .page-header {
    margin-bottom: 24px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-title {
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
          font-weight: 600;
          color: #262626;
        }

        p {
          margin: 0;
          color: #8c8c8c;
          font-size: 14px;
        }
      }
    }
  }

  .rules-section {
    margin-bottom: 24px;

    .search-section {
      margin-bottom: 16px;
    }

    .expanded-content {
      background: #fafafa;
      padding: 16px;
      border-radius: 6px;
      margin-top: 8px;
    }

    .device-tag {
      margin-left: 8px;
    }

    .device-location {
      margin-left: 4px;
      color: #666;
    }
  }

  .history-section {
    .history-item {
      .history-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .rule-name {
          font-weight: 600;
          color: #262626;
        }

        .history-time {
          margin-left: auto;
          font-size: 12px;
          color: #999;
        }
      }

      .history-content {
        color: #666;
        margin-bottom: 8px;
      }

      .affected-devices {
        display: flex;
        align-items: center;
        gap: 4px;
        margin-top: 8px;

        .device-label {
          font-size: 12px;
          color: #666;
        }

        .device-tag {
          margin-right: 4px;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .global-linkage-management {
    padding: 16px;

    .page-header {
      .header-content {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }
    }

    .search-section {
      .ant-row {
        .ant-col {
          margin-bottom: 8px;
        }
      }
    }
  }
}
</style>
