package io.github.moraesdelima.templateengine;

/**
 * Functional interface for custom value formatters in {@link TemplateEngine}.
 * <p>
 * Implementations receive the dot-notation path of the placeholder and the
 * resolved value, and return the String to be inserted in the template.
 * <p>
 * Register a formatter via {@link TemplateEngine#registerFormatter(String, CustomFormatter)}
 * and reference it in templates using the syntax {@code ${path|formatterName}}.
 *
 * @author <a href="mailto:luiz.moraes@zipdin.com.br">luiz.moraes</a>
 */
@FunctionalInterface
public interface CustomFormatter {

    /**
     * Formats the resolved value of a placeholder.
     *
     * @param propertyName  the dot-notation path of the placeholder (e.g. "cliente.endereco.rua")
     * @param resolvedValue the value resolved via reflection; may be {@code null}
     * @return the String to be inserted in the template; if {@code null} is returned,
     *         the engine will insert the literal String {@code "null"}
     * @throws Exception if formatting fails; checked exceptions will be wrapped
     *                   in {@link SerializePropertyException}
     */
    String format(String propertyName, Object resolvedValue) throws Exception;
}
