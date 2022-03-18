package ru.lanit.at.aspects;

import io.cucumber.core.gherkin.Feature;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.testng.asserts.Assertion;
import ru.lanit.at.corecommonstep.fragment.FragmentReplacer;
import ru.lanit.at.corecommonstep.fragment.GherkinSerializer;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
public class FragmentsAspect {

    /**
     * замена фрагментов и данных в фиче
     *
     * @param features  список фич
     * @param assertion assert
     * @return список фич
     * @throws IOException
     * @throws IllegalAccessException
     */
    public static List<Feature> replaceSteps(List<Feature> features, Assertion assertion) throws IOException, IllegalAccessException {
        features = features.stream()
                .filter(cucumberFeature -> cucumberFeature.getSource() != null)
                .collect(Collectors.toList());

        FragmentReplacer fragmentReplacer = new FragmentReplacer(features, assertion);
        fragmentReplacer.replace();
        features = new GherkinSerializer().reserializeFeatures(features, assertion);
        return features;
    }

    @Pointcut("execution(* io.cucumber.core.runtime.FeaturePathFeatureSupplier.get(..))")
    public void cucumberFeatures() {
    }

    /**
     * тут используется строгий assert
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("cucumberFeatures()")
    public Object replaceSteps(ProceedingJoinPoint joinPoint) throws Throwable {
        return replaceSteps((List<Feature>) joinPoint.proceed(), new Assertion());
    }
}
