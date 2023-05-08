package com.moraesdelima.templateengine;

public class GetPropertyException extends TemplateEngineException {

    private static final String FROM = " from ";
    private static final String CAN_T_GET_PROPERTY = "Can't get property ";

    public GetPropertyException(String message) {
        super(message);
    }

    public GetPropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public GetPropertyException(String property, Class<?> beanClass) {
        super(CAN_T_GET_PROPERTY + property + FROM + beanClass);
    }

    public GetPropertyException(String property, Class<?> beanClass, Throwable cause) {
        super(CAN_T_GET_PROPERTY + property + FROM + beanClass, cause);
    }

}