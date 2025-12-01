<template>
  <div class="temporary-permission-application">
    <!-- 申请表单 -->
    <a-card title="临时权限申请">
      <template #extra>
        <a-space>
          <a-button @click="resetForm">
            <template #icon><ReloadOutlined /></template>
            重置
          </a-button>
          <a-button type="primary" @click="handleSubmit" :loading="loading">
            <template #icon><SendOutlined /></template>
            提交申请
          </a-button>
        </a-space>
      </template>

      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
      >
        <!-- 申请人信息 -->
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="申请人">
              <a-input :value="props.user.userName" disabled />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="当前部门">
              <a-input :value="props.user.departmentName" disabled />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="当前安全级别">
              <a-tag :color="getSecurityLevelColor(props.user.securityLevelValue)">
                {{ props.user.securityLevelName }}
              </a-tag>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="申请类型">
              <a-select
                v-model:value="formData.applicationType"
                placeholder="请选择申请类型"
                @change="handleApplicationTypeChange"
              >
                <a-select-option value="TEMP_ACCESS">临时访问权限</a-select-option>
                <a-select-option value="TEMP_CONTROL">临时控制权限</a-select-option>
                <a-select-option value="TEMP_MANAGE">临时管理权限</a-select-option>
                <a-select-option value="EMERGENCY_ACCESS">紧急访问权限</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 权限详情 -->
        <a-divider>权限详情</a-divider>

        <a-form-item
          label="权限资源类型"
          name="resourceType"
          :rules="[{ required: true, message: '请选择权限资源类型' }]"
        >
          <a-select
            v-model:value="formData.resourceType"
            placeholder="请选择权限资源类型"
            @change="handleResourceTypeChange"
          >
            <a-select-option value="area">区域权限</a-select-option>
            <a-select-option value="device">设备权限</a-select-option>
            <a-select-option value="attendance">考勤权限</a-select-option>
            <a-select-option value="access">门禁权限</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item
          label="具体资源"
          name="resourceId"
          :rules="[{ required: true, message: '请选择具体资源' }]"
        >
          <a-select
            v-model:value="formData.resourceId"
            placeholder="请选择具体资源"
            show-search
            :filter-option="filterResourceOption"
          >
            <a-select-option
              v-for="resource in resourceList"
              :key="resource.id"
              :value="resource.id"
            >
              {{ resource.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item
          label="权限级别"
          name="permissionLevel"
          :rules="[{ required: true, message: '请选择权限级别' }]"
        >
          <a-select
            v-model:value="formData.permissionLevel"
            placeholder="请选择权限级别"
          >
            <a-select-option value="1">1级 - 基础权限</a-select-option>
            <a-select-option value="2">2级 - 普通权限</a-select-option>
            <a-select-option value="3">3级 - 高级权限</a-select-option>
            <a-select-option value="4">4级 - 管理权限</a-select-option>
          </a-select>
        </a-form-item>

        <!-- 时间范围 -->
        <a-divider>时间范围</a-divider>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="开始时间"
              name="startTime"
              :rules="[{ required: true, message: '请选择开始时间' }]"
            >
              <a-date-picker
                v-model:value="formData.startTime"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择开始时间"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item
              label="结束时间"
              name="endTime"
              :rules="[{ required: true, message: '请选择结束时间' }]"
            >
              <a-date-picker
                v-model:value="formData.endTime"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择结束时间"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="权限时长">
          <a-statistic
            :title="申请时长"
            :value="durationDays"
            :precision="1"
            suffix="天"
            :value-style="{ color: durationDays > 7 ? '#f5222d' : '#3f8600' }"
          />
        </a-form-item>

        <!-- 申请原因 -->
        <a-form-item
          label="申请原因"
          name="applyReason"
          :rules="[{ required: true, message: '请输入申请原因' }]"
        >
          <a-textarea
            v-model:value="formData.applyReason"
            placeholder="请详细说明申请临时权限的原因和用途"
            :rows="4"
            :maxlength="1000"
            show-count
          />
        </a-form-item>

        <!-- 业务上下文 -->
        <a-form-item label="业务上下文" name="businessContext">
          <a-textarea
            v-model:value="formData.businessContext"
            placeholder="请描述具体的业务场景和操作内容（可选）"
            :rows="3"
            :maxlength="500"
            show-count
          />
        </a-form-item>

        <!-- 紧急程度 -->
        <a-form-item
          label="紧急程度"
          name="urgencyLevel"
          :rules="[{ required: true, message: '请选择紧急程度' }]"
        >
          <a-radio-group v-model:value="formData.urgencyLevel">
            <a-radio value="low">普通</a-radio>
            <a-radio value="medium">较急</a-radio>
            <a-radio value="high">紧急</a-radio>
            <a-radio value="emergency">紧急</a-radio>
          </a-radio-group>
        </a-form-item>

        <!-- 审批人 -->
        <a-form-item
          label="建议审批人"
          name="approverId"
        >
          <a-select
            v-model:value="formData.approverId"
            placeholder="请选择审批人（可选，系统将自动分配）"
            show-search
            :filter-option="filterApproverOption"
            allow-clear
          >
            <a-select-option
              v-for="approver in approverList"
              :key="approver.userId"
              :value="approver.userId"
            >
              {{ approver.userName }} ({{ approver.departmentName }})
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 申请历史 -->
    <a-card title="我的申请历史" style="margin-top: 16px;">
      <a-table
        :columns="historyColumns"
        :data-source="applicationHistory"
        :loading="historyLoading"
        :pagination="historyPagination"
        row-key="id"
        @change="handleHistoryTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 申请状态 -->
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.approvalStatus)">
              {{ getStatusText(record.approvalStatus) }}
            </a-tag>
          </template>

          <!-- 申请详情 -->
          <template v-else-if="column.key === 'details'">
            <div class="application-details">
              <div class="application-type">{{ record.applicationType }}</div>
              <div class="resource-info">
                {{ record.resourceType }} - {{ record.resourceName }}
              </div>
              <div class="time-range">
                {{ formatDate(record.startTime) }} 至 {{ formatDate(record.endTime) }}
              </div>
            </div>
          </template>

          <!-- 审批信息 -->
          <template v-else-if="column.key === 'approvalInfo'">
            <div class="approval-info" v-if="record.approvalStatus !== 'PENDING'">
              <div class="approver">{{ record.approverName }}</div>
              <div class="approval-time">{{ formatDate(record.approvalTime) }}</div>
              <div class="approval-comment" v-if="record.approvalComment">
                {{ record.approvalComment }}
              </div>
            </div>
            <span v-else class="pending-text">等待审批</span>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="viewApplicationDetail(record)"
              >
                详情
              </a-button>
              <a-button
                type="link"
                size="small"
                danger
                @click="cancelApplication(record)"
                v-if="record.approvalStatus === 'PENDING'"
              >
                撤销
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  ReloadOutlined,
  SendOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'

// Props 定义
const props = defineProps({
  userId: {
    type: Number,
    required: true
  },
  user: {
    type: Object,
    required: true
  }
})

// 事件定义
const emit = defineEmits(['success'])

// 响应式数据
const formRef = ref()
const loading = ref(false)
const historyLoading = ref(false)
const resourceList = ref([])
const approverList = ref([])
const applicationHistory = ref([])

// 表单数据
const formData = reactive({
  applicationType: '',
  resourceType: '',
  resourceId: null,
  permissionLevel: '',
  startTime: null,
  endTime: null,
  applyReason: '',
  businessContext: '',
  urgencyLevel: 'medium',
  approverId: null
})

// 历史分页
const historyPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
})

