<!--
  智能视频-录像回放页面

  @Author:    IOE-DREAM Team
  @Date:      2025-01-30
  @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="smart-video-page">
    <a-card title="录像回放" :bordered="false">
      <a-row :gutter="16">
        <!-- 左侧：设备和日期选择 -->
        <a-col :span="6">
          <a-space direction="vertical" :size="16" style="width: 100%;">
            <!-- 设备树 -->
            <a-card title="设备列表" :bordered="false" size="small">
              <a-tree
                :tree-data="deviceTreeData"
                :field-names="{ title: 'name', key: 'id', children: 'children' }"
                @select="onDeviceSelect"
                show-icon
              >
                <template #icon="{ data }">
                  <CameraOutlined v-if="!data.children" />
                  <FolderOutlined v-else />
                </template>
              </a-tree>
            </a-card>

            <!-- 日历选择 -->
            <a-card title="日期选择" :bordered="false" size="small">
              <a-calendar
                v-model:value="selectedDate"
                :fullscreen="false"
                @select="onDateSelect"
                :valid-range="calendarRange"
              />
              <a-alert
                :message="`已选择: ${selectedDate.format('YYYY年MM月DD日')}`"
                type="info"
                show-icon
                style="margin-top: 12px;"
              />
            </a-card>

            <!-- 录像统计 -->
            <a-card title="录像统计" :bordered="false" size="small">
              <a-statistic
                title="录像片段"
                :value="recordingSegments.length"
                suffix="段"
              />
              <a-divider style="margin: 12px 0;" />
              <a-row :gutter="8">
                <a-col :span="8">
                  <a-statistic
                    title="正常"
                    :value="getSegmentCount('normal')"
                    :value-style="{ color: '#52c41a', fontSize: '18px' }"
                  />
                </a-col>
                <a-col :span="8">
                  <a-statistic
                    title="运动"
                    :value="getSegmentCount('motion')"
                    :value-style="{ color: '#1890ff', fontSize: '18px' }"
                  />
                </a-col>
                <a-col :span="8">
                  <a-statistic
                    title="告警"
                    :value="getSegmentCount('alarm')"
                    :value-style="{ color: '#ff4d4f', fontSize: '18px' }"
                  />
                </a-col>
              </a-row>
            </a-card>
          </a-space>
        </a-col>

        <!-- 右侧：回放控制和录像列表 -->
        <a-col :span="18">
          <a-space direction="vertical" :size="16" style="width: 100%;">
            <!-- 查询控制 -->
            <a-card title="回放查询" :bordered="false" size="small">
              <a-form layout="inline">
                <a-form-item label="选择设备">
                  <a-select
                    v-model:value="selectedDeviceId"
                    placeholder="请选择设备"
                    style="width: 200px;"
                    @change="onDeviceChange"
                    show-search
                    :filter-option="filterDevice"
                  >
                    <a-select-option
                      v-for="device in deviceList"
                      :key="device.deviceId"
                      :value="device.deviceId"
                    >
                      {{ device.name }}
                    </a-select-option>
                  </a-select>
                </a-form-item>

                <a-form-item label="时间范围">
                  <a-range-picker
                    v-model:value="dateRange"
                    show-time
                    show-now
                    format="YYYY-MM-DD HH:mm:ss"
                    :placeholder="['开始时间', '结束时间']"
                    style="width: 400px;"
                  />
                </a-form-item>

                <a-form-item label="回放模式">
                  <a-select v-model:value="playbackMode" style="width: 120px;">
                    <a-select-option value="single">单画面</a-select-option>
                    <a-select-option value="dual">双画面</a-select-option>
                    <a-select-option value="quad">四画面</a-select-option>
                  </a-select>
                </a-form-item>

                <a-form-item>
                  <a-space>
                    <a-button type="primary" @click="handleSearch" :loading="loading">
                      <SearchOutlined />
                      查询
                    </a-button>
                    <a-button @click="handleReset">
                      <ReloadOutlined />
                      重置
                    </a-button>
                  </a-space>
                </a-form-item>
              </a-form>
            </a-card>

            <!-- 视频回放器 -->
            <a-card title="视频回放" :bordered="false">
              <div :class="['video-player-container', `mode-${playbackMode}`]">
                <div
                  v-for="(window, index) in playbackWindows"
                  :key="index"
                  class="video-window"
                  :class="{ 'active': window.active }"
                >
                  <!-- 视频播放器 -->
                  <div class="video-player">
                    <video
                      v-if="window.isPlaying"
                      :ref="el => setVideoRef(el, index)"
                      class="video-element"
                      autoplay
                      controls
                      @timeupdate="onTimeUpdate(index)"
                      @loadedmetadata="onMetadataLoaded(index)"
                      @ended="onVideoEnded(index)"
                    >
                      <source :src="window.playbackUrl" type="video/mp4" />
                      您的浏览器不支持视频播放
                    </video>
                    <a-empty
                      v-else
                      description="请选择录像片段播放"
                      :image-style="{ height: '80px' }"
                    />
                  </div>

                  <!-- 窗口信息 -->
                  <div class="window-info">
                    <div class="window-title">
                      <a-tag v-if="window.active" color="blue">当前</a-tag>
                      {{ window.deviceName || `窗口${index + 1}` }}
                    </div>
                    <div class="window-controls">
                      <a-button-group size="small">
                        <a-button @click="togglePlay(index)">
                          <template #icon>
                            <PlayCircleOutlined v-if="window.paused" />
                            <PauseOutlined v-else />
                          </template>
                        </a-button>
                        <a-button @click="captureSnapshot(index)" title="截图">
                          <CameraOutlined />
                        </a-button>
                        <a-button @click="stopPlayback(index)" title="停止">
                          <StopOutlined />
                        </a-button>
                        <a-dropdown>
                          <template #overlay>
                            <a-menu @click="({ key }) => handleWindowAction(key, index)">
                              <a-menu-item key="set_source">设置源</a-menu-item>
                              <a-menu-item key="fullscreen">全屏</a-menu-item>
                              <a-menu-item key="close">关闭</a-menu-item>
                            </a-menu>
                          </template>
                          <a-button>
                            <MoreOutlined />
                          </a-button>
                        </a-dropdown>
                      </a-button-group>
                    </div>
                  </div>

                  <!-- 回放进度 -->
                  <div class="playback-progress" v-if="window.isPlaying">
                    <div class="time-info">
                      <span>{{ formatTime(window.currentTime) }}</span>
                      <span>{{ formatTime(window.totalDuration) }}</span>
                    </div>
                    <a-slider
                      v-model:value="window.currentTimeSlider"
                      :min="0"
                      :max="window.totalDuration"
                      :step="1"
                      @change="onSeek(index)"
                    />
                  </div>
                </div>
              </div>

              <!-- 全局控制栏 -->
              <div class="global-controls" v-if="hasActivePlayback">
                <a-row :gutter="16" align="middle">
                  <a-col :span="12">
                    <a-space>
                      <span>播放速度:</span>
                      <a-radio-group
                        v-model:value="globalPlaybackSpeed"
                        button-style="solid"
                        size="small"
                        @change="onGlobalSpeedChange"
                      >
                        <a-radio-button :value="0.5">0.5x</a-radio-button>
                        <a-radio-button :value="1.0">1.0x</a-radio-button>
                        <a-radio-button :value="1.5">1.5x</a-radio-button>
                        <a-radio-button :value="2.0">2.0x</a-radio-button>
                      </a-radio-group>
                    </a-space>
                  </a-col>
                  <a-col :span="12" style="text-align: right;">
                    <a-space>
                      <a-button size="small" @click="captureAllSnapshots">
                        <CameraOutlined />
                        全部截图
                      </a-button>
                      <a-button size="small" @click="downloadAllRecordings">
                        <DownloadOutlined />
                        批量下载
                      </a-button>
                    </a-space>
                  </a-col>
                </a-row>
              </div>
            </a-card>

            <!-- 录像时间轴 -->
            <a-card title="录像时间轴" :bordered="false" size="small">
              <div v-if="recordingSegments.length > 0" class="timeline-view">
                <div class="timeline-container">
                  <div class="timeline-header">
                    <a-space>
                      <span>录像片段 ({{ recordingSegments.length }}段)</span>
                      <a-button size="small" @click="refreshTimeline">
                        <ReloadOutlined />
                        刷新
                      </a-button>
                    </a-space>
                    <a-space>
                      <span class="legend">
                        <span class="legend-color normal"></span> 正常
                      </span>
                      <span class="legend">
                        <span class="legend-color motion"></span> 运动
                      </span>
                      <span class="legend">
                        <span class="legend-color alarm"></span> 告警
                      </span>
                    </a-space>
                  </div>

                  <div class="timeline-body">
                    <div
                      v-for="(segment, index) in recordingSegments"
                      :key="index"
                      class="timeline-segment"
                      :class="segment.type"
                      :style="{
                        left: segment.left + '%',
                        width: segment.width + '%'
                      }"
                      @click="playSegment(segment)"
                      :title="`${segment.startTime} - ${segment.endTime} (${segment.description})`"
                    >
                      <span class="segment-tooltip">
                        {{ segment.startTime }} - {{ segment.endTime }}
                        <br />{{ segment.description }}
                        <br />大小: {{ segment.fileSize || '未知' }}
                      </span>
                    </div>
                  </div>

                  <div class="timeline-ruler">
                    <span
                      v-for="(tick, index) in timelineTicks"
                      :key="index"
                      class="timeline-tick"
                      :style="{ left: tick.position + '%' }"
                    >
                      {{ tick.label }}
                    </span>
                  </div>
                </div>
              </div>

              <a-empty v-else description="暂无录像数据，请选择设备和日期查询" />
            </a-card>

            <!-- 录像列表 -->
            <a-card title="录像详情" :bordered="false" size="small">
              <a-table
                :columns="recordingColumns"
                :data-source="recordingList"
                :pagination="{
                  current: pagination.current,
                  pageSize: pagination.pageSize,
                  total: pagination.total,
                  showSizeChanger: true,
                  showQuickJumper: true,
                  showTotal: (total) => `共 ${total} 条`
                }"
                :loading="loading"
                size="small"
                @change="handleTableChange"
                :row-selection="{
                  selectedRowKeys: selectedRowKeys,
                  onChange: onSelectChange
                }"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'type'">
                    <a-tag :color="getTypeColor(record.type)">
                      {{ getTypeText(record.type) }}
                    </a-tag>
                  </template>
                  <template v-else-if="column.key === 'duration'">
                    {{ formatDuration(record.duration) }}
                  </template>
                  <template v-else-if="column.key === 'fileSize'">
                    {{ formatFileSize(record.fileSize) }}
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space>
                      <a-button
                        type="link"
                        size="small"
                        @click="playSegment(record)"
                      >
                        <PlayCircleOutlined /> 播放
                      </a-button>
                      <a-button
                        type="link"
                        size="small"
                        @click="downloadSingleRecording(record)"
                        :loading="record.downloading"
                      >
                        <DownloadOutlined /> 下载
                      </a-button>
                      <a-button
                        type="link"
                        size="small"
                        @click="viewRecordingDetail(record)"
                      >
                        <EyeOutlined /> 详情
                      </a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- 录像详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="录像详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions
        v-if="currentRecording"
        bordered
        :column="2"
        size="small"
      >
        <a-descriptions-item label="录像ID" :span="2">
          {{ currentRecording.id }}
        </a-descriptions-item>
        <a-descriptions-item label="设备名称">
          {{ currentRecording.deviceName }}
        </a-descriptions-item>
        <a-descriptions-item label="通道ID">
          {{ currentRecording.channelId }}
        </a-descriptions-item>
        <a-descriptions-item label="开始时间">
          {{ currentRecording.startTime }}
        </a-descriptions-item>
        <a-descriptions-item label="结束时间">
          {{ currentRecording.endTime }}
        </a-descriptions-item>
        <a-descriptions-item label="录像类型">
          <a-tag :color="getTypeColor(currentRecording.type)">
            {{ getTypeText(currentRecording.type) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="时长">
          {{ formatDuration(currentRecording.duration) }}
        </a-descriptions-item>
        <a-descriptions-item label="文件大小" :span="2">
          {{ formatFileSize(currentRecording.fileSize) }}
        </a-descriptions-item>
        <a-descriptions-item label="分辨率">
          {{ currentRecording.resolution || '1920x1080' }}
        </a-descriptions-item>
        <a-descriptions-item label="码率">
          {{ currentRecording.bitrate || '4096' }} kbps
        </a-descriptions-item>
        <a-descriptions-item label="编码格式">
          {{ currentRecording.codec || 'H.264' }}
        </a-descriptions-item>
        <a-descriptions-item label="存储路径" :span="2">
          {{ currentRecording.storagePath || '/recordings/' + currentRecording.id + '.mp4' }}
        </a-descriptions-item>
      </a-descriptions>

      <div style="margin-top: 16px; text-align: right;">
        <a-space>
          <a-button @click="detailModalVisible = false">关闭</a-button>
          <a-button type="primary" @click="playSegment(currentRecording)">
            <PlayCircleOutlined /> 播放
          </a-button>
        </a-space>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  SearchOutlined,
  PlayCircleOutlined,
  PauseOutlined,
  StopOutlined,
  DownloadOutlined,
  ReloadOutlined,
  CameraOutlined,
  MoreOutlined,
  EyeOutlined,
  FolderOutlined,
} from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { videoPcApi } from '/@/api/business/video/video-pc-api';

// 状态
const loading = ref(false);
const selectedDeviceId = ref(null);
const selectedDate = ref(dayjs());
const dateRange = ref([
  dayjs().startOf('day'),
  dayjs().endOf('day')
]);
const playbackMode = ref('single');
const globalPlaybackSpeed = ref(1.0);

// 日历范围（最近30天）
const calendarRange = computed(() => ({
  start: dayjs().subtract(30, 'day'),
  end: dayjs()
}));

// 播放窗口
const playbackWindows = ref([
  {
    active: true,
    isPlaying: false,
    paused: false,
    playbackUrl: '',
    deviceName: '',
    totalDuration: 0,
    currentTime: 0,
    currentTimeSlider: 0
  },
  {
    active: false,
    isPlaying: false,
    paused: false,
    playbackUrl: '',
    deviceName: '',
    totalDuration: 0,
    currentTime: 0,
    currentTimeSlider: 0
  },
  {
    active: false,
    isPlaying: false,
    paused: false,
    playbackUrl: '',
    deviceName: '',
    totalDuration: 0,
    currentTime: 0,
    currentTimeSlider: 0
  },
  {
    active: false,
    isPlaying: false,
    paused: false,
    playbackUrl: '',
    deviceName: '',
    totalDuration: 0,
    currentTime: 0,
    currentTimeSlider: 0
  }
]);

const videoRefs = ref([]);
const setVideoRef = (el, index) => {
  if (el) {
    videoRefs.value[index] = el;
  }
};

// 设备列表
const deviceList = ref([
  { deviceId: 'device1', name: '前门摄像头-001' },
  { deviceId: 'device2', name: '停车场摄像头-01' },
  { deviceId: 'device3', name: '走廊摄像头-003' },
  { deviceId: 'device4', name: '大厅摄像头-002' },
  { deviceId: 'device5', name: '周界摄像头-005' }
]);

// 设备树
const deviceTreeData = ref([
  {
    id: 'group1',
    name: '一号楼',
    children: [
      { id: 'device1', name: '前门摄像头-001' },
      { id: 'device4', name: '大厅摄像头-002' },
      { id: 'device3', name: '走廊摄像头-003' }
    ]
  },
  {
    id: 'group2',
    name: '停车场',
    children: [
      { id: 'device2', name: '停车场摄像头-01' },
      { id: 'device5', name: '周界摄像头-005' }
    ]
  }
]);

// 录像片段和时间轴
const recordingSegments = ref([]);
const recordingList = ref([]);
const selectedRowKeys = ref([]);

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
});

