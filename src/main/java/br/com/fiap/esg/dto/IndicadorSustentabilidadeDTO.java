package br.com.fiap.esg.dto;

import java.math.BigDecimal;
import java.util.List;

public record IndicadorSustentabilidadeDTO(
        BigDecimal consumoTotalRegistradoKwh,
        BigDecimal emissaoEstimadaCo2Kg,
        long totalSetoresAtivos,
        long totalEquipamentosIot,
        long equipamentosLigados,
        long equipamentosDesligados,
        long totalDesligamentosAutomaticosIoT,
        BigDecimal economiaEstimadaDesligamentoAutoKwh,
        long alertasAbertos,
        long alertasResolvidos,
        List<ResumoSetorIndicadorDTO> resumoPorSetor
) {
    public record ResumoSetorIndicadorDTO(
            Long setorId,
            String nomeSetor,
            BigDecimal consumoAcumuladoKwh,
            BigDecimal limiteMensalKwh,
            BigDecimal percentualUtilizadoMeta,
            String statusConformidadeEsg
    ) {
    }
}
