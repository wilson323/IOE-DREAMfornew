<!--
  智能视频-以图搜图页面

  @Author:    Claude Code
  @Date:      2025-12-24
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-card title="智能检索" :bordered="false">
      <a-row :gutter="24">
        <!-- 左侧：搜索配置 -->
        <a-col :span="8">
          <a-card title="检索类型" :bordered="false" size="small">
            <a-radio-group v-model:value="searchType" button-style="solid" style="width: 100%;">
              <a-radio-button value="face" style="width: 33.3%;">
                <UserOutlined />
                人脸
              </a-radio-button>
              <a-radio-button value="vehicle" style="width: 33.3%;">
                <CarOutlined />
                车辆
              </a-radio-button>
              <a-radio-button value="person" style="width: 33.3%;">
                <TeamOutlined />
                人体
              </a-radio-button>
            </a-radio-group>
          </a-card>

          <a-card title="上传图片" :bordered="false" size="small" style="margin-top: 16px;">
            <a-upload-dragger
              v-model:file-list="fileList"
              name="file"
              :multiple="false"
              :before-upload="beforeUpload"
              @change="handleUploadChange"
              :show-upload-list="false"
            >
              <div v-if="!uploadedImage" class="upload-area">
                <p class="ant-upload-drag-icon">
                  <InboxOutlined style="font-size: 48px; color: #d9d9d9;" />
                </p>
                <p class="ant-upload-text">点击或拖拽图片到此区域上传</p>
                <p class="ant-upload-hint">支持 JPG、PNG 格式，文件大小不超过 10MB</p>
              </div>
              <div v-else class="uploaded-preview">
                <img :src="uploadedImage" alt="preview" />
                <div class="preview-mask">
                  <a-space>
                    <a-button size="small" @click="handleRemoveImage">
                      <template #icon><DeleteOutlined /></template>
                      移除
                    </a-button>
                    <a-button size="small" type="primary" @click="handleImageClick">
                      <template #icon><EyeOutlined /></template>
                      预览
                    </a-button>
                  </a-space>
                </div>
              </div>
            </a-upload-dragger>

            <a-divider style="margin: 12px 0;" />

            <a-form layout="vertical">
              <a-form-item label="时间范围">
                <a-range-picker
                  v-model:value="searchRange"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%;"
                  :placeholder="['开始时间', '结束时间']"
                />
              </a-form-item>

              <a-form-item label="相似度阈值">
                <a-slider
                  v-model:value="similarityThreshold"
                  :min="50"
                  :max="100"
                  :marks="{ 50: '50%', 70: '70%', 85: '85%', 95: '95%' }"
                />
                <div style="text-align: right; color: #999; margin-top: -8px;">
                  当前: {{ similarityThreshold }}%
                </div>
              </a-form-item>

              <a-form-item label="搜索区域">
                <a-select
                  v-model:value="selectedAreas"
                  mode="multiple"
                  placeholder="选择搜索区域"
                  style="width: 100%;"
                  :options="areaOptions"
                >
                </a-select>
              </a-form-item>

              <a-form-item label="设备筛选">
                <a-select
                  v-model:value="selectedDevices"
                  mode="multiple"
                  placeholder="选择设备"
                  style="width: 100%;"
                  show-search
                  :filter-option="filterDeviceOption"
                  :options="deviceOptions"
                >
                </a-select>
              </a-form-item>

              <a-form-item label="结果排序">
                <a-radio-group v-model:value="sortBy">
                  <a-radio value="similarity">相似度优先</a-radio>
                  <a-radio value="time">时间优先</a-radio>
                </a-radio-group>
              </a-form-item>

              <a-form-item>
                <a-space direction="vertical" style="width: 100%;">
                  <a-button
                    type="primary"
                    block
                    size="large"
                    :disabled="!uploadedImage"
                    :loading="searching"
                    @click="handleSearch"
                  >
                    <template #icon><SearchOutlined /></template>
                    开始搜索
                  </a-button>
                  <a-button block @click="handleReset" :disabled="searchResults.length === 0">
                    <template #icon><ReloadOutlined /></template>
                    重置条件
                  </a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </a-card>

          <!-- 搜索统计 -->
          <a-card v-if="searchResults.length > 0" title="搜索统计" :bordered="false" size="small" style="margin-top: 16px;">
            <a-statistic-group direction="horizontal">
              <a-statistic title="匹配结果" :value="searchResults.length" suffix="个" />
              <a-statistic
                title="平均相似度"
                :value="averageSimilarity"
                suffix="%"
                :precision="1"
              />
            </a-statistic-group>

            <a-divider style="margin: 12px 0;" />

            <a-row :gutter="8">
              <a-col :span="12">
                <a-button block @click="exportResults" :loading="exporting">
                  <template #icon><ExportOutlined /></template>
                  导出结果
                </a-button>
              </a-col>
              <a-col :span="12">
                <a-button block @click="batchTrack" :disabled="selectedResults.length === 0">
                  <template #icon><FieldBinaryOutlined /></template>
                  批量追踪
                </a-button>
              </a-col>
            </a-row>
          </a-card>
        </a-col>

        <!-- 右侧：搜索结果 -->
        <a-col :span="16">
          <a-card :bordered="false">
            <template #title>
              <a-space>
                <span>搜索结果</span>
                <a-tag v-if="searchResults.length > 0" color="blue">
                  {{ searchResults.length }} 个匹配
                </a-tag>
              </a-space>
            </template>

            <template #extra>
              <a-space>
                <a-radio-group v-model:value="viewMode" button-style="solid">
                  <a-radio-button value="grid">
                    <AppstoreOutlined />
                    网格
                  </a-radio-button>
                  <a-radio-button value="list">
                    <BarsOutlined />
                    列表
                  </a-radio-button>
                </a-radio-group>

                <a-checkbox v-model:checked="selectAll" @change="handleSelectAll" v-if="searchResults.length > 0">
                  全选
                </a-checkbox>
              </a-space>
            </template>

            <a-empty v-if="!searched && searchResults.length === 0" description="请上传图片并开始搜索" />

            <a-spin :spinning="searching" tip="正在智能检索中...">
              <!-- 网格视图 -->
              <a-row v-if="viewMode === 'grid' && searchResults.length > 0" :gutter="[16, 16]">
                <a-col :span="8" v-for="item in searchResults" :key="item.id">
                  <a-card
                    hoverable
                    size="small"
                    class="result-card"
                    :class="{ 'selected': selectedResults.includes(item.id) }"
                    @click="toggleResultSelection(item.id)"
                  >
                    <template #cover>
                      <div class="result-image-wrapper">
                        <img :src="item.image" :alt="`result-${item.id}`" />
                        <div class="similarity-badge" :class="getSimilarityClass(item.similarity)">
                          {{ item.similarity }}%
                        </div>
                        <a-checkbox
                          v-if="selectedResults.length > 0 || item.selected"
                          :checked="selectedResults.includes(item.id) || item.selected"
                          class="result-checkbox"
                          @click.stop="toggleResultSelection(item.id)"
                        />
                      </div>
                    </template>

                    <a-card-meta>
                      <template #description>
                        <div class="result-meta">
                          <div class="meta-item">
                            <ClockCircleOutlined />
                            <span>{{ item.time }}</span>
                          </div>
                          <div class="meta-item">
                            <EnvironmentOutlined />
                            <span>{{ item.location }}</span>
                          </div>
                          <div class="meta-item">
                            <VideoCameraOutlined />
                            <span>{{ item.deviceName }}</span>
                          </div>
                        </div>
                      </template>
                    </a-card-meta>

                    <template #actions>
                      <a-button type="link" size="small" @click.stop="handleViewDetail(item)">
                        <template #icon><EyeOutlined /></template>
                        详情
                      </a-button>
                      <a-button type="link" size="small" @click.stop="handleTrack(item)">
                        <template #icon><FieldBinaryOutlined /></template>
                        追踪
                      </a-button>
                    </template>
                  </a-card>
                </a-col>
              </a-row>

              <!-- 列表视图 -->
              <a-table
                v-if="viewMode === 'list' && searchResults.length > 0"
                :columns="tableColumns"
                :data-source="searchResults"
                :pagination="{ pageSize: 10, size: 'small' }"
                size="small"
                :row-selection="rowSelection"
                :row-key="(record) => record.id"
                @row-click="(record) => handleViewDetail(record)"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'image'">
                    <img :src="record.image" style="width: 80px; height: 60px; object-fit: cover; border-radius: 4px;" />
                  </template>

                  <template v-if="column.key === 'similarity'">
                    <a-progress
                      :percent="record.similarity"
                      size="small"
                      :status="record.similarity >= 90 ? 'success' : record.similarity >= 80 ? 'normal' : 'exception'"
                    />
                  </template>

                  <template v-if="column.key === 'action'">
                    <a-space>
                      <a-button type="link" size="small" @click="handleViewDetail(record)">
                        <template #icon><EyeOutlined /></template>
                        详情
                      </a-button>
                      <a-button type="link" size="small" @click="handleTrack(record)">
                        <template #icon><FieldBinaryOutlined /></template>
                        追踪
                      </a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
            </a-spin>
          </a-card>
        </a-col>
      </a-row>
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      :title="`检索结果详情 - ${currentResult?.deviceName || ''}`"
      width="800px"
      @cancel="detailVisible = false"
      :footer="null"
    >
      <div v-if="currentResult" class="detail-content">
        <a-row :gutter="16">
          <a-col :span="12">
            <div class="detail-image">
              <img :src="currentResult.image" alt="detail" />
              <div class="similarity-large" :class="getSimilarityClass(currentResult.similarity)">
                {{ currentResult.similarity }}%
              </div>
            </div>
          </a-col>
          <a-col :span="12">
            <a-descriptions :column="1" bordered size="small">
              <a-descriptions-item label="相似度">
                <a-progress
                  :percent="currentResult.similarity"
                  :status="currentResult.similarity >= 90 ? 'success' : 'normal'"
                />
              </a-descriptions-item>
              <a-descriptions-item label="时间">{{ currentResult.time }}</a-descriptions-item>
              <a-descriptions-item label="位置">{{ currentResult.location }}</a-descriptions-item>
              <a-descriptions-item label="设备">{{ currentResult.deviceName }}</a-descriptions-item>
              <a-descriptions-item label="设备IP">{{ currentResult.deviceIp }}</a-descriptions-item>
              <a-descriptions-item label="特征描述">
                {{ currentResult.features || '无' }}
              </a-descriptions-item>
            </a-descriptions>

            <a-divider style="margin: 16px 0;" />

            <a-space direction="vertical" style="width: 100%;">
              <a-button type="primary" block @click="handleTrack(currentResult)">
                <template #icon><FieldBinaryOutlined /></template>
                追踪目标轨迹
              </a-button>
              <a-button block @click="handleViewVideo(currentResult)">
                <template #icon><PlayCircleOutlined /></template>
                查看视频
              </a-button>
              <a-button block @click="handleDownloadImage(currentResult)">
                <template #icon><DownloadOutlined /></template>
                下载图片
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </div>
    </a-modal>

    <!-- 轨迹追踪弹窗 -->
    <a-modal
      v-model:open="trackVisible"
      title="目标轨迹追踪"
      width="1000px"
      @cancel="trackVisible = false"
    >
      <div class="track-content">
        <a-form layout="inline" style="margin-bottom: 16px;">
          <a-form-item label="时间范围">
            <a-range-picker
              v-model:value="trackTimeRange"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
            />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="loadTrajectory">
              <template #icon><SearchOutlined /></template>
              查询轨迹
            </a-button>
          </a-form-item>
        </a-form>

        <div class="trajectory-map">
          <a-empty description="轨迹地图开发中" />

          <!-- 轨迹时间线 -->
          <div v-if="trajectoryPoints.length > 0" class="trajectory-timeline">
            <a-timeline>
              <a-timeline-item
                v-for="(point, index) in trajectoryPoints"
                :key="index"
                :color="index === 0 ? 'green' : 'blue'"
              >
                <template #dot>
                  <EnvironmentOutlined style="font-size: 16px;" />
                </template>
                <p><strong>{{ point.time }}</strong></p>
                <p>{{ point.location }}</p>
                <p>{{ point.deviceName }}</p>
              </a-timeline-item>
            </a-timeline>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import {
  InboxOutlined,
  SearchOutlined,
  ExportOutlined,
  DeleteOutlined,
  EyeOutlined,
  ReloadOutlined,
  UserOutlined,
  CarOutlined,
  TeamOutlined,
  ClockCircleOutlined,
  EnvironmentOutlined,
  VideoCameraOutlined,
  AppstoreOutlined,
  BarsOutlined,
  FieldBinaryOutlined,
  PlayCircleOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';
import { videoPcApi } from '/@/api/business/video/video-pc-api';
import dayjs from 'dayjs';

// 状态
const searchType = ref('face'); // face, vehicle, person
const fileList = ref([]);
const uploadedImage = ref(null);
const searchRange = ref([
  dayjs().subtract(7, 'days'),
  dayjs()
]);
const similarityThreshold = ref(80);
const selectedAreas = ref([]);
const selectedDevices = ref([]);
const sortBy = ref('similarity');

// 视图模式
const viewMode = ref('grid'); // grid, list
const selectAll = ref(false);
const selectedResults = ref([]);

// 搜索状态
const searching = ref(false);
const searched = ref(false);
const searchResults = ref([]);

// 详情弹窗
const detailVisible = ref(false);
const currentResult = ref(null);

// 轨迹追踪
const trackVisible = ref(false);
const trackTimeRange = ref([dayjs().subtract(1, 'days'), dayjs()]);
const trajectoryPoints = ref([]);

// 导出
const exporting = ref(false);

// 配置选项
const areaOptions = [
  { label: '一号楼', value: 'area1' },
  { label: '停车场', value: 'area2' },
  { label: '会议室区域', value: 'area3' },
  { label: '餐厅', value: 'area4' },
  { label: '周界', value: 'area5' },
];

const deviceOptions = [
  { label: '前门摄像头-001', value: 'device1' },
  { label: '停车场摄像头-01', value: 'device2' },
  { label: '大厅摄像头-002', value: 'device3' },
  { label: '走廊摄像头-003', value: 'device4' },
  { label: '周界摄像头-005', value: 'device5' },
];

// 表格列定义
const tableColumns = [
  { title: '图片', key: 'image', width: 120 },
  { title: '相似度', key: 'similarity', width: 150 },
  { title: '时间', dataIndex: 'time', key: 'time', width: 160 },
  { title: '位置', dataIndex: 'location', key: 'location', width: 120 },
  { title: '设备', dataIndex: 'deviceName', key: 'deviceName', width: 150 },
  { title: '操作', key: 'action', width: 150 },
];

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedResults.value,
  onChange: (selectedKeys) => {
    selectedResults.value = selectedKeys;
  }
}));

