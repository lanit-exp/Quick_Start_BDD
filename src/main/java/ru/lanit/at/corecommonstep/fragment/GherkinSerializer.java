package ru.lanit.at.corecommonstep.fragment;

import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.resource.Resource;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.DocStringArgument;
import org.testng.asserts.Assertion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class GherkinSerializer {

    public static final String DATA_TAG = "@data=$";
    private static final String NL = "\n";
    private static final String SPACE = " ";
    private StringBuilder builder;
    private Assertion assertion;

    public GherkinSerializer() {
        builder = new StringBuilder();
    }

    public List<Feature> reserializeFeatures(List<Feature> cucumberFeatures, Assertion assertion) {
        this.assertion = assertion;
        FeatureParser featureParser = new FeatureParser(() -> UUID.randomUUID());
        List<Feature> parsedFeatures = new ArrayList<>();
        cucumberFeatures.forEach(cucumberFeature -> {
            builder = new StringBuilder();
            List<Pickle> pickles = cucumberFeature.getPickles();
            if (pickles.size() > 0) {
                builder.append("#language: " + pickles.get(0).getLanguage());
            } else {
                this.assertion.fail(String.format("\nПроверьте синтаксис фича-файла!:\n%s\n", cucumberFeature.getUri()));
            }
            nl(1);
            Optional<String> featureKeyword = cucumberFeature.getKeyword();
            Optional<String> featureName = cucumberFeature.getName();
            if (featureKeyword.isPresent()) {
                builder.append(featureKeyword.get()).append(":").append(SPACE);
            } else {
                this.assertion.fail("\"Нет кейворда Функционал!\"");
            }

            featureName.ifPresent(name -> builder.append(name));
            nl(1);
            pickles.forEach(this::buildScenario);

            Resource gherkinResource = new GherkinResource(builder.toString(), cucumberFeature.getUri());
            Optional<Feature> feature = featureParser.parseResource(gherkinResource);
            feature.ifPresent(parsedFeatures::add);
        });

        return parsedFeatures;
    }

    private void buildScenario(Pickle pickle) {
        buildScenarioTags(pickle);

        tab(1);
        builder.append(pickle.getKeyword()).append(":").append(SPACE).append(pickle.getName());
        nl(1);

        List<String> dataTags = pickle.getTags().stream().filter(p -> p.startsWith(DATA_TAG)).collect(Collectors.toList());
        if (!dataTags.isEmpty() && dataTags.size() > 1) {
            assertion.fail(String.format("Есть два тега '%s' в сценарии '%s'. Необходимо указать только один тег для данных!\n", dataTags.toString(), pickle.getName()));
        }

        if (!dataTags.isEmpty()) {
            pickle.getSteps().forEach(step -> buildStep(step, dataTags.get(0), pickle));
        } else {
            pickle.getSteps().forEach(step -> buildStep(step, null, pickle));
        }
    }

    private void buildScenarioTags(Pickle scenarioDefinition) {
        List<String> tags = scenarioDefinition.getTags();
        if (!tags.isEmpty()) {
            tags = tags.stream().filter(p -> !p.startsWith(DATA_TAG)).collect(Collectors.toList());
            tab(1);
            tags.forEach(tag -> builder.append(tag).append(SPACE));
            nl(1);
        }
    }

    private void buildStep(Step step, String dataTag, Pickle scenario) {
        tab(2);
        builder.append(step.getKeyword()).append(SPACE).append(step.getText());
        if (step.getArgument() != null) {
            nl(1);
            if (step.getArgument() instanceof DataTableArgument) {
                DataTableArgument table = (DataTableArgument) step.getArgument();
                table.cells().forEach(tableRow -> buildTableRow(tableRow, dataTag, scenario));
            } else if (step.getArgument() instanceof DocStringArgument) {
                tab(2);
                builder.append("\"\"\"");
                nl(1);
                DocStringArgument docString = (DocStringArgument) step.getArgument();
                tab(2);
                builder.append(docString.getContent());
                nl(1);
                tab(2);
                builder.append("\"\"\"");
                nl(1);
            }

        } else {
            builder.append("\n");
        }
    }

    private void buildTableRow(List<String> tableRow, String dataTag, Pickle scenario) {
        List<String> collect = tableRow.stream()
                .map(tableCell -> tableCell.replaceAll("\\|", "\\\\|"))
                .collect(Collectors.toList());
        tab(2);
        space(2);
        builder.append("|").append(String.join("|", collect)).append("|");
        nl(1);
    }


    private void space(int count) {
        appendTimes(SPACE, count);
    }

    private void nl(int count) {
        appendTimes(NL, count);
    }

    private void tab(int count) {
        appendTimes(SPACE, count * 2);
    }

    private void appendTimes(String source, int times) {
        for (int i = 0; i < times; i++) {
            builder.append(source);
        }
    }
}
