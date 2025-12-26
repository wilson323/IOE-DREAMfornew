/**
 * 访客二维码管理工具
 * 提供二维码生成、缓存、验证等功能
 */

import CryptoJS from 'crypto-js';

const SECRET_KEY = 'IOE-DREAM-VISITOR-QR-2025';

// ==================== 二维码生成 ====================

/**
 * 生成访客二维码数据
 * @param {Object} params 二维码参数
 * @param {number} params.appointmentId 预约ID
 * @param {number} params.visitorId 访客ID
 * @param {string} params.visitorName 访客姓名
 * @param {string} params.visitorPhone 访客电话
 * @param {number} params.visiteeId 被访人ID
 * @param {string} params.visiteeName 被访人姓名
 * @param {string} params.appointmentTime 预约时间
 * @returns {string} 二维码数据字符串
 */
export function generateVisitorQRCodeData(params) {
  const qrData = {
    type: 'VISITOR',
    version: '1.0',
    appointmentId: params.appointmentId,
    visitorId: params.visitorId,
    visitorName: params.visitorName,
    visitorPhone: params.visitorPhone,
    visiteeId: params.visiteeId,
    visiteeName: params.visiteeName,
    appointmentTime: params.appointmentTime,
    timestamp: Date.now(),
    signature: generateSignature(params)
  };

  // 转换为JSON字符串并进行Base64编码
  const jsonStr = JSON.stringify(qrData);
  return CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse(jsonStr));
}

/**
 * 生成签名
 * @param {Object} params 参数对象
 * @returns {string} 签名字符串
 */
function generateSignature(params) {
  const signStr = `${params.appointmentId}|${params.visitorId}|${params.visitorPhone}|${params.appointmentTime}|${SECRET_KEY}`;
  return CryptoJS.MD5(signStr).toString();
}

// ==================== 二维码缓存 ====================

const CACHE_PREFIX = 'visitor_qrcode_';
const CACHE_EXPIRE_TIME = 24 * 60 * 60 * 1000; // 24小时

/**
 * 缓存访客二维码
 * @param {number} appointmentId 预约ID
 * @param {string} qrCodeData 二维码数据
 * @param {number} expireTime 过期时间（可选，默认24小时）
 */
export function cacheVisitorQRCode(appointmentId, qrCodeData, expireTime = CACHE_EXPIRE_TIME) {
  try {
    const cacheKey = `${CACHE_PREFIX}${appointmentId}`;
    const cacheData = {
      qrCodeData,
      timestamp: Date.now(),
      expireTime: Date.now() + expireTime
    };

    uni.setStorageSync(cacheKey, JSON.stringify(cacheData));

    console.log('[VisitorQRCode] 二维码已缓存:', appointmentId);
    return true;
  } catch (error) {
    console.error('[VisitorQRCode] 缓存二维码失败:', error);
    return false;
  }
}

/**
 * 获取缓存的访客二维码
 * @param {number} appointmentId 预约ID
 * @returns {string|null} 二维码数据，如果不存在或已过期返回null
 */
export function getCachedVisitorQRCode(appointmentId) {
  try {
    const cacheKey = `${CACHE_PREFIX}${appointmentId}`;
    const cacheStr = uni.getStorageSync(cacheKey);

    if (!cacheStr) {
      console.log('[VisitorQRCode] 二维码缓存不存在:', appointmentId);
      return null;
    }

    const cacheData = JSON.parse(cacheStr);

    // 检查是否过期
    if (Date.now() > cacheData.expireTime) {
      console.log('[VisitorQRCode] 二维码缓存已过期:', appointmentId);
      removeCachedVisitorQRCode(appointmentId);
      return null;
    }

    console.log('[VisitorQRCode] 二维码缓存命中:', appointmentId);
    return cacheData.qrCodeData;
  } catch (error) {
    console.error('[VisitorQRCode] 获取缓存二维码失败:', error);
    return null;
  }
}

/**
 * 移除缓存的访客二维码
 * @param {number} appointmentId 预约ID
 */
export function removeCachedVisitorQRCode(appointmentId) {
  try {
    const cacheKey = `${CACHE_PREFIX}${appointmentId}`;
    uni.removeStorageSync(cacheKey);
    console.log('[VisitorQRCode] 二维码缓存已移除:', appointmentId);
    return true;
  } catch (error) {
    console.error('[VisitorQRCode] 移除缓存二维码失败:', error);
    return false;
  }
}

/**
 * 清理所有过期的访客二维码缓存
 */
export function cleanExpiredVisitorQRCodes() {
  try {
    const storageInfo = uni.getStorageInfoSync();
    const keys = storageInfo.keys || [];
    let cleanedCount = 0;

    keys.forEach(key => {
      if (key.startsWith(CACHE_PREFIX)) {
        try {
          const cacheStr = uni.getStorageSync(key);
          if (cacheStr) {
            const cacheData = JSON.parse(cacheStr);
            if (Date.now() > cacheData.expireTime) {
              uni.removeStorageSync(key);
              cleanedCount++;
            }
          }
        } catch (error) {
          // 清理损坏的缓存
          uni.removeStorageSync(key);
          cleanedCount++;
        }
      }
    });

    console.log(`[VisitorQRCode] 清理过期缓存完成: ${cleanedCount}条`);
    return cleanedCount;
  } catch (error) {
    console.error('[VisitorQRCode] 清理过期缓存失败:', error);
    return 0;
  }
}

