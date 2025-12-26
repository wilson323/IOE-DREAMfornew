#!/usr/bin/env node

/**
 * 数据模型契约验证工具
 * 用于验证前后端数据模型的一致性，确保API接口的请求和响应数据结构匹配
 *
 * 使用方法:
 * node scripts/data-model-contract-validator.js
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-01-30
 * @Version: 1.0.0
 */

const fs = require('fs');
const path = require('path');
const glob = require('glob');

class DataModelContractValidator {
    constructor() {
        this.frontendModels = new Map();
        this.backendModels = new Map();
        this.validationResults = [];
        this.apiContracts = new Map();
    }

    /**
     * 执行完整验证流程
     */
    async validate() {
        console.log('🚀 开始数据模型契约验证...');
        console.log('='.repeat(60));

        try {
            // 1. 加载前端数据模型
            await this.loadFrontendModels();

            // 2. 加载后端数据模型
            await this.loadBackendModels();

            // 3. 加载API契约
            await this.loadApiContracts();

            // 4. 执行验证
            this.performValidation();

            // 5. 生成报告
            this.generateReport();

        } catch (error) {
            console.error('❌ 验证过程中发生错误:', error.message);
            process.exit(1);
        }
    }

    /**
     * 加载前端数据模型
     */
    async loadFrontendModels() {
        console.log('📂 加载前端数据模型...');

        // 加载前端API定义
        const apiFiles = glob.sync('smart-admin-web-javascript/src/api/**/*.js');
        for (const file of apiFiles) {
            await this.parseFrontendApiFile(file);
        }

        // 加载前端表单和VO定义
        const modelFiles = glob.sync('smart-admin-web-javascript/src/**/*Form.vue');
        for (const file of modelFiles) {
            await this.parseFrontendModelFile(file);
        }

        console.log(`✅ 前端模型加载完成: ${this.frontendModels.size} 个模型`);
    }

    /**
     * 加载后端数据模型
     */
    async loadBackendModels() {
        console.log('📂 加载后端数据模型...');

        // 加载Java Form类
        const formFiles = glob.sync('microservices/**/src/main/java/**/*Form.java');
        for (const file of formFiles) {
            await this.parseJavaFormFile(file);
        }

        // 加载Java VO类
        const voFiles = glob.sync('microservices/**/src/main/java/**/*VO.java');
        for (const file of voFiles) {
            await this.parseJavaVOFile(file);
        }

        // 加载Java Entity类
        const entityFiles = glob.sync('microservices/**/src/main/java/**/*Entity.java');
        for (const file of entityFiles) {
            await this.parseJavaEntityFile(file);
        }

        console.log(`✅ 后端模型加载完成: ${this.backendModels.size} 个模型`);
    }

    /**
     * 加载API契约
     */
    async loadApiContracts() {
        console.log('📂 加载API契约...');

        // 加载后端Controller定义
        const controllerFiles = glob.sync('microservices/**/controller/*Controller.java');
        for (const file of controllerFiles) {
            await this.parseControllerFile(file);
        }

        console.log(`✅ API契约加载完成: ${this.apiContracts.size} 个接口`);
    }

    /**
     * 解析前端API文件
     */
    async parseFrontendApiFile(filePath) {
        try {
            const content = fs.readFileSync(filePath, 'utf8');
            const modelName = path.basename(filePath, '.js');

            // 提取API调用定义
            const apiMethods = this.extractApiMethods(content);

            if (apiMethods.length > 0) {
                this.frontendModels.set(modelName, {
                    type: 'api',
                    path: filePath,
                    methods: apiMethods
                });
            }
        } catch (error) {
            console.warn(`⚠️ 解析前端API文件失败: ${filePath} - ${error.message}`);
        }
    }

    /**
     * 解析前端模型文件
     */
    async parseFrontendModelFile(filePath) {
        try {
            const content = fs.readFileSync(filePath, 'utf8');
            const modelName = path.basename(filePath, '.vue');

            // 提取表单字段定义
            const formFields = this.extractVueFormFields(content);

            if (formFields.length > 0) {
                this.frontendModels.set(modelName, {
                    type: 'form',
                    path: filePath,
                    fields: formFields
                });
            }
        } catch (error) {
            console.warn(`⚠️ 解析前端模型文件失败: ${filePath} - ${error.message}`);
        }
    }

