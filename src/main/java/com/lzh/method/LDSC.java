package com.lzh.method;

import com.lzh.UKB;
import com.lzh.util.PropsUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LDSC extends AbstractMethod{

    public LDSC(UKB.Trait trait, String sex, String name) {
        super(trait, sex, name);
    }

    @Override
    protected void prepare() {
        try {
            for (UKB.Gene gene : UKB.geneList) {
                Files.createDirectories(Paths.get(output(gene.getSymbol())));
                BufferedReader br = Files.newBufferedReader(Paths.get(summ(gene.getChr())));
                BufferedWriter bw = Files.newBufferedWriter(Paths.get(output(gene.getSymbol()) + "/summ.assoc.txt"));

                long minPos = Math.max(1, gene.getStart() - 500000);
                long maxPos = gene.getEnd() + 500000;

                bw.write(br.readLine() + "\n");
                String line;
                while ((line = br.readLine()) != null) {
                    String[] arr = line.split("\\s+");
                    if (Long.parseLong(arr[2]) < minPos) {
                        continue;
                    }
                    if (Long.parseLong(arr[2]) > maxPos) {
                        break;
                    }
                    bw.write(line + "\n");
                }
                br.close();
                bw.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void runScript() {
        for (UKB.Gene gene : UKB.geneList) {
            String cmd = String.format("%s --job-name=%s sh %s/ldsc.sh %s %s %s %s %s"
                    , PropsUtil.getProp(UKB.SRUN_4G)
                    , getName()
                    , BASE_DIR + getName()
                    , output(gene.getSymbol())
                    , PropsUtil.getProp("ldsc.py")
                    , PropsUtil.getProp("ldsc.dir")
                    , PropsUtil.getProp("ldsc.snps")
                    , PropsUtil.getProp("ldsc.ref")
            );
            runProcess(cmd);
        }
    }
}
