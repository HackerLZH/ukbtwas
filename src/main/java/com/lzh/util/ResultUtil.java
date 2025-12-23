package com.lzh.util;

import com.lzh.UKB;
import com.lzh.method.MethodType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 结果处理
 */
public class ResultUtil {
    private List<Result> resultList = new ArrayList<>();
    private List<LDSCResult> ldscResultList = new ArrayList<>();
    private List<ISOResult> isoResultList = new ArrayList<>();
    private static final String TISSUE = "Whole Blood";
    private static final String decimalFmt = "%.2e";

    public void pascal(MethodType type) {
        for (UKB.Gene gene : UKB.geneList) {
            for (UKB.Trait trait : UKB.traitList) {
                for (String sex : UKB.sexList) {
                    String result = input(trait.getName(), gene.getChr(), sex, type.name) + "/trait.pascal.sum.genescores.txt";
                    try (BufferedReader br = Files.newBufferedReader(Paths.get(result))) {
                        List<String> list = br.lines().collect(Collectors.toList());
                        for (String line : list) {
                            String[] lines = line.split("\\s+");
                            if (lines[5].equals(gene.getSymbol())) {
                                resultList.add(new Result(
                                        type.name
                                        , gene.getSymbol()
                                        , trait.getName()
                                        , sex
                                        , "-"
                                        , String.format(decimalFmt, Double.parseDouble(lines[7]))
                                        , "-"
                                        , "-"
                                ));
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void magma(MethodType type) {
        for (UKB.Gene gene : UKB.geneList) {
            for (UKB.Trait trait : UKB.traitList) {
                for (String sex : UKB.sexList) {
                    String result = input(trait.getName(), gene.getChr(), sex, type.name) + "/result.genes.out";
                    try (BufferedReader br = Files.newBufferedReader(Paths.get(result))) {
                        List<String> list = br.lines().collect(Collectors.toList());
                        for (String line : list) {
                            String[] lines = line.split("\\s+");
                            if (lines[0].equals(String.valueOf(gene.getNcbi()))) {
                                resultList.add(new Result(
                                        type.name
                                        , gene.getSymbol()
                                        , trait.getName()
                                        , sex
                                        , "-"
                                        , String.format(decimalFmt, Double.parseDouble(lines[8]))
                                        , String.format(decimalFmt, Double.parseDouble(lines[7]))
                                        , "-"
                                ));
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void spred(MethodType type) {
        for (UKB.Gene gene : UKB.geneList) {
            for (UKB.Trait trait : UKB.traitList) {
                for (String sex : UKB.sexList) {
                    String result = input(trait.getName(), gene.getChr(), sex, type.name) + "/result.csv";
                    try (BufferedReader br = Files.newBufferedReader(Paths.get(result))) {
                        List<String> list = br.lines().collect(Collectors.toList());
                        for (String line : list) {
                            String[] lines = line.split(",");
                            if (lines[1].equals(gene.getSymbol())) {
                                resultList.add(new Result(
                                        type.name
                                        , gene.getSymbol()
                                        , trait.getName()
                                        , sex
                                        , TISSUE
                                        , String.format(decimalFmt, Double.parseDouble(lines[4]))
                                        , String.format(decimalFmt, Double.parseDouble(lines[2]))
                                        , String.format(decimalFmt, Double.parseDouble(lines[3]))
                                ));
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void tf(MethodType type) {
        for (UKB.Gene gene : UKB.geneList) {
            for (UKB.Trait trait : UKB.traitList) {
                for (String sex : UKB.sexList) {
                    for (String tissue: new String[]{"brain", "breast", "lung", "prostate"}) {
                        String result = input(trait.getName(), gene.getChr(), sex, type.name) + "/" + tissue + ".csv";
                        try (BufferedReader br = Files.newBufferedReader(Paths.get(result))) {
                            List<String> list = br.lines().collect(Collectors.toList());
                            for (String line : list) {
                                String[] lines = line.split(",");
                                if (lines[1].equals(gene.getSymbol())) {
                                    resultList.add(new Result(
                                            type.name
                                            , gene.getSymbol()
                                            , trait.getName()
                                            , sex
                                            , tissue
                                            , String.format(decimalFmt, Double.parseDouble(lines[4]))
                                            , String.format(decimalFmt, Double.parseDouble(lines[2]))
                                            , String.format(decimalFmt, Double.parseDouble(lines[3]))
                                    ));
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void fusion(MethodType type) {
        for (UKB.Gene gene : UKB.geneList) {
            for (UKB.Trait trait : UKB.traitList) {
                for (String sex : UKB.sexList) {
                    String result = input(trait.getName(), gene.getChr(), sex, type.name) + "/result.txt";
                    try (BufferedReader br = Files.newBufferedReader(Paths.get(result))) {
                        List<String> list = br.lines().collect(Collectors.toList());
                        for (String line : list) {
                            String[] lines = line.split("\\s+");
                            if (lines[2].startsWith(gene.getEnsembl())) {
                                String P = lines[19].equals("NA") ? "NA" : String.format(decimalFmt, Double.parseDouble(lines[19]));
                                String Z = lines[18].equals("NA") ? "NA" : String.format(decimalFmt, Double.parseDouble(lines[18]));
                                resultList.add(new Result(
                                        type.name
                                        , gene.getSymbol()
                                        , trait.getName()
                                        , sex
                                        , TISSUE
                                        , P
                                        , Z
                                        , "-"
                                ));
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void smr(MethodType type) {
        for (UKB.Gene gene : UKB.geneList) {
            for (UKB.Trait trait : UKB.traitList) {
                for (String sex : UKB.sexList) {
                    String result = input(trait.getName(), gene.getChr(), sex, type.name) + "/result.smr";
                    try (BufferedReader br = Files.newBufferedReader(Paths.get(result))) {
                        List<String> list = br.lines().collect(Collectors.toList());
                        for (String line : list) {
                            String[] lines = line.split("\\s+");
                            if (lines[2].equals(gene.getSymbol())) {
                                resultList.add(new Result(
                                        type.name
                                        , gene.getSymbol()
                                        , trait.getName()
                                        , sex
                                        , TISSUE
                                        , String.format(decimalFmt, Double.parseDouble(lines[18]))
                                        , "-"
                                        , String.format(decimalFmt, Double.parseDouble(lines[16]))
                                ));
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void utmost(MethodType type) {
        for (UKB.Gene gene : UKB.geneList) {
            for (UKB.Trait trait : UKB.traitList) {
                for (String sex : UKB.sexList) {
                    String result = UKB.RESULT + "/" + trait.getName() + "/" + type.name + "/" + sex + "/step2/joint_1_17290.txt";
                    try (BufferedReader br = Files.newBufferedReader(Paths.get(result))) {
                        List<String> list = br.lines().collect(Collectors.toList());
                        for (String line : list) {
                            String[] lines = line.split("\\s+");
                            if (lines[0].equals(gene.getSymbol())) {
                                String Z = lines[1].equals("NA") ? "NA" : String.format(decimalFmt, Double.parseDouble(lines[1]));
                                String P = lines[2].equals("NA") ? "NA" : String.format(decimalFmt, Double.parseDouble(lines[2]));
                                resultList.add(new Result(
                                        type.name
                                        , gene.getSymbol()
                                        , trait.getName()
                                        , sex
                                        , TISSUE
                                        , P
                                        , Z
                                        , "-"
                                ));
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void ldsc(MethodType type) {
        // Total Observed scale h2: -0.0736 (0.0831)
        Pattern pattern = Pattern.compile("(-?\\d+\\.\\d+)\\s+\\((-?\\d+\\.\\d+)\\)");
        for (UKB.Trait trait : UKB.traitList) {
            for (UKB.Gene gene : UKB.geneList) {
                for (String sex : UKB.sexList) {
                    String result = UKB.RESULT + "/" + trait.getName() + "/" + type.name + "/" + gene.getSymbol() + "/" + sex + "/ldsc.log";
                    try (BufferedReader br = Files.newBufferedReader(Paths.get(result))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (line.startsWith("Total Observed")) {
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.find()) {
                                    ldscResultList.add(new LDSCResult(
                                            type.name
                                            , trait.getName()
                                            , sex
                                            , String.format(decimalFmt, Double.parseDouble(matcher.group(1)))
                                            , String.format(decimalFmt, Double.parseDouble(matcher.group(2)))
                                    ));
                                }
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void iso(MethodType type) {
        for (UKB.Gene gene : UKB.geneList) {
            for (UKB.Trait trait : UKB.traitList) {
                for (String sex : UKB.sexList) {
                    String result = input(trait.getName(), gene.getChr(), sex, type.name) + "/" + gene.getEnsembl() + ".txt";
                    try (BufferedReader br = Files.newBufferedReader(Paths.get(result))) {
                        br.readLine();
                        br.lines().forEach(line -> {
                            String[] lines = line.split("\\s+");
                            isoResultList.add(new ISOResult(
                                    type.name
                                    , trait.getName()
                                    , sex
                                    , TISSUE
                                    , gene.getSymbol()
                                    , lines[1]
                                    , String.format(decimalFmt, Double.parseDouble(lines[3]))
                                    , String.format(decimalFmt, Double.parseDouble(lines[2]))
                            ));
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String input(String t, Integer chr, String s, String method) {
        StringBuilder sb = new StringBuilder();
        sb.append(UKB.RESULT).append("/").append(t).append("/").append(method)
                .append("/").append(chr).append("/").append(s);
        return sb.toString();
    }

    // 其他结果
    private static class Result {
        String method;
        String gene;
        String trait;
        String sex;
        String tissue;
        String p;
        String z;
        String beta;

        public Result(String method, String gene, String trait, String sex, String tissue, String p, String z, String beta) {
            this.method = method;
            this.gene = gene;
            this.trait = trait;
            this.sex = sex;
            this.tissue = tissue;
            this.p = p;
            this.z = z;
            this.beta = beta;
        }
    }

    private static class LDSCResult {
        String method;
        String trait;
        String sex;
        String h;
        String p;

        public LDSCResult(String method, String trait, String sex, String h, String p) {
            this.method = method;
            this.trait = trait;
            this.sex = sex;
            this.h = h;
            this.p = p;
        }
    }

    private static class ISOResult {
        String method;
        String trait;
        String sex;
        String tissue;
        String gene;
        String feature;
        String p;
        String z;

        public ISOResult(String method, String trait, String sex, String tissue, String gene, String feature, String p, String z) {
            this.method = method;
            this.trait = trait;
            this.sex = sex;
            this.tissue = tissue;
            this.gene = gene;
            this.feature = feature;
            this.p = p;
            this.z = z;
        }
    }

    public void write() {
        if (!resultList.isEmpty()) {
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(UKB.RESULT + "/result.txt"))) {
                bw.write("method\tgene\ttrait\tsex\ttissue\tp\tz\tbeta\n");
                for (Result result : resultList) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(result.method)
                            .append("\t")
                            .append(result.gene)
                            .append("\t")
                            .append(result.trait)
                            .append("\t")
                            .append(result.sex)
                            .append("\t")
                            .append(result.tissue)
                            .append("\t")
                            .append(result.p)
                            .append("\t")
                            .append(result.z)
                            .append("\t")
                            .append(result.beta)
                            .append("\n");
                    bw.write(sb.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!ldscResultList.isEmpty()) {
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(UKB.RESULT + "/ldsc.txt"))) {
                bw.write("method\ttrait\tsex\th\tp\n");
                for (LDSCResult result : ldscResultList) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(result.method)
                            .append("\t")
                            .append(result.trait)
                            .append("\t")
                            .append(result.sex)
                            .append("\t")
                            .append(result.h)
                            .append("\t")
                            .append(result.p)
                            .append("\n");
                    bw.write(sb.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!isoResultList.isEmpty()) {
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(UKB.RESULT + "/iso.txt"))) {
                bw.write("method\ttrait\tsex\ttissue\tgene\tisoform\tp\tz\n");
                for (ISOResult result : isoResultList) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(result.method)
                            .append("\t")
                            .append(result.trait)
                            .append("\t")
                            .append(result.sex)
                            .append("\t")
                            .append(result.tissue)
                            .append("\t")
                            .append(result.gene)
                            .append("\t")
                            .append(result.feature)
                            .append("\t")
                            .append(result.p)
                            .append("\t")
                            .append(result.z)
                            .append("\n");
                    bw.write(sb.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
