package xyz.xzaslxr.guidance;

import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import edu.berkeley.cs.jqf.fuzz.util.ICoverage;
import edu.berkeley.cs.jqf.fuzz.util.IOUtils;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import org.eclipse.collections.api.iterator.IntIterator;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import xyz.xzaslxr.utils.coverage.ChainsCoverage;
import xyz.xzaslxr.utils.setting.ChainPaths;
import xyz.xzaslxr.utils.setting.ReadChainPathsConfigure;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    /** The max amount of time to run for, in milliseconds
     * <p>允许允许的最多时间，单位为秒</p>
     */
    protected long maxDurationMillis;

    /** The number of trials completed.
     * 当前有多少尝试完成
     * */
    protected long numTrials = 0;

    protected long numValid = 0;

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


    protected String chainsConfigPath;

    protected List<String> chainPaths;

    /** validityFuzzing -- if true then save valid inputs that increase valid
     * coverage
     */
    protected boolean validityFuzzing;

    /**
     * Number of saved inputs.
     *
     * This is usually the same as savedInputs.size(),
     * but we do not really save inputs in TOTALLY_RANDOM mode.
     */
    protected int numSavedInputs = 0;

    /** Coverage statistics for a single run. */
    protected ICoverage runCoverage = new ChainsCoverage();

    protected ICoverage totalCoverage = new ChainsCoverage();

    protected ICoverage validCoverage = new ChainsCoverage();

    protected ICoverage chainsCoverage = new ChainsCoverage();

    protected int maxCoverage = 0;

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


    /** The directory where fuzzing results are produced. */
    protected final File outputDirectory;

    /** The directory where interesting inputs are saved. */
    protected File savedCorpusDirectory;

    /** The directory where saved inputs are saved. */
    protected File savedFailuresDirectory;

    /**
     * The directory where all generated inputs are logged in sub-directories (if
     * enabled).
     */
    protected File allInputsDirectory;


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



    // ------------- TIMEOUT HANDLING ------------

    private long singleRunTimeoutMillis;

    private Date runStart;

    // -------- Initialization --------
    public ChainsCoverageGuidance(String testName, Duration duration, Long trials, File outputDirectory, File seedInputDir, Random sourceOfRandomness, String chainsConfigPath) throws IOException{
        this(testName, duration, trials, outputDirectory,  IOUtils.resolveInputFileOrDirectory(seedInputDir), sourceOfRandomness, chainsConfigPath);
    }

    public ChainsCoverageGuidance(String testName, Duration duration, Long trials, File outputDirectory, File[] seedInputFiles, Random sourceOfRandomness, String chainsConfigPath) throws IOException {
        this(testName, duration, trials, outputDirectory, sourceOfRandomness, chainsConfigPath);
        if (seedInputFiles != null) {
            for (File seedInputFile : seedInputFiles) {
                seedInputs.add(new SeedInput(seedInputFile));
            }
        }
    }

    public ChainsCoverageGuidance(String testName, Duration duration, Long trials, File outputDirectory, Random sourceOfRandomness, String chainsConfigPath) throws IOException {
        this.random = sourceOfRandomness;
        this.testName = testName;
        this.maxDurationMillis = duration != null ? duration.toMillis() : Long.MAX_VALUE;
        this.maxTrials = trials != null ? trials : Long.MAX_VALUE;
        this.outputDirectory = outputDirectory;
        this.validityFuzzing = !Boolean.getBoolean("jqf.ei.DISABLE_VALIDITY_FUZZING");
        this.chainsConfigPath = chainsConfigPath;

        ReadChainPathsConfigure reader = new ReadChainPathsConfigure();
        ChainPaths tmpChainPaths = reader.readConfiguration(chainsConfigPath, new ChainPaths());

        this.chainPaths = tmpChainPaths.getPaths();

        prepareOutputDirectory();

        String timeout = System.getProperty("jqf.ei.TIMEOUT");
        if (timeout != null && !timeout.isEmpty()) {
            try {
                // Interpret the timeout as milliseconds (just like `afl-fuzz -t`)
                this.singleRunTimeoutMillis = Long.parseLong(timeout);
            } catch (NumberFormatException e1) {
                throw new IllegalArgumentException("Invalid timeout duration: " + timeout);
            }
        }
        
    }


    /**
     * Todo: 需要自定义修改
     * @throws IOException
     */
    private void prepareOutputDirectory() throws IOException {
        // Create the output directory if it does not exist
        IOUtils.createDirectory(outputDirectory);

        // Name files and directories after AFL
        this.savedCorpusDirectory = IOUtils.createDirectory(outputDirectory, "corpus");
        this.savedFailuresDirectory = IOUtils.createDirectory(outputDirectory, "failures");

        this.logFile = new File(outputDirectory, "fuzz.log");

        // Delete everything that we may have created in a previous run.
        // Trying to stay away from recursive delete of parent output directory in case there was a
        // typo and that was not a directory we wanted to nuke.
        // We also do not check if the deletes are actually successful.
        logFile.delete();
        for (File file : savedCorpusDirectory.listFiles()) {
            file.delete();
        }
        for (File file : savedFailuresDirectory.listFiles()) {
            file.delete();
        }

    }


    // -------- Basic Method --------

    /**
     * 检查是否有新的输入：
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


    // --------- JQF Basic Frame ---------

    /**
     * 得到新的程序输入
     * <p>参考:
     * edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance#getInput()
     * </p>
     * @return java.io.InputStream
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
            } else if (savedInputs.isEmpty()) {
                // savedInputs 为空，表示的是当前trial的队列为空
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
     * 注册处理Event的函数
     * <p>⚠️ 需要注意: generateCallBack 在 JQF中的 执行顺序上优于 handleResult</p>
     * @param thread  the thread whose events to handle
     * @return Consumer<TraceEvent>
     */
    @Override
    public Consumer<TraceEvent> generateCallBack(Thread thread) {
        if (firstThread == null) {
            firstThread = thread;
        } else if (firstThread != thread) {
            multiThreaded = true;
        }
        return this::handleEvent;
    }

    /**
     *
     * @param result   the result of the fuzzing trial
     * @param error    the error thrown during the trial, or <code>null</code>
     */
    @Override
    public void handleResult(Result result, Throwable error) throws GuidanceException {
        conditionallySynchronize(multiThreaded, () -> {
            // Stop timeout handling
            this.runStart = null;

            // Increment run count
            this.numTrials++;

            boolean valid = result == Result.SUCCESS;


            if (valid) {
                this.numValid++;
            }

            // 当前trial为成功，或者该trial中忽视
            if (result == Result.SUCCESS || (result == Result.INVALID && !SAVE_ONLY_VALID)) {


                IntHashSet responsibilities = computeResponsibilities(valid);

                List<String> savingCriteriaSatisfied = checkSavingCriteriaSatisfied(result);
                boolean toSave = !savingCriteriaSatisfied.isEmpty();

                if (toSave) {
                    String why = String.join(" ", savingCriteriaSatisfied);

                    // Todo: 理解 gc 的作用
                    currentInput.gc();

                    // Save input to queue and to disk
                    final String reason = why;

                    GuidanceException.wrap(() -> saveCurrentInput(responsibilities, reason));

                    // Update coverage information
                    // updateCoverageFile();
                } else if (result == Result.FAILURE || result == Result.TIMEOUT) {
                    // 需要注意的是:
                    // 区别 FuzzException 和 其他 Exception 的区别

                    String msg = error.getMessage();

                    // Get the root cause of the failure
                    Throwable rootCause = error;
                    while (rootCause.getCause() != null) {
                        rootCause = rootCause.getCause();
                    }

                    // Attempt to add this to the set of unique failures
                    if (uniqueFailures.add(failureDigest(rootCause.getStackTrace()))) {
                        // Trim input (remove unused keys)
                        currentInput.gc();

                        // Save crash to disk
                        int crashIdx = uniqueFailures.size() - 1;
                        String saveFileName = String.format("id_%06d", crashIdx);
                        File saveFile = new File(savedFailuresDirectory, saveFileName);
                        GuidanceException.wrap(() -> writeCurrentInputToFile(saveFile));
                        infoLog("%s", "Found crash: " + error.getClass() + " - " + (msg != null ? msg : ""));
                        String how = currentInput.desc;
                        String why = result == Result.FAILURE ? "+crash" : "+hang";
                        infoLog("Saved - %s %s %s", saveFile.getPath(), how, why);

                        // if (EXACT_CRASH_PATH != null && !EXACT_CRASH_PATH.equals("")) {
                        //     File exactCrashFile = new File(EXACT_CRASH_PATH);
                        //     GuidanceException.wrap(() -> writeCurrentInputToFile(exactCrashFile));
                        // }
                    }

                    // Save input unconditionally if such a setting is enabled
                    // 暂时保存所有信息
                    if ((SAVE_ONLY_VALID ? valid : true)) {
                        File logDirectory = new File(allInputsDirectory, result.toString().toLowerCase());
                        String saveFileName = String.format("id_%09d", numTrials);
                        File saveFile = new File(logDirectory, saveFileName);
                        GuidanceException.wrap(() -> writeCurrentInputToFile(saveFile));
                    }
                }
            }
        });
    }

    // ------- Handle Result -------

    /**
     * computeResponsibilities 的计算结果为，(runCoverage - totalCoverage) and (runCoverage - validCoverage) and (runCoverage - validCoverage)
     *
     * <p>
     *     Todo: 可以优化
     * </p>
     *
     * @param valid
     * @return IntHashSet
     */
    protected IntHashSet computeResponsibilities(boolean valid) {
        IntHashSet result = new IntHashSet();

        // newValidCoverage 中保存着之前 totalCoverage中没有的新的edges信息
        IntList newCoverage = runCoverage.computeNewCoverage(totalCoverage);
        if (!newCoverage.isEmpty()) {
            result.addAll(newCoverage);
        }

        // 如果当前result为有效的，则同样更新validCoverage
        if (valid) {
            IntList newValidCoverage = runCoverage.computeNewCoverage(validCoverage);
            if (!newValidCoverage.isEmpty()) {
                result.addAll(newValidCoverage);
            }
        }

        // Todo: 测试可行性
        IntList newChainsCoverage = runCoverage.computeNewCoverage(chainsCoverage);
        if (!newChainsCoverage.isEmpty()) {
            result.addAll(newChainsCoverage);
        }

        // Todo: 理解 STEAL_RESPONSIBILITY 的作用，暂时先删除
        return result;
    }

    /**
     * 返回reasonsToSave，返回更新的原因
     * @param result
     * @return
     */
    protected List<String> checkSavingCriteriaSatisfied(Result result) {
        // Coverage Before

        // 之前，TotalCoverage的Edges数量
        int nonZeroBefore = totalCoverage.getNonZeroCount();
        // 之前，ValidCoverage的Edges数量
        int validNonZeroBefore = validCoverage.getNonZeroCount();
        // 之前，ChainsCoverage的Edges数量
        int chainsNoeZeroBefore = chainsCoverage.getNonZeroCount();

        // Todo: 理解updateBits函数中的hob函数和原理，得看 Coverage中counter的计算方式了
        boolean coverageBitsUpdated = totalCoverage.updateBits(runCoverage);

        if (result == Result.SUCCESS) {
            validCoverage.updateBits(runCoverage);
        }

        // Todo: 添加 chainsCoverage.updateBits
        // 当发现新的且有效的chainCoverage时，刷新chainsCoverage？


        // Coverage after
        int nonZeroAfter = totalCoverage.getNonZeroCount();
        if (nonZeroAfter > maxCoverage) {
            maxCoverage = nonZeroAfter;
        }
        int validNonZeroAfter = validCoverage.getNonZeroCount();

        // Possibly save input
        List<String> reasonsToSave = new ArrayList<>();

        if (!DISABLE_SAVE_NEW_COUNTS && coverageBitsUpdated) {
            reasonsToSave.add("+count");
        }

        // Save if new total coverage found
        if (nonZeroAfter > nonZeroBefore) {
            reasonsToSave.add("+cov");
        }

        // Save if new valid coverage is found
        if (this.validityFuzzing && validNonZeroAfter > validNonZeroBefore) {
            reasonsToSave.add("+valid");
        }

        return reasonsToSave;
    }


    /** Updates the data in the coverage file */
    // protected void updateCoverageFile() {
    //     try {
    //         PrintWriter pw = new PrintWriter(coverageFile);
    //         pw.println(getTotalCoverage().toString());
    //         pw.println("Hash code: " + getTotalCoverage().hashCode());
    //         pw.close();
    //     } catch (FileNotFoundException ignore) {
    //         throw new GuidanceException(ignore);
    //     }
    // }

    // -------- Stats --------

    /**
     * 展示Fuzzing的现状
     * @param force
     */
    private void displayStats(boolean force) {

    }

    // -------- Generate Input Part -------

    /**
     * 生成用于 generators 的 InputStream
     * @return InputStream
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

    /**
     * 因为seedInput和savedInput都为空，需要产生一个全新的输入
     * @return Input<?>
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

    protected void saveCurrentInput(IntHashSet responsibilities, String why) throws IOException {

        // First, save to disk (note: we issue IDs to everyone, but only write to disk
        // if valid)
        int newInputIdx = numSavedInputs++;
        String saveFileName = String.format("id_%06d", newInputIdx);
        String how = currentInput.desc;
        File saveFile = new File(savedCorpusDirectory, saveFileName);
        writeCurrentInputToFile(saveFile);
        infoLog("Saved - %s %s %s", saveFile.getPath(), how, why);


        // Second, save to queue
        savedInputs.add(currentInput);

        // Third, store basic book-keeping data
        currentInput.id = newInputIdx;
        currentInput.saveFile = saveFile;
        currentInput.coverage = runCoverage.copy();
        currentInput.nonZeroCoverage = runCoverage.getNonZeroCount();
        currentInput.offspring = 0;
        savedInputs.get(currentParentInputIdx).offspring += 1;

        // Fourth, assume responsibility for branches
        currentInput.responsibilities = responsibilities;
        if (responsibilities.size() > 0) {
            currentInput.setFavored();
        }
        IntIterator iter = responsibilities.intIterator();
        while (iter.hasNext()) {
            int b = iter.next();
            // If there is an old input that is responsible,
            // subsume it
            Input oldResponsible = responsibleInputs.get(b);
            if (oldResponsible != null) {
                oldResponsible.responsibilities.remove(b);
                // infoLog("-- Stealing responsibility for %s from input %d", b,
                // oldResponsible.id);
            } else {
                // infoLog("-- Assuming new responsibility for %s", b);
            }
            // We are now responsible
            responsibleInputs.put(b, currentInput);
        }

    }

    protected void writeCurrentInputToFile(File saveFile) throws IOException {
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(saveFile))) {
            for (Integer b : currentInput) {
                assert (b >= 0 && b < 256);
                out.write(b);
            }
        }

    }


    // --------- Handle Exception ------

    private static MessageDigest sha1;

    private static String failureDigest(StackTraceElement[] stackTrace) {
        if (sha1 == null) {
            try {
                sha1 = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new GuidanceException(e);
            }
        }
        byte[] bytes = sha1.digest(Arrays.deepToString(stackTrace).getBytes());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return sb.toString();
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
                throw new AssertionError("Responsibility mismatch");
            }
        }

        // Break log after cycle
        infoLog("\n\n\n");
    }

    // -------- Handle Events --------

    /**
     * Handles a trace event generated during test execution.
     * <p>handleEvent: 处理Event的过程中，会对 totalCoverage 等信息进行更新</p>
     *
     * Not used by FastNonCollidingCoverage, which does not allocate an
     * instance of TraceEvent at each branch probe execution.
     *
     * @param e the trace event to be handled
     */
    protected void handleEvent(TraceEvent e) {
        conditionallySynchronize(multiThreaded, () -> {
            // Collect totalCoverage
            ((ChainsCoverage) runCoverage).handleEvent(e);
            // Check for possible timeouts every so often
            if (this.singleRunTimeoutMillis > 0 &&
                    this.runStart != null && (++this.branchCount) % 10_000 == 0) {
                long elapsed = new Date().getTime() - runStart.getTime();
                if (elapsed > this.singleRunTimeoutMillis) {
                    throw new TimeoutException(elapsed, this.singleRunTimeoutMillis);
                }
            }
        });
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

    // Output Information
    public ICoverage getTotalCoverage() {
        return totalCoverage;
    }
}