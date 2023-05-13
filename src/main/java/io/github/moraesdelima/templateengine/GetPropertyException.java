package io.github.moraesdelima.templateengine;

import lombok.Getter;

@Getter
public class GetPropertyException extends TemplateEngineException {

    private static final String FROM_CLASS = " from class ";
    private static final String CAN_T_GET_PROPERTY = "Can't get property ";

    private final String property;
    private final Class<?> beanClass;

    public GetPropertyException(String property, Class<?> beanClass) {
        super(CAN_T_GET_PROPERTY + property + FROM_CLASS + beanClass.getCanonicalName());
        this.property = property;
        this.beanClass = beanClass;
    }

    public GetPropertyException(String property, Class<?> beanClass, Throwable cause) {
        super(CAN_T_GET_PROPERTY + property + FROM_CLASS + beanClass.getCanonicalName(), cause);
        this.property = property;
        this.beanClass = beanClass;
    }

}