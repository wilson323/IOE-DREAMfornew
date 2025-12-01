#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

/**
 * 项目质量门禁检查工具
 * 确保所有代码符合技术规范要求
 */

console.log('🔍 开始质量门禁检查...\n');

let hasErrors = false;
let errorCount = 0;
let warningCount = 0;

// 检查规则配置
const rules = {
  vue: [
    {
      name: 'v-model-prop检查',
      pattern: /v-model:(open|visible|checked)="[^"]*"/g,
      message: '检测到直接在prop上使用v-model，请使用:prop和@update:prop事件',
      severity: 'error',
      files: ['**/*.vue']
    },
    {
      name: 'TypeScript类型注解检查',
      pattern: /const\s+\w+\s*:\s*\w+.*=/g,
      message: '在JavaScript文件中检测到TypeScript类型注解，请移除或转换为TypeScript',
      severity: 'error',
      files: ['**/*.vue', '**/*.js']
    },
    {
      name: '变量重复声明检查',
      pattern: /const\s+(\w+)\s*=/g,
      message: '检测到可能的变量重复声明',
      severity: 'warning',
      files: ['**/*.vue', '**/*.js'],
      customCheck: (matches, filePath) => {
        const variableNames = {};
        matches.forEach(match => {
          const varName = match[1];
          if (variableNames[varName]) {
            return `变量"${varName}"重复声明`;
          }
          variableNames[varName] = true;
        });
        return null;
      }
    }
  ],

  javascript: [
    {
      name: 'console.log检查',
      pattern: /console\.log/g,
      message: '检测到console.log，请使用专业的日志工具',
      severity: 'warning',
      files: ['**/*.js', '**/*.vue']
    },
    {
      name: '硬编码配置检查',
      pattern: /(localhost|127\.0\.0\.1|http:\/\/[^\/]+\/api)/g,
      message: '检测到硬编码的API地址，请使用环境配置',
      severity: 'error',
      files: ['**/*.js', '**/*.vue', '**/*.ts']
    }
  ],

  typescript: [
    {
      name: 'any类型检查',
      pattern: /:\s*any\b/g,
      message: '避免使用any类型，建议使用具体类型',
      severity: 'warning',
      files: ['**/*.ts']
    }
  ]
};

// 查找文件函数
function findFiles(pattern, exclude = []) {
  const results = [];

  function searchDirectory(dir, depth = 0) {
    if (depth > 5) return; // 避免递归过深

    try {
      const items = fs.readdirSync(dir);
      for (const item of items) {
        const fullPath = path.join(dir, item);
        const stat = fs.statSync(fullPath);

        if (stat.isDirectory() && !exclude.includes(item)) {
          searchDirectory(fullPath, depth + 1);
        } else if (stat.isFile()) {
          // 简单的文件匹配
          if (pattern.some(p => fullPath.endsWith(p.replace('**/', '')))) {
            results.push(fullPath);
          }
        }
      }
    } catch (error) {
      // 忽略权限错误
    }
  }

  const searchPaths = ['src', 'smart-admin-web-javascript/src'];
  searchPaths.forEach(searchPath => {
    if (fs.existsSync(searchPath)) {
      searchDirectory(searchPath);
    }
  });

  return results;
}

// 检查单个文件
function checkFile(filePath, rule) {
  try {
    const content = fs.readFileSync(filePath, 'utf8');
    const matches = content.match(rule.pattern);

    if (matches) {
      if (rule.customCheck) {
        const customError = rule.customCheck(matches, filePath);
        if (customError) {
          reportError(filePath, rule.name, customError, rule.severity);
          return true;
        }
      } else {
        reportError(filePath, rule.name, rule.message, rule.severity);
        return true;
      }
    }
  } catch (error) {
    console.error(`❌ 检查文件失败: ${filePath} - ${error.message}`);
    return false;
  }

  return false;
}

// 报告错误
function reportError(filePath, ruleName, message, severity) {
  const relativePath = path.relative(process.cwd(), filePath);
  const icon = severity === 'error' ? '❌' : '⚠️';

  console.log(`${icon} ${ruleName}`);
  console.log(`   文件: ${relativePath}`);
  console.log(`   说明: ${message}`);
  console.log('');

  if (severity === 'error') {
    hasErrors = true;
    errorCount++;
  } else {
    warningCount++;
  }
}

