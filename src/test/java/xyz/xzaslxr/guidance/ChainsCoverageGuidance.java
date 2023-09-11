package xyz.xzaslxr.guidance;

import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.fuzz.util.ICoverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import xyz.xzaslxr.utils.coverage.ChainsCoverage;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

/**
 * A guidance that performs chains-coverage-guided fuzzing using two coverage maps,
 * one for total coverage and one for chains coverage.
 * @author fe1w0
 * <p> This class learn from the <a href="https://github.com/rohanpadhye/JQF/wiki/The-Guidance-interface">jqf-wiki: The Guidance interface</a>
 * and ZestGuidance
 * </p>
 */
public class ChainsCoverageGuidance implements Guidance {

    // ---------- Primary Parameter ----------
    /**
     *  A pseudo-random number generator for generating fresh values.
     *  <p>
     *      预先的随机数字生成器，用于后续的变量随机。
     *  </p>
     */
    protected Random random;

    /** The name of the test for display purposes.
     * <p>
     *     被Fuzzing的函数名
     * </p>
     * */
    protected String testName;

    // ---------- Algorithm Parameters ----------

    /** The max amount of time to run for, in milli-seconds
     * <p>允许允许的最多时间，单位为秒</p>
     */
    protected long maxDurationMillis;

    /** The number of trials completed.
     * 当前有多少尝试完成
     * */
    protected long numTrials = 0;

    /** The max number of trials to run
     * 设置的最多尝试次数
     * */
    protected long maxTrials;


    /** Time since this guidance instance was created.
     * <p>
     *     ChainsCG 实例被创建的时间。
     * </p>
     * */
    protected Date startTime = new Date();

    /** Whether to stop/exit once a crash is found.
     * <p>
     *     当被测试的产生奔溃时，是否停止或退出
     * </p>
     * */
    protected boolean EXIT_ON_CRASH = Boolean.getBoolean("jqf.ei.EXIT_ON_CRASH");

    /** The set of unique failures found so far.
     * <p>
     *     到目前为止，发现的独特错误
     * </p>
     * */
    protected Set<String> uniqueFailures = new HashSet<>();

    // ---------- Fuzzing Input and Coverage ----------

    /** Coverage statistics for a single run. */
    protected ICoverage runCoverage = new ChainsCoverage();

    protected ICoverage totalCoverage = new ChainsCoverage();

    protected ICoverage chainsCoverage = new ChainsCoverage();

    protected int maxCoverage = 0;


    /**
     *
     */
    protected int maxChainsCoverage = 0;

    protected Map<Object, Input> responsibleInputs = new HashMap<>(totalCoverage.size());


    /** Set of saved inputs to fuzz. */
    protected ArrayList<Input> savedInputs = new ArrayList<>();

    /** Queue of seeds to fuzz. */
    protected Deque<Input> seedInputs = new ArrayDeque<>();

    /**
     * Current input that's running -- valid after getInput() and before
     * handleResult().
     */
    protected Input<?> currentInput;

    /**
     * currentParentInputIdx 用于当 seedInput 为空，从非空的savedInput获取前父输入，以此构建新的 currentInput
     */
    protected int currentParentInputIdx = 0;

    /**
     * 为当前父输入生成的子输入个数
     */
    protected int numChildrenGeneratedForCurrentParentInput = 0;


    // --------- Debug and Logging ---------

    protected long branchCount = 0;

    /**
     * Number of cycles completed, i.e. how many times we've reset currentParentInputIdx to 0.
     */
    protected int cyclesCompleted = 0;

    /** Number of favored inputs in the last cycle. */
    protected long numFavoredLastCycle = 0;

    /**
     * 存储debug信息
     */
    protected File logFile;

    /** Whether to print log statements to stderr (debug option; manually edit). */
    protected final boolean verbose = true;

    // ---------- Thread Handling ----------

    /** The first thread in the application, which usually runs the test method.
     *
     * */
    protected Thread firstThread;

    /**
     * Whether the application has more than one thread running coverage-instrumented code
     * <p>
     *     需要修改，需要改成新的coverage-instrumented, 记录的信息不再是 basic block 的 hashcode，
     *     而是其函数名。
     * </p>
     *
     */
    protected boolean multiThreaded = false;

    // ------------- FUZZING HEURISTICS ------------

    /** Whether to save only valid inputs **/
    protected static final boolean SAVE_ONLY_VALID = Boolean.getBoolean("jqf.ei.SAVE_ONLY_VALID");

    /** Max input size to generate. */
    protected static final int MAX_INPUT_SIZE = Integer.getInteger("jqf.ei.MAX_INPUT_SIZE", 10240);

