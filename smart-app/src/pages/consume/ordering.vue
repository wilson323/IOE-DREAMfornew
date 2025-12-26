<template>
  <view class="ordering-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">在线订餐</view>
      <view class="nav-right" @click="viewCart">
        <text class="cart-icon">🛒</text>
        <view class="cart-badge" v-if="cartCount > 0">
          <text class="badge-text">{{ cartCount }}</text>
        </view>
      </view>
    </view>

    <!-- 订餐日期和餐别选择 -->
    <view class="filter-section">
      <scroll-view class="date-scroll" scroll-x>
        <view
          class="date-item"
          :class="{ selected: selectedDate === date.fullDate }"
          v-for="date in dateOptions"
          :key="date.fullDate"
          @click="selectDate(date.fullDate)"
        >
          <text class="date-weekday">{{ date.weekday }}</text>
          <text class="date-day">{{ date.day }}</text>
        </view>
      </scroll-view>

      <scroll-view class="meal-type-scroll" scroll-x>
        <view
          class="meal-type-item"
          :class="{ selected: selectedMealType === type.code }"
          v-for="type in mealTypeOptions"
          :key="type.code"
          @click="selectMealType(type.code)"
        >
          <text class="meal-type-text">{{ type.name }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 菜品分类 -->
    <view class="category-section">
      <scroll-view class="category-scroll" scroll-x>
        <view
          class="category-item"
          :class="{ selected: selectedCategory === null }"
          @click="selectCategory(null)"
        >
          <text class="category-text">全部</text>
        </view>
        <view
          class="category-item"
          :class="{ selected: selectedCategory === category.id }"
          v-for="category in categories"
          :key="category.id"
          @click="selectCategory(category.id)"
        >
          <text class="category-text">{{ category.name }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 菜品列表 -->
    <view class="dish-list">
      <view
        class="dish-item"
        :class="{ 'pressed': pressedDishIndex === index }"
        v-for="(dish, index) in dishList"
        :key="index"
        @click="handleDishClick(dish, index)"
        @touchstart="pressedDishIndex = index"
        @touchend="pressedDishIndex = -1"
        @touchcancel="pressedDishIndex = -1"
      >
        <view class="dish-image">
          <image :src="dish.dishImage || defaultDishImage" mode="aspectFill" class="image"></image>
          <view class="stock-badge" :class="{ soldout: dish.availableQuantity <= 0 }">
            <text class="stock-text">{{ dish.availableQuantity > 0 ? `余${dish.availableQuantity}` : '售罄' }}</text>
          </view>
        </view>

        <view class="dish-info">
          <text class="dish-name">{{ dish.dishName }}</text>
          <text class="dish-description">{{ dish.description || '暂无描述' }}</text>
          <view class="dish-tags">
            <text class="tag" v-for="tag in dish.dishTags" :key="tag">{{ tag }}</text>
          </view>
          <view class="dish-bottom">
            <view class="dish-price">
              <text class="price">¥{{ formatAmount(dish.price) }}</text>
              <text class="original-price" v-if="dish.originalPrice && dish.originalPrice > dish.price">
                ¥{{ formatAmount(dish.originalPrice) }}
              </text>
            </view>
            <view class="add-to-cart" @click.stop="addToCart(dish)">
              <text class="add-icon">+</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="no-data" v-if="dishList.length === 0 && !loadingDishes">
        <view class="no-data-icon-wrapper">
          <text class="no-data-icon">🍽️</text>
        </view>
        <text class="no-data-text">暂无可订菜品</text>
        <text class="no-data-hint">试试其他日期或餐别吧</text>
        <button class="refresh-btn" @click="loadDishes">
          <text class="refresh-icon">🔄</text>
          <text>刷新菜品</text>
        </button>
      </view>

      <!-- 加载状态 - 骨架屏 -->
      <view class="skeleton-container" v-if="loadingDishes">
        <view class="skeleton-item" v-for="i in 4" :key="i">
          <view class="skeleton-image"></view>
          <view class="skeleton-info">
            <view class="skeleton-title"></view>
            <view class="skeleton-text"></view>
            <view class="skeleton-text short"></view>
            <view class="skeleton-price"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 购物车悬浮按钮 -->
    <view class="cart-float" @click="showCartModal = true" v-if="cartCount > 0">
      <view class="cart-icon-large">🛒</view>
      <view class="cart-count">{{ cartCount }}</view>
      <view class="cart-total">
        <text class="total-text">¥{{ formatAmount(cartTotal) }}</text>
      </view>
    </view>

    <!-- 购物车弹窗 -->
    <view class="modal cart-modal" v-if="showCartModal" @click="showCartModal = false">
      <view class="modal-content cart-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">已选菜品</text>
          <text class="close-btn" @click="showCartModal = false">×</text>
        </view>

        <view class="cart-items">
          <view
            class="cart-item"
            v-for="(item, index) in cartItems"
            :key="index"
          >
            <view class="item-info">
              <text class="item-name">{{ item.dishName }}</text>
              <text class="item-price">¥{{ formatAmount(item.price) }}</text>
            </view>
            <view class="item-actions">
              <view class="quantity-control">
                <text class="control-btn" @click="decreaseQuantity(index)">-</text>
                <text class="quantity">{{ item.quantity }}</text>
                <text class="control-btn" @click="increaseQuantity(index)">+</text>
              </view>
              <text class="remove-btn" @click="removeFromCart(index)">🗑️</text>
            </view>
          </view>

          <!-- 空购物车 -->
          <view class="empty-cart" v-if="cartItems.length === 0">
            <text class="empty-icon">🛒</text>
            <text class="empty-text">购物车是空的</text>
          </view>
        </view>

        <view class="cart-summary" v-if="cartItems.length > 0">
          <view class="summary-row">
            <text class="summary-label">菜品金额</text>
            <text class="summary-value">¥{{ formatAmount(cartSubtotal) }}</text>
          </view>
          <view class="summary-row" v-if="discount > 0">
            <text class="summary-label">优惠金额</text>
            <text class="summary-value discount">-¥{{ formatAmount(discount) }}</text>
          </view>
          <view class="summary-row total">
            <text class="summary-label">合计</text>
            <text class="summary-value">¥{{ formatAmount(cartTotal) }}</text>
          </view>
        </view>

        <view class="cart-actions" v-if="cartItems.length > 0">
          <button class="submit-btn" @click="submitOrder">
            <text>提交订单</text>
          </button>
        </view>
      </view>
    </view>

    <!-- 订餐记录标签页 -->
    <view class="tab-container" v-if="showOrderTab">
      <view
        class="tab-item"
        :class="{ active: orderTab === 'list' }"
        @click="switchOrderTab('list')"
      >
        <text class="tab-text">订餐记录</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: orderTab === 'orders' }"
        @click="switchOrderTab('orders')"
      >
        <text class="tab-text">我的订单</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import { orderingApi } from '@/api/business/consume/ordering-api.js'
import cacheManager from '@/utils/cache-manager.js'

// 响应式数据
const userStore = useUserStore()
const selectedDate = ref('')
const selectedMealType = ref('LUNCH')
const selectedCategory = ref(null)

const dishList = ref([])
const categories = ref([])
const loadingDishes = ref(false)

const cartItems = ref([])
const cartCount = ref(0)
const cartTotal = ref(0)
const cartSubtotal = ref(0)
const discount = ref(0)

const showCartModal = ref(false)
const showOrderTab = ref(false)
const orderTab = ref('list')

const defaultDishImage = 'https://via.placeholder.com/200'

// 触摸反馈
const pressedDishIndex = ref(-1)

// 日期选项（当前日期+未来6天）
const dateOptions = ref([])
const mealTypeOptions = [
  { code: 'BREAKFAST', name: '早餐' },
  { code: 'LUNCH', name: '午餐' },
  { code: 'DINNER', name: '晚餐' }
]

// 初始化日期选项
const initDateOptions = () => {
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const today = new Date()
  const options = []

  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)

    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const weekday = i === 0 ? '今天' : weekdays[date.getDay()]

    options.push({
      fullDate: `${year}-${month}-${day}`,
      weekday: weekday,
      day: `${month}-${day}`
    })
  }

  dateOptions.value = options
  selectedDate.value = options[0].fullDate
}

