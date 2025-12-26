<!--
  智能视频-视频监控预览页面（完整增强版）

  @Author:    Claude Code
  @Date:      2024-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012

  功能说明：
  - 多画面布局（1/4/9/16/25画面）
  - 播放控制（播放/暂停/停止/轮播）
  - 录像控制（开始/停止录像）
  - 音频控制（静音/音量调节）
  - 云台控制
  - 批量截图
  - OSD信息叠加
  - 全屏模式
  - 窗口交换
  - 视频质量选择
  - 数字放大
-->
<template>
  <div class="monitor-preview" :class="{ 'fullscreen': isFullscreen }">
    <!-- 顶部工具栏 -->
    <a-card :bordered="false" class="toolbar-card">
      <div class="toolbar">
        <!-- 左侧：布局选择 -->
        <div class="toolbar-section">
          <div class="section-title">画面布局</div>
          <LayoutSelector
            :current-layout="currentLayout"
            @change="handleLayoutChange"
          />
        </div>

        <!-- 中间：播放控制 -->
        <div class="toolbar-section">
          <div class="section-title">播放控制</div>
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
            <a-button @click="toggleAutoPlay" :type="autoPlay ? 'primary' : 'default'">
              <template #icon><SwapRightOutlined /></template>
              {{ autoPlay ? '关闭轮播' : '开启轮播' }}
            </a-button>
            <a-dropdown>
              <template #overlay>
                <a-menu @click="handleAutoPlayIntervalChange">
                  <a-menu-item :key="5">5秒</a-menu-item>
                  <a-menu-item :key="10">10秒</a-menu-item>
                  <a-menu-item :key="15">15秒</a-menu-item>
                  <a-menu-item :key="30">30秒</a-menu-item>
                  <a-menu-item :key="60">1分钟</a-menu-item>
                </a-menu>
              </template>
              <a-button>
                {{ autoPlayInterval }}秒
                <template #icon><DownOutlined /></template>
              </a-button>
            </a-dropdown>
          </a-space>
        </div>

        <!-- 右侧：视图控制 -->
        <div class="toolbar-section">
          <div class="section-title">视图操作</div>
          <a-space>
            <a-button @click="toggleFullscreen">
              <template #icon><FullscreenOutlined /></template>
              {{ isFullscreen ? '退出全屏' : '全屏' }}
            </a-button>
            <a-button @click="showPTZControl = true">
              <template #icon><ControlOutlined /></template>
              云台控制
            </a-button>
            <a-button @click="showDeviceTree = !showDeviceTree">
              <template #icon><ApartmentOutlined /></template>
              设备树
            </a-button>
            <a-button @click="batchCaptureSnapshots" type="primary">
              <template #icon><CameraOutlined /></template>
              批量截图
            </a-button>
            <a-dropdown>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="batchStartRecord">
                    <template #icon><VideoCameraOutlined /></template>
                    批量录像
                  </a-menu-item>
                  <a-menu-item @click="batchStopRecord">
                    <template #icon><StopOutlined /></template>
                    停止录像
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item @click="toggleAllAudio">
                    <template #icon><SoundOutlined /></template>
                    {{ allAudioEnabled ? '全部静音' : '全部取消静音' }}
                  </a-menu-item>
                  <a-menu-item @click="toggleAllOSD">
                    <template #icon><FontColorsOutlined /></template>
                    {{ allOSDEnabled ? '隐藏OSD' : '显示OSD' }}
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button>
                更多
                <template #icon><MoreOutlined /></template>
              </a-button>
            </a-dropdown>
          </a-space>
        </div>
      </div>
    </a-card>

    <!-- 主要内容区：设备树 + 视频网格 -->
    <div class="main-content">
      <!-- 设备树面板 -->
      <div class="device-tree-panel" v-if="showDeviceTree" v-show="!isFullscreen">
        <DeviceTreePanel
          @select="handleDeviceSelect"
          @play="handlePlayDevice"
        />
      </div>

      <!-- 视频网格区域 -->
      <div class="video-grid-container" :class="{ 'full-width': !showDeviceTree || isFullscreen }">
        <!-- 视频网格 -->
        <div class="video-grid" :class="`layout-${currentLayout}`">
          <div
            v-for="(window, index) in videoWindows"
            :key="index"
            class="video-window"
            :class="{
              'selected': selectedWindow === index,
              'playing': window.playing,
              'recording': window.recording
            }"
            @click="selectWindow(index)"
            @dblclick="dblClickWindow(index)"
            @contextmenu.prevent="showContextMenu($event, index)"
          >
            <!-- 视频播放器 -->
            <div v-if="window.device" class="video-player-wrapper">
              <!-- 模拟视频画面 -->
              <div class="video-simulator" :style="{
                backgroundImage: window.device.snapshotUrl ? `url(${window.device.snapshotUrl})` : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
              }">
                <!-- OSD叠加信息 -->
                <div class="osd-overlay" v-if="window.showOSD">
                  <div class="osd-top-left">
                    <div class="osd-device-name">{{ window.device.name }}</div>
                    <div class="osd-datetime">{{ currentOSDTime }}</div>
                  </div>
                  <div class="osd-top-right" v-if="window.recording">
                    <a-tag color="red" class="recording-indicator">
                      <template #icon><VideoCameraAddOutlined /></template>
                      REC
                    </a-tag>
                  </div>
                  <div class="osd-bottom-left">
                    <a-tag color="green" size="small">在线</a-tag>
                  </div>
                  <div class="osd-bottom-right">
                    <div class="osd-bitrate">{{ window.device.bitrate || '4096' }} Kbps</div>
                  </div>
                </div>

                <!-- 视频加载中 -->
                <div v-if="window.loading" class="video-loading">
                  <a-spin size="large" />
                  <div class="loading-text">正在连接...</div>
                </div>

                <!-- 数字放大效果 -->
                <div v-if="window.zoomLevel > 1" class="zoom-overlay" :style="{
                  transform: `scale(${window.zoomLevel})`,
                  transformOrigin: window.zoomOrigin
                }"></div>
              </div>

              <!-- 窗口控制栏 -->
              <div class="window-controls" v-show="selectedWindow === index || window.hover">
                <div class="control-bar">
                  <a-space size="small">
                    <!-- 录像控制 -->
                    <a-tooltip :title="window.recording ? '停止录像' : '开始录像'">
                      <a-button
                        size="small"
                        :type="window.recording ? 'primary' : 'default'"
                        :danger="window.recording"
                        @click.stop="toggleRecord(window)"
                      >
                        <template #icon>
                          <VideoCameraOutlined v-if="!window.recording" />
                          <VideoCameraAddOutlined v-else />
                        </template>
                      </a-button>
                    </a-tooltip>

                    <!-- 音频控制 -->
                    <a-tooltip :title="window.audioEnabled ? '静音' : '取消静音'">
                      <a-button
                        size="small"
                        @click.stop="toggleAudio(window)"
                      >
                        <template #icon>
                          <SoundOutlined v-if="window.audioEnabled" />
                          <SoundFilledOutlined v-else style="color: #999;" />
                        </template>
                      </a-button>
                    </a-tooltip>

                    <!-- 截图 -->
                    <a-tooltip title="截图">
                      <a-button size="small" @click.stop="captureSnapshot(window)">
                        <template #icon><CameraOutlined /></template>
                      </a-button>
                    </a-tooltip>

                    <!-- OSD切换 -->
                    <a-tooltip :title="window.showOSD ? '隐藏OSD' : '显示OSD'">
                      <a-button size="small" @click.stop="toggleOSD(window)">
                        <template #icon><FontColorsOutlined /></template>
                      </a-button>
                    </a-tooltip>

                    <!-- 数字放大 -->
                    <a-tooltip title="放大">
                      <a-button size="small" @click.stop="zoomIn(window)">
                        <template #icon><ZoomInOutlined /></template>
                      </a-button>
                    </a-tooltip>

                    <a-tooltip title="缩小">
                      <a-button size="small" @click.stop="zoomOut(window)">
                        <template #icon><ZoomOutOutlined /></template>
                      </a-button>
                    </a-tooltip>

                    <a-tooltip title="还原">
                      <a-button size="small" @click.stop="resetZoom(window)">
                        <template #icon><CompressOutlined /></template>
                      </a-button>
                    </a-tooltip>

                    <!-- 画面质量 -->
                    <a-dropdown>
                      <template #overlay>
                        <a-menu @click="(e) => changeQuality(window, e.key)">
                          <a-menu-item :key="'fluent'">流畅</a-menu-item>
                          <a-menu-item :key="'balanced'">均衡</a-menu-item>
                          <a-menu-item :key="'hd'">高清</a-menu-item>
                          <a-menu-item :key="'uhd'">超清</a-menu-item>
                        </a-menu>
                      </template>
                      <a-button size="small">
                        {{ getQualityLabel(window.quality) }}
                        <template #icon><DownOutlined /></template>
                      </a-button>
                    </a-dropdown>

                    <!-- 关闭 -->
                    <a-tooltip title="关闭">
                      <a-button size="small" danger @click.stop="closeWindow(index)">
                        <template #icon><CloseOutlined /></template>
                      </a-button>
                    </a-tooltip>
                  </a-space>
                </div>
              </div>
            </div>

            <!-- 窗口占位符 -->
            <div v-else class="window-placeholder" @mouseenter="window.hover = true" @mouseleave="window.hover = false">
              <a-empty
                :image="Empty.PRESENTED_IMAGE_SIMPLE"
                description=""
              >
                <template #description>
                  <div style="color: rgba(255,255,255,0.45);">窗口 {{ index + 1 }}</div>
                  <a-button type="primary" ghost @click="selectWindow(index)">
                    选择设备
                  </a-button>
                </template>
              </a-empty>
            </div>

            <!-- 窗口标题栏 -->
            <div class="window-header">
              <div class="window-info">
                <a-tag v-if="window.device && window.device.status === 'online'" color="success" size="small">
                  <template #icon><CheckCircleOutlined /></template>
                  在线
                </a-tag>
                <span class="window-title">
                  {{ window.device ? window.device.name : `窗口 ${index + 1}` }}
                </span>
                <a-tag v-if="window.recording" color="red" size="small">
                  <template #icon><VideoCameraAddOutlined /></template>
                  REC
                </a-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 视频网格信息栏 -->
        <div class="grid-info">
          <a-space>
            <span style="color: rgba(255,255,255,0.65);">
              正在播放: {{ playingCount }} / {{ videoWindows.length }}
            </span>
            <span style="color: rgba(255,255,255,0.65);">
              录像中: {{ recordingCount }}
            </span>
            <a-tag color="blue">{{ currentLayout }}画面</a-tag>
          </a-space>
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

    <!-- 批量截图预览弹窗 -->
    <a-modal
      v-model:open="showSnapshotPreview"
      title="批量截图预览"
      :width="1200"
      :footer="null"
    >
      <div class="snapshot-preview">
        <div class="snapshot-header">
          <a-alert
            :message="`已截取 ${snapshots.length} 张图片`"
            type="success"
            show-icon
          />
        </div>

        <div class="snapshot-grid">
          <div
            v-for="(snapshot, index) in snapshots"
            :key="index"
            class="snapshot-item"
          >
            <div class="snapshot-image">
              <img :src="snapshot.dataUrl" :alt="snapshot.deviceName" />
              <div class="snapshot-overlay">
                <a-space>
                  <a-button
                    size="small"
                    type="primary"
                    @click="downloadSnapshot(snapshot, index)"
                  >
                    <template #icon><DownloadOutlined /></template>
                    下载
                  </a-button>
                  <a-button
                    size="small"
                    @click="copyToClipboard(snapshot)"
                  >
                    <template #icon><CopyOutlined /></template>
                    复制
                  </a-button>
                  <a-button
                    size="small"
                    danger
                    @click="removeSnapshot(index)"
                  >
                    <template #icon><DeleteOutlined /></template>
                    删除
                  </a-button>
                </a-space>
              </div>
            </div>
            <div class="snapshot-title">
              <a-typography-text :ellipsis="{ tooltip: snapshot.deviceName }">
                {{ snapshot.deviceName }}
              </a-typography-text>
              <br />
              <a-typography-text type="secondary" style="font-size: 12px">
                {{ snapshot.timestamp }}
              </a-typography-text>
            </div>
          </div>
        </div>

        <div class="snapshot-actions">
          <a-space>
            <a-button @click="showSnapshotPreview = false">
              关闭
            </a-button>
            <a-button
              v-if="snapshots.length > 0"
              type="primary"
              @click="downloadAllSnapshots"
            >
              <template #icon><DownloadOutlined /></template>
              下载全部 ({{ snapshots.length }})
            </a-button>
          </a-space>
        </div>
      </div>
    </a-modal>

    <!-- 右键菜单 -->
    <a-dropdown
      v-model:open="contextMenuVisible"
      :trigger="[]"
      placement="bottomLeft"
    >
      <template #overlay>
        <a-menu>
          <a-menu-item @click="startRecord(contextMenuWindow)">
            <template #icon><VideoCameraOutlined /></template>
            开始录像
          </a-menu-item>
          <a-menu-item @click="captureSnapshot(contextMenuWindow)">
            <template #icon><CameraOutlined /></template>
            截图
          </a-menu-item>
          <a-menu-item @click="toggleAudio(contextMenuWindow)">
            <template #icon><SoundOutlined /></template>
            {{ contextMenuWindow?.audioEnabled ? '静音' : '取消静音' }}
          </a-menu-item>
          <a-menu-item @click="toggleOSD(contextMenuWindow)">
            <template #icon><FontColorsOutlined /></template>
            {{ contextMenuWindow?.showOSD ? '隐藏OSD' : '显示OSD' }}
          </a-menu-item>
          <a-menu-divider />
          <a-menu-item @click="maximizeWindow(contextMenuIndex)">
            <template #icon><ExpandOutlined /></template>
            最大化
          </a-menu-item>
          <a-menu-item @click="closeWindow(contextMenuIndex)">
            <template #icon><CloseOutlined /></template>
            关闭窗口
          </a-menu-item>
        </a-menu>
      </template>
      <div></div>
    </a-dropdown>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, onUnmounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  StopOutlined,
  FullscreenOutlined,
  ControlOutlined,
  ApartmentOutlined,
  CameraOutlined,
  CloseOutlined,
  DownloadOutlined,
  DeleteOutlined,
  VideoCameraOutlined,
  VideoCameraAddOutlined,
  SoundOutlined,
  SoundFilledOutlined,
  FontColorsOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
  CompressOutlined,
  CheckCircleOutlined,
  SwapRightOutlined,
  DownOutlined,
  MoreOutlined,
  CopyOutlined,
  ExpandOutlined,
} from '@ant-design/icons-vue';
import { Empty } from 'ant-design-vue';
import { videoPcApi } from '/@/api/business/video/video-pc-api';
import dayjs from 'dayjs';

