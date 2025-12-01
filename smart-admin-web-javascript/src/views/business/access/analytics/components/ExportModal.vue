<!--
 * 数据导出模态框组件
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <a-modal
    :open="visible"
    @update:open="val => emit('update:visible', val)"
    title="导出数据报表"
    width="600px"
    :confirm-loading="loading"
    @ok="handleConfirm"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="exportForm"
      :rules="formRules"
      layout="vertical"
    >
      <!-- 导出格式 -->
      <a-form-item label="导出格式" name="format" required>
        <a-radio-group v-model:value="exportForm.format">
          <a-radio value="EXCEL">Excel 文件</a-radio>
          <a-radio value="PDF">PDF 文件</a-radio>
          <a-radio value="CSV">CSV 文件</a-radio>
        </a-radio-group>
      </a-form-item>

      <!-- 时间范围 -->
      <a-form-item label="时间范围" name="timeRange" required>
        <a-range-picker
          v-model:value="exportForm.timeRange"
          :shortcuts="dateShortcuts"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </a-form-item>

      <!-- 包含内容 -->
      <a-form-item label="包含内容" name="includeContent">
        <a-checkbox-group v-model:value="exportForm.includeContent">
          <a-checkbox value="summary">数据概览</a-checkbox>
          <a-checkbox value="charts">图表图片</a-checkbox>
          <a-checkbox value="details">详细数据</a-checkbox>
          <a-checkbox value="analysis">分析结论</a-checkbox>
        </a-checkbox-group>
      </a-form-item>

      <!-- 图表选择 -->
      <a-form-item
        label="选择图表"
        name="selectedCharts"
        v-if="exportForm.includeContent.includes('charts')"
      >
        <a-checkbox-group v-model:value="exportForm.selectedCharts">
          <a-row :gutter="16">
            <a-col :span="12" v-for="chart in chartOptions" :key="chart.value">
              <a-checkbox :value="chart.value">{{ chart.label }}</a-checkbox>
            </a-col>
          </a-row>
        </a-checkbox-group>
      </a-form-item>

      <!-- 文件名 -->
      <a-form-item label="文件名" name="fileName">
        <a-input
          v-model:value="exportForm.fileName"
          placeholder="请输入文件名（不包含扩展名）"
          :maxlength="50"
          show-count
        />
      </a-form-item>

      <!-- 邮件发送 -->
      <a-form-item label="邮件发送" name="emailSend">
        <a-checkbox v-model:checked="exportForm.emailSend">
          生成后发送到邮箱
        </a-checkbox>
      </a-form-item>

      <a-form-item
        label="收件邮箱"
        name="emailTo"
        v-if="exportForm.emailSend"
        required
      >
        <a-select
          v-model:value="exportForm.emailTo"
          mode="multiple"
          placeholder="请选择收件邮箱"
          style="width: 100%"
          :options="emailOptions"
          show-search
          :filter-option="filterEmailOption"
        />
      </a-form-item>

      <!-- 高级选项 -->
      <a-form-item>
        <a-collapse ghost>
          <a-collapse-panel key="advanced" header="高级选项">
            <!-- 数据精度 -->
            <a-form-item label="数据精度" name="precision">
              <a-select v-model:value="exportForm.precision" style="width: 200px">
                <a-select-option :value="0">整数</a-select-option>
                <a-select-option :value="1">1位小数</a-select-option>
                <a-select-option :value="2">2位小数</a-select-option>
                <a-select-option :value="3">3位小数</a-select-option>
              </a-select>
            </a-form-item>

            <!-- 日期格式 -->
            <a-form-item label="日期格式" name="dateFormat">
              <a-select v-model:value="exportForm.dateFormat" style="width: 200px">
                <a-select-option value="YYYY-MM-DD">2025-01-13</a-select-option>
                <a-select-option value="YYYY/MM/DD">2025/01/13</a-select-option>
                <a-select-option value="DD/MM/YYYY">13/01/2025</a-select-option>
                <a-select-option value="YYYY-MM-DD HH:mm">2025-01-13 14:30</a-select-option>
              </a-select>
            </a-form-item>

            <!-- 纸张方向（仅PDF） -->
            <a-form-item
              label="纸张方向"
              name="orientation"
              v-if="exportForm.format === 'PDF'"
            >
              <a-radio-group v-model:value="exportForm.orientation">
                <a-radio value="portrait">纵向</a-radio>
                <a-radio value="landscape">横向</a-radio>
              </a-radio-group>
            </a-form-item>

            <!-- 图片质量（仅包含图表时） -->
            <a-form-item
              label="图片质量"
              name="imageQuality"
              v-if="exportForm.includeContent.includes('charts')"
            >
              <a-select v-model:value="exportForm.imageQuality" style="width: 200px">
                <a-select-option value="low">低质量（快速）</a-select-option>
                <a-select-option value="medium">中等质量</a-select-option>
                <a-select-option value="high">高质量</a-select-option>
              </a-select>
            </a-form-item>
          </a-collapse-panel>
        </a-collapse>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  exportType: {
    type: String,
    default: 'TRAFFIC'
  },
  queryParams: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['update:visible', 'confirm']);

const formRef = ref();
const loading = ref(false);

// 导出表单
const exportForm = reactive({
  format: 'EXCEL',
  timeRange: [
    dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
    dayjs().format('YYYY-MM-DD')
  ],
  includeContent: ['summary', 'charts', 'details'],
  selectedCharts: [],
  fileName: '',
  emailSend: false,
  emailTo: [],
  precision: 2,
  dateFormat: 'YYYY-MM-DD',
  orientation: 'portrait',
  imageQuality: 'medium'
});

