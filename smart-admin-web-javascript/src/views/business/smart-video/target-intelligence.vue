<!--
  智能视频-目标智能页面

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-card title="目标智能分析" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="8">
          <a-card title="检测目标类型" :bordered="false" size="small">
            <a-checkbox-group v-model:value="targetTypes">
              <a-row>
                <a-col :span="24" v-for="type in targetTypeOptions" :key="type.value" style="margin-bottom: 8px;">
                  <a-checkbox :value="type.value">{{ type.label }}</a-checkbox>
                </a-col>
              </a-row>
            </a-checkbox-group>
          </a-card>

          <a-card title="统计信息" :bordered="false" size="small" style="margin-top: 16px;">
            <a-statistic title="今日检测目标数" :value="statistics.todayCount" suffix="个" />
            <a-divider />
            <a-statistic title="本周检测目标数" :value="statistics.weekCount" suffix="个" />
          </a-card>
        </a-col>

        <a-col :span="16">
          <a-card title="实时检测" :bordered="false">
            <a-table
              :columns="columns"
              :data-source="dataSource"
              :pagination="pagination"
              :loading="loading"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'image'">
                  <a-image
                    :width="80"
                    :src="record.image"
                    fallback="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
                  />
                </template>

                <template v-if="column.key === 'confidence'">
                  <a-progress :percent="record.confidence" size="small" />
                </template>

                <template v-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" size="small" @click="handleView(record)">查看详情</a-button>
                  </a-space>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';

const loading = ref(false);

const targetTypes = ref(['person', 'vehicle']);

const targetTypeOptions = [
  { label: '人员', value: 'person' },
  { label: '车辆', value: 'vehicle' },
  { label: '非机动车', value: 'bike' },
  { label: '动物', value: 'animal' },
  { label: '物体', value: 'object' },
];

const statistics = reactive({
  todayCount: 1234,
  weekCount: 8567,
});

const columns = [
  { title: '截图', key: 'image', width: 100 },
  { title: '目标类型', dataIndex: 'targetType', key: 'targetType' },
  { title: '设备名称', dataIndex: 'deviceName', key: 'deviceName' },
  { title: '置信度', key: 'confidence', width: 150 },
  { title: '检测时间', dataIndex: 'detectTime', key: 'detectTime' },
  { title: '操作', key: 'action', width: 120 },
];

const dataSource = ref([
  {
    id: 1,
    image: '',
    targetType: '人员',
    deviceName: '前门摄像头-001',
    confidence: 98,
    detectTime: '2024-11-06 15:32:10',
  },
  {
    id: 2,
    image: '',
    targetType: '车辆',
    deviceName: '停车场摄像头-01',
    confidence: 95,
    detectTime: '2024-11-06 15:28:35',
  },
]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 2,
  showTotal: (total) => `共 ${total} 条`,
});

const handleView = (record) => {
  console.log('查看详情:', record);
};
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;
}
</style>
