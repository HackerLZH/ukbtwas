package com.lzh.method;

import com.lzh.UKB;
import com.lzh.util.PropsUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FUSION extends AbstractMethod{

    public FUSION(UKB.Trait trait, String sex, String name) {
        super(trait, sex, name);
    }

    @Override
    protected void prepare() {
        try {
            for (Integer chr : UKB.chrSet) {
                Files.createDirectories(Paths.get(output(chr)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void runScript() {
        for (Integer chr : UKB.chrSet) {
            String cmd = String.format("%s --job-name=%s sh %s/fusion.sh %s %s %s %s %s"
                    , PropsUtil.getProp(UKB.SRUN_4G)
                    , getName()
                    , BASE_DIR + getName()
                    , output(chr)
                    , summ(chr)
                    , chr
                    , PropsUtil.getProp("fusion.dir")
                    , PropsUtil.getProp("fusion.rscript")
            );
            runProcess(cmd);
        }
    }
}
