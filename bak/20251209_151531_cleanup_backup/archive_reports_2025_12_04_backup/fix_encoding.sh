#!/bin/bash

# 修复Java文件中的UTF-8编码问题
# 主要修复中文字符被截断的问题（例如：控制�? -> 控制器）

echo "开始修复Java文件中的UTF-8编码问题..."
echo "========================================="

# 计数器
total_files=0
fixed_files=0

# 查找所有包含截断中文字符的Java文件
# 模式：中文字符后跟非字符、非空格、非中文的字符（通常是编码错误的标志）
while IFS= read -r -d '' file; do
    ((total_files++))

    # 检查文件是否包含需要修复的编码问题
    if grep -qP '[\u4e00-\u9fff][^\w\s\u4e00-\u9fff]' "$file"; then
        echo "正在修复文件: $file"

        # 创建临时文件
        temp_file=$(mktemp)

        # 使用iconv将文件从UTF-8转换为UTF-8，这会清理编码问题
        # 然后使用sed修复常见的截断字符
        iconv -f UTF-8 -t UTF-8 "$file" | \
        sed -e 's/控制\?[[:space:]]*$/控制器/g' \
            -e 's/规范\?[[:space:]]*$/规范/g' \
            -e 's/服务\?[[:space:]]*$/服务/g' \
            -e 's/管理\?[[:space:]]*$/管理/g' \
            -e 's/系统\?[[:space:]]*$/系统/g' \
            -e 's/数据\?[[:space:]]*$/数据/g' \
            -e 's/接口\?[[:space:]]*$/接口/g' \
            -e 's/模块\?[[:space:]]*$/模块/g' \
            -e 's/配置\?[[:space:]]*$/配置/g' \
            -e 's/实现\?[[:space:]]*$/实现/g' \
            -e 's/查询\?[[:space:]]*$/查询/g' \
            -e 's/操作\?[[:space:]]*$/操作/g' \
            -e 's/记录\?[[:space:]]*$/记录/g' \
            -e 's/用户\?[[:space:]]*$/用户/g' \
            -e 's/登录\?[[:space:]]*$/登录/g' \
            -e 's/权限\?[[:space:]]*$/权限/g' \
            -e 's/验证\?[[:space:]]*$/验证/g' \
            -e 's/格式\?[[:space:]]*$/格式/g' \
            -e 's/统一\?[[:space:]]*$/统一/g' \
            -e 's/完整\?[[:space:]]*$/完整/g' \
            -e 's/返回\?[[:space:]]*$/返回/g' \
            -e 's/请求\?[[:space:]]*$/请求/g' \
            -e 's/响应\?[[:space:]]*$/响应/g' \
            -e 's/处理\?[[:space:]]*$/处理/g' \
            -e 's/方法\?[[:space:]]*$/方法/g' \
            -e 's/参数\?[[:space:]]*$/参数/g' \
            -e 's/结果\?[[:space:]]*$/结果/g' \
            -e 's/类型\?[[:space:]]*$/类型/g' \
            -e 's/状态\?[[:space:]]*$/状态/g' \
            -e 's/信息\?[[:space:]]*$/信息/g' \
            -e 's/实体\?[[:space:]]*$/实体/g' \
            -e 's/对象\?[[:space:]]*$/对象/g' \
            -e 's/集合\?[[:space:]]*$/集合/g' \
            -e 's/列表\?[[:space:]]*$/列表/g' \
            -e 's/页面\?[[:space:]]*$/页面/g' \
            -e 's/表单\?[[:space:]]*$/表单/g' \
            -e 's/组件\?[[:space:]]*$/组件/g' \
            -e 's/工具\?[[:space:]]*$/工具/g' \
            -e 's/类\?[[:space:]]*$/类/g' \
            -e 's/异常\?[[:space:]]*$/异常/g' \
            -e 's/错误\?[[:space:]]*$/错误/g' \
            -e 's/测试\?[[:space:]]*$/测试/g' \
            -e 's/注解\?[[:space:]]*$/注解/g' \
            -e 's/枚举\?[[:space:]]*$/枚举/g' \
            -e 's/缓存\?[[:space:]]*$/缓存/g' \
            -e 's/会话\?[[:space:]]*$/会话/g' \
            -e 's/事务\?[[:space:]]*$/事务/g' \
            -e 's/事务\?[[:space:]]*$/事务管理/g' \
            -e 's/日志\?[[:space:]]*$/日志/g' \
            -e 's/监控\?[[:space:]]*$/监控/g' \
            -e 's/统计\?[[:space:]]*$/统计/g' \
            -e 's/分析\?[[:space:]]*$/分析/g' \
            -e 's/优化\?[[:space:]]*$/优化/g' \
            -e 's/性能\?[[:space:]]*$/性能/g' \
            -e 's/安全\?[[:space:]]*$/安全/g' \
            -e 's/加密\?[[:space:]]*$/加密/g' \
            -e 's/解密\?[[:space:]]*$/解密/g' \
            -e 's/认证\?[[:space:]]*$/认证/g' \
            -e 's/授权\?[[:space:]]*$/授权/g' \
            -e 's/审计\?[[:space:]]*$/审计/g' \
            -e 's/备份\?[[:space:]]*$/备份/g' \
            -e 's/恢复\?[[:space:]]*$/恢复/g' \
            -e 's/同步\?[[:space:]]*$/同步/g' \
            -e 's/异步\?[[:space:]]*$/异步/g' \
            -e 's/队列\?[[:space:]]*$/队列/g' \
            -e 's/消息\?[[:space:]]*$/消息/g' \
            -e 's/事件\?[[:space:]]*$/事件/g' \
            -e 's/通知\?[[:space:]]*$/通知/g' \
            -e 's/邮件\?[[:space:]]*$/邮件/g' \
            -e 's/短信\?[[:space:]]*$/短信/g' \
            -e 's/推送\?[[:space:]]*$/推送/g' \
            -e 's/调度\?[[:space:]]*$/调度/g' \
            -e 's/定时\?[[:space:]]*$/定时/g' \
            -e 's/计划\?[[:space:]]*$/计划/g' \
            -e 's/任务\?[[:space:]]*$/任务/g' \
            -e 's/作业\?[[:space:]]*$/作业/g' \
            -e 's/流程\?[[:space:]]*$/流程/g' \
            -e 's/引擎\?[[:space:]]*$/引擎/g' \
            -e 's/规则\?[[:space:]]*$/规则/g' \
            -e 's/策略\?[[:space:]]*$/策略/g' \
            -e 's/算法\?[[:space:]]*$/算法/g' \
            -e 's/模板\?[[:space:]]*$/模板/g' \
            -e 's/生成\?[[:space:]]*$/生成/g' \
            -e 's/解析\?[[:space:]]*$/解析/g' \
            -e 's/转换\?[[:space:]]*$/转换/g' \
            -e 's/映射\?[[:space:]]*$/映射/g' \
            -e 's/绑定\?[[:space:]]*$/绑定/g' \
            -e 's/注入\?[[:space:]]*$/注入/g' \
            -e 's/依赖\?[[:space:]]*$/依赖/g' \
            -e 's/组件\?[[:space:]]*$/组件/g' \
            -e 's/框架\?[[:space:]]*$/框架/g' \
            -e 's/应用\?[[:space:]]*$/应用/g' \
            -e 's/程序\?[[:space:]]*$/程序/g' \
            -e 's/代码\?[[:space:]]*$/代码/g' \
            -e 's/项目\?[[:space:]]*$/项目/g' \
            -e 's/模块\?[[:space:]]*$/模块/g' \
            -e 's/包\?[[:space:]]*$/包/g' \
            -e 's/库\?[[:space:]]*$/库/g' \
            -e 's/版本\?[[:space:]]*$/版本/g' \
            -e 's/更新\?[[:space:]]*$/更新/g' \
            -e 's/升级\?[[:space:]]*$/升级/g' \
            -e 's/部署\?[[:space:]]*$/部署/g' \
            -e 's/发布\?[[:space:]]*$/发布/g' \
            -e 's/运行\?[[:space:]]*$/运行/g' \
            -e 's/启动\?[[:space:]]*$/启动/g' \
            -e 's/停止\?[[:space:]]*$/停止/g' \
            -e 's/重启\?[[:space:]]*$/重启/g' \
            -e 's/关闭\?[[:space:]]*$/关闭/g' \
            -e 's/开启\?[[:space:]]*$/开启/g' \
            -e 's/启用\?[[:space:]]*$/启用/g' \
            -e 's/禁用\?[[:space:]]*$/禁用/g' \
            -e 's/激活\?[[:space:]]*$/激活/g' \
            -e 's/停用\?[[:space:]]*$/停用/g' \
            -e 's/删除\?[[:space:]]*$/删除/g' \
            -e 's/添加\?[[:space:]]*$/添加/g' \
            -e 's/编辑\?[[:space:]]*$/编辑/g' \
            -e 's/修改\?[[:space:]]*$/修改/g' \
            -e 's/保存\?[[:space:]]*$/保存/g' \
            -e 's/提交\?[[:space:]]*$/提交/g' \
            -e 's/取消\?[[:space:]]*$/取消/g' \
            -e 's/重置\?[[:space:]]*$/重置/g' \
            -e 's/清空\?[[:space:]]*$/清空/g' \
            -e 's/刷新\?[[:space:]]*$/刷新/g' \
            -e 's/重新\?[[:space:]]*$/重新/g' \
            -e 's/再次\?[[:space:]]*$/再次/g' \
            -e 's/继续\?[[:space:]]*$/继续/g' \
            -e 's/完成\?[[:space:]]*$/完成/g' \
            -e 's/结束\?[[:space:]]*$/结束/g' \
            -e 's/开始\?[[:space:]]*$/开始/g' \
            -e 's/创建\?[[:space:]]*$/创建/g' \
            -e 's/新建\?[[:space:]]*$/新建/g' \
            -e 's/复制\?[[:space:]]*$/复制/g' \
            -e 's/粘贴\?[[:space:]]*$/粘贴/g' \
            -e 's/剪切\?[[:space:]]*$/剪切/g' \
            -e 's/选择\?[[:space:]]*$/选择/g' \
            -e 's/全选\?[[:space:]]*$/全选/g' \
            -e 's/搜索\?[[:space:]]*$/搜索/g' \
            -e 's/查找\?[[:space:]]*$/查找/g' \
            -e 's/替换\?[[:space:]]*$/替换/g' \
            -e 's/过滤\?[[:space:]]*$/过滤/g' \
            -e 's/排序\?[[:space:]]*$/排序/g' \
            -e 's/分页\?[[:space:]]*$/分页/g' \
            -e 's/导出\?[[:space:]]*$/导出/g' \
            -e 's/导入\?[[:space:]]*$/导入/g' \
            -e 's/下载\?[[:space:]]*$/下载/g' \
            -e 's/上传\?[[:space:]]*$/上传/g' \
            -e 's/浏览\?[[:space:]]*$/浏览/g' \
            -e 's/预览\?[[:space:]]*$/预览/g' \
            -e 's/打印\?[[:space:]]*$/打印/g' \
            -e 's/设置\?[[:space:]]*$/设置/g' \
            -e 's/配置\?[[:space:]]*$/配置/g' \
            -e 's/选项\?[[:space:]]*$/选项/g' \
            -e 's/参数\?[[:space:]]*$/参数/g' \
            -e 's/属性\?[[:space:]]*$/属性/g' \
            -e 's/特性\?[[:space:]]*$/特性/g' \
            -e 's/功能\?[[:space:]]*$/功能/g' \
            -e 's/作用\?[[:space:]]*$/作用/g' \
            -e 's/目的\?[[:space:]]*$/目的/g' \
            -e 's/用途\?[[:space:]]*$/用途/g' \
            -e 's/说明\?[[:space:]]*$/说明/g' \
            -e 's/描述\?[[:space:]]*$/描述/g' \
            -e 's/内容\?[[:space:]]*$/内容/g' \
            -e 's/详细\?[[:space:]]*$/详细/g' \
            -e 's/简介\?[[:space:]]*$/简介/g' \
            -e 's/摘要\?[[:space:]]*$/摘要/g' \
            -e 's/总结\?[[:space:]]*$/总结/g' \
            -e 's/概述\?[[:space:]]*$/概述/g' \
            -e 's/简介\?[[:space:]]*$/简介/g' \
            -e 's/介绍\?[[:space:]]*$/介绍/g' \
            -e 's/标题\?[[:space:]]*$/标题/g' \
            -e 's/名称\?[[:space:]]*$/名称/g' \
            -e 's/标识\?[[:space:]]*$/标识/g' \
            -e 's/编号\?[[:space:]]*$/编号/g' \
            -e 's/ID\?[[:space:]]*$/ID/g' \
            -e 's/ID\?[[:space:]]*$/标识符/g' \
            -e 's/编码\?[[:space:]]*$/编码/g' \
            -e 's/解码\?[[:space:]]*$/解码/g' \
            -e 's/字符\?[[:space:]]*$/字符/g' \
            -e 's/字符串\?[[:space:]]*$/字符串/g' \
            -e 's/数字\?[[:space:]]*$/数字/g' \
            -e 's/数值\?[[:space:]]*$/数值/g' \
            -e 's/整数\?[[:space:]]*$/整数/g' \
            -e 's/小数\?[[:space:]]*$/小数/g' \
            -e 's/浮点\?[[:space:]]*$/浮点/g' \
            -e 's/布尔\?[[:space:]]*$/布尔/g' \
            -e 's/日期\?[[:space:]]*$/日期/g' \
            -e 's/时间\?[[:space:]]*$/时间/g' \
            -e 's/时间\?[[:space:]]*$/时间戳/g' \
            -e 's/格式\?[[:space:]]*$/格式化/g' \
            -e 's/解析\?[[:space:]]*$/解析器/g' \
            -e 's/验证\?[[:space:]]*$/验证器/g' \
            -e 's/检查\?[[:space:]]*$/检查/g' \
            -e 's/校验\?[[:space:]]*$/校验/g' \
            -e 's/测试\?[[:space:]]*$/测试/g' \
            -e 's/调试\?[[:space:]]*$/调试/g' \
            -e 's/开发\?[[:space:]]*$/开发/g' \
            -e 's/设计\?[[:space:]]*$/设计/g' \
            -e 's/架构\?[[:space:]]*$/架构/g' \
            -e 's/模式\?[[:space:]]*$/模式/g' \
            -e 's/规范\?[[:space:]]*$/规范/g' \
            -e 's/标准\?[[:space:]]*$/标准/g' \
            -e 's/协议\?[[:space:]]*$/协议/g' \
            -e 's/接口\?[[:space:]]*$/接口/g' \
            -e 's/API\?[[:space:]]*$/API/g' \
            -e 's/HTTP\?[[:space:]]*$/HTTP/g' \
            -e 's/HTTPS\?[[:space:]]*$/HTTPS/g' \
            -e 's/REST\?[[:space:]]*$/REST/g' \
            -e 's/JSON\?[[:space:]]*$/JSON/g' \
            -e 's/XML\?[[:space:]]*$/XML/g' \
            -e 's/YAML\?[[:space:]]*$/YAML/g' \
            -e 's/数据库\?[[:space:]]*$/数据库/g' \
            -e 's/表\?[[:space:]]*$/表/g' \
            -e 's/字段\?[[:space:]]*$/字段/g' \
            -e 's/索引\?[[:space:]]*$/索引/g' \
            -e 's/主键\?[[:space:]]*$/主键/g' \
            -e 's/外键\?[[:space:]]*$/外键/g' \
            -e 's/关联\?[[:space:]]*$/关联/g' \
            -e 's/关系\?[[:space:]]*$/关系/g' \
            -e 's/查询\?[[:space:]]*$/查询/g' \
            -e 's/条件\?[[:space:]]*$/条件/g' \
            -e 's/排序\?[[:space:]]*$/排序/g' \
            -e 's/分组\?[[:space:]]*$/分组/g' \
            -e 's/聚合\?[[:space:]]*$/聚合/g' \
            -e 's/统计\?[[:space:]]*$/统计/g' \
            -e 's/总计\?[[:space:]]*$/总计/g' \
            -e 's/平均值\?[[:space:]]*$/平均值/g' \
            -e 's/最大值\?[[:space:]]*$/最大值/g' \
            -e 's/最小值\?[[:space:]]*$/最小值/g' \
            -e 's/计数\?[[:space:]]*$/计数/g' \
            -e 's/求和\?[[:space:]]*$/求和/g' \
            > "$temp_file"

        # 如果修复成功，替换原文件
        if [ $? -eq 0 ]; then
            mv "$temp_file" "$file"
            ((fixed_files++))
            echo "✓ 已修复: $file"
        else
            rm -f "$temp_file"
            echo "✗ 修复失败: $file"
        fi
    fi
done < <(find . -name "*.java" -type f -print0)

echo "========================================="
echo "修复完成！"
echo "总文件数: $total_files"
echo "已修复文件数: $fixed_files"
echo "剩余未修复文件数: $((total_files - fixed_files))"

if [ $fixed_files -gt 0 ]; then
    echo ""
    echo "建议执行以下命令验证修复结果："
    echo "git status"
    echo "git diff --stat"
fi