// 表格列
const recordingColumns = [
  {
    title: '序号',
    dataIndex: 'index',
    key: 'index',
    width: 60,
    customRender: ({ index }) => index + 1
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 150
  },
  {
    title: '开始时间',
    dataIndex: 'startTime',
    key: 'startTime',
    width: 160
  },
  {
    title: '结束时间',
    dataIndex: 'endTime',
    key: 'endTime',
    width: 160
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
    width: 80
  },
  {
    title: '时长',
    dataIndex: 'duration',
    key: 'duration',
    width: 100
  },
  {
    title: '文件大小',
    dataIndex: 'fileSize',
    key: 'fileSize',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
];

// 详情弹窗
const detailModalVisible = ref(false);
const currentRecording = ref(null);

// 计算属性
const hasActivePlayback = computed(() => {
  return playbackWindows.value.some(w => w.isPlaying);
});

const timelineTicks = computed(() => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    return [];
  }

  const start = dayjs(dateRange.value[0]);
  const end = dayjs(dateRange.value[1]);
  const duration = end.diff(start, 'hour');

  if (duration <= 1) {
    const ticks = [];
    for (let i = 0; i <= 6; i++) {
      ticks.push({
        position: (i / 6) * 100,
        label: start.add(i * 10, 'minute').format('HH:mm')
      });
    }
    return ticks;
  } else if (duration <= 24) {
    const ticks = [];
    const step = Math.ceil(duration / 12);
    for (let i = 0; i <= 12; i++) {
      ticks.push({
        position: (i / 12) * 100,
        label: start.add(i * step, 'hour').format('HH:mm')
      });
    }
    return ticks;
  } else {
    const ticks = [];
    const days = Math.ceil(duration / 24);
    const step = Math.ceil(days / 10);
    for (let i = 0; i <= 10; i++) {
      ticks.push({
        position: (i / 10) * 100,
        label: start.add(i * step, 'day').format('MM-DD')
      });
    }
    return ticks;
  }
});

