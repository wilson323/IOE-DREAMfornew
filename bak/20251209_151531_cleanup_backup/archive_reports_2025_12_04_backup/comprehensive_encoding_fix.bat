@echo off
chcp 65001 > nul
echo 开始全面修复Java文件UTF-8编码问题...
echo ========================================

:: 使用PowerShell进行更精确的编码修复
powershell -Command "& {
    $encodingFixes = @{
        '管理', '管理员';
        '验证', '验证码';
        '验证', '验证器';
        '验证', '验证信息';
        '编码', '编码问题';
        '乱码', '乱码字符';
        '字符', '字符串';
        '格式', '格式化';
        '解析', '解析器';
        '转换', '转换器';
        '映射', '映射关系';
        '绑定', '绑定器';
        '注入', '注入器';
        '依赖', '依赖注入';
        '组件', '组件化';
        '框架', '框架级';
        '应用', '应用程序';
        '程序', '程序化';
        '代码', '代码化';
        '项目', '项目级';
        '模块', '模块化';
        '版本', '版本号';
        '更新', '更新化';
        '升级', '升级版';
        '部署', '部署化';
        '发布', '发布版';
        '运行', '运行时';
        '启动', '启动器';
        '停止', '停止器';
        '重启', '重启器';
        '关闭', '关闭器';
        '开启', '开启器';
        '启用', '启用器';
        '禁用', '禁用器';
        '激活', '激活器';
        '停用', '停用器';
        '删除', '删除器';
        '添加', '添加器';
        '编辑', '编辑器';
        '修改', '修改器';
        '保存', '保存器';
        '提交', '提交器';
        '取消', '取消器';
        '重置', '重置器';
        '清空', '清空器';
        '刷新', '刷新器';
        '重新', '重新化';
        '再次', '再次化';
        '继续', '继续器';
        '完成', '完成器';
        '结束', '结束器';
        '开始', '开始器';
        '创建', '创建器';
        '新建', '新建器';
        '复制', '复制器';
        '粘贴', '粘贴器';
        '剪切', '剪切器';
        '选择', '选择器';
        '全选', '全选器';
        '搜索', '搜索器';
        '查找', '查找器';
        '替换', '替换器';
        '过滤', '过滤器';
        '排序', '排序器';
        '分页', '分页器';
        '导出', '导出器';
        '导入', '导入器';
        '下载', '下载器';
        '上传', '上传器';
        '浏览', '浏览器';
        '预览', '预览器';
        '打印', '打印机';
        '设置', '设置器';
        '配置', '配置器';
        '选项', '选项器';
        '参数', '参数化';
        '属性', '属性化';
        '特性', '特性化';
        '功能', '功能化';
        '作用', '作用力';
        '目的', '目的性';
        '用途', '用途化';
        '说明', '说明书';
        '描述', '描述性';
        '内容', '内容化';
        '详细', '详细化';
        '简介', '简介版';
        '摘要', '摘要版';
        '总结', '总结版';
        '概述', '概述版';
        '介绍', '介绍版';
        '标题', '标题栏';
        '名称', '名称化';
        '标识', '标识符';
        '编号', '编号器';
        '编码', '编码器';
        '解码', '解码器';
    }

    $totalFiles = 0
    $fixedFiles = 0
    $totalFixes = 0

    Get-ChildItem -Path . -Filter '*.java' -Recurse | ForEach-Object {
        $totalFiles++
        $file = $_
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8

        $originalContent = $content
        $fixCount = 0

        # 应用所有修复规则
        foreach ($fix in $encodingFixes.GetEnumerator()) {
            if ($content -match [regex]::Escape($fix.Key)) {
                $content = $content -replace [regex]::Escape($fix.Key), $fix.Value
                $fixCount += ([regex]::Matches($originalContent, [regex]::Escape($fix.Key))).Count
            }
        }

        # 通用修复：中文字符后跟非字符数字空格中文的字符
        $content = $content -replace '([\u4e00-\u9fff])([^\w\s\u4e00-\u9fff])', '$1'

        # 如果有修复，写回文件
        if ($content -ne $originalContent) {
            Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
            $fixedFiles++
            $totalFixes += $fixCount
            Write-Host "[FIXED] $($file.FullName) (修复 $fixCount 处)" -ForegroundColor Green
        }
    }

    Write-Host '=========================================' -ForegroundColor Yellow
    Write-Host '修复完成！' -ForegroundColor Green
    Write-Host "总文件数: $totalFiles" -ForegroundColor Cyan
    Write-Host "已修复文件数: $fixedFiles" -ForegroundColor Cyan
    Write-Host "总修复处数: $totalFixes" -ForegroundColor Cyan
}"

echo.
echo 建议执行以下命令验证修复结果:
echo git status
echo git diff --stat
pause