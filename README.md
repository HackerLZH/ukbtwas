# 这是什么？
给我基因信息和性状信息，我能使用**UKBB** 30万欧洲人基因型数据进行**全转录组关联分析**。

**但是**，本项目并没有整合任何资源（比如UKBB数据，以及各种方法运行库），毕竟，太大了。
我写这个项目的目的纯粹是为了**调度**，因为手动一个个处理和运行实在是太麻烦了。

想要运行，首先你要将`src/main/resources/env.properties`中的常量替换成你自己的，
`UKB.java`中也有一些常量需要替换。最后编译成jar包。

# 文件输入
请输入文件绝对路径！
## 基因
```
--genes [input_file]

【文件内容】
-- 每行一个基因，格式
GeneSymbol,NCBIGene,Ensembl,chr,start,end
-- 比如：
TRIM24,8805,ENSG00000122779,7,138145004,138274741
```
## 性状
```
--traits [trait1,trait2,...,traitn]

【文件内容】【举例】
type:[Q|B]
threshold:30
dataId:21001,23104
ICD10:K70.0,K76.0
self-report:high cholesterol
```
type：性状类型，连续性（Q）还是二分类（B）

UK BioBank 目前提供三种性状来源：
1. **dataId**：threshold与dataId匹配，threshold用于分割性状的case和control。
2. **ICD10**
3. **self-report**

无需全部填写，根据具体性状进行**选填**

以上适用于简单性状**自动提取**，下面提供另外一种选项，用户需要**手动提取**性状
${\textsf{\color{red}尚未完成此功能！！！}}$

```
--plinks [trait1,trait2,...,traitn]
```
可以直接输入已提取性状的bed/bim/fam（不带扩展名）

两种方法可以一起用

## 性别
`--sex`

是否考虑性别，默认不考虑

## 方法
目前支持以下方法分析，相关资源可从后面链接中下载。
- `--magma` [可执行文件，Gene locations,build 37，Eurpean reference data](https://cncr.nl/research/magma/)
- `--pascal` [下载]()
- `--fusion` [下载]()
- `--iso` [下载]()
- `--tf` [下载]()
- `--utmost` [下载]()
- `--spred` [下载]()
- `--smr` [下载]()
- `--ldsc` [下载]()

## 其他
默认情况下运行所有步骤，可以通过以下选项只运行某一步或者某几步，当然前提是前面步骤都成功了

```--step1```

只执行预处理

```--step2```

只跑分析

```--step3```

只收集结果
