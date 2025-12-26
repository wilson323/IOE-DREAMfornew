<!--
  @fileoverview 排班模板管理主页面
  @author IOE-DREAM Team
  @description 排班模板列表、搜索、筛选、批量操作
-->
<template>
  <div class="template-management">
    <!-- 查询表单 -->
    <a-card :bordered="false" class="query-card">
      <a-form layout="inline" class="smart-query-form">
        <a-row :gutter="[16, 16]" class="smart-query-form-row">
          <a-form-item label="模板名称" class="smart-query-form-item">
            <a-input
              v-model:value="queryForm.templateName"
              placeholder="请输入模板名称"
              style="width: 220px"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="模板类型" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.templateType"
              placeholder="请选择模板类型"
              style="width: 160px"
              allow-clear
            >
              <a-select-option value="DEPARTMENT">部门模板</a-select-option>
              <a-select-option value="POSITION">岗位模板</a-select-option>
              <a-select-option value="PERSONAL">个人模板</a-select-option>
              <a-select-option value="GLOBAL">全局模板</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="状态" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.status"
              placeholder="请选择状态"
              style="width: 120px"
              allow-clear
            >
              <a-select-option :value="1">启用</a-select-option>
              <a-select-option :value="0">禁用</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-space>
              <a-button type="primary" @click="queryTemplates">
                <template #icon><SearchOutlined /></template>
                查询
              </a-button>
              <a-button @click="resetQuery">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-row>
      </a-form>
    </a-card>

    <!-- 模板列表 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>排班模板</span>
          <a-tag v-if="selectedRowKeys.length > 0" color="blue">
            已选 {{ selectedRowKeys.length }} 项
          </a-tag>
        </a-space>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增模板
          </a-button>
          <a-button @click="handleBatchEnable" :disabled="selectedRowKeys.length === 0">
            <template #icon><CheckCircleOutlined /></template>
            批量启用
          </a-button>
          <a-button @click="handleBatchDisable" :disabled="selectedRowKeys.length === 0" danger>
            <template #icon><StopOutlined /></template>
            批量禁用
          </a-button>
          <a-button @click="handleBatchDelete" :disabled="selectedRowKeys.length === 0">
            <template #icon><DeleteOutlined /></template>
            批量删除
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        :row-selection="rowSelection"
        row-key="templateId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'templateType'">
            <a-tag :color="getTemplateTypeColor(record.templateType)">
              {{ record.templateTypeDesc }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="record.status === 1 ? 'success' : 'error'"
              :text="record.status === 1 ? '启用' : '禁用'"
            />
          </template>
          <template v-else-if="column.key === 'cycleType'">
            <a-tag>{{ getCycleTypeText(record.templateConfig.cycleType) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleCopy(record)">复制</a-button>
              <a-button type="link" size="small" @click="handleApply(record)">应用</a-button>
              <a-button
                type="link"
                size="small"
                @click="handleToggleStatus(record)"
              >
                {{ record.status === 1 ? '禁用' : '启用' }}
              </a-button>
              <a-button type="link" size="small" @click="handleDelete(record)" danger>删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 模板表单对话框 -->
    <TemplateFormModal
      v-model:visible="formModalVisible"
      :template-id="currentTemplateId"
      @success="handleFormSuccess"
    />

    <!-- 模板应用对话框 -->
    <TemplateApplyModal
      v-model:visible="applyModalVisible"
      :template-id="currentTemplateId"
      @success="handleApplySuccess"
    />

    <!-- 模板详情抽屉 -->
    <a-drawer
      v-model:open="detailVisible"
      title="模板详情"
      width="700"
    >
      <a-descriptions :column="2" bordered size="small" v-if="currentTemplate">
        <a-descriptions-item label="模板名称" :span="2">
          {{ currentTemplate.templateName }}
        </a-descriptions-item>
        <a-descriptions-item label="模板类型">
          <a-tag :color="getTemplateTypeColor(currentTemplate.templateType)">
            {{ currentTemplate.templateTypeDesc }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-badge
            :status="currentTemplate.status === 1 ? 'success' : 'error'"
            :text="currentTemplate.status === 1 ? '启用' : '禁用'"
          />
        </a-descriptions-item>
        <a-descriptions-item label="适用部门" :span="2">
          {{ currentTemplate.departmentName || '全部部门' }}
        </a-descriptions-item>
        <a-descriptions-item label="周期类型">
          {{ getCycleTypeText(currentTemplate.templateConfig.cycleType) }}
        </a-descriptions-item>
        <a-descriptions-item label="班次数量">
          {{ currentTemplate.templateConfig.shiftPattern?.length || 0 }} 个
        </a-descriptions-item>
        <a-descriptions-item label="适用员工" :span="2">
          {{ currentTemplate.applicableEmployees }} 人
        </a-descriptions-item>
        <a-descriptions-item label="应用次数" :span="2">
          {{ currentTemplate.applyCount }} 次
        </a-descriptions-item>
        <a-descriptions-item label="最后应用" :span="2">
          {{ currentTemplate.lastAppliedTime || '未应用' }}
        </a-descriptions-item>
        <a-descriptions-item label="版本号" :span="2">
          {{ currentTemplate.templateVersion }}
        </a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">
          {{ currentTemplate.description || '无' }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ currentTemplate.createTime }}
        </a-descriptions-item>
        <a-descriptions-item label="创建人">
          {{ currentTemplate.createUserName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ currentTemplate.updateTime }}
        </a-descriptions-item>
        <a-descriptions-item label="更新人">
          {{ currentTemplate.updateUserName || '-' }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 班次模式展示 -->
      <a-divider orientation="left">班次模式</a-divider>
      <a-timeline v-if="currentTemplate?.templateConfig?.shiftPattern">
        <a-timeline-item
          v-for="pattern in currentTemplate.templateConfig.shiftPattern"
          :key="pattern.dayOfWeek"
        >
          <template #dot>
            <ClockCircleOutlined style="font-size: 16px;" />
          </template>
          <div>
            <div style="font-weight: 600; margin-bottom: 4px;">
              {{ getDayOfWeekText(pattern.dayOfWeek) }}
            </div>
            <a-tag color="blue">{{ pattern.shiftName }}</a-tag>
            <span v-if="pattern.requiredEmployees" style="margin-left: 8px;">
              需要 {{ pattern.requiredEmployees }} 人
            </span>
            <div v-if="pattern.optionalEmployees && pattern.optionalEmployees.length > 0" style="margin-top: 4px; color: #8c8c8c;">
              可选员工: {{ pattern.optionalEmployees.join(', ') }}
            </div>
          </div>
        </a-timeline-item>
      </a-timeline>
      <a-empty v-else description="暂无班次模式" />
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  CheckCircleOutlined,
  StopOutlined,
  DeleteOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue';
import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
import { smartSentry } from '/@/lib/smart-sentry';
import { scheduleApi, type TemplateVO, TemplateQueryForm } from '@/api/business/attendance/schedule';
import TemplateFormModal from './TemplateFormModal.vue';
import TemplateApplyModal from './TemplateApplyModal.vue';

/**
 * 组件状态
 */
const queryFormState = {
  templateName: null,
  templateType: null,
  departmentId: null,
  status: null,
  pageNum: 1,
  pageSize: PAGE_SIZE,
};

const queryForm = reactive({ ...queryFormState });
const tableData = ref<TemplateVO[]>([]);
const loading = ref(false);
const selectedRowKeys = ref<number[]>([]);

const pagination = reactive({
  current: 1,
  pageSize: PAGE_SIZE,
  total: 0,
  showSizeChanger: true,
  pageSizeOptions: PAGE_SIZE_OPTIONS,
  showTotal: (total: number) => `共 ${total} 条记录`,
});

const columns = [
  { title: '模板名称', dataIndex: 'templateName', key: 'templateName', width: 180 },
  { title: '模板类型', key: 'templateType', width: 120 },
  { title: '部门', dataIndex: 'departmentName', key: 'departmentName', width: 140 },
  { title: '周期类型', key: 'cycleType', width: 120 },
  { title: '适用员工', dataIndex: 'applicableEmployees', key: 'applicableEmployees', width: 100 },
  { title: '应用次数', dataIndex: 'applyCount', key: 'applyCount', width: 100 },
  { title: '状态', key: 'status', width: 100 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 160 },
  { title: '操作', key: 'action', width: 280, fixed: 'right' },
];

const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys: number[]) => {
    selectedRowKeys.value = keys;
  },
};

// 模态框状态
const formModalVisible = ref(false);
const applyModalVisible = ref(false);
const detailVisible = ref(false);
const currentTemplateId = ref<number | undefined>();
const currentTemplate = ref<TemplateVO | null>(null);

/**
 * 模板类型颜色映射
 */
const getTemplateTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    DEPARTMENT: 'blue',
    POSITION: 'green',
    PERSONAL: 'orange',
    GLOBAL: 'purple',
  };
  return colorMap[type] || 'default';
};

