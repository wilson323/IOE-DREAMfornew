/**
 * 物流管理工具
 * 提供物品寄存、领取、逾期检查等功能
 */

const API_BASE_URL = '/api/v1/mobile/visitor/logistics';
const OVERDUE_DAYS = 7; // 逾期天数（天）

// ==================== 物流单号生成 ====================

/**
 * 生成物流单号
 * @returns {string} 物流单号
 */
export function generateLogisticsNo() {
  const timestamp = Date.now().toString();
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0');
  return `LG${timestamp}${random}`;
}

/**
 * 解析物流单号
 * @param {string} logisticsNo 物流单号
 * @returns {Object|null} 解析结果
 */
export function parseLogisticsNo(logisticsNo) {
  if (!logisticsNo || typeof logisticsNo !== 'string') {
    return null;
  }

  // 物流单号格式：LG{timestamp}{random}
  const match = logisticsNo.match(/^LG(\d{13})(\d{4})$/);
  if (!match) {
    return null;
  }

  const timestamp = parseInt(match[1]);
  const random = match[2];

  return {
    logisticsNo,
    timestamp,
    date: new Date(timestamp),
    random
  };
}

// ==================== 物品寄存 ====================

/**
 * 物品寄存
 * @param {Object} params 寄存参数
 * @param {number} params.registrationId 登记ID
 * @param {string} params.itemName 物品名称
 * @param {string} params.itemType 物品类型
 * @param {number} params.itemCount 物品数量
 * @param {string} params.itemDescription 物品描述（可选）
 * @param {string} params.itemPhoto 物品照片（可选）
 * @param {string} params.depositorName 寄存人姓名
 * @param {string} params.depositorPhone 寄存人电话
 * @returns {Promise<Object>} 寄存结果
 */
export async function depositItem(params) {
  const {
    registrationId,
    itemName,
    itemType,
    itemCount,
    itemDescription,
    itemPhoto,
    depositorName,
    depositorPhone
  } = params;

  try {
    console.log('[LogisticsManager] 开始物品寄存:', itemName);

    // 1. 生成物流单号
    const logisticsNo = generateLogisticsNo();

    // 2. 上传物品照片（如果提供）
    let itemPhotoUrl = '';
    if (itemPhoto) {
      const uploadResult = await uploadItemPhoto(itemPhoto);
      if (uploadResult.success) {
        itemPhotoUrl = uploadResult.url;
      } else {
        return {
          success: false,
          error: '物品照片上传失败',
          detail: uploadResult.error
        };
      }
    }

    // 3. 调用后端API
    const result = await callLogisticsAPI('/deposit', 'POST', {
      logisticsNo,
      registrationId,
      itemName,
      itemType,
      itemCount,
      itemDescription,
      itemPhotoUrl,
      depositorName,
      depositorPhone
    });

    if (result.success) {
      console.log('[LogisticsManager] 物品寄存成功:', logisticsNo);
      return {
        success: true,
        logisticsNo,
        itemId: result.data.itemId,
        depositTime: result.data.depositTime
      };
    } else {
      return {
        success: false,
        error: result.message || '物品寄存失败'
      };
    }
  } catch (error) {
    console.error('[LogisticsManager] 物品寄存异常:', error);
    return {
      success: false,
      error: error.message || '物品寄存异常'
    };
  }
}

/**
 * 批量物品寄存
 * @param {number} registrationId 登记ID
 * @param {Array} items 物品列表
 * @param {string} depositorName 寄存人姓名
 * @param {string} depositorPhone 寄存人电话
 * @returns {Promise<Object>} 批量寄存结果
 */
export async function batchDepositItems(registrationId, items, depositorName, depositorPhone) {
  try {
    console.log('[LogisticsManager] 批量物品寄存:', items.length);

    const results = [];
    let successCount = 0;
    let failCount = 0;

    for (const item of items) {
      const result = await depositItem({
        registrationId,
        itemName: item.itemName,
        itemType: item.itemType,
        itemCount: item.itemCount,
        itemDescription: item.itemDescription,
        itemPhoto: item.itemPhoto,
        depositorName,
        depositorPhone
      });

      results.push({
        itemName: item.itemName,
        result
      });

      if (result.success) {
        successCount++;
      } else {
        failCount++;
      }
    }

    console.log('[LogisticsManager] 批量寄存完成:', { successCount, failCount });

    return {
      success: failCount === 0,
      successCount,
      failCount,
      results
    };
  } catch (error) {
    console.error('[LogisticsManager] 批量寄存异常:', error);
    return {
      success: false,
      error: error.message || '批量寄存异常'
    };
  }
}