// 检查项目结构
function checkProjectStructure() {
  console.log('📁 检查项目结构...');

  const requiredDirs = [
    'src',
    'src/api',
    'src/components',
    'src/utils',
    'docs'
  ];

  requiredDirs.forEach(dir => {
    if (fs.existsSync(dir)) {
      console.log(`✅ ${dir}/ 存在`);
    } else {
      console.log(`❌ ${dir}/ 缺失`);
      hasErrors = true;
      errorCount++;
    }
  });

  console.log('');
}

// 检查配置文件
function checkConfigFiles() {
  console.log('⚙️ 检查配置文件...');

  const configFiles = [
    'package.json',
    'vite.config.js',
    '.eslintrc.cjs',
    'tsconfig.json'
  ];

  configFiles.forEach(file => {
    if (fs.existsSync(file)) {
      console.log(`✅ ${file} 存在`);

      // 检查package.json的依赖安全性
      if (file === 'package.json') {
        try {
          const packageJson = JSON.parse(fs.readFileSync(file, 'utf8'));
          const vulnerabilities = checkPackageDependencies(packageJson);
          if (vulnerabilities.length > 0) {
            console.log(`⚠️ 发现${vulnerabilities.length}个依赖安全警告`);
            vulnerabilities.forEach(vuln => {
              console.log(`   - ${vuln}`);
            });
            warningCount += vulnerabilities.length;
          }
        } catch (error) {
          console.log(`❌ 无法解析package.json: ${error.message}`);
        }
      }
    } else {
      console.log(`⚠️ ${file} 不存在`);
      warningCount++;
    }
  });

  console.log('');
}

// 检查依赖安全性
function checkPackageDependencies(packageJson) {
  const vulnerabilities = [];

  // 检查过时的依赖
  if (packageJson.dependencies) {
    Object.entries(packageJson.dependencies).forEach(([name, version]) => {
      if (version.startsWith('^0.') || version.startsWith('~0.')) {
        vulnerabilities.push(`${name}@${version} - 使用0.x版本可能不稳定`);
      }
    });
  }

  return vulnerabilities;
}

// 生成质量报告
function generateReport() {
  const report = {
    timestamp: new Date().toISOString(),
    result: hasErrors ? 'FAILED' : 'PASSED',
    summary: {
      errors: errorCount,
      warnings: warningCount,
      totalIssues: errorCount + warningCount
    },
    recommendations: []
  };

  if (hasErrors) {
    report.recommendations.push(
      '请修复所有错误后再次提交',
      '建议运行ESLint和Prettier进行代码格式化',
      '确保所有Vue组件遵循Composition API规范'
    );
  }

  if (warningCount > 0) {
    report.recommendations.push(
      '建议修复警告以提高代码质量',
      '考虑添加更多的单元测试覆盖'
    );
  }

  fs.writeFileSync('quality-report.json', JSON.stringify(report, null, 2));

  console.log('📊 质量报告已生成: quality-report.json');
  return report;
}

// 主检查函数
function runQualityGate() {
  // 1. 检查项目结构
  checkProjectStructure();

  // 2. 检查配置文件
  checkConfigFiles();

  // 3. 检查代码质量
  console.log('🔍 检查代码质量...');

  Object.entries(rules).forEach(([category, categoryRules]) => {
    console.log(`\n📝 检查${category}规则:`);

    categoryRules.forEach(rule => {
      const files = findFiles(rule.files);
      console.log(`   检查规则: ${rule.name} (${files.length}个文件)`);

      files.forEach(filePath => {
        checkFile(filePath, rule);
      });
    });
  });

  // 4. 生成报告
  const report = generateReport();

  // 5. 输出结果
  console.log('\n' + '='.repeat(50));
  console.log('🏁 质量门禁检查结果');
  console.log('='.repeat(50));
  console.log(`状态: ${report.result}`);
  console.log(`错误: ${report.summary.errors}`);
  console.log(`警告: ${report.summary.warnings}`);
  console.log(`总计: ${report.summary.totalIssues}`);

  if (report.recommendations.length > 0) {
    console.log('\n💡 建议:');
    report.recommendations.forEach((rec, index) => {
      console.log(`${index + 1}. ${rec}`);
    });
  }

  return report;
}

// 执行质量检查
const report = runQualityGate();

// 退出码
process.exit(hasErrors ? 1 : 0);