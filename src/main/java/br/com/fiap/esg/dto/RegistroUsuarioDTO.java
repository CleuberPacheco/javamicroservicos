package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.RoleUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioDTO(
        @NotBlank(message = "O nome do usuario e obrigatorio")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String nome,

        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "Formato de e-mail invalido")
        String email,

        @NotBlank(message = "A senha e obrigatoria")
        @Size(min = 6, max = 60, message = "A senha deve ter no minimo 6 caracteres")
        String senha,

        RoleUsuario role
) {
}
