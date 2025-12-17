suppressMessages(library(isotwas))
library(optparse)

opt <- parse_args(OptionParser(
  option_list = list(
    make_option(c('-s', '--summ'), type = 'character', help = 'summary')
    , make_option(c('-o', '--out'), type = 'character', help = 'out')
    , make_option(c('-r', '--resource'), type = 'character', help = 'resource')
    , make_option(c('-g', '--gene'), type = 'character', help = 'gene')
  )
))

base <- opt$resource
iso_dir <- paste0(base, "/isoTWAS/")
ld_dir <- paste0(base, "/LD/")
# 1.Prepare data
print('Preparing data...')
gwas <- data.table::fread(opt$summ)
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

dir.create(opt$out, recursive = TRUE)
saveRDS(out_df, paste0(opt$out, "/", g, ".RDS"))