// ==================== 物品领取 ====================

/**
 * 物品领取
 * @param {Object} params 领取参数
 * @param {string} params.logisticsNo 物流单号
 * @param {string} params.pickupPersonName 领取人姓名
 * @param {string} params.pickupPersonPhone 领取人电话
 * @param {string} params.pickupPersonIdCard 领取人身份证（可选）
 * @param {string} params.signature 签名（可选）
 * @returns {Promise<Object>} 领取结果
 */
export async function pickupItem(params) {
  const {
    logisticsNo,
    pickupPersonName,
    pickupPersonPhone,
    pickupPersonIdCard,
    signature
  } = params;

  try {
    console.log('[LogisticsManager] 开始物品领取:', logisticsNo);

    // 1. 验证物流单号
    const itemInfo = await getItemInfo(logisticsNo);
    if (!itemInfo) {
      return {
        success: false,
        error: '物流单号不存在'
      };
    }

    // 2. 检查物品状态
    if (itemInfo.itemStatus !== 'DEPOSITED') {
      return {
        success: false,
        error: `物品已${itemInfo.itemStatus === 'PICKED_UP' ? '被领取' : '转移'}`
      };
    }

    // 3. 验证领取人信息
    if (!pickupPersonName || !pickupPersonPhone) {
      return {
        success: false,
        error: '请填写完整的领取人信息'
      };
    }

    // 4. 调用后端API
    const result = await callLogisticsAPI('/pickup', 'POST', {
      logisticsNo,
      pickupPersonName,
      pickupPersonPhone,
      pickupPersonIdCard,
      signature
    });

    if (result.success) {
      console.log('[LogisticsManager] 物品领取成功:', logisticsNo);
      return {
        success: true,
        pickupTime: result.data.pickupTime
      };
    } else {
      return {
        success: false,
        error: result.message || '物品领取失败'
      };
    }
  } catch (error) {
    console.error('[LogisticsManager] 物品领取异常:', error);
    return {
      success: false,
      error: error.message || '物品领取异常'
    };
  }
}

// ==================== 物品查询 ====================

/**
 * 根据物流单号查询物品信息
 * @param {string} logisticsNo 物流单号
 * @returns {Promise<Object|null>} 物品信息
 */
export async function getItemInfo(logisticsNo) {
  try {
    const result = await callLogisticsAPI(`/item/${logisticsNo}`, 'GET');
    if (result.success) {
      return result.data;
    }
    return null;
  } catch (error) {
    console.error('[LogisticsManager] 查询物品信息失败:', error);
    return null;
  }
}

/**
 * 根据登记ID查询所有物品
 * @param {number} registrationId 登记ID
 * @returns {Promise<Array>} 物品列表
 */
export async function getItemsByRegistration(registrationId) {
  try {
    const result = await callLogisticsAPI(`/items/${registrationId}`, 'GET');
    if (result.success) {
      return result.data || [];
    }
    return [];
  } catch (error) {
    console.error('[LogisticsManager] 查询登记物品失败:', error);
    return [];
  }
}

/**
 * 查询逾期物品
 * @param {number} overdueDays 逾期天数（默认7天）
 * @returns {Promise<Array>} 逾期物品列表
 */
export async function getOverdueItems(overdueDays = OVERDUE_DAYS) {
  try {
    const result = await callLogisticsAPI('/overdue', 'GET', { overdueDays });
    if (result.success) {
      return result.data || [];
    }
    return [];
  } catch (error) {
    console.error('[LogisticsManager] 查询逾期物品失败:', error);
    return [];
  }
}

/**
 * 检查物品是否逾期
 * @param {string} logisticsNo 物流单号
 * @param {number} overdueDays 逾期天数（默认7天）
 * @returns {Promise<Object>} 检查结果
 */
export async function checkItemOverdue(logisticsNo, overdueDays = OVERDUE_DAYS) {
  try {
    const itemInfo = await getItemInfo(logisticsNo);
    if (!itemInfo) {
      return {
        overdue: false,
        error: '物流单号不存在'
      };
    }

    // 已领取不算逾期
    if (itemInfo.itemStatus === 'PICKED_UP') {
      return {
        overdue: false,
        status: 'PICKED_UP'
      };
    }

    // 计算逾期天数
    const depositTime = new Date(itemInfo.depositTime);
    const now = new Date();
    const diffDays = Math.floor((now - depositTime) / (1000 * 60 * 60 * 24));

    const isOverdue = diffDays > overdueDays;

    return {
      overdue: isOverdue,
      diffDays,
      depositTime: itemInfo.depositTime
    };
  } catch (error) {
    console.error('[LogisticsManager] 检查物品逾期失败:', error);
    return {
      overdue: false,
      error: error.message
    };
  }
}

