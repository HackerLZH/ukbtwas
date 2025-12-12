package com.lzh;

import cn.hutool.core.io.file.FileNameUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UKB {
    
    private final List<Gene> geneList = new ArrayList<>();
    private final List<Trait> traitList = new ArrayList<>();
    private final List<String> fieldList = new ArrayList<>();

    // 线程池
//    private ExecutorService pool;

    // 是否使用对应方法
    private boolean magma, pascal, fusion, iso, tf, utmost, spred, smr;
    // 是否直接开始twas
    private boolean twas;
    // 是否考虑性别
    private boolean sex;
    // dataId对应性状表
    private static final String PARTICIPANT = "/gpfs/chencao/Temporary_Files/ukbb_features/participant2.csv";
    // 将当前工作目录+output作为输出目录
    private static final String OUTPUT = System.getProperty("user.dir") + "/output";

    private UKB(){}

    public UKB(String[] args) {
        try {
            boolean geneOk = false;
            boolean traitOk = false;
            boolean plinkOk = false;
            magma = pascal = fusion = iso = tf = utmost = spred = smr = false;
            twas = false;
            sex = false;

            // 处理传入参数
            for (int i = 0; i < args.length;) {
                switch (args[i]) {
                    case "--genes":
                        assert i < args.length - 1;
                        // 读取基因信息
                        geneOk = readGenes(args[i + 1]);
                        i += 2;
                        break;
                    case "--traits":
                        assert i < args.length - 1;
                        // 读取性状信息
                        traitOk = readTraits(args[i + 1]);
                        i += 2;
                        break;
                    case "--plink":
                        assert i < args.length - 1;
                        // 读取bed/bim/fam
                        plinkOk = readPlink(args[i + 1]);
                        i += 2;
                        break;
                    case "--sex":
                        i++;
                        sex = true;
                        break;
                    case "--magma":
                        i++;
                        magma = true;
                        break;
                    case "--pascal":
                        i++;
                        pascal = true;
                        break;
                    case "--fusion":
                        i++;
                        fusion = true;
                        break;
                    case "--iso":
                        i++;
                        iso = true;
                        break;
                    case "--tf":
                        i++;
                        tf = true;
                        break;
                    case "--utmost":
                        i++;
                        utmost = true;
                        break;
                    case "--spred":
                        i++;
                        spred = true;
                        break;
                    case "--smr":
                        i++;
                        smr = true;
                        break;
                    case "--twas":
                        i++;
                        twas = true;
                        break;
                    default:
                        System.out.println("invalid option with " + args[i]);
                        System.exit(0);
                }
            }

            if (!geneOk) {
                System.out.println("check your --genes");
                System.exit(0);
            }
            // --trait和--plink至少得有一个
            if (!traitOk && !plinkOk) {
                System.out.println("check your --trait or --plink");
                System.exit(0);
            }

            // 读取UKB dataID列表
            readFields();
            // 固定线程池（任务不会很多，不用担心OOM）
//            pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

//            File output = new File(OUTPUT);
//            if (!output.exists()) {
//                output.mkdirs();
//            }
        } catch (AssertionError e) {
            System.out.println("No params after --genes/--traits/--plink");
            System.exit(0);
        }
    }

    private void readFields() {
        InputStream in = getClass().getResourceAsStream("/field_order.txt");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                fieldList.add(line);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean readPlink(String arg) {
        // TODO readPlink
        return true;
    }

    private boolean readTraits(String arg) {
        for (String path : arg.split(",")) {
            Trait trait = new Trait();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(path))))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] arr = line.trim().split(":");
                    switch (arr[0]) {
                        case "type":
                            trait.setType(arr[1]);
                            break;
                        case "threshold":
                            trait.setThreshold(arr[1]);
                            break;
                        case "dataId":
                            trait.setDataId(arr[1].split(","));
                            break;
                        case "ICD10":
                            trait.setICD10(arr[1].split(","));
                            break;
                        case "self-report":
                            trait.setSelfReport(arr[1].split(","));
                            break;
                        default:
                            break;
                    }
                }
                // 根据文件名，设置性状名
                trait.setName(FileNameUtil.mainName(path));
                traitList.add(trait);
            } catch (IOException e) {
                System.out.println(path + " not found.");
                return false;
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                return false;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        return true;
    }

    private boolean readGenes(String arg) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(arg))))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] arr = line.trim().split(",");
                if (arr.length < Gene.props) {
                    throw new RuntimeException("please use English comma within " + arg);
                }
                geneList.add(new Gene(
                        arr[0].trim(),
                        Integer.parseInt(arr[1].trim()),
                        Integer.parseInt(arr[3].trim()),
                        arr[2].trim(),
                        Integer.parseInt(arr[4].trim()),
                        Integer.parseInt(arr[5].trim())
                ));
            }
        } catch (IOException e) {
            System.out.println(arg + " not found.");
            return false;
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    private static class Gene {
        private final static int props = 6;
        // GeneSymbol,NCBIGene,Ensembl,chr,start,end
        private String symbol;
        private Integer ncbi;
        private String ensembl;
        private Integer chr;
        private Integer start;
        private Integer end;

        private Gene() {}

        public Gene(String symbol, Integer ncbi, Integer chr, String ensembl, Integer start, Integer end) {
            this.symbol = symbol;
            this.ncbi = ncbi;
            this.chr = chr;
            this.ensembl = ensembl;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "Gene{" +
                    "symbol='" + symbol + '\'' +
                    ", ncbi=" + ncbi +
                    ", ensembl='" + ensembl + '\'' +
                    ", chr=" + chr +
                    ", start=" + start +
                    ", end=" + end +
                    '}';
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public Integer getNcbi() {
            return ncbi;
        }

        public void setNcbi(Integer ncbi) {
            this.ncbi = ncbi;
        }

        public String getEnsembl() {
            return ensembl;
        }

        public void setEnsembl(String ensembl) {
            this.ensembl = ensembl;
        }

        public Integer getChr() {
            return chr;
        }

        public void setChr(Integer chr) {
            this.chr = chr;
        }

        public Integer getStart() {
            return start;
        }

        public void setStart(Integer start) {
            this.start = start;
        }

        public Integer getEnd() {
            return end;
        }

        public void setEnd(Integer end) {
            this.end = end;
        }
    }

    private static class Trait {
//        type: [Q|B]
//        threshold:30
//        dataId:21001,23104
//        ICD10:K70.0,K76.0
//        self-report:high cholesterol
        private String name;
        private String type;
        private String threshold;
        private String[] dataId;
        private String[] ICD10;
        private String[] selfReport;


        public Trait() {}

        @Override
        public String toString() {
            return "Trait{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", threshold='" + threshold + '\'' +
                    ", dataId=" + Arrays.toString(dataId) +
                    ", ICD10=" + Arrays.toString(ICD10) +
                    ", selfReport=" + Arrays.toString(selfReport) +
                    '}';
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getThreshold() {
            return threshold;
        }

        public void setThreshold(String threshold) {
            this.threshold = threshold;
        }

        public String[] getICD10() {
            return ICD10;
        }

        public void setICD10(String[] ICD10) {
            this.ICD10 = ICD10;
        }

        public String[] getDataId() {
            return dataId;
        }

        public void setDataId(String[] dataId) {
            this.dataId = dataId;
        }

        public String[] getSelfReport() {
            return selfReport;
        }

        public void setSelfReport(String[] selfReport) {
            this.selfReport = selfReport;
        }
    }
    // 执行外部命令的异步任务
    private static class ProcessBuilderTask implements Runnable {
        String cmd;
        public ProcessBuilderTask(String cmd) {
            this.cmd = cmd;
        }
        @Override
        public void run() {
            try {
                new ProcessBuilder("sh", "-c", cmd).start().waitFor();
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

//    public List<Gene> getGeneList() {
//        return geneList;
//    }
//
//    public List<Trait> getTraitList() {
//        return traitList;
//    }


    // 提取性状
    private void extract() {
        // 使用CompletableFuture构建任务链
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Trait trait : traitList) {
            List<CompletableFuture<Void>> stageFutures = new ArrayList<>();
            String traitDir = OUTPUT + "/" + trait.getName();
            CompletableFuture<Void> future = CompletableFuture.runAsync(new ProcessBuilderTask("mkdir -p " + traitDir));
            // dataId
            if (trait.getDataId() != null) {
                for (String id : trait.getDataId()) {
                    int n = fieldList.indexOf("p" + id);
                    String cmd = String.format("awk -F, '(NR>1){print $1,$%d}' %s > %s", n, PARTICIPANT, OUTPUT + "/" + id + ".txt");
                    stageFutures.add(future.thenRunAsync(new ProcessBuilderTask(cmd)));
                }
//                CompletableFuture.allOf(stageFutures)
            }
        }
    }

    public static void main(String[] args) {
//        UKB ukb = new UKB();
        System.out.println(FileNameUtil.mainName("/a/b/c.tar.gz"));
    }
}
