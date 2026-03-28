package io.github.moraesdelima.templateengine;

import lombok.Getter;

/**
 * Exception thrown when a placeholder references a formatter that has not been
 * registered in the {@link TemplateEngine}.
 *
 * @author <a href="mailto:luiz.moraes@zipdin.com.br">luiz.moraes</a>
 */
@Getter
public class FormatterNotFoundException extends TemplateEngineException {

    private final String formatterName;

    public FormatterNotFoundException(String formatterName) {
        super("Formatter not registered: " + formatterName);
        this.formatterName = formatterName;
    }
}
