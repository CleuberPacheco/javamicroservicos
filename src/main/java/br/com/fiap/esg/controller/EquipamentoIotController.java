package br.com.fiap.esg.controller;

import br.com.fiap.esg.dto.EquipamentoIotRequestDTO;
import br.com.fiap.esg.dto.EquipamentoIotResponseDTO;
import br.com.fiap.esg.service.EquipamentoIotService;
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

@RestController
@RequestMapping("/equipamentos")
@RequiredArgsConstructor
@Tag(name = "3. Equipamentos e Sensores IoT", description = "Integracao com sensores IoT e controle de desligamento automatico de equipamentos ociosos")
public class EquipamentoIotController {

    private final EquipamentoIotService equipamentoService;

    @PostMapping
    @Operation(summary = "Cadastrar equipamento/sensor IoT", description = "Vincula um equipamento com sensor IoT a um setor e define seu limite horario de consumo")
    public ResponseEntity<EquipamentoIotResponseDTO> cadastrar(@RequestBody @Valid EquipamentoIotRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipamentoService.cadastrar(dto));
    }

    @GetMapping
    @Operation(summary = "Listar equipamentos IoT", description = "Retorna a lista paginada de equipamentos monitorados e seu status operacional atual")
    public ResponseEntity<Page<EquipamentoIotResponseDTO>> listarTodos(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(equipamentoService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar equipamento IoT por ID", description = "Detalha um equipamento especifico e sua configuracao de desligamento automatico")
    public ResponseEntity<EquipamentoIotResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(equipamentoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar equipamento IoT", description = "Atualiza parametros de potencia, limite de consumo e status de um equipamento")
    public ResponseEntity<EquipamentoIotResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid EquipamentoIotRequestDTO dto) {
        return ResponseEntity.ok(equipamentoService.atualizar(id, dto));
    }

    @PutMapping("/{id}/desligar-ocioso")
    @Operation(summary = "Desligar equipamento ocioso via IoT", description = "Envia comando ao atuador IoT para desligar imediatamente um equipamento ocioso e registra o evento de economia")
    public ResponseEntity<EquipamentoIotResponseDTO> desligarEquipamentoOcioso(@PathVariable Long id) {
        return ResponseEntity.ok(equipamentoService.desligarEquipamentoOcioso(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover equipamento IoT", description = "Remove o cadastro de um equipamento IoT (exclusivo para perfil ADMIN)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        equipamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