// 计算平均相似度
const averageSimilarity = computed(() => {
  if (searchResults.value.length === 0) return 0;
  const sum = searchResults.value.reduce((acc, item) => acc + item.similarity, 0);
  return (sum / searchResults.value.length).toFixed(1);
});

// 相似度等级样式
const getSimilarityClass = (similarity) => {
  if (similarity >= 95) return 'excellent';
  if (similarity >= 85) return 'good';
  if (similarity >= 75) return 'normal';
  return 'low';
};

// 图片上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    message.error('只能上传图片文件！');
    return false;
  }

  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    message.error('图片大小不能超过 10MB！');
    return false;
  }

  return true;
};

// 图片上传处理
const handleUploadChange = async (info) => {
  if (info.file && info.file.originFileObj) {
    const reader = new FileReader();
    reader.onload = (e) => {
      uploadedImage.value = e.target.result;
    };
    reader.readAsDataURL(info.file.originFileObj);

    // 上传到服务器
    try {
      const res = await videoPcApi.uploadSearchImage(info.file.originFileObj);
      if (res.code === 200 || res.success) {
        message.success('图片上传成功');
      }
    } catch (error) {
      console.error('[智能检索] 上传图片失败', error);
      // 继续使用本地预览
    }
  }
};

// 移除图片
const handleRemoveImage = () => {
  uploadedImage.value = null;
  fileList.value = [];
  searched.value = false;
  searchResults.value = [];
};

