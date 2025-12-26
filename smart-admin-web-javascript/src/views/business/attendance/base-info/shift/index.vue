<!--
  @fileoverview 班次管理主页面
  @author IOE-DREAM Team
  @description 班次基础信息管理，支持新增、编辑、删除、启用/禁用等操作
-->
<template>
  <div class="shift-management">
    <a-card :bordered="false">
      <!-- 搜索表单 -->
      <a-form class="search-form" :model="queryForm" layout="inline">
        <a-form-item label="班次名称">
          <a-input
            v-model:value="queryForm.shiftName"
            placeholder="请输入班次名称"
            allow-clear
            @pressEnter="handleQuery"
          />
        </a-form-item>
        <a-form-item label="班次类型">
          <a-select
            v-model:value="queryForm.shiftType"
            placeholder="请选择班次类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option
              v-for="(name, type) in ShiftTypeNames"
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
            新增班次
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
        row-key="shiftId"
        @change="handleTableChange"
      >
        <!-- 班次名称列 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'shiftName'">
            <a-space>
              <span
                v-if="record.colorCode"
                class="color-badge"
                :style="{ backgroundColor: record.colorCode }"
              ></span>
              <span>{{ record.shiftName }}</span>
            </a-space>
          </template>

          <!-- 班次类型列 -->
          <template v-else-if="column.key === 'shiftType'">
            <a-tag :color="getShiftTypeColor(record.shiftType)">
              {{ record.shiftTypeName }}
            </a-tag>
          </template>

          <!-- 时间段列 -->
          <template v-else-if="column.key === 'timeRange'">
            <span class="time-range">
              {{ record.startTime }} ~ {{ record.endTime }}
              <a-tag v-if="record.isOvernight" color="blue" size="small" style="margin-left: 8px">
                跨天
              </a-tag>
            </span>
          </template>

          <!-- 工作时长列 -->
          <template v-else-if="column.key === 'workHours'">
            <span>{{ record.workHours }} 小时</span>
            <div v-if="record.breakMinutes" class="break-info">
              休息 {{ record.breakMinutes }} 分钟
            </div>
          </template>

          <!-- 弹性时间列 -->
          <template v-else-if="column.key === 'flexible'">
            <a-tag v-if="record.isFlexible" color="green">
              弹性 {{ record.flexibleStartTime }}~{{ record.flexibleEndTime }} 分钟
            </a-tag>
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
                title="确定要删除这个班次吗？"
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
    <ShiftFormModal
      v-model:visible="formModalVisible"
      :shift-id="editingShiftId"
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
  shiftApi,
  type ShiftQueryForm,
  type ShiftVO,
  ShiftType,
  ShiftTypeNames
} from '@/api/business/attendance/shift';
import ShiftFormModal from './components/ShiftFormModal.vue';

// 查询表单
const queryForm = reactive<ShiftQueryForm>({
  shiftName: undefined,
  shiftType: undefined,
  enabled: undefined,
  pageNum: 1,
  pageSize: 20
});

// 数据源
const dataSource = ref<ShiftVO[]>([]);
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
    title: '班次名称',
    key: 'shiftName',
    width: 200,
    dataIndex: 'shiftName'
  },
  {
    title: '班次类型',
    key: 'shiftType',
    width: 120,
    dataIndex: 'shiftType'
  },
  {
    title: '时间段',
    key: 'timeRange',
    width: 200
  },
  {
    title: '工作时长',
    key: 'workHours',
    width: 150,
    dataIndex: 'workHours'
  },
  {
    title: '弹性时间',
    key: 'flexible',
    width: 150
  },
  {
    title: '排序',
    dataIndex: 'sortOrder',
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
const editingShiftId = ref<number>();

// 方法
const loadData = async () => {
  loading.value = true;
  try {
    const res = await shiftApi.queryPage({
      ...queryForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    });

    if (res.data) {
      dataSource.value = res.data.list || [];
      pagination.total = res.data.total || 0;
    }
  } catch (error) {
    console.error('加载班次数据失败:', error);
    message.error('加载班次数据失败');
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  pagination.current = 1;
  loadData();
};

const handleReset = () => {
  queryForm.shiftName = undefined;
  queryForm.shiftType = undefined;
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
  editingShiftId.value = undefined;
  formModalVisible.value = true;
};

const handleEdit = (record: ShiftVO) => {
  editingShiftId.value = record.shiftId;
  formModalVisible.value = true;
};

const handleDelete = async (record: ShiftVO) => {
  try {
    await shiftApi.delete(record.shiftId);
    message.success('删除成功');
    loadData();
  } catch (error) {
    console.error('删除班次失败:', error);
    message.error('删除班次失败');
  }
};

const handleBatchDelete = async () => {
  try {
    await shiftApi.batchDelete(selectedRowKeys.value);
    message.success('批量删除成功');
    selectedRowKeys.value = [];
    loadData();
  } catch (error) {
    console.error('批量删除失败:', error);
    message.error('批量删除失败');
  }
};

const handleToggleEnabled = async (record: ShiftVO, checked: boolean) => {
  record.switching = true;
  try {
    await shiftApi.updateEnabled(record.shiftId, checked);
    record.enabled = checked;
    message.success(checked ? '已启用' : '已禁用');
  } catch (error) {
    console.error('更新状态失败:', error);
    message.error('更新状态失败');
    // 恢复原状态
    record.enabled = !checked;
  } finally {
    record.switching = false;
  }
};

const handleFormSuccess = () => {
  formModalVisible.value = false;
  loadData();
};

const getShiftTypeColor = (type: ShiftType) => {
  const colors: Record<ShiftType, string> = {
    [ShiftType.DAY_SHIFT]: 'blue',
    [ShiftType.NIGHT_SHIFT]: 'purple',
    [ShiftType.ROTATING_SHIFT]: 'cyan',
    [ShiftType.FLEXIBLE_SHIFT]: 'green',
    [ShiftType.PART_TIME_SHIFT]: 'orange',
    [ShiftType.SPECIAL_SHIFT]: 'red'
  };
  return colors[type] || 'default';
};

// 生命周期
onMounted(() => {
  loadData();
});
</script>

<style scoped lang="less">
.shift-management {
  .search-form {
    margin-bottom: 16px;
  }

  .table-actions {
    margin-bottom: 16px;
  }

  .color-badge {
    display: inline-block;
    width: 12px;
    height: 12px;
    border-radius: 2px;
    vertical-align: middle;
  }

  .time-range {
    white-space: nowrap;
  }

  .break-info {
    font-size: 12px;
    color: rgba(0, 0, 0, 0.45);
    margin-top: 4px;
  }
}
</style>
