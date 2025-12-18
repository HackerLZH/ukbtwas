#!/bin/bash

output="$1"
SUMM_DIR="$2"
chr="$3"
fusion="$4"
rscript="$5"

if [ ! -f "$output"/trait.txt ]; then
awk '(NR > 1){print $2"\t"$6"\t"$7"\t"$9/$10}' "$SUMM_DIR" > "$output"/trait.txt
sed -i '1i SNP	A1	A2	Z' "$output"/trait.txt
fi

tissue="Whole_Blood"

${rscript} "$fusion"/FUSION.assoc_test.R \
  --sumstats "$output"/trait.txt \
  --weights "$fusion"/WEIGHTS/GTExv8.ALL."$tissue".pos \
  --weights_dir "$fusion"/WEIGHTS \
  --ref_ld_chr "$fusion"/LDREF/1000G.EUR. \
  --chr "$chr" \
  --out "$output"/result.txt

rm "$output"/trait.txt
