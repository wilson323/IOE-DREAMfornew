/**
 * 人脸识别验证工具
 * 提供人脸比对、活体检测、黑名单检查等功能
 */

// ==================== 配置常量 ====================

const FACE_SIMILARITY_THRESHOLD = 80; // 人脸相似度阈值（%）
const LIVENESS_THRESHOLD = 85; // 活体检测阈值（%）
const API_BASE_URL = '/api/v1/mobile/visitor';

// ==================== 人脸比对 ====================

/**
 * 人脸比对验证
 * @param {Object} params 比对参数
 * @param {number} params.visitorId 访客ID
 * @param {string} params.faceImage 人脸照片（Base64或URL）
 * @param {boolean} params.checkBlacklist 是否检查黑名单（默认true）
 * @returns {Promise<Object>} 验证结果
 */
export async function verifyFace(params) {
  const { visitorId, faceImage, checkBlacklist = true } = params;

  try {
    console.log('[FaceVerification] 开始人脸验证:', visitorId);

    // 1. 获取访客人脸模板
    const template = await getVisitorFaceTemplate(visitorId);
    if (!template) {
      return {
        verified: false,
        reason: '未找到访客人脸模板',
        code: 'TEMPLATE_NOT_FOUND'
      };
    }

    // 2. 执行人脸比对
    const comparisonResult = await compareFace({
      template: template.faceFeature,
      image: faceImage
    });

    if (!comparisonResult.success) {
      return {
        verified: false,
        reason: '人脸比对失败',
        code: 'COMPARISON_FAILED',
        error: comparisonResult.error
      };
    }

    // 3. 检查相似度阈值
    if (comparisonResult.similarity < FACE_SIMILARITY_THRESHOLD) {
      return {
        verified: false,
        reason: '人脸相似度不足',
        code: 'SIMILARITY_LOW',
        score: comparisonResult.similarity,
        threshold: FACE_SIMILARITY_THRESHOLD
      };
    }

    // 4. 活体检测
    const livenessResult = await detectLiveness(faceImage);
    if (!livenessResult.live || livenessResult.confidence < LIVENESS_THRESHOLD) {
      return {
        verified: false,
        reason: '活体检测失败',
        code: 'LIVENESS_FAILED',
        livenessScore: livenessResult.confidence,
        threshold: LIVENESS_THRESHOLD
      };
    }

    // 5. 黑名单检查
    if (checkBlacklist) {
      const blacklistResult = await checkBlacklist(visitorId);
      if (blacklistResult.blacklisted) {
        return {
          verified: false,
          reason: '访客在黑名单中',
          code: 'BLACKLISTED',
          blacklistInfo: blacklistResult.info
        };
      }
    }

    // 验证通过
    console.log('[FaceVerification] 人脸验证通过:', {
      visitorId,
      similarity: comparisonResult.similarity,
      livenessScore: livenessResult.confidence
    });

    return {
      verified: true,
      score: comparisonResult.similarity,
      livenessScore: livenessResult.confidence,
      visitorInfo: template.visitorInfo
    };
  } catch (error) {
    console.error('[FaceVerification] 人脸验证异常:', error);
    return {
      verified: false,
      reason: '人脸验证异常',
      code: 'VERIFICATION_ERROR',
      error: error.message
    };
  }
}

/**
 * 人脸比对（调用后端API）
 * @param {Object} params 比对参数
 * @param {string} params.template 人脸模板特征
 * @param {string} params.image 待比对人脸照片
 * @returns {Promise<Object>} 比对结果
 */
