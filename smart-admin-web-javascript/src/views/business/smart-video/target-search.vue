<!--
  智能视频-目标检索页面

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-card title="目标检索" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card title="检索条件" :bordered="false" size="small">
            <a-form layout="vertical">
              <a-form-item label="目标类型">
                <a-select v-model:value="searchForm.targetType" placeholder="请选择">
                  <a-select-option value="person">人员</a-select-option>
                  <a-select-option value="vehicle">车辆</a-select-option>
                  <a-select-option value="bike">非机动车</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="时间范围">
                <a-range-picker v-model:value="searchForm.timeRange" show-time style="width: 100%;" />
              </a-form-item>

              <a-form-item label="选择区域">
                <a-select v-model:value="searchForm.area" placeholder="请选择区域">
                  <a-select-option value="area1">一号楼</a-select-option>
                  <a-select-option value="area2">停车场</a-select-option>
                  <a-select-option value="area3">会议室区域</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="特征描述">
                <a-textarea
                  v-model:value="searchForm.description"
                  placeholder="如：红色上衣、蓝色裤子"
                  :rows="4"
                />
              </a-form-item>

              <a-form-item>
                <a-button type="primary" block @click="handleSearch">
                  <SearchOutlined />
                  开始检索
                </a-button>
              </a-form-item>

              <a-form-item>
                <a-button block @click="handleReset">
                  重置
                </a-button>
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>

        <a-col :span="18">
          <a-card title="检索结果" :bordered="false">
            <template #extra>
              <a-space>
                <span>共找到 {{ pagination.total }} 条记录</span>
                <a-button @click="handleExport">
                  <ExportOutlined />
                  导出结果
                </a-button>
              </a-space>
            </template>

            <a-row :gutter="[16, 16]">
              <a-col :span="6" v-for="item in searchResults" :key="item.id">
                <a-card :bordered="false" hoverable size="small" class="result-card">
                  <div class="result-image">
                    <a-image
                      :src="item.image"
                      fallback="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
                    />
                  </div>
                  <div class="result-info">
                    <div class="info-item">
                      <span class="label">时间：</span>
                      <span>{{ item.time }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">位置：</span>
                      <span>{{ item.location }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">相似度：</span>
                      <a-progress :percent="item.similarity" size="small" />
                    </div>
                    <a-button type="link" size="small" block @click="handleViewDetail(item)">
                      查看详情
                    </a-button>
                  </div>
                </a-card>
              </a-col>
            </a-row>

            <a-pagination
              v-if="searchResults.length > 0"
              v-model:current="pagination.current"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              :show-total="(total) => `共 ${total} 条`"
              style="margin-top: 16px; text-align: right;"
            />
          </a-card>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { SearchOutlined, ExportOutlined } from '@ant-design/icons-vue';

const searchForm = reactive({
  targetType: undefined,
  timeRange: [],
  area: undefined,
  description: '',
});

const searchResults = ref([
  {
    id: 1,
    image: '',
    time: '2024-11-06 15:32:10',
    location: '一号楼大厅',
    similarity: 95,
  },
  {
    id: 2,
    image: '',
    time: '2024-11-06 15:28:35',
    location: '停车场入口',
    similarity: 92,
  },
]);

const pagination = reactive({
  current: 1,
  pageSize: 12,
  total: 2,
});

const handleSearch = () => {
  console.log('开始检索:', searchForm);
};

const handleReset = () => {
  searchForm.targetType = undefined;
  searchForm.timeRange = [];
  searchForm.area = undefined;
  searchForm.description = '';
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
      margin-bottom: 8px;
    }

    .result-info {
      .info-item {
        margin-bottom: 4px;
        font-size: 12px;

        .label {
          color: #666;
        }
      }
    }
  }
}
</style>
