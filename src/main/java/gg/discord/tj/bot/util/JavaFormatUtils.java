package gg.discord.tj.bot.util;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class JavaFormatUtils {
    public static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```.*\\n(.*)\\n?```");

    private static final Formatter FORMATTER = new Formatter();

    public static Tuple<Optional<String>, Optional<Throwable>> format(String input) {
        Optional<String> product = Optional.empty();
        Optional<Throwable> lastThrowable = Optional.empty();
        if (input == null || input.isEmpty()) {
            return new Tuple<>(product, lastThrowable);
        }

        List<Map.Entry<Function<String, String>, Function<String, String>>> phases = List.of(
            Map.entry(Function.identity(), Function.identity()),
            Map.entry(s -> "public class A{" + s + "}", s -> s.substring("public class A {\n".length(), s.length() - "\n}".length()).replaceAll(" {2}(.+)", "$1")),
            Map.entry(s -> "public class A{public<T>T b(){" + s + "}}", s -> s.substring("public class A {\n  public <T> T b() {".length(), s.length() - "\n  }\n}".length()).replaceAll(" {4}(.+)", "$1"))
        );

        for (Map.Entry<Function<String, String>, Function<String, String>> phase : phases) {
            try {
                product = Optional.of(
                    phase.getValue()
                        .apply(FORMATTER.formatSource(phase.getKey()
                            .apply(input))));
                break;
            } catch (FormatterException ex) {
                lastThrowable = Optional.of(ex);
            }
        }
        return new Tuple<>(product, lastThrowable);
    }
}
