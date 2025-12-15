suppressMessages(library(isotwas))
library(optparse)

opt <- parse_args(OptionParser(
  option_list = list(
    make_option(c('-c', '--chr'), type = 'character', help = 'chromsome')
    ,make_option(c('-t', '--tissue'), type = 'character', help = 'type of tissue')
    , make_option(c('-g', '--gene'), type = 'character', help = 'gene')
    ,make_option(c('-s', '--sex'), type = 'character', help = 'sex')
#    ,make_option(c('-o', '--output'), type = 'character', help = 'output of result')
    # ,make_option(c('-p', '--pop'), type = 'character', default = 'eur', help = 'population(eur,afr,eas,sas,amr)')
  )
))

trait <- "BMI"
base <-"/gpfs/chencao/zhenghuili/data/GTEx/GTEx_isoform/"
iso_dir <- paste0(base, opt$tissue, "/isoTWAS/")
ld_dir <- paste0(base, opt$tissue, "/LD/")
# 1.Prepare data
print('Preparing data...')
# gwas <- read.table(opt$s, header = T, sep = '\t') 
gwas <- data.table::fread(paste0("/gpfs/chencao/zhenghuili/metabo_gwas/trait/", trait, "/", opt$chr, "/", opt$sex, "/output/summ.assoc.txt"))
#colnames(gwas) <- c('CHR', 'BP', 'SNP', 'A1', 'A2', 'MAF', 'BETA', 'P', 'SE', 'Z')
colnames(gwas) <- c('CHR', 'SNP', 'BP', 'NMIS', 'NOBS', 'A1', 'A2', 'MAF', 'BETA', 'SE', 'P')
gwas[, Z := BETA / SE]
# 2.Generate nominal Z-scores
print('Generating nominal Z-scores...')
out_df <- data.frame(Gene = c(),
                      Feature = c(),
                      Z = c(),
                      P = c(),
                      permute.P = c(),
                      topSNP = c(),
                      topSNP.P = c())
#genes <- gsub('_isoTWAS.RDS', '', list.files(iso_dir))
#print(length(genes))
#i <- 0
#for (g in genes) {
#  i <- i + 1
#  if (i %% 1000 == 0) {
#    print(i)
#  }
g <- opt$gene
  model <- readRDS(paste0(iso_dir, g, '_isoTWAS.RDS'))
  ld <- readRDS(paste0(ld_dir, g, '_LDMatrix.RDS'))
  for (tx in unique(model$Feature)) {
    tryCatch({
      sumstats.cur <- subset(gwas, SNP %in% subset(model, Feature == tx)$SNP)
      tx_df <- burdenTest(mod = subset(model, Feature == tx),
                                  ld = ld,
                                  gene = g,
                                  sumStats = sumstats.cur,
                                  chr = 'CHR',
                                  pos = 'BP',
                                  a1 = 'A1',
                                  a2 = 'A2',
                                  a1_mod = 'ALT',
                                  a2_mod = 'REF',
                                  snpName = 'SNP',
                                  Z = 'Z',
                                  beta = 'BETA',
                                  se = 'SE',
                                  featureName = 'Feature',
                                  R2cutoff = .01,
                                  alpha = 1e-3,
                                  nperms = 1000,
                                  usePos = F)
      if (tx_df$Gene != "SNPs not found.")
        out_df <- rbind(out_df, tx_df)
    }, error = function(e) {
      NA
    })
  }
#}
OUT <- paste0("/gpfs/chencao/zhenghuili/metabo_gwas/trait/", trait, "/ISOTWAS/", opt$chr, "/", opt$sex)
dir.create(OUT, recursive = TRUE)
saveRDS(out_df, paste0(OUT, "/", g, ".RDS"))
stop(1)
# out_df$P <- as.numeric(out_df$P)
# impute_na_var <- mean(out_df$P, na.rm=T)
# out_df$P[is.na(out_df$P)] <- impute_na_var

# out_df <- readRDS(paste0("/gpfs/chencao/Temporary_Files/ukbb_zh/webtwas2/tmp/isotwas/", opt$output, "/", opt$tissue, ".RDS"))

# 3.two-stage hypothesis test
## FDR
print('Controlling FDR...')
suppressPackageStartupMessages(library(dplyr))
gene <- out_df %>%
  group_by(Gene) %>%
  filter(!(any(P == 0) && any(P == 1))) %>%
  summarise(Screen.P = p_screen(P))
