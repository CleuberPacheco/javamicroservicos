package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.EquipamentoIot;
import br.com.fiap.esg.model.StatusEquipamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EquipamentoIotResponseDTO(
        Long id,
        Long setorId,
        String nomeSetor,
        String codigoSensor,
        String nome,
        String tipoEquipamento,
        BigDecimal potenciaWatts,
        BigDecimal limiteConsumoHoraKwh,
        StatusEquipamento statusOperacional,
        Boolean desligamentoAutomatico,
        LocalDateTime ultimaAtualizacao
) {
    public static EquipamentoIotResponseDTO fromEntity(EquipamentoIot eq) {
        return new EquipamentoIotResponseDTO(
                eq.getId(),
                eq.getSetor().getId(),
                eq.getSetor().getNome(),
                eq.getCodigoSensor(),
                eq.getNome(),
                eq.getTipoEquipamento(),
                eq.getPotenciaWatts(),
                eq.getLimiteConsumoHoraKwh(),
                eq.getStatusOperacional(),
                eq.getDesligamentoAutomatico(),
                eq.getUltimaAtualizacao()
        );
    }
}
