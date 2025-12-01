/*
 * 解码器管理页面
 *
 * @Author:    Claude Code
 * @Date:      2024-11-04
 * @Copyright  1024创新实验室
 */
<template>
  <div class="decoder-management">
    <!-- 搜索工具栏 -->
    <div class="smart-query-table-form">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-form-item label="解码器名称">
            <a-input v-model:value="queryForm.decoderName" placeholder="请输入解码器名称" allowClear />
          </a-form-item>
        </a-col>
        <a-col :span="4">
          <a-form-item label="状态">
            <a-select v-model:value="queryForm.status" placeholder="请选择状态" allowClear>
              <a-select-option value="">全部</a-select-option>
              <a-select-option :value="1">在线</a-select-option>
              <a-select-option :value="0">离线</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="4">
          <a-form-item label="厂商">
            <a-select v-model:value="queryForm.manufacturer" placeholder="请选择厂商" allowClear>
              <a-select-option value="">全部厂商</a-select-option>
              <a-select-option value="hikvision">海康威视</a-select-option>
              <a-select-option value="dahua">大华</a-select-option>
              <a-select-option value="uniview">宇视</a-select-option>
              <a-select-option value="tiandy">天地伟业</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="10">
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="queryData">
                <template #icon><SearchOutlined /></template>
                查询
              </a-button>
              <a-button @click="resetQuery">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-col>
      </a-row>
    </div>

    <!-- 操作按钮组 -->
    <div class="smart-query-table-operation">
      <a-space>
        <a-button type="primary" @click="showAddModal">
          <template #icon><PlusOutlined /></template>
          新增解码器
        </a-button>
        <a-button danger @click="batchDelete" :disabled="!hasSelected">
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
        <a-button @click="refreshData">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </a-space>
    </div>

    <!-- 统计信息 -->
    <DecoderStatistics :statistics="statistics" />

    <!-- 解码器列表 -->
    <a-card class="smart-margin-top10">
      <a-table
        :columns="columns"
        :data-source="decoderList"
        :pagination="pagination"
        :loading="loading"
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        :scroll="{ x: 1400 }"
        row-key="id"
        bordered
        @change="handleTableChange"
        class="decoder-table"
      >
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'error'">
              {{ record.status === 1 ? '在线' : '离线' }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'cpuUsage'">
            <a-progress
              :percent="record.cpuUsage"
              size="small"
              :status="record.cpuUsage > 80 ? 'exception' : 'normal'"
            />
          </template>
          <template v-else-if="column.dataIndex === 'memoryUsage'">
            <a-progress
              :percent="record.memoryUsage"
              size="small"
              :status="record.memoryUsage > 80 ? 'exception' : 'normal'"
            />
          </template>
          <template v-else-if="column.dataIndex === 'manufacturer'">
            {{ getManufacturerText(record.manufacturer) }}
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <div class="smart-table-operate">
              <a-button type="link" size="small" @click="editDecoder(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-button type="link" size="small" @click="viewChannels(record)">
                <template #icon><EyeOutlined /></template>
                查看通道
              </a-button>
              <a-button type="link" size="small" @click="restartDecoder(record)">
                <template #icon><ReloadOutlined /></template>
                重启
              </a-button>
              <a-popconfirm
                title="确定要删除该解码器吗？"
                @confirm="deleteDecoder(record)"
              >
                <a-button type="link" size="small" danger>
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 解码器表单弹窗 -->
    <DecoderForm
      ref="decoderFormRef"
      :visible="modalVisible"
      :is-edit="isEdit"
      :form-data="formData"
      @cancel="handleModalCancel"
      @submit="handleModalSubmit"
    />

    <!-- 通道列表抽屉 -->
    <ChannelList
      ref="channelListRef"
      :visible="channelDrawerVisible"
      :decoder-info="currentDecoder"
      @close="channelDrawerVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  DesktopOutlined,
  CheckCircleOutlined,
  VideoCameraOutlined,
  PieChartOutlined,
} from '@ant-design/icons-vue';
import DecoderStatistics from './components/DecoderStatistics.vue';
import DecoderForm from './components/DecoderForm.vue';
import ChannelList from './components/ChannelList.vue';
import { useDecoderStore } from '/@/store/modules/business/decoder';

// 解码器状态管理
const decoderStore = useDecoderStore();

// 查询表单
const queryForm = reactive({
  decoderName: '',
  status: undefined,
  manufacturer: '',
});

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: [10, 20, 50, 100],
  showTotal: (total) => `共 ${total} 条`,
});

// 表格选中
const selectedRowKeys = ref([]);
const hasSelected = computed(() => selectedRowKeys.value.length > 0);

// 弹窗状态
const modalVisible = ref(false);
const isEdit = ref(false);
const formData = ref({});
const channelDrawerVisible = ref(false);
const currentDecoder = ref({});

