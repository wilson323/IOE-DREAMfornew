<template>
  <a-modal
    v-model:open="visible"
    title="导出消费记录"
    width="600px"
    @ok="handleExport"
    @cancel="handleCancel"
    :confirm-loading="exporting"
  >
    <a-form
      ref="formRef"
      :model="exportForm"
      :rules="formRules"
      layout="vertical"
    >
      <!-- 导出格式 -->
      <a-form-item label="导出格式" name="exportFormat">
        <a-radio-group v-model:value="exportForm.exportFormat">
          <a-radio value="excel">Excel 格式</a-radio>
          <a-radio value="csv">CSV 格式</a-radio>
          <a-radio value="pdf">PDF 格式</a-radio>
        </a-radio-group>
      </a-form-item>

      <!-- 导出范围 -->
      <a-form-item label="导出范围" name="exportRange">
        <a-radio-group v-model:value="exportForm.exportRange" @change="handleExportRangeChange">
          <a-radio value="current_page">当前页数据</a-radio>
          <a-radio value="all_pages">全部数据</a-radio>
          <a-radio value="custom">自定义范围</a-radio>
        </a-radio-group>
      </a-form-item>

      <!-- 自定义范围 -->
      <a-form-item
        v-if="exportForm.exportRange === 'custom'"
        label="数据范围"
        name="customRange"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-input-number
              v-model:value="exportForm.customStart"
              :min="1"
              placeholder="起始页"
              style="width: 100%"
            />
          </a-col>
          <a-col :span="12">
            <a-input-number
              v-model:value="exportForm.customEnd"
              :min="1"
              placeholder="结束页"
              style="width: 100%"
            />
          </a-col>
        </a-row>
        <div class="range-hint">
          共 {{ totalRecords }} 条记录，每页 {{ pageSize }} 条，约 {{ totalPages }} 页
        </div>
      </a-form-item>

      <!-- 导出字段 -->
      <a-form-item label="导出字段" name="exportFields">
        <div class="field-selection">
          <div class="field-actions">
            <a-button size="small" @click="selectAllFields">全选</a-button>
            <a-button size="small" @click="deselectAllFields">全不选</a-button>
            <a-button size="small" @click="selectCommonFields">常用字段</a-button>
          </div>
          <a-checkbox-group v-model:value="exportForm.exportFields">
            <a-row>
              <a-col :span="12" v-for="field in exportFields" :key="field.key">
                <a-checkbox :value="field.key">{{ field.label }}</a-checkbox>
              </a-col>
            </a-row>
          </a-checkbox-group>
        </div>
      </a-form-item>

      <!-- 文件名称 -->
      <a-form-item label="文件名称" name="fileName">
        <a-input
          v-model:value="exportForm.fileName"
          placeholder="请输入文件名称（不含扩展名）"
        />
      </a-form-item>

      <!-- 高级选项 -->
      <a-form-item>
        <template #label>
          <span>高级选项</span>
          <a-button type="link" size="small" @click="showAdvanced = !showAdvanced">
            {{ showAdvanced ? '收起' : '展开' }}
          </a-button>
        </template>

        <div v-show="showAdvanced" class="advanced-options">
          <a-checkbox v-model:checked="exportForm.includeHeader">包含表头</a-checkbox>
          <a-checkbox v-model:checked="exportForm.includeStatistics">包含统计信息</a-checkbox>
          <a-checkbox v-model:checked="exportForm.includeCharts">包含图表（仅Excel）</a-checkbox>
          <a-checkbox v-model:checked="exportForm.compressFile">压缩文件</a-checkbox>

          <a-divider />

          <a-form-item label="数据过滤">
            <a-checkbox-group v-model:value="exportForm.dataFilters">
              <a-checkbox value="success_only">仅导出成功记录</a-checkbox>
              <a-checkbox value="exclude_refunded">排除已退款记录</a-checkbox>
              <a-checkbox value="exclude_cancelled">排除已取消记录</a-checkbox>
            </a-checkbox-group>
          </a-form-item>

          <a-form-item label="时间格式">
            <a-select v-model:value="exportForm.timeFormat" style="width: 100%">
              <a-select-option value="YYYY-MM-DD HH:mm:ss">年-月-日 时:分:秒</a-select-option>
              <a-select-option value="YYYY-MM-DD">年-月-日</a-select-option>
              <a-select-option value="HH:mm:ss">时:分:秒</a-select-option>
              <a-select-option value="timestamp">时间戳</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="数字格式">
            <a-select v-model:value="exportForm.numberFormat" style="width: 100%">
              <a-select-option value="currency">货币格式（¥1,000.00）</a-select-option>
              <a-select-option value="number">数字格式（1000.00）</a-select-option>
              <a-select-option value="raw">原始格式</a-select-option>
            </a-select>
          </a-form-item>
        </div>
      </a-form-item>

      <!-- 导出说明 -->
      <a-alert
        message="导出说明"
        type="info"
        show-icon
      >
        <template #description>
          <ul>
            <li>Excel格式支持图表和统计信息</li>
            <li>CSV格式适合数据分析工具导入</li>
            <li>PDF格式适合打印和存档</li>
            <li>大量数据导出可能需要等待较长时间</li>
            <li>导出文件将在24小时后自动删除</li>
          </ul>
        </template>
      </a-alert>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  searchParams: {
    type: Object,
    default: () => ({})
  },
  totalRecords: {
    type: Number,
    default: 0
  },
  pageSize: {
    type: Number,
    default: 20
  }
})

