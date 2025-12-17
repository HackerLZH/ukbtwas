package com.lzh.method;

import com.lzh.UKB;
import com.lzh.util.PropsUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PASCAL extends AbstractMethod{
    public PASCAL(UKB.Trait trait, String sex) {
        super(trait, sex);
    }

    @Override
    protected String getName() {
        return "PASCAL";
    }

    @Override
    protected void prepare() {
        for (UKB.Trait t : UKB.traitList) {
            for (Integer chr : UKB.chrSet) {
                for (String s : UKB.sexList) {
                    try {
                        Files.createDirectories(Paths.get(output(t.getName(), chr, s)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    protected void runScript() {
        for (UKB.Trait t : UKB.traitList) {
            for (Integer chr : UKB.chrSet) {
                for (String s : UKB.sexList) {
                    String OUT_DIR = output(t.getName(), chr, s);
                    String SUMM = summ(t.getName(), chr, s);
                    String cmd = String.format("%s --job-name=pascal sh %s/pascal.sh %s %s %s"
                                                , PropsUtil.getProp(UKB.SRUN_4G), BASE_DIR + getName(), OUT_DIR, SUMM, PropsUtil.getProp("pascal"));
                    runProcess(cmd);
                }
            }
        }
    }
}
