#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修复ReportServiceImpl.java文件中的乱码
"""
import re
import sys

def fix_garbled_text(content):
    """修复文件中的乱码字符"""

    # 常见的乱码模式替换（基于已识别的模式）
    replacements = {
        # 类和方法注释
        r'闂備胶顢婄紙浼村磿閹绢喖违闁告劦鍠栫€氬.*?殽\?': '报表服务实现类',
        r'濠电偞鍨堕幐鍫曞磿.*?磹\?': '提供消费报表生成、数据统计、Excel导出等功能',

        # 日志消息
        r'闂備焦鐪归崹濠氬窗閹版澘鍨傛慨姗嗗厴濡插牓鏌涢.*?梺\?': '开始生成消费报表',
        r'闂佽崵鍠愰悷杈╁緤妤ｅ啯鍊靛ù鐘差儏閻.*?婵\?': '解析参数',
        r'闂備礁鎼.*?鑲┾偓\?': '构建查询条件',
        r'闂佽崵濮崇欢銈囨.*?閹\?': '统计数据',
        r'闂備礁婀遍.*?鐛.*?埀\?': '按时间维度分组',
        r'闂備礁鎼.*?鍛.*?偓.*?妶\?': '构建报表结果',

        # 错误消息
        r'闂備焦鐪归崹濠氬窗閹版澘鍨傛慨姗嗗厴濡插牓鏌涢.*?鍎\?': '生成消费报表失败',

        # 其他常见乱码模式
        r'婵犵數鍋.*?闁\?': '消费',
        r'闂備胶顢婄紙.*?閹\?': '报表',
        r'闂備礁.*?鏌\?': '查询',
        r'闂佽.*?婵\?': '参数',
    }

    # 执行替换
    for pattern, replacement in replacements.items():
        content = re.sub(pattern, replacement, content, flags=re.DOTALL)

    return content

if __name__ == '__main__':
    file_path = 'src/main/java/net/lab1024/sa/consume/service/impl/ReportServiceImpl.java'

    try:
        # 读取文件
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()

        # 修复乱码
        fixed_content = fix_garbled_text(content)

        # 写回文件
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(fixed_content)

        print(f"Fixed encoding issues in {file_path}")

    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

