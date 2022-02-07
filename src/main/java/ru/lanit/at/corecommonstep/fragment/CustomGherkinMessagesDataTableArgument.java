package ru.lanit.at.corecommonstep.fragment;

import io.cucumber.core.gherkin.DataTableArgument;

import java.util.List;

/* пришлось написать свою реализацию класса io.cucumber.core.gherkin.messages.GherkinMessagesDataTableArgument */
public class CustomGherkinMessagesDataTableArgument implements DataTableArgument {

    private List<List<String>> cells;
    private int line;

    public CustomGherkinMessagesDataTableArgument(List<List<String>> cells, int line) {
        this.cells = cells;
        this.line = line;
    }

    @Override
    public List<List<String>> cells() {
        return cells;
    }

    @Override
    public int getLine() {
        return line;
    }
}
