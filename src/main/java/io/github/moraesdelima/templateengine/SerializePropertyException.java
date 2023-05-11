package io.github.moraesdelima.templateengine;

public class SerializePropertyException extends TemplateEngineException {
    private static final String FROM_CLASS = " from class ";
    private static final String WITH = " witch ";
    private static final String CAN_T_SERIALIZE_PROPERTY = "Can't serialize property ";

    public SerializePropertyException(String property, Class<?> beanClass) {
        super(CAN_T_SERIALIZE_PROPERTY + property + FROM_CLASS + beanClass.getCanonicalName());
    }

    public SerializePropertyException(String property, Class<?> beanClass, Throwable cause) {
        super(CAN_T_SERIALIZE_PROPERTY + property + FROM_CLASS + beanClass.getCanonicalName(), cause);
    }

    public SerializePropertyException(String property, Class<?> beanClass, int serializationType) {
        super(CAN_T_SERIALIZE_PROPERTY + property + FROM_CLASS + beanClass.getCanonicalName() + WITH
                + (serializationType == 0 ? "TemplateEngine.STRING_SERIALIZATION"
                        : "TemplateEngine.JSON_SERIALIZATION"));
    }

    public SerializePropertyException(String property, Class<?> beanClass, int serializationType, Throwable cause) {
        super(CAN_T_SERIALIZE_PROPERTY + property + FROM_CLASS + beanClass.getCanonicalName() + WITH
                + (serializationType == 0 ? "TemplateEngine.STRING_SERIALIZATION"
                        : "TemplateEngine.JSON_SERIALIZATION"),
                cause);
    }

}