<!--
  * 商品管理页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM
-->
<template>
  <div class="product-list-page">
    <a-card :bordered="false">
      <template #title>
        <span>商品管理</span>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增商品
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        row-key="productId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'imageUrl'">
            <a-image
              :src="record.imageUrl"
              :alt="record.productName"
              style="width: 60px; height: 60px; object-fit: cover;"
            />
          </template>

          <template v-else-if="column.key === 'price'">
            <span class="price-text">￥{{ Number(record.price || 0).toFixed(2) }}</span>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="record.status === 1 ? 'success' : 'error'"
              :text="record.status === 1 ? '上架' : '下架'"
            />
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">
                查看
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="handleToggleStatus(record)">
                {{ record.status === 1 ? '下架' : '上架' }}
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    PlusOutlined,
    ReloadOutlined,
  } from '@ant-design/icons-vue';
  import { consumeApi } from '/@/api/business/consume/consume-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  // 表格数据
  const tableData = ref([]);
  const loading = ref(false);

  // 分页配置
  const pagination = reactive({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  });

  // 表格列配置
  const columns = [
    {
      title: '商品图片',
      key: 'imageUrl',
      width: 100,
    },
    {
      title: '商品名称',
      dataIndex: 'productName',
      key: 'productName',
    },
    {
      title: '商品编码',
      dataIndex: 'productCode',
      key: 'productCode',
    },
    {
      title: '价格',
      key: 'price',
      width: 100,
    },
    {
      title: '库存',
      dataIndex: 'stock',
      key: 'stock',
      width: 80,
    },
    {
      title: '分类',
      dataIndex: 'categoryName',
      key: 'categoryName',
    },
    {
      title: '状态',
      key: 'status',
      width: 80,
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
    },
  ];

  // 加载商品列表
  const loadProductList = async () => {
    loading.value = true;
    try {
      const result = await consumeApi.pageProducts({
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      });
      if (result.code === 200 && result.data) {
        tableData.value = result.data.list || [];
        pagination.total = result.data.total || 0;
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('加载商品列表失败');
    } finally {
      loading.value = false;
    }
  };

  // 表格变化事件
  const handleTableChange = (pag) => {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadProductList();
  };

  // 新增商品
  const handleAdd = () => {
    message.info('新增商品功能开发中');
  };

  // 查看商品
  const handleView = (record) => {
    Modal.info({
      title: '商品详情',
      content: `商品名称: ${record.productName}`,
    });
  };

  // 编辑商品
  const handleEdit = (record) => {
    message.info('编辑商品功能开发中');
  };

  // 切换状态
  const handleToggleStatus = (record) => {
    const action = record.status === 1 ? '下架' : '上架';
    Modal.confirm({
      title: `确认${action}`,
      content: `确定要${action}商品 ${record.productName} 吗？`,
      onOk: async () => {
        try {
          await consumeApi.setProductAvailable(record.productId, record.status !== 1);
          message.success(`${action}成功`);
          loadProductList();
        } catch (error) {
          smartSentry.captureError(error);
          message.error(`${action}失败`);
        }
      },
    });
  };

  // 删除商品
  const handleDelete = (record) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除商品 ${record.productName} 吗？`,
      onOk: async () => {
        try {
          await consumeApi.deleteProduct(record.productId);
          message.success('删除成功');
          loadProductList();
        } catch (error) {
          smartSentry.captureError(error);
          message.error('删除失败');
        }
      },
    });
  };

  // 刷新列表
  const handleRefresh = () => {
    loadProductList();
  };

  // 组件挂载
  onMounted(() => {
    loadProductList();
  });
</script>

<style lang="less" scoped>
  .product-list-page {
    .price-text {
      font-weight: 600;
      color: #f50;
    }

    .ant-image {
      border-radius: 4px;
    }
  }
</style>