// 表单验证规则
const formRules = {
  format: [
    { required: true, message: '请选择导出格式', trigger: 'change' }
  ],
  timeRange: [
    { required: true, message: '请选择时间范围', trigger: 'change' }
  ],
  fileName: [
    { max: 50, message: '文件名不能超过50个字符', trigger: 'blur' }
  ],
  emailTo: [
    {
      validator: (rule, value) => {
        if (exportForm.emailSend && (!value || value.length === 0)) {
          return Promise.reject('请选择收件邮箱');
        }
        return Promise.resolve();
      },
      trigger: 'change'
    }
  ]
};

// 日期快捷选项
const dateShortcuts = [
  {
    label: '今天',
    value: () => [dayjs().format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]
  },
  {
    label: '昨天',
    value: () => [
      dayjs().subtract(1, 'day').format('YYYY-MM-DD'),
      dayjs().subtract(1, 'day').format('YYYY-MM-DD')
    ]
  },
  {
    label: '最近7天',
    value: () => [
      dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
      dayjs().format('YYYY-MM-DD')
    ]
  },
  {
    label: '最近30天',
    value: () => [
      dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
      dayjs().format('YYYY-MM-DD')
    ]
  }
];

// 图表选项（根据导出类型动态变化）
const chartOptions = computed(() => {
  const options = {
    TRAFFIC: [
      { label: '24小时通行热力图', value: 'heatmap' },
      { label: '通行流量趋势图', value: 'traffic-trend' },
      { label: '区域通行统计图', value: 'area-stats' },
      { label: '人员类型分布图', value: 'person-type' },
      { label: '异常通行趋势图', value: 'abnormal-trend' },
      { label: '通行高峰时段图', value: 'peak-hour' }
    ],
    DEVICE: [
      { label: '设备在线率统计', value: 'online-rate' },
      { label: '设备故障率分析', value: 'failure-rate' },
      { label: '设备使用频率排行', value: 'usage-ranking' },
      { label: '设备性能指标', value: 'performance' },
      { label: '设备健康度评分', value: 'health-score' }
    ],
    PERMISSION: [
      { label: '权限申请趋势', value: 'application-trend' },
      { label: '权限使用频率统计', value: 'usage-frequency' },
      { label: '权限类型分布', value: 'type-distribution' },
      { label: '权限使用效率分析', value: 'efficiency' },
      { label: '过期权限预警', value: 'expiring' }
    ],
    SECURITY: [
      { label: '安防事件趋势图', value: 'event-trend' },
      { label: '高风险区域识别', value: 'high-risk' },
      { label: '安防级别分布', value: 'level-distribution' },
      { label: '告警处理效率', value: 'alarm-efficiency' },
      { label: '安防态势评分', value: 'posture-score' }
    ]
  };

  return options[props.exportType] || [];
});

// 邮箱选项
const emailOptions = ref([
  { label: 'admin@ioe-dream.com', value: 'admin@ioe-dream.com' },
  { label: 'manager@ioe-dream.com', value: 'manager@ioe-dream.com' },
  { label: 'security@ioe-dream.com', value: 'security@ioe-dream.com' }
]);

// 邮箱过滤
const filterEmailOption = (input, option) => {
  return option.label.toLowerCase().includes(input.toLowerCase());
};

// 生成默认文件名
const generateDefaultFileName = () => {
  const typeNames = {
    TRAFFIC: '通行数据分析',
    DEVICE: '设备运行分析',
    PERMISSION: '权限使用分析',
    SECURITY: '安防态势分析',
    DASHBOARD: '综合仪表盘'
  };

  const typeName = typeNames[props.exportType] || '数据分析';
  const today = dayjs().format('YYYYMMDD');
  return `${typeName}_${today}`;
};

// 处理确认导出
const handleConfirm = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;

    const exportParams = {
      exportType: props.exportType,
      format: exportForm.format,
      timeRange: exportForm.timeRange,
      includeContent: exportForm.includeContent,
      selectedCharts: exportForm.includeContent.includes('charts') ? exportForm.selectedCharts : [],
      fileName: exportForm.fileName || generateDefaultFileName(),
      emailSend: exportForm.emailSend,
      emailTo: exportForm.emailTo,
      precision: exportForm.precision,
      dateFormat: exportForm.dateFormat,
      orientation: exportForm.orientation,
      imageQuality: exportForm.imageQuality,
      ...props.queryParams
    };

    emit('confirm', exportParams);
  } catch (error) {
    console.error('表单验证失败:', error);
  } finally {
    loading.value = false;
  }
};

// 处理取消
const handleCancel = () => {
  emit('update:visible', false);
};

// 监听可见性变化
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      // 重置表单
      exportForm.timeRange = props.queryParams.timeRange || [
        dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
        dayjs().format('YYYY-MM-DD')
      ];
      exportForm.fileName = generateDefaultFileName();

      // 默认选择所有图表
      if (chartOptions.value.length > 0) {
        exportForm.selectedCharts = chartOptions.value.map(chart => chart.value);
      }
    }
  }
);

// 监听包含内容变化
watch(
  () => exportForm.includeContent,
  (newVal) => {
    if (!newVal.includes('charts')) {
      exportForm.selectedCharts = [];
    } else if (exportForm.selectedCharts.length === 0 && chartOptions.value.length > 0) {
      exportForm.selectedCharts = chartOptions.value.map(chart => chart.value);
    }
  }
);
</script>

<style scoped>
:deep(.ant-collapse-header) {
  padding: 8px 0;
}

:deep(.ant-checkbox-wrapper) {
  margin-bottom: 8px;
}

:deep(.ant-col) .ant-checkbox-wrapper {
  margin-bottom: 8px;
}
</style>