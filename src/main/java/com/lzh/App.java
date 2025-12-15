package com.lzh;

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
                    "Usage:\n\n" +
                    "java -jar xxx.jar\n" +
                    "--genes [input_file]\n" +
                    "--traits [trait1,trait2,...,traitn]\n" +
                    "--plink [trait1,trait2,...,traitn]\n" +
                    "--sex\n" +
                    "--plink_path [path]\n" +
                    "--gemma_path [path]\n" +
                    "--[method1]\n--[method2]\n...\n\n" +
                    "You can access %s for more details.\n" +
                    "Warning: your java version must be equal to or greater than jdk8!!!\n", GITHUB);
        } else {
            UKB ukb = new UKB(args);
            ukb.start();
        }
    }
}
