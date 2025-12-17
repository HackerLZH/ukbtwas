library(bigsnpr)
library(dplyr)

argv <- commandArgs(T)

bed_file <- argv[1]
gene <- argv[2]
resource <- argv[3]
plink <- argv[4]

gene_data <- readRDS(paste0(resource, "/isoTWAS/", gene, "_isoTWAS.RDS"))

target_rsids <- gene_data$SNP

write(target_rsids, file = paste0("/tmp/", gene, ".snps"), sep='\n')
system(paste0(plink, ' --bfile ', bed_file, ' --extract /tmp/', gene, ".snps --make-bed --out /tmp/", gene))
G <- bed(paste0("/tmp/", gene, ".bed"))

cor_matrix <- bed_cor(G)

# 设置相关矩阵的行列名
rownames(cor_matrix) <- G$map$marker.ID
colnames(cor_matrix) <- G$map$marker.ID

saveRDS(cor_matrix, file = paste0(resource, "/LD/", gene, "_LDMatrix.RDS"))
