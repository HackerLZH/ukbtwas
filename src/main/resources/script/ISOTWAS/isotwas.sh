#!/bin/bash
#SBATCH --chdir=/gpfs/chencao/Temporary_Files/ukbb_zh/webtwas2/output/logs/isotwas
#SBATCH --mem=4g
#SBATCH --nodes=1
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=1
#SBATCH --partition=cu,privority,batch01,gpu,fat
#SBATCH --exclude=cu01

# make LD
#rscript=/gpfs/chencao/zhenghuili/software/anaconda3/envs/py27/bin/Rscript
#${rscript} make_ld.R \
#	-t 'Whole Blood'

# run association
rscript=/gpfs/chencao/zhenghuili/software/anaconda3/envs/py27/bin/Rscript

for sex in "all" "male" "female"; do
${rscript} /gpfs/chencao/zhenghuili/metabo_gwas/trait/BMI/isotwas.R \
  -c "chr7" \
  -t "Whole_Blood" \
  -g "ENSG00000106546" \
  -s "$sex"
done
