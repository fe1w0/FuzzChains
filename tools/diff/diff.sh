#!/bin/bash

# 使用$()执行命令，并将结果赋值给变量
machine_name=$(hostname)

# 检查参数数量是否正确
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <SerDumpJar> <DataSetPath>"
    exit 1
fi

# 从命令行参数中获取 SerDumpJar 和 DataSetPath
SerDumpJar="$1"
DataSetPath="$2"

poc_result=$(java -jar ${SerDumpJar} -r ${DataSetPath}/poc.ser)
no_poc_result=$(java -jar ${SerDumpJar} -r ${DataSetPath}/no-poc.ser)

wdiff <(echo "$poc_result") <(echo "$no_poc_result")