    /**
     * 解析Java Form文件
     */
    async parseJavaFormFile(filePath) {
        try {
            const content = fs.readFileSync(filePath, 'utf8');
            const className = this.extractClassName(content);

            if (className && className.endsWith('Form')) {
                const fields = this.extractJavaFields(content);
                const modelName = className.replace('Form', '');

                this.backendModels.set(modelName, {
                    type: 'form',
                    path: filePath,
                    className: className,
                    fields: fields
                });
            }
        } catch (error) {
            console.warn(`⚠️ 解析Java Form文件失败: ${filePath} - ${error.message}`);
        }
    }

    /**
     * 解析Java VO文件
     */
    async parseJavaVOFile(filePath) {
        try {
            const content = fs.readFileSync(filePath, 'utf8');
            const className = this.extractClassName(content);

            if (className && className.endsWith('VO')) {
                const fields = this.extractJavaFields(content);
                const modelName = className.replace('VO', '');

                this.backendModels.set(modelName, {
                    type: 'vo',
                    path: filePath,
                    className: className,
                    fields: fields
                });
            }
        } catch (error) {
            console.warn(`⚠️ 解析Java VO文件失败: ${filePath} - ${error.message}`);
        }
    }

    /**
     * 解析Java Entity文件
     */
    async parseJavaEntityFile(filePath) {
        try {
            const content = fs.readFileSync(filePath, 'utf8');
            const className = this.extractClassName(content);

            if (className && className.endsWith('Entity')) {
                const fields = this.extractJavaFields(content);
                const modelName = className.replace('Entity', '');

                this.backendModels.set(modelName, {
                    type: 'entity',
                    path: filePath,
                    className: className,
                    fields: fields
                });
            }
        } catch (error) {
            console.warn(`⚠️ 解析Java Entity文件失败: ${filePath} - ${error.message}`);
        }
    }

    /**
     * 解析Controller文件
     */
    async parseControllerFile(filePath) {
        try {
            const content = fs.readFileSync(filePath, 'utf8');
            const apiMethods = this.extractControllerMethods(content);

            if (apiMethods.length > 0) {
                const controllerName = path.basename(filePath, '.java');
                this.apiContracts.set(controllerName, {
                    path: filePath,
                    methods: apiMethods
                });
            }
        } catch (error) {
            console.warn(`⚠️ 解析Controller文件失败: ${filePath} - ${error.message}`);
        }
    }

    /**
     * 提取API方法定义
     */
    extractApiMethods(content) {
        const methods = [];

        // 匹配export const方法定义
        const methodRegex = /export\s+const\s+(\w+)\s*=\s*\([^)]*\)\s*=>\s*{\s*return\s+(request\.(get|post|put|delete))\s*\(\s*['"`]([^'"`]+)['"`]/g;
        let match;

        while ((match = methodRegex.exec(content)) !== null) {
            methods.push({
                name: match[1],
                httpMethod: match[3].toUpperCase(),
                url: match[4]
            });
        }

        return methods;
    }

    /**
     * 提取Vue表单字段
     */
    extractVueFormFields(content) {
        const fields = [];

        // 匹配表单字段定义
        const fieldRegex = /(\w+):\s*['"`]?([^'"`\s,}]+)['"`]?/g;
        let match;

        while ((match = fieldRegex.exec(content)) !== null) {
            if (!['data', 'methods', 'computed', 'watch'].includes(match[1])) {
                fields.push({
                    name: match[1],
                    type: this.inferType(match[2])
                });
            }
        }

        return fields;
    }

    /**
     * 提取Java类名
     */
    extractClassName(content) {
        const match = content.match(/public\s+class\s+(\w+)/);
        return match ? match[1] : null;
    }

    /**
     * 提取Java字段
     */
    extractJavaFields(content) {
        const fields = [];

        // 匹配字段定义
        const fieldRegex = /@(?:Column|TableField)\([^)]*\)\s*(?:public|private|protected)\s+(\w+(?:<[^>]+>)?)\s+(\w+)/g;
        let match;

        while ((match = fieldRegex.exec(content)) !== null) {
            fields.push({
                name: match[2],
                type: match[1]
            });
        }

        // 简单字段定义（没有注解的）
        const simpleFieldRegex = /(?:public|private|protected)\s+(\w+(?:<[^>]+>)?)\s+(\w+)/g;
        while ((match = simpleFieldRegex.exec(content)) !== null) {
            if (!fields.find(f => f.name === match[2])) {
                fields.push({
                    name: match[2],
                    type: match[1]
                });
            }
        }

        return fields;
    }

    /**
     * 提取Controller方法
     */
    extractControllerMethods(content) {
        const methods = [];

        // 匹配RequestMapping方法
        const methodRegex = /@(Get|Post|Put|Delete)Mapping\([^)]*\)\s*(?:public|private|protected)?\s*ResponseDTO<[^>]+>\s+(\w+)\s*\([^)]*\)/g;
        let match;

        while ((match = methodRegex.exec(content)) !== null) {
            methods.push({
                name: match[2],
                httpMethod: match[1].toUpperCase(),
                returnType: this.extractReturnType(content, match[2])
            });
        }

        return methods;
    }