    /**
     * Whether to generate EOFs when we run out of bytes in the input, instead of
     * randomly generating new bytes.
     **/
    protected static final boolean GENERATE_EOF_WHEN_OUT = Boolean.getBoolean("jqf.ei.GENERATE_EOF_WHEN_OUT");

    /** Baseline number of mutated children to produce from a given parent input.
     * <p>
     *     Baseline: 从ParentInput中生成ChildrenInput的数量
     * </p>
     * */
    protected static final int NUM_CHILDREN_BASELINE = 50;

    /**
     * Multiplication factor for number of children to produce for favored inputs.
     */
    protected static final int NUM_CHILDREN_MULTIPLIER_FAVORED = 20;

    /** Mean number of mutations to perform in each round. */
    protected static final double MEAN_MUTATION_COUNT = 8.0;

    /** Mean number of contiguous bytes to mutate in each mutation. */
    protected static final double MEAN_MUTATION_SIZE = 4.0; // Bytes

    /**
     * Whether to save inputs that only add new coverage bits (but no new
     * responsibilities).
     */
    protected static final boolean DISABLE_SAVE_NEW_COUNTS = Boolean.getBoolean("jqf.ei.DISABLE_SAVE_NEW_COUNTS");

    /**
     * Whether to steal responsibility from old inputs (this increases computation
     * cost).
     */
    protected static final boolean STEAL_RESPONSIBILITY = Boolean.getBoolean("jqf.ei.STEAL_RESPONSIBILITY");



    // ---------- Other Parameters ----------

    private long singleRunTimeoutTrial;

    private Date runStart;

    private Coverage coverage = new Coverage();

    private Set<String> branchesCoveredInCurrentRun;

    private Set<String> allBranchesCovered;


    // -------- Initialization --------
    public ChainsCoverageGuidance(String testName, Duration duration, Long trials, File outputDirectory, File[] seedInputFiles, Random sourceOfRandomness){

    }

    /**
     * 检查是否有新的输入
     * <p>参考:
     * edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance#hasInput()
     * </p>
     * @return boolean
     */
    @Override
    public boolean hasInput() {
        Date now = new Date();
        long elapsedMilliseconds = now.getTime() - startTime.getTime();
        if (EXIT_ON_CRASH && !uniqueFailures.isEmpty()) {
            // exit
            return false;
        }
        if (elapsedMilliseconds < maxDurationMillis
                && numTrials < maxTrials) {
            return true;
        } else {
            displayStats(true);
            return false;
        }
    }

    /**
     * 得到新的程序输入
     * <p>参考:
     * edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance#getInput()
     * </p>
     * @return java.io.InputStream
     * @throws IllegalStateException
     * @throws GuidanceException
     */
    @Override
    public InputStream getInput() throws IllegalStateException, GuidanceException {
        // 参考 ZestGuidance 代码，需要考虑到 多线程的问题
        conditionallySynchronize(multiThreaded, () -> {
            // 初始化
            runCoverage.clear();

            // Choose an input to execute based on state of queues
            // seedInputs 非空状态，表示保存有效的种子
            if (!seedInputs.isEmpty()) {
                // First, if we have some specific seeds, use those
                // 优先使用 seedInputs 前面的流量
                // removeFirst 会
                currentInput = seedInputs.removeFirst();

                // Hopefully, the seeds will lead to new coverage and be added to saved inputs

                // savedInputs 为空，表示的是当前trial的队列为空
            } else if (savedInputs.isEmpty()) {
                // If no seeds given try to start with something random
                if (numTrials > 100_000) {
                    throw new GuidanceException("Too many trials; " +
                            "likely all assumption violations");
                }

                // Make fresh input using either list or maps
                // infoLog("Spawning new input from thin air");
                // 创建一个全新的inputs队列
                currentInput = createFreshInput();
            } else {
                // The number of children to produce is determined by how much of the coverage
                // pool this parent input hits
                // 当前的新input内容受到之前父input的覆盖率所决定

                Input currentParentInput = savedInputs.get(currentParentInputIdx);
                int targetNumChildren = getTargetChildrenForParent(currentParentInput);
                if (numChildrenGeneratedForCurrentParentInput >= targetNumChildren) {
                    // Select the next saved input to fuzz
                    currentParentInputIdx = (currentParentInputIdx + 1) % savedInputs.size();

                    // Count cycles
                    if (currentParentInputIdx == 0) {
                        completeCycle();
                    }

                    numChildrenGeneratedForCurrentParentInput = 0;
                }
                Input parent = savedInputs.get(currentParentInputIdx);

                // Fuzz it to get a new input
                // infoLog("Mutating input: %s", parent.desc);
                currentInput = parent.fuzz(random);
                numChildrenGeneratedForCurrentParentInput++;

                // Write it to disk for debugging
                // try {
                //     writeCurrentInputToFile(currentInputFile);
                // } catch (IOException ignore) {
                // }

                // Start time-counting for timeout handling
                this.runStart = new Date();
                this.branchCount = 0;
            }
        });

        return createParameterStream();
    }

