package ru.lanit.at.utils.allure;

import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import io.qameta.allure.listener.StepLifecycleListener;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import org.aeonbits.owner.ConfigFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanit.at.utils.web.properties.Configurations;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static ru.lanit.at.assertion.AssertErrorType.CRITICAL_ASSERT;
import static ru.lanit.at.assertion.AssertErrorType.SOFT_ASSERT;

//import ru.lanit.at.utils.web.properties.Configurations;

public class AllureLogger implements StepLifecycleListener, TestLifecycleListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(AllureLogger.class);

    private final Configurations conf = ConfigFactory.create(Configurations.class, System.getProperties(),
            System.getenv());

    @Override
    public void beforeStepStop(StepResult result) {
        boolean screenAfterStep = conf.screenAfterStep();
        if (screenAfterStep && !result.getStatus().equals(Status.SKIPPED)) {
            Allure.addAttachment(result.getName(),
                    new ByteArrayInputStream(((TakesScreenshot)
                            WebDriverRunner.getWebDriver())
                            .getScreenshotAs(OutputType.BYTES)));
        }
    }

    @Override
    public void afterStepStop(StepResult result) {
        if (!result.getStatus().equals(Status.SKIPPED)) {
            if (isHasInnerBrokenStep(result)) {
                LOGGER.info("Устанавливаем для шага: '" + result.getName() + "', статус=BROKEN");
                result.setStatus(Status.BROKEN);
            }
        }


        if (result.getDescription() != null && (result.getDescription().contains(SOFT_ASSERT.getName()) || result.getDescription().contains(CRITICAL_ASSERT.getName()))) {
            LOGGER.info("Устанавливаем для шага: '" + result.getName() + "', статус=BROKEN");
            result.setStatus(Status.BROKEN);
        }
    }

    private boolean isHasInnerBrokenStep(StepResult result) {
        for (StepResult r : result.getSteps()) {
            if (r.getStatus().equals(Status.BROKEN)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ищет шаги-заглушки ФРАГМЕНТ "название фрагмента" и прячет шаги фрагмента под спойлер
     * @param testResult    -   результаты тестов
     */
    @Override
    public void beforeTestWrite(TestResult testResult) {
        List<StepResult> originSteps = testResult.getSteps();
        List<StepResult> newSteps = wrapFragment(originSteps);
        testResult.setSteps(newSteps);
    }

    /**
     * Метод прячет все шаги фрагмента под спойлер
     * @param originSteps   -   оригинальный набор шагов для отчета
     * @return  -   переопределенные шаги отчета
     */
    private List<StepResult> wrapFragment(List<StepResult> originSteps) {
        List<StepResult> newSteps = new ArrayList<>();
        for (int i=0; i < originSteps.size(); i++) {
            StepResult step = originSteps.get(i);
            if (step.getName().contains("ФРАГМЕНТ") && i+1 < originSteps.size()) {
                i++;
                long timeStop = step.getStop();
                List<StepResult> subSteps = new ArrayList<>();
                List<Status> statusList = new ArrayList<>();
                //идем дальше по тесту и собираем все шаги фрагмента в отдельный список
                do {
                    StepResult subStep = originSteps.get(i);
                    if (subStep.getName().contains(step.getName())) {
                        timeStop = subStep.getStop();
                        break;
                    } else {
                        subSteps.add(subStep);
                        statusList.add(subStep.getStatus());
                        i++;
                    }
                } while (i < originSteps.size());
                //если есть фрагменты внутри фрагмента
                subSteps = wrapFragment(subSteps);
                //таки все шаги фрагмента уйдут под спойлер (как саб-шаги)
                step.setSteps(subSteps);
                //ставим изменяем время конца шага, чтобы была сумма всех саб-шагов
                step.setStop(timeStop);
                //меняем статус шагу-фрагменту, если статус у одного из шагов не passed или не skipped
                if (statusList.contains(Status.FAILED)) {
                    step.setStatus(Status.FAILED);
                } else if (statusList.contains(Status.BROKEN)) {
                    step.setStatus(Status.BROKEN);
                }
                newSteps.add(step);
            } else {
                newSteps.add(step);
            }
        }
        return newSteps;
    }

}