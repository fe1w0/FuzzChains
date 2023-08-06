package xyz.xzaslxr.utils.generator;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import sun.misc.Unsafe;
import xyz.xzaslxr.utils.setting.PropertyTreeNode;
import xyz.xzaslxr.utils.setting.ReadConfiguration;
import xyz.xzaslxr.utils.setting.ReadPropertyTreeConfigure;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static xyz.xzaslxr.driver.FuzzChainsDriver.fuzzClassLoader;


public class ByteArrayInputStreamGenerator extends Generator<ByteArrayInputStream> {

    public static ArrayList<String> validRandomFieldClasses = new ArrayList<String>(
            List.of(
                    "java.lang.Integer"
            )
    );

    // Random number 的值域: [0, 100)
    public static Integer maxNumber = 100;

    public static short minShort = 0;

    public static short maxShort = 100;

    public static short MAX_STRING_LENGTH = 20;

    /**
     * 实例化 className
     * @param className
     * @return
     * @param <className>
     */
    public static <className> Object objectInstance(String className){
        className instantiatedObject = (className) new Object();
        try {
            Class<?> targetClass = Class.forName(className, true, fuzzClassLoader);

            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);

            instantiatedObject = (className) unsafe.allocateInstance(targetClass);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return instantiatedObject;
    }

