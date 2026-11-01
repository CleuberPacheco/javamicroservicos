package br.com.fiap.esg.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record CampoErroDTO(String campo, String mensagem) {
    }

    public record ErroPadraoDTO(
            LocalDateTime timestamp,
            int status,
            String erro,
            String mensagem,
            String caminho,
            List<CampoErroDTO> validacoes
    ) {
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErroPadraoDTO> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        ErroPadraoDTO body = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Recurso Nao Encontrado",
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErroPadraoDTO> handleBusinessRule(
            BusinessRuleException ex,
            HttpServletRequest request) {
        ErroPadraoDTO body = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Violacao de Regra de Negocio",
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroPadraoDTO> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<CampoErroDTO> campos = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new CampoErroDTO(error.getField(), error.getDefaultMessage()))
                .toList();

        ErroPadraoDTO body = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Validacao nos Dados de Entrada",
                "Um ou mais campos estao invalidos. Verifique os detalhes.",
                request.getRequestURI(),
                campos
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroPadraoDTO> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {
        ErroPadraoDTO body = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciais Invalidas",
                "E-mail ou senha incorretos.",
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroPadraoDTO> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        ErroPadraoDTO body = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Acesso Negado",
                "Voce nao possui permissao para executar esta operacao.",
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroPadraoDTO> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        ErroPadraoDTO body = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflito de Integridade de Dados",
                "Operacao violou uma restricao de unicidade ou chave estrangeira no banco de dados.",
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroPadraoDTO> handleNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        ErroPadraoDTO body = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Corpo da Requisicao Invalido",
                "Verifique a formatacao do JSON enviado e os valores dos enumeradores.",
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroPadraoDTO> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        ErroPadraoDTO body = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno no Servidor",
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
