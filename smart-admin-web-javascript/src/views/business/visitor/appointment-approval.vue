<template>
  <div class="appointment-approval">
    <!-- 页面头部 -->
    <a-card title="访客预约审批" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-space>
            <a-button type="primary" @click="batchApprove">
              <template #icon><CheckOutlined /></template>
              批量批准
            </a-button>
            <a-button @click="batchReject">
              <template #icon><CloseOutlined /></template>
              批量驳回
            </a-button>
          </a-space>
        </a-col>
        <a-col :span="12" style="text-align: right">
          <a-space>
            <a-select
              v-model:value="statusFilter"
              style="width: 150px"
              @change="handleStatusFilterChange"
            >
              <a-select-option :value="0">待审批</a-select-option>
              <a-select-option :value="1">已批准</a-select-option>
              <a-select-option :value="2">已驳回</a-select-option>
              <a-select-option :value="-1">全部</a-select-option>
            </a-select>
            <a-input-search
              v-model:value="searchText"
              placeholder="搜索访客姓名或手机号"
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
            title="待审批"
            :value="statistics.pendingCount"
            :value-style="{ color: '#faad14' }"
          >
            <template #suffix>
              <UserPendingOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="已批准"
            :value="statistics.approvedCount"
            :value-style="{ color: '#52c41a' }"
          >
            <template #suffix>
              <CheckCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="已驳回"
            :value="statistics.rejectedCount"
            :value-style="{ color: '#ff4d4f' }"
          >
            <template #suffix>
              <CloseCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="审批率"
            :value="statistics.approvalRate"
            suffix="%"
            :precision="1"
            :value-style="{ color: '#1890ff' }"
          >
            <template #suffix>
              <LikeOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 预约列表 -->
    <a-card title="预约列表" :bordered="false" style="margin-top: 16px">
      <a-table
        :columns="columns"
        :data-source="appointmentList"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        @change="handleTableChange"
        row-key="appointmentId"
      >
        <!-- 访客信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'visitorInfo'">
            <a-descriptions :column="1" size="small">
              <a-descriptions-item label="访客姓名">
                <a-tag color="blue">{{ record.visitorName }}</a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="联系电话">
                {{ record.phone }}
              </a-descriptions-item>
              <a-descriptions-item label="身份证号">
                {{ record.idCard }}
              </a-descriptions-item>
            </a-descriptions>
          </template>

          <!-- 访问信息 -->
          <template v-else-if="column.key === 'visitInfo'">
            <a-space direction="vertical" :size="4">
              <div>
                <CalendarOutlined style="margin-right: 4px" />
                访问日期：{{ record.visitDate }}
              </div>
              <div>
                <ClockCircleOutlined style="margin-right: 4px" />
                访问时段：{{ record.startTime }} - {{ record.endTime }}
              </div>
              <div>
                <EnvironmentOutlined style="margin-right: 4px" />
                访问区域：{{ record.areaName }}
              </div>
            </a-space>
          </template>

          <!-- 被访人信息 -->
          <template v-else-if="column.key === 'intervieweeInfo'">
            <a-space direction="vertical" :size="4">
              <div>
                <UserOutlined style="margin-right: 4px" />
                {{ record.intervieweeName }}
              </div>
              <div style="color: #8c8c8c; font-size: 12px">
                {{ record.departmentName }}
              </div>
              <div>
                <PhoneOutlined style="margin-right: 4px" />
                {{ record.intervieweePhone }}
              </div>
            </a-space>
          </template>

          <!-- 访问事由 -->
          <template v-else-if="column.key === 'visitReason'">
            <a-tooltip :title="record.visitReason">
              <div class="ellipsis-text">{{ record.visitReason }}</div>
            </a-tooltip>
          </template>

          <!-- 审批状态 -->
          <template v-else-if="column.key === 'approvalStatus'">
            <a-badge :status="getStatusBadge(record.approvalStatus)" />
            <a-tag :color="getStatusColor(record.approvalStatus)">
              {{ getStatusLabel(record.approvalStatus) }}
            </a-tag>
          </template>

          <!-- 提交时间 -->
          <template v-else-if="column.key === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="viewDetail(record)"
              >
                详情
              </a-button>
              <a-button
                v-if="record.approvalStatus === 0"
                type="link"
                size="small"
                @click="approve(record)"
              >
                批准
              </a-button>
              <a-button
                v-if="record.approvalStatus === 0"
                type="link"
                size="small"
                danger
                @click="reject(record)"
              >
                驳回
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 审批模态框 -->
    <a-modal
      v-model:visible="approvalModalVisible"
      :title="approvalModalTitle"
      width="700px"
      @ok="handleApprovalSubmit"
      @cancel="handleApprovalCancel"
    >
      <a-descriptions
        v-if="currentAppointment"
        title="预约信息"
        :column="2"
        bordered
        style="margin-bottom: 24px"
      >
        <a-descriptions-item label="访客姓名" :span="1">
          {{ currentAppointment.visitorName }}
        </a-descriptions-item>
        <a-descriptions-item label="联系电话" :span="1">
          {{ currentAppointment.phone }}
        </a-descriptions-item>
        <a-descriptions-item label="身份证号" :span="2">
          {{ currentAppointment.idCard }}
        </a-descriptions-item>
        <a-descriptions-item label="访问日期" :span="1">
          {{ currentAppointment.visitDate }}
        </a-descriptions-item>
        <a-descriptions-item label="访问时段" :span="1">
          {{ currentAppointment.startTime }} - {{ currentAppointment.endTime }}
        </a-descriptions-item>
        <a-descriptions-item label="访问区域" :span="1">
          {{ currentAppointment.areaName }}
        </a-descriptions-item>
        <a-descriptions-item label="陪同人数" :span="1">
          {{ currentAppointment.attendeeCount || 0 }}人
        </a-descriptions-item>
        <a-descriptions-item label="被访人" :span="1">
          {{ currentAppointment.intervieweeName }}
        </a-descriptions-item>
        <a-descriptions-item label="所属部门" :span="1">
          {{ currentAppointment.departmentName }}
        </a-descriptions-item>
        <a-descriptions-item label="访问事由" :span="2">
          {{ currentAppointment.visitReason }}
        </a-descriptions-item>
      </a-descriptions>

      <a-form
        ref="approvalFormRef"
        :model="approvalForm"
        :rules="approvalRules"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 19 }"
      >
        <a-form-item label="审批结果" name="approvalResult">
          <a-radio-group v-model:value="approvalForm.approvalResult">
            <a-radio :value="1">
              <CheckOutlined style="color: #52c41a; margin-right: 4px" />
              批准
            </a-radio>
            <a-radio :value="2">
              <CloseOutlined style="color: #ff4d4f; margin-right: 4px" />
              驳回
            </a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="审批意见" name="approvalOpinion">
          <a-textarea
            v-model:value="approvalForm.approvalOpinion"
            placeholder="请输入审批意见（必填）"
            :rows="4"
            :max="500"
            show-count
          />
        </a-form-item>

        <a-form-item v-if="approvalForm.approvalResult === 1" label="访问权限">
          <a-checkbox-group v-model:value="approvalForm.accessPermissions">
            <a-checkbox value="entrance">门禁通行</a-checkbox>
            <a-checkbox value="elevator">电梯使用</a-checkbox>
            <a-checkbox value="parking">停车场</a-checkbox>
            <a-checkbox value="network">Wi-Fi网络</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item v-if="approvalForm.approvalResult === 1" label="有效期">
          <a-range-picker
            v-model:value="approvalForm.validPeriod"
            show-time
            format="YYYY-MM-DD HH:mm"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item v-if="approvalForm.approvalResult === 1" label="需要陪同">
          <a-switch
            v-model:checked="approvalForm.needEscort"
            checked-children="是"
            un-checked-children="否"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 预约详情模态框 -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="预约详情"
      width="900px"
      :footer="null"
    >
      <a-descriptions
        v-if="currentAppointment"
        title="基本信息"
        :column="2"
        bordered
      >
        <a-descriptions-item label="预约编号">
          {{ currentAppointment.appointmentNo }}
        </a-descriptions-item>
        <a-descriptions-item label="审批状态">
          <a-badge :status="getStatusBadge(currentAppointment.approvalStatus)" />
          <a-tag :color="getStatusColor(currentAppointment.approvalStatus)">
            {{ getStatusLabel(currentAppointment.approvalStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="访客姓名">
          {{ currentAppointment.visitorName }}
        </a-descriptions-item>
        <a-descriptions-item label="联系电话">
          {{ currentAppointment.phone }}
        </a-descriptions-item>
        <a-descriptions-item label="身份证号" :span="2">
          {{ currentAppointment.idCard }}
        </a-descriptions-item>
        <a-descriptions-item label="访问日期">
          {{ currentAppointment.visitDate }}
        </a-descriptions-item>
        <a-descriptions-item label="访问时段">
          {{ currentAppointment.startTime }} - {{ currentAppointment.endTime }}
        </a-descriptions-item>
        <a-descriptions-item label="访问区域">
          {{ currentAppointment.areaName }}
        </a-descriptions-item>
        <a-descriptions-item label="陪同人数">
          {{ currentAppointment.attendeeCount || 0 }}人
        </a-descriptions-item>
        <a-descriptions-item label="被访人">
          {{ currentAppointment.intervieweeName }}
        </a-descriptions-item>
        <a-descriptions-item label="所属部门">
          {{ currentAppointment.departmentName }}
        </a-descriptions-item>
        <a-descriptions-item label="被访人电话">
          {{ currentAppointment.intervieweePhone }}
        </a-descriptions-item>
        <a-descriptions-item label="访问事由" :span="2">
          {{ currentAppointment.visitReason }}
        </a-descriptions-item>
        <a-descriptions-item label="提交时间">
          {{ formatDateTime(currentAppointment.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="提交人">
          {{ currentAppointment.creatorName }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 审批历史 -->
      <a-divider orientation="left">审批历史</a-divider>
      <a-timeline v-if="currentAppointment && currentAppointment.approvalRecords">
        <a-timeline-item
          v-for="(record, index) in currentAppointment.approvalRecords"
          :key="index"
          :color="getTimelineColor(record.approvalStatus)"
        >
          <template #dot>
            <CheckCircleOutlined v-if="record.approvalStatus === 1" />
            <CloseCircleOutlined v-else-if="record.approvalStatus === 2" />
            <ClockCircleOutlined v-else />
          </template>
          <p>
            <strong>{{ record.approvalRole }}</strong>
            <a-tag :color="getApprovalStatusColor(record.approvalStatus)" style="margin-left: 8px">
              {{ getApprovalStatusLabel(record.approvalStatus) }}
            </a-tag>
          </p>
          <p>审批人：{{ record.approverName }}</p>
          <p v-if="record.approvalOpinion">
            审批意见：{{ record.approvalOpinion }}
          </p>
          <p v-if="record.approvalTime">
            审批时间：{{ formatDateTime(record.approvalTime) }}
          </p>
        </a-timeline-item>
      </a-timeline>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import {
  CheckOutlined,
  CloseOutlined,
  ReloadOutlined,
  UserPendingOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  LikeOutlined,
  CalendarOutlined,
  ClockCircleOutlined,
  EnvironmentOutlined,
  UserOutlined,
  PhoneOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';

// 数据状态
const loading = ref(false);
const searchText = ref('');
const statusFilter = ref(0);
const appointmentList = ref([]);
const currentAppointment = ref(null);
const selectedRowKeys = ref([]);
const statistics = ref({
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  approvalRate: 0
});

// 模态框状态
const approvalModalVisible = ref(false);
const detailModalVisible = ref(false);

// 表单引用
const approvalFormRef = ref();

// 审批类型（approve/reject）
const approvalType = ref('approve');

// 审批表单
const approvalForm = reactive({
  approvalResult: 1,
  approvalOpinion: '',
  accessPermissions: ['entrance'],
  validPeriod: [],
  needEscort: false
});

// 表单验证规则
const approvalRules = {
  approvalResult: [
    { required: true, message: '请选择审批结果', trigger: 'change' }
  ],
  approvalOpinion: [
    { required: true, message: '请输入审批意见', trigger: 'blur' },
    { min: 5, message: '审批意见至少5个字', trigger: 'blur' }
  ]
};

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条记录`
});

// 表格列定义
const columns = [
  {
    title: '访客信息',
    key: 'visitorInfo',
    width: 250,
    fixed: 'left'
  },
  {
    title: '访问信息',
    key: 'visitInfo',
    width: 200
  },
  {
    title: '被访人信息',
    key: 'intervieweeInfo',
    width: 200
  },
  {
    title: '访问事由',
    key: 'visitReason',
    width: 200,
    ellipsis: true
  },
  {
    title: '审批状态',
    key: 'approvalStatus',
    width: 120
  },
  {
    title: '提交时间',
    key: 'createTime',
    width: 150
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
];

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  }
}));

// 审批模态框标题
const approvalModalTitle = computed(() => {
  return approvalType.value === 'approve' ? '批准预约' : '驳回预约';
});

// 生命周期
onMounted(() => {
  fetchAppointmentList();
  fetchStatistics();
});

// 获取预约列表
const fetchAppointmentList = async () => {
  loading.value = true;
  try {
    // TODO: 调用实际API
    // const response = await visitorApi.getAppointmentList({
    //   pageNum: pagination.current,
    //   pageSize: pagination.pageSize,
    //   searchText: searchText.value,
    //   status: statusFilter.value
    // });

    // 模拟数据
    const mockData = {
      records: [
        {
          appointmentId: '1',
          appointmentNo: 'VF202512260001',
          visitorName: '张三',
          phone: '13800138000',
          idCard: '310101199001011234',
          visitDate: '2025-12-28',
          startTime: '09:00',
          endTime: '18:00',
          areaName: 'A栋办公楼',
          attendeeCount: 2,
          intervieweeName: '李经理',
          intervieweePhone: '13900139000',
          departmentName: '销售部',
          visitReason: '商务洽谈合作事宜',
          approvalStatus: 0,
          creatorName: '李经理',
          createTime: '2025-12-26 10:00:00',
          approvalRecords: []
        },
        {
          appointmentId: '2',
          appointmentNo: 'VF202512260002',
          visitorName: '王五',
          phone: '13800138001',
          idCard: '310101199001011235',
          visitDate: '2025-12-29',
          startTime: '14:00',
          endTime: '16:00',
          areaName: 'B栋会议室',
          attendeeCount: 0,
          intervieweeName: '赵主管',
          intervieweePhone: '13900139001',
          departmentName: '技术部',
          visitReason: '技术方案交流',
          approvalStatus: 1,
          creatorName: '赵主管',
          createTime: '2025-12-26 11:00:00',
          approvalRecords: [
            {
              approvalRole: '部门主管',
              approverName: '系统管理员',
              approvalStatus: 1,
              approvalOpinion: '同意访问',
              approvalTime: '2025-12-26 14:00:00'
            }
          ]
        }
      ],
      total: 2
    };

    appointmentList.value = mockData.records;
    pagination.total = mockData.total;
  } catch (error) {
    message.error('获取预约列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 获取统计信息
const fetchStatistics = async () => {
  try {
    // TODO: 调用实际API
    // const response = await visitorApi.getApprovalStatistics();

    // 模拟数据
    statistics.value = {
      pendingCount: 15,
      approvedCount: 82,
      rejectedCount: 8,
      approvalRate: 91.1
    };
  } catch (error) {
    message.error('获取统计信息失败：' + error.message);
  }
};

// 批准预约
const approve = (record) => {
  currentAppointment.value = record;
  approvalType.value = 'approve';
  approvalForm.approvalResult = 1;
  approvalForm.approvalOpinion = '';
  approvalModalVisible.value = true;
};

// 驳回预约
const reject = (record) => {
  currentAppointment.value = record;
  approvalType.value = 'reject';
  approvalForm.approvalResult = 2;
  approvalForm.approvalOpinion = '';
  approvalModalVisible.value = true;
};

// 批量批准
const batchApprove = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要批准的预约');
    return;
  }
  message.info('批量批准功能开发中...');
};

// 批量驳回
const batchReject = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要驳回的预约');
    return;
  }
  message.info('批量驳回功能开发中...');
};

// 处理审批提交
const handleApprovalSubmit = async () => {
  try {
    await approvalFormRef.value.validate();

    // TODO: 调用实际API
    // await visitorApi.approveAppointment({
    //   appointmentId: currentAppointment.value.appointmentId,
    //   ...approvalForm
    // });

    const resultText = approvalForm.approvalResult === 1 ? '批准' : '驳回';
    message.success(`预约${resultText}成功`);
    approvalModalVisible.value = false;
    resetApprovalForm();
    fetchAppointmentList();
    fetchStatistics();
  } catch (error) {
    if (error.errorFields) {
      message.error('请检查表单填写是否正确');
    } else {
      message.error('提交失败：' + error.message);
    }
  }
};

// 重置审批表单
const resetApprovalForm = () => {
  approvalFormRef.value?.resetFields();
  Object.assign(approvalForm, {
    approvalResult: 1,
    approvalOpinion: '',
    accessPermissions: ['entrance'],
    validPeriod: [],
    needEscort: false
  });
};

// 取消审批
const handleApprovalCancel = () => {
  approvalModalVisible.value = false;
  resetApprovalForm();
};

// 查看详情
const viewDetail = (record) => {
  currentAppointment.value = record;
  detailModalVisible.value = true;
};

// 状态筛选变更
const handleStatusFilterChange = (value) => {
  pagination.current = 1;
  fetchAppointmentList();
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  fetchAppointmentList();
};

// 刷新列表
const refreshList = () => {
  fetchAppointmentList();
  fetchStatistics();
};

// 表格变化
const handleTableChange = (pag, filters, sorter) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchAppointmentList();
};

// 工具方法
const getStatusBadge = (status) => {
  const badgeMap = {
    0: 'processing',
    1: 'success',
    2: 'error'
  };
  return badgeMap[status] || 'default';
};

const getStatusColor = (status) => {
  const colorMap = {
    0: 'processing', // 待审批
    1: 'success',    // 已批准
    2: 'error'       // 已驳回
  };
  return colorMap[status] || 'default';
};

const getStatusLabel = (status) => {
  const labelMap = {
    0: '待审批',
    1: '已批准',
    2: '已驳回'
  };
  return labelMap[status] || '未知';
};

const getTimelineColor = (status) => {
  const colorMap = {
    0: 'blue',    // 待审批
    1: 'green',   // 已批准
    2: 'red'      // 已驳回
  };
  return colorMap[status] || 'gray';
};

const getApprovalStatusColor = (status) => {
  return getTimelineColor(status);
};

const getApprovalStatusLabel = (status) => {
  const labelMap = {
    0: '待审批',
    1: '已通过',
    2: '已驳回'
  };
  return labelMap[status] || '未知';
};

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-';
  return dateTimeStr;
};
</script>

<style scoped lang="less">
.appointment-approval {
  padding: 16px;

  :deep(.ant-card) {
    .ant-card-head {
      border-bottom: 1px solid #f0f0f0;
    }
  }

  :deep(.ant-descriptions) {
    .ant-descriptions-item-label {
      font-weight: 500;
    }
  }

  .ellipsis-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 200px;
  }
}
</style>
