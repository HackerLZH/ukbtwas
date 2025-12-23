#!/bin/bash

out="$1"
summ="$out"/summ.assoc.txt
py="$2"
dir="$3"
snps="$4"
ref="$5"/

awk '(NR>1){print $2,$6,$7,$9/$10,$4+$5,$11}' "$summ" > "$out"/ldsc.txt
N=$(sed -n 1p "$out"/ldsc.txt | cut -d' ' -f5)
sed -i '1i SNP A1 A2 Z N P' "$out"/ldsc.txt

"$py" "$dir"/munge_sumstats.py \
    --sumstats "$out"/ldsc.txt \
    --N "$N" \
    --out "$out"/ldsc \
    --merge-alleles "$snps"

# 遗传力
"$py" "$dir"/ldsc.py \
    --h2 "$out"/ldsc.sumstats.gz \
    --ref-ld-chr ${ref} \
    --w-ld-chr ${ref} \
    --out "$out"/ldsc

rm "$out"/summ.assoc.txt
rm "$out"/ldsc.txt
rm "$out"/ldsc.sumstats.gz
