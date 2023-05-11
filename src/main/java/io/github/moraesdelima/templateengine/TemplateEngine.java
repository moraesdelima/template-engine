package io.github.moraesdelima.templateengine;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The TemplateEngine class provides methods for replacing properties in a given
 * template with their respective values
 * from a Java Bean object, either in string or JSON format.
 * 
 * @author <a href="mailto:luiz.moraes@zipdin.com.br">luiz.moraes</a>
 */
public class TemplateEngine {

    public static final int STRING_SERIALIZATION = 0;
    public static final int JSON_SERIALIZATION = 1;
    Gson gson = new Gson();

    public String process(String template, Object bean)
            throws GetPropertyException, SerializePropertyException {
        return process(template, bean, STRING_SERIALIZATION);
    }

    /**
     * Replaces all properties in the given template with their respective values
     * from the given Java Bean object.
     * 
     * @param template          the template with properties to be replaced
     * @param bean              the Java Bean object containing the property values
     * @param serializationType the type of serialization to be used for the
     *                          property values (either STRING_SERIALIZATION or
     *                          JSON_SERIALIZATION)
     * @return the template with all properties replaced with their respective
     *         values
     * @throws GetPropertyException       if the value of a property cannot be
     *                                    obtained from the Java Bean object
     * @throws SerializePropertyException if an error occurs during serialization of
     *                                    a property value
     */
    public String process(
            String template, Object bean, int serializationType)
            throws GetPropertyException, SerializePropertyException {
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String property = matcher.group(1);
            String replacement = serializeProperty(bean, property, serializationType);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));

        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Serializes the value of the given property in the given Java Bean object
     * according to the given serialization type.
     * 
     * @param bean              the Java Bean object containing the property value
     * @param property          the name of the property to be serialized
     * @param serializationType the type of serialization to be used for the
     *                          property value (either STRING_SERIALIZATION or
     *                          JSON_SERIALIZATION)
     * @return the serialized property value
     * @throws GetPropertyException       if the value of the property cannot be
     *                                    obtained from the Java Bean object
     * @throws SerializePropertyException if an error occurs during serialization of
     *                                    the property value
     */
    private String serializeProperty(
            Object bean, String property, int serializationType)
            throws GetPropertyException, SerializePropertyException {
        Object propertyValue = getPropertyValue(bean, property);

        String serializedProperty = null;
        // try {
        serializedProperty = gson.toJson(propertyValue);
        // } catch (JsonProcessingException e) {
        // throw new SerializePropertyException(property, bean.getClass(), e);
        // }

        if (serializationType == JSON_SERIALIZATION) {
            return serializedProperty;

        } else if (serializedProperty == null) {
            return null;

        } else if (serializedProperty.startsWith("\"")) {
            // After Serialization String and Date properties goes here
            serializedProperty = serializedProperty
                    .replaceFirst("\"", "");
            serializedProperty = serializedProperty
                    .substring(0, serializedProperty.length() - 1);
            return serializedProperty;

        } else if (!serializedProperty.startsWith("{")
                && !serializedProperty.startsWith("[")) {
            // After Serialization Number and Boolean properties goes here
            return serializedProperty;

        }

        // After Serialization Object and Array properties goes here
        throw new SerializePropertyException(property, bean.getClass(), serializationType);

    }

    /**
     * Gets the value of the given property in the given Java Bean object.
     * 
     * @param bean the Java Bean object containing the property value
     * @param name the name of the property to be obtained
     * @return the value of the property
     * @throws GetPropertyException if the value of the property cannot be obtained
     *                              from the Java Bean object
     */
    private Object getPropertyValue(Object bean, String name)
            throws GetPropertyException {

        Object value = bean;
        String[] parts = name.split("\\.");
        for (String parte : parts) {

            PropertyDescriptor pd;
            try {
                pd = new PropertyDescriptor(parte, value.getClass());
            } catch (IntrospectionException e) {
                throw new GetPropertyException(parte, value.getClass(), e);
            }

            Method getter = pd.getReadMethod();
            if (getter == null) {
                throw new GetPropertyException(parte, value.getClass());
            }

            try {
                value = getter.invoke(value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new GetPropertyException(parte, value.getClass(), e);
            }
        }

        return value;
    }

    public static void main(String[] args) throws GetPropertyException, SerializePropertyException {

        @Data
        @AllArgsConstructor
        class User {
            private String name;
        }
        @Data
        @AllArgsConstructor
        class MyBean {
            private User user;
        }

        TemplateEngine engine = new TemplateEngine();
        String template = "{ \"user\": ${user} }";
        MyBean bean = new MyBean(new User("John"));
        String result = engine.process(template, bean, JSON_SERIALIZATION);
        System.out.println(result);

    }

}
