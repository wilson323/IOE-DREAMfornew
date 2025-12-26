<template>
  <div class="account-kind-page">
    <!-- 搜索区 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="类别名称">
          <a-input v-model:value="searchForm.name" placeholder="请输入类别名称" allow-clear style="width: 150px" />
        </a-form-item>
        <a-form-item label="类别代码">
          <a-input v-model:value="searchForm.code" placeholder="请输入类别代码" allow-clear style="width: 150px" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 主表格区 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <span>账户类别管理</span>
        <a-tag color="blue" style="margin-left: 8px">配置优先级最高</a-tag>
      </template>
      <template #extra>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增类别
        </a-button>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'kindName'">
            <a-space>
              <span>{{ record.kindName }}</span>
              <a-tag v-if="record.isDefault" color="gold">默认</a-tag>
            </a-space>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge :status="record.status === 1 ? 'success' : 'default'" :text="record.status === 1 ? '启用' : '禁用'" />
          </template>
          <template v-else-if="column.key === 'modeConfig'">
            <a-space v-if="record.modeConfig">
              <a-tag v-for="(mode, key) in getEnabledModes(record.modeConfig)" :key="key" color="blue">
                {{ getModeName(key) }}
              </a-tag>
              <span v-if="!getEnabledModes(record.modeConfig)" style="color: #999">-</span>
            </a-space>
            <span v-else style="color: #999">未配置</span>
          </template>
          <template v-else-if="column.key === 'accountCount'">
            <a-statistic :value="record.accountCount || 0" :value-style="{ fontSize: '14px' }">
              <template #suffix>个账户</template>
            </a-statistic>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleViewDetail(record)">详情</a-button>
              <a-button
                type="link"
                size="small"
                v-if="!record.isDefault"
                @click="handleSetDefault(record)"
              >
                设为默认
              </a-button>
              <a-button
                type="link"
                size="small"
                v-if="record.status === 1"
                @click="handleDisable(record)"
              >
                禁用
              </a-button>
              <a-button
                type="link"
                size="small"
                v-else
                @click="handleEnable(record)"
              >
                启用
              </a-button>
              <a-popconfirm
                title="确定要删除该账户类别吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 编辑抽屉 -->
    <a-drawer
      v-model:open="drawerVisible"
      :title="drawerTitle"
      width="600"
      :body-style="{ paddingBottom: '80px' }"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        layout="vertical"
      >
        <!-- 基本信息 -->
        <a-divider orientation="left">基本信息</a-divider>

        <a-form-item label="类别名称" name="kindName">
          <a-input v-model:value="formData.kindName" placeholder="例如：员工账户类别" />
        </a-form-item>

        <a-form-item label="类别代码" name="kindCode">
          <a-input v-model:value="formData.kindCode" placeholder="例如：STAFF" />
          <template #extra>类别代码必须唯一，建议使用大写英文字母</template>
        </a-form-item>

        <a-form-item label="排序" name="sort">
          <a-input-number v-model:value="formData.sort" :min="0" placeholder="数字越小越靠前" style="width: 100%" />
        </a-form-item>

        <a-form-item label="描述说明" name="description">
          <a-textarea v-model:value="formData.description" :rows="2" placeholder="输入类别描述" />
        </a-form-item>

        <!-- 消费模式配置 -->
        <a-divider orientation="left">
          <a-space>
            <span>消费模式配置</span>
            <a-tooltip title="配置该类别账户的消费模式，优先级高于区域配置">
              <QuestionCircleOutlined />
            </a-tooltip>
          </a-space>
        </a-divider>

        <a-form-item label="mode_config">
          <ModeConfigEditor
            v-model="formData.modeConfig"
            :readonly="false"
          />
          <template #extra>
            <div>配置6种消费模式的详细信息</div>
            <div style="color: #ff4d4f; font-size: 12px">⚠️ 此配置优先级最高，将覆盖区域配置</div>
          </template>
        </a-form-item>

        <!-- 区域权限配置 -->
        <a-divider orientation="left">
          <a-space>
            <span>区域权限配置</span>
            <a-tooltip title="配置该类别账户允许消费的区域">
              <QuestionCircleOutlined />
            </a-tooltip>
          </a-space>
        </a-divider>

        <a-form-item label="area_config">
          <AreaConfigEditor
            v-model="formData.areaConfig"
            :readonly="false"
          />
          <template #extra>
            <div>配置该类别账户允许消费的区域</div>
            <div style="color: #1890ff; font-size: 12px">ℹ️ 此配置将限制账户只能在指定区域消费</div>
          </template>
        </a-form-item>

        <!-- 账户级别定额配置 -->
        <a-divider orientation="left">
          <a-space>
            <span>账户级别定额配置</span>
            <a-tooltip title="配置该类别账户的消费限额，优先级高于区域定额">
              <QuestionCircleOutlined />
            </a-tooltip>
          </a-space>
        </a-divider>

        <a-form-item label="account_level_fixed">
          <FixedValueConfigEditor
            v-model="formData.accountLevelFixed"
            :readonly="false"
          />
          <template #extra>
            <div>配置该类别账户的消费限额</div>
            <div style="color: #ff4d4f; font-size: 12px">⚠️ 此配置优先级高于区域定额配置</div>
          </template>
        </a-form-item>

        <!-- 餐别关联 -->
        <a-divider orientation="left">餐别关联</a-divider>

        <a-form-item label="关联餐别">
          <a-select
            v-model:value="formData.mealCategoryIds"
            mode="multiple"
            placeholder="选择关联的餐别"
            style="width: 100%"
            :options="mealCategoryOptions"
            :field-names="{ label: 'categoryName', value: 'categoryId' }"
          />
          <template #extra>选择该账户类别关联的餐别，留空表示不限制</template>
        </a-form-item>
      </a-form>

      <template #footer>
        <a-space>
          <a-button @click="drawerVisible = false">取消</a-button>
          <a-button type="primary" @click="handleSubmit" :loading="submitLoading">
            <template #icon><CheckOutlined /></template>
            保存
          </a-button>
        </a-space>
      </template>
    </a-drawer>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="账户类别详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentDetail">
        <a-descriptions-item label="类别名称">{{ currentDetail.kindName }}</a-descriptions-item>
        <a-descriptions-item label="类别代码">{{ currentDetail.kindCode }}</a-descriptions-item>
        <a-descriptions-item label="排序">{{ currentDetail.sort }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-badge :status="currentDetail.status === 1 ? 'success' : 'default'" :text="currentDetail.status === 1 ? '启用' : '禁用'" />
        </a-descriptions-item>
        <a-descriptions-item label="是否默认">{{ currentDetail.isDefault ? '是' : '否' }}</a-descriptions-item>
        <a-descriptions-item label="关联账户数">{{ currentDetail.accountCount || 0 }} 个</a-descriptions-item>
        <a-descriptions-item label="描述说明" :span="2">{{ currentDetail.description || '-' }}</a-descriptions-item>

        <!-- 消费模式配置 -->
        <a-descriptions-item label="消费模式配置" :span="2">
          <div v-if="currentDetail.modeConfig">
            <a-tag v-for="(mode, key) in getEnabledModes(currentDetail.modeConfig)" :key="key" color="blue" style="margin: 4px">
              {{ getModeName(key) }}
            </a-tag>
          </div>
          <span v-else style="color: #999">未配置</span>
        </a-descriptions-item>

        <!-- 区域权限 -->
        <a-descriptions-item label="区域权限" :span="2">
          <div v-if="currentDetail.areaConfig">
            <a-tag v-if="currentDetail.areaConfig.allAreasAllowed" color="green">全部区域</a-tag>
            <template v-else>
              <a-tag v-for="area in (currentDetail.areaConfig.areas || [])" :key="area.areaId" style="margin: 4px">
                {{ area.areaName }}
                <span v-if="area.includeSubAreas" style="margin-left: 4px; font-size: 12px">+子区域</span>
              </a-tag>
            </template>
          </div>
          <span v-else style="color: #999">未配置</span>
        </a-descriptions-item>

        <!-- 定额配置 -->
        <a-descriptions-item label="账户定额" :span="2">
          <div v-if="currentDetail.accountLevelFixed && currentDetail.accountLevelFixed.enabled">
            <a-space direction="vertical" :size="4">
              <div>模式: {{ getFixedModeName(currentDetail.accountLevelFixed.mode) }}</div>
              <div v-if="currentDetail.accountLevelFixed.mode === 'MEAL_BASED' || currentDetail.accountLevelFixed.mode === 'HYBRID'">
                <a-tag v-for="(value, key) in currentDetail.accountLevelFixed.mealValues" :key="key" style="margin: 4px">
                  {{ getMealTypeName(key) }}: ¥{{ (value / 100).toFixed(2) }}
                </a-tag>
              </div>
            </a-space>
          </div>
          <span v-else style="color: #999">未启用</span>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CheckOutlined,
  QuestionCircleOutlined,
} from '@ant-design/icons-vue';
import { ModeConfigEditor, AreaConfigEditor, FixedValueConfigEditor } from '/@/components/business/consume/json-editor';
import { consumeApi } from '/@/api/business/consume/consume-api';

