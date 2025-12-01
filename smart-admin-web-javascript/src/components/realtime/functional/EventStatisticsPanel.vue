<template>
  <div class="event-statistics-panel">
    <a-card title="事件统计" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-statistic
            title="今日事件"
            :value="1128"
            :value-style="{ color: '#3f8600' }"
            prefix-icon="user"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="本周事件"
            :value="5678"
            :value-style="{ color: '#1890ff' }"
            prefix-icon="user"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="本月事件"
            :value="15678"
            :value-style="{ color: '#722ed1' }"
            prefix-icon="user"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="处理率"
            :value="93.5"
            suffix="%"
            :value-style="{ color: '#eb2f96' }"
          />
        </a-col>
      </a-row>

      <div style="margin-top: 24px;">
        <a-table
          :columns="columns"
          :data-source="eventData"
          :pagination="{ pageSize: 5 }"
          size="small"
        />
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const columns = [
  {
    title: '事件类型',
    dataIndex: 'eventType',
    key: 'eventType',
  },
  {
    title: '发生次数',
    dataIndex: 'count',
    key: 'count',
  },
  {
    title: '处理次数',
    dataIndex: 'handled',
    key: 'handled',
  },
  {
    title: '处理率',
    dataIndex: 'handleRate',
    key: 'handleRate',
  },
]

const eventData = ref([
  {
    key: '1',
    eventType: '门禁刷卡',
    count: 2456,
    handled: 2456,
    handleRate: '100%',
  },
  {
    key: '2',
    eventType: '异常开门',
    count: 128,
    handled: 128,
    handleRate: '100%',
  },
  {
    key: '3',
    eventType: '设备离线',
    count: 67,
    handled: 63,
    handleRate: '94.0%',
  },
  {
    key: '4',
    eventType: '消费记录',
    count: 3456,
    handled: 3456,
    handleRate: '100%',
  },
  {
    key: '5',
    eventType: '考勤记录',
    count: 1890,
    handled: 1890,
    handleRate: '100%',
  },
])
</script>

<style scoped>
.event-statistics-panel {
  width: 100%;
}
</style>