/**
 * 周期类型文本映射
 */
const getCycleTypeText = (cycleType: string) => {
  const textMap: Record<string, string> = {
    WEEKLY: '每周',
    MONTHLY: '每月',
    CUSTOM: '自定义',
  };
  return textMap[cycleType] || cycleType;
};

/**
 * 星期文本映射
 */
const getDayOfWeekText = (dayOfWeek: number) => {
  const texts = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  return texts[dayOfWeek] || `${dayOfWeek}`;
};

/**
 * 查询模板列表
 */
const queryTemplates = async () => {
  loading.value = true;
  try {
    const params: TemplateQueryForm = {
      templateName: queryForm.templateName,
      templateType: queryForm.templateType,
      departmentId: queryForm.departmentId,
      status: queryForm.status,
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
    };

    const result = await scheduleApi.getTemplates(params);

    if (result.data) {
      tableData.value = result.data.list || [];
      pagination.total = result.data.total || 0;
    }
  } catch (error) {
    smartSentry.captureError(error);
    message.error('加载模板列表失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 重置查询
 */
const resetQuery = () => {
  Object.assign(queryForm, queryFormState);
  selectedRowKeys.value = [];
  queryTemplates();
};

/**
 * 表格变化处理
 */
const handleTableChange = (pag: any) => {
  queryForm.pageNum = pag.current;
  queryForm.pageSize = pag.pageSize;
  queryTemplates();
};

/**
 * 新增模板
 */
const handleAdd = () => {
  currentTemplateId.value = undefined;
  formModalVisible.value = true;
};

/**
 * 查看模板
 */
const handleView = (record: TemplateVO) => {
  currentTemplate.value = record;
  detailVisible.value = true;
};

/**
 * 编辑模板
 */
const handleEdit = (record: TemplateVO) => {
  currentTemplateId.value = record.templateId;
  formModalVisible.value = true;
};

/**
 * 复制模板
 */
const handleCopy = async (record: TemplateVO) => {
  try {
    const result = await scheduleApi.copyTemplate(record.templateId, `${record.templateName} (副本)`);
    if (result.data) {
      message.success('复制模板成功');
      queryTemplates();
    }
  } catch (error) {
    console.error('复制模板失败:', error);
    message.error('复制模板失败');
  }
};

/**
 * 应用模板
 */
const handleApply = (record: TemplateVO) => {
  currentTemplateId.value = record.templateId;
  applyModalVisible.value = true;
};

/**
 * 切换状态
 */
const handleToggleStatus = async (record: TemplateVO) => {
  const newStatus = record.status === 1 ? 0 : 1;
  const statusText = newStatus === 1 ? '启用' : '禁用';

  try {
    await scheduleApi.updateTemplateStatus(record.templateId, newStatus);
    message.success(`${statusText}成功`);
    queryTemplates();
  } catch (error) {
    console.error('切换状态失败:', error);
    message.error(`${statusText}失败`);
  }
};

/**
 * 删除模板
 */
const handleDelete = (record: TemplateVO) => {
  Modal.confirm({
    title: '删除模板',
    content: `确定要删除模板 "${record.templateName}" 吗？`,
    onOk: async () => {
      try {
        await scheduleApi.deleteTemplate(record.templateId);
        message.success('删除成功');
        queryTemplates();
      } catch (error) {
        console.error('删除模板失败:', error);
        message.error('删除模板失败');
      }
    },
  });
};

/**
 * 批量启用
 */
const handleBatchEnable = async () => {
  if (!selectedRowKeys.value.length) {
    message.warning('请至少选择一个模板');
    return;
  }

  try {
    await scheduleApi.updateTemplateStatus(selectedRowKeys.value[0], 1);
    message.success('批量启用成功');
    selectedRowKeys.value = [];
    queryTemplates();
  } catch (error) {
    console.error('批量启用失败:', error);
    message.error('批量启用失败');
  }
};

/**
 * 批量禁用
 */
const handleBatchDisable = async () => {
  if (!selectedRowKeys.value.length) {
    message.warning('请至少选择一个模板');
    return;
  }

  try {
    await scheduleApi.updateTemplateStatus(selectedRowKeys.value[0], 0);
    message.success('批量禁用成功');
    selectedRowKeys.value = [];
    queryTemplates();
  } catch (error) {
    console.error('批量禁用失败:', error);
    message.error('批量禁用失败');
  }
};

/**
 * 批量删除
 */
const handleBatchDelete = () => {
  if (!selectedRowKeys.value.length) {
    message.warning('请至少选择一个模板');
    return;
  }

  Modal.confirm({
    title: '批量删除模板',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个模板吗？`,
    onOk: async () => {
      try {
        await scheduleApi.batchDeleteTemplates(selectedRowKeys.value);
        message.success('批量删除成功');
        selectedRowKeys.value = [];
        queryTemplates();
      } catch (error) {
        console.error('批量删除失败:', error);
        message.error('批量删除失败');
      }
    },
  });
};

/**
 * 表单提交成功
 */
const handleFormSuccess = () => {
  queryTemplates();
};

/**
 * 模板应用成功
 */
const handleApplySuccess = () => {
  message.success('模板应用成功');
};

onMounted(() => {
  queryTemplates();
});
</script>

<style lang="less" scoped>
.template-management {
  .query-card {
    margin-bottom: 16px;
  }
}
</style>