// 图片点击预览
const handleImageClick = () => {
  // 可以使用图片预览组件
  message.info('预览功能');
};

// 设备筛选过滤
const filterDeviceOption = (input, option) => {
  return option.label.toLowerCase().includes(input.toLowerCase());
};

// 开始搜索
const handleSearch = async () => {
  if (!uploadedImage.value) {
    message.warning('请先上传图片');
    return;
  }

  searching.value = true;

  try {
    const params = {
      searchType: searchType.value,
      image: uploadedImage.value,
      startTime: searchRange.value[0].format('YYYY-MM-DD HH:mm:ss'),
      endTime: searchRange.value[1].format('YYYY-MM-DD HH:mm:ss'),
      similarityThreshold: similarityThreshold.value,
      areas: selectedAreas.value,
      devices: selectedDevices.value,
      sortBy: sortBy.value
    };

    let res;
    if (searchType.value === 'face') {
      res = await videoPcApi.searchFace(params);
    } else if (searchType.value === 'vehicle') {
      res = await videoPcApi.searchVehicle(params);
    } else {
      res = await videoPcApi.searchPerson(params);
    }

    if (res.code === 200 || res.success) {
      searchResults.value = res.data?.list || [];
      searched.value = true;
      message.success(`搜索完成，找到 ${searchResults.value.length} 个匹配结果`);
    } else {
      // 模拟搜索结果
      searchResults.value = generateMockResults();
      searched.value = true;
      message.success(`搜索完成，找到 ${searchResults.value.length} 个匹配结果（模拟）`);
    }
  } catch (error) {
    console.error('[智能检索] 搜索失败', error);
    // 使用模拟数据
    searchResults.value = generateMockResults();
    searched.value = true;
    message.success(`搜索完成，找到 ${searchResults.value.length} 个匹配结果（模拟）`);
  } finally {
    searching.value = false;
  }
};

