# Requirements Document

## Introduction

Atualmente, quando o `TemplateEngine` navega por um path com dot-notation (ex: `${cliente.endereco.rua}`) e encontra um segmento intermediário com valor `null` (ex: `cliente` é `null`), o método `getPropertyValue` lança `GetPropertyException`. A feature **null-safe-path-navigation** altera esse comportamento: em vez de lançar exceção, o engine deve retornar a string `"null"` no template, tornando a navegação null-safe. O teste existente `test_replacing_a_property_with_a_null_parent_with_TemplateEngine_JSON_SERIALIZATION` precisa ser atualizado para refletir o novo comportamento.

## Glossary

- **TemplateEngine**: Classe principal da biblioteca responsável por processar templates e substituir placeholders.
- **Placeholder**: Expressão no formato `${path}` dentro de um template que será substituída pelo valor correspondente.
- **Path**: Sequência de nomes de propriedades separados por `.` que identifica um valor dentro de um bean (ex: `cliente.endereco.rua`).
- **Segmento intermediário**: Qualquer propriedade no path que não seja a última (ex: em `cliente.endereco.rua`, `cliente` e `endereco` são segmentos intermediários).
- **Bean**: Objeto Java cujas propriedades são acessadas via reflection para substituição no template.
- **GetPropertyException**: Exceção lançada quando uma propriedade não existe no bean ou não possui getter acessível.
- **Null-safe navigation**: Comportamento em que a presença de `null` em um segmento intermediário do path não causa exceção, retornando `"null"` como valor final.

## Requirements

### Requirement 1: Navegação null-safe em paths aninhados

**User Story:** Como desenvolvedor que usa o TemplateEngine, eu quero que placeholders com paths aninhados cujo segmento intermediário seja `null` retornem `"null"` no template, para que o processamento não seja interrompido por exceção.

#### Acceptance Criteria

1. WHEN o `TemplateEngine` processa um placeholder cujo segmento intermediário do path possui valor `null`, THE `TemplateEngine` SHALL substituir o placeholder pela string `"null"` no resultado final.
2. WHEN o `TemplateEngine` processa um placeholder cujo segmento intermediário do path possui valor `null` com `JSON_SERIALIZATION`, THE `TemplateEngine` SHALL substituir o placeholder pela string `"null"` no resultado final.
3. WHEN o `TemplateEngine` processa um placeholder cujo segmento intermediário do path possui valor `null` com `STRING_SERIALIZATION`, THE `TemplateEngine` SHALL substituir o placeholder pela string `"null"` no resultado final.
4. WHEN o `TemplateEngine` processa um template com múltiplos placeholders e apenas um deles possui segmento intermediário `null`, THE `TemplateEngine` SHALL substituir apenas o placeholder afetado por `"null"` e processar os demais normalmente.
5. IF uma propriedade referenciada no placeholder não existe no bean, THEN THE `TemplateEngine` SHALL lançar `GetPropertyException` com o nome da propriedade e a classe do bean.

---

### Requirement 2: Profundidade arbitrária de navegação null-safe

**User Story:** Como desenvolvedor que usa o TemplateEngine, eu quero que a navegação null-safe funcione em qualquer nível de profundidade do path, para que paths com 3 ou mais segmentos também sejam tratados corretamente.

#### Acceptance Criteria

1. WHEN o `TemplateEngine` processa um placeholder com path de profundidade 3 ou mais (ex: `a.b.c.d`) e qualquer segmento intermediário é `null`, THE `TemplateEngine` SHALL retornar `"null"` sem lançar exceção.
2. WHEN o `TemplateEngine` processa um placeholder com path de profundidade 3 ou mais e nenhum segmento é `null`, THE `TemplateEngine` SHALL retornar o valor da propriedade final normalmente.

---

### Requirement 3: Preservação do comportamento existente para propriedades inexistentes

**User Story:** Como desenvolvedor que usa o TemplateEngine, eu quero que a mudança de comportamento para `null` não afete o tratamento de propriedades inexistentes, para que erros de digitação em placeholders continuem sendo sinalizados.

#### Acceptance Criteria

1. WHEN o `TemplateEngine` processa um placeholder cujo nome de propriedade não existe no bean, THE `TemplateEngine` SHALL lançar `GetPropertyException` contendo o nome da propriedade e a classe do bean onde a falha ocorreu.
2. THE `GetPropertyException` SHALL conter o nome exato da propriedade que não foi encontrada.
3. THE `GetPropertyException` SHALL conter a classe exata do bean onde a propriedade não foi encontrada.

---

### Requirement 4: Atualização do teste de regressão existente

**User Story:** Como desenvolvedor que mantém o projeto, eu quero que o teste `test_replacing_a_property_with_a_null_parent_with_TemplateEngine_JSON_SERIALIZATION` seja atualizado para validar o novo comportamento null-safe, para que a suíte de testes reflita o comportamento atual do sistema.

#### Acceptance Criteria

1. THE `TemplateEngineTest` SHALL conter um teste que verifica que, quando `testBean.cliente` é `null`, o processamento do template `"${cliente.nome}"` retorna `"null"` com `JSON_SERIALIZATION`.
2. THE `TemplateEngineTest` SHALL conter um teste que verifica que, quando `testBean.cliente` é `null`, o processamento do template `"${cliente.nome}"` retorna `"null"` com `STRING_SERIALIZATION`.
3. WHEN o teste de regressão anterior esperava `GetPropertyException` para parent `null`, THE `TemplateEngineTest` SHALL substituir essa expectativa pela verificação do retorno `"null"`.
