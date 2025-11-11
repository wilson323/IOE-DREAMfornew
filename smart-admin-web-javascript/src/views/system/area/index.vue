<!----区域管理页面------>
<template>
  <div class="area-manage">
    <!-- 搜索区域 -->
    <a-card :bordered="false" size="small" class="search-card">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="区域编码">
          <a-input v-model:value="queryForm.areaCode" placeholder="请输入区域编码" allow-clear style="width: 200px" />
        </a-form-item>
        <a-form-item label="区域名称">
          <a-input v-model:value="queryForm.areaName" placeholder="请输入区域名称" allow-clear style="width: 200px" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryForm.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
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
    </a-card>

    <!-- 树形列表 -->
    <a-card :bordered="false" size="small" class="table-card">
      <div class="table-header">
        <a-space>
          <a-button type="primary" @click="handleAdd" v-privilege="'area:add'">
            <template #icon><PlusOutlined /></template>
            新增区域
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </div>

      <a-table
        :columns="columns"
        :data-source="areaTreeData"
        :loading="loading"
        :pagination="false"
        row-key="areaId"
        :default-expand-all-rows="false"
      >
        <template #bodyCell="{ column, record }">
          <!-- 状态 -->
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)" v-privilege="'area:update'">编辑</a-button>
              <a-popconfirm title="确定删除该区域吗？" @confirm="handleDelete(record.areaId)" v-privilege="'area:delete'">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <AreaModal
      v-model:visible="modalVisible"
      :area-data="currentArea"
      :area-tree="areaTreeData"
      @success="handleModalSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { SearchOutlined, ReloadOutlined, PlusOutlined } from '@ant-design/icons-vue';
import { areaApi } from '/@/api/system/area-api';
import AreaModal from './area-modal.vue';

// 响应式数据
const loading = ref(false);
const modalVisible = ref(false);
const currentArea = ref(null);
const areaTreeData = ref([]);

// 查询表单
const queryForm = reactive({
  areaCode: '',
  areaName: '',
  status: undefined,
});

// 表格列定义
const columns = [
  {
    title: '区域名称',
    dataIndex: 'areaName',
    key: 'areaName',
    width: 200,
  },
  {
    title: '区域编码',
    dataIndex: 'areaCode',
    key: 'areaCode',
    width: 150,
  },
  {
    title: '区域类型',
    dataIndex: 'areaType',
    key: 'areaType',
    width: 120,
  },
  {
    title: '负责人',
    dataIndex: 'managerName',
    key: 'managerName',
    width: 100,
  },
  {
    title: '联系电话',
    dataIndex: 'contactPhone',
    key: 'contactPhone',
    width: 120,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
];

// 方法定义
const loadAreaTree = async () => {
  try {
    loading.value = true;
    const res = await areaApi.getAreaTree();
    if (res.data) {
      areaTreeData.value = filterAreaTree(res.data, queryForm);
    }
  } catch (error) {
    message.error('加载区域树失败');
  } finally {
    loading.value = false;
  }
};

const filterAreaTree = (tree, query) => {
  if (!query.areaCode && !query.areaName && query.status === undefined) {
    return tree;
  }

  return tree.filter((item) => {
    let match = true;
    if (query.areaCode && !item.areaCode.includes(query.areaCode)) {
      match = false;
    }
    if (query.areaName && !item.areaName.includes(query.areaName)) {
      match = false;
    }
    if (query.status !== undefined && item.status !== query.status) {
      match = false;
    }

    if (item.children && item.children.length > 0) {
      item.children = filterAreaTree(item.children, query);
      if (item.children.length > 0) {
        match = true;
      }
    }

    return match;
  });
};

const handleSearch = () => {
  loadAreaTree();
};

const handleReset = () => {
  queryForm.areaCode = '';
  queryForm.areaName = '';
  queryForm.status = undefined;
  loadAreaTree();
};

const handleRefresh = () => {
  loadAreaTree();
};

const handleAdd = () => {
  currentArea.value = null;
  modalVisible.value = true;
};

const handleView = (record) => {
  currentArea.value = { ...record, viewMode: true };
  modalVisible.value = true;
};

const handleEdit = (record) => {
  currentArea.value = { ...record };
  modalVisible.value = true;
};

const handleDelete = async (areaId) => {
  try {
    await areaApi.delete(areaId);
    message.success('删除成功');
    loadAreaTree();
  } catch (error) {
    message.error(error.msg || '删除失败');
  }
};

const handleModalSuccess = () => {
  modalVisible.value = false;
  loadAreaTree();
};

// 生命周期
onMounted(() => {
  loadAreaTree();
});
</script>

<style lang="less" scoped>
.area-manage {
  padding: 16px;

  .search-card {
    margin-bottom: 16px;
  }

  .table-card {
    .table-header {
      margin-bottom: 16px;
    }
  }
}
</style>
