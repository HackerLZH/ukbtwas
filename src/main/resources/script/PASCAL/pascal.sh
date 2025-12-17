#!/bin/bash

OUT_DIR="$1"
SUMM="$2"
PASCAL_DIR="$3"

awk '(NR>1){print $2"\t"$11}' "$SUMM" > "$OUT_DIR"/trait.txt

cd "$PASCAL_DIR" && ./Pascal --pval="$OUT_DIR"/trait.txt --outsuffix="pascal"

mv "$PASCAL_DIR"/output/trait.*.genescores.txt "$OUT_DIR"/

rm "$PASCAL_DIR"/output/trait.*
rm "$OUT_DIR"/trait.txt
