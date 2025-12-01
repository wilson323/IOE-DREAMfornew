#!/usr/bin/env node

/**
 * 前后端API接口一致性验证脚本
 *
 * 使用方法: node scripts/verify-api-consistency.js
 *
 * @author IOE-DREAM Team
 * @date 2025-11-17
 */

const fs = require('fs');
const path = require('path');

// 配置路径
const FRONTEND_API_PATH = path.join(__dirname, '../smart-admin-web-javascript/src/api/business/consume');
const BACKEND_CONTROLLER_PATH = path.join(__dirname, '../smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/controller');

// API接口定义
const EXPECTED_APIS = {
  consume: [
    { method: 'POST', path: '/api/consume/pay', frontend: 'pay', backend: 'ConsumeController.pay' },
    { method: 'POST', path: '/api/consume/consume', frontend: 'consume', backend: 'ConsumeController.consume' },
    { method: 'GET', path: '/api/consume/records', frontend: 'getRecords', backend: 'ConsumeController.pageRecords' },
    { method: 'GET', path: '/api/consume/detail/{id}', frontend: 'getDetail', backend: 'ConsumeController.getConsumeDetail' },
    { method: 'GET', path: '/api/consume/statistics', frontend: 'getStatistics', backend: 'ConsumeController.getConsumeStatistics' },
    { method: 'POST', path: '/api/consume/refund/{id}', frontend: 'refund', backend: 'ConsumeController.refundConsume' },
    { method: 'GET', path: '/api/consume/modes', frontend: 'getModes', backend: 'ConsumeController.getConsumeModes' },
    { method: 'GET', path: '/api/consume/trend', frontend: 'getConsumeTrend', backend: 'ConsumeController.getConsumeTrend' },
    { method: 'GET', path: '/api/consume/validate', frontend: 'validateConsume', backend: 'ConsumeController.validateConsume' },
    { method: 'POST', path: '/api/consume/batch', frontend: 'batchConsume', backend: 'ConsumeController.batchConsume' },
    { method: 'GET', path: '/api/consume/export', frontend: 'exportRecords', backend: 'ConsumeController.exportRecords' },
    { method: 'POST', path: '/api/consume/cancel/{id}', frontend: 'cancelConsume', backend: 'ConsumeController.cancelConsume' },
    { method: 'GET', path: '/api/consume/logs/{id}', frontend: 'getConsumeLogs', backend: 'ConsumeController.getConsumeLogs' },
    { method: 'POST', path: '/api/consume/sync', frontend: 'syncConsumeData', backend: 'ConsumeController.syncConsumeData' }
  ],
  account: [
    { method: 'POST', path: '/api/consume/account/create', frontend: 'createAccount', backend: 'AccountController.createAccount' },
    { method: 'GET', path: '/api/consume/account/list', frontend: 'getAccountList', backend: 'AccountController.getAccountList' },
    { method: 'GET', path: '/api/consume/account/detail/{accountId}', frontend: 'getAccountDetail', backend: 'AccountController.getAccountDetail' },
    { method: 'PUT', path: '/api/consume/account/update', frontend: 'updateAccount', backend: 'AccountController.updateAccount' },
    { method: 'POST', path: '/api/consume/account/recharge', frontend: 'rechargeAccount', backend: 'AccountController.rechargeAccount' },
    { method: 'GET', path: '/api/consume/account/balance/{accountId}', frontend: 'getAccountBalance', backend: 'AccountController.getAccountBalance' },
    { method: 'POST', path: '/api/consume/account/freeze/{accountId}', frontend: 'freezeAccount', backend: 'AccountController.freezeAccount' },
    { method: 'POST', path: '/api/consume/account/unfreeze/{accountId}', frontend: 'unfreezeAccount', backend: 'AccountController.unfreezeAccount' },
    { method: 'POST', path: '/api/consume/account/close/{accountId}', frontend: 'closeAccount', backend: 'AccountController.closeAccount' },
    { method: 'GET', path: '/api/consume/account/transactions/{accountId}', frontend: 'getAccountTransactions', backend: 'AccountController.getAccountTransactions' },
    { method: 'GET', path: '/api/consume/account/statistics', frontend: 'getAccountStatistics', backend: 'AccountController.getAccountStatistics' },
    { method: 'GET', path: '/api/consume/account/export', frontend: 'exportAccounts', backend: 'AccountController.exportAccounts' },
    { method: 'GET', path: '/api/consume/account/types', frontend: 'getAccountTypes', backend: 'AccountController.getAccountTypes' },
    { method: 'POST', path: '/api/consume/account/batch/status', frontend: 'batchUpdateStatus', backend: 'AccountController.batchUpdateStatus' }
  ],
  report: [
    { method: 'GET', path: '/api/consume/report/summary', frontend: 'getConsumeSummary', backend: 'ReportController.getConsumeSummary' },
    { method: 'GET', path: '/api/consume/report/mode-distribution', frontend: 'getModeDistribution', backend: 'ReportController.getModeDistribution' },
    { method: 'GET', path: '/api/consume/report/daily-trend', frontend: 'getDailyTrend', backend: 'ReportController.getDailyTrend' },
    { method: 'GET', path: '/api/consume/report/device-ranking', frontend: 'getDeviceRanking', backend: 'ReportController.getDeviceRanking' },
    { method: 'GET', path: '/api/consume/report/user-ranking', frontend: 'getUserRanking', backend: 'ReportController.getUserRanking' },
    { method: 'GET', path: '/api/consume/report/hourly-distribution', frontend: 'getHourlyDistribution', backend: 'ReportController.getHourlyDistribution' },
    { method: 'GET', path: '/api/consume/report/abnormal-detection', frontend: 'getAbnormalDetection', backend: 'ReportController.getAbnormalDetection' },
    { method: 'GET', path: '/api/consume/report/export', frontend: 'exportReport', backend: 'ReportController.exportReport' }
  ]
};

