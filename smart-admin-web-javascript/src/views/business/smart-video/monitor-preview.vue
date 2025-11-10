<!--
  智能视频-视频监控预览页面

  @Author:    Claude Code
  @Date:      2024-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="monitor-preview">
    <!-- 顶部工具栏 -->
    <a-card :bordered="false" class="smart-margin-bottom10">
      <div class="toolbar">
        <!-- 左侧：布局选择 -->
        <LayoutSelector
          :current-layout="currentLayout"
          @change="handleLayoutChange"
        />

        <!-- 中间：播放控制 -->
        <div class="playback-controls">
          <a-space>
            <a-button @click="toggleAllPlay" :type="isAllPlaying ? 'primary' : 'default'">
              <template #icon>
                <PlayCircleOutlined v-if="!isAllPlaying" />
                <PauseCircleOutlined v-else />
              </template>
              {{ isAllPlaying ? '暂停全部' : '播放全部' }}
            </a-button>
            <a-button @click="stopAll">
              <template #icon><StopOutlined /></template>
              停止全部
            </a-button>
            <a-button @click="toggleAutoPlay">
              <template #icon><ReloadOutlined :rotate="autoPlay ? 0 : 180" /></template>
              {{ autoPlay ? '关闭轮播' : '开启轮播' }}
            </a-button>
          </a-space>
        </div>

        <!-- 右侧：视图控制 -->
        <div class="view-controls">
          <a-space>
            <a-button @click="toggleFullscreen">
              <template #icon><FullscreenOutlined /></template>
              全屏
            </a-button>
            <a-button @click="showPTZControl = true">
              <template #icon><ControlOutlined /></template>
              云台控制
            </a-button>
            <a-button @click="showDeviceTree = !showDeviceTree">
              <template #icon><ApartmentOutlined /></template>
              设备树
            </a-button>
          </a-space>
        </div>
      </div>
    </a-card>

    <!-- 主要内容区：设备树 + 视频网格 -->
    <div class="main-content">
      <!-- 设备树面板 -->
      <div class="device-tree-panel" v-if="showDeviceTree">
        <DeviceTreePanel
          @select="handleDeviceSelect"
          @play="handlePlayDevice"
        />
      </div>

      <!-- 视频网格区域 -->
      <div class="video-grid-container" :class="{ 'full-width': !showDeviceTree }">
        <!-- 视频网格 -->
        <div class="video-grid" :class="`layout-${currentLayout}`">
          <div
            v-for="(window, index) in videoWindows"
            :key="index"
            class="video-window"
            @click="selectWindow(index)"
          >
            <!-- 视频播放器 -->
            <VideoPlayer
              v-if="window.device"
              :device="window.device"
              :is-selected="selectedWindow === index"
              :show-controls="true"
              @play="handleVideoPlay"
              @stop="handleVideoStop"
              @error="handleVideoError"
            />

            <!-- 窗口占位符 -->
            <div v-else class="window-placeholder">
              <a-empty
                description="未选择设备"
                :image="Empty.PRESENTED_IMAGE_SIMPLE"
              >
                <a-button type="primary" @click="selectWindow(index)">
                  选择设备
                </a-button>
              </a-empty>
            </div>

            <!-- 窗口标题栏 -->
            <div class="window-title">
              <span v-if="window.device">{{ window.device.name }}</span>
              <span v-else>窗口 {{ index + 1 }}</span>
              <div class="window-actions" v-if="window.device">
                <a-space>
                  <a-button size="small" type="link" @click.stop="captureSnapshot(window)">
                    <template #icon><CameraOutlined /></template>
                  </a-button>
                  <a-button size="small" type="link" @click.stop="stopWindow(index)">
                    <template #icon><CloseOutlined /></template>
                  </a-button>
                </a-space>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 云台控制弹窗 -->
    <PTZControl
      :visible="showPTZControl"
      :device="currentDevice"
      @close="showPTZControl = false"
      @control="handlePTZControl"
    />

    <!-- 设备选择弹窗 -->
    <DeviceSelectModal
      :visible="showDeviceSelect"
      :selected-devices="selectedDevices"
      @close="showDeviceSelect = false"
      @confirm="handleDeviceConfirm"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  StopOutlined,
  ReloadOutlined,
  FullscreenOutlined,
  ControlOutlined,
  ApartmentOutlined,
  CameraOutlined,
  CloseOutlined,
} from '@ant-design/icons-vue';
import { Empty } from 'ant-design-vue';

// 组件
import LayoutSelector from './components/LayoutSelector.vue';
import VideoPlayer from './components/VideoPlayer.vue';
import DeviceTreePanel from './components/DeviceTreePanel.vue';
import PTZControl from './components/PTZControl.vue';

// 状态
const currentLayout = ref(4); // 1/4/9/16画面
const showDeviceTree = ref(true);
const showPTZControl = ref(false);
const showDeviceSelect = ref(false);
const selectedWindow = ref(0);
const isAllPlaying = ref(false);
const autoPlay = ref(false);
const currentDevice = ref(null);

// 视频窗口
const videoWindows = reactive([]);

// 轮播定时器
let autoPlayTimer = null;

// 布局配置
const layoutConfigs = {
  1: { rows: 1, cols: 1 },
  4: { rows: 2, cols: 2 },
  9: { rows: 3, cols: 3 },
  16: { rows: 4, cols: 4 },
};

// 初始化视频窗口
const initVideoWindows = () => {
  const config = layoutConfigs[currentLayout.value] || layoutConfigs[4];
  const total = config.rows * config.cols;

  videoWindows.length = 0;
  for (let i = 0; i < total; i++) {
    videoWindows.push({
      id: i,
      device: null,
      playing: false,
    });
  }
};

