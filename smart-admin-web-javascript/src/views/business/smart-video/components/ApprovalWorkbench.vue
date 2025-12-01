<!--
  审批工作台组件

  @Author:    Claude Code
  @Date:      2025-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="approval-workbench">
    <div class="workbench-layout">
      <!-- 左侧：待审批列表 -->
      <div class="pending-list">
        <div class="list-header">
          <h4>待审批申请</h4>
          <a-badge :count="pendingCount" :offset="[-5, 5]">
            <span class="pending-label">待处理</span>
          </a-badge>
          <a-button size="small" type="primary" @click="handleBatchApprove">
            批量审批
          </a-button>
        </div>

        <div class="pending-items">
          <div
            v-for="item in pendingApplications"
            :key="item.id"
            class="pending-item"
            :class="{ selected: selectedItem?.id === item.id }"
            @click="handleItemSelect(item)"
          >
            <div class="item-header">
              <a-tag v-if="item.urgent" color="red">紧急</a-tag>
              <span class="item-title">{{ item.title }}</span>
            </div>
            <div class="item-content">
              <p class="item-desc">{{ item.description }}</p>
              <div class="item-meta">
                <span><CalendarOutlined /> {{ item.applyTime }}</span>
                <span><UserOutlined /> {{ item.department }}</span>
              </div>
            </div>
            <div class="item-actions">
              <a-button size="small" type="primary" @click.stop="handleApprove(item)">
                <template #icon><CheckOutlined /></template>
                通过
              </a-button>
              <a-button size="small" danger @click.stop="handleReject(item)">
                <template #icon><CloseOutlined /></template>
                拒绝
              </a-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：统计和历史 -->
      <div class="right-panel">
        <!-- 审批统计 -->
        <div class="statistics-card">
          <h4>审批统计</h4>
          <div class="stats-list">
            <div class="stat-item">
              <span class="stat-label">待审批</span>
              <span class="stat-value pending">{{ pendingCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">已审批</span>
              <span class="stat-value approved">{{ approvedCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">今日已处理</span>
              <span class="stat-value today">{{ todayProcessed }}</span>
            </div>
          </div>
        </div>

        <!-- 审批历史 -->
        <div class="history-card">
          <h4>最近审批记录</h4>
          <a-table
            :columns="historyColumns"
            :data-source="approvalHistory"
            :pagination="false"
            :loading="loading"
            size="small"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'type'">
                <span>{{ record.typeName }}</span>
              </template>
              <template v-else-if="column.key === 'result'">
                <a-tag :color="getResultColor(record.result)">
                  {{ record.resultName }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'approveTime'">
                <span>{{ record.approveTime }}</span>
              </template>
              <template v-else-if="column.key === 'opinion'">
                <span>{{ record.opinion }}</span>
              </template>
            </template>
          </a-table>
        </div>
      </div>
    </div>

    <!-- 审批意见模态框 -->
    <a-modal
      v-model:open="opinionModalVisible"
      :title="opinionModalTitle"
      width="500"
      @ok="handleOpinionSubmit"
      @cancel="opinionModalVisible = false"
    >
      <a-form :model="opinionForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="审批意见">
          <a-textarea
            v-model:value="opinionForm.opinion"
            placeholder="请输入审批意见"
            :rows="4"
          />
        </a-form-item>
        <a-form-item label="抄送人员">
          <a-select
            v-model:value="opinionForm.ccUsers"
            mode="multiple"
            placeholder="请选择抄送人员"
          >
            <a-select-option value="user1">张三</a-select-option>
            <a-select-option value="user2">李四</a-select-option>
            <a-select-option value="user3">王五</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  CalendarOutlined,
  UserOutlined,
  CheckOutlined,
  CloseOutlined,
} from '@ant-design/icons-vue';

const emit = defineEmits(['approve', 'reject']);

// 响应式数据
const loading = ref(false);
const opinionModalVisible = ref(false);
const opinionModalType = ref('approve'); // approve 或 reject
const selectedItem = ref(null);
const opinionForm = ref({
  opinion: '',
  ccUsers: [],
});

const pendingApplications = ref([]);
const approvalHistory = ref([]);

// 计算属性
const pendingCount = computed(() => pendingApplications.value.length);
const approvedCount = computed(() => approvalHistory.value.filter(h => h.result === 'approved').length);
const todayProcessed = computed(() => {
  const today = new Date().toLocaleDateString();
  return approvalHistory.value.filter(h => h.approveTime.startsWith(today)).length;
});

const opinionModalTitle = computed(() => {
  return opinionModalType.value === 'approve' ? '审批通过' : '审批拒绝';
});

// 审批历史表格列
const historyColumns = [
  {
    title: '申请人',
    dataIndex: 'applicant',
    key: 'applicant',
    width: 100,
  },
  {
    title: '申请类型',
    dataIndex: 'type',
    key: 'type',
    width: 100,
  },
  {
    title: '审批结果',
    dataIndex: 'result',
    key: 'result',
    width: 80,
  },
  {
    title: '审批时间',
    dataIndex: 'approveTime',
    key: 'approveTime',
    width: 120,
  },
  {
    title: '审批意见',
    dataIndex: 'opinion',
    key: 'opinion',
    ellipsis: true,
  },
];

// 方法
const getResultColor = (result) => {
  const colors = {
    'approved': 'green',
    'rejected': 'red',
  };
  return colors[result] || 'default';
};

const handleItemSelect = (item) => {
  selectedItem.value = item;
};

const handleApprove = (item) => {
  selectedItem.value = item;
  opinionModalType.value = 'approve';
  opinionForm.value.opinion = '同意';
  opinionModalVisible.value = true;
};

const handleReject = (item) => {
  selectedItem.value = item;
  opinionModalType.value = 'reject';
  opinionForm.value.opinion = '';
  opinionModalVisible.value = true;
};

const handleOpinionSubmit = () => {
  const action = opinionModalType.value;
  const item = selectedItem.value;

  if (!opinionForm.value.opinion.trim()) {
    message.warning('请输入审批意见');
    return;
  }

  if (action === 'approve') {
    message.success(`已审批通过：${item.applicant}`);
    emit('approve', {
      ...item,
      opinion: opinionForm.value.opinion,
    });
  } else {
    message.warning(`已拒绝申请：${item.applicant}`);
    emit('reject', {
      ...item,
      opinion: opinionForm.value.opinion,
    });
  }

  opinionModalVisible.value = false;
  loadPendingApplications();
  loadApprovalHistory();
};

const handleBatchApprove = () => {
  message.info('批量审批功能开发中...');
};

const loadPendingApplications = async () => {
  // Mock 数据
  pendingApplications.value = [
    {
      id: 'PENDING001',
      title: '病假申请 - 李四',
      description: '申请时间：3天 (2024-01-16 至 2024-01-18)',
      detail: '身体不适，需要去医院检查',
      applyTime: '2024-01-15 14:30',
      department: '技术研发部',
      applicant: '李四',
      type: 'leave',
      urgent: true,
    },
    {
      id: 'PENDING002',
      title: '加班申请 - 王五',
      description: '加班时间：3小时 (2024-01-15 19:00-22:00)',
      detail: '项目紧急上线，需要加班完成',
      applyTime: '2024-01-15 16:00',
      department: '市场部',
      applicant: '王五',
      type: 'overtime',
      urgent: false,
    },
    {
      id: 'PENDING003',
      title: '调班申请 - 赵六',
      description: '原班次：2024-01-16 早班，调班：2024-01-17 早班',
      detail: '个人原因需要调班',
      applyTime: '2024-01-15 10:00',
      department: '销售部',
      applicant: '赵六',
      type: 'shift',
      urgent: false,
    },
  ];
};

const loadApprovalHistory = async () => {
  loading.value = true;
  try {
    // 模拟延迟
    await new Promise(resolve => setTimeout(resolve, 300));

    // Mock 数据
    approvalHistory.value = [
      {
        id: 'HISTORY001',
        applicant: '赵六',
        typeName: '请假申请',
        result: 'approved',
        resultName: '通过',
        approveTime: '2025-11-05 10:30',
        opinion: '同意',
      },
      {
        id: 'HISTORY002',
        applicant: '钱七',
        typeName: '补签申请',
        result: 'rejected',
        resultName: '拒绝',
        approveTime: '2025-11-05 09:15',
        opinion: '理由不充分',
      },
      {
        id: 'HISTORY003',
        applicant: '孙八',
        typeName: '加班申请',
        result: 'approved',
        resultName: '通过',
        approveTime: '2025-11-04 16:45',
        opinion: '同意',
      },
    ];
  } catch (error) {
    console.error('加载失败:', error);
    message.error('加载审批历史失败');
  } finally {
    loading.value = false;
  }
};

// 公共方法
const refresh = () => {
  loadPendingApplications();
  loadApprovalHistory();
};

// 初始化
onMounted(() => {
  refresh();
});

defineExpose({
  refresh,
});
</script>

<style lang="less" scoped>
.approval-workbench {
  .workbench-layout {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 24px;
  }

  .pending-list {
    background: #fff;
    border-radius: 8px;
    padding: 16px;

    .list-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;

      h4 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
      }

      .pending-label {
        font-size: 14px;
        color: #666;
      }
    }

    .pending-items {
      max-height: 600px;
      overflow-y: auto;

      .pending-item {
        border: 1px solid #e8e8e8;
        border-radius: 8px;
        padding: 12px;
        margin-bottom: 12px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: #1890ff;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        &.selected {
          border-color: #1890ff;
          background: #e6f7ff;
        }

        .item-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;

          .item-title {
            font-weight: 600;
            color: #333;
          }
        }

        .item-content {
          margin-bottom: 8px;

          .item-desc {
            margin: 0 0 4px;
            font-size: 14px;
            color: #666;
          }

          .item-meta {
            display: flex;
            gap: 16px;
            font-size: 12px;
            color: #999;

            span {
              display: flex;
              align-items: center;
              gap: 4px;
            }
          }
        }

        .item-actions {
          display: flex;
          gap: 8px;
        }
      }
    }
  }

  .right-panel {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .statistics-card,
    .history-card {
      background: #fff;
      border-radius: 8px;
      padding: 16px;

      h4 {
        margin: 0 0 16px;
        font-size: 16px;
        font-weight: 600;
      }
    }

    .statistics-card {
      .stats-list {
        .stat-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px 0;
          border-bottom: 1px solid #f0f0f0;

          &:last-child {
            border-bottom: none;
          }

          .stat-label {
            font-size: 14px;
            color: #666;
          }

          .stat-value {
            font-size: 20px;
            font-weight: 600;

            &.pending {
              color: #faad14;
            }

            &.approved {
              color: #52c41a;
            }

            &.today {
              color: #1890ff;
            }
          }
        }
      }
    }

    .history-card {
      flex: 1;

      :deep(.ant-table) {
        .ant-table-tbody {
          .ant-table-row {
            &:hover {
              background: #f5f5f5;
            }
          }
        }
      }
    }
  }
}
</style>
