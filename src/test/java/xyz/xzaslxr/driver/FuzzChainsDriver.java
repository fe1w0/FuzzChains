package xyz.xzaslxr.driver;


import org.apache.commons.cli.*;

/**
 * <p>This Class is a driver to run FuzzChains-DirectedGuidance, and inspired by this article <a href="https://github.com/rohanpadhye/JQF/issues/102">GuidedFuzzing.run does not collect coverage when run a second time in same program</a>
 * @author  <a href="https://github.com/fe1w0"> fe1w0</a>
 */
public class FuzzChainsDriver {

    /**
     * <p>FuzzChainsDriver-Cli has many arguments:
     * <p>TargetFile: TargetFile will be included by InstrumentedClassLoader
     * <p>FuzzTime: 50s
     * <p>isSkipException: default is false, need to add a new JVM OPTION
     * <p>FuzzGuidance: DirectedGuidance and other native guidance
     * <p>FuzzMode: report and fuzz
     * <p>OutputPath: include poc.ser, no-poc.ser, target/fuzz-report
     * <p>InputPath: should point to fuzz output files, and only used in report mode.
     */

    public static void main(String[] args) {
        // define options
        Options options = new Options();

        options.addOption("h", "help", false, "Print help information");
        options.addOption("v", "version", false, "Print version information");

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


    }

}