// ==================== 二维码验证 ====================

/**
 * 解析访客二维码数据
 * @param {string} qrCodeData 二维码数据字符串
 * @returns {Object|null} 解析后的二维码数据，如果解析失败返回null
 */
export function parseVisitorQRCodeData(qrCodeData) {
  try {
    // Base64解码
    const jsonStr = CryptoJS.enc.Utf8.stringify(CryptoJS.enc.Base64.parse(qrCodeData));
    const qrData = JSON.parse(jsonStr);

    // 验证类型和版本
    if (qrData.type !== 'VISITOR') {
      console.warn('[VisitorQRCode] 二维码类型不匹配:', qrData.type);
      return null;
    }

    if (qrData.version !== '1.0') {
      console.warn('[VisitorQRCode] 二维码版本不匹配:', qrData.version);
      return null;
    }

    // 验证签名
    const expectedSignature = generateSignature(qrData);
    if (qrData.signature !== expectedSignature) {
      console.warn('[VisitorQRCode] 二维码签名验证失败');
      return null;
    }

    return qrData;
  } catch (error) {
    console.error('[VisitorQRCode] 解析二维码数据失败:', error);
    return null;
  }
}

/**
 * 验证访客二维码是否有效
 * @param {string} qrCodeData 二维码数据字符串
 * @param {number} appointmentId 预约ID（用于二次验证）
 * @returns {Object} 验证结果
 */
export function verifyVisitorQRCode(qrCodeData, appointmentId) {
  const qrData = parseVisitorQRCodeData(qrCodeData);

  if (!qrData) {
    return {
      valid: false,
      reason: '二维码格式错误'
    };
  }

  // 验证预约ID是否匹配
  if (qrData.appointmentId !== appointmentId) {
    return {
      valid: false,
      reason: '预约ID不匹配'
    };
  }

  // 检查二维码是否过期（24小时）
  const qrAge = Date.now() - qrData.timestamp;
  if (qrAge > CACHE_EXPIRE_TIME) {
    return {
      valid: false,
      reason: '二维码已过期'
    };
  }

  return {
    valid: true,
    data: qrData
  };
}

/**
 * 检查二维码是否即将过期
 * @param {string} qrCodeData 二维码数据字符串
 * @param {number} expireMinutes 过期提醒时间（分钟，默认30分钟）
 * @returns {boolean} 是否即将过期
 */
export function isQRCodeExpiringSoon(qrCodeData, expireMinutes = 30) {
  try {
    const qrData = parseVisitorQRCodeData(qrCodeData);
    if (!qrData) {
      return true; // 解析失败视为已过期
    }

    const qrAge = Date.now() - qrData.timestamp;
    const expireTime = CACHE_EXPIRE_TIME - qrAge;

    return expireTime < expireMinutes * 60 * 1000;
  } catch (error) {
    console.error('[VisitorQRCode] 检查二维码过期状态失败:', error);
    return true;
  }
}

/**
 * 获取二维码剩余有效时间（分钟）
 * @param {string} qrCodeData 二维码数据字符串
 * @returns {number|null} 剩余有效时间（分钟），如果解析失败返回null
 */
export function getQRCodeRemainingTime(qrCodeData) {
  try {
    const qrData = parseVisitorQRCodeData(qrCodeData);
    if (!qrData) {
      return null;
    }

    const qrAge = Date.now() - qrData.timestamp;
    const remainingTime = CACHE_EXPIRE_TIME - qrAge;

    return Math.max(0, Math.floor(remainingTime / 60 / 1000));
  } catch (error) {
    console.error('[VisitorQRCode] 获取二维码剩余时间失败:', error);
    return null;
  }
}

// ==================== 二维码显示 ====================

/**
 * 渲染访客二维码到Canvas
 * @param {string} canvasId Canvas组件ID
 * @param {string} qrCodeData 二维码数据
 * @param {number} size 二维码尺寸（px，默认200）
 * @returns {Promise<boolean>} 渲染是否成功
 */
export function renderVisitorQRCode(canvasId, qrCodeData, size = 200) {
  return new Promise((resolve, reject) => {
    try {
      const ctx = uni.createCanvasContext(canvasId);

      // 清空画布
      ctx.clearRect(0, 0, size, size);

      // 绘制白色背景
      ctx.fillStyle = '#FFFFFF';
      ctx.fillRect(0, 0, size, size);

      // 计算二维码模块大小
      const moduleCount = Math.sqrt(qrCodeData.length);
      const moduleSize = size / (moduleCount + 2); // +2 为留白

      // 绘制二维码
      for (let i = 0; i < moduleCount; i++) {
        for (let j = 0; j < moduleCount; j++) {
          const index = i * moduleCount + j;
          if (index < qrCodeData.length && qrCodeData[index] === '1') {
            ctx.fillStyle = '#000000';
            ctx.fillRect(
              (j + 1) * moduleSize,
              (i + 1) * moduleSize,
              moduleSize,
              moduleSize
            );
          }
        }
      }

      ctx.draw(false, () => {
        console.log('[VisitorQRCode] 二维码渲染完成');
        resolve(true);
      });
    } catch (error) {
      console.error('[VisitorQRCode] 渲染二维码失败:', error);
      reject(error);
    }
  });
}