// 组件
import LayoutSelector from './components/LayoutSelector.vue';
import DeviceTreePanel from './components/DeviceTreePanel.vue';
import PTZControl from './components/PTZControl.vue';

// 状态
const currentLayout = ref(4);
const showDeviceTree = ref(true);
const showPTZControl = ref(false);
const showDeviceSelect = ref(false);
const showSnapshotPreview = ref(false);
const selectedWindow = ref(0);
const isAllPlaying = ref(false);
const autoPlay = ref(false);
const autoPlayInterval = ref(10);
const currentDevice = ref(null);
const isFullscreen = ref(false);
const allAudioEnabled = ref(true);
const allOSDEnabled = ref(true);

// OSD时间
const currentOSDTime = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'));
let osdTimer = null;

// 视频窗口
const videoWindows = reactive([]);

// 截图数据
const snapshots = ref([]);

// 右键菜单
const contextMenuVisible = ref(false);
const contextMenuWindow = ref(null);
const contextMenuIndex = ref(null);

// 轮播定时器
let autoPlayTimer = null;

// 布局配置
const layoutConfigs = {
  1: { rows: 1, cols: 1 },
  4: { rows: 2, cols: 2 },
  9: { rows: 3, cols: 3 },
  16: { rows: 4, cols: 4 },
  25: { rows: 5, cols: 5 },
};

