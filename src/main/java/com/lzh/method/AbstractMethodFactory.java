package com.lzh.method;

import com.lzh.UKB;

public class AbstractMethodFactory {
    public static AbstractMethod getMethod(MethodType type, UKB.Trait trait, String sex) {
        switch (type) {
            case PASCAL:
                return new PASCAL(trait, sex);
            case MAGMA:
                return new MAGMA(trait, sex);
            case FUSION:
                return new FUSION(trait, sex);
            case SPREDIXCAN:
                return new SPREDIXCAN(trait, sex);
            case TFTWAS:
                return new TFTWAS(trait, sex);
            case SMR:
                return new SMR(trait, sex);
            case UTMOST:
                return new UTMOST(trait, sex);
            case ISOTWAS:
                return new ISOTWAS(trait, sex);
            case LDSC:
                return new LDSC(trait, sex);
            default:
                return null;
        }
    }
}
