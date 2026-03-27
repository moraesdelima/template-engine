---
inclusion: manual
---

# Visão Geral do Projeto: template-engine

## Identidade do Projeto

- **Artefato**: `io.github.moraesdelima:template-engine:1.2.0-SNAPSHOT`
- **Tipo**: Biblioteca Java (JAR) publicada no Maven Central via Sonatype OSSRH
- **Java**: 11+
- **Build**: Apache Maven 3.x
- **Licença**: MIT

## Propósito

Biblioteca de substituição de placeholders em templates de texto usando valores extraídos de Java Beans via reflection. Suporta dois modos de serialização: string pura e JSON (via Gson).

## Estrutura de Pacotes

```
io.github.moraesdelima.templateengine
├── TemplateEngine.java              # Classe principal (ponto de entrada da API)
├── TemplateEngineException.java     # Exceção base (checked)
├── GetPropertyException.java        # Falha ao ler propriedade via reflection
└── SerializePropertyException.java  # Falha ao serializar valor da propriedade
```

## Dependências Principais

| Dependência | Versão | Escopo | Uso |
|---|---|---|---|
| lombok | 1.18.26 | provided | `@Data`, `@Getter` nos beans e exceções |
| gson | 2.10.1 | compile | Serialização JSON e normalização de tipos |
| junit | 4.13.2 | test | Runner de testes (JUnit 4) |
| junit-jupiter-api | 5.7.2 | test | API JUnit 5 (Jupiter) |
| mockito-core | 4.2.0 | test | Mocks nos testes |
| mockito-junit-jupiter | 4.2.0 | test | Integração Mockito + JUnit 5 |

> Atenção: o projeto mistura JUnit 4 (`@Test` de `org.junit`) com dependências JUnit 5. Os testes atuais usam JUnit 4.

## Pipeline de CI/CD

- Profile `ci-cd` no Maven ativa: geração de sources JAR, javadoc JAR e assinatura GPG
- Deploy via `nexus-staging-maven-plugin` para Sonatype OSSRH
- Repositório snapshot: `https://s01.oss.sonatype.org/content/repositories/snapshots`