console.log('🔍 开始验证前后端API接口一致性...\n');

// 验证结果统计
let totalApis = 0;
let passedApis = 0;
let failedApis = [];

// 验证前端API文件
function verifyFrontendApi() {
  console.log('📱 验证前端API文件...');

  try {
    // 检查主API文件
    const mainApiFile = path.join(FRONTEND_API_PATH, 'index.js');
    if (fs.existsSync(mainApiFile)) {
      console.log('✅ 主API文件存在: index.js');
    } else {
      console.log('❌ 主API文件不存在: index.js');
      failedApis.push('Frontend: index.js missing');
    }

    // 检查账户API文件
    const accountApiFile = path.join(FRONTEND_API_PATH, '../consumption/consume-account-api.js');
    if (fs.existsSync(accountApiFile)) {
      console.log('✅ 账户API文件存在: consume-account-api.js');
    } else {
      console.log('❌ 账户API文件不存在: consume-account-api.js');
      failedApis.push('Frontend: consume-account-api.js missing');
    }

    // 检查设备API文件
    const deviceApiFile = path.join(FRONTEND_API_PATH, '../consumption/consume-device-api.js');
    if (fs.existsSync(deviceApiFile)) {
      console.log('✅ 设备API文件存在: consume-device-api.js');
    } else {
      console.log('❌ 设备API文件不存在: consume-device-api.js');
      failedApis.push('Frontend: consume-device-api.js missing');
    }

  } catch (error) {
    console.log('❌ 前端API文件验证失败:', error.message);
    failedApis.push('Frontend: ' + error.message);
  }
}

// 验证后端Controller文件
function verifyBackendController() {
  console.log('\n🔧 验证后端Controller文件...');

  try {
    const controllers = ['ConsumeController.java', 'AccountController.java', 'ReportController.java'];

    controllers.forEach(controller => {
      const controllerFile = path.join(BACKEND_CONTROLLER_PATH, controller);
      if (fs.existsSync(controllerFile)) {
        console.log(`✅ Controller文件存在: ${controller}`);
      } else {
        console.log(`❌ Controller文件不存在: ${controller}`);
        failedApis.push(`Backend: ${controller} missing`);
      }
    });

  } catch (error) {
    console.log('❌ 后端Controller文件验证失败:', error.message);
    failedApis.push('Backend: ' + error.message);
  }
}

// 验证API路径一致性
function verifyApiPathConsistency() {
  console.log('\n🔗 验证API路径一致性...');

  Object.keys(EXPECTED_APIS).forEach(module => {
    console.log(`\n📋 验证 ${module} 模块API:`);

    EXPECTED_APIS[module].forEach(api => {
      totalApis++;

      try {
        // 检查前端API方法定义
        const frontendApiFile = path.join(FRONTEND_API_PATH, 'index.js');
        if (fs.existsSync(frontendApiFile)) {
          const frontendContent = fs.readFileSync(frontendApiFile, 'utf8');

          if (frontendContent.includes(api.frontend)) {
            console.log(`  ✅ ${api.method} ${api.path} -> ${api.frontend}`);
            passedApis++;
          } else {
            console.log(`  ❌ ${api.method} ${api.path} -> ${api.frontend} (前端方法不存在)`);
            failedApis.push(`${module}: ${api.frontend} not found in frontend`);
          }
        }

        // 检查后端Controller方法定义
        const backendControllerFile = path.join(BACKEND_CONTROLLER_PATH, `${module.charAt(0).toUpperCase() + module.slice(1)}Controller.java`);
        if (fs.existsSync(backendControllerFile)) {
          const backendContent = fs.readFileSync(backendControllerFile, 'utf8');

          if (backendContent.includes(api.backend.split('.')[1])) {
            // 检查路径映射
            if (backendContent.includes(api.path.split('/')[api.path.split('/').length - 1])) {
              console.log(`    ✅ 后端方法匹配: ${api.backend}`);
            } else {
              console.log(`    ❌ 后端路径不匹配: ${api.path}`);
              failedApis.push(`${module}: ${api.path} not found in backend`);
            }
          } else {
            console.log(`    ❌ 后端方法不存在: ${api.backend}`);
            failedApis.push(`${module}: ${api.backend} not found in backend`);
          }
        }

      } catch (error) {
        console.log(`  ❌ 验证 ${api.path} 时出错:`, error.message);
        failedApis.push(`${module}: ${api.path} verification failed`);
      }
    });
  });
}