async function compareFace(params) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${API_BASE_URL}/face/compare`,
      method: 'POST',
      data: {
        template: params.template,
        image: params.image
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve({
            success: true,
            similarity: res.data.data.similarity,
            match: res.data.data.similarity >= FACE_SIMILARITY_THRESHOLD
          });
        } else {
          resolve({
            success: false,
            error: res.data?.message || '比对失败'
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

/**
 * 活体检测
 * @param {string} faceImage 人脸照片
 * @returns {Promise<Object>} 活体检测结果
 */
async function detectLiveness(faceImage) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${API_BASE_URL}/face/liveness`,
      method: 'POST',
      data: {
        image: faceImage
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve({
            live: res.data.data.live,
            confidence: res.data.data.confidence
          });
        } else {
          resolve({
            live: false,
            confidence: 0
          });
        }
      },
      fail: (error) => {
        resolve({
          live: false,
          confidence: 0
        });
      }
    });
  });
}

/**
 * 黑名单检查
 * @param {number} visitorId 访客ID
 * @returns {Promise<Object>} 黑名单检查结果
 */
async function checkBlacklist(visitorId) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${API_BASE_URL}/blacklist/check/${visitorId}`,
      method: 'GET',
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve({
            blacklisted: res.data.data.blacklisted,
            info: res.data.data.info
          });
        } else {
          resolve({
            blacklisted: false
          });
        }
      },
      fail: (error) => {
        // 网络失败不影响验证流程
        resolve({
          blacklisted: false
        });
      }
    });
  });
}

// ==================== 人脸模板管理 ====================

/**
 * 获取访客人脸模板
 * @param {number} visitorId 访客ID
 * @returns {Promise<Object|null>} 人脸模板
 */
async function getVisitorFaceTemplate(visitorId) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${API_BASE_URL}/face/template/${visitorId}`,
      method: 'GET',
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve(res.data.data);
        } else {
          resolve(null);
        }
      },
      fail: (error) => {
        console.error('[FaceVerification] 获取人脸模板失败:', error);
        resolve(null);
      }
    });
  });
}

/**
 * 上传人脸照片并提取特征
 * @param {string} imagePath 照片路径
 * @param {number} visitorId 访客ID
 * @returns {Promise<Object>} 上传结果
 */