/**
 * 生成访客通行证图片（包含二维码和访客信息）
 * @param {Object} params 参数对象
 * @param {string} params.canvasId Canvas组件ID
 * @param {string} params.qrCodeData 二维码数据
 * @param {string} params.visitorName 访客姓名
 * @param {string} params.visiteeName 被访人姓名
 * @param {string} params.appointmentTime 预约时间
 * @param {number} params.width 画布宽度（px，默认300）
 * @param {number} params.height 画布高度（px，默认400）
 * @returns {Promise<boolean>} 渲染是否成功
 */
export function renderVisitorPass(params) {
  const {
    canvasId,
    qrCodeData,
    visitorName,
    visiteeName,
    appointmentTime,
    width = 300,
    height = 400
  } = params;

  return new Promise((resolve, reject) => {
    try {
      const ctx = uni.createCanvasContext(canvasId);

      // 清空画布
      ctx.clearRect(0, 0, width, height);

      // 绘制白色背景
      ctx.fillStyle = '#FFFFFF';
      ctx.fillRect(0, 0, width, height);

      // 绘制标题
      ctx.fillStyle = '#333333';
      ctx.font = 'bold 18px sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText('访客通行证', width / 2, 30);

      // 绘制分隔线
      ctx.strokeStyle = '#DDDDDD';
      ctx.lineWidth = 1;
      ctx.moveTo(20, 45);
      ctx.lineTo(width - 20, 45);
      ctx.stroke();

      // 绘制访客信息
      ctx.font = '14px sans-serif';
      ctx.textAlign = 'left';
      ctx.fillStyle = '#666666';
      ctx.fillText('访客姓名:', 30, 75);
      ctx.fillStyle = '#333333';
      ctx.fillText(visitorName, 100, 75);

      ctx.fillStyle = '#666666';
      ctx.fillText('被访人:', 30, 100);
      ctx.fillStyle = '#333333';
      ctx.fillText(visiteeName, 100, 100);

      ctx.fillStyle = '#666666';
      ctx.fillText('预约时间:', 30, 125);
      ctx.fillStyle = '#333333';
      ctx.fillText(appointmentTime, 100, 125);

      // 绘制二维码（居中显示）
      const qrSize = 150;
      const qrX = (width - qrSize) / 2;
      const qrY = 150;

      // 二维码边框
      ctx.strokeStyle = '#DDDDDD';
      ctx.strokeRect(qrX - 5, qrY - 5, qrSize + 10, qrSize + 10);

      // 绘制二维码
      renderQRCodeToContext(ctx, qrCodeData, qrX, qrY, qrSize);

      // 绘制底部提示
      ctx.fillStyle = '#999999';
      ctx.font = '12px sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText('请出示此二维码通行', width / 2, 350);

      ctx.draw(false, () => {
        console.log('[VisitorQRCode] 访客通行证渲染完成');
        resolve(true);
      });
    } catch (error) {
      console.error('[VisitorQRCode] 渲染访客通行证失败:', error);
      reject(error);
    }
  });
}

/**
 * 在Canvas上下文中绘制二维码
 * @param {Object} ctx Canvas上下文
 * @param {string} qrCodeData 二维码数据
 * @param {number} x X坐标
 * @param {number} y Y坐标
 * @param {number} size 二维码尺寸
 */
function renderQRCodeToContext(ctx, qrCodeData, x, y, size) {
  const moduleCount = Math.sqrt(qrCodeData.length);
  const moduleSize = size / (moduleCount + 2);

  for (let i = 0; i < moduleCount; i++) {
    for (let j = 0; j < moduleCount; j++) {
      const index = i * moduleCount + j;
      if (index < qrCodeData.length && qrCodeData[index] === '1') {
        ctx.fillStyle = '#000000';
        ctx.fillRect(
          x + (j + 1) * moduleSize,
          y + (i + 1) * moduleSize,
          moduleSize,
          moduleSize
        );
      }
    }
  }
}

// ==================== 导出 ====================

export default {
  // 生成
  generateVisitorQRCodeData,
  generateSignature,

  // 缓存
  cacheVisitorQRCode,
  getCachedVisitorQRCode,
  removeCachedVisitorQRCode,
  cleanExpiredVisitorQRCodes,

  // 验证
  parseVisitorQRCodeData,
  verifyVisitorQRCode,
  isQRCodeExpiringSoon,
  getQRCodeRemainingTime,

  // 显示
  renderVisitorQRCode,
  renderVisitorPass
};
