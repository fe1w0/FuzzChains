# FuzzChains

> author: fe1w0
>

## Setup

按照以下命令进行环境部署。

```bash  
  
```  

## Usage

### Prepare the needed configuration files

使用 GCFinder(基于doop修改)，扫描

### Fuzzing Java libraries

## Todo:

- [ ] 1 期目标:
  - [ ] 支持以下功能:
    - [ ] 提供基本的Fuzzing功能
      - [ ] JQF
    - [ ] 支持 根据 Property Trees 构造Fuzzing 种子
      - [ ] propertyTreeNode
  - [ ] 可以测试DataSet中的example

## LOG

### 2023-07-22

现在的问题是，不知道复杂的object如何构建，且Fuzzing效率最高。

SerHybrid 和 GCMiner 的 fuzzing 部分中，在一开始都有一个 SerChecker 部分，即检查生成的 Object(最终的) 是否可被序列化。

如无法被序列化，则也表明无法生成可触发到反序列化漏洞的 Object。

需要近期内完成：

- [ ] 属性树的调研
  - [x] 查看GCMiner上的设计
- [x] 查看SerHybrid上的设计
  - GCMiner 和 SerHybrid 中的属性树设计，都是为了服务checkIsSerializable，不是为了产生具体的Fuzzing Seed
  - 即无法利用Fuzzing的思路，产生有效的Poc
- [ ] 重点测试，JQF 能否拓展到 基于属性的复杂实例化的fuzzing
- [ ] 插桩

### 2023-07-28

Questions:
- JQF how to load `-Dclass` with jar files.
  - 这个问题，今天就需要解决
  - 换句话说，我需要在 mvn 指令中 添加 jar 的功能。
  - 解决方案:
    - 利用 Assembly 插件
      - https://stackoverflow.com/questions/36047637/how-can-i-include-test-classes-into-maven-jar-and-execute-them
      - https://github.com/rohanpadhye/JQF/issues/52
      - https://github.com/fuzzitdev/example-java
- how to confirm which `method` will be fuzzed.
  - 最大的问题，不太清楚，JQF 如何执行 `run(Program, inputs)`
  - 有了，从`writeObject()`开始
  - 此外，设置 `magicWords`

##### Question One  ✅

- [x] 手动测试，手动加入 example.jar

```bash
# mac
java -jar fuzz/target/jqf-fuzz-2.1-SNAPSHOT-zest-cli.jar /Users/fe1w0/Project/SoftWareAnalysis/Dynamic/FuzzChains/target/FuzzChains-1.0-SNAPSHOT-modified.jar  xyz.xzaslxr.fuzzing.FuzzChainsTest fuzz -d 50s

# ubuntu
java -jar fuzz/target/jqf-fuzz-2.1-SNAPSHOT-zest-cli.jar /home/fe1w0/SoftwareAnalysis/DynamicAnalysis/FuzzChains/target/FuzzChains-1.0-SNAPSHOT-modified.jar  xyz.xzaslxr.fuzzing.FuzzChainsTest fuzz -d 10s
```

##### Question Two

按照正常的fuzz之后的验证方式存在问题：
1. 有点迷惑，到底fuzz 输出有无效
2. fuzz output 无法进行验证，需要查看相关脚本，并修改

```bash
# mac
bin/jqf-repro -c .:$(scripts/classpath.sh) -c  /Users/fe1w0/Project/SoftWareAnalysis/Dynamic/FuzzChains/target/FuzzChains-1.0-SNAPSHOT-modified.jar  xyz.xzaslxr.fuzzing.FuzzChainsTest reportFuzz fuzz-results/corpus/id_000001

# ubuntu
bin/jqf-repro -c .:$(scripts/classpath.sh) -c  /home/fe1w0/SoftwareAnalysis/DynamicAnalysis/FuzzChains/target/FuzzChains-1.0-SNAPSHOT-modified.jar  xyz.xzaslxr.fuzzing.FuzzChainsTest reportFuzz fuzz-results/corpus/id_000001
```

>虽然 jqf-repro 可以简单的输出一下问题，但问题还是很多
>没有显示出，当前数据值到底是多少

