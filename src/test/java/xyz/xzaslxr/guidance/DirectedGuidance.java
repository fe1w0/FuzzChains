package xyz.xzaslxr.guidance;

import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;

import java.io.InputStream;
import java.util.function.Consumer;

public class DirectedGuidance implements Guidance {

    // 再每次迭代时，都会被调用
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
