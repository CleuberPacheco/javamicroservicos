package br.com.fiap.esg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolucaoAlertaRequestDTO(
        @NotBlank(message = "A descricao da acao corretiva executada e obrigatoria")
        @Size(min = 5, max = 255, message = "A acao corretiva deve ter entre 5 e 255 caracteres")
        String acaoExecutada
) {
}
