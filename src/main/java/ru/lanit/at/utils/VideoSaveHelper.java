package ru.lanit.at.utils;


import com.codeborne.selenide.Selenide;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanit.at.utils.allure.AllureHelper;

import java.net.URI;
import java.net.URL;

import static io.restassured.RestAssured.given;

public class VideoSaveHelper {
    private final static Logger LOGGER = LoggerFactory.getLogger(VideoSaveHelper.class);

    private final String sessionId;
    private final String hubUrl;

    public VideoSaveHelper(String sessionId, String hubUrl) {
        this.sessionId = sessionId;
        this.hubUrl = hubUrl;
    }

    /**
     * Метод для прикрепления видео к Аллюр отчету
     */
    public void attachVideoFileRest() {
        waitVideoFileDone();
        Response response = RestAssured.given().when()
                .get(buildVideoURL(sessionId));
        AllureHelper.attachSelenoidVideo("Video", response
                .then().extract().asInputStream());
    }

    /**
     * Метод для удаления видео из селенойда.
     */
    public void deleteSelenoidVideoRest() {
        try {
            given().delete(buildVideoURL(sessionId)).then().statusCode(200);
        } catch (Exception e) {
            LOGGER.warn("Произошла ошибка при удалении видео: " + e.getMessage());
        }
    }


    /**
     * Метод для построения урл с видео файлом
     */
    private URL buildVideoURL(String sessionId) {
        try {
            URI uri = new URI("http", hubUrl, "/video/" + sessionId + ".mp4", null, null);
            URL url = uri.toURL();
            LOGGER.info("URL для скачивания файла с видео  = " + url);
            return url;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать url для скачивания видео", e);
        }
    }

    /**
     * Ожидание формирования видео в селенойде
     */
    private void waitVideoFileDone() {
        for (int i = 0; i < 60; i++) {
            if (RestAssured.given().when()
                    .get(buildVideoURL(sessionId)).statusCode() == 200) {
                return;
            }
            LOGGER.info("Ожидание подготовки видео файла для скачивания");
            Selenide.sleep(5000);
        }
        throw new RuntimeException("Ошибка ожидания подготовки видео файла для скачивания");
    }
}