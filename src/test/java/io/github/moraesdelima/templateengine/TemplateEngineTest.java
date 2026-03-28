package io.github.moraesdelima.templateengine;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TemplateEngineTest {

    private Cliente cliente;
    private Endereco endereco;
    private TestBean testBean;
    private TemplateEngine engine;

    @Before
    public void setUp() {
        engine = new TemplateEngine();

        cliente = new Cliente();
        cliente.setNome("João");
        cliente.setIdade(32);

        endereco = new Endereco();
        endereco.setRua("Silveira Martins");
        endereco.setNumero(30);
        cliente.setEndereco(endereco);

        testBean = new TestBean();
        testBean.setRegistro("123456");
        testBean.setCliente(cliente);
        testBean.setLista(List.of("value1", "value2", "value3", "value4", "value5"));
    }

    @After
    public void tearDown() {
        engine = null;
        testBean = null;
        endereco = null;
        cliente = null;
    }

    private void testReplaceProperties(String template, String expectedResult, int serializationType) {
        try {
            String result = engine.process(template, testBean, serializationType);
            assertEquals(expectedResult, result);
        } catch (Exception anException) {
            fail("Expected to not be thrown any exceptios");
        }
    }

    private <T extends Exception> void testReplacePropertiesThrowsAnException(
            String template,
            String failedProperty,
            Class<?> failedClass,
            int serializationType,
            Class<T> exceptionClass) {

        try {
            engine.process(template, testBean, serializationType);
            fail("Expected an " + exceptionClass.getSimpleName() + " to be thrown");
        } catch (Exception anException) {
            assertEquals(exceptionClass, anException.getClass());

            if (anException instanceof GetPropertyException) {
                String expectedResult = "Can't get property " + failedProperty + " from class "
                        + failedClass.getCanonicalName();
                assertThat(anException.getMessage(), is(expectedResult));
                assertEquals(failedProperty, ((GetPropertyException) anException).getProperty());
                assertEquals(failedClass, ((GetPropertyException) anException).getBeanClass());
            }

            if (anException instanceof SerializePropertyException) {
                String expectedResult = "Can't serialize property " + failedProperty + " from class "
                        + failedClass.getCanonicalName() + " witch TemplateEngine.STRING_SERIALIZATION";
                assertThat(anException.getMessage(), is(expectedResult));
                assertEquals(failedProperty, ((SerializePropertyException) anException).getProperty());
                assertEquals(failedClass, ((SerializePropertyException) anException).getBeanClass());
            }

        }
    }

    @Test
    public void test_replacing_a_simple_property_with_TemplateEngine_JSON_SERIALIZATION_serialization_Type() {
        testReplaceProperties(
                "O registro do cliente é ${registro}.",
                "O registro do cliente é \"123456\".",
                TemplateEngine.JSON_SERIALIZATION);
    }

    @Test
    public void test_replacing_a_simple_property_with_TemplateEngine_STRING_SERIALIZATION_serialization_Type() {
        testReplaceProperties(
                "O registro do cliente é ${registro}.",
                "O registro do cliente é 123456.",
                TemplateEngine.STRING_SERIALIZATION);
    }

    @Test
    public void test_replacing_a_nested_property_with_TemplateEngine_JSON_SERIALIZATION_serialization_Type() {
        testReplaceProperties(
                "O nome do cliente é ${cliente.nome}.",
                "O nome do cliente é \"João\".",
                TemplateEngine.JSON_SERIALIZATION);
    }

    @Test
    public void test_replacing_a_nested_property_with_TemplateEngine_STRING_SERIALIZATION_serialization_Type() {
        testReplaceProperties(
                "O nome do cliente é ${cliente.nome}.",
                "O nome do cliente é João.",
                TemplateEngine.STRING_SERIALIZATION);
    }

    @Test
    public void test_replacing_multiple_properties_with_TemplateEngine_JSON_SERIALIZATION_serialization_Type() {
        testReplaceProperties(
                "O registro do cliente é ${registro}, o nome do cliente é ${cliente.nome}, a idade é ${cliente.idade} e o endereco é Rua ${cliente.endereco.rua}, ${cliente.endereco.numero}.",
                "O registro do cliente é \"123456\", o nome do cliente é \"João\", a idade é 32 e o endereco é Rua \"Silveira Martins\", 30.",
                TemplateEngine.JSON_SERIALIZATION);
    }

    @Test
    public void test_replacing_multiple_properties_with_TemplateEngine_STRING_SERIALIZATION_serialization_Type() {
        testReplaceProperties(
                "O registro do cliente é ${registro}, o nome do cliente é ${cliente.nome}, a idade é ${cliente.idade} e o endereco é Rua ${cliente.endereco.rua}, ${cliente.endereco.numero}.",
                "O registro do cliente é 123456, o nome do cliente é João, a idade é 32 e o endereco é Rua Silveira Martins, 30.",
                TemplateEngine.STRING_SERIALIZATION);
    }

    @Test
    public void test_replacing_an_object_property_with_TemplateEngine_JSON_SERIALIZATION_serialization_Type() {
        testReplaceProperties(
                "{ \"cliente\": ${cliente} }",
                "{ \"cliente\": {\"nome\":\"João\",\"idade\":32,\"endereco\":{\"rua\":\"Silveira Martins\",\"numero\":30}} }",
                TemplateEngine.JSON_SERIALIZATION);
    }

    @Test
    public void test_replacing_an_object_property_with_TemplateEngine_STRING_SERIALIZATION_serialization_Type() {
        testReplacePropertiesThrowsAnException(
                "{ \"cliente\": ${cliente} }",
                "cliente", TestBean.class,
                TemplateEngine.STRING_SERIALIZATION,
                SerializePropertyException.class);

    }

    @Test
    public void test_replacing_an_array_property_with_TemplateEngine_JSON_SERIALIZATION_serialization_Type() {
        testReplaceProperties(
                "{ \"lista\": ${lista} }",
                "{ \"lista\": [\"value1\",\"value2\",\"value3\",\"value4\",\"value5\"] }",
                TemplateEngine.JSON_SERIALIZATION);
    }

    @Test
    public void test_replacing_an_array_property_with_TemplateEngine_STRING_SERIALIZATION_serialization_Type() {
        testReplacePropertiesThrowsAnException(
                "{ \"lista\": ${lista} }",
                "lista", TestBean.class,
                TemplateEngine.STRING_SERIALIZATION,
                SerializePropertyException.class);
    }

    @Test
    public void test_replacing_a_property_with_a_null_value_with_TemplateEngine_JSON_SERIALIZATION_serialization_Type() {
        testBean.setRegistro(null);
        testReplaceProperties(
                "O valor do registro é ${registro}",
                "O valor do registro é null",
                TemplateEngine.JSON_SERIALIZATION);
    }

    @Test
    public void test_replacing_a_property_with_a_null_value_with_TemplateEngine_STRING_SERIALIZATION_serialization_Type() {
        testBean.setRegistro(null);
        testReplaceProperties(
                "O valor do registro é ${registro}",
                "O valor do registro é null",
                TemplateEngine.STRING_SERIALIZATION);
    }

    @Test
    public void test_replacing_a_property_with_a_null_parent_with_TemplateEngine_JSON_SERIALIZATION_serialization_Type() {
        testBean.setCliente(null);
        testReplaceProperties(
                "O registro do cliente é ${registro}, o nome do cliente é ${cliente.nome}, a idade é ${cliente.idade} e o endereco é Rua ${cliente.endereco.rua}, ${cliente.endereco.numero}.",
                "O registro do cliente é \"123456\", o nome do cliente é null, a idade é null e o endereco é Rua null, null.",
                TemplateEngine.JSON_SERIALIZATION);
    }

    @Test
    public void test_replacing_a_property_with_a_null_parent_with_TemplateEngine_STRING_SERIALIZATION_serialization_Type() {
        testBean.setCliente(null);
        testReplaceProperties(
                "${cliente.nome}",
                "null",
                TemplateEngine.STRING_SERIALIZATION);
    }

    @Test
    public void test_replacing_a_deep_path_with_null_intermediate_segment() {
        testBean.getCliente().setEndereco(null);
        testReplaceProperties(
                "${cliente.endereco.rua}",
                "null",
                TemplateEngine.STRING_SERIALIZATION);
        testReplaceProperties(
                "${cliente.endereco.rua}",
                "null",
                TemplateEngine.JSON_SERIALIZATION);
    }

    @Test
    public void test_replacing_a_non_existent_property_with_TemplateEngine_JSON_SERIALIZATION_serialization_Type() {
        testReplacePropertiesThrowsAnException(
                "This property doesn't exist: ${nonExistentProperty}",
                "nonExistentProperty", TestBean.class,
                TemplateEngine.JSON_SERIALIZATION,
                GetPropertyException.class);
    }

    @Test
    public void test_replacing_a_non_existent_property_with_TemplateEngine_STRING_SERIALIZATION_serialization_Type() {
        testReplacePropertiesThrowsAnException(
                "This property doesn't exist: ${nonExistentProperty}",
                "nonExistentProperty", TestBean.class,
                TemplateEngine.STRING_SERIALIZATION,
                GetPropertyException.class);
    }

    // --- Map navigation tests ---

    @Test
    public void test_map_simple_string_value_with_STRING_SERIALIZATION() {
        MapBean mapBean = new MapBean();
        mapBean.setDados(Map.of("nome", "João"));
        testBean.setMapBean(mapBean);
        try {
            String result = engine.process("${mapBean.dados.nome}", testBean, TemplateEngine.STRING_SERIALIZATION);
            assertEquals("João", result);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void test_map_nested_map_value_with_STRING_SERIALIZATION() {
        MapBean mapBean = new MapBean();
        mapBean.setDados(Map.of("cbo", Map.of("codigo", "1234")));
        testBean.setMapBean(mapBean);
        try {
            String result = engine.process("${mapBean.dados.cbo.codigo}", testBean, TemplateEngine.STRING_SERIALIZATION);
            assertEquals("1234", result);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void test_bean_to_map_to_string_navigation() {
        MapBean mapBean = new MapBean();
        mapBean.setDados(Map.of("nome", "João"));
        testBean.setMapBean(mapBean);
        try {
            String result = engine.process("${mapBean.dados.nome}", testBean, TemplateEngine.STRING_SERIALIZATION);
            assertEquals("João", result);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void test_map_to_bean_to_string_navigation() {
        MapBean mapBean = new MapBean();
        mapBean.setDados(Map.of("cliente", cliente));
        testBean.setMapBean(mapBean);
        try {
            String result = engine.process("${mapBean.dados.cliente.nome}", testBean, TemplateEngine.STRING_SERIALIZATION);
            assertEquals("João", result);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void test_map_missing_key_returns_null_string() {
        MapBean mapBean = new MapBean();
        mapBean.setDados(Map.of("nome", "João"));
        testBean.setMapBean(mapBean);
        try {
            String result = engine.process("${mapBean.dados.inexistente}", testBean, TemplateEngine.STRING_SERIALIZATION);
            assertEquals("null", result);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void test_null_map_in_intermediate_position_returns_null_string() {
        MapBean mapBean = new MapBean();
        mapBean.setDados(null);
        testBean.setMapBean(mapBean);
        try {
            String result = engine.process("${mapBean.dados.cbo.codigo}", testBean, TemplateEngine.STRING_SERIALIZATION);
            assertEquals("null", result);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void test_map_string_value_with_JSON_SERIALIZATION() {
        MapBean mapBean = new MapBean();
        mapBean.setDados(Map.of("nome", "João"));
        testBean.setMapBean(mapBean);
        try {
            String result = engine.process("${mapBean.dados.nome}", testBean, TemplateEngine.JSON_SERIALIZATION);
            assertEquals("\"João\"", result);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // --- Getter-only virtual property tests ---

    @Test
    public void test_getter_only_property_with_STRING_SERIALIZATION() {
        testReplaceProperties(
                "Registro formatado: ${registroFormatado}",
                "Registro formatado: REG-123456",
                TemplateEngine.STRING_SERIALIZATION);
    }

    @Test
    public void test_getter_only_property_with_JSON_SERIALIZATION() {
        testReplaceProperties(
                "Registro formatado: ${registroFormatado}",
                "Registro formatado: \"REG-123456\"",
                TemplateEngine.JSON_SERIALIZATION);
    }

    @Test
    public void test_getter_only_property_null_value_with_STRING_SERIALIZATION() {
        testBean.setRegistro(null);
        testReplaceProperties(
                "${registroFormatado}",
                "null",
                TemplateEngine.STRING_SERIALIZATION);
    }

    // --- java.time type tests ---

    @Test
    public void test_LocalDate_property_with_STRING_SERIALIZATION() {
        testBean.setDataLocal(LocalDate.of(2024, 3, 27));
        testReplaceProperties(
                "Data: ${dataLocal}",
                "Data: 2024-03-27",
                TemplateEngine.STRING_SERIALIZATION);
    }

    @Test
    public void test_LocalDate_property_with_JSON_SERIALIZATION() {
        testBean.setDataLocal(LocalDate.of(2024, 3, 27));
        testReplaceProperties(
                "Data: ${dataLocal}",
                "Data: \"2024-03-27\"",
                TemplateEngine.JSON_SERIALIZATION);
    }

    @Test
    public void test_LocalDateTime_property_with_STRING_SERIALIZATION() {
        testBean.setDataHoraLocal(LocalDateTime.of(2024, 3, 27, 10, 30, 0));
        testReplaceProperties(
                "DataHora: ${dataHoraLocal}",
                "DataHora: 2024-03-27T10:30",
                TemplateEngine.STRING_SERIALIZATION);
    }

    @Test
    public void test_LocalDateTime_property_with_JSON_SERIALIZATION() {
        testBean.setDataHoraLocal(LocalDateTime.of(2024, 3, 27, 10, 30, 0));
        testReplaceProperties(
                "DataHora: ${dataHoraLocal}",
                "DataHora: \"2024-03-27T10:30\"",
                TemplateEngine.JSON_SERIALIZATION);
    }

}