export async function uploadFacePhoto(imagePath, visitorId) {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${API_BASE_URL}/face/upload`,
      filePath: imagePath,
      name: 'file',
      formData: {
        visitorId: visitorId
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const data = JSON.parse(res.data);
          if (data.code === 200) {
            resolve({
              success: true,
              faceUrl: data.data.faceUrl,
              faceFeature: data.data.faceFeature
            });
          } else {
            resolve({
              success: false,
              error: data.message
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

/**
 * 更新访客人脸模板
 * @param {number} visitorId 访客ID
 * @param {string} faceImage 人脸照片
 * @returns {Promise<Object>} 更新结果
 */
export async function updateFaceTemplate(visitorId, faceImage) {
  try {
    console.log('[FaceVerification] 更新人脸模板:', visitorId);

    // 1. 上传新照片
    const uploadResult = await uploadFacePhoto(faceImage, visitorId);
    if (!uploadResult.success) {
      return {
        success: false,
        error: uploadResult.error
      };
    }

    // 2. 验证新模板
    const verifyResult = await verifyFace({
      visitorId,
      faceImage,
      checkBlacklist: false
    });

    if (!verifyResult.verified) {
      return {
        success: false,
        error: '人脸验证失败',
        reason: verifyResult.reason
      };
    }

    console.log('[FaceVerification] 人脸模板更新成功:', visitorId);
    return {
      success: true,
      faceUrl: uploadResult.faceUrl
    };
  } catch (error) {
    console.error('[FaceVerification] 更新人脸模板异常:', error);
    return {
      success: false,
      error: error.message
    };
  }
}

/**
 * 删除访客人脸模板
 * @param {number} visitorId 访客ID
 * @returns {Promise<Object>} 删除结果
 */
export async function deleteFaceTemplate(visitorId) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${API_BASE_URL}/face/template/${visitorId}`,
      method: 'DELETE',
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve({
            success: true
          });
        } else {
          resolve({
            success: false,
            error: res.data?.message || '删除失败'
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

// ==================== 人脸采集辅助 ====================

/**
 * 检查人脸照片质量
 * @param {string} imagePath 照片路径
 * @returns {Promise<Object>} 质量检查结果
 */
export async function checkFaceQuality(imagePath) {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${API_BASE_URL}/face/quality`,
      filePath: imagePath,
      name: 'file',
      success: (res) => {
        if (res.statusCode === 200) {
          const data = JSON.parse(res.data);
          if (data.code === 200) {
            resolve({
              qualified: data.data.qualified,
              score: data.data.score,
              issues: data.data.issues || []
            });
          } else {
            resolve({
              qualified: false,
              issues: [data.message]
            });
          }
        } else {
          resolve({
            qualified: false,
            issues: ['质量检测失败']
          });
        }
      },
      fail: (error) => {
        resolve({
          qualified: false,
          issues: ['网络请求失败']
        });
      }
    });
  });
}

/**
 * 从相册选择照片并验证质量
 * @param {number} maxCount 最大选择数量（默认1）
 * @returns {Promise<Object>} 选择结果
 */
export async function selectFacePhoto(maxCount = 1) {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count: maxCount,
      sizeType: ['compressed'], // 压缩图
      sourceType: ['album', 'camera'], // 从相册选择或拍照
      success: async (res) => {
        const tempFilePaths = res.tempFilePaths;

        if (tempFilePaths.length === 0) {
          resolve({
            success: false,
            error: '未选择照片'
          });
          return;
        }

        // 检查照片质量
        const qualityResult = await checkFaceQuality(tempFilePaths[0]);

        if (!qualityResult.qualified) {
          resolve({
            success: false,
            error: '照片质量不符合要求',
            issues: qualityResult.issues
          });
          return;
        }

        resolve({
          success: true,
          imagePath: tempFilePaths[0],
          qualityScore: qualityResult.score
        });
      },
      fail: (error) => {
        resolve({
          success: false,
          error: error.errMsg || '选择照片失败'
        });
      }
    });
  });
}

/**
 * 拍摄人脸照片
 * @returns {Promise<Object>} 拍照结果
 */
export async function captureFacePhoto() {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['camera'], // 仅拍照
      success: async (res) => {
        const tempFilePath = res.tempFilePaths[0];

        // 检查照片质量
        const qualityResult = await checkFaceQuality(tempFilePath);

        if (!qualityResult.qualified) {
          resolve({
            success: false,
            error: '照片质量不符合要求',
            issues: qualityResult.issues
          });
          return;
        }

        resolve({
          success: true,
          imagePath: tempFilePath,
          qualityScore: qualityResult.score
        });
      },
      fail: (error) => {
        resolve({
          success: false,
          error: error.errMsg || '拍照失败'
        });
      }
    });
  });
}

// ==================== 人脸识别统计 ====================

/**
 * 获取人脸识别统计
 * @param {Object} params 查询参数
 * @param {string} params.startDate 开始日期
 * @param {string} params.endDate 结束日期
 * @returns {Promise<Object>} 统计结果
 */
export async function getFaceVerificationStats(params) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${API_BASE_URL}/face/statistics`,
      method: 'GET',
      data: params,
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve({
            success: true,
            data: res.data.data
          });
        } else {
          resolve({
            success: false,
            error: res.data?.message || '获取统计失败'
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

// ==================== 导出 ====================

export default {
  // 验证
  verifyFace,
  compareFace,
  detectLiveness,
  checkBlacklist,

  // 模板管理
  getVisitorFaceTemplate,
  uploadFacePhoto,
  updateFaceTemplate,
  deleteFaceTemplate,

  // 采集辅助
  checkFaceQuality,
  selectFacePhoto,
  captureFacePhoto,

  // 统计
  getFaceVerificationStats,

  // 常量
  FACE_SIMILARITY_THRESHOLD,
  LIVENESS_THRESHOLD
};
