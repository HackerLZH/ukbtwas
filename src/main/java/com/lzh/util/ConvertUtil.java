package com.lzh.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理各种转换
 */
public class ConvertUtil {

    private static final String hg38_format = "/gpfs/chencao/zhenghuili/data/hg38/chr%s";

    public static void accept(String[] args) {
        switch (args[1]) {
            case "bim38id":
                bim38id(args[2], args[3]);
                break;
        }
    }

    /**
     * UKB bim中38的chr:pos转为rsid
     * @param bim
     * @param chr
     */
    private static void bim38id(String bim, String chr) {
        Map<Long, String> map = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(bim));
            BufferedReader hg38 = Files.newBufferedReader(Paths.get(String.format(hg38_format, chr)));
             BufferedWriter bw = Files.newBufferedWriter(Paths.get("result.bim"))) {
            br.lines().forEach(line -> {
                String[] splits = line.split("\\s+");
                map.put(Long.parseLong(splits[3]), splits[4] + splits[5]);
            });

            hg38.lines().forEach(line -> {
                String[] splits = line.split("\\s+");
                if (map.containsKey(Long.parseLong(splits[1]))) {
                    String alleles = map.get(Long.parseLong(splits[1]));
                    boolean ok = false;
                    for (String a1 : splits[3].split(",")) {
                        for (String a2 : splits[4].split(",")) {
                            if ((a1 + a2).equals(alleles)) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(chr).append("\t").append(splits[2])
                                        .append("\t").append(0)
                                        .append("\t").append(splits[1])
                                        .append("\t").append(a1)
                                        .append("\t").append(a2)
                                        .append("\n");
                                try {
                                    bw.write(sb.toString());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                ok = true;
                                break;
                            }
                        }
                        if (ok) {
                            break;
                        }
                    }

                    if (!ok) {
                        for (String a1 : splits[4].split(",")) {
                            for (String a2 : splits[3].split(",")) {
                                if ((a1 + a2).equals(alleles)) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(chr).append("\t").append(splits[2])
                                            .append("\t").append(0)
                                            .append("\t").append(splits[1])
                                            .append("\t").append(a1)
                                            .append("\t").append(a2)
                                            .append("\n");
                                    try {
                                        bw.write(sb.toString());
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    ok = true;
                                    break;
                                }
                            }
                            if (ok) {
                                break;
                            }
                        }
                    }
                }
            });
            System.out.println("结果写入result.bim");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