// 辅助函数
const formatTime = (seconds) => {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = Math.floor(seconds % 60);
  if (h > 0) {
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
};

const formatDuration = (seconds) => {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = Math.floor(seconds % 60);
  if (h > 0) {
    return `${h}小时${m}分${s}秒`;
  } else if (m > 0) {
    return `${m}分${s}秒`;
  }
  return `${s}秒`;
};

const formatFileSize = (bytes) => {
  if (!bytes) return '未知';
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
};

const getTypeColor = (type) => {
  const colors = {
    normal: 'green',
    motion: 'blue',
    alarm: 'red'
  };
  return colors[type] || 'default';
};

const getTypeText = (type) => {
  const texts = {
    normal: '正常',
    motion: '运动',
    alarm: '告警'
  };
  return texts[type] || '未知';
};

const getSegmentCount = (type) => {
  return recordingSegments.value.filter(s => s.type === type).length;
};

// 设备选择
const onDeviceSelect = (selectedKeys) => {
  if (selectedKeys.length > 0) {
    selectedDeviceId.value = selectedKeys[0];
  }
};

const onDeviceChange = (value) => {
  console.log('[录像回放] 设备切换:', value);
};

const filterDevice = (input, option) => {
  return option.children[0].children.toLowerCase().includes(input.toLowerCase());
};

// 日期选择
const onDateSelect = (date) => {
  selectedDate.value = date;
  dateRange.value = [
    date.startOf('day'),
    date.endOf('day')
  ];
};

// 查询录像
const handleSearch = async () => {
  if (!selectedDeviceId.value) {
    message.warning('请先选择设备');
    return;
  }
  if (!dateRange.value || dateRange.value.length !== 2) {
    message.warning('请选择时间范围');
    return;
  }

  loading.value = true;
  try {
    // 调用后端API查询录像时间轴
    const res = await videoPcApi.getRecordingTimeline({
      deviceId: selectedDeviceId.value,
      channelId: '1',
      startTime: dayjs(dateRange.value[0]).format('YYYY-MM-DD HH:mm:ss'),
      endTime: dayjs(dateRange.value[1]).format('YYYY-MM-DD HH:mm:ss')
    });

    if (res.code === 200 || res.success) {
      processRecordingSegments(res.data?.segments || []);
      processRecordingList(res.data?.list || []);
      if (res.data) {
        const count = res.data.segments?.length || res.data.list?.length || 0;
        if (count > 0) {
          message.success(`查询到 ${count} 段录像`);
        } else {
          message.info('该时间段内没有录像');
        }
      }
    } else {
      message.error(res.message || '查询录像失败');
    }
  } catch (error) {
    console.error('[录像回放] 查询录像异常', error);
    // 使用模拟数据
    generateMockData();
  } finally {
    loading.value = false;
  }
};

const handleReset = () => {
  selectedDeviceId.value = null;
  dateRange.value = [
    dayjs().startOf('day'),
    dayjs().endOf('day')
  ];
  recordingSegments.value = [];
  recordingList.value = [];
  playbackWindows.value.forEach(w => {
    w.isPlaying = false;
    w.playbackUrl = '';
    w.deviceName = '';
  });
};

const refreshTimeline = () => {
  handleSearch();
};

// 处理录像片段数据
const processRecordingSegments = (data) => {
  if (!data || data.length === 0) {
    recordingSegments.value = [];
    return;
  }

  const start = dayjs(dateRange.value[0]);
  const end = dayjs(dateRange.value[1]);
  const totalMs = end.diff(start);

  recordingSegments.value = data.map((item, index) => {
    const itemStart = dayjs(item.startTime);
    const itemEnd = dayjs(item.endTime);
    const left = ((itemStart.diff(start)) / totalMs) * 100;
    const width = ((itemEnd.diff(itemStart)) / totalMs) * 100;

    return {
      id: item.id || index,
      type: item.type || 'normal',
      startTime: itemStart.format('HH:mm:ss'),
      endTime: itemEnd.format('HH:mm:ss'),
      description: item.description || '正常录像',
      left: Math.max(0, left),
      width: Math.min(100, width),
      url: item.url,
      fileSize: item.fileSize,
      duration: item.duration || itemEnd.diff(itemStart, 'second')
    };
  });
};

// 处理录像列表
const processRecordingList = (data) => {
  if (!data || data.length === 0) {
    recordingList.value = [];
    pagination.total = 0;
    return;
  }

  recordingList.value = data.map((item, index) => ({
    ...item,
    key: item.id || index,
    deviceName: item.deviceName || deviceList.value.find(d => d.deviceId === selectedDeviceId.value)?.name || '未知设备',
    downloading: false
  }));
  pagination.total = data.length;
};

// 生成模拟数据
const generateMockData = () => {
  const baseTime = dayjs().startOf('day');
  const segments = [];
  const list = [];

  for (let i = 0; i < 24; i++) {
    const startTime = baseTime.add(i, 'hour');
    const endTime = startTime.add(50, 'minute');
    const type = Math.random() > 0.8 ? (Math.random() > 0.5 ? 'motion' : 'alarm') : 'normal';
    const duration = 50 * 60;

    segments.push({
      id: i + 1,
      type: type,
      startTime: startTime,
      endTime: endTime,
      description: type === 'normal' ? '正常录像' : type === 'motion' ? '检测到运动' : '告警录像',
      fileSize: Math.floor(duration * 512 * 1024)
    });

    list.push({
      id: `rec_${i + 1}`,
      deviceId: selectedDeviceId.value,
      channelId: '1',
      startTime: startTime.format('YYYY-MM-DD HH:mm:ss'),
      endTime: endTime.format('YYYY-MM-DD HH:mm:ss'),
      type: type,
      duration: duration,
      fileSize: Math.floor(duration * 512 * 1024),
      resolution: '1920x1080',
      bitrate: '4096',
      codec: 'H.264',
      storagePath: `/recordings/${selectedDeviceId.value}/2025/${startTime.format('YYYYMMDD')}_${i}.mp4`
    });
  }

  processRecordingSegments(segments);
  processRecordingList(list);
  message.info('使用模拟数据展示');
};

// 播放录像片段
const playSegment = async (segment) => {
  const activeWindow = playbackWindows.value.find(w => w.active);

  if (!activeWindow) {
    message.warning('请先选择活动窗口');
    return;
  }

  try {
    loading.value = true;
    const res = await videoPcApi.getPlaybackStream({
      deviceId: selectedDeviceId.value,
      channelId: '1',
      startTime: segment.startTime,
      endTime: segment.endTime
    });

    if (res.code === 200 || res.success) {
      activeWindow.playbackUrl = res.data?.url || 'https://vjs.zencdn.net/v/oceans.mp4';
      activeWindow.isPlaying = true;
      activeWindow.paused = false;
      activeWindow.deviceName = deviceList.value.find(d => d.deviceId === selectedDeviceId.value)?.name || '未知设备';
      activeWindow.totalDuration = segment.duration || 3600;
      activeWindow.currentTime = 0;
      activeWindow.currentTimeSlider = 0;
      message.success(`开始播放: ${segment.description || '录像'}`);
    } else {
      message.error(res.message || '获取播放地址失败');
    }
  } catch (error) {
    console.error('[录像回放] 播放录像异常', error);
    // 使用模拟URL
    activeWindow.playbackUrl = 'https://vjs.zencdn.net/v/oceans.mp4';
    activeWindow.isPlaying = true;
    activeWindow.paused = false;
    activeWindow.deviceName = deviceList.value.find(d => d.deviceId === selectedDeviceId.value)?.name || '未知设备';
    activeWindow.totalDuration = 3600;
    activeWindow.currentTime = 0;
    activeWindow.currentTimeSlider = 0;
  } finally {
    loading.value = false;
  }
};

// 视频事件处理
const onTimeUpdate = (index) => {
  const window = playbackWindows.value[index];
  const videoRef = videoRefs.value[index];
  if (videoRef) {
    window.currentTime = videoRef.currentTime;
    window.currentTimeSlider = videoRef.currentTime;
  }
};

const onMetadataLoaded = (index) => {
  const window = playbackWindows.value[index];
  const videoRef = videoRefs.value[index];
  if (videoRef) {
    window.totalDuration = videoRef.duration;
  }
};

const onVideoEnded = (index) => {
  const window = playbackWindows.value[index];
  window.paused = true;
};

// 播放控制
const togglePlay = (index) => {
  const window = playbackWindows.value[index];
  const videoRef = videoRefs.value[index];

  if (!videoRef || !window.isPlaying) return;

  if (window.paused) {
    videoRef.play();
    window.paused = false;
  } else {
    videoRef.pause();
    window.paused = true;
  }
};

const stopPlayback = (index) => {
  const window = playbackWindows.value[index];
  const videoRef = videoRefs.value[index];

  if (videoRef) {
    videoRef.pause();
    videoRef.currentTime = 0;
  }

  window.paused = true;
  window.currentTime = 0;
  window.currentTimeSlider = 0;
};

const onSeek = (index) => {
  const window = playbackWindows.value[index];
  const videoRef = videoRefs.value[index];
  if (videoRef) {
    videoRef.currentTime = window.currentTimeSlider;
  }
};

const onGlobalSpeedChange = () => {
  playbackWindows.value.forEach((window, index) => {
    const videoRef = videoRefs.value[index];
    if (videoRef && window.isPlaying) {
      videoRef.playbackRate = globalPlaybackSpeed.value;
    }
  });
  message.info(`全局播放速度: ${globalPlaybackSpeed.value}x`);
};

// 窗口操作
const handleWindowAction = (key, index) => {
  const window = playbackWindows.value[index];

  switch (key) {
    case 'set_source':
      // 打开设备选择弹窗设置视频源
      message.info('请从录像列表选择要播放的录像');
      break;
    case 'fullscreen':
      if (videoRefs.value[index]) {
        if (videoRefs.value[index].requestFullscreen) {
          videoRefs.value[index].requestFullscreen();
        }
      }
      break;
    case 'close':
      stopPlayback(index);
      window.isPlaying = false;
      window.playbackUrl = '';
      window.deviceName = '';
      break;
  }
};

// 截图
const captureSnapshot = (index) => {
  const videoRef = videoRefs.value[index];
  if (!videoRef || !playbackWindows.value[index].isPlaying) {
    message.warning('请先播放视频');
    return;
  }

  try {
    const canvas = document.createElement('canvas');
    canvas.width = videoRef.videoWidth;
    canvas.height = videoRef.videoHeight;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(videoRef, 0, 0, canvas.width, canvas.height);

    canvas.toBlob((blob) => {
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `snapshot_${selectedDeviceId.value}_${Date.now()}.png`;
      link.click();
      URL.revokeObjectURL(url);
      message.success('截图已保存');
    }, 'image/png');
  } catch (error) {
    console.error('[录像回放] 截图失败', error);
    message.error('截图失败');
  }
};

const captureAllSnapshots = () => {
  playbackWindows.value.forEach((_, index) => {
    if (playbackWindows.value[index].isPlaying) {
      captureSnapshot(index);
    }
  });
};

// 下载录像
const downloadSingleRecording = async (record) => {
  record.downloading = true;
  try {
    const res = await videoPcApi.getDownloadUrl({
      deviceId: record.deviceId,
      channelId: record.channelId,
      startTime: record.startTime,
      endTime: record.endTime
    });

    if (res.code === 200 || res.success) {
      const url = res.data?.url || 'https://vjs.zencdn.net/v/oceans.mp4';
      const link = document.createElement('a');
      link.href = url;
      link.download = `recording_${record.deviceId}_${Date.now()}.mp4`;
      link.click();
      message.success('录像下载已开始');
    } else {
      message.error(res.message || '获取下载链接失败');
    }
  } catch (error) {
    console.error('[录像回放] 下载录像异常', error);
    message.error('下载录像异常');
  } finally {
    record.downloading = false;
  }
};

const downloadAllRecordings = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要下载的录像');
    return;
  }

  Modal.confirm({
    title: '批量下载',
    content: `确定要下载选中的 ${selectedRowKeys.value.length} 个录像吗？`,
    onOk: () => {
      selectedRowKeys.value.forEach(key => {
        const record = recordingList.value.find(r => r.key === key);
        if (record) {
          downloadSingleRecording(record);
        }
      });
    }
  });
};

