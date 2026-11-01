package br.com.fiap.esg.service;

import br.com.fiap.esg.dto.SetorRequestDTO;
import br.com.fiap.esg.dto.SetorResponseDTO;
import br.com.fiap.esg.exception.BusinessRuleException;
import br.com.fiap.esg.exception.ResourceNotFoundException;
import br.com.fiap.esg.model.Setor;
import br.com.fiap.esg.repository.SetorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SetorService {

    private final SetorRepository setorRepository;

    @Transactional
    public SetorResponseDTO cadastrar(SetorRequestDTO dto) {
        validarLimites(dto);

        Setor setor = Setor.builder()
                .nome(dto.nome())
                .localizacao(dto.localizacao())
                .responsavel(dto.responsavel())
                .limiteMensalKwh(dto.limiteMensalKwh())
                .limiteDiarioKwh(dto.limiteDiarioKwh())
                .ativo(dto.ativo() != null ? dto.ativo() : true)
                .build();

        return SetorResponseDTO.fromEntity(setorRepository.save(setor));
    }

    @Transactional(readOnly = true)
    public Page<SetorResponseDTO> listarTodos(Pageable pageable) {
        return setorRepository.findAll(pageable).map(SetorResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public SetorResponseDTO buscarPorId(Long id) {
        Setor setor = buscarEntidadePorId(id);
        return SetorResponseDTO.fromEntity(setor);
    }

    @Transactional
    public SetorResponseDTO atualizar(Long id, SetorRequestDTO dto) {
        validarLimites(dto);
        Setor setor = buscarEntidadePorId(id);

        setor.setNome(dto.nome());
        setor.setLocalizacao(dto.localizacao());
        setor.setResponsavel(dto.responsavel());
        setor.setLimiteMensalKwh(dto.limiteMensalKwh());
        setor.setLimiteDiarioKwh(dto.limiteDiarioKwh());
        if (dto.ativo() != null) {
            setor.setAtivo(dto.ativo());
        }

        return SetorResponseDTO.fromEntity(setorRepository.save(setor));
    }

    @Transactional
    public void excluir(Long id) {
        Setor setor = buscarEntidadePorId(id);
        setorRepository.delete(setor);
    }

    public Setor buscarEntidadePorId(Long id) {
        return setorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setor nao encontrado com ID: " + id));
    }

    private void validarLimites(SetorRequestDTO dto) {
        if (dto.limiteDiarioKwh().compareTo(dto.limiteMensalKwh()) > 0) {
            throw new BusinessRuleException("O limite diario de consumo (kWh) nao pode ser maior que o limite mensal.");
        }
    }
}
