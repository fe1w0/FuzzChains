package xyz.xzaslxr.utils.generator;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;


public class ByteArrayInputStreamGenerator extends Generator<ByteArrayInputStream> {

    /**
     * 实例化 className
     * @param className
     * @return
     * @param <className>
     * @throws Exception
     */
    public static <className> Object objectInstance(String className){
        className instantiatedObject = (className) new Object();
        try {
            Class<?> targetClass = Class.forName(className);

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
            Class<className> rootClass = (Class<className>) Class.forName(className);

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

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
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

    @Override
    public ByteArrayInputStream generate(SourceOfRandomness random, GenerationStatus __ignore__) {
        // 获得 构造函数
        // 需要注意，FuzzChains只能根据json数据，知道涉及的class和 fields 是哪些。
        // 需要提供 下面处理:
        // 1. 根据 提供的 className classLoader 获得 class
        // 2. 得到 class 之后，newInstance 后 设置 field
        // 3. 自下(2-阶)而上，从 leaves 依次向上设置 class，直到 root 为止

        /**
         * sources.serialize.UnsafeSerialize
         *      .chainOne
         *          -> sources.demo.ExpOne
         *              .size $ means importance
         *      .chainTwo
         *          -> sources.demo.ExpTwo
         */

        // 关闭 generate 过程中，可能会出现的 print
        PrintStream standardOut = System.out;
        ByteArrayOutputStream genOutputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(genOutputStreamCaptor));

        String leafOneName = "sources.demo.ExpOne";
        String leafOneFromFieldName = "chainOne";
        String rootClassName = "sources.serialize.UnsafeSerialize";
        String leafTwoName = "sources.demo.ExpTwo";
        String leafTwoFromFieldName = "chainTwo";

        // 1. 创建 rootObject
        Object rootObject = objectInstance(rootClassName);

        // 2. 创建 leafObject
        Object leafOneObject = objectInstance(leafOneName);

        // 2.1 leafOneObject 中 size 是敏感属性:

        // Size  = [0, 100]
        Integer leafOneSize = random.nextInt(100);
        leafOneObject = setFieldTree(leafOneName, leafOneObject, "size", leafOneSize);

        Object leafTwoObject = objectInstance(leafTwoName);

        // 3. 设置 rootObject.field = leafObject
        rootObject = setFieldTree(rootClassName, rootObject, leafOneFromFieldName, leafOneObject);
        rootObject = setFieldTree(rootClassName, rootObject, leafTwoFromFieldName, leafTwoObject);

        // 序列化:
        // rootObject -> ObjectInputStream

        ByteArrayInputStream genByteArrayInputStream = objectToByteArrayInputStream(rootObject);

        // 关闭"屏蔽输出"功能
        System.setOut(standardOut);

        return genByteArrayInputStream;
    }
}
