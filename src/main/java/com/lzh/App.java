package com.lzh;

import com.lzh.util.ConvertUtil;

/**
 * Hello world!
 *
 */
public class App 
{
    private final static String GITHUB = "https://github.com/HackerLZH/ukbtwas";
    public static void main( String[] args )
    {
        if (args.length == 0) {
            System.out.format("Welcome to use this tool!\n" +
                    "Usage:\n" +
                    "java -jar xxx.jar\n" +
                    "--param [input_file]\n" +
                    "--genes [input_file]\n" +
                    "--traits [trait1,trait2,...,traitn]\n" +
                    "--trait2 [trait1.code,trait2.code,...,traitn.code]\n" +
                    "--sex\n" +
                    "--[method1]\n--[method2]\n...\n\n" +
                    "You can access %s for more details.\n" +
                    "Warning: your java version must be equal to or greater than jdk8!!!\n", GITHUB);
        } else if (args[0].startsWith("--")){
            UKB ukb = new UKB(args);
            ukb.start();
        } else if (args[0].equals("convert")){
            ConvertUtil.accept(args);
        }
    }
}
