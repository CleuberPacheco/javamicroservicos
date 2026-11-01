package br.com.fiap.esg.service;

import br.com.fiap.esg.dto.EquipamentoIotRequestDTO;
import br.com.fiap.esg.dto.EquipamentoIotResponseDTO;
import br.com.fiap.esg.exception.BusinessRuleException;
import br.com.fiap.esg.exception.ResourceNotFoundException;
import br.com.fiap.esg.model.*;
import br.com.fiap.esg.repository.AlertaEficienciaRepository;
import br.com.fiap.esg.repository.EquipamentoIotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EquipamentoIotService {

    private final EquipamentoIotRepository equipamentoRepository;
    private final AlertaEficienciaRepository alertaRepository;
    private final SetorService setorService;

    @Transactional
    public EquipamentoIotResponseDTO cadastrar(EquipamentoIotRequestDTO dto) {
        if (equipamentoRepository.existsByCodigoSensor(dto.codigoSensor())) {
            throw new BusinessRuleException("Ja existe um equipamento cadastrado com o codigo de sensor IoT: " + dto.codigoSensor());
        }

        Setor setor = setorService.buscarEntidadePorId(dto.setorId());

        EquipamentoIot equipamento = EquipamentoIot.builder()
                .setor(setor)
                .codigoSensor(dto.codigoSensor())
                .nome(dto.nome())
                .tipoEquipamento(dto.tipoEquipamento())
                .potenciaWatts(dto.potenciaWatts())
                .limiteConsumoHoraKwh(dto.limiteConsumoHoraKwh())
                .statusOperacional(dto.statusOperacional() != null ? dto.statusOperacional() : StatusEquipamento.LIGADO)
                .desligamentoAutomatico(dto.desligamentoAutomatico() != null ? dto.desligamentoAutomatico() : true)
                .build();

        return EquipamentoIotResponseDTO.fromEntity(equipamentoRepository.save(equipamento));
    }

    @Transactional(readOnly = true)
    public Page<EquipamentoIotResponseDTO> listarTodos(Pageable pageable) {
        return equipamentoRepository.findAll(pageable).map(EquipamentoIotResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public EquipamentoIotResponseDTO buscarPorId(Long id) {
        return EquipamentoIotResponseDTO.fromEntity(buscarEntidadePorId(id));
    }

    @Transactional
    public EquipamentoIotResponseDTO atualizar(Long id, EquipamentoIotRequestDTO dto) {
        EquipamentoIot equipamento = buscarEntidadePorId(id);

        if (!equipamento.getCodigoSensor().equalsIgnoreCase(dto.codigoSensor())
                && equipamentoRepository.existsByCodigoSensor(dto.codigoSensor())) {
            throw new BusinessRuleException("Ja existe outro equipamento com o codigo de sensor IoT: " + dto.codigoSensor());
        }

        Setor setor = setorService.buscarEntidadePorId(dto.setorId());

        equipamento.setSetor(setor);
        equipamento.setCodigoSensor(dto.codigoSensor());
        equipamento.setNome(dto.nome());
        equipamento.setTipoEquipamento(dto.tipoEquipamento());
        equipamento.setPotenciaWatts(dto.potenciaWatts());
        equipamento.setLimiteConsumoHoraKwh(dto.limiteConsumoHoraKwh());
        if (dto.statusOperacional() != null) {
            equipamento.setStatusOperacional(dto.statusOperacional());
        }
        if (dto.desligamentoAutomatico() != null) {
            equipamento.setDesligamentoAutomatico(dto.desligamentoAutomatico());
        }

        return EquipamentoIotResponseDTO.fromEntity(equipamentoRepository.save(equipamento));
    }

    @Transactional
    public EquipamentoIotResponseDTO desligarEquipamentoOcioso(Long id) {
        EquipamentoIot equipamento = buscarEntidadePorId(id);

        if (equipamento.getStatusOperacional() == StatusEquipamento.DESLIGADO) {
            throw new BusinessRuleException("O equipamento '" + equipamento.getNome() + "' ja se encontra DESLIGADO.");
        }

        equipamento.setStatusOperacional(StatusEquipamento.DESLIGADO);
        EquipamentoIot salvo = equipamentoRepository.save(equipamento);

        AlertaEficiencia alerta = AlertaEficiencia.builder()
                .setor(salvo.getSetor())
                .equipamento(salvo)
                .tipoAlerta(TipoAlerta.DESLIGAMENTO_COMANDO_REMOTO)
                .severidade(SeveridadeAlerta.BAIXA)
                .mensagem("Desligamento remoto acionado via integracao IoT para o equipamento " + salvo.getNome()
                        + " (Sensor: " + salvo.getCodigoSensor() + ") por ociosidade.")
                .acaoAutomaticaExecutada("Atuador IoT alterou status para DESLIGADO")
                .statusAlerta(StatusAlerta.RESOLVIDO)
                .dataResolucao(LocalDateTime.now())
                .build();
        alertaRepository.save(alerta);

        return EquipamentoIotResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public void excluir(Long id) {
        EquipamentoIot equipamento = buscarEntidadePorId(id);
        equipamentoRepository.delete(equipamento);
    }

    public EquipamentoIot buscarEntidadePorId(Long id) {
        return equipamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipamento IoT nao encontrado com ID: " + id));
    }
}
