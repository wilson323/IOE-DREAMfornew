<!--
  * 消费管理 - Dashboard
  * 
  * @Author:    SmartAdmin
  * @Date:      2025-11-04
  * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="consumption-dashboard">
    <!-- Hero Section -->
    <div class="hero-section">
      <h1 class="hero-title">欢迎使用消费管理系统</h1>
      <p class="hero-subtitle">专业的企业消费管理解决方案，让数据化管理更简单高效</p>
    </div>

    <!-- 系统通知 -->
    <SystemNotice :notice-data="noticeData" />

    <!-- 统计卡片 -->
    <a-row :gutter="[16, 16]" class="stat-cards">
      <a-col :xs="24" :sm="12" :lg="6" v-for="stat in statsData" :key="stat.key">
        <div class="stat-card">
          <div class="stat-card-header">
            <div class="stat-card-title">{{ stat.title }}</div>
            <div class="stat-card-icon" :style="{ background: stat.color }">
              <component :is="$antIcons[stat.icon]" />
            </div>
          </div>
          <div class="stat-card-value">{{ stat.value }}</div>
          <div class="stat-card-change" :class="stat.changeType">
            <component :is="$antIcons[stat.changeType === 'positive' ? 'ArrowUpOutlined' : 'ArrowDownOutlined']" />
            {{ stat.change }} 较昨日
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 快速操作 -->
    <a-card class="quick-actions-card" :bordered="false">
      <template #title>
        <div class="card-title-wrapper">
          <AppstoreOutlined class="card-icon" />
          <span>快速操作</span>
        </div>
      </template>
      <a-row :gutter="[16, 16]">
        <a-col :xs="12" :sm="8" :lg="6" :xl="4" v-for="action in quickActions" :key="action.path">
          <div class="quick-action-card" @click="handleQuickAction(action)">
            <div class="quick-action-icon">
              <component :is="$antIcons[action.icon]" />
            </div>
            <div class="quick-action-title">{{ action.title }}</div>
            <div class="quick-action-desc">{{ action.desc }}</div>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <!-- 最近活动 -->
    <ActivityFeed :activities="activities" :loading="activityLoading" @refresh="queryActivities" />
  </div>
</template>

