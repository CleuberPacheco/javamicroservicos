package br.com.fiap.esg.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LeituraConsumoRequestDTO(
        @NotNull(message = "O ID do equipamento IoT e obrigatorio")
        Long equipamentoId,

        @NotNull(message = "O consumo em kWh e obrigatorio")
        @PositiveOrZero(message = "O consumo em kWh nao pode ser negativo")
        BigDecimal consumoKwh,

        @NotNull(message = "O tempo ocioso em minutos e obrigatorio")
        @PositiveOrZero(message = "O tempo ocioso em minutos nao pode ser negativo")
        Integer tempoOciosoMinutos,

        @Size(max = 255, message = "A observacao deve ter no maximo 255 caracteres")
        String observacao
) {
}