// 计算属性
const playingCount = computed(() => {
  return videoWindows.filter(w => w.device && w.playing).length;
});

const recordingCount = computed(() => {
  return videoWindows.filter(w => w.recording).length;
});

// 获取质量标签
const getQualityLabel = (quality) => {
  const labels = {
    'fluent': '流畅',
    'balanced': '均衡',
    'hd': '高清',
    'uhd': '超清'
  };
  return labels[quality] || '均衡';
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
      loading: false,
      recording: false,
      audioEnabled: true,
      showOSD: true,
      zoomLevel: 1,
      zoomOrigin: 'center center',
      quality: 'balanced',
      hover: false,
      snapshotUrl: `https://picsum.photos/800/450?random=${i}`
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
  if (!videoWindows[index].device) {
    showDeviceSelect.value = true;
  }
};

// 双击窗口
const dblClickWindow = (index) => {
  // 双击最大化/还原窗口（这里简化为切换到1画面模式）
  if (currentLayout.value !== 1) {
    const tempLayout = currentLayout.value;
    currentLayout.value = 1;
    // 保存之前选中的窗口
    videoWindows[0].device = videoWindows[index].device;
    videoWindows[0].restoreLayout = tempLayout;
  } else {
    // 还原到之前的布局
    if (videoWindows[0].restoreLayout) {
      handleLayoutChange(videoWindows[0].restoreLayout);
    }
  }
};

