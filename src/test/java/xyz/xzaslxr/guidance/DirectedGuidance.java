package xyz.xzaslxr.guidance;

import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;

import java.io.InputStream;
import java.util.function.Consumer;


/**
 *  This class learn from the <a href="https://github.com/rohanpadhye/JQF/wiki/The-Guidance-interface">jqf-wiki: The Guidance interface</a>
 * @author fe1w0
 */
public class DirectedGuidance implements Guidance {

    @Override
    public InputStream getInput() throws IllegalStateException, GuidanceException {
        return null;
    }

    @Override
    public boolean hasInput() {
        return false;
    }

    @Override
    public void handleResult(Result result, Throwable error) throws GuidanceException {

    }

    @Override
    public Consumer<TraceEvent> generateCallBack(Thread thread) {
        return null;
    }
}
