package xyz.xzaslxr.utils.coverage;

import edu.berkeley.cs.jqf.fuzz.util.Counter;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.fuzz.util.ICoverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.*;
import org.eclipse.collections.api.list.primitive.IntList;

/**
 * @author fe1w0
 * @date 2023/9/11 10:22
 * @Project FuzzChains
 */
public class ChainsCoverage extends Coverage implements TraceEventVisitor, ICoverage<Counter> {

    /**
     * Todo:
     * 完善handleEvent机制。
     * <p>
     *     handleEvent会执行applyVisitor，applyVisitor将会调用TraceEventVisitor中各类visitor函数，
     *     如对于CallEvent，会调用visitCallEvent。
     * </p>
     * @param e
     */
    public void handleEvent(TraceEvent e) {
        e.applyVisitor(this);
    }
}
