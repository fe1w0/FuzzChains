package xyz.xzaslxr.utils.generator;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;



public class ComplexObjectGenerator extends Generator<Object> {

    protected ComplexObjectGenerator(Class<Object> type) {
        super(type);
    }

    @Override
    public Object generate(SourceOfRandomness random, GenerationStatus __ignore__) {
        return new Object();
    }
}
