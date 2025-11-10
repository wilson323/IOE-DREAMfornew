<!--
  智能视频-以图搜图页面

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-card title="以图搜图" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="8">
          <a-card title="上传图片" :bordered="false" size="small">
            <a-upload-dragger
              v-model:file-list="fileList"
              name="file"
              :multiple="false"
              :before-upload="beforeUpload"
              @change="handleUploadChange"
            >
              <p class="ant-upload-drag-icon">
                <InboxOutlined />
              </p>
              <p class="ant-upload-text">点击或拖拽图片到此区域上传</p>
              <p class="ant-upload-hint">支持 JPG、PNG 格式，文件大小不超过 10MB</p>
            </a-upload-dragger>

            <a-divider />

            <a-form layout="vertical">
              <a-form-item label="搜索范围">
                <a-range-picker v-model:value="searchRange" show-time style="width: 100%;" />
              </a-form-item>

              <a-form-item label="相似度阈值">
                <a-slider v-model:value="similarityThreshold" :min="60" :max="100" :marks="{ 60: '60%', 80: '80%', 100: '100%' }" />
              </a-form-item>

              <a-form-item label="搜索区域">
                <a-checkbox-group v-model:value="searchAreas">
                  <a-row>
                    <a-col :span="24" v-for="area in areaOptions" :key="area.value" style="margin-bottom: 8px;">
                      <a-checkbox :value="area.value">{{ area.label }}</a-checkbox>
                    </a-col>
                  </a-row>
                </a-checkbox-group>
              </a-form-item>

              <a-form-item>
                <a-button type="primary" block :disabled="!uploadedImage" @click="handleSearch">
                  <SearchOutlined />
                  开始搜索
                </a-button>
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>

        <a-col :span="16">
          <a-card title="搜索结果" :bordered="false">
            <template #extra>
              <a-space>
                <span v-if="searchResults.length > 0">找到 {{ searchResults.length }} 个相似目标</span>
                <a-button @click="handleExport" :disabled="searchResults.length === 0">
                  <ExportOutlined />
                  导出结果
                </a-button>
              </a-space>
            </template>

            <a-empty v-if="searchResults.length === 0" description="请上传图片并开始搜索" />

            <a-row :gutter="[16, 16]" v-else>
              <a-col :span="8" v-for="item in searchResults" :key="item.id">
                <a-card hoverable size="small" class="result-card">
                  <div class="result-image">
                    <a-image
                      :src="item.image"
                      fallback="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
                    />
                  </div>
                  <div class="result-info">
                    <a-descriptions :column="1" size="small">
                      <a-descriptions-item label="相似度">
                        <a-progress :percent="item.similarity" size="small" :status="item.similarity >= 90 ? 'success' : 'normal'" />
                      </a-descriptions-item>
                      <a-descriptions-item label="时间">
                        {{ item.time }}
                      </a-descriptions-item>
                      <a-descriptions-item label="位置">
                        {{ item.location }}
                      </a-descriptions-item>
                      <a-descriptions-item label="设备">
                        {{ item.deviceName }}
                      </a-descriptions-item>
                    </a-descriptions>
                    <a-button type="link" size="small" block @click="handleViewDetail(item)">
                      查看详情
                    </a-button>
                  </div>
                </a-card>
              </a-col>
            </a-row>
          </a-card>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { InboxOutlined, SearchOutlined, ExportOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';

const fileList = ref([]);
const uploadedImage = ref(null);
const searchRange = ref([]);
const similarityThreshold = ref(80);
const searchAreas = ref(['area1']);

const areaOptions = [
  { label: '一号楼', value: 'area1' },
  { label: '停车场', value: 'area2' },
  { label: '会议室区域', value: 'area3' },
  { label: '餐厅', value: 'area4' },
];

const searchResults = ref([]);

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

  return false; // 阻止自动上传
};

const handleUploadChange = (info) => {
  if (info.file && info.file.originFileObj) {
    const reader = new FileReader();
    reader.onload = (e) => {
      uploadedImage.value = e.target.result;
    };
    reader.readAsDataURL(info.file.originFileObj);
  }
};

const handleSearch = () => {
  console.log('开始搜索');
  // 模拟搜索结果
  searchResults.value = [
    {
      id: 1,
      image: uploadedImage.value,
      similarity: 96,
      time: '2024-11-06 15:32:10',
      location: '一号楼大厅',
      deviceName: '前门摄像头-001',
    },
    {
      id: 2,
      image: uploadedImage.value,
      similarity: 92,
      time: '2024-11-06 15:28:35',
      location: '停车场入口',
      deviceName: '停车场摄像头-01',
    },
  ];
};

const handleExport = () => {
  console.log('导出结果');
};

const handleViewDetail = (item) => {
  console.log('查看详情:', item);
};
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;

  .result-card {
    .result-image {
      margin-bottom: 12px;
    }

    .result-info {
      font-size: 12px;
    }
  }
}
</style>
