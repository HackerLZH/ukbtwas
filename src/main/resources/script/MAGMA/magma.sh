#!/bin/bash

snploc="$1"
resource="$2"
# 1kg
ref="$3"
magma="$4"
summary="$5"
output="$6"

awk '{if(NR>1) print $2"\t"$11}' ${summary}.txt > "$output"/p.txt

n=$(awk '(NR==2){print $4+$5}' "$summary".txt)

${magma} --annotate --snp-loc "$snploc" --gene-loc "$resource"/NCBI37.3.gene.loc --out "$output"/anno

${magma} --bfile ${ref} --pval "$output"/p.txt  N="$n" --gene-annot "$output"/anno.genes.annot --out ${output}/result

rm "$output"/snp.txt
rm "$output"/p.txt
rm "$output"/anno.genes.annot
rm "$output"/anno.log
rm "$output"/result.genes.raw
rm "$output"/result.log
rm "$output"/result.log.suppl