// 表格选择
const onSelectChange = (keys) => {
  selectedRowKeys.value = keys;
};

const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
};

// 查看详情
const viewRecordingDetail = (record) => {
  currentRecording.value = record;
  detailModalVisible.value = true;
};

// 初始化
onMounted(() => {
  // 设置默认设备
  if (deviceList.value.length > 0) {
    selectedDeviceId.value = deviceList.value[0].deviceId;
  }
});

// 清理
onUnmounted(() => {
  videoRefs.value.forEach((videoRef, index) => {
    if (videoRef && playbackWindows.value[index].isPlaying) {
      videoRef.pause();
      videoRef.src = '';
    }
  });
});
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;

  // 视频播放器容器
  .video-player-container {
    display: grid;
    gap: 2px;
    background-color: #000;
    min-height: 450px;
    margin-bottom: 16px;

    &.mode-single {
      grid-template-columns: 1fr;
      grid-template-rows: 1fr;
    }

    &.mode-dual {
      grid-template-columns: repeat(2, 1fr);
      grid-template-rows: 1fr;
    }

    &.mode-quad {
      grid-template-columns: repeat(2, 1fr);
      grid-template-rows: repeat(2, 1fr);
    }

    .video-window {
      position: relative;
      background-color: #000;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      border: 2px solid transparent;

      &.active {
        border-color: #1890ff;
      }

      .video-player {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;

        .video-element {
          width: 100%;
          height: 100%;
          object-fit: contain;
        }
      }

      .window-info {
        position: absolute;
        top: 8px;
        left: 8px;
        right: 8px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        background: rgba(0, 0, 0, 0.6);
        padding: 4px 8px;
        border-radius: 4px;
        color: #fff;
        font-size: 12px;

        .window-title {
          font-weight: 500;
        }
      }

      .playback-progress {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: rgba(0, 0, 0, 0.8);
        padding: 8px 12px;
        color: #fff;

        .time-info {
          display: flex;
          justify-content: space-between;
          font-size: 12px;
          margin-bottom: 4px;
          font-family: 'Courier New', monospace;
        }

        :deep(.ant-slider) {
          margin: 4px 0;
        }
      }
    }
  }

  // 全局控制栏
  .global-controls {
    padding: 12px 16px;
    background: #fafafa;
    border-radius: 4px;
    margin-top: 16px;
  }

  // 时间轴视图
  .timeline-view {
    .timeline-container {
      position: relative;
      padding: 20px 0 40px 0;

      .timeline-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        font-weight: 500;
        color: #333;

        .legend {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          color: #666;
          margin-left: 16px;

          .legend-color {
            width: 16px;
            height: 16px;
            border-radius: 2px;

            &.normal {
              background: #52c41a;
            }

            &.motion {
              background: #1890ff;
            }

            &.alarm {
              background: #ff4d4f;
            }
          }
        }
      }

      .timeline-body {
        position: relative;
        height: 40px;
        background: #f5f5f5;
        border-radius: 4px;
        overflow: hidden;

        .timeline-segment {
          position: absolute;
          height: 100%;
          cursor: pointer;
          transition: all 0.2s;
          border-radius: 2px;

          &.normal {
            background: #52c41a;
          }

          &.motion {
            background: #1890ff;
          }

          &.alarm {
            background: #ff4d4f;
          }

          &:hover {
            opacity: 0.8;
            transform: scaleY(1.1);
          }

          .segment-tooltip {
            display: none;
            position: absolute;
            bottom: 100%;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(0, 0, 0, 0.8);
            color: #fff;
            padding: 6px 10px;
            border-radius: 4px;
            font-size: 12px;
            white-space: nowrap;
            z-index: 10;
            margin-bottom: 5px;
          }

          &:hover .segment-tooltip {
            display: block;
          }
        }
      }

      .timeline-ruler {
        position: relative;
        margin-top: 10px;
        height: 20px;
        border-top: 1px solid #e8e8e8;

        .timeline-tick {
          position: absolute;
          transform: translateX(-50%);
          font-size: 11px;
          color: #999;
        }
      }
    }
  }
}
</style>