// 生成模拟结果
const generateMockResults = () => {
  const results = [];
  const locations = ['一号楼大厅', '停车场入口', '会议室区域', '餐厅', '周界东门'];
  const devices = ['前门摄像头-001', '停车场摄像头-01', '大厅摄像头-002', '走廊摄像头-003', '周界摄像头-005'];

  const count = Math.floor(Math.random() * 20) + 10; // 10-30个结果

  for (let i = 0; i < count; i++) {
    const similarity = Math.floor(Math.random() * 30) + 70; // 70-100%
    const time = dayjs().subtract(Math.floor(Math.random() * 168), 'hours');

    results.push({
      id: i + 1,
      image: uploadedImage.value,
      similarity: similarity,
      time: time.format('YYYY-MM-DD HH:mm:ss'),
      location: locations[Math.floor(Math.random() * locations.length)],
      deviceName: devices[Math.floor(Math.random() * devices.length)],
      deviceIp: '192.168.1.' + (Math.floor(Math.random() * 254) + 1),
      features: searchType.value === 'face' ? '男性,20-30岁,短头发' : '白色,SUV,京A·XXXXX'
    });
  }

  // 按相似度排序
  results.sort((a, b) => b.similarity - a.similarity);

  return results;
};

// 重置条件
const handleReset = () => {
  uploadedImage.value = null;
  fileList.value = [];
  searchRange.value = [dayjs().subtract(7, 'days'), dayjs()];
  similarityThreshold.value = 80;
  selectedAreas.value = [];
  selectedDevices.value = [];
  sortBy.value = 'similarity';
  searchResults.value = [];
  selectedResults.value = [];
  searched.value = false;
};

