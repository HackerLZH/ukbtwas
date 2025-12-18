package com.lzh.method;

import com.lzh.UKB;

public class AbstractMethodFactory {
    public static AbstractMethod getMethod(MethodType type, UKB.Trait trait, String sex) {
        switch (type) {
            case PASCAL:
                return new PASCAL(trait, sex, type.name);
            case MAGMA:
                return new MAGMA(trait, sex, type.name);
            case FUSION:
                return new FUSION(trait, sex, type.name);
            case SPREDIXCAN:
                return new SPREDIXCAN(trait, sex, type.name);
            case TFTWAS:
                return new TFTWAS(trait, sex, type.name);
            case SMR:
                return new SMR(trait, sex, type.name);
            case UTMOST:
                return new UTMOST(trait, sex, type.name);
            case ISOTWAS:
                return new ISOTWAS(trait, sex, type.name);
            case LDSC:
                return new LDSC(trait, sex, type.name);
            default:
                return null;
        }
    }
}
