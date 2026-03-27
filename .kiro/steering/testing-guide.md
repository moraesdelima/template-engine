---
inclusion: manual
---

# Guia de Testes

## Estrutura dos Testes Existentes

Os testes cobrem os seguintes cenários em `TemplateEngineTest`:

| Teste | STRING | JSON |
|---|---|---|
| Propriedade simples (String) | ✅ | ✅ |
| Propriedade aninhada (dot-notation) | ✅ | ✅ |
| Múltiplas propriedades no mesmo template | ✅ | ✅ |
| Propriedade do tipo Object | lança `SerializePropertyException` | ✅ |
| Propriedade do tipo List/Array | lança `SerializePropertyException` | ✅ |
| Propriedade com valor null | ✅ (`null`) | ✅ (`null`) |
| Parent null em path aninhado | lança `GetPropertyException` | lança `GetPropertyException` |
| Propriedade inexistente | lança `GetPropertyException` | lança `GetPropertyException` |

## Beans de Teste Disponíveis

### TestBean (raiz)
```java
@Data
public class TestBean {
    private String registro;    // "123456"
    private Cliente cliente;
    private List<String> lista; // ["value1"..."value5"]
}
```

### Cliente
```java
@Data
public class Cliente {
    private String nome;     // "João"
    private int idade;       // 32
    private Endereco endereco;
}
```

### Endereco
```java
@Data
public class Endereco {
    private String rua;   // "Silveira Martins"
    private int numero;   // 30
}
```

## Como Escrever Novos Testes

### Teste de substituição bem-sucedida
```java
@Test
public void test_[descricao]_with_[TIPO]_serialization_Type() {
    testReplaceProperties(
        "template com ${propriedade}",
        "resultado esperado",
        TemplateEngine.[TIPO]_SERIALIZATION);
}
```

### Teste de exceção esperada
```java
@Test
public void test_[descricao]_throws_[ExceptionType]() {
    testReplacePropertiesThrowsAnException(
        "template com ${propriedade}",
        "nomePropriedade",       // property que causou o erro
        ClasseDoBean.class,      // beanClass onde ocorreu o erro
        TemplateEngine.[TIPO]_SERIALIZATION,
        GetPropertyException.class // ou SerializePropertyException.class
    );
}
```

## Cenários Ainda Não Cobertos (gaps identificados)

- Propriedade do tipo `Date` / `LocalDate` / `LocalDateTime`
- Propriedade do tipo `enum`
- Template vazio (`""`)
- Template sem nenhum placeholder
- Template com placeholder repetido (`${nome} e ${nome}`)
- Propriedade com valor numérico `0` ou `false`
- Profundidade de aninhamento > 2 níveis (ex: `a.b.c.d`)
- Bean com herança (getter herdado de superclasse)
- Propriedade com nome em camelCase composto (ex: `nomeCompleto`)
