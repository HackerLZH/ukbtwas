package com.lzh.method;

public class AbstractMethodFactory {
    public static AbstractMethod getMethod(MethodType type, String dir) {
        switch (type) {
            case PASCAL:
                return new PASCAL();
            case MAGMA:
                return new MAGMA();
            case FUSION:
                return new FUSION();
            case SPREDIXCAN:
                return new SPREDIXCAN();
            case TFTWAS:
                return new TFTWAS();
            case SMR:
                return new SMR();
            case UTMOST:
                return new UTMOST();
            case ISOTWAS:
                return new ISOTWAS();
            default:
                return null;
        }
    }
}
