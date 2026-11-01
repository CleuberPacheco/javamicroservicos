package br.com.fiap.esg.controller;

import br.com.fiap.esg.dto.AlertaEficienciaResponseDTO;
import br.com.fiap.esg.dto.ResolucaoAlertaRequestDTO;
import br.com.fiap.esg.model.StatusAlerta;
import br.com.fiap.esg.service.AlertaEficienciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alertas")
@RequiredArgsConstructor
@Tag(name = "5. Alertas Automaticos de Consumo", description = "Consulta e tratativa de alertas gerados por excesso de consumo ou ociosidade IoT")
public class AlertaEficienciaController {

    private final AlertaEficienciaService alertaService;

    @GetMapping
    @Operation(summary = "Listar alertas de eficiencia energetica", description = "Permite listar todos os alertas ou filtrar por status (ABERTO, RESOLVIDO)")
    public ResponseEntity<List<AlertaEficienciaResponseDTO>> listar(
            @RequestParam(required = false) StatusAlerta status) {
        return ResponseEntity.ok(alertaService.listarAlertas(status));
    }

    @PutMapping("/{id}/resolver")
    @Operation(summary = "Resolver alerta de consumo", description = "Registra a acao de mitigacao/correcao adotada e altera o status do alerta para RESOLVIDO")
    public ResponseEntity<AlertaEficienciaResponseDTO> resolver(
            @PathVariable Long id,
            @RequestBody @Valid ResolucaoAlertaRequestDTO dto) {
        return ResponseEntity.ok(alertaService.resolverAlerta(id, dto));
    }
}
