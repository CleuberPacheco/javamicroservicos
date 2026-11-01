package br.com.fiap.esg.controller;

import br.com.fiap.esg.dto.LeituraConsumoRequestDTO;
import br.com.fiap.esg.dto.LeituraConsumoResponseDTO;
import br.com.fiap.esg.service.LeituraConsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leituras-consumo")
@RequiredArgsConstructor
@Tag(name = "4. Monitoramento de Consumo Energetico", description = "Recebimento de telemetria IoT, geracao automatica de alertas de excesso e desligamento por ociosidade")
public class LeituraConsumoController {

    private final LeituraConsumoService leituraService;

    @PostMapping
    @Operation(
            summary = "Registrar leitura de consumo energetico",
            description = "Registra o consumo em kWh e o tempo ocioso. Dispara alerta automatico se ultrapassar o limite e desliga automaticamente o equipamento se ocioso >= 30 min."
    )
    public ResponseEntity<LeituraConsumoResponseDTO> registrar(@RequestBody @Valid LeituraConsumoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leituraService.registrarLeitura(dto));
    }

    @GetMapping
    @Operation(summary = "Listar historico de leituras", description = "Retorna todas as leituras de consumo energetico de forma paginada")
    public ResponseEntity<Page<LeituraConsumoResponseDTO>> listarTodas(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(leituraService.listarTodas(pageable));
    }

    @GetMapping("/setor/{setorId}")
    @Operation(summary = "Consultar historico de consumo por setor", description = "Lista todas as leituras registradas para os equipamentos de um setor especifico")
    public ResponseEntity<List<LeituraConsumoResponseDTO>> listarPorSetor(@PathVariable Long setorId) {
        return ResponseEntity.ok(leituraService.listarPorSetor(setorId));
    }
}
