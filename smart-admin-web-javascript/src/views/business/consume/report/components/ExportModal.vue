<template>
  <a-modal
    v-model:open="visible"
    title="导出统计报表"
    width="700px"
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
      <!-- 导出内容 -->
      <a-form-item label="导出内容" name="exportContent">
        <a-checkbox-group v-model:value="exportForm.exportContent">
          <a-row>
            <a-col :span="12">
              <a-checkbox value="summary">核心指标卡片</a-checkbox>
            </a-col>
            <a-col :span="12">
              <a-checkbox value="trend">消费趋势图表</a-checkbox>
            </a-col>
            <a-col :span="12">
              <a-checkbox value="mode">消费模式分布</a-checkbox>
            </a-col>
            <a-col :span="12">
              <a-checkbox value="device">设备消费排行</a-checkbox>
            </a-col>
            <a-col :span="12">
              <a-checkbox value="user">用户消费排行</a-checkbox>
            </a-col>
            <a-col :span="12">
              <a-checkbox value="hour">时段分布分析</a-checkbox>
            </a-col>
            <a-col :span="12">
              <a-checkbox value="region">地区分布热力图</a-checkbox>
            </a-col>
            <a-col :span="12">
              <a-checkbox value="table">详细数据表格</a-checkbox>
            </a-col>
          </a-row>
        </a-checkbox-group>
      </a-form-item>

      <!-- 导出格式 -->
      <a-form-item label="导出格式" name="exportFormat">
        <a-radio-group v-model:value="exportForm.exportFormat">
          <a-radio value="pdf">PDF 报告</a-radio>
          <a-radio value="excel">Excel 工作簿</a-radio>
          <a-radio value="html">HTML 网页</a-checkbox>
          <a-radio value="ppt">PowerPoint 演示</a-radio>
        </a-radio-group>
      </a-form-item>

      <!-- 报告样式 -->
      <a-form-item label="报告样式" name="reportStyle">
        <a-select v-model:value="exportForm.reportStyle" style="width: 100%">
          <a-select-option value="standard">标准样式</a-select-option>
          <a-select-option value="professional">专业样式</a-select-option>
          <a-select-option value="colorful">彩色样式</a-select-option>
          <a-select-option value="minimal">极简样式</a-select-option>
          <a-select-option value="custom">自定义样式</a-select-option>
        </a-select>
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
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="页面方向">
                <a-select v-model:value="exportForm.pageOrientation">
                  <a-select-option value="portrait">纵向</a-select-option>
                  <a-select-option value="landscape">横向</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="纸张大小">
                <a-select v-model:value="exportForm.pageSize">
                  <a-select-option value="A4">A4</a-select-option>
                  <a-select-option value="A3">A3</a-select-option>
                  <a-select-option value="Letter">Letter</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="图表分辨率">
                <a-select v-model:value="exportForm.chartResolution">
                  <a-select-option value="low">低 (72 DPI)</a-select-option>
                  <a-select-option value="medium">中 (150 DPI)</a-select-option>
                  <a-select-option value="high">高 (300 DPI)</a-select-option>
                  <a-select-option value="ultra">超高 (600 DPI)</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="图片质量">
                <a-select v-model:value="exportForm.imageQuality">
                  <a-select-option value="low">低质量</a-select-option>
                  <a-select-option value="medium">中等质量</a-select-option>
                  <a-select-option value="high">高质量</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item label="包含内容">
            <a-checkbox-group v-model:value="exportForm.includeContent">
              <a-checkbox value="cover">封面页</a-checkbox>
              <a-checkbox value="toc">目录页</a-checkbox>
              <a-checkbox value="filters">筛选条件</a-checkbox>
              <a-checkbox value="data_source">数据来源</a-checkbox>
              <a-checkbox value="generation_time">生成时间</a-checkbox>
              <a-checkbox value="page_numbers">页码</a-checkbox>
              <a-checkbox value="watermark">水印</a-checkbox>
            </a-checkbox-group>
          </a-form-item>

          <a-form-item label="页眉页脚">
            <a-checkbox v-model:checked="exportForm.enableHeaderFooter">
              启用页眉页脚
            </a-checkbox>

            <div v-if="exportForm.enableHeaderFooter" class="header-footer-config">
              <a-input
                v-model:value="exportForm.headerText"
                placeholder="页眉文本"
                style="margin-bottom: 8px"
              />
              <a-input
                v-model:value="exportForm.footerText"
                placeholder="页脚文本"
              />
            </div>
          </a-form-item>

          <a-form-item label="水印设置" v-if="exportForm.includeContent?.includes('watermark')">
            <a-input
              v-model:value="exportForm.watermarkText"
              placeholder="水印文字"
              style="margin-bottom: 8px"
            />
            <a-row :gutter="8">
              <a-col :span="8">
                <a-select v-model:value="exportForm.watermarkOpacity" placeholder="透明度">
                  <a-select-option :value="0.1">10%</a-select-option>
                  <a-select-option :value="0.2">20%</a-select-option>
                  <a-select-option :value="0.3">30%</a-select-option>
                  <a-select-option :value="0.5">50%</a-select-option>
                </a-select>
              </a-col>
              <a-col :span="8">
                <a-select v-model:value="exportForm.watermarkSize" placeholder="大小">
                  <a-select-option value="small">小</a-select-option>
                  <a-select-option value="medium">中</a-select-option>
                  <a-select-option value="large">大</a-select-option>
                </a-select>
              </a-col>
              <a-col :span="8">
                <a-select v-model:value="exportForm.watermarkColor" placeholder="颜色">
                  <a-select-option value="gray">灰色</a-select-option>
                  <a-select-option value="blue">蓝色</a-select-option>
                  <a-select-option value="red">红色</a-select-option>
                </a-select>
              </a-col>
            </a-row>
          </a-form-item>

          <a-form-item label="安全设置">
            <a-checkbox v-model:checked="exportForm.enablePassword">设置访问密码</a-checkbox>
            <a-input
              v-if="exportForm.enablePassword"
              v-model:value="exportForm.accessPassword"
              placeholder="请输入访问密码"
              type="password"
              style="margin-top: 8px"
            />
          </a-form-item>
        </div>
      </a-form-item>

      <!-- 文件信息 -->
      <a-form-item label="文件名称" name="fileName">
        <a-input
          v-model:value="exportForm.fileName"
          placeholder="请输入文件名称（不含扩展名）"
        />
      </a-form-item>

      <a-form-item label="文件描述">
        <a-textarea
          v-model:value="exportForm.fileDescription"
          :rows="2"
          placeholder="请输入文件描述（选填）"
          :maxlength="200"
          show-count
        />
      </a-form-item>

      <!-- 报告预览 -->
      <a-form-item label="报告预览">
        <div class="report-preview">
          <a-card size="small" title="预览信息">
            <div class="preview-item">
              <span class="preview-label">报告类型：</span>
              <span class="preview-value">消费统计分析报告</span>
            </div>
            <div class="preview-item">
              <span class="preview-label">生成时间：</span>
              <span class="preview-value">{{ formatDateTime(new Date()) }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-label">数据周期：</span>
              <span class="preview-value">{{ getDataRangeText() }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-label">包含图表：</span>
              <span class="preview-value">{{ getSelectedChartsCount() }} 个</span>
            </div>
            <div class="preview-item">
              <span class="preview-label">预计页数：</span>
              <span class="preview-value">{{ getEstimatedPageCount() }} 页</span>
            </div>
            <div class="preview-item">
              <span class="preview-label">预计大小：</span>
              <span class="preview-value">{{ getEstimatedFileSize() }}</span>
            </div>
          </a-card>
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
            <li>PDF格式适合打印和正式报告</li>
            <li>Excel格式包含完整数据，便于进一步分析</li>
            <li>HTML格式可在浏览器中查看，支持交互</li>
            <li>PPT格式适合演示汇报使用</li>
            <li>大量图表导出可能需要较长时间</li>
            <li>导出文件将在48小时后自动删除</li>
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
  reportData: {
    type: Object,
    default: () => ({})
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
  exportContent: ['summary', 'trend', 'mode', 'device', 'user', 'table'],
  exportFormat: 'pdf',
  reportStyle: 'standard',
  pageOrientation: 'portrait',
  pageSize: 'A4',
  chartResolution: 'high',
  imageQuality: 'high',
  includeContent: ['cover', 'toc', 'filters', 'generation_time', 'page_numbers'],
  enableHeaderFooter: false,
  headerText: '',
  footerText: '',
  watermarkText: '消费统计分析报告',
  watermarkOpacity: 0.1,
  watermarkSize: 'medium',
  watermarkColor: 'gray',
  enablePassword: false,
  accessPassword: '',
  fileName: '',
  fileDescription: ''
})

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 表单验证规则
const formRules = {
  exportContent: [
    { required: true, message: '请选择至少一个导出内容' }
  ],
  exportFormat: [
    { required: true, message: '请选择导出格式' }
  ],
  reportStyle: [
    { required: true, message: '请选择报告样式' }
  ],
  fileName: [
    { required: true, message: '请输入文件名称' },
    { max: 50, message: '文件名称不能超过50个字符' },
    { pattern: /^[a-zA-Z0-9\u4e00-\u9fa5_-]+$/, message: '文件名称只能包含中英文、数字、下划线和横线' }
  ]
}

// 方法定义
const handleExport = async () => {
  try {
    await formRef.value.validate()

    exporting.value = true

    // 构建导出参数
    const exportParams = buildExportParams()

    // 触发导出事件
    emit('export', exportParams)

    message.success('报表导出请求已提交')
    visible.value = false

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
  return {
    exportContent: exportForm.exportContent,
    exportFormat: exportForm.exportFormat,
    reportStyle: exportForm.reportStyle,
    pageSettings: {
      orientation: exportForm.pageOrientation,
      pageSize: exportForm.pageSize
    },
    qualitySettings: {
      chartResolution: exportForm.chartResolution,
      imageQuality: exportForm.imageQuality
    },
    contentSettings: {
      includeContent: exportForm.includeContent,
      enableHeaderFooter: exportForm.enableHeaderFooter,
      headerText: exportForm.headerText,
      footerText: exportForm.footerText
    },
    watermarkSettings: exportForm.includeContent?.includes('watermark') ? {
      text: exportForm.watermarkText,
      opacity: exportForm.watermarkOpacity,
      size: exportForm.watermarkSize,
      color: exportForm.watermarkColor
    } : null,
    securitySettings: {
      enablePassword: exportForm.enablePassword,
      password: exportForm.accessPassword
    },
    fileInfo: {
      fileName: exportForm.fileName,
      description: exportForm.fileDescription,
      generatedAt: new Date().toISOString()
    },
    reportData: props.reportData
  }
}

const resetForm = () => {
  Object.assign(exportForm, {
    exportContent: ['summary', 'trend', 'mode', 'device', 'user', 'table'],
    exportFormat: 'pdf',
    reportStyle: 'standard',
    pageOrientation: 'portrait',
    pageSize: 'A4',
    chartResolution: 'high',
    imageQuality: 'high',
    includeContent: ['cover', 'toc', 'filters', 'generation_time', 'page_numbers'],
    enableHeaderFooter: false,
    headerText: '',
    footerText: '',
    watermarkText: '消费统计分析报告',
    watermarkOpacity: 0.1,
    watermarkSize: 'medium',
    watermarkColor: 'gray',
    enablePassword: false,
    accessPassword: '',
    fileName: '',
    fileDescription: ''
  })

  showAdvanced.value = false
}

// 工具方法
const formatDateTime = (dateTime) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const getDataRangeText = () => {
  if (props.reportData?.filterForm?.dateRange) {
    const [start, end] = props.reportData.filterForm.dateRange
    return `${start.format('YYYY-MM-DD')} 至 ${end.format('YYYY-MM-DD')}`
  }
  return '未选择时间范围'
}

const getSelectedChartsCount = () => {
  const chartItems = ['trend', 'mode', 'device', 'user', 'hour', 'region']
  return exportForm.exportContent.filter(item => chartItems.includes(item)).length
}

const getEstimatedPageCount = () => {
  let pageCount = 1 // 封面

  if (exportForm.includeContent?.includes('toc')) {
    pageCount += 1 // 目录
  }

  if (exportForm.includeContent?.includes('filters')) {
    pageCount += 1 // 筛选条件
  }

  // 每个图表大约1-2页
  pageCount += getSelectedChartsCount() * 1.5

  if (exportForm.exportContent?.includes('table')) {
    pageCount += 3 // 数据表格
  }

  return Math.ceil(pageCount)
}

const getEstimatedFileSize = () => {
  let sizeMB = 2 // 基础大小

  // 根据图表分辨率调整大小
  const resolutionMultiplier = {
    low: 0.5,
    medium: 1,
    high: 2,
    ultra: 4
  }
  sizeMB *= resolutionMultiplier[exportForm.chartResolution] || 1

  // 根据导出格式调整大小
  const formatMultiplier = {
    pdf: 1,
    excel: 0.7,
    html: 0.5,
    ppt: 1.5
  }
  sizeMB *= formatMultiplier[exportForm.exportFormat] || 1

  // 根据图片质量调整大小
  const qualityMultiplier = {
    low: 0.5,
    medium: 1,
    high: 2
  }
  sizeMB *= qualityMultiplier[exportForm.imageQuality] || 1

  if (sizeMB < 1) {
    return `${(sizeMB * 1024).toFixed(0)} KB`
  } else {
    return `${sizeMB.toFixed(1)} MB`
  }
}

// 监听visible变化，自动生成文件名
watch(() => props.visible, (newVisible) => {
  if (newVisible && !exportForm.fileName) {
    exportForm.fileName = `消费统计分析报告_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}`
  }
})
</script>

<style lang="less" scoped>
.advanced-options {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;

  .ant-row {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .header-footer-config {
    margin-top: 12px;
    padding-left: 24px;
  }
}

.report-preview {
  .preview-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 4px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .preview-label {
      color: #8c8c8c;
      font-size: 13px;
    }

    .preview-value {
      color: #262626;
      font-size: 13px;
      font-weight: 500;
    }
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