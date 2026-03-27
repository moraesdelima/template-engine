# Requirements Document

## Introduction

Esta feature adiciona suporte a formatadores customizados no `TemplateEngine`. Atualmente, o engine resolve cada placeholder via reflection e serializa o valor usando Gson (STRING ou JSON). Com esta feature, o usuário poderá registrar funções de formatação (`CustomFormatter`) no engine via `registerFormatter`, e referenciá-las diretamente na sintaxe do template usando `${campo|nomeDoFormatter}`.

Casos de uso típicos incluem: formatação de datas, mascaramento de CPF, aplicação de uppercase, formatação de moeda, entre outros. A API existente deve permanecer compatível — placeholders sem formatter (`${campo}`) continuam funcionando sem alteração.

## Glossary

- **TemplateEngine**: Classe principal da biblioteca, responsável por processar templates substituindo placeholders por valores extraídos de um Java Bean.
- **Placeholder**: Expressão no formato `${path}` ou `${path|formatterName}` dentro do template, onde `path` é uma dot-notation que referencia uma propriedade do bean (ex: `${cliente.endereco.rua}`).
- **CustomFormatter**: Interface funcional que recebe o nome da propriedade (String) e o valor resolvido (Object) e retorna a String final a ser inserida no template.
- **Formatter**: Instância de `CustomFormatter` registrada no engine via `registerFormatter`.
- **FormatterName**: Identificador string usado para referenciar um formatter registrado na sintaxe do template (ex: `uppercase` em `${cliente.nome|uppercase}`).
- **Valor Resolvido**: O valor obtido via reflection a partir do bean para um dado placeholder, antes de qualquer serialização ou formatação.
- **Serialização**: Processo de converter o valor resolvido em String, usando Gson com as regras de `STRING_SERIALIZATION` ou `JSON_SERIALIZATION`.

## Requirements

### Requirement 1: Interface CustomFormatter

**User Story:** Como desenvolvedor que usa a biblioteca, quero uma interface funcional `CustomFormatter` para que eu possa implementar lógicas de formatação customizadas de forma simples e compatível com lambdas Java.

#### Acceptance Criteria

1. THE `TemplateEngine` library SHALL provide a `CustomFormatter` functional interface with a single method `format(String propertyName, Object resolvedValue)` that returns a `String`.
2. THE `CustomFormatter` interface SHALL be annotated with `@FunctionalInterface` to allow lambda expressions.
3. WHEN `resolvedValue` is `null`, THE `CustomFormatter` implementation SHALL receive `null` as the second argument and be responsible for handling it.

---

### Requirement 2: Registro de formatters no engine

**User Story:** Como desenvolvedor, quero registrar formatters no engine via `registerFormatter` para que eu possa reutilizá-los em múltiplos templates sem passá-los como parâmetro a cada chamada.

#### Acceptance Criteria

1. THE `TemplateEngine` SHALL provide a method `registerFormatter(String name, CustomFormatter formatter)` that stores the formatter internally associated with the given name.
2. WHEN `registerFormatter` is called with a name that is already registered, THE `TemplateEngine` SHALL replace the existing formatter with the new one.
3. IF `name` is `null` or empty, THEN THE `TemplateEngine` SHALL throw an `IllegalArgumentException` with a descriptive message.
4. IF `formatter` is `null`, THEN THE `TemplateEngine` SHALL throw an `IllegalArgumentException` with a descriptive message.
5. THE `TemplateEngine` SHALL maintain the registered formatters as internal state (a `Map<String, CustomFormatter>`) across multiple calls to `process`.

---

### Requirement 3: Sintaxe de formatter no template

**User Story:** Como desenvolvedor, quero referenciar um formatter diretamente na sintaxe do placeholder (`${campo|nomeDoFormatter}`) para que a intenção de formatação fique explícita no template.

#### Acceptance Criteria

1. THE `TemplateEngine` SHALL support the placeholder syntax `${path|formatterName}` where `formatterName` refers to a formatter previously registered via `registerFormatter`.
2. WHEN a placeholder uses the `${path|formatterName}` syntax, THE `TemplateEngine` SHALL resolve the property value via reflection and then invoke the registered formatter with `(propertyName, resolvedValue)`, using the returned String as the replacement.
3. WHEN a placeholder uses the `${path|formatterName}` syntax and the named formatter is NOT registered, THE `TemplateEngine` SHALL throw an exception indicating the unregistered formatter name.
4. WHEN a placeholder uses the `${path}` syntax (no formatter), THE `TemplateEngine` SHALL apply the default serialization pipeline (Gson) as before, preserving existing behavior.

---

### Requirement 4: Compatibilidade com a API existente

**User Story:** Como desenvolvedor que já usa a biblioteca, quero que meu código existente continue funcionando sem modificações para que a adoção da nova feature seja opcional.

#### Acceptance Criteria

1. THE `TemplateEngine` SHALL preserve the existing method signatures `process(String template, Object bean)` and `process(String template, Object bean, int serializationType)` without behavioral changes.
2. WHEN `process(String template, Object bean)` is called, THE `TemplateEngine` SHALL behave identically to the behavior prior to this feature.
3. WHEN `process(String template, Object bean, int serializationType)` is called, THE `TemplateEngine` SHALL behave identically to the behavior prior to this feature.

---

### Requirement 5: Propagação de exceções dentro do CustomFormatter

**User Story:** Como desenvolvedor, quero que exceções lançadas dentro do meu formatter sejam propagadas de forma previsível para que eu possa tratar erros de formatação no meu código.

#### Acceptance Criteria

1. IF the `CustomFormatter.format` method throws a `RuntimeException`, THEN THE `TemplateEngine` SHALL propagate the exception without wrapping it.
2. IF the `CustomFormatter.format` method throws a checked `Exception`, THEN THE `TemplateEngine` SHALL wrap it in a `SerializePropertyException` carrying the `propertyName` and the bean's `Class`.

---

### Requirement 6: Aplicação do formatter a cada placeholder

**User Story:** Como desenvolvedor, quero que o formatter seja aplicado individualmente a cada placeholder que o referencia para que eu possa aplicar lógicas diferentes por nome de propriedade.

#### Acceptance Criteria

1. WHEN a template contains multiple placeholders with formatters, THE `TemplateEngine` SHALL invoke the respective `formatter.format(propertyName, resolvedValue)` once per placeholder, passing the exact dot-notation path as `propertyName` (ex: `"cliente.endereco.rua"`).
2. WHEN `formatter.format` returns `null` for a placeholder, THE `TemplateEngine` SHALL insert the String `"null"` in place of that placeholder.
3. WHEN `formatter.format` returns an empty String for a placeholder, THE `TemplateEngine` SHALL insert an empty String in place of that placeholder.