    /**
     * 提取返回类型
     */
    extractReturnType(content, methodName) {
        const methodMatch = content.match(new RegExp(methodName + '\\s*\\([^)]*\\)\\s*:\\s*ResponseDTO<([^>]+)>'));
        return methodMatch ? methodMatch[1] : 'ResponseDTO';
    }

    /**
     * 推断类型
     */
    inferType(value) {
        if (value === 'true' || value === 'false') return 'Boolean';
        if (!isNaN(value) && value !== '') return 'Number';
        return 'String';
    }

    /**
     * 执行验证
     */
    performValidation() {
        console.log('🔍 执行数据模型验证...');

        // 验证API契约一致性
        this.validateApiContracts();

        // 验证Form模型一致性
        this.validateFormModels();

        // 验证VO模型一致性
        this.validateVOModels();

        console.log('✅ 验证完成');
    }

    /**
     * 验证API契约
     */
    validateApiContracts() {
        for (const [controllerName, controller] of this.apiContracts) {
            for (const method of controller.methods) {
                // 检查前端是否有对应的API调用
                const frontendApi = this.findFrontendApi(method);

                if (frontendApi) {
                    // 验证HTTP方法一致性
                    if (frontendApi.httpMethod !== method.httpMethod) {
                        this.addValidationResult('error',
                            `API方法不一致: ${method.name} - 前端使用${frontendApi.httpMethod}, 后端使用${method.httpMethod}`,
                            { frontend: frontendApi, backend: method }
                        );
                    }

                    // 验证URL路径一致性
                    if (!this.urlsMatch(frontendApi.url, method.returnType)) {
                        this.addValidationResult('warning',
                            `API路径可能不匹配: ${method.name} - 前端:${frontendApi.url}, 后端:${method.returnType}`,
                            { frontend: frontendApi, backend: method }
                        );
                    }
                } else {
                    this.addValidationResult('warning',
                        `前端缺少API调用: ${method.name} (${method.httpMethod})`,
                        { backend: method }
                    );
                }
            }
        }
    }

    /**
     * 验证Form模型
     */
    validateFormModels() {
        for (const [modelName, frontendModel] of this.frontendModels) {
            if (frontendModel.type === 'form') {
                const backendForm = this.backendModels.get(modelName);

                if (backendForm && backendForm.type === 'form') {
                    // 比较字段
                    this.compareFields(modelName, frontendModel.fields, backendForm.fields, 'Form');
                } else {
                    this.addValidationResult('warning',
                        `后端缺少Form模型: ${modelName}`,
                        { frontend: frontendModel }
                    );
                }
            }
        }
    }

    /**
     * 验证VO模型
     */
    validateVOModels() {
        for (const [modelName, backendModel] of this.backendModels) {
            if (backendModel.type === 'vo') {
                const frontendModel = this.frontendModels.get(modelName.toLowerCase());

                if (frontendModel) {
                    // 比较字段
                    this.compareFields(modelName, frontendModel.fields || [], backendModel.fields, 'VO');
                } else {
                    this.addValidationResult('info',
                        `前端可能缺少VO模型处理: ${modelName}`,
                        { backend: backendModel }
                    );
                }
            }
        }
    }

    /**
     * 比较字段
     */
    compareFields(modelName, frontendFields, backendFields, type) {
        const frontendFieldNames = new Set(frontendFields.map(f => f.name));
        const backendFieldNames = new Set(backendFields.map(f => f.name));

        // 检查前端缺少的字段
        for (const fieldName of backendFieldNames) {
            if (!frontendFieldNames.has(fieldName)) {
                const backendField = backendFields.find(f => f.name === fieldName);
                this.addValidationResult('warning',
                    `${modelName} ${type}: 前端缺少字段 '${fieldName}' (${backendField.type})`,
                    { backend: backendField }
                );
            }
        }

        // 检查后端缺少的字段
        for (const fieldName of frontendFieldNames) {
            if (!backendFieldNames.has(fieldName)) {
                const frontendField = frontendFields.find(f => f.name === fieldName);
                this.addValidationResult('warning',
                    `${modelName} ${type}: 后端缺少字段 '${fieldName}' (${frontendField.type})`,
                    { frontend: frontendField }
                );
            }
        }

        // 检查字段类型匹配
        for (const frontendField of frontendFields) {
            const backendField = backendFields.find(f => f.name === frontendField.name);
            if (backendField && !this.typesMatch(frontendField.type, backendField.type)) {
                this.addValidationResult('warning',
                    `${modelName} ${type}: 字段 '${frontendField.name}' 类型不匹配 - 前端:${frontendField.type}, 后端:${backendField.type}`,
                    { frontend: frontendField, backend: backendField }
                );
            }
        }
    }

