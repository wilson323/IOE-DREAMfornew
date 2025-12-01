<template>
  <div class="consume-mobile-container">
    <!-- 移动端头部导航 -->
    <div class="mobile-header">
      <div class="header-content">
        <div class="header-left">
          <van-icon name="arrow-left" @click="goBack" />
          <span class="header-title">智慧消费</span>
        </div>
        <div class="header-right">
          <van-icon name="search" @click="showSearch = true" />
          <van-icon name="user-o" @click="goToProfile" />
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 快捷功能区 -->
      <div class="quick-actions">
        <van-grid :column-num="4" :gutter="12">
          <van-grid-item
            v-for="action in quickActions"
            :key="action.id"
            :icon="action.icon"
            :text="action.text"
            @click="handleQuickAction(action)"
          />
        </van-grid>
      </div>

      <!-- 消费模式选择 -->
      <div class="consume-mode-section">
        <div class="section-title">选择消费模式</div>
        <div class="mode-cards">
          <div
            v-for="mode in consumeModes"
            :key="mode.code"
            class="mode-card"
            :class="{ active: selectedMode === mode.code }"
            @click="selectMode(mode)"
          >
            <div class="mode-icon">
              <van-icon :name="mode.icon" size="24" />
            </div>
            <div class="mode-info">
              <div class="mode-name">{{ mode.name }}</div>
              <div class="mode-desc">{{ mode.description }}</div>
            </div>
            <div class="mode-arrow" v-if="selectedMode === mode.code">
              <van-icon name="arrow" />
            </div>
          </div>
        </div>
      </div>

      <!-- 消费表单区域 -->
      <div class="consume-form-section" v-if="selectedMode">
        <div class="section-title">{{ getFormTitle() }}</div>

        <!-- 固定金额消费 -->
        <div v-if="selectedMode === 'FIXED_AMOUNT'" class="form-content">
          <div class="amount-selector">
            <div class="preset-amounts">
              <van-button
                v-for="amount in presetAmounts"
                :key="amount"
                size="small"
                round
                :type="selectedAmount === amount ? 'primary' : 'default'"
                @click="selectedAmount = amount"
              >
                ¥{{ amount }}
              </van-button>
            </div>
            <van-field
              v-model="customAmount"
              label="自定义金额"
              placeholder="输入消费金额"
              type="number"
            />
          </div>
        </div>

        <!-- 自由金额消费 -->
        <div v-if="selectedMode === 'FREE_AMOUNT'" class="form-content">
          <van-field
            v-model="freeAmount"
            label="消费金额"
            placeholder="请输入消费金额"
            type="number"
            :rules="[{ required: true, message: '请输入消费金额' }]"
          />
          <van-field
            v-model="consumeRemark"
            label="消费备注"
            placeholder="请输入消费备注"
            type="textarea"
            rows="2"
          />
        </div>

        <!-- 产品扫码消费 -->
        <div v-if="selectedMode === 'PRODUCT_SCAN'" class="form-content">
          <div class="scan-section">
            <van-button
              type="primary"
              block
              icon="scan"
              @click="startScan"
            >
              扫描产品条码
            </van-button>
            <div v-if="scannedProduct" class="scanned-product">
              <div class="product-info">
                <div class="product-name">{{ scannedProduct.name }}</div>
                <div class="product-price">¥{{ scannedProduct.price }}</div>
              </div>
              <van-stepper
                v-model="productQuantity"
                :min="1"
                :max="scannedProduct.stock"
              />
            </div>
          </div>
        </div>

        <!-- 点餐消费 -->
        <div v-if="selectedMode === 'ORDERING'" class="form-content">
          <div class="menu-section">
            <van-tabs v-model:active="activeCategory" @change="onCategoryChange">
              <van-tab
                v-for="category in menuCategories"
                :key="category.id"
                :title="category.name"
              />
            </van-tabs>

            <div class="menu-items">
              <div
                v-for="item in menuItems"
                :key="item.id"
                class="menu-item"
                @click="addToCart(item)"
              >
                <img :src="item.image" class="item-image" />
                <div class="item-info">
                  <div class="item-name">{{ item.name }}</div>
                  <div class="item-desc">{{ item.description }}</div>
                  <div class="item-price">¥{{ item.price }}</div>
                </div>
                <van-icon name="plus" class="add-icon" />
              </div>
            </div>
          </div>

          <!-- 购物车 -->
          <div class="cart-section" v-if="cartItems.length > 0">
            <div class="cart-header">
              <span>购物车 ({{ cartItems.length }})</span>
              <van-button size="mini" @click="clearCart">清空</van-button>
            </div>
            <div class="cart-items">
              <div
                v-for="item in cartItems"
                :key="item.id"
                class="cart-item"
              >
                <span class="item-name">{{ item.name }}</span>
                <div class="item-right">
                  <van-stepper
                    v-model="item.quantity"
                    :min="1"
                    size="small"
                    @change="updateCartItem(item)"
                  />
                  <span class="item-total">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                </div>
              </div>
            </div>
            <div class="cart-total">
              总计: ¥{{ cartTotal.toFixed(2) }}
            </div>
          </div>
        </div>

        <!-- 支付方式选择 -->
        <div class="payment-section">
          <div class="section-title">选择支付方式</div>
          <van-radio-group v-model="selectedPayment">
            <van-cell-group>
              <van-cell
                v-for="payment in paymentMethods"
                :key="payment.code"
                :title="payment.name"
                :label="payment.description"
                clickable
                @click="selectedPayment = payment.code"
              >
                <template #right-icon>
                  <van-radio :name="payment.code" />
                </template>
                <template #icon>
                  <van-icon :name="payment.icon" />
                </template>
              </van-cell>
            </van-cell-group>
          </van-radio-group>
        </div>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <van-button
            type="primary"
            block
            size="large"
            :loading="submitting"
            @click="submitConsume"
          >
            {{ getSubmitButtonText() }}
          </van-button>
        </div>
      </div>

      <!-- 历史记录 -->
      <div class="history-section">
        <div class="section-title">
          <span>消费历史</span>
          <van-button size="mini" @click="viewAllHistory">查看全部</van-button>
        </div>
        <div class="history-list">
          <van-cell
            v-for="record in recentHistory"
            :key="record.id"
            :title="record.description"
            :label="formatTime(record.createTime)"
            :value="`¥${record.amount}`"
            is-link
            @click="viewHistoryDetail(record)"
          />
        </div>
      </div>
    </div>

    <!-- 搜索弹窗 -->
    <van-popup v-model:show="showSearch" position="top" :style="{ height: '60%' }">
      <div class="search-popup">
        <van-search
          v-model="searchKeyword"
          placeholder="搜索产品或服务"
          @search="onSearch"
          @cancel="showSearch = false"
        />
        <div class="search-results" v-if="searchResults.length > 0">
          <van-cell
            v-for="result in searchResults"
            :key="result.id"
            :title="result.name"
            :label="result.description"
            :value="`¥${result.price}`"
            @click="selectSearchResult(result)"
          />
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { consumeApi } from '@/api/business/consume'
import { productApi } from '@/api/business/product'
import { menuApi } from '@/api/business/menu'

