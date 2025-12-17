package com.lzh.util;

import java.io.File;
import java.io.IOException;

// 执行外部命令的异步任务
public class ProcessBuilderTask implements Runnable {
    String cmd;
    public ProcessBuilderTask(String cmd) {
        this.cmd = cmd;
    }
    @Override
    public void run() {
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", cmd);
            pb.redirectOutput(new File("/dev/null"));
            pb.redirectError(new File("/dev/null"));
            pb.start().waitFor();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}