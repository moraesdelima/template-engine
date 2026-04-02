
# Template Engine

[![GitHub stars](https://img.shields.io/github/stars/moraesdelima/template-engine.svg?style=social)](https://github.com/moraesdelima/template-engine/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/moraesdelima/template-engine.svg?style=social)](https://github.com/moraesdelima/template-engine/network/members)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/moraesdelima/template-engine.svg?style=social)](https://github.com/moraesdelima/template-engine/pulls)
[![GitHub issues](https://img.shields.io/github/issues/moraesdelima/template-engine.svg)](https://github.com/moraesdelima/template-engine/issues)
[![GitHub license](https://img.shields.io/github/license/moraesdelima/template-engine.svg)](https://github.com/moraesdelima/template-engine/blob/main/LICENSE)

Template Engine é uma biblioteca Java para substituir placeholders em templates com valores vindos de Java Beans, Maps ou qualquer combinação dos dois. Suporta serialização em string ou JSON, navegação em propriedades aninhadas, tipos `java.time` e formatadores customizados.

## Table of Contents

- [Template Engine](#template-engine)
  - [Table of Contents](#table-of-contents)
  - [Usage](#usage)
    - [Configuration](#configuration)
    - [Basic Usage](#basic-usage)
    - [Nested Properties](#nested-properties)
    - [Map Navigation](#map-navigation)
    - [JSON Serialization](#json-serialization)
    - [java.time Support](#javatime-support)
    - [Custom Formatters](#custom-formatters)
    - [Handling Exceptions](#handling-exceptions)
    - [Building from Source](#building-from-source)
  - [Dependencies](#dependencies)
  - [Contributing](#contributing)
  - [License](#license)

## Usage

### Configuration

Adicione a dependência ao seu `pom.xml`:

```xml
<dependency>
    <groupId>io.github.moraesdelima</groupId>
    <artifactId>template-engine</artifactId>
    <version>1.4.1</version>
</dependency>
```

### Basic Usage

Use `${propertyName}` para referenciar qualquer propriedade do bean:

```java
TemplateEngine engine = new TemplateEngine();
String template = "Hello, ${name}!";
MyBean bean = new MyBean("John");
String result = engine.process(template, bean);
// Hello, John!
```

### Nested Properties

Navegue em propriedades aninhadas com notação de ponto. Se qualquer segmento do caminho for `null`, o placeholder é substituído por `"null"` sem lançar exceção:

```java
String template = "Rua: ${cliente.endereco.rua}";
String result = engine.process(template, bean);
// Rua: Silveira Martins
```

### Map Navigation

O engine navega transparentemente dentro de `Map`s usando a mesma notação de ponto. Você pode misturar beans e maps no mesmo caminho:

```java
// Bean → Map → valor
MapBean bean = new MapBean();
bean.setDados(Map.of("nome", "João"));

String result = engine.process("${dados.nome}", bean);
// João

// Bean → Map → Bean → valor
bean.setDados(Map.of("cliente", clienteBean));
String result = engine.process("${dados.cliente.nome}", bean);
// João
```

Chaves inexistentes no Map retornam `"null"` sem lançar exceção.

### JSON Serialization

Passe `TemplateEngine.JSON_SERIALIZATION` para serializar os valores como JSON. Útil para montar payloads diretamente no template:

```java
// Valor simples — string recebe aspas
engine.process("${name}", bean, JSON_SERIALIZATION);
// "John"

// Objeto completo
engine.process("{ \"user\": ${user} }", bean, JSON_SERIALIZATION);
// { "user": {"name":"John","age":30} }

// Array
engine.process("{ \"tags\": ${tags} }", bean, JSON_SERIALIZATION);
// { "tags": ["java","library"] }
```

> Tentar serializar um objeto ou array com `STRING_SERIALIZATION` lança `SerializePropertyException`. Use `JSON_SERIALIZATION` nesses casos.

### java.time Support

`LocalDate`, `LocalDateTime` e `LocalTime` são suportados nativamente, sem configuração extra:

```java
bean.setDataLocal(LocalDate.of(2024, 3, 27));
engine.process("Data: ${dataLocal}", bean);
// Data: 2024-03-27

bean.setDataHoraLocal(LocalDateTime.of(2024, 3, 27, 10, 30, 0));
engine.process("DataHora: ${dataHoraLocal}", bean);
// DataHora: 2024-03-27T10:30
```

Com `JSON_SERIALIZATION`, os valores são envolvidos em aspas: `"2024-03-27"`.

### Custom Formatters

Registre formatadores para controlar exatamente como um valor é exibido no template. Use a sintaxe `${path|formatterName}`:

```java
engine.registerFormatter("upper", (property, value) ->
    value != null ? value.toString().toUpperCase() : "null"
);

engine.process("${cliente.nome|upper}", bean);
// JOÃO
```

O formatador recebe dois argumentos:
- `property` — o caminho completo do placeholder (ex: `"cliente.nome"`)
- `value` — o valor resolvido via reflection; pode ser `null`

Você pode registrar quantos formatadores precisar e usá-los no mesmo template:

```java
engine.registerFormatter("stars", (p, v) -> "***" + v + "***");

engine.process("${cliente.nome|upper} ${registro|stars}", bean);
// JOÃO ***123456***
```

Formatadores também funcionam com `JSON_SERIALIZATION` — nesse caso, o Gson é ignorado e o retorno do formatador é inserido diretamente, sem aspas adicionais.

**Comportamentos importantes:**
- Se o formatador retornar `null`, o engine insere a string literal `"null"`.
- `RuntimeException` lançada pelo formatador é propagada diretamente.
- Checked exceptions são encapsuladas em `SerializePropertyException`.
- Referenciar um formatador não registrado lança `FormatterNotFoundException`.

**Exemplo com data customizada:**

```java
DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
engine.registerFormatter("yyyymmdd", (p, v) ->
    v != null ? ((LocalDate) v).format(fmt) : "null"
);

bean.setDataLocal(LocalDate.of(2024, 3, 27));
engine.process("${dataLocal|yyyymmdd}", bean);
// 20240327
```

### Handling Exceptions

O método `process` pode lançar três tipos de exceção:

- `GetPropertyException` — a propriedade não existe ou não pode ser lida do bean
- `SerializePropertyException` — erro ao serializar o valor (ex: objeto com `STRING_SERIALIZATION`)
- `FormatterNotFoundException` — o template referencia um formatador que não foi registrado

```java
try {
    String result = engine.process(template, bean);
} catch (GetPropertyException e) {
    System.err.println("Propriedade inválida: " + e.getProperty());
    System.err.println("Classe: " + e.getBeanClass().getSimpleName());
} catch (SerializePropertyException e) {
    System.err.println("Erro de serialização: " + e.getProperty());
} catch (FormatterNotFoundException e) {
    System.err.println("Formatador não registrado: " + e.getFormatterName());
}
```

`GetPropertyException` e `SerializePropertyException` expõem:
- `getProperty()` — o nome da propriedade que causou o erro
- `getBeanClass()` — a classe do bean onde o erro ocorreu

`FormatterNotFoundException` expõe:
- `getFormatterName()` — o nome do formatador referenciado no template

### Building from Source

Requisitos:
- JDK 11 ou superior
- Apache Maven 3.x

```bash
mvn package
```

O JAR será gerado em `target/`.

## Dependencies

| Dependency | Version | Scope |
|---|---|---|
| org.projectlombok:lombok | 1.18.36 | provided |
| com.google.code.gson:gson | 2.10.1 | compile |
| junit:junit | 4.13.2 | test |
| org.junit.jupiter:junit-jupiter-api | 5.7.2 | test |
| org.mockito:mockito-core | 4.2.0 | test |
| org.mockito:mockito-junit-jupiter | 4.2.0 | test |

## Contributing

**Reportando bugs:** Abra uma issue no repositório descrevendo o problema, mensagens de erro e passos para reproduzir.

**Sugerindo funcionalidades:** Abra uma issue descrevendo o caso de uso e o comportamento esperado.

**Contribuindo com código:**

1. Faça um fork do repositório
2. Clone localmente e crie um branch para sua mudança
3. Implemente e teste suas alterações
4. Abra um pull request descrevendo o que foi feito

## License

MIT License. Veja o arquivo [LICENSE](LICENSE) para detalhes.
