package com.lzh;

import cn.hutool.core.io.file.FileNameUtil;
import com.lzh.method.AbstractMethodFactory;
import com.lzh.method.MethodType;
import com.lzh.util.LogUtil;
import com.lzh.util.ProcessBuilderTask;
import com.lzh.util.PropsUtil;
import com.lzh.util.ResultUtil;
import org.slf4j.Logger;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UKB {

    public static final List<Gene> geneList = new ArrayList<>();
    public static final List<Trait> traitList = new ArrayList<>();
    private final List<String> fieldList = new ArrayList<>();
    public static final List<String> sexList = new ArrayList<>();
    // 基因位于哪些染色体
    public static final Set<Integer> chrSet = new HashSet<>();
    // 待执行方法
    private final List<MethodType> methodList = new ArrayList<>();
    private final static Logger log = LogUtil.getLogger(UKB.class);

    private boolean step1; // 预处理
    private boolean step2; // 分析
    private boolean step3; // 收集结果

    // 是否考虑性别
    private boolean sex;
    // dataId对应表格
    private static final String PARTICIPANT = "/gpfs/chencao/Temporary_Files/ukbb_features/participant2.csv";
    // ICD10对应表格
    private static final String ICD10 = "/gpfs/chencao/Temporary_Files/ukbb_features/diseases/ICD10/diagno.csv";
    // self-report目录
    private static final String SELFREPORT = "/gpfs/chencao/Temporary_Files/ukbb_features/diseases/selfreport";
    // eur id
    private static final String EUR = "/gpfs/chencao/Temporary_Files/ukbb_features/eur.eid";
    // eur male id for plink --extract
    private static final String EUR_MALE = "/gpfs/chencao/Temporary_Files/ukbb_features/eur_male.eid";
    // eur female id for plink --extract
    private static final String EUR_FEMALE = "/gpfs/chencao/Temporary_Files/ukbb_features/eur_female.eid";
    // DRAGEN_WGS
    private static final String DRAGEN_WGS = "/gpfs/chencao/Temporary_Files/DRAGEN_WGS";
    // 将当前工作目录+output作为输出目录
    public static final String OUTPUT = System.getProperty("user.dir") + "/output";
    // summary目录
    public static final String SUMMARY = OUTPUT + "/summary";
    // 结果目录
    public static final String RESULT = OUTPUT + "/result";

    private static final String PLINK = "plink";
    private static final String GEMMA = "gemma";
    public static final String SRUN_4G = "SRUN4G";
    public static final String SRUN_8G = "SRUN8G";
    public static final String SRUN_32G = "SRUN32G";

    private UKB(){}

    public UKB(String[] args) {
        try {
            boolean geneOk = false;
            boolean traitOk = false;
            boolean plinkOk = false;
            step1 = step2 = step3 = false;
            sex = false;
            sexList.add("all");
            // 读取UKB dataID列表
            readFields();

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
                    case "--plinks":
                        assert i < args.length - 1;
                        // 读取bed/bim/fam
                        plinkOk = readPlinks(args[i + 1]);
                        i += 2;
                        break;
                    case "--sex":
                        i++;
                        sex = true;
                        sexList.add("male");
                        sexList.add("female");
                        break;
                    case "--magma":
                        methodList.add(MethodType.MAGMA);
                        i++;
                        break;
                    case "--pascal":
                        methodList.add(MethodType.PASCAL);
                        i++;
                        break;
                    case "--fusion":
                        methodList.add(MethodType.FUSION);
                        i++;
                        break;
                    case "--iso":
                        methodList.add(MethodType.ISOTWAS);
                        i++;
                        break;
                    case "--tf":
                        methodList.add(MethodType.TFTWAS);
                        i++;
                        break;
                    case "--utmost":
                        methodList.add(MethodType.UTMOST);
                        i++;
                        break;
                    case "--spred":
                        methodList.add(MethodType.SPREDIXCAN);
                        i++;
                        break;
                    case "--smr":
                        methodList.add(MethodType.SMR);
                        i++;
                        break;
                    case "--ldsc":
                        methodList.add(MethodType.LDSC);
                        i++;
                        break;
                    case "--step1":
                        i++;
                        step1 = true;
                        break;
                    case "--step2":
                        i++;
                        step2 = true;
                        break;
                    case "--step3":
                        i++;
                        step3 = true;
                        break;
                    default:
                        log.error("invalid option with " + args[i]);
                        System.exit(0);
                }
            }

            if (!geneOk) {
                log.warn("check your --genes");
                System.exit(0);
            }
            // --trait和--plink至少得有一个
            if (!traitOk && !plinkOk) {
                log.warn("check your --trait or --plink");
                System.exit(0);
            }

        } catch (AssertionError e) {
            log.error("No params after options");
            log.error(getStackTrace(e));
            System.exit(0);
        }
    }

    private void readFields() {
        InputStream in = getClass().getResourceAsStream("/field_order.txt");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            br.lines().forEach(fieldList::add);
        } catch (IOException e) {
            log.error(getStackTrace(e));
        }
    }

    private boolean readPlinks(String arg) {
        // TODO readPlink

        return true;
    }

    private boolean readTraits(String arg) {
        for (String path : arg.split(",")) {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
                Trait trait = new Trait();
                br.lines().forEach(line -> {
                    String[] arr = line.trim().split(":");
                    switch (arr[0]) {
                        case "type":
                            trait.setType(arr[1]);
                            break;
                        case "threshold":
                            trait.setThreshold(arr[1]);
                            break;
                        case "dataId":
                            // 找到dataId索引
                            List<Integer> dataIdIndexes = new ArrayList<>();
                            for (String id : arr[1].trim().split(",")) {
                                for (int i = 0; i < fieldList.size(); ++i) {
                                    if (fieldList.get(i).startsWith("p" + id)) {
                                        dataIdIndexes.add(i);
                                        break;
                                    }
                                }
                            }
                            trait.setDataIdIndexes(dataIdIndexes);
                            break;
                        case "ICD10":
                            trait.setICD10(arr[1].trim().split(","));
                            break;
                        case "self-report":
                            trait.setSelfReport(arr[1].trim().split(","));
                            break;
                        default:
                            break;
                    }
                });
                // 根据文件名，设置性状名
                trait.setName(FileNameUtil.mainName(path));
                traitList.add(trait);
            } catch (IOException e) {
                log.error(path + " not found.");
                log.error(getStackTrace(e));
                return false;
            } catch (RuntimeException e) {
                log.error(getStackTrace(e));
                return false;
            } catch (Exception e) {
                log.error(getStackTrace(e));
                return false;
            }
        }
        return true;
    }

    private boolean readGenes(String arg) {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(arg))) {
            br.lines().forEach(line -> {
                String[] arr = line.trim().split(",");
                if (arr.length < Gene.props) {
                    throw new RuntimeException("please use English comma within " + arg);
                }
                geneList.add(new Gene(
                        arr[0].trim(),
                        Integer.parseInt(arr[1].trim()),
                        Integer.parseInt(arr[3].trim()),
                        arr[2].trim(),
                        Long.parseLong(arr[4].trim()),
                        Long.parseLong(arr[5].trim())
                ));
            });
            geneList.forEach(gene -> chrSet.add(gene.getChr()));
        } catch (IOException e) {
            log.error(arg + " not found.");
            log.error(getStackTrace(e));
            return false;
        } catch (RuntimeException e) {
            log.error(getStackTrace(e));
            return false;
        } catch (Exception e) {
            log.error(getStackTrace(e));
            return false;
        }
        return true;
    }

    public static class Gene {
        private final static int props = 6;
        // GeneSymbol,NCBIGene,Ensembl,chr,start,end
        private String symbol;
        private Integer ncbi;
        private String ensembl;
        private Integer chr;
        private Long start;
        private Long end;

        private Gene() {}

        public Gene(String symbol, Integer ncbi, Integer chr, String ensembl, Long start, Long end) {
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

        public Long getStart() {
            return start;
        }

        public void setStart(Long start) {
            this.start = start;
        }

        public Long getEnd() {
            return end;
        }

        public void setEnd(Long end) {
            this.end = end;
        }
    }

    public static class Trait {
//        type: [Q|B]
//        threshold:30
//        dataId:21001,23104
//        ICD10:K70.0,K76.0
//        self-report:high cholesterol
        private String name;
        private String type;
        private String threshold;
        private List<Integer> dataIdIndexes; // dataId索引
        private String[] ICD10;
        private String[] selfReport;


        public Trait() {}

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

        public List<Integer> getDataIdIndexes() {
            return dataIdIndexes;
        }

        public void setDataIdIndexes(List<Integer> dataIdIndexes) {
            this.dataIdIndexes = dataIdIndexes;
        }

        public String[] getSelfReport() {
            return selfReport;
        }

        public void setSelfReport(String[] selfReport) {
            this.selfReport = selfReport;
        }
    }

    private List<Gene> getGeneList() {
        return geneList;
    }

    private List<Trait> getTraitList() {
        return traitList;
    }

    /**
     * 创建目录
     * @param dir
     * @return false表示之前已经创建过了
     */
    private boolean createDirs(String dir) {
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
            return true;
        }
        return false;
    }
    // 获取异常信息字符串
    private String getStackTrace(Exception e) {
        try (StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return sw.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getStackTrace(Error e) {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return sw.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 提取连续性性状
     * @param idSets 作为case的样本Id集合
     * @param values 每个样本的特征值数组
     * @param dataIdIndexes 待提取特征索引
     * @param thresh 连续性性状阈值
     */
    private void extractQuants(Set<String> idSets, String[] values, List<Integer> dataIdIndexes, double thresh) {

        dataIdIndexes.forEach(i -> {
            // 暂时设置大于阈值时为case
            if (!values[i].isEmpty() && Double.parseDouble(values[i]) > thresh) {
                idSets.add(values[0]);
            }
        });

    }

    /**
     * 提取二分类性状
     * @param idSets 作为case的样本Id集合
     * @param values 每个样本的特征值数组
     * @param dataIdIndexes 待提取特征索引
     */
    private void extractBinaries(Set<String> idSets, String[] values, List<Integer> dataIdIndexes) {
        // 暂时规定值不为空就是case
        dataIdIndexes.forEach(i -> {
            if (!values[i].isEmpty()) {
                idSets.add(values[0]);
            }
        });
    }

    /**
     * 提取ICD10
     * @param idSets
     * @param values
     * @param codes ICD10编码
     */
    private void extractICD10(Set<String> idSets, String[] values, String[] codes) {
        for (String code : codes) {
            if (values[1].contains(code)) {
                idSets.add(values[0]);
                break;
            }
        }
    }

    /**
     * 提取self-report
     * @param idSets
     * @param values
     * @param names
     */
    private void extractSelfReport(Set<String> idSets, String[] values, String[] names) {
        for (String name : names) {
            if (values[1].equals(name)) {
                idSets.add(values[0]);
                break;
            }
        }
    }

    /**
     * 提取性状
     */
    private void extract() {
        // trait对应case set
        Map<String, Set<String>> caseMap = new HashMap<>();
        // 有dataId的trait集合
        List<Trait> traitHasDataId = new ArrayList<>();
        // 有ICD10的trait集合
        List<Trait> traitHasICD10 = new ArrayList<>();
        // 有self-report的trait集合
        List<Trait> traitHasSelfReport = new ArrayList<>();
        traitList.forEach(trait -> {
            caseMap.put(trait.getName(), new HashSet<>());
            if (trait.getDataIdIndexes() != null) {
                traitHasDataId.add(trait);
            }
            if (trait.getICD10() != null) {
                traitHasICD10.add(trait);
            }
            if (trait.getSelfReport() != null) {
                traitHasSelfReport.add(trait);
            }
        });
        // a.获取来源于dataId的trait case
        log.info("提取dataId来源......");
        try(BufferedReader br = Files.newBufferedReader(Paths.get(PARTICIPANT))) {
            // 跳过标题
            br.readLine();
            br.lines().forEach(line -> {
                // 所有表型值
                String[] values =  line.trim().split(",");
                traitHasDataId.forEach(trait -> {
                    if (trait.getType().equals("Q")) {
                        extractQuants(caseMap.get(trait.getName()), values, trait.getDataIdIndexes(), Double.parseDouble(trait.getThreshold()));
                    } else {
                        extractBinaries(caseMap.get(trait.getName()), values, trait.getDataIdIndexes());
                    }
                });
            });
        } catch (Exception e) {
            log.error("提取失败:\n" + getStackTrace(e));
            System.exit(0);
        }
        log.info("提取成功！");
        // b.获取来源于ICD10的trait case
        log.info("提取ICD10来源......");
        try(BufferedReader br = Files.newBufferedReader(Paths.get(ICD10))) {
            br.readLine();
            br.lines().forEach(line -> {
                String[] values = line.trim().split(",");
                if (values.length > 1) {
                    traitHasICD10.forEach(trait -> extractICD10(caseMap.get(trait.getName()), values, trait.getICD10()));
                }
            });
        } catch (Exception e) {
            log.error("提取失败:\n" + getStackTrace(e));
            System.exit(0);
        }
        log.info("提取成功！");
        log.info("提取self-report来源......");
        for (int i = 0; i <= 33; ++i) {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(String.format("%s/selfreport_%d.csv", SELFREPORT, i)))) {
                br.readLine();
                br.lines().forEach(line -> {
                    String[] values = line.trim().split(",");
                    if (values.length > 1) {
                        traitHasSelfReport.forEach(trait -> extractSelfReport(caseMap.get(trait.getName()), values, trait.getSelfReport()));
                    }
                });
            } catch (Exception e) {
                log.error("提取失败:\n" + getStackTrace(e));
                System.exit(0);
            }
        }
        log.info("提取成功！");

//        caseMap.forEach((k, v) -> {
//            log.info(k + ":" + v.size());
//        });

        // 创建summary目录
        try {
            Files.createDirectories(Paths.get(SUMMARY));
        } catch (IOException e) {
            log.error(getStackTrace(e));
            System.exit(0);
        }


        // 和eur id求交集输出trait code，其中case=1,control=0
        Map<String, BufferedWriter> bwMap = new HashMap<>();
        caseMap.forEach((name, set) -> {
            try {
                bwMap.put(name, Files.newBufferedWriter(Paths.get(SUMMARY + "/" + name + ".code")));
            } catch (IOException e) {
                log.error(getStackTrace(e));
                System.exit(0);
            }
        });
        try (BufferedReader br = Files.newBufferedReader(Paths.get(EUR))) {
            br.lines().forEach(id -> {
                caseMap.forEach((name, set) -> {
                    try {
                        if (caseMap.get(name).contains(id)) {
                            bwMap.get(name).write("1\n");
                        } else {
                            bwMap.get(name).write("0\n");
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        } catch (Exception e) {
            log.error(getStackTrace(e));
            System.exit(0);
        } finally {
            bwMap.forEach((k, bw) -> {
                try {
                    bw.close();
                } catch (IOException e) {
                    log.error(getStackTrace(e));
                    System.exit(0);
                }
            });
        }
        caseMap.clear();

        // bed/bim/fam
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        chrSet.forEach(chr -> {
            traitList.forEach(trait -> {
                String o = SUMMARY + "/" + chr + "/" + trait.getName() + "/all";
                createDirs(o);
                String paste = String.format("paste -d' ' %s/chr%d.fam %s > %s",
                        DRAGEN_WGS, chr, SUMMARY + "/" + trait.getName() + ".code", o + "/plink.fam");
                String lnsBed = String.format("ln -s %s/chr%d.bed %s", DRAGEN_WGS, chr, o + "/plink.bed");
                String lnsBim = String.format("ln -s %s/chr%d.bim %s", DRAGEN_WGS, chr, o + "/plink.bim");
                futures.add(CompletableFuture.runAsync(new ProcessBuilderTask(paste))
                        .thenRunAsync(new ProcessBuilderTask(lnsBed)).thenRunAsync(new ProcessBuilderTask(lnsBim)));
            });
        });
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        // TODO maybe plinks begin here

        // 考虑性别
        if (sex) {
            Set<String> maleSet = new HashSet<>();
            Set<String> femaleSet = new HashSet<>();
            List<Integer> maleIndex = new ArrayList<>();
            List<Integer> femaleIndex = new ArrayList<>();
            // 对每个性状code划分sex
            try (BufferedReader brMale = Files.newBufferedReader(Paths.get(EUR_MALE));
                 BufferedReader brFemale = Files.newBufferedReader(Paths.get(EUR_FEMALE));
                 BufferedReader brEur = Files.newBufferedReader(Paths.get(EUR))) {
                brMale.lines().forEach(line -> maleSet.add(line.split("\\s+")[0]));
                brFemale.lines().forEach(line -> femaleSet.add(line.split("\\s+")[0]));
                int i = 0;
                String id;
                while ((id = brEur.readLine()) != null) {
                    if (maleSet.contains(id)) {
                        maleIndex.add(i);
                    } else if (femaleSet.contains(id)) {
                        femaleIndex.add(i);
                    }
                    ++i;
                }
                traitList.forEach(trait -> {
                    try (BufferedReader br = Files.newBufferedReader(Paths.get(SUMMARY + "/" + trait.getName() + ".code"));
                            BufferedWriter bw1 = Files.newBufferedWriter(Paths.get(SUMMARY + "/" + trait.getName() + "_male.code"));
                            BufferedWriter bw2 = Files.newBufferedWriter(Paths.get(SUMMARY + "/" + trait.getName() + "_female.code"))) {
                        List<String> codes = br.lines().collect(Collectors.toList());
                        for (int index : maleIndex) {
                            bw1.write(codes.get(index) + "\n");
                        }
                        for (int index : femaleIndex) {
                            bw2.write(codes.get(index) + "\n");
                        }
                    } catch (IOException e){
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                log.info(getStackTrace(e));
                System.exit(0);
            }
            // plink --keep
            futures.clear();
            chrSet.forEach(chr -> traitList.forEach(trait -> {
                String in = SUMMARY + "/" + chr + "/" + trait.getName() + "/all";
                String o1 = SUMMARY + "/" + chr + "/" + trait.getName() + "/male";
                String o2 = SUMMARY + "/" + chr + "/" + trait.getName() + "/female";
                createDirs(o1);
                createDirs(o2);
                String plink1 = String.format("%s %s --bfile %s --keep %s --memory 4096 --make-bed --out %s",
                                                    PropsUtil.getProp(SRUN_4G), PropsUtil.getProp(PLINK), in + "/plink", EUR_MALE, o1 + "/plink");
                String plink2 = String.format("%s %s --bfile %s --keep %s --memory 4096 --make-bed --out %s",
                                                    PropsUtil.getProp(SRUN_4G), PropsUtil.getProp(PLINK), in + "/plink", EUR_FEMALE, o2 + "/plink");
                String paste1 = String.format("paste -d' ' %s %s > %s && mv %s %s",
                        o1 + "/plink.fam", SUMMARY + "/" + trait.getName() + "_male.code", o1 + "/tmp.txt", o1 + "/tmp.txt", o1 + "/plink.fam");
                String paste2 = String.format("paste -d' ' %s %s > %s && mv %s %s",
                        o2 + "/plink.fam", SUMMARY + "/" + trait.getName() + "_female.code", o2 + "/tmp.txt", o2 + "/tmp.txt", o2 + "/plink.fam");
                futures.add(CompletableFuture.runAsync(new ProcessBuilderTask(plink1)).thenRunAsync(new ProcessBuilderTask(paste1)));
                futures.add(CompletableFuture.runAsync(new ProcessBuilderTask(plink2)).thenRunAsync(new ProcessBuilderTask(paste2)));
            }));
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        log.info("plink准备完毕，开始计算summary（大约耗时1~2h，请耐心等待）");
        futures.clear();
        chrSet.forEach(chr -> traitList.forEach(trait -> sexList.forEach(s -> {
            String dir = SUMMARY + "/" + chr + "/" + trait.getName() + "/" + s;
            String cmd = String.format("cd %s && %s %s -bfile %s -notsnp -n 2 -lm 1 -o summ",
                                        dir, PropsUtil.getProp(SRUN_4G), PropsUtil.getProp(GEMMA), dir + "/plink");
            futures.add(CompletableFuture.runAsync(new ProcessBuilderTask(cmd))
                                            .thenRunAsync(() -> {
                                                String summ1 = dir + "/output/summ.assoc.txt";
                                                String summ2 = dir + "/output/summ2.assoc.txt";
                                                try (BufferedReader br = Files.newBufferedReader(Paths.get(summ1));
                                                     BufferedWriter bw = Files.newBufferedWriter(Paths.get(summ2));
                                                     BufferedReader hg37 = Files.newBufferedReader(Paths.get("/gpfs/chencao/Temporary_Files/chr" + chr + "_hg37"))) {
                                                    // hg38 -> hg37
                                                    Map<String, String[]> map = new HashMap<>();
                                                    Set<String> set = new HashSet<>();
                                                    long maxPos = 0, minPos;
                                                    // 写入标题
                                                    bw.write(String.join(" ", br.readLine().split("\\s+")) + "\n");
                                                    // 从第一行获取最小pos
                                                    String line = br.readLine();
                                                    String[] lines = line.split("\\s+");
                                                    minPos = Long.parseLong(lines[2]);
                                                    map.put(lines[1], lines);
                                                    while ((line = br.readLine()) != null) {
                                                        lines = line.split("\\s+");
                                                        maxPos = Long.parseLong(lines[2]);
                                                        map.put(lines[1], lines);
                                                    }
                                                    while ((line = hg37.readLine()) != null) {
                                                        lines = line.split("\\s+");
                                                        if (Long.parseLong(lines[1]) < minPos) {
                                                            continue;
                                                        }
                                                        if (Long.parseLong(lines[1]) > maxPos) {
                                                            break;
                                                        }
                                                        if (map.containsKey(lines[2]) && !set.contains(lines[2])) {
                                                            String[] finalLine = map.get(lines[2]);
                                                            finalLine[2] = lines[1];
                                                            set.add(lines[2]);
                                                            bw.write(String.join(" ", finalLine) + "\n");
                                                        }
                                                    }
                                                    // 删除原summ
                                                    Files.delete(Paths.get(summ1));
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }));
            })));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void run() {
        log.info("summary准备完毕，开始执行TWAS......");
        try {
            Files.createDirectories(Paths.get(RESULT));
        } catch (IOException e) {
            log.error(getStackTrace(e));
            System.exit(0);
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        // pascal只能顺序执行，避免文件覆盖
        if (methodList.contains(MethodType.PASCAL)) {
            futures.add(CompletableFuture.runAsync(() -> AbstractMethodFactory.getMethod(MethodType.PASCAL, null, null).execute()));
        }
        // 其他方法按性状性别划分
        methodList.forEach(
                m -> {
                    if (m == MethodType.PASCAL) {
                        return;
                    }
                    traitList.forEach(
                        trait -> sexList.forEach(sex -> {
                            futures.add(CompletableFuture.runAsync(() -> AbstractMethodFactory.getMethod(m, trait, sex).execute()));
                        })
                    );
                }
        );

//        traitList.forEach(
//            trait -> sexList.forEach(sex -> {
//                futures.add(CompletableFuture.runAsync(() -> AbstractMethodFactory.getMethod(MethodType.MAGMA, trait, sex).execute()));
//            })
//        );

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("执行完毕！");
    }

    // 收集结果
    private void collect() {
        log.info("整合结果...");
        ResultUtil util = new ResultUtil();
        for (MethodType type : methodList) {
            switch (type) {
                case PASCAL:
                    util.pascal(type);
                    break;
                case MAGMA:
                    util.magma(type);
                    break;
                case SPREDIXCAN:
                    util.spred(type);
                    break;
                case TFTWAS:
                    util.tf(type);
                    break;
                case SMR:
                    util.smr(type);
                    break;
                case FUSION:
                    util.fusion(type);
                    break;
                case UTMOST:
                    util.utmost(type);
                    break;
                case ISOTWAS:
                    util.iso(type);
                    break;
                case LDSC:
                    util.ldsc(type);
                    break;
            }
        }
        util.write();
        log.info("整合完毕！");
    }

    public void start() {
        // 默认全部执行
        if (!step1 && !step2 && !step3) {
            extract();
            run();
            collect();
            return;
        }
        if (step1) {
            extract();
        }
        if (step2) {
            run();
        }
        if (step3) {
            collect();
        }
    }
}
