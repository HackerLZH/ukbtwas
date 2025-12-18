package com.lzh.method;

import com.lzh.UKB;
import com.lzh.util.PropsUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ISOTWAS extends AbstractMethod{
    // 是否准备好运行前的资源
    private static volatile boolean prepared = false;

    public ISOTWAS(UKB.Trait trait, String sex, String name) {
        super(trait, sex, name);
    }

    @Override
    protected void prepare() {
        try {
            for (Integer chr : UKB.chrSet) {
                Files.createDirectories(Paths.get(output(chr)));
            }
            // 只需要一次makeld
            synchronized (ISOTWAS.class) {
                if (!prepared) {
                    for (UKB.Gene gene : UKB.geneList) {
                        String cmd = String.format("%s %s/makekd.R %s %s %s %s"
                                        , PropsUtil.getProp("iso.rscript")
                                        , BASE_DIR + getName()
                                        , PropsUtil.getProp("1kg")
                                        , gene.getEnsembl()
                                        , PropsUtil.getProp("iso.resource")
                                        , PropsUtil.getProp("plink"));
                        runProcess(cmd);
                    }
                    prepared = true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void runScript() {
        for (UKB.Gene gene : UKB.geneList) {
            String cmd = String.format("%s %s -s %s -o %s -r %s -g %s"
                    , PropsUtil.getProp("iso.rscript")
                    , BASE_DIR + getName() + "/isotwas.R"
                    , summ(gene.getChr())
                    , output(gene.getChr())
                    , PropsUtil.getProp("iso.resource")
                    , gene.getEnsembl()
            );
            runProcess(cmd);
        }
    }
}
