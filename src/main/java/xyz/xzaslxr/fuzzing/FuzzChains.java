package xyz.xzaslxr.fuzzing;

import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import xyz.xzaslxr.utils.generator.ComplexObjectGenerator;


/**
 * FuzzChains 用于Fuzzing libraries，主要与JQF进行交互。
 */

@RunWith(JQF.class)
public class FuzzChains {

    @Fuzz
    public void testTrigger(@From(ComplexObjectGenerator.class) Object inputObject) {

    }
}