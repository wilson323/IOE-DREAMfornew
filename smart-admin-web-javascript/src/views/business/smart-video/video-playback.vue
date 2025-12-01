<!--
  智能视频-录像回放页面

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-card title="录像回放" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card title="设备列表" :bordered="false" size="small">
            <a-tree
              :tree-data="deviceTreeData"
              :field-names="{ title: 'name', key: 'id', children: 'children' }"
              @select="onDeviceSelect"
            />
          </a-card>
        </a-col>

        <a-col :span="18">
          <a-space direction="vertical" :size="16" style="width: 100%;">
            <a-card title="回放控制" :bordered="false" size="small">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form layout="inline">
                    <a-form-item label="选择设备">
                      <a-select v-model:value="selectedDevice" placeholder="请选择设备" style="width: 200px;">
                        <a-select-option value="device1">前门摄像头-001</a-select-option>
                        <a-select-option value="device2">停车场摄像头-01</a-select-option>
                        <a-select-option value="device3">走廊摄像头-003</a-select-option>
                      </a-select>
                    </a-form-item>

                    <a-form-item label="时间范围">
                      <a-range-picker v-model:value="dateRange" show-time />
                    </a-form-item>

                    <a-form-item>
                      <a-button type="primary" @click="handleSearch">
                        <SearchOutlined />
                        查询
                      </a-button>
                    </a-form-item>
                  </a-form>
                </a-col>
              </a-row>
            </a-card>

            <a-card title="视频回放" :bordered="false">
              <div class="video-player">
                <a-empty description="请选择设备和时间范围查看录像" />
              </div>

              <div class="playback-controls">
                <a-space>
                  <a-button type="primary">
                    <PlayCircleOutlined />
                    播放
                  </a-button>
                  <a-button>
                    <PauseOutlined />
                    暂停
                  </a-button>
                  <a-button>
                    <StepBackwardOutlined />
                    快退
                  </a-button>
                  <a-button>
                    <StepForwardOutlined />
                    快进
                  </a-button>
                  <a-button>
                    <DownloadOutlined />
                    下载
                  </a-button>
                </a-space>
              </div>
            </a-card>

            <a-card title="录像时间轴" :bordered="false" size="small">
              <a-timeline>
                <a-timeline-item v-for="item in recordingList" :key="item.id" :color="item.color">
                  {{ item.time }} - {{ item.description }}
                </a-timeline-item>
              </a-timeline>
            </a-card>
          </a-space>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import {
  SearchOutlined,
  PlayCircleOutlined,
  PauseOutlined,
  StepBackwardOutlined,
  StepForwardOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue';

const selectedDevice = ref(undefined);
const dateRange = ref([]);

const deviceTreeData = ref([
  {
    id: 'group1',
    name: '一号楼',
    children: [
      { id: 'device1', name: '前门摄像头-001' },
      { id: 'device2', name: '大厅摄像头-002' },
    ],
  },
  {
    id: 'group2',
    name: '停车场',
    children: [
      { id: 'device3', name: '停车场摄像头-01' },
      { id: 'device4', name: '停车场摄像头-02' },
    ],
  },
]);

const recordingList = ref([
  { id: 1, time: '08:00:00', description: '正常录像', color: 'green' },
  { id: 2, time: '10:30:15', description: '检测到运动', color: 'blue' },
  { id: 3, time: '14:22:40', description: '告警录像', color: 'red' },
]);

const onDeviceSelect = (selectedKeys, e) => {
  console.log('选择设备:', selectedKeys, e);
};

const handleSearch = () => {
  console.log('查询录像:', selectedDevice.value, dateRange.value);
};
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;

  .video-player {
    height: 400px;
    background-color: #000;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;
  }

  .playback-controls {
    text-align: center;
    padding: 16px 0;
  }
}
</style>