// 验证权限注解一致性
function verifyPermissionConsistency() {
  console.log('\n🔐 验证权限注解一致性...');

  const expectedPermissions = [
    'consume:pay:add',
    'consume:record:query',
    'consume:record:detail',
    'consume:record:export',
    'consume:execute:add',
    'consume:refund:add',
    'consume:statistics:view',
    'consume:trend:view',
    'consume:account:create',
    'consume:account:list',
    'consume:account:detail',
    'consume:account:update',
    'consume:account:recharge',
    'consume:account:freeze',
    'consume:account:unfreeze',
    'consume:report:summary',
    'consume:report:export'
  ];

  try {
    const controllers = ['ConsumeController.java', 'AccountController.java', 'ReportController.java'];

    controllers.forEach(controller => {
      const controllerFile = path.join(BACKEND_CONTROLLER_PATH, controller);
      if (fs.existsSync(controllerFile)) {
        const content = fs.readFileSync(controllerFile, 'utf8');

        expectedPermissions.forEach(permission => {
          if (content.includes(permission)) {
            console.log(`  ✅ 权限注解存在: ${permission}`);
          } else {
            console.log(`  ⚠️  权限注解缺失: ${permission}`);
          }
        });
      }
    });

  } catch (error) {
    console.log('❌ 权限注解验证失败:', error.message);
    failedApis.push('Permission verification failed: ' + error.message);
  }
}

// 验证响应格式一致性
function verifyResponseFormatConsistency() {
  console.log('\n📊 验证响应格式一致性...');

  try {
    const controllers = ['ConsumeController.java', 'AccountController.java', 'ReportController.java'];

    controllers.forEach(controller => {
      const controllerFile = path.join(BACKEND_CONTROLLER_PATH, controller);
      if (fs.existsSync(controllerFile)) {
        const content = fs.readFileSync(controllerFile, 'utf8');

        // 检查ResponseDTO使用
        if (content.includes('ResponseDTO.ok(') && content.includes('ResponseDTO.error(')) {
          console.log(`  ✅ ${controller} 响应格式正确`);
        } else {
          console.log(`  ❌ ${controller} 响应格式不规范`);
          failedApis.push(`${controller}: Response format not consistent`);
        }

        // 检查异常处理
        if (content.includes('try {') && content.includes('catch (Exception')) {
          console.log(`    ✅ ${controller} 异常处理完整`);
        } else {
          console.log(`    ⚠️  ${controller} 异常处理不完整`);
        }
      }
    });

  } catch (error) {
    console.log('❌ 响应格式验证失败:', error.message);
    failedApis.push('Response format verification failed: ' + error.message);
  }
}

// 生成验证报告
function generateReport() {
  console.log('\n📋 验证报告');
  console.log('=' .repeat(50));
  console.log(`总API数量: ${totalApis}`);
  console.log(`通过验证: ${passedApis}`);
  console.log(`失败数量: ${failedApis.length}`);
  console.log(`通过率: ${((passedApis / totalApis) * 100).toFixed(2)}%`);

  if (failedApis.length > 0) {
    console.log('\n❌ 失败项目:');
    failedApis.forEach((failure, index) => {
      console.log(`  ${index + 1}. ${failure}`);
    });
  }

  // 生成建议
  console.log('\n💡 改进建议:');
  if (failedApis.length === 0) {
    console.log('✅ 所有API接口验证通过，前后端接口完全一致！');
  } else {
    console.log('1. 修复缺失的前端API方法');
    console.log('2. 补充缺失的后端Controller方法');
    console.log('3. 统一API路径格式');
    console.log('4. 完善权限注解配置');
    console.log('5. 规范化异常处理机制');
  }

  console.log('\n📝 详细文档参考: docs/前后端接口对照表.md');
}

// 主执行函数
function main() {
  verifyFrontendApi();
  verifyBackendController();
  verifyApiPathConsistency();
  verifyPermissionConsistency();
  verifyResponseFormatConsistency();
  generateReport();
}

// 执行验证
main();