#!/bin/bash

python2=/gpfs/chencao/zhenghuili/software/anaconda3/envs/utmost/bin/python
base=/gpfs/chencao/GroupData/webTWAS/webtwas2/UTMOST
DIR=/gpfs/chencao/zhenghuili/metabo_gwas/trait/BMI

# TISSUE_GTEx=(Adipose_Subcutaneous Adipose_Visceral_Omentum Adrenal_Gland Artery_Aorta Artery_Coronary Artery_Tibial Brain_Anterior_cingulate_cortex_BA24 Brain_Caudate_basal_ganglia Brain_Cerebellar_Hemisphere Brain_Cerebellum Brain_Cortex Brain_Frontal_Cortex_BA9 Brain_Hippocampus Brain_Hypothalamus Brain_Nucleus_accumbens_basal_ganglia Brain_Putamen_basal_ganglia Breast_Mammary_Tissue Cells_EBV-transformed_lymphocytes Cells_Transformed_fibroblasts Colon_Sigmoid Colon_Transverse Esophagus_Gastroesophageal_Junction Esophagus_Mucosa Esophagus_Muscularis Heart_Atrial_Appendage Heart_Left_Ventricle Liver Lung Muscle_Skeletal Nerve_Tibial Ovary Pancreas Pituitary Prostate Skin_Not_Sun_Exposed_Suprapubic Skin_Sun_Exposed_Lower_leg Small_Intestine_Terminal_Ileum Spleen Stomach Testis Thyroid Uterus Vagina Whole_Blood)
#TISSUE_GTEx=(Artery_Aorta Artery_Coronary Artery_Tibial Heart_Atrial_Appendage Heart_Left_Ventricle)

for sex in "all" "male" "female"; do
mkdir -p "$DIR"/UTMOST/"$sex"/step1/
#for tissue in "Pancreas" "Liver" "Adipose_Subcutaneous" "Adipose_Visceral_Omentum" "Muscle_Skeletal" "Whole_Blood" "Brain_Hypothalamus"
#do
#${python2} "$base"/single_tissue_association_test.py \
#--model_db_path "$base"/sample_data/weight_db_GTEx/"$tissue".db \
#--covariance "$base"/sample_data/covariance_tissue/"$tissue".txt.gz \
#--gwas_folder "$DIR" \
#--gwas_file_pattern "$sex".assoc.txt \
#--snp_column rs \
#--effect_allele_column allele1 \
#--non_effect_allele_column allele0 \
#--beta_column beta \
#--pvalue_column p_wald \
#--output_file "$DIR"/UTMOST/"$sex"/step1/"$tissue".csv
#done

mkdir "$DIR"/UTMOST/"$sex"/step2

${python2} "$base"/joint_GBJ_test.py \
--weight_db "$base"/GTEX/database/ \
--output_dir "$DIR"/UTMOST/"$sex"/step2 \
--cov_dir "$base"/sample_data/covariance_joint/ \
--input_folder "$DIR"/UTMOST/"$sex"/step1 \
--gene_info "$base"/intermediate/gene_info.txt \
--output_name joint \
--start_gene_index 1 \
--end_gene_index 17290

done