// 格式化金额
const formatAmount = (amount) => {
  if (!amount && amount !== 0) return '0.00'
  return Number(amount).toFixed(2)
}

// 选择日期
const selectDate = (date) => {
  selectedDate.value = date
  loadDishes()
}

// 选择餐别
const selectMealType = (type) => {
  selectedMealType.value = type
  loadDishes()
}

// 选择分类
const selectCategory = (categoryId) => {
  selectedCategory.value = categoryId
  loadDishes()
}

// 加载菜品列表
const loadDishes = async () => {
  loadingDishes.value = true
  try {
    const userId = userStore.employeeId
    const params = {
      userId: userId,
      orderDate: selectedDate.value,
      mealType: selectedMealType.value
    }

    if (selectedCategory.value !== null) {
      params.categoryId = selectedCategory.value
    }

    // 生成缓存key
    const cacheKey = `dishes_${selectedDate.value}_${selectedMealType.value}_${selectedCategory.value || 'all'}`

    // 先尝试从缓存获取
    const cachedData = cacheManager.getCache(cacheKey)
    if (cachedData) {
      console.log('[订餐] 使用缓存数据:', cacheKey)
      dishList.value = cachedData
      loadingDishes.value = false
      return
    }

    // 缓存未命中，请求API
    const result = await orderingApi.getDishes(params)
    if (result.success && result.data) {
      dishList.value = result.data
      // 缓存数据，有效期10分钟（600000ms）
      cacheManager.setCache(cacheKey, result.data, 600000)
      console.log('[订餐] 已缓存数据:', cacheKey)
    }
  } catch (error) {
    console.error('加载菜品列表失败:', error)
  } finally {
    loadingDishes.value = false
  }
}

