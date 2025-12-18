package com.lzh.method;

/**
 * 方法类型
 */
public enum MethodType {
    PASCAL("PASCAL")
    , MAGMA("MAGMA")
    , SPREDIXCAN("SPREDIXCAN")
    , TFTWAS("TFTWAS")
    , SMR("SMR")
    , FUSION("FUSION")
    , UTMOST("UTMOST")
    , ISOTWAS("ISOTWAS")
    , LDSC("LDSC");

    public String name;

    MethodType(String name){
        this.name = name;
    }
}
