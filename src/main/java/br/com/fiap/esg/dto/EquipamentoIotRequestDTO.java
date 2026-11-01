package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.StatusEquipamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record EquipamentoIotRequestDTO(
        @NotNull(message = "O ID do setor e obrigatorio")
        Long setorId,

        @NotBlank(message = "O codigo do sensor IoT e obrigatorio")
        @Size(min = 3, max = 50, message = "O codigo do sensor deve ter entre 3 e 50 caracteres")
        String codigoSensor,

        @NotBlank(message = "O nome do equipamento e obrigatorio")
        @Size(min = 3, max = 120, message = "O nome do equipamento deve ter entre 3 e 120 caracteres")
        String nome,

        @NotBlank(message = "O tipo do equipamento e obrigatorio")
        @Size(max = 60, message = "O tipo do equipamento deve ter no maximo 60 caracteres")
        String tipoEquipamento,

        @NotNull(message = "A potencia nominal (Watts) e obrigatoria")
        @Positive(message = "A potencia (Watts) deve ser maior que zero")
        BigDecimal potenciaWatts,

        @NotNull(message = "O limite de consumo por hora (kWh) e obrigatorio")
        @Positive(message = "O limite de consumo por hora (kWh) deve ser maior que zero")
        BigDecimal limiteConsumoHoraKwh,

        StatusEquipamento statusOperacional,

        Boolean desligamentoAutomatico
) {
}
