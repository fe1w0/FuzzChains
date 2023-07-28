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
import xyz.xzaslxr.utils.generator.ByteArrayInputStreamGenerator;
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

    @Fuzz
    public void fuzz(@From(ByteArrayInputStreamGenerator.class) ByteArrayInputStream byteArrayInputStream) throws IOException, ClassNotFoundException {
        // 屏蔽输出
        System.setOut(new PrintStream(outputStreamCaptor));

        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

        // 反序列化
        objectInputStream.readObject();

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

    public void saveByteArrayInputStream(String filePath, ByteArrayInputStream byteArrayInputStream) {
        try{
            if (filePath.isEmpty()) {
                System.out.println("文件路径不能为空");
                return;
            }

            if (byteArrayInputStream.available() > 0){
                System.out.println("有效");
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = byteArrayInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            byte[] byteArray = byteArrayOutputStream.toByteArray();

            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(byteArray);
            fileOutputStream.flush();

            byteArrayOutputStream.close();
            byteArrayInputStream.close();


            System.out.println("保存: " + filePath + " 成功");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 与 fuzz 不同，reportFuzz 是用于fuzz得到的seed, 保存危险的Object
     * @param byteArrayInputStream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Fuzz
    public void reportFuzz(@From(ByteArrayInputStreamGenerator.class) ByteArrayInputStream byteArrayInputStream) throws IOException, ClassNotFoundException, URISyntaxException {
        String saveFilePath = "/Users/fe1w0/Project/SoftWareAnalysis/Dynamic/FuzzChains/DataSet/output/poc.ser";

        ByteArrayInputStream saveStream = byteArrayInputStream;

        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

        // 反序列化
        objectInputStream.readObject();

        byteArrayInputStream.close();
        objectInputStream.close();

        // 检验插桩， 若触发插桩，则 isExploitable = true;
        Boolean isExploitable = outputStreamCaptor.toString().trim().contains(magicWords);

        // 恢复输出
        System.setOut(standardOut);

        System.out.println("isExploitable:" + isExploitable);

        if (isExploitable) {
            // 保存 objectInputStream
            saveByteArrayInputStream(saveFilePath, saveStream);
            // 触发 assumeFalse
            assumeFalse(true);
        }
    }
}