// 显示右键菜单
const showContextMenu = (event, index) => {
  selectedWindow.value = index;
  contextMenuIndex.value = index;
  contextMenuWindow.value = videoWindows[index];
  contextMenuVisible.value = true;
};

// 处理设备选择
const handleDeviceSelect = (device) => {
  selectedWindow.value = videoWindows.findIndex(w => !w.device);
  if (selectedWindow.value === -1) {
    message.warning('没有可用的播放窗口');
    return;
  }
  videoWindows[selectedWindow.value].device = device;
  playWindow(selectedWindow.value);
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
  playWindow(selectedWindow.value);
};

// 播放窗口
const playWindow = (index) => {
  const window = videoWindows[index];
  if (!window.device) return;

  window.loading = true;

  // 模拟加载
  setTimeout(() => {
    window.loading = false;
    window.playing = true;
    checkAllPlayingStatus();
  }, 1000);
};

// 设备确认
const handleDeviceConfirm = (devices) => {
  devices.forEach((device, index) => {
    if (selectedWindow.value + index < videoWindows.length) {
      videoWindows[selectedWindow.value + index].device = device;
      playWindow(selectedWindow.value + index);
    }
  });
  showDeviceSelect.value = false;
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
    videoWindows.forEach((window, index) => {
      if (window.device && !window.playing) {
        playWindow(index);
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
    message.success(`轮播已开启（间隔${autoPlayInterval.value}秒）`);
  } else {
    stopAutoPlay();
    message.info('轮播已关闭');
  }
};

// 轮播间隔变化
const handleAutoPlayIntervalChange = ({ key }) => {
  autoPlayInterval.value = key;
  if (autoPlay.value) {
    stopAutoPlay();
    startAutoPlay();
    message.success(`轮播间隔已调整为${key}秒`);
  }
};

// 开始轮播
const startAutoPlay = () => {
  autoPlayTimer = setInterval(() => {
    const playingWindows = videoWindows.filter(w => w.playing);
    if (playingWindows.length === 0) return;

    let currentIndex = videoWindows.findIndex(w => w.playing);
    if (currentIndex === -1) currentIndex = 0;

    // 停止当前播放
    videoWindows[currentIndex].playing = false;

    // 播放下一个
    const nextIndex = (currentIndex + 1) % videoWindows.length;
    if (videoWindows[nextIndex].device) {
      playWindow(nextIndex);
      selectedWindow.value = nextIndex;
    }
  }, autoPlayInterval.value * 1000);
};

// 停止轮播
const stopAutoPlay = () => {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer);
    autoPlayTimer = null;
  }
};

