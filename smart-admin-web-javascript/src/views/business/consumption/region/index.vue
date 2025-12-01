<template>
  <div class="region-page">
    <!-- 搜索区 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="区域名称">
          <a-input v-model:value="searchForm.name" placeholder="请输入区域名称" allow-clear style="width: 150px" />
        </a-form-item>
        <a-form-item label="区域代码">
          <a-input v-model:value="searchForm.code" placeholder="请输入区域代码" allow-clear style="width: 150px" />
        </a-form-item>
        <a-form-item label="区域类型">
          <a-select v-model:value="searchForm.type" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option value="canteen">食堂</a-select-option>
            <a-select-option value="supermarket">超市</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="经营模式">
          <a-select v-model:value="searchForm.businessMode" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option value="meal">餐别模式</a-select-option>
            <a-select-option value="product">商品模式</a-select-option>
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
        <a-space>
          <span>区域管理</span>
          <a-radio-group v-model:value="viewMode" button-style="solid" size="small">
            <a-radio-button value="list">
              <UnorderedListOutlined /> 列表模式
            </a-radio-button>
            <a-radio-button value="tree">
              <ApartmentOutlined /> 树形模式
            </a-radio-button>
          </a-radio-group>
        </a-space>
      </template>
      <template #extra>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增区域
        </a-button>
      </template>

      <!-- 列表模式 -->
      <a-table
        v-show="viewMode === 'list'"
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <span :style="{ marginLeft: (record.level - 1) * 24 + 'px' }">{{ record.name }}</span>
          </template>
          <template v-else-if="column.key === 'type'">
            <a-tag :color="getTypeColor(record.type)">{{ getTypeName(record.type) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'businessMode'">
            {{ getBusinessModeName(record.businessMode) }}
          </template>
          <template v-else-if="column.key === 'features'">
            <a-space>
              <a-tag v-if="record.orderDeduction" color="success">📅 订餐扣款</a-tag>
              <a-tag v-if="record.inventoryManagement" color="processing">📦 进销存</a-tag>
              <span v-if="!record.orderDeduction && !record.inventoryManagement">-</span>
            </a-space>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge status="success" text="启用" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button v-if="record.level < 3" type="link" size="small" @click="handleAddChild(record)">
                <PlusOutlined /> 添加子区域
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-popconfirm
                title="确定要删除该区域吗？"
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

      <!-- 树形模式 -->
      <div v-show="viewMode === 'tree'" class="tree-view">
        <a-tree
          :tree-data="treeData"
          :field-names="{ children: 'children', title: 'name', key: 'id' }"
          default-expand-all
        >
          <template #title="{ name, code, level, devices, type, id }">
            <div class="tree-node-content">
              <div class="node-info">
                <a-tag :color="getTypeTagColor(level)">
                  <component :is="getTypeIcon(type)" />
                </a-tag>
                <span class="node-name">{{ name }}</span>
                <a-tag color="default" class="node-code">{{ code }}</a-tag>
                <span class="node-meta">
                  <TeamOutlined /> 第{{ level }}级
                  <a-divider type="vertical" />
                  <DesktopOutlined /> {{ devices || 0 }}台设备
                </span>
              </div>
              <div class="node-actions">
                <a-button v-if="level < 3" type="link" size="small" @click.stop="handleAddChild({ id })">
                  <PlusOutlined />
                </a-button>
                <a-button type="link" size="small" @click.stop="handleEdit({ id })">
                  <EditOutlined />
                </a-button>
                <a-popconfirm
                  title="确定要删除吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm.stop="handleDelete(id)"
                >
                  <a-button type="link" size="small" danger @click.stop>
                    <DeleteOutlined />
                  </a-button>
                </a-popconfirm>
              </div>
            </div>
          </template>
        </a-tree>
      </div>
    </a-card>

    <!-- 编辑抽屉 -->
    <a-drawer
      v-model:open="drawerVisible"
      :title="drawerTitle"
      width="500"
      :body-style="{ paddingBottom: '80px' }"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        layout="vertical"
      >
        <a-form-item label="区域名称" name="name">
          <a-input v-model:value="formData.name" placeholder="例如：第一食堂" />
        </a-form-item>

        <a-form-item label="区域代码" name="code">
          <a-input v-model:value="formData.code" placeholder="例如：CANTEEN01" />
          <template #extra>区域代码必须唯一</template>
        </a-form-item>

        <a-form-item label="上级区域" name="parentId">
          <a-tree-select
            v-model:value="formData.parentId"
            :tree-data="parentOptions"
            :field-names="{ children: 'children', label: 'name', value: 'id' }"
            placeholder="选择上级区域（可选）"
            allow-clear
            tree-default-expand-all
          />
          <template #extra>选择上级区域后，该区域将作为子区域</template>
        </a-form-item>

        <a-form-item label="区域类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择">
            <a-select-option value="canteen">食堂</a-select-option>
            <a-select-option value="supermarket">超市</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="经营模式" name="businessMode">
          <a-select v-model:value="formData.businessMode" placeholder="请选择" @change="handleBusinessModeChange">
            <a-select-option value="meal">餐别模式</a-select-option>
            <a-select-option value="product">商品模式</a-select-option>
          </a-select>
          <template #extra>餐别模式：按早中晚餐管理；商品模式：按商品销售管理</template>
        </a-form-item>

        <!-- 餐别模式专属 -->
        <a-form-item v-if="formData.businessMode === 'meal'" label="订餐扣款">
          <a-radio-group v-model:value="formData.orderDeduction">
            <a-radio :value="true">启用订餐扣款</a-radio>
            <a-radio :value="false">不启用</a-radio>
          </a-radio-group>
          <template #extra>启用后，用户可以预订餐食并自动扣款</template>
        </a-form-item>

        <!-- 商品模式专属 -->
        <a-form-item v-if="formData.businessMode === 'product'" label="进销存管理">
          <a-radio-group v-model:value="formData.inventoryManagement">
            <a-radio :value="true">启用进销存</a-radio>
            <a-radio :value="false">不启用</a-radio>
          </a-radio-group>
          <template #extra>启用后，系统会自动管理商品库存、进货、销售等</template>
        </a-form-item>

        <a-form-item label="排序" name="sort">
          <a-input-number v-model:value="formData.sort" :min="0" placeholder="数字越小越靠前" style="width: 100%" />
        </a-form-item>

        <a-form-item label="描述说明" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" placeholder="输入区域的描述信息" />
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CheckOutlined,
  UnorderedListOutlined,
  ApartmentOutlined,
  TeamOutlined,
  DesktopOutlined,
  ShopOutlined,
  ShoppingCartOutlined,
  AppstoreOutlined,
  HomeOutlined,
  ShoppingOutlined,
} from '@ant-design/icons-vue';

// 搜索表单
const searchForm = reactive({
  name: '',
  code: '',
  type: undefined,
  businessMode: undefined,
});

// 视图模式
const viewMode = ref('list');

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
  { title: '区域名称', dataIndex: 'name', key: 'name', width: 200 },
  { title: '区域代码', dataIndex: 'code', key: 'code', width: 150 },
  { title: '区域类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '经营模式', dataIndex: 'businessMode', key: 'businessMode', width: 120 },
  { title: '特殊功能', key: 'features', width: 150 },
  { title: '设备数量', dataIndex: 'devices', key: 'devices', width: 100, align: 'center' },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 280, fixed: 'right' },
];