// 表单验证规则
const rules = {
  applicationType: [
    { required: true, message: '请选择申请类型', trigger: 'change' }
  ],
  resourceType: [
    { required: true, message: '请选择权限资源类型', trigger: 'change' }
  ],
  resourceId: [
    { required: true, message: '请选择具体资源', trigger: 'change' }
  ],
  permissionLevel: [
    { required: true, message: '请选择权限级别', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  applyReason: [
    { required: true, message: '请输入申请原因', trigger: 'blur' },
    { min: 10, max: 1000, message: '申请原因长度应在10-1000字符之间', trigger: 'blur' }
  ],
  urgencyLevel: [
    { required: true, message: '请选择紧急程度', trigger: 'change' }
  ]
}

// 历史表格列
const historyColumns = [
  {
    title: '申请状态',
    key: 'status',
    width: 100,
    align: 'center'
  },
  {
    title: '申请详情',
    key: 'details',
    width: 300
  },
  {
    title: '审批信息',
    key: 'approvalInfo',
    width: 200
  },
  {
    title: '申请时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 150
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    align: 'center'
  }
]

// 计算属性
const durationDays = computed(() => {
  if (formData.startTime && formData.endTime) {
    const duration = formData.endTime.diff(formData.startTime, 'hour')
    return (duration / 24).toFixed(1)
  }
  return 0
})

// 方法定义
const handleApplicationTypeChange = (value) => {
  // 根据申请类型设置默认值
  switch (value) {
    case 'EMERGENCY_ACCESS':
      formData.urgencyLevel = 'emergency'
      break
    case 'TEMP_ACCESS':
      formData.permissionLevel = '1'
      break
    case 'TEMP_CONTROL':
      formData.permissionLevel = '2'
      break
    case 'TEMP_MANAGE':
      formData.permissionLevel = '3'
      break
  }
}

const handleResourceTypeChange = (value) => {
  // 重置资源选择
  formData.resourceId = null
  loadResourceList(value)
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    // 验证时间范围
    if (formData.startTime && formData.endTime && formData.endTime.isBefore(formData.startTime)) {
      message.error('结束时间必须晚于开始时间')
      return
    }

    // 验证权限时长
    if (durationDays.value > 30) {
      message.warning('临时权限申请时长超过30天，请联系管理员')
      return
    }

    // 构建申请数据
    const requestData = {
      userId: props.userId,
      applicationType: formData.applicationType,
      resourceType: formData.resourceType,
      resourceId: formData.resourceId,
      permissionLevel: formData.permissionLevel,
      startTime: formData.startTime.toISOString(),
      endTime: formData.endTime.toISOString(),
      applyReason: formData.applyReason,
      businessContext: formData.businessContext,
      urgencyLevel: formData.urgencyLevel,
      approverId: formData.approverId
    }

    // 调用API提交申请
    const response = await submitApplication(requestData)

    if (response.data) {
      message.success('临时权限申请提交成功')
      emit('success')
      resetForm()
      fetchApplicationHistory()
    }
  } catch (error) {
    message.error('申请提交失败: ' + (error.message || '未知错误'))
    console.error('申请提交失败:', error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    applicationType: '',
    resourceType: '',
    resourceId: null,
    permissionLevel: '',
    startTime: null,
    endTime: null,
    applyReason: '',
    businessContext: '',
    urgencyLevel: 'medium',
    approverId: null
  })
}

const filterResourceOption = (input, option) => {
  const resource = resourceList.value.find(r => r.id === option.value)
  if (!resource) return false
  return resource.name.toLowerCase().includes(input.toLowerCase())
}

const filterApproverOption = (input, option) => {
  const approver = approverList.value.find(a => a.userId === option.value)
  if (!approver) return false
  return approver.userName.toLowerCase().includes(input.toLowerCase()) ||
         approver.departmentName.toLowerCase().includes(input.toLowerCase())
}

const loadResourceList = async (resourceType) => {
  try {
    // 模拟加载资源列表
    // 实际应该调用API获取
    const mockResources = {
      area: [
        { id: 1, name: '生产车间A' },
        { id: 2, name: '办公区域B' },
        { id: 3, name: '仓库区域C' }
      ],
      device: [
        { id: 1, name: '生产线设备A' },
        { id: 2, name: '监控系统设备B' }
      ],
      attendance: [
        { id: 1, name: '考勤管理权限' },
        { id: 2, name: '考勤数据导出' }
      ],
      access: [
        { id: 1, name: '正门出入口' },
        { id: 2, name: '侧门出入口' }
      ]
    }

    resourceList.value = mockResources[resourceType] || []
  } catch (error) {
    message.error('加载资源列表失败')
    console.error('加载资源列表失败:', error)
  }
}

const fetchApplicationHistory = async () => {
  try {
    historyLoading.value = true
    const params = {
      page: historyPagination.current,
      pageSize: historyPagination.pageSize,
      userId: props.userId
    }

    // 模拟获取申请历史
    // 实际应该调用API获取
    const mockHistory = {
      records: [
        {
          id: 1,
          applicationType: 'TEMP_ACCESS',
          resourceType: 'area',
          resourceName: '生产车间A',
          approvalStatus: 'APPROVED',
          approverName: '张三',
          approvalTime: '2025-11-14 10:30:00',
          approvalComment: '同意临时访问',
          createTime: '2025-11-14 09:15:00',
          startTime: '2025-11-14 09:00:00',
          endTime: '2025-11-14 18:00:00'
        }
      ],
      total: 1
    }

    applicationHistory.value = mockHistory.records || []
    historyPagination.total = mockHistory.total || 0
  } catch (error) {
    message.error('获取申请历史失败')
    console.error('获取申请历史失败:', error)
  } finally {
    historyLoading.value = false
  }
}

const handleHistoryTableChange = (paginationInfo) => {
  historyPagination.current = paginationInfo.current
  historyPagination.pageSize = paginationInfo.pageSize
  fetchApplicationHistory()
}

const viewApplicationDetail = (record) => {
  // 查看申请详情
  console.log('查看申请详情:', record)
}

const cancelApplication = async (record) => {
  try {
    // 调用API撤销申请
    await cancelApplicationRequest(record.id)
    message.success('申请已撤销')
    fetchApplicationHistory()
  } catch (error) {
    message.error('撤销申请失败')
    console.error('撤销申请失败:', error)
  }
}

const submitApplication = async (requestData) => {
  // 模拟API调用
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({ data: { id: Date.now() } })
    }, 1000)
  })
}

