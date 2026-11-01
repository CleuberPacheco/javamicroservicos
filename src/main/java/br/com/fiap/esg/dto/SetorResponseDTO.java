package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.Setor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SetorResponseDTO(
        Long id,
        String nome,
        String localizacao,
        String responsavel,
        BigDecimal limiteMensalKwh,
        BigDecimal limiteDiarioKwh,
        Boolean ativo,
        LocalDateTime dataCadastro
) {
    public static SetorResponseDTO fromEntity(Setor setor) {
        return new SetorResponseDTO(
                setor.getId(),
                setor.getNome(),
                setor.getLocalizacao(),
                setor.getResponsavel(),
                setor.getLimiteMensalKwh(),
                setor.getLimiteDiarioKwh(),
                setor.getAtivo(),
                setor.getDataCadastro()
        );
    }
}
