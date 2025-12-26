/**
 * 离线缓存管理
 * 缓存告警数据和设备状态
 */

const CACHE_KEYS = {
  ALARMS: 'offline_alarms',
  DEVICES: 'offline_devices',
  DEVICE_STATUS: 'offline_device_status',
  USER_FAVORITES: 'user_favorites',
  CACHE_TIMESTAMP: 'cache_timestamp'
};

/**
 * 缓存告警列表
 * @param {Array} alarms 告警列表
 */
export async function cacheAlarms(alarms) {
  try {
    await uni.setStorage({
      key: CACHE_KEYS.ALARMS,
      data: JSON.stringify(alarms)
    });

    // 更新缓存时间戳
    await updateCacheTimestamp(CACHE_KEYS.ALARMS);

    console.log('[OfflineCache] 缓存告警成功:', alarms.length, '条');
  } catch (error) {
    console.error('[OfflineCache] 缓存告警失败:', error);
  }
}

/**
 * 获取缓存的告警
 * @param {number} limit 限制数量，默认全部
 * @returns {Promise<Array>} 告警列表
 */
export async function getCachedAlarms(limit = null) {
  try {
    const res = await uni.getStorage({ key: CACHE_KEYS.ALARMS });
    if (!res.data) {
      return [];
    }

    let alarms = JSON.parse(res.data);

    // 如果限制了数量，只返回最新的N条
    if (limit && limit < alarms.length) {
      alarms = alarms.slice(0, limit);
    }

    console.log('[OfflineCache] 获取缓存告警:', alarms.length, '条');
    return alarms;
  } catch (error) {
    console.error('[OfflineCache] 获取缓存告警失败:', error);
    return [];
  }
}

/**
 * 添加单条告警到缓存
 * @param {Object} alarm 告警对象
 */
export async function addCachedAlarm(alarm) {
  try {
    const alarms = await getCachedAlarms();

    // 添加到最前面
    alarms.unshift(alarm);

    // 只保留最近1000条
    if (alarms.length > 1000) {
      alarms.length = 1000;
    }

    await cacheAlarms(alarms);
  } catch (error) {
    console.error('[OfflineCache] 添加缓存告警失败:', error);
  }
}

/**
 * 清除告警缓存
 */
export async function clearCachedAlarms() {
  try {
    await uni.removeStorage({ key: CACHE_KEYS.ALARMS });
    console.log('[OfflineCache] 清除告警缓存成功');
  } catch (error) {
    console.error('[OfflineCache] 清除告警缓存失败:', error);
  }
}

/**
 * 缓存设备状态
 * @param {Array} devices 设备列表
 */
export async function cacheDeviceStatus(devices) {
  try {
    await uni.setStorage({
      key: CACHE_KEYS.DEVICE_STATUS,
      data: JSON.stringify(devices)
    });

    await updateCacheTimestamp(CACHE_KEYS.DEVICE_STATUS);

    console.log('[OfflineCache] 缓存设备状态成功:', devices.length, '个');
  } catch (error) {
    console.error('[OfflineCache] 缓存设备状态失败:', error);
  }
}

/**
 * 获取缓存的设备状态
 * @returns {Promise<Array>} 设备状态列表
 */
export async function getCachedDeviceStatus() {
  try {
    const res = await uni.getStorage({ key: CACHE_KEYS.DEVICE_STATUS });
    if (!res.data) {
      return [];
    }

    const devices = JSON.parse(res.data);
    console.log('[OfflineCache] 获取缓存设备状态:', devices.length, '个');
    return devices;
  } catch (error) {
    console.error('[OfflineCache] 获取缓存设备状态失败:', error);
    return [];
  }
}

/**
 * 缓存设备列表
 * @param {Array} devices 设备列表
 */
export async function cacheDevices(devices) {
  try {
    await uni.setStorage({
      key: CACHE_KEYS.DEVICES,
      data: JSON.stringify(devices)
    });

    await updateCacheTimestamp(CACHE_KEYS.DEVICES);

    console.log('[OfflineCache] 缓存设备列表成功:', devices.length, '个');
  } catch (error) {
    console.error('[OfflineCache] 缓存设备列表失败:', error);
  }
}

/**
 * 获取缓存的设备列表
 * @returns {Promise<Array>} 设备列表
 */
export async function getCachedDevices() {
  try {
    const res = await uni.getStorage({ key: CACHE_KEYS.DEVICES });
    if (!res.data) {
      return [];
    }

    const devices = JSON.parse(res.data);
    console.log('[OfflineCache] 获取缓存设备:', devices.length, '个');
    return devices;
  } catch (error) {
    console.error('[OfflineCache] 获取缓存设备失败:', error);
    return [];
  }
}

/**
 * 添加用户收藏
 * @param {number} deviceId 设备ID
 */
export async function addUserFavorite(deviceId) {
  try {
    const favorites = await getUserFavorites();

    if (!favorites.includes(deviceId)) {
      favorites.push(deviceId);
      await uni.setStorage({
        key: CACHE_KEYS.USER_FAVORITES,
        data: JSON.stringify(favorites)
      });

      console.log('[OfflineCache] 添加收藏:', deviceId);
    }
  } catch (error) {
    console.error('[OfflineCache] 添加收藏失败:', error);
  }
}

/**
 * 移除用户收藏
 * @param {number} deviceId 设备ID
 */
