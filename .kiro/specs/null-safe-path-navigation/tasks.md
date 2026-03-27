# Plano de Implementação: null-safe-path-navigation

## Visão Geral

A mudança no código já foi aplicada (`getPropertyValue` retorna `null` em vez de lançar exceção para segmentos intermediários null). O trabalho restante é atualizar o teste de regressão existente e adicionar novos testes unitários.

## Tasks

- [ ] 1. Atualizar teste de regressão e adicionar testes unitários em TemplateEngineTest
  - [ ] 1.1 Atualizar test_replacing_a_property_with_a_null_parent_with_TemplateEngine_JSON_SERIALIZATION
    - Converter de testReplacePropertiesThrowsAnException para testReplaceProperties
    - Verificar que testBean.cliente = null + template "${cliente.nome}" retorna "null" com JSON_SERIALIZATION
    - _Requirements: 4.1, 4.3_
  - [ ] 1.2 Adicionar test_replacing_a_property_with_a_null_parent_with_TemplateEngine_STRING_SERIALIZATION
    - Verificar que testBean.cliente = null + template "${cliente.nome}" retorna "null" com STRING_SERIALIZATION
    - _Requirements: 4.2_
  - [ ] 1.3 Adicionar test_replacing_a_deep_path_with_null_intermediate_segment
    - Setar testBean.cliente.endereco = null, template "${cliente.endereco.rua}", verificar retorno "null" com ambos os tipos de serialização
    - _Requirements: 2.1_

- [ ] 2. Checkpoint final — garantir que todos os testes passam
  - Garantir que todos os testes passam, perguntar ao usuário se houver dúvidas.

## Notas

- Nenhuma dependência nova necessária — testes cobertos com JUnit 4 existente
