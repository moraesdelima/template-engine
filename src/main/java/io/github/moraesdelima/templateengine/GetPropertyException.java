package io.github.moraesdelima.templateengine;

public class GetPropertyException extends TemplateEngineException {

    private static final String FROM_CLASS = " from class ";
    private static final String CAN_T_GET_PROPERTY = "Can't get property ";

    public GetPropertyException(String message) {
        super(message);
    }

    public GetPropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public GetPropertyException(String property, Class<?> beanClass) {
        super(CAN_T_GET_PROPERTY + property + FROM_CLASS + beanClass.getCanonicalName());
    }

    public GetPropertyException(String property, Class<?> beanClass, Throwable cause) {
        super(CAN_T_GET_PROPERTY + property + FROM_CLASS + beanClass.getCanonicalName(), cause);
    }

}