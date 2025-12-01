<template>
  <div class="consume-main-page">
    <!-- 头部信息 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">智能消费系统</h1>
        <p class="page-description">支持多种消费模式的智能消费管理平台</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" size="large" @click="showQuickConsume">
          <template #icon><Icon type="ios-cart" /></template>
          快速消费
        </a-button>
      </div>
    </div>

    <!-- 用户余额卡片 -->
    <div class="balance-card">
      <div class="balance-info">
        <div class="balance-amount">
          <span class="currency">¥</span>
          <span class="amount">{{ formatAmount(userBalance) }}</span>
        </div>
        <div class="balance-label">账户余额</div>
      </div>
      <div class="balance-actions">
        <a-button type="link" @click="rechargeAccount">
          <template #icon><Icon type="ios-card" /></template>
          充值
        </a-button>
        <a-button type="link" @click="viewAccountDetails">
          <template #icon><Icon type="ios-list-box" /></template>
          详情
        </a-button>
      </div>
    </div>

    <!-- 消费模式选择 -->
    <div class="consume-modes">
      <h2 class="section-title">选择消费模式</h2>
      <div class="modes-grid">
        <!-- 固定金额模式 -->
        <div class="mode-card" @click="selectConsumeMode('FIXED_AMOUNT')">
          <div class="mode-icon">
            <Icon type="ios-cash" size="32" />
          </div>
          <div class="mode-content">
            <h3 class="mode-name">固定金额模式</h3>
            <p class="mode-description">适用于食堂标准餐、固定收费项目</p>
            <div class="mode-features">
              <span class="feature-tag">快速便捷</span>
              <span class="feature-tag">标准定价</span>
            </div>
          </div>
          <div class="mode-arrow">
            <Icon type="ios-arrow-forward" />
          </div>
        </div>

        <!-- 自由金额模式 -->
        <div class="mode-card" @click="selectConsumeMode('FREE_AMOUNT')">
          <div class="mode-icon">
            <Icon type="ios-create" size="32" />
          </div>
          <div class="mode-content">
            <h3 class="mode-name">自由金额模式</h3>
            <p class="mode-description">适用于超市购物、个性化服务</p>
            <div class="mode-features">
              <span class="feature-tag">灵活定价</span>
              <span class="feature-tag">实时结算</span>
            </div>
          </div>
          <div class="mode-arrow">
            <Icon type="ios-arrow-forward" />
          </div>
        </div>

        <!-- 商品扫码模式 -->
        <div class="mode-card" @click="selectConsumeMode('PRODUCT')">
          <div class="mode-icon">
            <Icon type="ios-barcode" size="32" />
          </div>
          <div class="mode-content">
            <h3 class="mode-name">商品扫码模式</h3>
            <p class="mode-description">适用于超市商品、便利店购买</p>
            <div class="mode-features">
              <span class="feature-tag">扫码识别</span>
              <span class="feature-tag">库存管理</span>
            </div>
          </div>
          <div class="mode-arrow">
            <Icon type="ios-arrow-forward" />
          </div>
        </div>

        <!-- 计量计费模式 -->
        <div class="mode-card" @click="selectConsumeMode('METERING')">
          <div class="mode-icon">
            <Icon type="ios-speedometer" size="32" />
          </div>
          <div class="mode-content">
            <h3 class="mode-name">计量计费模式</h3>
            <p class="mode-description">适用于水电费、按量计费</p>
            <div class="mode-features">
              <span class="feature-tag">按量计费</span>
              <span class="feature-tag">精确计量</span>
            </div>
          </div>
          <div class="mode-arrow">
            <Icon type="ios-arrow-forward" />
          </div>
        </div>

        <!-- 订餐模式 -->
        <div class="mode-card" @click="selectConsumeMode('ORDERING')">
          <div class="mode-icon">
            <Icon type="ios-restaurant" size="32" />
          </div>
          <div class="mode-content">
            <h3 class="mode-name">订餐模式</h3>
            <p class="mode-description">适用于餐厅订餐、预约服务</p>
            <div class="mode-features">
              <span class="feature-tag">提前预订</span>
              <span class="feature-tag">菜单选择</span>
            </div>
          </div>
          <div class="mode-arrow">
            <Icon type="ios-arrow-forward" />
          </div>
        </div>

        <!-- 智能模式 -->
        <div class="mode-card smart-mode" @click="selectConsumeMode('SMART')">
          <div class="mode-icon">
            <Icon type="ios-bulb" size="32" />
          </div>
          <div class="mode-content">
            <h3 class="mode-name">智能模式</h3>
            <p class="mode-description">AI推荐、个性化服务</p>
            <div class="mode-features">
              <span class="feature-tag">智能推荐</span>
              <span class="feature-tag">个性化</span>
            </div>
          </div>
          <div class="mode-arrow">
            <Icon type="ios-arrow-forward" />
          </div>
        </div>
      </div>
    </div>

    <!-- 最近消费记录 -->
    <div class="recent-consumes">
      <div class="section-header">
        <h2 class="section-title">最近消费记录</h2>
        <a-button type="link" @click="viewAllRecords">
          查看全部
          <template #icon><Icon type="ios-arrow-forward" /></template>
        </a-button>
      </div>

      <div class="records-list">
        <a-list :data="recentRecords" :loading="recordsLoading">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #title>
                  <span class="record-title">{{ item.orderNo }}</span>
                </template>
                <template #description>
                  <div class="record-info">
                    <span class="record-mode">{{ getModeDescription(item.consumeMode) }}</span>
                    <span class="record-time">{{ formatTime(item.payTime) }}</span>
                  </div>
                </template>
              </a-list-item-meta>
            </a-list-item>
            <template #actions>
              <div class="record-amount">¥{{ formatAmount(item.amount) }}</div>
            </template>
          </a-list-item>
        </template>
      </a-list>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <h2 class="section-title">快速操作</h2>
      <div class="actions-grid">
        <div class="action-item" @click="scanQRCode">
          <div class="action-icon">
            <Icon type="ios-qr-scanner" size="24" />
          </div>
          <div class="action-text">扫码消费</div>
        </div>
        <div class="action-item" @click="viewStatistics">
          <div class="action-icon">
            <Icon type="ios-stats" size="24" />
          </div>
          <div class="action-text">消费统计</div>
        </div>
        <div class="action-item" @click="viewReports">
          <div class="action-icon">
            <Icon type="ios-document" size="24" />
          </div>
          <div class="action-text">消费报表</div>
        </div>
        <div class="action-item" @click="deviceManagement">
          <div class="action-icon">
            <Icon type="ios-desktop" size="24" />
          </div>
          <div class="action-text">设备管理</div>
        </div>
      </div>
    </div>

    <!-- 快速消费弹窗 -->
    <a-modal
      v-model:open="quickConsumeVisible"
      title="快速消费"
      width="500px"
      @ok="handleQuickConsume"
      @cancel="quickConsumeVisible = false"
    >
      <div class="quick-consume-form">
        <a-form :model="quickConsumeForm" layout="vertical">
          <a-form-item label="选择消费模式">
            <a-select
              v-model:value="quickConsumeForm.mode"
              placeholder="请选择消费模式"
              @change="onModeChange"
            >
              <a-select-option value="FIXED_AMOUNT">固定金额</a-select-option>
              <a-select-option value="FREE_AMOUNT">自由金额</a-select-option>
              <a-select-option value="PRODUCT">商品扫码</a-select-option>
              <a-select-option value="SMART">智能模式</a-select-option>
            </a-select>
          </a-form-item>

          <!-- 固定金额模式特有字段 -->
          <a-form-item v-if="quickConsumeForm.mode === 'FIXED_AMOUNT'" label="选择金额">
            <a-select
              v-model:value="quickConsumeForm.fixedAmount"
              placeholder="请选择固定金额"
            >
              <a-select-option value="5.00">¥5.00</a-select-option>
              <a-select-option value="8.00">¥8.00</a-select-option>
              <a-select-option value="10.00">¥10.00</a-select-option>
              <a-select-option value="12.00">¥12.00</a-select-option>
              <a-select-option value="15.00">¥15.00</a-select-option>
            </a-select>
          </a-form-item>

          <!-- 自由金额模式特有字段 -->
          <a-form-item v-if="quickConsumeForm.mode === 'FREE_AMOUNT'" label="消费金额">
            <a-input-number
              v-model:value="quickConsumeForm.amount"
              :precision="2"
              :min="0.01"
              :max="9999.99"
              placeholder="请输入消费金额"
              style="width: 100%"
            />
          </a-form-item>

          <!-- 商品扫码模式特有字段 -->
          <a-form-item v-if="quickConsumeForm.mode === 'PRODUCT'" label="商品编码">
            <a-input
              v-model:value="quickConsumeForm.productCode"
              placeholder="请输入或扫描商品编码"
              @keyup.enter="handleProductSearch"
            >
              <template #suffix>
                <a-button type="link" @click="scanProductCode">
                  <Icon type="ios-camera" />
                </a-button>
              </template>
            </a-input>
          </a-form-item>

          <!-- 智能模式特有字段 -->
          <a-form-item v-if="quickConsumeForm.mode === 'SMART'" label="推荐商品">
            <div class="smart-recommendations">
              <div v-for="item in smartRecommendations" :key="item.id" class="recommendation-item">
                <div class="recommendation-name">{{ item.name }}</div>
                <div class="recommendation-price">¥{{ item.price }}</div>
                <a-button size="small" type="primary" @click="selectRecommendation(item)">
                  选择
                </a-button>
              </div>
            </div>
          </a-form-item>

          <a-form-item label="备注">
            <a-textarea
              v-model:value="quickConsumeForm.remark"
              placeholder="请输入备注信息（可选）"
              :rows="3"
            />
          </a-form-item>
        </a-form>
      </div>
    </a-modal>

    <!-- 扫码组件 -->
    <QRCodeScanner
      v-model:open="qrScannerVisible"
      :title="扫描二维码"
      @scan="handleQRScan"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const router = useRouter()

