#!/bin/bash
trait="BMI"
PASCAL_DIR=/gpfs/chencao/zhenghuili/software/PASCAL

for sex in "all" "male" "female"; do
for chr in 7; do

OUT_DIR=/gpfs/chencao/zhenghuili/metabo_gwas/trait/"$trait"/PASCAL/"$chr"/"$sex"
SUMM_DIR=/gpfs/chencao/zhenghuili/metabo_gwas/trait/"$trait"/chr"$chr"/"$sex"/output/summ.assoc.txt
mkdir -p "$OUT_DIR"
awk -F'\t' '(NR>1){print $2"\t"$11}' "$SUMM_DIR" > "$OUT_DIR"/trait.txt

cd "$PASCAL_DIR" && ./Pascal --pval="$OUT_DIR"/trait.txt --outsuffix="pascal"

mv "$PASCAL_DIR"/output/trait.*.genescores.txt "$OUT_DIR"/

#rm "$OUT_DIR"/"$1".txt
done
done

rm "$PASCAL_DIR"/output/trait.*