<script setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { message } from 'ant-design-vue';
  import { AppstoreOutlined } from '@ant-design/icons-vue';
  import { consumptionApi } from '/@/api/business/consumption/consumption-api';
  import { QUICK_ACTION_MENU } from '/@/constants/business/consumption/consumption-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import SystemNotice from './components/system-notice.vue';
  import ActivityFeed from './components/activity-feed.vue';

  // ----------------------- 基础数据 -----------------------
  const router = useRouter();
  const loading = ref(false);
  const activityLoading = ref(false);

  // 系统通知数据
  const noticeData = ref({
    title: '系统当前状态',
    content: '系统运行正常，当前已配置 8 个基础模块。更多功能模块正在开发中...',
    type: 'info',
  });

  // 统计数据
  const statsData = ref([
    {
      key: 'turnover',
      title: '今日营业额',
      value: '¥12,580.00',
      change: '+12.5%',
      changeType: 'positive',
      icon: 'DollarOutlined',
      color: 'linear-gradient(135deg, #1890ff, #096dd9)',
    },
    {
      key: 'orderCount',
      title: '今日订单数',
      value: '856',
      change: '+8.3%',
      changeType: 'positive',
      icon: 'ShoppingCartOutlined',
      color: 'linear-gradient(135deg, #52c41a, #389e0d)',
    },
    {
      key: 'activeUsers',
      title: '活跃用户',
      value: '425',
      change: '+15.2%',
      changeType: 'positive',
      icon: 'TeamOutlined',
      color: 'linear-gradient(135deg, #faad14, #d48806)',
    },
    {
      key: 'averagePrice',
      title: '平均客单价',
      value: '¥14.70',
      change: '-2.1%',
      changeType: 'negative',
      icon: 'FileTextOutlined',
      color: 'linear-gradient(135deg, #2f54eb, #1d39c4)',
    },
  ]);

  // 快速操作菜单
  const quickActions = ref(QUICK_ACTION_MENU);

  // 活动列表
  const activities = ref([]);

  // ----------------------- 初始化 -----------------------
  onMounted(() => {
    queryDashboardData();
    queryActivities();
  });

  // ----------------------- 查询数据 -----------------------
  
  /**
   * 查询Dashboard统计数据
   * TODO: 对接后端接口
   */
  async function queryDashboardData() {
    try {
      loading.value = true;
      
      // TODO: 后端接口对接
      // const res = await consumptionApi.queryDashboardStats();
      // if (res.data) {
      //   updateStatsData(res.data);
      // }
      
      // 模拟数据（开发阶段使用）
      setTimeout(() => {
        const mockData = {
          turnover: {
            value: '¥' + (Math.random() * 20000 + 10000).toFixed(2),
            change: '+' + (Math.random() * 20).toFixed(1) + '%',
            changeType: 'positive',
          },
          orderCount: {
            value: Math.floor(Math.random() * 500 + 500).toString(),
            change: '+' + (Math.random() * 15).toFixed(1) + '%',
            changeType: 'positive',
          },
          activeUsers: {
            value: Math.floor(Math.random() * 300 + 300).toString(),
            change: '+' + (Math.random() * 25).toFixed(1) + '%',
            changeType: 'positive',
          },
          averagePrice: {
            value: '¥' + (Math.random() * 10 + 10).toFixed(2),
            change: (Math.random() > 0.5 ? '+' : '-') + (Math.random() * 5).toFixed(1) + '%',
            changeType: Math.random() > 0.5 ? 'positive' : 'negative',
          },
        };
        updateStatsData(mockData);
      }, 500);
      
    } catch (error) {
      smartSentry.captureError(error);
    } finally {
      setTimeout(() => {
        loading.value = false;
      }, 500);
    }
  }

  /**
   * 查询最近活动
   * TODO: 对接后端接口
   */
  async function queryActivities() {
    try {
      activityLoading.value = true;
      
      // TODO: 后端接口对接
      // const res = await consumptionApi.queryRecentActivities({ pageSize: 10 });
      // if (res.data && res.data.list) {
      //   activities.value = res.data.list;
      //   return;
      // }
      
      // 模拟数据（开发阶段使用）
      setTimeout(() => {
        activities.value = [
          {
            id: 1,
            type: 'success',
            icon: 'UserAddOutlined',
            title: '新用户注册',
            description: '张三 (工号: EMP001) 完成账户注册并审核通过',
            time: '2分钟前',
          },
          {
            id: 2,
            type: 'info',
            icon: 'WalletOutlined',
            title: '批量补贴发放',
            description: '向员工账户批量发放本月补贴 ¥125,000',
            time: '15分钟前',
          },
          {
            id: 3,
            type: 'warning',
            icon: 'ExclamationCircleOutlined',
            title: '设备离线提醒',
            description: '第二食堂3号消费机连接异常，已发送维修通知',
            time: '1小时前',
          },
          {
            id: 4,
            type: 'success',
            icon: 'SyncOutlined',
            title: '数据同步完成',
            description: '所有分店数据已同步至中央服务器，同步记录2,847条',
            time: '2小时前',
          },
          {
            id: 5,
            type: 'info',
            icon: 'ShoppingCartOutlined',
            title: '订单高峰期',
            description: '午餐时段订单量激增，当前待处理订单256笔',
            time: '3小时前',
          },
          {
            id: 6,
            type: 'success',
            icon: 'CheckCircleOutlined',
            title: '月度对账完成',
            description: '10月份财务对账已完成，总交易额 ¥1,258,000',
            time: '5小时前',
          },
        ];
      }, 300);
      
    } catch (error) {
      smartSentry.captureError(error);
    } finally {
      setTimeout(() => {
        activityLoading.value = false;
      }, 300);
    }
  }

  /**
   * 更新统计数据
   */
  function updateStatsData(data) {
    if (!data) return;
    
    statsData.value = statsData.value.map((stat) => {
      const newData = data[stat.key];
      if (newData) {
        return {
          ...stat,
          value: newData.value || stat.value,
          change: newData.change || stat.change,
          changeType: newData.changeType || stat.changeType,
        };
      }
      return stat;
    });
  }

  // ----------------------- 操作方法 -----------------------

  /**
   * 快速操作点击
   */
  function handleQuickAction(action) {
    if (action.path) {
      router.push(action.path);
    } else {
      message.info(`${action.title} 功能开发中...`);
    }
  }
