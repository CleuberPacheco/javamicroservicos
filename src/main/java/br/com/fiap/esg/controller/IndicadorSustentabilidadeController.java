package br.com.fiap.esg.controller;

import br.com.fiap.esg.dto.IndicadorSustentabilidadeDTO;
import br.com.fiap.esg.service.IndicadorSustentabilidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/indicadores-sustentabilidade")
@RequiredArgsConstructor
@Tag(name = "6. Indicadores ESG de Sustentabilidade", description = "Painel consolidado de consumo energetico, pegada de carbono equivalente e economia via IoT")
public class IndicadorSustentabilidadeController {

    private final IndicadorSustentabilidadeService indicadorService;

    @GetMapping
    @Operation(
            summary = "Obter indicadores gerais de eficiencia energetica e ESG",
            description = "Retorna consumo total (kWh), emissoes estimadas (kgCO2), total de desligamentos automaticos IoT, economia estimada e conformidade por setor"
    )
    public ResponseEntity<IndicadorSustentabilidadeDTO> obterIndicadores() {
        return ResponseEntity.ok(indicadorService.obterIndicadoresConsolidados());
    }
}
