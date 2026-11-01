package br.com.fiap.esg.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_SETOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Setor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_setor")
    @SequenceGenerator(name = "seq_setor", sequenceName = "SEQ_TB_SETOR", allocationSize = 1)
    @Column(name = "ID_SETOR")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    @Column(name = "LOCALIZACAO", nullable = false, length = 150)
    private String localizacao;

    @Column(name = "RESPONSAVEL", nullable = false, length = 100)
    private String responsavel;

    @Column(name = "LIMITE_MENSAL_KWH", nullable = false, precision = 12, scale = 2)
    private BigDecimal limiteMensalKwh;

    @Column(name = "LIMITE_DIARIO_KWH", nullable = false, precision = 12, scale = 2)
    private BigDecimal limiteDiarioKwh;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo;

    @Column(name = "DATA_CADASTRO", nullable = false)
    private LocalDateTime dataCadastro;

    @PrePersist
    public void prePersist() {
        if (this.ativo == null) {
            this.ativo = true;
        }
        if (this.dataCadastro == null) {
            this.dataCadastro = LocalDateTime.now();
        }
    }
}