// 响应式数据
const router = useRouter()
const showSearch = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const selectedMode = ref('')
const selectedAmount = ref('')
const customAmount = ref('')
const freeAmount = ref('')
const consumeRemark = ref('')
const selectedPayment = ref('WECHAT')
const submitting = ref(false)
const activeCategory = ref(0)

// 产品扫码相关
const scannedProduct = ref(null)
const productQuantity = ref(1)

// 点餐相关
const menuCategories = ref([])
const menuItems = ref([])
const cartItems = ref([])

// 快捷操作
const quickActions = ref([
  { id: 1, icon: 'scan', text: '扫码消费', action: 'scan' },
  { id: 2, icon: 'shopping-cart-o', text: '快速买单', action: 'quick_pay' },
  { id: 3, icon: 'balance-list-o', text: '账单查询', action: 'bills' },
  { id: 4, icon: 'chart-trending-o', text: '消费统计', action: 'stats' }
])

// 消费模式
const consumeModes = ref([
  {
    code: 'FIXED_AMOUNT',
    name: '固定金额',
    description: '选择预设金额进行消费',
    icon: 'currency-yen'
  },
  {
    code: 'FREE_AMOUNT',
    name: '自由金额',
    description: '自定义消费金额',
    icon: 'edit'
  },
  {
    code: 'PRODUCT_SCAN',
    name: '产品扫码',
    description: '扫描产品条码消费',
    icon: 'scan'
  },
  {
    code: 'ORDERING',
    name: '点餐消费',
    description: '选择菜单点餐消费',
    icon: 'food'
  }
])

