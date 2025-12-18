#!/bin/bash

SMR="$1"
OUT="$2"
SUMM="$3"

if [ ! -f "$OUT"/summ.ma ]; then
awk '(NR>1){print $2"\t"$6"\t"$7"\t"$8"\t"$9"\t"$10"\t"$11"\t"$4+$5}' "$SUMM" > "$OUT"/summ.ma
sed -i "1i SNP  A1  A2  freq    b   se  p   N" "$OUT"/summ.ma
fi

tissue="Whole_Blood"
${SMR} \
    --bfile "$4" \
    --gwas-summary "$OUT"/summ.ma \
    --eqtl-summary "$5"/"$tissue"/"$tissue"_hm3_cis \
    --peqtl-smr 5e-8 \
    --ld-upper-limit 0.9 \
    --ld-lower-limit 0.05 \
    --heidi-mtd 1 \
    --heidi-min-m 3 \
    --heidi-max-m 20 \
    --peqtl-heidi 1.57e-3 \
    --diff-freq-prop 1 \
    --cis-wind 10000 \
    --thread-num 1 \
    --out "$OUT"/result

rm "$OUT"/summ.ma
rm "$OUT"/*.list
