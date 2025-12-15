#!/bin/bash

trait="BMI"
rscript=/gpfs/chencao/zhenghuili/software/anaconda3/envs/fusion/bin/Rscript
fusion=/gpfs/chencao/zhenghuili/software/fusion_twas-master

for sex in "all" "male" "female"; do
for chr in 7; do
SUMM_DIR=/gpfs/chencao/zhenghuili/metabo_gwas/trait/"$trait"/chr"$chr"/"$sex"/output/summ.assoc.txt
output=/gpfs/chencao/zhenghuili/metabo_gwas/trait/"$trait"/FUSION/"$chr"/"$sex"
if [ ! -d "$output" ]; then
	mkdir -p "$output"
fi


if [ ! -f "$output"/trait.txt ]; then
awk -F'\t' '(NR > 1){print $2"\t"$6"\t"$7"\t"$9/$10}' "$SUMM_DIR" > "$output"/trait.txt
sed -i '1i SNP	A1	A2	Z' "$output"/trait.txt
fi


#for tissue in "Whole_Blood" "Artery_Aorta" "Artery_Coronary" "Artery_Tibial" "Heart_Atrial_Appendage" "Heart_Left_Ventricle"; do
tissue="Whole_Blood"
for num in "$chr"
do
	${rscript} "$fusion"/FUSION.assoc_test.R \
		--sumstats "$output"/trait.txt \
		--weights "$fusion"/WEIGHTS/GTExv8.ALL."$tissue".pos \
		--weights_dir "$fusion"/WEIGHTS \
		--ref_ld_chr "$fusion"/LDREF/1000G.EUR. \
		--chr "$num" \
		--out "$output"/trait."$num"."$tissue".txt
done

done

#rm "$output"/"$1".txt


done
