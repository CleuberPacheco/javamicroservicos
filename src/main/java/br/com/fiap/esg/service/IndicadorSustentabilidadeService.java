package br.com.fiap.esg.service;

import br.com.fiap.esg.dto.IndicadorSustentabilidadeDTO;
import br.com.fiap.esg.dto.IndicadorSustentabilidadeDTO.ResumoSetorIndicadorDTO;
import br.com.fiap.esg.model.Setor;
import br.com.fiap.esg.model.StatusAlerta;
import br.com.fiap.esg.model.StatusEquipamento;
import br.com.fiap.esg.repository.AlertaEficienciaRepository;
import br.com.fiap.esg.repository.EquipamentoIotRepository;
import br.com.fiap.esg.repository.LeituraConsumoRepository;
import br.com.fiap.esg.repository.SetorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndicadorSustentabilidadeService {

    // Fator medio de emissao do Sistema Interligado Nacional (SIN) em kgCO2 por kWh
    private static final BigDecimal FATOR_EMISSAO_CO2_KG_POR_KWH = new BigDecimal("0.0385");
    private static final BigDecimal ECONOMIA_MEDIA_POR_DESLIGAMENTO_KWH = new BigDecimal("4.50");

    private final SetorRepository setorRepository;
    private final EquipamentoIotRepository equipamentoRepository;
    private final LeituraConsumoRepository leituraRepository;
    private final AlertaEficienciaRepository alertaRepository;

    @Transactional(readOnly = true)
    public IndicadorSustentabilidadeDTO obterIndicadoresConsolidados() {
        BigDecimal consumoTotal = leituraRepository.somarConsumoTotalKwh();
        BigDecimal emissaoCo2Kg = consumoTotal.multiply(FATOR_EMISSAO_CO2_KG_POR_KWH)
                .setScale(3, RoundingMode.HALF_UP);

        List<Setor> setoresAtivos = setorRepository.findByAtivoTrue();
        long totalEquipamentos = equipamentoRepository.count();
        long ligados = equipamentoRepository.countByStatusOperacional(StatusEquipamento.LIGADO);
        long desligados = equipamentoRepository.countByStatusOperacional(StatusEquipamento.DESLIGADO);

        long totalDesligamentosAuto = leituraRepository.countByDesligadoAutomaticamenteTrue();
        BigDecimal economiaEstimadaKwh = ECONOMIA_MEDIA_POR_DESLIGAMENTO_KWH
                .multiply(BigDecimal.valueOf(totalDesligamentosAuto))
                .setScale(2, RoundingMode.HALF_UP);

        long alertasAbertos = alertaRepository.countByStatusAlerta(StatusAlerta.ABERTO);
        long alertasResolvidos = alertaRepository.countByStatusAlerta(StatusAlerta.RESOLVIDO);

        List<ResumoSetorIndicadorDTO> resumoSetores = setoresAtivos.stream().map(setor -> {
            BigDecimal consumoSetor = leituraRepository.somarConsumoPorSetorKwh(setor.getId());
            BigDecimal percentual = BigDecimal.ZERO;
            if (setor.getLimiteMensalKwh().compareTo(BigDecimal.ZERO) > 0) {
                percentual = consumoSetor.multiply(BigDecimal.valueOf(100))
                        .divide(setor.getLimiteMensalKwh(), 2, RoundingMode.HALF_UP);
            }

            String statusConformidade;
            if (percentual.compareTo(BigDecimal.valueOf(100)) > 0) {
                statusConformidade = "ACIMA_DA_META_CRITICO";
            } else if (percentual.compareTo(BigDecimal.valueOf(80)) >= 0) {
                statusConformidade = "ATENCAO_PROXIMO_AO_LIMITE";
            } else {
                statusConformidade = "CONFORME_EFICIENTE";
            }

            return new ResumoSetorIndicadorDTO(
                    setor.getId(),
                    setor.getNome(),
                    consumoSetor,
                    setor.getLimiteMensalKwh(),
                    percentual,
                    statusConformidade
            );
        }).toList();

        return new IndicadorSustentabilidadeDTO(
                consumoTotal,
                emissaoCo2Kg,
                setoresAtivos.size(),
                totalEquipamentos,
                ligados,
                desligados,
                totalDesligamentosAuto,
                economiaEstimadaKwh,
                alertasAbertos,
                alertasResolvidos,
                resumoSetores
        );
    }
}
