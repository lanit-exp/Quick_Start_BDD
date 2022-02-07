package ru.lanit.at.corecommonstep.fragment;

import io.cucumber.core.gherkin.Argument;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.gherkin.StepType;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.DocStringArgument;
import io.cucumber.plugin.event.Location;

import java.util.List;


public class CustomStep implements Step {

    private final Argument argument;
    private final String keyWord;
    private final StepType stepType;
    private final String previousGwtKeyWord;
    private final Location location;
    private final String id;
    private final String text;

    public CustomStep(
            String id,
            String text,
            List<List<String>> argument,
            Class argumentType,
            GherkinDialect dialect,
            String previousGwtKeyWord,
            Location location,
            String keyword
    ) {
        this.id = id;
        this.text = text;
        this.argument = extractArgument(argument, argumentType, location);
        this.keyWord = keyword;
        this.stepType = extractKeyWordType(keyWord, dialect);
        this.previousGwtKeyWord = previousGwtKeyWord;
        this.location = location;
    }

    private static Argument extractArgument(List<List<String>> argument, Class argumentType, Location location) {
        if (!argument.isEmpty()) {
            if (DataTableArgument.class.equals(argumentType)) {
                return new CustomGherkinMessagesDataTableArgument(argument, location.getLine() + 1);
            } else if (DocStringArgument.class.equals(argumentType)) {
                return new CustomGherkinMessagesDocStringArgument(argument.get(0).get(0), "", "", location.getLine() + 1);
            }
            throw new IllegalStateException(String.format("Неожиданный тип значения: %s.\n", argumentType.getName()));
        }
        return null;
    }

    private static StepType extractKeyWordType(String keyWord, GherkinDialect dialect) {
        if (StepType.isAstrix(keyWord)) {
            return StepType.OTHER;
        }
        if (dialect.getGivenKeywords().contains(keyWord)) {
            return StepType.GIVEN;
        }
        if (dialect.getWhenKeywords().contains(keyWord)) {
            return StepType.WHEN;
        }
        if (dialect.getThenKeywords().contains(keyWord)) {
            return StepType.THEN;
        }
        if (dialect.getAndKeywords().contains(keyWord)) {
            return StepType.AND;
        }
        if (dialect.getButKeywords().contains(keyWord)) {
            return StepType.BUT;
        }
        throw new IllegalStateException("Keyword " + keyWord + " was neither given, when, then, and, but nor *");
    }

    @Override
    public StepType getType() {
        return stepType;
    }

    @Override
    public String getPreviousGivenWhenThenKeyword() {
        return previousGwtKeyWord;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Argument getArgument() {
        return argument;
    }

    @Override
    public String getKeyword() {
        return keyWord;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public int getLine() {
        return location.getLine();
    }

    @Override
    public Location getLocation() {
        return new Location(location.getLine(), location.getColumn());
    }
}
