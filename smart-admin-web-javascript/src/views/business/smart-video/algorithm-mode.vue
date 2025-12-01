<!--
  智能视频-算法模式页面

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-card title="算法模式管理" :bordered="false">
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <PlusOutlined />
            添加算法
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="pagination"
        :loading="loading"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-switch v-model:checked="record.status" checked-children="启用" un-checked-children="禁用" />
          </template>

          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleConfig(record)">配置</a-button>
              <a-button type="link" size="small" @click="handleTest(record)">测试</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { PlusOutlined } from '@ant-design/icons-vue';

const loading = ref(false);

const columns = [
  { title: '算法名称', dataIndex: 'name', key: 'name' },
  { title: '算法类型', dataIndex: 'type', key: 'type' },
  { title: '版本', dataIndex: 'version', key: 'version' },
  { title: '准确率', dataIndex: 'accuracy', key: 'accuracy' },
  { title: '状态', key: 'status', width: 150 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime' },
  { title: '操作', key: 'action', width: 250 },
];

const dataSource = ref([
  {
    id: 1,
    name: '人脸识别算法',
    type: '人脸识别',
    version: 'v2.1.0',
    accuracy: '98.5%',
    status: true,
    updateTime: '2024-11-01 10:30:00',
  },
  {
    id: 2,
    name: '车牌识别算法',
    type: '车牌识别',
    version: 'v1.8.2',
    accuracy: '99.2%',
    status: true,
    updateTime: '2024-11-02 14:20:00',
  },
  {
    id: 3,
    name: '行为分析算法',
    type: '行为分析',
    version: 'v3.0.1',
    accuracy: '95.8%',
    status: false,
    updateTime: '2024-11-03 09:15:00',
  },
]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 3,
  showTotal: (total) => `共 ${total} 条`,
});

const handleAdd = () => {
  console.log('添加算法');
};

const handleConfig = (record) => {
  console.log('配置算法:', record);
};

const handleTest = (record) => {
  console.log('测试算法:', record);
};

const handleEdit = (record) => {
  console.log('编辑算法:', record);
};

const handleDelete = (record) => {
  console.log('删除算法:', record);
};
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;
}
</style>
