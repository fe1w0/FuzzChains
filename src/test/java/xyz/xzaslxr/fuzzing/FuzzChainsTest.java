package xyz.xzaslxr.fuzzing;

import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.After;
import org.junit.Before;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.runner.RunWith;
import xyz.xzaslxr.utils.generator.ObjectInputStreamGenerator;

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
    // static {
    //     // System.out.println("Start FuzzChains");
    // }

    @Before
    public void setUp() {
        // 每次 runProgram 前，将之后的输出转到 outputStreamCaptor
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @After
    public void tearDown() {
        // 恢复正常输出
        System.setOut(standardOut);
        // System.out.println(outputStreamCaptor.toString().trim().contains(magicWords));
    }



    public void saveObjectInputStream(String filePath, ObjectInputStream inputStream) {
        try {
            if (filePath != "") {
                FileOutputStream fileOut = new FileOutputStream(filePath);
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);

                // 从 ObjectInputStream 读取对象，并写入到文件
                Object obj;
                while ((obj = inputStream.readObject()) != null) {
                    objectOut.writeObject(obj);
                }
                objectOut.close();
                fileOut.close();
                System.out.println("ObjectInputStream 已成功保存到文件: " + filePath);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("保存 ObjectInputStream 时出现错误: " + e.getMessage());
        }
    }

    @Fuzz
    public void fuzz(@From(ObjectInputStreamGenerator.class) ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        // 屏蔽输出
        System.setOut(new PrintStream(outputStreamCaptor));

        // 反序列化
        inputStream.readObject();

        // 检验插桩， 若触发插桩，则 isExploitable = true;
        Boolean isExploitable = outputStreamCaptor.toString().trim().contains(magicWords);

        // 触发 assumeFalse
        if (isExploitable) {
            assumeFalse(true);
        }
    }


    public String getJarFilePath() throws URISyntaxException {
        URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
        URI uri = url.toURI();
        File jarFile = new File(uri);
        String jarFilePath = jarFile.getAbsolutePath();

        return jarFilePath;
    }


    /**
     * 与 fuzz 不同，reportFuzz 是用于fuzz得到的seed, 保存危险的Object
     * @param inputStream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Fuzz
    public void reportFuzz(@From(ObjectInputStreamGenerator.class) ObjectInputStream inputStream) throws IOException, ClassNotFoundException, URISyntaxException {
        String saveFilePath = "/Users/fe1w0/Project/SoftWareAnalysis/Dynamic/FuzzChains/DataSet/output/poc.ser";

        // 反序列化
        inputStream.readObject();

        // 检验插桩， 若触发插桩，则 isExploitable = true;
        Boolean isExploitable = outputStreamCaptor.toString().trim().contains(magicWords);

        // 恢复输出
        System.setOut(standardOut);

        System.out.println(isExploitable);

        if (isExploitable) {
            // 保存 objectInputStream
            saveObjectInputStream(saveFilePath, inputStream);
            // 触发 assumeFalse
            assumeFalse(true);
        }
    }
}