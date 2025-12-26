<!--
 * 访客信息管理页面（访客档案）
 *
 * 功能：访客档案的CRUD管理
 * - 查询表单
 * - 访客列表
 * - 新增/编辑访客
 * - 删除/批量删除
 * - 导出功能
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="visitor-info-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <h1>访客信息管理</h1>
        <p>管理访客档案信息和等级设置</p>
      </div>
      <div class="quick-actions">
        <a-space>
          <a-button type="primary" @click="showAddModal">
            <template #icon><PlusOutlined /></template>
            新增访客
          </a-button>
          <a-button @click="showImportModal">
            <template #icon><UploadOutlined /></template>
            批量导入
          </a-button>
          <a-button @click="handleExport">
            <template #icon><DownloadOutlined /></template>
            导出
          </a-button>
          <a-button
            danger
            :disabled="!selectedRowKeys.length"
            @click="handleBatchDelete"
          >
            <template #icon><DeleteOutlined /></template>
            批量删除 ({{ selectedRowKeys.length }})
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 搜索表单 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="访客姓名">
          <a-input
            v-model:value="searchForm.visitorName"
            placeholder="请输入访客姓名"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input
            v-model:value="searchForm.phone"
            placeholder="请输入手机号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="身份证号">
          <a-input
            v-model:value="searchForm.idCard"
            placeholder="请输入身份证号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="公司名称">
          <a-input
            v-model:value="searchForm.company"
            placeholder="请输入公司名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="访客等级">
          <a-select
            v-model:value="searchForm.visitorLevel"
            placeholder="请选择访客等级"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="NORMAL">普通访客</a-select-option>
            <a-select-option value="VIP">VIP访客</a-select-option>
            <a-select-option value="FREQUENT">常客</a-select-option>
            <a-select-option value="BLACKLIST">黑名单</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="link" @click="toggleAdvancedFilter">
              {{ advancedFilterVisible ? '收起' : '高级筛选' }}
              <template #icon>
                <DownOutlined v-if="!advancedFilterVisible" />
                <UpOutlined v-else />
              </template>
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 高级筛选（可折叠） -->
      <a-collapse v-model:activeKey="advancedFilterActiveKey" ghost>
        <a-collapse-panel key="1" :show-arrow="false">
          <a-form layout="inline" :model="advancedSearchForm" style="margin-top: 16px">
            <a-form-item label="访问时间范围">
              <a-range-picker
                v-model:value="advancedSearchForm.visitDateRange"
                :ranges="{
                  '最近7天': [dayjs().subtract(7, 'days'), dayjs()],
                  '最近30天': [dayjs().subtract(30, 'days'), dayjs()],
                  '最近90天': [dayjs().subtract(90, 'days'), dayjs()],
                }"
                format="YYYY-MM-DD"
                style="width: 280px"
              />
            </a-form-item>
            <a-form-item label="访问次数">
              <a-input-group compact>
                <a-input
                  v-model:value="advancedSearchForm.visitCountMin"
                  placeholder="最小次数"
                  allow-clear
                  style="width: 100px"
                />
                <a-input
                  placeholder="-"
                  disabled
                  style="width: 30px; text-align: center"
                />
                <a-input
                  v-model:value="advancedSearchForm.visitCountMax"
                  placeholder="最大次数"
                  allow-clear
                  style="width: 100px"
                />
              </a-input-group>
            </a-form-item>
            <a-form-item label="访问目的">
              <a-select
                v-model:value="advancedSearchForm.purpose"
                placeholder="请选择访问目的"
                allow-clear
                style="width: 180px"
              >
                <a-select-option value="商务拜访">商务拜访</a-select-option>
                <a-select-option value="个人拜访">个人拜访</a-select-option>
                <a-select-option value="面试">面试</a-select-option>
                <a-select-option value="快递外卖">快递外卖</a-select-option>
                <a-select-option value="其他">其他</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="被访人">
              <a-input
                v-model:value="advancedSearchForm.visiteeName"
                placeholder="请输入被访人姓名"
                allow-clear
                style="width: 180px"
              />
            </a-form-item>
            <a-form-item label="访问区域">
              <a-tree-select
                v-model:value="advancedSearchForm.areaId"
                :tree-data="areaTreeData"
                placeholder="请选择访问区域"
                allow-clear
                tree-default-expand-all
                style="width: 180px"
              />
            </a-form-item>
          </a-form>
        </a-collapse-panel>
      </a-collapse>
    </a-card>

    <!-- 访客列表 -->
    <a-card :bordered="false" class="table-card" style="margin-top: 16px">
      <template #title>
        <span>访客档案列表</span>
        <a-tag color="blue" style="margin-left: 8px">
          共 {{ pagination.total }} 条记录
        </a-tag>
      </template>

      <!-- 骨架屏加载状态 -->
      <SkeletonLoader v-if="loading && visitorList.length === 0" type="table" :rowCount="5" />

      <a-table
        v-else
        :columns="visitorColumns"
        :data-source="visitorList"
        :loading="loading && visitorList.length > 0"
        :pagination="pagination"
        :row-selection="{
          selectedRowKeys: selectedRowKeys,
          onChange: onSelectionChange,
        }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'visitorInfo'">
            <a-space>
              <a-avatar :size="40" :src="record.facePhoto">
                {{ record.visitorName?.charAt(0) }}
              </a-avatar>
              <div>
                <div class="visitor-name">{{ record.visitorName }}</div>
                <div class="visitor-contact">
                  <PhoneOutlined /> {{ formatPhone(record.phone) }}
                </div>
              </div>
            </a-space>
          </template>

          <template v-else-if="column.key === 'gender'">
            <a-tag :color="record.gender === 1 ? 'blue' : 'pink'">
              {{ record.gender === 1 ? '男' : '女' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'idCard'">
            <span>{{ formatIdCard(record.idCard) }}</span>
          </template>

          <template v-else-if="column.key === 'visitorLevel'">
            <a-tag :color="getLevelColor(record.visitorLevel)">
              {{ getLevelText(record.visitorLevel) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'visitCount'">
            <a-statistic
              :value="record.visitCount || 0"
              :value-style="{ fontSize: '14px' }"
            >
              <template #suffix>
                <span style="font-size: 12px; color: #999;">次</span>
              </template>
            </a-statistic>
          </template>

          <template v-else-if="column.key === 'lastVisitTime'">
            <span v-if="record.lastVisitTime">
              {{ formatDateTime(record.lastVisitTime) }}
            </span>
            <span v-else style="color: #999;">无记录</span>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space class="action-buttons">
              <a-button type="link" size="small" @click="handleView(record)">
                <template #icon><EyeOutlined /></template>
                查看
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-button
                type="link"
                size="small"
                danger
                @click="handleDelete(record)"
              >
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-space>

            <!-- 移动端详情信息（响应式显示隐藏的列数据） -->
            <div class="mobile-detail-info">
              <div class="detail-item">
                <span class="label">身份证:</span>
                <span>{{ formatIdCard(record.idCard) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">访客等级:</span>
                <span>{{ getLevelText(record.visitorLevel) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">访问次数:</span>
                <span>{{ record.visitCount || 0 }} 次</span>
              </div>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑访客弹窗 -->
    <a-modal
      v-model:open="editModalVisible"
      :title="editModalTitle"
      :width="800"
      @ok="handleSave"
      @cancel="handleCancelEdit"
      :confirm-loading="saving"
    >
      <a-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-divider orientation="left">基本信息</a-divider>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="访客姓名" name="visitorName">
              <a-input
                v-model:value="editForm.visitorName"
                placeholder="请输入访客姓名"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="性别" name="gender">
              <a-radio-group v-model:value="editForm.gender">
                <a-radio :value="1">男</a-radio>
                <a-radio :value="2">女</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="手机号" name="phone">
              <a-input
                v-model:value="editForm.phone"
                placeholder="请输入手机号"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="身份证号" name="idCard">
              <a-input
                v-model:value="editForm.idCard"
                placeholder="请输入身份证号"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="邮箱" name="email">
              <a-input
                v-model:value="editForm.email"
                placeholder="请输入邮箱"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="公司名称" name="company">
              <a-input
                v-model:value="editForm.company"
                placeholder="请输入公司名称"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="访客等级" name="visitorLevel">
              <a-select
                v-model:value="editForm.visitorLevel"
                placeholder="请选择访客等级"
              >
                <a-select-option value="NORMAL">普通访客</a-select-option>
                <a-select-option value="VIP">VIP访客</a-select-option>
                <a-select-option value="FREQUENT">常客</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="备注" name="remark">
              <a-input
                v-model:value="editForm.remark"
                placeholder="请输入备注"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="人脸照片" name="facePhoto">
          <a-upload
            v-model:file-list="facePhotoFileList"
            list-type="picture-card"
            :max-count="1"
            :before-upload="beforeUpload"
            @preview="handlePreview"
          >
            <div v-if="facePhotoFileList.length < 1">
              <PlusOutlined />
              <div style="margin-top: 8px">上传照片</div>
            </div>
          </a-upload>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 访客详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="访客详情"
      :width="800"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentRecord">
        <a-descriptions-item label="访客姓名" :span="2">
          <a-space>
            <a-avatar :size="48" :src="currentRecord.facePhoto">
              {{ currentRecord.visitorName?.charAt(0) }}
            </a-avatar>
            <span style="font-size: 16px; font-weight: 600;">
              {{ currentRecord.visitorName }}
            </span>
          </a-space>
        </a-descriptions-item>

        <a-descriptions-item label="性别">
          <a-tag :color="currentRecord.gender === 1 ? 'blue' : 'pink'">
            {{ currentRecord.gender === 1 ? '男' : '女' }}
          </a-tag>
        </a-descriptions-item>

        <a-descriptions-item label="访客等级">
          <a-tag :color="getLevelColor(currentRecord.visitorLevel)">
            {{ getLevelText(currentRecord.visitorLevel) }}
          </a-tag>
        </a-descriptions-item>

        <a-descriptions-item label="手机号">
          {{ formatPhone(currentRecord.phone) }}
        </a-descriptions-item>

        <a-descriptions-item label="身份证号">
          {{ formatIdCard(currentRecord.idCard) }}
        </a-descriptions-item>

        <a-descriptions-item label="邮箱">
          {{ currentRecord.email || '未填写' }}
        </a-descriptions-item>

        <a-descriptions-item label="公司名称">
          {{ currentRecord.company || '未填写' }}
        </a-descriptions-item>

        <a-descriptions-item label="来访次数" :span="2">
          <a-statistic
            :value="currentRecord.visitCount || 0"
            :value-style="{ color: '#1890ff' }"
          >
            <template #suffix>
              <span style="font-size: 14px;">次</span>
            </template>
          </a-statistic>
        </a-descriptions-item>

        <a-descriptions-item label="最后来访时间" :span="2">
          {{ currentRecord.lastVisitTime ? formatDateTime(currentRecord.lastVisitTime) : '无记录' }}
        </a-descriptions-item>

        <a-descriptions-item label="创建时间" :span="2">
          {{ formatDateTime(currentRecord.createTime) }}
        </a-descriptions-item>

        <a-descriptions-item label="更新时间" :span="2">
          {{ formatDateTime(currentRecord.updateTime) }}
        </a-descriptions-item>

        <a-descriptions-item label="备注" :span="2">
          {{ currentRecord.remark || '无' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 批量导入弹窗 -->
    <ImportModal
      v-model:visible="importModalVisible"
      @success="handleImportSuccess"
    />

    <!-- 图片预览 -->
    <a-modal
      v-model:open="previewVisible"
      :footer="null"
      :title="previewTitle"
    >
      <img alt="example" style="width: 100%" :src="previewImage" />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  PlusOutlined,
  DownloadOutlined,
  DeleteOutlined,
  SearchOutlined,
  EyeOutlined,
  EditOutlined,
  PhoneOutlined,
  UploadOutlined,
  DownOutlined,
  UpOutlined,
} from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { visitorApi } from '/@/api/business/visitor/visitor-api';
import { debounce, CacheManager, withCache } from '/@/utils/performance';
import SkeletonLoader from '/@/components/SkeletonLoader.vue';
import ImportModal from './components/ImportModal.vue';

// ==================== 性能优化：缓存管理 ====================
const cacheManager = new CacheManager(5 * 60 * 1000); // 5分钟缓存

// 定期清理过期缓存（每分钟）
const cacheCleanupTimer = setInterval(() => {
  cacheManager.clearExpired();
}, 60 * 1000);

// 搜索表单
const searchForm = reactive({
  visitorName: '',
  phone: '',
  idCard: '',
  company: '',
  visitorLevel: undefined,
});

// 高级筛选状态
const advancedFilterVisible = ref(false);
const advancedFilterActiveKey = ref([]);

// 高级搜索表单
const advancedSearchForm = reactive({
  visitDateRange: null,        // 访问时间范围
  visitCountMin: undefined,    // 访问次数最小值
  visitCountMax: undefined,    // 访问次数最大值
  purpose: undefined,          // 访问目的
  visiteeName: '',             // 被访人姓名
  areaId: undefined,           // 访问区域ID
});

// 区域树数据（示例数据，实际应从API获取）
const areaTreeData = ref([
  {
    title: 'A栋',
    value: '1',
    key: '1',
    children: [
      { title: '1楼大厅', value: '1-1', key: '1-1' },
      { title: '2楼办公区', value: '1-2', key: '1-2' },
      { title: '3楼会议室', value: '1-3', key: '1-3' },
    ],
  },
  {
    title: 'B栋',
    value: '2',
    key: '2',
    children: [
      { title: '1楼接待区', value: '2-1', key: '2-1' },
      { title: '2楼研发中心', value: '2-2', key: '2-2' },
    ],
  },
]);

// 表格数据
const visitorList = ref([]);
const loading = ref(false);
const selectedRowKeys = ref([]);

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
});

// 表格列定义
const visitorColumns = [
  {
    title: '访客信息',
    key: 'visitorInfo',
    width: 250,
  },
  {
    title: '性别',
    key: 'gender',
    width: 80,
    align: 'center',
  },
  {
    title: '身份证号',
    key: 'idCard',
    width: 180,
  },
  {
    title: '公司名称',
    dataIndex: 'company',
    key: 'company',
    width: 200,
  },
  {
    title: '访客等级',
    key: 'visitorLevel',
    width: 120,
    align: 'center',
  },
  {
    title: '来访次数',
    key: 'visitCount',
    width: 100,
    align: 'center',
  },
  {
    title: '最后来访时间',
    key: 'lastVisitTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 200,
  },
];

// 编辑表单
const editModalVisible = ref(false);
const editModalTitle = ref('新增访客');
const editFormRef = ref(null);
const editForm = reactive({
  visitorId: null,
  visitorName: '',
  gender: 1,
  phone: '',
  idCard: '',
  email: '',
  company: '',
  visitorLevel: 'NORMAL',
  facePhoto: '',
  remark: '',
});
const saving = ref(false);

// 表单验证规则
const editRules = {
  visitorName: [
    { required: true, message: '请输入访客姓名', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' },
  ],
  visitorLevel: [
    { required: true, message: '请选择访客等级', trigger: 'change' },
  ],
};

// 人脸照片上传
const facePhotoFileList = ref([]);
const previewVisible = ref(false);
const previewImage = ref('');
const previewTitle = ref('');

// 详情弹窗
const detailModalVisible = ref(false);
const currentRecord = ref(null);

// 批量导入弹窗
const importModalVisible = ref(false);

// 显示导入弹窗
const showImportModal = () => {
  importModalVisible.value = true;
};

// 导入成功回调
const handleImportSuccess = (result) => {
  message.success(`成功导入 ${result.successCount} 条访客信息`);
  // 清除缓存并重新加载列表
  cacheManager.clear('visitor:list');
  loadVisitorList(false);
};

// 加载访客列表（带缓存优化）
const loadVisitorList = async (useCache = true) => {
  loading.value = true;
  try {
    // 处理时间范围参数
    let visitStartDate = undefined;
    let visitEndDate = undefined;
    if (advancedSearchForm.visitDateRange && advancedSearchForm.visitDateRange.length === 2) {
      visitStartDate = advancedSearchForm.visitDateRange[0].format('YYYY-MM-DD');
      visitEndDate = advancedSearchForm.visitDateRange[1].format('YYYY-MM-DD');
    }

    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm,
      // 高级搜索参数
      visitStartDate,
      visitEndDate,
      visitCountMin: advancedSearchForm.visitCountMin,
      visitCountMax: advancedSearchForm.visitCountMax,
      purpose: advancedSearchForm.purpose,
      visiteeName: advancedSearchForm.visiteeName,
      areaId: advancedSearchForm.areaId,
    };

    // 生成缓存键
    const cacheKey = cacheManager.generateKey('visitor:list', params);

    // 尝试从缓存获取
    if (useCache) {
      const cached = cacheManager.get(cacheKey);
      if (cached) {
        console.log('[Cache] 使用缓存数据');
        visitorList.value = cached.data?.list || [];
        pagination.total = cached.data?.total || 0;
        loading.value = false;
        return;
      }
    }

    const result = await visitorApi.queryVisitors(params);

    if (result.code === 200 || result.success) {
      visitorList.value = result.data?.list || [];
      pagination.total = result.data?.total || 0;

      // 存入缓存
      cacheManager.set(cacheKey, result, 5 * 60 * 1000); // 5分钟缓存
    } else {
      message.error(result.message || '加载访客列表失败');
    }
  } catch (error) {
    console.error('加载访客列表失败:', error);
    message.error('加载访客列表失败');
  } finally {
    loading.value = false;
  }
};

// 防抖搜索（500ms延迟）
const debouncedSearch = debounce(() => {
  pagination.current = 1;
  loadVisitorList(false); // 搜索时不使用缓存
}, 500);

// 查询
const handleSearch = () => {
  debouncedSearch();
};

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    visitorName: '',
    phone: '',
    idCard: '',
    company: '',
    visitorLevel: undefined,
  });
  Object.assign(advancedSearchForm, {
    visitDateRange: null,
    visitCountMin: undefined,
    visitCountMax: undefined,
    purpose: undefined,
    visiteeName: '',
    areaId: undefined,
  });
  advancedFilterActiveKey.value = [];
  pagination.current = 1;
  loadVisitorList();
};

// 切换高级筛选面板
const toggleAdvancedFilter = () => {
  advancedFilterVisible.value = !advancedFilterVisible.value;
  advancedFilterActiveKey.value = advancedFilterVisible.value ? ['1'] : [];
};

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadVisitorList();
};

// 选择变化
const onSelectionChange = (keys) => {
  selectedRowKeys.value = keys;
};

// 新增访客
const showAddModal = () => {
  editModalTitle.value = '新增访客';
  Object.assign(editForm, {
    visitorId: null,
    visitorName: '',
    gender: 1,
    phone: '',
    idCard: '',
    email: '',
    company: '',
    visitorLevel: 'NORMAL',
    facePhoto: '',
    remark: '',
  });
  facePhotoFileList.value = [];
  editModalVisible.value = true;
};

// 编辑访客
const handleEdit = (record) => {
  editModalTitle.value = '编辑访客';
  Object.assign(editForm, {
    visitorId: record.visitorId,
    visitorName: record.visitorName,
    gender: record.gender,
    phone: record.phone,
    idCard: record.idCard,
    email: record.email,
    company: record.company,
    visitorLevel: record.visitorLevel,
    facePhoto: record.facePhoto,
    remark: record.remark,
  });

  if (record.facePhoto) {
    facePhotoFileList.value = [{
      uid: '1',
      name: 'facePhoto.jpg',
      status: 'done',
      url: record.facePhoto,
    }];
  } else {
    facePhotoFileList.value = [];
  }

  editModalVisible.value = true;
};

// 保存访客（优化用户体验）
const handleSave = async () => {
  try {
    await editFormRef.value.validate();
    saving.value = true;

    const apiMethod = editForm.visitorId
      ? visitorApi.updateVisitor(editForm)
      : visitorApi.addVisitor(editForm);

    const result = await apiMethod;

    if (result.code === 200 || result.success) {
      // 成功提示
      message.success({
        content: editForm.visitorId ? '访客信息更新成功' : '访客添加成功',
        duration: 2,
      });

      // 清除缓存
      cacheManager.clear('visitor:list');

      editModalVisible.value = false;
      await loadVisitorList();
    } else {
      // 错误提示
      message.error({
        content: result.message || '保存失败，请重试',
        duration: 3,
      });
    }
  } catch (error) {
    if (error.errorFields) {
      // 表单验证错误
      console.log('表单验证失败:', error);
      message.warning({
        content: '请检查表单填写是否正确',
        duration: 2,
      });
    } else {
      console.error('保存访客失败:', error);
      message.error({
        content: '保存失败，请稍后重试',
        duration: 3,
      });
    }
  } finally {
    saving.value = false;
  }
};

// 取消编辑
const handleCancelEdit = () => {
  editModalVisible.value = false;
  editFormRef.value?.clearValidate();
};

// 查看详情
const handleView = (record) => {
  currentRecord.value = record;
  detailModalVisible.value = true;
};

// 删除访客
const handleDelete = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除访客"${record.visitorName}"吗？`,
    onOk: async () => {
      try {
        const result = await visitorApi.deleteVisitor(record.visitorId);

        if (result.code === 200 || result.success) {
          message.success('删除成功');
          loadVisitorList();
        } else {
          message.error(result.message || '删除失败');
        }
      } catch (error) {
        console.error('删除访客失败:', error);
        message.error('删除失败');
      }
    },
  });
};

// 批量删除
const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条访客记录吗？`,
    onOk: async () => {
      try {
        const result = await visitorApi.batchDeleteVisitors(selectedRowKeys.value);

        if (result.code === 200 || result.success) {
          message.success('批量删除成功');
          selectedRowKeys.value = [];
          loadVisitorList();
        } else {
          message.error(result.message || '批量删除失败');
        }
      } catch (error) {
        console.error('批量删除失败:', error);
        message.error('批量删除失败');
      }
    },
  });
};