const cancelApplicationRequest = async (id) => {
  // 模拟API调用
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({ data: { success: true } })
    }, 500)
  })
}

const getSecurityLevelColor = (levelValue) => {
  const colors = {
    1: 'blue',    // 公开级
    2: 'green',   // 内部级
    3: 'orange',  // 秘密级
    4: 'red',     // 机密级
    5: 'purple'   // 绝密级
  }
  return colors[levelValue] || 'default'
}

const getStatusColor = (status) => {
  const colorMap = {
    'PENDING': 'orange',
    'APPROVED': 'green',
    'REJECTED': 'red',
    'EXPIRED': 'gray'
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'EXPIRED': '已过期'
  }
  return textMap[status] || status
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return dayjs(dateString).format('YYYY-MM-DD HH:mm')
}

// 生命周期
onMounted(async () => {
  await fetchApplicationHistory()
})
</script>

<style lang="less" scoped>
.temporary-permission-application {
  .application-details {
    .application-type {
      font-weight: 500;
      color: #262626;
      margin-bottom: 4px;
    }

    .resource-info {
      font-size: 14px;
      color: #1890ff;
      margin-bottom: 4px;
    }

    .time-range {
      font-size: 12px;
      color: #8c8c8c;
    }
  }

  .approval-info {
    .approver {
      font-weight: 500;
      color: #262626;
      margin-bottom: 4px;
    }

    .approval-time {
      font-size: 12px;
      color: #8c8c8c;
      margin-bottom: 4px;
    }

    .approval-comment {
      font-size: 12px;
      color: #52c41a;
      font-style: italic;
    }
  }

  .pending-text {
    color: #faad14;
    font-style: italic;
  }
}

:deep(.ant-divider) {
  margin: 20px 0;
}

:deep(.ant-statistic) {
  .ant-statistic-content {
    .ant-statistic-title {
      font-size: 14px;
      color: #262626;
    }
  }
}
</style>