// Emits
const emit = defineEmits(['update:visible', 'export'])

// 表单引用
const formRef = ref()

// 响应式数据
const exporting = ref(false)
const showAdvanced = ref(false)

// 导出表单
const exportForm = reactive({
  exportFormat: 'excel',
  exportRange: 'current_page',
  customStart: undefined,
  customEnd: undefined,
  exportFields: [],
  fileName: '',
  includeHeader: true,
  includeStatistics: true,
  includeCharts: false,
  compressFile: false,
  dataFilters: [],
  timeFormat: 'YYYY-MM-DD HH:mm:ss',
  numberFormat: 'currency'
})

// 可导出字段定义
const exportFields = ref([
  { key: 'orderNo', label: '消费单号', required: true },
  { key: 'userId', label: '用户ID' },
  { key: 'userName', label: '用户名' },
  { key: 'deviceName', label: '设备名称' },
  { key: 'consumeMode', label: '消费模式' },
  { key: 'consumeAmount', label: '消费金额', required: true },
  { key: 'discountAmount', label: '优惠金额' },
  { key: 'actualAmount', label: '实付金额' },
  { key: 'balanceBefore', label: '消费前余额' },
  { key: 'balanceAfter', label: '消费后余额' },
  { key: 'paymentMethod', label: '支付方式' },
  { key: 'status', label: '消费状态' },
  { key: 'consumeTime', label: '消费时间' },
  { key: 'createTime', label: '创建时间' },
  { key: 'remark', label: '备注' }
])

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const totalPages = computed(() => {
  return Math.ceil(props.totalRecords / props.pageSize)
})

// 表单验证规则
const formRules = {
  exportFields: [
    { required: true, message: '请至少选择一个导出字段' }
  ],
  fileName: [
    { required: true, message: '请输入文件名称' },
    { max: 50, message: '文件名称不能超过50个字符' },
    { pattern: /^[a-zA-Z0-9\u4e00-\u9fa5_-]+$/, message: '文件名称只能包含中英文、数字、下划线和横线' }
  ],
  customRange: [
    {
      validator: (rule, value) => {
        if (exportForm.exportRange !== 'custom') {
          return Promise.resolve()
        }
        if (!exportForm.customStart || !exportForm.customEnd) {
          return Promise.reject(new Error('请输入完整的页码范围'))
        }
        if (exportForm.customStart < 1 || exportForm.customEnd < 1) {
          return Promise.reject(new Error('页码必须大于0'))
        }
        if (exportForm.customStart > exportForm.customEnd) {
          return Promise.reject(new Error('起始页不能大于结束页'))
        }
        if (exportForm.customEnd > totalPages.value) {
          return Promise.reject(new Error(`结束页不能超过总页数${totalPages.value}`))
        }
        return Promise.resolve()
      }
    }
  ]
}

