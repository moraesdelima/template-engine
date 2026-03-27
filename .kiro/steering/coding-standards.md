---
inclusion: manual
---

# Padrões de Código e Convenções

## Estilo Geral

- Java 11, sem uso de features acima disso (records, sealed classes, etc.)
- Lombok é usado nos beans de teste (`@Data`) e nas exceções (`@Getter`)
- A classe principal `TemplateEngine` **não usa Lombok** — getters/setters não são necessários pois não há campos públicos de estado
- Javadoc obrigatório em métodos públicos e privados da `TemplateEngine`
- Encoding: UTF-8 em todos os arquivos

## Nomenclatura

- Classes: PascalCase (`TemplateEngine`, `GetPropertyException`)
- Métodos: camelCase (`process`, `serializeProperty`, `getPropertyValue`)
- Constantes: UPPER_SNAKE_CASE (`STRING_SERIALIZATION`, `JSON_SERIALIZATION`)
- Testes: snake_case descritivo com prefixo `test_` (ex: `test_replacing_a_simple_property_with_TemplateEngine_JSON_SERIALIZATION_serialization_Type`)

## Estrutura de Testes

- Framework: JUnit 4 (runner atual), com dependências JUnit 5 disponíveis
- Beans de teste ficam em `src/test/java` no mesmo pacote da classe testada
- Padrão de setup: `@Before` / `@After` para inicialização e limpeza
- Helpers privados para evitar repetição: `testReplaceProperties()` e `testReplacePropertiesThrowsAnException()`
- Beans de teste usam Lombok `@Data` (gera getters, setters, equals, hashCode, toString)

## Exceções

- Todas as exceções do projeto estendem `TemplateEngineException` (checked)
- Sempre incluir `property` e `beanClass` ao lançar exceções
- Mensagens de erro seguem o padrão: `"Can't [ação] property [nome] from class [canonicalName]"`
- Quando relevante, incluir o tipo de serialização na mensagem: `" witch TemplateEngine.[TIPO]_SERIALIZATION"`
  - Nota: "witch" é um typo existente no código (deveria ser "with") — manter para não quebrar compatibilidade de mensagens

## Adicionando Novas Funcionalidades

1. Novos tipos de serialização devem ser constantes `public static final int` em `TemplateEngine`
2. Novos tipos de exceção devem estender `TemplateEngineException`
3. Novos beans de teste devem usar `@Data` do Lombok e ficar em `src/test/java/.../templateengine/`
4. Todo novo método público deve ter Javadoc completo com `@param`, `@return` e `@throws`

## Build

```bash
# Compilar e testar
mvn package

# Apenas testes
mvn test

# Deploy snapshot
mvn deploy

# Release com assinatura GPG
mvn deploy -P ci-cd
```
