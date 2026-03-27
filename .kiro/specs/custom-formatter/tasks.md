# Implementation Plan: custom-formatter

## Overview

Adiciona suporte a formatadores customizados no `TemplateEngine`. A implementação segue a ordem: nova interface e exceção → campo de estado e `registerFormatter` → atualização da regex e lógica de despacho → testes unitários.

## Tasks

- [ ] 1. Criar a interface `CustomFormatter` e a exceção `FormatterNotFoundException`
  - Criar `CustomFormatter.java` no pacote `io.github.moraesdelima.templateengine` com a anotação `@FunctionalInterface` e o método `String format(String propertyName, Object resolvedValue) throws Exception`
  - Criar `FormatterNotFoundException.java` estendendo `TemplateEngineException`, com campo `@Getter String formatterName` e mensagem `"Formatter not registered: " + formatterName`
  - _Requirements: 1.1, 1.2, 3.3_

- [ ] 2. Adicionar estado interno e `registerFormatter` ao `TemplateEngine`
  - [ ] 2.1 Adicionar campo `private final Map<String, CustomFormatter> formatters = new HashMap<>()` em `TemplateEngine`
    - _Requirements: 2.5_
  - [ ] 2.2 Implementar método público `registerFormatter(String name, CustomFormatter formatter)` com validações de `null`/empty e Javadoc completo
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  - [ ] 2.3 Escrever testes unitários para `registerFormatter`
    - Registro simples, substituição de formatter existente, `name` null, `name` vazio, `formatter` null
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [ ] 3. Atualizar a regex e implementar o despacho de formatters em `process`
  - [ ] 3.1 Substituir a regex em `process(String, Object, int)` por `Pattern.compile("\\$\\{([^|{}]+)(?:\\|([^}]+))?\\}")`
    - Grupo 1: path; Grupo 2: formatterName (opcional)
    - _Requirements: 3.1_
  - [ ] 3.2 Implementar método privado `applyFormatter(CustomFormatter, String, Object, Class<?>)` que propaga `RuntimeException` diretamente e wrapa checked `Exception` em `SerializePropertyException`; retorna `"null"` quando o formatter retorna `null`
    - _Requirements: 5.1, 5.2, 6.2, 6.3_
  - [ ] 3.3 Atualizar o loop de substituição em `process` para: se grupo 2 presente → buscar formatter no Map (lançar `FormatterNotFoundException` se ausente) → chamar `applyFormatter`; se grupo 2 ausente → `serializeProperty` existente
    - _Requirements: 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 6.1_
  - [ ] 3.4 Escrever testes unitários para o despacho de formatters
    - Formatter simples aplicado a um placeholder
    - Template com múltiplos formatters diferentes
    - Placeholder sem formatter ao lado de placeholder com formatter no mesmo template
    - Formatter retornando `null` → `"null"` no resultado
    - Formatter retornando string vazia
    - Formatter não registrado → `FormatterNotFoundException`
    - `RuntimeException` propagada do formatter
    - Checked exception wrappada em `SerializePropertyException`
    - _Requirements: 3.2, 3.3, 3.4, 5.1, 5.2, 6.1, 6.2, 6.3_

- [ ] 4. Checkpoint final — garantir que todos os testes passam
  - Garantir que todos os testes passam, inclusive os existentes em `TemplateEngineTest`. Perguntar ao usuário se houver dúvidas.

## Notas

- Nenhuma dependência nova necessária — testes cobertos com JUnit 4 existente
- A regex atualizada é retrocompatível: placeholders sem `|formatterName` continuam funcionando
