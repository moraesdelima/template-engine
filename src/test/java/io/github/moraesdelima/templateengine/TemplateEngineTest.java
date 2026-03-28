package io.github.moraesdelima.templateengine;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
                "{ \"cliente\": {\"nome\":\"João\",\"idade\":32,\"ativo\":false,\"endereco\":{\"rua\":\"Silveira Martins\",\"numero\":30}} }",
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

    // --- registerFormatter tests ---

    @Test
    public void test_registerFormatter_simple() {
        engine.registerFormatter("upper", (p, v) -> v != null ? v.toString().toUpperCase() : "null");
        testReplaceProperties("${registro|upper}", "123456", TemplateEngine.STRING_SERIALIZATION);
    }

    @Test
    public void test_registerFormatter_replaces_existing() {
        engine.registerFormatter("fmt", (p, v) -> "first");
        engine.registerFormatter("fmt", (p, v) -> "second");
        testReplaceProperties("${registro|fmt}", "second", TemplateEngine.STRING_SERIALIZATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_registerFormatter_null_name_throws() {
        engine.registerFormatter(null, (p, v) -> "x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_registerFormatter_empty_name_throws() {
        engine.registerFormatter("", (p, v) -> "x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_registerFormatter_null_formatter_throws() {
        engine.registerFormatter("fmt", null);
    }

    // --- formatter dispatch tests ---

    @Test
    public void test_formatter_applied_to_placeholder() throws Exception {
        engine.registerFormatter("upper", (p, v) -> v.toString().toUpperCase());
        String result = engine.process("Nome: ${cliente.nome|upper}", testBean, TemplateEngine.STRING_SERIALIZATION);
        assertEquals("Nome: JOÃO", result);
    }

    @Test
    public void test_multiple_formatters_in_template() throws Exception {
        engine.registerFormatter("upper", (p, v) -> v.toString().toUpperCase());
        engine.registerFormatter("stars", (p, v) -> "***" + v + "***");
        String result = engine.process("${cliente.nome|upper} ${registro|stars}", testBean, TemplateEngine.STRING_SERIALIZATION);
        assertEquals("JOÃO ***123456***", result);
    }

    @Test
    public void test_mixed_placeholder_with_and_without_formatter() throws Exception {
        engine.registerFormatter("upper", (p, v) -> v.toString().toUpperCase());
        String result = engine.process("${registro} ${cliente.nome|upper}", testBean, TemplateEngine.STRING_SERIALIZATION);
        assertEquals("123456 JOÃO", result);
    }

    @Test
    public void test_formatter_returning_null_inserts_null_string() throws Exception {
        engine.registerFormatter("nullfmt", (p, v) -> null);
        String result = engine.process("${registro|nullfmt}", testBean, TemplateEngine.STRING_SERIALIZATION);
        assertEquals("null", result);
    }

    @Test
    public void test_formatter_returning_empty_string() throws Exception {
        engine.registerFormatter("empty", (p, v) -> "");
        String result = engine.process("${registro|empty}", testBean, TemplateEngine.STRING_SERIALIZATION);
        assertEquals("", result);
    }

    @Test(expected = FormatterNotFoundException.class)
    public void test_unregistered_formatter_throws_FormatterNotFoundException() throws Exception {
        engine.process("${registro|naoExiste}", testBean, TemplateEngine.STRING_SERIALIZATION);
    }

    @Test
    public void test_runtime_exception_propagated_from_formatter() {
        engine.registerFormatter("boom", (p, v) -> { throw new RuntimeException("boom!"); });
        try {
            engine.process("${registro|boom}", testBean, TemplateEngine.STRING_SERIALIZATION);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("boom!", e.getMessage());
        } catch (Exception e) {
            fail("Expected RuntimeException, got: " + e.getClass());
        }
    }

    @Test
    public void test_checked_exception_wrapped_in_SerializePropertyException() {
        engine.registerFormatter("checked", (p, v) -> { throw new Exception("checked!"); });
        try {
            engine.process("${registro|checked}", testBean, TemplateEngine.STRING_SERIALIZATION);
            fail("Expected SerializePropertyException");
        } catch (SerializePropertyException e) {
            assertEquals("checked!", e.getCause().getMessage());
        } catch (Exception e) {
            fail("Expected SerializePropertyException, got: " + e.getClass());
        }
    }

    // --- date formatting tests ---

    @Test
    public void test_formatter_LocalDate_to_yyyymmdd() throws Exception {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        engine.registerFormatter("yyyymmdd", (p, v) -> v != null ? ((LocalDate) v).format(fmt) : "null");
        testBean.setDataLocal(LocalDate.of(2024, 3, 27));
        String result = engine.process("${dataLocal|yyyymmdd}", testBean, TemplateEngine.STRING_SERIALIZATION);
        assertEquals("20240327", result);
    }

    @Test
    public void test_formatter_reformat_string_date_ddmmyyyy_to_yyyymmdd() throws Exception {
        DateTimeFormatter input = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter output = DateTimeFormatter.ofPattern("yyyyMMdd");
        engine.registerFormatter("reformat", (p, v) -> {
            if (v == null) return "null";
            return LocalDate.parse(v.toString(), input).format(output);
        });
        testBean.setRegistro("27/03/2024");
        String result = engine.process("${registro|reformat}", testBean, TemplateEngine.STRING_SERIALIZATION);
        assertEquals("20240327", result);
    }

    // --- coverage gap tests ---

    @Test
    public void test_LocalTime_property_with_STRING_SERIALIZATION() {
        testBean.setHoraLocal(LocalTime.of(10, 30, 0));
        testReplaceProperties("Hora: ${horaLocal}", "Hora: 10:30", TemplateEngine.STRING_SERIALIZATION);
    }

    @Test
    public void test_LocalTime_property_with_JSON_SERIALIZATION() {
        testBean.setHoraLocal(LocalTime.of(10, 30, 0));
        testReplaceProperties("Hora: ${horaLocal}", "Hora: \"10:30\"", TemplateEngine.JSON_SERIALIZATION);
    }

    @Test
    public void test_boolean_property_with_STRING_SERIALIZATION() {
        cliente.setAtivo(true);
        testReplaceProperties("Ativo: ${cliente.ativo}", "Ativo: true", TemplateEngine.STRING_SERIALIZATION);
    }

    @Test
    public void test_boolean_property_with_JSON_SERIALIZATION() {
        cliente.setAtivo(true);
        testReplaceProperties("Ativo: ${cliente.ativo}", "Ativo: true", TemplateEngine.JSON_SERIALIZATION);
    }

    @Test
    public void test_map_integer_value_with_STRING_SERIALIZATION() {
        MapBean mapBean = new MapBean();
        mapBean.setDados(Map.of("score", 42));
        testBean.setMapBean(mapBean);
        try {
            String result = engine.process("${mapBean.dados.score}", testBean, TemplateEngine.STRING_SERIALIZATION);
            assertEquals("42", result);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void test_map_integer_value_with_JSON_SERIALIZATION() {
        MapBean mapBean = new MapBean();
        mapBean.setDados(Map.of("score", 42));
        testBean.setMapBean(mapBean);
        try {
            String result = engine.process("${mapBean.dados.score}", testBean, TemplateEngine.JSON_SERIALIZATION);
            assertEquals("42", result);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void test_map_boolean_value_with_STRING_SERIALIZATION() {
        MapBean mapBean = new MapBean();
        mapBean.setDados(Map.of("ativo", true));
        testBean.setMapBean(mapBean);
        try {
            String result = engine.process("${mapBean.dados.ativo}", testBean, TemplateEngine.STRING_SERIALIZATION);
            assertEquals("true", result);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // --- additional formatter coverage ---

    @Test
    public void test_formatter_receives_null_resolved_value() throws Exception {
        engine.registerFormatter("safe", (p, v) -> v == null ? "N/A" : v.toString());
        testBean.setRegistro(null);
        String result = engine.process("${registro|safe}", testBean, TemplateEngine.STRING_SERIALIZATION);
        assertEquals("N/A", result);
    }

    @Test
    public void test_formatter_receives_correct_nested_property_name() throws Exception {
        engine.registerFormatter("upper", (p, v) -> p + "=" + v.toString().toUpperCase());
        String result = engine.process("${cliente.nome|upper}", testBean, TemplateEngine.STRING_SERIALIZATION);
        assertEquals("cliente.nome=JOÃO", result);
    }

    @Test
    public void test_FormatterNotFoundException_contains_formatter_name() {
        try {
            engine.process("${registro|naoExiste}", testBean, TemplateEngine.STRING_SERIALIZATION);
            fail("Expected FormatterNotFoundException");
        } catch (FormatterNotFoundException e) {
            assertEquals("naoExiste", e.getFormatterName());
        } catch (Exception e) {
            fail("Expected FormatterNotFoundException, got: " + e.getClass());
        }
    }

    @Test
    public void test_nonexistent_path_with_formatter_throws_GetPropertyException() {
        engine.registerFormatter("upper", (p, v) -> v.toString().toUpperCase());
        try {
            engine.process("${campoInexistente|upper}", testBean, TemplateEngine.STRING_SERIALIZATION);
            fail("Expected GetPropertyException");
        } catch (GetPropertyException e) {
            assertEquals("campoInexistente", e.getProperty());
        } catch (Exception e) {
            fail("Expected GetPropertyException, got: " + e.getClass());
        }
    }

    @Test
    public void test_formatter_with_JSON_SERIALIZATION_bypasses_gson() throws Exception {
        engine.registerFormatter("upper", (p, v) -> v.toString().toUpperCase());
        String result = engine.process("${cliente.nome|upper}", testBean, TemplateEngine.JSON_SERIALIZATION);
        // formatter bypasses Gson — no quotes added
        assertEquals("JOÃO", result);
    }

}
