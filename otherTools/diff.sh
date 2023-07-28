#!/bin/bash

SerDumpJar=/home/fe1w0/SoftwareAnalysis/DataAnalysisTools/SerializationDumper/SerializationDumper.jar

poc_result=$(java -jar ${SerDumpJar} -r /home/fe1w0/SoftwareAnalysis/DynamicAnalysis/FuzzChains/DataSet/output/poc.ser)
no_poc_result=$(java -jar ${SerDumpJar} -r /home/fe1w0/SoftwareAnalysis/DynamicAnalysis/FuzzChains/DataSet/output/no-poc.ser)

wdiff <(echo "$poc_result") <(echo "$no_poc_result")