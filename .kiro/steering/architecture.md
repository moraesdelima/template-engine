---
inclusion: manual
---

# Arquitetura e Design do template-engine

## Fluxo de Processamento

```
template (String) + bean (Object) + serializationType (int)
        │
        ▼
  TemplateEngine.process()
        │
        ├─ Regex: Pattern.compile("\\$\\{([^}]+)\\}")
        │         Captura tudo entre ${ e }
        │
        ├─ Para cada match encontrado:
        │     │
        │     ├─ getPropertyValue(bean, property)
        │     │     │
        │     │     ├─ Suporta propriedades aninhadas via "." (ex: cliente.endereco.rua)
        │     │     ├─ Usa java.beans.PropertyDescriptor para localizar o getter
        │     │     ├─ Invoca o getter via reflection (Method.invoke)
        │     │     └─ Itera pelos segmentos do path, atualizando bean e beanClass
        │     │
        │     └─ serializeProperty(bean, property, serializationType)
        │           │
        │           ├─ Gson.toJson(propertyValue) → sempre serializa primeiro
        │           │
        │           ├─ JSON_SERIALIZATION (1): retorna o JSON bruto
        │           │
        │           └─ STRING_SERIALIZATION (0):
        │                 ├─ null → retorna "null"
        │                 ├─ String/Date (começa com ") → remove as aspas externas
        │                 ├─ Number/Boolean (não começa com { ou [) → retorna como está
        │                 └─ Object/Array (começa com { ou [) → lança SerializePropertyException
        │
        └─ StringBuffer com appendReplacement/appendTail → resultado final
```

## Decisões de Design

### 1. Serialização via Gson como intermediário universal
Todos os valores passam pelo `Gson.toJson()` antes de qualquer decisão de serialização. Isso garante consistência na representação de tipos primitivos, datas e objetos complexos. A distinção entre STRING e JSON é feita **após** a serialização Gson, inspecionando o primeiro caractere do JSON resultante.

### 2. Propriedades aninhadas com dot-notation
O método `getPropertyValue` divide o path por `.` e navega recursivamente pelo grafo de objetos. A cada nível, atualiza `beanClass` com o tipo de retorno do getter, permitindo navegação type-safe sem casting.

### 3. Hierarquia de exceções checked
```
Exception
└── TemplateEngineException (checked)
    ├── GetPropertyException       → falha na leitura (reflection)
    └── SerializePropertyException → falha na serialização (tipo incompatível)
```
Ambas carregam `property` (String) e `beanClass` (Class<?>) para diagnóstico preciso.

### 4. Constantes de serialização como int
`STRING_SERIALIZATION = 0` e `JSON_SERIALIZATION = 1` são constantes `public static final int`. Não é um enum — decisão de simplicidade da API pública.

## Comportamentos Críticos

| Cenário | STRING_SERIALIZATION | JSON_SERIALIZATION |
|---|---|---|
| String `"João"` | `João` | `"João"` |
| int `32` | `32` | `32` |
| boolean `true` | `true` | `true` |
| null | `null` | `null` |
| Object `{...}` | lança `SerializePropertyException` | `{"campo":"valor"}` |
| List/Array | lança `SerializePropertyException` | `["v1","v2"]` |
| Propriedade inexistente | lança `GetPropertyException` | lança `GetPropertyException` |
| Parent null em path aninhado | lança `GetPropertyException` | lança `GetPropertyException` |

## Limitações Conhecidas

- `Gson` é instanciado como campo de instância (`Gson gson = new Gson()`), sem configuração customizada. Não há suporte a adaptadores de tipo, formatação de datas customizada, etc.
- Não há suporte a expressões além de dot-notation (sem índices de array, sem condicionais).
- `TemplateEngine` não é thread-safe se `Gson` for substituído por uma instância com estado.
- O campo `gson` tem visibilidade package-private (sem modificador), o que pode ser intencional para testes mas é inconsistente com o encapsulamento da classe.
