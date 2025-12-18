package com.lzh.method;

import com.lzh.UKB;
import com.lzh.util.PropsUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

public class UTMOST extends AbstractMethod{

    public UTMOST(UKB.Trait trait, String sex, String name) {
        super(trait, sex, name);
    }

    @Override
    protected void prepare() {
        try {
            Files.createDirectories(Paths.get(output() + "/step1"));
            Files.createDirectories(Paths.get(output() + "/step2"));
            combineSummary();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void runScript() {
        String cmd = String.format("%s --job-name=%s sh %s/utmost.sh %s %s %s"
                , PropsUtil.getProp(UKB.SRUN_32G)
                , getName()
                , BASE_DIR + getName()
                , PropsUtil.getProp("utmost.bin")
                , PropsUtil.getProp("utmost.dir")
                , output()
        );
        runProcess(cmd);
    }
}
