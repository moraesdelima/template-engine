package io.github.moraesdelima.templateengine;

public class TemplateEngineException extends Exception {

    public TemplateEngineException(String message) {
        super(message);
    }

    public TemplateEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
