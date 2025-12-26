<template>
  <div class="subsidy-rule-config">
    <!-- 顶部操作栏 -->
    <div class="config-header">
      <a-space>
        <a-button type="primary" @click="handleAddRule">
          <template #icon><PlusOutlined /></template>
          新增规则
        </a-button>
        <a-button @click="refreshRules">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </a-space>
    </div>

    <!-- 规则列表 -->
    <a-card :bordered="false" class="rule-list-card">
      <a-table
        :columns="columns"
        :data-source="rules"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <!-- 规则类型 -->
        <template #ruleType="{ record }">
          <a-tag :color="getRuleTypeColor(record.ruleType)">
            {{ getRuleTypeName(record.ruleType) }}
          </a-tag>
        </template>

        <!-- 补贴类型 -->
        <template #subsidyType="{ record }">
          <a-tag :color="getSubsidyTypeColor(record.subsidyType)">
            {{ getSubsidyTypeName(record.subsidyType) }}
          </a-tag>
        </template>

        <!-- 优先级 -->
        <template #priority="{ record }">
          <a-progress
            :percent="Math.min(record.priority, 100)"
            :show-info="false"
            :stroke-color="getPriorityColor(record.priority)"
          />
          <span style="margin-left: 8px">{{ record.priority }}</span>
        </template>

        <!-- 状态 -->
        <template #status="{ record }">
          <a-switch
            :checked="record.status === 1"
            @change="(checked) => handleStatusChange(record, checked)"
          />
        </template>

        <!-- 操作 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">
              编辑
            </a-button>
            <a-button type="link" size="small" @click="handleAdjustPriority(record)">
              优先级
            </a-button>
            <a-popconfirm
              title="确定删除此规则吗？"
              @confirm="handleDelete(record)"
            >
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 规则编辑弹窗 -->
    <a-modal
      v-model:open="showEditModal"
      :title="editModalTitle"
      width="800px"
      @ok="handleSaveRule"
      @cancel="handleCancelEdit"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="规则编码" name="ruleCode">
          <a-input
            v-model:value="formState.ruleCode"
            placeholder="请输入规则编码"
            :disabled="isEdit"
          />
        </a-form-item>

        <a-form-item label="规则名称" name="ruleName">
          <a-input v-model:value="formState.ruleName" placeholder="请输入规则名称" />
        </a-form-item>

        <a-form-item label="规则类型" name="ruleType">
          <a-select v-model:value="formState.ruleType" placeholder="请选择规则类型">
            <a-select-option :value="1">固定金额</a-select-option>
            <a-select-option :value="2">比例补贴</a-select-option>
            <a-select-option :value="3">阶梯补贴</a-select-option>
            <a-select-option :value="4">限时补贴</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="补贴类型" name="subsidyType">
          <a-select v-model:value="formState.subsidyType" placeholder="请选择补贴类型">
            <a-select-option :value="1">餐补</a-select-option>
            <a-select-option :value="2">交通补</a-select-option>
            <a-select-option :value="3">其他</a-select-option>
          </a-select>
        </a-form-item>

        <!-- 固定金额模式 -->
        <template v-if="formState.ruleType === 1 || formState.ruleType === 4">
          <a-form-item label="补贴金额" name="subsidyAmount">
            <a-input-number
              v-model:value="formState.subsidyAmount"
              :min="0"
              :precision="2"
              style="width: 100%"
            />
          </a-form-item>
        </template>

        <!-- 比例补贴模式 -->
        <template v-if="formState.ruleType === 2">
          <a-form-item label="补贴比例" name="subsidyRate">
            <a-input-number
              v-model:value="formState.subsidyRate"
              :min="0"
              :max="1"
              :step="0.01"
              :precision="4"
              style="width: 100%"
            />
            <span style="margin-left: 8px">0-1之间</span>
          </a-form-item>

          <a-form-item label="最高限额" name="maxSubsidyAmount">
            <a-input-number
              v-model:value="formState.maxSubsidyAmount"
              :min="0"
              :precision="2"
              style="width: 100%"
            />
          </a-form-item>
        </template>

        <!-- 适用时间 -->
        <a-form-item label="适用时间" name="applyTimeType">
          <a-select v-model:value="formState.applyTimeType" placeholder="请选择适用时间">
            <a-select-option :value="1">全部</a-select-option>
            <a-select-option :value="2">工作日</a-select-option>
            <a-select-option :value="3">周末</a-select-option>
            <a-select-option :value="4">自定义</a-select-option>
          </a-select>
        </a-form-item>

        <template v-if="formState.applyTimeType === 4">
          <a-form-item label="适用日期" name="applyDays">
            <a-checkbox-group v-model:value="applyDaysChecked">
              <a-checkbox :value="1">周一</a-checkbox>
              <a-checkbox :value="2">周二</a-checkbox>
              <a-checkbox :value="3">周三</a-checkbox>
              <a-checkbox :value="4">周四</a-checkbox>
              <a-checkbox :value="5">周五</a-checkbox>
              <a-checkbox :value="6">周六</a-checkbox>
              <a-checkbox :value="7">周日</a-checkbox>
            </a-checkbox-group>
          </a-form-item>

          <a-form-item label="开始时间" name="applyStartTime">
            <a-time-picker
              v-model:value="formState.applyStartTime"
              format="HH:mm"
              style="width: 100%"
            />
          </a-form-item>

          <a-form-item label="结束时间" name="applyEndTime">
            <a-time-picker
              v-model:value="formState.applyEndTime"
              format="HH:mm"
              style="width: 100%"
            />
          </a-form-item>
        </template>

        <!-- 适用餐别 -->
        <a-form-item label="适用餐别" name="applyMealTypes">
          <a-checkbox-group v-model:value="applyMealTypesChecked">
            <a-checkbox :value="1">早餐</a-checkbox>
            <a-checkbox :value="2">午餐</a-checkbox>
            <a-checkbox :value="3">晚餐</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <!-- 优先级 -->
        <a-form-item label="优先级" name="priority">
          <a-slider
            v-model:value="formState.priority"
            :min="0"
            :max="100"
            :marks="{ 0: '低', 50: '中', 100: '高' }"
          />
        </a-form-item>

        <!-- 生效时间 -->
        <a-form-item label="生效时间" name="effectiveDate">
          <a-date-picker
            v-model:value="formState.effectiveDate"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </a-form-item>

        <!-- 失效时间 -->
        <a-form-item label="失效时间" name="expireDate">
          <a-date-picker
            v-model:value="formState.expireDate"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </a-form-item>

        <!-- 描述 -->
        <a-form-item label="规则描述" name="description">
          <a-textarea
            v-model:value="formState.description"
            :rows="4"
            placeholder="请输入规则描述"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 优先级调整弹窗 -->
    <a-modal
      v-model:open="showPriorityModal"
      title="调整优先级"
      @ok="handleSavePriority"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="当前优先级">
          <a-input-number v-model:value="currentRule.priority" disabled />
        </a-form-item>

        <a-form-item label="新优先级">
          <a-slider
            v-model:value="newPriority"
            :min="0"
            :max="100"
            :marks="{ 0: '低', 50: '中', 100: '高' }"
          />
          <div style="margin-top: 16px; text-align: center">
            <a-tag color="blue">{{ newPriority }}</a-tag>
          </div>
        </a-form-item>

        <a-alert
          message="优先级说明"
          description="数字越大，优先级越高。系统会优先应用高优先级的规则。"
          type="info"
          show-icon
        />
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { request } from '@/api/request'
import dayjs from 'dayjs'

