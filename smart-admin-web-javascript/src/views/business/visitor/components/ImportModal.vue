<!--
 * 访客批量导入组件
 *
 * 功能：
 * - Excel文件导入
 * - 模板下载
 * - 数据预览
 * - 错误提示
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    v-model:open="visible"
    title="批量导入访客信息"
    width="800px"
    :confirmLoading="importing"
    @ok="handleImport"
    @cancel="handleCancel"
  >
    <div class="import-modal">
      <!-- 步骤1：选择文件 -->
      <div v-if="currentStep === 1" class="step-section">
        <a-alert
          message="导入说明"
          type="info"
          show-icon
          style="margin-bottom: 16px"
        >
          <template #description>
            <ul>
              <li>支持导入 .xlsx 和 .xls 格式的Excel文件</li>
              <li>单次最多导入 1000 条记录</li>
              <li>请先下载模板，按照模板格式填写数据</li>
              <li>必填字段：访客姓名、手机号、身份证号</li>
            </ul>
          </template>
        </a-alert>

        <div class="template-download">
          <a-button type="primary" ghost @click="downloadTemplate">
            <template #icon><DownloadOutlined /></template>
            下载导入模板
          </a-button>
          <span style="margin-left: 12px; color: #999;">
            模板包含所有必填和可选字段的说明
          </span>
        </div>

        <a-divider />

        <a-upload
          :file-list="fileList"
          :before-upload="beforeUpload"
          :on-remove="handleRemove"
          :accept="'.xlsx,.xls'"
          :max-count="1"
        >
          <a-button>
            <template #icon><UploadOutlined /></template>
            选择文件
          </a-button>
        </a-upload>

        <div v-if="fileList.length > 0" class="file-info">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="文件名">
              {{ fileList[0].name }}
            </a-descriptions-item>
            <a-descriptions-item label="文件大小">
              {{ formatFileSize(fileList[0].size) }}
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </div>

      <!-- 步骤2：数据预览 -->
      <div v-if="currentStep === 2" class="step-section">
        <a-alert
          :message="`共解析 ${previewData.length} 条数据`"
          type="success"
          show-icon
          style="margin-bottom: 16px"
        />

        <a-table
          :columns="previewColumns"
          :data-source="previewData.slice(0, 10)"
          :pagination="false"
          size="small"
          :scroll="{ x: 1200 }"
          max-height="400px"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'rowNum'">
              <a-tag color="blue">{{ record.rowNum }}</a-tag>
            </template>
            <template v-else-if="column.key === 'valid'">
              <a-tag :color="record.valid ? 'success' : 'error'">
                {{ record.valid ? '有效' : '无效' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'errors'">
              <a-tooltip v-if="record.errors && record.errors.length > 0">
                <template #title>
                  <div v-for="(error, idx) in record.errors" :key="idx">
                    {{ error }}
                  </div>
                </template>
                <a-tag color="warning">{{ record.errors.length }} 个问题</a-tag>
              </a-tooltip>
              <span v-else style="color: #52c41a;">无</span>
            </template>
          </template>
        </a-table>

        <div v-if="previewData.length > 10" style="margin-top: 8px; text-align: center;">
          <a-typography-text type="secondary">
            仅显示前 10 条数据预览，实际将导入全部 {{ previewData.length }} 条
          </a-typography-text>
        </div>

        <a-divider />

        <a-statistic-group direction="row">
          <a-statistic title="有效数据" :value="validCount" :value-style="{ color: '#52c41a' }" />
          <a-statistic title="无效数据" :value="invalidCount" :value-style="{ color: '#ff4d4f' }" />
        </a-statistic-group>
      </div>

      <!-- 步骤3：导入结果 -->
      <div v-if="currentStep === 3" class="step-section">
        <a-result
          :status="importResult.success ? 'success' : 'warning'"
          :title="importResult.success ? '导入成功' : '导入完成'"
        >
          <template #subTitle>
            <div v-if="importResult.success">
              <p>成功导入 {{ importResult.successCount }} 条访客信息</p>
              <p v-if="importResult.failCount > 0">
                失败 {{ importResult.failCount }} 条（已跳过）
              </p>
            </div>
            <div v-else>
              <p>导入 {{ importResult.totalCount }} 条数据</p>
              <p>成功 {{ importResult.successCount }} 条</p>
              <p>失败 {{ importResult.failCount }} 条</p>
            </div>
          </template>
          <template #extra>
            <a-space>
              <a-button type="primary" @click="handleFinish">
                完成
              </a-button>
              <a-button v-if="importResult.failCount > 0" @click="downloadErrorLog">
                <template #icon><DownloadOutlined /></template>
                下载错误日志
              </a-button>
            </a-space>
          </template>
        </a-result>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import {
  DownloadOutlined,
  UploadOutlined,
} from '@ant-design/icons-vue';
import { visitorApi } from '/@/api/business/visitor/visitor-api';
import * as XLSX from 'xlsx';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['update:visible', 'success']);

// 当前步骤
const currentStep = ref(1);
const importing = ref(false);
const fileList = ref([]);
const previewData = ref([]);
const importResult = ref({
  success: false,
  totalCount: 0,
  successCount: 0,
  failCount: 0,
  errorLog: [],
});

// 预览表格列
const previewColumns = [
  { title: '行号', dataIndex: 'rowNum', key: 'rowNum', width: 80, align: 'center' },
  { title: '访客姓名', dataIndex: 'visitorName', key: 'visitorName', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '身份证号', dataIndex: 'idCard', key: 'idCard', width: 180 },
  { title: '公司名称', dataIndex: 'company', key: 'company', width: 150 },
  { title: '访客等级', dataIndex: 'visitorLevel', key: 'visitorLevel', width: 100 },
  { title: '状态', dataIndex: 'valid', key: 'valid', width: 80, align: 'center' },
  { title: '问题', dataIndex: 'errors', key: 'errors', width: 120 },
];

// 有效/无效数据统计
const validCount = computed(() => {
  return previewData.value.filter(item => item.valid).length;
});

const invalidCount = computed(() => {
  return previewData.value.filter(item => !item.valid).length;
});

// 下载模板
const downloadTemplate = () => {
  const template = [
    {
      '访客姓名*': '张三',
      '手机号*': '13800138000',
      '身份证号*': '110101199001011234',
      '性别': '男',
      '公司名称': '某某公司',
      '访客等级': '普通访客',
      '备注': '示例数据',
    },
    {
      '访客姓名*': '李四',
      '手机号*': '13900139000',
      '身份证号*': '110101199001011235',
      '性别': '女',
      '公司名称': '另一公司',
      '访客等级': 'VIP访客',
      '备注': '',
    },
  ];

  const ws = XLSX.utils.json_to_sheet(template);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, '访客导入模板');
  XLSX.writeFile(wb, `访客导入模板_${new Date().getTime()}.xlsx`);
  message.success('模板下载成功');
};

