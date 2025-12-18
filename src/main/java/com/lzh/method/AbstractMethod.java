package com.lzh.method;

import com.lzh.UKB;
import com.lzh.util.PropsUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class AbstractMethod {
    private UKB.Trait trait;
    private String sex;
    private String name; //方法名
    protected static final String BASE_DIR = UKB.OUTPUT + "/script/";
//    // 因为待分析的基因可能位于相同染色体，这种情况只需要分析一次;不同方法实例独享set，相同方法实例共享set；线程安全
//    private static final Map<Class<? extends AbstractMethod>, Set<Integer>> chrMap = new ConcurrentHashMap<>();
//    private Set<Integer> getSharedSet() {
//        return chrMap.computeIfAbsent(getClass(), k -> Collections.synchronizedSet(new HashSet<>()));
//    }
//    protected final boolean add(Integer chr) {
//        return getSharedSet().add(chr);
//    }

    private AbstractMethod() {}

    public AbstractMethod(UKB.Trait trait, String sex, String name) {
        this.trait = trait;
        this.sex = sex;
        this.name = name;
    }

    public final void execute() {
        try {
            copyScript();
            prepare();
            runScript();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 方法名
     * @return 方法名
     */
    protected final String getName() {
        return name;
    }

    /**
     * 准备
     */
    protected abstract void prepare();
    /**
     * 运行脚本
     */
    protected abstract void runScript();

    /**
     * 拷贝脚本（提供执行脚本）
     */
    private void copyScript() throws Exception {
        // 同一种方法只拷贝一次
        synchronized (getClass()) {
            String resource = "script/" + getName();
            Path target = Paths.get(BASE_DIR + getName());
            if (Files.exists(target)) {
                return;
            }
            copyFromJar(resource, target);
        }
    }

    /**
     * 从jar中复制指定资源目录到系统指定位置
     * @param resourceDir
     * @param targetBase
     * @throws Exception
     */
    private void copyFromJar(String resourceDir, Path targetBase) throws Exception {
        // 获取jar中资源链接
        URL url = getClass().getClassLoader().getResource(resourceDir);
        // 建立jar连接
        JarURLConnection jarConn = (JarURLConnection) url.openConnection();
        JarFile jarFile = jarConn.getJarFile();
        // 获取jar资源条目
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            // 只处理指定目录下的文件
            if (!name.startsWith(resourceDir + "/") || entry.isDirectory()) {
                continue;
            }
            // targetBase + 相对路径
            Path target = targetBase.resolve(name.substring(resourceDir.length() + 1));
            Files.createDirectories(target.getParent());

            try (InputStream in = jarFile.getInputStream(entry)) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * 结果输出目录
     * @param t 性状名
     * @param chr 染色体号
     * @param s 性别
     * @return 路径
     * 用于PASCAL
     */
    protected final String output(String t, Integer chr, String s) {
        StringBuilder sb = new StringBuilder();
        sb.append(UKB.RESULT).append("/").append(t).append("/").append(getName())
                .append("/").append(chr).append("/").append(s);
        return sb.toString();
    }

    /**
     * 结果输出目录
     * @param chr
     * @return
     * 用于非PASCAL，UTMOST, LDSC
     */
    protected final String output(Integer chr) {
        return output(trait.getName(), chr, sex);
    }

    /**
     * 结果输出目录
     * @return
     * 用于UTMOST,LDSC
     */
    protected final String output() {
        return UKB.RESULT + "/" + trait.getName() + "/" + getName() + "/" + sex;
    }
    /**
     * summary输入
     * @param t 性状名
     * @param chr 染色体号
     * @param s 性别
     * @return 路径
     * 用于PASCAL
     */
    protected final String summ(String t, Integer chr, String s) {
        StringBuilder sb = new StringBuilder();
        sb.append(UKB.SUMMARY).append("/").append(chr).append("/").append(t).append("/").append(s).append("/output/summ2.assoc.txt");
        return sb.toString();
    }

    /**
     * summary输入
     * @param chr
     * @return
     * 用于非PASCAL，UTMOST, LDSC
     */
    protected final String summ(Integer chr) {
        return summ(trait.getName(), chr, sex);
    }

    protected final void runProcess(String cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", cmd);
            // 丢弃输出
            pb.redirectOutput(new File("/dev/null"));
            pb.redirectError(new File("/dev/null"));
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用于utmost,LDSC合并summary
     */
    protected final void combineSummary() {
        List<Integer> chrList = new ArrayList<>(UKB.chrSet);
        Collections.sort(chrList);
        String target = output() + "/summ.assoc.txt";
        for (Integer chr : chrList) {
            String cmd = String.format("tail -n +2 %s >> %s"
                                        , summ(chr)
                                        , target
            );
            runProcess(cmd);
        }
        String sed = "sed -i '1i chr rs ps n_mis n_obs allele1 allele0 af beta se p_wald' " + target;
        runProcess(sed);
    }
}
