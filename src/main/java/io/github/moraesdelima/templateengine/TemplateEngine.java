package io.github.moraesdelima.templateengine;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

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
    private final Map<String, CustomFormatter> formatters = new HashMap<>();
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalTime.class,
                    (JsonSerializer<LocalTime>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .create();

    public String process(String template, Object bean)
            throws GetPropertyException, SerializePropertyException, FormatterNotFoundException {
        return process(template, bean, STRING_SERIALIZATION);
    }

    /**
     * Registers a custom formatter under the given name.
     * The formatter can be referenced in templates using the syntax {@code ${path|name}}.
     *
     * @param name      the formatter identifier (must not be null or empty)
     * @param formatter the formatter implementation (must not be null)
     * @throws IllegalArgumentException if name is null/empty or formatter is null
     */
    public void registerFormatter(String name, CustomFormatter formatter) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("formatter name must not be null or empty");
        }
        if (formatter == null) {
            throw new IllegalArgumentException("formatter must not be null");
        }
        formatters.put(name, formatter);
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
     * @return the template with all properties replaced with their respective values
     * @throws GetPropertyException       if the value of a property cannot be obtained
     * @throws SerializePropertyException if an error occurs during serialization
     */
    public String process(
            String template, Object bean, int serializationType)
            throws GetPropertyException, SerializePropertyException, FormatterNotFoundException {
        Pattern pattern = Pattern.compile("\\$\\{([^|{}]+)(?:\\|([^}]+))?\\}");
        Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String property = matcher.group(1);
            String formatterName = matcher.group(2);
            String replacement;
            if (formatterName != null) {
                CustomFormatter formatter = formatters.get(formatterName);
                if (formatter == null) {
                    throw new FormatterNotFoundException(formatterName);
                }
                Object resolvedValue = getPropertyValue(bean, property);
                replacement = applyFormatter(formatter, property, resolvedValue, bean.getClass());
            } else {
                replacement = serializeProperty(bean, property, serializationType);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Applies a registered formatter to a resolved property value.
     */
    private String applyFormatter(CustomFormatter formatter, String property,
            Object resolvedValue, Class<?> beanClass)
            throws SerializePropertyException {
        try {
            String result = formatter.format(property, resolvedValue);
            return result == null ? "null" : result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SerializePropertyException(property, beanClass, e);
        }
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
        Class<?> beanClass = bean.getClass();
        String[] nestedProperties = name.split("\\.");

        for (String property : nestedProperties) {

            if (value == null) {
                return null;
            }

            if (value instanceof Map) {
                value = ((Map<?, ?>) value).get(property);
                beanClass = value != null ? value.getClass() : Object.class;
                continue;
            }

            PropertyDescriptor pd;
            try {
                String getterName = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
                try {
                    pd = new PropertyDescriptor(property, beanClass, getterName, null);
                } catch (IntrospectionException e) {
                    // fallback para boolean: isXxx()
                    String isGetterName = "is" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
                    pd = new PropertyDescriptor(property, beanClass, isGetterName, null);
                }
            } catch (IntrospectionException e) {
                throw new GetPropertyException(property, beanClass, e);
            }

            Method getter = pd.getReadMethod();
            if (getter == null) {
                throw new GetPropertyException(property, beanClass);
            }

            try {
                value = getter.invoke(value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new GetPropertyException(property, value.getClass(), e);
            }

            beanClass = getter.getReturnType();
        }

        return value;
    }

}