gene <- as.data.frame(gene)
alpha1 <- .05
G <- nrow(gene)
gene$Screen.P.Adjusted <- p.adjust(gene$Screen.P, method = 'fdr')
R <- length(unique(gene$Gene[gene$Screen.P.Adjusted < alpha1]))
alpha2 <- (R * alpha1) / G
# print(paste("alpha2", alpha2, sep = ":"))
# print(head(gene))
## FWER
print('Controlling FWER...')
isoform_new <- as.data.frame(matrix(nrow = 0,
                                   ncol = ncol(out_df) + 2))
colnames(isoform_new) <- c(colnames(out_df), 'Screen.P', 'Confirmation.P')
gene <- gene[order(gene$Screen.P),]
ttt <- merge(out_df,
             gene[, c('Gene', 'Screen.P',
                     'Screen.P.Adjusted')],
             by = 'Gene')
isoform_new <- ttt %>%
 group_by(Gene) %>%
 summarise(Feature = Feature,
           Confirmation.P = p_confirm(P, alpha = alpha2))
isoform_new <- merge(isoform_new, ttt, by = c('Gene', 'Feature'))
isoform_new$Confirmation.P <- ifelse(isoform_new$Screen.P.Adjusted,
                                     isoform_new$Confirmation.P,
                                     1)
isoform_new <- isoform_new[, c('Gene', 'Feature', 'Z', 'P', 'permute.P',
                               'topSNP', 'topSNP.P',
                               'Screen.P', 'Screen.P.Adjusted', 'Confirmation.P')]
#print(head(isoform_new))
print('Saving results passing two tests...')
#out <- paste0("/gpfs/chencao/Temporary_Files/webtwas2/", opt$output, "/isotwas/")
#if (!dir.exists(out)) {
#  dir.create(out, recursive = TRUE)
#}
write.table(subset(isoform_new, Screen.P.Adjusted < alpha1 &
                   Confirmation.P < alpha2 &
                   permute.P < alpha1), file = paste0(OUT, "/", g, ".txt"), quote = F, sep = "\t", row.names = F)
print("isoTWAS finished.")

