package io.github.moraesdelima.templateengine;

import lombok.Data;

@Data
public class Cliente {
    private String nome;
    private int idade;
    private boolean ativo;
    private Endereco endereco;
}
