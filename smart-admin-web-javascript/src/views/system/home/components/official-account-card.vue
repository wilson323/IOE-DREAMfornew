<!--
  * Official Account Card Component
  * 企业级公众号管理卡片组件
-->
<template>
  <a-card :bordered="false" class="official-account-card" :loading="loading">
    <template #title>
      <a-space>
        <WechatOutlined />
        <span>Official Account</span>
      </a-space>
    </template>

    <template #extra>
      <a-button type="link" @click="handleRefresh">
        <template #icon><ReloadOutlined /></template>
      </a-button>
    </template>

    <div class="account-stats">
      <div class="stat-item">
        <a-statistic
          title="Followers"
          :value="accountData.followers"
          :prefix="h(UserOutlined)"
          :value-style="{ color: '#3f8600' }"
        />
      </div>
      <div class="stat-item">
        <a-statistic
          title="Articles"
          :value="accountData.articles"
          :prefix="h(FileTextOutlined)"
          :value-style="{ color: '#1890ff' }"
        />
      </div>
      <div class="stat-item">
        <a-statistic
          title="Messages"
          :value="accountData.messages"
          :prefix="h(MessageOutlined)"
          :value-style="{ color: '#722ed1' }"
        />
      </div>
    </div>

    <div class="account-info">
      <a-descriptions size="small" :column="2">
        <a-descriptions-item label="Account Name">
          {{ accountData.accountName || 'IOE-DREAM Official' }}
        </a-descriptions-item>
        <a-descriptions-item label="Account ID">
          {{ accountData.accountId || 'N/A' }}
        </a-descriptions-item>
        <a-descriptions-item label="Status">
          <a-badge
            :status="accountData.status === 'active' ? 'success' : 'error'"
            :text="accountData.status === 'active' ? 'Active' : 'Inactive'"
          />
        </a-descriptions-item>
        <a-descriptions-item label="Last Updated">
          {{ accountData.lastUpdated || 'Never' }}
        </a-descriptions-item>
      </a-descriptions>
    </div>

    <div class="quick-actions">
      <a-space>
        <a-button type="primary" @click="handleManageAccount">
          <template #icon><SettingOutlined /></template>
          Manage
        </a-button>
        <a-button @click="handleViewAnalytics">
          <template #icon><BarChartOutlined /></template>
          Analytics
        </a-button>
        <a-button @click="handleSendMessage">
          <template #icon><SendOutlined /></template>
          Send Message
        </a-button>
      </a-space>
    </div>
  </a-card>
</template>

<script setup>
  import { ref, reactive, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import {
    WechatOutlined,
    ReloadOutlined,
    UserOutlined,
    FileTextOutlined,
    MessageOutlined,
    SettingOutlined,
    BarChartOutlined,
    SendOutlined,
  } from '@ant-design/icons-vue';

  const loading = ref(false);
  const accountData = reactive({
    followers: 0,
    articles: 0,
    messages: 0,
    accountName: '',
    accountId: '',
    status: 'active',
    lastUpdated: '',
  });

  const loadAccountData = async () => {
    loading.value = true;
    try {
      await new Promise(resolve => setTimeout(resolve, 1000));
      Object.assign(accountData, {
        followers: 12580,
        articles: 156,
        messages: 89,
        accountName: 'IOE-DREAM Official',
        accountId: 'gh_ioedream_official',
        status: 'active',
        lastUpdated: new Date().toLocaleString(),
      });
    } catch (error) {
      console.error('Failed to load account data:', error);
      message.error('Failed to load official account data');
    } finally {
      loading.value = false;
    }
  };

  const handleRefresh = () => {
    loadAccountData();
  };

  const handleManageAccount = () => {
    message.info('Opening account management interface');
  };

  const handleViewAnalytics = () => {
    message.info('Opening analytics dashboard');
  };

  const handleSendMessage = () => {
    message.info('Opening message composer');
  };

  const refreshInterval = ref(null);

  onMounted(() => {
    loadAccountData();
    refreshInterval.value = setInterval(() => {
      loadAccountData();
    }, 5 * 60 * 1000);
  });
</script>

<style lang="less" scoped>
  .official-account-card {
    .account-stats {
      display: flex;
      justify-content: space-between;
      margin-bottom: 24px;

      .stat-item {
        flex: 1;
        text-align: center;
        padding: 16px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 8px;
        color: white;
        margin: 0 8px;

        &:first-child {
          margin-left: 0;
        }

        &:last-child {
          margin-right: 0;
        }
      }
    }

    .account-info {
      margin-bottom: 24px;
    }

    .quick-actions {
      .ant-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 40px;
        border-radius: 6px;
        font-weight: 500;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }
      }
    }

    @media (max-width: 768px) {
      .account-stats {
        flex-direction: column;

        .stat-item {
          margin: 8px 0;
        }
      }
    }
  }
</style>