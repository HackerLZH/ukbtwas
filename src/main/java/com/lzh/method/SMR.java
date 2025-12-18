package com.lzh.method;

import com.lzh.UKB;
import com.lzh.util.PropsUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SMR extends AbstractMethod{

    public SMR(UKB.Trait trait, String sex, String name) {
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
            String cmd = String.format("%s --job-name=%s sh %s/smr.sh %s %s %s %s %s"
                    , PropsUtil.getProp(UKB.SRUN_4G)
                    , getName()
                    , BASE_DIR + getName()
                    , PropsUtil.getProp("smr.script")
                    , output(chr)
                    , summ(chr)
                    , PropsUtil.getProp("smr.bfile")
                    , PropsUtil.getProp("smr.eqtl")
            );
            runProcess(cmd);
        }
    }
}
