package xyz.xzaslxr.utils.generator;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import sun.misc.Unsafe;

import java.io.ObjectInputStream;
import java.lang.reflect.Field;


public class ComplexObjectGenerator extends Generator<Object> {

    public static <className> Object objectInit(String className) throws Exception{
        Class<?> targetClass = Class.forName(className);

        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);

        className instantiatedObject = (className) unsafe.allocateInstance(targetClass);

        return instantiatedObject;
    }



    protected ComplexObjectGenerator(Class<Object> type) {
        super(type);
    }

    /**
     *
     * @param random source of randomness to be used when generating the value
     * @param __ignore__ an object that can be used to influence the generated
     * value. For example, generating lists can use the {@link
     * GenerationStatus#size() size} method to generate lists with a given
     * number of elements.
     * @return
     */
    @Override
    public ObjectInputStream generate(SourceOfRandomness random, GenerationStatus __ignore__) {
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
         *
         *      .chainTwo
         *          -> sources.demo.ExpTwo
         */

        String leafOne = "sources.demo.ExpOne";
        String leafOneFromField = "chainOne";
        String leafOneFromClass = "sources.serialize.UnsafeSerialize";

        String leafTwo = "sources.demo.ExpTwo";
        String leafTwoFromField = "chainTwo";
        String leafTwoFromClass = "sources.serialize.UnsafeSerialize";

        // 序列化 -> ObjectInputStream

    }
}
