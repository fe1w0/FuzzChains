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