// 预设金额
const presetAmounts = ref([5, 10, 20, 50, 100, 200])

// 支付方式
const paymentMethods = ref([
  {
    code: 'WECHAT',
    name: '微信支付',
    description: '使用微信余额或银行卡支付',
    icon: 'wechat'
  },
  {
    code: 'ALIPAY',
    name: '支付宝',
    description: '使用支付宝余额或花呗支付',
    icon: 'alipay'
  },
  {
    code: 'BALANCE',
    name: '余额支付',
    description: '使用账户余额支付',
    icon: 'balance-o'
  }
])

// 消费历史
const recentHistory = ref([])

// 计算属性
const cartTotal = computed(() => {
  return cartItems.value.reduce((total, item) => {
    return total + (item.price * item.quantity)
  }, 0)
})

// 生命周期
onMounted(() => {
  loadRecentHistory()
  loadMenuCategories()
})

// 方法
const goBack = () => {
  router.back()
}

const goToProfile = () => {
  router.push('/profile')
}

const handleQuickAction = (action) => {
  switch (action.action) {
    case 'scan':
      selectMode(consumeModes.value.find(m => m.code === 'PRODUCT_SCAN'))
      break
    case 'quick_pay':
      selectMode(consumeModes.value.find(m => m.code === 'FREE_AMOUNT'))
      break
    case 'bills':
      router.push('/consume/bills')
      break
    case 'stats':
      router.push('/consume/statistics')
      break
  }
}

const selectMode = (mode) => {
  selectedMode.value = mode.code

  // 根据模式执行特定操作
  if (mode.code === 'PRODUCT_SCAN') {
    // 准备扫码
  } else if (mode.code === 'ORDERING') {
    // 加载菜单数据
    loadMenuItems()
  }
}

const getFormTitle = () => {
  const mode = consumeModes.value.find(m => m.code === selectedMode.value)
  return mode ? mode.name : '消费'
}

const getSubmitButtonText = () => {
  const totalPrice = calculateTotalPrice()
  return `确认支付 ¥${totalPrice.toFixed(2)}`
}

const calculateTotalPrice = () => {
  switch (selectedMode.value) {
    case 'FIXED_AMOUNT':
      return parseFloat(selectedAmount.value || customAmount.value || 0)
    case 'FREE_AMOUNT':
      return parseFloat(freeAmount.value || 0)
    case 'PRODUCT_SCAN':
      return scannedProduct.value ? scannedProduct.value.price * productQuantity.value : 0
    case 'ORDERING':
      return cartTotal.value
    default:
      return 0
  }
}

const startScan = () => {
  // 调用扫码API或使用相机
  showToast('扫码功能开发中...')
}

const onCategoryChange = () => {
  loadMenuItems()
}

const loadMenuCategories = async () => {
  try {
    const response = await menuApi.getCategories()
    if (response.data) {
      menuCategories.value = response.data
    }
  } catch (error) {
    showToast('加载菜单分类失败')
  }
}

const loadMenuItems = async () => {
  try {
    const categoryId = menuCategories.value[activeCategory.value]?.id
    const response = await menuApi.getMenuItems({ categoryId })
    if (response.data) {
      menuItems.value = response.data
    }
  } catch (error) {
    showToast('加载菜单项失败')
  }
}

