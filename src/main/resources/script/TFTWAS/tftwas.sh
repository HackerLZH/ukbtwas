#!/bin/bash

summ="$1"
out="$2"
spred="$3"
model="$4"
py="$5"

for tissue in "brain" "breast" "lung" "prostate"; do

"$py" \
"$spred"/SPrediXcan.py \
--model_db_path "$model"/"$tissue"/GTEX.db \
--covariance "$model"/"$tissue"/GTEX.txt.gz \
--gwas_file "$summ" \
--snp_column rs \
--effect_allele_column allele1 \
--non_effect_allele_column allele0 \
--beta_column beta \
--pvalue_column p_wald \
--output_file "$out"/"$tissue".csv

done
