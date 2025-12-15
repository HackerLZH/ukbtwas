#!/bin/bash
trait="BMI"
snploc=/gpfs/chencao/zhenghuili/data/hg37/snp_loc.txt
geneloc=/gpfs/chencao/zhenghuili/software/magma/resource/NCBI37.3.gene.loc
ref=/gpfs/chencao/zhenghuili/data/geno/1000_genome/eur/g1000_eur
magma=/gpfs/chencao/zhenghuili/software/magma/magma

for sex in "all" "male" "female"; do
for chr in 7; do

summary=/gpfs/chencao/zhenghuili/metabo_gwas/trait/"$trait"/chr"$chr"/"$sex"/output/summ.assoc
output=/gpfs/chencao/zhenghuili/metabo_gwas/trait/"$trait"/MAGMA/"$chr"/"$sex"

mkdir -p "$output"

#awk -F'\t' '{if(NR>1) print $2"\t"$1"\t"$3}' ${summary}.txt > ${summary}_snp.txt
awk -F'\t' '{if(NR>1) print $2"\t"$11}' ${summary}.txt >${summary}_p.txt

n=$(awk -F'\t' '(NR==2){print $4+$5}' "$summary".txt)

${magma} --annotate --snp-loc "$snploc" --gene-loc ${geneloc} --out ${summary}_anno

${magma} --bfile ${ref} --pval ${summary}_p.txt  N="$n" --gene-annot ${summary}_anno.genes.annot --out ${output}/result

rm ${summary}_snp.txt
rm ${summary}_p.txt
rm ${summary}_anno.genes.annot
rm ${summary}_anno.log

done
done
