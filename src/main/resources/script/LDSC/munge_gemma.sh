#!/bin/bash

summ="$1"
out="$2"

name="ldsc"

awk '(NR>1){print $2,$6,$7,$9/$10,$4+$5,$11}' "$summ" > "$name".txt
N=$(sed -n 1p "$name".txt | cut -d' ' -f5)
sed -i '1i SNP A1 A2 Z N P' "$name".txt

/gpfs/chencao/zhenghuili/software/anaconda3/envs/ldsc/bin/python /gpfs/chencao/zhenghuili/software/ldsc/munge_sumstats.py \
    --sumstats "$name".txt \
    --N "$N" \
    --out "$out"/"$name" \
    --merge-alleles /gpfs/chencao/zhenghuili/data/w_hm3.snplist \
    --chunksize 500000

rm "$name".txt