// 响应式数据
const userBalance = ref(0.00)
const recentRecords = ref([])
const recordsLoading = ref(false)
const quickConsumeVisible = ref(false)
const qrScannerVisible = ref(false)
const smartRecommendations = ref([])

// 快速消费表单
const quickConsumeForm = reactive({
  mode: '',
  fixedAmount: '',
  amount: null,
  productCode: '',
  remark: ''
})

// 消费模式描述映射
const modeDescriptions = {
  FIXED_AMOUNT: '固定金额',
  FREE_AMOUNT: '自由金额',
  PRODUCT: '商品扫码',
  ORDERING: '订餐',
  METERING: '计量计费',
  SMART: '智能模式'
}

// 生命周期
onMounted(() => {
  loadUserData()
  loadRecentRecords()
  loadSmartRecommendations()
})

// 加载用户数据
const loadUserData = async () => {
  try {
    // TODO: 调用API获取用户余额
    // const response = await api.getUserBalance()
    // userBalance.value = response.data.balance
    userBalance.value = 1000.00 // 模拟数据
  } catch (error) {
    console.error('获取用户数据失败:', error)
  }
}

// 加载最近消费记录
const loadRecentRecords = async () => {
  recordsLoading.value = true
  try {
    // TODO: 调用API获取最近消费记录
    // const response = await api.getRecentConsumeRecords()
    // recentRecords.value = response.data.records
    recentRecords.value = [
      {
        id: 1,
        orderNo: 'ORD20251125001',
        consumeMode: 'FIXED_AMOUNT',
        amount: 12.00,
        payTime: '2025-11-25 12:30:00',
        status: 'SUCCESS'
      },
      {
        id: 2,
        orderNo: 'ORD20251125002',
        consumeMode: 'FREE_AMOUNT',
        amount: 25.50,
        payTime: '2025-11-25 12:15:00',
        status: 'SUCCESS'
      }
    ] // 模拟数据
  } catch (error) {
    console.error('获取最近记录失败:', error)
  } finally {
    recordsLoading.value = false
  }
}

