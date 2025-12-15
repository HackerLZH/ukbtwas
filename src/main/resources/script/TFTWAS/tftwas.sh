#!/bin/bash

BASE=/gpfs/chencao/zhenghuili/metabo_gwas/trait/BMI
py=/gpfs/chencao/zhenghuili/software/anaconda3/envs/imlabtools/bin/python

for tissue in "brain" "breast" "lung" "prostate"; do
for sex in "all" "male" "female"; do
for ch in 7; do
mkdir -p "$BASE"/TFTWAS/"$tissue"/"$ch"/"$sex"
"$py" \
/gpfs/chencao/zhenghuili/software/metaxcan/MetaXcan-master/software/SPrediXcan.py \
--model_db_path /gpfs/chencao/GroupData/webTWAS/webtwas2/TF-TWAS/DB/"$tissue"/GTEX.db \
--covariance /gpfs/chencao/GroupData/webTWAS/webtwas2/TF-TWAS/DB/"$tissue"/GTEX.txt.gz \
--gwas_file "$BASE"/chr"$ch"/"$sex"/output/summ.assoc.txt \
--snp_column rs \
--effect_allele_column allele1 \
--non_effect_allele_column allele0 \
--beta_column beta \
--pvalue_column p_wald \
--output_file "$BASE"/TFTWAS/"$tissue"/"$ch"/"$sex"/result.csv
done
done
done