// 文件上传前处理
const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'application/vnd.ms-excel';
  if (!isExcel) {
    message.error('只能上传 Excel 文件！');
    return false;
  }
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB！');
    return false;
  }

  // 读取文件
  const reader = new FileReader();
  reader.onload = (e) => {
    try {
      const data = new Uint8Array(e.target.result);
      const workbook = XLSX.read(data, { type: 'array' });
      const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
      const jsonData = XLSX.utils.sheet_to_json(firstSheet, { header: 1 });

      // 解析数据
      parseExcelData(jsonData);
    } catch (error) {
      message.error('文件解析失败，请检查文件格式');
      console.error('文件解析错误:', error);
    }
  };
  reader.readAsArrayBuffer(file);

  fileList.value = [file];
  return false; // 阻止自动上传
};

// 解析Excel数据
const parseExcelData = (data) => {
  if (data.length < 2) {
    message.warning('Excel文件中没有数据');
    return;
  }

  const headers = data[0];
  const rows = data.slice(1);

  const parsed = rows.map((row, index) => {
    const item = {
      rowNum: index + 2, // Excel行号从2开始（第1行是表头）
      visitorName: row[0],
      phone: row[1],
      idCard: row[2],
      gender: row[3],
      company: row[4],
      visitorLevel: row[5],
      remark: row[6],
      valid: true,
      errors: [],
    };

    // 验证数据
    const errors = [];

    if (!item.visitorName || item.visitorName.trim() === '') {
      errors.push('访客姓名不能为空');
    }

    if (!item.phone || !/^1[3-9]\d{9}$/.test(item.phone)) {
      errors.push('手机号格式不正确');
    }

    if (!item.idCard || !/^\d{15}$|^\d{18}$/.test(item.idCard)) {
      errors.push('身份证号格式不正确');
    }

    if (item.gender && !['男', '女'].includes(item.gender)) {
      errors.push('性别只能是"男"或"女"');
    }

    if (item.visitorLevel && !['普通访客', 'VIP访客', '常客', '黑名单'].includes(item.visitorLevel)) {
      errors.push('访客等级无效');
    }

    if (errors.length > 0) {
      item.valid = false;
      item.errors = errors;
    }

    return item;
  });

  previewData.value = parsed;
  currentStep.value = 2;

  message.success(`成功解析 ${parsed.length} 条数据`);
};

