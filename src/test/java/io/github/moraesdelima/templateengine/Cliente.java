package io.github.moraesdelima.templateengine;

import lombok.Data;

@Data
public class Cliente {
    private String nome;
    private int idade;
    private Endereco endereco;
}
