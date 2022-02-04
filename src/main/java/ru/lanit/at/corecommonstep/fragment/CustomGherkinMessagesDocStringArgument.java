package ru.lanit.at.corecommonstep.fragment;

import io.cucumber.core.gherkin.DocStringArgument;

public class CustomGherkinMessagesDocStringArgument implements DocStringArgument {

    private String content;
    private String contentType;
    private String mediaType;
    private int line;

    public CustomGherkinMessagesDocStringArgument(String content, String contentType, String mediaType, int line) {
        this.content = content;
        this.contentType = contentType;
        this.mediaType = mediaType;
        this.line = line;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }

    @Override
    public int getLine() {
        return line;
    }
}