// 处理布局变化
const handleLayoutChange = (layout) => {
  currentLayout.value = layout;
  initVideoWindows();
};

// 选中窗口
const selectWindow = (index) => {
  selectedWindow.value = index;
  showDeviceSelect.value = true;
};

// 处理设备选择
const handleDeviceSelect = (device) => {
  selectedWindow.value = videoWindows.findIndex(w => !w.device);
  if (selectedWindow.value === -1) {
    message.warning('没有可用的播放窗口');
    return;
  }
  videoWindows[selectedWindow.value].device = device;
};

// 播放设备
const handlePlayDevice = (device, windowIndex) => {
  if (windowIndex !== undefined) {
    selectedWindow.value = windowIndex;
  } else {
    selectedWindow.value = videoWindows.findIndex(w => !w.device);
    if (selectedWindow.value === -1) {
      message.warning('没有可用的播放窗口');
      return;
    }
  }
  videoWindows[selectedWindow.value].device = device;
};

// 设备确认
const handleDeviceConfirm = (devices) => {
  devices.forEach((device, index) => {
    if (selectedWindow.value + index < videoWindows.length) {
      videoWindows[selectedWindow.value + index].device = device;
    }
  });
  showDeviceSelect.value = false;
};

// 视频播放
const handleVideoPlay = (windowId) => {
  const window = videoWindows.find(w => w.id === windowId);
  if (window) {
    window.playing = true;
  }
  checkAllPlayingStatus();
};

// 视频停止
const handleVideoStop = (windowId) => {
  const window = videoWindows.find(w => w.id === windowId);
  if (window) {
    window.playing = false;
  }
  checkAllPlayingStatus();
};

// 检查全部播放状态
const checkAllPlayingStatus = () => {
  isAllPlaying.value = videoWindows.every(w => !w.device || w.playing);
};

// 停止全部
const stopAll = () => {
  videoWindows.forEach(window => {
    window.playing = false;
  });
  isAllPlaying.value = false;
};

// 切换全部播放
const toggleAllPlay = () => {
  if (isAllPlaying.value) {
    stopAll();
  } else {
    videoWindows.forEach(window => {
      if (window.device) {
        window.playing = true;
      }
    });
    isAllPlaying.value = true;
  }
};

// 切换轮播
const toggleAutoPlay = () => {
  autoPlay.value = !autoPlay.value;
  if (autoPlay.value) {
    startAutoPlay();
  } else {
    stopAutoPlay();
  }
};

// 开始轮播
const startAutoPlay = () => {
  autoPlayTimer = setInterval(() => {
    let currentIndex = videoWindows.findIndex(w => w.playing);
    if (currentIndex === -1) {
      currentIndex = 0;
    }

    const nextIndex = (currentIndex + 1) % videoWindows.length;
    videoWindows[currentIndex].playing = false;
    if (videoWindows[nextIndex].device) {
      videoWindows[nextIndex].playing = true;
    }
  }, 10000);
};

// 停止轮播
const stopAutoPlay = () => {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer);
    autoPlayTimer = null;
  }
};

// 截图
const captureSnapshot = (window) => {
  message.success(`开始截图：${window.device.name}`);
};

// 停止窗口
const stopWindow = (index) => {
  videoWindows[index].device = null;
  videoWindows[index].playing = false;
  checkAllPlayingStatus();
};

// 云台控制
const handlePTZControl = (command) => {
  if (!currentDevice.value) {
    message.warning('请先选择一个设备');
    return;
  }
  message.info(`云台控制：${command}`);
};

// 处理视频错误
const handleVideoError = (windowId, error) => {
  console.error('视频播放错误:', error);
  message.error(`视频播放失败：${error.message}`);
};

// 全屏
const toggleFullscreen = () => {
  message.info('全屏功能开发中...');
};

// 获取选中的设备
const getSelectedDevices = () => {
  return videoWindows.filter(w => w.device).map(w => w.device);
};

// 生命周期
onMounted(() => {
  initVideoWindows();
});

onUnmounted(() => {
  stopAutoPlay();
});
</script>

<style scoped lang="less">
.monitor-preview {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 16px;
  background-color: #000;

  .smart-margin-bottom10 {
    margin-bottom: 10px;
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: rgba(255, 255, 255, 0.05);
    padding: 12px 16px;
    border-radius: 6px;
  }

  .main-content {
    flex: 1;
    display: flex;
    gap: 10px;
    margin-top: 10px;
  }

  .device-tree-panel {
    width: 280px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 6px;
    overflow: hidden;
  }

  .video-grid-container {
    flex: 1;
    background: rgba(0, 0, 0, 0.3);
    border-radius: 6px;
    padding: 10px;

    &.full-width {
      width: 100%;
    }
  }

  .video-grid {
    height: 100%;
    display: grid;
    gap: 8px;

    &.layout-1 {
      grid-template-columns: 1fr;
      grid-template-rows: 1fr;
    }

    &.layout-4 {
      grid-template-columns: repeat(2, 1fr);
      grid-template-rows: repeat(2, 1fr);
    }

    &.layout-9 {
      grid-template-columns: repeat(3, 1fr);
      grid-template-rows: repeat(3, 1fr);
    }

    &.layout-16 {
      grid-template-columns: repeat(4, 1fr);
      grid-template-rows: repeat(4, 1fr);
    }
  }

  .video-window {
    position: relative;
    background: #1a1a1a;
    border: 2px solid transparent;
    border-radius: 4px;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      border-color: rgba(255, 255, 255, 0.3);
    }

    &.selected {
      border-color: #1890ff;
    }
  }

  .window-placeholder {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.03);
  }

  .window-title {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 36px;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.8), transparent);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 12px;
    color: #fff;
    font-size: 14px;

    .window-actions {
      display: flex;
      gap: 4px;
    }
  }
}
</style>