// 方法定义
const handleExportRangeChange = () => {
  if (exportForm.exportRange === 'current_page') {
    exportForm.customStart = undefined
    exportForm.customEnd = undefined
  }
}

const selectAllFields = () => {
  exportForm.exportFields = exportFields.value.map(field => field.key)
}

const deselectAllFields = () => {
  exportForm.exportFields = []
}

const selectCommonFields = () => {
  exportForm.exportFields = [
    'orderNo',
    'userName',
    'deviceName',
    'consumeMode',
    'consumeAmount',
    'actualAmount',
    'status',
    'consumeTime'
  ]
}

const handleExport = async () => {
  try {
    await formRef.value.validate()

    exporting.value = true

    // 构建导出参数
    const exportParams = buildExportParams()

    // 触发导出事件
    emit('export', exportParams)

  } catch (error) {
    console.error('导出验证失败:', error)
  } finally {
    exporting.value = false
  }
}

const handleCancel = () => {
  visible.value = false
  resetForm()
}

const buildExportParams = () => {
  // 基础搜索参数
  const baseParams = { ...props.searchParams }

  // 页面范围参数
  if (exportForm.exportRange === 'current_page') {
    baseParams.pageNum = 1
    baseParams.pageSize = props.pageSize
  } else if (exportForm.exportRange === 'all_pages') {
    baseParams.pageNum = 1
    baseParams.pageSize = props.totalRecords
  } else if (exportForm.exportRange === 'custom') {
    const startIndex = (exportForm.customStart - 1) * props.pageSize
    const endIndex = exportForm.customEnd * props.pageSize
    baseParams.pageNum = Math.floor(startIndex / props.pageSize) + 1
    baseParams.pageSize = endIndex - startIndex
  }

  // 导出配置参数
  const exportConfig = {
    exportFormat: exportForm.exportFormat,
    exportFields: exportForm.exportFields,
    fileName: exportForm.fileName,
    includeHeader: exportForm.includeHeader,
    includeStatistics: exportForm.includeStatistics,
    includeCharts: exportForm.includeCharts,
    compressFile: exportForm.compressFile,
    dataFilters: exportForm.dataFilters,
    timeFormat: exportForm.timeFormat,
    numberFormat: exportForm.numberFormat
  }

  return {
    ...baseParams,
    exportConfig
  }
}

const resetForm = () => {
  Object.assign(exportForm, {
    exportFormat: 'excel',
    exportRange: 'current_page',
    customStart: undefined,
    customEnd: undefined,
    exportFields: [],
    fileName: '',
    includeHeader: true,
    includeStatistics: true,
    includeCharts: false,
    compressFile: false,
    dataFilters: [],
    timeFormat: 'YYYY-MM-DD HH:mm:ss',
    numberFormat: 'currency'
  })

  showAdvanced.value = false
}

// 监听visible变化，自动生成文件名
watch(() => props.visible, (newVisible) => {
  if (newVisible && !exportForm.fileName) {
    exportForm.fileName = `消费记录_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}`
    // 默认选择常用字段
    selectCommonFields()
  }
})
</script>

<style lang="less" scoped>
.field-selection {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  padding: 12px;
  background-color: #fafafa;

  .field-actions {
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #e8e8e8;

    .ant-btn {
      margin-right: 8px;
      margin-bottom: 4px;
    }
  }

  .ant-checkbox-group {
    .ant-row {
      .ant-col {
        padding: 4px 0;

        .ant-checkbox-wrapper {
          width: 100%;
          margin-right: 0;
        }
      }
    }
  }
}

.range-hint {
  color: #8c8c8c;
  font-size: 12px;
  margin-top: 8px;
  line-height: 1.4;
}

.advanced-options {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;

  .ant-checkbox {
    margin-right: 16px;
    margin-bottom: 8px;
  }

  .ant-divider {
    margin: 16px 0;
  }

  .ant-form-item {
    margin-bottom: 16px;
  }
}

.ant-alert {
  margin-top: 16px;

  :deep(.ant-alert-description) {
    ul {
      margin: 8px 0 0 0;
      padding-left: 20px;

      li {
        margin-bottom: 4px;
        line-height: 1.4;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}
</style>