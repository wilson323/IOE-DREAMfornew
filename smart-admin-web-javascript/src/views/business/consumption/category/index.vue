<!--
  * 账户分类管理页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025/11/17
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="consume-category-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <h1>账户分类管理</h1>
        <p>管理账户分类体系和权限配置，实现精细化权限控制</p>
      </div>
      <div class="page-actions">
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增分类
          </a-button>
          <a-button @click="handleExpandAll">
            <template #icon><FullscreenOutlined /></template>
            展开全部
          </a-button>
          <a-button @click="handleCollapseAll">
            <template #icon><FullscreenExitOutlined /></template>
            收起全部
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stats-cards">
      <a-col :xs="24" :sm="12} :lg="6">
        <div class="stat-card">
          <div class="stat-card-icon" style="background: linear-gradient(135deg, #1890ff, #096dd9)">
            <GroupOutlined />
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">{{ stats.totalCategories }}</div>
            <div class="stat-card-title">总分类数</div>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :sm="12} :lg="6">
        <div class="stat-card">
          <div class="stat-card-icon" style="background: linear-gradient(135deg, #52c41a, #389e0d)">
            <UserOutlined />
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">{{ stats.totalAccounts }}</div>
            <div class="stat-card-title">关联账户</div>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :sm="12} :lg="6">
        <div class="stat-card">
          <div class="stat-card-icon" style="background: linear-gradient(135deg, #faad14, #d48806)">
            <SettingOutlined />
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">{{ stats.totalPermissions }}</div>
            <div class="stat-card-title">权限规则</div>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :sm="12} :lg="6">
        <div class="stat-card">
          <div class="stat-card-icon" style="background: linear-gradient(135deg, #722ed1, #531dab)">
            <FileTextOutlined />
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">{{ stats.levelDepth }}</div>
            <div class="stat-card-title">分类层级</div>
          </div>
        </div>
      </a-col>
    </a-row>

    <a-row :gutter="16">
      <!-- 左侧：分类树 -->
      <a-col :span="8">
        <a-card title="分类树" :bordered="false" class="category-tree-card">
          <template #extra>
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索分类"
              style="width: 200px"
              @search="handleSearch"
            />
          </template>

          <a-tree
            v-model:expandedKeys="expandedKeys"
            v-model:selectedKeys="selectedKeys"
            :tree-data="categoryTreeData"
            :field-names="{ title: 'categoryName', key: 'categoryId', children: 'children' }"
            show-line
            @select="handleSelectCategory"
            @expand="handleExpand"
          >
            <template #title="{ title, categoryCode, status }">
              <div class="tree-node-title">
                <span class="node-title">{{ title }}</span>
                <a-tag size="small" :color="getStatusColor(status)" class="node-status">
                  {{ status === 'ENABLE' ? '启用' : '禁用' }}
                </a-tag>
              </div>
            </template>
          </a-tree>
        </a-card>
      </a-col>

      <!-- 右侧：分类详情 -->
      <a-col :span="16">
        <a-card
          :title="selectedCategory ? `分类详情 - ${selectedCategory.categoryName}` : '请选择分类'"
          :bordered="false"
          class="category-detail-card"
        >
          <template #extra v-if="selectedCategory">
            <a-space>
              <a-button type="primary" @click="handleEdit">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-button @click="handleAddChild">
                <template #icon><PlusOutlined /></template>
                添加子分类
              </a-button>
              <a-popconfirm
                title="确定要删除这个分类吗？"
                @confirm="handleDelete"
                ok-text="确定"
                cancel-text="取消"
              >
                <a-button type="primary" danger>
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>

          <div v-if="!selectedCategory" class="empty-state">
            <a-empty description="请从左侧选择分类查看详情" />
          </div>

          <div v-else class="category-detail">
            <!-- 基本信息 -->
            <a-descriptions title="基本信息" :column="2" bordered>
              <a-descriptions-item label="分类名称">
                {{ selectedCategory.categoryName }}
              </a-descriptions-item>
              <a-descriptions-item label="分类编码">
                {{ selectedCategory.categoryCode }}
              </a-descriptions-item>
              <a-descriptions-item label="上级分类">
                {{ selectedCategory.parentName || '无' }}
              </a-descriptions-item>
              <a-descriptions-item label="分类级别">
                <a-tag color="blue">L{{ selectedCategory.level }}</a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="状态">
                <a-tag :color="getStatusColor(selectedCategory.status)">
                  {{ selectedCategory.status === 'ENABLE' ? '启用' : '禁用' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="排序">
                {{ selectedCategory.sortOrder }}
              </a-descriptions-item>
              <a-descriptions-item label="关联账户数" :span="2">
                <a-link @click="handleViewAccounts">{{ selectedCategory.accountCount || 0 }} 个账户</a-link>
              </a-descriptions-item>
              <a-descriptions-item label="创建时间" :span="2">
                {{ formatDateTime(selectedCategory.createTime) }}
              </a-descriptions-item>
              <a-descriptions-item label="分类描述" :span="2">
                {{ selectedCategory.description || '暂无描述' }}
              </a-descriptions-item>
            </a-descriptions>

            <!-- 权限配置 -->
            <div class="permission-section" style="margin-top: 24px;">
              <a-divider>权限配置</a-divider>
              <a-descriptions title="消费权限" :column="2" bordered size="small">
                <a-descriptions-item label="单次消费限额">
                  ¥{{ formatAmount(selectedCategory.singleConsumeLimit) }}
                </a-descriptions-item>
                <a-descriptions-item label="日消费限额">
                  ¥{{ formatAmount(selectedCategory.dailyConsumeLimit) }}
                </a-descriptions-item>
                <a-descriptions-item label="月消费限额">
                  ¥{{ formatAmount(selectedCategory.monthlyConsumeLimit) }}
                </a-descriptions-item>
                <a-descriptions-item label="跨区域消费">
                  <a-tag :color="selectedCategory.crossAreaConsume ? 'success' : 'default'">
                    {{ selectedCategory.crossAreaConsume ? '允许' : '禁止' }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="退款权限">
                  <a-tag :color="selectedCategory.refundPermission ? 'success' : 'default'">
                    {{ selectedCategory.refundPermission ? '允许' : '禁止' }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="透支权限">
                  <a-tag :color="selectedCategory.overdraftPermission ? 'warning' : 'default'">
                    {{ selectedCategory.overdraftPermission ? '允许' : '禁止' }}
                  </a-tag>
                </a-descriptions-item>
              </a-descriptions>

              <!-- 时间权限 -->
              <a-descriptions title="时间权限" :column="2" bordered size="small" style="margin-top: 16px;">
                <a-descriptions-item label="消费时间限制">
                  {{ selectedCategory.timeRestriction ? '启用' : '禁用' }}
                </a-descriptions-item>
                <a-descriptions-item label="工作日消费">
                  {{ formatTimeRange(selectedCategory.weekdayTimeRange) }}
                </a-descriptions-item>
                <a-descriptions-item label="周末消费">
                  {{ formatTimeRange(selectedCategory.weekendTimeRange) }}
                </a-descriptions-item>
                <a-descriptions-item label="节假日消费">
                  {{ selectedCategory.holidayConsume ? '允许' : '禁止' }}
                </a-descriptions-item>
              </a-descriptions>
            </div>

            <!-- 消费场所权限 -->
            <div class="place-permission-section" style="margin-top: 24px;">
              <a-divider>消费场所权限</a-divider>
              <a-row :gutter="16">
                <a-col :span="8" v-for="place in selectedCategory.allowedPlaces" :key="place.placeId">
                  <a-card size="small" class="place-card">
                    <div class="place-info">
                      <div class="place-name">{{ place.placeName }}</div>
                      <div class="place-desc">{{ place.description }}</div>
                      <a-tag size="small" color="green">{{ place.status === 'ENABLE' ? '可用' : '禁用' }}</a-tag>
                    </div>
                  </a-card>
                </a-col>
              </a-row>
            </div>

            <!-- 子分类列表 -->
            <div class="children-section" style="margin-top: 24px;" v-if="selectedCategory.children && selectedCategory.children.length > 0">
              <a-divider>子分类列表</a-divider>
              <a-table
                :columns="childrenColumns"
                :data-source="selectedCategory.children"
                :pagination="false"
                size="small"
              >
                <template #status="{ record }">
                  <a-tag :color="getStatusColor(record.status)">
                    {{ record.status === 'ENABLE' ? '启用' : '禁用' }}
                  </a-tag>
                </template>
                <template #action="{ record }">
                  <a-space>
                    <a-button type="link" size="small" @click="handleEditChild(record)">
                      编辑
                    </a-button>
                    <a-popconfirm
                      title="确定要删除这个子分类吗？"
                      @confirm="handleDeleteChild(record)"
                      ok-text="确定"
                      cancel-text="取消"
                    >
                      <a-button type="link" size="small" danger>
                        删除
                      </a-button>
                    </a-popconfirm>
                  </a-space>
                </template>
              </a-table>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 分类表单弹窗 -->
    <CategoryFormModal
      v-model:visible="formModalVisible"
      :category="currentCategory"
      :mode="formMode"
      :parent-category="parentCategory"
      @success="handleFormSuccess"
    />

    <!-- 账户列表弹窗 -->
    <AccountListModal
      v-model:visible="accountListModalVisible"
      :category="selectedCategory"
    />
  </div>
</template>

<script setup>
  import { ref, reactive, computed, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    PlusOutlined,
    FullscreenOutlined,
    FullscreenExitOutlined,
    GroupOutlined,
    UserOutlined,
    SettingOutlined,
    FileTextOutlined,
    EditOutlined,
    DeleteOutlined,
    EyeOutlined,
  } from '@ant-design/icons-vue';
  import { consumeCategoryApi } from '/@/api/business/consume/consume-category-api';
  import { formatAmount, formatDateTime } from '/@/lib/format';
  import CategoryFormModal from './components/CategoryFormModal.vue';
  import AccountListModal from './components/AccountListModal.vue';

  // ----------------------- 响应式数据 -----------------------
  const loading = ref(false);
  const searchKeyword = ref('');
  const expandedKeys = ref([]);
  const selectedKeys = ref([]);
  const categoryTreeData = ref([]);
  const selectedCategory = ref(null);
  const currentCategory = ref(null);
  const parentCategory = ref(null);

  // 统计数据
  const stats = reactive({
    totalCategories: 0,
    totalAccounts: 0,
    totalPermissions: 0,
    levelDepth: 0,
  });

  // 弹窗状态
  const formModalVisible = ref(false);
  const accountListModalVisible = ref(false);
  const formMode = ref('add'); // add | edit | addChild

  // 子分类表格列
  const childrenColumns = [
    {
      title: '分类名称',
      dataIndex: 'categoryName',
      key: 'categoryName',
    },
    {
      title: '分类编码',
      dataIndex: 'categoryCode',
      key: 'categoryCode',
    },
    {
      title: '关联账户',
      dataIndex: 'accountCount',
      key: 'accountCount',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      slots: { customRender: 'status' },
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'action',
      width: 120,
      slots: { customRender: 'action' },
    },
  ];

  // ----------------------- 生命周期 -----------------------
  onMounted(() => {
    loadCategoryTree();
    loadStatsData();
  });

  // ----------------------- 数据查询 -----------------------
  const loadCategoryTree = async () => {
    try {
      loading.value = true;

      // TODO: 调用后端API
      // const res = await consumeCategoryApi.getCategoryTree();
      // if (res.data) {
      //   categoryTreeData.value = res.data;
      // }

      // 模拟数据
      setTimeout(() => {
        categoryTreeData.value = generateMockTreeData();
        // 默认展开第一层
        expandedKeys.value = categoryTreeData.value.map(item => item.categoryId);
      }, 500);

    } catch (error) {
      console.error('加载分类树失败:', error);
      message.error('加载分类树失败');
    } finally {
      loading.value = false;
    }
  };

  const loadStatsData = async () => {
    try {
      // TODO: 调用后端API
      // const res = await consumeCategoryApi.getCategoryStats();
      // if (res.data) {
      //   Object.assign(stats, res.data);
      // }

      // 模拟数据
      stats.totalCategories = 48;
      stats.totalAccounts = 2456;
      stats.totalPermissions = 156;
      stats.levelDepth = 3;
    } catch (error) {
      console.error('加载统计数据失败:', error);
    }
  };

  // 生成模拟树形数据
  const generateMockTreeData = () => {
    return [
      {
        categoryId: 1,
        categoryName: '员工账户',
        categoryCode: 'EMPLOYEE',
        status: 'ENABLE',
        level: 1,
        sortOrder: 1,
        accountCount: 1856,
        description: '正式员工账户分类',
        createTime: new Date('2023-01-01'),
        children: [
          {
            categoryId: 11,
            categoryName: '管理层',
            categoryCode: 'EMPLOYEE_MGR',
            status: 'ENABLE',
            level: 2,
            sortOrder: 1,
            accountCount: 56,
            description: '管理层员工账户',
            createTime: new Date('2023-01-01'),
          },
          {
            categoryId: 12,
            categoryName: '技术部',
            categoryCode: 'EMPLOYEE_TECH',
            status: 'ENABLE',
            level: 2,
            sortOrder: 2,
            accountCount: 245,
            description: '技术部员工账户',
            createTime: new Date('2023-01-01'),
          },
          {
            categoryId: 13,
            categoryName: '行政部',
            categoryCode: 'EMPLOYEE_ADMIN',
            status: 'ENABLE',
            level: 2,
            sortOrder: 3,
            accountCount: 128,
            description: '行政部员工账户',
            createTime: new Date('2023-01-01'),
          },
        ],
      },
      {
        categoryId: 2,
        categoryName: '学生账户',
        categoryCode: 'STUDENT',
        status: 'ENABLE',
        level: 1,
        sortOrder: 2,
        accountCount: 523,
        description: '学生账户分类',
        createTime: new Date('2023-01-01'),
        children: [
          {
            categoryId: 21,
            categoryName: '本科生',
            categoryCode: 'STUDENT_UNDERGRAD',
            status: 'ENABLE',
            level: 2,
            sortOrder: 1,
            accountCount: 412,
            description: '本科生账户',
            createTime: new Date('2023-01-01'),
          },
          {
            categoryId: 22,
            categoryName: '研究生',
            categoryCode: 'STUDENT_GRADUATE',
            status: 'ENABLE',
            level: 2,
            sortOrder: 2,
            accountCount: 111,
            description: '研究生账户',
            createTime: new Date('2023-01-01'),
          },
        ],
      },
      {
        categoryId: 3,
        categoryName: '临时账户',
        categoryCode: 'TEMPORARY',
        status: 'ENABLE',
        level: 1,
        sortOrder: 3,
        accountCount: 77,
        description: '临时人员账户',
        createTime: new Date('2023-01-01'),
      },
    ];
  };

  // ----------------------- 事件处理 -----------------------
  const handleSelectCategory = (selectedKeys, { node }) => {
    if (selectedKeys.length > 0) {
      selectedCategory.value = { ...node };
      // 添加权限相关字段（模拟数据）
      selectedCategory.value.singleConsumeLimit = 100.00;
      selectedCategory.value.dailyConsumeLimit = 500.00;
      selectedCategory.value.monthlyConsumeLimit = 3000.00;
      selectedCategory.value.crossAreaConsume = true;
      selectedCategory.value.refundPermission = true;
      selectedCategory.value.overdraftPermission = false;
      selectedCategory.value.timeRestriction = true;
      selectedCategory.value.weekdayTimeRange = '07:00-22:00';
      selectedCategory.value.weekendTimeRange = '08:00-21:00';
      selectedCategory.value.holidayConsume = true;
      selectedCategory.value.allowedPlaces = [
        { placeId: 1, placeName: '第一食堂', description: '主食堂', status: 'ENABLE' },
        { placeId: 2, placeName: '第二食堂', description: '分食堂', status: 'ENABLE' },
        { placeId: 3, placeName: '便利店', description: '校园便利店', status: 'ENABLE' },
        { placeId: 4, placeName: '咖啡厅', description: '休闲咖啡厅', status: 'ENABLE' },
      ];
    } else {
      selectedCategory.value = null;
    }
  };

  const handleExpand = (expandedKeys, { node }) => {
    // 展开节点时的处理
  };

  const handleSearch = (value) => {
    // 搜索功能
    if (value) {
      // TODO: 实现搜索逻辑
      message.info('搜索功能开发中...');
    }
  };

  const handleExpandAll = () => {
    const allKeys = [];
    const collectKeys = (nodes) => {
      nodes.forEach(node => {
        allKeys.push(node.categoryId);
        if (node.children && node.children.length > 0) {
          collectKeys(node.children);
        }
      });
    };
    collectKeys(categoryTreeData.value);
    expandedKeys.value = allKeys;
  };

  const handleCollapseAll = () => {
    expandedKeys.value = [];
  };

  const handleAdd = () => {
    currentCategory.value = null;
    parentCategory.value = null;
    formMode.value = 'add';
    formModalVisible.value = true;
  };

  const handleEdit = () => {
    currentCategory.value = { ...selectedCategory.value };
    formMode.value = 'edit';
    formModalVisible.value = true;
  };

  const handleAddChild = () => {
    currentCategory.value = null;
    parentCategory.value = selectedCategory.value;
    formMode.value = 'addChild';
    formModalVisible.value = true;
  };

  const handleDelete = async () => {
    try {
      // TODO: 调用删除API
      // await consumeCategoryApi.deleteCategory(selectedCategory.value.categoryId);
      message.success('删除分类成功');
      loadCategoryTree();
      selectedCategory.value = null;
    } catch (error) {
      message.error('删除分类失败');
    }
  };

  const handleEditChild = (record) => {
    currentCategory.value = { ...record };
    formMode.value = 'edit';
    formModalVisible.value = true;
  };

  const handleDeleteChild = async (record) => {
    try {
      // TODO: 调用删除API
      // await consumeCategoryApi.deleteCategory(record.categoryId);
      message.success('删除子分类成功');
      loadCategoryTree();
      if (selectedCategory.value) {
        handleSelectCategory([selectedCategory.value.categoryId], { node: selectedCategory.value });
      }
    } catch (error) {
      message.error('删除子分类失败');
    }
  };

  const handleViewAccounts = () => {
    accountListModalVisible.value = true;
  };

  const handleFormSuccess = () => {
    formModalVisible.value = false;
    loadCategoryTree();
    loadStatsData();
    message.success(`${formMode.value === 'add' ? '添加' : '编辑'}分类成功`);
  };

  // ----------------------- 工具方法 -----------------------
  const getStatusColor = (status) => {
    return status === 'ENABLE' ? 'success' : 'error';
  };

  const formatTimeRange = (timeRange) => {
    return timeRange || '无限制';
  };
</script>

<style lang="less" scoped>
  .consume-category-page {
    padding: 24px;
    background: #f5f5f5;

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .page-title {
        h1 {
          margin: 0 0 8px 0;
          font-size: 28px;
          font-weight: 700;
          color: #262626;
        }

        p {
          margin: 0;
          color: #8c8c8c;
          font-size: 16px;
        }
      }
    }

    .stats-cards {
      margin-bottom: 24px;

      .stat-card {
        background: white;
        border-radius: 12px;
        padding: 24px;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
        border: 1px solid #f0f0f0;
        display: flex;
        align-items: center;
        gap: 16px;
        height: 100%;

        .stat-card-icon {
          width: 64px;
          height: 64px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 32px;
          color: white;
          flex-shrink: 0;
        }

        .stat-card-content {
          flex: 1;

          .stat-card-value {
            font-size: 28px;
            font-weight: 700;
            color: #262626;
            line-height: 1.2;
            margin-bottom: 4px;
          }

          .stat-card-title {
            font-size: 14px;
            color: #8c8c8c;
            font-weight: 500;
          }
        }
      }
    }

    .category-tree-card {
      height: calc(100vh - 300px);
      overflow-y: auto;

      .tree-node-title {
        display: flex;
        align-items: center;
        gap: 8px;
        flex: 1;

        .node-title {
          flex: 1;
        }

        .node-status {
          flex-shrink: 0;
        }
      }
    }

    .category-detail-card {
      height: calc(100vh - 300px);
      overflow-y: auto;

      .empty-state {
        height: 400px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .category-detail {
        .permission-section,
        .place-permission-section,
        .children-section {
          background: white;
          border-radius: 8px;
          padding: 16px;
          border: 1px solid #f0f0f0;
        }

        .place-card {
          .place-info {
            .place-name {
              font-weight: 600;
              margin-bottom: 4px;
            }

            .place-desc {
              font-size: 12px;
              color: #8c8c8c;
              margin-bottom: 8px;
            }
          }
        }
      }
    }

    // 响应式布局
    @media (max-width: 1200px) {
      .stats-cards {
        .stat-card {
          padding: 16px;

          .stat-card-icon {
            width: 48px;
            height: 48px;
            font-size: 24px;
          }

          .stat-card-content {
            .stat-card-value {
              font-size: 24px;
            }
          }
        }
      }
    }

    @media (max-width: 768px) {
      padding: 16px;

      .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }

      :deep(.ant-col) {
        margin-bottom: 16px;
      }
    }
  }
</style>