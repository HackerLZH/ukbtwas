package com.lzh;

/**
 * Hello world!
 *
 */
public class App 
{
    private final static String GITHUB = "github";
    public static void main( String[] args )
    {
        if (args.length == 0) {
            System.out.format("Welcome to use this tool!\n" +
                    "Usage:\n\n" +
                    "java -jar xxx.jar\n" +
                    "--genes [input_file]\n" +
                    "--traits [trait1,trait2,...,traitn]\n" +
                    "--plink [trait1,trait2,...,traitn]\n" +
                    "--sex\n\n" +
                    "You can access %s for more details.\n" +
                    "Warning: you must use jdk8 and over!\n", GITHUB);
        } else {
            UKB ukb = new UKB(args);
//            System.out.println(ukb.getGeneList());
//            System.out.println(ukb.getTraitList());
        }
    }
}
