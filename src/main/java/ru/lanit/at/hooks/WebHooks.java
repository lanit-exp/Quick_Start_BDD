package ru.lanit.at.hooks;

import com.codeborne.selenide.WebDriverRunner;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.aeonbits.owner.ConfigFactory;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanit.at.utils.VideoSaveHelper;
import ru.lanit.at.utils.web.properties.Configurations;

import static ru.lanit.at.assertion.AssertsManager.getAssertsManager;

public class WebHooks {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebHooks.class);

    @Before
    public void setup(Scenario scenario) {
    }

    @After
    public void close() {
        if (WebDriverRunner.hasWebDriverStarted()) {
            String sessionId = ((RemoteWebDriver) WebDriverRunner.getWebDriver()).getSessionId().toString();
            LOGGER.info("Закрытие сессии драйвера");
            WebDriverRunner.closeWebDriver();
            attachVideo(sessionId);
        }

        getAssertsManager().softAssert().assertAll();
        getAssertsManager().softAssert().flush();
    }


    /** Прикрепление видео в Аллюр отчет при условии запускать  с параметром remoteUrl. */
    private void attachVideo(String sessionId) {
        Configurations cf = ConfigFactory.create(Configurations.class
                , System.getProperties(),
                System.getenv());
        if (!cf.getRemoteURL().isEmpty() && cf.getEnableVideo()) {
            VideoSaveHelper vs = new VideoSaveHelper(sessionId, cf.getRemoteURL());
            vs.attachVideoFileRest();
            vs.deleteSelenoidVideoRest();
        }
    }


}
