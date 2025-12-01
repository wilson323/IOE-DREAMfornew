#!/usr/bin/env node

/**
 * 考勤模块前端组件验证脚本
 * 验证前端组件的完整性、API调用、数据流和用户体验
 */

const fs = require('fs');
const path = require('path');

class FrontendComponentValidator {
    constructor() {
        this.baseDir = path.join(__dirname, '../smart-admin-web-javascript/src');
        this.attendanceDir = path.join(this.baseDir, 'views/business/attendance');
        this.apiDir = path.join(this.baseDir, 'api/business/attendance');

        this.results = {
            totalChecks: 0,
            passedChecks: 0,
            failedChecks: 0,
            warnings: [],
            errors: []
        };

        this.requiredComponents = [
            'attendance-punch.vue',
            'attendance-schedule.vue',
            'attendance-statistics.vue'
        ];

        this.requiredApiFile = 'attendance-api.js';

        this.requiredSubComponents = [
            'schedule-detail-drawer.vue',
            'schedule-conflict-modal.vue',
            'conflict-list.vue'
        ];
    }

    log(message, type = 'info') {
        const timestamp = new Date().toISOString();
        const logMessage = `[${timestamp}] ${message}`;

        console.log(logMessage);

        switch(type) {
            case 'success':
                this.results.passedChecks++;
                break;
            case 'error':
                this.results.failedChecks++;
                this.results.errors.push(message);
                break;
            case 'warning':
                this.results.warnings.push(message);
                break;
        }
        this.results.totalChecks++;
    }

    logSuccess(message) {
        console.log(`✅ ${message}`);
        this.results.passedChecks++;
        this.results.totalChecks++;
    }

    logError(message) {
        console.log(`❌ ${message}`);
        this.results.failedChecks++;
        this.results.errors.push(message);
        this.results.totalChecks++;
    }

    logWarning(message) {
        console.log(`⚠️  ${message}`);
        this.results.warnings.push(message);
    }

    validateFileExists(filePath, description) {
        if (fs.existsSync(filePath)) {
            this.logSuccess(`${description} 文件存在: ${path.relative(this.baseDir, filePath)}`);
            return true;
        } else {
            this.logError(`${description} 文件缺失: ${path.relative(this.baseDir, filePath)}`);
            return false;
        }
    }

    validateComponentStructure() {
        console.log('\n🔍 验证组件结构...');

        // 验证主组件文件
        for (const component of this.requiredComponents) {
            const componentPath = path.join(this.attendanceDir, component);
            this.validateFileExists(componentPath, `主组件 ${component}`);
        }

        // 验证API文件
        const apiPath = path.join(this.apiDir, this.requiredApiFile);
        this.validateFileExists(apiPath, `API文件 ${this.requiredApiFile}`);

        // 验证子组件目录
        const componentsDir = path.join(this.attendanceDir, 'components');
        if (fs.existsSync(componentsDir)) {
            this.logSuccess('components 目录存在');

            // 验证子组件文件
            for (const subComponent of this.requiredSubComponents) {
                const subComponentPath = path.join(componentsDir, subComponent);
                this.validateFileExists(subComponentPath, `子组件 ${subComponent}`);
            }
        } else {
            this.logWarning('components 目录不存在，可能缺少子组件');
        }
    }

