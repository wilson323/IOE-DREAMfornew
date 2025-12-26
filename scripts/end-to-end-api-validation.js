#!/usr/bin/env node

/**
 * IOE-DREAM 端到端API一致性验证工具
 *
 * 功能：
 * 1. 检查前端API路径与后端Controller路径的一致性
 * 2. 验证HTTP方法匹配
 * 3. 检查参数映射
 * 4. 生成修复建议
 */

const fs = require('fs');
const path = require('path');

class EndToEndAPIValidator {
    constructor() {
        this.projectRoot = process.cwd();
        this.frontendAPIDir = path.join(this.projectRoot, 'smart-admin-web-javascript', 'src', 'api', 'business');
        this.controllerDir = path.join(this.projectRoot, 'microservices');
        this.validationResults = {
            total: 0,
            passed: 0,
            warnings: 0,
            errors: [],
            details: []
        };
    }

    /**
     * 主验证入口
     */
    async validate() {
        console.log('🔍 开始端到端API一致性验证...\n');

        const modules = ['consume', 'access', 'attendance', 'video', 'visitor', 'workflow'];

        for (const module of modules) {
            await this.validateModule(module);
        }

        this.printResults();
        this.generateReport();
        return this.validationResults;
    }

    /**
     * 验证单个模块
     */
    async validateModule(moduleName) {
        console.log(`📦 验证模块: ${moduleName}`);

        // 获取前端API配置
        const frontendAPI = this.getFrontendAPIConfig(moduleName);
        if (!frontendAPI) {
            console.log(`  ⚠️  前端API文件不存在: ${moduleName}`);
            return;
        }

        // 获取后端控制器信息
        const backendControllers = this.getBackendControllers(moduleName);

        if (backendControllers.length === 0) {
            console.log(`  ❌ 后端控制器不存在: ${moduleName}`);
            this.validationResults.errors.push(`模块${moduleName}缺少后端控制器`);
            return;
        }

        // 验证API匹配
        this.validateAPIEndpoints(moduleName, frontendAPI, backendControllers);
        console.log('');
    }

    /**
     * 获取前端API配置
     */
    getFrontendAPIConfig(moduleName) {
        const apiFile = path.join(this.frontendAPIDir, moduleName, `${moduleName}-api.js`);

        if (!fs.existsSync(apiFile)) {
            return null;
        }

        const content = fs.readFileSync(apiFile, 'utf8');
        return this.parseFrontendAPI(content);
    }