    /**
     * 通过反射的方式去 assign rootObject.[fieldName] = leafObject;
     * <p></p>
     * 其中 className 为 RootClass的 className, 用于 return 修改后的 rootObject。
     * @param className
     * @param rootObject
     * @param fieldName
     * @param leafObject
     * @return
     * @param <className>
     * @throws Exception
     */
    public static <className> Object setFieldTree(String className, Object rootObject, String fieldName, Object leafObject) {
        try {
            Class<className> rootClass = (Class<className>) Class.forName(className, true, fuzzClassLoader);

            Field rootField = rootClass.getDeclaredField(fieldName);

            rootField.setAccessible(true);

            rootField.set(rootObject, leafObject);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return rootObject;
    }

    public static Object getFieldFromObject(String className, Object object, String fieldName) {
        try {
            Class rootClass =  Class.forName(className);

            Field rootField = rootClass.getDeclaredField(fieldName);
            rootField.setAccessible(true);


            Object field = rootField.get(object);

            return field;

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public ByteArrayInputStreamGenerator() {
        // Register the type of objects that we can create
        super(ByteArrayInputStream.class);
    }

    public ByteArrayInputStream objectToByteArrayInputStream(Object object) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            objectOutputStream.writeObject(object);
            objectOutputStream.flush();


            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            return byteArrayInputStream;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * generateFromPropertyTree 递归过程中产生的临时结构
     */
    class IntermediateProduct{
        String fieldName;
        Object fieldObject;

        IntermediateProduct() {
            fieldName = null;
            fieldObject = null;
        }

        boolean isEmpty() {
            return fieldName == null && fieldObject == null;
        }

        boolean isRoot() {
            return fieldName == null && fieldObject != null;
        }
    }

    /**
     * 根据propertyTree生成对象，并设置Random，以用于Fuzzing
     * @param propertyTree
     * @return
     */
    public IntermediateProduct generateFromPropertyTree(SourceOfRandomness random, PropertyTreeNode propertyTree) {

        IntermediateProduct iProduct = new IntermediateProduct();

        if (propertyTree.isEmpty()) {
            // 若 propertyTree 为空
            return null;
        } else if (propertyTree.getFields().isEmpty()) {
            // 若 propertyTree 的 fields 为空
            // 只 create 单个个体，且不赋值

            String propertyFieldClassName = propertyTree.getClassName();
            String propertyFieldName = propertyTree.getFieldName();

            iProduct.fieldName = propertyFieldName;

            switch (propertyFieldClassName) {
                case "java.lang.Integer":
                    iProduct.fieldObject = random.nextInt(maxNumber);
                    break;

                case "java.lang.Long":
                    iProduct.fieldObject = random.nextLong();
                    break;

                case "java.lang.Short":
                    iProduct.fieldObject = random.nextShort(minShort, maxShort);
                    break;

                case "java.lang.Double":
                    iProduct.fieldObject = random.nextDouble();
                    break;

                case "java.lang.Float":
                    iProduct.fieldObject = random.nextFloat();
                    break;

                case "java.lang.Byte":
                    iProduct.fieldObject = random.nextByte((byte) 0, (byte) 127);
                    break;

                case "java.lang.Character":
                    // iProduct.fieldObject = random.nextChar(Character.MIN_VALUE, Character.MAX_VALUE);
                    iProduct.fieldObject = random.nextChar((char) 0, (char) 127);
                    break;

                case "java.lang.Boolean":
                    iProduct.fieldObject = random.nextBoolean();
                    break;

                case "java.lang.String":
                    int stringLength = random.nextInt(MAX_STRING_LENGTH);
                    String tmpString = new String();
                    for (int  i = 0; i < stringLength; i++) {
                        tmpString = tmpString + String.valueOf(random.nextChar((char) 0, (char) 127));
                    }
                    iProduct.fieldObject = tmpString;
                default:
                    iProduct.fieldObject = objectInstance(propertyFieldClassName);
            }
        } else {
            // 若 propertyTree 的 fields 为非空
            String propertyFieldClassName = propertyTree.getClassName();
            String propertyFieldName = propertyTree.getFieldName();

            // 创建 root 对象
            Object root = objectInstance(propertyFieldClassName);

            // 设置 root 对象的变量
            for (PropertyTreeNode node : propertyTree.getFields()) {
                IntermediateProduct tmpIProduct = generateFromPropertyTree(random, node);
                setFieldTree(propertyFieldClassName, root,
                        tmpIProduct.fieldName, tmpIProduct.fieldObject);
            }

            // 设置 iProduct
            iProduct.fieldName = propertyFieldName;
            iProduct.fieldObject = root;
        }

        if (!iProduct.isEmpty()) {
            return iProduct;
        } else {
            return null;
        }
    }


    public static void main(String[] args) {
        String configurationPath = "DataSet/tree.json";

        ReadConfiguration reader = new ReadPropertyTreeConfigure();
        PropertyTreeNode root = reader.readConfiguration(configurationPath, new PropertyTreeNode());

        ByteArrayInputStreamGenerator byteArrayInputStreamGenerator = new ByteArrayInputStreamGenerator();

        IntermediateProduct iProduct = byteArrayInputStreamGenerator.generateFromPropertyTree(new SourceOfRandomness(new Random()), root);

        System.out.println(root);

    }


    @Override
    public ByteArrayInputStream generate(SourceOfRandomness random, GenerationStatus __ignore__) {
        // 获得 构造函数
        // 需要注意，FuzzChains只能根据json数据，知道涉及的class和 fields 是哪些。
        // 需要提供 下面处理:
        // 1. 根据 提供的 className classLoader 获得 class
        // 2. 得到 class 之后，newInstance 后 设置 field
        // 3. 自下(2-阶)而上，从 leaves 依次向上设置 class，直到 root 为止

        // 关闭 generate 过程中，可能会出现的 print
        PrintStream standardOut = System.out;
        ByteArrayOutputStream genOutputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(genOutputStreamCaptor));

        // 读取 PropertyTree 文件
        String configurationPath = "DataSet/tree.json";

        ReadConfiguration reader = new ReadPropertyTreeConfigure();
        PropertyTreeNode root = reader.readConfiguration(configurationPath, new PropertyTreeNode());

        IntermediateProduct iProduct = generateFromPropertyTree(random, root);

        Object rootObject = null;

        if (iProduct.isRoot()) {
            rootObject = iProduct.fieldObject;
        }

        // 序列化:
        // rootObject -> ObjectInputStream

        ByteArrayInputStream genByteArrayInputStream = objectToByteArrayInputStream(rootObject);

        // 关闭"屏蔽输出"功能
        System.setOut(standardOut);

        return genByteArrayInputStream;
    }
}