    validateVueComponent(filePath) {
        try {
            const content = fs.readFileSync(filePath, 'utf8');

            const checks = [
                { pattern: /export default\s*\{/, name: '包含导出对象' },
                { pattern: /<script\s+setup>/, name: '使用 script setup 语法' },
                { pattern: /import.*from\s+['"]vue['"]/, name: '导入 Vue' },
                { pattern: /import.*ant-design-vue/, name: '导入 Ant Design Vue' },
                { pattern: /import.*dayjs/, name: '导入 dayjs' },
                { pattern: /import.*attendance-api/, name: '导入 API' }
            ];

            let componentIssues = 0;

            for (const check of checks) {
                if (check.pattern.test(content)) {
                    this.logSuccess(`${path.basename(filePath)}: ${check.name}`);
                } else {
                    this.logWarning(`${path.basename(filePath)}: 缺少 ${check.name}`);
                    componentIssues++;
                }
            }

            // 检查必要的生命周期钩子
            const lifecycleChecks = [
                { pattern: /onMounted/, name: 'onMounted 钩子' }
            ];

            for (const check of lifecycleChecks) {
                if (check.pattern.test(content)) {
                    this.logSuccess(`${path.basename(filePath)}: 包含 ${check.name}`);
                }
            }

            return componentIssues === 0;

        } catch (error) {
            this.logError(`读取文件失败 ${filePath}: ${error.message}`);
            return false;
        }
    }

    validateComponentContents() {
        console.log('\n🔍 验证组件内容...');

        for (const component of this.requiredComponents) {
            const componentPath = path.join(this.attendanceDir, component);
            if (fs.existsSync(componentPath)) {
                const isValid = this.validateVueComponent(componentPath);
                if (!isValid) {
                    this.logWarning(`${component} 存在内容问题`);
                }
            }
        }
    }

    validateApiStructure() {
        console.log('\n🔍 验证API结构...');

        const apiPath = path.join(this.apiDir, this.requiredApiFile);
        if (!fs.existsSync(apiPath)) {
            return;
        }

        try {
            const content = fs.readFileSync(apiPath, 'utf8');

            // 检查必要的API方法
            const requiredMethods = [
                'punchIn',
                'punchOut',
                'getTodayPunchRecord',
                'queryAttendanceRecords',
                'getAttendanceStatistics',
                'getPersonalStatistics',
                'getDepartmentStatistics',
                'getEmployeeSchedule',
                'getDepartmentSchedule',
                'saveOrUpdateSchedule'
            ];

            for (const method of requiredMethods) {
                const pattern = new RegExp(`${method}\\s*:\\s*`, 'i');
                if (pattern.test(content)) {
                    this.logSuccess(`API方法存在: ${method}`);
                } else {
                    this.logError(`API方法缺失: ${method}`);
                }
            }

            // 检查导出结构
            if (/export\s+const\s+attendanceApi\s*=/.test(content)) {
                this.logSuccess('API导出结构正确');
            } else {
                this.logError('API导出结构不正确');
            }

        } catch (error) {
            this.logError(`验证API文件失败: ${error.message}`);
        }
    }

    validateRouteConfiguration() {
        console.log('\n🔍 验证路由配置...');

        // 检查是否有路由配置文件
        const routerFiles = [
            path.join(this.baseDir, 'router/routers.js'),
            path.join(this.baseDir, 'router/index.js'),
            path.join(this.baseDir, 'router/business-routes.js')
        ];

        let hasRoutes = false;
        for (const routerFile of routerFiles) {
            if (fs.existsSync(routerFile)) {
                try {
                    const content = fs.readFileSync(routerFile, 'utf8');
                    if (content.includes('attendance')) {
                        this.logSuccess(`路由配置存在考勤相关路由: ${path.basename(routerFile)}`);
                        hasRoutes = true;
                    }
                } catch (error) {
                    this.logWarning(`读取路由文件失败: ${routerFile}`);
                }
            }
        }

        if (!hasRoutes) {
            this.logWarning('未找到考勤相关路由配置');
        }
    }

    validatePermissionUsage() {
        console.log('\n🔍 验证权限配置...');

        for (const component of this.requiredComponents) {
            const componentPath = path.join(this.attendanceDir, component);
            if (fs.existsSync(componentPath)) {
                try {
                    const content = fs.readFileSync(componentPath, 'utf8');

                    // 检查权限指令使用
                    if (content.includes('v-privilege')) {
                        this.logSuccess(`${component}: 使用权限指令`);
                    } else {
                        this.logWarning(`${component}: 未使用权限指令`);
                    }

                    // 检查权限相关的API调用
                    const permissionApiPatterns = [
                        /attendance.*:punch/,
                        /attendance.*:schedule/,
                        /attendance.*:statistics/,
                        /attendance.*:query/
                    ];

                    let hasPermission = false;
                    for (const pattern of permissionApiPatterns) {
                        if (pattern.test(content)) {
                            hasPermission = true;
                            break;
                        }
                    }

                    if (hasPermission) {
                        this.logSuccess(`${component}: 包含权限相关配置`);
                    } else {
                        this.logWarning(`${component}: 缺少权限相关配置`);
                    }

                } catch (error) {
                    this.logError(`检查权限配置失败 ${component}: ${error.message}`);
                }
            }
        }
    }

    validateResponsiveDesign() {
        console.log('\n🔍 验证响应式设计...');

        for (const component of this.requiredComponents) {
            const componentPath = path.join(this.attendanceDir, component);
            if (fs.existsSync(componentPath)) {
                try {
                    const content = fs.readFileSync(componentPath, 'utf8');

                    // 检查响应式设计相关
                    const responsiveChecks = [
                        { pattern: /<a-col\s+[^>]*:span=/, name: '使用栅格系统' },
                        { pattern: /\.responsive|@media|mobile|phone/, name: '响应式样式' },
                        { pattern: /flex|grid|display\s*:/, name: '现代布局' }
                    ];

                    let responsiveScore = 0;
                    for (const check of responsiveChecks) {
                        if (check.pattern.test(content)) {
                            responsiveScore++;
                            this.logSuccess(`${component}: ${check.name}`);
                        }
                    }

                    if (responsiveScore === 0) {
                        this.logWarning(`${component}: 缺少响应式设计考虑`);
                    }

                } catch (error) {
                    this.logError(`检查响应式设计失败 ${component}: ${error.message}`);
                }
            }
        }
    }

    validateDataFlow() {
        console.log('\n🔍 验证数据流...');

        for (const component of this.requiredComponents) {
            const componentPath = path.join(this.attendanceDir, component);
            if (fs.existsSync(componentPath)) {
                try {
                    const content = fs.readFileSync(componentPath, 'utf8');

                    // 检查数据流相关
                    const dataFlowChecks = [
                        { pattern: /ref\(|reactive\(/, name: '使用响应式数据' },
                        { pattern: /computed\(/, name: '使用计算属性' },
                        { pattern: /watch\(|watchEffect\(/, name: '使用监听器' },
                        { pattern: /async\s+function|await\s+/, name: '异步数据处理' }
                    ];

                    let dataFlowScore = 0;
                    for (const check of dataFlowChecks) {
                        if (check.pattern.test(content)) {
                            dataFlowScore++;
                            this.logSuccess(`${component}: ${check.name}`);
                        }
                    }

                    if (dataFlowScore < 2) {
                        this.logWarning(`${component}: 数据流处理可能不够完善`);
                    }

                } catch (error) {
                    this.logError(`检查数据流失败 ${component}: ${error.message}`);
                }
            }
        }
    }

    runAllValidations() {
        console.log('🚀 开始考勤模块前端组件验证...\n');
        console.log('📍 基础目录:', this.baseDir);
        console.log('📍 考勤目录:', this.attendanceDir);
        console.log('📍 API目录:', this.apiDir);
        console.log('=' .repeat(60));

        // 执行各项验证
        this.validateComponentStructure();
        this.validateComponentContents();
        this.validateApiStructure();
        this.validateRouteConfiguration();
        this.validatePermissionUsage();
        this.validateResponsiveDesign();
        this.validateDataFlow();

        // 输出结果
        console.log('\n' + '=' .repeat(60));
        console.log('📊 验证结果汇总:');
        console.log(`   总检查项: ${this.results.totalChecks}`);
        console.log(`   通过检查: ${this.results.passedChecks}`);
        console.log(`   失败检查: ${this.results.failedChecks}`);
        console.log(`   警告数量: ${this.results.warnings.length}`);

        if (this.results.errors.length > 0) {
            console.log('\n❌ 错误详情:');
            this.results.errors.forEach(error => console.log(`   - ${error}`));
        }

        if (this.results.warnings.length > 0) {
            console.log('\n⚠️  警告详情:');
            this.results.warnings.forEach(warning => console.log(`   - ${warning}`));
        }

        const successRate = this.results.totalChecks > 0
            ? ((this.results.passedChecks / this.results.totalChecks) * 100).toFixed(1)
            : 0;

        console.log(`\n📈 通过率: ${successRate}%`);

        if (this.results.failedChecks === 0) {
            console.log('🎉 所有检查通过！前端组件结构良好。');
        } else {
            console.log(`⚠️  有 ${this.results.failedChecks} 个检查失败，需要修复。`);
        }

        return {
            totalChecks: this.results.totalChecks,
            passedChecks: this.results.passedChecks,
            failedChecks: this.results.failedChecks,
            warnings: this.results.warnings,
            errors: this.results.errors,
            successRate: parseFloat(successRate)
        };
    }
}

// 执行验证
if (require.main === module) {
    const validator = new FrontendComponentValidator();
    const results = validator.runAllValidations();

    // 设置退出码
    process.exit(results.failedChecks > 0 ? 1 : 0);
}

module.exports = FrontendComponentValidator;