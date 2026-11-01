package br.com.fiap.esg.controller;

import br.com.fiap.esg.dto.LoginRequestDTO;
import br.com.fiap.esg.dto.RegistroUsuarioDTO;
import br.com.fiap.esg.dto.TokenResponseDTO;
import br.com.fiap.esg.exception.BusinessRuleException;
import br.com.fiap.esg.model.RoleUsuario;
import br.com.fiap.esg.model.Usuario;
import br.com.fiap.esg.repository.UsuarioRepository;
import br.com.fiap.esg.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "1. Autenticacao", description = "Endpoints de login e cadastro de usuarios para emissao de Token JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario", description = "Valida as credenciais (e-mail e senha) e retorna um token JWT Bearer")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        UsernamePasswordAuthenticationToken credentials =
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());
        Authentication auth = authenticationManager.authenticate(credentials);

        Usuario usuario = (Usuario) auth.getPrincipal();
        String token = tokenService.gerarToken(usuario);

        return ResponseEntity.ok(new TokenResponseDTO(
                token,
                "Bearer",
                usuario.getEmail(),
                usuario.getRole().name()
        ));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuario", description = "Cadastra um novo usuario com senha criptografada (BCrypt) e retorna o token JWT")
    public ResponseEntity<TokenResponseDTO> registrar(@RequestBody @Valid RegistroUsuarioDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new BusinessRuleException("Ja existe um usuario cadastrado com o e-mail informado.");
        }

        Usuario novoUsuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(passwordEncoder.encode(dto.senha()))
                .role(dto.role() != null ? dto.role() : RoleUsuario.OPERADOR)
                .build();

        Usuario salvo = usuarioRepository.save(novoUsuario);
        String token = tokenService.gerarToken(salvo);

        return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponseDTO(
                token,
                "Bearer",
                salvo.getEmail(),
                salvo.getRole().name()
        ));
    }
}
