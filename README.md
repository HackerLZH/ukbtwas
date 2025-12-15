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

目前提供三种性状来源：
1. **dataId**：threshold与dataId匹配，threshold用于分割性状的case和control。
2. **ICD10**
3. **self-report**

无需全部填写，根据具体性状进行**选填**

以上适用于简单性状**自动提取**，下面提供另外一种选项，用户需要**手动提取**性状：
```
--plink [trait1,trait2,...,traitn]
```
可以直接输入已提取性状的bed/bim/fam（不带扩展名）

两种方法可以一起用

## 性别
`--sex`

是否考虑性别，默认不考虑

## 方法
指定待执行方法的源目录（包含可执行文件，脚本，参考数据等资源），可从后面链接中下载。
- `--magma` [下载可执行文件。Gene locations,build 37。Eurpean reference data](https://cncr.nl/research/magma/)
- `--pascal` [下载]()
- `--fusion` [下载]()
- `--iso` [下载]()
- `--tf` [下载]()
- `--utmost` [下载]()
- `--spred` [下载]()
- `--smr` [下载]()
- `--ldsc` [下载]()

## 其他
```--twas```

默认gwas summary都已准备好，直接跑twas

```--plink_path```

plink路径

```--gemma_path```

gemma路径（计算summary）