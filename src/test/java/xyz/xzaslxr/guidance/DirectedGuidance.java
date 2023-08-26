package xyz.xzaslxr.guidance;

import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;


/**
 * @author fe1w0
 * <p> This class learn from the <a href="https://github.com/rohanpadhye/JQF/wiki/The-Guidance-interface">jqf-wiki: The Guidance interface</a></p>
 */
public class DirectedGuidance implements Guidance {

    private long singleRunTimeoutTrial;

    private Date runStart;

    private Coverage coverage = new Coverage();

    private Set<String> branchesCoveredInCurrentRun;

    private Set<String> allBranchesCovered;

    public DirectedGuidance(String testName, Duration duration, Long trials, File outputDirectory, File[] seedInputFiles, Random sourceOfRandomness){

    }


    /**
     * 参考:
     * edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance#hasInput()
     * @return
     */
    @Override
    public boolean hasInput() {
        return false;
    }

    /**
     *
     * @return
     * @throws IllegalStateException
     * @throws GuidanceException
     */
    @Override
    public InputStream getInput() throws IllegalStateException, GuidanceException {
        return null;
    }

    @Override
    public void handleResult(Result result, Throwable error) throws GuidanceException {

    }

    @Override
    public Consumer<TraceEvent> generateCallBack(Thread thread) {
        return null;
    }
}
