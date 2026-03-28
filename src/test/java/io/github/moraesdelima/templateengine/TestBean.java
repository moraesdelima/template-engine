package io.github.moraesdelima.templateengine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class TestBean {
    private String registro;
    private Cliente cliente;
    private List<String> lista;
    private MapBean mapBean;
    private LocalDate dataLocal;
    private LocalDateTime dataHoraLocal;

    // getter-only virtual property (no backing field, no setter)
    public String getRegistroFormatado() {
        return registro != null ? "REG-" + registro : null;
    }
}