</script>

<style lang="less" scoped>
  .consumption-dashboard {
    // Hero Section
    .hero-section {
      background: linear-gradient(135deg, #e6f4ff, #bae0ff);
      padding: 48px 24px;
      margin: -24px -24px 24px;
      text-align: center;
      border-radius: 0 0 12px 12px;

      .hero-title {
        font-size: 32px;
        font-weight: 700;
        color: #262626;
        margin-bottom: 12px;
      }

      .hero-subtitle {
        font-size: 16px;
        color: #595959;
        margin: 0;
      }
    }

    // 统计卡片
    .stat-cards {
      margin-bottom: 24px;

      .stat-card {
        background: white;
        border-radius: 12px;
        padding: 24px;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
        border: 1px solid #f0f0f0;
        transition: all 0.3s ease;
        position: relative;
        overflow: hidden;
        height: 100%;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 4px;
          background: linear-gradient(90deg, #1890ff, #096dd9);
        }

        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        }

        .stat-card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16px;

          .stat-card-title {
            font-size: 14px;
            color: #8c8c8c;
            font-weight: 500;
          }

          .stat-card-icon {
            width: 48px;
            height: 48px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            color: white;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          }
        }

        .stat-card-value {
          font-size: 32px;
          font-weight: 700;
          color: #262626;
          margin-bottom: 8px;
          line-height: 1.2;
        }

        .stat-card-change {
          font-size: 14px;
          font-weight: 500;
          display: flex;
          align-items: center;
          gap: 4px;

          &.positive {
            color: #52c41a;
          }

          &.negative {
            color: #ff4d4f;
          }
        }
      }
    }

    // 快速操作
    .quick-actions-card {
      margin-bottom: 24px;

      :deep(.ant-card-head) {
        border-bottom: 1px solid #f0f0f0;
      }

      .card-title-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 18px;
        font-weight: 600;

        .card-icon {
          font-size: 20px;
          color: #1890ff;
        }
      }

      .quick-action-card {
        padding: 20px;
        border: 1px solid #f0f0f0;
        border-radius: 8px;
        text-align: center;
        cursor: pointer;
        transition: all 0.3s ease;
        background: #fafafa;
        height: 100%;

        &:hover {
          background: #e6f4ff;
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
          border-color: #91caff;
        }

        .quick-action-icon {
          font-size: 32px;
          margin-bottom: 12px;
          color: #1890ff;
        }

        .quick-action-title {
          font-weight: 600;
          margin-bottom: 4px;
          color: #262626;
          font-size: 14px;
        }

        .quick-action-desc {
          font-size: 12px;
          color: #8c8c8c;
          line-height: 1.4;
        }
      }
    }
  }

  // 响应式处理
  @media (max-width: 768px) {
    .consumption-dashboard {
      .hero-section {
        padding: 32px 16px;

        .hero-title {
          font-size: 24px;
        }

        .hero-subtitle {
          font-size: 14px;
        }
      }

      .stat-card {
        .stat-card-value {
          font-size: 24px;
        }
      }
    }
  }
</style>