// 树形数据
const treeData = ref([]);

// 抽屉相关
const drawerVisible = ref(false);
const drawerTitle = ref('新增区域');
const formRef = ref();
const submitLoading = ref(false);
const currentEditId = ref(null);

// 表单数据
const formData = reactive({
  name: '',
  code: '',
  parentId: undefined,
  type: undefined,
  businessMode: undefined,
  orderDeduction: false,
  inventoryManagement: false,
  sort: 0,
  description: '',
});

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入区域名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入区域代码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择区域类型', trigger: 'change' }],
  businessMode: [{ required: true, message: '请选择经营模式', trigger: 'change' }],
};

// 上级区域选项
const parentOptions = ref([]);

// 模拟数据
const mockData = ref([
  {
    id: 1,
    name: '第一食堂',
    code: 'CANTEEN01',
    type: 'canteen',
    businessMode: 'meal',
    orderDeduction: true,
    parentId: null,
    level: 1,
    devices: 0,
    sort: 1,
    children: [
      { id: 11, name: '一楼主食区', code: 'C01-F1', type: 'floor', businessMode: 'meal', parentId: 1, level: 2, devices: 4, sort: 1, children: [] },
      { id: 12, name: '二楼面食区', code: 'C01-F2', type: 'floor', businessMode: 'meal', parentId: 1, level: 2, devices: 5, sort: 2, children: [] },
      { id: 13, name: '三楼小吃区', code: 'C01-F3', type: 'floor', businessMode: 'meal', parentId: 1, level: 2, devices: 3, sort: 3, children: [] },
    ],
  },
  {
    id: 2,
    name: '第二食堂',
    code: 'CANTEEN02',
    type: 'canteen',
    businessMode: 'meal',
    orderDeduction: false,
    parentId: null,
    level: 1,
    devices: 0,
    sort: 2,
    children: [
      { id: 21, name: '一楼窗口区', code: 'C02-F1', type: 'floor', businessMode: 'meal', parentId: 2, level: 2, devices: 4, sort: 1, children: [] },
      { id: 22, name: '二楼自选区', code: 'C02-F2', type: 'floor', businessMode: 'meal', parentId: 2, level: 2, devices: 4, sort: 2, children: [] },
    ],
  },
  {
    id: 3,
    name: '超市便利店',
    code: 'SUPERMARKET01',
    type: 'supermarket',
    businessMode: 'product',
    inventoryManagement: true,
    parentId: null,
    level: 1,
    devices: 0,
    sort: 3,
    children: [
      { id: 31, name: '日用品区', code: 'SM01-DAILY', type: 'area', businessMode: 'product', parentId: 3, level: 2, devices: 2, sort: 1, children: [] },
      { id: 32, name: '食品饮料区', code: 'SM01-FOOD', type: 'area', businessMode: 'product', parentId: 3, level: 2, devices: 3, sort: 2, children: [] },
    ],
  },
  {
    id: 4,
    name: '员工餐厅',
    code: 'RESTAURANT01',
    type: 'canteen',
    businessMode: 'meal',
    orderDeduction: false,
    parentId: null,
    level: 1,
    devices: 4,
    sort: 4,
    children: [],
  },
]);

