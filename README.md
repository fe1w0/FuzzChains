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

使用 GCFinder(基于doop修改)，扫描

### Fuzzing Java libraries

## Todo:

- [ ] 1 期目标:
  - [ ] 支持以下功能:
    - [ ] 提供基本的Fuzzing功能
      - [ ] JQF
    - [x] 支持 根据 Property Trees 构造Fuzzing 种子
      - [x] propertyTreeNode
  - [x] 可以测试DataSet中的example

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

#### Question 1  ✅

- [x] 手动测试，手动加入 example.jar

```bash
java -jar fuzz/target/jqf-fuzz-2.1-SNAPSHOT-zest-cli.jar /Users/fe1w0/Project/SoftWareAnalysis/Dynamic/FuzzChains/target/FuzzChains-1.0-SNAPSHOT-modified.jar  xyz.xzaslxr.fuzzing.FuzzChainsTest fuzz
```

#### Question 2  ✅

按照正常的fuzz之后的验证方式存在问题：
1. 有点迷惑，到底fuzz 输出有无效
2. fuzz output 无法进行验证，需要查看相关脚本，并修改

```bash
bin/jqf-repro -c .:$(scripts/classpath.sh) -c  /Users/fe1w0/Project/SoftWareAnalysis/Dynamic/FuzzChains/target/FuzzChains-1.0-modified.jar  xyz.xzaslxr.fuzzing.FuzzChainsTest fuzz fuzz-results/corpus/id_000001
```

>虽然 jqf-repro 可以简单的输出一下问题，但问题还是很多
>没有显示出，当前数据值到底是多少

利用 SerializationDumper.jar 实现 Program Input 可视化

### 2023-07-29

#### Question 1  ✅

如何设置debug - JQF

```bash
java -jar -Djqf.failOnDeclaredExceptions=true fuzz/target/jqf-fuzz-2.1-SNAPSHOT-zest-cli.jar /Users/fe1w0/Project/SoftWareAnalysis/Dynamic/FuzzChains/target/FuzzChains-1.0-SNAPSHOT-modified.jar  xyz.xzaslxr.fuzzing.FuzzChainsTest fuzz -d 5s 
```

其中fuzz/target/jqf-fuzz-2.1-SNAPSHOT-zest-cli.jar 是我需要debug的部分

![16911667909531691166790091.png](https://raw.githubusercontent.com/fe1w0/ImageHost/main/image/16911667909531691166790091.png)

debug:
#### Question 2  ✅

如何设置debug - FuzzChains

jqf-maven插件源代码中 `project.getTestClasspathElements()`
```java
        try {
            List<String> classpathElements = project.getTestClasspathElements();

            if (disableCoverage) {
                loader = new URLClassLoader(
                        stringsToUrls(classpathElements.toArray(new String[0])),
                        getClass().getClassLoader());

            } else {
                loader = new InstrumentingClassLoader(
                        classpathElements.toArray(new String[0]),
                        getClass().getClassLoader());
            }
        } catch (DependencyResolutionRequiredException|MalformedURLException e) {
            throw new MojoExecutionException("Could not get project classpath", e);
        }
```

#### Question 3  ✅

The files in correspond to test inputs saved by JQF during its search. Each file corresponds to an input that discovers new code coverage. So, the union of all inputs will be the test suite with maximum coverage that JQF could find.`corpus`

>推荐查看源代码

The files in correspond to test inputs saved by JQF which led to assertion failures or other unexpected exceptions. If you repro with any of these files, you should see a stacktrace with the relevant exception. JQF does its best to avoid duplicate failures stemming from the same underlying issue, but in general it cannot promise to do so.`failures

### 2023-07-31

#### Question 1 ❎
添加 JVM属性，设置 Object.ser 反序列化文件 地址。

#### Question 2 ✅
设计合理的Property Tree json 格式与框架

属性树 - 算法如下:
1. 递归迭代，以树的形式设置root属性
2. 标注不同的变量 - label，分别分为`ordinary`, `priority`
  1.  `ordinary`: 表示 属性树中的一般 节点，其中 RootObject$Field = LeafObject，涉及到 变量的传递
  2. `priority`: 表示 属性树中的 重要节点，需要优先被选择和考虑，以覆盖更多的 TaintCallGraph:
    1. `priority` 的 类型有多种，需要考虑到多种情况，在未完全调研的情况下，大致有:
      1. if
      2. for
      3. while
      4. ... 需要查看
3. `Node` 的 Class 类型，也存在多种，generator的方式也存在多样性
  - [ ] 需要考虑到 哪些 Class 可以构造任意一个 未知的 Class 及其 Field Class


```json
	{
		"RootClass": {
			"ClassName": "sources.serialize.UnsafeSerialize",
			"FieldNames": {
				"chainOne": {
					"Lable": "ORDINARY",
					"ClassName": "sources.demo.ExpOne",
					"FieldNames": {
						"size": {
							"Lable": "PRIORITY",
							"ClassName": "java.lang.Integer"
						}
					}
				},
				"chainTwo": {
					"Lable": "ORDINARY",
					"ClassName": "sources.demo.ExpTwo"
				}
			}
		}
	}
```

#### Question 3 ❎

TaintCallGraph:

Souffle 推导的 TaintCallGraph 是有向有环图，对于FuzzChains来说，需要转价为 有向无环图。
### 2023-08-03

#### Question 1 ✅

设置 ReadPropertyTreeConfigure

#### Question 2 extends 2023-07-31-Question 2 ✅

在得到 PropertyTree 后，需要设计 1.0.a 版 Generator，可以根据 json 自定义生成 `ByteArrayInputStreamGenerator`

>其中有个性能/优化问题，对于 Tree 而言，有些 node 是无 children，可以设置多线程生成这些 Childless-node。
>或者🤔️更快一点，首先生成所有的 node，之后根据类型设置不同的Random，最后一个个拼上去。


### 2023-08-04

添加启动命令

```bash
mvn test
```


