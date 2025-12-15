#!/bin/bash

ref=/gpfs/chencao/zhenghuili/data/EUR_w_ld_chr/
/gpfs/chencao/zhenghuili/software/anaconda3/envs/ldsc/bin/python \
    /gpfs/chencao/zhenghuili/software/ldsc/ldsc.py \
    --h2 "$1" \
    --ref-ld-chr ${ref} \
    --w-ld-chr ${ref} \
    --out "$2"/ldsc