// ==================== 物品转移 ====================

/**
 * 转移逾期物品到保安处
 * @param {string} logisticsNo 物流单号
 * @param {string} handlerName 处理人姓名
 * @param {string} handlerRemark 处理备注（可选）
 * @returns {Promise<Object>} 转移结果
 */
export async function transferOverdueItem(logisticsNo, handlerName, handlerRemark) {
  try {
    console.log('[LogisticsManager] 转移逾期物品:', logisticsNo);

    // 检查是否逾期
    const overdueCheck = await checkItemOverdue(logisticsNo);
    if (!overdueCheck.overdue) {
      return {
        success: false,
        error: '物品未逾期，无需转移'
      };
    }

    // 调用后端API
    const result = await callLogisticsAPI('/transfer', 'POST', {
      logisticsNo,
      handlerName,
      handlerRemark
    });

    if (result.success) {
      console.log('[LogisticsManager] 物品转移成功:', logisticsNo);
      return {
        success: true,
        transferTime: result.data.transferTime
      };
    } else {
      return {
        success: false,
        error: result.message || '物品转移失败'
      };
    }
  } catch (error) {
    console.error('[LogisticsManager] 物品转移异常:', error);
    return {
      success: false,
      error: error.message || '物品转移异常'
    };
  }
}

// ==================== 物品照片上传 ====================

/**
 * 上传物品照片
 * @param {string} filePath 照片文件路径
 * @returns {Promise<Object>} 上传结果
 */
async function uploadItemPhoto(filePath) {
  return new Promise((resolve) => {
    uni.uploadFile({
      url: `${API_BASE_URL}/upload`,
      filePath: filePath,
      name: 'file',
      success: (res) => {
        if (res.statusCode === 200) {
          const data = JSON.parse(res.data);
          if (data.code === 200) {
            resolve({
              success: true,
              url: data.data.url
            });
          } else {
            resolve({
              success: false,
              error: data.message || '上传失败'
            });
          }
        } else {
          resolve({
            success: false,
            error: '上传失败'
          });
        }
      },
      fail: (error) => {
        resolve({
          success: false,
          error: error.errMsg || '网络请求失败'
        });
      }
    });
  });
}

// ==================== 物品统计 ====================

/**
 * 获取物品统计信息
 * @param {Object} params 查询参数
 * @param {string} params.startDate 开始日期
 * @param {string} params.endDate 结束日期
 * @returns {Promise<Object>} 统计信息
 */
export async function getItemStatistics(params) {
  try {
    const result = await callLogisticsAPI('/statistics', 'GET', params);
    if (result.success) {
      return {
        success: true,
        data: result.data
      };
    }
    return {
      success: false,
      error: result.message || '获取统计信息失败'
    };
  } catch (error) {
    console.error('[LogisticsManager] 获取物品统计失败:', error);
    return {
      success: false,
      error: error.message || '获取统计信息异常'
    };
  }
}

// ==================== API调用工具 ====================

/**
 * 调用物流管理API
 * @param {string} url API路径
 * @param {string} method HTTP方法
 * @param {Object} data 请求数据（可选）
 * @returns {Promise<Object>} API响应
 */
function callLogisticsAPI(url, method, data) {
  return new Promise((resolve) => {
    uni.request({
      url: `${API_BASE_URL}${url}`,
      method: method,
      data: data,
      success: (res) => {
        if (res.statusCode === 200) {
          resolve({
            success: res.data.code === 200,
            code: res.data.code,
            message: res.data.message,
            data: res.data.data
          });
        } else {
          resolve({
            success: false,
            message: `HTTP错误: ${res.statusCode}`
          });
        }
      },
      fail: (error) => {
        resolve({
          success: false,
          message: error.errMsg || '网络请求失败'
        });
      }
    });
  });
}

// ==================== 导出 ====================

export default {
  // 单号管理
  generateLogisticsNo,
  parseLogisticsNo,

  // 物品寄存
  depositItem,
  batchDepositItems,

  // 物品领取
  pickupItem,

  // 物品查询
  getItemInfo,
  getItemsByRegistration,
  getOverdueItems,
  checkItemOverdue,

  // 物品转移
  transferOverdueItem,

  // 统计
  getItemStatistics,

  // 常量
  OVERDUE_DAYS
};
