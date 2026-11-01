package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.AlertaEficiencia;
import br.com.fiap.esg.model.SeveridadeAlerta;
import br.com.fiap.esg.model.StatusAlerta;
import br.com.fiap.esg.model.TipoAlerta;

import java.time.LocalDateTime;

public record AlertaEficienciaResponseDTO(
        Long id,
        Long setorId,
        String nomeSetor,
        Long equipamentoId,
        String codigoSensor,
        Long leituraId,
        TipoAlerta tipoAlerta,
        SeveridadeAlerta severidade,
        String mensagem,
        String acaoAutomaticaExecutada,
        StatusAlerta statusAlerta,
        LocalDateTime dataGeracao,
        LocalDateTime dataResolucao
) {
    public static AlertaEficienciaResponseDTO fromEntity(AlertaEficiencia a) {
        return new AlertaEficienciaResponseDTO(
                a.getId(),
                a.getSetor().getId(),
                a.getSetor().getNome(),
                a.getEquipamento() != null ? a.getEquipamento().getId() : null,
                a.getEquipamento() != null ? a.getEquipamento().getCodigoSensor() : null,
                a.getLeitura() != null ? a.getLeitura().getId() : null,
                a.getTipoAlerta(),
                a.getSeveridade(),
                a.getMensagem(),
                a.getAcaoAutomaticaExecutada(),
                a.getStatusAlerta(),
                a.getDataGeracao(),
                a.getDataResolucao()
        );
    }
}
