package br.com.fiap.esg.controller;

import br.com.fiap.esg.dto.SetorRequestDTO;
import br.com.fiap.esg.dto.SetorResponseDTO;
import br.com.fiap.esg.service.SetorService;
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
@RequestMapping("/setores")
@RequiredArgsConstructor
@Tag(name = "2. Setores e Metas de Consumo", description = "Gestao de unidades/setores empresariais e seus limites de consumo energetico (kWh)")
public class SetorController {

    private final SetorService setorService;

    @PostMapping
    @Operation(summary = "Cadastrar novo setor", description = "Registra um setor da empresa com limites diario e mensal de consumo de energia em kWh")
    public ResponseEntity<SetorResponseDTO> cadastrar(@RequestBody @Valid SetorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(setorService.cadastrar(dto));
    }

    @GetMapping
    @Operation(summary = "Listar setores", description = "Lista todos os setores monitorados com paginacao")
    public ResponseEntity<Page<SetorResponseDTO>> listarTodos(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(setorService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar setor por ID", description = "Retorna os detalhes e metas de consumo de um setor especifico")
    public ResponseEntity<SetorResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(setorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar setor", description = "Atualiza dados cadastrais e limites de consumo energetico de um setor existente")
    public ResponseEntity<SetorResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid SetorRequestDTO dto) {
        return ResponseEntity.ok(setorService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir setor", description = "Remove um setor do sistema (exclusivo para perfil ADMIN)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        setorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
