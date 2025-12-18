package com.lzh.method;

import com.lzh.UKB;
import com.lzh.util.PropsUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MAGMA extends AbstractMethod{
    private static volatile boolean prepared = false;

    public MAGMA(UKB.Trait trait, String sex, String name) {
        super(trait, sex, name);
    }

    @Override
    protected void prepare() {
        try {
            for (Integer chr : UKB.chrSet) {
                Files.createDirectories(Paths.get(output(chr)));
            }
            synchronized (MAGMA.class) {
                if (!prepared) {
                    Files.deleteIfExists(Paths.get(PropsUtil.getProp("magma.snploc")));
                    for (UKB.Gene gene : UKB.geneList) {
                        String cmd = String.format(
                                "awk -F'\\t' '($2>=%d&&$2<=%d){print $3\"\\t\"$1\"\\t\"$2}' /gpfs/chencao/Temporary_Files/chr%d_hg37 >> %s"
                                , Math.max(0, gene.getStart() - 1000000)
                                , gene.getEnd() + 1000000
                                , gene.getChr()
                                , PropsUtil.getProp("magma.snploc")
                        );
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
        for (Integer chr : UKB.chrSet) {
            String cmd = String.format("%s --job-name=%s sh %s/magma.sh %s %s %s %s %s %s"
                                        , PropsUtil.getProp(UKB.SRUN_4G)
                                        , getName()
                                        , BASE_DIR + getName()
                                        , PropsUtil.getProp("magma.snploc")
                                        , PropsUtil.getProp("magma.resource")
                                        , PropsUtil.getProp("1kg")
                                        , PropsUtil.getProp("magma.script")
                                        , summ(chr).substring(0, summ(chr).lastIndexOf("."))
                                        , output(chr)
                    );
            runProcess(cmd);
        }
    }
}