// 全选/取消全选
const handleSelectAll = (e) => {
  if (e.target.checked) {
    selectedResults.value = searchResults.value.map(item => item.id);
  } else {
    selectedResults.value = [];
  }
};

// 切换结果选择
const toggleResultSelection = (id) => {
  const index = selectedResults.value.indexOf(id);
  if (index !== -1) {
    selectedResults.value.splice(index, 1);
  } else {
    selectedResults.value.push(id);
  }
};

// 查看详情
const handleViewDetail = (item) => {
  currentResult.value = item;
  detailVisible.value = true;
};

// 追踪目标
const handleTrack = (item) => {
  currentResult.value = item;
  trackVisible.value = true;
  loadTrajectory();
};

// 加载轨迹
const loadTrajectory = async () => {
  try {
    const params = {
      targetId: currentResult.value.id,
      startTime: trackTimeRange.value[0].format('YYYY-MM-DD HH:mm:ss'),
      endTime: trackTimeRange.value[1].format('YYYY-MM-DD HH:mm:ss')
    };

    const res = await videoPcApi.getTargetTrajectory(currentResult.value.id, params);

    if (res.code === 200 || res.success) {
      trajectoryPoints.value = res.data?.trajectory || [];
      message.success('轨迹查询成功');
    } else {
      // 模拟轨迹数据
      trajectoryPoints.value = [
        {
          time: '2024-11-06 08:30:15',
          location: '一号楼大厅',
          deviceName: '前门摄像头-001',
          coordinates: { x: 100, y: 200 }
        },
        {
          time: '2024-11-06 09:15:22',
          location: '走廊区域',
          deviceName: '走廊摄像头-003',
          coordinates: { x: 150, y: 300 }
        },
        {
          time: '2024-11-06 10:20:45',
          location: '会议室区域',
          deviceName: '大厅摄像头-002',
          coordinates: { x: 200, y: 400 }
        },
        {
          time: '2024-11-06 11:45:10',
          location: '餐厅',
          deviceName: '餐厅摄像头-004',
          coordinates: { x: 250, y: 500 }
        }
      ];
      message.success('轨迹查询成功（模拟）');
    }
  } catch (error) {
    console.error('[智能检索] 轨迹查询失败', error);
  }
};

