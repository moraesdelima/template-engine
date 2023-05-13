package io.github.moraesdelima.templateengine;

import lombok.Getter;

@Getter
public class SerializePropertyException extends TemplateEngineException {
    private static final String FROM_CLASS = " from class ";
    private static final String WITH = " witch ";
    private static final String CAN_T_SERIALIZE_PROPERTY = "Can't serialize property ";

    private final String property;
    private final Class<?> beanClass;

    public SerializePropertyException(String property, Class<?> beanClass) {
        super(CAN_T_SERIALIZE_PROPERTY + property + FROM_CLASS + beanClass.getCanonicalName());
        this.property = property;
        this.beanClass = beanClass;
    }

    public SerializePropertyException(String property, Class<?> beanClass, Throwable cause) {
        super(CAN_T_SERIALIZE_PROPERTY + property + FROM_CLASS + beanClass.getCanonicalName(), cause);
        this.property = property;
        this.beanClass = beanClass;
    }

    public SerializePropertyException(String property, Class<?> beanClass, int serializationType) {
        super(CAN_T_SERIALIZE_PROPERTY + property + FROM_CLASS + beanClass.getCanonicalName() + WITH
                + (serializationType == 0 ? "TemplateEngine.STRING_SERIALIZATION"
                        : "TemplateEngine.JSON_SERIALIZATION"));
        this.property = property;
        this.beanClass = beanClass;
    }

    public SerializePropertyException(String property, Class<?> beanClass, int serializationType, Throwable cause) {
        super(CAN_T_SERIALIZE_PROPERTY + property + FROM_CLASS + beanClass.getCanonicalName() + WITH
                + (serializationType == 0 ? "TemplateEngine.STRING_SERIALIZATION"
                        : "TemplateEngine.JSON_SERIALIZATION"),
                cause);
        this.property = property;
        this.beanClass = beanClass;
    }

}