package com.lzh.method;

import com.lzh.UKB;
import com.lzh.util.PropsUtil;

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
            Files.createDirectories(Paths.get(output()));
            combineSummary();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void runScript() {
        String cmd = String.format("%s --job-name=%s sh %s/ldsc.sh %s %s %s %s %s"
                , PropsUtil.getProp(UKB.SRUN_4G)
                , getName()
                , BASE_DIR + getName()
                , output()
                , PropsUtil.getProp("ldsc.py")
                , PropsUtil.getProp("ldsc.dir")
                , PropsUtil.getProp("ldsc.snps")
                , PropsUtil.getProp("ldsc.ref")
        );
        runProcess(cmd);
    }
}