// 加载菜品分类
const loadCategories = async () => {
  try {
    // 菜品分类缓存key
    const cacheKey = 'dish_categories_all'

    // 先尝试从缓存获取
    const cachedData = cacheManager.getCache(cacheKey)
    if (cachedData) {
      console.log('[订餐] 使用缓存分类数据')
      categories.value = cachedData
      return
    }

    // 缓存未命中，请求API
    const result = await orderingApi.getDishCategories()
    if (result.success && result.data) {
      categories.value = result.data
      // 缓存分类数据，有效期1小时（3600000ms）
      cacheManager.setCache(cacheKey, result.data, 3600000)
      console.log('[订餐] 已缓存分类数据')
    }
  } catch (error) {
    console.error('加载菜品分类失败:', error)
  }
}

// 添加到购物车
const addToCart = (dish) => {
  if (dish.availableQuantity <= 0) {
    uni.showToast({ title: '该菜品已售罄', icon: 'none' })
    return
  }

  const existingIndex = cartItems.value.findIndex(item => item.dishId === dish.dishId)

  if (existingIndex >= 0) {
    const item = cartItems.value[existingIndex]
    if (item.quantity < dish.availableQuantity) {
      item.quantity++
      uni.vibrateShort()
    } else {
      uni.showToast({ title: '超过库存数量', icon: 'none' })
    }
  } else {
    cartItems.value.push({
      dishId: dish.dishId,
      dishName: dish.dishName,
      price: dish.price,
      quantity: 1,
      availableQuantity: dish.availableQuantity
    })
    uni.vibrateShort()
  }

  updateCartSummary()
}