    /**
     *
     * <p>⚠️ 需要注意: generateCallBack 在 JQF中的 执行顺序上优于 handleResult，处于在 run 过程中。</p>
     * @param thread  the thread whose events to handle
     * @return Consumer<TraceEvent>
     */
    @Override
    public Consumer<TraceEvent> generateCallBack(Thread thread) {
        return null;
    }


    /**
     *
     * @param result   the result of the fuzzing trial
     * @param error    the error thrown during the trial, or <code>null</code>
     * @throws GuidanceException
     */
    @Override
    public void handleResult(Result result, Throwable error) throws GuidanceException {

    }

    /**
     * 展示Fuzzing的现状
     * @param force
     */
    private void displayStats(boolean force) {

    }

    /**
     * 生成用于 generators 的 InputStream
     * @return
     */
    protected InputStream createParameterStream() {
        return new InputStream() {
            int bytesRead = 0;

            @Override
            public int read() throws IOException {
                // LinearInput 线性输入
                assert currentInput instanceof LinearInput : "ChainsCoverageGuidance should only mutate LinearInput(s)";
                // For linear inputs, get with key = bytesRead (which is then incremented)
                LinearInput linearInput = (LinearInput) currentInput;
                // Attempt to get a value from the list, or else generate a random value
                int ret = linearInput.getOrGenerateFresh(bytesRead++, random);
                // infoLog("read(%d) = %d", bytesRead, ret);
                return ret;
            }
        };
    }

    // -------- Generate Input Part -------

    /**
     * 因为seedInput和savedInput都为空，需要产生一个全新的输入
     * @return
     */
    protected Input<?> createFreshInput() {
        return new LinearInput();
    }

    /**
     * 计算该父输入最多有多少子输入。
     */
    protected int getTargetChildrenForParent(Input parentInput) {
        int target = NUM_CHILDREN_BASELINE;

        // nonZeroCoverage: 覆盖中非空的元素数
        if (maxCoverage > 0) {
            target = (NUM_CHILDREN_BASELINE * parentInput.nonZeroCoverage) / maxCoverage;
        }

        // isFavored的父输入，至少有一个没空覆盖元素
        if (parentInput.isFavored()) {
            target = target * NUM_CHILDREN_MULTIPLIER_FAVORED;
        }
        return target;
    }


    // -------- Multi thread Part -------

    /**
     * Conditionally run a method using synchronization.
     * This is used to handle multithreaded fuzzing.
     * <p>Taking from JQF source code, ZestGuidance.</p>
     * <p>当 cond 为真时，以当前对象为锁，只有一个线程可以同时执行 task 中的代码。当 cond 为假时，多个线程可以同时执行 task 中的代码，没有同步限制。</p>
     */
    protected void conditionallySynchronize(boolean cond, Runnable task) {
        if (cond) {
            synchronized (this) {
                task.run();
            }
        } else {
            task.run();
        }
    }

    /**
     * Handles the end of fuzzing cycle (i.e., having gone through the entire queue)
     * <p>
     *
     * </p>
     */
    protected void completeCycle() {
        // Increment cycle count
        // 循环计数++
        cyclesCompleted++;
        infoLog("\n# Cycle " + cyclesCompleted + " completed.");

        // Go over all inputs and do a sanity check (plus log)
        infoLog("Here is a list of favored inputs:");
        int sumResponsibilities = 0;
        numFavoredLastCycle = 0;
        for (Input input : savedInputs) {
            if (input.isFavored()) {
                int responsibleFor = input.responsibilities.size();
                infoLog("Input %d is responsible for %d branches", input.id, responsibleFor);
                sumResponsibilities += responsibleFor;
                numFavoredLastCycle++;
            }
        }
        int totalCoverageCount = totalCoverage.getNonZeroCount();
        infoLog("Total %d branches covered", totalCoverageCount);
        if (sumResponsibilities != totalCoverageCount) {
            if (multiThreaded) {
                infoLog("Warning: other threads are adding coverage between test executions");
            } else {
                throw new AssertionError("Responsibilty mismatch");
            }
        }

        // Break log after cycle
        infoLog("\n\n\n");
    }

    // -------- Debug and Log --------

    /* Writes a line of text to the log file. */
    protected void infoLog(String str, Object... args) {
        if (verbose) {
            String line = String.format(str, args);
            if (logFile != null) {
                appendLineToFile(logFile, line);

            } else {
                System.err.println(line);
            }
        }
    }

    protected void appendLineToFile(File logFile, String line) {
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(logFile, true))){
            printWriter.println(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
