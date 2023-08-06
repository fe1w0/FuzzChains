package xyz.xzaslxr.driver;


import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.instrument.InstrumentingClassLoader;
import org.junit.runner.Result;
import picocli.CommandLine;
import xyz.xzaslxr.fuzzing.FuzzChainsTest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static edu.berkeley.cs.jqf.instrument.InstrumentingClassLoader.stringsToUrls;

/**
 * <p>This Class is a driver to run FuzzChains-DirectedGuidance, and inspired by this article <a href="https://github.com/rohanpadhye/JQF/issues/102">GuidedFuzzing.run does not collect coverage when run a second time in same program</a>
 * @author  <a href="https://github.com/fe1w0"> fe1w0</a>
 */
@CommandLine.Command(name = "java -jar FuzzChains.jar")
public class FuzzChainsDriver {
    /**
     * <p>FuzzChainsDriver-Cli has many arguments:
     * <p>TargetFile: TargetFile will be included by InstrumentedClassLoader
     * <p>FuzzTime: 50s
     * <p>isSkipException: default is false, need to add a new JVM OPTION
     * <p>FuzzGuidance: DirectedGuidance and other native guidance
     * <p>FuzzMode: report and fuzz
     * <p>outputDirectory: include poc.ser, no-poc.ser, target/fuzz-report
     * <p>configDirectory: include propertyTree.json
     * <p>InputDirectory: should point to fuzz output files, and only used in report mode.
     */

    public static ClassLoader fuzzClassLoader = null;

    public static List<String> getArtifacts(String targetDirectory) throws IOException {
        List<String> classpathElements = new ArrayList<>(Arrays.asList(targetDirectory));

        return classpathElements;
    }

    public static void setUpClassLoader(String fuzzTargetDirectory) throws IOException {

        List<String> classpathElements = getArtifacts(fuzzTargetDirectory);

        ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();

        ClassLoader classLoader = new InstrumentingClassLoader(
                classpathElements.toArray(new String[0]),
                appClassLoader);

        fuzzClassLoader = classLoader;
    }

    public static void main(String[] args) throws IOException {
        // 设置启动参数

        String fuzzTime = "10s";
        String isSkipException = "false";
        String fuzzGuidance = "ZEST";
        String fuzzMode = "fuzz";
        String outputDirectoryName = "/Users/fe1w0/Project/SoftWareAnalysis/Dynamic/FuzzChains/DataSet/output/fuzz-results/";
        String testClassName = FuzzChainsTest.class.getName();
        String testMethodName = null;

        Long trials = 10L;

        String fuzzTargetDirectory = "/Users/fe1w0/Project/SoftWareAnalysis/Dynamic/FuzzChains/DataSet/targets/xyz-xzaslxr-1.0.jar";

        // 设置 fuzz 模式:
        // 1. fuzz
        // 2. report
        if (fuzzMode.equals("fuzz") ) {
            testMethodName = "fuzz";
        } else if (fuzzMode.equals("report")) {
            testMethodName = "reportFuzz";
        }

        // 设置 isSkipException
        if (isSkipException.equals("false")) {
            System.setProperty("jqf.failOnDeclaredExceptions", "true");
        } else {
            System.setProperty("jqf.failOnDeclaredExceptions", "false");
        }

        // 设置 ClassLoader
        setUpClassLoader(fuzzTargetDirectory);

        // 需要检查输出文件夹的有效性
        File outputDirectory = new File(outputDirectoryName);

        File seedDirectories = null;

        Random random = new Random();

        try {
            // Load the guidance
            String title = testClassName + "#" + testMethodName;

            Duration fuzzDuration = Duration.parse("PT" + fuzzTime);

            ZestGuidance guidance = new ZestGuidance(title, fuzzDuration, trials, outputDirectory, seedDirectories, random);

            // Run the Junit test
            Result res = GuidedFuzzing.run(testClassName, testMethodName, fuzzClassLoader, guidance, System.out);

            if (Boolean.getBoolean("jqf.logCoverage")) {
                System.out.println(String.format("Covered %d edges.",
                        guidance.getTotalCoverage().getNonZeroCount()));
            }
            if (Boolean.getBoolean("jqf.ei.EXIT_ON_CRASH") && !res.wasSuccessful()) {
                System.exit(3);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