/**
 * 补贴规则配置组件
 */

// Refs
const loading = ref(false)
const rules = ref([])
const showEditModal = ref(false)
const showPriorityModal = ref(false)
const isEdit = ref(false)
const currentRule = ref(null)

const formRef = ref()
const applyDaysChecked = ref([])
const applyMealTypesChecked = ref([])
const newPriority = ref(50)

// 表单数据
const formState = reactive({
  id: undefined,
  ruleCode: '',
  ruleName: '',
  ruleType: 1,
  subsidyType: 1,
  subsidyAmount: undefined,
  subsidyRate: undefined,
  maxSubsidyAmount: undefined,
  applyTimeType: 1,
  applyStartTime: undefined,
  applyEndTime: undefined,
  applyMealTypes: undefined,
  priority: 50,
  effectiveDate: undefined,
  expireDate: undefined,
  description: ''
})

// 表单验证规则
const rules = {
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  subsidyType: [{ required: true, message: '请选择补贴类型', trigger: 'change' }],
  priority: [{ required: true, message: '请设置优先级', trigger: 'change' }],
  effectiveDate: [{ required: true, message: '请选择生效时间', trigger: 'change' }]
}

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
})

// 表格列定义
const columns = [
  {
    title: '规则编码',
    dataIndex: 'ruleCode',
    key: 'ruleCode',
    width: 150
  },
  {
    title: '规则名称',
    dataIndex: 'ruleName',
    key: 'ruleName',
    width: 200
  },
  {
    title: '规则类型',
    dataIndex: 'ruleType',
    key: 'ruleType',
    width: 120,
    slots: { customRender: ({ record }) => record.ruleType }
  },
  {
    title: '补贴类型',
    dataIndex: 'subsidyType',
    key: 'subsidyType',
    width: 100,
    slots: { customRender: ({ record }) => record.subsidyType }
  },
  {
    title: '补贴金额/比例',
    key: 'subsidy',
    width: 150,
    customRender: ({ record }) => {
      if (record.ruleType === 1 || record.ruleType === 4) {
        return `¥${record.subsidyAmount || 0}`
      } else if (record.ruleType === 2) {
        return `${(record.subsidyRate * 100).toFixed(0)}%`
      }
      return '-'
    }
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
    width: 150,
    slots: { customRender: ({ record }) => record.priority }
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80,
    slots: { customRender: ({ record }) => record.status }
  },
  {
    title: '生效时间',
    dataIndex: 'effectiveDate',
    key: 'effectiveDate',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
    slots: { customRender: ({ record }) => record }
  }
]

