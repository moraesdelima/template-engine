package io.github.moraesdelima.templateengine;

import java.util.List;

import lombok.Data;

@Data
public class TestBean {
    private String registro;
    private Cliente cliente;
    private List<String> lista;
}