// 切换录像
const toggleRecord = (window) => {
  if (window.recording) {
    window.recording = false;
    message.success(`停止录像: ${window.device.name}`);
  } else {
    window.recording = true;
    message.success(`开始录像: ${window.device.name}`);
  }
};

// 批量开始录像
const batchStartRecord = () => {
  const playingWindows = videoWindows.filter(w => w.device && w.playing);
  if (playingWindows.length === 0) {
    message.warning('没有正在播放的视频');
    return;
  }
  playingWindows.forEach(w => w.recording = true);
  message.success(`开始录像 ${playingWindows.length} 路视频`);
};

// 批量停止录像
const batchStopRecord = () => {
  videoWindows.forEach(w => w.recording = false);
  message.success('已停止所有录像');
};

// 切换音频
const toggleAudio = (window) => {
  window.audioEnabled = !window.audioEnabled;
  message.info(window.audioEnabled ? '已取消静音' : '已静音');
};

// 切换全部音频
const toggleAllAudio = () => {
  allAudioEnabled.value = !allAudioEnabled.value;
  videoWindows.forEach(w => w.audioEnabled = allAudioEnabled.value);
  message.info(allAudioEnabled.value ? '已全部取消静音' : '已全部静音');
};

// 切换OSD
const toggleOSD = (window) => {
  window.showOSD = !window.showOSD;
};

