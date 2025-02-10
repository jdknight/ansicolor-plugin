package hudson.plugins.ansicolor.action;

import hudson.plugins.ansicolor.JenkinsTestSupport;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@WithJenkins
class ShortlogActionCreatorIntegrationTest extends JenkinsTestSupport {
    private static final String AS_1K = repeat("a", 1024);
    private static final String BS_1K = repeat("b", 1024);
    private static final String CS_1K = repeat("c", 1024);
    private static final String DS_1K = repeat("d", 1024);

    @Test
    void canAnnotateLongLogOutputInShortlogLinesWholeFalse(JenkinsRule jenkinsRule) throws Exception {
        final String script = "ansiColor('xterm') {\n" +
            repeat("echo '\033[32m" + AS_1K + "\033[0m'\n", 150) +
            "}";
        final Map<String, String> properties = new HashMap<>();
        properties.put(ShortlogActionCreator.PROP_LINES_WHOLE, "false");
        BooleanSupplier brokenLinesJenkins = () -> Optional.ofNullable(Jenkins.getVersion())
            .orElse(ShortlogActionCreator.LINES_WHOLE_SINCE_VERSION)
            .isOlderThan(ShortlogActionCreator.LINES_WHOLE_SINCE_VERSION);
        assertOutputOnRunningPipeline(jenkinsRule, brokenLinesJenkins, "<span style=\"color: #00CD00;\">" + AS_1K + "</span>", "\033", script, true, properties);
    }

    @Test
    @Disabled("Needs adjustments for Jenkins > 2.260")
    void canAnnotateLongLogOutputInShortlogLinesWholeTrue(JenkinsRule jenkinsRule) throws Exception {
        final String script = "ansiColor('xterm') {\n" +
            repeat("echo '\033[32m" + AS_1K + "\033[0m'\n", 150) +
            "echo 'Abc'\n" +
            "}";
        final Map<String, String> properties = new HashMap<>();
        properties.put(ShortlogActionCreator.PROP_LINES_WHOLE, "true");
        BooleanSupplier wholeLinesJenkins = () -> Optional.ofNullable(Jenkins.getVersion())
            .orElse(ShortlogActionCreator.LINES_WHOLE_SINCE_VERSION)
            .isNewerThan(ShortlogActionCreator.LINES_WHOLE_SINCE_VERSION);
        assertOutputOnRunningPipeline(jenkinsRule, wholeLinesJenkins, "<span style=\"color: #00CD00;\">" + AS_1K + "</span>", "\033", script, true, properties);
    }

    @Test
    void canAnnotateLongLogOutputInShortlogMultipleStepsLinesWholeFalse(JenkinsRule jenkinsRule) throws Exception {
        final String script = "echo '\033[32mBeginning\033[0m'\n" +
            "ansiColor('vga') {\n" +
            repeat("    echo '\033[32m" + AS_1K + "\033[0m'\n", 11) +
            "}\n" +
            "ansiColor('xterm') {\n" +
            repeat("    echo '\033[32m" + BS_1K + "\033[0m'\n", 30) +
            "}\n" +
            "ansiColor('css') {\n" +
            repeat("    echo '\033[32m" + CS_1K + "\033[0m'\n", 50) +
            repeat("    echo '\033[32m" + DS_1K + "\033[0m'\n", 50) +
            "}\n" +
            "echo 'End'";

        final Map<String, String> properties = new HashMap<>();
        properties.put(ShortlogActionCreator.PROP_LINES_WHOLE, "false");
        assertOutputOnRunningPipeline(
            jenkinsRule,
            Arrays.asList(
                "<span style=\"color: #00CD00;\">" + BS_1K + "</span>",
                "<span style=\"color: green;\">" + CS_1K + "</span>",
                "<span style=\"color: green;\">" + DS_1K + "</span>",
                "End"
            ),
            Arrays.asList(
                "Beginning",
                "<span style=\"color: #00AA00;\">a",
                "\033[32m" + AS_1K + "\033[0m",
                "\033[32m" + BS_1K + "\033[0m",
                "\033[32m" + CS_1K + "\033[0m",
                "\033[32m" + DS_1K + "\033[0m"
            ),
            script,
            true,
            properties
        );
    }

    @Test
    void canAnnotateLongLogOutputInShortlogMultipleStepsLinesWholeTrue(JenkinsRule jenkinsRule) throws Exception {
        final String script = "echo '\033[32mBeginning\033[0m'\n" +
            "ansiColor('vga') {\n" +
            repeat("    echo '\033[32m" + AS_1K + "\033[0m'\n", 10) +
            "}\n" +
            "ansiColor('xterm') {\n" +
            repeat("    echo '\033[32m" + BS_1K + "\033[0m'\n", 30) +
            "}\n" +
            "ansiColor('css') {\n" +
            repeat("    echo '\033[32m" + CS_1K + "\033[0m'\n", 50) +
            repeat("    echo '\033[32m" + DS_1K + "\033[0m'\n", 50) +
            "}\n" +
            "echo 'End'";

        final Map<String, String> properties = new HashMap<>();
        properties.put(ShortlogActionCreator.PROP_LINES_WHOLE, "true");
        assertOutputOnRunningPipeline(
            jenkinsRule,
            Arrays.asList(
                "\033[32m" + BS_1K + "\033[0m",
                "<span style=\"color: green;\">" + CS_1K + "</span>",
                "<span style=\"color: green;\">" + DS_1K + "</span>",
                "End"
            ),
            Arrays.asList(
                "Beginning",
                "<span style=\"color: #00AA00;\">a",
                "\033[32m" + AS_1K + "\033[0m",
                "<span style=\"color: #00CD00;\">" + BS_1K + "</span>",
                "\033[32m" + CS_1K + "\033[0m",
                "\033[32m" + DS_1K + "\033[0m"
            ),
            script,
            true,
            properties
        );
    }
}