    /**
     * 查找前端API
     */
    findFrontendApi(backendMethod) {
        for (const [modelName, frontendModel] of this.frontendModels) {
            if (frontendModel.type === 'api') {
                const apiMethod = frontendModel.methods.find(m =>
                    m.name.toLowerCase().includes(backendMethod.name.toLowerCase()) ||
                    backendMethod.name.toLowerCase().includes(m.name.toLowerCase())
                );
                if (apiMethod) {
                    return apiMethod;
                }
            }
        }
        return null;
    }

    /**
     * 检查URL是否匹配
     */
    urlsMatch(frontendUrl, backendUrl) {
        // 简化匹配逻辑，实际应该更精确
        return frontendUrl && backendUrl &&
               frontendUrl.includes('/api/') && backendUrl.includes('/api/');
    }

    /**
     * 检查类型是否匹配
     */
    typesMatch(frontendType, backendType) {
        const typeMapping = {
            'String': ['String', 'string'],
            'Number': ['Integer', 'Long', 'Double', 'BigDecimal', 'number'],
            'Boolean': ['Boolean', 'boolean'],
            'Date': ['LocalDate', 'LocalDateTime', 'Date', 'Date']
        };

        const compatibleTypes = typeMapping[frontendType] || [];
        return compatibleTypes.includes(backendType);
    }

    /**
     * 添加验证结果
     */
    addValidationResult(level, message, details = {}) {
        this.validationResults.push({
            level,
            message,
            details,
            timestamp: new Date().toISOString()
        });
    }

    /**
     * 生成验证报告
     */
    generateReport() {
        console.log('\n📊 数据模型契约验证报告');
        console.log('='.repeat(60));

        const errorCount = this.validationResults.filter(r => r.level === 'error').length;
        const warningCount = this.validationResults.filter(r => r.level === 'warning').length;
        const infoCount = this.validationResults.filter(r => r.level === 'info').length;

        console.log(`\n📈 验证统计:`);
        console.log(`   ❌ 错误: ${errorCount}`);
        console.log(`   ⚠️  警告: ${warningCount}`);
        console.log(`   ℹ️  信息: ${infoCount}`);
        console.log(`   📋 总计: ${this.validationResults.length}`);

        if (this.validationResults.length > 0) {
            console.log(`\n📝 详细结果:`);

            // 按严重程度排序
            const sortedResults = this.validationResults.sort((a, b) => {
                const levelOrder = { 'error': 0, 'warning': 1, 'info': 2 };
                return levelOrder[a.level] - levelOrder[b.level];
            });

            sortedResults.forEach((result, index) => {
                const icon = result.level === 'error' ? '❌' :
                           result.level === 'warning' ? '⚠️' : 'ℹ️';
                console.log(`\n${index + 1}. ${icon} [${result.level.toUpperCase()}] ${result.message}`);

                if (Object.keys(result.details).length > 0) {
                    console.log(`   详情: ${JSON.stringify(result.details, null, 2)}`);
                }
            });
        }

        // 生成报告文件
        this.saveReportToFile();

        console.log(`\n✨ 验证完成! ${errorCount === 0 ? '✅ 无错误' : '❌ 发现错误'}`);

        if (errorCount > 0) {
            process.exit(1);
        }
    }

    /**
     * 保存报告到文件
     */
    saveReportToFile() {
        const report = {
            timestamp: new Date().toISOString(),
            summary: {
                total: this.validationResults.length,
                errors: this.validationResults.filter(r => r.level === 'error').length,
                warnings: this.validationResults.filter(r => r.level === 'warning').length,
                info: this.validationResults.filter(r => r.level === 'info').length
            },
            models: {
                frontend: this.frontendModels.size,
                backend: this.backendModels.size,
                apiContracts: this.apiContracts.size
            },
            results: this.validationResults
        };

        const reportPath = 'data-model-contract-validation-report.json';
        fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
        console.log(`\n📄 详细报告已保存到: ${reportPath}`);
    }
}

// 执行验证
if (require.main === module) {
    const validator = new DataModelContractValidator();
    validator.validate().catch(error => {
        console.error('验证失败:', error);
        process.exit(1);
    });
}

module.exports = DataModelContractValidator;