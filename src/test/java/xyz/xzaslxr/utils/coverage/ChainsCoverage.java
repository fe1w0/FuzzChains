package xyz.xzaslxr.utils.coverage;

import edu.berkeley.cs.jqf.fuzz.util.Counter;
import edu.berkeley.cs.jqf.fuzz.util.ICoverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEventVisitor;
import org.eclipse.collections.api.list.primitive.IntList;

/**
 * @author fe1w0
 * @date 2023/9/11 10:22
 * @Project FuzzChains
 */
public class ChainsCoverage implements TraceEventVisitor, ICoverage<Counter> {
    @Override
    public int size() {
        return 0;
    }

    @Override
    public int getNonZeroCount() {
        return 0;
    }

    @Override
    public IntList getCovered() {
        return null;
    }

    @Override
    public IntList computeNewCoverage(ICoverage baseline) {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean updateBits(ICoverage that) {
        return false;
    }

    @Override
    public int nonZeroHashCode() {
        return 0;
    }

    @Override
    public Counter getCounter() {
        return null;
    }

    @Override
    public ICoverage<Counter> copy() {
        return null;
    }
}
