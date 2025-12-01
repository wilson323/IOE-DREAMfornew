<!--
  智能视频-大屏管控页面

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-row :gutter="16">
      <a-col :span="6">
        <a-card title="大屏列表" :bordered="false">
          <a-list
            :data-source="screenList"
            size="small"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta :title="item.name" :description="`${item.resolution} | ${item.status}`">
                  <template #avatar>
                    <MonitorOutlined style="font-size: 24px;" />
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a @click="handleSelectScreen(item)">控制</a>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>

      <a-col :span="18">
        <a-card title="大屏控制" :bordered="false">
          <template #extra>
            <a-space>
              <a-button type="primary">
                <PlayCircleOutlined />
                启动大屏
              </a-button>
              <a-button>
                <PauseOutlined />
                停止大屏
              </a-button>
              <a-button>
                <SyncOutlined />
                刷新
              </a-button>
            </a-space>
          </template>

          <div class="screen-preview">
            <a-empty description="请选择大屏进行控制" />
          </div>

          <a-divider />

          <a-card title="布局配置" :bordered="false" size="small">
            <a-form layout="horizontal" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
              <a-form-item label="布局模式">
                <a-select v-model:value="layoutMode" style="width: 200px;">
                  <a-select-option value="1x1">1x1</a-select-option>
                  <a-select-option value="2x2">2x2</a-select-option>
                  <a-select-option value="3x3">3x3</a-select-option>
                  <a-select-option value="4x4">4x4</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="视频源">
                <a-select v-model:value="videoSource" mode="multiple" placeholder="选择视频源" style="width: 400px;">
                  <a-select-option value="camera1">前门摄像头-001</a-select-option>
                  <a-select-option value="camera2">停车场摄像头-01</a-select-option>
                  <a-select-option value="camera3">走廊摄像头-003</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="轮播间隔">
                <a-input-number v-model:value="rotationInterval" :min="5" :max="300" suffix="秒" />
              </a-form-item>

              <a-form-item :wrapper-col="{ offset: 4 }">
                <a-space>
                  <a-button type="primary" @click="handleApplyLayout">应用配置</a-button>
                  <a-button @click="handleResetLayout">重置</a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </a-card>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import {
  MonitorOutlined,
  PlayCircleOutlined,
  PauseOutlined,
  SyncOutlined,
} from '@ant-design/icons-vue';

const screenList = ref([
  { id: 1, name: '监控大厅大屏1', resolution: '3840x2160', status: '在线' },
  { id: 2, name: '安保中心大屏', resolution: '1920x1080', status: '在线' },
  { id: 3, name: '会议室大屏', resolution: '2560x1440', status: '离线' },
]);

const layoutMode = ref('3x3');
const videoSource = ref([]);
const rotationInterval = ref(30);

const handleSelectScreen = (screen) => {
  console.log('选择大屏:', screen);
};

const handleApplyLayout = () => {
  console.log('应用布局配置');
};

const handleResetLayout = () => {
  layoutMode.value = '3x3';
  videoSource.value = [];
  rotationInterval.value = 30;
};
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;

  .screen-preview {
    height: 400px;
    background-color: #000;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;
  }
}
</style>
