# Requirements Document

## Introduction

O `TemplateEngine` atualmente navega em paths com dot-notation (ex: `${retorno.cbo.codigo}`) usando `java.beans.PropertyDescriptor` via reflection. Quando um nó intermediário do path é do tipo `Map`, a reflection falha com `IntrospectionException` pois `Map` não possui getters convencionais.

Esta feature adiciona suporte transparente à navegação em nós do tipo `Map<String, Object>` (ou qualquer `Map`) dentro de um path aninhado. Quando o valor atual durante a navegação for uma instância de `Map`, o `TemplateEngine` deve usar `map.get(property)` em vez de reflection para aquele segmento. A sintaxe do template permanece idêntica (`${path.aninhado}`), e a compatibilidade com Java Beans e com a navegação null-safe existente é preservada.

## Glossary

- **TemplateEngine**: Classe principal do projeto que processa templates substituindo expressões `${...}` pelos valores correspondentes no bean fornecido.
- **Map_Node**: Um nó intermediário ou folha em um path de navegação cujo valor em runtime é uma instância de `java.util.Map`.
- **Bean_Node**: Um nó intermediário ou folha em um path de navegação cujo valor em runtime é um Java Bean (POJO com getters via reflection).
- **Path**: Sequência de segmentos separados por `.` dentro de uma expressão `${...}`, ex: `retorno.cbo.codigo`.
- **Segment**: Um único identificador dentro de um Path, ex: `cbo` em `retorno.cbo.codigo`.
- **Navigator**: Lógica interna do `TemplateEngine` responsável por percorrer os segmentos de um Path e resolver o valor final.
- **Null_Safe_Navigation**: Comportamento existente onde, se um nó intermediário for `null`, o Navigator retorna `null` sem lançar exceção.

## Requirements

### Requirement 1: Navegação em Map_Node via map.get()

**User Story:** Como desenvolvedor, quero usar a dot-notation `${retorno.cbo.codigo}` em templates mesmo quando `retorno` ou `cbo` são instâncias de `Map`, para que eu não precise converter Maps em Java Beans apenas para usar o TemplateEngine.

#### Acceptance Criteria

1. WHEN o Navigator encontra um Segment cujo valor atual é uma instância de `Map`, THE Navigator SHALL resolver o próximo valor chamando `map.get(segment)` em vez de usar `PropertyDescriptor`.
2. WHEN o Navigator resolve um Segment via `map.get(segment)` e o resultado é um valor não-nulo, THE Navigator SHALL continuar a navegação nos segmentos restantes do Path usando o valor retornado.
3. WHEN o Navigator resolve um Segment via `map.get(segment)` e a chave não existe no Map (resultado é `null`), THE Navigator SHALL retornar `null` para o Path completo, respeitando o comportamento de Null_Safe_Navigation.
4. WHEN o Navigator encontra um Segment cujo valor atual é uma instância de `Map` e o Map contém uma chave cujo valor é outro `Map`, THE Navigator SHALL continuar a navegação no Map aninhado usando `map.get(segment)` para os segmentos subsequentes.
5. THE TemplateEngine SHALL suportar Paths onde segmentos Bean_Node e Map_Node se alternam (ex: `beanProp.mapProp.outraBeanProp`).

### Requirement 2: Compatibilidade com navegação em Bean_Node

**User Story:** Como desenvolvedor, quero que a navegação em Java Beans continue funcionando exatamente como antes, para que a adição do suporte a Maps não quebre nenhum comportamento existente.

#### Acceptance Criteria

1. WHEN o Navigator encontra um Segment cujo valor atual não é uma instância de `Map`, THE Navigator SHALL usar `PropertyDescriptor` e reflection para resolver o valor, mantendo o comportamento atual.
2. WHEN o Navigator falha ao criar um `PropertyDescriptor` para um Segment em um Bean_Node, THE Navigator SHALL lançar `GetPropertyException` com o nome do Segment e a classe do Bean_Node.
3. WHEN o Navigator encontra um Bean_Node com getter que retorna `null`, THE Navigator SHALL retornar `null` para o Path completo, respeitando o comportamento de Null_Safe_Navigation.

### Requirement 3: Compatibilidade com Null_Safe_Navigation

**User Story:** Como desenvolvedor, quero que a navegação null-safe existente continue funcionando para nós do tipo Map, para que um Map nulo em posição intermediária retorne `"null"` no template sem lançar exceção.

#### Acceptance Criteria

1. WHEN o Navigator encontra um Map_Node cujo valor é `null` durante a navegação de um Path, THE Navigator SHALL retornar `null` para o Path completo sem lançar exceção.
2. WHEN o Navigator encontra um Map_Node cujo valor é `null` e o TemplateEngine usa STRING_SERIALIZATION, THE TemplateEngine SHALL substituir a expressão pelo texto `"null"` no template resultante.
3. WHEN o Navigator encontra um Map_Node cujo valor é `null` e o TemplateEngine usa JSON_SERIALIZATION, THE TemplateEngine SHALL substituir a expressão pelo texto `"null"` no template resultante.

### Requirement 4: Serialização do valor folha resolvido via Map

**User Story:** Como desenvolvedor, quero que o valor final resolvido a partir de um Map seja serializado pelas regras existentes de STRING_SERIALIZATION e JSON_SERIALIZATION, para que o comportamento de saída seja consistente independentemente de o valor ter vindo de um Bean ou de um Map.

#### Acceptance Criteria

1. WHEN o valor folha de um Path é resolvido via `map.get()` e é uma `String`, THE TemplateEngine SHALL serializar o valor seguindo as regras de STRING_SERIALIZATION e JSON_SERIALIZATION existentes.
2. WHEN o valor folha de um Path é resolvido via `map.get()` e é um tipo numérico ou booleano, THE TemplateEngine SHALL serializar o valor seguindo as regras de STRING_SERIALIZATION e JSON_SERIALIZATION existentes.
3. WHEN o valor folha de um Path é resolvido via `map.get()` e é um objeto complexo (Map ou outro Object), THE TemplateEngine SHALL serializar o valor seguindo as regras de STRING_SERIALIZATION e JSON_SERIALIZATION existentes (lançando `SerializePropertyException` para STRING_SERIALIZATION quando aplicável).
4. FOR ALL valores folha resolvidos via `map.get()`, THE TemplateEngine SHALL produzir o mesmo resultado que produziria se o mesmo valor tivesse sido obtido via reflection de um Bean_Node equivalente.
