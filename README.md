# FuzzChains

> author: fe1w0

## Setup

按照以下命令进行环境部署。

```bash
# 编译
mvn test

# fuzz
mvn jqf:fuzz -Djqf.failOnDeclaredExceptions=true -Dclass=xyz.xzaslxr.fuzzing.FuzzChainsTest -Dmethod=fuzz -Dtime=5s

# repro
mvn jqf:repro -Djqf.failOnDeclaredExceptions=true -Dclass=xyz.xzaslxr.fuzzing.FuzzChainsTest -Dmethod=reportFuzz -Dtime=5s -Dinput=/Users/fe1w0/Project/SoftWareAnalysis/Dynamic/FuzzChains/target/fuzz-results/xyz.xzaslxr.fuzzing.FuzzChainsTest/fuzz/failures/id_000000
```

## Usage

### Prepare the needed configuration files

使用 GCFinder(基于doop修改) 扫描

### Fuzzing Java libraries

## Todo:

- [ ] 1 期目标:
  - [ ] 支持以下功能:
    - [ ] 提供基本的Fuzzing功能
      - [ ] JQF
    - [x] 支持 根据 Property Trees 构造Fuzzing 种子
      - [x] propertyTreeNode
  - [x] 可以测试DataSet中的example


