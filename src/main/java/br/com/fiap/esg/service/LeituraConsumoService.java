package br.com.fiap.esg.service;

import br.com.fiap.esg.dto.LeituraConsumoRequestDTO;
import br.com.fiap.esg.dto.LeituraConsumoResponseDTO;
import br.com.fiap.esg.model.*;
import br.com.fiap.esg.repository.AlertaEficienciaRepository;
import br.com.fiap.esg.repository.EquipamentoIotRepository;
import br.com.fiap.esg.repository.LeituraConsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeituraConsumoService {

    private static final int LIMITE_OCIOSIDADE_MINUTOS = 30;

    private final LeituraConsumoRepository leituraRepository;
    private final EquipamentoIotRepository equipamentoRepository;
    private final AlertaEficienciaRepository alertaRepository;
    private final EquipamentoIotService equipamentoService;
    private final SetorService setorService;

    @Transactional
    public LeituraConsumoResponseDTO registrarLeitura(LeituraConsumoRequestDTO dto) {
        EquipamentoIot equipamento = equipamentoService.buscarEntidadePorId(dto.equipamentoId());

        boolean ultrapassouLimite = dto.consumoKwh().compareTo(equipamento.getLimiteConsumoHoraKwh()) > 0;
        boolean deveDesligarPorOciosidade = dto.tempoOciosoMinutos() >= LIMITE_OCIOSIDADE_MINUTOS
                && Boolean.TRUE.equals(equipamento.getDesligamentoAutomatico())
                && equipamento.getStatusOperacional() == StatusEquipamento.LIGADO;

        if (deveDesligarPorOciosidade) {
            equipamento.setStatusOperacional(StatusEquipamento.DESLIGADO);
            equipamentoRepository.save(equipamento);
        }

        LeituraConsumo leitura = LeituraConsumo.builder()
                .equipamento(equipamento)
                .consumoKwh(dto.consumoKwh())
                .tempoOciosoMinutos(dto.tempoOciosoMinutos())
                .ultrapassouLimite(ultrapassouLimite)
                .desligadoAutomaticamente(deveDesligarPorOciosidade)
                .observacao(dto.observacao())
                .build();

        LeituraConsumo leituraSalva = leituraRepository.save(leitura);

        // Regra 1: Alerta automatico quando o consumo ultrapassa o limite configurado
        if (ultrapassouLimite) {
            SeveridadeAlerta severidade = dto.consumoKwh()
                    .compareTo(equipamento.getLimiteConsumoHoraKwh().multiply(java.math.BigDecimal.valueOf(1.5))) > 0
                    ? SeveridadeAlerta.CRITICA
                    : SeveridadeAlerta.ALTA;

            AlertaEficiencia alertaExcesso = AlertaEficiencia.builder()
                    .setor(equipamento.getSetor())
                    .equipamento(equipamento)
                    .leitura(leituraSalva)
                    .tipoAlerta(TipoAlerta.LIMITE_ULTRAPASSADO)
                    .severidade(severidade)
                    .mensagem(String.format(
                            "Consumo registrado (%.3f kWh) ultrapassou o limite horario (%.2f kWh) do equipamento '%s' (Sensor: %s).",
                            dto.consumoKwh(),
                            equipamento.getLimiteConsumoHoraKwh(),
                            equipamento.getNome(),
                            equipamento.getCodigoSensor()))
                    .acaoAutomaticaExecutada(deveDesligarPorOciosidade
                            ? "Equipamento desligado automaticamente e alerta critico enviado ao gestor"
                            : "Alerta automatico enviado ao responsavel do setor: " + equipamento.getSetor().getResponsavel())
                    .statusAlerta(StatusAlerta.ABERTO)
                    .build();
            alertaRepository.save(alertaExcesso);
        }

        // Regra 2: Integracao com sensor IoT para desligamento automatico de equipamento ocioso
        if (deveDesligarPorOciosidade) {
            AlertaEficiencia alertaOciosidade = AlertaEficiencia.builder()
                    .setor(equipamento.getSetor())
                    .equipamento(equipamento)
                    .leitura(leituraSalva)
                    .tipoAlerta(TipoAlerta.DESLIGAMENTO_AUTOMATICO_OCIOSIDADE)
                    .severidade(SeveridadeAlerta.MEDIA)
                    .mensagem(String.format(
                            "Equipamento '%s' permaneceu ocioso por %d minutos (limite: %d min). Desligamento automatico acionado pelo sensor IoT %s.",
                            equipamento.getNome(),
                            dto.tempoOciosoMinutos(),
                            LIMITE_OCIOSIDADE_MINUTOS,
                            equipamento.getCodigoSensor()))
                    .acaoAutomaticaExecutada("Status operacional alterado automaticamente para DESLIGADO pelo sensor " + equipamento.getCodigoSensor())
                    .statusAlerta(StatusAlerta.RESOLVIDO)
                    .dataResolucao(LocalDateTime.now())
                    .build();
            alertaRepository.save(alertaOciosidade);
        }

        return LeituraConsumoResponseDTO.fromEntity(leituraSalva);
    }

    @Transactional(readOnly = true)
    public Page<LeituraConsumoResponseDTO> listarTodas(Pageable pageable) {
        return leituraRepository.findAll(pageable).map(LeituraConsumoResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<LeituraConsumoResponseDTO> listarPorSetor(Long setorId) {
        setorService.buscarEntidadePorId(setorId);
        return leituraRepository.findByEquipamentoSetorIdOrderByDataHoraLeituraDesc(setorId)
                .stream()
                .map(LeituraConsumoResponseDTO::fromEntity)
                .toList();
    }
}