// 增加数量
const increaseQuantity = (index) => {
  const item = cartItems.value[index]
  if (item.quantity < item.availableQuantity) {
    item.quantity++
    updateCartSummary()
  } else {
    uni.showToast({ title: '超过库存数量', icon: 'none' })
  }
}

// 减少数量
const decreaseQuantity = (index) => {
  const item = cartItems.value[index]
  if (item.quantity > 1) {
    item.quantity--
    updateCartSummary()
  } else {
    removeFromCart(index)
  }
}

// 从购物车移除
const removeFromCart = (index) => {
  cartItems.value.splice(index, 1)
  updateCartSummary()
}

// 更新购物车汇总
const updateCartSummary = () => {
  cartCount.value = cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
  cartSubtotal.value = cartItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0)
  cartTotal.value = cartSubtotal.value - discount.value
}

// 提交订单
const submitOrder = async () => {
  if (cartItems.value.length === 0) {
    uni.showToast({ title: '请选择菜品', icon: 'none' })
    return
  }

  const dishIds = cartItems.value.map(item => item.dishId)

  try {
    const result = await orderingApi.createOrder({
      userId: userStore.employeeId,
      dishIds: dishIds,
      orderDate: selectedDate.value,
      mealType: selectedMealType.value,
      quantities: cartItems.value.map(item => item.quantity)
    })

    if (result.success) {
      uni.showToast({ title: '下单成功', icon: 'success' })

      // 清空购物车
      cartItems.value = []
      updateCartSummary()
      showCartModal.value = false

      // 跳转到订单详情
      setTimeout(() => {
        uni.navigateTo({
          url: `/pages/consume/order-detail?orderId=${result.data.orderId}`
        })
      }, 1500)
    }
  } catch (error) {
    console.error('提交订单失败:', error)
    uni.showToast({ title: '下单失败', icon: 'none' })
  }
}

// 查看菜品详情
const handleDishClick = (dish, index) => {
  // 触觉反馈
  uni.vibrateShort({ type: 'light' })

  // 延迟一小段时间以显示按压效果
  setTimeout(() => {
    viewDishDetail(dish)
  }, 100)
}

const viewDishDetail = (dish) => {
  const dishStr = encodeURIComponent(JSON.stringify(dish))
  uni.navigateTo({
    url: `/pages/consume/dish-detail?data=${dishStr}`
  })
}

// 查看购物车
const viewCart = () => {
  if (cartItems.value.length > 0) {
    showCartModal.value = true
  } else {
    uni.showToast({ title: '购物车是空的', icon: 'none' })
  }
}

// 切换订单标签
const switchOrderTab = (tab) => {
  orderTab.value = tab
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 页面生命周期
onMounted(() => {
  initDateOptions()
  loadCategories()
  loadDishes()
})
</script>

<style lang="scss" scoped>
.ordering-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 100px;
}

.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;

  .nav-left, .nav-right {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 600;
    color: #333;
  }

  .icon-back {
    font-size: 20px;
    color: #333;
  }

  .cart-icon {
    font-size: 20px;
    position: relative;
  }

  .cart-badge {
    position: absolute;
    top: -5px;
    right: -5px;
    min-width: 16px;
    height: 16px;
    background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;

    .badge-text {
      font-size: 10px;
      color: #fff;
      font-weight: 600;
    }
  }
}

.filter-section {
  background-color: #fff;
  margin-bottom: 10px;

  .date-scroll,
  .meal-type-scroll {
    white-space: nowrap;
    padding: 12px 15px;

    .date-item,
    .meal-type-item {
      display: inline-block;
      padding: 8px 16px;
      margin-right: 10px;
      background: #f5f5f5;
      border-radius: 20px;
      text-align: center;
      border: 1px solid #e8e8e8;
      transition: all 0.3s ease;

      &.selected {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-color: #667eea;

        .date-weekday,
        .date-day,
        .meal-type-text {
          color: #fff;
        }
      }

      .date-weekday {
        display: block;
        font-size: 11px;
        margin-bottom: 2px;
        color: #666;
      }

      .date-day {
        display: block;
        font-size: 14px;
        font-weight: 600;
        color: #333;
      }

      .meal-type-text {
        font-size: 14px;
        font-weight: 500;
        color: #666;
      }
    }
  }
}

