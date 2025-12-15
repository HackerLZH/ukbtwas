#!/bin/bash
trait="BMI"
id="trait"

SMR=/gpfs/chencao/zhenghuili/software/smr/smr-1.3.1-linux-x86_64/smr-1.3.1

for sex in "all" "male" "female"; do
for chr in 7; do

OUT=/gpfs/chencao/zhenghuili/metabo_gwas/trait/"$trait"/SMR/"$chr"/"$sex"
BASE=/gpfs/chencao/zhenghuili/metabo_gwas/trait/"$trait"/chr"$chr"/"$sex"/output/summ.assoc.txt
mkdir -p "$OUT"

if [ ! -f "$OUT"/"$id".ma ]; then
awk -F'\t' '(NR>1){print $2"\t"$6"\t"$7"\t"$8"\t"$9"\t"$10"\t"$11"\t"$4+$5}' "$BASE" > "$OUT"/"$id".ma
sed -i "1i SNP  A1  A2  freq    b   se  p   N" "$OUT"/"$id".ma
fi

#for tissue in "Whole_Blood" "Artery_Aorta" "Artery_Coronary" "Artery_Tibial" "Heart_Atrial_Appendage" "Heart_Left_Ventricle"; do
tissue="Whole_Blood"
    ${SMR} \
        --bfile /gpfs/chencao/zhenghuili/data/geno/1kg/EUR/hm3_imp/merge_imp \
        --gwas-summary "$OUT"/"$id".ma \
        --eqtl-summary /gpfs/chencao/zhenghuili/metabo_gwas/smr_eqtl/"$tissue"/"$tissue"_hm3_cis \
        --peqtl-smr 5e-8 \
        --ld-upper-limit 0.9 \
        --ld-lower-limit 0.05 \
        --heidi-mtd 1 \
        --heidi-min-m 3 \
        --heidi-max-m 20 \
        --peqtl-heidi 1.57e-3 \
        --diff-freq-prop 1 \
        --cis-wind 10000 \
        --thread-num 1 \
        --out "$OUT"/"$id"_"$tissue"
#done

rm "$OUT"/"$id".ma

done
done