// 搜索表单
const searchForm = reactive({
  name: '',
  code: '',
  status: undefined,
});

// 表格数据
const loading = ref(false);
const tableData = ref([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`,
});

// 表格列定义
const columns = [
  { title: '类别名称', dataIndex: 'kindName', key: 'kindName', width: 150 },
  { title: '类别代码', dataIndex: 'kindCode', key: 'kindCode', width: 120 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80, align: 'center' },
  { title: '消费模式', key: 'modeConfig', width: 200 },
  { title: '关联账户数', key: 'accountCount', width: 120, align: 'center' },
  { title: '状态', key: 'status', width: 100, align: 'center' },
  { title: '操作', key: 'action', width: 240, fixed: 'right' },
];

// 抽屉相关
const drawerVisible = ref(false);
const drawerTitle = ref('新增账户类别');
const formRef = ref();
const submitLoading = ref(false);
const currentEditId = ref(null);

// 详情弹窗
const detailVisible = ref(false);
const currentDetail = ref(null);

// 餐别选项
const mealCategoryOptions = ref([]);

// 表单数据
const formData = reactive({
  kindName: '',
  kindCode: '',
  sort: 0,
  description: '',
  modeConfig: {
    FIXED_AMOUNT: {
      enabled: false,
      subType: 'SECTION',
      amount: 0,
      keyValues: [],
      values: []
    },
    FREE_AMOUNT: {
      enabled: false,
      maxAmount: 0,
      dailyLimit: 0
    },
    METERED: {
      enabled: false,
      subType: 'TIMING',
      unitPrice: 0,
      precision: 60
    },
    PRODUCT: {
      enabled: false,
      allowOverdraw: false,
      overdrawLimit: 0,
      requireQuantity: true
    },
    ORDER: {
      enabled: false,
      orderDeadline: 30,
      allowCancel: true,
      cancelDeadline: 60
    },
    INTELLIGENCE: {
      enabled: false,
      ruleType: 'TIME_BASED',
      ruleConfig: '{}'
    }
  },
  areaConfig: {
    allAreasAllowed: false,
    areas: []
  },
  accountLevelFixed: {
    enabled: false,
    mode: 'MEAL_BASED',
    mealValues: {
      BREAKFAST: 0,
      LUNCH: 0,
      DINNER: 0,
      SNACK: 0
    },
    timeSlots: [],
    overMode: 'FORBID',
    overdrawLimit: 0,
    showHint: true
  },
  mealCategoryIds: [],
});

// 表单验证规则
const formRules = {
  kindName: [{ required: true, message: '请输入类别名称', trigger: 'blur' }],
  kindCode: [{ required: true, message: '请输入类别代码', trigger: 'blur' }],
};

// 获取启用的模式
const getEnabledModes = (modeConfig) => {
  if (!modeConfig) return {};
  const enabled = {};
  for (const [key, value] of Object.entries(modeConfig)) {
    if (value && value.enabled) {
      enabled[key] = value;
    }
  }
  return enabled;
};

// 获取模式名称
const getModeName = (key) => {
  const map = {
    FIXED_AMOUNT: '固定金额',
    FREE_AMOUNT: '自由金额',
    METERED: '计量计费',
    PRODUCT: '商品模式',
    ORDER: '订餐模式',
    INTELLIGENCE: '智能模式',
  };
  return map[key] || key;
};

// 获取定额模式名称
const getFixedModeName = (mode) => {
  const map = {
    MEAL_BASED: '基于餐别',
    TIME_BASED: '基于时间段',
    HYBRID: '混合模式',
  };
  return map[mode] || mode;
};

// 获取餐别名称
const getMealTypeName = (key) => {
  const map = {
    BREAKFAST: '早餐',
    LUNCH: '午餐',
    DINNER: '晚餐',
    SNACK: '零食',
  };
  return map[key] || key;
};

// 查询数据
const loadData = () => {
  loading.value = true;

  setTimeout(() => {
    // 模拟数据
    const mockData = [
      {
        id: 1,
        kindName: '员工账户类别',
        kindCode: 'STAFF',
        sort: 1,
        status: 1,
        isDefault: true,
        accountCount: 1250,
        description: '默认员工账户类别',
        modeConfig: {
          FIXED_AMOUNT: { enabled: true, subType: 'SECTION' },
          FREE_AMOUNT: { enabled: false },
        },
        areaConfig: {
          allAreasAllowed: false,
          areas: [
            { areaId: 1, areaName: '第一食堂', includeSubAreas: true },
            { areaId: 2, areaName: '第二食堂', includeSubAreas: true },
          ]
        },
        accountLevelFixed: {
          enabled: true,
          mode: 'MEAL_BASED',
          mealValues: {
            BREAKFAST: 1500,
            LUNCH: 2500,
            DINNER: 2000,
            SNACK: 800
          }
        },
        mealCategoryIds: [1, 2, 3],
      },
      {
        id: 2,
        kindName: '学生账户类别',
        kindCode: 'STUDENT',
        sort: 2,
        status: 1,
        isDefault: false,
        accountCount: 3200,
        description: '学生专用账户类别，享受补贴',
        modeConfig: {
          FIXED_AMOUNT: { enabled: true, subType: 'SECTION' },
          ORDER: { enabled: true },
        },
        areaConfig: {
          allAreasAllowed: true,
          areas: []
        },
        accountLevelFixed: {
          enabled: true,
          mode: 'HYBRID',
          mealValues: {
            BREAKFAST: 1000,
            LUNCH: 2000,
            DINNER: 1500,
            SNACK: 500
          }
        },
        mealCategoryIds: [1, 2],
      },
      {
        id: 3,
        kindName: '临时账户类别',
        kindCode: 'TEMPORARY',
        sort: 3,
        status: 1,
        isDefault: false,
        accountCount: 50,
        description: '临时访客账户',
        modeConfig: {
          FREE_AMOUNT: { enabled: true, maxAmount: 5000, dailyLimit: 10000 },
        },
        areaConfig: {
          allAreasAllowed: false,
          areas: [
            { areaId: 3, areaName: '超市便利店', includeSubAreas: false },
          ]
        },
        accountLevelFixed: {
          enabled: false
        },
        mealCategoryIds: [],
      },
    ];

    // 过滤数据
    const filtered = mockData.filter((item) => {
      if (searchForm.name && !item.kindName.includes(searchForm.name)) return false;
      if (searchForm.code && !item.kindCode.includes(searchForm.code)) return false;
      if (searchForm.status !== undefined && item.status !== searchForm.status) return false;
      return true;
    });

    tableData.value = filtered;
    pagination.total = filtered.length;
    loading.value = false;
  }, 300);
};

// 加载餐别选项
const loadMealCategories = () => {
  // 模拟餐别数据
  mealCategoryOptions.value = [
    { categoryId: 1, categoryName: '早餐' },
    { categoryId: 2, categoryName: '午餐' },
    { categoryId: 3, categoryName: '晚餐' },
    { categoryId: 4, categoryName: '零食' },
  ];
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadData();
};

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    code: '',
    status: undefined,
  });
  handleSearch();
};

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadData();
};

// 新增
const handleAdd = () => {
  currentEditId.value = null;
  drawerTitle.value = '新增账户类别';
  resetForm();
  drawerVisible.value = true;
};

// 编辑
const handleEdit = (record) => {
  currentEditId.value = record.id;
  drawerTitle.value = '编辑账户类别';

  Object.assign(formData, {
    kindName: record.kindName || '',
    kindCode: record.kindCode || '',
    sort: record.sort || 0,
    description: record.description || '',
    modeConfig: record.modeConfig || formData.modeConfig,
    areaConfig: record.areaConfig || formData.areaConfig,
    accountLevelFixed: record.accountLevelFixed || formData.accountLevelFixed,
    mealCategoryIds: record.mealCategoryIds || [],
  });

  drawerVisible.value = true;
};

// 查看详情
const handleViewDetail = (record) => {
  currentDetail.value = record;
  detailVisible.value = true;
};

// 设为默认
const handleSetDefault = async (record) => {
  try {
    await consumeApi.setDefaultAccountKind(record.id);
    message.success('设置成功');
    loadData();
  } catch (error) {
    message.error('设置失败');
  }
};

// 启用
const handleEnable = async (record) => {
  try {
    await consumeApi.enableAccountKind(record.id);
    message.success('启用成功');
    loadData();
  } catch (error) {
    message.error('启用失败');
  }
};

// 禁用
const handleDisable = async (record) => {
  try {
    await consumeApi.disableAccountKind(record.id);
    message.success('禁用成功');
    loadData();
  } catch (error) {
    message.error('禁用失败');
  }
};

// 删除
const handleDelete = async (id) => {
  try {
    await consumeApi.deleteAccountKind(id);
    message.success('删除成功');
    loadData();
  } catch (error) {
    message.error('删除失败');
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validateFields();
    submitLoading.value = true;

    setTimeout(() => {
      if (currentEditId.value) {
        message.success('更新成功');
      } else {
        message.success('新增成功');
      }

      submitLoading.value = false;
      drawerVisible.value = false;
      loadData();
    }, 500);
  } catch (error) {
    console.error('表单验证失败:', error);
  }
};

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    kindName: '',
    kindCode: '',
    sort: 0,
    description: '',
    modeConfig: {
      FIXED_AMOUNT: { enabled: false, subType: 'SECTION', amount: 0, keyValues: [], values: [] },
      FREE_AMOUNT: { enabled: false, maxAmount: 0, dailyLimit: 0 },
      METERED: { enabled: false, subType: 'TIMING', unitPrice: 0, precision: 60 },
      PRODUCT: { enabled: false, allowOverdraw: false, overdrawLimit: 0, requireQuantity: true },
      ORDER: { enabled: false, orderDeadline: 30, allowCancel: true, cancelDeadline: 60 },
      INTELLIGENCE: { enabled: false, ruleType: 'TIME_BASED', ruleConfig: '{}' },
    },
    areaConfig: { allAreasAllowed: false, areas: [] },
    accountLevelFixed: {
      enabled: false,
      mode: 'MEAL_BASED',
      mealValues: { BREAKFAST: 0, LUNCH: 0, DINNER: 0, SNACK: 0 },
      timeSlots: [],
      overMode: 'FORBID',
      overdrawLimit: 0,
      showHint: true
    },
    mealCategoryIds: [],
  });
  formRef.value?.resetFields();
};

// 初始化
onMounted(() => {
  loadData();
  loadMealCategories();
});
</script>

<style scoped lang="less">
.account-kind-page {
  .search-card {
    margin-bottom: 16px;
  }
}
</style>
