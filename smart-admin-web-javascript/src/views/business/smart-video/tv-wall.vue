<!--
  智能视频-电视墙页面

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-card title="电视墙管理" :bordered="false">
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <PlusOutlined />
            新建电视墙
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
            <a-badge :status="record.status === '在线' ? 'success' : 'default'" :text="record.status" />
          </template>

          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
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
  { title: '电视墙名称', dataIndex: 'name', key: 'name' },
  { title: '布局模式', dataIndex: 'layout', key: 'layout' },
  { title: '分辨率', dataIndex: 'resolution', key: 'resolution' },
  { title: '窗口数量', dataIndex: 'windowCount', key: 'windowCount' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 200 },
];

const dataSource = ref([
  {
    id: 1,
    name: '监控大厅电视墙1',
    layout: '3x3',
    resolution: '1920x1080',
    windowCount: 9,
    status: '在线',
    createTime: '2024-11-01 10:30:00',
  },
  {
    id: 2,
    name: '安保中心电视墙',
    layout: '4x4',
    resolution: '3840x2160',
    windowCount: 16,
    status: '在线',
    createTime: '2024-11-02 14:20:00',
  },
]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 2,
  showTotal: (total) => `共 ${total} 条`,
});

const handleAdd = () => {
  console.log('新建电视墙');
};

const handleView = (record) => {
  console.log('查看电视墙:', record);
};

const handleEdit = (record) => {
  console.log('编辑电视墙:', record);
};

const handleDelete = (record) => {
  console.log('删除电视墙:', record);
};
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;
}
</style>