# 4.Fine-mapping overlapping and trait-associated isoforms
## obtain the gene annotation (不确定以下字面量是否是固定的？)
# print('--------------------------gene annotation')
# gene_names <- unique(isoform_new$Gene)
# ensembl <- biomaRt::useEnsembl(biomart = "ensembl",
#                                dataset = "hsapiens_gene_ensembl",
#                                mirror = "useast")
# bm <- biomaRt::getBM(attributes = c('ensembl_gene_id',
#                                     'chromosome_name',
#                                     'start_position',
#                                     'end_position'),
#                      filters = 'ensembl_gene_id',
#                      values = gene_names,
#                      mart = ensembl)
# colnames(bm) <- c('Gene', 'Chromosome', 'Start', 'End')
# ## select the overlapping isoforms
# print('--------------------------select the overlapping isoforms')
# isoform_new <- merge(bm, isoform_new, by = 'Gene')
# isoform_new <- isoform_new[order(isoform_new$Chromosome,
#                                  isoform_new$Start,
#                                  decreasing = F),]
# isoform_sig <- subset(isoform_new,
#                       Screen.P.Adjusted < alpha1 &
#                         Confirmation.P < alpha2 &
#                         permute.P < 0.05)
# keep.isoform <- c()
# if (nrow(isoform_sig) > 1) {
#   for (i in 1:(nrow(isoform_sig) - 1)) {
#     if (isoform_sig$End[i] > isoform_sig$Start[i + 1] - 1e6) {
#       keep.isoform <- unique(c(keep.isoform,
#                                c(isoform_sig$Feature[c(i, i + 1)])))
#     }
#   }
# }
# isoform_sig <- subset(isoform_sig, Feature %in% keep.isoform)
# ## aggregate the models and generate a single table with the SNP-to-isoform weights
# ## of all the SNPs that predict these overlapping isoforms.
# all.snps <- c()
# omega <- c()
# pos <- c()
# gene <- c()
# snp.chr <- c()
#
# ### COLLECT WEIGHTS FOR SNPS IN THE MODELS
# print('--------------------------collect weights')
# for (i in seq_len(nrow(isoform_sig))) {
#   gene_in <- isoform_sig$Gene[i]
#   model_in <- readRDS(paste0(dir, gene_in, "_isoTWAS.RDS"))
#   model_in <- subset(model_in,
#                      Feature == isoform_sig$Feature[i])
#   Model <- data.frame(SNP = model_in$SNP,
#                       Chromosome = model_in$Chromosome,
#                       Position = model_in$Position,
#                       Effect = model_in$Weight,
#                       A1 = model_in$ALT,
#                       A2 = model_in$REF)
#   Model <- subset(Model, Effect != 0)
#   Model <- Model[!duplicated(Model$SNP),]
#   all.snps <- c(all.snps,
#                 as.character(Model$SNP))
#   omega <- c(omega,
#              as.numeric(Model$Effect))
#   gene <- c(gene,
#             rep(isoform_sig$Feature[i], nrow(Model)))
#   snp.chr <- c(snp.chr,
#                as.numeric(Model$Chromosome))
#   pos <- c(pos, as.numeric(Model$Position))
# }
#
# tot.df <- data.frame(SNP = all.snps,
#                      Gene = gene,
#                      Effect = omega,
#                      Chromosome = snp.chr)
#
# model.df <- as.data.frame(matrix(nrow = length(unique(all.snps)),
#                                  ncol = nrow(isoform_sig) + 1))
# colnames(model.df) <- c('SNP', isoform_sig$Feature)
# model.df$SNP <- as.character(unique(all.snps))
#
# for (q in seq_len(nrow(isoform_sig))) {
#   cur.tot.df <- subset(tot.df, Gene == isoform_sig$Feature[q])
#   cur.tot.df$SNP <- as.character(cur.tot.df$SNP)
#   for (i in seq_len(nrow(model.df))) {
#     w <- which(cur.tot.df$SNP == model.df$SNP[i])
#     model.df[i, q + 1] <- ifelse(length(w) != 0,
#                                  cur.tot.df$Effect[w],
#                                  0)
#   }
# }
#
# model.df$Chromosome <- 2
# for (i in seq_len(nrow(model.df))) {
#   rrr <- subset(tot.df, SNP == model.df$SNP[i])
#   model.df$Chromosome[i] <- rrr$Chromosome[1]
# }
#
# print(head(model.df))
#
# print('--------------------------fine-mapping')
# V <- readRDS(system.file("extdata",
#                          "test_LD_finemapping.RDS",
#                          package = "isotwas"))
# V <- V[model.df$SNP, model.df$SNP]
# Omega <- Matrix::Matrix(as.matrix(model.df[, -c(1, ncol(model.df))]))
# zscores <- isoform_sig$Z
# m <- length(zscores)
#
# ### COMPUTE LD BETWEEN TX ON THE GENETIC LEVEL
# wcor <- isotwas::estimate_cor(as.matrix(Omega),
#                               as.matrix(V),
#                               intercept=T)[[1]]
# diag(wcor) <- 1
# wcor[is.na(wcor)] <- 0
#
# ### COMPUTE LD INTERCEPT BETWEEN ISOFRM ON THE GENETIC LEVEL
# swld <- isotwas::estimate_cor(as.matrix(Omega),
#                               as.matrix(V),
#                               intercept=T)[[2]]
#
# null_res <- m * log(1 - 1e-3)
# marginal <- m * log(1 - 1e-3)
# comb_list <- list()
# for (n in 1:min(2,length(zscores))){
#   comb_list <- c(comb_list,
#                  combn(seq_along(zscores), n, simplify=F))
#   }
#
# pips <- rep(0, length(zscores))
#
# ### COMPUTE BAYES FACTORS/LIKELIHOOD AT EACH CAUSAL CONFIG
# for (j in seq_along(comb_list)){
#   subset <- comb_list[[j]]
#   local <- isotwas::bayes_factor(zscores,
#                                  idx_set = subset,
#                                  wcor = wcor)
#
#   marginal <- log(exp(local) + exp(marginal))
#   for (idx in subset){
#     if (pips[idx] == 0){
#       pips[idx] <- local
#       } else {
#         pips[idx] <- log(exp(pips[idx]) + exp(local))
#       }
#   }
#   }
#
# pips <- exp(pips - marginal)
# null_res <- exp(null_res - marginal)
# isoform_sig$pip <- pips
# isoform_sig <- isoform_sig[order(isoform_sig$pip, decreasing = T),]
# npost <- isoform_sig$pip/sum(isoform_sig$pip)
# csum <- cumsum(npost)
# isoform_sig$in_cred_set <- F
#
# for (i in seq_len(nrow(isoform_sig))){
#   isoform_sig$in_cred_set[i] <- T
#   if (i > 1){
#     if (csum[i] > .9 & csum[i-1] < .9){
#       isoform_sig$in_cred_set[i] <- T
#       }
#     if (csum[i] < .9){
#       isoform_sig$in_cred_set[i] <- T
#       }
#     if (csum[i] > .9 & csum[i-1] > .9){
#       isoform_sig$in_cred_set[i] <- F
#     }
#   }
#   }
#
# print(isoform_sig)
