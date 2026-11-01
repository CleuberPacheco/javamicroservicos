package br.com.fiap.esg.service;

import br.com.fiap.esg.dto.AlertaEficienciaResponseDTO;
import br.com.fiap.esg.dto.ResolucaoAlertaRequestDTO;
import br.com.fiap.esg.exception.BusinessRuleException;
import br.com.fiap.esg.exception.ResourceNotFoundException;
import br.com.fiap.esg.model.AlertaEficiencia;
import br.com.fiap.esg.model.StatusAlerta;
import br.com.fiap.esg.repository.AlertaEficienciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaEficienciaService {

    private final AlertaEficienciaRepository alertaRepository;

    @Transactional(readOnly = true)
    public List<AlertaEficienciaResponseDTO> listarAlertas(StatusAlerta status) {
        List<AlertaEficiencia> alertas = (status != null)
                ? alertaRepository.findByStatusAlertaOrderByDataGeracaoDesc(status)
                : alertaRepository.findAll();

        return alertas.stream()
                .map(AlertaEficienciaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public AlertaEficienciaResponseDTO resolverAlerta(Long id, ResolucaoAlertaRequestDTO dto) {
        AlertaEficiencia alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta de eficiencia nao encontrado com ID: " + id));

        if (alerta.getStatusAlerta() == StatusAlerta.RESOLVIDO) {
            throw new BusinessRuleException("Este alerta ja se encontra com status RESOLVIDO.");
        }

        alerta.setStatusAlerta(StatusAlerta.RESOLVIDO);
        alerta.setAcaoAutomaticaExecutada(dto.acaoExecutada());
        alerta.setDataResolucao(LocalDateTime.now());

        return AlertaEficienciaResponseDTO.fromEntity(alertaRepository.save(alerta));
    }
}