// 计算属性
const editModalTitle = computed(() => isEdit.value ? '编辑规则' : '新增规则')

// 方法
const loadRules = async () => {
  try {
    loading.value = true
    const response = await request({
      url: '/api/consume/subsidy/rules',
      method: 'get'
    })

    if (response.code === 200) {
      rules.value = response.data || []
      pagination.total = response.data?.length || 0
    }
  } catch (error) {
    console.error('[补贴规则配置] 加载失败:', error)
    message.error('加载规则失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const refreshRules = () => {
  loadRules()
  message.success('刷新成功')
}

const handleAddRule = () => {
  isEdit.value = false
  Object.assign(formState, {
    id: undefined,
    ruleCode: '',
    ruleName: '',
    ruleType: 1,
    subsidyType: 1,
    subsidyAmount: undefined,
    subsidyRate: undefined,
    maxSubsidyAmount: undefined,
    applyTimeType: 1,
    applyStartTime: undefined,
    applyEndTime: undefined,
    applyMealTypes: undefined,
    priority: 50,
    effectiveDate: undefined,
    expireDate: undefined,
    description: ''
  })
  applyDaysChecked.value = []
  applyMealTypesChecked.value = []
  showEditModal.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  currentRule.value = record
  Object.assign(formState, record)

  // 处理复选框数据
  if (record.applyDays) {
    applyDaysChecked.value = record.applyDays.split(',').map(Number)
  }
  if (record.applyMealTypes) {
    applyMealTypesChecked.value = record.applyMealTypes.split(',').map(Number)
  }

  showEditModal.value = true
}

const handleSaveRule = async () => {
  try {
    await formRef.value.validate()

    // 组装数据
    const data = { ...formState }

    // 处理复选框
    if (applyDaysChecked.value.length > 0) {
      data.applyDays = applyDaysChecked.value.join(',')
    }
    if (applyMealTypesChecked.value.length > 0) {
      data.applyMealTypes = applyMealTypesChecked.value.join(',')
    }

    const response = isEdit.value
        ? await request({
            url: `/api/consume/subsidy/rules/${formState.id}`,
            method: 'put',
            data
          })
        : await request({
            url: '/api/consume/subsidy/rules',
            method: 'post',
            data
          })

    if (response.code === 200) {
      message.success(isEdit.value ? '更新成功' : '新增成功')
      showEditModal.value = false
      loadRules()
    }
  } catch (error) {
    console.error('[补贴规则配置] 保存失败:', error)
    message.error('保存失败: ' + error.message)
  }
}

const handleCancelEdit = () => {
  showEditModal.value = false
  formRef.value?.resetFields()
}

const handleStatusChange = async (record, checked) => {
  try {
    const url = checked
        ? `/api/consume/subsidy/rules/${record.id}/enable`
        : `/api/consume/subsidy/rules/${record.id}/disable`

    const response = await request({
      url,
      method: 'put'
    })

    if (response.code === 200) {
      message.success(checked ? '启用成功' : '禁用成功')
      loadRules()
    }
  } catch (error) {
    console.error('[补贴规则配置] 状态更新失败:', error)
    message.error('状态更新失败')
    loadRules() // 刷新恢复原状态
  }
}

const handleAdjustPriority = (record) => {
  currentRule.value = record
  newPriority.value = record.priority
  showPriorityModal.value = true
}

const handleSavePriority = async () => {
  try {
    const response = await request({
      url: `/api/consume/subsidy/rules/${currentRule.value.id}/priority`,
      method: 'put',
      params: { priority: newPriority.value }
    })

    if (response.code === 200) {
      message.success('优先级调整成功')
      showPriorityModal.value = false
      loadRules()
    }
  } catch (error) {
    console.error('[补贴规则配置] 优先级调整失败:', error)
    message.error('优先级调整失败')
  }
}

const handleDelete = async (record) => {
  try {
    const response = await request({
      url: `/api/consume/subsidy/rules/${record.id}`,
      method: 'delete'
    })

    if (response.code === 200) {
      message.success('删除成功')
      loadRules()
    }
  } catch (error) {
    console.error('[补贴规则配置] 删除失败:', error)
    message.error('删除失败')
  }
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadRules()
}

// 工具方法
const getRuleTypeColor = (type) => {
  const colors = {
    1: 'blue',
    2: 'green',
    3: 'orange',
    4: 'purple'
  }
  return colors[type] || 'default'
}

const getRuleTypeName = (type) => {
  const names = {
    1: '固定金额',
    2: '比例补贴',
    3: '阶梯补贴',
    4: '限时补贴'
  }
  return names[type] || '未知'
}

const getSubsidyTypeColor = (type) => {
  const colors = {
    1: 'cyan',
    2: 'magenta',
    3: 'gold'
  }
  return colors[type] || 'default'
}

const getSubsidyTypeName = (type) => {
  const names = {
    1: '餐补',
    2: '交通补',
    3: '其他'
  }
  return names[type] || '未知'
}

const getPriorityColor = (priority) => {
  if (priority >= 80) return '#ff4d4f'
  if (priority >= 50) return '#faad14'
  return '#52c41a'
}

// 生命周期
onMounted(() => {
  loadRules()
})
</script>

<style scoped lang="less">
.subsidy-rule-config {
  .config-header {
    margin-bottom: 16px;
  }

  .rule-list-card {
    :deep(.ant-table) {
      font-size: 13px;
    }
  }
}
</style>
