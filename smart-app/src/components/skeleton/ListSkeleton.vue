<template>
  <view class="list-skeleton">
    <view
      v-for="i in count"
      :key="i"
      class="skeleton-item"
    >
      <view class="skeleton-avatar skeleton-loading"></view>
      <view class="skeleton-content">
        <view class="skeleton-title skeleton-loading"></view>
        <view class="skeleton-subtitle skeleton-loading"></view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'ListSkeleton',
  props: {
    count: {
      type: Number,
      default: 5
    }
  }
}
</script>

<style lang="scss" scoped>
.list-skeleton {
  padding: 0 32rpx;
}

.skeleton-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1px solid #f0f0f0;
  
  .skeleton-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    margin-right: 24rpx;
  }
  
  .skeleton-content {
    flex: 1;
    
    .skeleton-title {
      width: 60%;
      height: 32rpx;
      border-radius: 4rpx;
      margin-bottom: 16rpx;
    }
    
    .skeleton-subtitle {
      width: 40%;
      height: 24rpx;
      border-radius: 4rpx;
    }
  }
}

.skeleton-loading {
  background: linear-gradient(
    90deg,
    #f0f0f0 25%,
    #e0e0e0 50%,
    #f0f0f0 75%
  );
  background-size: 200% 100%;
  animation: loading 1.5s ease-in-out infinite;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>