.category-section {
  background-color: #fff;
  margin-bottom: 10px;
  padding: 12px 15px;

  .category-scroll {
    white-space: nowrap;

    .category-item {
      display: inline-block;
      padding: 8px 16px;
      margin-right: 10px;
      background: #f5f5f5;
      border-radius: 20px;
      border: 1px solid #e8e8e8;
      transition: all 0.3s ease;

      &.selected {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-color: #667eea;

        .category-text {
          color: #fff;
        }
      }

      .category-text {
        font-size: 13px;
        color: #666;
        font-weight: 500;
      }
    }
  }
}

.dish-list {
  padding: 15px;

  .dish-item {
    display: flex;
    background-color: #fff;
    border-radius: 12px;
    padding: 12px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.2s ease;
    cursor: pointer;

    &.pressed {
      transform: scale(0.98);
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
      background-color: #f8f8f8;
    }

    &:active {
      transform: scale(0.98);
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    }

    .dish-image {
      width: 120px;
      height: 120px;
      border-radius: 10px;
      overflow: hidden;
      position: relative;
      margin-right: 12px;

      .image {
        width: 100%;
        height: 100%;
      }

      .stock-badge {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 4px 0;
        text-align: center;

        &.soldout {
          background: rgba(0, 0, 0, 0.6);
        }

        background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);

        .stock-text {
          font-size: 11px;
          color: #fff;
          font-weight: 500;
        }
      }
    }

    .dish-info {
      flex: 1;
      display: flex;
      flex-direction: column;

      .dish-name {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin-bottom: 6px;
      }

      .dish-description {
        font-size: 12px;
        color: #999;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .dish-tags {
        margin-bottom: 8px;

        .tag {
          display: inline-block;
          padding: 2px 8px;
          margin-right: 6px;
          background: #f0f5ff;
          color: #1890ff;
          font-size: 10px;
          border-radius: 10px;
        }
      }

      .dish-bottom {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: auto;

        .dish-price {
          .price {
            font-size: 18px;
            font-weight: 700;
            color: #ff4d4f;
          }

          .original-price {
            font-size: 13px;
            color: #999;
            text-decoration: line-through;
            margin-left: 6px;
          }
        }

        .add-to-cart {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

          .add-icon {
            font-size: 20px;
            color: #fff;
            font-weight: 600;
            line-height: 1;
          }
        }
      }
    }
  }
}