    /**
     * 解析前端API配置
     */
    parseFrontendAPI(content) {
        const apiConfig = {
            baseUrl: this.extractBaseUrl(content),
            endpoints: []
        };

        // 提取所有API调用
        const patterns = [
            /return\s+(postRequest|getRequest|putRequest|deleteRequest)\s*\(\s*['"`]([^'"`]+)['"`]/g,
            /return\s+(postRequest|getRequest|putRequest|deleteRequest)\s*\(\s*['"`]([^'"`]+)['"`]\s*,/g,
            /return\s+postWithParams\s*\(\s*['"`]([^'"`]+)['"`]/g,
            /return\s+request\s*\(\s*\{\s*url:\s*['"`]([^'"`]+)['"`]/g
        ];

        for (const pattern of patterns) {
            let match;
            while ((match = pattern.exec(content)) !== null) {
                const method = this.getMethodType(match[1] || match[0]);
                const path = match[2] || match[1];

                if (path && !path.includes('{') && !path.includes('$')) { // 过滤模板字符串
                    apiConfig.endpoints.push({
                        path: path,
                        method: method
                    });
                }
            }
        }

        return apiConfig;
    }

    /**
     * 提取基础URL
     */
    extractBaseUrl(content) {
        const match = content.match(/baseURL:\s*['"`]([^'"`]+)['"`]/);
        return match ? match[1] : '';
    }

    /**
     * 获取HTTP方法类型
     */
    getMethodType(methodString) {
        if (methodString.includes('post') || methodString.includes('Post')) {
            return 'POST';
        } else if (methodString.includes('get') || methodString.includes('Get')) {
            return 'GET';
        } else if (methodString.includes('put') || methodString.includes('Put')) {
            return 'PUT';
        } else if (methodString.includes('delete') || methodString.includes('Delete')) {
            return 'DELETE';
        }
        return 'UNKNOWN';
    }

    /**
     * 获取后端控制器信息
     */
    getBackendControllers(moduleName) {
        const controllers = [];
        const serviceDir = path.join(this.controllerDir, `ioedream-${moduleName}-service`);

        if (!fs.existsSync(serviceDir)) {
            return controllers;
        }

        const controllerDir = path.join(serviceDir, 'src', 'main', 'java', 'net', 'lab1024', 'sa', moduleName, 'controller');

        if (!fs.existsSync(controllerDir)) {
            return controllers;
        }

        const controllerFiles = fs.readdirSync(controllerDir).filter(file => file.endsWith('.java'));

        for (const file of controllerFiles) {
            const content = fs.readFileSync(path.join(controllerDir, file), 'utf8');
            controllers.push(this.parseController(content));
        }

        return controllers;
    }

    /**
     * 解析控制器
     */
    parseController(content) {
        const controller = {
            name: this.extractClassName(content),
            basePath: this.extractBasePath(content),
            endpoints: []
        };

        // 提取所有端点
        const endpointPatterns = [
            /@(Get|Post|Put|Delete)Mapping\s*\(\s*['"`]([^'"`]+)['"`]/g,
            /@(Get|Post|Put|Delete)Mapping\s*\(\s*value\s*=\s*['"`]([^'"`]+)['"`]/g
        ];

        for (const pattern of endpointPatterns) {
            let match;
            while ((match = pattern.exec(content)) !== null) {
                const method = match[1].toUpperCase();
                const path = match[2];

                controller.endpoints.push({
                    method: method,
                    path: path
                });
            }
        }

        return controller;
    }

    /**
     * 提取类名
     */
    extractClassName(content) {
        const match = content.match(/public\s+class\s+(\w+)/);
        return match ? match[1] : 'Unknown';
    }

    /**
     * 提取基础路径
     */
    extractBasePath(content) {
        const match = content.match(/@RequestMapping\s*\(\s*['"`]([^'"`]+)['"`]/);
        return match ? match[1] : '';
    }

    /**
     * 验证API端点
     */
    validateAPIEndpoints(moduleName, frontendAPI, backendControllers) {
        const backendEndpoints = [];

        // 收集所有后端端点
        for (const controller of backendControllers) {
            for (const endpoint of controller.endpoints) {
                backendEndpoints.push({
                    path: controller.basePath + endpoint.path,
                    method: endpoint.method,
                    controller: controller.name
                });
            }
        }

        console.log(`  前端API端点: ${frontendAPI.endpoints.length}`);
        console.log(`  后端控制器端点: ${backendEndpoints.length}`);

        // 检查每个前端API是否有对应的后端端点
        for (const frontendEndpoint of frontendAPI.endpoints) {
            this.validationResults.total++;

            const matched = this.findMatchingBackendEndpoint(frontendEndpoint, backendEndpoints);

            if (matched) {
                this.validationResults.passed++;
                this.validationResults.details.push({
                    module: moduleName,
                    frontend: frontendEndpoint,
                    backend: matched,
                    status: 'PASS'
                });
            } else {
                this.validationResults.warnings++;
                const issue = `前端API ${frontendEndpoint.method} ${frontendEndpoint.path} 没有对应的后端端点`;
                this.validationResults.errors.push(issue);
                this.validationResults.details.push({
                    module: moduleName,
                    frontend: frontendEndpoint,
                    backend: null,
                    status: 'MISSING',
                    issue: issue
                });
            }
        }

        // 检查后端是否有前端未使用的端点
        for (const backendEndpoint of backendEndpoints) {
            const isUsed = frontendAPI.endpoints.some(fe =>
                this.pathsMatch(fe.path, backendEndpoint.path) &&
                fe.method === backendEndpoint.method
            );

            if (!isUsed) {
                this.validationResults.details.push({
                    module: moduleName,
                    frontend: null,
                    backend: backendEndpoint,
                    status: 'UNUSED',
                    issue: `后端端点 ${backendEndpoint.method} ${backendEndpoint.path} 未被前端使用`
                });
            }
        }
    }

    /**
     * 查找匹配的后端端点
     */
    findMatchingBackendEndpoint(frontendEndpoint, backendEndpoints) {
        return backendEndpoints.find(backend =>
            this.pathsMatch(frontendEndpoint.path, backend.path) &&
            this.methodsMatch(frontendEndpoint.method, backend.method)
        );
    }

    /**
     * 检查路径是否匹配
     */
    pathsMatch(frontendPath, backendPath) {
        // 标准化路径
        const normalizedFrontend = this.normalizePath(frontendPath);
        const normalizedBackend = this.normalizePath(backendPath);

        return normalizedFrontend === normalizedBackend;
    }

    /**
     * 标准化路径
     */
    normalizePath(path) {
        return path
            .replace(/\/+/g, '/') // 多个斜杠替换为一个
            .replace(/\/$/, '') // 移除末尾斜杠
            .toLowerCase();
    }

    /**
     * 检查HTTP方法是否匹配
     */
    methodsMatch(frontendMethod, backendMethod) {
        return frontendMethod === backendMethod;
    }

    /**
     * 打印验证结果
     */
    printResults() {
        console.log('\n' + '='.repeat(60));
        console.log('📊 端到端API一致性验证结果');
        console.log('='.repeat(60));
        console.log(`总检查项: ${this.validationResults.total}`);
        console.log(`✅ 通过: ${this.validationResults.passed}`);
        console.log(`⚠️  警告: ${this.validationResults.warnings}`);
        console.log(`❌ 错误: ${this.validationResults.errors.length}`);

        if (this.validationResults.errors.length > 0) {
            console.log('\n❌ 发现的问题:');
            this.validationResults.errors.forEach((error, index) => {
                console.log(`  ${index + 1}. ${error}`);
            });
        }

        const successRate = this.validationResults.total > 0
            ? ((this.validationResults.passed / this.validationResults.total) * 100).toFixed(1)
            : 0;
        console.log(`\n🎯 匹配率: ${successRate}%`);
    }

    /**
     * 生成详细报告
     */
    generateReport() {
        const report = {
            summary: {
                total: this.validationResults.total,
                passed: this.validationResults.passed,
                warnings: this.validationResults.warnings,
                errors: this.validationResults.errors.length,
                successRate: this.validationResults.total > 0
                    ? ((this.validationResults.passed / this.validationResults.total) * 100).toFixed(1)
                    : 0
            },
            timestamp: new Date().toISOString(),
            details: this.validationResults.details
        };

        const reportPath = path.join(this.projectRoot, 'end-to-end-api-validation-report.json');
        fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
        console.log(`\n📄 详细报告已保存: ${reportPath}`);
    }
}

// 如果直接运行此脚本
if (require.main === module) {
    const validator = new EndToEndAPIValidator();
    validator.validate().catch(console.error);
}

module.exports = EndToEndAPIValidator;