export async function removeUserFavorite(deviceId) {
  try {
    const favorites = await getUserFavorites();
    const index = favorites.indexOf(deviceId);

    if (index > -1) {
      favorites.splice(index, 1);
      await uni.setStorage({
        key: CACHE_KEYS.USER_FAVORITES,
        data: JSON.stringify(favorites)
      });

      console.log('[OfflineCache] 移除收藏:', deviceId);
    }
  } catch (error) {
    console.error('[OfflineCache] 移除收藏失败:', error);
  }
}

/**
 * 获取用户收藏列表
 * @returns {Promise<Array>} 设备ID列表
 */
export async function getUserFavorites() {
  try {
    const res = await uni.getStorage({ key: CACHE_KEYS.USER_FAVORITES });
    if (!res.data) {
      return [];
    }

    return JSON.parse(res.data);
  } catch (error) {
    console.error('[OfflineCache] 获取收藏失败:', error);
    return [];
  }
}

/**
 * 检查设备是否已收藏
 * @param {number} deviceId 设备ID
 * @returns {Promise<boolean>} 是否已收藏
 */
export async function isDeviceFavorited(deviceId) {
  const favorites = await getUserFavorites();
  return favorites.includes(deviceId);
}

/**
 * 更新缓存时间戳
 * @param {string} key 缓存键
 */
async function updateCacheTimestamp(key) {
  try {
    const timestamps = await getCacheTimestamps();
    timestamps[key] = Date.now();

    await uni.setStorage({
      key: CACHE_KEYS.CACHE_TIMESTAMP,
      data: JSON.stringify(timestamps)
    });
  } catch (error) {
    console.error('[OfflineCache] 更新时间戳失败:', error);
  }
}

/**
 * 获取缓存时间戳
 * @returns {Promise<Object>} 时间戳对象
 */
export async function getCacheTimestamps() {
  try {
    const res = await uni.getStorage({ key: CACHE_KEYS.CACHE_TIMESTAMP });
    if (!res.data) {
      return {};
    }

    return JSON.parse(res.data);
  } catch (error) {
    console.error('[OfflineCache] 获取时间戳失败:', error);
    return {};
  }
}

/**
 * 检查缓存是否过期
 * @param {string} key 缓存键
 * @param {number} maxAge 最大缓存时间（毫秒），默认1小时
 * @returns {Promise<boolean>} 是否过期
 */
export async function isCacheExpired(key, maxAge = 3600000) {
  try {
    const timestamps = await getCacheTimestamps();
    const timestamp = timestamps[key];

    if (!timestamp) {
      return true; // 无时间戳，认为过期
    }

    const now = Date.now();
    return (now - timestamp) > maxAge;
  } catch (error) {
    console.error('[OfflineCache] 检查缓存过期失败:', error);
    return true;
  }
}

/**
 * 清除所有缓存
 */
export async function clearAllCache() {
  try {
    await uni.removeStorage({ key: CACHE_KEYS.ALARMS });
    await uni.removeStorage({ key: CACHE_KEYS.DEVICES });
    await uni.removeStorage({ key: CACHE_KEYS.DEVICE_STATUS });
    await uni.removeStorage({ key: CACHE_KEYS.CACHE_TIMESTAMP });

    console.log('[OfflineCache] 清除所有缓存成功');
  } catch (error) {
    console.error('[OfflineCache] 清除所有缓存失败:', error);
  }
}

/**
 * 获取缓存大小信息
 * @returns {Promise<Object>} 缓存大小信息
 */
export async function getCacheInfo() {
  try {
    const info = await uni.getStorageInfo();

    return {
      currentSize: info.currentSize,
      limitSize: info.limitSize,
      usagePercent: ((info.currentSize / info.limitSize) * 100).toFixed(2)
    };
  } catch (error) {
    console.error('[OfflineCache] 获取缓存信息失败:', error);
    return {
      currentSize: 0,
      limitSize: 0,
      usagePercent: '0'
    };
  }
}

/**
 * 清理过期缓存
 * @param {number} maxAge 最大缓存时间（毫秒），默认24小时
 */
export async function cleanExpiredCache(maxAge = 86400000) {
  try {
    const timestamps = await getCacheTimestamps();
    const now = Date.now();

    for (const key in timestamps) {
      if ((now - timestamps[key]) > maxAge) {
        await uni.removeStorage({ key });
        delete timestamps[key];
      }
    }

    // 更新剩余的时间戳
    await uni.setStorage({
      key: CACHE_KEYS.CACHE_TIMESTAMP,
      data: JSON.stringify(timestamps)
    });

    console.log('[OfflineCache] 清理过期缓存完成');
  } catch (error) {
    console.error('[OfflineCache] 清理过期缓存失败:', error);
  }
}

export default {
  // 告警缓存
  cacheAlarms,
  getCachedAlarms,
  addCachedAlarm,
  clearCachedAlarms,

  // 设备缓存
  cacheDeviceStatus,
  getCachedDeviceStatus,
  cacheDevices,
  getCachedDevices,

  // 用户收藏
  addUserFavorite,
  removeUserFavorite,
  getUserFavorites,
  isDeviceFavorited,

  // 缓存管理
  getCacheTimestamps,
  isCacheExpired,
  clearAllCache,
  getCacheInfo,
  cleanExpiredCache
};
