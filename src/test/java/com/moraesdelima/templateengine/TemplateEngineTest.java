package com.moraesdelima.templateengine;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import lombok.Data;

public class TemplateEngineTest {

    private static Cliente cliente;
    private static Endereco endereco;
    private static TestBean testBean;

    @Before
    public void setUp() {
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
        testBean = null;
        endereco = null;
        cliente = null;
    }

    private void testReplaceProperties(String template, String expectedResult, int serializationType) {
        try {
            String result = TemplateEngine.replaceProperties(template, testBean, serializationType);
            assertEquals(expectedResult, result);
        } catch (Exception anException) {
            fail("Expected to not be thrown any exceptios");
        }
    }

    private <T extends Exception> void testReplacePropertiesThrowsAnException(
            String template, String expectedResult, int serializationType,
            Class<T> exceptionClass) {
        try {
            TemplateEngine.replaceProperties(template, testBean, serializationType);
            fail("Expected an " + exceptionClass.getSimpleName() + " to be thrown");
        } catch (Exception anException) {
            assertEquals(anException.getClass(), exceptionClass);
            assertThat(anException.getMessage(), is(expectedResult));
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
                "Can't serialize property cliente from class com.moraesdelima.templateengine.TemplateEngineTest$TestBean witch TemplateEngine.STRING_SERIALIZATION",
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
                "Can't serialize property lista from class com.moraesdelima.templateengine.TemplateEngineTest$TestBean witch TemplateEngine.STRING_SERIALIZATION",
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
    public void test_replacing_a_non_existent_property_with_TemplateEngine_JSON_SERIALIZATION_serialization_Type() {
        testReplacePropertiesThrowsAnException(
                "This property doesn't exist: ${nonExistentProperty}",
                "Can't get property nonExistentProperty from class com.moraesdelima.templateengine.TemplateEngineTest$TestBean",
                TemplateEngine.JSON_SERIALIZATION,
                GetPropertyException.class);
    }

    @Test
    public void test_replacing_a_non_existent_property_with_TemplateEngine_STRING_SERIALIZATION_serialization_Type() {
        testReplacePropertiesThrowsAnException(
                "This property doesn't exist: ${nonExistentProperty}",
                "Can't get property nonExistentProperty from class com.moraesdelima.templateengine.TemplateEngineTest$TestBean",
                TemplateEngine.STRING_SERIALIZATION,
                GetPropertyException.class);
    }

    @Data
    static class Endereco {
        private String rua;
        private int numero;
    }

    @Data
    static class Cliente {
        private String nome;
        private int idade;
        private Endereco endereco;
    }

    @Data
    static class TestBean {
        private String registro;
        private Cliente cliente;
        private List<String> lista;
    }

}
