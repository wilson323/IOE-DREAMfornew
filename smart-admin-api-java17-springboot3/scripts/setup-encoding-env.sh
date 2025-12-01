#!/bin/bash
# 编码环境设置脚本 - 确保所有工具使用UTF-8编码
echo "🔧 设置编码环境..."

export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"
export MAVEN_OPTS="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
export LANG="zh_CN.UTF-8"
export LC_ALL="zh_CN.UTF-8"
export LESSCHARSET="utf-8"

echo "编码环境设置完成:"
echo "  JAVA_TOOL_OPTIONS: $JAVA_TOOL_OPTIONS"
echo "  MAVEN_OPTS: $MAVEN_OPTS"
echo "  LANG: $LANG"
echo "  LC_ALL: $LC_ALL"

echo "编码环境设置完成！"