.cart-float {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 30px;
  padding: 15px 30px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  z-index: 99;

  .cart-icon-large {
    font-size: 24px;
  }

  .cart-count {
    font-size: 18px;
    font-weight: 700;
    color: #fff;
  }

  .cart-total {
    padding-left: 10px;
    border-left: 1px solid rgba(255, 255, 255, 0.3);

    .total-text {
      font-size: 16px;
      font-weight: 600;
      color: #fff;
    }
  }
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  z-index: 999;
  display: flex;
  align-items: flex-end;

  .modal-content {
    width: 100%;
    background-color: #fff;
    border-radius: 20px 20px 0 0;
    max-height: 80vh;
    overflow-y: auto;

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid #f0f0f0;

      .modal-title {
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }

      .close-btn {
        font-size: 28px;
        color: #999;
        font-weight: 300;
      }
    }

    .cart-items {
      padding: 20px;
      max-height: 40vh;
      overflow-y: auto;

      .cart-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .item-info {
          flex: 1;

          .item-name {
            display: block;
            font-size: 15px;
            font-weight: 500;
            color: #333;
            margin-bottom: 6px;
          }

          .item-price {
            display: block;
            font-size: 14px;
            color: #ff4d4f;
            font-weight: 600;
          }
        }

        .item-actions {
          display: flex;
          align-items: center;
          gap: 15px;

          .quantity-control {
            display: flex;
            align-items: center;
            gap: 15px;
            background: #f5f5f5;
            border-radius: 15px;
            padding: 5px;

            .control-btn {
              width: 24px;
              height: 24px;
              line-height: 24px;
              text-align: center;
              background: #fff;
              border-radius: 50%;
              font-size: 16px;
              color: #666;
            }

            .quantity {
              font-size: 14px;
              font-weight: 600;
              color: #333;
              min-width: 20px;
              text-align: center;
            }
          }

          .remove-btn {
            font-size: 18px;
          }
        }
      }

      .empty-cart {
        text-align: center;
        padding: 60px 0;

        .empty-icon {
          display: block;
          font-size: 64px;
          margin-bottom: 15px;
        }

        .empty-text {
          display: block;
          font-size: 14px;
          color: #999;
        }
      }
    }

    .cart-summary {
      padding: 15px 20px;
      border-top: 1px solid #f0f0f0;

      .summary-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;

        &:last-child {
          margin-bottom: 0;
        }

        .summary-label {
          font-size: 14px;
          color: #666;
        }

        .summary-value {
          font-size: 14px;
          font-weight: 500;
          color: #333;

          &.discount {
            color: #52c41a;
          }
        }

        &.total {
          .summary-label {
            font-size: 16px;
            font-weight: 600;
            color: #333;
          }

          .summary-value {
            font-size: 18px;
            font-weight: 700;
            color: #ff4d4f;
          }
        }
      }
    }

    .cart-actions {
      padding: 15px 20px 20px;
      border-top: 1px solid #f0f0f0;

      .submit-btn {
        width: 100%;
        height: 50px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        border-radius: 25px;
        font-size: 16px;
        font-weight: 600;
        color: #fff;
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
      }
    }
  }
}

.no-data {
  text-align: center;
  padding: 80px 30px;

  .no-data-icon-wrapper {
    width: 120px;
    height: 120px;
    margin: 0 auto 30px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: pulse 2s ease-in-out infinite;
  }

  .no-data-icon {
    font-size: 48px;
  }

  .no-data-text {
    display: block;
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin-bottom: 10px;
  }

  .no-data-hint {
    display: block;
    font-size: 14px;
    color: #999;
    margin-bottom: 30px;
  }

  .refresh-btn {
    margin: 0 auto;
    padding: 12px 32px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 24px;
    border: none;
    display: flex;
    align-items: center;
    gap: 8px;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    transition: all 0.3s;

    &:active {
      transform: scale(0.95);
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
    }

    text {
      color: #fff;
      font-size: 15px;
      font-weight: 500;
    }

    .refresh-icon {
      font-size: 18px;
      animation: rotate 2s linear infinite;
    }
  }
}

// 骨架屏样式
.skeleton-container {
  padding: 15px;

  .skeleton-item {
    display: flex;
    background-color: #fff;
    border-radius: 12px;
    padding: 12px;
    margin-bottom: 12px;
    animation: skeleton-pulse 1.5s ease-in-out infinite;

    .skeleton-image {
      width: 120px;
      height: 120px;
      border-radius: 10px;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: skeleton-shimmer 1.5s infinite;
      margin-right: 12px;
    }

    .skeleton-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
      gap: 10px;

      .skeleton-title {
        width: 60%;
        height: 20px;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-shimmer 1.5s infinite;
        border-radius: 4px;
      }

      .skeleton-text {
        width: 80%;
        height: 14px;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-shimmer 1.5s infinite;
        border-radius: 4px;

        &.short {
          width: 40%;
        }
      }

      .skeleton-price {
        width: 30%;
        height: 24px;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-shimmer 1.5s infinite;
        border-radius: 4px;
        margin-top: 10px;
      }
    }
  }
}

// 动画定义
@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.8;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes skeleton-shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

@keyframes skeleton-pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}
</style>
