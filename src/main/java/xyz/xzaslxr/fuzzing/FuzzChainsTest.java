package xyz.xzaslxr.fuzzing;

import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.After;
import org.junit.Before;
import java.io.ByteArrayOutputStream;
import org.junit.runner.RunWith;
import xyz.xzaslxr.utils.generator.ComplexObjectGenerator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;

import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;


/**
 * FuzzChains 用于Fuzzing libraries，主要与JQF进行交互。
 */

@RunWith(JQF.class)
public class FuzzChainsTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private final String magicWords = "FuzzChains@fe1w0";

    // 设置加载 FuzzChains
    static {
        // System.out.println("Start FuzzChains");
    }

    // Runs before tests are executed
    // @Before
    // public void beforeFuzz() {
    //
    // }


    // @Before 获取当前程序Fuzz的所有 sout

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }


    @After
    public void tearDown() {
        System.setOut(standardOut);
    }



    @Fuzz
    public void fuzz(@From(ComplexObjectGenerator.class) ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        // 获得当前RunProgram中所有的sout输出，并记为 `acutalSout`.
        // 同时设置 MagicWords 为 `FuzzChain@fe1w0`
        // 最后采用 assiemTrue(acutalSout.include(MagicWords))
        // 在fuzz结束后，查看 successfual 中的结果
        inputStream.readObject();

        String currentStringOutput = outputStreamCaptor.toString().trim();

        assumeTrue(currentStringOutput.contains(magicWords));
    }

    // public static void main(String[] args) {
    //
    //     PrintStream standardOut = System.out;
    //
    //     ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    //
    //     System.setOut(new PrintStream(outputStreamCaptor));
    //
    //     System.out.println("test: " + magicWords );
    //
    //     String currentStringOutput = outputStreamCaptor.toString().trim();
    //
    //     System.setOut(standardOut);
    //
    //     if (currentStringOutput.contains(magicWords)) {
    //         System.out.println("Yes");
    //     }
    // }
}