const addToCart = (item) => {
  const existingItem = cartItems.value.find(cartItem => cartItem.id === item.id)
  if (existingItem) {
    existingItem.quantity++
  } else {
    cartItems.value.push({
      ...item,
      quantity: 1
    })
  }
  showToast(`已添加 ${item.name}`)
}

const updateCartItem = (item) => {
  if (item.quantity <= 0) {
    cartItems.value = cartItems.value.filter(cartItem => cartItem.id !== item.id)
  }
}

const clearCart = () => {
  cartItems.value = []
  showToast('购物车已清空')
}

const onSearch = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }

  try {
    const response = await productApi.search(searchKeyword.value)
    if (response.data) {
      searchResults.value = response.data
    }
  } catch (error) {
    showToast('搜索失败')
  }
}

const selectSearchResult = (result) => {
  showSearch.value = false
  searchKeyword.value = ''
  searchResults.value = []
  // 处理搜索结果选择
}

const loadRecentHistory = async () => {
  try {
    const response = await consumeApi.getRecentHistory({ limit: 5 })
    if (response.data) {
      recentHistory.value = response.data
    }
  } catch (error) {
    console.error('加载消费历史失败', error)
  }
}

const viewAllHistory = () => {
  router.push('/consume/history')
}

const viewHistoryDetail = (record) => {
  router.push(`/consume/history/${record.id}`)
}

const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

