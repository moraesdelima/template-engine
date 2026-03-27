# Implementation Plan: map-property-navigation

## Overview

Adiciona suporte a Map na navegação por dot-notation. A mudança é cirúrgica — um único branch no loop de `getPropertyValue` em `TemplateEngine`.

## Tasks

- [x] 1. Adicionar suporte a Map em getPropertyValue
  - Em `TemplateEngine.getPropertyValue`, adicionar branch `instanceof Map` no início do loop, antes do `PropertyDescriptor`:
    ```java
    if (value instanceof Map) {
        value = ((Map<?, ?>) value).get(property);
        beanClass = value != null ? value.getClass() : Object.class;
        continue;
    }
    ```
  - Adicionar import `java.util.Map` se necessário
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 2. Adicionar bean de teste MapBean e testes unitários
  - Criar `src/test/java/io/github/moraesdelima/templateengine/MapBean.java` com campo `Map<String, Object> dados` usando Lombok `@Data`
  - Adicionar testes em `TemplateEngineTest`:
    - Map simples: `${dados.nome}` onde `dados = {"nome": "João"}` → `"João"` com STRING_SERIALIZATION
    - Map aninhado: `${dados.cbo.codigo}` onde `dados = {"cbo": {"codigo": "1234"}}` → `"1234"`
    - Bean → Map → String: bean com campo Map, navegar até String dentro do Map
    - Map → Bean → String: Map com valor Bean, navegar até propriedade do Bean
    - Chave inexistente no Map: `${dados.inexistente}` → `"null"`
    - Map nulo em posição intermediária: `${dados.cbo.codigo}` com `dados = null` → `"null"`
    - Serialização JSON de valor String vindo de Map: `${dados.nome}` com JSON_SERIALIZATION → `"\"João\""`
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 3.1, 3.2, 3.3, 4.1, 4.2_

- [x] 3. Checkpoint final — garantir que todos os testes passam
  - Rodar `mvn test` e garantir que todos os testes passam, incluindo os existentes
  - Perguntar ao usuário se houver dúvidas

## Notas

- Nenhuma dependência nova necessária
- A mudança não afeta `serializeProperty` — o valor folha é serializado normalmente
- Compatível com null-safe navigation já implementada
