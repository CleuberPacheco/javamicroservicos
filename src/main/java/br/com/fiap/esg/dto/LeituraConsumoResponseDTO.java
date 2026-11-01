package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.LeituraConsumo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeituraConsumoResponseDTO(
        Long id,
        Long equipamentoId,
        String codigoSensor,
        String nomeEquipamento,
        Long setorId,
        String nomeSetor,
        BigDecimal consumoKwh,
        BigDecimal limiteHoraEquipamentoKwh,
        Integer tempoOciosoMinutos,
        Boolean ultrapassouLimite,
        Boolean desligadoAutomaticamente,
        String statusAtualEquipamento,
        LocalDateTime dataHoraLeitura,
        String observacao
) {
    public static LeituraConsumoResponseDTO fromEntity(LeituraConsumo l) {
        return new LeituraConsumoResponseDTO(
                l.getId(),
                l.getEquipamento().getId(),
                l.getEquipamento().getCodigoSensor(),
                l.getEquipamento().getNome(),
                l.getEquipamento().getSetor().getId(),
                l.getEquipamento().getSetor().getNome(),
                l.getConsumoKwh(),
                l.getEquipamento().getLimiteConsumoHoraKwh(),
                l.getTempoOciosoMinutos(),
                l.getUltrapassouLimite(),
                l.getDesligadoAutomaticamente(),
                l.getEquipamento().getStatusOperacional().name(),
                l.getDataHoraLeitura(),
                l.getObservacao()
        );
    }
}