const submitConsume = async () => {
  if (submitting.value) return

  const totalPrice = calculateTotalPrice()
  if (totalPrice <= 0) {
    showToast('请输入有效的消费金额')
    return
  }

  if (!selectedPayment.value) {
    showToast('请选择支付方式')
    return
  }

  submitting.value = true
  showLoadingToast({ message: '提交中...', forbidClick: true })

  try {
    const consumeData = buildConsumeData()
    const response = await consumeApi.create(consumeData)

    if (response.success) {
      closeToast()
      showToast('提交成功')

      // 如果是线上支付，跳转到支付页面
      if (selectedPayment.value !== 'BALANCE') {
        await processOnlinePayment(response.data)
      } else {
        // 余额支付成功
        resetForm()
        loadRecentHistory()
      }
    } else {
      showToast(response.message || '提交失败')
    }
  } catch (error) {
    showToast('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

const buildConsumeData = () => {
  const baseData = {
    consumeMode: selectedMode.value,
    amount: calculateTotalPrice(),
    paymentMethod: selectedPayment.value,
    remark: consumeRemark.value
  }

  switch (selectedMode.value) {
    case 'PRODUCT_SCAN':
      return {
        ...baseData,
        productId: scannedProduct.value.id,
        quantity: productQuantity.value
      }
    case 'ORDERING':
      return {
        ...baseData,
        items: cartItems.value.map(item => ({
          productId: item.id,
          quantity: item.quantity,
          price: item.price
        }))
      }
    default:
      return baseData
  }
}

const processOnlinePayment = async (consumeData) => {
  try {
    const paymentData = {
      consumeId: consumeData.id,
      amount: consumeData.amount,
      paymentMethod: selectedPayment.value
    }

    const paymentResponse = await consumeApi.createPayment(paymentData)
    if (paymentResponse.success) {
      // 跳转到支付页面或调用支付SDK
      router.push({
        path: '/consume/payment',
        query: {
          paymentId: paymentResponse.data.paymentId,
          type: selectedPayment.value
        }
      })
    }
  } catch (error) {
    showToast('支付初始化失败')
  }
}

const resetForm = () => {
  selectedMode.value = ''
  selectedAmount.value = ''
  customAmount.value = ''
  freeAmount.value = ''
  consumeRemark.value = ''
  scannedProduct.value = null
  productQuantity.value = 1
  cartItems.value = []
}
</script>

<style lang="less" scoped>
.consume-mobile-container {
  min-height: 100vh;
  background-color: #f7f8fa;

  .mobile-header {
    position: sticky;
    top: 0;
    z-index: 100;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;

    .header-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;

      .header-left {
        display: flex;
        align-items: center;
        gap: 12px;

        .header-title {
          font-size: 18px;
          font-weight: 600;
        }
      }

      .header-right {
        display: flex;
        gap: 16px;

        .van-icon {
          font-size: 20px;
        }
      }
    }
  }

  .main-content {
    padding: 16px;

    .quick-actions {
      margin-bottom: 20px;
    }

    .consume-mode-section {
      margin-bottom: 20px;

      .section-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 12px;
        color: #323233;
      }

      .mode-cards {
        .mode-card {
          background: white;
          border-radius: 12px;
          padding: 16px;
          margin-bottom: 12px;
          display: flex;
          align-items: center;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
          transition: all 0.3s ease;

          &.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
          }

          .mode-icon {
            margin-right: 12px;
          }

          .mode-info {
            flex: 1;

            .mode-name {
              font-size: 16px;
              font-weight: 600;
              margin-bottom: 4px;
            }

            .mode-desc {
              font-size: 12px;
              opacity: 0.8;
            }
          }

          .mode-arrow {
            margin-left: 8px;
          }
        }
      }
    }

    .consume-form-section {
      background: white;
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      .section-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 16px;
        color: #323233;
      }

      .form-content {
        margin-bottom: 20px;

        .amount-selector {
          .preset-amounts {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-bottom: 16px;

            .van-button {
              flex: 0 0 calc(33.33% - 6px);
            }
          }
        }

        .scan-section {
          text-align: center;

          .scanned-product {
            margin-top: 16px;
            padding: 12px;
            background: #f7f8fa;
            border-radius: 8px;
            display: flex;
            justify-content: space-between;
            align-items: center;

            .product-info {
              .product-name {
                font-weight: 600;
                margin-bottom: 4px;
              }

              .product-price {
                color: #f56c6c;
                font-size: 16px;
              }
            }
          }
        }

        .menu-section {
          .menu-items {
            max-height: 300px;
            overflow-y: auto;

            .menu-item {
              display: flex;
              padding: 12px 0;
              border-bottom: 1px solid #ebedf0;

              .item-image {
                width: 60px;
                height: 60px;
                border-radius: 8px;
                margin-right: 12px;
                object-fit: cover;
              }

              .item-info {
                flex: 1;

                .item-name {
                  font-weight: 600;
                  margin-bottom: 4px;
                }

                .item-desc {
                  font-size: 12px;
                  color: #969799;
                  margin-bottom: 8px;
                }

                .item-price {
                  color: #f56c6c;
                  font-weight: 600;
                }
              }

              .add-icon {
                color: #1989fa;
                font-size: 20px;
                align-self: center;
              }
            }
          }
        }

        .cart-section {
          margin-top: 16px;
          padding: 12px;
          background: #f7f8fa;
          border-radius: 8px;

          .cart-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
          }

          .cart-items {
            .cart-item {
              display: flex;
              justify-content: space-between;
              align-items: center;
              padding: 8px 0;

              .item-right {
                display: flex;
                align-items: center;
                gap: 12px;

                .item-total {
                  font-weight: 600;
                  color: #f56c6c;
                }
              }
            }
          }

          .cart-total {
            text-align: right;
            font-size: 16px;
            font-weight: 600;
            color: #f56c6c;
            margin-top: 12px;
            padding-top: 12px;
            border-top: 1px solid #ebedf0;
          }
        }
      }

      .payment-section {
        margin-bottom: 20px;
      }

      .submit-section {
        .van-button {
          height: 50px;
          font-size: 16px;
          font-weight: 600;
        }
      }
    }

    .history-section {
      background: white;
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      .section-title {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        span {
          font-size: 16px;
          font-weight: 600;
          color: #323233;
        }
      }
    }
  }

  .search-popup {
    padding: 16px;

    .van-search {
      margin-bottom: 16px;
    }

    .search-results {
      max-height: 400px;
      overflow-y: auto;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .consume-mobile-container {
    .main-content {
      padding: 12px;
    }
  }
}

@media (min-width: 769px) {
  .consume-mobile-container {
    max-width: 500px;
    margin: 0 auto;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  }
}
</style>