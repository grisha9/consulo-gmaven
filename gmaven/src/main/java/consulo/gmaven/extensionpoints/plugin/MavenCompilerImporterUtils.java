package consulo.gmaven.extensionpoints.plugin;

import consulo.gmaven.api.model.MavenProject;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class MavenCompilerImporterUtils {
    private static final String propStartTag = "${";
    private static final String propEndTag = "}";

    @Nonnull
    public static List<String> collectCompilerArgs(@Nonnull MavenProject mavenProject,
                                            @Nullable Element pluginConfiguration) {
        var options = new ArrayList<String>();
        var parameters = pluginConfiguration != null ? pluginConfiguration.getChild("parameters") : null;
        var propertyCompilerParameters = mavenProject.getProperties().get("maven.compiler.parameters");
        if (parameters != null && parameters.getTextTrim() != null
                && "true".equalsIgnoreCase(parameters.getTextTrim())) {
            options.add("-parameters");
        } else if (parameters == null && propertyCompilerParameters instanceof String
                && "true".equalsIgnoreCase((String) propertyCompilerParameters)) {
            options.add("-parameters");
        }

        if (pluginConfiguration == null) return options;

        Optional.ofNullable(pluginConfiguration.getChild("proc"))
                .map(Element::getValue)
                .ifPresent(it -> {
                    if ("none".equalsIgnoreCase(it)) options.add("-proc:none");
                    if ("only".equalsIgnoreCase(it)) options.add("-proc:only");
                });

        var compilerArguments = pluginConfiguration.getChild("compilerArguments");
        if (compilerArguments != null) {
            var unresolvedArgs = new HashSet<String>();
            Map<String, String> effectiveArguments = compilerArguments.getChildren().stream()
                    .map(it -> {
                                var key = it.getName().startsWith("-") ? it.getName() : "-$this";
                                var value = getResolvedText(it);
                                if (value == null && hasUnresolvedProperty(it.getTextTrim())) {
                                    unresolvedArgs.add(key);
                                }
                                return new Pair<>(key, value);
                            }
                    )
                    .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

            effectiveArguments.forEach((key, value) -> {
                if (key.startsWith("-A") && value != null) {
                    options.add("$key=$value");
                } else if (!unresolvedArgs.contains(key)) {
                    options.add(key);
                    ContainerUtil.addIfNotNull(options, value);
                }
            });
        }

        ContainerUtil.addIfNotNull(
                options,
                getResolvedText(pluginConfiguration.getChildTextTrim("compilerArgument"))
        );

        var compilerArgs = pluginConfiguration.getChild("compilerArgs");
        if (compilerArgs != null) {
            for (var arg : compilerArgs.getChildren("arg")) {
                ContainerUtil.addIfNotNull(options, getResolvedText(arg));
            }
            for (var compilerArg : compilerArgs.getChildren("compilerArg")) {
                ContainerUtil.addIfNotNull(options, getResolvedText(compilerArg));
            }
        }
        return options;
    }

    @Nullable
    private static String getResolvedText(@Nullable String txt) {
        var result = StringUtil.nullize(txt);
        if (result == null) return null;
        if (hasUnresolvedProperty(result)) return null;
        return result;
    }

    private static boolean hasUnresolvedProperty(String txt) {
        var i = txt.indexOf(propStartTag);
        return i >= 0 && findClosingBraceOrNextUnresolvedProperty(i + 1, txt) != -1;
    }

    private static int findClosingBraceOrNextUnresolvedProperty(int index, @Nonnull String s) {
        if (index == -1) return -1;
        var pair = findAnyOf(s, List.of(propEndTag, propStartTag), index);
        if (pair == null) return -1;
        if (pair.second.equals(propEndTag)) return pair.first;
        var nextIndex = (pair.second.equals(propStartTag)) ? pair.first + 2 : pair.first + 1;
        return findClosingBraceOrNextUnresolvedProperty(nextIndex, s);
    }

    @Nullable
    private static String getResolvedText(@Nonnull Element it) {
        return getResolvedText(it.getTextTrim());
    }

    @Nullable
    private static Pair<Integer, String> findAnyOf(@Nonnull String s,
                                            @Nonnull Collection<String> strings,
                                            int startIndex) {
        if (strings.size() == 1) {
            var string = strings.iterator().next();
            var index = s.indexOf(string, startIndex);
            return index < 0 ? null : new Pair<>(index, string);
        }

        var indices = Math.max(startIndex, 0);

        for (int i = indices; i < s.length(); i++) {
            final int index = i;
            var matchingString = strings.stream()
                    .filter(it -> it.regionMatches(0, s, index, it.length()))
                    .findFirst()
                    .orElse(null);
            if (matchingString != null)
                return new Pair<>(index, matchingString);
        }
        return null;
    }
}