// 扁平化数据
const flattenData = (data) => {
  let result = [];
  const flatten = (items) => {
    items.forEach((item) => {
      result.push(item);
      if (item.children && item.children.length > 0) {
        flatten(item.children);
      }
    });
  };
  flatten(data);
  return result;
};

// 获取类型名称
const getTypeName = (type) => {
  const map = {
    canteen: '食堂',
    supermarket: '超市',
  };
  return map[type] || '-';
};

// 获取类型颜色
const getTypeColor = (type) => {
  const map = {
    canteen: 'blue',
    supermarket: 'green',
  };
  return map[type] || 'default';
};

// 获取类型标签颜色
const getTypeTagColor = (level) => {
  const map = {
    1: 'blue',
    2: 'green',
    3: 'orange',
  };
  return map[level] || 'default';
};

// 获取类型图标
const getTypeIcon = (type) => {
  const map = {
    canteen: ShopOutlined,
    supermarket: ShoppingCartOutlined,
  };
  return map[type] || ShopOutlined;
};

// 获取经营模式名称
const getBusinessModeName = (mode) => {
  const map = {
    meal: '餐别模式',
    product: '商品模式',
  };
  return map[mode] || '-';
};

// 查询数据
const loadData = () => {
  loading.value = true;
  
  setTimeout(() => {
    // 过滤数据
    let filteredData = JSON.parse(JSON.stringify(mockData.value));
    const flatData = flattenData(filteredData);
    
    const filtered = flatData.filter((item) => {
      if (searchForm.name && !item.name.includes(searchForm.name)) return false;
      if (searchForm.code && !item.code.includes(searchForm.code)) return false;
      if (searchForm.type && item.type !== searchForm.type) return false;
      if (searchForm.businessMode && item.businessMode !== searchForm.businessMode) return false;
      return true;
    });
    
    tableData.value = filtered;
    treeData.value = filteredData;
    pagination.total = filtered.length;
    loading.value = false;
  }, 300);
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
    type: undefined,
    businessMode: undefined,
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
  drawerTitle.value = '新增区域';
  resetForm();
  
  // 生成上级区域选项（最多2级）
  const flatData = flattenData(mockData.value).filter((r) => r.level < 3);
  parentOptions.value = mockData.value;
  
  drawerVisible.value = true;
};

// 添加子区域
const handleAddChild = (record) => {
  currentEditId.value = null;
  drawerTitle.value = '新增子区域';
  resetForm();
  formData.parentId = record.id;
  
  parentOptions.value = mockData.value;
  drawerVisible.value = true;
};

