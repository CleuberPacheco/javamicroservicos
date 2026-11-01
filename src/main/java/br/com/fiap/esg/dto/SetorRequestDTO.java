package br.com.fiap.esg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SetorRequestDTO(
        @NotBlank(message = "O nome do setor e obrigatorio")
        @Size(min = 3, max = 100, message = "O nome do setor deve ter entre 3 e 100 caracteres")
        String nome,

        @NotBlank(message = "A localizacao do setor e obrigatoria")
        @Size(max = 150, message = "A localizacao deve ter no maximo 150 caracteres")
        String localizacao,

        @NotBlank(message = "O nome do responsavel e obrigatorio")
        @Size(max = 100, message = "O nome do responsavel deve ter no maximo 100 caracteres")
        String responsavel,

        @NotNull(message = "O limite mensal de consumo (kWh) e obrigatorio")
        @Positive(message = "O limite mensal (kWh) deve ser maior que zero")
        BigDecimal limiteMensalKwh,

        @NotNull(message = "O limite diario de consumo (kWh) e obrigatorio")
        @Positive(message = "O limite diario (kWh) deve ser maior que zero")
        BigDecimal limiteDiarioKwh,

        Boolean ativo
) {
}