// 加载智能推荐
const loadSmartRecommendations = () => {
  // TODO: 调用AI推荐API
  smartRecommendations.value = [
    {
      id: 1,
      name: '营养套餐A',
      price: 15.00,
      description: '营养丰富，均衡搭配'
    },
    {
      id: 2,
      name: '经济套餐',
      price: 8.00,
      description: '实惠选择，性价比高'
    }
  ]
}

// 选择消费模式
const selectConsumeMode = (mode) => {
  router.push({
    path: `/business/consume/mode/${mode}`
  })
}

// 快速消费
const showQuickConsume = () => {
  quickConsumeVisible.value = true
  quickConsumeForm.mode = 'FIXED_AMOUNT'
  quickConsumeForm.fixedAmount = '10.00'
}

// 处理快速消费
const handleQuickConsume = () => {
  if (!quickConsumeForm.mode) {
    message.error('请选择消费模式')
    return
  }

  // 根据模式处理消费
  switch (quickConsumeForm.mode) {
    case 'FIXED_AMOUNT':
      if (!quickConsumeForm.fixedAmount) {
        message.error('请选择固定金额')
        return
      }
      processFixedAmountConsume()
      break
    case 'FREE_AMOUNT':
      if (!quickConsumeForm.amount || quickConsumeForm.amount <= 0) {
        message.error('请输入有效的消费金额')
        return
      }
      processFreeAmountConsume()
      break
    case 'PRODUCT':
      if (!quickConsumeForm.productCode) {
        message.error('请输入商品编码')
        return
      }
      processProductConsume()
      break
    case 'SMART':
      processSmartConsume()
      break
  }

  quickConsumeVisible.value = false
}