// 移除文件
const handleRemove = () => {
  fileList.value = [];
  previewData.value = [];
  currentStep.value = 1;
};

// 执行导入
const handleImport = async () => {
  if (currentStep.value === 1) {
    if (fileList.length === 0) {
      message.warning('请先选择要导入的文件');
      return;
    }
    return;
  }

  if (currentStep.value === 2) {
    if (invalidCount.value > 0) {
      message.warning(`有 ${invalidCount.value} 条无效数据，请修正后再导入`);
      return;
    }

    importing.value = true;
    try {
      // 转换为API格式
      const data = previewData.value
        .filter(item => item.valid)
        .map(item => ({
          visitorName: item.visitorName,
          phone: item.phone,
          idCard: item.idCard,
          gender: item.gender === '男' ? 1 : 2,
          company: item.company,
          visitorLevel: convertLevelCode(item.visitorLevel),
          remark: item.remark,
        }));

      // 批量导入
      const result = await visitorApi.batchImportVisitors(data);

      if (result.code === 200) {
        importResult.value = {
          success: true,
          totalCount: data.length,
          successCount: result.data?.successCount || data.length,
          failCount: result.data?.failCount || 0,
          errorLog: result.data?.errorLog || [],
        };
        currentStep.value = 3;
      } else {
        message.error(result.message || '导入失败');
      }
    } catch (error) {
      message.error('导入失败');
      console.error('导入错误:', error);
    } finally {
      importing.value = false;
    }
  }

  if (currentStep.value === 3) {
    handleFinish();
  }
};

// 转换访客等级代码
const convertLevelCode = (levelName) => {
  const levelMap = {
    '普通访客': 'NORMAL',
    'VIP访客': 'VIP',
    '常客': 'FREQUENT',
    '黑名单': 'BLACKLIST',
  };
  return levelMap[levelName] || 'NORMAL';
};

// 取消导入
const handleCancel = () => {
  currentStep.value = 1;
  fileList.value = [];
  previewData.value = [];
  importResult.value = {};
  emit('update:visible', false);
};

// 完成导入
const handleFinish = () => {
  emit('success', importResult.value);
  handleCancel();
};

// 下载错误日志
const downloadErrorLog = () => {
  const errorLog = importResult.value.errorLog || [];
  if (errorLog.length === 0) {
    message.info('没有错误日志');
    return;
  }

  const logContent = errorLog.map(log => {
    return JSON.stringify(log, null, 2);
  }).join('\n\n');

  const blob = new Blob([logContent], { type: 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `导入错误日志_${new Date().getTime()}.txt`;
  link.click();
  URL.revokeObjectURL(url);
};

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
};

// 重置状态
const reset = () => {
  currentStep.value = 1;
  importing.value = false;
  fileList.value = [];
  previewData.value = [];
  importResult.value = {};
};

defineExpose({
  reset,
});
</script>

<style lang="scss" scoped>
.import-modal {
  min-height: 300px;
}

.template-download {
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 4px;
  text-align: center;
}

.file-info {
  margin-top: 16px;
  padding: 12px;
  background-color: #f0f2f5;
  border-radius: 4px;
}

.step-section {
  padding: 8px 0;
}
</style>
