package com.lzh.method;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public abstract class AbstractMethod {
    public AbstractMethod() {

    }

    public final void execute() {
        copyScript();
    }
    // 方法名
    protected abstract String getName();

    /**
     * 拷贝脚本（提供执行脚本）
     */
    protected void copyScript() {
        try {
            Path srcPath = Paths.get(getClass().getResource("/script/" + getName()).toURI());
            Path distPath = Paths.get("/tmp/lzh", getName());
            // 遍历目录树
            Files.walk(srcPath)
                    .forEach(source -> {
                        try {
                            // 先计算source相对于srcPath的相对路径relativePath，然后连接distPath+relativePath
                            Path target = distPath.resolve(srcPath.relativize(source));
                            if (Files.isDirectory(source)) {
                                Files.createDirectories(target);
                            } else {
                                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