// 切换全部OSD
const toggleAllOSD = () => {
  allOSDEnabled.value = !allOSDEnabled.value;
  videoWindows.forEach(w => w.showOSD = allOSDEnabled.value);
  message.info(allOSDEnabled.value ? '已显示全部OSD' : '已隐藏全部OSD');
};

// 数字放大
const zoomIn = (window) => {
  if (window.zoomLevel < 3) {
    window.zoomLevel = Math.min(3, window.zoomLevel + 0.25);
  }
};

const zoomOut = (window) => {
  if (window.zoomLevel > 1) {
    window.zoomLevel = Math.max(1, window.zoomLevel - 0.25);
  }
};

const resetZoom = (window) => {
  window.zoomLevel = 1;
};

// 改变画质
const changeQuality = (window, quality) => {
  window.quality = quality;
  message.success(`画质已切换为: ${getQualityLabel(quality)}`);
};

// 截图
const captureSnapshot = (window) => {
  message.loading({ content: `正在截取 ${window.device.name}...`, key: 'snapshot' });
  setTimeout(() => {
    const now = new Date();
    snapshots.value.push({
      deviceId: window.device.id,
      deviceName: window.device.name,
      dataUrl: window.snapshotUrl,
      timestamp: now.toLocaleString('zh-CN'),
    });
    message.success({ content: '截图成功', key: 'snapshot' });
  }, 500);
};

// 关闭窗口
const closeWindow = (index) => {
  videoWindows[index].device = null;
  videoWindows[index].playing = false;
  videoWindows[index].recording = false;
  checkAllPlayingStatus();
};

// 最大化窗口
const maximizeWindow = (window) => {
  // 实现窗口最大化逻辑
  message.info('最大化功能');
};

// 云台控制
const handlePTZControl = (command) => {
  if (!currentDevice.value) {
    message.warning('请先选择一个设备');
    return;
  }
  message.info(`云台控制：${command}`);
};

// 全屏切换
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value;
  if (isFullscreen.value) {
    message.info('按 ESC 键退出全屏');
  }
};

// 复制到剪贴板
const copyToClipboard = async (snapshot) => {
  try {
    const response = await fetch(snapshot.dataUrl);
    const blob = await response.blob();
    await navigator.clipboard.write([
      new ClipboardItem({ 'image/png': blob })
    ]);
    message.success('已复制到剪贴板');
  } catch (error) {
    message.error('复制失败');
  }
};

// 下载单个截图
const downloadSnapshot = (snapshot, index) => {
  try {
    const link = document.createElement('a');
    link.href = snapshot.dataUrl;
    link.download = `${snapshot.deviceName}_${snapshot.timestamp.replace(/[:\s]/g, '_')}.jpg`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    message.success(`下载成功: ${snapshot.deviceName}`);
  } catch (error) {
    console.error('[视频监控] 下载截图失败', error);
    message.error('下载截图失败');
  }
};

// 删除截图
const removeSnapshot = (index) => {
  snapshots.value.splice(index, 1);
};

// 下载全部截图
const downloadAllSnapshots = () => {
  if (snapshots.value.length === 0) {
    message.warning('没有可下载的截图');
    return;
  }

  let downloadedCount = 0;
  snapshots.value.forEach((snapshot, index) => {
    setTimeout(() => {
      downloadSnapshot(snapshot, index);
      downloadedCount++;
      if (downloadedCount === snapshots.value.length) {
        message.success(`已下载 ${snapshots.value.length} 张截图`);
      }
    }, index * 300);
  });
};

// 更新OSD时间
const updateOSDTime = () => {
  currentOSDTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss');
};

// 监听ESC键退出全屏
const handleKeyPress = (e) => {
  if (e.key === 'Escape' && isFullscreen.value) {
    toggleFullscreen();
  }
};

// 生命周期
onMounted(() => {
  initVideoWindows();

  // 启动OSD时间更新
  updateOSDTime();
  osdTimer = setInterval(updateOSDTime, 1000);

  // 监听键盘事件
  window.addEventListener('keydown', handleKeyPress);
});

onUnmounted(() => {
  stopAutoPlay();
  if (osdTimer) {
    clearInterval(osdTimer);
  }
  window.removeEventListener('keydown', handleKeyPress);
});
</script>