// 表格列配置
const columns = [
  {
    title: '解码器名称',
    dataIndex: 'decoderName',
    key: 'decoderName',
    width: 200,
    ellipsis: true,
  },
  {
    title: 'IP地址',
    dataIndex: 'ipAddress',
    key: 'ipAddress',
    width: 120,
  },
  {
    title: '厂商',
    dataIndex: 'manufacturer',
    key: 'manufacturer',
    width: 100,
  },
  {
    title: '型号',
    dataIndex: 'model',
    key: 'model',
    width: 150,
    ellipsis: true,
  },
  {
    title: '通道数',
    dataIndex: 'channelCount',
    key: 'channelCount',
    width: 80,
  },
  {
    title: '已用通道',
    dataIndex: 'usedChannels',
    key: 'usedChannels',
    width: 100,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80,
  },
  {
    title: 'CPU使用率',
    dataIndex: 'cpuUsage',
    key: 'cpuUsage',
    width: 120,
  },
  {
    title: '内存使用率',
    dataIndex: 'memoryUsage',
    key: 'memoryUsage',
    width: 120,
  },
  {
    title: '最后心跳',
    dataIndex: 'lastHeartbeat',
    key: 'lastHeartbeat',
    width: 150,
  },
  {
    title: '操作',
    dataIndex: 'action',
    key: 'action',
    width: 280,
    fixed: 'right',
  },
];

// 获取厂商文本
const getManufacturerText = (manufacturer) => {
  const manufacturerMap = {
    hikvision: '海康威视',
    dahua: '大华',
    uniview: '宇视',
    tiandy: '天地伟业',
  };
  return manufacturerMap[manufacturer] || manufacturer;
};

// 获取解码器列表数据
const decoderList = computed(() => decoderStore.decoderList);
const loading = computed(() => decoderStore.loading);
const statistics = computed(() => decoderStore.statistics);

// 表格选择变化
const onSelectChange = (keys) => {
  selectedRowKeys.value = keys;
};

// 查询数据
const queryData = async () => {
  pagination.current = 1;
  await fetchDecoderList();
};

// 重置查询
const resetQuery = () => {
  Object.assign(queryForm, {
    decoderName: '',
    status: undefined,
    manufacturer: '',
  });
  queryData();
};

// 刷新数据
const refreshData = () => {
  fetchDecoderList();
};

// 表格变化处理
const handleTableChange = (pag) => {
  Object.assign(pagination, pag);
  fetchDecoderList();
};

// 获取解码器列表
const fetchDecoderList = async () => {
  const params = {
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
    ...queryForm,
  };

  try {
    await decoderStore.fetchDecoderList(params);
    pagination.total = decoderStore.statistics.totalCount || 0;
  } catch (error) {
    message.error('获取解码器列表失败');
  }
};

// 显示新增弹窗
const showAddModal = () => {
  isEdit.value = false;
  formData.value = {
    decoderName: '',
    ipAddress: '',
    port: 37777,
    username: '',
    password: '',
    manufacturer: '',
    model: '',
    channelCount: 16,
    description: '',
  };
  modalVisible.value = true;
};

// 编辑解码器
const editDecoder = (record) => {
  isEdit.value = true;
  formData.value = { ...record };
  modalVisible.value = true;
};

// 查看通道
const viewChannels = (record) => {
  currentDecoder.value = record;
  channelDrawerVisible.value = true;
};

// 重启解码器
const restartDecoder = (record) => {
  modal.confirm({
    title: '重启确认',
    content: `确定要重启解码器"${record.decoderName}"吗？重启需要几分钟时间。`,
    onOk: async () => {
      try {
        const result = await decoderStore.restartDecoder(record.id);
        if (result) {
          message.success('重启命令已发送');
          setTimeout(() => {
            fetchDecoderList();
          }, 3000);
        }
      } catch (error) {
        message.error('重启失败');
      }
    },
  });
};

// 删除解码器
const deleteDecoder = async (record) => {
  try {
    const result = await decoderStore.deleteDecoder(record.id);
    if (result) {
      message.success('删除成功');
      fetchDecoderList();
    }
  } catch (error) {
    message.error('删除失败');
  }
};

// 批量删除
const batchDelete = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的解码器');
    return;
  }

  modal.confirm({
    title: '批量删除确认',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个解码器吗？`,
    onOk: async () => {
      try {
        const results = await Promise.all(
          selectedRowKeys.value.map(id => decoderStore.deleteDecoder(id))
        );
        const successCount = results.filter(Boolean).length;
        if (successCount > 0) {
          message.success(`成功删除 ${successCount} 个解码器`);
          selectedRowKeys.value = [];
          fetchDecoderList();
        }
      } catch (error) {
        message.error('批量删除失败');
      }
    },
  });
};

// 弹窗取消
const handleModalCancel = () => {
  modalVisible.value = false;
};

// 弹窗提交
const handleModalSubmit = async (values) => {
  try {
    if (isEdit.value) {
      await decoderStore.updateDecoder(values);
      message.success('更新成功');
    } else {
      await decoderStore.addDecoder(values);
      message.success('添加成功');
    }
    modalVisible.value = false;
    fetchDecoderList();
  } catch (error) {
    message.error(isEdit.value ? '更新失败' : '添加失败');
  }
};

// 初始化
onMounted(() => {
  fetchDecoderList();
});
</script>

<style lang="less" scoped>
.decoder-management {
  padding: 24px;
  height: 100%;
  overflow-y: auto;

  .decoder-table {
    :deep(.ant-table-thead > tr > th) {
      background: #fafafa;
      font-weight: 500;
    }

    :deep(.ant-table-tbody > tr:hover > td) {
      background: #f5f5f5;
    }

    :deep(.ant-progress-line) {
      margin-bottom: 0;
    }
  }
}

.smart-margin-top10 {
  margin-top: 10px;
}
</style>