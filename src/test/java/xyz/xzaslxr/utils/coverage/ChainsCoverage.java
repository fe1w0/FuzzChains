package xyz.xzaslxr.utils.coverage;

import edu.berkeley.cs.jqf.fuzz.util.Counter;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.fuzz.util.ICoverage;
import edu.berkeley.cs.jqf.fuzz.util.NonZeroCachingCounter;
import edu.berkeley.cs.jqf.instrument.tracing.events.*;
import org.eclipse.collections.api.iterator.IntIterator;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * @author fe1w0
 * @date 2023/9/11 10:22
 * @Project FuzzChains
 */
public class ChainsCoverage extends Coverage {

    /** The size of the coverage map. */
    private final int COVERAGE_MAP_SIZE = (1 << 16) - 1; // Minus one to reduce collisions

    /** The coverage counts for each edge. */
    private final Counter counter = new NonZeroCachingCounter(COVERAGE_MAP_SIZE);

    public Counter getChainsCodeCounter() {
        return chainsCodeCounter;
    }

    /**
     * The hashCode counts for the method name of each edge.
     */
    private final Counter chainsCodeCounter = new NonZeroCachingCounter(COVERAGE_MAP_SIZE);


    /**
     * 计算 this.chainsCodeCounter in chainPaths
     * @param chainPaths
     * @return
     */
    public IntList computeCoveredChainsPath(Map<Integer, String> chainPaths) {
        IntArrayList result = new IntArrayList();

        IntList currentPaths = this.chainsCodeCounter.getNonZeroIndices();

        currentPaths.forEach(num -> {
            if (chainPaths.containsKey(num)) {
                result.add(num);
            }
        });

        return result;
    }

    @Override
    public ChainsCoverage copy() {
        ChainsCoverage ret = new ChainsCoverage();

        for (int idx = 0; idx < COVERAGE_MAP_SIZE; idx++) {
            ret.counter.setAtIndex(idx, this.counter.getAtIndex(idx));
        }

        for (int idx = 0; idx < COVERAGE_MAP_SIZE; idx++) {
            ret.chainsCodeCounter.setAtIndex(idx, this.chainsCodeCounter.getAtIndex(idx));
        }

        return ret;
    }

    /**
     * <p>
     *     handleEvent会执行applyVisitor，applyVisitor将会调用TraceEventVisitor中各类visitor函数，
     *     如对于CallEvent，会调用visitCallEvent。
     * </p>
     * @param e
     */
    public void handleEvent(TraceEvent e) {
        e.applyVisitor(this);
    }

    /**
     * 对于 CallEvent，获取getInvokedMethodName；
     * 对于 BranchEvent， 获取 getContainingMethodName；
     * 其他，返回null
     * @param traceEvent
     * @return
     */
    public String getEventMethodName(TraceEvent traceEvent) {
        if (traceEvent instanceof CallEvent) {
            return ((CallEvent) traceEvent).getInvokedMethodName();
        } else if (traceEvent instanceof BranchEvent) {
            return ((BranchEvent) traceEvent).getContainingMethodName();
        } else {
            return null;
        }
    }

    /**
     * 处理 CallEvent
     * @param e
     */
    @Override
    public void visitCallEvent(CallEvent e) {
        super.visitCallEvent(e);

        // hashCodeCounter 中添加 invokedMethodName.hashCode
        chainsCodeCounter.increment(getEventMethodName(e).hashCode());
    }

    /**
     * 处理 BranchEvent
     * @param b
     */
    @Override
    public void visitBranchEvent(BranchEvent b) {
        super.visitBranchEvent(b);

        // hashCodeCounter 中添加 getContainingMethodName.hashCode
        chainsCodeCounter.increment(getEventMethodName(b).hashCode());
    }

    /**
     * 获得 非0的空间大小
     * @return int
     */
    public int getNonZeroChainsCount() {
        return chainsCodeCounter.getNonZeroSize();
    }

    /**
     * 获得 非0的空间
     * @return IntList
     */
    public IntList getChainsCovered() {
        return chainsCodeCounter.getNonZeroIndices();
    }

    /**
     * 清理 counter 和 chainsCodeCounter
     */
    @Override
    public void clear() {
        this.counter.clear();
        this.chainsCodeCounter.clear();
    }
}