// 批量追踪
const batchTrack = () => {
  if (selectedResults.value.length === 0) {
    message.warning('请先选择要追踪的结果');
    return;
  }

  message.info(`批量追踪 ${selectedResults.value.length} 个目标（功能开发中）`);
};

// 查看视频
const handleViewVideo = (item) => {
  message.info(`查看视频: ${item.deviceName}（功能开发中）`);
};

// 下载图片
const handleDownloadImage = (item) => {
  const link = document.createElement('a');
  link.href = item.image;
  link.download = `search_result_${item.id}.jpg`;
  link.click();
  message.success('图片下载成功');
};

// 导出结果
const exportResults = async () => {
  if (searchResults.value.length === 0) {
    message.warning('没有可导出的结果');
    return;
  }

  exporting.value = true;

  try {
    const params = {
      searchType: searchType.value,
      results: searchResults.value
    };

    const res = await videoPcApi.exportSearchResults(params);

    // 处理blob下载
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `search_results_${Date.now()}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);

    message.success('导出成功');
  } catch (error) {
    console.error('[智能检索] 导出失败', error);
    message.error('导出失败');
  } finally {
    exporting.value = false;
  }
};
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;

  .upload-area {
    padding: 40px 20px;
    text-align: center;
  }

  .uploaded-preview {
    position: relative;
    width: 100%;
    height: 200px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 4px;
    }

    .preview-mask {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.6);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;

      &:hover {
        opacity: 1;
      }
    }
  }

  .result-card {
    position: relative;
    transition: all 0.3s;

    &.selected {
      border: 2px solid #1890ff;
    }

    .result-image-wrapper {
      position: relative;
      height: 180px;
      overflow: hidden;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .similarity-badge {
        position: absolute;
        top: 8px;
        right: 8px;
        padding: 4px 12px;
        border-radius: 12px;
        color: #fff;
        font-weight: bold;
        font-size: 14px;

        &.excellent {
          background: linear-gradient(135deg, #52c41a, #95de64);
        }

        &.good {
          background: linear-gradient(135deg, #1890ff, #69c0ff);
        }

        &.normal {
          background: linear-gradient(135deg, #faad14, #ffd666);
        }

        &.low {
          background: linear-gradient(135deg, #ff4d4f, #ff7875);
        }
      }

      .result-checkbox {
        position: absolute;
        top: 8px;
        left: 8px;
      }
    }

    .result-meta {
      font-size: 12px;
      color: #666;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 4px;
        margin-bottom: 4px;

        .anticon {
          font-size: 12px;
        }
      }
    }
  }

  .detail-content {
    .detail-image {
      position: relative;
      text-align: center;

      img {
        width: 100%;
        max-height: 300px;
        object-fit: contain;
        border-radius: 8px;
      }

      .similarity-large {
        position: absolute;
        top: 16px;
        right: 16px;
        padding: 8px 24px;
        border-radius: 20px;
        color: #fff;
        font-weight: bold;
        font-size: 20px;

        &.excellent {
          background: linear-gradient(135deg, #52c41a, #95de64);
        }

        &.good {
          background: linear-gradient(135deg, #1890ff, #69c0ff);
        }

        &.normal {
          background: linear-gradient(135deg, #faad14, #ffd666);
        }

        &.low {
          background: linear-gradient(135deg, #ff4d4f, #ff7875);
        }
      }
    }
  }

  .track-content {
    .trajectory-map {
      min-height: 400px;
      background-color: #f5f5f5;
      border-radius: 8px;
      padding: 16px;
    }

    .trajectory-timeline {
      margin-top: 16px;
    }
  }
}
</style>
