#!/bin/bash
# 解决Maven编译中文乱码问题的专用脚本 (Linux/Mac版本)
# 设置UTF-8编码环境变量，确保中文错误信息正确显示

echo "==============================================="
echo "Maven编译专用脚本 - 解决中文乱码问题"
echo "==============================================="

# 设置Java系统属性，强制UTF-8编码
export JAVA_OPTS="-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.country=CN -Duser.timezone=Asia/Shanghai"
export MAVEN_OPTS="-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.country=CN -Duser.timezone=Asia/Shanghai"

# 设置本地化环境变量
export LANG=zh_CN.UTF-8
export LC_ALL=zh_CN.UTF-8
export LANGUAGE=zh_CN.UTF-8

echo "环境变量设置完成："
echo "  JAVA_OPTS=$JAVA_OPTS"
echo "  MAVEN_OPTS=$MAVEN_OPTS"
echo "  LANG=$LANG"
echo "  LC_ALL=$LC_ALL"
echo ""

# 执行传入的Maven命令
mvn "$@"

echo ""
echo "Maven命令执行完成"