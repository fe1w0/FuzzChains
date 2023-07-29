#!/bin/bash

# 使用$()执行命令，并将结果赋值给变量
 machine_name=$(hostname)

# 判断当前机器名是否为 fe1w0deMacBook-Air.local
if [ "$machine_name" = "fe1w0deMacBook-Air.local" ]; then
  SerDumpJar=/Users/fe1w0/Project/SoftWareAnalysis/OtherTools/SerializationDumper/SerializationDumper.jar
#  SerDumpJar=./SerializationDumper.jar
  DataSetPath=/Users/fe1w0/Project/SoftWareAnalysis/Dynamic/FuzzChains/DataSet/output
else
  SerDumpJar=/home/fe1w0/SoftwareAnalysis/DataAnalysisTools/SerializationDumper/SerializationDumper.jar
  DataSetPath=/home/fe1w0/SoftwareAnalysis/DynamicAnalysis/FuzzChains/DataSet/output
fi

poc_result=$(java -jar ${SerDumpJar} -r ${DataSetPath}/poc.ser)
no_poc_result=$(java -jar ${SerDumpJar} -r ${DataSetPath}/no-poc.ser)

wdiff <(echo "$poc_result") <(echo "$no_poc_result")