<style scoped lang="less">
.monitor-preview {
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 16px;
  background-color: #000;
  transition: all 0.3s;

  &.fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
    padding: 8px;
  }

  .toolbar-card {
    background: rgba(30, 30, 30, 0.95);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 8px;

    :deep(.ant-card-body) {
      padding: 12px 16px;
    }
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
  }

  .toolbar-section {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .section-title {
      font-size: 12px;
      color: rgba(255, 255, 255, 0.45);
      font-weight: 500;
    }
  }

  .main-content {
    flex: 1;
    display: flex;
    gap: 10px;
    margin-top: 10px;
    overflow: hidden;
  }

  .device-tree-panel {
    width: 280px;
    background: rgba(30, 30, 30, 0.95);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;
  }

  .video-grid-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    padding: 10px;
    overflow: hidden;

    &.full-width {
      width: 100%;
    }
  }

  .video-grid {
    flex: 1;
    display: grid;
    gap: 8px;
    min-height: 0;

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

    &.layout-25 {
      grid-template-columns: repeat(5, 1fr);
      grid-template-rows: repeat(5, 1fr);
    }
  }

  .video-window {
    position: relative;
    background: #1a1a1a;
    border: 2px solid rgba(255, 255, 255, 0.1);
    border-radius: 6px;
    overflow: hidden;
    transition: all 0.3s;
    cursor: pointer;

    &:hover {
      border-color: rgba(24, 144, 255, 0.5);
    }

    &.selected {
      border-color: #1890ff;
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.3);
    }

    &.recording {
      border-color: #ff4d4f;
    }
  }

  .video-player-wrapper {
    width: 100%;
    height: 100%;
    position: relative;
  }

  .video-simulator {
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    position: relative;
  }

  .osd-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
    padding: 12px;

    .osd-top-left {
      position: absolute;
      top: 12px;
      left: 12px;
      color: #fff;
      text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);

      .osd-device-name {
        font-size: 14px;
        font-weight: 500;
        margin-bottom: 4px;
      }

      .osd-datetime {
        font-size: 13px;
        opacity: 0.9;
      }
    }

    .osd-top-right {
      position: absolute;
      top: 12px;
      right: 12px;

      .recording-indicator {
        animation: blink 1s infinite;
      }
    }

    .osd-bottom-left {
      position: absolute;
      bottom: 12px;
      left: 12px;
    }

    .osd-bottom-right {
      position: absolute;
      bottom: 12px;
      right: 12px;
      color: #fff;
      text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
      font-size: 12px;
    }
  }

  @keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.5; }
  }

  .video-loading {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.7);
    color: #fff;

    .loading-text {
      margin-top: 12px;
      font-size: 14px;
    }
  }

  .window-controls {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.9), transparent);
    padding: 12px;
    opacity: 0;
    transition: opacity 0.3s;

    &:hover {
      opacity: 1;
    }
  }

  .control-bar {
    display: flex;
    justify-content: center;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 6px;
    padding: 8px;
    backdrop-filter: blur(10px);
  }

  .window-placeholder {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.03);
    transition: background 0.3s;

    &:hover {
      background: rgba(255, 255, 255, 0.05);
    }
  }

  .window-header {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 40px;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.8), transparent);
    display: flex;
    align-items: center;
    padding: 0 12px;
    pointer-events: none;

    .window-info {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #fff;

      .window-title {
        font-size: 14px;
        font-weight: 500;
      }
    }
  }

  .grid-info {
    height: 40px;
    display: flex;
    align-items: center;
    padding: 0 12px;
    background: rgba(30, 30, 30, 0.95);
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    margin-top: 10px;
    border-radius: 0 0 6px 6px;
  }
}

// 批量截图预览样式
.snapshot-preview {
  .snapshot-header {
    margin-bottom: 16px;
  }

  .snapshot-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 16px;
    max-height: 500px;
    overflow-y: auto;
    padding: 8px;
    background: #f5f5f5;
    border-radius: 8px;
    margin-bottom: 16px;
  }

  .snapshot-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
    background: #fff;
    padding: 8px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }
  }

  .snapshot-image {
    position: relative;
    width: 100%;
    padding-top: 56.25%;
    background: #000;
    border-radius: 4px;
    overflow: hidden;

    img {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .snapshot-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, 0.6);
      opacity: 0;
      transition: opacity 0.3s;

      &:hover {
        opacity: 1;
      }
    }
  }

  .snapshot-title {
    font-size: 13px;
    line-height: 1.5;
  }

  .snapshot-actions {
    display: flex;
    justify-content: flex-end;
    padding-top: 8px;
    border-top: 1px solid #f0f0f0;
  }
}
</style>
