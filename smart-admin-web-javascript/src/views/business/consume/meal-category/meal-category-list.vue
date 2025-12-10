<!--
  * Meal Category List
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
-->
<template>
  <div class="meal-category-list-page">
    <a-card size="small" :bordered="false" class="query-card">
      <a-form class="smart-query-form" layout="inline">
        <a-row class="smart-query-form-row" :gutter="[16, 16]">
          <a-form-item label="Keyword" class="smart-query-form-item">
            <a-input
              v-model:value="queryForm.keyword"
              placeholder="Name / type"
              style="width: 200px"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="Type" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.mealType"
              placeholder="Select type"
              style="width: 150px"
              :allow-clear="true"
            >
              <a-select-option value="BREAKFAST">Breakfast</a-select-option>
              <a-select-option value="LUNCH">Lunch</a-select-option>
              <a-select-option value="DINNER">Dinner</a-select-option>
              <a-select-option value="SNACK">Snack</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="Status" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.status"
              placeholder="Select status"
              style="width: 120px"
              :allow-clear="true"
            >
              <a-select-option :value="1">Enabled</a-select-option>
              <a-select-option :value="0">Disabled</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-space>
              <a-button type="primary" @click="queryCategoryList">
                <template #icon><SearchOutlined /></template>
                Search
              </a-button>
              <a-button @click="resetQuery">
                <template #icon><ReloadOutlined /></template>
                Reset
              </a-button>
            </a-space>
          </a-form-item>
        </a-row>
      </a-form>
    </a-card>

    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>Meal Categories</span>
          <a-tag v-if="selectedRowKeys.length > 0" color="blue">
            Selected {{ selectedRowKeys.length }}
          </a-tag>
        </a-space>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="openForm(false)">
            <template #icon><PlusOutlined /></template>
            New Category
          </a-button>
          <a-button @click="handleBatchEnable" :disabled="selectedRowKeys.length === 0">
            <template #icon><CheckCircleOutlined /></template>
            Enable
          </a-button>
          <a-button @click="handleBatchDisable" :disabled="selectedRowKeys.length === 0" danger>
            <template #icon><StopOutlined /></template>
            Disable
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        :row-selection="rowSelection"
        row-key="categoryId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-badge
              :status="record.status === 1 ? 'success' : 'error'"
              :text="record.status === 1 ? 'Enabled' : 'Disabled'"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openForm(true, record)">Edit</a-button>
              <a-button type="link" size="small" @click="handleDelete(record)" danger>Delete</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <MealCategoryFormModal
      v-model:visible="formVisible"
      :form-data="currentCategory"
      :is-edit="isEdit"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  CheckCircleOutlined,
  StopOutlined,
} from '@ant-design/icons-vue';
import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
import { smartSentry } from '/@/lib/smart-sentry';
import MealCategoryFormModal from './components/MealCategoryFormModal.vue';

const queryFormState = {
  pageNum: 1,
  pageSize: PAGE_SIZE,
  keyword: null,
  mealType: null,
  status: null,
};

const queryForm = reactive({ ...queryFormState });
const tableData = ref([]);
const loading = ref(false);
const selectedRowKeys = ref([]);
const formVisible = ref(false);
const isEdit = ref(false);
const currentCategory = ref(null);

const pagination = reactive({
  current: 1,
  pageSize: PAGE_SIZE,
  total: 0,
  showSizeChanger: true,
  pageSizeOptions: PAGE_SIZE_OPTIONS,
  showTotal: (total) => `Total ${total} items`,
});

const columns = [
  { title: 'Name', dataIndex: 'categoryName', key: 'categoryName', width: 200 },
  { title: 'Type', dataIndex: 'mealType', key: 'mealType', width: 120 },
  { title: 'Time', dataIndex: 'timeRange', key: 'timeRange', width: 140 },
  { title: 'Price Range', dataIndex: 'priceRange', key: 'priceRange', width: 160 },
  { title: 'Areas', dataIndex: 'areaNames', key: 'areaNames', width: 200 },
  { title: 'Daily Limit', dataIndex: 'dailyLimit', key: 'dailyLimit', width: 120 },
  { title: 'Status', key: 'status', width: 100 },
  { title: 'Updated At', dataIndex: 'updateTime', key: 'updateTime', width: 160 },
  { title: 'Action', key: 'action', width: 160, fixed: 'right' },
];

const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  },
};

const mockData = () => {
  const now = dayjs();
  return Array.from({ length: 12 }).map((_, idx) => ({
    categoryId: idx + 1,
    categoryName: `Category ${idx + 1}`,
    mealType: ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'][idx % 4],
    timeRange: '07:00 - 10:00',
    priceRange: '¥10.00 ~ ¥30.00',
    areaNames: 'Building A, Building B',
    dailyLimit: 50 + idx,
    status: idx % 3 === 0 ? 0 : 1,
    updateTime: now.subtract(idx, 'day').format('YYYY-MM-DD'),
  }));
};

const queryCategoryList = async () => {
  loading.value = true;
  try {
    tableData.value = mockData();
    pagination.total = tableData.value.length;
  } catch (error) {
    smartSentry.captureError(error);
    message.error('Failed to load categories');
  } finally {
    loading.value = false;
  }
};

const resetQuery = () => {
  Object.assign(queryForm, queryFormState);
  selectedRowKeys.value = [];
  queryCategoryList();
};

const handleTableChange = (pag) => {
  queryForm.pageNum = pag.current;
  queryForm.pageSize = pag.pageSize;
  queryCategoryList();
};

const openForm = (edit = false, record = null) => {
  isEdit.value = edit;
  currentCategory.value = record ? { ...record } : null;
  formVisible.value = true;
};

const handleFormSuccess = (data) => {
  if (isEdit.value && currentCategory.value) {
    const idx = tableData.value.findIndex((item) => item.categoryId === currentCategory.value.categoryId);
    if (idx !== -1) {
      tableData.value[idx] = { ...tableData.value[idx], ...data };
    }
  } else {
    tableData.value.unshift({
      ...data,
      categoryId: Date.now(),
      timeRange: `${data.startTime || '--'} - ${data.endTime || '--'}`,
      priceRange: `¥${(data.minPrice || 0).toFixed(2)} ~ ¥${(data.maxPrice || 0).toFixed(2)}`,
      updateTime: dayjs().format('YYYY-MM-DD'),
    });
  }
  queryCategoryList();
};

const handleDelete = (record) => {
  Modal.confirm({
    title: 'Delete category',
    content: `Delete ${record.categoryName}?`,
    onOk: () => {
      tableData.value = tableData.value.filter((item) => item.categoryId !== record.categoryId);
      message.success('Deleted');
    },
  });
};

const handleBatchEnable = () => {
  if (!selectedRowKeys.value.length) {
    message.warning('Select at least one category');
    return;
  }
  tableData.value = tableData.value.map((item) =>
    selectedRowKeys.value.includes(item.categoryId) ? { ...item, status: 1 } : item
  );
  message.success('Enabled');
};

const handleBatchDisable = () => {
  if (!selectedRowKeys.value.length) {
    message.warning('Select at least one category');
    return;
  }
  tableData.value = tableData.value.map((item) =>
    selectedRowKeys.value.includes(item.categoryId) ? { ...item, status: 0 } : item
  );
  message.success('Disabled');
};

onMounted(() => {
  queryCategoryList();
});
</script>

<style lang="less" scoped>
.meal-category-list-page {
  .query-card {
    margin-bottom: 16px;
  }
}
</style>