// 导出
const handleExport = async () => {
  try {
    const blob = await visitorApi.exportVisitors(searchForm);

    // 创建下载链接
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `访客信息_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);

    message.success('导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    message.error('导出失败');
  }
};

// 上传前校验
const beforeUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    message.error('只能上传 JPG/PNG 格式的图片!');
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB!');
    return false;
  }
  return false; // 手动上传
};

// 预览图片
const handlePreview = async (file) => {
  if (!file.url && !file.preview) {
    file.preview = await getBase64(file.originFileObj);
  }
  previewImage.value = file.url || file.preview;
  previewVisible.value = true;
  previewTitle.value = file.name;
};

// 转换为base64
const getBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = (error) => reject(error);
  });
};

// 格式化手机号（脱敏）
const formatPhone = (phone) => {
  if (!phone) return '未填写';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
};

// 格式化身份证号（脱敏）
const formatIdCard = (idCard) => {
  if (!idCard) return '未填写';
  return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2');
};

// 格式化日期时间
const formatDateTime = (dateTime) => {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '';
};

// 获取等级颜色
const getLevelColor = (level) => {
  const colorMap = {
    'NORMAL': 'default',
    'VIP': 'gold',
    'FREQUENT': 'blue',
    'BLACKLIST': 'red',
  };
  return colorMap[level] || 'default';
};

// 获取等级文本
const getLevelText = (level) => {
  const textMap = {
    'NORMAL': '普通访客',
    'VIP': 'VIP访客',
    'FREQUENT': '常客',
    'BLACKLIST': '黑名单',
  };
  return textMap[level] || level;
};

// ==================== 性能优化：自动防抖搜索 ====================
// 监听搜索表单变化，自动触发防抖搜索
watch(
  () => ({ ...searchForm }),
  () => {
    debouncedSearch();
  },
  { deep: true }
);

// 初始化
onMounted(() => {
  loadVisitorList();
});

// 清理
onUnmounted(() => {
  // 清理缓存定时器
  if (cacheCleanupTimer) {
    clearInterval(cacheCleanupTimer);
  }
  // 清理缓存
  cacheManager.clear();
});
</script>

<style lang="scss" scoped>
.visitor-info-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.page-title {
  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px 0;
    color: #1890ff;
  }

  p {
    margin: 0;
    color: #666;
    font-size: 14px;
  }
}

.search-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.table-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  :deep(.ant-table) {
    .visitor-name {
      font-weight: 600;
      color: #333;
    }

    .visitor-contact {
      font-size: 12px;
      color: #999;
    }
  }
}

:deep(.ant-upload-select-picture-card) {
  width: 104px;
  height: 104px;
}

// 移动端详情信息（默认隐藏，移动端显示）
.mobile-detail-info {
  display: none;
}

// ==================== 响应式设计 ====================

// 平板设备 (768px - 1024px)
@media (max-width: 1024px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 16px;

    .quick-actions {
      width: 100%;
      justify-content: flex-start;
    }
  }

  .search-card {
    :deep(.ant-form) {
      .ant-form-item {
        margin-right: 0;
        margin-bottom: 16px;
      }
    }
  }

  .table-card {
    :deep(.ant-table) {
      font-size: 13px;
    }
  }
}

// 移动设备 (<768px)
@media (max-width: 767px) {
  .visitor-info-page {
    padding: 12px;
  }

  .page-header {
    padding: 16px;
    margin-bottom: 12px;

    .page-title {
      h1 {
        font-size: 18px;
      }

      p {
        font-size: 12px;
      }
    }

    .quick-actions {
      :deep(.ant-space) {
        flex-wrap: wrap;
        gap: 8px !important;
      }

      :deep(.ant-btn) {
        font-size: 13px;
        padding: 4px 12px;
        height: auto;
      }
    }
  }

  .search-card {
    padding: 16px;

    :deep(.ant-form) {
      .ant-form-item {
        width: 100%;
        margin-right: 0;
        margin-bottom: 12px;
      }

      .ant-input,
      .ant-select {
        width: 100% !important;
      }
    }
  }

  .table-card {
    :deep(.ant-table) {
      font-size: 12px;

      .ant-table-thead > tr > th {
        padding: 8px 4px;
        font-size: 12px;
      }

      .ant-table-tbody > tr > td {
        padding: 8px 4px;
      }

      // 隐藏次要列
      .ant-table-cell {
        &:nth-child(5),
        &:nth-child(6),
        &:nth-child(7) {
          display: none;
        }
      }
    }

    // 移动端显示的隐藏列信息
    .mobile-detail-info {
      display: block !important;
      margin-top: 8px;
      padding-top: 8px;
      border-top: 1px solid #f0f0f0;

      .detail-item {
        display: flex;
        justify-content: space-between;
        margin-bottom: 4px;
        font-size: 12px;
        color: #666;

        .label {
          color: #999;
        }
      }
    }
  }

  // 分页器移动端优化
  :deep(.ant-pagination) {
    .ant-pagination-total-text {
      display: none;
    }

    .ant-pagination-item {
      min-width: 28px;
      height: 28px;
      line-height: 26px;
      font-size: 12px;
    }

    .ant-pagination-prev,
    .ant-pagination-next {
      min-width: 28px;
      height: 28px;
      line-height: 26px;
    }
  }
}

// 小屏手机 (<576px)
@media (max-width: 575px) {
  .visitor-info-page {
    padding: 8px;
  }

  .page-header {
    padding: 12px;

    .page-title {
      h1 {
        font-size: 16px;
      }

      p {
        display: none; // 小屏隐藏副标题
      }
    }

    .quick-actions {
      :deep(.ant-btn) {
        font-size: 12px;
        padding: 4px 8px;

        span:not(.anticon) {
          display: none; // 只显示图标
        }
      }
    }
  }

  .search-card {
    padding: 12px;

    // 搜索表单折叠
    :deep(.ant-form-item-label) {
      padding-bottom: 4px;

      > label {
        font-size: 13px;
        height: auto;
      }
    }
  }

  .table-card {
    // 卡片操作按钮只显示图标
    :deep(.action-buttons) {
      .ant-btn {
        padding: 4px 8px;
        font-size: 12px;

        span:not(.anticon) {
          display: none;
        }
      }
    }
  }
}
</style>
