<!--
  智能视频-联动设置页面

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-card title="联动设置" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="handleAdd">
          <PlusOutlined />
          新建联动规则
        </a-button>
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

          <template v-if="column.key === 'priority'">
            <a-tag :color="getPriorityColor(record.priority)">{{ record.priority }}</a-tag>
          </template>

          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleCopy(record)">复制</a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>

        <template #expandedRowRender="{ record }">
          <a-descriptions :column="2" size="small" bordered>
            <a-descriptions-item label="触发条件">
              {{ record.triggerCondition }}
            </a-descriptions-item>
            <a-descriptions-item label="执行动作">
              {{ record.action }}
            </a-descriptions-item>
            <a-descriptions-item label="延迟时间" :span="2">
              {{ record.delay }}秒
            </a-descriptions-item>
            <a-descriptions-item label="描述" :span="2">
              {{ record.description }}
            </a-descriptions-item>
          </a-descriptions>
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
  { title: '规则名称', dataIndex: 'name', key: 'name' },
  { title: '触发设备', dataIndex: 'triggerDevice', key: 'triggerDevice' },
  { title: '联动设备', dataIndex: 'linkageDevice', key: 'linkageDevice' },
  { title: '优先级', key: 'priority' },
  { title: '状态', key: 'status', width: 150 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 200 },
];

const dataSource = ref([
  {
    id: 1,
    name: '入侵检测联动告警',
    triggerDevice: '前门摄像头-001',
    linkageDevice: '报警器-01, 闪光灯-01',
    triggerCondition: '检测到可疑人员',
    action: '触发报警器，开启闪光灯',
    priority: '高',
    delay: 0,
    status: true,
    description: '当前门摄像头检测到可疑人员时，立即触发报警器和闪光灯',
    createTime: '2024-11-01 10:30:00',
  },
  {
    id: 2,
    name: '火灾检测联动疏散',
    triggerDevice: '烟雾探测器-01',
    linkageDevice: '疏散广播, 应急照明',
    triggerCondition: '检测到烟雾',
    action: '启动疏散广播，开启应急照明',
    priority: '紧急',
    delay: 0,
    status: true,
    description: '检测到烟雾时立即启动疏散系统',
    createTime: '2024-11-02 14:20:00',
  },
  {
    id: 3,
    name: '车辆违停联动通知',
    triggerDevice: '停车场摄像头-01',
    linkageDevice: '车主通知系统',
    triggerCondition: '检测到车辆违停',
    action: '发送通知给车主',
    priority: '中',
    delay: 30,
    status: false,
    description: '检测到车辆违停30秒后发送通知',
    createTime: '2024-11-03 09:15:00',
  },
]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 3,
  showTotal: (total) => `共 ${total} 条`,
});

const getPriorityColor = (priority) => {
  const colorMap = {
    '紧急': 'red',
    '高': 'orange',
    '中': 'blue',
    '低': 'default',
  };
  return colorMap[priority] || 'default';
};

const handleAdd = () => {
  console.log('新建联动规则');
};

const handleEdit = (record) => {
  console.log('编辑规则:', record);
};

const handleCopy = (record) => {
  console.log('复制规则:', record);
};

const handleDelete = (record) => {
  console.log('删除规则:', record);
};
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;
}
</style>