// 处理固定金额消费
const processFixedAmountConsume = async () => {
  try {
    // TODO: 调用API处理固定金额消费
    message.loading('处理中...')

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))

    message.success('消费成功！')
    // 重置表单
    quickConsumeForm.fixedAmount = '10.00'
    quickConsumeForm.remark = ''

    // 刷新数据
    loadUserData()
    loadRecentRecords()

  } catch (error) {
    console.error('固定金额消费失败:', error)
    message.error('消费失败，请重试')
  }
}

// 处理自由金额消费
const processFreeAmountConsume = async () => {
  try {
    // TODO: 调用API处理自由金额消费
    message.loading('处理中...')

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))

    message.success(`消费成功！金额：¥${quickConsumeForm.amount}`)
    // 重置表单
    quickConsumeForm.amount = null
    quickConsume.remark = ''

    // 刷新数据
    loadUserData()
    loadRecentRecords()

  } catch (error) {
    console.error('自由金额消费失败:', error)
    message.error('消费失败，请重试')
  }
}

// 处理商品消费
const processProductConsume = async () => {
  try {
    // TODO: 调用API处理商品消费
    message.loading('处理中...')

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))

    message.success('商品消费成功！')
    // 重置表单
    quickConsumeForm.productCode = ''
    quickConsumeForm.remark = ''

    // 刷新数据
    loadUserData()
    loadRecentRecords()

  } catch (error) {
    console.error('商品消费失败:', error)
    message.error('消费失败，请重试')
  }
}

// 处理智能消费
const processSmartConsume = async () => {
  try {
    // TODO: 调用API处理智能消费
    message.loading('处理中...')

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))

    message.success('智能消费成功！')
    // 重置表单
    quickConsumeForm.remark = ''

    // 刷新数据
    loadUserData()
    loadRecentRecords()
    loadSmartRecommendations()

  } catch (error) {
    console.error('智能消费失败:', error)
    message.error('消费失败，请重试')
  }
}

// 模式变化处理
const onModeChange = (value) => {
  // 清空特定模式的字段
  quickConsumeForm.fixedAmount = ''
  quickConsumeForm.amount = null
  quickConsumeForm.productCode = ''

  // 设置默认值
  if (value === 'FIXED_AMOUNT') {
    quickConsumeForm.fixedAmount = '10.00'
  }
}

// 选择推荐商品
const selectRecommendation = (item) => {
  quickConsumeForm.remark = `智能推荐: ${item.name} - ${item.description}`
}

// 充值账户
const rechargeAccount = () => {
  router.push('/business/consume/account')
}

