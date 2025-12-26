<!--
  @fileoverview 时间段管理主页面
  @author IOE-DREAM Team
  @description 打卡时间段配置管理，支持新增、编辑、删除、启用/禁用等操作
-->
<template>
  <div class="time-period-management">
    <a-card :bordered="false">
      <!-- 搜索表单 -->
      <a-form class="search-form" :model="queryForm" layout="inline">
        <a-form-item label="时间段名称">
          <a-input
            v-model:value="queryForm.periodName"
            placeholder="请输入时间段名称"
            allow-clear
            @pressEnter="handleQuery"
          />
        </a-form-item>
        <a-form-item label="时间段类型">
          <a-select
            v-model:value="queryForm.periodType"
            placeholder="请选择时间段类型"
            allow-clear
            style="width: 180px"
          >
            <a-select-option
              v-for="(name, type) in TimePeriodTypeNames"
              :key="type"
              :value="type"
            >
              {{ name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 操作按钮 -->
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增时间段
          </a-button>
          <a-button
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
          >
            <template #icon><DeleteOutlined /></template>
            批量删除
          </a-button>
          <a-button @click="loadData" :loading="loading">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </div>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        row-key="periodId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 时间段类型列 -->
          <template v-if="column.key === 'periodType'">
            <a-tag :color="getPeriodTypeColor(record.periodType)">
              {{ record.periodTypeName }}
            </a-tag>
          </template>

          <!-- 时间段列 -->
          <template v-else-if="column.key === 'timeRange'">
            <span class="time-range">
              {{ record.startTime }} ~ {{ record.endTime }}
            </span>
          </template>

          <!-- 弹性时间列 -->
          <template v-else-if="column.key === 'flexibleTime'">
            <span v-if="record.isFlexible">
              提前 {{ record.allowEarlyMinutes }} 分钟 / 延后 {{ record.allowLateMinutes }} 分钟
            </span>
            <span v-else>-</span>
          </template>

          <!-- 关联班次列 -->
          <template v-else-if="column.key === 'shiftName'">
            <a-tag v-if="record.shiftName" color="blue">{{ record.shiftName }}</a-tag>
            <span v-else>-</span>
          </template>

          <!-- 启用状态列 -->
          <template v-else-if="column.key === 'enabled'">
            <a-switch
              :checked="record.enabled"
              :loading="record.switching"
              @change="(checked) => handleToggleEnabled(record, checked)"
            />
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除这个时间段吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record)"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑对话框 -->
    <TimePeriodFormModal
      v-model:visible="formModalVisible"
      :period-id="editingPeriodId"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue';
import {
  timePeriodApi,
  type TimePeriodQueryForm,
  type TimePeriodVO,
  TimePeriodType,
  TimePeriodTypeNames
} from '@/api/business/attendance/time-period';
import TimePeriodFormModal from './components/TimePeriodFormModal.vue';

// 查询表单
const queryForm = reactive<TimePeriodQueryForm>({
  periodName: undefined,
  periodType: undefined,
  enabled: undefined,
  pageNum: 1,
  pageSize: 20
});

// 数据源
const dataSource = ref<TimePeriodVO[]>([]);
const loading = ref(false);
const selectedRowKeys = ref<number[]>([]);

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条记录`
});

// 表格列配置
const columns = [
  {
    title: '时间段名称',
    dataIndex: 'periodName',
    key: 'periodName',
    width: 200
  },
  {
    title: '时间段类型',
    key: 'periodType',
    width: 180
  },
  {
    title: '时间段',
    key: 'timeRange',
    width: 200
  },
  {
    title: '弹性时间',
    key: 'flexibleTime',
    width: 250
  },
  {
    title: '关联班次',
    key: 'shiftName',
    width: 150
  },
  {
    title: '排序',
    dataIndex: 'sortOrder',
    key: 'sortOrder',
    width: 80,
    align: 'center'
  },
  {
    title: '状态',
    key: 'enabled',
    width: 80,
    align: 'center'
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right',
    align: 'center'
  }
];

// 行选择配置
const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys: number[]) => {
    selectedRowKeys.value = keys;
  }
};

// 表单对话框
const formModalVisible = ref(false);
const editingPeriodId = ref<number>();

// 方法
const loadData = async () => {
  loading.value = true;
  try {
    const res = await timePeriodApi.queryPage({
      ...queryForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    });

    if (res.data) {
      dataSource.value = res.data.list || [];
      pagination.total = res.data.total || 0;
    }
  } catch (error) {
    console.error('加载时间段数据失败:', error);
    message.error('加载时间段数据失败');
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  pagination.current = 1;
  loadData();
};

const handleReset = () => {
  queryForm.periodName = undefined;
  queryForm.periodType = undefined;
  queryForm.enabled = undefined;
  pagination.current = 1;
  loadData();
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadData();
};

const handleAdd = () => {
  editingPeriodId.value = undefined;
  formModalVisible.value = true;
};

const handleEdit = (record: TimePeriodVO) => {
  editingPeriodId.value = record.periodId;
  formModalVisible.value = true;
};

const handleDelete = async (record: TimePeriodVO) => {
  try {
    await timePeriodApi.delete(record.periodId);
    message.success('删除成功');
    loadData();
  } catch (error) {
    console.error('删除时间段失败:', error);
    message.error('删除时间段失败');
  }
};

const handleBatchDelete = async () => {
  try {
    await timePeriodApi.batchDelete(selectedRowKeys.value);
    message.success('批量删除成功');
    selectedRowKeys.value = [];
    loadData();
  } catch (error) {
    console.error('批量删除失败:', error);
    message.error('批量删除失败');
  }
};

const handleToggleEnabled = async (record: TimePeriodVO, checked: boolean) => {
  record.switching = true;
  try {
    await timePeriodApi.updateEnabled(record.periodId, checked);
    record.enabled = checked;
    message.success(checked ? '已启用' : '已禁用');
  } catch (error) {
    console.error('更新状态失败:', error);
    message.error('更新状态失败');
    record.enabled = !checked;
  } finally {
    record.switching = false;
  }
};

const handleFormSuccess = () => {
  formModalVisible.value = false;
  loadData();
};

const getPeriodTypeColor = (type: TimePeriodType) => {
  const colors: Record<TimePeriodType, string> = {
    [TimePeriodType.MORNING_CHECK_IN]: 'orange',
    [TimePeriodType.MORNING_CHECK_OUT]: 'gold',
    [TimePeriodType.AFTERNOON_CHECK_IN]: 'cyan',
    [TimePeriodType.AFTERNOON_CHECK_OUT]: 'blue',
    [TimePeriodType.OVERTIME_CHECK_IN]: 'purple',
    [TimePeriodType.OVERTIME_CHECK_OUT]: 'magenta',
    [TimePeriodType.NIGHT_SHIFT_CHECK_IN]: 'volcano',
    [TimePeriodType.NIGHT_SHIFT_CHECK_OUT]: 'red'
  };
  return colors[type] || 'default';
};

// 生命周期
onMounted(() => {
  loadData();
});
</script>

<style scoped lang="less">
.time-period-management {
  .search-form {
    margin-bottom: 16px;
  }

  .table-actions {
    margin-bottom: 16px;
  }

  .time-range {
    white-space: nowrap;
  }
}
</style>
