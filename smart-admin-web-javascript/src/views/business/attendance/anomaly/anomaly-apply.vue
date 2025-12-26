<template>
  <div class="anomaly-apply-container">
    <!-- 页面标题 -->
    <a-page-header
      title="异常申请管理"
      sub-title="提交和查看考勤异常申请"
      @back="() => $router.back()"
    />

    <!-- Tab切换 -->
    <a-card :bordered="false" class="tab-card">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <!-- 我提交的申请 -->
        <a-tab-pane key="my-applies" tab="我的申请">
          <div class="apply-actions">
            <a-button type="primary" @click="showApplyModal">
              <template #icon><PlusOutlined /></template>
              提交新申请
            </a-button>
          </div>

          <a-table
            :columns="applyColumns"
            :data-source="myApplies"
            :loading="loading"
            :pagination="false"
            rowKey="applyId"
          >
            <template #applyType="{ record }">
              <a-tag :color="getApplyTypeColor(record.applyType)">
                {{ getApplyTypeName(record.applyType) }}
              </a-tag>
            </template>

            <template #applyStatus="{ record }">
              <a-badge :status="getApplyStatusBadge(record.applyStatus)" :text="getApplyStatusName(record.applyStatus)" />
            </template>

            <template #action="{ record }">
              <a-space>
                <a-button type="link" size="small" @click="handleViewApply(record)">
                  查看详情
                </a-button>
                <a-button
                  v-if="record.applyStatus === 'PENDING'"
                  type="link"
                  size="small"
                  danger
                  @click="handleCancelApply(record)"
                >
                  撤销申请
                </a-button>
              </a-space>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 待审批的申请（管理员） -->
        <a-tab-pane key="pending-approvals" tab="待审批申请">
          <div class="apply-actions">
            <a-space>
              <a-button type="primary" @click="handleBatchApprove(true)">
                <template #icon><CheckOutlined /></template>
                批量批准
              </a-button>
              <a-button danger @click="handleBatchReject(false)">
                <template #icon><CloseOutlined /></template>
                批量驳回
              </a-button>
            </a-space>
          </div>

          <a-table
            :columns="approvalColumns"
            :data-source="pendingApplies"
            :loading="loading"
            :pagination="false"
            :row-selection="rowSelection"
            rowKey="applyId"
          >
            <template #applyType="{ record }">
              <a-tag :color="getApplyTypeColor(record.applyType)">
                {{ getApplyTypeName(record.applyType) }}
              </a-tag>
            </template>

            <template #action="{ record }">
              <a-space>
                <a-button type="link" size="small" @click="handleViewApply(record)">
                  查看详情
                </a-button>
                <a-button type="link" size="small" @click="handleApprove(record)">
                  批准
                </a-button>
                <a-button type="link" size="small" danger @click="handleReject(record)">
                  驳回
                </a-button>
              </a-space>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 提交申请弹窗 -->
    <a-modal
      v-model:visible="applyModalVisible"
      title="提交异常申请"
      width="600px"
      @ok="handleSubmitApply"
    >
      <a-form
        ref="applyFormRef"
        :model="applyForm"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
        :rules="applyFormRules"
      >
        <a-form-item label="申请类型" name="applyType" required>
          <a-select v-model:value="applyForm.applyType" placeholder="请选择申请类型">
            <a-select-option value="SUPPLEMENT_CARD">补卡申请</a-select-option>
            <a-select-option value="LATE_EXPLANATION">迟到说明</a-select-option>
            <a-select-option value="EARLY_EXPLANATION">早退说明</a-select-option>
            <a-select-option value="ABSENT_APPEAL">旷工申诉</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="考勤日期" name="attendanceDate" required>
          <a-date-picker
            v-model:value="applyForm.attendanceDate"
            format="YYYY-MM-DD"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="打卡类型" name="punchType" v-if="applyForm.applyType === 'SUPPLEMENT_CARD'">
          <a-radio-group v-model:value="applyForm.punchType">
            <a-radio value="CHECK_IN">上班打卡</a-radio>
            <a-radio value="CHECK_OUT">下班打卡</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item
          label="申请打卡时间"
          name="appliedPunchTime"
          v-if="applyForm.applyType === 'SUPPLEMENT_CARD'"
          required
        >
          <a-date-picker
            v-model:value="applyForm.appliedPunchTime"
            format="YYYY-MM-DD HH:mm"
            showTime
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="申请原因" name="applyReason" required>
          <a-input v-model:value="applyForm.applyReason" placeholder="请输入申请原因" />
        </a-form-item>

        <a-form-item label="详细说明" name="description">
          <a-textarea
            v-model:value="applyForm.description"
            placeholder="请输入详细说明"
            :rows="4"
          />
        </a-form-item>

        <a-form-item label="附件上传">
          <a-upload
            name="file"
            :action="uploadUrl"
            :headers="uploadHeaders"
          >
            <a-button>
              <UploadOutlined /> 点击上传
            </a-button>
          </a-upload>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 申请详情弹窗 -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="申请详情"
      width="700px"
      :footer="null"
    >
      <a-descriptions :column="1" bordered v-if="currentApply">
        <a-descriptions-item label="申请单号">
          {{ currentApply.applyNo }}
        </a-descriptions-item>
        <a-descriptions-item label="申请人">
          {{ currentApply.applicantName }}
        </a-descriptions-item>
        <a-descriptions-item label="部门">
          {{ currentApply.departmentName }}
        </a-descriptions-item>
        <a-descriptions-item label="申请类型">
          <a-tag :color="getApplyTypeColor(currentApply.applyType)">
            {{ getApplyTypeName(currentApply.applyType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="考勤日期">
          {{ currentApply.attendanceDate }}
        </a-descriptions-item>
        <a-descriptions-item label="申请原因">
          {{ currentApply.applyReason }}
        </a-descriptions-item>
        <a-descriptions-item label="详细说明">
          {{ currentApply.description || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="申请状态">
          <a-badge :status="getApplyStatusBadge(currentApply.applyStatus)" :text="getApplyStatusName(currentApply.applyStatus)" />
        </a-descriptions-item>
        <a-descriptions-item label="申请时间">
          {{ formatDateTime(currentApply.applyTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="审批意见" v-if="currentApply.approveComment">
          {{ currentApply.approveComment }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 审批操作（管理员） -->
      <div class="approval-actions" v-if="currentApply && currentApply.applyStatus === 'PENDING' && activeTab === 'pending-approvals'">
        <a-divider />
        <a-space style="width: 100%; display: flex; justify-content: center;">
          <a-button type="primary" @click="handleApprove(currentApply)">
            批准
          </a-button>
          <a-button danger @click="handleReject(currentApply)">
            驳回
          </a-button>
        </a-space>
      </div>
    </a-modal>

    <!-- 驳回原因弹窗 -->
    <a-modal
      v-model:visible="rejectModalVisible"
      title="驳回申请"
      @ok="handleConfirmReject"
    >
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="驳回原因" required>
          <a-textarea
            v-model:value="rejectReason"
            placeholder="请输入驳回原因"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  PlusOutlined,
  CheckOutlined,
  CloseOutlined,
  UploadOutlined
} from '@ant-design/icons-vue';
import { anomalyApi } from '@/api/business/attendance/anomaly-api';
import dayjs from 'dayjs';

// Tab
const activeTab = ref('my-applies');

// 数据源
const myApplies = ref([]);
const pendingApplies = ref([]);
const loading = ref(false);

// 行选择
const rowSelection = {
  selectedRowKeys: ref([]),
  onChange: (selectedKeys) => {
    rowSelection.selectedRowKeys.value = selectedKeys;
  }
};

// 提交申请弹窗
const applyModalVisible = ref(false);
const applyFormRef = ref(null);
const applyForm = reactive({
  applyType: null,
  attendanceDate: null,
  punchType: 'CHECK_IN',
  appliedPunchTime: null,
  applyReason: '',
  description: ''
});

const applyFormRules = {
  applyType: [{ required: true, message: '请选择申请类型' }],
  attendanceDate: [{ required: true, message: '请选择考勤日期' }],
  appliedPunchTime: [{ required: true, message: '请选择打卡时间' }],
  applyReason: [{ required: true, message: '请输入申请原因' }]
};

// 详情弹窗
const detailModalVisible = ref(false);
const currentApply = ref(null);

// 驳回弹窗
const rejectModalVisible = ref(false);
const rejectReason = ref('');
const rejectApplyId = ref(null);

// 表格列配置
const applyColumns = [
  {
    title: '申请单号',
    dataIndex: 'applyNo',
    key: 'applyNo'
  },
  {
    title: '申请类型',
    key: 'applyType',
    slots: { customRender: 'applyType' }
  },
  {
    title: '考勤日期',
    dataIndex: 'attendanceDate',
    key: 'attendanceDate'
  },
  {
    title: '申请原因',
    dataIndex: 'applyReason',
    key: 'applyReason',
    ellipsis: true
  },
  {
    title: '申请时间',
    dataIndex: 'applyTime',
    key: 'applyTime',
    customRender: ({ record }) => formatDateTime(record.applyTime)
  },
  {
    title: '状态',
    key: 'applyStatus',
    slots: { customRender: 'applyStatus' }
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' }
  }
];

const approvalColumns = [
  ...applyColumns.slice(0, -1),
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' }
  }
];

// Tab切换
const handleTabChange = () => {
  if (activeTab.value === 'my-applies') {
    fetchMyApplies();
  } else {
    fetchPendingApplies();
  }
};

// 查询我的申请
const fetchMyApplies = async () => {
  loading.value = true;
  try {
    const response = await anomalyApi.getMyApplies(1); // TODO: 从用户信息获取
    if (response.data) {
      myApplies.value = response.data;
    }
  } catch (error) {
    message.error('查询申请列表失败');
  } finally {
    loading.value = false;
  }
};

// 查询待审批申请
const fetchPendingApplies = async () => {
  loading.value = true;
  try {
    const response = await anomalyApi.getPendingApplies();
    if (response.data) {
      pendingApplies.value = response.data;
    }
  } catch (error) {
    message.error('查询待审批申请失败');
  } finally {
    loading.value = false;
  }
};

// 显示申请弹窗
const showApplyModal = () => {
  Object.assign(applyForm, {
    applyType: null,
    attendanceDate: null,
    punchType: 'CHECK_IN',
    appliedPunchTime: null,
    applyReason: '',
    description: ''
  });
  applyModalVisible.value = true;
};

// 提交申请
const handleSubmitApply = async () => {
  try {
    await applyFormRef.value.validate();

    const data = {
      applicantId: 1, // TODO: 从用户信息获取
      applicantName: '张三', // TODO: 从用户信息获取
      departmentId: 1, // TODO: 从用户信息获取
      departmentName: '研发部', // TODO: 从用户信息获取
      attendanceDate: applyForm.attendanceDate.format('YYYY-MM-DD'),
      punchType: applyForm.punchType,
      appliedPunchTime: applyForm.appliedPunchTime?.format('YYYY-MM-DD HH:mm:ss'),
      applyReason: applyForm.applyReason,
      description: applyForm.description
    };

    let response;
    switch (applyForm.applyType) {
      case 'SUPPLEMENT_CARD':
        response = await anomalyApi.submitSupplementCardApply(data);
        break;
      case 'LATE_EXPLANATION':
        response = await anomalyApi.submitLateExplanationApply(data);
        break;
      case 'EARLY_EXPLANATION':
        response = await anomalyApi.submitEarlyExplanationApply(data);
        break;
      case 'ABSENT_APPEAL':
        response = await anomalyApi.submitAbsentAppealApply(data);
        break;
    }

    if (response.data) {
      message.success('申请提交成功');
      applyModalVisible.value = false;
      fetchMyApplies();
    }
  } catch (error) {
    message.error('申请提交失败');
  }
};

// 查看详情
const handleViewApply = (record) => {
  currentApply.value = record;
  detailModalVisible.value = true;
};

// 撤销申请
const handleCancelApply = (record) => {
  Modal.confirm({
    title: '确认撤销',
    content: '确定要撤销此申请吗？',
    onOk: async () => {
      try {
        await anomalyApi.cancelApply(record.applyId, 1); // TODO: 从用户信息获取
        message.success('撤销成功');
        fetchMyApplies();
      } catch (error) {
        message.error('撤销失败');
      }
    }
  });
};

// 批准申请
const handleApprove = (record) => {
  Modal.confirm({
    title: '确认批准',
    content: '确定要批准此申请吗？',
    onOk: async () => {
      try {
        await anomalyApi.approveApply(
          record.applyId,
          1, // TODO: 从用户信息获取
          '管理员',
          ''
        );
        message.success('批准成功');
        detailModalVisible.value = false;
        fetchPendingApplies();
      } catch (error) {
        message.error('批准失败');
      }
    }
  });
};

// 驳回申请
const handleReject = (record) => {
  rejectApplyId.value = record.applyId;
  rejectReason.value = '';
  rejectModalVisible.value = true;
};

// 确认驳回
const handleConfirmReject = async () => {
  try {
    await anomalyApi.rejectApply(
      rejectApplyId.value,
      1,
      '管理员',
      rejectReason.value
    );
    message.success('驳回成功');
    rejectModalVisible.value = false;
    detailModalVisible.value = false;
    fetchPendingApplies();
  } catch (error) {
    message.error('驳回失败');
  }
};

// 批量批准
const handleBatchApprove = (approve) => {
  if (rowSelection.selectedRowKeys.value.length === 0) {
    message.warning('请先选择要操作的记录');
    return;
  }

  Modal.confirm({
    title: approve ? '确认批量批准' : '确认批量驳回',
    content: `确定要${approve ? '批准' : '驳回'}选中的 ${rowSelection.selectedRowKeys.value.length} 条申请吗？`,
    onOk: async () => {
      try {
        await anomalyApi.batchApprove({
          applyIds: rowSelection.selectedRowKeys.value,
          approverId: 1,
          approverName: '管理员',
          comment: '',
          approve: approve
        });
        message.success(`${approve ? '批准' : '驳回'}成功`);
        rowSelection.selectedRowKeys.value = [];
        fetchPendingApplies();
      } catch (error) {
        message.error('操作失败');
      }
    }
  });
};

// 批量驳回
const handleBatchReject = () => {
  handleBatchApprove(false);
};

// 辅助函数
const getApplyTypeName = (type) => {
  const names = {
    'SUPPLEMENT_CARD': '补卡申请',
    'LATE_EXPLANATION': '迟到说明',
    'EARLY_EXPLANATION': '早退说明',
    'ABSENT_APPEAL': '旷工申诉'
  };
  return names[type] || type;
};

const getApplyTypeColor = (type) => {
  const colors = {
    'SUPPLEMENT_CARD': 'blue',
    'LATE_EXPLANATION': 'orange',
    'EARLY_EXPLANATION': 'volcano',
    'ABSENT_APPEAL': 'red'
  };
  return colors[type] || 'default';
};

const getApplyStatusName = (status) => {
  const names = {
    'PENDING': '待审批',
    'APPROVED': '已批准',
    'REJECTED': '已驳回',
    'CANCELLED': '已撤销'
  };
  return names[status] || status;
};

const getApplyStatusBadge = (status) => {
  const badges = {
    'PENDING': 'processing',
    'APPROVED': 'success',
    'REJECTED': 'error',
    'CANCELLED': 'default'
  };
  return badges[status] || 'default';
};

const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss');
};

// 初始化
onMounted(() => {
  fetchMyApplies();
});
</script>

<style scoped lang="less">
.anomaly-apply-container {
  padding: 24px;
}

.tab-card {
  margin-bottom: 16px;
}

.apply-actions {
  margin-bottom: 16px;
}

.approval-actions {
  margin-top: 16px;
}
</style>
