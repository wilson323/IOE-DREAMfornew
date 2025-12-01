#!/bin/bash
cd "D:\IOE-DREAM\smart-admin-api-java17-springboot3\sa-admin"
mvn clean compile 2>&1 > compilation_full_output.txt
echo "完整编译输出已保存到 compilation_full_output.txt"
echo "前100行输出："
head -100 compilation_full_output.txt