// 查看账户详情
const viewAccountDetails = () => {
  router.push('/business/consume/account')
}

// 查看全部记录
const viewAllRecords = () => {
  router.push('/business/consume/record')
}

// 扫描二维码
const scanQRCode = () => {
  qrScannerVisible.value = true
}

// 处理二维码扫描
const handleQRScan = (result) => {
  qrScannerVisible.value = false

  if (result.type === 'PRODUCT') {
    // 商品二维码，跳转到商品消费模式
    quickConsumeForm.mode = 'PRODUCT'
    quickConsumeForm.productCode = result.data.code
    quickConsumeVisible.value = true
  } else {
    // 其他类型的二维码处理
    message.info(`识别到二维码类型: ${result.type}`)
  }
}

// 扫描商品码
const scanProductCode = () => {
  qrScannerVisible.value = true
}

// 处理商品搜索
const handleProductSearch = async () => {
  if (!quickConsumeForm.productCode) {
    return
  }

  try {
    // TODO: 调用API搜索商品
    console.log('搜索商品:', quickConsumeForm.productCode)
    // 模拟搜索结果
    message.success(`找到商品: ${quickConsumeForm.productCode}`)
  } catch (error) {
    console.error('搜索商品失败:', error)
    message.error('商品搜索失败')
  }
}

// 查看统计
const viewStatistics = () => {
  router.push('/business/consume/statistics')
}

// 查看报表
const viewReports = () => {
  router.push('/business/consume/report')
}

// 设备管理
const deviceManagement = () => {
  router.push('/business/consume/device')
}

// 工具函数
const formatAmount = (amount) => {
  return Number(amount || 0).toFixed(2)
}

const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

const getModeDescription = (mode) => {
  return modeDescriptions[mode] || mode
}
</script>

<style scoped>
.consume-main-page {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  padding: 0 4px;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2328;
  margin: 0 0 8px 0;
}

.page-description {
  color: #64748b;
  font-size: 16px;
  margin: 0;
}

.balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.balance-amount {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.currency {
  font-size: 20px;
  font-weight: normal;
}

.amount {
  font-size: 36px;
  font-weight: bold;
}

.balance-label {
  font-size: 14px;
  opacity: 0.8;
  margin-top: 4px;
}

.balance-actions {
  display: flex;
  gap: 16px;
}

.balance-actions .ant-btn-link {
  color: white;
  font-weight: 500;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2328;
  margin: 0 0 16px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.modes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.mode-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 2px solid transparent;
}

.mode-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  border-color: #667eea;
}

.mode-card.smart-mode {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.mode-icon {
  margin-right: 16px;
  padding: 12px;
  border-radius: 8px;
  background: #f0f2f5;
  color: #667eea;
}

.mode-card.smart-mode .mode-icon {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.mode-content {
  flex: 1;
  margin-right: 16px;
}

.mode-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: inherit;
}

.mode-description {
  font-size: 14px;
  margin: 0 0 12px 0;
  opacity: 0.7;
}

.mode-features {
  display: flex;
  gap: 8px;
}

.feature-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f0f2f5;
  color: #667eea;
  font-weight: 500;
}

.mode-card.smart-mode .feature-tag {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.mode-arrow {
  color: #999;
}

.recent-consumes {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.records-list {
  margin-top: 16px;
}

.record-title {
  font-weight: 500;
}

.record-info {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #64748b;
}

.record-mode {
  padding: 2px 8px;
  background: #e2e8f0;
  border-radius: 4px;
  font-size: 12px;
}

.record-time {
  color: #64748b;
}

.record-amount {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.quick-actions {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item:hover {
  background: #e2e8f0;
}

.action-icon {
  margin-bottom: 8px;
  color: #667eea;
}

.action-text {
  font-size: 14px;
  color: #475569;
  font-weight: 500;
}

.quick-consume-form {
  padding: 16px 0;
}

.smart-recommendations {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommendation-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.recommendation-name {
  flex: 1;
  font-weight: 500;
}

.recommendation-price {
  margin: 0 16px;
  color: #f56c6c;
  font-weight: 600;
}
</style>