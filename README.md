[Chinese](README_zh.md) | [English](README.md)

# What is this?

Give me genetic information and trait information, and I can perform **Transcriptome-Wide Association Analysis** using the **UKBB** 300,000 European genotype data.

**However**, this project does not integrate any resources (such as UKBB data, or various method libraries), as they are simply too large.
The purpose of writing this project is purely for **orchestration**, because manually processing and running each one is far too troublesome.

To run it, you first need to replace the constants in `src/main/resources/env.properties` with your own.
There are also some constants in `UKB.java` that need to be replaced. Finally, compile it into a jar package.

# File Input

Please enter the absolute file path!

## Genes

```
--genes [input_file]

【file content】
-- a gene each line，format
GeneSymbol,NCBIGene,Ensembl,chr,start,end
-- such as
TRIM24,8805,ENSG00000122779,7,138145004,138274741
```

## Trait

```
--traits [trait1,trait2,...,traitn]

【file content】【example】
type:[Q|B]
threshold:30
dataId:21001,23104
ICD10:K70.0,K76.0
self-report:high cholesterol
```

type: Trait type, continuous (Q) or binary (B)

UK BioBank currently provides three sources of traits:

1. **dataId**: The threshold matches the dataId, and the threshold is used to split the trait into cases and controls.
2. **ICD10**
3. **self-report**

It is not necessary to fill in all; **choose** based on the specific trait.

The above applies to **automatic extraction** of simple traits. Another option is provided below, where users need to **manually extract** traits.
${\textsf{\color{red}This feature is not yet implemented!!!}}$

```
--plinks [trait1,trait2,...,traitn]
```

You can directly input the bed/bim/fam (without extension) of already extracted traits.

Both methods can be used together.

## Sex

`--sex`

Whether to consider sex, default is not to consider.

## Methods

The following analysis methods are currently supported. Related resources can be downloaded from the links provided.

- `--magma` [Executable, Gene locations, build 37, European reference data](https://cncr.nl/research/magma/)
- `--pascal` [Download]()
- `--fusion` [Download]()
- `--iso` [Download]()
- `--tf` [Download]()
- `--utmost` [Download]()
- `--spred` [Download]()
- `--smr` [Download]()
- `--ldsc` [Download]()

## Other

By default, all steps are run. You can use the following options to run only a specific step or steps, provided the preceding steps have succeeded.

```--step1```

Execute only preprocessing.

```--step2```

Run only the analysis.

```--step3```

Collect only the results.