// 编辑
const handleEdit = (record) => {
  currentEditId.value = record.id;
  drawerTitle.value = '编辑区域';
  
  // 查找完整数据
  const findRegion = (id, data = mockData.value) => {
    for (let item of data) {
      if (item.id === id) return item;
      if (item.children) {
        const found = findRegion(id, item.children);
        if (found) return found;
      }
    }
    return null;
  };
  
  const region = findRegion(record.id);
  if (region) {
    Object.assign(formData, {
      name: region.name,
      code: region.code,
      parentId: region.parentId,
      type: region.type,
      businessMode: region.businessMode,
      orderDeduction: region.orderDeduction || false,
      inventoryManagement: region.inventoryManagement || false,
      sort: region.sort || 0,
      description: region.description || '',
    });
  }
  
  parentOptions.value = mockData.value;
  drawerVisible.value = true;
};

// 删除
const handleDelete = (id) => {
  // 查找并删除
  const deleteFromTree = (data, targetId) => {
    for (let i = 0; i < data.length; i++) {
      if (data[i].id === targetId) {
        if (data[i].children && data[i].children.length > 0) {
          message.error('该区域下还有子区域，无法删除！');
          return false;
        }
        data.splice(i, 1);
        return true;
      }
      if (data[i].children && deleteFromTree(data[i].children, targetId)) {
        return true;
      }
    }
    return false;
  };
  
  if (deleteFromTree(mockData.value, id)) {
    message.success('删除成功');
    loadData();
  }
};

// 经营模式变化处理
const handleBusinessModeChange = (value) => {
  // 切换经营模式时，重置对应的配置
  if (value !== 'meal') {
    formData.orderDeduction = false;
  }
  if (value !== 'product') {
    formData.inventoryManagement = false;
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validateFields();
    submitLoading.value = true;
    
    setTimeout(() => {
      // 根据经营模式设置相应的属性
      const submitData = { ...formData };
      if (formData.businessMode === 'meal') {
        // 餐别模式：保留订餐扣款，清除进销存
        delete submitData.inventoryManagement;
      } else if (formData.businessMode === 'product') {
        // 商品模式：保留进销存，清除订餐扣款
        delete submitData.orderDeduction;
      }
      
      if (currentEditId.value) {
        // 更新逻辑
        const updateInTree = (data, id, newData) => {
          for (let item of data) {
            if (item.id === id) {
              Object.assign(item, newData);
              return true;
            }
            if (item.children && updateInTree(item.children, id, newData)) {
              return true;
            }
          }
          return false;
        };
        
        updateInTree(mockData.value, currentEditId.value, submitData);
        message.success('更新成功');
      } else {
        // 新增逻辑
        const newRegion = {
          ...submitData,
          id: Date.now(),
          devices: 0,
          children: [],
        };
        
        if (formData.parentId) {
          // 添加到父节点
          const addToParent = (data, parentId, child) => {
            for (let item of data) {
              if (item.id === parentId) {
                child.level = item.level + 1;
                if (!item.children) item.children = [];
                item.children.push(child);
                return true;
              }
              if (item.children && addToParent(item.children, parentId, child)) {
                return true;
              }
            }
            return false;
          };
          addToParent(mockData.value, formData.parentId, newRegion);
        } else {
          // 添加为顶级节点
          newRegion.level = 1;
          mockData.value.push(newRegion);
        }
        
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
    name: '',
    code: '',
    parentId: undefined,
    type: undefined,
    businessMode: undefined,
    orderDeduction: false,
    inventoryManagement: false,
    sort: 0,
    description: '',
  });
  formRef.value?.resetFields();
};

// 初始化
onMounted(() => {
  loadData();
});
</script>

<style scoped lang="less">
.region-page {
  .search-card {
    margin-bottom: 16px;
  }

  .tree-view {
    padding: 16px;

    :deep(.ant-tree) {
      .tree-node-content {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        padding-right: 16px;

        .node-info {
          display: flex;
          align-items: center;
          gap: 8px;
          flex: 1;

          .node-name {
            font-weight: 500;
            color: rgba(0, 0, 0, 0.85);
          }

          .node-code {
            font-size: 12px;
          }

          .node-meta {
            font-size: 12px;
            color: rgba(0, 0, 0, 0.45);
            margin-left: 16px;
          }
        }

        .node-actions {
          display: none;
          gap: 4px;
        }

        &:hover .node-actions {
          display: flex;
        }
      }
    }
  }
}
</style>

