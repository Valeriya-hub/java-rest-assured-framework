package listener;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Attaches InfraRetryAnalyzer to every @Test method, so the tests do not need
 * a retryAnalyzer attribute each.
 * <p>
 * Must be registered in testng.xml (<listeners>): TestNG reads annotations before
 * classes are loaded, so an annotation transformer declared with @Listeners is ignored.
 */
public class RetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(InfraRetryAnalyzer.class);
    }
}